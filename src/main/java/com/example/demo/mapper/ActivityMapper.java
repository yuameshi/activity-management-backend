package com.example.demo.mapper;

@org.apache.ibatis.annotations.Mapper
public interface ActivityMapper {

    int insertActivity(com.example.demo.model.Activity activity);

    com.example.demo.model.Activity findById(@org.apache.ibatis.annotations.Param("id") Long id);

    java.util.List<com.example.demo.model.Activity> findAll();

    java.util.List<com.example.demo.model.Activity> findByPublisherId(@org.apache.ibatis.annotations.Param("publisherId") Long publisherId);

    int updateActivity(com.example.demo.model.Activity activity);

    int deleteById(@org.apache.ibatis.annotations.Param("id") Long id);
}