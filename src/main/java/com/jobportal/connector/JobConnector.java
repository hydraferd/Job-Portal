package com.jobportal.connector;

import com.jobportal.domain.*;
import java.util.List;

/** Strategy: one implementation per career-platform, discovered through public, read-only endpoints. */
public interface JobConnector {
    Platform platform();
    List<DiscoveredJob> fetch(Company company) throws Exception;
}
