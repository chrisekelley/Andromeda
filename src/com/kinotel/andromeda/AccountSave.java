package com.kinotel.andromeda;

import org.apache.cordova.api.CordovaInterface;
import org.apache.cordova.api.Plugin;
import org.apache.cordova.api.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class AccountSave extends Plugin {
	
	private static final int ACTIVITY_ACCOUNTS = 1;
	BroadcastReceiver receiver;
	String TAG = "AccountSave-Cordova";
	private String pluginCallbackId;
	
	 /**
		 * Sets the context of the Command. This can then be used to do things like
		 * get file paths associated with the Activity.
		 * 
		 * @param ctx The context of the main Activity.
		 */
		public void setContext(CordovaInterface ctx) {
			super.setContext(ctx);
			
			this.pluginCallbackId = null;

			// We need to listen to connectivity events to update 
			IntentFilter intentFilter = new IntentFilter() ;
			if (this.receiver == null) {
				this.receiver = new BroadcastReceiver() {
					@Override
					public void onReceive(Context arg0, Intent arg1) {
						// TODO Auto-generated method stub
						
					}
				};
				// register the receiver... this is so it doesn't have to be added to AndroidManifest.xml
				cordova.getContext().registerReceiver(this.receiver, intentFilter);
			}
		}

	@Override
	public PluginResult execute(String action, JSONArray args, String callbackId) {
		try {
			this.pluginCallbackId = callbackId;
			JSONObject obj = args.getJSONObject(0);
			String account = obj.getString("name");
			Log.d(TAG, account + " selected.");
			JSONArray res = new JSONArray();
			res.put(account);
			//Intent intent = new Intent("com.kinotel.cordova.AccountSave");
			//Intent intent = new Intent("com.example.android.actionbarcompat.MainActivity");
			//Intent intent = cordova.getActivity().getIntent();
			//Intent intent = new Intent(cordova.getContext(), MainActivity.class);
			Intent intent = new Intent();
			intent.putExtra( "account", account);
			//String msg = intent.getStringExtra("returnedData");
			
			/*Activity activity = cordova.getActivity();
			activity.setResult( Activity.RESULT_OK, intent );*/
			cordova.onMessage("account", account);
			PluginResult result = new PluginResult(PluginResult.Status.OK, res);
			//result.setKeepCallback(true);
			//cordova.getActivity().getIntent();

			this.success(result, this.pluginCallbackId);
			//cordova.startActivityForResult((Plugin) this, intent, ACTIVITY_ACCOUNTS);
			return result;
		} catch (JSONException e) {
			return new PluginResult(PluginResult.Status.JSON_EXCEPTION);
		}
	}
	
	/**
	 * Stop  receiver.
	 */
	public void onDestroy() {
		removeListener();
	}

	/**
	 * Stop the battery receiver and set it to null.
	 */
	private void removeListener() {
		if (this.receiver != null) {
			try {
				this.cordova.getActivity().unregisterReceiver(this.receiver);
				this.receiver = null;
			} catch (Exception e) {
				Log.e(TAG, "Error unregistering  receiver: " + e.getMessage(), e);
			}
		}
	}

}
