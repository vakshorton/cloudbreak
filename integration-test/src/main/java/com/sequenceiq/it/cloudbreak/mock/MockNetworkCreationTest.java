package com.sequenceiq.it.cloudbreak.mock;

import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.sequenceiq.cloudbreak.api.model.NetworkRequest;
import com.sequenceiq.it.cloudbreak.AbstractCloudbreakIntegrationTest;
import com.sequenceiq.it.cloudbreak.CloudbreakITContextConstants;

public class MockNetworkCreationTest extends AbstractCloudbreakIntegrationTest {
    @Test
    @Parameters({ "networkName", "subnetCIDR" })
    public void testGcpTemplateCreation(@Optional("it-mock-network") String networkName, @Optional("10.0.36.0/24") String subnetCIDR)
            throws Exception {
        // GIVEN
        // WHEN
        NetworkRequest networkRequest = new NetworkRequest();
        networkRequest.setDescription("Mock network for integration testing");
        networkRequest.setSubnetCIDR(subnetCIDR);
        getItContext().putContextParam(CloudbreakITContextConstants.NETWORK, networkRequest, true);
    }
}
