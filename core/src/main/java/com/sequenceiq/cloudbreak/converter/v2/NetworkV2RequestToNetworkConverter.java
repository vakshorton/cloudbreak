package com.sequenceiq.cloudbreak.converter.v2;

import java.util.Map;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sequenceiq.cloudbreak.api.model.v2.NetworkV2Request;
import com.sequenceiq.cloudbreak.common.type.APIResourceType;
import com.sequenceiq.cloudbreak.common.type.ResourceStatus;
import com.sequenceiq.cloudbreak.controller.BadRequestException;
import com.sequenceiq.cloudbreak.converter.AbstractConversionServiceAwareConverter;
import com.sequenceiq.cloudbreak.domain.Network;
import com.sequenceiq.cloudbreak.domain.json.Json;
import com.sequenceiq.cloudbreak.service.MissingResourceNameGenerator;
import com.sequenceiq.cloudbreak.service.topology.TopologyService;

@Component
public class NetworkV2RequestToNetworkConverter extends AbstractConversionServiceAwareConverter<NetworkV2Request, Network> {
    @Inject
    private TopologyService topologyService;

    @Inject
    private MissingResourceNameGenerator missingResourceNameGenerator;

    @Override
    public Network convert(NetworkV2Request source) {
        Network network = new Network();
        network.setName(missingResourceNameGenerator.generateName(APIResourceType.NETWORK));
        network.setSubnetCIDR(source.getSubnetCIDR());
        network.setStatus(ResourceStatus.USER_MANAGED);
        Map<String, Object> parameters = source.getParameters();
        if (parameters != null && !parameters.isEmpty()) {
            try {
                network.setAttributes(new Json(parameters));
            } catch (JsonProcessingException ignored) {
                throw new BadRequestException("Invalid parameters");
            }
        }
        return network;
    }
}
