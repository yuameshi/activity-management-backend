package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

/**
 * 认证相关接口：注册与登录
 */
@RestController
@RequestMapping("/api")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/user/register")
    public ResponseEntity<?> register(@RequestBody User user, jakarta.servlet.http.HttpServletRequest request) {
        try {
            User created = userService.register(user);
            com.example.demo.util.OperationLogUtil.log(created.getId(), "用户注册", created.getId(), "User", request);
            // 适配avatar字段
            return ResponseEntity.status(HttpStatus.CREATED).body(new User() {{
                setId(created.getId());
                setUsername(created.getUsername());
                setRealName(created.getRealName());
                setEmail(created.getEmail());
                setPhone(created.getPhone());
                setAvatar(created.getAvatar());
                setStatus(created.getStatus());
                setCreateTime(created.getCreateTime());
                setRole(created.getRole());
            }});
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req, jakarta.servlet.http.HttpServletRequest request) {
        try {
            Map<String, Object> result = userService.login(req.getUsername(), req.getPassword());
            // 检查用户状态
            if (result.containsKey("user") && result.get("user") instanceof User) {
                User u = (User) result.get("user");
                if (u.getStatus() == null || u.getStatus() != 1) {
                    // 状态不是1，禁止登录且不返回token和其他信息
                    com.example.demo.util.OperationLogUtil.log(u.getId(), "用户登录-状态异常", u.getId(), "User", request);
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "用户状态异常，禁止登录"));
                }
                // 只在状态为1时返回token和其他信息
                result.put("user", new User() {{
                    setId(u.getId());
                    setUsername(u.getUsername());
                    setRealName(u.getRealName());
                    setEmail(u.getEmail());
                    setPhone(u.getPhone());
                    setAvatar(u.getAvatar());
                    setStatus(u.getStatus());
                    setCreateTime(u.getCreateTime());
                    setRole(u.getRole());
                }});
                com.example.demo.util.OperationLogUtil.log(u.getId(), "用户登录", u.getId(), "User", request);
                return ResponseEntity.ok(result);
            }
            // 理论上不会走到这里
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "登录失败"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", ex.getMessage()));
        }
    }

    public static class LoginRequest {
        private String username;
        private String password;

        public LoginRequest() {
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}