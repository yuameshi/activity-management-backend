package com.example.demo.service;

import com.example.demo.mapper.HelloMapper;
import com.example.demo.model.HelloEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HelloService {

    private final HelloMapper helloMapper;

    public HelloService(HelloMapper helloMapper) {
        this.helloMapper = helloMapper;
    }

    public List<HelloEntity> findAll() {
        return helloMapper.findAll();
    }

    public List<HelloEntity> findAllDesc() {
        return helloMapper.findAllDesc();
    }
}