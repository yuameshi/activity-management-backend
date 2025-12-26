package com.example.demo.controller;

import com.example.demo.model.Activity;
import com.example.demo.service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;
import java.util.Collections;

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
    public List<Activity> list() {
        return activityService.getAll();
    }

    /**
     * 根据 id 获取活动详情
     *
     * @param id 活动 id
     * @return 活动或 null（若 id 为 null 或不存在）
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        if (id == null)
            return ResponseEntity.badRequest().body(Map.of("error", "id is required"));
        Activity r = activityService.getById(id);
        if (r == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("data", null));
        return ResponseEntity.ok(r);
    }

    /**
     * 创建活动
     *
     * @param activity 活动实体
     * @return 受影响行数
     */
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody Activity activity) {
        if (activity == null)
            return ResponseEntity.badRequest().body(Map.of("affected", 0));
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
    public ResponseEntity<?> update(@PathVariable Long id,
            @RequestBody Activity activity) {
        if (id == null || activity == null)
            return ResponseEntity.badRequest().body(Map.of("affected", 0));
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
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (id == null)
            return ResponseEntity.badRequest().body(Map.of("affected", 0));
        int res = activityService.deleteById(id);
        return ResponseEntity.ok(Map.of("affected", res));
    }

    /**
     * 根据标题模糊搜索活动
     * 
     * @param title 标题关键字
     * @return 活动列表
     */
    @GetMapping("/search")
    public List<Activity> searchByTitle(String title) {
        return activityService.searchByTitle(title);
    }
}