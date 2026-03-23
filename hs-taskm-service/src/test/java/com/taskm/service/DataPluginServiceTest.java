package com.taskm.service;

import com.taskm.entity.DataPlugin;
import com.taskm.exception.DataPluginNotFoundException;
import com.taskm.mapper.DataPluginMapper;
import com.taskm.service.impl.DataPluginServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for DataPluginService.
 * Tests service layer business logic.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DataPluginServiceTest {

    @Autowired
    private DataPluginService dataPluginService;

    @Autowired
    private DataPluginMapper dataPluginMapper;

    private DataPlugin testPlugin1;
    private DataPlugin testPlugin2;

    @BeforeEach
    void setUp() {
        // Clean up database
        dataPluginMapper.delete(null);

        // Create test data
        testPlugin1 = new DataPlugin();
        testPlugin1.setName("stock-quote-plugin");
        testPlugin1.setDescription("Get stock quotes from Yahoo Finance");
        testPlugin1.setPluginType("market_data");
        testPlugin1.setLanguage("python");
        testPlugin1.setCode("def get_stock_quote(symbol): return {'symbol': symbol, 'price': 100.0}");

        Map<String, Object> params1 = new HashMap<>();
        params1.put("parameters", List.of(
            Map.of("name", "symbol", "type", "string", "default", "AAPL", "required", true)
        ));
        testPlugin1.setConfigParameters(params1);

        Map<String, Object> metadata1 = new HashMap<>();
        metadata1.put("author", "John Doe");
        metadata1.put("version", "1.0");
        testPlugin1.setMetadata(metadata1);

        testPlugin2 = new DataPlugin();
        testPlugin2.setName("news-sentiment-plugin");
        testPlugin2.setDescription("Analyze news sentiment for stocks");
        testPlugin2.setPluginType("analytics");
        testPlugin2.setLanguage("javascript");
        testPlugin2.setCode("function analyzeSentiment(symbol) { return { symbol, sentiment: 'positive' }; }");

        Map<String, Object> params2 = new HashMap<>();
        params2.put("parameters", List.of(
            Map.of("name", "symbol", "type", "string", "required", true)
        ));
        testPlugin2.setConfigParameters(params2);

        Map<String, Object> metadata2 = new HashMap<>();
        metadata2.put("author", "Jane Smith");
        metadata2.put("version", "2.0");
        testPlugin2.setMetadata(metadata2);
    }

    @Test
    void canGetAllPlugins() {
        dataPluginService.save(testPlugin1);
        dataPluginService.save(testPlugin2);

        List<DataPlugin> plugins = dataPluginService.getAllPlugins();
        assertEquals(2, plugins.size());
    }

    @Test
    void canGetPluginById() {
        dataPluginService.save(testPlugin1);
        Long id = testPlugin1.getId();

        DataPlugin found = dataPluginService.getPlugin(id);
        assertNotNull(found);
        assertEquals("stock-quote-plugin", found.getName());
        assertEquals("python", found.getLanguage());
    }

    @Test
    void shouldThrowExceptionWhenPluginNotFound() {
        assertThrows(DataPluginNotFoundException.class, () -> {
            dataPluginService.getPlugin(99999L);
        });
    }

    @Test
    void canFilterPluginsByLanguage() {
        dataPluginService.save(testPlugin1);
        dataPluginService.save(testPlugin2);

        List<DataPlugin> pythonPlugins = dataPluginService.getPluginsByLanguage("python");
        assertEquals(1, pythonPlugins.size());
        assertEquals("stock-quote-plugin", pythonPlugins.get(0).getName());
    }

    @Test
    void canFilterPluginsByType() {
        dataPluginService.save(testPlugin1);
        dataPluginService.save(testPlugin2);

        List<DataPlugin> analyticsPlugins = dataPluginService.getPluginsByType("analytics");
        assertEquals(1, analyticsPlugins.size());
        assertEquals("news-sentiment-plugin", analyticsPlugins.get(0).getName());
    }

    @Test
    void testPluginReturnsPlaceholder() {
        dataPluginService.save(testPlugin1);
        Long id = testPlugin1.getId();

        Map<String, Object> testParams = new HashMap<>();
        testParams.put("symbol", "AAPL");

        Map<String, Object> result = dataPluginService.testPlugin(id, testParams);

        assertNotNull(result);
        assertEquals(false, result.get("success"));
        assertEquals("Plugin testing not yet implemented", result.get("message"));
        assertEquals(id, result.get("pluginId"));
        assertEquals("stock-quote-plugin", result.get("pluginName"));
    }
}
