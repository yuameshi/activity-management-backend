package com.example.demo.mapper;

import com.example.demo.model.Attendance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

 // 签到表 Mapper
@Mapper
public interface AttendanceMapper {
    // 插入签到记录，返回受影响行数
    int insertAttendance(Attendance attendance);

    // 根据活动ID查询签到记录列表
    List<Attendance> findByActivityId(@Param("activityId") Long activityId);

    // 根据用户ID查询签到记录列表
    List<Attendance> findByUserId(@Param("userId") Long userId);
    // 按活动ID和状态筛选签到记录
    List<Attendance> findByActivityIdAndStatus(@Param("activityId") Long activityId, @Param("status") String status);
    // 根据ID删除签到记录，返回受影响行数
    int deleteAttendanceById(@Param("id") Long id);
    // 根据用户ID与活动ID查找签到记录
    Attendance findByUserIdAndActivityId(@Param("userId") Long userId, @Param("activityId") Long activityId);
}