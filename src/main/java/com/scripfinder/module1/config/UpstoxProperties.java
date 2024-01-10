package com.scripfinder.module1.config;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public final class UpstoxProperties {
    private final String apiKey;
    private final String secretKey;
    private final String redirectUrl;
    private final String totpKey;
    private final String pin;
    private final String phoneNumber;
    private String accessToken;
    private String authorizationToken;
    private List<String> symbolList;
    private final SimpleDateFormat sd = new SimpleDateFormat(
        "yyyy-MM-dd'T'HH:mm:ss");

    @Autowired
    public UpstoxProperties(@Value("${upstox.apikey}") String apiKey, 
                            @Value("${upstox.secretkey}") String secretKey, 
                            @Value("${upstox.redirectUrl}") String redirectUrl, 
                            @Value("${upstox.TOTPKey}") String totpKey,
                            @Value("${upstox.phoneNumber}") String phoneNumber,
                            @Value("${upstox.pin}") String pin) {
        this.apiKey = apiKey;
        this.secretKey = secretKey;
        this.redirectUrl = redirectUrl;
        this.totpKey = totpKey;
        this.phoneNumber = phoneNumber;
        this.pin = pin;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getTotpKey() {
        return totpKey;
    }

    public String getPin() {
        return pin;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setAuthorizationToken(String authorizationToken) {
        this.authorizationToken = authorizationToken;
    }

    public String getAuthorizationToken() {
        return authorizationToken;
    }

    public void setAccessToken(String accessToken){
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setSymbolList(List<String> symbolList) {
        this.symbolList = symbolList;
    }
    
    public List<String> getSymbolList(){
        return symbolList;
    }
    public String getCurrentDateTime(){
        Date date = new Date();
        sd.setTimeZone(TimeZone.getTimeZone("IST"));
        return sd.format(date);
    }
}
