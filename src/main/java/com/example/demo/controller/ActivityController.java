package com.example.demo.controller;

import com.example.demo.service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.Map;

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
    public ResponseEntity<?> get(@org.springframework.web.bind.annotation.PathVariable Long id) {
        if (id == null) return ResponseEntity.badRequest().body(Map.of("error", "id is required"));
        com.example.demo.model.Activity r = activityService.getById(id);
        if (r == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("data", null));
        return ResponseEntity.ok(r);
    }

    /**
     * 创建活动
     *
     * @param activity 活动实体
     * @return 受影响行数
     */
    @PostMapping("/create")
    public ResponseEntity<?> create(@org.springframework.web.bind.annotation.RequestBody com.example.demo.model.Activity activity) {
        if (activity == null) return ResponseEntity.badRequest().body(Map.of("affected", 0));
        int res = activityService.createActivity(activity);
        return ResponseEntity.ok(Map.of("affected", res));
    }

    /**
     * 更新活动（将 path 中的 id 设置到 activity）
     *
     * @param id       活动 id
     * @param activity 活动实体（更新内容）
     * @return 受影响行数
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@org.springframework.web.bind.annotation.PathVariable Long id,
                                    @org.springframework.web.bind.annotation.RequestBody com.example.demo.model.Activity activity) {
        if (id == null || activity == null) return ResponseEntity.badRequest().body(Map.of("affected", 0));
        activity.setId(id);
        int res = activityService.updateActivity(activity);
        return ResponseEntity.ok(Map.of("affected", res));
    }

    /**
     * 删除活动
     *
     * @param id 活动 id
     * @return 受影响行数
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@org.springframework.web.bind.annotation.PathVariable Long id) {
        if (id == null) return ResponseEntity.badRequest().body(Map.of("affected", 0));
        int res = activityService.deleteById(id);
        return ResponseEntity.ok(Map.of("affected", res));
    }

    /**
     * 审核活动：设置状态并更新
     *
     * @param id     活动 id
     * @param status 新状态
     * @return 受影响行数
     */
    @PostMapping("/audit/{id}")
    public ResponseEntity<?> audit(@org.springframework.web.bind.annotation.PathVariable Long id,
                                   @org.springframework.web.bind.annotation.RequestParam Byte status) {
        if (id == null || status == null) return ResponseEntity.badRequest().body(Map.of("affected", 0));
        int res = activityService.auditActivity(id, status);
        return ResponseEntity.ok(Map.of("affected", res));
    }
}