## Andromeda

Andromeda is a demo of TouchDB and Syncpoint using Cordova, Couchapp, TodoMVC, and ActionBarSherlock. It is an extension of [Android Couchbase Callback](https://github.com/chrisekelley/Android-TouchDB-Cordova#android-couchbase-callback), which is a demonstration of the [TodoMVC backbone-require](https://github.com/addyosmani/todomvc/tree/master/dependency-examples/backbone_require) 
app, which has been modified to work as a [Backbone boilerplate](https://github.com/tbranyen/backbone-boilerplate) project. 
It is based upon [Android-Couchbase-Callback] (https://github.com/couchbaselabs/Android-Couchbase-Callback).

The [TodoMVC](https://github.com/addyosmani/todomvc) project serves as a demonstration of popular javascript MV* frameworks. 
I am using TodoMVC as a generic project called [couchabb](https://github.com/chrisekelley/couchabb) to help assess TouchDB-Android performance. 
 
You may also use this app to deploy a <a href="http://couchapp.org/">CouchApp</a> to an Android device 
using <a href="https://github.com/couchbaselabs/TouchDB-Android">TouchDB-Android</a> and 
<a href="http://incubator.apache.org/projects/callback.html">Apache Cordova (formerly PhoneGap)</a>.

## Requirements

This project requires the latest version of the Android SDK. If you already have the SDK tools, 
you can upgrade by running `android update sdk`, if you don't have them, you can 
[install via this link](http://developer.android.com/sdk/installing.html)

You also must have CouchDB and [Syncpoint](http://www.couchbase.com/wiki/display/couchbase/Mobile+Syncpoint) installed.

## Getting Started

These instructions are divided into two sections, the first describes the development mode.  
In this mode you can continually couchapp push your changes in for test.  The second describes distribution mode where you package your application for distribution.

### Development

My instructions are for Eclipse. There are instructions for non-Eclipse users in the  [Android-Couchbase-Callback] (https://github.com/couchbaselabs/Android-Couchbase-Callback#getting-started) README.

1.  Clone this repository
2.  Edit res/raw/coconut.properties to point to your Syncpoint installation:

        master_server=http://192.168.0.50:5984/

3.  Populate your local CouchDB instance using assets/couchabb.json:

        curl -XPUT http://admin:party@localhost:5984/couchabb
        curl -d @assets/couchabb.json -X PUT http://admin:party@localhost:5984/couchabb/_design/couchabb -H "Content-type: application/json"

4. Start syncpoint:

        [chrisk@mbp:~]$ cd source                                                                                         
        [chrisk@mbp:~/source]$ npm start syncpoint

    It will display the url to your Syncpoint Admin Console:

        browse Syncpoint Admin Console at this URL:
        http://localhost:5984/sp_admin/_design/console/index.html

5.  Debug as Android application. I'm currently deploying directly to a Galaxy Nexus running Jellybean.
6.  TouchDB is now running. The account registration screen will appear. Choose the Google account you wish to associate with this app. 
7.  [TodoMVC](https://github.com/addyosmani/todomvc) app will display. Sometimes the app will not display; check for the following message:

        05-11 17:36:00.251: D/CordovaLog(2647): Error: Load timeout for modules: use 

    This issue happens in Android 2.2 emulator; Android 2.3 works fine. 

8. If you view logs within LogCat, it will throw an error while it is trying to setup the user database in Syncpoint if it is not available or not pointed to in coconut.properties. 
9. After selecting the account, the log will display:

        V/SyncpointClient(24203): Checking to see if pairing completed...
        V/SyncpointClient(24203): Pairing state is stuck at new

	Go to your Syncpoint Admin Console and approve the new user. See the listing of new users in the right column marked "Needs Admin Approval to Pair."

	When you approve the new user, you'll see all sorts of messages in LogCat. This is setting up local database and the user db on the server.

10. Create a new Todo. Watch LogCat. It will push the new record almost immediately. Refresh the Syncpoint Admin Console and click the Channels link for this new user. Click View data. It should have replicated your new record.  


### Distribution

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

    adb uninstall com.couchbase.callback

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