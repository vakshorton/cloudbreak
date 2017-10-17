package com.sequenceiq.cloudbreak.controller.validation.stack;

import java.util.Set;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.sequenceiq.cloudbreak.controller.validation.network.NetworkConfigurationValidator;
import com.sequenceiq.cloudbreak.domain.InstanceGroup;
import com.sequenceiq.cloudbreak.domain.Network;

@Service
public class StackRelatedNetworkValidator {

    @Inject
    private NetworkConfigurationValidator networkConfigurationValidator;

    public void validate(Network network, Set<InstanceGroup> instanceGroups) {
        if(network != null){
            networkConfigurationValidator.validateNetworkForStack(network, instanceGroups);
        }
    }
}
