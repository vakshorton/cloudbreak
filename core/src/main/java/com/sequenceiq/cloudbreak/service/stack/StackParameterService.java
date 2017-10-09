package com.sequenceiq.cloudbreak.service.stack;

import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;

import com.google.common.base.Strings;
import com.sequenceiq.cloudbreak.api.model.StackRequest;
import com.sequenceiq.cloudbreak.cloud.context.CloudContext;
import com.sequenceiq.cloudbreak.cloud.event.model.EventStatus;
import com.sequenceiq.cloudbreak.cloud.event.platform.GetStackParamValidationRequest;
import com.sequenceiq.cloudbreak.cloud.event.platform.GetStackParamValidationResult;
import com.sequenceiq.cloudbreak.cloud.model.StackParamValidation;
import com.sequenceiq.cloudbreak.cloud.reactor.ErrorHandlerAwareReactorEventFactory;
import com.sequenceiq.cloudbreak.common.model.user.IdentityUser;
import com.sequenceiq.cloudbreak.domain.Credential;
import com.sequenceiq.cloudbreak.service.credential.CredentialService;
import com.sequenceiq.cloudbreak.service.stack.connector.OperationException;

import reactor.bus.EventBus;

@Service
public class StackParameterService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StackParameterService.class);

    @Inject
    private CredentialService credentialService;

    @Resource
    @Qualifier("conversionService")
    private ConversionService conversionService;

    @Inject
    private EventBus eventBus;

    @Inject
    private ErrorHandlerAwareReactorEventFactory eventFactory;

    public List<StackParamValidation> getStackParams(IdentityUser user, StackRequest stackRequest) {
        LOGGER.debug("Get stack params");
        Long credentialId = stackRequest.getCredentialId();
        String credentialName = stackRequest.getCredentialName();

        if (credentialId != null || !Strings.isNullOrEmpty(credentialName)) {
            Credential credential = null;
            if (credentialId != null) {
                credential = credentialService.get(credentialId);
            } else if (!Strings.isNullOrEmpty(credentialName)) {
                credential = credentialService.get(credentialName, user.getAccount());
            }
            if (credential != null) {
                CloudContext cloudContext = new CloudContext(null, stackRequest.getName(), credential.cloudPlatform(), credential.getOwner());

                GetStackParamValidationRequest getStackParamValidationRequest = new GetStackParamValidationRequest(cloudContext);
                eventBus.notify(getStackParamValidationRequest.selector(), eventFactory.createEvent(getStackParamValidationRequest));
                try {
                    GetStackParamValidationResult res = getStackParamValidationRequest.await();
                    LOGGER.info("Get stack params result: {}", res);
                    if (res.getStatus().equals(EventStatus.FAILED)) {
                        LOGGER.error("Failed to get stack params", res.getErrorDetails());
                        throw new OperationException(res.getErrorDetails());
                    }
                    return res.getStackParamValidations();
                } catch (InterruptedException e) {
                    LOGGER.error("Error while getting the stack params", e);
                    throw new OperationException(e);
                }
            }
        }
        return Collections.emptyList();
    }
}
