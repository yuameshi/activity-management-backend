package com.example.demo.mapper;

import com.example.demo.model.Attendance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 签到表 Mapper。
 */
@Mapper
public interface AttendanceMapper {
    /**
     * 插入签到记录
     * @param attendance 签到实体
     * @return 受影响行数
     */
    int insertAttendance(Attendance attendance);

    /**
     * 根据活动ID查询签到记录
     * @param activityId 活动ID
     * @return 签到记录列表
     */
    List<Attendance> findByActivityId(@Param("activityId") Long activityId);

    /**
     * 根据用户ID查询签到记录
     * @param userId 用户ID
     * @return 签到记录列表
     */
    List<Attendance> findByUserId(@Param("userId") Long userId);
    /**
     * 按活动ID和签到状态筛选签到记录
     * @param activityId 活动ID
     * @param status 状态（可选，null为全部）
     * @return 签到记录列表
     */
    List<Attendance> findByActivityIdAndStatus(@Param("activityId") Long activityId, @Param("status") String status);
}