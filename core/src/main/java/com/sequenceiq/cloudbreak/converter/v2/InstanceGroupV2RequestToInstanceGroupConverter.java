package com.sequenceiq.cloudbreak.converter.v2;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sequenceiq.cloudbreak.api.model.v2.InstanceGroupV2Request;
import com.sequenceiq.cloudbreak.controller.validation.template.TemplateValidator;
import com.sequenceiq.cloudbreak.converter.AbstractConversionServiceAwareConverter;
import com.sequenceiq.cloudbreak.domain.InstanceGroup;
import com.sequenceiq.cloudbreak.domain.SecurityGroup;
import com.sequenceiq.cloudbreak.domain.Template;
import com.sequenceiq.cloudbreak.domain.json.Json;
import com.sequenceiq.cloudbreak.service.securitygroup.SecurityGroupService;
import com.sequenceiq.cloudbreak.service.template.TemplateService;

@Component
public class InstanceGroupV2RequestToInstanceGroupConverter extends AbstractConversionServiceAwareConverter<InstanceGroupV2Request, InstanceGroup> {

    @Inject
    private TemplateService templateService;

    @Inject
    private SecurityGroupService securityGroupService;

    @Inject
    private TemplateValidator templateValidator;

    @Override
    public InstanceGroup convert(InstanceGroupV2Request json) {
        InstanceGroup instanceGroup = new InstanceGroup();
        instanceGroup.setGroupName(json.getGroup());
        instanceGroup.setNodeCount(json.getNodeCount());
        instanceGroup.setInstanceGroupType(json.getType());
        if (json.getTemplate() != null) {
            Template template = getConversionService().convert(json.getTemplate(), Template.class);
            templateValidator.validateTemplateRequest(template);
            instanceGroup.setTemplate(template);
        }
        if (json.getSecurityGroup() != null) {
            instanceGroup.setSecurityGroup(getConversionService().convert(json.getSecurityGroup(), SecurityGroup.class));
        }
        try {
            Json jsonProperties = new Json(json.getParameters());
            instanceGroup.setAttributes(jsonProperties);
        } catch (JsonProcessingException ignored) {
            instanceGroup.setAttributes(null);
        }

        return instanceGroup;
    }
}
