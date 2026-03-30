package com.usermanagement.service;

import com.usermanagement.domain.Department;
import com.usermanagement.domain.DepartmentStatus;
import com.usermanagement.repository.DepartmentRepository;
import com.usermanagement.service.cache.CacheEvictionListener;
import com.usermanagement.service.cache.DepartmentCache;
import com.usermanagement.web.dto.DepartmentCreateRequest;
import com.usermanagement.web.dto.DepartmentDTO;
import com.usermanagement.web.dto.DepartmentTreeResponse;
import com.usermanagement.web.dto.DepartmentUpdateRequest;
import com.usermanagement.web.mapper.DepartmentMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 部门服务实现类
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Service
@Transactional
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;
    private final TreeBuilder<Department> treeBuilder;
    private final DepartmentCache departmentCache;
    private final CacheEvictionListener cacheEvictionListener;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository,
                                 DepartmentMapper departmentMapper,
                                 TreeBuilder<Department> treeBuilder,
                                 DepartmentCache departmentCache,
                                 CacheEvictionListener cacheEvictionListener) {
        this.departmentRepository = departmentRepository;
        this.departmentMapper = departmentMapper;
        this.treeBuilder = treeBuilder;
        this.departmentCache = departmentCache;
        this.cacheEvictionListener = cacheEvictionListener;
    }

    @Override
    @Transactional
    public DepartmentDTO createDepartment(DepartmentCreateRequest request) {
        // 检查代码是否已存在
        if (departmentRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("部门代码已存在：" + request.getCode());
        }

        Department department = departmentMapper.toEntity(request);

        // 设置默认状态
        department.setStatus(DepartmentStatus.ACTIVE);

        // 生成 path 和 level
        if (request.getParentId() != null) {
            UUID parentId = UUID.fromString(request.getParentId());
            Department parent = departmentRepository.findById(parentId)
                    .orElseThrow(() -> new IllegalArgumentException("父部门不存在：" + request.getParentId()));

            department.setParentId(parentId);
            department.setLevel(parent.getLevel() + 1);
            department.setPath(parent.getPath() + "/" + department.getId());
        } else {
            // 根部门
            department.setParentId(null);
            department.setLevel(request.getLevel());
            department.setPath("/" + department.getId());
        }

        // 验证层级
        if (department.getLevel() < 1 || department.getLevel() > 5) {
            throw new IllegalArgumentException("部门层级必须在 1-5 范围内");
        }

        Department saved = departmentRepository.save(department);

        // 清除缓存
        cacheEvictionListener.onDepartmentCreated(saved);

        return departmentMapper.toDto(saved);
    }

    @Override
    @Transactional
    public DepartmentDTO updateDepartment(UUID id, DepartmentUpdateRequest request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("部门不存在：" + id));

        // 更新字段
        if (request.getName() != null) {
            department.setName(request.getName());
        }
        if (request.getManagerId() != null) {
            department.setManagerId(UUID.fromString(request.getManagerId()));
        }
        if (request.getStatus() != null) {
            department.setStatus(DepartmentStatus.valueOf(request.getStatus()));
        }
        if (request.getSortOrder() != null) {
            department.setSortOrder(request.getSortOrder());
        }
        if (request.getDescription() != null) {
            department.setDescription(request.getDescription());
        }

        Department saved = departmentRepository.save(department);

        // 清除缓存
        cacheEvictionListener.onDepartmentUpdated(saved);

        return departmentMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentDTO getDepartmentById(UUID id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("部门不存在：" + id));
        return departmentMapper.toDto(department);
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentTreeResponse getDepartmentTree(Integer level) {
        // 优先从缓存读取（仅当不指定层级时）
        if (level == null) {
            DepartmentTreeResponse cached = departmentCache.getDepartmentTree();
            if (cached != null) {
                return cached;
            }
        }

        List<Department> departments = departmentRepository.findAllOrderedByPath();

        // 如果指定了层级，先过滤
        List<Department> filteredDepartments = departments;
        if (level != null) {
            filteredDepartments = departments.stream()
                    .filter(d -> d.getLevel() == level)
                    .collect(Collectors.toList());
        }

        // 使用 TreeBuilder 进行层级筛选
        List<Department> tree = treeBuilder.buildTree(filteredDepartments, level);

        // 转换为 DTO
        List<DepartmentDTO> dtoList = departmentMapper.toTreeDto(tree);

        // 构建完整的树形结构（带子节点）
        List<DepartmentDTO> treeWithChildren = buildFullTree(dtoList);

        DepartmentTreeResponse response = DepartmentTreeResponse.builder()
                .tree(treeWithChildren)
                .total((long) tree.size())
                .build();

        // 缓存结果（仅当不指定层级时）
        if (level == null) {
            departmentCache.setDepartmentTree(response);
        }

        return response;
    }

    /**
     * 构建完整的树形结构（带子节点）
     */
    private List<DepartmentDTO> buildFullTree(List<DepartmentDTO> items) {
        if (items == null || items.isEmpty()) {
            return new ArrayList<>();
        }

        // 使用 LinkedHashMap 保持顺序
        Map<String, DepartmentDTO> idToItemMap = new LinkedHashMap<>();
        for (DepartmentDTO item : items) {
            idToItemMap.put(item.getId(), item);
        }

        // 创建子节点映射
        Map<String, List<DepartmentDTO>> childrenMap = new LinkedHashMap<>();
        for (DepartmentDTO item : items) {
            if (item.getParentId() != null) {
                childrenMap.computeIfAbsent(item.getParentId(), k -> new ArrayList<>()).add(item);
            }
        }

        // 将子节点添加到父节点
        for (Map.Entry<String, List<DepartmentDTO>> entry : childrenMap.entrySet()) {
            DepartmentDTO parent = idToItemMap.get(entry.getKey());
            if (parent != null) {
                List<DepartmentDTO> existingChildren = parent.getChildren();
                if (existingChildren == null) {
                    existingChildren = new ArrayList<>();
                }
                existingChildren.addAll(entry.getValue());
                parent.setChildren(existingChildren);
            }
        }

        // 返回根节点 (parentId 为 null 的节点)
        List<DepartmentDTO> roots = new ArrayList<>();
        for (DepartmentDTO item : items) {
            if (item.getParentId() == null) {
                roots.add(item);
            }
        }

        return roots.isEmpty() ? items : roots;
    }

    @Override
    @Transactional
    public void deleteDepartment(UUID id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("部门不存在：" + id));

        // 软删除
        department.setDeletedAt(java.time.Instant.now());
        Department saved = departmentRepository.save(department);

        // 清除缓存
        cacheEvictionListener.onDepartmentDeleted(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasChildren(UUID id) {
        List<Department> children = departmentRepository.findByParentId(id);
        return !children.isEmpty();
    }
}
