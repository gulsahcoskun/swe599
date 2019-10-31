package com.sbp.processmetrics.client;

import com.sbp.processmetrics.config.BitBucketApiConfig;
import com.sbp.processmetrics.model.*;
import com.sbp.processmetrics.model.response.ClassData;
import com.sbp.processmetrics.model.response.ProcessMetric;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class BitBucketClient {

    private final BitBucketApiConfig apiConfig;
    private final RestTemplate restTemplate;

    public BitBucketClient(BitBucketApiConfig apiConfig, RestTemplate restTemplate) {
        this.apiConfig = apiConfig;
        this.restTemplate = restTemplate;
    }

    public String getHashOfSrc(String token) {
        HttpEntity entity = getHttpEntity(token);
        StringBuilder url = new StringBuilder();
        url.append(apiConfig.getBitBucketApiUrl())
                .append(apiConfig.getBitBucketUsername())
                .append("/")
                .append(apiConfig.getRepositoryName())
                .append("/src/");

        ResponseEntity<SrcResponse> response = restTemplate.exchange(url.toString(), HttpMethod.GET, entity, SrcResponse.class);

        return response.getBody().getValues().stream()
                .filter(v -> v.getPath().equalsIgnoreCase("src"))
                .findFirst().get()
                .getCommit()
                .getHash();
    }

    public List<ClassData> getAllClassesOfRepository(String token, String hashValue) {
        HttpEntity entity = getHttpEntity(token);
        StringBuilder url = new StringBuilder();
        url.append(apiConfig.getBitBucketApiUrl())
                .append(apiConfig.getBitBucketUsername())
                .append("/")
                .append(apiConfig.getRepositoryName())
                .append("/src/")
                .append(hashValue)
                .append("/")
                .append(apiConfig.getMainProjectPath());

        ResponseEntity<SrcResponse> response = restTemplate.exchange(url.toString(), HttpMethod.GET, entity, SrcResponse.class);

        List<ClassData> classData = new ArrayList<>();
        return retrieveClasses(token, response, classData);
    }

    private List<ClassData> retrieveClasses(String token, ResponseEntity<SrcResponse> response, List<ClassData> classData) {
        for (SrcDirectory directory : response.getBody().getValues()) {
            if (directory.getType().equalsIgnoreCase("commit_directory")) {
                getAllClassesOfDirectory(token, directory.getLinks().getSelf().getHref(), classData);
            } else {
                classData.add(ClassData.builder().className(directory.getPath()).processMetric(ProcessMetric.builder().build()).build());
                log.info("new class added {}", directory.getPath());
            }
        }
        return classData;
    }

    public List<ClassData> getAllClassesOfDirectory(String token, String url, List<ClassData> classData) {
        HttpEntity entity = getHttpEntity(token);
        url = url.replace("!", "");
        ResponseEntity<SrcResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity, SrcResponse.class);

        return retrieveClasses(token, response, classData);
    }


    public List<CommitValue> getAllCommitsOfRepository(String token) {
        HttpEntity entity = getHttpEntity(token);
        StringBuilder url = new StringBuilder();
        url.append(apiConfig.getBitBucketApiUrl())
                .append(apiConfig.getBitBucketUsername())
                .append("/")
                .append(apiConfig.getRepositoryName())
                .append("/commits/");

        ResponseEntity<CommitResponse> response = restTemplate.exchange(url.toString(), HttpMethod.GET, entity, CommitResponse.class);
        List<CommitValue> commitValues = collectCommits(entity, response.getBody().getNext(), response.getBody().getValues());

        log.info("RESPONSE: {}", commitValues);
        return commitValues;
    }

    private List<CommitValue> collectCommits(HttpEntity entity, String nextUrl, List<CommitValue> values) {
        if (nextUrl != null && !nextUrl.isEmpty()) {
            ResponseEntity<CommitResponse> response = restTemplate.exchange(nextUrl, HttpMethod.GET, entity, CommitResponse.class);
            values.addAll(response.getBody().getValues());
            collectCommits(entity, response.getBody().getNext(), values);
        }
        return values;
    }


    public DiffStatResponse getDiffStatOfCommit(String token, String commitHash) {
        HttpEntity entity = getHttpEntity(token);
        StringBuilder url = new StringBuilder();
        url.append(apiConfig.getBitBucketApiUrl())
                .append(apiConfig.getBitBucketUsername())
                .append("/")
                .append(apiConfig.getRepositoryName())
                .append("/diffstat/")
                .append(commitHash);

        ResponseEntity<DiffStatResponse> response = restTemplate.exchange(url.toString(), HttpMethod.GET, entity, DiffStatResponse.class);
        return response.getBody();
    }

    private HttpEntity getHttpEntity(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);

        return new HttpEntity(headers);
    }

}
