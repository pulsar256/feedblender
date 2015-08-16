package feedblender.model;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by:
 * User: Paul Rogalinski
 * Date: 16.08.2015
 * Time: 15:38
 */
public class FeedItem implements Serializable {

	private String feedId;
	private String title;
	private String feedName;
	private Date date;
	private String description;
	private String link;
	private List<FeedItemMediaLink> feedItemMediaLinks;

	public FeedItem(String title, String feedName, Date date, String description,
	                List<FeedItemMediaLink> feedItemMediaLinks, String link, String feedId) {
		this.title = title;
		this.feedName = feedName;
		this.date = date;
		this.description = description;
		this.feedItemMediaLinks = feedItemMediaLinks;
		this.link = link;
	}

	public String getTitle() {
		return title;
	}

	public String getFeedName() {
		return feedName;
	}

	public Date getDate() {
		return date;
	}

	public String getDescription() {
		return description;
	}

	public List<FeedItemMediaLink> getFeedItemMediaLinks() {
		return feedItemMediaLinks;
	}

	public String getLink() {
		return link;
	}

	public String getFeedId() {
		return feedId;
	}

	public void setFeedId(String feedId) {
		this.feedId = feedId;
	}

	@Override
	public String toString() {
		return "FeedItem{" +
			"feedId='" + feedId + '\'' +
			", title='" + title + '\'' +
			", feedName='" + feedName + '\'' +
			", date=" + date +
			", description='" + description + '\'' +
			", link='" + link + '\'' +
			", feedItemMediaLinks=" + feedItemMediaLinks +
			'}';
	}

	public JsonObject asJsonObject() {
		return new JsonObject() {{
			put("feedId", feedId);
			put("title", title);
			put("feedName", feedName);
			put("date", date.getTime());
			put("description", description);
			put("link", link);
			put("feedItemMediaLinks", new JsonArray(
				feedItemMediaLinks.stream().
					map(FeedItemMediaLink::asJsonObject).
					collect(Collectors.toCollection(LinkedList::new))
			));
		}};

	}
}
