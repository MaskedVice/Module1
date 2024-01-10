package com.scripfinder.module1.service;

import java.util.Map;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.scripfinder.module1.dto.CurrentDataRequest;
import com.scripfinder.module1.dto.InstrumentData;
import com.scripfinder.module1.dto.ResponseObject;

@Service
public class SaveCurrentDataServiceImpl implements SaveCurrentDataService {

    private final String BASE_URL = "http://localhost:8085";

    @Override
    public Runnable saveCurrentData(Map<String, InstrumentData> batch) {
        return() -> {
            try{
                RestTemplate restTemplate = new RestTemplate();
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                CurrentDataRequest d = new CurrentDataRequest(batch);
                HttpEntity<CurrentDataRequest> requestEntity = new HttpEntity<>(d, headers);

                ResponseEntity<ResponseObject> responseEntity = restTemplate.exchange(
                        BASE_URL + "/save/saveCurrentData",
                        HttpMethod.POST,
                        requestEntity,
                        ResponseObject.class
                );
                if(responseEntity.getBody().getResponseCode().equals(200)){
                    System.out.println(responseEntity.getBody().getResponseResult());
                } else{
                    System.out.println("Failed insert");
                }
            }catch(Exception e){
                System.out.println("Error: " + e.getMessage());
            }
        };
    }
}
