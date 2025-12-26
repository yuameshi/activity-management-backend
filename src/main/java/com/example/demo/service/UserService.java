package com.example.demo.service;

import com.example.demo.mapper.UserMapper;
import com.example.demo.model.User;
import com.example.demo.util.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * 注册新用户，username 必须唯一，password 会被 BCrypt 哈希
     */
    public User register(User user) {
        if (user == null || user.getUsername() == null || user.getPassword() == null) {
            throw new IllegalArgumentException("username and password are required");
        }
        User exist = userMapper.findByUsername(user.getUsername());
        if (exist != null) {
            throw new IllegalArgumentException("username already exists");
        }
        String hashed = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashed);
        if (user.getStatus() == null) {
            user.setStatus((byte) 1);
        }
        user.setCreateTime(LocalDateTime.now());
        userMapper.insertUser(user);
        // build a safe response object (do not expose password)
        User resp = new User();
        resp.setId(user.getId());
        resp.setUsername(user.getUsername());
        resp.setRealName(user.getRealName());
        resp.setEmail(user.getEmail());
        resp.setPhone(user.getPhone());
        resp.setAvatar(user.getAvatar());
        resp.setStatus(user.getStatus());
        resp.setCreateTime(user.getCreateTime());
        return resp;
    }

    /**
     * 登录校验，成功返回包含 token 与用户信息的 map
     */
    public Map<String, Object> login(String username, String password) {
        if (username == null || password == null) {
            throw new IllegalArgumentException("username and password are required");
        }
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("invalid username or password");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("invalid username or password");
        }
        Boolean isAdmin = this.isAdmin(user.getId());
        String token = JwtUtil.generateToken(user.getId(), user.getUsername(), isAdmin);
        Map<String, Object> result = new HashMap<>();
        // return basic user info without password
        User safe = new User();
        safe.setId(user.getId());
        safe.setUsername(user.getUsername());
        safe.setRealName(user.getRealName());
        safe.setEmail(user.getEmail());
        safe.setPhone(user.getPhone());
        result.put("token", token);
        result.put("user", safe);
        result.put("isAdmin", isAdmin);
        return result;
    }

    /**
     * 根据 id 获取用户（安全视图：不返回密码）
     */
    public User getById(Long id) {
        if (id == null)
            throw new IllegalArgumentException("id required");
        User u = userMapper.findById(id);
        if (u == null)
            return null;
        User safe = new User();
        safe.setId(u.getId());
        safe.setUsername(u.getUsername());
        safe.setRealName(u.getRealName());
        safe.setEmail(u.getEmail());
        safe.setPhone(u.getPhone());
        safe.setAvatar(u.getAvatar());
        safe.setStatus(u.getStatus());
        safe.setCreateTime(u.getCreateTime());
        return safe;
    }

    /**
     * 列出所有用户（安全视图）
     */
    public List<User> listUsers() {
        List<User> all = userMapper.findAll();
        List<User> out = new ArrayList<>();
        if (all == null)
            return out;
        for (User u : all) {
            User safe = new User();
            safe.setId(u.getId());
            safe.setUsername(u.getUsername());
            safe.setRealName(u.getRealName());
            safe.setEmail(u.getEmail());
            safe.setPhone(u.getPhone());
            safe.setAvatar(u.getAvatar());
            safe.setStatus(u.getStatus());
            safe.setCreateTime(u.getCreateTime());
            out.add(safe);
        }
        return out;
    }

    /**
     * 更新用户信息。仅允许管理员或用户本人修改。
     * 简单 RBAC：用户名为 "admin" 的用户视为管理员。
     */
    public User updateUser(Long requesterId, String requesterUsername, Long targetId, User update) {
        if (requesterId == null || requesterUsername == null || targetId == null || update == null) {
            throw new IllegalArgumentException("invalid parameters");
        }
        User target = userMapper.findById(targetId);
        if (target == null) {
            throw new IllegalArgumentException("user not found");
        }
        boolean isAdmin = "admin".equalsIgnoreCase(requesterUsername);
        if (!isAdmin && !requesterId.equals(targetId)) {
            throw new IllegalArgumentException("forbidden: only admin or owner can modify");
        }
        // apply allowed updates
        if (update.getRealName() != null)
            target.setRealName(update.getRealName());
        if (update.getEmail() != null)
            target.setEmail(update.getEmail());
        if (update.getPhone() != null)
            target.setPhone(update.getPhone());
        if (update.getAvatar() != null)
            target.setAvatar(update.getAvatar());
        if (update.getStatus() != null && isAdmin) {
            // only admin can change status
            target.setStatus(update.getStatus());
        }
        if (update.getPassword() != null && !update.getPassword().isEmpty()) {
            // allow password change for owner or admin
            String hashed = passwordEncoder.encode(update.getPassword());
            target.setPassword(hashed);
        }
        userMapper.updateUser(target);
        // return safe view
        User safe = new User();
        safe.setId(target.getId());
        safe.setUsername(target.getUsername());
        safe.setRealName(target.getRealName());
        safe.setEmail(target.getEmail());
        safe.setPhone(target.getPhone());
        safe.setAvatar(target.getAvatar());
        safe.setStatus(target.getStatus());
        safe.setCreateTime(target.getCreateTime());
        return safe;
    }

    public boolean isAdmin(Long userId) {
        if (userId == null) {
            return false;
        }
        // 调用mapper方法查询
        int count = userMapper.countAdminRoleByUserId(userId);
        return count > 0;
    }
}