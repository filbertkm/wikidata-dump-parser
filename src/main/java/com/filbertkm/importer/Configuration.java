package com.filbertkm.importer;

import org.kohsuke.args4j.Option;

public class Configuration {

	@Option(name = "-dbhost", usage = "database host", required = true)
	private String dbhost;

	@Option(name = "-dbuser", usage = "database user", required = true)
	private String dbuser;

	@Option(name = "-dbname", usage = "database name", required = true)
	private String dbname;

	@Option(name = "-dbpass", usage = "database password", required = true)
	private String dbpass;

	@Option(name = "-dumpdir", usage = "dump directory", required = true)
	private String dumpdir;

	public String getDBHost() {
		return dbhost;
	}

	public String getDbUser() {
		return dbuser;
	}

	public String getDbName() {
		return dbname;
	}

	public String getDbPass() {
		return dbpass;
	}

	public String getDumpDir() {
		return dumpdir;
	}

}
