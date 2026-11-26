package com.jobportal.connector;

import com.jobportal.domain.Platform;
import java.util.*;
import org.springframework.stereotype.Component;

/** Factory: resolves the connector strategy for a platform. Empty means discovery is unsupported. */
@Component
public class ConnectorFactory {
    private final Map<Platform, JobConnector> byPlatform = new EnumMap<>(Platform.class);

    public ConnectorFactory(List<JobConnector> connectors) {
        connectors.forEach(c -> byPlatform.put(c.platform(), c));
    }
    public Optional<JobConnector> find(Platform p) { return Optional.ofNullable(byPlatform.get(p)); }
}
