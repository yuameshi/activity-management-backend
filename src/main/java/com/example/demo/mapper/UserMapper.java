package com.example.demo.mapper;

import com.example.demo.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {
	int insertUser(User user);

	User findByUsername(@Param("username") String username);

	List<User> searchByUsername(@Param("username") String username);

	// 通过 query 同时模糊搜索用户名和真实姓名
	List<User> searchByQuery(@Param("query") String query);

	User findById(@Param("id") Long id);

	List<User> findAll();

	int updateUser(User user);

	int setUserRole(@Param("id") Long id, @Param("role") int role);

	int setUserStatus(@Param("id") Long id, @Param("status") Byte status);

	// 获取用户角色（1=管理员，2=普通用户）
	int getRoleByUserId(@Param("userId") Long userId);

	int deleteUserById(@Param("id") Long id);
}