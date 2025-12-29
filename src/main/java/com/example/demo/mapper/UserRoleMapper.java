package com.example.demo.mapper;

import com.example.demo.model.UserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserRoleMapper {
	@Select("SELECT id, name, description FROM user_role")
	List<UserRole> selectAll();
}