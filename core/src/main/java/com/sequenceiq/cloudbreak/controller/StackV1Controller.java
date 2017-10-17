package com.sequenceiq.cloudbreak.controller;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.api.endpoint.v1.StackV1Endpoint;
import com.sequenceiq.cloudbreak.api.model.AmbariAddressJson;
import com.sequenceiq.cloudbreak.api.model.AutoscaleStackResponse;
import com.sequenceiq.cloudbreak.api.model.CertificateResponse;
import com.sequenceiq.cloudbreak.api.model.PlatformVariantsJson;
import com.sequenceiq.cloudbreak.api.model.StackRequest;
import com.sequenceiq.cloudbreak.api.model.StackResponse;
import com.sequenceiq.cloudbreak.api.model.StackValidationRequest;
import com.sequenceiq.cloudbreak.api.model.UpdateStackJson;
import com.sequenceiq.cloudbreak.cloud.model.CloudCredential;
import com.sequenceiq.cloudbreak.common.model.user.IdentityUser;
import com.sequenceiq.cloudbreak.controller.validation.StackSensitiveDataPropagator;
import com.sequenceiq.cloudbreak.controller.validation.filesystem.FileSystemValidator;
import com.sequenceiq.cloudbreak.controller.validation.stack.StackParameterValidator;
import com.sequenceiq.cloudbreak.controller.validation.stack.StackRelatedBlueprintValidator;
import com.sequenceiq.cloudbreak.controller.validation.stack.StackRelatedNetworkValidator;
import com.sequenceiq.cloudbreak.converter.spi.CredentialToCloudCredentialConverter;
import com.sequenceiq.cloudbreak.domain.Cluster;
import com.sequenceiq.cloudbreak.domain.Stack;
import com.sequenceiq.cloudbreak.domain.StackValidation;
import com.sequenceiq.cloudbreak.logger.MDCBuilder;
import com.sequenceiq.cloudbreak.service.account.AccountAndUserPermissionEvaluator;
import com.sequenceiq.cloudbreak.service.account.AccountPreferencesValidationFailed;
import com.sequenceiq.cloudbreak.service.decorator.Decorator;
import com.sequenceiq.cloudbreak.service.stack.StackService;

@Component
public class StackV1Controller extends NotificationController implements StackV1Endpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(StackV1Controller.class);

    @Autowired
    private StackService stackService;

    @Autowired
    @Qualifier("conversionService")
    private ConversionService conversionService;

    @Autowired
    private Decorator<Stack> stackDecorator;

    @Autowired
    private AccountAndUserPermissionEvaluator accountAndUserPermissionEvaluator;

    @Autowired
    private FileSystemValidator fileSystemValidator;

    @Autowired
    private AuthenticatedUserService authenticatedUserService;

    @Autowired
    private StackParameterValidator stackParameterValidator;

    @Autowired
    private CredentialToCloudCredentialConverter credentialToCloudCredentialConverter;

    @Autowired
    private StackSensitiveDataPropagator stackSensitiveDataPropagator;

    @Autowired
    private ClusterCreationSetupService clusterCreationService;

    @Autowired
    private StackCommonController stackCommonController;

    @Autowired
    private StackRelatedBlueprintValidator stackRelatedBlueprintValidator;

    @Autowired
    private StackRelatedNetworkValidator stackRelatedNetworkValidator;

    @Override
    public StackResponse postPrivate(StackRequest stackRequest) throws Exception {
        IdentityUser user = authenticatedUserService.getCbUser();
        return createStack(user, stackRequest, false);
    }

    @Override
    public StackResponse postPublic(StackRequest stackRequest) throws Exception {
        IdentityUser user = authenticatedUserService.getCbUser();
        return createStack(user, stackRequest, true);
    }

    private StackResponse createStack(IdentityUser user, StackRequest stackRequest, boolean publicInAccount) throws Exception {
        stackRequest.setAccount(user.getAccount());
        stackRequest.setOwner(user.getUserId());
        stackParameterValidator.validate(user, stackRequest.getName(), Optional.of(stackRequest.getCredentialSource()),
                stackRequest.getCredentialId(), Optional.of(stackRequest.getCredential()), stackRequest.getParameters());
        Stack stack = conversionService.convert(stackRequest, Stack.class);
        MDCBuilder.buildMdcContext(stack);
        stack = stackSensitiveDataPropagator.propagate(stackRequest.getCredentialSource(), stack, user);
        stack = stackDecorator.decorate(stack, stackRequest.getCredentialId(), stackRequest.getNetworkId(), user,
                stackRequest.getFlexId(), stackRequest.getCredentialName());
        stack.setPublicInAccount(publicInAccount);
        validateAccountPreferences(stack, user);

        if (stack.getOrchestrator() != null && stack.getOrchestrator().getApiEndpoint() != null) {
            stackService.validateContainerOrchestrator(stack.getOrchestrator());
        }

        if (stackRequest.getClusterRequest() != null) {
            StackValidationRequest stackValidationRequest = conversionService.convert(stackRequest, StackValidationRequest.class);
            StackValidation stackValidation = conversionService.convert(stackValidationRequest, StackValidation.class);
            stackRelatedBlueprintValidator.validate(
                    stackValidation.getBlueprint(),
                    stackValidation.getHostGroups(),
                    stackValidation.getInstanceGroups(),
                    stackRequest.getClusterRequest().getValidateBlueprint());
            stackRelatedNetworkValidator.validate(stackValidation.getNetwork(), stackValidation.getInstanceGroups());
            CloudCredential cloudCredential = credentialToCloudCredentialConverter.convert(stackValidation.getCredential());
            fileSystemValidator.validateFileSystem(stackValidationRequest.getPlatform(), cloudCredential, stackValidationRequest.getFileSystem());
            clusterCreationService.validate(stackRequest.getClusterRequest(), stack, user);
        }

        stack = stackService.create(user, stack, stackRequest.getAmbariVersion(), stackRequest.getHdpVersion(),
                stackRequest.getImageCatalog(), Optional.ofNullable(stackRequest.getCustomImage()));

        if (stackRequest.getClusterRequest() != null) {
            Cluster cluster = clusterCreationService.prepare(stackRequest.getClusterRequest(), stack, user);
            stack.setCluster(cluster);
        }
        return conversionService.convert(stack, StackResponse.class);
    }

    private void validateAccountPreferences(Stack stack, IdentityUser user) {
        try {
            accountAndUserPermissionEvaluator.validate(stack, user.getAccount(), user.getUserId());
        } catch (AccountPreferencesValidationFailed e) {
            throw new BadRequestException(e.getMessage(), e);
        }
    }

    @Override
    public Set<StackResponse> getPrivates() {
        return stackCommonController.getPrivates();
    }

    @Override
    public Set<StackResponse> getPublics() {
        return stackCommonController.getPublics();
    }

    @Override
    public StackResponse getPrivate(String name, Set<String> entries) {
        return stackCommonController.getPrivate(name, entries);
    }

    @Override
    public StackResponse getPublic(String name, Set<String> entries) {
        return stackCommonController.getPublic(name, entries);
    }

    @Override
    public StackResponse get(Long id, Set<String> entries) {
        return stackCommonController.get(id, entries);
    }

    @Override
    public void deletePublic(String name, Boolean forced, Boolean deleteDependencies) {
        stackCommonController.deletePublic(name, forced, deleteDependencies);
    }

    @Override
    public void deletePrivate(String name, Boolean forced, Boolean deleteDependencies) {
        stackCommonController.deletePrivate(name, forced, deleteDependencies);
    }

    @Override
    public void delete(Long id, Boolean forced, Boolean deleteDependencies) {
        stackCommonController.delete(id, forced, deleteDependencies);
    }

    @Override
    public Response put(Long id, UpdateStackJson updateRequest) {
        return stackCommonController.put(id, updateRequest);
    }

    @Override
    public Map<String, Object> status(Long id) {
        return stackCommonController.status(id);
    }

    @Override
    public PlatformVariantsJson variants() {
        return stackCommonController.variants();
    }

    @Override
    public Response deleteInstance(Long stackId, String instanceId) {
        return stackCommonController.deleteInstance(stackId, instanceId);
    }

    @Override
    public CertificateResponse getCertificate(Long stackId) {
        return stackCommonController.getCertificate(stackId);
    }

    @Override
    public Response validate(StackValidationRequest stackValidationRequest) {
        return stackCommonController.validate(stackValidationRequest);
    }

    @Override
    public StackResponse getStackForAmbari(AmbariAddressJson json) {
        return stackCommonController.getStackForAmbari(json);
    }

    @Override
    public Set<AutoscaleStackResponse> getAllForAutoscale() {
        return stackCommonController.getAllForAutoscale();
    }
}
