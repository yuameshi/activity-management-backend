package com.example.demo.service;

import com.example.demo.mapper.FeedbackMapper;
import com.example.demo.model.Feedback;
import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@Service
public class FeedbackService {

    @Autowired
    private FeedbackMapper feedbackMapper;

    public void submitFeedback(Feedback feedback) {
        feedbackMapper.insertFeedback(feedback);
    }

    public List<Feedback> listFeedbacks(Long activityId) {
        return feedbackMapper.listFeedbacks(activityId);
    }
}