package com.kinotel.andromeda;

import java.io.Serializable;

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
	private SyncpointClient syncpoint;
	private SyncpointSession session;
	private CouchDbConnector localControlDatabase;
	private TDServer server;
	private TDListener listener;
	private TDDatabase newDb;
	private String localSyncpointDbName;
	private String url;
	private String appDb;
	private String couchAppInstanceUrl;
	private String filesDir;
	private SyncpointInstallation syncpointInstallation;
	private String couchAppUrl;
	private CouchDbInstance localServer;

	public SessionData(TDServer server) {
		this.server = server;
	}

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

	public SyncpointClient getSyncpoint() {
		return syncpoint;
	}

	public void setSyncpoint(SyncpointClient syncpoint) {
		this.syncpoint = syncpoint;
	}

	public SyncpointSession getSession() {
		return session;
	}

	public void setSession(SyncpointSession session) {
		this.session = session;
	}

	public CouchDbConnector getLocalControlDatabase() {
		return localControlDatabase;
	}

	public void setLocalControlDatabase(CouchDbConnector localControlDatabase) {
		this.localControlDatabase = localControlDatabase;
	}

	public TDServer getServer() {
		return server;
	}

	public void setServer(TDServer server) {
		this.server = server;
	}

	public TDListener getListener() {
		return listener;
	}

	public void setListener(TDListener listener) {
		this.listener = listener;
	}

	public TDDatabase getNewDb() {
		return newDb;
	}

	public void setNewDb(TDDatabase newDb) {
		this.newDb = newDb;
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

	public SyncpointInstallation getSyncpointInstallation() {
		return syncpointInstallation;
	}

	public void setSyncpointInstallation(SyncpointInstallation syncpointInstallation) {
		this.syncpointInstallation = syncpointInstallation;
	}

	public String getCouchAppUrl() {
		return couchAppUrl;
	}

	public void setCouchAppUrl(String couchAppUrl) {
		this.couchAppUrl = couchAppUrl;
	}

	public CouchDbInstance getLocalServer() {
		return localServer;
	}

	public void setLocalServer(CouchDbInstance localServer) {
		this.localServer = localServer;
	}
}