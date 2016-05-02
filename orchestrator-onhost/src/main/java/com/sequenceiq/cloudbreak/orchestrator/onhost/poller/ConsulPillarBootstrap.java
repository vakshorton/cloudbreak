package com.sequenceiq.cloudbreak.orchestrator.onhost.poller;

import com.sequenceiq.cloudbreak.orchestrator.OrchestratorBootstrap;
import com.sequenceiq.cloudbreak.orchestrator.exception.CloudbreakOrchestratorFailedException;
import com.sequenceiq.cloudbreak.orchestrator.onhost.client.OnHostClient;

public class ConsulPillarBootstrap implements OrchestratorBootstrap {

    private final OnHostClient client;
    private final String path;
    private final String json;

    public ConsulPillarBootstrap(OnHostClient client, String path, String json) {
        this.client = client;
        this.path = path;
        this.json = json;
    }

    @Override
    public Boolean call() throws Exception {
        if (!client.copySaltPillar(path, json)) {
            throw new CloudbreakOrchestratorFailedException("Failed to save salt pillar config");
        }
        return true;
    }
}
