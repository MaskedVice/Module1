package com.scripfinder.module1.service;

import java.util.List;

import com.scripfinder.module1.dto.Candle;

public interface SaveMonthDataService {
    Runnable saveMonthData(String scripName, List<Candle> candles);
}
