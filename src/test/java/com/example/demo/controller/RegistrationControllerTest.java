package com.example.demo.controller;

import com.example.demo.model.Registration;
import com.example.demo.service.RegistrationService;
import com.example.demo.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * RegistrationController 单元测试：使用 standalone MockMvcBuilders 并手动 mock RegistrationService。
 */
class RegistrationControllerTest {

    private MockMvc mockMvc;
    private RegistrationService registrationService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        registrationService = Mockito.mock(RegistrationService.class);
        RegistrationController controller = new RegistrationController(registrationService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
    }

    private String bearerToken(Long id, String username) {
        String tok = JwtUtil.generateToken(id, username);
        return "Bearer " + tok;
    }

    @Test
    void getSelfRegistration_exists_returnsObject() throws Exception {
        Registration r = new Registration();
        r.setId(2L);
        r.setUserId(5L);
        r.setActivityId(10L);
        r.setRegisterTime(LocalDateTime.now());
        when(registrationService.getByUserAndActivity(5L, 10L)).thenReturn(r);

        mockMvc.perform(get("/api/registration/10")
                        .header("Authorization", bearerToken(5L, "user5"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(r)));

        verify(registrationService).getByUserAndActivity(5L, 10L);
    }

    @Test
    void getSelfRegistration_notFound_returnsEmptyBody() throws Exception {
        when(registrationService.getByUserAndActivity(99L, 20L)).thenReturn(null);

        mockMvc.perform(get("/api/registration/20")
                        .header("Authorization", bearerToken(99L, "u99")))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(registrationService).getByUserAndActivity(99L, 20L);
    }

    @Test
    void checkRegistered_returnsBoolean() throws Exception {
        when(registrationService.isRegistered(6L, 11L)).thenReturn(true);

        mockMvc.perform(post("/api/registration/check/11")
                        .header("Authorization", bearerToken(6L, "user6")))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(registrationService).isRegistered(6L, 11L);
    }

    @Test
    void apply_callsService_andReturnsValue() throws Exception {
        when(registrationService.createRegistration(any())).thenReturn(1);

        mockMvc.perform(post("/api/registration/12/apply")
                        .header("Authorization", bearerToken(7L, "user7"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(null)))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"applied\":true}"));

        ArgumentCaptor<Registration> captor = ArgumentCaptor.forClass(Registration.class);
        verify(registrationService).createRegistration(captor.capture());
        assertEquals(7L, captor.getValue().getUserId());
        assertEquals(12L, captor.getValue().getActivityId());
    }

    @Test
    void apply_admin_canSpecifyUser() throws Exception {
        when(registrationService.createRegistration(any())).thenReturn(1);
        Registration body = new Registration();
        body.setUserId(20L);

        mockMvc.perform(post("/api/registration/15/apply")
                        .header("Authorization", bearerToken(100L, "admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"applied\":true}"));

        ArgumentCaptor<Registration> captor = ArgumentCaptor.forClass(Registration.class);
        verify(registrationService).createRegistration(captor.capture());
        assertEquals(20L, captor.getValue().getUserId());
        assertEquals(15L, captor.getValue().getActivityId());
    }

    @Test
    void cancel_owner_success() throws Exception {
        Registration target = new Registration();
        target.setId(3L);
        target.setUserId(7L);
        when(registrationService.getById(3L)).thenReturn(target);
        when(registrationService.deleteById(3L)).thenReturn(1);

        mockMvc.perform(delete("/api/registration/cancel/3")
                        .header("Authorization", bearerToken(7L, "user7")))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"cancelled\":true}"));

        verify(registrationService).deleteById(3L);
    }

    @Test
    void cancel_forbidden_whenNotOwner() throws Exception {
        Registration target = new Registration();
        target.setId(4L);
        target.setUserId(9L);
        when(registrationService.getById(4L)).thenReturn(target);

        mockMvc.perform(delete("/api/registration/cancel/4")
                        .header("Authorization", bearerToken(8L, "user8")))
                .andExpect(status().isForbidden());

        verify(registrationService, never()).deleteById(anyLong());
    }

    @Test
    void listByActivity_admin_success() throws Exception {
        Registration a = new Registration();
        a.setId(1L);
        a.setUserId(2L);
        Registration b = new Registration();
        b.setId(2L);
        b.setUserId(3L);
        when(registrationService.listByActivityId(30L)).thenReturn(List.of(a, b));

        mockMvc.perform(get("/api/registration/30/list-applications")
                        .header("Authorization", bearerToken(200L, "admin"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(a, b))));
    }

    @Test
    void delete_admin_success() throws Exception {
        when(registrationService.deleteById(5L)).thenReturn(1);

        mockMvc.perform(delete("/api/registration/delete/5")
                        .header("Authorization", bearerToken(300L, "admin")))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"cancelled\":true}"));

        verify(registrationService).deleteById(5L);
    }

    @Test
    void delete_forbidden_forNonAdmin() throws Exception {
        mockMvc.perform(delete("/api/registration/delete/6")
                        .header("Authorization", bearerToken(9L, "user9")))
                .andExpect(status().isForbidden());
        verify(registrationService, never()).deleteById(anyLong());
    }
}