# Twitter Client

This is an Android application for a simple Twitter Client, which allows a user to view a Twitter timeline and compose a tweet.

Time Spent: ~20 - 22 hours 
Completed user stories:

 * [x] Required: User can sign in to Twitter using OAuth login.
 * [x] Required: User can view the tweets from their home timeline:
       * User is displayed the username, name, and body for each tweet
       * User is displayed the [relative timestamp](https://gist.github.com/nesquena/f786232f5ef72f6e10a7) for each tweet "8m", "7h"
       * User can view more tweets as they scroll with [infinite pagination](http://guides.codepath.com/android/Endless-Scrolling-with-AdapterViews)
       * Links in tweets are clickable and will launch the web browser
 * [x] Required: User can compose a tweet
       * User can click a “Compose” icon in the Action Bar on the top right
       * User can then enter a new tweet and post this to twitter
       * User is taken back to home timeline with new tweet visible in timeline
       * User can see a counter with total number of characters left for tweet
 * [x] Advanced: User can refresh tweets timeline by [pulling down to refresh](http://guides.codepath.com/android/Implementing-Pull-to-Refresh-Guide)
 * [x] Advanced: User can open the twitter app offline and see last loaded tweets (Tweets are persisted into sqlite and can be displayed from the local DB) 
 * [x] Advanced: User can tap a tweet to display a "detailed" view of that tweet
 * [x] Advanced: User can select "reply" from detail view to respond to a tweet
 * [x] Advanced:Improve the user interface and theme the app to feel "twitter branded"
       * Login screen resembles the Twitter login screen
         - World map background
         - Login to Twitter button with the Twitter color and bird icon
         - Twitter caption (Follow your interests...) 
       * Compose tweet dialog screen has the Twitter colors (and looks pretty close to the actual app)
         - The tweet button is Twitter-blue and has rounded corners
         - It's disabled and changes color accordingly when <= 0 characters or >= 140 
       * Reply dialog shows the @ tag upon opening 
       * The detailed view has been laid out (including buttons etc) to look like the Twitter client too. 
 * [x] Bonus: User can see embedded image media within the tweet detail view
 * [x] Bonus: Compose activity is replaced with a modal overlay 
 
Libraries that helped make the project easier :-)

[Android async-http](http://loopj.com/android-async-http/) to make calls to the Google API

[Picasso](http://square.github.io/picasso/) to help load the images

[ActiveAndroid](https://github.com/pardom/ActiveAndroid/wiki/Getting-started) to help persist to SQLLite DB

Codepath jars for OAuth

Video Walk Through: 

![VideoWalkThrough](VideoWalkThroughForTwitterClient1.gif)

Video Walk Through in offline mode: 
![VideoWalkThroughOffline]
