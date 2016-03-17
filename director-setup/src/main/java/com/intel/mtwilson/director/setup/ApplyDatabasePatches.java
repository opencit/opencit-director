/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.director.setup;

//import com.intel.dcsg.cpg.io.Resource;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import com.intel.director.common.Constants;
import com.intel.mtwilson.Folders;
import com.intel.mtwilson.configuration.ConfigurationFactory;
import com.intel.mtwilson.configuration.ConfigurationProvider;
import com.intel.mtwilson.setup.AbstractSetupTask;
import com.intel.mtwilson.setup.SetupException;

/**
 *
 * @author aakash
 */
public class ApplyDatabasePatches extends AbstractSetupTask {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ApplyDatabasePatches.class);



    private String databaseDriver;
    private String databaseUsername;
    private String databasePassword;
    private String databaseUrl;
    private String databaseVendor;
            
    @Override
    protected void configure() throws Exception {
    	
    	log.info("Inside ApplyDatabasePatches configure");
    	File customFile = new File( Folders.configuration() + File.separator + "director.properties" );
		ConfigurationProvider provider;
		try {
		provider = ConfigurationFactory.createConfigurationProvider(customFile);
		com.intel.dcsg.cpg.configuration.Configuration loadedConfiguration = provider.load();
		databaseDriver= loadedConfiguration.get(Constants.DIRECTOR_DB_DRIVER);
		databaseUrl=loadedConfiguration.get(Constants.DIRECTOR_DB_URL);
		databaseUsername=loadedConfiguration.get(Constants.DIRECTOR_DB_USERNAME);
	    databasePassword=loadedConfiguration.get(Constants.DIRECTOR_DB_PASSWORD);
	    databaseVendor="postgresql";
	    
		} catch (IOException e1) {
			log.error("Failed to fetch database properties form director.properties",e1);
		}
    	
		
       
        if( databaseDriver == null ) {
            configuration("Database driver not configured");
        }
        else {
            log.info("Database driver: {}", databaseDriver);
        }
       
        if( databaseUrl == null ) {
            configuration("Database URL not configured");
        }
        else {
            log.info("Database URL: {}", databaseUrl); 
        }
        
        DirectorDbConnect.initialize(databaseUrl,databaseDriver,databaseUsername,databasePassword);
        checkAvailableUpdates(); 
    }

    @Override
    protected void validate() throws Exception {
        if( testConnection() ) {
            checkAvailableUpdates();
        }
    }

    @Override
    protected void execute() throws Exception {
        initDatabase();
    }
    
    private boolean testConnection() {
    	 try {
    	log.debug("Inside  testConnection ");
    	Connection c1 = DirectorDbConnect.getConnection();
    	Statement s1 = c1.createStatement();
    	s1.executeQuery("SELECT 1"); 
            return true;
        }
        catch(Exception e) {
            log.error("Director : cannot connect to database", e);
            validation("Director : Cannot connect to database");
            return false;
        }
        
    }
 
  

    public static class ChangelogEntry {
        public String id;
        public String applied_at;
        public String description;
        public ChangelogEntry() { }
        public ChangelogEntry(String id, String applied_at, String description) {
            this.id = id;
            this.applied_at = applied_at;
            this.description = description;
        }
    }
    
    private void verbose(String format, Object... args) {
        
        log.debug(String.format(format, args));
    }
    
    private Map<Long,Resource> sql;
   
    
    
    private HashSet<Long> fetchChangesToApply() throws SetupException, IOException, SQLException{
    	 HashSet<Long> changesToApply =null;
    	log.info("Inside fetchChangesToApply()", databaseVendor);
        sql = getSql(databaseVendor); 
        

        
        log.debug("Connecting to {}", databaseVendor);
        Connection c;
        try {
            c = DirectorDbConnect.getConnection();  // username and password should already be set in the datasource
        }
        catch(SQLException e) {
            log.error("Failed to connect to {} with schema: error = {}", databaseVendor, e.getMessage()); 
                return changesToApply;
        } catch (ClassNotFoundException e) {
        	 log.error("Failed to connect to {} with schema: error = {}", databaseVendor, e.getMessage()); 
             return changesToApply;
		}
        List<ChangelogEntry> changelog=null; 
        try{
        changelog = getChangelog(c);
        }finally{
        	c.close();
        }
        HashMap<Long,ChangelogEntry> presentChanges = new HashMap<>(); // what is already in the database according to the changelog
        verbose("Existing database changelog has %d entries", changelog.size());
        for(ChangelogEntry entry : changelog) {
            if( entry != null ) { 
                verbose("%s %s %s", entry.id, entry.applied_at, entry.description); 
                presentChanges.put(Long.valueOf(entry.id), entry);
            }
        }

        HashSet<Long> unknownChanges = new HashSet<>(presentChanges.keySet()); // list of what is in database
            unknownChanges.removeAll(sql.keySet()); // remove what we have in this installer
            if( unknownChanges.isEmpty() ) {
                log.info("Database is compatible");              
            }
            else { 
                log.warn("Database schema is newer than this version of Mt Wilson");
                ArrayList<Long> unknownChangesInOrder = new ArrayList<>(unknownChanges);
                Collections.sort(unknownChangesInOrder);
                for(Long unknownChangeId : unknownChangesInOrder) {
                    ChangelogEntry entry = presentChanges.get(unknownChangeId);
                    log.info(String.format("%s %s %s", entry.id, entry.applied_at, entry.description));
                }
            }
        changesToApply = new HashSet<>(sql.keySet());
        changesToApply.removeAll(presentChanges.keySet());
        return changesToApply;
    }
    
    private void checkAvailableUpdates() throws SetupException, IOException, SQLException {
        
    	HashSet<Long> changesToApply = fetchChangesToApply();
       
        if( changesToApply.isEmpty() ) {
            log.info("No database updates available");
        }
        else {
            validation("There are %s database updates to apply", changesToApply.size());
        }
        
    }

    /**
     * Must call checkAvailableUpdates() first in order to initialize the 
     * "sql" and "changesToApply" member variables
     * 
     * @throws SetupException
     * @throws IOException
     * @throws SQLException 
     * @throws ClassNotFoundException 
     */
    private void initDatabase() throws SetupException, IOException, SQLException, ClassNotFoundException {

    	HashSet<Long> changesToApply = fetchChangesToApply();
        log.info(" initDatabase  Connecting to {}", databaseVendor);
        try (Connection c = DirectorDbConnect.getConnection()) {
            ArrayList<Long> changesToApplyInOrder = new ArrayList<>(changesToApply);
            Collections.sort(changesToApplyInOrder);
            
            
    //        if(options.getBoolean("check", false)) {
                log.info("The following changes will be applied:");
                        for(Long changeId : changesToApplyInOrder) {
                            /*
                            ChangelogEntry entry = presentChanges.get(changeId);
                            System.out.println(String.format("%s %s %s", entry.id, entry.applied_at, entry.description));
                            */
                            log.info("Change ID: {}", changeId);
                        }
    //            System.exit(0); // database is compatible
    //            return;
    //        }
            
            ResourceDatabasePopulator rdp = new ResourceDatabasePopulator();
            // removing unneeded output as user can't choice what updates to apply
            //System.out.println("Available database updates:");
            for(Long id : changesToApplyInOrder) {
                //System.out.println(String.format("%d %s", id, basename(sql.get(id).getURL())));
                rdp.addScript(sql.get(id)); // new ClassPathResource("/com/intel/mtwilson/database/mysql/bootstrap.sql")); // must specify full path to resource
            }
            
            rdp.setContinueOnError(true);
            rdp.setIgnoreFailedDrops(true);
            rdp.setSeparator(";");
            rdp.populate(c);
        }  catch(SQLException e) {
            log.error("Failed to connect to {} with schema: error = {}", databaseVendor, e.getMessage()); 
                validation("Cannot connect to database");
        }

    }
   
    /**
     * Locates the SQL files for the specified vendor, and reads them to
     * create a mapping of changelog-date to SQL content. This mapping can
     * then be used to select which files to execute against an existing
     * database.
     * See also iBatis, which we are (very) roughly emulating.
     * @param databaseVendor
     * @return 
     */
    private Map<Long,Resource> getSql(String databaseVendor) throws SetupException {
        System.out.println("Scanning for "+databaseVendor+" SQL files");
        HashMap<Long,Resource> sqlmap = new HashMap<>();
        try {
            Resource[] list = listResources(databaseVendor); // each URL like: jar:file:/C:/Users/jbuhacof/workspace/mountwilson-0.5.4/desktop/setup-console/target/setup-console-0.5.4-SNAPSHOT-with-dependencies.jar!/com/intel/mtwilson/database/mysql/20121226000000_remove_created_by_patch_rc3.sql
            for(Resource resource : list) {
                URL url = resource.getURL();
                
                Long timestamp = getTimestampFromSqlFilename(basename(url));
                if( timestamp != null ) {
                    sqlmap.put(timestamp, resource);
                }
                else {
                    log.error("SQL filename is not in recognized format: "+url.toExternalForm());
                }
            }
        }
        catch(IOException e) {
            throw new SetupException("Error while scanning for SQL files: "+e.getLocalizedMessage(), e);
        }
        //System.err.println("Number of SQL files: "+sqlmap.size());
        return sqlmap;        
    }
    
    private Resource[] listResources(String databaseVendor) throws IOException {
    	
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(getClass().getClassLoader());
        Resource[] resources = resolver.getResources("classpath:com/intel/mtwilson/director/database/"+databaseVendor+"/*.sql");
        return resources; 
    }
    
    Pattern pTimestampName = Pattern.compile("^([0-9]+).*");
    
    /**
     * Given a URL, returns the final component filename
     * 
     * Example URL: jar:file:/C:/Users/jbuhacof/workspace/mountwilson-0.5.4/desktop/setup-console/target/setup-console-0.5.4-SNAPSHOT-with-dependencies.jar!/com/intel/mtwilson/database/mysql/20121226000000_remove_created_by_patch_rc3.sql
     * Example output: 20121226000000_remove_created_by_patch_rc3.sql
     * @param url
     * @return 
     */
    private String basename(URL url) {
        String[] parts = StringUtils.split(url.toExternalForm(), "/");
        return parts[parts.length-1];
    }
    
    /**
     * @param filename without any path like: 20121226000000_remove_created_by_patch_rc3.sql
     * @return 
     */
    private Long getTimestampFromSqlFilename(String filename) {
        Matcher mTimestampName = pTimestampName.matcher(filename);
        if( mTimestampName.matches() ) {
            String timestamp = mTimestampName.group(1); // the timestamp like: 20121226000000
            return Long.valueOf(timestamp);
        }
        return null;
    }

 


    
    
    private List<String> getTableNames(Connection c) throws SQLException {
        
       ArrayList<String> list = new ArrayList<>();
        try (Statement s = c.createStatement()) {
            String sqlStmt = "";
           switch (databaseVendor) {
               case "mysql":
                   sqlStmt = "SHOW TABLES";
                   break;
               case "postgresql":
                   sqlStmt = "SELECT table_name FROM information_schema.tables;";
                   break;
           }
           try (ResultSet rs = s.executeQuery(sqlStmt)) {
               while(rs.next()) {
                   list.add(rs.getString(1));
               }
           }
        }
        return list;

    }
    
    private List<ChangelogEntry> getChangelog(Connection c) throws SQLException {
        ArrayList<ChangelogEntry> list = new ArrayList<>();
        log.debug("Listing tables...");
        // first determine if we have the new changelog table `mw_changelog`, or the old one `changelog`, or none at all
        List<String> tableNames = getTableNames(c);
        boolean hasMwChangelog = false;
        boolean hasChangelog = false;
        if( tableNames.contains("mw_changelog") ) {
            hasMwChangelog = true;
        }
        if( tableNames.contains("changelog") ) {
            hasChangelog = true;
        }
        
        if( !hasChangelog && !hasMwChangelog) {
            return list; /*  empty list indicates database is not initialized and all scripts need to be executed */ 
        }
        
        String changelogTableName = null;
        // if we have both changelog tables, copy all records from old changelog to new changelog and then use that
        if( hasChangelog && hasMwChangelog ) {
            try (PreparedStatement check = c.prepareStatement("SELECT APPLIED_AT FROM mw_changelog WHERE ID=?")) {
                try (PreparedStatement insert = c.prepareStatement("INSERT INTO mw_changelog SET ID=?, APPLIED_AT=?, DESCRIPTION=?")) {
                    try (Statement select = c.createStatement()) {
                        try (ResultSet rs = select.executeQuery("SELECT ID,APPLIED_AT,DESCRIPTION FROM changelog")) {
                            while(rs.next()) {
                                check.setLong(1, rs.getLong("ID"));
                                try (ResultSet rsCheck = check.executeQuery()) {
                                    if( rsCheck.next() ) {
                                        // the id is already in the new mw_changelog table
                                    }
                                    else {
                                        insert.setLong(1, rs.getLong("ID"));
                                        insert.setString(2, rs.getString("APPLIED_AT"));
                                        insert.setString(3, rs.getString("DESCRIPTION"));
                                        insert.executeUpdate();
                                    }
                                }
                            }
                        }
                    }
                }
            }
            changelogTableName = "mw_changelog"; 
        }
        else if( hasMwChangelog ) {
            changelogTableName = "mw_changelog";
        }
        else if( hasChangelog ) {
            changelogTableName = "changelog";
        }
        try (Statement s = c.createStatement()) {
            try (ResultSet rs = s.executeQuery(String.format("SELECT ID,APPLIED_AT,DESCRIPTION FROM %s", changelogTableName))) {
                while(rs.next()) {
                    ChangelogEntry entry = new ChangelogEntry();
                    entry.id = rs.getString("ID");
                    entry.applied_at = rs.getString("APPLIED_AT");
                    entry.description = rs.getString("DESCRIPTION");
                    list.add(entry);
                }
            }
        }
        return list;
    }
    
}
