INSERT INTO changelog (ID, APPLIED_AT, DESCRIPTION) VALUES (20120101000002,NOW(),'Windows Drive Letter Addition');

ALTER TABLE MW_IMAGE ADD COLUMN DRIVES VARCHAR(36);

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
	mwimg.tag,
	mwimg.drives
   FROM mw_image mwimg
	LEFT JOIN mw_trust_policy_info_view mwtrustpolicy ON mwimg.id::text = mwtrustpolicy.image_id::text
    LEFT JOIN mw_trust_policy_draft mwpolicydraft ON mwpolicydraft.id::text = mwimg.trust_policy_draft_id::text;

ALTER TABLE mw_image_info_view
  OWNER TO postgres;
  
INSERT INTO mw_policy_template(id, name, deployment_type, content, active, deployment_type_identifier,policy_type) VALUES ('3', 'Bare Metal (W)', 'BareMetal', '<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Manifest xmlns="mtwilson:trustdirector:manifest:1.1" DigestAlg="sha1"><File Path="/C:/Users/GS-1011/Downloads/SomeRemoteHost1.tar/trustpolicy.xml"/><File Path="/C:/Users/GS-1011/Downloads/SomeRemoteHost1.tar/manifest.xml"/><File Path="/C:/dcg_security-director-swift/database/postgresql/target/trust-director-postgresql-3.0-SNAPSHOT-sources.jar"/><File Path="/C:/dcg_security-director-swift/database/pom.xml"/><File Path="/C:/dcg_security-director-swift/database/postgresql/target/maven-archiver/pom.properties"/><File Path="/C:/dcg_security-director-swift/database/postgresql/src/main/resources/com/intel/mtwilson/director/database/postgresql/20120101000001_bootstrap.sql"/><File Path="/C:/dcg_security-director-swift/database/postgresql/pom.xml"/><File Path="/C:/dcg_security-director-swift/database/postgresql/target/trust-director-postgresql-3.0-SNAPSHOT.jar"/><Dir Exclude="" Include="*.*" Path="/C:/Eclipse/eclipse-jee-luna-SR2-win32-x86_64/eclipse" Recursive="false"/><File Path="/C:/Eclipse/eclipse-jee-luna-SR2-win32-x86_64/eclipse/.eclipseproduct"/><File Path="/C:/Eclipse/eclipse-jee-luna-SR2-win32-x86_64/eclipse/artifacts.xml"/><File Path="/C:/Eclipse/eclipse-jee-luna-SR2-win32-x86_64/eclipse/eclipse.ini"/><File Path="/C:/Eclipse/eclipse-jee-luna-SR2-win32-x86_64/eclipse/notice.html"/><File Path="/C:/Eclipse/eclipse-jee-luna-SR2-win32-x86_64/eclipse/eclipsec.exe"/><File Path="/C:/Eclipse/eclipse-jee-luna-SR2-win32-x86_64/eclipse/eclipse.exe"/><File Path="/C:/Eclipse/eclipse-jee-luna-SR2-win32-x86_64/eclipse/epl-v10.html"/></Manifest>', true, 'W', 'Manifest'); 
 