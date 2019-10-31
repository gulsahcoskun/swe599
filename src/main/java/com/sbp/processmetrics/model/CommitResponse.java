package com.sbp.processmetrics.model;

import lombok.Data;

import java.util.List;

@Data
public class CommitResponse {

    private long pagelen;
    private List<CommitValue> values;
    private String next;

}
