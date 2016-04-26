UPDATE pg_attribute SET atttypmod = 304 WHERE attrelid = 'mw_host'::regclass AND attname = 'ip_address';

INSERT INTO changelog (ID, APPLIED_AT, DESCRIPTION) VALUES (20120101000551,NOW(),'Increase size of IP address in MW_HOST to account for FQDN');
