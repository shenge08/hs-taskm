package com.taskm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.taskm.entity.DataPlugin;
import org.apache.ibatis.annotations.Mapper;

/**
 * MyBatis-Plus Mapper for DataPlugin entity.
 * Provides CRUD operations for data plugins.
 */
@Mapper
public interface DataPluginMapper extends BaseMapper<DataPlugin> {
    // MyBatis-Plus provides basic CRUD methods automatically
}
