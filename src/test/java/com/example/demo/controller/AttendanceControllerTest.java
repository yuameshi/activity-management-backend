package com.example.demo.controller;

import com.example.demo.model.Activity;
import com.example.demo.model.Attendance;
import com.example.demo.service.ActivityService;
import com.example.demo.service.AttendanceService;
import com.example.demo.service.UserService;
import com.example.demo.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AttendanceControllerTest {

    private MockMvc mockMvc;
    private AttendanceService attendanceService;
    private ActivityService activityService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        attendanceService = Mockito.mock(AttendanceService.class);
        activityService = Mockito.mock(ActivityService.class);
        UserService userService = Mockito.mock(UserService.class);
        AttendanceController controller = new AttendanceController(attendanceService, activityService, userService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void getByActivityId_returnsAttendanceList() throws Exception {
        List<Attendance> list = Arrays.asList(new Attendance(), new Attendance());
        when(attendanceService.getByActivityId(1L)).thenReturn(list);

        mockMvc.perform(get("/api/attendance/activity/1"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(list)));

        verify(attendanceService).getByActivityId(1L);
    }

    @Test
    void signAttendance_success_QR() throws Exception {
        AttendanceController.SignRequest req = new AttendanceController.SignRequest();
        req.activityId = 2L;
        req.signType = "QR";
        req.code = "123456";
        req.userId = null;

        Activity act = new Activity();
        act.setId(2L);
        act.setTitle("活动");
        act.setDescription("描述");
        act.setLocation("地点");

        when(activityService.getById(2L)).thenReturn(act);
        when(attendanceService.createAttendance(any())).thenReturn(1);

        // mock static method getTOTPCode
        try (var mocked = Mockito.mockStatic(AttendanceController.class, Mockito.CALLS_REAL_METHODS)) {
            mocked.when(() -> AttendanceController.getTOTPCode(anyString())).thenReturn("123456");

            mockMvc.perform(post("/api/attendance/sign")
                    .header("Authorization", "Bearer " + JwtUtil.generateToken(5L, "user5", false))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.message").value("签到成功"));
        }
    }

    @Test
    void signAttendance_forbiddenForOtherUser() throws Exception {
        AttendanceController.SignRequest req = new AttendanceController.SignRequest();
        req.activityId = 2L;
        req.signType = "MANUAL";
        req.userId = 99L;

        mockMvc.perform(post("/api/attendance/sign")
                .header("Authorization", "Bearer " + JwtUtil.generateToken(5L, "user5", false))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.msg").value("无权为他人签到"));
    }

    @Test
    void listAttendanceByActivityId_adminSuccess() throws Exception {
        List<Attendance> list = Arrays.asList(new Attendance(), new Attendance());
        when(attendanceService.getAttendanceListByActivityIdAndStatus(3L, null)).thenReturn(list);

        String token = JwtUtil.generateToken(100L, "admin", true);
        mockMvc.perform(get("/api/attendance/list/3")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        verify(attendanceService).getAttendanceListByActivityIdAndStatus(3L, null);
    }

    @Test
    void listAttendanceByActivityId_forbiddenForNonAdmin() throws Exception {
        String token = JwtUtil.generateToken(101L, "user101", false);
        mockMvc.perform(get("/api/attendance/list/3")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.msg").value("仅管理员可访问"));
    }

    @Test
    void signAttendance_invalidJwt() throws Exception {
        AttendanceController.SignRequest req = new AttendanceController.SignRequest();
        req.activityId = 2L;
        req.signType = "QR";
        req.code = "123456";

        mockMvc.perform(post("/api/attendance/sign")
                .header("Authorization", "Bearer invalid.token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.msg").value("JWT 无效"));
    }
}