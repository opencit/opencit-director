-- tables
-- Table: mw_host
CREATE TABLE mw_host (
    id varchar(36)  NOT NULL,
    name varchar(100)  NOT NULL,
    ip_address varchar(15)  NOT NULL,
    username varchar(50)  NOT NULL,
    ssh_key_id varchar(36)  NULL,
    ssh_password_id varchar(50)  NULL,
    CONSTRAINT mw_host_pk PRIMARY KEY (id)
);



-- Table: mw_image
CREATE TABLE mw_image (
    id varchar(36)  NOT NULL,
    image_deployments varchar(20)  NOT NULL,
    image_format varchar(255)  NOT NULL,
    location varchar(255)  NOT NULL,
    mounted_by_user_id varchar(36)  NULL,
    deleted boolean  NOT NULL,
    trust_policy_id varchar(36)  NOT NULL,
    date_created date  NOT NULL,
    content_length int  NOT NULL,
    CONSTRAINT mw_image_pk PRIMARY KEY (id)
);



-- Table: mw_image_upload
CREATE TABLE mw_image_upload (
    id varchar(36)  NOT NULL,
    image_id varchar(36)  NULL,
    image_uri text  NOT NULL,
    date date  NOT NULL,
    tmp_location varchar(255)  NOT NULL,
    checksum varchar(64)  NOT NULL,
    status varchar(20)  NOT NULL,
    content_length int  NOT NULL,
    content_sent int  NOT NULL,
    CONSTRAINT mw_image_upload_pk PRIMARY KEY (id)
);



-- Table: mw_policy_upload
CREATE TABLE mw_policy_upload (
    id varchar(36)  NOT NULL,
    date date  NOT NULL,
    policy_uri text  NOT NULL,
    trust_policy_id varchar(36)  NULL,
    status varchar(20)  NOT NULL,
    CONSTRAINT mw_policy_upload_pk PRIMARY KEY (id)
);



-- Table: mw_ssh_key
CREATE TABLE mw_ssh_key (
    id varchar(36)  NOT NULL,
    ssh_key text  NOT NULL,
    CONSTRAINT mw_ssh_key_pk PRIMARY KEY (id)
);



-- Table: mw_ssh_password
CREATE TABLE mw_ssh_password (
    id varchar(36)  NOT NULL,
    ssh_password varchar(50)  NOT NULL,
    CONSTRAINT mw_ssh_password_pk PRIMARY KEY (id)
);



-- Table: mw_trust_policy
CREATE TABLE mw_trust_policy (
    id varchar(36)  NOT NULL,
    description varchar(255)  NOT NULL,
    trust_policy xml  NOT NULL,
    host_id varchar(36)  NULL,
    CONSTRAINT mw_trust_policy_pk PRIMARY KEY (id)
);



-- Table: mw_trust_policy_draft
CREATE TABLE mw_trust_policy_draft (
    id varchar(36)  NOT NULL,
    image_id varchar(36)  NOT NULL,
    trust_policy text  NOT NULL,
    user_id varchar(36)  NOT NULL,
    CONSTRAINT mw_trust_policy_draft_pk PRIMARY KEY (id)
);



-- Table: mw_user
CREATE TABLE mw_user (
    id varchar(36)  NOT NULL,
    name varchar(255)  NOT NULL,
    password varchar(255)  NOT NULL,
    CONSTRAINT mw_user_pk PRIMARY KEY (id)
);







-- foreign keys
-- Reference:  image_policy (table: mw_image)


ALTER TABLE mw_image ADD CONSTRAINT image_policy 
    FOREIGN KEY (trust_policy_id)
    REFERENCES mw_trust_policy (id)
    NOT DEFERRABLE 
    INITIALLY IMMEDIATE 
;

-- Reference:  mw_host_mw_ssh_key (table: mw_host)


ALTER TABLE mw_host ADD CONSTRAINT mw_host_mw_ssh_key 
    FOREIGN KEY (ssh_key_id)
    REFERENCES mw_ssh_key (id)
    NOT DEFERRABLE 
    INITIALLY IMMEDIATE 
;

-- Reference:  mw_host_mw_ssh_password (table: mw_host)


ALTER TABLE mw_host ADD CONSTRAINT mw_host_mw_ssh_password 
    FOREIGN KEY (ssh_password_id)
    REFERENCES mw_ssh_password (id)
    NOT DEFERRABLE 
    INITIALLY IMMEDIATE 
;

-- Reference:  mw_image_mw_trust_policy_draft (table: mw_trust_policy_draft)


ALTER TABLE mw_trust_policy_draft ADD CONSTRAINT mw_image_mw_trust_policy_draft 
    FOREIGN KEY (image_id)
    REFERENCES mw_image (id)
    NOT DEFERRABLE 
    INITIALLY IMMEDIATE 
;

-- Reference:  mw_image_mw_user (table: mw_image)


ALTER TABLE mw_image ADD CONSTRAINT mw_image_mw_user 
    FOREIGN KEY (mounted_by_user_id)
    REFERENCES mw_user (id)
    NOT DEFERRABLE 
    INITIALLY IMMEDIATE 
;

-- Reference:  mw_image_store_mapping_mw_image (table: mw_image_upload)


ALTER TABLE mw_image_upload ADD CONSTRAINT mw_image_store_mapping_mw_image 
    FOREIGN KEY (image_id)
    REFERENCES mw_image (id)
    NOT DEFERRABLE 
    INITIALLY IMMEDIATE 
;

-- Reference:  mw_policy_upload_mw_trust_policy (table: mw_policy_upload)


ALTER TABLE mw_policy_upload ADD CONSTRAINT mw_policy_upload_mw_trust_policy 
    FOREIGN KEY (trust_policy_id)
    REFERENCES mw_trust_policy (id)
    NOT DEFERRABLE 
    INITIALLY IMMEDIATE 
;

-- Reference:  mw_trust_policy_mw_host (table: mw_trust_policy)


ALTER TABLE mw_trust_policy ADD CONSTRAINT mw_trust_policy_mw_host 
    FOREIGN KEY (host_id)
    REFERENCES mw_host (id)
    NOT DEFERRABLE 
    INITIALLY IMMEDIATE 
;

-- Reference:  mw_user_mw_trust_policy_draft (table: mw_trust_policy_draft)


ALTER TABLE mw_trust_policy_draft ADD CONSTRAINT mw_user_mw_trust_policy_draft 
    FOREIGN KEY (user_id)
    REFERENCES mw_user (id)
    NOT DEFERRABLE 
    INITIALLY IMMEDIATE 
;






-- End of file.
