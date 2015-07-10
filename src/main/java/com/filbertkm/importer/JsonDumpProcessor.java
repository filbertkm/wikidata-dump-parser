package com.filbertkm.importer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.wikidata.wdtk.datamodel.interfaces.EntityDocumentProcessor;
import org.wikidata.wdtk.datamodel.interfaces.EntityIdValue;
import org.wikidata.wdtk.datamodel.interfaces.GlobeCoordinatesValue;
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.datamodel.interfaces.MonolingualTextValue;
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

		extractTerms(itemDocument);
		extractSnaks(itemDocument);
	}
	
	private void extractTerms(ItemDocument itemDocument) {
		extractLabels(itemDocument);
		extractAliases(itemDocument);
		extractDescriptions(itemDocument);
	}
	
	private void extractLabels(ItemDocument itemDocument) {	
		Map<String, MonolingualTextValue> labels = itemDocument.getLabels();
		
		for (Map.Entry<String, MonolingualTextValue> label : labels.entrySet()) {
			addTermToDatabase(
				itemDocument.getEntityId().getId(),
				"label",
				label.getValue().getLanguageCode(),
				label.getValue().getText()
			);		
		}
	}
	
	private void extractAliases(ItemDocument itemDocument) {
		Map<String, List<MonolingualTextValue>> aliases = itemDocument.getAliases();
		
		for (Map.Entry<String, List<MonolingualTextValue>> aliasMap : aliases.entrySet()) {
			List<MonolingualTextValue> languageAliases = aliasMap.getValue();
			
			for (MonolingualTextValue alias : languageAliases) {
				addTermToDatabase(
					itemDocument.getEntityId().getId(),
					"alias",
					alias.getLanguageCode(),
					alias.getText()
				);
			}
		}
	}
	
	private void extractDescriptions(ItemDocument itemDocument) {
		Map<String, MonolingualTextValue> descriptions = itemDocument.getDescriptions();
		
		String query = "INSERT INTO descriptions (entity_id, term_language, term_text)"
				+ " VALUES(?, ?, ?)";
		
		for (Map.Entry<String, MonolingualTextValue> description : descriptions.entrySet()) {
			try {
				PreparedStatement pst = this.conn.prepareStatement(query);
				pst.setString(1, itemDocument.getEntityId().getId());
				pst.setString(2, description.getValue().getLanguageCode());
				pst.setString(3, description.getValue().getText());
				
				pst.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void addTermToDatabase(String itemId, String termType, String languageCode, String text) {
		String query = "INSERT INTO terms (entity_id, term_type, term_language, term_text)"
				+ " VALUES(?, ?, ?, ?)";
		
		try {
			PreparedStatement pst = this.conn.prepareStatement(query);
			pst.setString(1, itemId);
			pst.setString(2, termType);
			pst.setString(3, languageCode);
			pst.setString(4, text);
			
			pst.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}	
	}

	private void extractSnaks(ItemDocument itemDocument) {
		ArrayList<String> snaks = new ArrayList<>();
		
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
                    	snaks.add(this.buildEntityIdValueSnak(propertyId, entityIdValue));
                    } else if (value instanceof StringValue) {
                    	StringValue stringValue = (StringValue)value;
                    	snaks.add(this.buildValueSnak(propertyId, stringValue.getString()));
                    }
                }
            }
        }
		
		insertValueSnaks(itemDocument, snaks);
	}
	
	private void insertCoordinates(ItemDocument itemDocument, GlobeCoordinatesValue value) {
		String query = "INSERT INTO coordinates (entity_id, globe, precision, latitude, longitude)"
				+ " VALUES(?, ?, ?, ?, ?)";
		
		try {
			PreparedStatement pst = this.conn.prepareStatement(query);
			pst.setString(1, itemDocument.getEntityId().getId());
			pst.setString(2, value.getGlobe());
			pst.setDouble(3, value.getPrecision());
			pst.setDouble(4, value.getLatitude());
			pst.setDouble(5, value.getLongitude());
			
			pst.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private String buildEntityIdValueSnak(String propertyId, EntityIdValue value) {
		return this.buildValueSnak(
			propertyId,
			value.getId()
		);
	}
		
	private String buildValueSnak(String propertyId, String value) {
		final StringBuilder builder = new StringBuilder();
		builder.append(propertyId);
		builder.append(HSTORE_SEPARATOR_TOKEN);
		builder.append("\"");
		builder.append(value);
		builder.append("\"");
		
		return builder.toString();
	}
	
	private void insertValueSnaks(ItemDocument itemDocument, ArrayList<String> snaks) {
		String query = "INSERT INTO value_snaks (entity_id, values) VALUES(?,?)";
	
		try {
			PreparedStatement pst = this.conn.prepareStatement(query);
			pst.setString(1, itemDocument.getEntityId().getId());			
			pst.setObject(2, buildSnaksString(snaks), Types.OTHER);
			System.out.println(pst.toString());
			pst.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private String buildSnaksString(ArrayList<String> snaks) {
		String snakString = "";
		
		for (int i = 0; i < snaks.size() - 1; i++) {
			if ( i == 0 ) {
				snakString = snakString + snaks.get(i);
			} else {
				snakString = snakString + ", " + snaks.get(i);
			}
		}
		
		return snakString;
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
