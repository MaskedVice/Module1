package com.scripfinder.module1.Schedulers;

import java.net.URLEncoder;
import java.time.Duration;

import javax.annotation.PostConstruct;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import com.scripfinder.module1.config.UpstoxProperties;
import com.scripfinder.module1.util.GenerateTotp;

@EnableAsync
@Component
public class ScheduledAutoLogin {
    
    private static String uri = "https://api-v2.upstox.com/v2/login/authorization/dialog?response_type=code&client_id={1}&redirect_uri={2}";
    
    private static final Duration maxWaitTimeInSeconds = Duration.ofSeconds(10);
    
    private UpstoxProperties upstoxProperties;

    private GenerateTotp generateTotp;


    @Autowired
    public ScheduledAutoLogin(UpstoxProperties upstoxProperties,GenerateTotp generateTotp) {
        this.upstoxProperties = upstoxProperties;
        this.generateTotp = generateTotp;
    }

    @PostConstruct
    protected void onStartup() {
        run();
    }
    @Async
    @Scheduled(cron = "0 30 3 * * ?",zone = "Asia/Kolkata") // Everyday at 3:30 AM
    protected void dailyTask() {
        run();
    }

    private void run() {
        try {
            createAuthorizationToken();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void createAuthorizationToken() throws Exception {
        uri = uri.replace("{1}", upstoxProperties.getApiKey()).replace("{2}", URLEncoder.encode(upstoxProperties.getRedirectUrl(), "UTF-8"));
        String authorizationToken = createAuthorizationToken(uri);
        if (authorizationToken != null) {
            upstoxProperties.setAccessToken(authorizationToken);
        }
    }

    private String createAuthorizationToken(String url) {

        
        MultiValueMap<String, String> parameters;
            
        try {
            EdgeOptions op=new EdgeOptions();
            //op.addArguments("headless");
            WebDriver driver = new EdgeDriver(op);
            WebDriverWait wait = new WebDriverWait(driver, maxWaitTimeInSeconds);
            System.setProperty("webdriver.edge.driver,", "classpath:msedgedriver.exe");

            driver.get(url);
            
            //Add Mobile Number
            WebElement mobileNumberInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("mobileNum")));
            mobileNumberInput.sendKeys(upstoxProperties.getPhoneNumber());
            mobileNumberInput.submit();

            //Add TOTP
            String code = generateTotp.getCurTOTP();
            WebElement TOTPInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("otpNum")));
            TOTPInput.sendKeys(code);
            TOTPInput.submit();

            //Add Password
            WebElement passwordInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("pinCode")));
            passwordInput.sendKeys(upstoxProperties.getPin());
            passwordInput.submit();


            Thread.sleep(2000);
            String currentUrl = driver.getCurrentUrl();
            parameters = UriComponentsBuilder.fromUriString(currentUrl).build().getQueryParams();
            driver.quit();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
        return parameters.get("code").get(0);
    }
}
