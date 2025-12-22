package com.example.demo.service;

import com.example.demo.mapper.UserMapper;
import com.example.demo.model.User;
import com.example.demo.util.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
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
            user.setStatus((byte)1);
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
        String token = JwtUtil.generateToken(user.getId(), user.getUsername());
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
        return result;
    }
}