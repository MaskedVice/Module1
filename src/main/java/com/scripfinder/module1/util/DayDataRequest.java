package com.scripfinder.module1.util;

import com.scripfinder.module1.dto.Candle;

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
public class DayDataRequest {
    private String scripName;
    private Candle candle;

}
