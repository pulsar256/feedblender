package feedblender;

import com.google.gson.Gson;
import feedblender.model.FeedItem;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by:
 * User: Paul Rogalinski
 * Date: 16.08.2015
 * Time: 14:00
 */

public class Main {

	private static Logger log = LoggerFactory.getLogger(Main.class);
	public static int HTTP_BIND_PORT = 8080;
	public static int UPDATE_FREQUENCY = 1000 * 60 * 30;

	public static void main(String[] args) {

		log.info("Usage: java -jar <feedblender.jar> bind-port");
		log.info("Usage: default value for bild-port is 8080");

		if (args.length > 0) {
			HTTP_BIND_PORT = Integer.parseInt(args[0]);
		}

		System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");

		Vertx vertx = Vertx.vertx();
		vertx.eventBus().registerDefaultCodec(FeedItem.class, new MessageCodec<FeedItem, FeedItem>() {
			Gson gson = new Gson();

			@Override
			public void encodeToWire(Buffer buffer, FeedItem feedItem) {
				buffer.appendString(gson.toJson(feedItem));
			}

			@Override
			public FeedItem decodeFromWire(int pos, Buffer buffer) {
				return gson.fromJson(buffer.getString(pos, buffer.length()), FeedItem.class);
			}

			@Override
			public FeedItem transform(FeedItem feedItem) {
				return feedItem;
			}

			@Override
			public String name() {
				return "FeedItemCodec";
			}

			@Override
			public byte systemCodecID() {
				return -1;
			}
		});
		vertx.deployVerticle(new WebServerVerticle());
		vertx.deployVerticle(new FeedManagerVerticle());

		log.info("\n\n" +
			" _____             _ ____  _                _\n" +
			"|  ___|__  ___  __| | __ )| | ___ _ __   __| | ___ _ __\n" +
			"| |_ / _ \\/ _ \\/ _` |  _ \\| |/ _ \\ '_ \\ / _` |/ _ \\ '__|\n" +
			"|  _|  __/  __/ (_| | |_) | |  __/ | | | (_| |  __/ |\n" +
			"|_|  \\___|\\___|\\__,_|____/|_|\\___|_| |_|\\__,_|\\___|_|\n" +
			"\n 1.0 up and running. Use RESTish API, check http://localhost:" + HTTP_BIND_PORT + "/\n\n"+
			String.format("I will check every %d minutes for new feed items",UPDATE_FREQUENCY / 60000));
	}
}