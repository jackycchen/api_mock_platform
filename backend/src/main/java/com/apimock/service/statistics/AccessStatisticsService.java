package com.apimock.service.statistics;

import com.apimock.dto.statistics.AccessStatisticsRequest;
import com.apimock.dto.statistics.AccessStatisticsResponse;
import com.apimock.entity.mock.MockCallLog;
import com.apimock.repository.mock.MockCallLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AccessStatisticsService {

    @Autowired
    private MockCallLogRepository mockCallLogRepository;

    public AccessStatisticsResponse getAccessStatistics(AccessStatisticsRequest request) {
        AccessStatisticsResponse response = new AccessStatisticsResponse();

        // 设置默认时间范围（如果未提供）
        if (request.getStartTime() == null || request.getEndTime() == null) {
            request.setEndTime(LocalDateTime.now());
            request.setStartTime(request.getEndTime().minusDays(7));
        }

        // 获取基础统计数据
        calculateBasicStatistics(response, request);

        // 获取响应时间统计
        calculateResponseTimeStatistics(response, request);

        // 获取时间序列数据
        calculateTimeSeriesData(response, request);

        // 获取状态码分布
        calculateStatusCodeDistribution(response, request);

        // 获取响应时间分布
        calculateResponseTimeDistribution(response, request);

        // 获取客户端IP分布
        calculateClientIpDistribution(response, request);

        return response;
    }

    private void calculateBasicStatistics(AccessStatisticsResponse response, AccessStatisticsRequest request) {
        Long totalCalls;
        Long successCalls;

        if (request.getApiId() != null) {
            totalCalls = mockCallLogRepository.countByApiIdAndTimeRange(
                request.getApiId(), request.getStartTime(), request.getEndTime());
            successCalls = mockCallLogRepository.countSuccessByApiIdAndTimeRange(
                request.getApiId(), request.getStartTime(), request.getEndTime());
        } else {
            totalCalls = mockCallLogRepository.countByProjectIdAndTimeRange(
                request.getProjectId(), request.getStartTime(), request.getEndTime());
            // 对于项目级统计，需要实现相应的成功率查询
            successCalls = 0L; // 暂时设为0，后续可以扩展
        }

        response.setTotalCalls(totalCalls);
        response.setSuccessCalls(successCalls);
        response.setErrorCalls(totalCalls - successCalls);
        response.setSuccessRate(totalCalls > 0 ? (successCalls.doubleValue() / totalCalls.doubleValue()) * 100 : 0.0);
    }

    private void calculateResponseTimeStatistics(AccessStatisticsResponse response, AccessStatisticsRequest request) {
        if (request.getApiId() != null) {
            Double avgTime = mockCallLogRepository.getAverageResponseTimeByApiIdAndTimeRange(
                request.getApiId(), request.getStartTime(), request.getEndTime());
            Integer maxTime = mockCallLogRepository.getMaxResponseTimeByApiId(request.getApiId());
            Integer minTime = mockCallLogRepository.getMinResponseTimeByApiId(request.getApiId());

            response.setAverageResponseTime(avgTime);
            response.setMaxResponseTime(maxTime);
            response.setMinResponseTime(minTime);
        }
    }

    private void calculateTimeSeriesData(AccessStatisticsResponse response, AccessStatisticsRequest request) {
        if (request.getApiId() == null) {
            response.setTimeSeriesData(new ArrayList<>());
            return;
        }

        List<Object[]> rawData;
        DateTimeFormatter formatter;

        if ("hour".equals(request.getTimeGranularity())) {
            rawData = mockCallLogRepository.getHourlyDistributionByApiIdAndTimeRange(
                request.getApiId(), request.getStartTime(), request.getEndTime());
            formatter = DateTimeFormatter.ofPattern("HH:00");
        } else {
            rawData = mockCallLogRepository.getDailyDistributionByApiIdAndTimeRange(
                request.getApiId(), request.getStartTime(), request.getEndTime());
            formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        }

        List<AccessStatisticsResponse.TimeSeriesData> timeSeriesData = rawData.stream()
            .map(row -> {
                String timeKey = row[0].toString();
                Long count = ((Number) row[1]).longValue();
                return new AccessStatisticsResponse.TimeSeriesData(timeKey, count, null);
            })
            .collect(Collectors.toList());

        response.setTimeSeriesData(timeSeriesData);
    }

    private void calculateStatusCodeDistribution(AccessStatisticsResponse response, AccessStatisticsRequest request) {
        if (request.getApiId() == null) {
            response.setStatusCodeDistribution(new HashMap<>());
            return;
        }

        List<Object[]> rawData = mockCallLogRepository.getStatusCodeDistributionByApiIdAndTimeRange(
            request.getApiId(), request.getStartTime(), request.getEndTime());

        Map<String, Long> distribution = rawData.stream()
            .collect(Collectors.toMap(
                row -> row[0].toString(),
                row -> ((Number) row[1]).longValue()
            ));

        response.setStatusCodeDistribution(distribution);
    }

    private void calculateResponseTimeDistribution(AccessStatisticsResponse response, AccessStatisticsRequest request) {
        if (request.getApiId() == null) {
            response.setResponseTimeDistribution(new HashMap<>());
            return;
        }

        List<Object[]> rawData = mockCallLogRepository.getResponseTimeDistributionByApiId(request.getApiId());

        Map<String, Long> distribution = rawData.stream()
            .collect(Collectors.toMap(
                row -> (String) row[0],
                row -> ((Number) row[1]).longValue()
            ));

        response.setResponseTimeDistribution(distribution);
    }

    private void calculateClientIpDistribution(AccessStatisticsResponse response, AccessStatisticsRequest request) {
        if (request.getApiId() == null) {
            response.setClientIpDistribution(new ArrayList<>());
            return;
        }

        List<Object[]> rawData = mockCallLogRepository.getClientIpDistributionByApiId(request.getApiId());

        List<AccessStatisticsResponse.ClientIpData> ipDistribution = rawData.stream()
            .limit(10) // 只取前10个IP
            .map(row -> new AccessStatisticsResponse.ClientIpData(
                (String) row[0],
                ((Number) row[1]).longValue()
            ))
            .collect(Collectors.toList());

        response.setClientIpDistribution(ipDistribution);
    }

    // 获取最近的调用日志
    public List<MockCallLog> getRecentCallLogs(Long apiId, int limit) {
        if (limit <= 0) {
            limit = 10;
        }
        return mockCallLogRepository.findTop10ByApiIdOrderByCreatedAtDesc(apiId);
    }

    // 清理过期日志
    public void cleanExpiredLogs(int days) {
        LocalDateTime expireTime = LocalDateTime.now().minusDays(days);
        mockCallLogRepository.deleteExpiredLogs(expireTime);
    }

    // 项目级统计概览
    public Map<String, Object> getProjectStatisticsOverview(Long projectId) {
        Map<String, Object> overview = new HashMap<>();

        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(30); // 最近30天

        long totalCalls = mockCallLogRepository.countByProjectIdAndTimeRange(projectId, startTime, endTime);

        overview.put("totalCalls", totalCalls);
        overview.put("timeRange", "最近30天");

        return overview;
    }
}