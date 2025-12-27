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

    /**
     * 支持通过 query 同时模糊搜索用户名和真实姓名
     */
    List<User> searchByQuery(@Param("query") String query);

    User findById(@Param("id") Long id);

    List<User> findAll();

    int updateUser(User user);

    int setUserRole(@Param("id") Long id, @Param("role") int role);

    int setUserStatus(@Param("id") Long id, @Param("status") Byte status);

    /**
     * user中role为1表示管理员，2表示普通用户
     * 
     * @param userId 用户ID
     * @return 角色id
     */
    int getRoleByUserId(@Param("userId") Long userId);

    int deleteUserById(@Param("id") Long id);
}