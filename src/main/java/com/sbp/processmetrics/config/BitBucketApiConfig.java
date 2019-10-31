package com.sbp.processmetrics.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class BitBucketApiConfig {

    @Value("${repository.name}")
    private String repositoryName;

    @Value("${main.project.path}")
    private String mainProjectPath;

    @Value("${bitBucket.username}")
    private String bitBucketUsername;

    @Value("${bitBucket.api.url}")
    private String bitBucketApiUrl;

}
