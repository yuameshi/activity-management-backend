package com.example.demo.service;

import com.example.demo.mapper.UserMapper;
import com.example.demo.model.User;
import com.example.demo.util.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.demo.model.Image;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    private ImageService imageService;

    /**
     * 根据用户名获取用户（安全视图，不返回密码）
     */
    /**
     * 支持通过 query 同时模糊搜索用户名和真实姓名（安全视图，不返回密码）
     */
    public List<User> searchByQuery(String query) {
        if (query == null || query.isEmpty()) {
            throw new IllegalArgumentException("query参数不能为空");
        }
        List<User> users = userMapper.searchByQuery(query);
        List<User> safeUsers = new ArrayList<>();
        for (User u : users) {
            User safe = new User();
            safe.setId(u.getId());
            safe.setUsername(u.getUsername());
            safe.setRealName(u.getRealName());
            safe.setEmail(u.getEmail());
            safe.setPhone(u.getPhone());
            safe.setAvatar(u.getAvatar());
            safe.setStatus(u.getStatus());
            safe.setCreateTime(u.getCreateTime());
            safe.setRole(u.getRole());
            safeUsers.add(safe);
        }
        return safeUsers;
    }

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
        if (user.getEmail() == null || user.getRealName() == null || user.getPhone() == null) {
            throw new IllegalArgumentException("email, realName, phone are required");
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
        user.setRole(2); // 2 是学生
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
        resp.setRole(user.getRole());
        return resp;
    }

    /**
     * 管理员专用：修改用户所有字段（除ID）
     */
    public User adminUpdateUser(Long id, User update) {
        if (id == null || update == null) {
            throw new IllegalArgumentException("id and update data required");
        }
        User user = userMapper.findById(id);
        if (user == null) {
            throw new IllegalArgumentException("user not found");
        }
        if (update.getUsername() != null)
            user.setUsername(update.getUsername());
        if (update.getPassword() != null && !update.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(update.getPassword()));
        }
        if (update.getRealName() != null)
            user.setRealName(update.getRealName());
        if (update.getEmail() != null)
            user.setEmail(update.getEmail());
        if (update.getPhone() != null)
            user.setPhone(update.getPhone());
        if (update.getAvatar() != null)
            user.setAvatar(update.getAvatar());
        if (update.getStatus() != null)
            user.setStatus(update.getStatus());
        if (update.getRole() != 0)
            user.setRole(update.getRole());
        userMapper.updateUser(user);
        User safe = new User();
        safe.setId(user.getId());
        safe.setUsername(user.getUsername());
        safe.setRealName(user.getRealName());
        safe.setEmail(user.getEmail());
        safe.setPhone(user.getPhone());
        safe.setAvatar(user.getAvatar());
        safe.setStatus(user.getStatus());
        safe.setCreateTime(user.getCreateTime());
        safe.setRole(user.getRole());
        return safe;
    }

    /**
     * 仅管理员新建用户（可指定角色）
     */
    public User createUserByAdmin(User user) {
        if (user == null || user.getUsername() == null || user.getPassword() == null
                || user.getEmail() == null || user.getRealName() == null
                || user.getPhone() == null) {
            throw new IllegalArgumentException("email, username, realName, phone, password are required");
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
        // 返回安全视图
        User resp = new User();
        resp.setId(user.getId());
        resp.setUsername(user.getUsername());
        resp.setRealName(user.getRealName());
        resp.setEmail(user.getEmail());
        resp.setPhone(user.getPhone());
        resp.setAvatar(user.getAvatar());
        resp.setStatus(user.getStatus());
        resp.setCreateTime(user.getCreateTime());
        resp.setRole(user.getRole());
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
        safe.setAvatar(user.getAvatar());
        safe.setStatus(user.getStatus());
        safe.setCreateTime(user.getCreateTime());
        safe.setRole(user.getRole());
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
        safe.setRole(u.getRole());
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
            safe.setRole(u.getRole());
            out.add(safe);
        }
        return out;
    }

    public User updateUser(Long requesterId, String requesterUsername, Long targetId, User update) {
        if (requesterId == null || requesterUsername == null || targetId == null || update == null) {
            throw new IllegalArgumentException("invalid parameters");
        }
        User target = userMapper.findById(targetId);
        if (target == null) {
            throw new IllegalArgumentException("user not found");
        }
        // 只允许修改 username, realName, email, phone, password
        if (update.getUsername() != null)
            target.setUsername(update.getUsername());
        if (update.getRealName() != null)
            target.setRealName(update.getRealName());
        if (update.getEmail() != null)
            target.setEmail(update.getEmail());
        if (update.getPhone() != null)
            target.setPhone(update.getPhone());
        if (update.getPassword() != null && !update.getPassword().isEmpty()) {
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
        safe.setRole(target.getRole());
        return safe;
    }

    public boolean isAdmin(Long userId) {
        if (userId == null) {
            return false;
        }
        // 调用mapper方法查询
        int role = userMapper.getRoleByUserId(userId);
        return role == 1;
    }

    /**
     * 删除用户（仅管理员可用）
     */
    public void deleteUserById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id required");
        }
        userMapper.deleteUserById(id);
    }

    /**
     * 管理员设置用户角色
     */
    public User setUserRole(Long userId, int role) {
        if (userId == null) {
            throw new IllegalArgumentException("id required");
        }
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("user not found");
        }
        user.setRole(role);
        userMapper.updateUser(user);
        // 返回安全视图
        User safe = new User();
        safe.setId(user.getId());
        safe.setUsername(user.getUsername());
        safe.setRealName(user.getRealName());
        safe.setEmail(user.getEmail());
        safe.setPhone(user.getPhone());
        safe.setAvatar(user.getAvatar());
        safe.setStatus(user.getStatus());
        safe.setCreateTime(user.getCreateTime());
        safe.setRole(user.getRole());
        return safe;
    }

    /**
     * 管理员设置用户状态
     */
    public User setUserStatus(Long userId, Byte status) {
        if (userId == null) {
            throw new IllegalArgumentException("id required");
        }
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("user not found");
        }
        user.setStatus(status);
        userMapper.updateUser(user);
        // 返回安全视图
        User safe = new User();
        safe.setId(user.getId());
        safe.setUsername(user.getUsername());
        safe.setRealName(user.getRealName());
        safe.setEmail(user.getEmail());
        safe.setPhone(user.getPhone());
        safe.setAvatar(user.getAvatar());
        safe.setStatus(user.getStatus());
        safe.setCreateTime(user.getCreateTime());
        safe.setRole(user.getRole());
        return safe;
    }

    /**
     * 用户更新头像
     * 
     * @param userId  用户ID
     * @param imageId 图片ID
     * @return 更新后的用户安全视图
     */
    public User updateAvatar(Long userId, Integer imageId) {
        if (userId == null || imageId == null) {
            throw new IllegalArgumentException("userId和imageId不能为空");
        }
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        Image image = imageService.getImageById(imageId);
        if (image == null) {
            throw new IllegalArgumentException("图片不存在");
        }
        user.setAvatar(image.getPath());
        userMapper.updateUser(user);

        User safe = new User();
        safe.setId(user.getId());
        safe.setUsername(user.getUsername());
        safe.setRealName(user.getRealName());
        safe.setEmail(user.getEmail());
        safe.setPhone(user.getPhone());
        safe.setAvatar(user.getAvatar());
        safe.setStatus(user.getStatus());
        safe.setCreateTime(user.getCreateTime());
        safe.setRole(user.getRole());
        return safe;
    }
}