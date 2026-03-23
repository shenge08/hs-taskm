package com.taskm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.taskm.entity.Strategy;

import java.util.List;

/**
 * Service interface for Strategy entity.
 * Provides business logic operations for strategies.
 */
public interface StrategyService extends IService<Strategy> {

    /**
     * Get all strategies.
     *
     * @return list of all strategies
     */
    List<Strategy> getAllStrategies();

    /**
     * Get strategy by ID.
     *
     * @param id strategy ID
     * @return strategy with the given ID
     * @throws com.taskm.exception.StrategyNotFoundException if strategy not found
     */
    Strategy getStrategy(Long id);

    /**
     * Get strategies by programming language.
     *
     * @param language programming language (python, javascript, etc.)
     * @return list of strategies using the given language
     */
    List<Strategy> getStrategiesByLanguage(String language);
}
