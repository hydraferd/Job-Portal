package com.jobportal;

import static org.junit.jupiter.api.Assertions.*;

import com.jobportal.connector.WorkdayConnector;
import org.junit.jupiter.api.Test;

class WorkdaySupportTest {
    @Test void parsesSiteWithLocale() {
        var s = WorkdayConnector.parseSite("https://acme.wd3.myworkdayjobs.com/en-US/Acme_Careers");
        assertEquals("acme.wd3.myworkdayjobs.com", s.host());
        assertEquals("acme", s.tenant());
        assertEquals("Acme_Careers", s.site());
    }
    @Test void parsesSiteWithoutLocale() {
        assertEquals("Ext", WorkdayConnector.parseSite("https://x.wd1.myworkdayjobs.com/Ext/").site());
    }
    @Test void rejectsNonWorkdayUrl() {
        assertThrows(IllegalArgumentException.class, () -> WorkdayConnector.parseSite("https://example.com/careers"));
        assertThrows(IllegalArgumentException.class, () -> WorkdayConnector.parseSite("https://x.wd1.myworkdayjobs.com/"));
    }
}
