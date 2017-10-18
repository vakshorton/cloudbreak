package com.sequenceiq.cloudbreak.controller.validation.stack;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;

import com.sequenceiq.cloudbreak.api.model.StackResponse;
import com.sequenceiq.cloudbreak.api.model.StackValidationRequest;
import com.sequenceiq.cloudbreak.api.model.v2.StackV2Request;
import com.sequenceiq.cloudbreak.common.model.user.IdentityUser;
import com.sequenceiq.cloudbreak.controller.BadRequestException;
import com.sequenceiq.cloudbreak.controller.ClusterCreationSetupService;
import com.sequenceiq.cloudbreak.controller.validation.ClusterValidatorFactory;
import com.sequenceiq.cloudbreak.controller.validation.StackSensitiveDataPropagator;
import com.sequenceiq.cloudbreak.controller.validation.filesystem.FileSystemValidator;
import com.sequenceiq.cloudbreak.converter.spi.CredentialToCloudCredentialConverter;
import com.sequenceiq.cloudbreak.domain.Cluster;
import com.sequenceiq.cloudbreak.domain.Stack;
import com.sequenceiq.cloudbreak.domain.StackValidation;
import com.sequenceiq.cloudbreak.logger.MDCBuilder;
import com.sequenceiq.cloudbreak.service.account.AccountAndUserPermissionEvaluator;
import com.sequenceiq.cloudbreak.service.account.AccountPreferencesValidationFailed;
import com.sequenceiq.cloudbreak.service.decorator.Decorator;
import com.sequenceiq.cloudbreak.service.stack.StackService;

@Service
public class StackPreparatorService {

    @Autowired
    private StackService stackService;

    @Autowired
    @Qualifier("conversionService")
    private ConversionService conversionService;

    @Autowired
    private Decorator<Stack, StackV2Request> stackDecorator;

    @Autowired
    private AccountAndUserPermissionEvaluator accountAndUserPermissionEvaluator;

    @Autowired
    private FileSystemValidator fileSystemValidator;

    @Autowired
    private StackParameterValidator stackParameterValidator;

    @Autowired
    private CredentialToCloudCredentialConverter credentialToCloudCredentialConverter;

    @Autowired
    private StackSensitiveDataPropagator stackSensitiveDataPropagator;

    @Autowired
    private ClusterCreationSetupService clusterCreationService;

    @Autowired
    private StackRelatedNetworkValidator stackRelatedNetworkValidator;

    @Autowired
    private ClusterValidatorFactory clusterValidatorFactory;

    public StackResponse createStack(IdentityUser user, StackV2Request stackV2Request, boolean publicInAccount) throws Exception {
        updateRequestWithMissingObjects(user, stackV2Request);

        stackParameterValidator.validate(
                user,
                stackV2Request.getName(),
                Optional.empty(),
                stackV2Request.getCredentialId(),
                Optional.empty(),
                stackV2Request.getParameters());

        Stack stack = conversionService.convert(stackV2Request, Stack.class);

        MDCBuilder.buildMdcContext(stack);

        stack = stackDecorator.decorate(stack, stackV2Request, user);

        validateAccountPreferences(stack, user);
        stack.setPublicInAccount(publicInAccount);

        if (containerOrchestratorBasedStack(stack)) {
            stackService.validateContainerOrchestrator(stack.getOrchestrator());
        }

        if (stackRequestIncludesClusterRequest(stackV2Request)) {
            StackValidationRequest stackValidationRequest = prepareStackValidationRequest(stackV2Request);
            StackValidation stackValidation = prepareStackValidation(stackValidationRequest);

            clusterValidatorFactory.validate(stackValidation);
            stackRelatedNetworkValidator.validate(stack.getNetwork(), stack.getInstanceGroups());

            fileSystemValidator.validateFileSystem(stackValidationRequest.getPlatform(),
                    credentialToCloudCredentialConverter.convert(stackValidation.getCredential()),
                    stackValidationRequest.getFileSystem());
            clusterCreationService.validate(stackV2Request.getClusterRequest(), stack, user);
        }

        stack = stackService.create(user,
                stack,
                stackV2Request.getAmbariVersion(),
                stackV2Request.getHdpVersion(),
                stackV2Request.getImageCatalog(),
                Optional.ofNullable(stackV2Request.getCustomImage()));

        if (stackRequestIncludesClusterRequest(stackV2Request)) {
            Cluster cluster = clusterCreationService.prepare(stackV2Request.getClusterRequest(), stack, user, stackV2Request.getInstanceGroups());
            stack.setCluster(cluster);
        }
        return conversionService.convert(stack, StackResponse.class);
    }

    private void updateRequestWithMissingObjects(IdentityUser user, StackV2Request stackV2Request) {
        stackV2Request.setAccount(user.getAccount());
        stackV2Request.setOwner(user.getUserId());
        stackV2Request.getClusterRequest().setName(stackV2Request.getName());
    }

    private StackValidationRequest prepareStackValidationRequest(StackV2Request stackV2Request) {
        return conversionService.convert(stackV2Request, StackValidationRequest.class);
    }


    private StackValidation prepareStackValidation(StackValidationRequest stackValidationRequest) {
        return conversionService.convert(stackValidationRequest, StackValidation.class);
    }

    private boolean stackRequestIncludesClusterRequest(StackV2Request stackV2Request) {
        return stackV2Request.getClusterRequest() != null;
    }

    private boolean containerOrchestratorBasedStack(Stack stack) {
        return stack.getOrchestrator() != null && stack.getOrchestrator().getApiEndpoint() != null;
    }


    private void validateAccountPreferences(Stack stack, IdentityUser user) {
        try {
            accountAndUserPermissionEvaluator.validate(stack, user.getAccount(), user.getUserId());
        } catch (AccountPreferencesValidationFailed e) {
            throw new BadRequestException(e.getMessage(), e);
        }
    }

}
