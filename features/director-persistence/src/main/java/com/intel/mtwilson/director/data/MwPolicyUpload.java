package com.intel.mtwilson.director.data;

import java.sql.Date;
import java.util.Arrays;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.persistence.ManyToOne;

import org.eclipse.persistence.annotations.UuidGenerator;

@Entity
@Table(name = "MW_POLICY_UPLOAD")
public class MwPolicyUpload {

    @Id
    @UuidGenerator(name = "UUID")
    @GeneratedValue(generator = "UUID")
    @Column(name = "ID", length = 36)
    private String id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "POLICY_ID", referencedColumnName = "ID")
    private MwTrustPolicy trustPolicy;

    @Column(name = "DATE")
    private Date date;

    @Column(name = "POLICY_URI")
    private Character[] policyUri;

    @Column(name = "STATUS", length = 20)
    private String status;

    public MwPolicyUpload() {
        super();
    }

    public MwPolicyUpload(MwTrustPolicy trustPolicy, Date date,
            Character[] policyUri, String status) {
        super();
        this.trustPolicy = trustPolicy;
        this.date = date;
        this.policyUri = policyUri;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public MwTrustPolicy getTrustPolicy() {
        return trustPolicy;
    }

    public void setTrustPolicy(MwTrustPolicy trustPolicy) {
        this.trustPolicy = trustPolicy;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Character[] getPolicyUri() {
        return policyUri;
    }

    public void setPolicyUri(Character[] policyUri) {
        this.policyUri = policyUri;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "MwPolicyUpload [id=" + id + ", trustPolicy=" + trustPolicy
                + ", date=" + date + ", policyUri="
                + Arrays.toString(policyUri) + ", status=" + status + "]";
    }

}
