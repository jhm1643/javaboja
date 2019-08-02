package com.example.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.vo.TestVo;

public interface TestDao extends JpaRepository<TestVo, Long>{

}
