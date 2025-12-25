package com.example.demo.controller;

import com.example.demo.model.Registration;
import com.example.demo.service.RegistrationService;
import com.example.demo.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 报名相关接口：
 * - GET /api/registration/{activityId} 查看自身报名状态（返回报名记录或空体）
 * - POST /api/registration/check/{activityId} 检查是否已报名（返回 boolean）
 * - POST /api/registration/{activityId}/apply 给自身报名（管理员可以在 body 指定 userId）
 * - DELETE /api/registration/cancel/{id} 取消指定报名 id（默认只能取消自身，管理员可取消任何）
 * - GET /api/registration/list/{activityId} 报名列表（仅管理员）
 * - DELETE /api/registration/delete/{id} 删除报名记录（仅管理员）
 */
@RestController
@RequestMapping("/api/registration")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    private Claims parseAuth(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("missing or invalid Authorization header");
        }
        String token = authHeader.substring("Bearer ".length());
        return JwtUtil.parseToken(token);
    }

    @GetMapping("/{activityId}")
    public ResponseEntity<?> getSelfRegistration(@RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable Long activityId) {
        try {
            Claims claims = parseAuth(auth);
            Long requesterId = ((Number) claims.get("id")).longValue();
            Registration r = registrationService.getByUserAndActivity(requesterId, activityId);
            if (r == null)
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("id", null));
            return ResponseEntity.ok(r);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/check/{activityId}")
    public ResponseEntity<?> checkRegistered(@RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable Long activityId) {
        try {
            Claims claims = parseAuth(auth);
            Long requesterId = ((Number) claims.get("id")).longValue();
            boolean registered = registrationService.isRegistered(requesterId, activityId);
            return ResponseEntity.ok(registered);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/{activityId}/apply")
    public ResponseEntity<?> apply(@RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable Long activityId,
            @RequestBody(required = false) Registration body) {
        try {
            Claims claims = parseAuth(auth);
            Long requesterId = ((Number) claims.get("id")).longValue();
            String requesterUsername = (String) claims.get("username");
            boolean isAdmin = "admin".equalsIgnoreCase(requesterUsername);

            Long userId = requesterId;
            if (isAdmin && body != null && body.getUserId() != null) {
                userId = body.getUserId();
            }

            Registration reg = new Registration();
            reg.setUserId(userId);
            reg.setActivityId(activityId);
            if (body != null && body.getStatus() != null)
                reg.setStatus(body.getStatus());
            int res = registrationService.createRegistration(reg);
            if (res > 0) {
                Registration exist = registrationService.getByUserAndActivity(userId, activityId);
                return ResponseEntity.ok(exist);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "failed to create registration"));
            }
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", ex.getMessage()));
        }
    }

    @DeleteMapping("/cancel/{id}")
    public ResponseEntity<?> cancel(@RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable Long id) {
        try {
            Claims claims = parseAuth(auth);
            Long requesterId = ((Number) claims.get("id")).longValue();
            String requesterUsername = (String) claims.get("username");
            boolean isAdmin = "admin".equalsIgnoreCase(requesterUsername);

            Registration target = registrationService.getById(id);
            if (target == null) {
                // 若数据库中不存在记录，视为已取消（幂等）
                return ResponseEntity.ok(Map.of("cancelled", true));
            }
            if (!isAdmin && !requesterId.equals(target.getUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "forbidden"));
            }
            int r = registrationService.deleteById(id);
            boolean cancelled = r > 0;
            if (!cancelled) {
                // 若删除未影响行数，检查是否已被取消或已不存在
                if (target.getStatus() != null && target.getStatus() == 0) {
                    cancelled = true;
                } else {
                    Registration now = registrationService.getById(id);
                    if (now == null)
                        cancelled = true;
                }
            }
            return ResponseEntity.ok(Map.of("cancelled", cancelled));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping("/{activityId}/list-applications")
    public ResponseEntity<?> listByActivity(@RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable Long activityId) {
        try {
            Claims claims = parseAuth(auth);
            String requesterUsername = (String) claims.get("username");
            boolean isAdmin = "admin".equalsIgnoreCase(requesterUsername);
            if (!isAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "forbidden"));
            }
            List<Registration> list = registrationService.listByActivityId(activityId);
            return ResponseEntity.ok(list);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", ex.getMessage()));
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable Long id) {
        try {
            Claims claims = parseAuth(auth);
            String requesterUsername = (String) claims.get("username");
            boolean isAdmin = "admin".equalsIgnoreCase(requesterUsername);
            if (!isAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "forbidden"));
            }
            int r = registrationService.deleteById(id);
            boolean cancelled = r > 0;
            if (!cancelled) {
                // 若未删除成功，二次确认记录是否已不存在
                Registration now = registrationService.getById(id);
                if (now == null)
                    cancelled = true;
            }
            return ResponseEntity.ok(Map.of("cancelled", cancelled));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", ex.getMessage()));
        }
    }
}