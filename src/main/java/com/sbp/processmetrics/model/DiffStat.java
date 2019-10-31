package com.sbp.processmetrics.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DiffStat {

    private String status;
    private long lines_removed;
    private long lines_added;

    @JsonProperty("new")
    private DiffNew newStat;

}
