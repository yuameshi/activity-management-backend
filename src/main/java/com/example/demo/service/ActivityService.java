package com.example.demo.service;

import com.example.demo.mapper.ActivityMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 活动服务层实现。
 */
@Service
@Transactional
public class ActivityService {

    private final ActivityMapper activityMapper;

    public ActivityService(ActivityMapper activityMapper) {
        this.activityMapper = activityMapper;
    }

    /**
     * 插入活动，若 createTime 为 null 则设置为当前时间。
     *
     * @param activity 活动实体
     * @return 受影响行数
     */
    public int createActivity(com.example.demo.model.Activity activity) {
        if (activity == null) return 0;
        if (activity.getCreateTime() == null) {
            activity.setCreateTime(LocalDateTime.now());
        }
        return activityMapper.insertActivity(activity);
    }

    /**
     * 根据 id 获取活动。
     *
     * @param id 活动 id
     * @return 活动或 null
     */
    public com.example.demo.model.Activity getById(Long id) {
        if (id == null) return null;
        return activityMapper.findById(id);
    }

    /**
     * 获取所有活动列表。
     *
     * @return 活动列表（可能为空）
     */
    public java.util.List<com.example.demo.model.Activity> getAll() {
        List<com.example.demo.model.Activity> all = activityMapper.findAll();
        return all == null ? new ArrayList<>() : all;
    }

    /**
     * 根据发布者 id 获取活动列表。
     *
     * @param publisherId 发布者 id
     * @return 活动列表（可能为空）
     */
    public java.util.List<com.example.demo.model.Activity> getByPublisherId(Long publisherId) {
        if (publisherId == null) return new ArrayList<>();
        List<com.example.demo.model.Activity> list = activityMapper.findByPublisherId(publisherId);
        return list == null ? new ArrayList<>() : list;
    }

    /**
     * 更新活动。
     *
     * @param activity 活动实体
     * @return 受影响行数
     */
    public int updateActivity(com.example.demo.model.Activity activity) {
        if (activity == null) return 0;
        return activityMapper.updateActivity(activity);
    }

    /**
     * 根据 id 删除活动。
     *
     * @param id 活动 id
     * @return 受影响行数
     */
    public int deleteById(Long id) {
        if (id == null) return 0;
        return activityMapper.deleteById(id);
    }

    /**
     * 审核活动：设置状态并更新。若活动不存在返回 0。
     *
     * @param id     活动 id
     * @param status 新状态
     * @return updateActivity 的返回值或 0
     */
    public int auditActivity(Long id, Byte status) {
        if (id == null) return 0;
        com.example.demo.model.Activity activity = activityMapper.findById(id);
        if (activity == null) return 0;
        activity.setStatus(status);
        return updateActivity(activity);
    }
}