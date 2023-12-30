package com.scripfinder.module1.service;

import java.util.List;
import java.util.function.Supplier;

import com.scripfinder.module1.dto.Candle;

public interface SaveMonthDataService {
    Supplier<String> saveMonthData(String scripName, List<Candle> candles);
}
