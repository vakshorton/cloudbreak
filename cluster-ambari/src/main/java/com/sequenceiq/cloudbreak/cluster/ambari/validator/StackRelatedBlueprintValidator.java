package com.sequenceiq.cloudbreak.cluster.ambari.validator;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.sequenceiq.cloudbreak.api.model.v2.AmbariV2Request;
import com.sequenceiq.cloudbreak.cluster.ClusterValidator;
import com.sequenceiq.cloudbreak.cluster.ambari.AmbariValidationException;
import com.sequenceiq.cloudbreak.domain.Stack;

@Service
public class StackRelatedBlueprintValidator implements ClusterValidator<Stack, AmbariValidationException> {

    @Inject
    private BlueprintValidator blueprintValidator;

    @Override
    public void validate(Stack stack) throws AmbariValidationException {
        if (stack.getCluster().getTopologyValidation()) {
            blueprintValidator.validateBlueprintForStack(stack.getCluster().getBlueprint(), stack.getCluster().getHostGroups(), stack.getInstanceGroups());
        }
    }

    @Override
    public Class getRelatedClass() {
        return AmbariV2Request.class;
    }
}
