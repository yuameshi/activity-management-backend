package com.example.demo.service;

import com.example.demo.mapper.UserRoleMapper;
import com.example.demo.mapper.UserStatusMapper;
import com.example.demo.model.UserRole;
import com.example.demo.model.UserStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConstantsService {
    private final UserRoleMapper userRoleMapper;
    private final UserStatusMapper userStatusMapper;

    public ConstantsService(UserRoleMapper userRoleMapper, UserStatusMapper userStatusMapper) {
        this.userRoleMapper = userRoleMapper;
        this.userStatusMapper = userStatusMapper;
    }

    public List<UserRole> getAllUserRoles() {
        return userRoleMapper.selectAll();
    }

    public List<UserStatus> getAllUserStatuses() {
        return userStatusMapper.selectAll();
    }
}