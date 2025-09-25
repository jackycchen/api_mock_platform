package com.apimock.controller.statistics;

import com.apimock.dto.ApiResponse;
import com.apimock.dto.statistics.AccessStatisticsRequest;
import com.apimock.dto.statistics.AccessStatisticsResponse;
import com.apimock.entity.mock.MockCallLog;
import com.apimock.service.statistics.AccessStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/statistics")
public class AccessStatisticsController {

    @Autowired
    private AccessStatisticsService accessStatisticsService;

    /**
     * 获取访问统计数据
     */
    @PostMapping("/access")
    public ResponseEntity<ApiResponse<AccessStatisticsResponse>> getAccessStatistics(
            @RequestBody AccessStatisticsRequest request) {
        try {
            AccessStatisticsResponse statistics = accessStatisticsService.getAccessStatistics(request);
            return ResponseEntity.ok(ApiResponse.success(statistics));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("获取访问统计失败: " + e.getMessage()));
        }
    }

    /**
     * 获取API访问统计数据（简化版，通过URL参数）
     */
    @GetMapping("/api/{apiId}")
    public ResponseEntity<ApiResponse<AccessStatisticsResponse>> getApiStatistics(
            @PathVariable Long apiId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "day") String timeGranularity) {
        try {
            AccessStatisticsRequest request = new AccessStatisticsRequest();
            request.setApiId(apiId);
            request.setTimeGranularity(timeGranularity);

            // 解析时间参数
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            if (startTime != null && !startTime.isEmpty()) {
                request.setStartTime(LocalDateTime.parse(startTime + " 00:00:00", formatter));
            }
            if (endTime != null && !endTime.isEmpty()) {
                request.setEndTime(LocalDateTime.parse(endTime + " 23:59:59", formatter));
            }

            AccessStatisticsResponse statistics = accessStatisticsService.getAccessStatistics(request);
            return ResponseEntity.ok(ApiResponse.success(statistics));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("获取API统计失败: " + e.getMessage()));
        }
    }

    /**
     * 获取项目访问统计概览
     */
    @GetMapping("/project/{projectId}/overview")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProjectStatisticsOverview(
            @PathVariable Long projectId) {
        try {
            Map<String, Object> overview = accessStatisticsService.getProjectStatisticsOverview(projectId);
            return ResponseEntity.ok(ApiResponse.success(overview));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("获取项目统计概览失败: " + e.getMessage()));
        }
    }

    /**
     * 获取项目访问统计数据
     */
    @GetMapping("/project/{projectId}")
    public ResponseEntity<ApiResponse<AccessStatisticsResponse>> getProjectStatistics(
            @PathVariable Long projectId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "day") String timeGranularity) {
        try {
            AccessStatisticsRequest request = new AccessStatisticsRequest();
            request.setProjectId(projectId);
            request.setTimeGranularity(timeGranularity);

            // 解析时间参数
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            if (startTime != null && !startTime.isEmpty()) {
                request.setStartTime(LocalDateTime.parse(startTime + " 00:00:00", formatter));
            }
            if (endTime != null && !endTime.isEmpty()) {
                request.setEndTime(LocalDateTime.parse(endTime + " 23:59:59", formatter));
            }

            AccessStatisticsResponse statistics = accessStatisticsService.getAccessStatistics(request);
            return ResponseEntity.ok(ApiResponse.success(statistics));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("获取项目统计失败: " + e.getMessage()));
        }
    }

    /**
     * 获取最近的调用日志
     */
    @GetMapping("/api/{apiId}/recent-logs")
    public ResponseEntity<ApiResponse<List<MockCallLog>>> getRecentCallLogs(
            @PathVariable Long apiId,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<MockCallLog> logs = accessStatisticsService.getRecentCallLogs(apiId, limit);
            return ResponseEntity.ok(ApiResponse.success(logs));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("获取调用日志失败: " + e.getMessage()));
        }
    }

    /**
     * 清理过期日志（管理员功能）
     */
    @DeleteMapping("/logs/cleanup")
    public ResponseEntity<ApiResponse<String>> cleanExpiredLogs(
            @RequestParam(defaultValue = "90") int days) {
        try {
            accessStatisticsService.cleanExpiredLogs(days);
            return ResponseEntity.ok(ApiResponse.success("清理过期日志成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("清理日志失败: " + e.getMessage()));
        }
    }
}