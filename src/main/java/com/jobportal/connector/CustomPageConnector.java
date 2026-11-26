package com.jobportal.connector;

import com.jobportal.domain.*;
import java.util.*;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.springframework.stereotype.Component;

/** Best-effort static-HTML link discovery for custom career pages. Does not execute JavaScript. */
@Component
public class CustomPageConnector implements JobConnector {
    private static final Pattern ROLE = Pattern.compile("java|spring|backend|back-end|microservice|full.?stack", Pattern.CASE_INSENSITIVE);
    public Platform platform() { return Platform.CUSTOM; }

    public List<DiscoveredJob> fetch(Company c) throws Exception {
        Document doc = Jsoup.connect(c.careersUrl).userAgent("JobPortalBot/0.1").timeout(20000).get();
        Map<String, DiscoveredJob> out = new LinkedHashMap<>();
        for (Element a : doc.select("a[href]")) {
            String text = a.text().trim(), href = a.absUrl("href");
            if (text.length() < 6 || text.length() > 150 || href.isBlank() || !ROLE.matcher(text).find()) continue;
            out.putIfAbsent(href, new DiscoveredJob(Integer.toHexString(href.hashCode()), text, "", href, text, null));
        }
        return new ArrayList<>(out.values());
    }
}
