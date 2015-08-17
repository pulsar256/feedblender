package feedblender;

import com.rometools.rome.feed.synd.SyndContentImpl;
import com.rometools.rome.feed.synd.SyndEnclosureImpl;
import com.rometools.rome.feed.synd.SyndEntryImpl;
import com.rometools.rome.feed.synd.SyndFeedImpl;
import com.rometools.rome.io.SyndFeedOutput;
import feedblender.model.FeedItem;
import feedblender.utils.FeedStorage;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.util.LinkedList;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Created by:
 * User: Paul Rogalinski
 * Date: 16.08.2015
 * Time: 15:00
 */
public class WebServerVerticle extends AbstractVerticle {

	public static final String APPLICATION_JSON = "application/json";
	public static final String TEXT_XML = "text/xml";

	private TreeSet<FeedItem> data = new TreeSet<FeedItem>((o1, o2) -> o2.getDate().compareTo(o1.getDate())) {
		@Override
		public boolean add(FeedItem feedItem) {
			boolean b = super.add(feedItem);
			while (size() > 500) remove(last()); // cap the set to 500 elements
			return b;
		}
	};

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		Router router = Router.router(vertx);

		router.route().handler(BodyHandler.create());

		vertx.eventBus().consumer(FeedManagerVerticle.FEED_BUS, message -> data.add((FeedItem) message.body()));

		router.get().path("/items").produces(APPLICATION_JSON).handler(this::getItems);
		router.get().path("/feeds").produces(APPLICATION_JSON).handler(this::getFeeds);
		router.get().pathRegex("/feeds?\\.xml").produces(TEXT_XML).handler(this::getAggregatedFeed);
		router.post().path("/feeds").consumes(APPLICATION_JSON).produces(APPLICATION_JSON).handler(this::putFeeds);
		router.post().path("/refresh").produces(APPLICATION_JSON).handler(this::refresh);
		router.delete().path("/feeds").consumes(APPLICATION_JSON).produces(APPLICATION_JSON).handler(this::deleteFeeds);
		router.route().handler(StaticHandler.create());

		vertx.createHttpServer().requestHandler(router::accept).listen(Main.HTTP_BIND_PORT);
	}

	private void refresh(RoutingContext ctx) {
		vertx.eventBus().publish(FeedManagerVerticle.FEED_REFRESH, new Date().getTime());
		ctx.response().end();
	}

	private void getItems(RoutingContext ctx) {
		ctx.response().
			setChunked(true).
			putHeader("content-type", APPLICATION_JSON).
			end(new JsonObject() {{
				put("items", data.stream().map(FeedItem::asJsonObject).collect(Collectors.toCollection(LinkedList::new)));
			}}.encode());
	}

	private void getFeeds(RoutingContext ctx) {
		ctx.response().setChunked(true).
			putHeader("content-type", APPLICATION_JSON).
			end(new JsonObject() {{
				put("feeds", new LinkedList<>(FeedStorage.getFeeds()));
			}}.encode());
	}

	private void deleteFeeds(RoutingContext ctx) {
		HttpServerResponse response = ctx.response().
			setChunked(true).
			putHeader("content-type", APPLICATION_JSON);

		JsonObject feeds = ctx.getBodyAsJson();
		if (feeds.getJsonArray("feeds") == null) {
			invalidFeedsRequest(response);
		}
		else {
			feeds.getJsonArray("feeds").stream().forEach(feed ->
				vertx.eventBus().publish(FeedManagerVerticle.FEED_DEL, feed));
			ctx.response().end(feeds.encode());
		}
	}

	private void putFeeds(RoutingContext ctx) {
		HttpServerResponse response = ctx.response().
			setChunked(true).
			putHeader("content-type", APPLICATION_JSON);

		JsonObject feeds = ctx.getBodyAsJson();
		if (feeds.getJsonArray("feeds") == null) {
			invalidFeedsRequest(response);
		}
		else {
			feeds.getJsonArray("feeds").stream().forEach(feed ->
				vertx.eventBus().publish(FeedManagerVerticle.FEED_ADD, feed));
			ctx.response().end(feeds.encode());
		}
	}

	private void getAggregatedFeed(RoutingContext ctx) {
		ctx.response().
			setChunked(true).
			putHeader("content-type", TEXT_XML);
		try {
			new SyndFeedOutput().output(
				new SyndFeedImpl() {{
					setFeedType("rss_2.0");
					setTitle("Aggregated Feed");
					setAuthor("me");
					setLink("http://www.codewut.de");
					setDescription("Feed Aggregation Foo!");
					setEntries(data.stream().map(feedItem -> new SyndEntryImpl() {{
						setTitle(String.format("%s: %s", feedItem.getFeedName(), feedItem.getTitle()));
						setPublishedDate(feedItem.getDate());
						setLink(feedItem.getLink());
						setDescription(new SyndContentImpl() {{
							setValue(feedItem.getDescription());
							setType("text/html"); // hack
							setMode("text/html"); // hack
						}});
						setEnclosures(feedItem.getFeedItemMediaLinks().stream().map(
							feedItemMediaLink -> new SyndEnclosureImpl() {{
								setType(feedItemMediaLink.getType());
								setLength(feedItemMediaLink.getLength());
								setUrl(feedItemMediaLink.getLink());
							}}).collect(Collectors.toCollection(LinkedList::new)));
					}}).collect(Collectors.toCollection(LinkedList::new)));
				}},
				new Writer() {
					@Override
					public void write(char[] cbuf, int off, int len) throws IOException {
						ctx.response().write(new String(cbuf, off, len));
					}

					@Override
					public void flush() throws IOException {}

					@Override
					public void close() throws IOException {}
				});
		}
		catch (Throwable th) {
			throw new IllegalStateException(th);
		}
		finally {
			ctx.response().end();
		}
	}


	private void invalidFeedsRequest(HttpServerResponse response) {
		response.setStatusCode(400);
		response.end(new JsonObject() {{
			put("error", "Invalid data");
			put("expected", new JsonObject() {{
				put("feeds", new LinkedList<String>() {{
					add("http://feed1");
					add("http://feed2");
					add("http://feedn");
				}});
			}});
		}}.encode());
	}
}
