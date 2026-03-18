package com.taskm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taskm.entity.TestEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * Test entity mapper for verifying MyBatis-Plus database operations.
 */
@Mapper
public interface TestEntityMapper extends BaseMapper<TestEntity> {
}
