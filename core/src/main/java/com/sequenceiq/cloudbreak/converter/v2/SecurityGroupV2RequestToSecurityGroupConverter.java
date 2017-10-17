package com.sequenceiq.cloudbreak.converter.v2;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.api.model.SecurityRuleRequest;
import com.sequenceiq.cloudbreak.api.model.v2.SecurityGroupV2Request;
import com.sequenceiq.cloudbreak.common.type.APIResourceType;
import com.sequenceiq.cloudbreak.common.type.ResourceStatus;
import com.sequenceiq.cloudbreak.converter.AbstractConversionServiceAwareConverter;
import com.sequenceiq.cloudbreak.domain.SecurityGroup;
import com.sequenceiq.cloudbreak.domain.SecurityRule;
import com.sequenceiq.cloudbreak.service.MissingResourceNameGenerator;

@Component
public class SecurityGroupV2RequestToSecurityGroupConverter extends AbstractConversionServiceAwareConverter<SecurityGroupV2Request, SecurityGroup> {

    @Inject
    private MissingResourceNameGenerator missingResourceNameGenerator;

    @Override
    public SecurityGroup convert(SecurityGroupV2Request source) {
        SecurityGroup entity = new SecurityGroup();
        entity.setName(missingResourceNameGenerator.generateName(APIResourceType.SECURITY_GROUP));
        entity.setStatus(ResourceStatus.USER_MANAGED);
        entity.setSecurityGroupId(source.getSecurityGroupId());
        entity.setSecurityRules(convertSecurityRules(source.getSecurityRules(), entity));
        return entity;
    }

    private Set<SecurityRule> convertSecurityRules(List<SecurityRuleRequest> securityRuleRequests, SecurityGroup securityGroup) {
        Set<SecurityRule> convertedSet = (Set<SecurityRule>) getConversionService().convert(securityRuleRequests, TypeDescriptor.forObject(securityRuleRequests),
                TypeDescriptor.collection(Set.class, TypeDescriptor.valueOf(SecurityRule.class)));
        for (SecurityRule securityRule : convertedSet) {
            securityRule.setSecurityGroup(securityGroup);
        }
        return convertedSet;
    }
}
