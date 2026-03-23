package com.taskm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taskm.entity.Strategy;
import org.apache.ibatis.annotations.Mapper;

/**
 * MyBatis-Plus Mapper for Strategy entity.
 * Provides CRUD operations and custom query methods.
 */
@Mapper
public interface StrategyMapper extends BaseMapper<Strategy> {
    // MyBatis-Plus provides basic CRUD methods automatically:
    // - insert(Strategy entity)
    // - deleteById(Serializable id)
    // - updateById(Strategy entity)
    // - selectById(Serializable id)
    // - selectList(Wrapper<Strategy> wrapper)
    // - selectCount(Wrapper<Strategy> wrapper)
    // etc.

    // Custom query methods can be added here using XML mapping or annotations
}
