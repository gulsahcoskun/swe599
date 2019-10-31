package com.sbp.processmetrics.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class VersionData {

    private int version;
    private List<ClassData> classDataList;

}
