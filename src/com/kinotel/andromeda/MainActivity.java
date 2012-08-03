package com.kinotel.andromeda;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.List;
import java.util.Properties;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.api.CordovaInterface;
import org.apache.cordova.api.IPlugin;
import org.apache.cordova.api.LOG;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.DbAccessException;
import org.ektorp.android.http.AndroidHttpClient;
import org.ektorp.android.util.EktorpAsyncTask;
import org.ektorp.http.HttpClient;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbInstance;
import org.zahangirbd.android.cert.InstallCert;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.byarger.exchangeit.EasySSLSocketFactory;
import com.couchbase.syncpoint.SyncpointClient;
import com.couchbase.syncpoint.impl.SyncpointClientImpl;
import com.couchbase.syncpoint.model.PairingUser;
import com.couchbase.syncpoint.model.SyncpointChannel;
import com.couchbase.syncpoint.model.SyncpointInstallation;
import com.couchbase.syncpoint.model.SyncpointSession;
import com.couchbase.touchdb.TDDatabase;
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
    private Account selectedAccount;
    private boolean registered;
    private SyncpointClient syncpoint;
    private SyncpointSession session;
    private CouchDbConnector localControlDatabase;
    private TDServer server = null;
    private TDListener listener;
    private TDDatabase newDb;
	private String localSyncpointDbName;	// null if local syncpoint DB has not been created and replicated.
	private String url;	//baseURL for local couchapp.
	private String appDb;	// db name from properties file used in installation.
	private String couchAppInstanceUrl;	// from properties file used in installation.
	String filesDir;
	
	@SuppressLint("NewApi")
	@Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); 
        mainView =  (CordovaWebView) findViewById(R.id.mainView);
        //mainView.loadUrl("file:///android_asset/www/blank.html");

        if (android.os.Build.VERSION.SDK_INT > 9) {  
        	StrictMode.VmPolicy vmpolicy = new StrictMode.VmPolicy.Builder().penaltyLog().build();
        	StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        	StrictMode.setThreadPolicy(policy);
        	StrictMode.setVmPolicy(vmpolicy);
        }
        
        filesDir = getFilesDir().getAbsolutePath();
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
            server = new TDServer(filesDir);
            //server.setDefaultHttpClientFactory(defaultHttpClientFactory);
            
            server.setDefaultHttpClientFactory(new HttpClientFactory() {

                @Override
                public org.apache.http.client.HttpClient getHttpClient() {
                    DefaultHttpClient httpClient = new DefaultHttpClient();
                    // to enable self-signed SSL certs.
        	        httpClient.getConnectionManager().getSchemeRegistry().register(new Scheme("https", new EasySSLSocketFactory(), 6984));
                    return httpClient;
                }
            });
            listener = new TDListener(server, 8888);
            listener.start();
            TDView.setCompiler(new TDJavaScriptViewCompiler());
        } catch (IOException e) {
            Log.e(TAG, "Unable to create TDServer", e);
        }

        // start couchbase
        String ipAddress = "0.0.0.0";
        Log.d(TAG, ipAddress);
		String host = ipAddress;
		int port = 8888;
		url = "http://" + host + ":" + Integer.toString(port) + "/";
		
		//uiHandler = new Handler();
        appDb = properties.getProperty("app_db");
        couchAppInstanceUrl = properties.getProperty("couchAppInstanceUrl");
	    File destination = new File(filesDir + File.separator + appDb + TOUCHDB_DATABASE_SUFFIX);
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
	    
	    Log.d(TAG, "Checking for touchdb at " + filesDir + File.separator + appDb + TOUCHDB_DATABASE_SUFFIX);
	    if (!destination.exists()) {
	    	Log.d(TAG, "Touchdb does not exist. Installing.");
	    	// must be in the assets directory
	    	try {
	    		//db.replaceWithDatabase(appDb + TOUCHDB_DATABASE_SUFFIX, appDb);
	    	    AssetManager assetManager = this.getAssets();
	    		// This is the appDb touchdb
	        	CoconutUtils.copyFileOrDir(assetManager, appDb + TOUCHDB_DATABASE_SUFFIX, filesDir);
	    		// These are the appDb attachments
	        	CoconutUtils.copyFileOrDir(assetManager, appDb, filesDir);
	        	// This is the mobilefuton touchdb
	        	CoconutUtils.copyFileOrDir(assetManager, "mobilefuton" + TOUCHDB_DATABASE_SUFFIX, filesDir);
	        	// These are the mobilefuton attachments
	        	CoconutUtils.copyFileOrDir(assetManager, "mobilefuton", filesDir);
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
	    	URL masterServerUrl = new URL(masterServer);
	    	Log.d(TAG, "Syncpoint masterServerUrl: " + masterServerUrl);
	    	try {
	    		// Check if there is already a pairing session in-use.
	    		HttpClient httpClient = new TouchDBHttpClient(server);
	    		CouchDbInstance localServer = new StdCouchDbInstance(httpClient);  	
	    		//CouchDbConnector userDb = localServer.createConnector("_users", false);
	    		localControlDatabase = localServer.createConnector(SyncpointClientImpl.LOCAL_CONTROL_DATABASE_NAME, false);
	    		session = SyncpointSession.sessionInDatabase(getApplicationContext(), localServer, localControlDatabase);
	    		if((session!= null) && (!session.isPaired())) {
	    			final PairingUser pairingUser = session.getPairingUser();		    		
		    		if (pairingUser != null) {
		    			Log.v(TAG, "Pairing still in-progress");
			    		
			    		HttpClient remoteHttpClient = new AndroidHttpClient.Builder().url(masterServerUrl).relaxedSSLSettings(true)
			    				.username(session.getPairingCreds().getUsername()).password(session.getPairingCreds().getPassword()).maxConnections(100).build();

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
			    		syncpoint = new SyncpointClientImpl(getApplicationContext(), localServer, masterServerUrl, Constants.syncpointAppId);
		    		}
	    		} else {
	    			syncpoint = new SyncpointClientImpl(getApplicationContext(), localServer, masterServerUrl, Constants.syncpointAppId);
	    		}
	    	} catch (org.ektorp.UpdateConflictException e1) {
	    		Log.v(TAG, "Error: " + e1);
	    	} catch (DbAccessException e1) {
	    		Log.e( TAG, "Error: " , e1);
	    		e1.printStackTrace();
	    		Toast.makeText(this, "Error: Unable to connect to Syncpoint Server: " + e1.getMessage(), Toast.LENGTH_LONG).show();
	    	}
	    	SyncpointChannel channel = null;
	    	if (session != null) {
	    		channel = session.getMyChannel(syncpointDefaultChannelName);
	    	} else {
	    		if (syncpoint!= null) {
		    		channel = syncpoint.getMyChannel(syncpointDefaultChannelName);
	    		}
	    	}
	    	SyncpointInstallation inst = channel.getInstallation(getApplicationContext());
	    	if(inst != null) {
	    		CouchDbConnector localDatabase = inst.getLocalDatabase(getApplicationContext());
	    		String localDatabaseName = localDatabase.getDatabaseName();
	    		Log.v(TAG, "localDatabaseName: " + localDatabaseName);
	    		//TDDatabase origDb = server.getDatabaseNamed(appDb);
	    		//origDb.open();
	    		newDb = server.getDatabaseNamed(localDatabaseName);
	    		newDb.open();

	    		long designDocId = newDb.getDocNumericID("_design/couchabb");

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
						newDb.replaceWithDatabase(filesDir + "/" + appDb,  filesDir + "/" + appDb + TOUCHDB_DATABASE_SUFFIX);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	    			// show signup progress indicator
	    			//startSignupProcessActivity();
	    			startAccountSelector();
	    		} else {
	    			localSyncpointDbName = localDatabaseName;
	    			String couchAppUrl = url + appDb + "/" + properties.getProperty("couchAppInstanceUrl");
	    			if (newDb != null) {
	    				couchAppUrl = url + localSyncpointDbName + "/" + properties.getProperty("couchAppInstanceUrl");
	    			}
	    			Log.d( TAG, "Loading couchAppUrl: " + couchAppUrl );
	    			//AndroidCouchbaseCallback.this.loadUrl(couchAppUrl);
	    			//setContentView(R.layout.main);
	    			//mainView =  (CordovaWebView) findViewById(R.id.mainView);
	    			mainView.loadUrl(couchAppUrl);
	    		}
	    	} else {
	    		startAccountSelector();
	    	}
	    }  catch (MalformedURLException e) {
	    	// TODO Auto-generated catch block
	    	e.printStackTrace();
	    }
	    
        //mainView.loadUrl("file:///android_asset/www/index.html");
	    
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
    		selectedAccount = AccountUtils.getAccountFromAccountName(accountManager, accountName);
    		this.getActivity().runOnUiThread(new Runnable() {
    			public void run() {
    				Context context = getApplicationContext();
    				//Toast.makeText(activity, "Hello", Toast.LENGTH_SHORT).show();
    				Toast.makeText(context, "Account selected: "+accountName, Toast.LENGTH_SHORT).show();
    				if( selectedAccount != null ) {
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
            mainView.pluginManager.onDestroy();
        }
        
        if (this.mainView != null) {

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
        }
    }
	
	// Andromeda-specific methods
	
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
    	if( registered )
    		unregister();
    	else {
    		Log.d( TAG, "register()" );
    		//C2DMessaging.register( this, C2DM_SENDER );
    		//syncpoint.pairSessionWithType("console", selectedAccount.name);
    		if (selectedAccount != null) {
    			try {
    				
    				EktorpAsyncTask task = new EktorpAsyncTask() {

    		            @Override
    		            protected void doInBackground() {
    		            	syncpoint.pairSession("console", selectedAccount.name);
    		            }

    		            @Override
    		            protected void onSuccess() {
    		                Log.v(TAG, "Initiated pairing.");
    		            }
    		        };
    		        task.execute();
    				HttpClient httpClient = new TouchDBHttpClient(server);
    				CouchDbInstance localServer = new StdCouchDbInstance(httpClient);  	
    				//CouchDbConnector userDb = localServer.createConnector("_users", false);
    				CouchDbConnector localControlDatabase = localServer.createConnector(SyncpointClientImpl.LOCAL_CONTROL_DATABASE_NAME, false);			    	
    				//PairingUser pairingUser = session.getPairingUser();
    				//PairingUser result = userDb.get(PairingUser.class, pairingUser.getId());
    				//waitForPairingToComplete(localServer, localControlDatabase);
    				//setupLocalSyncpointDatabase(localServer);
    				
    				SyncpointChannel channel = syncpoint.getMyChannel(Constants.syncpointDefaultChannelName);
    				CouchDbConnector localDatabase = channel.ensureLocalDatabase(getApplicationContext());
    				String localDatabaseName = localDatabase.getDatabaseName();
    				
    				newDb = server.getDatabaseNamed(localDatabaseName);
    	    		//newDb.open();
    	    		String destFileName = filesDir + "/" + localDatabaseName + TOUCHDB_DATABASE_SUFFIX ;
    	    		File destFile = new File(destFileName);
    	    		if(!destFile.exists()) {
    	    			destFile.delete();
    	    		}
    				
    				try {
						newDb.replaceWithDatabase(filesDir + "/" + appDb + TOUCHDB_DATABASE_SUFFIX,  filesDir + "/" + appDb + "/attachments");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
    				
    				launchCouchAppView(localDatabaseName);
    			} catch (DbAccessException e) {
    				Log.e( TAG, "Error: " , e);
    				Toast.makeText(this, "Error: Unable to connect to Syncpoint Server: " + e.getMessage(), Toast.LENGTH_LONG).show();
    			}
    			Log.d( TAG, "register() done" );
    		}
    	}
    }
    
    private void unregister() {
		if( registered ) {
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
		 SyncpointChannel channel = syncpoint.getMyChannel(Constants.syncpointDefaultChannelName);
		 CouchDbConnector localDatabase = channel.ensureLocalDatabase(getApplicationContext());
		 
		 final String localDatabaseName = localDatabase.getDatabaseName();
		 Log.v(TAG, "localDatabaseName: " + localDatabaseName);
		 //TDDatabase origDb = server.getDatabaseNamed(appDb);
		 //origDb.open();
		 newDb = server.getDatabaseNamed(localDatabaseName);
		 newDb.open();

		 URL localCouchappUrl = null;
		 try {
			 localCouchappUrl = new URL(url + appDb);
		 } catch (MalformedURLException e) {
			 // TODO Auto-generated catch block
			 e.printStackTrace();
		 }
		 TDReplicator replPull = newDb.getReplicator(localCouchappUrl, false, false);
		 replPull.start();
		 
		 boolean activeReplication = true;
		 while (activeReplication == true) {
			 List<TDReplicator> activeReplicators = newDb.getActiveReplicators();
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
	    localSyncpointDbName = localDatabaseName;
		String couchAppUrl = url + appDb + "/" + couchAppInstanceUrl;
	    if (newDb != null) {
	    	couchAppUrl = url + localSyncpointDbName + "/" + couchAppInstanceUrl;
	    }
	    Log.d( TAG, "Loading couchAppUrl: " + couchAppUrl );
	    //this.loadUrl(couchAppUrl);
	    mainView.loadUrl(couchAppUrl);
	}
	
	public Account getSelectedAccount() {
		return selectedAccount;
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
	
    void pairingDidComplete(final CouchDbConnector remote, final PairingUser userDoc) {
        session.setState("paired");
        session.setOwnerId(userDoc.getOwnerId());
        session.setControlDatabase(userDoc.getControlDatabase());

        EktorpAsyncTask task = new EktorpAsyncTask() {

            @Override
            protected void doInBackground() {
                // TODO Auto-generated method stub
                localControlDatabase.update(session);
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
