package com.example.demo.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.HelloService;
import com.example.demo.model.HelloEntity;

@RestController
public class HelloController {

	private final HelloService helloService;

	public HelloController(HelloService helloService) {
		this.helloService = helloService;
	}

	@GetMapping("/api/hello")
	public List<HelloEntity> helloApi(
			@org.springframework.web.bind.annotation.RequestHeader(value = "Authorization", required = false) String auth,
			jakarta.servlet.http.HttpServletRequest request) {
		return helloService.findAll();
	}

	@GetMapping("/api/hello-desc")
	public List<HelloEntity> helloDescApi(
			@org.springframework.web.bind.annotation.RequestHeader(value = "Authorization", required = false) String auth,
			jakarta.servlet.http.HttpServletRequest request) {
		return helloService.findAllDesc();
	}
}