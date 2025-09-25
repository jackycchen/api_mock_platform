package com.apimock.dto.statistics;

import java.time.LocalDateTime;

public class AccessStatisticsRequest {

    private Long projectId;
    private Long apiId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String timeGranularity = "day"; // hour, day, week, month
    private String statisticsType = "all"; // all, success, error, response_time

    public AccessStatisticsRequest() {}

    public AccessStatisticsRequest(Long projectId, Long apiId) {
        this.projectId = projectId;
        this.apiId = apiId;
        // 默认查询最近7天的数据
        this.endTime = LocalDateTime.now();
        this.startTime = this.endTime.minusDays(7);
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getApiId() {
        return apiId;
    }

    public void setApiId(Long apiId) {
        this.apiId = apiId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getTimeGranularity() {
        return timeGranularity;
    }

    public void setTimeGranularity(String timeGranularity) {
        this.timeGranularity = timeGranularity;
    }

    public String getStatisticsType() {
        return statisticsType;
    }

    public void setStatisticsType(String statisticsType) {
        this.statisticsType = statisticsType;
    }
}