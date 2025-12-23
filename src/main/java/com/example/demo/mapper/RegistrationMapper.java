package com.example.demo.mapper;

import com.example.demo.model.Registration;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RegistrationMapper {

    int insertRegistration(Registration registration);

    Registration findById(@Param("id") Long id);

    /**
     * 根据 userId 和 activityId 查找报名（用于检查是否已报名）
     */
    Registration findByUserAndActivity(@Param("userId") Long userId, @Param("activityId") Long activityId);

    /**
     * 获取某活动的报名列表
     */
    List<Registration> findByActivityId(@Param("activityId") Long activityId);

    /**
     * 获取某用户的报名列表
     */
    List<Registration> findByUserId(@Param("userId") Long userId);

    int deleteById(@Param("id") Long id);

    /**
     * 根据 userId 和 activityId 删除（用于用户取消自身报名）
     */
    int deleteByUserAndActivity(@Param("userId") Long userId, @Param("activityId") Long activityId);

    int updateRegistration(Registration registration);
}