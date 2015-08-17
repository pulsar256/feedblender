# FeedBlender

## What the heck is that?
 Nothing of much interesst, really. It is my crude attempt to HelloWorld the Vertx.io Framework. I am very aware
 of the conflicting concepts in my approach vs. the way Vertx.io does things. Point is: I had to make some
 mistakes to learn from.

## What does it do?
 I have been using Yahoo Pipes to mix my Podcast-Feeds so I could subscribe to only one aggregated feed on my podcatcher.
 Pipes is going away so I wrote my own pipe. So, what does it really do? You can add / remove RSS feeds via the provided
 RESTish API and get a nice, remixed RSS feed sorted by publish date.

## How do I run that thing?

You will need java8 and maven. Then:

```
➜  FeedBlender.io git:(master) ✗ mvn package

(... some garbage ...)
[INFO] BUILD SUCCESS
(... some garbage ...)

➜  FeedBlender.io git:(master) ✗ java -jar feedblender-standalone/target/feedblender-1.0-SNAPSHOT-fat.jar 8080
2015-08-17 00:15:16.259 [main] INFO  f.Main - Usage: java -jar <feedblender.jar> bind-port
2015-08-17 00:15:16.262 [main] INFO  f.Main - Usage: default value for bind-port is 8080
2015-08-17 00:15:16.536 [main] INFO  f.Main -
 _____             _ ____  _                _
|  ___|__  ___  __| | __ )| | ___ _ __   __| | ___ _ __
| |_ / _ \/ _ \/ _` |  _ \| |/ _ \ '_ \ / _` |/ _ \ '__|
|  _|  __/  __/ (_| | |_) | |  __/ | | | (_| |  __/ |
|_|  \___|\___|\__,_|____/|_|\___|_| |_|\__,_|\___|_|

 1.0 up and running. Use RESTish API, check http://localhost:8080/
```

## Can I haz it?
Fork off!

## Manual?
not yet, but this might help:

RESTish Endpoints:

* GET:    localhost:8080/feeds    -> json, currently registered rss feeds
* GET:    localhost:8080/items    -> json, currently registered rss items
* GET:    localhost:8080/feeds.xml -> xml/rss_2.0

* PUT:    localhost:8080/feeds    -> json (see example below, add one or more rss feeds)
* DELETE: localhost:8080/feeds    -> json (see example below, delete one or more rss feeds)

* POST:   localhost:8080/refresh  -> null

Json-Example:
```
{
  "feeds": [
		"http://embedded.fm/episodes?format=rss",
		"http://leoville.tv/podcasts/floss.xml",
		"http://feeds.feedburner.com/headphonecommutepodcast?format=xml"
	]
}
```


## License?

```
This is free and unencumbered software released into the public domain.

Anyone is free to copy, modify, publish, use, compile, sell, or
distribute this software, either in source code form or as a compiled
binary, for any purpose, commercial or non-commercial, and by any
means.

In jurisdictions that recognize copyright laws, the author or authors
of this software dedicate any and all copyright interest in the
software to the public domain. We make this dedication for the benefit
of the public at large and to the detriment of our heirs and
successors. We intend this dedication to be an overt act of
relinquishment in perpetuity of all present and future rights to this
software under copyright law.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
OTHER DEALINGS IN THE SOFTWARE.

For more information, please refer to <http://unlicense.org/>
```
