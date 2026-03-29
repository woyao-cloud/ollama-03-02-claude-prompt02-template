package com.usermanagement.service;

import com.usermanagement.domain.UserStatus;
import com.usermanagement.web.dto.UserCreateRequest;
import com.usermanagement.web.dto.UserDTO;
import com.usermanagement.web.dto.UserListResponse;
import com.usermanagement.web.dto.UserUpdateRequest;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * 用户服务接口
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
public interface UserService {

    /**
     * 创建用户
     *
     * @param request 创建用户请求
     * @return 用户 DTO
     */
    UserDTO createUser(UserCreateRequest request);

    /**
     * 根据 ID 获取用户
     *
     * @param id 用户 ID
     * @return 用户 DTO
     */
    UserDTO getUserById(UUID id);

    /**
     * 获取用户列表（分页）
     *
     * @param pageable     分页参数
     * @param keyword      关键词（邮箱/姓名）
     * @param departmentId 部门 ID
     * @param status       用户状态
     * @return 分页用户列表
     */
    UserListResponse getUsers(Pageable pageable, String keyword, UUID departmentId, UserStatus status);

    /**
     * 更新用户
     *
     * @param id      用户 ID
     * @param request 更新用户请求
     * @return 用户 DTO
     */
    UserDTO updateUser(UUID id, UserUpdateRequest request);

    /**
     * 删除用户（软删除）
     *
     * @param id 用户 ID
     */
    void deleteUser(UUID id);

    /**
     * 更新用户状态
     *
     * @param id     用户 ID
     * @param status 用户状态
     * @return 用户 DTO
     */
    UserDTO updateUserStatus(UUID id, UserStatus status);

    /**
     * 重置用户密码
     *
     * @param id          用户 ID
     * @param newPassword 新密码
     */
    void resetPassword(UUID id, String newPassword);
}
