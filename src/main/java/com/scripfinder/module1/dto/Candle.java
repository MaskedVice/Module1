package com.scripfinder.module1.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Candle {

    private String timestamp;

    private double open;

    private double high;

    private double low;

    private double close;

    private double volume;
    
    private double openInterest;

}
