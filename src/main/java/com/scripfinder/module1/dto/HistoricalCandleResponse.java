package com.scripfinder.module1.dto;

import com.google.gson.annotations.SerializedName;

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
public class HistoricalCandleResponse {

    private String status;
    
    @SerializedName("data")
    private CandleData data;
}
