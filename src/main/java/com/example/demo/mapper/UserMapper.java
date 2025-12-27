package com.example.demo.mapper;

import com.example.demo.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {
    int insertUser(User user);

    User findByUsername(@Param("username") String username);

    User findById(@Param("id") Long id);

    List<User> findAll();

    int updateUser(User user);

    int setUserRole(@Param("id") Long id, @Param("role") int role);

    int setUserStatus(@Param("id") Long id, @Param("status") Byte status);

    /**
     * 查询指定用户是否为管理员（user_role表中存在user_id=userId且role_id=1的记录）
     * 
     * @param userId 用户ID
     * @return 记录数（大于0表示是管理员）
     */
    int countAdminRoleByUserId(@Param("userId") Long userId);
    int deleteUserById(@Param("id") Long id);
}