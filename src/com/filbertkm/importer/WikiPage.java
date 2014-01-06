package com.filbertkm.importer;

public class WikiPage {
	
	private Integer id;
	
	private Integer namespace;

	private String title;
	
	private Revision revision;
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	public Integer getId() {
		return this.id;
	}
	
	public void setNamespace(Integer ns) {
		this.namespace = ns;
	}
	
	public Integer getNamespace() {
		return this.namespace;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getTitle() {
		return this.title;
	}
	
	public void setRevision(Revision rev) {
		this.revision = rev;
	}
	
	public Revision getRevision() {
		return this.revision;
	}
	
	@Override
	public String toString() {
		return this.id.toString() + ": " + this.getNamespace() + ":" + this.getTitle()
				+ ", revision: " + this.getRevision().getId().toString()
				+ ", content: " + this.getRevision().getContent();
	}
	
}
