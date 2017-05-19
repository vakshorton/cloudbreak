package com.sequenceiq.cloudbreak.repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.sequenceiq.cloudbreak.api.model.Status;
import com.sequenceiq.cloudbreak.domain.Blueprint;
import com.sequenceiq.cloudbreak.domain.Cluster;
import com.sequenceiq.cloudbreak.domain.LdapConfig;
import com.sequenceiq.cloudbreak.domain.SssdConfig;

@EntityType(entityClass = Cluster.class)
public interface ClusterRepository extends CrudRepository<Cluster, Long> {

    Cluster findById(@Param("id") Long id);

    Cluster findOneWithLists(@Param("id") Long id);

    List<Cluster> findByStatuses(@Param("statuses") Collection<Status> statuses);

    Cluster findByNameInAccount(@Param("name") String name, @Param("account") String account);

    List<Cluster> findAllClustersForConstraintTemplate(@Param("id") Long id);

    Set<Cluster> findAllClustersByRDSConfig(@Param("id") Long rdsConfigId);

    Long countByBlueprint(Blueprint blueprint);

    Long countByLdapConfig(LdapConfig ldapConfig);

    Long countBySssdConfig(SssdConfig sssdConfig);
}