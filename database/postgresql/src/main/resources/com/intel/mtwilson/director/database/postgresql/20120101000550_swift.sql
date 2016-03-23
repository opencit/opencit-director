
ALTER TABLE MW_TRUST_POLICY ADD IMAGE_ID VARCHAR(36),ADD ARCHIVE BOOLEAN;

ALTER TABLE MW_IMAGE
ADD repository VARCHAR(255) ,ADD  tag VARCHAR(255)
,ADD  TMP_LOCATION VARCHAR(255) ,ADD  UPLOAD_VARIABLES_MD5 VARCHAR(32);


UPDATE  MW_TRUST_POLICY mw_trust_policy SET IMAGE_ID = ( SELECT mw_image.ID FROM MW_IMAGE  mw_image WHERE mw_image.TRUST_POLICY_ID = mw_trust_policy.ID)
    WHERE EXISTS (SELECT null FROM MW_IMAGE  mw_image WHERE mw_image.TRUST_POLICY_ID = mw_trust_policy.ID);

DROP VIEW MW_IMAGE_INFO_VIEW;  /* Dropping it so that we can delete columns referenced by it */
	
ALTER TABLE MW_IMAGE DROP CONSTRAINT FK_MW_IMAGE_TRUST_POLICY_ID;	
	  
ALTER TABLE MW_IMAGE DROP COLUMN TRUST_POLICY_ID;
	 
ALTER TABLE MW_TRUST_POLICY ADD CONSTRAINT FK_MW_TRUST_POLICY_IMAGE_ID FOREIGN KEY (IMAGE_ID) REFERENCES MW_IMAGE (ID);

UPDATE MW_TRUST_POLICY SET ARCHIVE = false;

ALTER TABLE MW_IMAGE DROP COLUMN IS_TARBALL_UPLOAD;		 

ALTER TABLE MW_IMAGE_ACTION ALTER action TYPE VARCHAR(1000);	

ALTER TABLE MW_IMAGE_ACTION ADD EXECUTION_TIME TIMESTAMP,ADD CREATED_TIME TIMESTAMP;

ALTER TABLE MW_IMAGE_UPLOAD

ADD IS_DELETED BOOLEAN,ADD POLICY_UPLOAD_ID VARCHAR(36),ADD STORE_ARTIFACT_ID VARCHAR(36),ADD STORE_ARTIFACT_NAME VARCHAR(36),ADD ACTION_ID VARCHAR(36),ADD UPLOAD_VARIABLES_MD5 VARCHAR(32),ADD STORE_ID VARCHAR(36);

UPDATE MW_IMAGE_UPLOAD SET IS_DELETED = false;

UPDATE MW_IMAGE_ACTION SET current_task_status = 'Obsolete';

DROP TABLE MW_POLICY_UPLOAD;

CREATE TABLE MW_POLICY_UPLOAD (ID VARCHAR(36) NOT NULL, DATE DATE, IS_DELETED BOOLEAN, POLICY_URI TEXT, STATUS VARCHAR(20), STORE_ARTIFACT_ID VARCHAR(36), UPLOAD_VARIABLES_MD5 VARCHAR(32), STORE_ID VARCHAR(36), POLICY_ID VARCHAR(36), PRIMARY KEY (ID));

CREATE TABLE MW_IMAGE_STORE (id VARCHAR(36) NOT NULL, artifact_type VARCHAR(255) NOT NULL, connector VARCHAR(255) NOT NULL, deleted BOOLEAN, name VARCHAR(255) NOT NULL, PRIMARY KEY (id));

CREATE TABLE MW_IMAGE_STORE_DETAILS (id VARCHAR(36) NOT NULL, key VARCHAR(255) NOT NULL, value VARCHAR(255) , IMAGE_STORE_ID VARCHAR(36), PRIMARY KEY (id));



ALTER TABLE MW_IMAGE_UPLOAD ADD CONSTRAINT FK_MW_IMAGE_UPLOAD_STORE_ID FOREIGN KEY (STORE_ID) REFERENCES MW_IMAGE_STORE (id);

ALTER TABLE MW_POLICY_UPLOAD ADD CONSTRAINT FK_MW_POLICY_UPLOAD_STORE_ID FOREIGN KEY (STORE_ID) REFERENCES MW_IMAGE_STORE (id);

ALTER TABLE MW_POLICY_UPLOAD ADD CONSTRAINT FK_MW_POLICY_UPLOAD_POLICY_ID FOREIGN KEY (POLICY_ID) REFERENCES MW_TRUST_POLICY (ID);

ALTER TABLE MW_IMAGE_STORE_DETAILS ADD CONSTRAINT FK_MW_IMAGE_STORE_DETAILS_IMAGE_STORE_ID FOREIGN KEY (IMAGE_STORE_ID) REFERENCES MW_IMAGE_STORE (id) ON DELETE CASCADE;




CREATE OR REPLACE VIEW mw_trust_policy_info_view AS SELECT * FROM  mw_trust_policy mwtrustpolicy  WHERE mwtrustpolicy.archive IS  FALSE;

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
	mwimg.tmp_location,
	mwimg.upload_variables_md5,
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
          WHERE mwimageupload.image_id::text = mwimg.id::text) AS image_upload_count,
	 ( SELECT count(*) AS count
           FROM mw_policy_upload mwpolicyupload
          WHERE mwpolicyupload.policy_id::text = mwtrustpolicy.id::text) AS policy_upload_count	  ,
	mwimg.repository,
	mwimg.tag
   FROM mw_image mwimg
	LEFT JOIN mw_trust_policy_info_view mwtrustpolicy ON mwimg.id::text = mwtrustpolicy.image_id::text
    LEFT JOIN mw_trust_policy_draft mwpolicydraft ON mwpolicydraft.id::text = mwimg.trust_policy_draft_id::text;
	
	
INSERT INTO changelog (ID, APPLIED_AT, DESCRIPTION) VALUES (20120101000550,NOW(),'ran swift sql');





	  


 












	 
		 