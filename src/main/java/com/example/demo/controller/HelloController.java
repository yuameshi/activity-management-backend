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
	public List<HelloEntity> helloApi() {
		return helloService.findAll();
	}
	@GetMapping("/api/hello-desc")
	public List<HelloEntity> helloDescApi() {
		return helloService.findAllDesc();
	}
}