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

// 用户接口：提供信息查询、个人修改和管理员管理操作。
@RestController
@RequestMapping("/api/user")
public class UserController {

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

	// 管理员按用户名搜索用户
	@GetMapping("/admin_search")
	public ResponseEntity<?> searchUser(
			@RequestHeader(value = "Authorization", required = false) String auth,
			@RequestParam(value = "query", required = false) String query,
			jakarta.servlet.http.HttpServletRequest request) {
		try {
			Claims claims = parseAuth(auth);
			Boolean isAdmin = claims.get("isAdmin", Boolean.class);
			Long userId = claims.get("id", Long.class);
			if (isAdmin == null || !isAdmin) {
				com.example.demo.util.OperationLogUtil.log(userId, "管理员搜索用户-鉴权失败", null, "User", request);
				return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "forbidden"));
			}
			if (query == null || query.isEmpty()) {
				return ResponseEntity.badRequest().body(Map.of("error", "query参数不能为空"));
			}
			List<User> users = userService.searchByQuery(query);
			String logMsg = String.format("管理员搜索用户，搜索值：%s，结果数量：%d", query, users != null ? users.size() : 0);
			com.example.demo.util.OperationLogUtil.log(userId, logMsg, null, "User", request);
			return ResponseEntity.ok(users);
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", ex.getMessage()));
		} catch (Exception ex) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", ex.getMessage()));
		}
	}

	// 获取用户信息，非管理员查看他人时只返回有限字段
	@GetMapping("/info")
	public ResponseEntity<?> info(@RequestHeader(value = "Authorization", required = false) String auth,
			@RequestParam(value = "id", required = false) Long id) {
		try {
			Claims claims = parseAuth(auth);
			Long requesterId = ((Number) claims.get("id")).longValue();
			Boolean isAdmin = claims.get("isAdmin", Boolean.class);

			Long targetId = id == null ? requesterId : id;
			User u = userService.getById(targetId);
			if (u == null)
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "user not found"));
			if ((isAdmin == null || !isAdmin) && !requesterId.equals(targetId)) {
				Map<String, Object> limited = Map.of(
						"realName", u.getRealName(),
						"email", u.getEmail(),
						"createTime", u.getCreateTime());
				return ResponseEntity.ok(limited);
			}
			return ResponseEntity.ok(u);
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", ex.getMessage()));
		} catch (Exception ex) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", ex.getMessage()));
		}
	}

	// 仅允许本人修改指定字段
	@PutMapping("/update")
	public ResponseEntity<?> update(@RequestHeader(value = "Authorization", required = false) String auth,
			@RequestBody User update) {
		try {
			Claims claims = parseAuth(auth);
			Long requesterId = ((Number) claims.get("id")).longValue();
			String requesterUsername = (String) claims.get("username");

			Long tid = requesterId;
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

	// 管理员获取用户列表
	@GetMapping("/list")
	public ResponseEntity<?> list(@RequestHeader(value = "Authorization", required = false) String auth,
			jakarta.servlet.http.HttpServletRequest request) {
		try {
			Claims claims = parseAuth(auth);
			Boolean isAdmin = claims.get("isAdmin", Boolean.class);
			Long userId = claims.get("id", Long.class);
			if (isAdmin == null || !isAdmin) {
				com.example.demo.util.OperationLogUtil.log(userId, "管理员获取用户列表-鉴权失败", null, "User", request);
				return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "forbidden"));
			}
			List<User> users = userService.listUsers();
			com.example.demo.util.OperationLogUtil.log(userId, "管理员获取用户列表", null, "User", request);
			return ResponseEntity.ok(users);
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", ex.getMessage()));
		} catch (Exception ex) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", ex.getMessage()));
		}
	}

	// 管理员新建用户
	@PostMapping("/create")
	public ResponseEntity<?> createUser(@RequestHeader(value = "Authorization", required = false) String auth,
			@RequestBody User user,
			jakarta.servlet.http.HttpServletRequest request) {
		try {
			Claims claims = parseAuth(auth);
			Boolean isAdmin = claims.get("isAdmin", Boolean.class);
			Long userId = claims.get("id", Long.class);
			if (isAdmin == null || !isAdmin) {
				com.example.demo.util.OperationLogUtil.log(userId, "管理员新建用户-鉴权失败", null, "User", request);
				return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "forbidden"));
			}
			User created = userService.createUserByAdmin(user);
			String logMsg = String.format("管理员新建用户，用户ID=%d", created.getId());
			com.example.demo.util.OperationLogUtil.log(userId, logMsg, created.getId(), "User", request);
			return ResponseEntity.ok(created);
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
		} catch (Exception ex) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", ex.getMessage()));
		}
	}

	// 管理员删除用户
	@DeleteMapping("/delete")
	public ResponseEntity<?> delete(@RequestHeader(value = "Authorization", required = false) String auth,
			@RequestParam(value = "id") Long id,
			jakarta.servlet.http.HttpServletRequest request) {
		try {
			Claims claims = parseAuth(auth);
			Boolean isAdmin = claims.get("isAdmin", Boolean.class);
			Long userId = claims.get("id", Long.class);
			if (isAdmin == null || !isAdmin) {
				com.example.demo.util.OperationLogUtil.log(userId, "管理员删除用户-鉴权失败", id, "User", request);
				return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "forbidden"));
			}
			userService.deleteUserById(id);
			String logMsg = String.format("管理员删除用户，用户ID=%d", id);
			com.example.demo.util.OperationLogUtil.log(userId, logMsg, id, "User", request);
			return ResponseEntity.ok(Map.of("success", true));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
		} catch (Exception ex) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", ex.getMessage()));
		}
	}

	// 管理员修改用户（除ID）
	@PutMapping("/{id}/admin-update")
	public ResponseEntity<?> adminUpdateUser(@RequestHeader(value = "Authorization", required = false) String auth,
			@PathVariable Long id,
			@RequestBody User update,
			jakarta.servlet.http.HttpServletRequest request) {
		try {
			Claims claims = parseAuth(auth);
			Boolean isAdmin = claims.get("isAdmin", Boolean.class);
			Long userId = claims.get("id", Long.class);
			if (isAdmin == null || !isAdmin) {
				com.example.demo.util.OperationLogUtil.log(userId, "管理员修改用户信息-鉴权失败", id, "User", request);
				return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "forbidden"));
			}
			User updated = userService.adminUpdateUser(id, update);
			com.example.demo.util.OperationLogUtil.log(userId, "管理员修改用户信息", id, "User", request);
			return ResponseEntity.ok(updated);
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
		} catch (Exception ex) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", ex.getMessage()));
		}
	}

	// 管理员设置用户角色
	@PutMapping("/{id}/set-role")
	public ResponseEntity<?> setRole(@RequestHeader(value = "Authorization", required = false) String auth,
			@PathVariable("id") Long id,
			@RequestParam("role") int role,
			jakarta.servlet.http.HttpServletRequest request) {
		try {
			Claims claims = parseAuth(auth);
			Boolean isAdmin = claims.get("isAdmin", Boolean.class);
			Long userId = claims.get("id", Long.class);
			if (isAdmin == null || !isAdmin) {
				com.example.demo.util.OperationLogUtil.log(userId, "管理员设置用户角色-鉴权失败", id, "User", request);
				return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "forbidden"));
			}
			User updated = userService.setUserRole(id, role);
			String logMsg = String.format("管理员设置用户角色，用户ID=%d，目标角色ID=%d", id, role);
			com.example.demo.util.OperationLogUtil.log(userId, logMsg, id, "User", request);
			return ResponseEntity.ok(updated);
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
		} catch (Exception ex) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", ex.getMessage()));
		}
	}

	// 管理员设置用户状态
	@PutMapping("/{id}/set-status")
	public ResponseEntity<?> setStatus(@RequestHeader(value = "Authorization", required = false) String auth,
			@PathVariable("id") Long id,
			@RequestParam("status") Byte status,
			jakarta.servlet.http.HttpServletRequest request) {
		try {
			Claims claims = parseAuth(auth);
			Boolean isAdmin = claims.get("isAdmin", Boolean.class);
			Long userId = claims.get("id", Long.class);
			if (isAdmin == null || !isAdmin) {
				com.example.demo.util.OperationLogUtil.log(userId, "管理员设置用户状态-鉴权失败", id, "User", request);
				return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "forbidden"));
			}
			User updated = userService.setUserStatus(id, status);
			String logMsg = String.format("管理员设置用户状态，用户ID=%d，目标状态ID=%d", id, status);
			com.example.demo.util.OperationLogUtil.log(userId, logMsg, id, "User", request);
			return ResponseEntity.ok(updated);
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
		} catch (Exception ex) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", ex.getMessage()));
		}
	}

	// 用户或管理员更新头像；管理员可指定 userId
	@PutMapping("/update-avatar")
	public ResponseEntity<?> updateAvatar(
			@RequestHeader(value = "Authorization", required = false) String auth,
			@RequestParam("imageId") Integer imageId,
			@RequestParam(value = "userId", required = false) Long userId) {
		try {
			Claims claims = parseAuth(auth);
			Long requesterId = ((Number) claims.get("id")).longValue();
			Boolean isAdmin = claims.get("isAdmin", Boolean.class);

			Long targetUserId = userId != null ? userId : requesterId;

			if ((isAdmin == null || !isAdmin) && !requesterId.equals(targetUserId)) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "forbidden"));
			}

			User updated = userService.updateAvatar(targetUserId, imageId);
			return ResponseEntity.ok(updated);
		} catch (IllegalArgumentException ex) {
			String msg = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();
			if (msg.contains("forbidden")) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", ex.getMessage()));
			} else if (msg.contains("not found") || msg.contains("不存在")) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
			} else if (msg.contains("missing") || msg.contains("required") || msg.contains("invalid")
					|| msg.contains("不能为空")) {
				return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
			} else {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
			}
		} catch (Exception ex) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", ex.getMessage()));
		}
	}
}