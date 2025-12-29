package com.example.demo.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.example.demo.mapper.HelloMapper;
import com.example.demo.model.HelloEntity;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = HelloControllerTest.TestConfig.class)
class HelloControllerTest {

	@Configuration
	@EnableWebMvc
	@Import(HelloController.class)
	static class TestConfig {
	
		@Bean
		HelloMapper helloMapper() {
			// stub 返回固定数据，避免依赖数据库
			return new HelloMapper() {
				@Override
				public List<HelloEntity> findAll() {
					return List.of(
							new HelloEntity(1, "row1"),
							new HelloEntity(2, "row2"));
				}
	
				@Override
				public List<HelloEntity> findAllDesc() {
					return List.of(
							new HelloEntity(2, "row2"),
							new HelloEntity(1, "row1"));
				}
			};
		}
	
		@Bean
		public com.example.demo.service.HelloService helloService() {
			return new com.example.demo.service.HelloService(helloMapper());
		}
	}

	@Autowired
	private WebApplicationContext webApplicationContext;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
	}

	@Test
	void helloApiReturnsAllDataAsJson() throws Exception {
		mockMvc.perform(get("/api/hello").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(content().json("""
						[
						  {"id": 1, "content": "row1"},
						  {"id": 2, "content": "row2"}
						]
						"""));
	}
}