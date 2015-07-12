package com.filbertkm.importer;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.wikidata.wdtk.dumpfiles.DumpProcessingController;

public class Importer {

	private Connection conn;

	private String dbHost;

	private String dbUser;

	private String dbName;

	private String dbPass;

	public static void main(String[] args) {
		Configuration config = new Configuration();
		CmdLineParser parser = new CmdLineParser(config);

		try {
			parser.parseArgument(args);

			Importer importer = new Importer(config.getDBHost(), config.getDbUser(), config.getDbName(), config.getDbPass());
			importer.process("wikidatawiki", config.getDumpDir());
		} catch (CmdLineException e) {
			// omg!
			e.printStackTrace();
		}


		System.out.println("done");
	}

	public Importer(String dbHost, String dbUser, String dbName, String dbPass) {
		this.dbHost = dbHost;
		this.dbUser = dbUser;
		this.dbName = dbName;
		this.dbPass = dbPass;
	}

	public void process(String wikiId, String dumpDirectory) {
		JsonDumpProcessor jsonDumpProcessor = new JsonDumpProcessor(getConnection());
		JsonDumpProcessor.configureLogging();

		DumpProcessingController dumpProcessingController = new DumpProcessingController(wikiId);
		dumpProcessingController.setOfflineMode(true);

		try {
			dumpProcessingController.setDownloadDirectory(dumpDirectory);
			dumpProcessingController.registerEntityDocumentProcessor(jsonDumpProcessor, null, true);
			dumpProcessingController.processMostRecentJsonDump();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private Connection getConnection() {
		if (this.conn == null) {
			try {
				this.conn = DriverManager.getConnection(
					"jdbc:postgresql://" + this.dbHost + ":5432/" + this.dbName,
					this.dbUser,
					this.dbPass
				);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}

		return this.conn;
	}
}
