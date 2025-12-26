package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.service.UserService;
import com.example.demo.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerTest {

    private MockMvc mockMvc;
    private UserService userService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userService = Mockito.mock(UserService.class);
        UserController controller = new UserController(userService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
    }

    private String bearerToken(Long id, String username) {
        Boolean isAdmin = username.equalsIgnoreCase("admin");
        String tok = JwtUtil.generateToken(id, username, isAdmin);
        return "Bearer " + tok;
    }

    @Test
    void info_self_returnsUser() throws Exception {
        User u = new User();
        u.setId(5L);
        u.setUsername("user5");
        u.setEmail("u5@example.com");
        u.setCreateTime(LocalDateTime.now());
        when(userService.getById(5L)).thenReturn(u);

        mockMvc.perform(get("/api/user/info")
                .header("Authorization", bearerToken(5L, "user5"))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.username").value("user5"))
                .andExpect(jsonPath("$.email").value("u5@example.com"));
    }

    @Test
    void info_forbidden_whenNotAdminAndNotSelf() throws Exception {
        mockMvc.perform(get("/api/user/info")
                .header("Authorization", bearerToken(8L, "user8"))
                .param("id", "2")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void update_self_success() throws Exception {
        User update = new User();
        update.setEmail("new@example.com");

        User returned = new User();
        returned.setId(7L);
        returned.setUsername("user7");
        returned.setEmail("new@example.com");

        when(userService.updateUser(eq(7L), eq("user7"), eq(7L), any())).thenReturn(returned);

        mockMvc.perform(put("/api/user/update")
                .header("Authorization", bearerToken(7L, "user7"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.email").value("new@example.com"));
    }

    @Test
    void update_forbidden_whenNotAdminAndNotOwner() throws Exception {
        User update = new User();
        update.setEmail("x@example.com");

        mockMvc.perform(put("/api/user/update")
                .header("Authorization", bearerToken(9L, "user9"))
                .param("targetId", "3")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isForbidden());
    }

    @Test
    void list_admin_success() throws Exception {
        User a = new User();
        a.setId(1L);
        a.setUsername("u1");
        User b = new User();
        b.setId(2L);
        b.setUsername("u2");
        when(userService.listUsers()).thenReturn(List.of(a, b));

        mockMvc.perform(get("/api/user/list")
                .header("Authorization", bearerToken(100L, "admin"))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].username").value("u1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].username").value("u2"));
    }

    @Test
    void list_forbidden_forNonAdmin() throws Exception {
        mockMvc.perform(get("/api/user/list")
                .header("Authorization", bearerToken(11L, "user11"))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}