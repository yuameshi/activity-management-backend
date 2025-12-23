package com.example.demo.controller;

import com.example.demo.service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 活动控制器：列表/详情/创建/更新/删除/审核
 */
@RestController
@RequestMapping("/api/activity")
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    /**
     * 获取所有活动
     *
     * @return 活动列表
     */
    @GetMapping("/list")
    public java.util.List<com.example.demo.model.Activity> list() {
        return activityService.getAll();
    }

    /**
     * 根据 id 获取活动详情
     *
     * @param id 活动 id
     * @return 活动或 null（若 id 为 null 或不存在）
     */
    @GetMapping("/{id}")
    public com.example.demo.model.Activity get(@org.springframework.web.bind.annotation.PathVariable Long id) {
        if (id == null) return null;
        return activityService.getById(id);
    }

    /**
     * 创建活动
     *
     * @param activity 活动实体
     * @return 受影响行数
     */
    @PostMapping("/create")
    public int create(@org.springframework.web.bind.annotation.RequestBody com.example.demo.model.Activity activity) {
        if (activity == null) return 0;
        return activityService.createActivity(activity);
    }

    /**
     * 更新活动（将 path 中的 id 设置到 activity）
     *
     * @param id       活动 id
     * @param activity 活动实体（更新内容）
     * @return 受影响行数
     */
    @PutMapping("/update/{id}")
    public int update(@org.springframework.web.bind.annotation.PathVariable Long id,
                      @org.springframework.web.bind.annotation.RequestBody com.example.demo.model.Activity activity) {
        if (id == null || activity == null) return 0;
        activity.setId(id);
        return activityService.updateActivity(activity);
    }

    /**
     * 删除活动
     *
     * @param id 活动 id
     * @return 受影响行数
     */
    @DeleteMapping("/delete/{id}")
    public int delete(@org.springframework.web.bind.annotation.PathVariable Long id) {
        if (id == null) return 0;
        return activityService.deleteById(id);
    }

    /**
     * 审核活动：设置状态并更新
     *
     * @param id     活动 id
     * @param status 新状态
     * @return 受影响行数
     */
    @PostMapping("/audit/{id}")
    public int audit(@org.springframework.web.bind.annotation.PathVariable Long id,
                     @org.springframework.web.bind.annotation.RequestParam Byte status) {
        if (id == null || status == null) return 0;
        return activityService.auditActivity(id, status);
    }
}