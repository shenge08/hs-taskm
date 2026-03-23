package com.taskm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taskm.entity.Listener;
import org.apache.ibatis.annotations.Mapper;

/**
 * MyBatis-Plus Mapper for Listener entity.
 * Provides CRUD operations for listeners.
 */
@Mapper
public interface ListenerMapper extends BaseMapper<Listener> {
    // MyBatis-Plus provides basic CRUD methods automatically
}
