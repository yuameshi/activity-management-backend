package com.example.demo.controller;

import com.example.demo.model.Activity;
import com.example.demo.service.ActivityService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 使用 standalone MockMvcBuilders 测试 ActivityController，手动 mock ActivityService（参考 UserControllerTest 风格）。
 */
class ActivityControllerTest {

    private MockMvc mockMvc;
    private ActivityService activityService;
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() {
        activityService = Mockito.mock(ActivityService.class);
        ActivityController controller = new ActivityController();
        ReflectionTestUtils.setField(controller, "activityService", activityService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
    }

    // a) GET /api/activity/list -> 返回 200 并包含 service 返回的活动列表 JSON。
    @Test
    void list_returnsActivities() throws Exception {
        List<Activity> list = Arrays.asList(new Activity(), new Activity());
        when(activityService.getAll()).thenReturn(list);

        mockMvc.perform(get("/api/activity/list"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(list)));

        verify(activityService).getAll();
    }

    // b) GET /api/activity/{id} -> id 存在返回 200 与对象 JSON；id 不存在按照 controller 实现（返回 null -> 200 空体）
    @Test
    void get_existing_returnsObjectJson() throws Exception {
        Activity a = new Activity();
        a.setId(1L);
        a.setTitle("Title");
        when(activityService.getById(1L)).thenReturn(a);

        mockMvc.perform(get("/api/activity/1"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(a)));

        verify(activityService).getById(1L);
    }

    @Test
    void get_notFound_returnsEmptyBody() throws Exception {
        when(activityService.getById(99L)).thenReturn(null);

        mockMvc.perform(get("/api/activity/99"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(activityService).getById(99L);
    }

    // c) POST /api/activity/create -> 发送 JSON 活动对象，断言 service.createActivity 被调用且返回值体内可见或状态为 200。
    @Test
    void create_callsService_andReturnsValue() throws Exception {
        Activity req = new Activity();
        req.setTitle("New");
        when(activityService.createActivity(any())).thenReturn(1);

        mockMvc.perform(post("/api/activity/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"affected\":1}"));

        ArgumentCaptor<Activity> captor = ArgumentCaptor.forClass(Activity.class);
        verify(activityService).createActivity(captor.capture());
        assertEquals("New", captor.getValue().getTitle());
    }

    // d) PUT /api/activity/update/{id} -> 发送 JSON，断言调用 service.updateActivity。
    @Test
    void update_callsService_andReturnsValue() throws Exception {
        Activity req = new Activity();
        req.setTitle("Upd");
        when(activityService.updateActivity(any())).thenReturn(1);

        mockMvc.perform(put("/api/activity/update/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"affected\":1}"));

        ArgumentCaptor<Activity> captor = ArgumentCaptor.forClass(Activity.class);
        verify(activityService).updateActivity(captor.capture());
        assertEquals(5L, captor.getValue().getId());
        assertEquals("Upd", captor.getValue().getTitle());
    }

    // e) DELETE /api/activity/delete/{id} -> 断言调用 service.deleteById。
    @Test
    void delete_callsService_andReturnsValue() throws Exception {
        when(activityService.deleteById(7L)).thenReturn(1);

        mockMvc.perform(delete("/api/activity/delete/7"))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"affected\":1}"));

        verify(activityService).deleteById(7L);
    }

    // f) POST /api/activity/audit/{id}?status=1 -> 断言调用 service.auditActivity 并返回预期值。
    @Test
    void audit_callsService_andReturnsValue() throws Exception {
        when(activityService.auditActivity(8L, (byte)1)).thenReturn(1);

        mockMvc.perform(post("/api/activity/audit/8").param("status", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"affected\":1}"));

        verify(activityService).auditActivity(8L, (byte)1);
    }
}