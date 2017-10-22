package com.sequenceiq.cloudbreak.cluster.ambari;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.sequenceiq.cloudbreak.cloud.model.AmbariDatabase;
import com.sequenceiq.cloudbreak.cloud.model.AmbariRepo;
import com.sequenceiq.cloudbreak.cloud.model.HDPRepo;
import com.sequenceiq.cloudbreak.common.type.ComponentType;
import com.sequenceiq.cloudbreak.domain.ClusterComponent;
import com.sequenceiq.cloudbreak.task.CloudbreakServiceException;

@Service
public class AmbariComponentConfigProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(AmbariComponentConfigProvider.class);

    public ClusterComponent getComponent(Set<ClusterComponent> components, ComponentType componentType) {
        return getComponentByType(components, componentType);
    }

    public HDPRepo getHDPRepo(Set<ClusterComponent> components) {
        try {
            ClusterComponent component = getComponent(components, ComponentType.HDP_REPO_DETAILS);
            if (component == null) {
                return null;
            }
            return component.getAttributes().get(HDPRepo.class);
        } catch (IOException e) {
            throw new CloudbreakServiceException("Failed to read HDP repo details.", e);
        }
    }

    public AmbariRepo getAmbariRepo(Set<ClusterComponent> components) {
        try {
            ClusterComponent component = getComponent(components, ComponentType.AMBARI_REPO_DETAILS);
            if (component == null) {
                return null;
            }
            return component.getAttributes().get(AmbariRepo.class);
        } catch (IOException e) {
            throw new CloudbreakServiceException("Failed to read Ambari repo", e);
        }
    }

    public ClusterComponent getComponentByType(Set<ClusterComponent> components, ComponentType componentType) {
            Optional<ClusterComponent> comp = components.stream().filter(
                    c -> c.getComponentType() == componentType).findFirst();
            return comp.orElse(null);
    }

    public AmbariDatabase getAmbariDatabase(Set<ClusterComponent> components) {
        try {
            ClusterComponent component = getComponent(components, ComponentType.AMBARI_DATABASE_DETAILS);
            if (component == null) {
                return null;
            }
            return component.getAttributes().get(AmbariDatabase.class);
        } catch (IOException e) {
            throw new CloudbreakServiceException("Failed to read Ambari database", e);
        }
    }
}
