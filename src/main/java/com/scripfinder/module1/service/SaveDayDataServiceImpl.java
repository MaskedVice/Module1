package com.scripfinder.module1.service;

import java.util.List;
import java.util.function.Supplier;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.scripfinder.module1.dto.Candle;
import com.scripfinder.module1.util.DayDataRequest;

public class SaveDayDataServiceImpl implements SaveDayDataService {

    private final String BASE_URL = "http://localhost:8085";

    @Override
    public Supplier<String> saveDayData(String scripName, Candle candle) {
        return() -> {
            try{
                RestTemplate restTemplate = new RestTemplate();

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                DayDataRequest dayDataRequest = new DayDataRequest(scripName, candle);
                HttpEntity<DayDataRequest> requestEntity = new HttpEntity<>(dayDataRequest, headers);

                ResponseEntity<String> responseEntity = restTemplate.exchange(
                        BASE_URL + "/save/saveDayData",
                        HttpMethod.POST,
                        requestEntity,
                        String.class
                );
                if(responseEntity.getStatusCode().equals(HttpStatusCode.valueOf(200))){
                    String response = responseEntity.getBody();
                    return response;
                } else{
                    return responseEntity.getBody();
                }
            }catch(Exception e){
                return e.getMessage();
            }
        };
    }
    
}
