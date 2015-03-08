package com.filbertkm.importer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.wikidata.wdtk.datamodel.interfaces.EntityDocumentProcessor;
import org.wikidata.wdtk.datamodel.interfaces.EntityIdValue;
import org.wikidata.wdtk.datamodel.interfaces.GlobeCoordinatesValue;
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.datamodel.interfaces.PropertyDocument;
import org.wikidata.wdtk.datamodel.interfaces.Statement;
import org.wikidata.wdtk.datamodel.interfaces.StatementGroup;
import org.wikidata.wdtk.datamodel.interfaces.StringValue;
import org.wikidata.wdtk.datamodel.interfaces.Value;
import org.wikidata.wdtk.datamodel.interfaces.ValueSnak;

public class JsonDumpProcessor implements EntityDocumentProcessor {

	private static final Logger logger = Logger.getLogger(Importer.class);
	
	private static final String HSTORE_SEPARATOR_TOKEN = "=>";
	
	private Connection conn;
	
	public JsonDumpProcessor(Connection conn) {
		this.conn = conn;
	}

	public void processItemDocument(ItemDocument itemDocument) {
		String itemId = itemDocument.getEntityId().getId();
		logger.info("Processing: " + itemId);

		extractCoordinates(itemDocument);
	}

	private void extractCoordinates(ItemDocument itemDocument) {
        for (StatementGroup statementGroup : itemDocument.getStatementGroups()) {                  
            String propertyId = statementGroup.getProperty().getId();                              
            
            for (Statement statement : statementGroup.getStatements()) {                                
                if (statement.getClaim().getMainSnak() instanceof ValueSnak) {                          
                    Value value = ((ValueSnak) statement.getClaim().getMainSnak()).getValue(); 
                    
                    if (value instanceof GlobeCoordinatesValue) {
                    	GlobeCoordinatesValue coordinates = (GlobeCoordinatesValue)value;
                    	this.insertCoordinates(itemDocument, coordinates);
                    } else if (value instanceof EntityIdValue) {
                    	EntityIdValue entityIdValue = (EntityIdValue)value;
                    	this.insertEntityIdValue(itemDocument, propertyId, entityIdValue);
                    } else if (value instanceof StringValue) {
                    	StringValue stringValue = (StringValue)value;
                    	this.insertValueSnak(itemDocument, propertyId, stringValue.getString());
                    }
                }
            }
        }
	}
	
	private void insertCoordinates(ItemDocument itemDocument, GlobeCoordinatesValue value) {
		String query = "INSERT INTO coordinates (entity_id, globe, precision, latitude, longitude)"
				+ " VALUES(?, ?, ?, ?, ?)";
		
		try {
			PreparedStatement pst = this.conn.prepareStatement(query);
			pst.setString(1, itemDocument.getEntityId().getId());
			pst.setString(2, value.getGlobe());
			pst.setDouble(3,  value.getPrecision());
			pst.setDouble(4, value.getLatitude());
			pst.setDouble(5, value.getLongitude());
			
			pst.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void insertEntityIdValue(ItemDocument itemDocument, String propertyId, EntityIdValue value) {
		this.insertValueSnak(
			itemDocument,
			propertyId,
			value.getId()
		);
	}
		
	private void insertValueSnak(ItemDocument itemDocument, String propertyId, String value) {
		String query = "INSERT INTO value_snaks (entity_id, values) VALUES(?,?)";
		
		try {
			PreparedStatement pst = this.conn.prepareStatement(query);
			pst.setString(1, itemDocument.getEntityId().getId());

			final StringBuilder builder = new StringBuilder();
			builder.append("\"");
			builder.append(propertyId);
			builder.append("\"");
			builder.append(HSTORE_SEPARATOR_TOKEN);
			builder.append("\"");
			builder.append(value);
			builder.append("\"");
			
			pst.setObject(2, builder.toString(), Types.OTHER);
			System.out.println(pst.toString());
			pst.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void processPropertyDocument(PropertyDocument arg0) {
		// TODO Auto-generated method stub

	}

	public static void configureLogging() {
		// Create the appender that will write log messages to the console.
		ConsoleAppender consoleAppender = new ConsoleAppender();
		// Define the pattern of log messages.
		// Insert the string "%c{1}:%L" to also show class name and line.
		String pattern = "%d{yyyy-MM-dd HH:mm:ss} %-5p - %m%n";
		consoleAppender.setLayout(new PatternLayout(pattern));
		// Change to Level.ERROR for fewer messages:
		consoleAppender.setThreshold(Level.INFO);

		consoleAppender.activateOptions();
		Logger.getRootLogger().addAppender(consoleAppender);
	}

}
