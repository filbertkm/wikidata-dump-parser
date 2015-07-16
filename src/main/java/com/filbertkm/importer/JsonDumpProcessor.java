package com.filbertkm.importer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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
import org.wikidata.wdtk.datamodel.interfaces.SiteLink;
import org.wikidata.wdtk.datamodel.interfaces.Statement;
import org.wikidata.wdtk.datamodel.interfaces.StatementDocument;
import org.wikidata.wdtk.datamodel.interfaces.StatementGroup;
import org.wikidata.wdtk.datamodel.interfaces.StringValue;
import org.wikidata.wdtk.datamodel.interfaces.TermedDocument;
import org.wikidata.wdtk.datamodel.interfaces.TimeValue;
import org.wikidata.wdtk.datamodel.interfaces.Value;
import org.wikidata.wdtk.datamodel.interfaces.ValueSnak;

public class JsonDumpProcessor implements EntityDocumentProcessor {

	private static final Logger logger = Logger.getLogger(Importer.class);
	
	private Connection conn;

	private PreparedStatement pstInsertLabel;
	private PreparedStatement pstInsertAlias;
	private PreparedStatement pstInsertDescription;
	private PreparedStatement pstInsertSiteLink;
	private PreparedStatement pstInsertClauseCoordinates;
	private PreparedStatement pstInsertClauseDateTime;
	private PreparedStatement pstInsertClauseEntity;
	private PreparedStatement pstInsertClauseString;

	private int documentCount = 0;
	private int documentBatchSize = 1000;
	
	public JsonDumpProcessor(Connection conn) {
		this.conn = conn;

		try {
			String queryInsertLabel = "INSERT INTO label (entity_id, label_language, label_text)"
					+ " VALUES(?, ?, ?)";
			pstInsertLabel = this.conn.prepareStatement(queryInsertLabel);
		
			String queryInsertAlias = "INSERT INTO alias (entity_id, alias_language, alias_text)"
					+ " VALUES(?, ?, ?)";
			pstInsertAlias = this.conn.prepareStatement(queryInsertAlias);

			String queryInsertDescriptions = "INSERT INTO description (entity_id, description_language, description_text)"
					+ " VALUES(?, ?, ?)";
			pstInsertDescription = this.conn.prepareStatement(queryInsertDescriptions);
		
			String queryInsertSiteLink = "INSERT INTO sitelink (entity_id, site_key, page_title)"
					+ " VALUES(?, ?, ?)";
			pstInsertSiteLink = this.conn.prepareStatement(queryInsertSiteLink);
		
			String queryInsertClauseCoordinates = "INSERT INTO claim_coordinate (entity_id, property_id, globe, precision, latitude, longitude)"
					+ " VALUES(?, ?, ?, ?, ?, ?)";
			pstInsertClauseCoordinates = this.conn.prepareStatement(queryInsertClauseCoordinates);

			String queryInsertClauseDateTime = "INSERT INTO claim_datetime (entity_id, property_id, calendar, year, month, day, hour, minute, second, precision, tolerance_before, tolerance_after)"
					+ " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			pstInsertClauseDateTime = this.conn.prepareStatement(queryInsertClauseDateTime);

			String queryInsertClauseEntity = "INSERT INTO claim_entity (entity_id, property_id, value) VALUES (?,?,?)";
			pstInsertClauseEntity = this.conn.prepareStatement(queryInsertClauseEntity);

			String queryInsertClauseString = "INSERT INTO claim_string (entity_id, property_id, value) VALUES (?,?,?)";
			pstInsertClauseString = this.conn.prepareStatement(queryInsertClauseString);

		} catch (SQLException e) {
			e.printStackTrace();
		}	
	}

	public void processItemDocument(ItemDocument itemDocument) {
		String itemId = itemDocument.getEntityId().getId();
		logger.info("Processing: " + itemId);
		
		extractLabels(itemDocument);
		extractAliases(itemDocument);
		extractDescriptions(itemDocument);
		extractSiteLinks(itemDocument);
		extractClaims(itemDocument);

		flushBatch();
	}

	public void processPropertyDocument(PropertyDocument propertyDoc) {
		String itemId = propertyDoc.getEntityId().getId();
		logger.info("Processing: " + itemId);

		extractLabels(propertyDoc);
		extractAliases(propertyDoc);
		extractDescriptions(propertyDoc);
		extractClaims(propertyDoc);

		flushBatch();
	}
	
	private void flushBatch() {
		documentCount++;
		if (documentCount > documentBatchSize) {
			flush();
		}		
	}

	public void flush() {
		try {
			pstInsertLabel.executeBatch();
			pstInsertAlias.executeBatch();
			pstInsertDescription.executeBatch();
			pstInsertSiteLink.executeBatch();
			pstInsertClauseCoordinates.executeBatch();
			pstInsertClauseDateTime.executeBatch();
			pstInsertClauseEntity.executeBatch();
			pstInsertClauseString.executeBatch();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
		
	private void extractLabels(TermedDocument itemDocument) {	
		Map<String, MonolingualTextValue> labels = itemDocument.getLabels();
		
		for (Map.Entry<String, MonolingualTextValue> label : labels.entrySet()) {
			try {
				pstInsertLabel.setString(1, itemDocument.getEntityId().getId());
				pstInsertLabel.setString(2, label.getValue().getLanguageCode());
				pstInsertLabel.setString(3, label.getValue().getText());
				pstInsertLabel.addBatch();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void extractAliases(TermedDocument itemDocument) {
		Map<String, List<MonolingualTextValue>> aliases = itemDocument.getAliases();
		
		for (Map.Entry<String, List<MonolingualTextValue>> aliasMap : aliases.entrySet()) {
			List<MonolingualTextValue> languageAliases = aliasMap.getValue();
			
			for (MonolingualTextValue alias : languageAliases) {
				
				try {
					pstInsertAlias.setString(1, itemDocument.getEntityId().getId());
					pstInsertAlias.setString(2, alias.getLanguageCode());
					pstInsertAlias.setString(3, alias.getText());
					pstInsertAlias.addBatch();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private void extractDescriptions(TermedDocument itemDocument) {
		Map<String, MonolingualTextValue> descriptions = itemDocument.getDescriptions();
		
		for (Map.Entry<String, MonolingualTextValue> description : descriptions.entrySet()) {
			try {
				pstInsertDescription.setString(1, itemDocument.getEntityId().getId());
				pstInsertDescription.setString(2, description.getValue().getLanguageCode());
				pstInsertDescription.setString(3, description.getValue().getText());
				pstInsertDescription.addBatch();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	private void extractSiteLinks(ItemDocument itemDocument) {
		Map<String, SiteLink> sitelinks = itemDocument.getSiteLinks();
		
		for (Map.Entry<String, SiteLink> sitelink : sitelinks.entrySet()) {
			try {
				pstInsertSiteLink.setString(1, itemDocument.getEntityId().getId());
				pstInsertSiteLink.setString(2, sitelink.getValue().getSiteKey());
				pstInsertSiteLink.setString(3, sitelink.getValue().getPageTitle());
				// getBadges
				pstInsertSiteLink.addBatch();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void extractClaims(StatementDocument itemDocument) {

		for (StatementGroup statementGroup : itemDocument.getStatementGroups()) {                  
			String propertyId = statementGroup.getProperty().getId();                     
                
            for (Statement statement : statementGroup.getStatements()) {                                
                if (statement.getClaim().getMainSnak() instanceof ValueSnak) {                          
                    Value value = ((ValueSnak) statement.getClaim().getMainSnak()).getValue(); 
                    
                    if (value instanceof GlobeCoordinatesValue) {
                    	GlobeCoordinatesValue coordinates = (GlobeCoordinatesValue)value;
                    	insertCoordinates(itemDocument, propertyId, coordinates);
                    } else if (value instanceof TimeValue) {
                    	TimeValue timeValue = (TimeValue)value;
                    	insertDateTime(itemDocument, propertyId, timeValue);
                    } else if (value instanceof EntityIdValue) {
                    	EntityIdValue entityIdValue = (EntityIdValue)value;
                        insertEntity(itemDocument, propertyId, entityIdValue.getId());
                    } else if (value instanceof StringValue) {
                    	StringValue stringValue = (StringValue)value;
                    	insertString(itemDocument, propertyId, stringValue.getString());
                    }
                }
            }
        }
	}
	
	private void insertCoordinates(StatementDocument itemDocument, String propertyId, GlobeCoordinatesValue value) {

		try {
			pstInsertClauseCoordinates.setString(1, itemDocument.getEntityId().getId());
			pstInsertClauseCoordinates.setString(2, propertyId);
			pstInsertClauseCoordinates.setString(3, value.getGlobe());
			pstInsertClauseCoordinates.setDouble(4, value.getPrecision());
			pstInsertClauseCoordinates.setDouble(5, value.getLatitude());
			pstInsertClauseCoordinates.setDouble(6, value.getLongitude());
			pstInsertClauseCoordinates.addBatch();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void insertDateTime(StatementDocument itemDocument, String propertyId, TimeValue value) {
		try {
			pstInsertClauseDateTime.setString(1, itemDocument.getEntityId().getId());
			pstInsertClauseDateTime.setString(2, propertyId);
			pstInsertClauseDateTime.setString(3, value.getPreferredCalendarModel());
			pstInsertClauseDateTime.setDouble(4, value.getYear());
			pstInsertClauseDateTime.setDouble(5, value.getMonth());
			pstInsertClauseDateTime.setDouble(6, value.getDay());
			pstInsertClauseDateTime.setDouble(7, value.getHour());
			pstInsertClauseDateTime.setDouble(8, value.getMinute());
			pstInsertClauseDateTime.setDouble(9, value.getSecond());
			pstInsertClauseDateTime.setDouble(10, value.getPrecision());
			pstInsertClauseDateTime.setDouble(11, value.getBeforeTolerance());
			pstInsertClauseDateTime.setDouble(12, value.getAfterTolerance());
			pstInsertClauseDateTime.addBatch();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void insertString(StatementDocument itemDocument, String propertyId, String value) {
		
		try {
			pstInsertClauseString.setString(1, itemDocument.getEntityId().getId());
			pstInsertClauseString.setString(2, propertyId);
			pstInsertClauseString.setString(3, value);
			pstInsertClauseString.addBatch();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void insertEntity(StatementDocument itemDocument, String propertyId, String value) {
		
		try {
			pstInsertClauseEntity.setString(1, itemDocument.getEntityId().getId());
			pstInsertClauseEntity.setString(2, propertyId);
			pstInsertClauseEntity.setString(3, value);
			pstInsertClauseEntity.addBatch();
		} catch (SQLException e) {
			e.printStackTrace();
		}
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
