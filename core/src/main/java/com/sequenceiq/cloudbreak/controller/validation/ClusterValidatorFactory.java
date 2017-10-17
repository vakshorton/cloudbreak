package com.sequenceiq.cloudbreak.controller.validation;

import java.io.Serializable;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.sequenceiq.cloudbreak.cluster.ClusterValidator;
import com.sequenceiq.cloudbreak.cluster.ambari.ValidatorException;
import com.sequenceiq.cloudbreak.controller.BadRequestException;
import com.sequenceiq.cloudbreak.controller.validation.stack.StackParameterValidator;

@Service
public class ClusterValidatorFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(StackParameterValidator.class);

    @Resource
    private Map<Class, ClusterValidator> classClusterValidator;

    public <E extends Serializable> void validate(E e) throws ValidatorException {
        ClusterValidator clusterValidator = classClusterValidator.get(e.getClass());
        if (clusterValidator == null) {
            clusterValidator.validate(e);
        } else {
            LOGGER.error("Validator for {} class was not implemented. Requested validation failed", e.getClass());
            throw new BadRequestException(String.format("Validator for %s class was not implemented. Requested validation failed", e.getClass()));
        }
    }
}
