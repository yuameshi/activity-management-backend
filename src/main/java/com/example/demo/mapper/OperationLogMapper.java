package com.example.demo.mapper;

import com.example.demo.model.OperationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OperationLogMapper {
	int insert(OperationLog log);

	List<OperationLog> selectAll();

	List<OperationLog> selectByPage(@Param("offset") int offset, @Param("limit") int limit);

	int count();
}