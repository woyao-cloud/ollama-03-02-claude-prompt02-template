package com.usermanagement.web.controller;

import com.usermanagement.service.DepartmentMoveService;
import com.usermanagement.service.DepartmentService;
import com.usermanagement.web.dto.DepartmentCreateRequest;
import com.usermanagement.web.dto.DepartmentDTO;
import com.usermanagement.web.dto.DepartmentMoveRequest;
import com.usermanagement.web.dto.DepartmentTreeResponse;
import com.usermanagement.web.dto.DepartmentUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

/**
 * 部门管理 Controller
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentService departmentService;
    private final DepartmentMoveService departmentMoveService;

    public DepartmentController(DepartmentService departmentService,
                                DepartmentMoveService departmentMoveService) {
        this.departmentService = departmentService;
        this.departmentMoveService = departmentMoveService;
    }

    /**
     * 创建部门
     */
    @PostMapping
    public ResponseEntity<DepartmentDTO> createDepartment(@Valid @RequestBody DepartmentCreateRequest request) {
        DepartmentDTO dto = departmentService.createDepartment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    /**
     * 获取部门树
     */
    @GetMapping("/tree")
    public ResponseEntity<DepartmentTreeResponse> getDepartmentTree(@RequestParam(required = false) Integer level) {
        DepartmentTreeResponse response = departmentService.getDepartmentTree(level);
        return ResponseEntity.ok(response);
    }

    /**
     * 根据 ID 获取部门
     */
    @GetMapping("/{id}")
    public ResponseEntity<DepartmentDTO> getDepartmentById(@PathVariable UUID id) {
        DepartmentDTO dto = departmentService.getDepartmentById(id);
        return ResponseEntity.ok(dto);
    }

    /**
     * 更新部门
     */
    @PutMapping("/{id}")
    public ResponseEntity<DepartmentDTO> updateDepartment(@PathVariable UUID id,
                                                          @Valid @RequestBody DepartmentUpdateRequest request) {
        DepartmentDTO dto = departmentService.updateDepartment(id, request);
        return ResponseEntity.ok(dto);
    }

    /**
     * 删除部门
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable UUID id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 移动部门
     */
    @PutMapping("/{id}/move")
    public ResponseEntity<DepartmentDTO> moveDepartment(@PathVariable UUID id,
                                                        @Valid @RequestBody DepartmentMoveRequest request) {
        DepartmentDTO dto = departmentMoveService.moveDepartment(id, request);
        return ResponseEntity.ok(dto);
    }

    /**
     * 检查部门是否有子部门
     */
    @GetMapping("/{id}/children")
    public ResponseEntity<Map<String, Boolean>> hasChildren(@PathVariable UUID id) {
        boolean hasChildren = departmentService.hasChildren(id);
        return ResponseEntity.ok(Map.of("hasChildren", hasChildren));
    }
}
