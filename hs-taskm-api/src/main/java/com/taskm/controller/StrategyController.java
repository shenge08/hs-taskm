package com.taskm.controller;

import com.taskm.dto.Result;
import com.taskm.entity.Strategy;
import com.taskm.service.StrategyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Strategy management.
 * Provides endpoints for querying strategies.
 */
@RestController
@RequestMapping("/api/strategies")
public class StrategyController {

    private final StrategyService strategyService;

    @Autowired
    public StrategyController(StrategyService strategyService) {
        this.strategyService = strategyService;
    }

    /**
     * Get all strategies.
     * Optionally filter by programming language.
     *
     * @param language optional language filter
     * @return list of strategies
     */
    @GetMapping
    public Result<List<Strategy>> getAllStrategies(
            @RequestParam(required = false) String language) {

        if (language != null && !language.isEmpty()) {
            List<Strategy> strategies = strategyService.getStrategiesByLanguage(language);
            return Result.success("Strategies filtered by language: " + language, strategies);
        }

        List<Strategy> strategies = strategyService.getAllStrategies();
        return Result.success(strategies);
    }

    /**
     * Get strategy by ID.
     *
     * @param id strategy ID
     * @return strategy details
     */
    @GetMapping("/{id}")
    public Result<Strategy> getStrategy(@PathVariable Long id) {
        Strategy strategy = strategyService.getStrategy(id);
        return Result.success(strategy);
    }
}
