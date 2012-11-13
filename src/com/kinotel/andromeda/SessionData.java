package com.kinotel.andromeda;

import java.io.Serializable;
import java.net.URL;

import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;

import android.accounts.Account;

import com.couchbase.syncpoint.SyncpointClient;
import com.couchbase.syncpoint.model.SyncpointInstallation;
import com.couchbase.syncpoint.model.SyncpointSession;
import com.couchbase.touchdb.TDDatabase;
import com.couchbase.touchdb.TDServer;
import com.couchbase.touchdb.listener.TDListener;

public class SessionData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3125889331914899630L;
	private Account selectedAccount;
	private boolean registered;

	private String localSyncpointDbName;
	private String url;
	private String appDb;
	private String couchAppInstanceUrl;
	private String filesDir;
	private String couchAppUrl;

	private URL remoteServerURL;

	public Account getSelectedAccount() {
		return selectedAccount;
	}

	public void setSelectedAccount(Account selectedAccount) {
		this.selectedAccount = selectedAccount;
	}

	public boolean isRegistered() {
		return registered;
	}

	public void setRegistered(boolean registered) {
		this.registered = registered;
	}

	

	public String getLocalSyncpointDbName() {
		return localSyncpointDbName;
	}

	public void setLocalSyncpointDbName(String localSyncpointDbName) {
		this.localSyncpointDbName = localSyncpointDbName;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getAppDb() {
		return appDb;
	}

	public void setAppDb(String appDb) {
		this.appDb = appDb;
	}

	public String getCouchAppInstanceUrl() {
		return couchAppInstanceUrl;
	}

	public void setCouchAppInstanceUrl(String couchAppInstanceUrl) {
		this.couchAppInstanceUrl = couchAppInstanceUrl;
	}

	public String getFilesDir() {
		return filesDir;
	}

	public void setFilesDir(String filesDir) {
		this.filesDir = filesDir;
	}

	public String getCouchAppUrl() {
		return couchAppUrl;
	}

	public void setCouchAppUrl(String couchAppUrl) {
		this.couchAppUrl = couchAppUrl;
	}

	public URL getRemoteServerURL() {
		return remoteServerURL;
	}

	public void setRemoteServerURL(URL remoteServerURL) {
		this.remoteServerURL = remoteServerURL;
	}

}