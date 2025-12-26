package com.example.demo.controller;

import com.example.demo.model.Activity;
import com.example.demo.model.Attendance;
import com.example.demo.service.ActivityService;
import com.example.demo.service.AttendanceService;
import com.example.demo.util.JwtUtil;

import io.jsonwebtoken.Claims;

import org.apache.commons.codec.binary.Base32;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
import java.util.Date;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * 签到相关接口。
 */
@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final ActivityService activityService;
    private final String SALT = "SOME_RANDOM_SALT_VALUE";

    public AttendanceController(AttendanceService attendanceService, ActivityService activityService) {
        this.attendanceService = attendanceService;
        this.activityService = activityService;
    }

    public static String getTOTPCode(String secretKey) {
        try {
            long time = new Date().getTime() / (30 * 1000); // 30秒窗口
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

    /**
     * 根据活动ID查询签到记录列表。
     * 
     * @param activityId 活动ID
     * @return 签到记录列表
     */
    @GetMapping("/activity/{activityId}")
    public List<Attendance> getByActivityId(@PathVariable Long activityId) {
        return attendanceService.getByActivityId(activityId);
    }

    /**
     * 通过 SSE 推送动态签到码，仅管理员可访问。
     *
     * @param request    HTTP 请求
     * @param response   HTTP 响应
     * @param activityId 活动ID，类型为 Long，必填。用于指定当前签到码所属的活动，仅做参数校验，不做业务处理。
     */
    @GetMapping(value = "/generate", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public void generateAttendanceCode(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam("aid") Long activityId) throws IOException {
        // activityId 基本校验：非空且为正整数
        if (activityId == null || activityId <= 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "参数 activityId 必须为正整数且不能为空");
            return;
        }

        // 1. 校验 JWT 并判断角色
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "未提供有效的 JWT");
            return;
        }
        String token = authHeader.substring(7);
        Claims claims;
        try {
            claims = JwtUtil.parseToken(token);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT 无效");
            return;
        }
        Boolean isAdmin = claims.get("isAdmin", Boolean.class);
        if (isAdmin == null || !isAdmin) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "仅管理员可访问");
            return;
        }

        // 2. SSE 响应设置
        response.setContentType(MediaType.TEXT_EVENT_STREAM_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");

        // 3. 推送动态签到码
        try {
            // SHA-256
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

                String data = String.format("{\"code\":\"%s\",\"time\":\"%s\",\\\"aid\\\":\\\"%s\\\"}", code, timeStr,
                        activityId);

                response.getWriter().write("data: " + data + "\n\n");
                response.getWriter().flush();

                // 计算距离下一个 30 秒的剩余时间，精准刷新
                long sleepMillis = 30000 - (epochSeconds % 30) * 1000;
                if (sleepMillis < 500)
                    sleepMillis = 500; // 防止极短睡眠
                Thread.sleep(sleepMillis);
            }
        } catch (Exception e) {
            // 客户端断开等异常无需处理
        }
    }

    /**
     * 签到请求体
     */
    public static class SignRequest {
        /** 活动ID */
        public Long activityId;
        /** 签到码，仅扫码签到时必填 */
        public String code;
        /** 签到方式，"QR" 或 "MANUAL" */
        public String signType;
        /** 指定签到用户，仅管理员可用 */
        public Long userId;
    }

    /**
     * 签到接口：支持扫码与手动签到，校验码有效性，写入数据库。
     * 普通用户只能为自己签到，管理员可为他人签到。
     */
    @PostMapping("/sign")
    /**
     * 签到接口：支持扫码与手动签到，校验码有效性，写入数据库。
     * 普通用户只能为自己签到，管理员可为他人签到。
     * 
     * @param req     签到请求体
     * @param request HTTP 请求
     * @return 统一响应对象
     */
    public org.springframework.http.ResponseEntity<Object> signAttendance(
            @RequestBody SignRequest req,
            HttpServletRequest request) throws NoSuchAlgorithmException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return org.springframework.http.ResponseEntity.status(401).body(error(401, "未提供有效的 JWT"));
        }
        String token = authHeader.substring(7);
        Claims claims;
        try {
            claims = JwtUtil.parseToken(token);
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.status(401).body(error(401, "JWT 无效"));
        }
        Long jwtUserId = claims.get("id", Long.class);
        Boolean isAdmin = claims.get("isAdmin", Boolean.class);

        Long signUserId = jwtUserId;
        if (isAdmin != null && isAdmin) {
            signUserId = req.userId;
        } else {
            if (req.userId != null && !req.userId.equals(jwtUserId))
                return org.springframework.http.ResponseEntity.status(403).body(error(403, "无权为他人签到"));
        }
        if (!"QR".equals(req.signType) && !"MANUAL".equals(req.signType)) {
            return org.springframework.http.ResponseEntity.badRequest()
                    .body(error(400, "签到方式 signType 必须为 QR 或 MANUAL"));
        }

        Activity act = activityService.getById(req.activityId);
        if ("QR".equals(req.signType)) {
            if (req.code == null || req.code.length() != 6) {
                return org.springframework.http.ResponseEntity.badRequest().body(error(400, "签到码无效"));
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
                return org.springframework.http.ResponseEntity.badRequest().body(error(400, "签到码无效或过期"));
            }
        }

        Attendance attendance = new Attendance();
        attendance.setActivityId(req.activityId);
        attendance.setUserId(signUserId);
        attendance.setSignTime(java.time.LocalDateTime.now());
        attendance.setSignType(req.signType);

        int rows = attendanceService.createAttendance(attendance);
        if (rows > 0) {
            return org.springframework.http.ResponseEntity.ok(ok(java.util.Map.of("message", "签到成功", "act", act)));
        } else {
            return org.springframework.http.ResponseEntity.status(500).body(error(500, "签到失败"));
        }
    }

    /** 统一成功响应 */
    private Object ok(Object data) {
        return java.util.Collections.singletonMap("data", data);
    }

    /** 统一错误响应 */
    private Object error(int code, String msg) {
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("code", code);
        map.put("msg", msg);
        return map;
    }

    /**
     * 查询指定活动的签到列表，支持按状态筛选，仅管理员可访问。
     * 
     * @param activityId 活动ID
     * @param status     签到状态（可选，已签/迟到/未签等）
     * @param request    HTTP请求
     * @return 统一响应对象
     */
    @GetMapping("/list/{activityId}")
    public org.springframework.http.ResponseEntity<Object> listAttendanceByActivityId(
            @PathVariable Long activityId,
            @RequestParam(value = "status", required = false) String status,
            HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return org.springframework.http.ResponseEntity.status(401).body(error(401, "未提供有效的 JWT"));
        }
        String token = authHeader.substring(7);
        Claims claims;
        try {
            claims = JwtUtil.parseToken(token);
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.status(401).body(error(401, "JWT 无效"));
        }
        Boolean isAdmin = claims.get("isAdmin", Boolean.class);
        if (isAdmin == null || !isAdmin) {
            return org.springframework.http.ResponseEntity.status(403).body(error(403, "仅管理员可访问"));
        }
        List<Attendance> list = attendanceService
                .getAttendanceListByActivityIdAndStatus(activityId, status);
        return org.springframework.http.ResponseEntity.ok(ok(list));
    }
}