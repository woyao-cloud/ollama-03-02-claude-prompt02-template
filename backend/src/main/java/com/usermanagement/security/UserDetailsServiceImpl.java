package com.usermanagement.security;

import com.usermanagement.domain.Role;
import com.usermanagement.domain.User;
import com.usermanagement.domain.UserRole;
import com.usermanagement.repository.RoleRepository;
import com.usermanagement.repository.UserRepository;
import com.usermanagement.repository.UserRoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * UserDetailsService 实现 - 从数据库加载用户详情
 *
 * @author UserManagement Team
 * @since 1.0.0
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;

    public UserDetailsServiceImpl(UserRepository userRepository,
                                  UserRoleRepository userRoleRepository,
                                  RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        logger.debug("Loading user by email: {}", email);

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("用户不存在：" + email));

        // Load user roles from database
        List<UserRole> userRoles = userRoleRepository.findByUserId(user.getId());

        Set<GrantedAuthority> authorities = userRoles.stream()
            .map(UserRole::getRoleId)
            .map(roleRepository::findById)
            .filter(java.util.Optional::isPresent)
            .map(java.util.Optional::get)
            .filter(Role::isActive)
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getCode().replace("ROLE_", "")))
            .collect(Collectors.toSet());

        // Ensure at least ROLE_USER authority
        if (authorities.isEmpty()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        return new CustomUserDetails(
            user.getId().toString(),
            user.getEmail(),
            user.getDepartmentId(),
            authorities
        );
    }
}
