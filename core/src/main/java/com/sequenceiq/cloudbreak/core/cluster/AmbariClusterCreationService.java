package com.sequenceiq.cloudbreak.core.cluster;

import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.sequenceiq.cloudbreak.CloudbreakException;
import com.sequenceiq.cloudbreak.domain.SmartSenseSubscription;
import com.sequenceiq.cloudbreak.domain.Stack;
import com.sequenceiq.cloudbreak.cluster.ambari.task.AmbariClusterConnector;
import com.sequenceiq.cloudbreak.service.smartsense.SmartSenseSubscriptionService;
import com.sequenceiq.cloudbreak.service.stack.StackService;

@Service
public class AmbariClusterCreationService {
    @Inject
    private StackService stackService;

    @Inject
    private AmbariClusterConnector ambariClusterConnector;

    @Inject
    private SmartSenseSubscriptionService smartSenseSubscriptionService;

    public void startAmbari(Long stackId) throws CloudbreakException {
        Stack stack = stackService.getById(stackId);
        ambariClusterConnector.waitForAmbariServer(stack);
        ambariClusterConnector.changeOriginalAmbariCredentialsAndCreateCloudbreakUser(stack);
    }

    public void buildAmbariCluster(Long stackId) {
        Stack stack = stackService.getByIdWithLists(stackId);
        Optional<SmartSenseSubscription> smartSenseSubscription = smartSenseSubscriptionService.getDefault();
        ambariClusterConnector.buildAmbariCluster(stack, smartSenseSubscription);
    }
}
