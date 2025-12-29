package com.example.demo.service;

import com.example.demo.mapper.ActivityMapper;
import com.example.demo.model.Activity;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

 // Activity service — CRUD for activities
 @Service
 @Transactional
 public class ActivityService {
 
     private final ActivityMapper activityMapper;
 
     public ActivityService(ActivityMapper activityMapper) {
         this.activityMapper = activityMapper;
     }
 
     // 创建活动（若 createTime 为空则设置为当前时间）
     public int createActivity(Activity activity) {
         if (activity == null)
             return 0;
         if (activity.getCreateTime() == null) {
             activity.setCreateTime(LocalDateTime.now());
         }
         return activityMapper.insertActivity(activity);
     }
 
     // 根据 id 获取活动
     public Activity getById(Long id) {
         if (id == null)
             return null;
         return activityMapper.findById(id);
     }
 
     // 获取所有活动
     public List<Activity> getAll() {
         List<Activity> all = activityMapper.findAll();
         return all == null ? new ArrayList<>() : all;
     }
 
     // 根据发布者 id 获取活动列表
     public List<Activity> getByPublisherId(Long publisherId) {
         if (publisherId == null)
             return new ArrayList<>();
         List<Activity> list = activityMapper.findByPublisherId(publisherId);
         return list == null ? new ArrayList<>() : list;
     }
 
     // 更新活动
     public int updateActivity(Activity activity) {
         if (activity == null)
             return 0;
         return activityMapper.updateActivity(activity);
     }
 
     // 删除活动
     public int deleteById(Long id) {
         if (id == null)
             return 0;
         return activityMapper.deleteById(id);
     }
 
     // 标题模糊搜索
     public List<Activity> searchByTitle(String title) {
         if (title == null || title.trim().isEmpty()) {
             return new ArrayList<>();
         }
         List<Activity> list = activityMapper.searchByTitle(title);
         return list == null ? new ArrayList<>() : list;
     }
 }