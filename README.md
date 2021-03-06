## Andromeda

Andromeda is an Android app that runs a chat example application that demonstrates user signup, login, and data synchronization using using Couchbase Sync Gateway and Couchbase Server on the backend. 

Quick setup:

1. launch Couchbase
2. Install Sync Gateway and configure. Launch it in Terminal by entering:
	sync_gateway
3. In another terminal tab, cd to your project source code dir. 
4. Enter:
	SYNC_HOSTNAME=192.168.0.60 node assets/server/serve.js


## Elements of Andromeda
Andromeda is a demo of [TouchDB-Android](https://github.com/couchbaselabs/TouchDB-Android) and [Sync Gateway](https://github.com/couchbaselabs/sync_gateway) using <a href="http://incubator.apache.org/projects/callback.html">Apache Cordova (formerly PhoneGap)</a>, [Couchapp](http://couchapp.org), the chat example from [CouchbaseGap](https://github.com/couchbaselabs/CouchbaseGap.git), and [ActionBarSherlock](http://actionbarsherlock.com/). It is an extension of [Android-TouchDB-Cordova](https://github.com/chrisekelley/Android-TouchDB-Cordova#android-couchbase-callback), which hosts a demonstration of the [TodoMVC backbone-require](https://github.com/addyosmani/todomvc/tree/master/dependency-examples/backbone_require) 
app, which has been modified to work as a [Backbone boilerplate](https://github.com/tbranyen/backbone-boilerplate) project. 
It is based upon [Android-Couchbase-Callback] (https://github.com/couchbaselabs/Android-Couchbase-Callback).

The [TodoMVC](https://github.com/addyosmani/todomvc) project serves as a demonstration of popular javascript MV* frameworks. 
I am using TodoMVC as a generic project called [couchabb](https://github.com/chrisekelley/couchabb) to help assess TouchDB-Android performance. Couchabb also uses [backbone boilerplate](https://github.com/tbranyen/backbone-boilerplate). The current version does not have couchabb installed, but will soon.

## What does it do?

It runs a chat example application that demonstrates how user signup, login, and data synchronization using Couchbase Sync Gateway. 

If you know how to make a Couchapp, or are willing to learn, you could use Andromeda as an Android wrapper for your html code.

## How is this different from previous iterations of your Couch-related Android projects? 

In [Android-Coconut-MobileFuton](https://github.com/chrisekelley/Android-Coconut-MobileFuton), an earlier iteration of this app, I used native Android widgets and code for functions such as Account registration; I'm moving those operations to Cordova, with the goal of coding more HTML and Javascript. 

Rebecca Murphy wrote a great article called [A Baseline for Front-End Developers](http://rmurphey.com/blog/2012/04/12/a-baseline-for-front-end-developers/) that helped push me a bit further into improving my front-end skills. My goal is to incorporate some of these best practices into my workflow and projects.

## Software Development Requirements

This project requires the latest version of the Android SDK. If you already have the SDK tools, 
you can upgrade by running `android update sdk`, if you don't have them, you can 
[install via this link](http://developer.android.com/sdk/installing.html)

You also must have CouchDB and [Syncpoint](http://www.couchbase.com/wiki/display/couchbase/Mobile+Syncpoint) installed.


## Distribution

These are tips that will help when you package your own app.

1.  Copy the database off the device and into this Android application's assets directory. To keep the filesystem clean, first create a directory named your_couchapp in your project assets dir. In the console, cd to that directory. Run the following commmand:

	    adb pull /data/data/com.couchbase.callback/files/your_couchapp
	
    This should have put an attachments directory in that directory. 

2. Now cd up one directory (to your assets dir) and run the following command:
	
	    adb pull /data/data/com.couchbase.callback/files/your_couchapp.touchdb
    
3. Edit res/raw/coconut.properties and change coconut-sample to the name of your couchapp and adjust the couchAppInstanceUrl. Note that you can also change the port in this file.

        app_db=coconut-sample
        couchAppInstanceUrl=_design/coconut/index.html    
	
3.  Repackage your application with the database file included

        ant debug

4.  Reinstall the application to launch the CouchApp

        adb uninstall com.kinotel.andromeda

        adb install bin/AndroidCouchbaseCallback-debug.apk

        adb shell am start -n com.couchbase.callback/.AndroidCouchbaseCallback


## Assumptions

A few assumptions are currently made to reduce the number of options that must be configured to get started.  Currently these can only be changed by modifying the code.

-  The name of the database can be anything (couchapp is used in the examples above).  BUT, the design document must have the same name.
    
## Further Customizations

*  Change the name and package of your application

    Refactor name and package of ExampleAppActivity to suit your needs

*  Provide your own custom splash screen

    Override the getSplashScreenDrawable() method to point to your splash screen image

## Frequently Asked Questions

Q: When I start my application the splash screen shows for a long time, then I get the message "Application Error: The connection to the server was unsuccessful."  In the background behind this message I now see my application.  But when I press OK, the application exits.  What is going on?

A: Most likely your application is loading a resource (something like the _chagnes feed) and this causes the PhoneGap container to fail to recognize that the page has loaded.  The fix is simple, add a function to your application that listens for the "deviceReady" event and start your work after this event fires.  For example:

    document.addEventListener("deviceready", onDeviceReady, false);
    
    function onDeviceReady() {
        //  start listenting to changes feed here
    }

We are still looking for a better approach to this problem.

## TODO

* spoof a call to `onPageFinished` so that phonegap paints the screen even if a long ajax call happens before onload.

## License

Portions under Apache, Erlang, and other licenses.

The overall package is released under the Apache license, 2.0.

Copyright 2011-2012, Couchbase, Inc.