package com.example.demo.controller;

import com.example.demo.model.UserRole;
import com.example.demo.model.UserStatus;
import com.example.demo.service.ConstantsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/constants")
public class ConstantsController {

	private final ConstantsService constantsService;

	public ConstantsController(ConstantsService constantsService) {
		this.constantsService = constantsService;
	}

	@GetMapping("/user_roles")
	public List<UserRole> getUserRoles(
			@org.springframework.web.bind.annotation.RequestHeader(value = "Authorization", required = false) String auth,
			jakarta.servlet.http.HttpServletRequest request) {
		return constantsService.getAllUserRoles();
	}

	@GetMapping("/user_statuses")
	public List<UserStatus> getUserStatuses(
			@org.springframework.web.bind.annotation.RequestHeader(value = "Authorization", required = false) String auth,
			jakarta.servlet.http.HttpServletRequest request) {
		return constantsService.getAllUserStatuses();
	}
}