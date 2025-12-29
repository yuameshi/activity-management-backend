package com.example.demo.mapper;

import com.example.demo.model.Feedback;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FeedbackMapper {
	int insertFeedback(Feedback feedback);

	List<Feedback> listFeedbacks(@Param("activityId") Long activityId);
}