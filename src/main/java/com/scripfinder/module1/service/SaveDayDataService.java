package com.scripfinder.module1.service;

import java.util.List;
import java.util.function.Supplier;

import com.scripfinder.module1.dto.Candle;

public interface SaveDayDataService {
    Supplier<String> saveDayData(String scripName, Candle candles);
}