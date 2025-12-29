package com.example.demo.mapper;

import com.example.demo.model.UserStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserStatusMapper {
	@Select("SELECT id, name FROM user_status")
	List<UserStatus> selectAll();
}