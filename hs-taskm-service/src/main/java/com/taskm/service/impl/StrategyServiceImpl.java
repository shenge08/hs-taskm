package com.taskm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taskm.entity.Strategy;
import com.taskm.exception.StrategyNotFoundException;
import com.taskm.mapper.StrategyMapper;
import com.taskm.service.StrategyService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementation of StrategyService.
 * Provides business logic for strategy management.
 */
@Service
public class StrategyServiceImpl extends ServiceImpl<StrategyMapper, Strategy> implements StrategyService {

    @Override
    public List<Strategy> getAllStrategies() {
        return list();
    }

    @Override
    public Strategy getStrategy(Long id) {
        Strategy strategy = getById(id);
        if (strategy == null) {
            throw new StrategyNotFoundException("Strategy not found with id: " + id);
        }
        return strategy;
    }

    @Override
    public List<Strategy> getStrategiesByLanguage(String language) {
        QueryWrapper<Strategy> wrapper = new QueryWrapper<>();
        wrapper.eq("language", language);
        return list(wrapper);
    }
}
