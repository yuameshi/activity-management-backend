package com.example.demo.model;

import java.util.Date;

public class OperationLog {
    private Long id;
    private Long userId;
    private String operation;
    private Long targetId;
    private String targetType;
    private String ipAddress;
    private Date createTime;

    public OperationLog() {}

    public OperationLog(Long userId, String operation, Long targetId, String targetType, String ipAddress) {
        this.userId = userId;
        this.operation = operation;
        this.targetId = targetId;
        this.targetType = targetType;
        this.ipAddress = ipAddress;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}