package com.scripfinder.module1.Schedulers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.scripfinder.module1.config.UpstoxProperties;
import com.scripfinder.module1.dto.Candle;
import com.scripfinder.module1.dto.CandleResponse;
import com.scripfinder.module1.service.SaveMonthDataService;

import jakarta.annotation.PostConstruct;

@Component
@EnableAsync
public class ScheduledGetMarketData {

    private static final String url = "https://api-v2.upstox.com/v2/historical-candle/";
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private final ExecutorService saveScripDataExecutorService = Executors
                .newFixedThreadPool
                (1,
                    new ThreadFactoryBuilder()
                    .setNameFormat("Save Scrip Data")
                    .setThreadFactory(Executors.defaultThreadFactory())
                    .build()
                );

    private UpstoxProperties upstoxProperties;
    private SaveMonthDataService saveMonthDataService;
    
    @Autowired
    public ScheduledGetMarketData(UpstoxProperties upstoxProperties,SaveMonthDataService saveMonthDataService) {
        this.upstoxProperties = upstoxProperties;
        this.saveMonthDataService = saveMonthDataService;
    }

    @Async
    @Scheduled(initialDelay = 10000)
    protected void dailyTask() {
        historicalData();
        //currentData();
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
            
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    protected void historicalData() {
        try{
            // Format dates as strings
            String todayStr = formatDate(LocalDate.parse(upstoxProperties.getCurrentDateTime(), formatter).minusDays(1)); 
            String oneMonthAgoStr = formatDate(LocalDate.parse(upstoxProperties.getCurrentDateTime(), formatter).minusDays(42));
            List<String> instruments = upstoxProperties.getSymbolList();
            instruments.parallelStream().forEach(x ->{
                try {
                    String[] instrumentKeyNameArray = x.split(",");
                    List<Candle> candles = getCandles(instrumentKeyNameArray[0],todayStr,oneMonthAgoStr);
                    if(candles != null && candles.size() > 25){
                        CompletableFuture<String> result = CompletableFuture.supplyAsync(saveMonthDataService.saveMonthData(instrumentKeyNameArray[1],candles),saveScripDataExecutorService);
                        String reString = result.get(10,TimeUnit.SECONDS);
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
        String curUrl = url + insKey +"/day" + "/" + todayStr + "/" + oneMonthAgoStr;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(curUrl))
                .header("Api-Version", "2.0")
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {

            Gson gson = new Gson();
            CandleResponse candleResponse = gson.fromJson(response.body(), CandleResponse.class);
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
