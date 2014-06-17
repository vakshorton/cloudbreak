package com.sequenceiq.cloudbreak.domain;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class AwsCredential extends Credential implements ProvisionEntity {

    private String roleArn;
    private String instanceProfileRoleArn;
    private String notificationArn;

    @ManyToOne
    private User awsCredentialOwner;

    public AwsCredential() {

    }

    public User getAwsCredentialOwner() {
        return awsCredentialOwner;
    }

    public void setAwsCredentialOwner(User awsCredentialOwner) {
        this.awsCredentialOwner = awsCredentialOwner;
    }

    public String getRoleArn() {
        return roleArn;
    }

    public void setRoleArn(String roleArn) {
        this.roleArn = roleArn;
    }

    public String getInstanceProfileRoleArn() {
        return instanceProfileRoleArn;
    }

    public void setInstanceProfileRoleArn(String instanceProfileRoleArn) {
        this.instanceProfileRoleArn = instanceProfileRoleArn;
    }

    public String getNotificationArn() {
        return notificationArn;
    }

    public void setNotificationArn(String notificationArn) {
        this.notificationArn = notificationArn;
    }

    @Override
    public CloudPlatform cloudPlatform() {
        return CloudPlatform.AWS;
    }

    @Override
    public User getOwner() {
        return awsCredentialOwner;
    }

}
