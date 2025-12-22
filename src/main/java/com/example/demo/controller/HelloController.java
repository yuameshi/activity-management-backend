package com.example.demo.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.mapper.HelloMapper;
import com.example.demo.model.HelloEntity;

@RestController
public class HelloController {

	private final HelloMapper helloMapper;

	public HelloController(HelloMapper helloMapper) {
		this.helloMapper = helloMapper;
	}

	@GetMapping("/api/hello")
	public List<HelloEntity> helloApi() {
		return helloMapper.findAll();
	}
}