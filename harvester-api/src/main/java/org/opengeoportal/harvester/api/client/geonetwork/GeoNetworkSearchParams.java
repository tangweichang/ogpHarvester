package org.opengeoportal.harvester.api.client.geonetwork;

import org.jdom.Element;
import org.opengeoportal.harvester.api.domain.IngestGeonetwork;

/**
 * Page is 0 based.
 * 
 * @author <a href="mailto:juanluisrp@geocat.net">Juan Luis Rodríguez</a>.
 * 
 */
public class GeoNetworkSearchParams {
	private static final int NUMBER_OF_RESULTS_PER_PAGE = 40;

	private String freeText;
	private String title;
	private String abstractText;
	private String keyword;
	private String siteId;
	private int page = 0;

	private int pageSize = NUMBER_OF_RESULTS_PER_PAGE;

	public int getPageSize() {
		return pageSize;
	}

	public int getFrom() {
		return (page * pageSize) + 1;
	}

	public int getTo() {
		return (page + 1) * pageSize;
	}

	public GeoNetworkSearchParams(IngestGeonetwork ingest) {
		this(ingest, NUMBER_OF_RESULTS_PER_PAGE);

	}

	public GeoNetworkSearchParams(IngestGeonetwork ingest, int pageSize) {
		this.freeText = ingest.getFreeText();
		this.title = ingest.getTitle();
		this.abstractText = ingest.getAbstractText();
		this.keyword = ingest.getKeyword();
		// TODO
		this.siteId = "";

		this.pageSize = pageSize;
	}

	public Element toXml() {
		Element req = new Element("request");

		add(req, "any", (this.freeText != null) ? this.freeText : "");
		add(req, "title", (this.title != null) ? this.title : "");
		add(req, "abstract", (this.abstractText != null) ? this.abstractText
				: "");
		add(req, "themekey", (this.keyword != null) ? this.keyword : "");
		add(req, "siteId", this.siteId);
		add(req, "from", this.getFrom() + "");
		add(req, "to", this.getTo() + "");

		return req;
	}

	public String toString() {
		StringBuilder result = new StringBuilder();
		String NEW_LINE = System.getProperty("line.separator");

		result.append(this.getClass().getName() + " Object {" + NEW_LINE);
		result.append("any: " + this.freeText + NEW_LINE);
		result.append("title: " + this.title + NEW_LINE);
		result.append("abstract: " + this.abstractText + NEW_LINE);
		result.append("themekey: " + this.keyword + NEW_LINE);
		result.append("siteId: " + this.siteId + NEW_LINE);
		result.append("from: " + this.getFrom() + NEW_LINE);
		result.append("to: " + this.getTo() + NEW_LINE);
		result.append("}");

		return result.toString();
	}

	private void add(Element req, String name, String value) {
		if (value.length() != 0)
			req.addContent(new Element(name).setText(value));
	}

	/**
	 * @return the page
	 */
	public int getPage() {
		return page;
	}

	/**
	 * @param page
	 *            the page to set
	 */
	public void setPage(int page) {
		this.page = page;
	}
}