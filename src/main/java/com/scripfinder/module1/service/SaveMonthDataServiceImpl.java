package com.scripfinder.module1.service;

import java.util.List;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.scripfinder.module1.dto.Candle;
import com.scripfinder.module1.dto.HistoricalDataRequest;
import com.scripfinder.module1.dto.ResponseObject;
@Service
public class SaveMonthDataServiceImpl implements SaveMonthDataService {

    private final String BASE_URL = "http://localhost:8085";

    @Override
    public Runnable saveMonthData(String scripName, List<Candle> candles) {
        return () -> {
            try {

                RestTemplate restTemplate = new RestTemplate();
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HistoricalDataRequest hD = new HistoricalDataRequest(scripName, candles);
                HttpEntity<HistoricalDataRequest> requestEntity = new HttpEntity<>(hD, headers);

                ResponseEntity<ResponseObject> responseEntity = restTemplate.exchange(
                        BASE_URL + "/save/saveMonthData",
                        HttpMethod.POST,
                        requestEntity,
                        ResponseObject.class
                );

                if(responseEntity.getBody().getResponseCode().equals(200)){
                    System.out.println("Saved Successfully");
                } else{
                    System.out.println("Failed insert for :" + scripName);
                }
            } catch(Exception e) {

                System.out.println("Error: " + e.getMessage());
            }
        };
    }

}
