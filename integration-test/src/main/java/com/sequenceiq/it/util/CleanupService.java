package com.sequenceiq.it.util;

import java.util.Set;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.api.model.BlueprintResponse;
import com.sequenceiq.cloudbreak.api.model.CredentialResponse;
import com.sequenceiq.cloudbreak.api.model.RDSConfigResponse;
import com.sequenceiq.cloudbreak.api.model.RecipeResponse;
import com.sequenceiq.cloudbreak.api.model.StackResponse;
import com.sequenceiq.cloudbreak.client.CloudbreakClient;
import com.sequenceiq.it.cloudbreak.CloudbreakUtil;
import com.sequenceiq.it.cloudbreak.WaitResult;
import com.sequenceiq.it.cloudbreak.config.ITProps;

@Component
public class CleanupService {
    private static final Logger LOG = LoggerFactory.getLogger(CleanupService.class);

    private static final int DELETE_SLEEP = 30000;

    private boolean cleanedUp;

    @Value("${integrationtest.cleanup.retryCount}")
    private int cleanUpRetryCount;

    @Inject
    private ITProps itProps;

    public synchronized void deleteTestStacksAndResources(CloudbreakClient cloudbreakClient) {
        if (cleanedUp) {
            return;
        }
        cleanedUp = true;
        Set<StackResponse> stacks = cloudbreakClient.stackEndpoint().getPrivates();
        for (StackResponse stack : stacks) {
            if (stack.getName().startsWith("it-")) {
                deleteStackAndWait(cloudbreakClient, String.valueOf(stack.getId()));
            }
        }
        Set<BlueprintResponse> blueprints = cloudbreakClient.blueprintEndpoint().getPrivates();
        for (BlueprintResponse blueprint : blueprints) {
            if (blueprint.getName().startsWith("it-")) {
                deleteBlueprint(cloudbreakClient, String.valueOf(blueprint.getId()));
            }
        }
        Set<RecipeResponse> recipes = cloudbreakClient.recipeEndpoint().getPrivates();
        for (RecipeResponse recipe : recipes) {
            if (recipe.getName().startsWith("it-")) {
                deleteRecipe(cloudbreakClient, recipe.getId());
            }
        }
        Set<CredentialResponse> credentials = cloudbreakClient.credentialEndpoint().getPrivates();
        for (CredentialResponse credential : credentials) {
            if (("AZURE".equals(credential.getCloudPlatform()) && credential.getName().startsWith("its"))
                    || (!"AZURE".equals(credential.getCloudPlatform()) && credential.getName().startsWith("its-"))) {
                deleteCredential(cloudbreakClient, String.valueOf(credential.getId()));
            }
        }

        Set<RDSConfigResponse> rdsconfigs = cloudbreakClient.rdsConfigEndpoint().getPrivates();
        for (RDSConfigResponse rds : rdsconfigs) {
            if (rds.getName().startsWith("it-")) {
                deleteRdsConfigs(cloudbreakClient, rds.getId().toString());
            }
        }
    }

    public boolean deleteCredential(CloudbreakClient cloudbreakClient, String credentialId) {
        boolean result = false;
        if (credentialId != null) {
            cloudbreakClient.credentialEndpoint().delete(Long.valueOf(credentialId));
            result = true;
        }
        return result;
    }

    public boolean deleteBlueprint(CloudbreakClient cloudbreakClient, String blueprintId) {
        boolean result = false;
        if (blueprintId != null) {
            cloudbreakClient.blueprintEndpoint().delete(Long.valueOf(blueprintId));
            result = true;
        }
        return result;
    }

    public boolean deleteStackAndWait(CloudbreakClient cloudbreakClient, String stackId) {
        boolean deleted = false;
        for (int i = 0; i < cleanUpRetryCount; i++) {
            if (deleteStack(cloudbreakClient, stackId)) {
                WaitResult waitResult = CloudbreakUtil.waitForStackStatus(cloudbreakClient, stackId, "DELETE_COMPLETED");
                if (waitResult == WaitResult.SUCCESSFUL) {
                    deleted = true;
                    break;
                }
                try {
                    Thread.sleep(DELETE_SLEEP);
                } catch (InterruptedException e) {
                    LOG.warn("interrupted ex", e);
                }
            }
        }
        return deleted;
    }

    public boolean deleteStack(CloudbreakClient cloudbreakClient, String stackId) {
        boolean result = false;
        if (stackId != null) {
            cloudbreakClient.stackEndpoint().delete(Long.valueOf(stackId), false, false);
            result = true;
        }
        return result;
    }

    public boolean deleteRecipe(CloudbreakClient cloudbreakClient, Long recipeId) {
        cloudbreakClient.recipeEndpoint().delete(recipeId);
        return true;
    }

    public boolean deleteRdsConfigs(CloudbreakClient cloudbreakClient, String rdsConfigId) {
        if (rdsConfigId != null) {
            cloudbreakClient.rdsConfigEndpoint().delete(Long.valueOf(rdsConfigId));
        }
        return true;
    }
}
