package com.filbertkm.importer;

import org.kohsuke.args4j.Option;

public class Configuration {
	
	@Option(name = "-dbuser", usage = "database user", required = true)
	private String dbuser;
	
	@Option(name = "-dbname", usage = "database name", required = true)
	private String dbname;
	
	@Option(name = "-dumpdir", usage = "dump directory", required = true)
	private String dumpdir;
	
	public String getDbUser() {
		return dbuser;
	}
	
	public String getDbName() {
		return dbname;
	}
	
	public String getDumpDir() {
		return dumpdir;
	}

}
