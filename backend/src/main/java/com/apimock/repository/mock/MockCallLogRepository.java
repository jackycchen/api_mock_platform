package com.apimock.repository.mock;

import com.apimock.entity.mock.MockCallLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MockCallLogRepository extends JpaRepository<MockCallLog, Long> {

    // 基础查询方法
    Page<MockCallLog> findByApiIdOrderByCreatedAtDesc(Long apiId, Pageable pageable);

    Page<MockCallLog> findByMockRuleIdOrderByCreatedAtDesc(Long mockRuleId, Pageable pageable);

    List<MockCallLog> findByApiIdAndCreatedAtBetween(Long apiId, LocalDateTime startTime, LocalDateTime endTime);

    // 统计查询
    @Query("SELECT COUNT(m) FROM MockCallLog m WHERE m.apiId = :apiId")
    long countByApiId(@Param("apiId") Long apiId);

    @Query("SELECT COUNT(m) FROM MockCallLog m WHERE m.apiId = :apiId AND m.createdAt BETWEEN :startTime AND :endTime")
    long countByApiIdAndTimeRange(@Param("apiId") Long apiId,
                                  @Param("startTime") LocalDateTime startTime,
                                  @Param("endTime") LocalDateTime endTime);

    @Query("SELECT COUNT(m) FROM MockCallLog m WHERE m.apiId = :apiId AND m.responseStatus >= 200 AND m.responseStatus < 300")
    long countSuccessByApiId(@Param("apiId") Long apiId);

    @Query("SELECT COUNT(m) FROM MockCallLog m WHERE m.apiId = :apiId AND m.responseStatus >= 200 AND m.responseStatus < 300 AND m.createdAt BETWEEN :startTime AND :endTime")
    long countSuccessByApiIdAndTimeRange(@Param("apiId") Long apiId,
                                         @Param("startTime") LocalDateTime startTime,
                                         @Param("endTime") LocalDateTime endTime);

    // 响应时间统计
    @Query("SELECT AVG(m.responseTime) FROM MockCallLog m WHERE m.apiId = :apiId AND m.responseTime IS NOT NULL")
    Double getAverageResponseTimeByApiId(@Param("apiId") Long apiId);

    @Query("SELECT AVG(m.responseTime) FROM MockCallLog m WHERE m.apiId = :apiId AND m.responseTime IS NOT NULL AND m.createdAt BETWEEN :startTime AND :endTime")
    Double getAverageResponseTimeByApiIdAndTimeRange(@Param("apiId") Long apiId,
                                                     @Param("startTime") LocalDateTime startTime,
                                                     @Param("endTime") LocalDateTime endTime);

    @Query("SELECT MAX(m.responseTime) FROM MockCallLog m WHERE m.apiId = :apiId AND m.responseTime IS NOT NULL")
    Integer getMaxResponseTimeByApiId(@Param("apiId") Long apiId);

    @Query("SELECT MIN(m.responseTime) FROM MockCallLog m WHERE m.apiId = :apiId AND m.responseTime IS NOT NULL")
    Integer getMinResponseTimeByApiId(@Param("apiId") Long apiId);

    // 按项目统计（通过API关联）
    @Query("SELECT COUNT(m) FROM MockCallLog m JOIN Api a ON m.apiId = a.id WHERE a.projectId = :projectId")
    long countByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT COUNT(m) FROM MockCallLog m JOIN Api a ON m.apiId = a.id WHERE a.projectId = :projectId AND m.createdAt BETWEEN :startTime AND :endTime")
    long countByProjectIdAndTimeRange(@Param("projectId") Long projectId,
                                      @Param("startTime") LocalDateTime startTime,
                                      @Param("endTime") LocalDateTime endTime);

    // 状态码分布统计
    @Query("SELECT m.responseStatus, COUNT(m) FROM MockCallLog m WHERE m.apiId = :apiId GROUP BY m.responseStatus")
    List<Object[]> getStatusCodeDistributionByApiId(@Param("apiId") Long apiId);

    @Query("SELECT m.responseStatus, COUNT(m) FROM MockCallLog m WHERE m.apiId = :apiId AND m.createdAt BETWEEN :startTime AND :endTime GROUP BY m.responseStatus")
    List<Object[]> getStatusCodeDistributionByApiIdAndTimeRange(@Param("apiId") Long apiId,
                                                                @Param("startTime") LocalDateTime startTime,
                                                                @Param("endTime") LocalDateTime endTime);

    // 时间分布统计 - 按小时
    @Query("SELECT HOUR(m.createdAt), COUNT(m) FROM MockCallLog m WHERE m.apiId = :apiId AND m.createdAt BETWEEN :startTime AND :endTime GROUP BY HOUR(m.createdAt) ORDER BY HOUR(m.createdAt)")
    List<Object[]> getHourlyDistributionByApiIdAndTimeRange(@Param("apiId") Long apiId,
                                                            @Param("startTime") LocalDateTime startTime,
                                                            @Param("endTime") LocalDateTime endTime);

    // 时间分布统计 - 按日期
    @Query("SELECT DATE(m.createdAt), COUNT(m) FROM MockCallLog m WHERE m.apiId = :apiId AND m.createdAt BETWEEN :startTime AND :endTime GROUP BY DATE(m.createdAt) ORDER BY DATE(m.createdAt)")
    List<Object[]> getDailyDistributionByApiIdAndTimeRange(@Param("apiId") Long apiId,
                                                           @Param("startTime") LocalDateTime startTime,
                                                           @Param("endTime") LocalDateTime endTime);

    // 响应时间分布统计
    @Query("SELECT " +
           "CASE " +
           "WHEN m.responseTime < 100 THEN '0-100ms' " +
           "WHEN m.responseTime < 500 THEN '100-500ms' " +
           "WHEN m.responseTime < 1000 THEN '500ms-1s' " +
           "WHEN m.responseTime < 5000 THEN '1-5s' " +
           "ELSE '>5s' " +
           "END as range, " +
           "COUNT(m) " +
           "FROM MockCallLog m " +
           "WHERE m.apiId = :apiId AND m.responseTime IS NOT NULL " +
           "GROUP BY " +
           "CASE " +
           "WHEN m.responseTime < 100 THEN '0-100ms' " +
           "WHEN m.responseTime < 500 THEN '100-500ms' " +
           "WHEN m.responseTime < 1000 THEN '500ms-1s' " +
           "WHEN m.responseTime < 5000 THEN '1-5s' " +
           "ELSE '>5s' " +
           "END")
    List<Object[]> getResponseTimeDistributionByApiId(@Param("apiId") Long apiId);

    // 清理过期日志
    @Query("DELETE FROM MockCallLog m WHERE m.createdAt < :expireTime")
    void deleteExpiredLogs(@Param("expireTime") LocalDateTime expireTime);

    // 获取最近的调用记录
    List<MockCallLog> findTop10ByApiIdOrderByCreatedAtDesc(Long apiId);

    List<MockCallLog> findTop100ByOrderByCreatedAtDesc();

    // 根据客户端IP统计
    @Query("SELECT m.clientIp, COUNT(m) FROM MockCallLog m WHERE m.apiId = :apiId AND m.clientIp IS NOT NULL GROUP BY m.clientIp ORDER BY COUNT(m) DESC")
    List<Object[]> getClientIpDistributionByApiId(@Param("apiId") Long apiId);
}