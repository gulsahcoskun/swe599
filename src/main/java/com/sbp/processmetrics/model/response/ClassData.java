package com.sbp.processmetrics.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClassData {

    private String className;
    private ProcessMetric processMetric;

}
