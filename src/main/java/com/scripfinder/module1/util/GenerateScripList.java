package com.scripfinder.module1.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.scripfinder.module1.config.UpstoxProperties;

import jakarta.annotation.PostConstruct;

@Component
@EnableAsync
public class GenerateScripList {

    private UpstoxProperties upstoxProperties;
    String fileUrl = "https://assets.upstox.com/market-quote/instruments/exchange/NSE.csv.gz";

    @Autowired
    public GenerateScripList(UpstoxProperties upstoxProperties) {
        this.upstoxProperties = upstoxProperties;
    }


    @PostConstruct
    protected void onStartup() throws IOException {
        List<String> res = readAndUnzipFile(fileUrl);
        upstoxProperties.setSymbolList(res);
    }
    @Async
    @Scheduled(cron = "0 15 5 * * ?") // Everyday at 5:15 AM
    protected void dailyTask() throws IOException {
         List<String> res = readAndUnzipFile(fileUrl);
         upstoxProperties.setSymbolList(res);
    }


    private static List<String> readAndUnzipFile(String gzippedFilePath) throws IOException {
        List<String> list= new ArrayList<>();
        URL url = new URL(gzippedFilePath);
        try (InputStream inputStream = url.openStream();
            GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream);
            InputStreamReader isr = new InputStreamReader(gzipInputStream);
            BufferedReader br = new BufferedReader(isr)) {

            String line;
            while ((line = br.readLine()) != null) {
                if(line.contains("NSE_EQ")){
                    String[] cur = line.split(",");
                    list.add(cur[0].replace("|", "%7C").replace("\"", "") + "," + cur[2].replace("\"", ""));
                } 
            }
        }
        return list;
    }
}
