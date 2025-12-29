package com.example.demo.service;

import com.example.demo.model.OperationLog;
import com.example.demo.mapper.OperationLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OperationLogService {

	@Autowired
	private OperationLogMapper operationLogMapper;

	public void addLog(OperationLog log) {
		System.out.println("[OperationLogService.addLog] userId=" + (log != null ? log.getUserId() : "null"));
		operationLogMapper.insert(log);
	}

	public List<OperationLog> getAllLogs() {
		return operationLogMapper.selectAll();
	}

	public List<OperationLog> getLogsByPage(int page, int size) {
		int offset = (page - 1) * size;
		return operationLogMapper.selectByPage(offset, size);
	}

	public int getLogCount() {
		return operationLogMapper.count();
	}
}