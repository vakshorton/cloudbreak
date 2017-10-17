package com.sequenceiq.cloudbreak.controller.validation.stack;

import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.sequenceiq.cloudbreak.controller.validation.blueprint.BlueprintValidator;
import com.sequenceiq.cloudbreak.domain.Blueprint;
import com.sequenceiq.cloudbreak.domain.HostGroup;
import com.sequenceiq.cloudbreak.domain.InstanceGroup;

@Service
public class StackRelatedBlueprintValidator {

    @Inject
    private BlueprintValidator blueprintValidator;

    public void validate(Blueprint blueprint, Set<HostGroup> hostGroups, Set<InstanceGroup> instanceGroups, boolean validateBlueprint) {
        if (validateBlueprint) {
            blueprintValidator.validateBlueprintForStack(blueprint, hostGroups, instanceGroups);
        }
    }

    public void validateHostGroupScalingRequest(Blueprint blueprint, Optional<HostGroup> hostGroup, Integer adjustment) {
        blueprintValidator.validateHostGroupScalingRequest(blueprint, hostGroup.get(), adjustment);
    }
}
