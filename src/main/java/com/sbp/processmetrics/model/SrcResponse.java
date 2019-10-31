package com.sbp.processmetrics.model;

import lombok.Data;

import java.util.List;

@Data
public class SrcResponse {

    private String pageLen;
    private List<SrcDirectory> values;
    private String page;


}
