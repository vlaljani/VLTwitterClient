package com.codepath.apps.mysimpletweets;

import android.content.Context;

import com.activeandroid.ActiveAndroid;

/*
 * This is the Android application itself and is used to configure various settings
 * including the image cache in memory and on disk. This also adds a singleton
 * for accessing the relevant rest client.
 *
 *     TwitterClient client = TwitterApplication.getRestClient();
 *     // use client to send requests to API
 *
 *     vlaljani: This application is the one that's started up when we hit run/launch app.
 *     See application name attribute. This is a singleton.
 *
 *     Extending com.activeandroid.app.Application allows you to use the built in persistence
 *     which allows you to store data to SQLlite
 *
 *     getRestClient - gives user the TwitterClient that will allow them to access the data using the
 *     API
 *
 */
public class TwitterApplication extends com.activeandroid.app.Application {
	private static Context context;

	public void onCreate() {
		super.onCreate();
		TwitterApplication.context = this;
        ActiveAndroid.initialize(this);
	}

	public static TwitterClient getRestClient() {
		return (TwitterClient) TwitterClient.getInstance(TwitterClient.class,
                                                         TwitterApplication.context);
	}
}