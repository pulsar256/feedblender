package feedblender;

import com.google.common.base.Strings;
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

		EventBus eb = vertx.eventBus();

		eb.consumer(FEED_REFRESH, h -> vertx.executeBlocking(future -> fetchFeeds(FeedStorage.getFeeds()), result -> {}));

		eb.consumer(FEED_ADD, message -> {
			String feedUrl = (String) message.body();
			if (!Strings.isNullOrEmpty(feedUrl)) {
				try {
					if (!new URI(feedUrl).isAbsolute()) throw new IllegalArgumentException("Invalid feed Uri");
					fetchFeeds(new HashSet<String>(){{add(FeedStorage.addFeed(feedUrl));}});
				}
				catch (Exception e) {
					log.error("could not add feed", e);
				}
			}
		});

		eb.consumer(FEED_DEL, message -> FeedStorage.remove((String) message.body()));

		// update feeds right after booting up
		vertx.executeBlocking(blockingHandler -> {
			fetchFeeds(FeedStorage.getFeeds());
			blockingHandler.complete();
		}, resultHandler -> {});

		// schedule periodic feed update
		vertx.setPeriodic(Main.UPDATE_FREQUENCY,
			periodicHandler ->
				vertx.executeBlocking(blockingHandler -> fetchFeeds(FeedStorage.getFeeds()), resultHandler -> {}));
	}

	private void fetchFeeds(Set<String> feedUrls) {
		log.debug(String.format("frefreshing %d feeds", feedUrls.size()));

		feedUrls.stream().forEach(feedUrl -> {
			log.info("refreshing: " + feedUrl);
			try (InputStream in = (new URL(feedUrl)).openStream()) {
				SyndFeed rssFeed = (new SyndFeedInput()).build(new XmlReader(in));
				log.trace("Feed title: " + rssFeed.getTitle());
				rssFeed.getEntries().stream().forEach(entry -> {
					try {
						List<FeedItemMediaLink> mediaLinks = new ArrayList<>();

						if (entry.getEnclosures() == null || entry.getEnclosures().isEmpty()) {
							log.trace("no media found for " + entry.getTitle());
							return;
						}
						else {
							entry.getEnclosures().stream().forEach(enclosure ->
								mediaLinks.add(new FeedItemMediaLink(enclosure.getUrl(), enclosure.getType(), enclosure.getLength())));
						}

						vertx.eventBus().publish(FEED_BUS,
							new FeedItem(entry.getTitle(), rssFeed.getTitle(), entry.getPublishedDate(),
								entry.getDescription().getValue(), mediaLinks, entry.getLink(), feedUrl));
					}
					catch (Exception ex) {
						log.error("feed update failed", ex);
					}
				});
			}
			catch (Exception ex) {
				log.warn("could not update all feeds", ex);
			}
		});

		log.info("All feeds refreshed.");
	}
}