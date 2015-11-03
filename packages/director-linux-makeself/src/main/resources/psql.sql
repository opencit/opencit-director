CREATE TABLE MW_USER (ID VARCHAR(36) NOT NULL, DISPLAY_NAME VARCHAR(255), EMAIL VARCHAR(255), USER_NAME VARCHAR(255), PRIMARY KEY (ID));
CREATE TABLE MW_IMAGE (ID VARCHAR(36) NOT NULL, CONTENT_LENGTH INTEGER, CREATED_BY_USER_ID VARCHAR(36), CREATED_DATE DATE, DELETED BOOLEAN, EDITED_BY_USER_ID VARCHAR(36), EDITED_DATE DATE, IMAGE_DEPLOYMENTS VARCHAR(20), IMAGE_FORMAT VARCHAR(255), LOCATION VARCHAR(255), MOUNTED_BY_USER_ID VARCHAR(36), NAME VARCHAR(255), SENT INTEGER, STATUS VARCHAR(255), TRUST_POLICY_ID VARCHAR(36), TRUST_POLICY_DRAFT_ID VARCHAR(36), PRIMARY KEY (ID));
CREATE TABLE MW_IMAGE_ACTION (id VARCHAR(36) NOT NULL, action VARCHAR(500), action_completed INTEGER, action_count INTEGER, action_size INTEGER, action_size_max INTEGER, current_task_name VARCHAR(255), current_task_status VARCHAR(255), image_id VARCHAR(255), PRIMARY KEY (id));
CREATE TABLE MW_TRUST_POLICY (ID VARCHAR(36) NOT NULL, CREATED_BY_USER_ID VARCHAR(36), CREATED_DATE DATE, DESCRIPTION VARCHAR(255), DISPLAY_NAME VARCHAR(255), EDITED_BY_USER_ID VARCHAR(36), EDITED_DATE DATE, NAME VARCHAR(255), TRUST_POLICY TEXT, HOST_ID VARCHAR(36), PRIMARY KEY (ID));
CREATE TABLE MW_IMAGE_UPLOAD (ID VARCHAR(36) NOT NULL, CHECKSUM VARCHAR(255), CONTENT_LENGTH INTEGER, DATE DATE, IMAGE_URI TEXT, IS_TARBALL_UPLOAD BOOLEAN, SENT INTEGER, STATUS VARCHAR(20), TMP_LOCATION VARCHAR(255), IMAGE_ID VARCHAR(36), PRIMARY KEY (ID));
CREATE TABLE MW_POLICY_UPLOAD (ID VARCHAR(36) NOT NULL, DATE DATE, POLICY_URI TEXT, STATUS VARCHAR(20), POLICY_ID VARCHAR(36), PRIMARY KEY (ID));
CREATE TABLE MW_TRUST_POLICY_DRAFT (ID VARCHAR(36) NOT NULL, CREATED_BY_USER_ID VARCHAR(36), CREATED_DATE DATE, DISPLAY_NAME VARCHAR(255), EDITED_BY_USER_ID VARCHAR(36), EDITED_DATE DATE, NAME VARCHAR(255), TRUST_POLICY_DRAFT TEXT, PRIMARY KEY (ID));
CREATE TABLE mw_host
(
  ID character varying(36) NOT NULL,
  created_by_user_id character varying(36),
  created_date date,
  edited_by_user_id character varying(36),
  edited_date date,
  ip_address character varying(15),
  name character varying(100),
  username character varying(50),
  ssh_key_id character varying(36),
  image_id character varying(36),
  ssh_password_id character varying(36), PRIMARY KEY (ID)
);
CREATE TABLE MW_SSH_KEY (ID VARCHAR(36) NOT NULL, SSH_KEY TEXT, PRIMARY KEY (ID));
CREATE TABLE MW_SSH_PASSWORD (ID VARCHAR(36) NOT NULL, SSH_KEY TEXT, PRIMARY KEY (ID));
CREATE TABLE MW_IMAGE_STORE_SETTINGS (ID VARCHAR(36) NOT NULL, NAME VARCHAR(255) UNIQUE, PROVIDER_CLASS VARCHAR(255), PRIMARY KEY (ID));
ALTER TABLE MW_IMAGE ADD CONSTRAINT FK_MW_IMAGE_TRUST_POLICY_DRAFT_ID FOREIGN KEY (TRUST_POLICY_DRAFT_ID) REFERENCES MW_TRUST_POLICY_DRAFT (ID);
ALTER TABLE MW_IMAGE ADD CONSTRAINT FK_MW_IMAGE_TRUST_POLICY_ID FOREIGN KEY (TRUST_POLICY_ID) REFERENCES MW_TRUST_POLICY (ID);
ALTER TABLE MW_TRUST_POLICY ADD CONSTRAINT FK_MW_TRUST_POLICY_HOST_ID FOREIGN KEY (HOST_ID) REFERENCES MW_HOST (ID);
ALTER TABLE MW_IMAGE_UPLOAD ADD CONSTRAINT FK_MW_IMAGE_UPLOAD_IMAGE_ID FOREIGN KEY (IMAGE_ID) REFERENCES MW_IMAGE (ID);
ALTER TABLE MW_POLICY_UPLOAD ADD CONSTRAINT FK_MW_POLICY_UPLOAD_POLICY_ID FOREIGN KEY (POLICY_ID) REFERENCES MW_TRUST_POLICY (ID);
ALTER TABLE MW_HOST ADD CONSTRAINT FK_MW_HOST_SSH_KEY_ID FOREIGN KEY (SSH_KEY_ID) REFERENCES MW_SSH_KEY (ID);
ALTER TABLE MW_HOST ADD CONSTRAINT FK_MW_HOST_SSH_PASSWORD_ID FOREIGN KEY (SSH_PASSWORD_ID) REFERENCES MW_SSH_PASSWORD (ID);
ALTER TABLE MW_HOST ADD CONSTRAINT FK_MW_HOST_IMAGE_ID FOREIGN KEY (image_id)     REFERENCES mw_image (id);
CREATE TABLE mw_policy_template(id character varying(36) NOT NULL, name character varying(255), deployment_type character varying(255), content character varying(2000), active boolean, deployment_type_identifier character varying(255), policy_type character varying(255), CONSTRAINT mw_policy_template_pkey PRIMARY KEY (id));

-- View: mw_image_info_view

-- DROP VIEW mw_image_info_view;

CREATE OR REPLACE VIEW mw_image_info_view AS 
 SELECT mwimg.id,
    mwimg.name,
    mwimg.content_length,
    mwimg.image_format,
    mwimg.image_deployments,
    mwimg.created_by_user_id,
    mwimg.created_date,
    mwimg.edited_date,
    mwimg.edited_by_user_id,
    mwimg.deleted,
    mwimg.location,
    mwimg.mounted_by_user_id,
    mwimg.sent,
    mwimg.status,
    mwtrustpolicy.id AS trust_policy_id,
    mwtrustpolicy.name AS trust_policy_name,
    mwpolicydraft.id AS trust_policy_draft_id,
    mwtrustpolicy.edited_by_user_id AS trust_policy_edited_by_user_id,
    mwtrustpolicy.edited_date AS trust_policy_edited_date,
    mwtrustpolicy.created_date AS trust_policy_created_date,
    mwpolicydraft.name AS trust_policy_draft_name,
    mwpolicydraft.edited_by_user_id AS trust_policy_draft_edited_by_user_id,
    mwpolicydraft.edited_date AS trust_policy_draft_edited_date,
    ( SELECT count(*) AS count
           FROM mw_image_upload mwimageupload
          WHERE mwimageupload.image_id::text = mwimg.id::text) AS image_upload_count
   FROM mw_image mwimg
     LEFT JOIN mw_trust_policy mwtrustpolicy ON mwimg.trust_policy_id::text = mwtrustpolicy.id::text
     LEFT JOIN mw_trust_policy_draft mwpolicydraft ON mwpolicydraft.id::text = mwimg.trust_policy_draft_id::text;

ALTER TABLE mw_image_info_view
  OWNER TO postgres;

INSERT INTO mw_policy_template(id, name, deployment_type, content, active, deployment_type_identifier,policy_type)
    VALUES ('1', 'Bare Metal (NV)', 'BareMetal', '<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Manifest xmlns="mtwilson:trustdirector:manifest:1.1" DigestAlg="sha1"><Dir Path="/home"/><File Path="/home/director-0.1-SNAPSHOT.bin"/><File Path="/home/intelmh/.cache/motd.legal-displayed"/><File Path="/home/intelmh/.bash_logout"/></Manifest>',
     true, 'NV', 'Manifest');

INSERT INTO mw_policy_template(id, name, deployment_type, content, active, deployment_type_identifier,policy_type)
    VALUES ('2', 'Bare Metal (V)', 'BareMetal', '<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Manifest xmlns="mtwilson:trustdirector:manifest:1.1" DigestAlg="sha1"><Dir Path="/home"/><File Path="/home/director-0.1-SNAPSHOT.bin"/><File Path="/home/intelmh/.cache/motd.legal-displayed"/><File Path="/home/intelmh/.bash_logout"/><File Path="/home/intelmh/.bashrc"/></Manifest>',
     true, 'V', 'Manifest');
