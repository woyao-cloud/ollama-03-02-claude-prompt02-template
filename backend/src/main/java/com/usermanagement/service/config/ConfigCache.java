package com.usermanagement.service.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.usermanagement.domain.SystemConfig;
import com.usermanagement.web.dto.SystemConfigDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 配置缓存服务 - 本地缓存（Caffeine）+ Redis 分布式缓存
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Component
public class ConfigCache {

    private static final Logger logger = LoggerFactory.getLogger(ConfigCache.class);

    /**
     * 本地缓存键前缀
     */
    private static final String LOCAL_CACHE_KEY_PREFIX = "config:local:";

    /**
     * Redis 缓存键前缀
     */
    private static final String REDIS_CACHE_KEY_PREFIX = "config:redis:";

    /**
     * 配置类型缓存键
     */
    private static final String REDIS_CACHE_TYPE_KEY_PREFIX = "config:redis:type:";

    /**
     * 本地缓存过期时间（秒）
     */
    private static final long LOCAL_EXPIRATION_SECONDS = 300L;

    /**
     * Redis 缓存过期时间（秒）
     */
    private static final long REDIS_EXPIRATION_SECONDS = 600L;

    /**
     * 本地缓存 - 使用 Caffeine
     */
    private final Cache<String, SystemConfigDTO> localCache;

    /**
     * Redis 模板
     */
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 本地配置内存缓存（用于快速访问）
     */
    private final Map<String, SystemConfigDTO> configMap;

    public ConfigCache(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.localCache = Caffeine.newBuilder()
                .expireAfterWrite(LOCAL_EXPIRATION_SECONDS, TimeUnit.SECONDS)
                .maximumSize(500)
                .recordStats()
                .build();
        this.configMap = new ConcurrentHashMap<>();
        log.info("配置缓存服务已初始化，本地缓存过期时间：{}秒", LOCAL_EXPIRATION_SECONDS);
    }

    /**
     * 从缓存获取配置值
     *
     * @param configKey 配置键
     * @return 配置 DTO，缓存不存在时返回 null
     */
    public SystemConfigDTO get(String configKey) {
        if (configKey == null) {
            return null;
        }

        // 1. 先从内存缓存获取
        SystemConfigDTO dto = configMap.get(configKey);
        if (dto != null) {
            log.debug("从内存缓存命中配置：{}", configKey);
            return dto;
        }

        // 2. 从本地缓存获取
        dto = localCache.getIfPresent(LOCAL_CACHE_KEY_PREFIX + configKey);
        if (dto != null) {
            log.debug("从本地缓存命中配置：{}", configKey);
            // 回填到内存缓存
            configMap.put(configKey, dto);
            return dto;
        }

        // 3. 从 Redis 获取
        try {
            dto = (SystemConfigDTO) redisTemplate.opsForValue()
                    .get(REDIS_CACHE_KEY_PREFIX + configKey);
            if (dto != null) {
                log.debug("从 Redis 缓存命中配置：{}", configKey);
                // 回填到本地缓存和内存缓存
                localCache.put(LOCAL_CACHE_KEY_PREFIX + configKey, dto);
                configMap.put(configKey, dto);
                return dto;
            }
        } catch (Exception e) {
            log.warn("从 Redis 获取配置失败，降级处理：{}", e.getMessage());
        }

        log.debug("缓存未命中配置：{}", configKey);
        return null;
    }

    /**
     * 将配置存入缓存
     *
     * @param config 配置实体
     * @param dto    配置 DTO
     */
    public void put(SystemConfig config, SystemConfigDTO dto) {
        if (config == null || dto == null) {
            return;
        }

        String configKey = config.getConfigKey();

        // 1. 存入内存缓存
        configMap.put(configKey, dto);

        // 2. 存入本地缓存
        localCache.put(LOCAL_CACHE_KEY_PREFIX + configKey, dto);

        // 3. 存入 Redis 缓存
        try {
            redisTemplate.opsForValue().set(
                    REDIS_CACHE_KEY_PREFIX + configKey,
                    dto,
                    REDIS_EXPIRATION_SECONDS,
                    TimeUnit.SECONDS
            );
            log.debug("配置已缓存到 Redis：{}", configKey);
        } catch (Exception e) {
            log.warn("将配置存入 Redis 失败：{}", e.getMessage());
        }
    }

    /**
     * 从缓存中删除配置
     *
     * @param configKey 配置键
     */
    public void evict(String configKey) {
        if (configKey == null) {
            return;
        }

        // 1. 从内存缓存删除
        configMap.remove(configKey);

        // 2. 从本地缓存删除
        localCache.invalidate(LOCAL_CACHE_KEY_PREFIX + configKey);

        // 3. 从 Redis 删除
        try {
            redisTemplate.delete(REDIS_CACHE_KEY_PREFIX + configKey);
            log.debug("配置缓存已清除：{}", configKey);
        } catch (Exception e) {
            log.warn("清除 Redis 配置缓存失败：{}", e.getMessage());
        }
    }

    /**
     * 清除所有配置缓存
     */
    public void evictAll() {
        // 1. 清空内存缓存
        configMap.clear();

        // 2. 清空本地缓存
        localCache.invalidateAll();

        // 3. 清空 Redis 缓存（使用 pattern 删除）
        try {
            redisTemplate.delete(redisTemplate.keys(REDIS_CACHE_KEY_PREFIX + "*"));
            log.info("所有配置缓存已清除");
        } catch (Exception e) {
            log.warn("清除所有 Redis 配置缓存失败：{}", e.getMessage());
        }
    }

    /**
     * 预加载配置到缓存
     *
     * @param configs 配置列表
     */
    public void preload(java.util.List<SystemConfig> configs, java.util.function.Function<SystemConfig, SystemConfigDTO> mapper) {
        if (configs == null || configs.isEmpty()) {
            return;
        }

        for (SystemConfig config : configs) {
            try {
                SystemConfigDTO dto = mapper.apply(config);
                put(config, dto);
            } catch (Exception e) {
                log.warn("预加载配置失败：{}", config.getConfigKey(), e);
            }
        }

        log.info("预加载 {} 个配置到缓存", configs.size());
    }

    /**
     * 获取缓存统计信息
     */
    public Map<String, Object> getStats() {
        return Map.of(
                "localCacheSize", localCache.estimatedSize(),
                "localCacheHitRate", localCache.stats().hitRate(),
                "memoryCacheSize", configMap.size()
        );
    }
}
