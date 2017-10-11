package com.sequenceiq.cloudbreak.converter.v2;

import java.util.Map;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sequenceiq.cloudbreak.api.model.v2.TemplateV2Request;
import com.sequenceiq.cloudbreak.common.type.APIResourceType;
import com.sequenceiq.cloudbreak.common.type.ResourceStatus;
import com.sequenceiq.cloudbreak.controller.BadRequestException;
import com.sequenceiq.cloudbreak.converter.AbstractConversionServiceAwareConverter;
import com.sequenceiq.cloudbreak.domain.Template;
import com.sequenceiq.cloudbreak.domain.json.Json;
import com.sequenceiq.cloudbreak.service.MissingResourceNameGenerator;
import com.sequenceiq.cloudbreak.service.topology.TopologyService;

@Component
public class TemplateV2RequestToTemplateConverter extends AbstractConversionServiceAwareConverter<TemplateV2Request, Template> {
    @Inject
    private TopologyService topologyService;

    @Inject
    private MissingResourceNameGenerator missingResourceNameGenerator;

    @Override
    public Template convert(TemplateV2Request source) {
        Template template = new Template();
        template.setName(missingResourceNameGenerator.generateName(APIResourceType.TEMPLATE));
        template.setStatus(ResourceStatus.USER_MANAGED);
        template.setVolumeCount(source.getVolumeCount());
        template.setVolumeSize(source.getVolumeSize());
        template.setInstanceType(source.getInstanceType());
        String volumeType = source.getVolumeType();
        template.setVolumeType(volumeType == null ? "HDD" : volumeType);
        Map<String, Object> parameters = source.getParameters();
        if (parameters != null && !parameters.isEmpty()) {
            try {
                template.setAttributes(new Json(parameters));
            } catch (JsonProcessingException ignored) {
                throw new BadRequestException("Invalid parameters");
            }
        }
        return template;
    }
}
