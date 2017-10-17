package com.sequenceiq.cloudbreak.cluster.ambari.validator;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.sequenceiq.cloudbreak.cluster.ClusterValidator;
import com.sequenceiq.cloudbreak.cluster.ambari.AmbariValidationException;
import com.sequenceiq.cloudbreak.domain.StackValidation;

@Service
public class StackValidationValidator implements ClusterValidator<StackValidation, AmbariValidationException> {

    @Inject
    private BlueprintValidator blueprintValidator;

    @Override
    public void validate(StackValidation request) throws AmbariValidationException {
        blueprintValidator.validateBlueprintForStack(request.getBlueprint(), request.getHostGroups(), request.getInstanceGroups());
    }

    @Override
    public Class getRelatedClass() {
        return StackValidation.class;
    }
}
