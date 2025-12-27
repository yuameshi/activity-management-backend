package com.example.demo.controller;

import com.example.demo.model.Feedback;
import com.example.demo.service.FeedbackService;
import com.example.demo.service.UserService;
import com.example.demo.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private UserService userService;

    // 提交反馈/建议，需鉴权
    @PostMapping("/submit")
    public Object submitFeedback(@RequestBody Feedback feedback, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            return new org.springframework.http.ResponseEntity<>("未登录",
                    org.springframework.http.HttpStatus.UNAUTHORIZED);
        }
        try {
            Claims claims = JwtUtil.parseToken(token.replace("Bearer ", ""));
            Long userId = claims.get("id", Long.class);
            feedback.setUserId(userId);
            feedbackService.submitFeedback(feedback);
            return "反馈提交成功";
        } catch (Exception e) {
            return new org.springframework.http.ResponseEntity<>("Token无效",
                    org.springframework.http.HttpStatus.UNAUTHORIZED);
        }
    }

    // 仅管理员可列出反馈
    @GetMapping("/list")
    public Object listFeedbacks(@RequestParam(required = false) Long activityId, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            return new org.springframework.http.ResponseEntity<>("未登录",
                    org.springframework.http.HttpStatus.UNAUTHORIZED);
        }
        try {
            Claims claims = JwtUtil.parseToken(token.replace("Bearer ", ""));
            Long userId = claims.get("id", Long.class);
            Boolean isAdmin = claims.get("isAdmin", Boolean.class);
            // 双重校验，防止token被伪造
            if (!Boolean.TRUE.equals(isAdmin) && !userService.isAdmin(userId)) {
                return new org.springframework.http.ResponseEntity<>("无权限",
                        org.springframework.http.HttpStatus.FORBIDDEN);
            }
            return feedbackService.listFeedbacks(activityId);
        } catch (Exception e) {
            return new org.springframework.http.ResponseEntity<>("Token无效",
                    org.springframework.http.HttpStatus.UNAUTHORIZED);
        }
    }
}