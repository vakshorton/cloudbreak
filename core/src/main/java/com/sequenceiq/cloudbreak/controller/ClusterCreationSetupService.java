package com.sequenceiq.cloudbreak.controller;

import static com.sequenceiq.cloudbreak.common.type.CloudConstants.BYOS;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.convert.ConversionService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Sets;
import com.sequenceiq.cloudbreak.api.model.AmbariDatabaseDetailsJson;
import com.sequenceiq.cloudbreak.api.model.AmbariRepoDetailsJson;
import com.sequenceiq.cloudbreak.api.model.AmbariStackDetailsJson;
import com.sequenceiq.cloudbreak.api.model.BlueprintRequest;
import com.sequenceiq.cloudbreak.api.model.ClusterRequest;
import com.sequenceiq.cloudbreak.api.model.HostGroupRequest;
import com.sequenceiq.cloudbreak.api.model.v2.ClusterV2Request;
import com.sequenceiq.cloudbreak.api.model.v2.InstanceGroupV2Request;
import com.sequenceiq.cloudbreak.cloud.model.AmbariDatabase;
import com.sequenceiq.cloudbreak.cloud.model.AmbariRepo;
import com.sequenceiq.cloudbreak.cloud.model.CloudCredential;
import com.sequenceiq.cloudbreak.cloud.model.DefaultHDFEntries;
import com.sequenceiq.cloudbreak.cloud.model.DefaultHDFInfo;
import com.sequenceiq.cloudbreak.cloud.model.DefaultHDPEntries;
import com.sequenceiq.cloudbreak.cloud.model.DefaultHDPInfo;
import com.sequenceiq.cloudbreak.cloud.model.HDPInfo;
import com.sequenceiq.cloudbreak.cloud.model.HDPRepo;
import com.sequenceiq.cloudbreak.cluster.ambari.domain.KerberosValidationRequest;
import com.sequenceiq.cloudbreak.common.model.user.IdentityUser;
import com.sequenceiq.cloudbreak.common.type.ComponentType;
import com.sequenceiq.cloudbreak.controller.validation.ClusterValidatorFactory;
import com.sequenceiq.cloudbreak.controller.validation.filesystem.FileSystemValidator;
import com.sequenceiq.cloudbreak.converter.spi.CredentialToCloudCredentialConverter;
import com.sequenceiq.cloudbreak.domain.Blueprint;
import com.sequenceiq.cloudbreak.domain.Cluster;
import com.sequenceiq.cloudbreak.domain.ClusterComponent;
import com.sequenceiq.cloudbreak.domain.Component;
import com.sequenceiq.cloudbreak.domain.Stack;
import com.sequenceiq.cloudbreak.domain.json.Json;
import com.sequenceiq.cloudbreak.logger.MDCBuilder;
import com.sequenceiq.cloudbreak.service.ComponentConfigProvider;
import com.sequenceiq.cloudbreak.service.blueprint.BlueprintService;
import com.sequenceiq.cloudbreak.service.blueprint.BlueprintUtils;
import com.sequenceiq.cloudbreak.service.cluster.ClusterService;
import com.sequenceiq.cloudbreak.service.decorator.ClusterDecorator;
import com.sequenceiq.cloudbreak.service.decorator.ClusterV2Decorator;
import com.sequenceiq.cloudbreak.util.JsonUtil;

@org.springframework.stereotype.Component
public class ClusterCreationSetupService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterCreationSetupService.class);

    @Autowired
    private FileSystemValidator fileSystemValidator;

    @Autowired
    private CredentialToCloudCredentialConverter credentialToCloudCredentialConverter;

    @Autowired
    @Qualifier("conversionService")
    private ConversionService conversionService;

    @Autowired
    private ClusterDecorator clusterDecorator;

    @Autowired
    private ClusterV2Decorator clusterV2Decorator;

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private ComponentConfigProvider componentConfigProvider;

    @Autowired
    private BlueprintUtils blueprintUtils;

    @Autowired
    private DefaultHDPEntries defaultHDPEntries;

    @Autowired
    private DefaultHDFEntries defaultHDFEntries;

    @Autowired
    private BlueprintService blueprintService;

    @Autowired
    private ClusterValidatorFactory clusterValidatorFactory;

    public void validate(ClusterRequest request, Stack stack, IdentityUser user) {
        clusterValidatorFactory.validate(new KerberosValidationRequest(request.getEnableSecurity(), request.getKerberos()));
        MDCBuilder.buildUserMdcContext(user);
        if (!stack.isAvailable() && BYOS.equals(stack.cloudPlatform())) {
            throw new BadRequestException("Stack is not in 'AVAILABLE' status, cannot create cluster now.");
        }
        CloudCredential cloudCredential = credentialToCloudCredentialConverter.convert(stack.getCredential());

        fileSystemValidator.validateFileSystem(stack.cloudPlatform(), cloudCredential, request.getFileSystem());
    }

    public void validate(ClusterV2Request request, Stack stack, IdentityUser user) {
        clusterValidatorFactory.validate(new KerberosValidationRequest(request.getAmbariRequest().getEnableSecurity(), request.getAmbariRequest().getKerberos()));
        MDCBuilder.buildUserMdcContext(user);
        if (!stack.isAvailable() && BYOS.equals(stack.cloudPlatform())) {
            throw new BadRequestException("Stack is not in 'AVAILABLE' status, cannot create cluster now.");
        }

    }

    public Cluster prepare(ClusterRequest request, Stack stack, IdentityUser user) throws Exception {
        Cluster cluster = conversionService.convert(request, Cluster.class);
        cluster = clusterDecorator.decorate(cluster, request, user);
        List<ClusterComponent> components = new ArrayList<>();
        Set<Component> allComponent = componentConfigProvider.getAllComponentsByStackIdAndType(stack.getId(),
                Sets.newHashSet(ComponentType.AMBARI_REPO_DETAILS, ComponentType.HDP_REPO_DETAILS));
        Optional<Component> stackAmbariRepoConfig = allComponent.stream().filter(c -> c.getComponentType().equals(ComponentType.AMBARI_REPO_DETAILS)
                && c.getName().equalsIgnoreCase(ComponentType.AMBARI_REPO_DETAILS.name())).findAny();
        Optional<Component> stackHdpRepoConfig = allComponent.stream().filter(c -> c.getComponentType().equals(ComponentType.HDP_REPO_DETAILS)
                && c.getName().equalsIgnoreCase(ComponentType.HDP_REPO_DETAILS.name())).findAny();
        components = addAmbariRepoConfig(stackAmbariRepoConfig, components, request.getAmbariRepoDetailsJson(), cluster);
        components = addHDPRepoConfig(stackHdpRepoConfig, components, request.getAmbariStackDetails(), cluster, user,
                request.getBlueprint(), request.getBlueprintId(), request.getBlueprintName());
        components = addAmbariDatabaseConfig(components, request.getAmbariDatabaseDetails(), cluster);
        return clusterService.create(user, stack, cluster, components);
    }


    public Cluster prepare(ClusterV2Request request, Stack stack, IdentityUser user, List<InstanceGroupV2Request> instanceGroups) throws Exception {
        Cluster cluster = conversionService.convert(request, Cluster.class);
        cluster = clusterV2Decorator.decorate(cluster, request, user, stack.getId(), hostGroupRequests(instanceGroups));
        List<ClusterComponent> components = new ArrayList<>();
        Set<Component> allComponent = componentConfigProvider.getAllComponentsByStackIdAndType(stack.getId(),
                Sets.newHashSet(ComponentType.AMBARI_REPO_DETAILS, ComponentType.HDP_REPO_DETAILS));
        Optional<Component> stackAmbariRepoConfig = allComponent.stream().filter(c -> c.getComponentType().equals(ComponentType.AMBARI_REPO_DETAILS)
                && c.getName().equalsIgnoreCase(ComponentType.AMBARI_REPO_DETAILS.name())).findAny();
        Optional<Component> stackHdpRepoConfig = allComponent.stream().filter(c -> c.getComponentType().equals(ComponentType.HDP_REPO_DETAILS)
                && c.getName().equalsIgnoreCase(ComponentType.HDP_REPO_DETAILS.name())).findAny();
        components = addAmbariRepoConfig(stackAmbariRepoConfig, components, request.getAmbariRequest().getAmbariRepoDetailsJson(), cluster);
        components = addHDPRepoConfig(stackHdpRepoConfig, components, request.getAmbariRequest().getAmbariStackDetails(), cluster, user,
                null, request.getAmbariRequest().getBlueprintId(), request.getAmbariRequest().getBlueprintName());
        components = addAmbariDatabaseConfig(components, request.getAmbariRequest().getAmbariDatabaseDetails(), cluster);
        return clusterService.create(user, stack, cluster, components);
    }

    private Set<HostGroupRequest> hostGroupRequests(List<InstanceGroupV2Request> instanceGroups){
        return instanceGroups.stream()
                .map(instanceGroup -> conversionService.convert(instanceGroup, HostGroupRequest.class)).collect(Collectors.toSet());
    }

    private List<ClusterComponent> addAmbariRepoConfig(Optional<Component> stackAmbariRepoConfig, List<ClusterComponent> components,
            AmbariRepoDetailsJson ambariRepoDetailsJson, Cluster cluster) throws JsonProcessingException {
        // If it is not predefined in image catalog
        if (!stackAmbariRepoConfig.isPresent()) {
            if (ambariRepoDetailsJson == null) {
                ambariRepoDetailsJson = new AmbariRepoDetailsJson();
            }
            AmbariRepo ambariRepo = conversionService.convert(ambariRepoDetailsJson, AmbariRepo.class);
            ClusterComponent component = new ClusterComponent(ComponentType.AMBARI_REPO_DETAILS, new Json(ambariRepo), cluster);
            components.add(component);
        } else {
            ClusterComponent ambariRepo = new ClusterComponent(ComponentType.AMBARI_REPO_DETAILS, stackAmbariRepoConfig.get().getAttributes(), cluster);
            components.add(ambariRepo);
        }
        return components;
    }

    private List<ClusterComponent> addHDPRepoConfig(Optional<Component> stackHdpRepoConfig, List<ClusterComponent> components,
            AmbariStackDetailsJson ambariStackDetailsJson, Cluster cluster, IdentityUser user,
            BlueprintRequest blueprintRequest, Long blueprintId, String blueprintName) throws JsonProcessingException {
        if (!stackHdpRepoConfig.isPresent()) {
            if (ambariStackDetailsJson != null) {
                HDPRepo hdpRepo = conversionService.convert(ambariStackDetailsJson, HDPRepo.class);
                ClusterComponent component = new ClusterComponent(ComponentType.HDP_REPO_DETAILS, new Json(hdpRepo), cluster);
                components.add(component);
            } else {
                ClusterComponent hdpRepoComponent = new ClusterComponent(ComponentType.HDP_REPO_DETAILS,
                        new Json(defaultHDPInfo(blueprintRequest, blueprintId, blueprintName, user).getRepo()), cluster);
                components.add(hdpRepoComponent);
            }
        } else {
            ClusterComponent hdpRepoComponent = new ClusterComponent(ComponentType.HDP_REPO_DETAILS, stackHdpRepoConfig.get().getAttributes(), cluster);
            components.add(hdpRepoComponent);
        }
        return components;
    }

    private HDPInfo defaultHDPInfo(BlueprintRequest blueprintRequest, Long blueprintId, String blueprintName, IdentityUser user) {
        try {
            JsonNode root;
            if (blueprintId != null) {
                Blueprint blueprint = blueprintService.get(blueprintId);
                root = JsonUtil.readTree(blueprint.getBlueprintText());
            } else if (blueprintName != null) {
                Blueprint blueprint = blueprintService.get(blueprintName, user.getAccount());
                root = JsonUtil.readTree(blueprint.getBlueprintText());
            } else {
                root = JsonUtil.readTree(blueprintRequest.getAmbariBlueprint());
            }
            if (root != null) {
                String stackVersion = blueprintUtils.getBlueprintHdpVersion(root);
                String stackName = blueprintUtils.getBlueprintStackName(root);
                if ("HDF".equalsIgnoreCase(stackName)) {
                    LOGGER.info("Stack name is HDF, use the default HDF repo for version: " + stackVersion);
                    for (Entry<String, DefaultHDFInfo> entry : defaultHDFEntries.getEntries().entrySet()) {
                        if (entry.getKey().equals(stackVersion)) {
                            return entry.getValue();
                        }
                    }
                } else {
                    LOGGER.info("Stack name is HDP, use the default HDP repo for version: " + stackVersion);
                    for (Entry<String, DefaultHDPInfo> entry : defaultHDPEntries.getEntries().entrySet()) {
                        if (entry.getKey().equals(stackVersion)) {
                            return entry.getValue();
                        }
                    }
                }
            }
        } catch (IOException ex) {
            LOGGER.warn("Can not initiate default hdp info: ", ex);
        }
        return defaultHDPEntries.getEntries().values().iterator().next();
    }

    private List<ClusterComponent> addAmbariDatabaseConfig(List<ClusterComponent> components, AmbariDatabaseDetailsJson ambariRepoDetailsJson, Cluster cluster)
            throws JsonProcessingException {
        if (ambariRepoDetailsJson == null) {
            ambariRepoDetailsJson = new AmbariDatabaseDetailsJson();
        }
        AmbariDatabase ambariDatabase = conversionService.convert(ambariRepoDetailsJson, AmbariDatabase.class);
        ClusterComponent component = new ClusterComponent(ComponentType.AMBARI_DATABASE_DETAILS, new Json(ambariDatabase), cluster);
        components.add(component);
        return components;
    }

}
