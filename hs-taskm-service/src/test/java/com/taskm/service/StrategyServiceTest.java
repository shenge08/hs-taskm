package com.taskm.service;

import com.taskm.entity.Strategy;
import com.taskm.mapper.StrategyMapper;
import com.taskm.exception.StrategyNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test class for StrategyService.
 * Tests business logic operations for strategies.
 */
@SpringBootTest(classes = com.taskm.TestApplication.class)
@ActiveProfiles("test")
@Transactional
class StrategyServiceTest {

    @Autowired
    private StrategyService strategyService;

    @MockBean
    private StrategyMapper strategyMapper;

    private Strategy pythonStrategy;
    private Strategy jsStrategy;

    @BeforeEach
    void setUp() {
        // Reset mock before each test
        reset(strategyMapper);

        // Create test data
        pythonStrategy = new Strategy();
        pythonStrategy.setId(1L);
        pythonStrategy.setName("python-strategy");
        pythonStrategy.setDescription("Python test strategy");
        pythonStrategy.setLanguage("python");
        pythonStrategy.setCode("print('test')");

        jsStrategy = new Strategy();
        jsStrategy.setId(2L);
        jsStrategy.setName("js-strategy");
        jsStrategy.setDescription("JavaScript test strategy");
        jsStrategy.setLanguage("javascript");
        jsStrategy.setCode("console.log('test');");
    }

    @Test
    void canGetAllStrategies() {
        // Given
        when(strategyMapper.selectList(any())).thenReturn(Arrays.asList(pythonStrategy, jsStrategy));

        // When
        List<Strategy> strategies = strategyService.getAllStrategies();

        // Then
        assertThat(strategies).hasSize(2);
        assertThat(strategies).extracting("name")
                .containsExactly("python-strategy", "js-strategy");
    }

    @Test
    void canGetStrategyById() {
        // Given
        when(strategyMapper.selectById(1L)).thenReturn(pythonStrategy);

        // When
        Strategy strategy = strategyService.getStrategy(1L);

        // Then
        assertThat(strategy).isNotNull();
        assertThat(strategy.getName()).isEqualTo("python-strategy");
        assertThat(strategy.getLanguage()).isEqualTo("python");
    }

    @Test
    void shouldThrowExceptionWhenStrategyNotFound() {
        // Given
        when(strategyMapper.selectById(999L)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> strategyService.getStrategy(999L))
                .isInstanceOf(StrategyNotFoundException.class)
                .hasMessageContaining("Strategy not found with id: 999");
    }

    @Test
    void canGetStrategiesByLanguage() {
        // Given
        when(strategyMapper.selectList(any())).thenReturn(Arrays.asList(pythonStrategy));

        // When
        List<Strategy> strategies = strategyService.getStrategiesByLanguage("python");

        // Then
        assertThat(strategies).hasSize(1);
        assertThat(strategies.get(0).getLanguage()).isEqualTo("python");
    }
}
