package com.taskm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taskm.entity.DataPluginInstance;
import org.apache.ibatis.annotations.Mapper;

/**
 * MyBatis-Plus Mapper for DataPluginInstance entity.
 * Provides CRUD operations for data plugin instances.
 */
@Mapper
public interface DataPluginInstanceMapper extends BaseMapper<DataPluginInstance> {
    // MyBatis-Plus provides basic CRUD methods automatically:
    // - insert(DataPluginInstance)
    // - deleteById(Serializable)
    // - updateById(DataPluginInstance)
    // - selectById(Serializable)
    // - selectList(Wrapper)
    // - selectCount(Wrapper)
}
