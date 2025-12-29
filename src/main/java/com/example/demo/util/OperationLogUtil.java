package com.example.demo.util;

import com.example.demo.model.OperationLog;
import com.example.demo.service.OperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class OperationLogUtil {

	private static OperationLogService operationLogService;

	@Autowired
	public OperationLogUtil(OperationLogService operationLogService) {
		OperationLogUtil.operationLogService = operationLogService;
	}

	public static void log(Long userId, String operation, Long targetId, String targetType,
			HttpServletRequest request) {
		String ip = request != null ? request.getRemoteAddr() : null;
		if ("0:0:0:0:0:0:0:1".equals(ip)) {
			ip = "127.0.0.1";
		}
		System.out.println("[OperationLogUtil.log] userId=" + userId + ", operation=" + operation + ", targetId="
				+ targetId + ", targetType=" + targetType + ", ip=" + ip);
		OperationLog log = new OperationLog(userId, operation, targetId, targetType, ip);
		if (operationLogService != null) {
			operationLogService.addLog(log);
		} else {
			System.out.println("[OperationLogUtil] operationLogService is null, skipping persist");
		}
	}
}