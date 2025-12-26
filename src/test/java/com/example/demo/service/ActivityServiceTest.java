package com.example.demo.service;

import com.example.demo.mapper.ActivityMapper;
import com.example.demo.model.Activity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 与项目中 UserServiceTest 风格一致，使用 MockitoExtension 与注解方式。
 */
@ExtendWith(MockitoExtension.class)
class ActivityServiceTest {

    @Mock
    private ActivityMapper activityMapper;

    @InjectMocks
    private ActivityService activityService;

    @BeforeEach
    void setUp() {
        // Mockito 注解驱动，不需要手动初始化 mocks
    }

    // a) createActivity：当 createTime 为 null 时设置 createTime 并调用 mapper.insertActivity，返回 mapper 的返回值
    @Test
    void createActivity_whenCreateTimeNull_setsCreateTime_and_callsInsert() {
        Activity activity = new Activity();
        activity.setTitle("T1");
        activity.setCreateTime(null);

        when(activityMapper.insertActivity(activity)).thenReturn(1);

        int res = activityService.createActivity(activity);

        assertEquals(1, res);
        assertNotNull(activity.getCreateTime());
        verify(activityMapper).insertActivity(activity);
    }

    // b) getById：mapper 返回 activity，则 service 返回相同对象；mapper 返回 null，service 返回 null。
    @Test
    void getById_whenFound_returnsActivity() {
        Activity a = new Activity();
        a.setId(10L);
        when(activityMapper.findById(10L)).thenReturn(a);

        Activity res = activityService.getById(10L);

        assertSame(a, res);
        verify(activityMapper).findById(10L);
    }

    @Test
    void getById_whenNotFound_returnsNull() {
        when(activityMapper.findById(99L)).thenReturn(null);

        Activity res = activityService.getById(99L);

        assertNull(res);
        verify(activityMapper).findById(99L);
    }

    // c) getAll：mapper 返回列表，service 返回相同列表。
    @Test
    void getAll_returnsListFromMapper() {
        List<Activity> list = Arrays.asList(new Activity(), new Activity());
        when(activityMapper.findAll()).thenReturn(list);

        List<Activity> res = activityService.getAll();

        assertEquals(list, res);
        verify(activityMapper).findAll();
    }

    // d) updateActivity：当传入 null 或 id 为 null，断言返回 0 或按实现行为；当 mapper.updateActivity 返回 >0，断言方法返回对应值。
    @Test
    void updateActivity_whenNull_returnsZero() {
        assertEquals(0, activityService.updateActivity(null));
        verifyNoInteractions(activityMapper);
    }

    @Test
    void updateActivity_whenMapperReturnsPositive_returnsThatValue() {
        Activity a = new Activity();
        a.setId(5L);
        when(activityMapper.updateActivity(a)).thenReturn(2);

        int res = activityService.updateActivity(a);

        assertEquals(2, res);
        verify(activityMapper).updateActivity(a);
    }

    @Test
    void updateActivity_whenIdNull_delegatesToMapper_and_returnsMapperValue() {
        Activity a = new Activity();
        a.setId(null);
        when(activityMapper.updateActivity(a)).thenReturn(0);

        int res = activityService.updateActivity(a);

        assertEquals(0, res);
        verify(activityMapper).updateActivity(a);
    }

    // e) deleteById：测试 mapper.deleteById 被调用并返回预期值。
    @Test
    void deleteById_whenIdNull_returnsZero() {
        assertEquals(0, activityService.deleteById(null));
        verifyNoInteractions(activityMapper);
    }

    @Test
    void deleteById_callsMapper_and_returnsValue() {
        when(activityMapper.deleteById(7L)).thenReturn(1);

        int res = activityService.deleteById(7L);

        assertEquals(1, res);
        verify(activityMapper).deleteById(7L);
    }
}