package com.example.demo.service;

import com.example.demo.mapper.ActivityMapper;
import com.example.demo.model.Activity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivityServiceTest {

	@Mock
	private ActivityMapper activityMapper;

	@InjectMocks
	private ActivityService activityService;

	@BeforeEach
	void setUp() {
	}

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

	@Test
	void getAll_returnsListFromMapper() {
		List<Activity> list = Arrays.asList(new Activity(), new Activity());
		when(activityMapper.findAll()).thenReturn(list);

		List<Activity> res = activityService.getAll();

		assertEquals(list, res);
		verify(activityMapper).findAll();
	}

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