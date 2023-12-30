package com.scripfinder.module1.util;

import javax.annotation.PostConstruct;

import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.scripfinder.module1.config.UpstoxProperties;

import de.taimos.totp.TOTP;

@EnableAsync
@Component
public class GenerateTotp {
    private static String TOTPKey;
    private String curTOTP;

    @Autowired
    public GenerateTotp(UpstoxProperties upstoxProperties) {
        TOTPKey = upstoxProperties.getTotpKey();
    }
    
    @PostConstruct
    protected void onStartup() {
        run();
    }
    @Async
    @Scheduled(cron = "0 30 3 * * ?") // Everyday at 3:30 AM
    protected void dailyTask() {
        run();
    }

    private void run() {
        try {
            String code = getTOTPCode(TOTPKey);
            if (!code.equals(curTOTP)) {
                curTOTP = code;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getTOTPCode(String secretKey) {
        Base32 base32 = new Base32();
        byte[] bytes = base32.decode(secretKey);
        String hexKey = Hex.encodeHexString(bytes);
        return TOTP.getOTP(hexKey);
    }

    public String getCurTOTP() {
        return curTOTP;
    }
}
