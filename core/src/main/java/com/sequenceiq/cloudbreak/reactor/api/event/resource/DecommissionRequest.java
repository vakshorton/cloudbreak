package com.sequenceiq.cloudbreak.reactor.api.event.resource;

import java.util.Optional;
import java.util.Set;

import com.sequenceiq.cloudbreak.orchestrator.container.ContainerOrchestrator;
import com.sequenceiq.cloudbreak.orchestrator.host.HostOrchestrator;

public class DecommissionRequest extends AbstractClusterScaleRequest {

    private final Set<String> hostNames;

    private final Optional<HostOrchestrator> hostOrchestrator;

    private final Optional<ContainerOrchestrator> containerOrchestrator;

    public DecommissionRequest(Long stackId, String hostGroupName, Set<String> hostNames,
            Optional<HostOrchestrator> hostOrchestrator, Optional<ContainerOrchestrator> containerOrchestrator) {
        super(stackId, hostGroupName);
        this.hostNames = hostNames;
        this.hostOrchestrator = hostOrchestrator;
        this.containerOrchestrator = containerOrchestrator;
    }

    public Set<String> getHostNames() {
        return hostNames;
    }

    public Optional<HostOrchestrator> getHostOrchestrator() {
        return hostOrchestrator;
    }

    public Optional<ContainerOrchestrator> getContainerOrchestrator() {
        return containerOrchestrator;
    }
}
