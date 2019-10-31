package com.sbp.processmetrics.service;

import com.sbp.processmetrics.model.response.MetricExtractionResponse;

public interface MetricExtractionService {

    MetricExtractionResponse findProcessMetrics(String token, long period);

}
