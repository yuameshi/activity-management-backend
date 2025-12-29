package com.example.demo.controller;

import com.example.demo.model.Activity;
import com.example.demo.service.ActivityService;
import com.example.demo.util.JwtUtil;
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

	private String bearerToken(Long id, String username) {
		Boolean isAdmin = username.equalsIgnoreCase("admin");
		String tok = JwtUtil.generateToken(id, username, isAdmin);
		return "Bearer " + tok;
	}

	@Test
	void list_returnsActivities() throws Exception {
		List<Activity> list = Arrays.asList(new Activity(), new Activity());
		when(activityService.getAll()).thenReturn(list);

		mockMvc.perform(get("/api/activity/list"))
				.andExpect(status().isOk())
				.andExpect(content().json(objectMapper.writeValueAsString(list)));

		verify(activityService).getAll();
	}

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
				.andExpect(status().isNotFound())
				.andExpect(content().json("{\"data\":null}"));

		verify(activityService).getById(99L);
	}

	@Test
	void create_callsService_andReturnsValue() throws Exception {
		Activity req = new Activity();
		req.setTitle("New");
		when(activityService.createActivity(any())).thenReturn(1);

		mockMvc.perform(post("/api/activity/create")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", bearerToken(1L, "admin"))
				.content(objectMapper.writeValueAsString(req)))
				.andExpect(status().isOk())
				.andExpect(content().string("{\"affected\":1}"));

		ArgumentCaptor<Activity> captor = ArgumentCaptor.forClass(Activity.class);
		verify(activityService).createActivity(captor.capture());
		assertEquals("New", captor.getValue().getTitle());
	}

	@Test
	void update_callsService_andReturnsValue() throws Exception {
		Activity req = new Activity();
		req.setTitle("Upd");
		when(activityService.updateActivity(any())).thenReturn(1);

		mockMvc.perform(put("/api/activity/update/5")
				.header("Authorization", bearerToken(1L, "admin"))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(req)))
				.andExpect(status().isOk())
				.andExpect(content().string("{\"affected\":1}"));

		ArgumentCaptor<Activity> captor = ArgumentCaptor.forClass(Activity.class);
		verify(activityService).updateActivity(captor.capture());
		assertEquals(5L, captor.getValue().getId());
		assertEquals("Upd", captor.getValue().getTitle());
	}

	@Test
	void delete_callsService_andReturnsValue() throws Exception {
		when(activityService.deleteById(7L)).thenReturn(1);

		mockMvc.perform(delete("/api/activity/delete/7")
				.header("Authorization", bearerToken(1L, "admin")))
				.andExpect(status().isOk())
				.andExpect(content().string("{\"affected\":1}"));

		verify(activityService).deleteById(7L);
	}
}