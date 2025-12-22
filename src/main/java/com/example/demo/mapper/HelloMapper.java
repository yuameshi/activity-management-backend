package com.example.demo.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.example.demo.model.HelloEntity;

@Mapper
public interface HelloMapper {

	@Select("""
			SELECT id, content
			FROM hello
			ORDER BY id
			""")
	List<HelloEntity> findAll();

}