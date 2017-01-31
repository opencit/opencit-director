package com.intel.mtwilson.director.setup;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;


/**
*
* @author aakash
*/
public class DirectorDbConnect {
	
	 private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DirectorDbConnect.class);
	public static String url;
	public static String userName;
	public static String password;
	public static String driver;
	public static Connection conn;

	public static String getUrl() {
		return url;
	}

	public static void setUrl(String url) {
		DirectorDbConnect.url = url;
	}

	public static String getUserName() {
		return userName;
	}

	public static void setUserName(String userName) {
		DirectorDbConnect.userName = userName;
	}

	public static String getPassword() {
		return password;
	}

	public static void setPassword(String password) {
		DirectorDbConnect.password = password;
	}

	public static String getDriver() {
		return driver;
	}

	public static void setDriver(String driver) {
		DirectorDbConnect.driver = driver;
	}

	public static Connection getConnection() throws ClassNotFoundException,
			SQLException {
		log.debug("###Inside DirectorDbConnect get Connection:: url::"+url+" , userName::"+userName+" driver::"+driver);
		Properties info = new Properties( );
		info.put( "user", userName );
		info.put( "password", password);
	
		if(conn!=null && !conn.isClosed()){
			return conn;
		}
		Class.forName(driver);		
		conn = DriverManager.getConnection(url, info);		
		return conn;
	}

	public static void initialize(String databaseUrl, String databaseDriver,
			String databaseUsername, String databasePassword) {
		setUrl(databaseUrl);
		setUserName(databaseUsername);
		setPassword(databasePassword);
		setDriver(databaseDriver);
	}
	
	/*public static void main(String args[]){
		initialize("jdbc:postgresql://10.35.35.83:5432/director_data_pu","org.postgresql.Driver","admin","passwd");
		try {
			getConnection();
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/
}
