package com.scripfinder.module1.Schedulers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.scripfinder.module1.config.UpstoxProperties;
import com.scripfinder.module1.dto.Candle;
import com.scripfinder.module1.dto.CurrentDataResponse;
import com.scripfinder.module1.dto.HistoricalCandleResponse;
import com.scripfinder.module1.dto.InstrumentData;
import com.scripfinder.module1.service.SaveCurrentDataService;
import com.scripfinder.module1.service.SaveMonthDataService;

@Component
@EnableAsync
public class ScheduledGetMarketData {

    private static final String historicalDataUrl = "https://api-v2.upstox.com/v2/historical-candle/";
    private static final String currentDataUrl = "https://api.upstox.com/v2/market-quote/quotes/";
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private List<String> symbolList;
    private UpstoxProperties upstoxProperties;
    private SaveMonthDataService saveMonthDataService;
    private SaveCurrentDataService saveCurrentDataService;

    private final ExecutorService saveScripDataExecutorService = Executors
                .newFixedThreadPool
                (1000,
                    new ThreadFactoryBuilder()
                    .setNameFormat("Save Historical Data")
                    .setThreadFactory(Executors.defaultThreadFactory())
                    .build()
                );
    private final ExecutorService saveCurrentDataExecutorService = Executors
                .newFixedThreadPool
                (1000,
                    new ThreadFactoryBuilder()
                    .setNameFormat("Save Current Data")
                    .setThreadFactory(Executors.defaultThreadFactory())
                    .build()
                );


    
    @Autowired
    public ScheduledGetMarketData(UpstoxProperties upstoxProperties,SaveMonthDataService saveMonthDataService,SaveCurrentDataService saveCurrentDataService) {
        this.upstoxProperties = upstoxProperties;
        this.saveMonthDataService = saveMonthDataService;
        this.saveCurrentDataService = saveCurrentDataService;
    }

    @Async
    @Scheduled(initialDelay = 10000)
    protected void dailyTask() {
        symbolList = upstoxProperties.getSymbolList();
        //historicalData();
        currentData();
    }

    @Async
    @Scheduled(cron = "0 0 6 * * MON-FRI", zone = "Asia/Kolkata")
    protected void runAt6AMWeekdays() {
        historicalData();
    }

    @Async
    @Scheduled(cron = "0 0/15 9-17 * * MON-FRI", zone = "Asia/Kolkata")
    public void runEvery15MinutesBetween9AMAnd5PM() {
        //currentData();
    }

    protected void currentData() {
        try {
            int batchSize = 490;

            IntStream.range(0, (symbolList.size() + batchSize - 1) / batchSize)
                    .mapToObj(i -> symbolList.subList(i * batchSize, Math.min((i + 1) * batchSize, symbolList.size())))
                    .forEach(batch -> {
                        try {
                            String curBatch = batch.stream()
                                .map(s -> s.split(",")[0])
                                .collect(Collectors.joining(","));
                            Map<String,InstrumentData> candles = getCandle(curBatch);
                            if(candles != null) {
                                CompletableFuture<Void> result = CompletableFuture.runAsync(saveCurrentDataService.saveCurrentData(candles), saveCurrentDataExecutorService);
                                result.get(10, TimeUnit.SECONDS);
                            }
                        } catch (InterruptedException | ExecutionException | TimeoutException e) {
                            System.out.println("Error: " + e.getMessage());
                        }    
                    });
            System.out.println("ALL DONE");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private Map<String,InstrumentData> getCandle(String batch) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(currentDataUrl + "?instrument_key=" + batch))
                .header("Api-Version", "2.0")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + upstoxProperties.getAccessToken())
                .GET()
                .build();
            
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            Gson gson = new Gson();
            CurrentDataResponse cr = gson.fromJson(response.body(), CurrentDataResponse.class);
            return cr.getData();
        }
        System.out.println("Error: " + response.body());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        return null;
    }


protected void historicalData() {
    try {
        String todayStr = formatDate(LocalDate.parse(upstoxProperties.getCurrentDateTime(), formatter).minusDays(1));
        String oneMonthAgoStr = formatDate(LocalDate.parse(upstoxProperties.getCurrentDateTime(), formatter).minusYears(1));
        
        symbolList.parallelStream().forEach(x -> {
            try {
                String[] instrumentKeyNameArray = x.split(",");
                List<Candle> candles = getCandles(instrumentKeyNameArray[0], todayStr, oneMonthAgoStr);
                System.out.println(instrumentKeyNameArray[1] + " : " + candles.size());
                if (candles != null && candles.size() > 150) {
                    CompletableFuture<Void> result = CompletableFuture.runAsync(saveMonthDataService.saveMonthData(instrumentKeyNameArray[0].substring(instrumentKeyNameArray[0].indexOf("%7C")+3), candles), saveScripDataExecutorService);
                    result.get();
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        });
        System.out.println("ALL DONE");
    } catch (Exception e) {
        System.out.println("Error: " + e.getMessage());
    }
}

    private List<Candle> getCandles(String insKey, String todayStr, String oneMonthAgoStr) throws IOException, InterruptedException, URISyntaxException {
        HttpClient httpClient = HttpClient.newHttpClient();
        String curUrl = historicalDataUrl + insKey +"/day" + "/" + todayStr + "/" + oneMonthAgoStr;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(curUrl))
                .header("Api-Version", "2.0")
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {

            Gson gson = new Gson();
            HistoricalCandleResponse candleResponse = gson.fromJson(response.body(), HistoricalCandleResponse.class);
            List<Candle> candles =convertToCandles(candleResponse.getData().getCandles());
            return candles;
        } else {
            // Handle the case where the API response indicates failure
            System.out.println("API call failed. Status: " + response.statusCode());
            return null; // Adjust the return type accordingly
        }
    }

    private static List<Candle> convertToCandles(List<List<Object>> candlesData) {
        return candlesData.stream()
                .map(candleData -> new Candle(
                        (String) candleData.get(0),
                        (double) candleData.get(1),
                        (double) candleData.get(2),
                        (double) candleData.get(3),
                        (double) candleData.get(4),
                        (double) candleData.get(5),
                        (double) candleData.get(6)
                ))
                .collect(Collectors.toList());
    }


    private static String formatDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return date.format(formatter);
    }
    
}
