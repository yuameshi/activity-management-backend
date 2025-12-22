package com.example.demo.model;

public class HelloEntity {

	private Integer id;

	private String content;

	public HelloEntity() {
	}

	public HelloEntity(Integer id, String content) {
		this.id = id;
		this.content = content;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

}