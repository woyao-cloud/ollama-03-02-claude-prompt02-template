package com.usermanagement.repository;

import com.usermanagement.domain.ChangeType;
import com.usermanagement.domain.ConfigHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * 配置变更历史 Repository
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Repository
public interface ConfigHistoryRepository extends JpaRepository<ConfigHistory, UUID> {

    /**
     * 根据配置 ID 查询历史记录
     */
    Page<ConfigHistory> findByConfigId(UUID configId, Pageable pageable);

    /**
     * 根据配置键查询历史记录
     */
    Page<ConfigHistory> findByConfigKey(String configKey, Pageable pageable);

    /**
     * 根据配置 ID 查询所有历史记录（按时间倒序）
     */
    List<ConfigHistory> findByConfigIdOrderByChangedAtDesc(UUID configId);

    /**
     * 根据变更类型查询历史记录
     */
    Page<ConfigHistory> findByChangeType(ChangeType changeType, Pageable pageable);

    /**
     * 根据变更人查询历史记录
     */
    Page<ConfigHistory> findByChangedBy(UUID changedBy, Pageable pageable);

    /**
     * 根据时间范围查询历史记录
     */
    Page<ConfigHistory> findByChangedAtBetween(Instant startTime, Instant endTime, Pageable pageable);

    /**
     * 根据配置 ID 和时间范围查询历史记录
     */
    Page<ConfigHistory> findByConfigIdAndChangedAtBetween(UUID configId, Instant startTime, Instant endTime, Pageable pageable);

    /**
     * 高级查询 - 支持多条件筛选
     */
    @Query("SELECT ch FROM ConfigHistory ch " +
           "WHERE (:configId IS NULL OR ch.configId = :configId) " +
           "AND (:configKey IS NULL OR ch.configKey LIKE %:configKey%) " +
           "AND (:changeType IS NULL OR ch.changeType = :changeType) " +
           "AND (:changedBy IS NULL OR ch.changedBy = :changedBy) " +
           "AND (:startTime IS NULL OR ch.changedAt >= :startTime) " +
           "AND (:endTime IS NULL OR ch.changedAt <= :endTime)")
    Page<ConfigHistory> findByFilters(
        @Param("configId") UUID configId,
        @Param("configKey") String configKey,
        @Param("changeType") ChangeType changeType,
        @Param("changedBy") UUID changedBy,
        @Param("startTime") Instant startTime,
        @Param("endTime") Instant endTime,
        Pageable pageable
    );

    /**
     * 删除指定配置的所有历史记录
     */
    void deleteByConfigId(UUID configId);
}
