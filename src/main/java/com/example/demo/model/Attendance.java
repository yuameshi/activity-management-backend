package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.time.LocalDateTime;

 // 签到表实体类（序列化忽略 null 字段）
@JsonInclude(Include.NON_NULL)
public class Attendance {
    // 签到记录ID
    private Long id;

    // 活动ID
    private Long activityId;

    // 用户ID
    private Long userId;

    // 签到时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime signTime;

    // 签到方式，"QR" 或 "MANUAL"
    private String signType;

    public Attendance() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDateTime getSignTime() {
        return signTime;
    }

    public void setSignTime(LocalDateTime signTime) {
        this.signTime = signTime;
    }

    public String getSignType() {
        return signType;
    }

    public void setSignType(String signType) {
        this.signType = signType;
    }

    @Override
    public String toString() {
        return "Attendance{" +
                "id=" + id +
                ", activityId=" + activityId +
                ", userId=" + userId +
                ", signTime=" + signTime +
                ", signType=" + signType +
                '}';
    }
}