package com.sequenceiq.it.cloudbreak.mock;

import static com.sequenceiq.it.spark.ITResponse.IMAGE_CATALOG;
import static com.sequenceiq.it.spark.ITResponse.MOCK_ROOT;
import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.post;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.sequenceiq.cloudbreak.api.model.AdjustmentType;
import com.sequenceiq.cloudbreak.api.model.FailurePolicyRequest;
import com.sequenceiq.cloudbreak.api.model.InstanceGroupRequest;
import com.sequenceiq.cloudbreak.api.model.InstanceGroupType;
import com.sequenceiq.cloudbreak.api.model.OnFailureAction;
import com.sequenceiq.cloudbreak.api.model.OrchestratorRequest;
import com.sequenceiq.cloudbreak.api.model.StackAuthenticationRequest;
import com.sequenceiq.cloudbreak.api.model.StackRequest;
import com.sequenceiq.cloudbreak.client.RestClientUtil;
import com.sequenceiq.cloudbreak.cloud.model.CloudVmMetaDataStatus;
import com.sequenceiq.it.IntegrationTestContext;
import com.sequenceiq.it.cloudbreak.AbstractMockIntegrationTest;
import com.sequenceiq.it.cloudbreak.CloudbreakITContextConstants;
import com.sequenceiq.it.cloudbreak.CloudbreakUtil;
import com.sequenceiq.it.cloudbreak.InstanceGroup;
import com.sequenceiq.it.cloudbreak.mock.json.CBVersion;
import com.sequenceiq.it.spark.spi.CloudMetaDataStatuses;
import com.sequenceiq.it.util.ResourceUtil;

public class MockStackCreationWithSaltSuccessTest extends AbstractMockIntegrationTest {

    @Value("${mock.server.address:localhost}")
    private String mockServerAddress;

    @Value("${integrationtest.publicKeyFile}")
    private String defaultPublicKeyFile;

    @BeforeMethod
    public void setContextParams() {
        IntegrationTestContext itContext = getItContext();
        Assert.assertNotNull(itContext.getContextParam(CloudbreakITContextConstants.TEMPLATE_ID, List.class), "Template id is mandatory.");
        Assert.assertNotNull(itContext.getContextParam(CloudbreakITContextConstants.CREDENTIAL_ID), "Credential id is mandatory.");
        Assert.assertNotNull(itContext.getContextParam(CloudbreakITContextConstants.NETWORK_ID), "Network id is mandatory.");
        Assert.assertNotNull(itContext.getContextParam(CloudbreakITContextConstants.SECURITY_GROUP_ID), "Security group id is mandatory.");
    }

    @Test
    @Parameters({"stackName", "region", "onFailureAction", "threshold", "adjustmentType", "variant", "availabilityZone", "persistentStorage", "orchestrator",
            "mockPort", "sshPort", "publicKeyFile"})
    public void testStackCreation(@Optional("testing1") String stackName, @Optional("europe-west1") String region,
            @Optional("DO_NOTHING") String onFailureAction, @Optional("4") Long threshold, @Optional("EXACT") String adjustmentType,
            @Optional("") String variant, @Optional String availabilityZone, @Optional String persistentStorage, @Optional("SALT") String orchestrator,
            @Optional("9443") int mockPort, @Optional("2020") int sshPort, @Optional("") String publicKeyFile)
            throws Exception {
        // GIVEN
        IntegrationTestContext itContext = getItContext();
        List<InstanceGroup> instanceGroups = itContext.getContextParam(CloudbreakITContextConstants.TEMPLATE_ID, List.class);
        List<InstanceGroupRequest> igMap = new ArrayList<>();

        int numberOfServers = 0;
        for (InstanceGroup ig : instanceGroups) {
            InstanceGroupRequest instanceGroupRequest = new InstanceGroupRequest();
            instanceGroupRequest.setGroup(ig.getName());
            instanceGroupRequest.setNodeCount(ig.getNodeCount());
            instanceGroupRequest.setTemplateId(Long.valueOf(ig.getTemplateId()));
            instanceGroupRequest.setType(InstanceGroupType.valueOf(ig.getType()));
            igMap.add(instanceGroupRequest);
            numberOfServers += ig.getNodeCount();
        }

        Map<String, CloudVmMetaDataStatus> instanceMap = new HashMap<>();

        MockInstanceUtil mockInstanceUtil = new MockInstanceUtil(mockServerAddress, sshPort);
        mockInstanceUtil.addInstance(instanceMap, numberOfServers);

        String credentialId = itContext.getContextParam(CloudbreakITContextConstants.CREDENTIAL_ID);
        String networkId = itContext.getContextParam(CloudbreakITContextConstants.NETWORK_ID);
        StackRequest stackRequest = new StackRequest();

        publicKeyFile = StringUtils.hasLength(publicKeyFile) ? publicKeyFile : defaultPublicKeyFile;
        String publicKey = ResourceUtil.readStringFromResource(applicationContext, publicKeyFile).replaceAll("\n", "");
        StackAuthenticationRequest stackAuthenticationRequest = new StackAuthenticationRequest();
        stackAuthenticationRequest.setPublicKey(publicKey);
        stackRequest.setStackAuthentication(stackAuthenticationRequest);

        stackRequest.setName(stackName);
        stackRequest.setCredentialId(Long.valueOf(credentialId));
        stackRequest.setRegion(region);
        stackRequest.setOnFailureAction(OnFailureAction.valueOf(onFailureAction));
        FailurePolicyRequest failurePolicyRequest = new FailurePolicyRequest();
        failurePolicyRequest.setAdjustmentType(AdjustmentType.valueOf(adjustmentType));
        failurePolicyRequest.setThreshold(threshold);
        stackRequest.setFailurePolicy(failurePolicyRequest);
        stackRequest.setNetworkId(Long.valueOf(networkId));
        stackRequest.setPlatformVariant(variant);
        stackRequest.setAvailabilityZone(availabilityZone);
        stackRequest.setInstanceGroups(igMap);

        OrchestratorRequest orchestratorRequest = new OrchestratorRequest();
        orchestratorRequest.setType(orchestrator);
        stackRequest.setOrchestrator(orchestratorRequest);

        Map<String, String> map = new HashMap<>();
        if (persistentStorage != null && !persistentStorage.isEmpty()) {
            map.put("persistentStorage", persistentStorage);
        }
        stackRequest.setParameters(map);
        port(mockPort);
        addSPIEndpoints(instanceMap);
        mockImageCatalogResponse(itContext);
        initSpark();

        // WHEN
        String stackId = getCloudbreakClient().stackV1Endpoint().postPrivate(stackRequest).getId().toString();
        // THEN
        Assert.assertNotNull(stackId);
        itContext.putCleanUpParam(CloudbreakITContextConstants.STACK_ID, stackId);
        CloudbreakUtil.waitAndCheckStackStatus(getCloudbreakClient(), stackId, "AVAILABLE");
        itContext.putContextParam(CloudbreakITContextConstants.STACK_ID, stackId);
        itContext.putContextParam(CloudbreakITContextConstants.MOCK_INSTANCE_MAP, instanceMap);
    }

    private void addSPIEndpoints(Map<String, CloudVmMetaDataStatus> instanceMap) {
        post(MOCK_ROOT + "/cloud_metadata_statuses", new CloudMetaDataStatuses(instanceMap), gson()::toJson);
    }

    private void mockImageCatalogResponse(IntegrationTestContext itContext) {
        get(IMAGE_CATALOG, (request, response) -> {
            String cbServerRoot = itContext.getContextParam(CloudbreakITContextConstants.CLOUDBREAK_SERVER_ROOT);
            Client client = RestClientUtil.get();
            WebTarget target = client.target(cbServerRoot + "/info");
            CBVersion cbVersion = target.request().get().readEntity(CBVersion.class);
            return responseFromJsonFile("imagecatalog/catalog.json").replace("CB_VERSION", cbVersion.getApp().getVersion());
        });
    }
}
