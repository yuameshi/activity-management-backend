package com.example.demo.model;

import java.io.Serializable;

public class UserStatus implements Serializable {
	private Byte id;
	private String name;

	public UserStatus() {
	}

	public UserStatus(Byte id, String name) {
		this.id = id;
		this.name = name;
	}

	public Byte getId() {
		return id;
	}

	public void setId(Byte id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}