package com.example.demo.service;

import com.example.demo.mapper.AttendanceMapper;
import com.example.demo.model.Attendance;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 签到服务层实现。
 */
@Service
@Transactional
public class AttendanceService {

    private final AttendanceMapper attendanceMapper;

    public AttendanceService(AttendanceMapper attendanceMapper) {
        this.attendanceMapper = attendanceMapper;
    }

    /**
     * 插入签到记录，若 createTime 为 null 则设置为当前时间。
     *
     * @param attendance 签到实体
     * @return 受影响行数
     */
    public int createAttendance(Attendance attendance) {
        if (attendance == null)
            return 0;
        return attendanceMapper.insertAttendance(attendance);
    }

    /**
     * 根据活动ID查询签到记录列表。
     *
     * @param activityId 活动ID
     * @return 签到记录列表（可能为空）
     */
    public List<Attendance> getByActivityId(Long activityId) {
        if (activityId == null)
            return new ArrayList<>();
        List<Attendance> list = attendanceMapper.findByActivityId(activityId);
        return list == null ? new ArrayList<>() : list;
    }

    /**
     * 根据用户ID查询签到记录列表。
     *
     * @param userId 用户ID
     * @return 签到记录列表（可能为空）
     */
    public List<Attendance> getByUserId(Long userId) {
        if (userId == null)
            return new ArrayList<>();
        List<Attendance> list = attendanceMapper.findByUserId(userId);
        return list == null ? new ArrayList<>() : list;
    }

    /**
     * 按活动ID和签到状态筛选签到记录，返回VO列表
     * 
     * @param activityId 活动ID
     * @param status     状态（可选，null为全部）
     * @return 签到VO列表
     */
    public List<Attendance> getAttendanceListByActivityIdAndStatus(Long activityId, String status) {
        List<Attendance> list = attendanceMapper.findByActivityIdAndStatus(activityId, status);
        return list == null ? new ArrayList<>() : list;
    }
}