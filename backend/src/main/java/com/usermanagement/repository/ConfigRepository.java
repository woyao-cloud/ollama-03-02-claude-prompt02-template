package com.usermanagement.repository;

import com.usermanagement.domain.ConfigStatus;
import com.usermanagement.domain.ConfigType;
import com.usermanagement.domain.SystemConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 系统配置 Repository
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Repository
public interface ConfigRepository extends JpaRepository<SystemConfig, UUID> {

    /**
     * 根据配置键查找配置
     */
    Optional<SystemConfig> findByConfigKey(String configKey);

    /**
     * 根据配置类型查找配置列表
     */
    List<SystemConfig> findByConfigType(ConfigType configType);

    /**
     * 根据配置状态查找配置列表
     */
    List<SystemConfig> findByStatus(ConfigStatus status);

    /**
     * 根据配置类型和状态查找配置列表
     */
    List<SystemConfig> findByConfigTypeAndStatus(ConfigType configType, ConfigStatus status);

    /**
     * 检查配置键是否存在
     */
    boolean existsByConfigKey(String configKey);

    /**
     * 根据配置键删除配置
     */
    void deleteByConfigKey(String configKey);

    /**
     * 根据分类查询配置
     */
    List<SystemConfig> findByCategory(ConfigType category);

    /**
     * 高级查询 - 支持多条件筛选
     */
    @Query("SELECT sc FROM SystemConfig sc " +
           "WHERE (:configKey IS NULL OR sc.configKey LIKE %:configKey%) " +
           "AND (:configType IS NULL OR sc.configType = :configType) " +
           "AND (:category IS NULL OR sc.category = :category) " +
           "AND (:status IS NULL OR sc.status = :status) " +
           "AND (:isSensitive IS NULL OR sc.isSensitive = :isSensitive)")
    Page<SystemConfig> findByFilters(
        @Param("configKey") String configKey,
        @Param("configType") ConfigType configType,
        @Param("category") ConfigType category,
        @Param("status") ConfigStatus status,
        @Param("isSensitive") Boolean isSensitive,
        Pageable pageable
    );

    /**
     * 查询所有非加密配置（用于导出）
     */
    @Query("SELECT sc FROM SystemConfig sc WHERE sc.isEncrypted = false")
    List<SystemConfig> findAllNonEncrypted();
}
