package com.example.demo.controller;

import com.example.demo.model.OperationLog;
import com.example.demo.service.OperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/operation-log")
public class OperationLogController {

	@Autowired
	private OperationLogService operationLogService;

	// 管理员查询操作日志（分页）
	@GetMapping("/list")
	public Map<String, Object> list(
			@RequestHeader(value = "Authorization", required = false) String auth,
			@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "10") int size,
			jakarta.servlet.http.HttpServletRequest request) {
		Long userId = null;
		@SuppressWarnings("unused")
		boolean forbidden = false;
		try {
			if (auth == null || !auth.startsWith("Bearer ")) {
				forbidden = true;
				throw new IllegalArgumentException("missing or invalid Authorization header");
			}
			io.jsonwebtoken.Claims claims = com.example.demo.util.JwtUtil
					.parseToken(auth.substring("Bearer ".length()));
			userId = claims.get("id", Long.class);
			Boolean isAdmin = claims.get("isAdmin", Boolean.class);
			if (isAdmin == null || !isAdmin) {
				forbidden = true;
				throw new SecurityException("forbidden");
			}
		} catch (Exception e) {
			com.example.demo.util.OperationLogUtil.log(userId, "管理员访问操作日志-鉴权失败", null, "OperationLog", request);
			throw new org.springframework.web.server.ResponseStatusException(
					org.springframework.http.HttpStatus.FORBIDDEN, "仅管理员可访问");
		}
		List<OperationLog> logs = operationLogService.getLogsByPage(page, size);
		int total = operationLogService.getLogCount();
		Map<String, Object> result = new HashMap<>();
		result.put("data", logs);
		result.put("total", total);
		result.put("page", page);
		result.put("size", size);
		com.example.demo.util.OperationLogUtil.log(userId, "管理员访问操作日志", null, "OperationLog", request);
		return result;
	}

	// 导出日志为文本
	@GetMapping("/export")
	public String exportAllLogs(
			@RequestHeader(value = "Authorization", required = false) String auth,
			jakarta.servlet.http.HttpServletRequest request) {
		Long userId = null;
		@SuppressWarnings("unused")
		boolean forbidden = false;
		try {
			if (auth == null || !auth.startsWith("Bearer ")) {
				forbidden = true;
				throw new IllegalArgumentException("missing or invalid Authorization header");
			}
			io.jsonwebtoken.Claims claims = com.example.demo.util.JwtUtil
					.parseToken(auth.substring("Bearer ".length()));
			userId = claims.get("id", Long.class);
			Boolean isAdmin = claims.get("isAdmin", Boolean.class);
			if (isAdmin == null || !isAdmin) {
				forbidden = true;
				throw new SecurityException("forbidden");
			}
		} catch (Exception e) {
			com.example.demo.util.OperationLogUtil.log(userId, "管理员导出操作日志-鉴权失败", null, "OperationLog", request);
			throw new org.springframework.web.server.ResponseStatusException(
					org.springframework.http.HttpStatus.FORBIDDEN, "仅管理员可访问");
		}
		List<OperationLog> logs = operationLogService.getAllLogs();
		StringBuilder sb = new StringBuilder();
		sb.append("ID\t用户ID\t操作\t目标ID\t目标类型\tIP\t时间\n");
		for (OperationLog log : logs) {
			sb.append(log.getId()).append("\t")
					.append(log.getUserId()).append("\t")
					.append(log.getOperation()).append("\t")
					.append(log.getTargetId()).append("\t")
					.append(log.getTargetType()).append("\t")
					.append(log.getIpAddress()).append("\t")
					.append(log.getCreateTime()).append("\n");
		}
		com.example.demo.util.OperationLogUtil.log(userId, "管理员导出操作日志", null, "OperationLog", request);
		return sb.toString();
	}
}