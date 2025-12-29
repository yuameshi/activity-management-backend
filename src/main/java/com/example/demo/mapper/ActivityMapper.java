package com.example.demo.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.demo.model.Activity;

@Mapper
public interface ActivityMapper {

    int insertActivity(com.example.demo.model.Activity activity);

    Activity findById(@Param("id") Long id);

    java.util.List<Activity> findAll();

    java.util.List<Activity> findByPublisherId(@Param("publisherId") Long publisherId);

    int updateActivity(Activity activity);

    int deleteById(@Param("id") Long id);

    // 根据标题模糊搜索活动
    java.util.List<Activity> searchByTitle(@Param("title") String title);
}