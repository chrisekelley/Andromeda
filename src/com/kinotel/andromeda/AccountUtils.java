package com.kinotel.andromeda;

import android.accounts.Account;
import android.accounts.AccountManager;

public class AccountUtils {

	public static Account getAccountFromAccountName( AccountManager accountManager, String accountName ) {
		Account accounts[] = accountManager.getAccounts();
		for( int i = 0 ; i < accounts.length ; ++i )
			if( accountName.equals( accounts[i].name ) )
				return accounts[i];
		return null;
	}

}
