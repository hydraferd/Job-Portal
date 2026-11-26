package com.jobportal.connector;

import java.time.*;
import org.jsoup.Jsoup;

final class Text {
    private Text() {}
    static String html(String s) { return s == null ? "" : Jsoup.parse(s).text(); }
    static Instant iso(String s) {
        if (s == null || s.isBlank()) return null;
        try { return OffsetDateTime.parse(s).toInstant(); } catch (Exception e) { return null; }
    }
    static Instant millis(long ms) { return ms > 0 ? Instant.ofEpochMilli(ms) : null; }
}
