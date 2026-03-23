package com.taskm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taskm.entity.Task;
import org.apache.ibatis.annotations.Mapper;

/**
 * MyBatis-Plus Mapper for Task entity.
 * Provides CRUD operations for tasks.
 */
@Mapper
public interface TaskMapper extends BaseMapper<Task> {
    // MyBatis-Plus provides basic CRUD methods automatically
}
