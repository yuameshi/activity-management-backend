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
    public List<UserRole> getUserRoles() {
        return constantsService.getAllUserRoles();
    }

    @GetMapping("/user_statuses")
    public List<UserStatus> getUserStatuses() {
        return constantsService.getAllUserStatuses();
    }
}