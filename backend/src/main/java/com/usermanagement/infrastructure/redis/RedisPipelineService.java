package com.usermanagement.infrastructure.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Redis Pipeline 批量操作服务
 *
 * 用于高性能批量 Redis 操作，减少网络往返
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Service
public class RedisPipelineService {

    private static final Logger logger = LoggerFactory.getLogger(RedisPipelineService.class);

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisPipelineService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 批量获取多个 Key 的值
     *
     * @param keys Key 集合
     * @return 值列表 (按 Key 顺序，不存在则为 null)
     */
    public List<Object> multiGet(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return List.of();
        }

        return redisTemplate.executePipelined((RedisCallback<?>) action -> {
            for (String key : keys) {
                action.keySerializer();
                redisTemplate.opsForValue().get(key);
            }
            return null;
        });
    }

    /**
     * 批量设置多个 Key-Value
     *
     * @param entries Key-Value 对
     * @param ttlSeconds TTL 时间 (秒)
     */
    public void multiSet(Map<String, Object> entries, long ttlSeconds) {
        if (entries == null || entries.isEmpty()) {
            return;
        }

        redisTemplate.executePipelined((RedisCallback<?>) action -> {
            for (Map.Entry<String, Object> entry : entries.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                redisTemplate.opsForValue().set(key, value, ttlSeconds, java.util.concurrent.TimeUnit.SECONDS);
            }
            return null;
        });

        logger.debug("批量设置 {} 个缓存项，TTL={}s", entries.size(), ttlSeconds);
    }

    /**
     * 批量删除多个 Key
     *
     * @param keys Key 集合
     * @return 删除的数量
     */
    public Long multiDelete(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return 0L;
        }

        return redisTemplate.executePipelined((RedisCallback<?>) action -> {
            long count = 0;
            for (String key : keys) {
                if (Boolean.TRUE.equals(redisTemplate.delete(key))) {
                    count++;
                }
            }
            return count;
        });
    }

    /**
     * 批量设置缓存（使用 Pipeline 优化）
     *
     * @param prefix Key 前缀
     * @param items Key 后缀到值的映射
     * @param ttlSeconds TTL 时间 (秒)
     */
    public void batchSetCache(String prefix, Map<String, Object> items, long ttlSeconds) {
        if (items == null || items.isEmpty()) {
            return;
        }

        redisTemplate.executePipelined((RedisCallback<?>) action -> {
            for (Map.Entry<String, Object> entry : items.entrySet()) {
                String key = prefix + ":" + entry.getKey();
                Object value = entry.getValue();
                redisTemplate.opsForValue().set(key, value, ttlSeconds, java.util.concurrent.TimeUnit.SECONDS);
            }
            return null;
        });

        logger.info("批量缓存 {} 项到 Redis，前缀={}, TTL={}s", items.size(), prefix, ttlSeconds);
    }

    /**
     * 批量获取缓存（使用 Pipeline 优化）
     *
     * @param prefix Key 前缀
     * @param keys Key 后缀集合
     * @return 值列表 (按 Key 顺序)
     */
    public List<Object> batchGetCache(String prefix, Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return List.of();
        }

        List<String> fullKeys = keys.stream()
            .map(key -> prefix + ":" + key)
            .collect(Collectors.toList());

        return redisTemplate.executePipelined((RedisCallback<?>) action -> {
            for (String key : fullKeys) {
                redisTemplate.opsForValue().get(key);
            }
            return null;
        });
    }

    /**
     * 批量删除缓存（使用 Pipeline 优化）
     *
     * @param prefix Key 前缀
     * @param keys Key 后缀集合
     * @return 删除的数量
     */
    public Long batchDeleteCache(String prefix, Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return 0L;
        }

        List<String> fullKeys = keys.stream()
            .map(key -> prefix + ":" + key)
            .collect(Collectors.toList());

        return redisTemplate.execute(new RedisConnectionCallback<Long>() {
            @Override
            public Long doInRedis(RedisConnection connection) {
                long count = 0;
                for (String key : fullKeys) {
                    if (Boolean.TRUE.equals(redisTemplate.delete(key))) {
                        count++;
                    }
                }
                return count;
            }
        });
    }

    /**
     * 使用 Pipeline 批量检查 Keys 是否存在
     *
     * @param keys Key 集合
     * @return 存在性列表 (按 Key 顺序)
     */
    public List<Boolean> multiExists(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return List.of();
        }

        return redisTemplate.executePipelined((RedisCallback<?>) action -> {
            for (String key : keys) {
                redisTemplate.hasKey(key);
            }
            return null;
        });
    }

    /**
     * 批量设置过期时间
     *
     * @param keys Key 集合
     * @param ttlSeconds TTL 时间 (秒)
     */
    public void multiExpire(Collection<String> keys, long ttlSeconds) {
        if (keys == null || keys.isEmpty()) {
            return;
        }

        redisTemplate.executePipelined((RedisCallback<?>) action -> {
            for (String key : keys) {
                redisTemplate.expire(key, ttlSeconds, java.util.concurrent.TimeUnit.SECONDS);
            }
            return null;
        });

        logger.debug("批量设置 {} 个 Key 的过期时间为{}s", keys.size(), ttlSeconds);
    }

    /**
     * 批量递增计数器
     *
     * @param keys Key 集合
     * @return 递增后的值列表
     */
    public List<Long> multiIncrement(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return List.of();
        }

        return redisTemplate.executePipelined((RedisCallback<?>) action -> {
            for (String key : keys) {
                redisTemplate.opsForValue().increment(key);
            }
            return null;
        });
    }

    /**
     * 批量获取计数器值
     *
     * @param keys Key 集合
     * @return 值列表
     */
    public List<Long> multiGetCounters(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return List.of();
        }

        return redisTemplate.executePipelined((RedisCallback<?>) action -> {
            for (String key : keys) {
                redisTemplate.opsForValue().get(key);
            }
            return null;
        });
    }
}
