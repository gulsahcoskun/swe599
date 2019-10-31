package com.sbp.processmetrics.conroller;

import com.sbp.processmetrics.service.MetricExtractionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MetricExtractionController {

    private final MetricExtractionService metricExtractionService;

    private MetricExtractionController(MetricExtractionService metricExtractionService){
        this.metricExtractionService = metricExtractionService;
    }


    @GetMapping("/extract")
    public ResponseEntity getHashOfSrc(@RequestHeader("token") String token,
                                       @RequestParam("period") long period){
        return ResponseEntity.ok(metricExtractionService.findProcessMetrics(token,period));
    }

}
