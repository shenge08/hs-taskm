package com.taskm.service;

import com.taskm.entity.Listener;
import com.taskm.exception.ListenerNotFoundException;
import com.taskm.mapper.ListenerMapper;
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
 * Test class for ListenerService.
 * Tests service layer business logic.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ListenerServiceTest {

    @Autowired
    private ListenerService listenerService;

    @Autowired
    private ListenerMapper listenerMapper;

    private Listener testListener1;
    private Listener testListener2;

    @BeforeEach
    void setUp() {
        // Clean up database
        listenerMapper.delete(null);

        // Create test data
        testListener1 = new Listener();
        testListener1.setName("email-notification-listener");
        testListener1.setDescription("Send email notifications on task completion");
        testListener1.setEventType("TASK_COMPLETED");
        testListener1.setLanguage("python");
        testListener1.setCode("def send_notification(task): send_email(task)");
        testListener1.setEnabled(true);

        Map<String, Object> params1 = new HashMap<>();
        params1.put("parameters", List.of(
            Map.of("name", "recipients", "type", "array", "default", List.of("admin@example.com"), "required", true)
        ));
        testListener1.setParameters(params1);

        Map<String, Object> defaults1 = new HashMap<>();
        defaults1.put("recipients", List.of("admin@example.com"));
        testListener1.setParameterDefaults(defaults1);

        testListener2 = new Listener();
        testListener2.setName("webhook-listener");
        testListener2.setDescription("Send webhook notifications on signal generation");
        testListener2.setEventType("SIGNAL_GENERATED");
        testListener2.setLanguage("javascript");
        testListener2.setCode("function sendWebhook(signal) { /* webhook code */ }");
        testListener2.setEnabled(false);

        Map<String, Object> params2 = new HashMap<>();
        params2.put("parameters", List.of(
            Map.of("name", "url", "type", "string", "required", true)
        ));
        testListener2.setParameters(params2);

        Map<String, Object> defaults2 = new HashMap<>();
        defaults2.put("url", "https://example.com/webhook");
        testListener2.setParameterDefaults(defaults2);
    }

    @Test
    void canGetAllListeners() {
        listenerService.save(testListener1);
        listenerService.save(testListener2);

        List<Listener> listeners = listenerService.getAllListeners();
        assertEquals(2, listeners.size());
    }

    @Test
    void canGetListenerById() {
        listenerService.save(testListener1);
        Long id = testListener1.getId();

        Listener found = listenerService.getListener(id);
        assertNotNull(found);
        assertEquals("email-notification-listener", found.getName());
        assertEquals("python", found.getLanguage());
    }

    @Test
    void shouldThrowExceptionWhenListenerNotFound() {
        assertThrows(ListenerNotFoundException.class, () -> {
            listenerService.getListener(99999L);
        });
    }

    @Test
    void canFilterListenersByLanguage() {
        listenerService.save(testListener1);
        listenerService.save(testListener2);

        List<Listener> pythonListeners = listenerService.getListenersByLanguage("python");
        assertEquals(1, pythonListeners.size());
        assertEquals("email-notification-listener", pythonListeners.get(0).getName());
    }

    @Test
    void canFilterListenersByEventType() {
        listenerService.save(testListener1);
        listenerService.save(testListener2);

        List<Listener> signalListeners = listenerService.getListenersByEventType("SIGNAL_GENERATED");
        assertEquals(1, signalListeners.size());
        assertEquals("webhook-listener", signalListeners.get(0).getName());
    }
}
