package feedblender;

import com.google.common.base.Strings;
import com.rometools.rome.feed.synd.SyndEnclosure;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import feedblender.model.FeedItem;
import feedblender.model.FeedItemMediaLink;
import feedblender.utils.FeedStorage;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by:
 * User: Paul Rogalinski
 * Date: 16.08.2015
 * Time: 14:50
 */
public class FeedManagerVerticle extends AbstractVerticle {

	public static final String FEED_BUS = "de.codewut.feedblender.feeditem";
	public static final String FEED_ADD = "de.codewut.feedblender.feed.add";
	public static final String FEED_DEL = "de.codewut.feedblender.feed.del";
	public static final String FEED_REFRESH = "de.codewut.feedblender.feed.refresh";

	private static Logger log = LoggerFactory.getLogger(FeedManagerVerticle.class.getName());

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		super.start(startFuture);

		Set<String> feedUrls = FeedStorage.getFeeds();
		EventBus eb = vertx.eventBus();

		eb.consumer(FEED_REFRESH, h -> vertx.executeBlocking(future -> fetchFeeds(feedUrls), result -> {
		}));

		eb.consumer(FEED_ADD, h -> {
			String feedUrl = (String) h.body();
			if (!Strings.isNullOrEmpty(feedUrl)) {
				try {
					// do some basic uri validation
					URI feedUri = new URI(feedUrl);
					if (!feedUri.isAbsolute()) throw new IllegalArgumentException("Invalid feed Uri");
					FeedStorage.addFeed(feedUrl);
					fetchFeeds(new HashSet<String>() {{
						add(feedUrl);
					}});
				} catch (Exception e) {
					log.error("could not add feed", e);
				}
			}
		});

		eb.consumer(FEED_DEL, h -> {
			String feedUrl = (String) h.body();
			FeedStorage.remove(feedUrl);
		});

		// once on boot
		vertx.executeBlocking(blockingHandler -> {
			fetchFeeds(feedUrls);
			blockingHandler.complete();
		}, resultHandler -> {});

		// and once every 30 minutes
		vertx.setPeriodic(1000 * 60 * 30,
			h ->
				vertx.executeBlocking(blockingHandler ->
					fetchFeeds(feedUrls), resultHandler -> {
				}));

	}

	private void fetchFeeds(Set<String> feedUrls) {
		EventBus eb = vertx.eventBus();

		log.debug(String.format("frefreshing %d feeds", feedUrls.size()));

		for (String feedUrl : feedUrls) {
			log.info("refreshing: " + feedUrl);
			SyndFeedInput input = new SyndFeedInput();

			try (InputStream in = (new URL(feedUrl)).openStream()) {

				SyndFeed rssFeed = input.build(new XmlReader(in));
				log.trace("Feed title: " + rssFeed.getTitle());
				for (SyndEntry syndEntry : rssFeed.getEntries()) {
					try {
						List<FeedItemMediaLink> mediaLinks = new ArrayList<>();
						// log.debug("->"+syndEntry.getTitle());

						if (syndEntry.getEnclosures() == null || syndEntry.getEnclosures().isEmpty()) {
							log.trace("no media found for " + syndEntry.getTitle());
							continue;
						} else {
							for (SyndEnclosure syndEnclosure : syndEntry.getEnclosures()) {
								mediaLinks.add(new FeedItemMediaLink(syndEnclosure.getUrl(), syndEnclosure.getType(), syndEnclosure.getLength()));
							}
						}

						eb.publish(FEED_BUS,
							new FeedItem(
								syndEntry.getTitle(), rssFeed.getTitle(), syndEntry.getPublishedDate(),
								syndEntry.getDescription().getValue(), mediaLinks, syndEntry.getLink(), feedUrl));
					} catch (Exception ex) {
						log.error("feed update failed", ex);
					}
				}
			} catch (Exception ex) {
				log.warn("could not update all feeds", ex);
			}
		}

		log.info("All feeds refreshed.");
	}
}