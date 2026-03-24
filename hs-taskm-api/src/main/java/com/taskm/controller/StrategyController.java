package com.taskm.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskm.dto.CreateStrategyDTO;
import com.taskm.dto.Result;
import com.taskm.dto.UpdateStrategyDTO;
import com.taskm.entity.Strategy;
import com.taskm.service.StrategyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 策略管理 REST Controller
 *
 * <p>提供策略的增删改查 REST API 接口
 *
 * @author HS-TASKM Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/strategies")
@Tag(name = "策略管理", description = "交易策略的创建、查询、更新和删除接口")
public class StrategyController {

    private final StrategyService strategyService;
    private final ObjectMapper objectMapper;

    @Autowired
    public StrategyController(StrategyService strategyService, ObjectMapper objectMapper) {
        this.strategyService = strategyService;
        this.objectMapper = objectMapper;
    }

    /**
     * 创建新策略
     *
     * @param strategyDTO 策略创建请求
     * @return 创建的策略
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "创建策略", description = "创建一个新的交易策略，包含名称、代码、配置参数等信息")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "策略创建成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "参数错误"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<Strategy> createStrategy(@RequestBody @Valid CreateStrategyDTO strategyDTO) {
        Strategy strategy = new Strategy();
        strategy.setName(strategyDTO.getName());
        strategy.setDescription(strategyDTO.getDescription());
        strategy.setLanguage(strategyDTO.getLanguage());
        strategy.setCode(strategyDTO.getCode());
        strategy.setDockerImageId(strategyDTO.getDockerImageId());

        // Convert config parameters map to JSON string
        if (strategyDTO.getConfigParameters() != null) {
            try {
                strategy.setConfigParameters(objectMapper.writeValueAsString(strategyDTO.getConfigParameters()));
            } catch (JsonProcessingException e) {
                return Result.error(400, "Invalid config parameters format");
            }
        }

        // Convert parameter defaults map to JSON string
        if (strategyDTO.getParameterDefaults() != null) {
            try {
                strategy.setParameterDefaults(objectMapper.writeValueAsString(strategyDTO.getParameterDefaults()));
            } catch (JsonProcessingException e) {
                return Result.error(400, "Invalid parameter defaults format");
            }
        }

        strategyService.save(strategy);
        return Result.success("Strategy created successfully", strategy);
    }

    /**
     * 获取所有策略列表
     *
     * <p>支持按编程语言过滤策略
     *
     * @param language 编程语言（可选），如 python、javascript、java
     * @return 策略列表
     */
    @GetMapping
    @Operation(summary = "获取策略列表", description = "获取所有策略，支持按编程语言过滤")
    @Parameter(name = "language", description = "编程语言，如 python、javascript、java", required = false)
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
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
     * 根据 ID 获取策略详情
     *
     * @param id 策略 ID
     * @return 策略详细信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取策略详情", description = "根据策略 ID 查询策略的详细信息")
    @Parameter(name = "id", description = "策略 ID", required = true, example = "1")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "策略不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<Strategy> getStrategy(@PathVariable Long id) {
        Strategy strategy = strategyService.getStrategy(id);
        return Result.success(strategy);
    }

    /**
     * 更新策略
     *
     * @param id 策略 ID
     * @param strategyDTO 策略更新请求
     * @return 更新后的策略
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新策略", description = "更新指定策略的信息")
    @Parameter(name = "id", description = "策略 ID", required = true, example = "1")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "更新成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "参数错误"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "策略不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<Strategy> updateStrategy(
            @PathVariable Long id,
            @RequestBody @Valid UpdateStrategyDTO strategyDTO) {

        Strategy strategy = strategyService.getStrategy(id);

        // Update fields if provided
        if (strategyDTO.getName() != null) {
            strategy.setName(strategyDTO.getName());
        }
        if (strategyDTO.getDescription() != null) {
            strategy.setDescription(strategyDTO.getDescription());
        }
        if (strategyDTO.getLanguage() != null) {
            strategy.setLanguage(strategyDTO.getLanguage());
        }
        if (strategyDTO.getCode() != null) {
            strategy.setCode(strategyDTO.getCode());
        }
        if (strategyDTO.getDockerImageId() != null) {
            strategy.setDockerImageId(strategyDTO.getDockerImageId());
        }
        if (strategyDTO.getConfigParameters() != null) {
            try {
                strategy.setConfigParameters(objectMapper.writeValueAsString(strategyDTO.getConfigParameters()));
            } catch (JsonProcessingException e) {
                return Result.error(400, "Invalid config parameters format");
            }
        }
        if (strategyDTO.getParameterDefaults() != null) {
            try {
                strategy.setParameterDefaults(objectMapper.writeValueAsString(strategyDTO.getParameterDefaults()));
            } catch (JsonProcessingException e) {
                return Result.error(400, "Invalid parameter defaults format");
            }
        }

        strategyService.updateById(strategy);
        return Result.success("Strategy updated successfully", strategy);
    }

    /**
     * 删除策略
     *
     * @param id 策略 ID
     * @return 成功消息
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除策略", description = "删除指定的策略")
    @Parameter(name = "id", description = "策略 ID", required = true, example = "1")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "删除成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "策略不存在"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public Result<String> deleteStrategy(@PathVariable Long id) {
        strategyService.removeById(id);
        return Result.success("Strategy deleted successfully");
    }
}
