package feedblender.model;

import io.vertx.core.json.JsonObject;

/**
 * Created by:
 * User: Paul Rogalinski
 * Date: 16.08.2015
 * Time: 16:36
 */
public class FeedItemMediaLink {

	private String link;
	private String type;
	private long length;

	public FeedItemMediaLink() {
	}

	public FeedItemMediaLink(String link, String type, long length) {
		this.link = link;
		this.type = type;
		this.length = length;
	}

	public String getLink() {
		return link;
	}

	public String getType() {
		return type;
	}

	public long getLength() {
		return length;
	}

	public JsonObject asJsonObject() {
		return new JsonObject() {{
			put("link", link);
			put("length", length);
			put("type", type);
		}};
	}

	@Override
	public String toString() {
		return "FeedItemMediaLink{" +
			"link='" + link + '\'' +
			", type='" + type + '\'' +
			", length=" + length +
			'}';
	}
}
