package com.example.demo.controller;

import com.example.demo.model.Activity;
import com.example.demo.model.Attendance;
import com.example.demo.model.User;
import com.example.demo.service.ActivityService;
import com.example.demo.service.AttendanceService;
import com.example.demo.service.UserService;
import com.example.demo.util.JwtUtil;

import io.jsonwebtoken.Claims;

import org.apache.commons.codec.binary.Base32;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

// 签到控制器 — 处理签到码生成、签到及管理员管理签到记录。
@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

	private final AttendanceService attendanceService;
	private final ActivityService activityService;
	private final UserService userService;
	private final String SALT = "SOME_RANDOM_SALT_VALUE";

	public AttendanceController(AttendanceService attendanceService, ActivityService activityService,
			UserService userService) {
		this.attendanceService = attendanceService;
		this.activityService = activityService;
		this.userService = userService;
	}

	// 生成基于 TOTP 的 6 位动态码
	public static String getTOTPCode(String secretKey) {
		try {
			long time = new Date().getTime() / (30 * 1000);
			byte[] key = new Base32().decode(secretKey);

			byte[] data = new byte[8];
			for (int i = 8; i-- > 0; time >>>= 8) {
				data[i] = (byte) time;
			}

			Mac mac = Mac.getInstance("HmacSHA1");
			mac.init(new SecretKeySpec(key, "HmacSHA1"));
			byte[] hash = mac.doFinal(data);

			int offset = hash[hash.length - 1] & 0xF;
			long truncatedHash = 0;
			for (int i = 0; i < 4; ++i) {
				truncatedHash <<= 8;
				truncatedHash |= (hash[offset + i] & 0xFF);
			}

			truncatedHash &= 0x7FFFFFFF;
			truncatedHash %= 1000000;

			return String.format("%06d", truncatedHash);
		} catch (NoSuchAlgorithmException | InvalidKeyException e) {
			throw new RuntimeException(e);
		}
	}

	// 根据活动ID查询签到记录，返回包含用户基本信息。
	@GetMapping("/activity/{activityId}")
	public List<Map<String, Object>> getByActivityId(@PathVariable Long activityId) {
		List<Map<String, Object>> attendanceListReturn = new ArrayList<>();
		List<Attendance> attendanceList = attendanceService.getByActivityId(activityId);
		for (Attendance attendance : attendanceList) {
			Map<String, Object> attendenceCopy = new HashMap<>();
			attendenceCopy.put("id", attendance.getId());
			attendenceCopy.put("activityId", attendance.getActivityId());
			attendenceCopy.put("userId", attendance.getUserId());
			attendenceCopy.put("signTime", attendance.getSignTime());
			User user = userService.getById(attendance.getUserId());
			attendenceCopy.put("username", user.getUsername());
			attendenceCopy.put("userRealName", user.getRealName());
			attendenceCopy.put("userAvatar", user.getAvatar());
			attendanceListReturn.add(attendenceCopy);
		}
		return attendanceListReturn;
	}

	// 通过 SSE 推送动态签到码（管理员）
	@GetMapping(value = "/generate", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public void generateAttendanceCode(
			HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam("aid") Long activityId) throws IOException {
		if (activityId == null || activityId <= 0) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "参数 activityId 必须为正整数且不能为空");
			return;
		}

		String authHeader = request.getHeader("Authorization");
		Long userId = null;
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "未提供有效的 JWT");
			return;
		}
		String token = authHeader.substring(7);
		Claims claims;
		try {
			claims = JwtUtil.parseToken(token);
			userId = claims.get("id", Long.class);
		} catch (Exception e) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT 无效");
			return;
		}
		Boolean isAdmin = claims.get("isAdmin", Boolean.class);
		if (isAdmin == null || !isAdmin) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "仅管理员可访问");
			return;
		}

		String logMsg = String.format("管理员生成签到码，用户ID=%d，活动ID=%d", userId, activityId);
		com.example.demo.util.OperationLogUtil.log(userId, logMsg, activityId, "Attendance", request);

		response.setContentType(MediaType.TEXT_EVENT_STREAM_VALUE);
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("Connection", "keep-alive");

		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			Activity act = activityService.getById(activityId);
			String input = act.getTitle();
			input = input.concat(SALT).concat(act.getDescription()).concat(SALT).concat(act.getLocation());
			byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
			StringBuilder hexString = new StringBuilder();
			for (byte b : hashBytes) {
				hexString.append(String.format("%02x", b));
			}
			String hashHex = hexString.toString();

			while (!response.isCommitted() || !response.getWriter().checkError()) {
				long epochSeconds = Instant.now().getEpochSecond();
				String code = getTOTPCode(hashHex);

				String timeStr = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
						.withZone(ZoneId.of("Asia/Shanghai"))
						.format(Instant.now());

				String data = String.format("{\"code\":\"%s\",\"time\":\"%s\",\"aid\":\"%s\"}", code, timeStr,
						activityId);

				response.getWriter().write("data: " + data + "\n\n");
				response.getWriter().flush();

				long sleepMillis = 30000 - (epochSeconds % 30) * 1000;
				if (sleepMillis < 500)
					sleepMillis = 500;
				Thread.sleep(sleepMillis);
			}
		} catch (Exception e) {
			// 忽略客户端断开等异常
		}
	}

	// 签到：支持 QR / MANUAL，两种方式。普通用户仅可为自己签到。
	@PostMapping("/sign")
	public ResponseEntity<Object> signAttendance(
			@RequestBody SignRequest req,
			HttpServletRequest request) throws NoSuchAlgorithmException {
		String authHeader = request.getHeader("Authorization");
		Long userId = null;
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			return ResponseEntity.status(401).body(error(401, "未提供有效的 JWT"));
		}
		String token = authHeader.substring(7);
		Claims claims;
		try {
			claims = JwtUtil.parseToken(token);
			userId = claims.get("id", Long.class);
		} catch (Exception e) {
			return ResponseEntity.status(401).body(error(401, "JWT 无效"));
		}
		Long jwtUserId = claims.get("id", Long.class);

		Long signUserId = jwtUserId;
		if (req.userId != null && !req.userId.equals(jwtUserId)) {
			return ResponseEntity.status(403).body(error(403, "无权为他人签到"));
		}
		if (!"QR".equals(req.signType) && !"MANUAL".equals(req.signType)) {
			return ResponseEntity.badRequest()
					.body(error(400, "签到方式 signType 必须为 QR 或 MANUAL"));
		}

		Activity act = activityService.getById(req.activityId);
		if ("QR".equals(req.signType)) {
			if (req.code == null || req.code.length() != 6) {
				return ResponseEntity.badRequest().body(error(400, "签到码无效"));
			}
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			String input = act.getTitle();
			input = input.concat(SALT).concat(act.getDescription()).concat(SALT).concat(act.getLocation());
			byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
			StringBuilder hexString = new StringBuilder();
			for (byte b : hashBytes) {
				hexString.append(String.format("%02x", b));
			}
			String hashHex = hexString.toString();
			boolean isCodeValid = req.code.equals(getTOTPCode(hashHex));
			if (!isCodeValid) {
				return ResponseEntity.badRequest().body(error(400, "签到码无效或过期"));
			}
		}

		if (attendanceService.getByUserIdAndActivityId(signUserId, req.activityId) != null) {
			String logMsg = String.format("用户签到，用户ID=%d，活动ID=%d（重复签到）", signUserId, req.activityId);
			com.example.demo.util.OperationLogUtil.log(userId, logMsg, null, "Attendance", request);
			return ResponseEntity.ok(ok(java.util.Map.of("message", "签到成功", "act", act)));
		}

		Attendance attendance = new Attendance();
		attendance.setActivityId(req.activityId);
		attendance.setUserId(signUserId);
		attendance.setSignTime(java.time.LocalDateTime.now());
		attendance.setSignType(req.signType);

		int rows = attendanceService.createAttendance(attendance);
		if (rows > 0) {
			String logMsg = String.format("用户签到，用户ID=%d，活动ID=%d，签到方式=%s", signUserId, req.activityId, req.signType);
			com.example.demo.util.OperationLogUtil.log(userId, logMsg, null, "Attendance", request);
			return ResponseEntity.ok(ok(java.util.Map.of("message", "签到成功", "act", act)));
		} else {
			return ResponseEntity.status(500).body(error(500, "签到失败"));
		}
	}

	// 管理员新增签到记录
	@PostMapping("/add")
	public ResponseEntity<Object> adminAddAttendance(@RequestBody Attendance attendance, HttpServletRequest request) {
		String authHeader = request.getHeader("Authorization");
		Long userId = null;
		Claims claims = null;
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			return ResponseEntity.status(401).body(error(401, "未提供有效的 JWT"));
		}
		String token = authHeader.substring(7);
		try {
			claims = JwtUtil.parseToken(token);
			userId = claims.get("id", Long.class);
		} catch (Exception e) {
			return ResponseEntity.status(401).body(error(401, "JWT 无效"));
		}
		Boolean isAdmin = claims.get("isAdmin", Boolean.class);
		if (isAdmin == null || !isAdmin) {
			com.example.demo.util.OperationLogUtil.log(userId, "管理员新增签到记录-鉴权失败", attendance.getId(), "Attendance",
					request);
			return ResponseEntity.status(403).body(error(403, "仅管理员可操作"));
		}
		Attendance createdAttendance = attendanceService.getByUserIdAndActivityId(attendance.getUserId(),
				attendance.getActivityId());
		if (createdAttendance == null) {
			attendanceService.createAttendance(attendance);
			createdAttendance = attendanceService.getByUserIdAndActivityId(attendance.getUserId(),
					attendance.getActivityId());
			if (createdAttendance == null) {
				return ResponseEntity.status(500).body(error(500, "添加失败"));
			} else {
				String logMsg = String.format("管理员新增签到记录，签到ID=%d，用户ID=%d，活动ID=%d",
						createdAttendance.getId(), createdAttendance.getUserId(), createdAttendance.getActivityId());
				com.example.demo.util.OperationLogUtil.log(userId, logMsg, createdAttendance.getId(), "Attendance",
						request);
				Map<String, Object> responseData = new HashMap<>();
				responseData.put("id", createdAttendance.getId());
				responseData.put("activityId", createdAttendance.getActivityId());
				responseData.put("userId", createdAttendance.getUserId());
				responseData.put("signTime", createdAttendance.getSignTime());
				User user = userService.getById(createdAttendance.getUserId());
				responseData.put("username", user.getUsername());
				responseData.put("userRealName", user.getRealName());
				responseData.put("userAvatar", user.getAvatar());
				return ResponseEntity.ok(ok(responseData));
			}
		} else {
			Map<String, Object> responseData = new HashMap<>();
			responseData.put("id", createdAttendance.getId());
			responseData.put("activityId", createdAttendance.getActivityId());
			responseData.put("userId", createdAttendance.getUserId());
			responseData.put("signTime", createdAttendance.getSignTime());
			User user = userService.getById(createdAttendance.getUserId());
			responseData.put("username", user.getUsername());
			responseData.put("userRealName", user.getRealName());
			responseData.put("userAvatar", user.getAvatar());
			return ResponseEntity.ok(ok(responseData));
		}
	}

	// 管理员删除签到记录
	@DeleteMapping("/delete")
	public ResponseEntity<Object> adminDeleteAttendance(@RequestParam Long id, HttpServletRequest request) {
		String authHeader = request.getHeader("Authorization");
		Long userId = null;
		Claims claims = null;
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			return ResponseEntity.status(401).body(error(401, "未提供有效的 JWT"));
		}
		String token = authHeader.substring(7);
		try {
			claims = JwtUtil.parseToken(token);
			userId = claims.get("id", Long.class);
		} catch (Exception e) {
			return ResponseEntity.status(401).body(error(401, "JWT 无效"));
		}
		Boolean isAdmin = claims.get("isAdmin", Boolean.class);
		if (isAdmin == null || !isAdmin) {
			com.example.demo.util.OperationLogUtil.log(userId, "管理员删除签到记录-鉴权失败", id, "Attendance", request);
			return ResponseEntity.status(403).body(error(403, "仅管理员可操作"));
		}
		int rows = attendanceService.deleteAttendanceById(id);
		if (rows > 0) {
			String logMsg = String.format("管理员删除签到记录，签到ID=%d", id);
			com.example.demo.util.OperationLogUtil.log(userId, logMsg, id, "Attendance", request);
			return ResponseEntity.ok(ok("签到记录删除成功"));
		} else {
			return ResponseEntity.status(404).body(error(404, "记录不存在或删除失败"));
		}
	}

	// 统一成功响应
	private Object ok(Object data) {
		return java.util.Collections.singletonMap("data", data);
	}

	// 统一错误响应
	private Object error(int code, String msg) {
		java.util.Map<String, Object> map = new java.util.HashMap<>();
		map.put("code", code);
		map.put("msg", msg);
		return map;
	}

	// 根据用户ID查询签到记录，返回活动名称和签到时间
	@GetMapping("/user/{userId}/records")
	public List<Map<String, Object>> getAttendanceRecordsByUserId(@PathVariable Long userId) {
		List<Map<String, Object>> result = new ArrayList<>();
		List<Attendance> attendanceList = attendanceService.getByUserId(userId);
		for (Attendance attendance : attendanceList) {
			Map<String, Object> map = new HashMap<>();
			Activity activity = activityService.getById(attendance.getActivityId());
			map.put("activityName", activity != null ? activity.getTitle() : null);
			map.put("signTime", attendance.getSignTime());
			result.add(map);
		}
		return result;
	}

	// 签到请求体
	public static class SignRequest {
		public Long activityId;
		public String code;
		public String signType;
		public Long userId;
	}
}