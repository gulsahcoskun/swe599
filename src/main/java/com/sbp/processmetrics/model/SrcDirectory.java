package com.sbp.processmetrics.model;

import lombok.Data;


@Data
public class SrcDirectory {

    private String path;
    private SrcCommit commit;
    private String type;
    private SrcLink links;
}
