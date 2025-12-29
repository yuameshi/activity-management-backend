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
        Attendance att1 = new Attendance();
        att1.setUserId(1L);
        Attendance att2 = new Attendance();
        att2.setUserId(2L);
        List<Attendance> list = Arrays.asList(att1, att2);
        when(attendanceService.getByActivityId(1L)).thenReturn(list);

        UserService userService = Mockito.mock(UserService.class);
        com.example.demo.model.User mockUser1 = new com.example.demo.model.User();
        mockUser1.setUsername("mockUser1");
        mockUser1.setRealName("mockRealName1");
        com.example.demo.model.User mockUser2 = new com.example.demo.model.User();
        mockUser2.setUsername("mockUser2");
        mockUser2.setRealName("mockRealName2");
        when(userService.getById(1L)).thenReturn(mockUser1);
        when(userService.getById(2L)).thenReturn(mockUser2);

        AttendanceController controller = new AttendanceController(attendanceService, activityService, userService);
        MockMvc localMockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        localMockMvc.perform(get("/api/attendance/activity/1"))
                .andExpect(status().isOk());

        verify(attendanceService).getByActivityId(1L);
        verify(userService).getById(1L);
        verify(userService).getById(2L);
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