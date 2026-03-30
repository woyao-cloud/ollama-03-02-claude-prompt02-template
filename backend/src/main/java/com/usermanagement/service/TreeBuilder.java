package com.usermanagement.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 树形结构构建器
 * <p>
 * 支持将扁平列表转换为树形结构
 *
 * @param <T> 实体类型
 * @author UserManagement Team
 * @since 1.0.0
 */
public class TreeBuilder<T> {

    private final Function<T, UUID> idExtractor;
    private final Function<T, UUID> parentIdExtractor;
    private final Function<T, Integer> levelExtractor;
    private final Function<T, List<T>> childrenGetter;
    private final BiConsumer<T, List<T>> childrenSetter;

    /**
     * 构造函数
     *
     * @param idExtractor    ID 提取器
     * @param parentIdExtractor 父 ID 提取器
     * @param levelExtractor    层级提取器
     * @param childrenGetter    子节点获取器
     * @param childrenSetter    子节点设置器
     */
    public TreeBuilder(Function<T, UUID> idExtractor,
                       Function<T, UUID> parentIdExtractor,
                       Function<T, Integer> levelExtractor,
                       Function<T, List<T>> childrenGetter,
                       BiConsumer<T, List<T>> childrenSetter) {
        this.idExtractor = idExtractor;
        this.parentIdExtractor = parentIdExtractor;
        this.levelExtractor = levelExtractor;
        this.childrenGetter = childrenGetter;
        this.childrenSetter = childrenSetter;
    }

    /**
     * 默认构造函数 - 使用反射
     */
    public TreeBuilder() {
        this.idExtractor = this::getIdByReflection;
        this.parentIdExtractor = this::getParentIdByReflection;
        this.levelExtractor = this::getLevelByReflection;
        this.childrenGetter = this::getChildrenByReflection;
        this.childrenSetter = this::setChildrenByReflection;
    }

    /**
     * 构建树形结构（按层级筛选）
     *
     * @param items 扁平列表
     * @param level 层级筛选 (可选)
     * @return 筛选后的列表
     */
    public List<T> buildTree(List<T> items, Integer level) {
        if (items == null || items.isEmpty()) {
            return new ArrayList<>();
        }

        // 如果指定了层级，先过滤
        if (level != null) {
            return items.stream()
                    .filter(item -> levelExtractor.apply(item) == level)
                    .collect(Collectors.toList());
        }

        return new ArrayList<>(items);
    }

    /**
     * 构建完整的树形结构（带子节点）
     *
     * @param items 扁平列表
     * @return 根节点列表
     */
    public List<T> buildFullTree(List<T> items) {
        if (items == null || items.isEmpty()) {
            return new ArrayList<>();
        }

        // 使用 LinkedHashMap 保持顺序
        Map<UUID, T> idToItemMap = new LinkedHashMap<>();
        for (T item : items) {
            idToItemMap.put(idExtractor.apply(item), item);
        }

        // 创建子节点映射
        Map<UUID, List<T>> childrenMap = new LinkedHashMap<>();
        for (T item : items) {
            UUID parentId = parentIdExtractor.apply(item);
            if (parentId != null) {
                childrenMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(item);
            }
        }

        // 将子节点添加到父节点
        for (Map.Entry<UUID, List<T>> entry : childrenMap.entrySet()) {
            T parent = idToItemMap.get(entry.getKey());
            if (parent != null) {
                List<T> existingChildren = childrenGetter.apply(parent);
                if (existingChildren == null) {
                    existingChildren = new ArrayList<>();
                }
                existingChildren.addAll(entry.getValue());
                childrenSetter.accept(parent, existingChildren);
            }
        }

        // 返回根节点 (parentId 为 null 的节点)
        List<T> roots = new ArrayList<>();
        for (T item : items) {
            if (parentIdExtractor.apply(item) == null) {
                roots.add(item);
            }
        }

        return roots;
    }

    // 反射辅助方法
    @SuppressWarnings("unchecked")
    private UUID getIdByReflection(T item) {
        try {
            var method = item.getClass().getMethod("getId");
            return (UUID) method.invoke(item);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get ID", e);
        }
    }

    @SuppressWarnings("unchecked")
    private UUID getParentIdByReflection(T item) {
        try {
            var method = item.getClass().getMethod("getParentId");
            return (UUID) method.invoke(item);
        } catch (Exception e) {
            return null;
        }
    }

    private int getLevelByReflection(T item) {
        try {
            var method = item.getClass().getMethod("getLevel");
            return (int) method.invoke(item);
        } catch (Exception e) {
            return 1;
        }
    }

    @SuppressWarnings("unchecked")
    private List<T> getChildrenByReflection(T item) {
        try {
            var method = item.getClass().getMethod("getChildren");
            return (List<T>) method.invoke(item);
        } catch (Exception e) {
            return null;
        }
    }

    private void setChildrenByReflection(T parent, List<T> children) {
        try {
            var method = parent.getClass().getMethod("setChildren", List.class);
            method.invoke(parent, children);
        } catch (Exception e) {
            // 忽略，实体可能没有 children 属性
        }
    }

    /**
     * 函数式接口用于设置子节点
     */
    @FunctionalInterface
    public interface BiConsumer<T, U> {
        void accept(T t, U u);
    }
}
