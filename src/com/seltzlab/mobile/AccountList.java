package com.seltzlab.mobile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import org.apache.cordova.api.CordovaInterface;
import org.apache.cordova.api.Plugin;
import org.apache.cordova.api.PluginResult;

public class AccountList extends Plugin {
	
	BroadcastReceiver receiver;
	String TAG = "AccountList-Cordova";
	
	 /**
     * Constructor.
     */
    public AccountList() {
        this.receiver = null;
    }
    
    /**
	 * Sets the context of the Command. This can then be used to do things like
	 * get file paths associated with the Activity.
	 * 
	 * @param ctx The context of the main Activity.
	 */
	public void setContext(CordovaInterface ctx) {
		super.setContext(ctx);

		// We need to listen to connectivity events to update navigator.connection
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
			JSONObject obj = args.getJSONObject(0);
			
			AccountManager am = AccountManager.get(cordova.getActivity().getApplicationContext());
			
			Account[] accounts;
			if (obj.has("type"))
				accounts = am.getAccountsByType(obj.getString("type"));
			else
				accounts = am.getAccounts();
			
			JSONArray res = new JSONArray();
			for (int i = 0; i < accounts.length; i++) {
				Account a = accounts[i];
				res.put(a.name);
			}
		
			return new PluginResult(PluginResult.Status.OK, res);
			
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
     * Stop the receiver and set it to null.
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
