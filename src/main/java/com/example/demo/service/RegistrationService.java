package com.example.demo.service;

import com.example.demo.mapper.RegistrationMapper;
import com.example.demo.model.Registration;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 报名服务层
 */
@Service
@Transactional
public class RegistrationService {

    private final RegistrationMapper registrationMapper;

    public RegistrationService(RegistrationMapper registrationMapper) {
        this.registrationMapper = registrationMapper;
    }

    /**
     * 插入报名：防止重复报名。
     * - 若已存在且 status != 0（已报名等），则不再插入，返回 0。
     * - 若已存在且 status == 0（已取消），则复用该记录（更新为已报名并更新时间），返回 update 的结果。
     * - 否则插入新记录。
     * 
     * @return 受影响行数（插入或更新），若因重复报名未插入则返回 0
     */
    public int createRegistration(Registration registration) {
        if (registration == null)
            return 0;
        if (registration.getRegisterTime() == null) {
            registration.setRegisterTime(LocalDateTime.now());
        }
        if (registration.getStatus() == null) {
            registration.setStatus((byte) 1);
        }

        // 先检查是否已存在相同 userId + activityId 的记录
        Registration exist = registrationMapper.findByUserAndActivity(registration.getUserId(),
                registration.getActivityId());
        if (exist != null) {
            Byte s = exist.getStatus();
            // 已存在且未取消（status != 0）则视为已报名，避免重复插入
            if (s != null && s != 0) {
                return 0;
            }
            // 存在但已取消，复用该记录：设置 id 并更新为已报名
            registration.setId(exist.getId());
            registration.setRegisterTime(LocalDateTime.now());
            registration.setStatus((byte) 1);
            return registrationMapper.updateRegistration(registration);
        }

        return registrationMapper.insertRegistration(registration);
    }

    public Registration getById(Long id) {
        if (id == null)
            return null;
        return registrationMapper.findById(id);
    }

    /**
     * 根据 userId + activityId 查询单条报名（用于检查是否已报名）
     */
    public Registration getByUserAndActivity(Long userId, Long activityId) {
        if (userId == null || activityId == null)
            return null;
        return registrationMapper.findByUserAndActivity(userId, activityId);
    }

    public List<Registration> listByActivityId(Long activityId) {
        if (activityId == null)
            return new ArrayList<>();
        List<Registration> list = registrationMapper.findByActivityId(activityId);
        return list == null ? new ArrayList<>() : list;
    }

    public List<Registration> listByUserId(Long userId) {
        if (userId == null)
            return new ArrayList<>();
        List<Registration> list = registrationMapper.findByUserId(userId);
        return list == null ? new ArrayList<>() : list;
    }

    public int deleteById(Long id) {
        if (id == null)
            return 0;
        return registrationMapper.deleteById(id);
    }

    public int deleteByUserAndActivity(Long userId, Long activityId) {
        if (userId == null || activityId == null)
            return 0;
        return registrationMapper.deleteByUserAndActivity(userId, activityId);
    }

    public int updateRegistration(Registration registration) {
        if (registration == null)
            return 0;
        return registrationMapper.updateRegistration(registration);
    }

    /**
     * 简单检查：是否已报名（存在记录且 status != 0）
     */
    public boolean isRegistered(Long userId, Long activityId) {
        Registration r = getByUserAndActivity(userId, activityId);
        return r != null && r.getStatus() != null && r.getStatus() != 0;
    }
}