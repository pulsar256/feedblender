package feedblender.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Set;
import java.util.prefs.Preferences;

/**
 * Created by:
 * User: Paul Rogalinski
 * Date: 16.08.2015
 * Time: 20:36
 */
public class FeedStorage {

	private static Preferences prefs = Preferences.userRoot().node(FeedStorage.class.getName());
	private static Gson gson = new Gson();
	private static Type listType = new TypeToken<Set<String>>(){}.getType();
	public static final String PREFS_FEED_URL = "feedUrls";

	public static void addFeed(String feed) {
		try{
			Set<String> feeds = getFeeds();
			feeds.add(feed);
			prefs.put(PREFS_FEED_URL, gson.toJson(feeds));
			prefs.sync();
		}
		catch (Exception ex){
			throw new IllegalStateException("Could not save preferences");
		}
	}

	public static void remove(String feed) {
		try{
			Set<String> feeds = getFeeds();
			feeds.remove(feed);
			prefs.put(PREFS_FEED_URL, gson.toJson(feeds));
			prefs.sync();
		}
		catch (Exception ex){
			throw new IllegalStateException("Could not save preferences");
		}
	}

	public static Set<String> getFeeds(){
		return gson.fromJson(prefs.get(PREFS_FEED_URL, "[]"), listType);
	}

}
