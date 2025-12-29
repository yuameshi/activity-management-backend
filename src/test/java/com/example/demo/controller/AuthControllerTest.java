package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest {

	private MockMvc mockMvc;
	private UserService userService;
	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		userService = Mockito.mock(UserService.class);
		AuthController controller = new AuthController(userService);
		mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
		objectMapper = new ObjectMapper();
	}

	@Test
	void register_success() throws Exception {
		User created = new User();
		created.setId(10L);
		created.setUsername("alice");
		created.setEmail("a@example.com");
		when(userService.register(any())).thenReturn(created);

		User req = new User();
		req.setUsername("alice");
		req.setPassword("pwd");

		mockMvc.perform(post("/api/user/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(req)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.username").value("alice"))
				.andExpect(jsonPath("$.id").value(10));
	}

	@Test
	void register_duplicate() throws Exception {
		when(userService.register(any())).thenThrow(new IllegalArgumentException("username already exists"));

		User req = new User();
		req.setUsername("bob");
		req.setPassword("x");

		mockMvc.perform(post("/api/user/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(req)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").exists());
	}

	@Test
	void login_success() throws Exception {
		User user = new User();
		user.setId(5L);
		user.setUsername("carol");
		Map<String, Object> res = Map.of("token", "tok-123", "user", user);
		when(userService.login("carol", "secret")).thenReturn(res);

		AuthController.LoginRequest req = new AuthController.LoginRequest();
		req.setUsername("carol");
		req.setPassword("secret");

		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(req)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").value("tok-123"))
				.andExpect(jsonPath("$.user.username").value("carol"));
	}

	@Test
	void login_unauthorized() throws Exception {
		when(userService.login("dan", "wrong")).thenThrow(new IllegalArgumentException("invalid username or password"));

		AuthController.LoginRequest req = new AuthController.LoginRequest();
		req.setUsername("dan");
		req.setPassword("wrong");

		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(req)))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error").exists());
	}
}