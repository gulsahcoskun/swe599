package com.sbp.processmetrics.model.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class MetricExtractionResponse {

    private List<VersionData> versionDataList;

}
