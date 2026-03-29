package com.usermanagement.web.controller;

import com.usermanagement.domain.UserStatus;
import com.usermanagement.service.UserService;
import com.usermanagement.service.exporter.UserExportService;
import com.usermanagement.service.importer.UserImportService;
import com.usermanagement.web.dto.PasswordResetRequest;
import com.usermanagement.web.dto.UserCreateRequest;
import com.usermanagement.web.dto.UserDTO;
import com.usermanagement.web.dto.UserListResponse;
import com.usermanagement.web.dto.UserStatusUpdateRequest;
import com.usermanagement.web.dto.UserUpdateRequest;
import com.usermanagement.web.dto.ImportResult;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;

/**
 * 用户管理控制器
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final UserImportService importService;
    private final UserExportService exportService;

    public UserController(
            UserService userService,
            UserImportService importService,
            UserExportService exportService
    ) {
        this.userService = userService;
        this.importService = importService;
        this.exportService = exportService;
    }

    /**
     * 创建用户
     *
     * POST /api/users
     *
     * @param request 创建用户请求
     * @return 创建的用户
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserCreateRequest request) {
        logger.info("创建用户：{}", request.getEmail());
        UserDTO user = userService.createUser(request);
        return ResponseEntity.created(URI.create("/api/users/" + user.getId())).body(user);
    }

    /**
     * 批量导入用户
     *
     * POST /api/users/import
     *
     * @param file Excel 或 CSV 文件
     * @return 导入结果
     */
    @PostMapping("/import")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ImportResult> importUsers(@RequestParam("file") MultipartFile file) {
        logger.info("批量导入用户，文件名：{}, 大小：{} bytes", file.getOriginalFilename(), file.getSize());

        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }

        ImportResult result = importService.importUsers(file.getBytes());
        return ResponseEntity.ok(result);
    }

    /**
     * 批量导出用户
     *
     * GET /api/users/export
     *
     * @param page         页码（默认 0）
     * @param size         每页大小（默认 100）
     * @param sortBy       排序字段（默认 createdAt）
     * @param direction    排序方向（默认 DESC）
     * @param keyword      关键词（邮箱/姓名）
     * @param departmentId 部门 ID
     * @param status       用户状态
     * @return Excel 文件
     */
    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<byte[]> exportUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UUID departmentId,
            @RequestParam(required = false) UserStatus status
    ) {
        Sort sort = direction.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        logger.info("批量导出用户，页码：{}, 大小：{}, 部门：{}, 状态：{}", page, size, departmentId, status);

        byte[] excelData = exportService.exportUsers(pageable, keyword, departmentId, status);

        String filename = "users_export_" + Instant.now().toEpochMilli() + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .contentLength(excelData.length)
                .body(excelData);
    }

    /**
     * 获取用户列表
     *
     * GET /api/users
     *
     * @param page         页码（默认 0）
     * @param size         每页大小（默认 10）
     * @param sortBy       排序字段（默认 createdAt）
     * @param direction    排序方向（默认 DESC）
     * @param keyword      关键词（邮箱/姓名）
     * @param departmentId 部门 ID
     * @param status       用户状态
     * @return 分页用户列表
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<UserListResponse> getUsers(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "DESC") String direction,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) UUID departmentId,
        @RequestParam(required = false) UserStatus status
    ) {
        Sort sort = direction.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        UserListResponse response = userService.getUsers(pageable, keyword, departmentId, status);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取用户详情
     *
     * GET /api/users/{id}
     *
     * @param id 用户 ID
     * @return 用户详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<UserDTO> getUser(@PathVariable UUID id) {
        logger.info("获取用户详情：{}", id);
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * 更新用户
     *
     * PUT /api/users/{id}
     *
     * @param id      用户 ID
     * @param request 更新用户请求
     * @return 更新后的用户
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<UserDTO> updateUser(
        @PathVariable UUID id,
        @Valid @RequestBody UserUpdateRequest request
    ) {
        logger.info("更新用户：{}", id);
        UserDTO user = userService.updateUser(id, request);
        return ResponseEntity.ok(user);
    }

    /**
     * 删除用户
     *
     * DELETE /api/users/{id}
     *
     * @param id 用户 ID
     * @return 无内容
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        logger.info("删除用户：{}", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 更新用户状态
     *
     * PATCH /api/users/{id}/status
     *
     * @param id      用户 ID
     * @param request 更新状态请求
     * @return 更新后的用户
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<UserDTO> updateUserStatus(
        @PathVariable UUID id,
        @Valid @RequestBody UserStatusUpdateRequest request
    ) {
        logger.info("更新用户状态：{} -> {}", id, request.getStatus());
        UserDTO user = userService.updateUserStatus(id, request.getStatus());
        return ResponseEntity.ok(user);
    }

    /**
     * 重置用户密码
     *
     * POST /api/users/{id}/password/reset
     *
     * @param id      用户 ID
     * @param request 密码重置请求
     * @return 无内容
     */
    @PostMapping("/{id}/password/reset")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> resetPassword(
        @PathVariable UUID id,
        @Valid @RequestBody PasswordResetRequest request
    ) {
        logger.info("重置用户密码：{}", id);
        userService.resetPassword(id, request.getPassword());
        return ResponseEntity.ok().build();
    }
}
