package com.jobportal;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobportal.connector.*;
import com.jobportal.domain.Platform;
import java.util.List;
import org.junit.jupiter.api.Test;

class ConnectorParseTest {
    private final ObjectMapper om = new ObjectMapper();

    @Test void greenhouseParsesJobs() throws Exception {
        var json = om.readTree("{\"jobs\":[{\"id\":42,\"title\":\"Java Developer\",\"location\":{\"name\":\"Remote\"},\"absolute_url\":\"https://x/42\",\"content\":\"&lt;p&gt;Spring Boot&lt;/p&gt;\",\"updated_at\":\"2025-01-02T03:04:05-05:00\"}]}");
        var jobs = new GreenhouseConnector(null).parse(json);
        assertEquals(1, jobs.size());
        assertEquals("42", jobs.get(0).externalId());
        assertEquals("Spring Boot", jobs.get(0).description());
        assertNotNull(jobs.get(0).postedAt());
    }
    @Test void leverParsesArray() throws Exception {
        var json = om.readTree("[{\"id\":\"a1\",\"text\":\"Backend Developer\",\"hostedUrl\":\"https://l/a1\",\"categories\":{\"location\":\"Pune\"},\"descriptionPlain\":\"Java\",\"createdAt\":1700000000000}]");
        var jobs = new LeverConnector(null).parse(json);
        assertEquals("Pune", jobs.get(0).location());
        assertNotNull(jobs.get(0).postedAt());
    }
    @Test void factoryResolvesByPlatform() {
        var f = new ConnectorFactory(List.of(new LeverConnector(null), new CustomPageConnector()));
        assertTrue(f.find(Platform.LEVER).isPresent());
        assertTrue(f.find(Platform.WORKDAY).isEmpty());
    }
}
