package com.example.demo.mapper;

import com.example.demo.model.Image;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ImageMapper {
    int insertImage(Image image);

    Image selectImageById(@Param("id") Integer id);
}