package com.apimock.dto.statistics;

import java.util.List;
import java.util.Map;

public class AccessStatisticsResponse {

    // 基础统计数据
    private Long totalCalls;
    private Long successCalls;
    private Long errorCalls;
    private Double successRate;

    // 响应时间统计
    private Double averageResponseTime;
    private Integer maxResponseTime;
    private Integer minResponseTime;

    // 时间分布数据
    private List<TimeSeriesData> timeSeriesData;

    // 状态码分布
    private Map<String, Long> statusCodeDistribution;

    // 响应时间分布
    private Map<String, Long> responseTimeDistribution;

    // 客户端IP分布
    private List<ClientIpData> clientIpDistribution;

    public AccessStatisticsResponse() {}

    // 内部类：时间序列数据
    public static class TimeSeriesData {
        private String time;
        private Long count;
        private Double avgResponseTime;

        public TimeSeriesData() {}

        public TimeSeriesData(String time, Long count, Double avgResponseTime) {
            this.time = time;
            this.count = count;
            this.avgResponseTime = avgResponseTime;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public Long getCount() {
            return count;
        }

        public void setCount(Long count) {
            this.count = count;
        }

        public Double getAvgResponseTime() {
            return avgResponseTime;
        }

        public void setAvgResponseTime(Double avgResponseTime) {
            this.avgResponseTime = avgResponseTime;
        }
    }

    // 内部类：客户端IP数据
    public static class ClientIpData {
        private String clientIp;
        private Long count;

        public ClientIpData() {}

        public ClientIpData(String clientIp, Long count) {
            this.clientIp = clientIp;
            this.count = count;
        }

        public String getClientIp() {
            return clientIp;
        }

        public void setClientIp(String clientIp) {
            this.clientIp = clientIp;
        }

        public Long getCount() {
            return count;
        }

        public void setCount(Long count) {
            this.count = count;
        }
    }

    // Getters and Setters
    public Long getTotalCalls() {
        return totalCalls;
    }

    public void setTotalCalls(Long totalCalls) {
        this.totalCalls = totalCalls;
    }

    public Long getSuccessCalls() {
        return successCalls;
    }

    public void setSuccessCalls(Long successCalls) {
        this.successCalls = successCalls;
    }

    public Long getErrorCalls() {
        return errorCalls;
    }

    public void setErrorCalls(Long errorCalls) {
        this.errorCalls = errorCalls;
    }

    public Double getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(Double successRate) {
        this.successRate = successRate;
    }

    public Double getAverageResponseTime() {
        return averageResponseTime;
    }

    public void setAverageResponseTime(Double averageResponseTime) {
        this.averageResponseTime = averageResponseTime;
    }

    public Integer getMaxResponseTime() {
        return maxResponseTime;
    }

    public void setMaxResponseTime(Integer maxResponseTime) {
        this.maxResponseTime = maxResponseTime;
    }

    public Integer getMinResponseTime() {
        return minResponseTime;
    }

    public void setMinResponseTime(Integer minResponseTime) {
        this.minResponseTime = minResponseTime;
    }

    public List<TimeSeriesData> getTimeSeriesData() {
        return timeSeriesData;
    }

    public void setTimeSeriesData(List<TimeSeriesData> timeSeriesData) {
        this.timeSeriesData = timeSeriesData;
    }

    public Map<String, Long> getStatusCodeDistribution() {
        return statusCodeDistribution;
    }

    public void setStatusCodeDistribution(Map<String, Long> statusCodeDistribution) {
        this.statusCodeDistribution = statusCodeDistribution;
    }

    public Map<String, Long> getResponseTimeDistribution() {
        return responseTimeDistribution;
    }

    public void setResponseTimeDistribution(Map<String, Long> responseTimeDistribution) {
        this.responseTimeDistribution = responseTimeDistribution;
    }

    public List<ClientIpData> getClientIpDistribution() {
        return clientIpDistribution;
    }

    public void setClientIpDistribution(List<ClientIpData> clientIpDistribution) {
        this.clientIpDistribution = clientIpDistribution;
    }
}