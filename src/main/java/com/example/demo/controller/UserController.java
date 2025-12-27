package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.service.UserService;
import com.example.demo.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户相关接口：信息查询、修改、列表（管理员）
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    /**
     * 按用户名搜索用户（仅管理员）
     */
    @GetMapping("/admin_search")
    public ResponseEntity<?> searchUser(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestParam(value = "query", required = false) String query) {
        try {
            Claims claims = parseAuth(auth);
            Boolean isAdmin = claims.get("isAdmin", Boolean.class);
            if (isAdmin == null || !isAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "forbidden"));
            }
            if (query == null || query.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "query参数不能为空"));
            }
            List<User> users = userService.searchByQuery(query);
            return ResponseEntity.ok(users);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", ex.getMessage()));
        }
    }

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    private Claims parseAuth(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("missing or invalid Authorization header");
        }
        String token = authHeader.substring("Bearer ".length());
        return JwtUtil.parseToken(token);
    }

    /**
     * 获取用户信息
     * 可选参数 id：管理员可以查询任意用户，普通用户只能查询本人。不传 id 则返回当前登录用户信息。
     */
    @GetMapping("/info")
    public ResponseEntity<?> info(@RequestHeader(value = "Authorization", required = false) String auth,
            @RequestParam(value = "id", required = false) Long id) {
        try {
            Claims claims = parseAuth(auth);
            Long requesterId = ((Number) claims.get("id")).longValue();
            Boolean isAdmin = claims.get("isAdmin", Boolean.class);

            Long targetId = id == null ? requesterId : id;
            if ((isAdmin == null || !isAdmin) && !requesterId.equals(targetId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "forbidden"));
            }
            User u = userService.getById(targetId);
            if (u == null)
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "user not found"));
            return ResponseEntity.ok(u);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", ex.getMessage()));
        }
    }

    /**
     * 修改用户信息（仅管理员或本人）
     * 请求体为用户的可修改字段（realName, email, phone, avatar, password, status）
     * 管理员可修改 status；普通用户不能修改 status。
     * 通过 query param targetId 指定要修改的用户（管理员可以指定，普通用户忽略并仅修改自己）
     */
    @PutMapping("/update")
    public ResponseEntity<?> update(@RequestHeader(value = "Authorization", required = false) String auth,
            @RequestParam(value = "targetId", required = false) Long targetId,
            @RequestBody User update) {
        try {
            Claims claims = parseAuth(auth);
            Long requesterId = ((Number) claims.get("id")).longValue();
            String requesterUsername = (String) claims.get("username");
            Boolean isAdmin = claims.get("isAdmin", Boolean.class);
            Long tid = targetId == null ? requesterId : targetId;

            // 前置权限校验：非管理员且非本人禁止修改 —— 使控制器在 service 未模拟抛异常时仍然能返回 403（满足单元测试预期）
            if ((isAdmin == null || !isAdmin) && !requesterId.equals(tid)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "forbidden"));
            }

            User updated = userService.updateUser(requesterId, requesterUsername, tid, update);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException ex) {
            String msg = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();
            if (msg.contains("forbidden")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", ex.getMessage()));
            } else if (msg.contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
            } else if (msg.contains("missing") || msg.contains("required") || msg.contains("invalid")) {
                return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
            }
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", ex.getMessage()));
        }
    }

    /**
     * 管理员获取用户列表
     */
    @GetMapping("/list")
    public ResponseEntity<?> list(@RequestHeader(value = "Authorization", required = false) String auth) {
        try {
            Claims claims = parseAuth(auth);
            Boolean isAdmin = claims.get("isAdmin", Boolean.class);
            if (isAdmin == null || !isAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "forbidden"));
            }
            List<User> users = userService.listUsers();
            return ResponseEntity.ok(users);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", ex.getMessage()));
        }
    }

    /**
     * 新建用户（仅管理员）
     */
    @PostMapping("/create")
    public ResponseEntity<?> createUser(@RequestHeader(value = "Authorization", required = false) String auth,
            @RequestBody User user) {
        try {
            Claims claims = parseAuth(auth);
            Boolean isAdmin = claims.get("isAdmin", Boolean.class);
            if (isAdmin == null || !isAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "forbidden"));
            }
            User created = userService.createUserByAdmin(user);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", ex.getMessage()));
        }
    }

    /**
     * 删除用户（仅管理员）
     */
    @DeleteMapping("/delete")
    public ResponseEntity<?> delete(@RequestHeader(value = "Authorization", required = false) String auth,
            @RequestParam(value = "id") Long id) {
        try {
            Claims claims = parseAuth(auth);
            Boolean isAdmin = claims.get("isAdmin", Boolean.class);
            if (isAdmin == null || !isAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "forbidden"));
            }
            userService.deleteUserById(id);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", ex.getMessage()));
        }
    }

    /**
     * 管理员专用：修改用户所有字段（除ID）
     */
    @PutMapping("/{id}/admin-update")
    public ResponseEntity<?> adminUpdateUser(@RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable Long id,
            @RequestBody User update) {
        try {
            Claims claims = parseAuth(auth);
            Boolean isAdmin = claims.get("isAdmin", Boolean.class);
            if (isAdmin == null || !isAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "forbidden"));
            }
            User updated = userService.adminUpdateUser(id, update);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", ex.getMessage()));
        }
    }

    /**
     * 管理员设置用户角色
     */
    @PutMapping("/{id}/set-role")
    public ResponseEntity<?> setRole(@RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable("id") Long id,
            @RequestParam("role") int role) {
        try {
            Claims claims = parseAuth(auth);
            Boolean isAdmin = claims.get("isAdmin", Boolean.class);
            if (isAdmin == null || !isAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "forbidden"));
            }
            User updated = userService.setUserRole(id, role);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", ex.getMessage()));
        }
    }

    /**
     * 管理员设置用户状态
     */
    @PutMapping("/{id}/set-status")
    public ResponseEntity<?> setStatus(@RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable("id") Long id,
            @RequestParam("status") Byte status) {
        try {
            Claims claims = parseAuth(auth);
            Boolean isAdmin = claims.get("isAdmin", Boolean.class);
            if (isAdmin == null || !isAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "forbidden"));
            }
            User updated = userService.setUserStatus(id, status);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", ex.getMessage()));
        }
    }
}