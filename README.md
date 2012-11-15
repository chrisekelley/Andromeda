## Andromeda

Andromeda demonstrates how to manage the user registration and management process using Syncpoint-Android. It is a demo of [TouchDB-Android](https://github.com/couchbaselabs/TouchDB-Android) and [Syncpoint-Android](https://github.com/couchbaselabs/Syncpoint-Android) using <a href="http://incubator.apache.org/projects/callback.html">Apache Cordova (formerly PhoneGap)</a>, [Couchapp](http://couchapp.org), [TodoMVC](https://github.com/addyosmani/todomvc), and [ActionBarSherlock](http://actionbarsherlock.com/). It is an extension of [Android-TouchDB-Cordova](https://github.com/chrisekelley/Android-TouchDB-Cordova#android-couchbase-callback), which is a demonstration of the [TodoMVC backbone-require](https://github.com/addyosmani/todomvc/tree/master/dependency-examples/backbone_require) 
app, which has been modified to work as a [Backbone boilerplate](https://github.com/tbranyen/backbone-boilerplate) project. 
It is based upon [Android-Couchbase-Callback] (https://github.com/couchbaselabs/Android-Couchbase-Callback).

The [TodoMVC](https://github.com/addyosmani/todomvc) project serves as a demonstration of popular javascript MV* frameworks. 
I am using TodoMVC as a generic project called [couchabb](https://github.com/chrisekelley/couchabb) to help assess TouchDB-Android performance. Couchabb also uses [backbone boilerplate](https://github.com/tbranyen/backbone-boilerplate).

The Account Registration screen, which is used for registering the device on the Syncpoint server, uses the [AccountList-Phonegap-Android-Plugin](https://github.com/seltzlab/AccountList-Phonegap-Android-Plugin).

## What does it do?

It demonstrates how to manage the user registration and management process using Syncpoint-Android. After the user registers an email address, the app goes through the process of requesting a user on the remote Syncpoint server. Once the admin approves the user on the Syncpoint server, the app creates the user's database on the local device that will replicate with the remote server. 

This app installs two Couchapps on the mobile device, a Todos task list example and the Mobilefuton Couch admin app. The Todos app is displayed after the user submits the initial registration form.

If you know how to make a Couchapp, or are willing to learn, you could use Andromeda as an Android wrapper for your html code.

## How is this different from previous iterations of your Couch-related Android projects? 

In [Android-Coconut-MobileFuton](https://github.com/chrisekelley/Android-Coconut-MobileFuton), an earlier iteration of this app, I used native Android widgets and code for functions such as Account registration; I'm moving those operations to Cordova, with the goal of coding more HTML and Javascript. 

Rebecca Murphy wrote a great article called [A Baseline for Front-End Developers](http://rmurphey.com/blog/2012/04/12/a-baseline-for-front-end-developers/) that helped push me a bit further into improving my front-end skills. My goal is to incorporate some of these best practices into my workflow and projects.

## Screenshots

![Registration](https://raw.github.com/chrisekelley/Andromeda/master/docs/register_50.png) 
![Registration process](https://raw.github.com/chrisekelley/Andromeda/master/docs/register2_50.png) 
![Home](https://raw.github.com/chrisekelley/Andromeda/master/docs/home_50.png) 

## Quick Test 

If you just want to check out the Andromeda app and how Syncpoint works:
1. Install the [Andromeda APK](https://github.com/chrisekelley/Andromeda/raw/master/bin/Andromeda.apk) on your Android device.
2. Launch Andromeda, choose the account you wish to register.
3. Go to the [Andromeda Syncpoint Admin site](http://andromeda.iriscouch.com/sp_admin/_design/console/index.html) and click the "approval workflow" link on the right side of the page for the account you created. I'm running the Andromeda Admin Syncpoint site as admin party, so you don't need to login. Please be nice.

![Admin Syncpoint approval workflow screenshot](https://raw.github.com/chrisekelley/Andromeda/master/docs/syncpoint_approval_workflow.png) 

## Software Development Requirements

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
2.  Edit res/raw/coconut.properties to point to your CouchDB installation's base URL (including port):

        master_server=http://192.168.0.50:5984/
        
    Important - you must include the server port, even if it's the default http port (80).

3.  Populate your local CouchDB instance using assets/couchabb.json:

        curl -XPUT http://admin:party@localhost:5984/couchabb
        curl -d @assets/couchabb.json -X PUT http://admin:party@localhost:5984/couchabb/_design/couchabb -H "Content-type: application/json"

4. Start syncpoint:

        cd source                                                                                         
        npm start syncpoint

    It will display the url to your Syncpoint Admin Console:

        browse Syncpoint Admin Console at this URL:
        http://localhost:5984/sp_admin/_design/console/index.html

5.  Debug as Android application. I'm currently deploying directly to a Galaxy Nexus running Jellybean.
6.  TouchDB is now running. The account registration screen will appear. Choose the Google account you wish to associate with this app. 
7.  TodoMVC app will display. Sometimes the app will not display; check for the following message:

        05-11 17:36:00.251: D/CordovaLog(2647): Error: Load timeout for modules: use 

    This issue happens in Android 2.2 emulator; Android 2.3 works fine. 

8. If you view logs within LogCat, it will throw an error while it is trying to setup the user database in Syncpoint if it is not available or not pointed to in coconut.properties. 
9. After selecting the account, the log will display:

        V/SyncpointClient(24203): Checking to see if pairing completed...
        V/SyncpointClient(24203): Pairing state is stuck at new

	You may add new records to the Todos app. Go to your Syncpoint Admin Console and approve the new user. See the listing of new users in the right column marked "Needs Admin Approval to Pair."

	When you approve the new user, you'll see all sorts of messages in LogCat. This is setting up local database and the user db on the server. This will also copy any records you already created in the local db to a new db, which will be replicated with the new user's db on the Syncpoint server.

10. Create a new Todo. Watch LogCat. It will push the new record almost immediately. Refresh the Syncpoint Admin Console and click the Channels link for this new user. Click View data. It should have replicated your new record.  

11. There is a refresh button in the title bar that will initiate the sync process. Sometimes continuous replication will cease; this button helps. 

12. The [Mobilefuton](https://github.com/daleharvey/mobilefuton) couchapp is included. In the browser, go to http://localhost:8888/mobilefuton/_design/mobilefuton/index.html


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