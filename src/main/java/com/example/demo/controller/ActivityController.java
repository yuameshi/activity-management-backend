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
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;
import java.util.Map;
import java.util.Collections;

 // 活动相关接口：列出、查看、创建、更新、删除等。
@RestController
@RequestMapping("/api/activity")
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    // 获取所有活动
    @GetMapping("/list")
    public List<Activity> list(@RequestHeader(value = "Authorization", required = false) String auth, jakarta.servlet.http.HttpServletRequest request) {
        return activityService.getAll();
    }

    // 获取活动详情
    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id, @RequestHeader(value = "Authorization", required = false) String auth, jakarta.servlet.http.HttpServletRequest request) {
        Long userId = null;
        // 尝试解析 JWT 获取 userId（若不可用则忽略）
        try {
            if (auth != null && auth.startsWith("Bearer ")) {
                userId = com.example.demo.util.JwtUtil.parseToken(auth.substring(7)).get("id", Long.class);
            }
        } catch (Exception ignored) {}
        com.example.demo.util.OperationLogUtil.log(userId, String.format("获取活动详情，活动ID=%d", id), id, "Activity", request);

        if (id == null)
            return ResponseEntity.badRequest().body(Map.of("error", "id is required"));
        Activity r = activityService.getById(id);
        if (r == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("data", null));
        return ResponseEntity.ok(r);
    }

    // 创建活动
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody Activity activity, @RequestHeader(value = "Authorization", required = false) String auth, jakarta.servlet.http.HttpServletRequest request) {
        Long userId = null;
        try {
            if (auth != null && auth.startsWith("Bearer ")) {
                userId = com.example.demo.util.JwtUtil.parseToken(auth.substring(7)).get("id", Long.class);
            }
        } catch (Exception ignored) {}
        com.example.demo.util.OperationLogUtil.log(userId, "创建活动", null, "Activity", request);

        if (activity == null)
            return ResponseEntity.badRequest().body(Map.of("affected", 0));
        int res = activityService.createActivity(activity);
        return ResponseEntity.ok(Map.of("affected", res));
    }

    // 更新活动（将 path 中的 id 设置到 activity）
    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
            @RequestBody Activity activity,
            jakarta.servlet.http.HttpServletRequest request) {
        Long userId = null;
        try {
            String auth = request.getHeader("Authorization");
            if (auth != null && auth.startsWith("Bearer ")) {
                userId = com.example.demo.util.JwtUtil.parseToken(auth.substring(7)).get("id", Long.class);
            }
        } catch (Exception ignored) {}
        com.example.demo.util.OperationLogUtil.log(userId, String.format("更新活动，活动ID=%d", id), id, "Activity", request);

        if (id == null || activity == null)
            return ResponseEntity.badRequest().body(Map.of("affected", 0));
        activity.setId(id);
        int res = activityService.updateActivity(activity);
        return ResponseEntity.ok(Map.of("affected", res));
    }

    // 删除活动
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, @RequestHeader(value = "Authorization", required = false) String auth, jakarta.servlet.http.HttpServletRequest request) {
        Long userId = null;
        try {
            if (auth != null && auth.startsWith("Bearer ")) {
                userId = com.example.demo.util.JwtUtil.parseToken(auth.substring(7)).get("id", Long.class);
            }
        } catch (Exception ignored) {}
        com.example.demo.util.OperationLogUtil.log(userId, String.format("删除活动，活动ID=%d", id), id, "Activity", request);

        if (id == null)
            return ResponseEntity.badRequest().body(Map.of("affected", 0));
        int res = activityService.deleteById(id);
        return ResponseEntity.ok(Map.of("affected", res));
    }

    // 根据标题模糊搜索活动
    @GetMapping("/search")
    public List<Activity> searchByTitle(String title, @RequestHeader(value = "Authorization", required = false) String auth, jakarta.servlet.http.HttpServletRequest request) {
        return activityService.searchByTitle(title);
    }
}