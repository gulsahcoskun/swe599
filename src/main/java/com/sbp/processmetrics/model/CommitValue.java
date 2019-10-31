package com.sbp.processmetrics.model;

import lombok.Data;


@Data
public class CommitValue {

    private String hash;
    private CommitAuthor author;
    private String date;
    private String message;
    private String type;

}
