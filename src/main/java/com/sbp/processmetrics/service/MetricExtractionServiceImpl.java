package com.sbp.processmetrics.service;

import com.sbp.processmetrics.client.BitBucketClient;
import com.sbp.processmetrics.model.CommitValue;
import com.sbp.processmetrics.model.DiffStat;
import com.sbp.processmetrics.model.DiffStatResponse;
import com.sbp.processmetrics.model.response.ClassData;
import com.sbp.processmetrics.model.response.MetricExtractionResponse;
import com.sbp.processmetrics.model.response.ProcessMetric;
import com.sbp.processmetrics.model.response.VersionData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MetricExtractionServiceImpl implements MetricExtractionService {

    private final BitBucketClient bitBucketClient;

    public MetricExtractionServiceImpl(BitBucketClient bitBucketClient) {
        this.bitBucketClient = bitBucketClient;
    }

    @Override
    public MetricExtractionResponse findProcessMetrics(String token, long period) {
        final List<ClassData> classDataList = initializeClasses(token);
        List<CommitValue> commitValues = bitBucketClient.getAllCommitsOfRepository(token);
        return populateClassesWithCommits(token, classDataList, commitValues, period);
    }

    private MetricExtractionResponse populateClassesWithCommits(String token, final List<ClassData> initialClassData,
                                                                List<CommitValue> commitValues, long period) {
        ZonedDateTime startPeriod = getLocalDate(commitValues.get(commitValues.size() - 1).getDate());
        ZonedDateTime endPeriod = startPeriod.plusDays(period);

        List<VersionData> versionDataList = new ArrayList<>();
        int versionCount = 0;

        versionDataList.add(VersionData.builder().version(versionCount).classDataList(getDeepCopyOfClassData(initialClassData)).build());

        List<CommitValue> filteredCommits = commitValues.stream()
                .filter(commitValue -> !commitValue.getMessage().contains("Merge") && !commitValue.getMessage().contains("merge"))
                .collect(Collectors.toList());

        filteredCommits.sort(Comparator.comparing(o -> getLocalDate(o.getDate())));
        VersionData currentVersion = versionDataList.get(0);

        for (CommitValue commit : filteredCommits) {
            ZonedDateTime commitDate = getLocalDate(commit.getDate());

            if ((commitDate.isEqual(startPeriod) || commitDate.isAfter(startPeriod)) && commitDate.isBefore(endPeriod)) {
                currentVersion.setClassDataList(getClassData(token, currentVersion, commit, versionDataList));

                log.info("CLASS DATA set for commit {}", commit);
            } else if (commitDate.isAfter(endPeriod)) {
                versionCount = versionCount + 1;
                VersionData versionData = new VersionData();
                versionData.setVersion(versionCount);

                versionData.setClassDataList(getDeepCopyOfClassData(initialClassData));
                versionDataList.add(versionData);

                currentVersion = versionData;

                currentVersion.setClassDataList(getClassData(token, currentVersion, commit, versionDataList));

                log.info("NEW VERSION created for commit {}", commit);

                startPeriod = endPeriod;
                endPeriod = startPeriod.plusDays(period);
            }
        }

        log.info("Version data list {} ", versionDataList);
        return MetricExtractionResponse.builder().versionDataList(versionDataList).build();
    }


    private List<ClassData> getClassData(String token, VersionData lastVersion, CommitValue commitValue, List<VersionData> versionDataList) {
        List<ClassData> versionClassDataList = lastVersion.getClassDataList();
        DiffStatResponse diffStatResponse = bitBucketClient.getDiffStatOfCommit(token, commitValue.getHash());

        String author = commitValue.getAuthor().getRaw();

        for (DiffStat diff : diffStatResponse.getValues()) {
            if (!diff.getStatus().equalsIgnoreCase("removed")) {
                long modifiedLine = diff.getLines_added() + diff.getLines_removed();
                String classPath = diff.getNewStat().getPath();

                versionClassDataList.stream().filter(c -> c.getClassName().equalsIgnoreCase(classPath))
                        .findFirst().ifPresent(c -> {
                    ProcessMetric processMetric = c.getProcessMetric();
                    processMetric.setNumberOfChangedLines(processMetric.getNumberOfChangedLines() + modifiedLine);
                    processMetric.setNumberOfRevisions(processMetric.getNumberOfRevisions() + 1);

                    List<String> developers = processMetric.getDevelopers();
                    int countOfDevelopers;

                    if (developers != null) {
                        countOfDevelopers = developers.size();
                        Optional<String> optionalDev = developers.stream().filter(d -> d.equalsIgnoreCase(author)).findAny();
                        if (!optionalDev.isPresent()) {
                            developers.add(author);
                            countOfDevelopers++;
                        }
                    } else {
                        countOfDevelopers = 1;
                        developers = new ArrayList<>();
                        developers.add(author);
                    }

                    processMetric.setDevelopers(developers);
                    processMetric.setNumberOfDistinctDevelopers(countOfDevelopers);

                    c.setProcessMetric(processMetric);
                });

                if (commitValue.getMessage().contains("bug") || commitValue.getMessage().contains("fix") ||
                        commitValue.getMessage().contains("conflict") || commitValue.getMessage().contains("defect")) {
                    log.info("Bug found: {} , in version {}, in class {}",commitValue, lastVersion.getVersion(), diff.getNewStat().getPath());
                    int currentVersion = lastVersion.getVersion();
                    if (currentVersion > 0) {
                        assignDefectiveToPreviousVersion(versionDataList.get(currentVersion - 1), diff);
                    }
                }
            }
        }

        return versionClassDataList;
    }

    private void assignDefectiveToPreviousVersion(VersionData versionData, DiffStat diff) {
        versionData.getClassDataList().parallelStream().filter(c -> c.getClassName().equalsIgnoreCase(diff.getNewStat().getPath()))
                .findAny().ifPresent(c -> {
                    ProcessMetric processMetric = c.getProcessMetric();
                    processMetric.setDefective(true);
                    long defectNumber = processMetric.getNumberOfDefects() +1;
                    processMetric.setNumberOfDefects(defectNumber);
                    c.setProcessMetric(processMetric);
        });
    }

    public ZonedDateTime getLocalDate(String dateTimeString) {
        return ZonedDateTime.parse(dateTimeString);
    }

    private List<ClassData> initializeClasses(String token) {
        String hashOfSrc = bitBucketClient.getHashOfSrc(token);
        return bitBucketClient.getAllClassesOfRepository(token, hashOfSrc);
    }

    public List<ClassData> getDeepCopyOfClassData(List<ClassData> classDataToCopy){
        List<ClassData> deepCopy = new ArrayList<>();
        for(ClassData classData : classDataToCopy){
            ProcessMetric processMetric = new ProcessMetric();
            ClassData newClassData = new ClassData();

            newClassData.setProcessMetric(processMetric);
            newClassData.setClassName(classData.getClassName());
            deepCopy.add(newClassData);
        }
        return deepCopy;
    }

}
