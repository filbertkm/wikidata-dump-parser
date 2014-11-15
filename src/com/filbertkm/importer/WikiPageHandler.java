package com.filbertkm.importer;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;

public class WikiPageHandler extends DefaultHandler {

	private Connection conn;

	private WikiPage page;

	private Revision rev;

	private String currentElement;

	@Override
	public void startElement(java.lang.String uri, java.lang.String localName, java.lang.String qName, Attributes attributes) {
		this.currentElement = qName;

		if (qName == "page") {
			this.page = new WikiPage();
		}

		if (qName == "revision") {
			this.rev = new Revision();
		}

	}

	@Override
	public void characters(char[] ch, int start, int length) {
		String value = new String(ch, start, length).trim();

		if (this.page != null) {
			if ((this.currentElement == "title") && (this.page.getTitle() == null)) {
				this.page.setTitle(value);
			}

			if ((this.currentElement == "ns" ) && (this.page.getNamespace() == null)) {
				this.page.setNamespace(new Integer(value));
			}

			if ((this.currentElement == "id") && (this.page.getId() == null)) {
				this.page.setId(new Integer(value));
			}

			if (this.rev != null) {
				if ((this.currentElement == "id") && (this.rev.getId() == null)) {
					rev.setId(new Integer(value));
					this.page.setRevision(rev);
				}

				if (this.currentElement == "text") {
					if (rev.getContent() == null) {
						rev.setContent("");
					}

					String curText = rev.getContent();
					rev.setContent(curText + value);
					this.page.setRevision(rev);
				}

				if ((this.currentElement == "model") && (this.rev.getModel() == null)) {
					rev.setModel(value);
				}

				if ((this.currentElement == "format") && (this.rev.getFormat() == null)) {
					rev.setFormat(value);
				}
			}
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) {
		if ((this.page != null ) && (qName == "page")) {

			if (this.page.getNamespace() == 0) {
				this.processItem();
			} else if (this.page.getNamespace() == 120) {
				this.processProperty();
			} else {

			}

			this.page = null;
		}

		if ((this.rev != null) && (qName == "revision")) {
			this.rev = null;
		}
	}

	private void processItem() {
		this.processEntity("items_20140804");
	}

	private void processProperty() {
		this.processEntity("properties_20140804");
	}

	private void processEntity(String tableName) {
		try {
			if (this.conn == null) {
				this.conn = DriverManager.getConnection(
					"jdbc:postgresql://127.0.0.1:5432/wikidata", "katie",
					"wikidata");
				System.out.println(this.conn.toString());
			}

			String query = "SELECT id FROM " + tableName + " where id = ?";
			PreparedStatement pst = this.conn.prepareStatement(query);
			pst.setInt(1, new Integer(this.page.getTitle().substring(1)));
			ResultSet rs = pst.executeQuery();

			if (!rs.next()) {
				query = "INSERT INTO " + tableName + " VALUES(?, ?, ?, ?, ?)";
				pst = this.conn.prepareStatement(query);

				Integer entityId = new Integer(this.page.getTitle().substring(1));

				if (entityId > 0) {
					pst.setInt(1, entityId);
					pst.setInt(2, this.page.getId());
					pst.setString(3, this.page.getRevision().getContent());
					pst.setInt(4, this.page.getRevision().getId());

					if ("Q".equals(this.page.getTitle().substring(0,1))) {
							pst.setString(5, "item");
					} else if ("q".equals(this.page.getTitle().substring(0,1))) {
						pst.setString(5, "item");
					} else {
						pst.setString(5, "property");
					}

					pst.executeUpdate();
				}
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	public int getPageId() {
		return this.page.getId();
	}
}
