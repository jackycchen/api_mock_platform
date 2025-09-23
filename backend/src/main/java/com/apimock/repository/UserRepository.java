package com.apimock.repository;

import com.apimock.entity.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据用户名查找用户
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据邮箱查找用户
     */
    Optional<User> findByEmail(String email);

    /**
     * 根据用户名或邮箱查找用户
     */
    @Query("SELECT u FROM User u WHERE u.username = :usernameOrEmail OR u.email = :usernameOrEmail")
    Optional<User> findByUsernameOrEmail(@Param("usernameOrEmail") String usernameOrEmail);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);

    /**
     * 更新用户登录失败次数
     */
    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = :attempts, u.updatedAt = :now WHERE u.id = :id")
    void updateFailedLoginAttempts(@Param("id") Long id, @Param("attempts") Integer attempts, @Param("now") LocalDateTime now);

    /**
     * 锁定用户账户
     */
    @Modifying
    @Query("UPDATE User u SET u.lockedUntil = :lockedUntil, u.updatedAt = :now WHERE u.id = :id")
    void lockUser(@Param("id") Long id, @Param("lockedUntil") LocalDateTime lockedUntil, @Param("now") LocalDateTime now);

    /**
     * 解锁用户账户
     */
    @Modifying
    @Query("UPDATE User u SET u.lockedUntil = null, u.failedLoginAttempts = 0, u.updatedAt = :now WHERE u.id = :id")
    void unlockUser(@Param("id") Long id, @Param("now") LocalDateTime now);

    /**
     * 更新最后登录时间
     */
    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :loginTime, u.failedLoginAttempts = 0, u.updatedAt = :now WHERE u.id = :id")
    void updateLastLoginTime(@Param("id") Long id, @Param("loginTime") LocalDateTime loginTime, @Param("now") LocalDateTime now);
}