package com.example.mapper.synchronization;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.example.mapper.R;
import com.example.mapper.activities.dialogs.GenerateScheduleModuleDialog;
import com.example.mapper.apis.Mapper;

public class SyncAccount {
    public static Account Create(Context context) {
        // Create the account type and default account
        Account newAccount = new Account(context.getString(R.string.account), context.getString(R.string.accountType));
        // Get an instance of the Android account manager
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
        if (accountManager.addAccountExplicitly(newAccount, null, null)) {
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call context.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

//            Bundle extras = new Bundle();
//            context.getContentResolver().setIsSyncable(newAccount, context.getString(R.string.authority), 1);
//            context.getContentResolver().setSyncAutomatically(newAccount, context.getString(R.string.authority), true);
//            ContentResolver.addPeriodicSync(newAccount, context.getString(R.string.authority), Bundle.EMPTY, 1);
//            context.getContentResolver().requestSync(newAccount, context.getString(R.string.authority), extras);
        } else {
            /*
             * The account exists or some other error occurred. Log this, report it,
             * or handle it internally.
             */
        }


        return newAccount;
    }
}
