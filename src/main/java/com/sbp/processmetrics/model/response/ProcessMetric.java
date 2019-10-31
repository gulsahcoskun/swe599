package com.sbp.processmetrics.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcessMetric {

    private long numberOfRevisions;
    private long numberOfChangedLines;
    private long numberOfDistinctDevelopers;
    private long numberOfDefects;
    private boolean defective;

    @JsonIgnore
    private List<String> developers;

}
