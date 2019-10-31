package com.sbp.processmetrics.model;

import lombok.Data;

import java.util.List;

@Data
public class DiffStatResponse {

    private long pagelen;
    private List<DiffStat> values;
    private long page;
    private long size;


}
