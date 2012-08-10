package com.kinotel.andromeda;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaWebViewClient;
import org.apache.cordova.DroidGap;
import org.apache.cordova.api.CordovaInterface;
import org.apache.cordova.api.IPlugin;
import org.apache.cordova.api.LOG;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.DbAccessException;
import org.ektorp.android.http.AndroidHttpClient;
import org.ektorp.android.util.EktorpAsyncTask;
import org.ektorp.http.HttpClient;
import org.ektorp.impl.StdCouchDbInstance;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.byarger.exchangeit.EasySSLSocketFactory;
import com.couchbase.syncpoint.impl.SyncpointClientImpl;
import com.couchbase.syncpoint.impl.SyncpointModelFactory;
import com.couchbase.syncpoint.model.PairingUser;
import com.couchbase.syncpoint.model.SyncpointChannel;
import com.couchbase.syncpoint.model.SyncpointInstallation;
import com.couchbase.syncpoint.model.SyncpointSession;
import com.couchbase.touchdb.TDServer;
import com.couchbase.touchdb.TDView;
import com.couchbase.touchdb.ektorp.TouchDBHttpClient;
import com.couchbase.touchdb.javascript.TDJavaScriptViewCompiler;
import com.couchbase.touchdb.listener.TDListener;
import com.couchbase.touchdb.replicator.TDReplicator;
import com.couchbase.touchdb.support.HttpClientFactory;

public class MainActivity extends SherlockActivity implements CordovaInterface {

	String TAG = "MainActivity-Andromeda";
	
	CordovaWebView mainView;
	
	private static final int ACTIVITY_ACCOUNTS = 1;
    public static final String COUCHBASE_DATABASE_SUFFIX = ".couch";
    public static final String TOUCHDB_DATABASE_SUFFIX = ".touchdb";
	
    private IPlugin activityResultCallback;
    private Object activityResultKeepRunning;
    private Object keepRunning;
    private ProgressDialog progressDialog;
    private SessionData sessionData = new SessionData(null);
    
    private static MainActivity activityRef;
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {


    	menu.add("Refresh")
    	.setIcon( R.drawable.ic_refresh)
    	.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

    	return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	//This uses the imported MenuItem from ActionBarSherlock
    	Toast.makeText(this, "Got click: " + item.toString(), Toast.LENGTH_SHORT).show();
    	sessionData.setSyncpoint(new SyncpointClientImpl(getApplicationContext(), sessionData.getLocalServer(), sessionData.getMasterServerUrl(), Constants.syncpointAppId));
    	return true;
    }


	@SuppressLint("NewApi")
	@Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); 
        mainView =  (CordovaWebView) findViewById(R.id.mainView);
        //mainView.clearCache(true);
        //mainView.clearHistory();
        //mainView.setWebChromeClient(new CustomWebViewClient());
        //mainView.setWebViewClient(new CustomWebViewClient(this));
        mainView.setWebViewClient(new CustomCordovaWebViewClient(this, mainView));
        /*mainView.setWebViewClient(new CordovaWebViewClient(this){

        	public boolean shouldOverrideUrlLoading(final WebView view, String url) {
        		Log.i("BugTest", "shouldOverrideUrlLoading: " + url);
        		return true;
        	}
        	@Override
        	public void onPageStarted(WebView view, String url, Bitmap favicon) {
        		super.onPageStarted(view, url, favicon);
        		Log.i("BugTest", "onPageStarted: " + url);
        	}
        	@Override
        	public void onPageFinished(WebView view, String url) {
        		super.onPageFinished(view, url);
        		Log.i("BugTest", "onPageFinished: " + url);
        	}
        });*/
        activityRef = this;
        //mainView.loadUrl("file:///android_asset/www/blank.html");

        if (android.os.Build.VERSION.SDK_INT > 9) {  
        	StrictMode.VmPolicy vmpolicy = new StrictMode.VmPolicy.Builder().penaltyLog().build();
        	StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        	StrictMode.setThreadPolicy(policy);
        	StrictMode.setVmPolicy(vmpolicy);
        }
        
        // try to keep app from reloading everything when orientation is changed.
        /*if (savedInstanceState != null) {
            // Restore last state for checked position.
        	sessionData = (SessionData) savedInstanceState.getSerializable("sessionData");
        } else {*/
        //if (sessionData.getSession() == null) {
        	sessionData.setFilesDir(getFilesDir().getAbsolutePath());
        	Properties properties = new Properties();

        	try {
        		InputStream rawResource = getResources().openRawResource(R.raw.coconut);
        		properties.load(rawResource);
        	} catch (Resources.NotFoundException e) {
        		System.err.println("Did not find raw resource: " + e);
        	} catch (IOException e) {
        		System.err.println("Failed to open property file");
        	}

        	try {
        		sessionData.setServer(new TDServer(sessionData.getFilesDir()));
        		//server.setDefaultHttpClientFactory(defaultHttpClientFactory);

        		sessionData.getServer().setDefaultHttpClientFactory(new HttpClientFactory() {

        			@Override
        			public org.apache.http.client.HttpClient getHttpClient() {
        				DefaultHttpClient httpClient = new DefaultHttpClient();
        				// to enable self-signed SSL certs.
        				httpClient.getConnectionManager().getSchemeRegistry().register(new Scheme("https", new EasySSLSocketFactory(), 6984));
        				return httpClient;
        			}
        		});
        		sessionData.setListener(new TDListener(sessionData.getServer(), 8888));
        		sessionData.getListener().start();
        		TDView.setCompiler(new TDJavaScriptViewCompiler());
        	} catch (IOException e) {
        		Log.e(TAG, "Unable to create TDServer", e);
        	}

        	String ipAddress = "0.0.0.0";
        	Log.d(TAG, ipAddress);
        	String host = ipAddress;
        	int port = 8888;
        	sessionData.setUrl("http://" + host + ":" + Integer.toString(port) + "/");

        	//uiHandler = new Handler();
        	sessionData.setAppDb(properties.getProperty("app_db"));
        	sessionData.setCouchAppInstanceUrl(properties.getProperty("couchAppInstanceUrl"));
        	File destination = new File(sessionData.getFilesDir() + File.separator + sessionData.getAppDb() + TOUCHDB_DATABASE_SUFFIX);
        	String masterServer = properties.getProperty("master_server");
        	if (masterServer != null) {
        		Constants.serverURLString = masterServer;
        		//Constants.replicationURL = masterServer + "/" + appDb;
        		Log.d(TAG, "Disabled Constants.replicationURL: no replication.");
        		//Log.d(TAG, "replicationURL: " + Constants.replicationURL);
        	}

        	String syncpointAppId = properties.getProperty("syncpoint_app_id");
        	if (syncpointAppId != null) {
        		Constants.syncpointAppId = syncpointAppId;
        	}
        	String syncpointDefaultChannelName = properties.getProperty("syncpoint_default_channel");
        	if (syncpointDefaultChannelName != null) {
        		Constants.syncpointDefaultChannelName = syncpointDefaultChannelName;
        	}

        	Log.d(TAG, "Checking for touchdb at " + sessionData.getFilesDir() + File.separator + sessionData.getAppDb() + TOUCHDB_DATABASE_SUFFIX);
        	if (!destination.exists()) {
        		Log.d(TAG, "Touchdb does not exist. Installing.");
        		// must be in the assets directory
        		try {
        			//db.replaceWithDatabase(appDb + TOUCHDB_DATABASE_SUFFIX, appDb);
        			AssetManager assetManager = this.getAssets();
        			// This is the appDb touchdb
        			CoconutUtils.copyFileOrDir(assetManager, sessionData.getAppDb() + TOUCHDB_DATABASE_SUFFIX, sessionData.getFilesDir());
        			// These are the appDb attachments
        			CoconutUtils.copyFileOrDir(assetManager, sessionData.getAppDb(), sessionData.getFilesDir());
        			// This is the mobilefuton touchdb
        			CoconutUtils.copyFileOrDir(assetManager, "mobilefuton" + TOUCHDB_DATABASE_SUFFIX, sessionData.getFilesDir());
        			// These are the mobilefuton attachments
        			CoconutUtils.copyFileOrDir(assetManager, "mobilefuton", sessionData.getFilesDir());
        		} catch (Exception e) {
        			e.printStackTrace();
        			String errorMessage = "There was an error extracting the database.";
        			//displayLargeMessage(errorMessage, "big");
        			Log.d(TAG, errorMessage);
        			//progressDialog.setMessage(errorMessage);
        			mainView.loadUrl("file:///android_asset/www/error.html");
        		}
        	} else {
        		Log.d(TAG, "Touchdb exists. Checking Syncpoint status.");	    	
        	}

        	//** Syncpoint	**//*

        	try {
        		sessionData.setMasterServerUrl(new URL(masterServer));
        	} catch (MalformedURLException e) {
        		e.printStackTrace();
        	}
        	Log.d(TAG, "Syncpoint masterServerUrl: " + sessionData.getMasterServerUrl());
        	try {
        		// Check if there is already a pairing session in-use.
        		HttpClient httpClient = new TouchDBHttpClient(sessionData.getServer());
        		CouchDbInstance localServer = new StdCouchDbInstance(httpClient);
        		sessionData.setLocalServer(localServer);
        		//CouchDbConnector userDb = localServer.createConnector("_users", false);
        		sessionData.setLocalControlDatabase(localServer.createConnector(SyncpointClientImpl.LOCAL_CONTROL_DATABASE_NAME, false));
        		sessionData.setSession(SyncpointSession.sessionInDatabase(getApplicationContext(), localServer, sessionData.getLocalControlDatabase()));
        		if((sessionData.getSession()!= null) && (!sessionData.getSession().isPaired())) {
        			final PairingUser pairingUser = sessionData.getSession().getPairingUser();		    		
        			if ((pairingUser != null) && (sessionData.getSelectedAccount() != null) && (sessionData.getSession().isReadyToPair())) {
        				Log.v(TAG, "Pairing still in-progress");

        				HttpClient remoteHttpClient = new AndroidHttpClient.Builder().url(sessionData.getMasterServerUrl()).relaxedSSLSettings(true)
        						.username(sessionData.getSession().getPairingCreds().getUsername()).password(sessionData.getSession().getPairingCreds().getPassword()).maxConnections(100).build();

        				//.url(masterServerUrl).username(session.getPairingCreds().getUsername()).password(session.getPairingCreds().getPassword()).maxConnections(100).build();
        				CouchDbInstance remote = new StdCouchDbInstance(remoteHttpClient);
        				final CouchDbConnector userDb = remote.createConnector("_users", false);
        				EktorpAsyncTask task = new EktorpAsyncTask() {

        					PairingUser result = null;

        					@Override
        					protected void doInBackground() {
        						result = userDb.get(PairingUser.class, pairingUser.getId());
        					}

        					@Override
        					protected void onSuccess() {
        						waitForPairingToComplete(userDb, result);
        					}

        				};

        				task.execute();
        			} else {
        				sessionData.setSyncpoint(new SyncpointClientImpl(getApplicationContext(), localServer, sessionData.getMasterServerUrl(), Constants.syncpointAppId));
        			}
        		} else {
        			sessionData.setSyncpoint(new SyncpointClientImpl(getApplicationContext(), localServer, sessionData.getMasterServerUrl(), Constants.syncpointAppId));
        		}
        	} catch (org.ektorp.UpdateConflictException e1) {
        		Log.v(TAG, "Error: " + e1);
        	} catch (DbAccessException e1) {
        		Log.e( TAG, "Error: " , e1);
        		e1.printStackTrace();
        		Toast.makeText(this, "Error: Unable to connect to Syncpoint Server: " + e1.getMessage(), Toast.LENGTH_LONG).show();
        	}
        //}
	    	
        SyncpointChannel channel = null;
        if (sessionData.getSession() != null) {
        	channel = sessionData.getSession().getMyChannel(Constants.syncpointDefaultChannelName);
        } else {
        	if (sessionData.getSyncpoint()!= null) {
        		channel = sessionData.getSyncpoint().getMyChannel(Constants.syncpointDefaultChannelName);
        	}
        }
        
        SyncpointInstallation syncpointInstallation = channel.getInstallation(getApplicationContext());
        sessionData.setSyncpointInstallation(syncpointInstallation);
        
        if(syncpointInstallation != null) {
        	CouchDbConnector localDatabase = syncpointInstallation.getLocalDatabase(getApplicationContext());
        	String localDatabaseName = localDatabase.getDatabaseName();
        	Log.v(TAG, "localDatabaseName: " + localDatabaseName);
        	//TDDatabase origDb = server.getDatabaseNamed(appDb);
        	//origDb.open();
        	sessionData.setNewDb(sessionData.getServer().getDatabaseNamed(localDatabaseName));
        	sessionData.getNewDb().open();

        	long designDocId = sessionData.getNewDb().getDocNumericID("_design/couchabb");

        	if (designDocId < 1) {
        		/*
        		 * URL localCouchappUrl = null;
	    			try {
	    				localCouchappUrl = new URL(url + appDb);
	    			} catch (MalformedURLException e) {
	    				// TODO Auto-generated catch block
	    				e.printStackTrace();
	    			}
	    			TDReplicator replPull = newDb.getReplicator(localCouchappUrl, false, false);
	    			replPull.start();*/
        		try {
        			sessionData.getNewDb().replaceWithDatabase(sessionData.getFilesDir() + "/" + sessionData.getAppDb(),  sessionData.getFilesDir() + "/" + sessionData.getAppDb() + TOUCHDB_DATABASE_SUFFIX);
        		} catch (IOException e) {
        			// TODO Auto-generated catch block
        			e.printStackTrace();
        		}
        		// show signup progress indicator
        		//startSignupProcessActivity();
        		startAccountSelector();
        	} else {
        		sessionData.setLocalSyncpointDbName(localDatabaseName);
        		String couchAppUrl = sessionData.getUrl() + sessionData.getAppDb() + "/" + sessionData.getCouchAppInstanceUrl();
        		if (sessionData.getNewDb() != null) {
        			couchAppUrl = sessionData.getUrl() + sessionData.getLocalSyncpointDbName() + "/" + sessionData.getCouchAppInstanceUrl();
        		}
        		sessionData.setCouchAppUrl(couchAppUrl);
        		Log.d( TAG, "Loading couchAppUrl: " + couchAppUrl );
        		//AndroidCouchbaseCallback.this.loadUrl(couchAppUrl);
        		//setContentView(R.layout.main);
        		//mainView =  (CordovaWebView) findViewById(R.id.mainView);
        		mainView.loadUrl(sessionData.getCouchAppUrl());
        		//mainView.setWebViewClient(new CustomCordovaWebViewClient(this));
        	}
        } else {
        	startAccountSelector();
        }
	    
        //mainView.loadUrl("file:///android_asset/www/index.html");
    }
	
	private class CustomWebViewClient extends CordovaWebViewClient {
	/*	public CustomWebViewClient(CordovaInterface cordova) {
			super(cordova);
		}*/
		
		public CustomWebViewClient(DroidGap ctx) {
			super(ctx);
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			if (url.startsWith("tel:")) {
				Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
				startActivity(intent);
			} else if (url.startsWith("http:") || url.startsWith("https:")) {
				view.loadUrl(url);
			}
			return true;
		}
		
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
			Log.i("BugTest", "onPageStarted: " + url);
		}
		
		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			Log.i("BugTest", "onPageFinished: " + url);
            if (MainActivity.this.progressDialog != null) {
           	 MainActivity.this.progressDialog.dismiss();
            }
        }
		
		 @Override
		 public void doUpdateVisitedHistory(WebView view, String url, boolean isReload){  
		     super.doUpdateVisitedHistory(view, url, isReload);  
		 }

		 @Override
		 public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
		     super.onReceivedError(view, errorCode, description, failingUrl);
		 }
		
	}
	
	public class CustomCordovaWebViewClient extends CordovaWebViewClient {

		 public CustomCordovaWebViewClient(CordovaInterface ctx, CordovaWebView view) {
		   super(ctx, view);
		 }

		 @Override
		 public void onPageStarted(WebView view, String url, Bitmap bitmap) {
		   super.onPageStarted(view, url, bitmap);
		   Log.i("TEST", "onPageStarted: " + url);
		 }

		 @Override
		 public void onPageFinished(WebView view, String url) {
		   super.onPageFinished(view, url);
		   Log.i("TEST", "onPageFinished: " + url);
		   if (MainActivity.this.progressDialog != null) {
			   MainActivity.this.progressDialog.dismiss();
		   }
		 }

		 @Override
		 public void doUpdateVisitedHistory(WebView view, String url, boolean isReload){  
		     super.doUpdateVisitedHistory(view, url, isReload);  
		 }

		 @Override
		 public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
		     super.onReceivedError(view, errorCode, description, failingUrl);
		 }

		}
	

	@Override
	@Deprecated
	public void cancelLoadUrl() {
		// This is a no-op.
		
	}

	@Override
	public Activity getActivity() {
		return this;
	}

	@Override
	@Deprecated
	public Context getContext() {
		return getApplicationContext();
	}

	@Override
	public Object onMessage(String id, Object data) {
		LOG.d(TAG, "onMessage(" + id + "," + data + ")");
    	if ("exit".equals(id)) {
    		super.finish();
    	} else if ("account".equals(id)) {
    		final String accountName = data.toString();
    		AccountManager accountManager = AccountManager.get( this );
    		sessionData.setSelectedAccount(AccountUtils.getAccountFromAccountName(accountManager, accountName));
    		this.getActivity().runOnUiThread(new Runnable() {
    			public void run() {
    				Context context = getApplicationContext();
    				//Toast.makeText(activity, "Hello", Toast.LENGTH_SHORT).show();
    				Toast.makeText(context, "Account selected: "+accountName, Toast.LENGTH_SHORT).show();
    				if( sessionData.getSelectedAccount() != null ) {
    	    			register();
    	    			//startSignupProcessActivity();
    	    		}	
    			}
    		});	
    	} 
    	return null;
	}

	 @Override
	    public void setActivityResultCallback(IPlugin plugin) {
	        this.activityResultCallback = plugin;        
	    }
	    /**
	     * Launch an activity for which you would like a result when it finished. When this activity exits, 
	     * your onActivityResult() method will be called.
	     *
	     * @param command           The command object
	     * @param intent            The intent to start
	     * @param requestCode       The request code that is passed to callback to identify the activity
	     */
	    public void startActivityForResult(IPlugin command, Intent intent, int requestCode) {
	        this.activityResultCallback = command;
	        this.activityResultKeepRunning = this.keepRunning;

	        // If multitasking turned on, then disable it for activities that return results
	        if (command != null) {
	            this.keepRunning = false;
	        }

	        // Start activity
	        super.startActivityForResult(intent, requestCode);
	    }
	    
	    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
	    	super.onActivityResult(requestCode, resultCode, intent);
	    	IPlugin callback = this.activityResultCallback;
	    	if (callback != null) {
	    		callback.onActivityResult(requestCode, resultCode, intent);
	    	}
	    }
	    
/*	
	@Override
    protected void onActivityResult( int requestCode,
                                        int resultCode, 
                                        Intent extras ) {
        super.onActivityResult( requestCode, resultCode, extras);
        switch(requestCode) {
        case ACTIVITY_ACCOUNTS: {
        	if (resultCode == RESULT_OK) {
        		//startSignupProcessActivity();
        		String accountName = extras.getStringExtra( "account" );
        		AccountManager accountManager = AccountManager.get( this );
        		selectedAccount = AccountUtils.getAccountFromAccountName(accountManager, accountName);
        		Toast.makeText(this, "Account selected: "+accountName, Toast.LENGTH_SHORT).show();
        		if( selectedAccount != null ) {
        			register();
            		//startSignupProcessActivity();
        		}	
        	} 
        }
        break;
        }
    }*/
    
    
    @Override
    /**
     * Called when the system is about to start resuming a previous activity.
     */
    protected void onPause() {
        super.onPause();

         // Send pause event to JavaScript
        this.mainView.loadUrl("javascript:try{cordova.fireDocumentEvent('pause');}catch(e){console.log('exception firing pause event from native');};");

        // Forward to plugins
        if (this.mainView.pluginManager != null) {
            this.mainView.pluginManager.onPause(true);
        }
    }

    @Override
    /**
     * Called when the activity receives a new intent
     **/
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        //Forward to plugins
        if ((this.mainView != null) && (this.mainView.pluginManager != null)) {
            this.mainView.pluginManager.onNewIntent(intent);
        }
    }

    @Override
    /**
     * Called when the activity will start interacting with the user.
     */
    protected void onResume() {
        super.onResume();
        Log.v(TAG, "onResume");
       
        if (this.mainView == null) {
            return;
        }

        // Send resume event to JavaScript
        this.mainView.loadUrl("javascript:try{cordova.fireDocumentEvent('resume');}catch(e){console.log('exception firing resume event from native');};");

        // Forward to plugins
        if (this.mainView.pluginManager != null) {
            this.mainView.pluginManager.onResume(true);
        }

    }

    @Override
    /**
     * The final call you receive before your activity is destroyed.
     */
    public void onDestroy() {
        LOG.d(TAG, "onDestroy()");
        super.onDestroy();
        if (mainView.pluginManager != null) {
            try {
				mainView.pluginManager.onDestroy();
			} catch (IllegalArgumentException e) {
				Log.e(TAG,"Error while destroying pluginManager: ", e);
			}
        }
        
/*        if (this.mainView != null) {

            // Send destroy event to JavaScript
            this.mainView.loadUrl("javascript:try{cordova.require('cordova/channel').onDestroy.fire();}catch(e){console.log('exception firing destroy event from native');};");

            // Load blank page so that JavaScript onunload is called
            this.mainView.loadUrl("about:blank");

            // Forward to plugins
            if (this.mainView.pluginManager != null) {
                try {
					this.mainView.pluginManager.onDestroy();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        }
        else {
            //this.endActivity();
        }*/
    }

    //this is called when the screen rotates.
    // (onCreate is no longer called when screen rotates due to manifest, see: android:configChanges)
    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
    	super.onConfigurationChanged(newConfig);
    	//setContentView(R.layout.main);
    	Log.v(TAG, "Configuration changed.");

    	//InitializeUI();
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.v(TAG, "onSaveInstanceState");
        //outState.putSerializable("sessionData", sessionData);
    }
	
	// Andromeda-specific methods
    
  //used in onCreate() and onConfigurationChanged() to set up the UI elements
    public void InitializeUI()
    {
        //get views from ID's
        mainView =  (CordovaWebView) findViewById(R.id.mainView);
        
        if (sessionData.getSyncpointInstallation() != null) {
        	mainView.loadUrl(sessionData.getCouchAppUrl());
        	//mainView.setWebViewClient(new CustomCordovaWebViewClient(this));
        } else {
        	startAccountSelector();
        }
        
        //etc... hook up click listeners, whatever you need from the Views
    }
	
    public void startAccountSelector() {
		Log.i(TAG, "startAccountSelector");
		/*Intent i = new Intent();
		i.setClassName( 
		    "com.example.android.actionbarcompat",
		    "com.example.android.actionbarcompat.AccountSelector" );
		startActivityForResult(i, ACTIVITY_ACCOUNTS );*/
		//setContentView(R.layout.main);
        //mainView =  (CordovaWebView) findViewById(R.id.mainView);
		mainView.loadUrl("file:///android_asset/www/accounts.html");
	}
	
	public void startSignupProcessActivity() {
		Log.i(TAG, "startSignupProcessActivity");
		/*Intent i = new Intent();
		i.setClassName( 
				"com.example.android.actionbarcompat",
				"com.example.android.actionbarcompat.SignupProcessActivity" );
		startActivityForResult(i, SIGNUP_PROCESS );*/
		mainView.loadUrl("file:///android_asset/www/processing.html");
	}
	
	public void register() {
    	if( sessionData.isRegistered() )
    		unregister();
    	else {
    		Log.d( TAG, "register()" );
    		//C2DMessaging.register( this, C2DM_SENDER );
    		//syncpoint.pairSessionWithType("console", selectedAccount.name);
    		if (sessionData.getSelectedAccount() != null) {
    			progressDialog = new ProgressDialog(MainActivity.this);
				//progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				progressDialog.setTitle(R.string.app_name);
				progressDialog.setMessage("Registering. Please wait...");
				progressDialog.setCancelable(false);
			    progressDialog.setOwnerActivity(activityRef);
			    progressDialog.setIndeterminate(true);
			    progressDialog.setProgress(0);
			    progressDialog.show();
			    
			    // Start a new thread that will register the acct.
		        new RegisterAccount().execute();
    		}
    	}
    }
	
	  private class RegisterAccount extends AsyncTask<String, Void, Object> {
	         protected Object doInBackground(String... args) {
	             Log.i("MyApp", "Background register thread starting");

	             try {
	    				EktorpAsyncTask task = new EktorpAsyncTask() {

	    		            @Override
	    		            protected void doInBackground() {
	    		            	try {
									sessionData.getSyncpoint().pairSession("console", sessionData.getSelectedAccount().name);
								} catch (DbAccessException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}       
	    		            }

	    		            @Override
	    		            protected void onSuccess() {
	    		                Log.v(TAG, "Initiated pairing.");
	    		            }
	    		            @Override
	    		            protected void onDbAccessException(DbAccessException dbAccessException) {
	    		            	Log.e(TAG, "onDbAccessException", dbAccessException);
	    		            }
	    		        };
	    		        task.execute();
	    				
	    				SyncpointChannel channel = sessionData.getSyncpoint().getMyChannel(Constants.syncpointDefaultChannelName);
	    				CouchDbConnector localDatabase = channel.ensureLocalDatabase(getApplicationContext());
	    				String localDatabaseName = localDatabase.getDatabaseName();
	    				
	    				sessionData.setNewDb(sessionData.getServer().getDatabaseNamed(localDatabaseName));
	    	    		//newDb.open();
	    	    		String destFileName = sessionData.getFilesDir() + "/" + localDatabaseName + TOUCHDB_DATABASE_SUFFIX ;
	    	    		File destFile = new File(destFileName);
	    	    		if(!destFile.exists()) {
	    	    			destFile.delete();
	    	    		}
	    				
	    				try {
							sessionData.getNewDb().replaceWithDatabase(sessionData.getFilesDir() + "/" + sessionData.getAppDb() + TOUCHDB_DATABASE_SUFFIX,  sessionData.getFilesDir() + "/" + sessionData.getAppDb() + "/attachments");
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	    				
	    				launchCouchAppView(localDatabaseName);
	    				//progressDialog.dismiss();
	    			} catch (DbAccessException e) {
	    				Log.e( TAG, "Error: " , e);
	    				Toast.makeText(MainActivity.this, "Error: Unable to connect to Syncpoint Server: " + e.getMessage(), Toast.LENGTH_LONG).show();
	    			}

	             return "replace this with your data object";
	         }

	         protected void onPostExecute(Object result) {
	             // Pass the result data back to the main activity
	             //MainActivity.this.progressDialog = result;

	             /*if (MainActivity.this.progressDialog != null) {
	            	 MainActivity.this.progressDialog.dismiss();
	             }*/
	         }
	    }
    
    private void unregister() {
		if( sessionData.isRegistered() ) {
			Log.d( TAG, "unregister()" );
			//C2DMessaging.unregister( this );
			Log.d( TAG, "unregister() done" );
		}
	}
    
    void setupLocalSyncpointDatabase(final CouchDbInstance localServer) {
		 //CouchDbConnector localControlDatabase = localServer.createConnector(SyncpointClientImpl.LOCAL_CONTROL_DATABASE_NAME, false);
		 //SyncpointSession session = SyncpointSession.sessionInDatabase(getApplicationContext(), localServer, localControlDatabase);
		 //if(session != null) {
		 //if(session.isPaired()) {
		 
		 String message = "Installing local profile.";
		 Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
		 Log.v(TAG, message);
		 SyncpointChannel channel = sessionData.getSyncpoint().getMyChannel(Constants.syncpointDefaultChannelName);
		 CouchDbConnector localDatabase = channel.ensureLocalDatabase(getApplicationContext());
		 
		 final String localDatabaseName = localDatabase.getDatabaseName();
		 Log.v(TAG, "localDatabaseName: " + localDatabaseName);
		 //TDDatabase origDb = server.getDatabaseNamed(appDb);
		 //origDb.open();
		 sessionData.setNewDb(sessionData.getServer().getDatabaseNamed(localDatabaseName));
		 sessionData.getNewDb().open();

		 URL localCouchappUrl = null;
		 try {
			 localCouchappUrl = new URL(sessionData.getUrl() + sessionData.getAppDb());
		 } catch (MalformedURLException e) {
			 // TODO Auto-generated catch block
			 e.printStackTrace();
		 }
		 TDReplicator replPull = sessionData.getNewDb().getReplicator(localCouchappUrl, false, false);
		 replPull.start();
		 
		 boolean activeReplication = true;
		 while (activeReplication == true) {
			 List<TDReplicator> activeReplicators = sessionData.getNewDb().getActiveReplicators();
			 int i = 0;
			 if(activeReplicators != null) {
				 for (TDReplicator replicator : activeReplicators) {
					 String source = replicator.getRemote().toExternalForm();
					 Log.v(TAG, "remote " + source);
					 if (source.equals(localCouchappUrl.toExternalForm())) {
						 if (replicator.isRunning() == true) {
							 try {
								 i++;
								 Thread.sleep(1000);
							 } catch (InterruptedException e) {
								 // TODO Auto-generated catch block
								 e.printStackTrace();
							 }
						 }
					 }
				 }
			 }
			 if (i == 0) {
				 activeReplication = false;
			 }
		 }
		 /*Observer observer = new Observer() {

			 @Override
			 public void update(Observable observable, Object data) {
				 Log.v(TAG, "Waiting for replicator to finish ");
				 replicationChangesProcessed = replPull.getChangesProcessed();
				 //if (observable == replPull) {
					 if (!replPull.isRunning()) {
						 launchCouchAppView(localDatabaseName, replPull);
					 }
				 //}
			 }
		 };
		 replPull.addObserver(observer);*/
		 
		 /*int replicationChangesTotal = replPull.getChangesTotal();
		 
		 while(replicationChangesProcessed > replicationChangesTotal) {
			 Log.i(TAG, "Waiting for replicator to finish");
			 try {
				 Thread.sleep(1000);
			 } catch (InterruptedException e) {
				 // TODO Auto-generated catch block
				 e.printStackTrace();
			 }
		 }*/
		 
		 /*Thread t1 = new Thread() {
			 public void run() {
				 replPull.start();
			 }
		 };

		 t1.start();*/
		 
		 

		 /*while(replPull.isRunning()) {
			 Log.i(TAG, "Waiting for replicator to finish");
			 try {
				 Thread.sleep(1000);
			 } catch (InterruptedException e) {
				 // TODO Auto-generated catch block
				 e.printStackTrace();
			 }
		 }*/

		 launchCouchAppView(localDatabaseName);
		
		/* 
		 // create a sample document to verify that replication in the channel is working
		 Map<String,Object> testObject = new HashMap<String,Object>();
		 testObject.put("key", "value");
		 // store the document
		 localDatabase.create(testObject);*/
		 //}
		 //}
	 }
	
	public void launchCouchAppView(String localDatabaseName) {
		Log.i(TAG, "launchCouchAppView");
		try {
			Thread.sleep(2*1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    sessionData.setLocalSyncpointDbName(localDatabaseName);
		String couchAppUrl = sessionData.getUrl() + sessionData.getAppDb() + "/" + sessionData.getCouchAppInstanceUrl();
	    if (sessionData.getNewDb() != null) {
	    	couchAppUrl = sessionData.getUrl() + sessionData.getLocalSyncpointDbName() + "/" + sessionData.getCouchAppInstanceUrl();
	    }
	    Log.d( TAG, "Loading couchAppUrl: " + couchAppUrl );
	    
	    //this.loadUrl(couchAppUrl);
	    mainView.loadUrl(couchAppUrl);
	}
	

	
	public Account getSelectedAccount() {
		return sessionData.getSelectedAccount();
	}

	void waitForPairingToComplete(final CouchDbConnector remote, final PairingUser userDoc) {
        Log.v(TAG, "Waiting for pairing to complete...");
        Looper l = Looper.getMainLooper();
        Handler h = new Handler(l);
        h.postDelayed(new Runnable() {

            @Override
            public void run() {
                Log.v(TAG, "Checking to see if pairing completed...");
                PairingUser user = remote.get(PairingUser.class, userDoc.getId());
                if("paired".equals(user.getPairingState())) {
                    pairingDidComplete(remote, user);
                } else {
                    Log.v(TAG, "Pairing state is stuck at " + user.getPairingState());
                    waitForPairingToComplete(remote, user);
                }
            }
        }, 3000);
    }
	
	public void checkChannelReadiness() {
		Log.v(TAG, "Checking is channel was not ready when it tried to getUpToDateWithSubscriptions.");
		//again this part of the implementation differs because we just
		//have channel ids and need to load the channels
		Map<String, SyncpointChannel> channelMap = new HashMap<String, SyncpointChannel>();
		List<SyncpointChannel> channels = (List<SyncpointChannel>)SyncpointModelFactory.getModelsOfType(sessionData.getLocalControlDatabase(), "channel", SyncpointChannel.class);
		for (SyncpointChannel channel : channels) {
			channel.attach(sessionData.getLocalServer(), sessionData.getLocalControlDatabase());
			channelMap.put(channel.getId(), channel);
		}

		// Sync all installations whose channels are ready:
		List<SyncpointInstallation> allInstallations = sessionData.getSession().getAllInstallations();
		Log.v(TAG, String.format("There are %d installations here", allInstallations.size()));
		for (SyncpointInstallation installation : allInstallations) {
			SyncpointChannel channel = channelMap.get(installation.getChannelId());
			if(channel == null) {
				Log.e(TAG, String.format("Installation %s references missing channel %s", installation, channel));
			} else if(channel.isReady()) {
				Log.v(TAG, String.format("Channel %s is ready, calling sync", channel.getName()));
				//installation.sync(sessionData.getSession(), channel);
			} else {
				Log.v(TAG, String.format("Channel %s is not ready", channel.getName()));
			}

		}
	}
	
    void pairingDidComplete(final CouchDbConnector remote, final PairingUser userDoc) {
        sessionData.getSession().setState("paired");
        sessionData.getSession().setOwnerId(userDoc.getOwnerId());
        sessionData.getSession().setControlDatabase(userDoc.getControlDatabase());

        EktorpAsyncTask task = new EktorpAsyncTask() {

            @Override
            protected void doInBackground() {
                // TODO Auto-generated method stub
                sessionData.getLocalControlDatabase().update(sessionData.getSession());
            }

            @Override
            protected void onSuccess() {
                Log.v(TAG, "Device is now paired");
                //FIXME this delete is not working, investigate later
                //remote.delete(userDoc);
                //connectToControlDB();
            }
        };
        task.execute();
    }


}
