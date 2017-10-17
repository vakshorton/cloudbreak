package com.sequenceiq.cloudbreak.controller.validation.stack;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.api.model.CredentialRequest;
import com.sequenceiq.cloudbreak.api.model.CredentialSourceRequest;
import com.sequenceiq.cloudbreak.cloud.model.StackParamValidation;
import com.sequenceiq.cloudbreak.common.model.user.IdentityUser;
import com.sequenceiq.cloudbreak.service.stack.StackParameterService;

@Component
public class StackParameterValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(StackParameterValidator.class);

    @Inject
    private List<ParameterValidator> parameterValidators;

    @Inject
    private StackParameterService stackParameterService;

    public void validate(IdentityUser user, String name, Optional<CredentialSourceRequest> credentialSourceRequest,
            Long credentialId, Optional<CredentialRequest> credential, Map<String, String> parameters) {
        List<StackParamValidation> stackParamValidations = stackParameterService
                .getStackParams(user, name, credentialSourceRequest.orElse(null), credentialId, credential.orElse(null));
        for (ParameterValidator parameterValidator : parameterValidators) {
            parameterValidator.validate(parameters, stackParamValidations);
        }
    }

}

