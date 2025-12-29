package com.example.demo.service;

import com.example.demo.mapper.AttendanceMapper;
import com.example.demo.model.Attendance;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

 // Attendance service — operations for attendance records
 @Service
 @Transactional
 public class AttendanceService {
 
     private final AttendanceMapper attendanceMapper;
 
     public AttendanceService(AttendanceMapper attendanceMapper) {
         this.attendanceMapper = attendanceMapper;
     }
 
     // 创建签到记录
     public int createAttendance(Attendance attendance) {
         if (attendance == null)
             return 0;
         return attendanceMapper.insertAttendance(attendance);
     }
 
     // 根据活动ID查询签到记录
     public List<Attendance> getByActivityId(Long activityId) {
         if (activityId == null)
             return new ArrayList<>();
         List<Attendance> list = attendanceMapper.findByActivityId(activityId);
         return list == null ? new ArrayList<>() : list;
     }
 
     // 根据用户ID查询签到记录
     public List<Attendance> getByUserId(Long userId) {
         if (userId == null)
             return new ArrayList<>();
         List<Attendance> list = attendanceMapper.findByUserId(userId);
         return list == null ? new ArrayList<>() : list;
     }
 
     // 按活动ID和状态筛选签到记录
     public List<Attendance> getAttendanceListByActivityIdAndStatus(Long activityId, String status) {
         List<Attendance> list = attendanceMapper.findByActivityIdAndStatus(activityId, status);
         return list == null ? new ArrayList<>() : list;
     }
 
     // 根据用户ID和活动ID查找签到记录
     public Attendance getByUserIdAndActivityId(Long userId, Long activityId) {
         if (userId == null || activityId == null) return null;
         return attendanceMapper.findByUserIdAndActivityId(userId, activityId);
     }
 
     // 删除签到记录
     public int deleteAttendanceById(Long id) {
         if (id == null) return 0;
         return attendanceMapper.deleteAttendanceById(id);
     }
 }