package com.taskm.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.taskm.entity.Strategy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for StrategyMapper.
 * Verifies CRUD operations and query methods work correctly.
 */
@SpringBootTest(classes = com.taskm.TestApplication.class)
@ActiveProfiles("test")
@Transactional
class StrategyMapperTest {

    @Autowired
    private StrategyMapper strategyMapper;

    @Test
    void canInsertStrategy() {
        // Given
        Strategy strategy = new Strategy();
        strategy.setName("test-strategy");
        strategy.setDescription("Test strategy description");
        strategy.setLanguage("python");
        strategy.setCode("print('Hello, World!')");

        // When
        int result = strategyMapper.insert(strategy);

        // Then
        assertThat(result).isGreaterThan(0);
        assertThat(strategy.getId()).isNotNull();
    }

    @Test
    void canSelectById() {
        // Given
        Strategy strategy = new Strategy();
        strategy.setName("test-strategy-2");
        strategy.setLanguage("javascript");
        strategy.setCode("console.log('test');");
        strategyMapper.insert(strategy);

        // When
        Strategy found = strategyMapper.selectById(strategy.getId());

        // Then
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("test-strategy-2");
        assertThat(found.getLanguage()).isEqualTo("javascript");
    }

    @Test
    void canSelectAll() {
        // Given
        Strategy s1 = new Strategy();
        s1.setName("strategy-1");
        s1.setLanguage("python");
        s1.setCode("code1");
        strategyMapper.insert(s1);

        Strategy s2 = new Strategy();
        s2.setName("strategy-2");
        s2.setLanguage("javascript");
        s2.setCode("code2");
        strategyMapper.insert(s2);

        // When
        List<Strategy> strategies = strategyMapper.selectList(null);

        // Then
        assertThat(strategies).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void canFilterByLanguage() {
        // Given
        Strategy pythonStrategy = new Strategy();
        pythonStrategy.setName("python-test");
        pythonStrategy.setLanguage("python");
        pythonStrategy.setCode("python code");
        strategyMapper.insert(pythonStrategy);

        Strategy jsStrategy = new Strategy();
        jsStrategy.setName("js-test");
        jsStrategy.setLanguage("javascript");
        jsStrategy.setCode("js code");
        strategyMapper.insert(jsStrategy);

        // When
        QueryWrapper<Strategy> wrapper = new QueryWrapper<>();
        wrapper.eq("language", "python");
        List<Strategy> pythonStrategies = strategyMapper.selectList(wrapper);

        // Then
        assertThat(pythonStrategies).isNotEmpty();
        assertThat(pythonStrategies).allMatch(s -> "python".equals(s.getLanguage()));
    }

    @Test
    void canUpdateStrategy() {
        // Given
        Strategy strategy = new Strategy();
        strategy.setName("update-test");
        strategy.setDescription("Original description");
        strategy.setLanguage("python");
        strategy.setCode("original code");
        strategyMapper.insert(strategy);

        // When
        strategy.setDescription("Updated description");
        strategy.setCode("updated code");
        int result = strategyMapper.updateById(strategy);

        // Then
        assertThat(result).isGreaterThan(0);
        Strategy updated = strategyMapper.selectById(strategy.getId());
        assertThat(updated.getDescription()).isEqualTo("Updated description");
        assertThat(updated.getCode()).isEqualTo("updated code");
    }

    @Test
    void canDeleteStrategy() {
        // Given
        Strategy strategy = new Strategy();
        strategy.setName("delete-test");
        strategy.setLanguage("python");
        strategy.setCode("code");
        strategyMapper.insert(strategy);
        Long id = strategy.getId();

        // When
        int result = strategyMapper.deleteById(id);

        // Then
        assertThat(result).isGreaterThan(0);
        Strategy deleted = strategyMapper.selectById(id);
        assertThat(deleted).isNull();
    }
}
