package com.apimock.repository.auth;

import com.apimock.entity.auth.UserOperationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户操作日志Repository接口
 *
 * @author AI Assistant
 */
@Repository
public interface UserOperationLogRepository extends JpaRepository<UserOperationLog, Long> {

    /**
     * 根据用户ID查找操作日志
     */
    Page<UserOperationLog> findByUserId(Long userId, Pageable pageable);

    /**
     * 根据操作类型查找日志
     */
    Page<UserOperationLog> findByOperation(String operation, Pageable pageable);

    /**
     * 根据资源类型查找日志
     */
    Page<UserOperationLog> findByResource(String resource, Pageable pageable);

    /**
     * 根据资源类型和资源ID查找日志
     */
    Page<UserOperationLog> findByResourceAndResourceId(String resource, Long resourceId, Pageable pageable);

    /**
     * 根据操作结果查找日志
     */
    Page<UserOperationLog> findByResult(String result, Pageable pageable);

    /**
     * 根据时间范围查找日志
     */
    @Query("SELECT log FROM UserOperationLog log WHERE log.createdAt >= :startTime AND log.createdAt <= :endTime ORDER BY log.createdAt DESC")
    Page<UserOperationLog> findByCreatedAtBetween(@Param("startTime") LocalDateTime startTime,
                                                   @Param("endTime") LocalDateTime endTime,
                                                   Pageable pageable);

    /**
     * 根据用户ID和时间范围查找日志
     */
    @Query("SELECT log FROM UserOperationLog log WHERE log.userId = :userId AND log.createdAt >= :startTime AND log.createdAt <= :endTime ORDER BY log.createdAt DESC")
    Page<UserOperationLog> findByUserIdAndCreatedAtBetween(@Param("userId") Long userId,
                                                            @Param("startTime") LocalDateTime startTime,
                                                            @Param("endTime") LocalDateTime endTime,
                                                            Pageable pageable);

    /**
     * 统计用户的操作次数
     */
    @Query("SELECT COUNT(log) FROM UserOperationLog log WHERE log.userId = :userId AND log.createdAt >= :startTime")
    long countByUserIdAndCreatedAtAfter(@Param("userId") Long userId, @Param("startTime") LocalDateTime startTime);

    /**
     * 统计操作类型的次数
     */
    @Query("SELECT log.operation, COUNT(log) FROM UserOperationLog log WHERE log.createdAt >= :startTime GROUP BY log.operation")
    List<Object[]> countByOperationAndCreatedAtAfter(@Param("startTime") LocalDateTime startTime);

    /**
     * 删除指定时间之前的日志
     */
    void deleteByCreatedAtBefore(LocalDateTime time);
}