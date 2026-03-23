package com.taskm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taskm.entity.ListenerInstance;
import org.apache.ibatis.annotations.Mapper;

/**
 * MyBatis-Plus Mapper for ListenerInstance entity.
 * Provides CRUD operations for listener instances.
 */
@Mapper
public interface ListenerInstanceMapper extends BaseMapper<ListenerInstance> {
    // MyBatis-Plus provides basic CRUD methods automatically:
    // - insert(ListenerInstance)
    // - deleteById(Serializable)
    // - updateById(ListenerInstance)
    // - selectById(Serializable)
    // - selectList(Wrapper)
    // - selectCount(Wrapper)
}
