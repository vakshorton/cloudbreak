package com.sequenceiq.cloudbreak.service.cluster.flow.blueprint.template;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import com.github.jknack.handlebars.Template;
import com.sequenceiq.cloudbreak.domain.Cluster;
import com.sequenceiq.cloudbreak.domain.RDSConfig;
import com.sequenceiq.cloudbreak.service.ClusterComponentConfigProvider;

@Component
public class BlueprintTemplateProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlueprintTemplateProcessor.class);

    @Inject
    private ClusterComponentConfigProvider clusterComponentConfigProvider;

    public String process(String blueprintText, Cluster cluster, Set<RDSConfig> rdsConfigs) throws IOException {
        long started = System.currentTimeMillis();
        String generateBlueprint = generateBlueprintWithParameters(blueprintText, cluster, rdsConfigs);
        long generationTime = System.currentTimeMillis() - started;
        LOGGER.info("The blueprint was generated successfully under {} ms, the generated blueprint is: {}", generationTime, generateBlueprint);
        return generateBlueprint;
    }

    private String generateBlueprintWithParameters(String blueprintText, Cluster cluster, Set<RDSConfig> rdsConfigs) throws IOException {
        Handlebars handlebars = new Handlebars();
        handlebars.registerHelperMissing(new Helper<Object>() {
            @Override
            public CharSequence apply(final Object context, final Options options) throws IOException {
                return options.fn.text();
            }
        });
        Template template = handlebars.compileInline(blueprintText, "{{{", "}}}");

        return template.apply(prepareTemplateObject(cluster.getBlueprintInputs().get(Map.class), cluster, rdsConfigs));
    }

    private Map<String, Object> prepareTemplateObject(Map<String, Object> blueprintInputs, Cluster cluster, Set<RDSConfig> rdsConfigs) {
        if (blueprintInputs == null) {
            blueprintInputs = new HashMap<>();
        }

        return new BlueprintTemplateModelContextBuilder()
                .withAmbariDatabase(clusterComponentConfigProvider.getAmbariDatabase(cluster.getId()))
                .withClusterName(cluster.getName())
                .withLdap(cluster.getLdapConfig())
                .withRdsConfigs(rdsConfigs)
                .withCustomProperties(blueprintInputs)
                .build();
    }
}
