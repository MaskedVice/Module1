package com.scripfinder.module1.service;

import java.util.Map;
import com.scripfinder.module1.dto.InstrumentData;


public interface SaveCurrentDataService {
    Runnable saveCurrentData(Map<String, InstrumentData> batch);
}