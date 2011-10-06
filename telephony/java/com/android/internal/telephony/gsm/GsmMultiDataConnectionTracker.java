// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   GsmMultiDataConnectionTracker.java

package com.android.internal.telephony.gsm;

import Lcom.android.internal.telephony.gsm.GsmMultiDataConnectionTracker;;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.*;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.*;
import android.os.*;
import android.preference.PreferenceManager;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.EventLog;
import android.util.Log;
import com.android.internal.telephony.*;
import com.android.internal.telephony.ims.sms.ImsSMSInterface;
import java.io.*;
import java.net.*;
import java.util.*;

// Referenced classes of package com.android.internal.telephony.gsm:
//            GSMPhone, SIMRecords, GsmCallTracker, GsmServiceStateTracker, 
//            ApnSetting, GsmDataConnection

public final class GsmMultiDataConnectionTracker extends DataConnectionTracker
{
    /* member class not found */
    class NetPollStatTimer {}

    /* member class not found */
    class DataRoamingContentObserver {}

    /* member class not found */
    class ApnChangeObserver {}

    private class OemCommands
    {

        static final int OEM_FUNCTION_ID_GPRS = 9;
        static final int OEM_GPRS_FORCE_DORMANCY = 1;
        final GsmMultiDataConnectionTracker this$0;

        private OemCommands()
        {
            this$0 = GsmMultiDataConnectionTracker.this;
            Object();
        }
    }


    GsmMultiDataConnectionTracker(GSMPhone gsmphone)
    {
        SharedPreferences sharedpreferences;
        boolean flag10;
        DataConnectionTracker(gsmphone);
        LOG_TAG = "GSM";
        mIsSimSupportMultiPdp = true;
        LinkedList linkedlist = new LinkedList();
        qOnDemandPdnRequestQueue = linkedlist;
        mCleanupCount = 0;
        autoAttach = true;
        mReregisterOnReconnectFailure = false;
        mPdpResetCount = 0;
        mIsScreenOn = true;
        mAttachApnType = "ims";
        mIsImsEnabled = false;
        mIsAdminEnabled = false;
        failNextConnect = false;
        allApns = null;
        backup_allApns = null;
        waitingApns = null;
        preferredApn = null;
        mCurrentRequestedApnType = "ims";
        mPendingIpv6DataCallList = null;
        mPendingRequestedApns = null;
        mIsPsRestricted = false;
        HashMap hashmap = new HashMap();
        oldList = hashmap;
        HashMap hashmap1 = new HashMap();
        mInactivityTimerList = hashmap1;
        powerManager = null;
        canSetPreferApn = false;
        inactivityPeriod = 0L;
        oldPollTime = 0L;
        RetryManager aretrymanager[] = new RetryManager[6];
        mRetryMgr = aretrymanager;
        cur_gprsState = 1;
        BroadcastReceiver broadcastreceiver = new BroadcastReceiver() ;
        mIntentReceiver = broadcastreceiver;
        Runnable runnable = new Runnable() ;
        mPollNetStat = runnable;
        mGsmPhone = gsmphone;
        HandoverTracker handovertracker = gsmphone.mHOT;
        mHandoverTracker = handovertracker;
        gsmphone.mCM.registerForAvailable(this, 3, null);
        gsmphone.mCM.registerForOffOrNotAvailable(this, 12, null);
        gsmphone.mSIMRecords.registerForRecordsLoaded(this, 4, null);
        gsmphone.mCM.registerForDataStateChanged(this, 6, null);
        gsmphone.mCT.registerForVoiceCallEnded(this, 15, null);
        gsmphone.mCT.registerForVoiceCallStarted(this, 14, null);
        gsmphone.mSST.registerForGprsAttached(this, 26, null);
        gsmphone.mSST.registerForGprsDetached(this, 19, null);
        gsmphone.mSST.registerForRoamingOn(this, 21, null);
        gsmphone.mSST.registerForRoamingOff(this, 22, null);
        gsmphone.mSST.registerForPsRestrictedEnabled(this, 32, null);
        gsmphone.mSST.registerForPsRestrictedDisabled(this, 33, null);
        gsmphone.mCM.registerForNetworkDisconnectReq(this, 47, null);
        gsmphone.mSST.registerForInitiateHandover(this, 55, null);
        gsmphone.mSST.registerForlteFromeHRPDHandover(this, 56, null);
        mHandoverTracker.registerForInitiateHandoverFromLte(this, 55, null);
        mHandoverTracker.registerForStartHandoverFromEhrpd(this, 56, null);
        mHandoverTracker.registerForHandoverResumeTimeoutFromLte(this, 61, null);
        gsmphone.mSST.registerForHome(this, 72, null);
        IntentFilter intentfilter = new IntentFilter();
        intentfilter.addAction("com.android.internal.telephony.gsm-reconnect");
        intentfilter.addAction("com.android.internal.telephony.gsm-inactivity.ims");
        intentfilter.addAction("com.android.internal.telephony.gsm-inactivity.default");
        intentfilter.addAction("com.android.internal.telephony.gsm-inactivity.vzwapp");
        intentfilter.addAction("com.android.internal.telephony.gsm-inactivity.admin");
        intentfilter.addAction("android.intent.action.SCREEN_ON");
        intentfilter.addAction("android.intent.action.SCREEN_OFF");
        intentfilter.addAction("android.net.wifi.STATE_CHANGE");
        intentfilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        intentfilter.addAction("android.net.action.DUN_DISCONNECTED");
        intentfilter.addAction("android.net.action.DUN_ATTEMPTED");
        Context context = gsmphone.getContext();
        BroadcastReceiver broadcastreceiver1 = mIntentReceiver;
        Intent intent = context.registerReceiver(broadcastreceiver1, intentfilter, null, gsmphone);
        gsmphone.mCM.registerForIpv6AddrStatusChanged(this, 42, null);
        mDataConnectionTracker = this;
        ContentResolver contentresolver = phone.getContext().getContentResolver();
        mResolver = contentresolver;
        ApnChangeObserver apnchangeobserver = new ApnChangeObserver();
        apnObserver = apnchangeobserver;
        ContentResolver contentresolver1 = gsmphone.getContext().getContentResolver();
        Uri uri = android.provider.Telephony.Carriers.CONTENT_URI;
        ApnChangeObserver apnchangeobserver1 = apnObserver;
        contentresolver1.registerContentObserver(uri, true, apnchangeobserver1);
        DataRoamingContentObserver dataroamingcontentobserver = new DataRoamingContentObserver();
        mDataRoaming = dataroamingcontentobserver;
        ContentResolver contentresolver2 = gsmphone.getContext().getContentResolver();
        Uri uri1 = android.provider.Settings.Secure.getUriFor("data_roam_access_settings");
        DataRoamingContentObserver dataroamingcontentobserver1 = mDataRoaming;
        contentresolver2.registerContentObserver(uri1, false, dataroamingcontentobserver1);
        PowerManager powermanager = (PowerManager)phone.getContext().getSystemService("power");
        powerManager = powermanager;
        sWakeLockConnect = powerManager.newWakeLock(1, "GSM");
        CommandsInterface commandsinterface = gsmphone.mCM;
        mCommandsInterfaceGSM = commandsinterface;
        createAllPdpList();
        createPendingRequestedApnList();
        createPendingIpv6DataCallList();
        sharedpreferences = PreferenceManager.getDefaultSharedPreferences(phone.getContext());
        boolean flag = sharedpreferences.getBoolean("enable_ims_test_mode", false);
        mImsTestMode = flag;
        StringBuilder stringbuilder = (new StringBuilder()).append("GsmMultiDataConnectionTracker constrcutor mImsTestMode ");
        boolean flag1 = mImsTestMode;
        String s = stringbuilder.append(flag1).toString();
        log(s);
        boolean flag2 = sharedpreferences.getBoolean("ondemand_mode_on_key", false);
        isOnDemandEnable = flag2;
        StringBuilder stringbuilder1 = (new StringBuilder()).append("GsmMultiDataConnectionTracker constrcutor isOnDemandEnable ");
        boolean flag3 = isOnDemandEnable;
        String s1 = stringbuilder1.append(flag3).toString();
        log(s1);
        if(isOnDemandEnable)
            startInternetOnDemandThread();
        RetryManager aretrymanager1[] = mRetryMgr;
        RetryManager retrymanager = new RetryManager("ims");
        aretrymanager1[0] = retrymanager;
        if(!mRetryMgr[0].configure("max_retries=infinite,50,60000,120000,240000,480000,900000"))
        {
            int i = Log.e("GSM", "Could not configure using LTE_SPECIFIC_DATA_RETRY_CONFIG=max_retries=infinite,50,60000,120000,240000,480000,900000");
            boolean flag4 = mRetryMgr[0].configure(20, 2000, 1000);
        }
        RetryManager aretrymanager2[] = mRetryMgr;
        RetryManager retrymanager1 = new RetryManager("default");
        aretrymanager2[1] = retrymanager1;
        if(!mRetryMgr[1].configure("max_retries=infinite,50,60000,120000,240000,480000,900000"))
        {
            int j = Log.e("GSM", "Could not configure using LTE_SPECIFIC_DATA_RETRY_CONFIG=max_retries=infinite,50,60000,120000,240000,480000,900000");
            boolean flag5 = mRetryMgr[1].configure(20, 2000, 1000);
        }
        RetryManager aretrymanager3[] = mRetryMgr;
        RetryManager retrymanager2 = new RetryManager("mms");
        aretrymanager3[2] = retrymanager2;
        if(!mRetryMgr[2].configure("max_retries=3,5000,5000,5000"))
        {
            int k = Log.e("GSM", "Could not configure using SECONDARY_DATA_RETRY_CONFIG=max_retries=3,5000,5000,5000");
            boolean flag6 = mRetryMgr[2].configure(3, 5000, 5000);
        }
        RetryManager aretrymanager4[] = mRetryMgr;
        RetryManager retrymanager3 = new RetryManager("hipri");
        aretrymanager4[3] = retrymanager3;
        if(!mRetryMgr[3].configure("max_retries=3,5000,5000,5000"))
        {
            int l = Log.e("GSM", "Could not configure using SECONDARY_DATA_RETRY_CONFIG=max_retries=3,5000,5000,5000");
            boolean flag7 = mRetryMgr[3].configure(3, 5000, 5000);
        }
        RetryManager aretrymanager5[] = mRetryMgr;
        RetryManager retrymanager4 = new RetryManager("admin");
        aretrymanager5[4] = retrymanager4;
        if(!mRetryMgr[4].configure("max_retries=3,5000,5000,5000"))
        {
            int i1 = Log.e("GSM", "Could not configure using SECONDARY_DATA_RETRY_CONFIG=max_retries=3,5000,5000,5000");
            boolean flag8 = mRetryMgr[4].configure(3, 5000, 5000);
        }
        RetryManager aretrymanager6[] = mRetryMgr;
        RetryManager retrymanager5 = new RetryManager("vzwapp");
        aretrymanager6[5] = retrymanager5;
        if(!mRetryMgr[5].configure("max_retries=3,5000,5000,5000"))
        {
            int j1 = Log.e("GSM", "Could not configure using SECONDARY_DATA_RETRY_CONFIG=max_retries=3,5000,5000,5000");
            boolean flag9 = mRetryMgr[5].configure(3, 5000, 5000);
        }
        flag10 = true;
        boolean flag11 = android.net.IConnectivityManager.Stub.asInterface(ServiceManager.getService("connectivity")).getMobileDataEnabled();
        flag10 = flag11;
        boolean flag12;
        StringBuilder stringbuilder2;
        int l1;
        String s2;
        StringBuilder stringbuilder3;
        boolean flag14;
        String s3;
        int i2;
        ImsSMSInterface imssmsinterface;
        if(!sharedpreferences.getBoolean("disabled_on_boot_key", false) && flag10)
            flag12 = true;
        else
            flag12 = false;
        autoAttach = flag12;
        if(mImsTestMode)
        {
            mAttachApnType = "default";
            boolean aflag[] = dataEnabled;
            boolean flag13 = autoAttach;
            aflag[0] = flag13;
            if(dataEnabled[0] != null)
            {
                int k1 = enabledCount + 1;
                enabledCount = k1;
            }
            mRequestedApnType = "default";
            dataEnabled[5] = false;
        } else
        {
            mAttachApnType = "ims";
            dataEnabled[5] = true;
            int j2 = enabledCount + 1;
            enabledCount = j2;
            if(!isOnDemandEnable)
            {
                boolean aflag1[] = dataEnabled;
                boolean flag15 = autoAttach;
                aflag1[0] = flag15;
                StringBuilder stringbuilder4 = (new StringBuilder()).append("GsmMultiDataConnectionTracker isOnDemandEnable: false, autoAttach: ");
                boolean flag16 = autoAttach;
                String s4 = stringbuilder4.append(flag16).toString();
                log(s4);
                if(dataEnabled[0] != null)
                {
                    int k2 = enabledCount + 1;
                    enabledCount = k2;
                }
            }
        }
        stringbuilder2 = (new StringBuilder()).append("GsmMultiDataConnectionTracker enabledCount ");
        l1 = enabledCount;
        s2 = stringbuilder2.append(l1).toString();
        log(s2);
        stringbuilder3 = (new StringBuilder()).append("[LTEpsBearerTracker] valueof Ims @ boot is  ");
        if(mImsTestMode)
            flag14 = dataEnabled[0];
        else
            flag14 = dataEnabled[5];
        s3 = stringbuilder3.append(flag14).toString();
        i2 = Log.e("GSM", s3);
        imssmsinterface = ImsSMSInterface.getInstance(gsmphone.getContext(), gsmphone);
        mImsSMSInterface = imssmsinterface;
        return;
    }

    private void addPendingApnRequest(String s)
    {
        if(mPendingRequestedApns == null)
            return;
        if(s == null)
            return;
        synchronized(mPendingRequestedApns)
        {
            if(mPendingRequestedApns.contains(s))
            {
                String s1 = (new StringBuilder()).append(s).append(" is already in list").toString();
                log(s1);
            } else
            {
                String s2 = (new StringBuilder()).append("type ").append(s).append(" is added to list").toString();
                log(s2);
                boolean flag = mPendingRequestedApns.add(s);
            }
        }
    }

    private void addPendingIpv6DataCallList(DataCallState datacallstate)
    {
        if(mPendingIpv6DataCallList == null)
            return;
        if(datacallstate == null)
        {
            return;
        } else
        {
            String s = (new StringBuilder()).append("DataCallState ").append(datacallstate).append(" is added to list").toString();
            log(s);
            Map map = mPendingIpv6DataCallList;
            String s1 = datacallstate.apn;
            Object obj = map.put(s1, datacallstate);
            return;
        }
    }

    private String apnListToString(ArrayList arraylist)
    {
        StringBuilder stringbuilder = new StringBuilder();
        int i = 0;
        for(int j = arraylist.size(); i < j; i++)
        {
            StringBuilder stringbuilder1 = stringbuilder.append('[');
            String s = ((ApnSetting)arraylist.get(i)).toString();
            StringBuilder stringbuilder2 = stringbuilder1.append(s).append(']');
        }

        return stringbuilder.toString();
    }

    private ArrayList buildWaitingApns()
    {
        ArrayList arraylist = new ArrayList();
        if (!mCurrentRequestedApnType.equals("dun")) {
            String s = mGsmPhone.mSIMRecords.getSIMOperatorNumeric();
            if(!mCurrentRequestedApnType.equals("default") || !canSetPreferApn || preferredApn == null)
                break; /* Loop/switch isn't completed */
            StringBuilder stringbuilder = (new StringBuilder()).append("Preferred APN:").append(s).append(":");
            String s1 = preferredApn.numeric;
            StringBuilder stringbuilder1 = stringbuilder.append(s1).append(":");
            ApnSetting apnsetting1 = preferredApn;
            String s2 = stringbuilder1.append(apnsetting1).toString();
            log(s2);
            log("Waiting APN set to preferred APN");
            ApnSetting apnsetting2 = preferredApn;
            boolean flag1 = arraylist.add(apnsetting2);
            if (!flag1) {
                if(allApns != null)
                {
                    Iterator iterator = allApns.iterator();
                    while(iterator.hasNext()) 
                    {
                        ApnSetting apnsetting3 = (ApnSetting)iterator.next();
                        String s3 = mCurrentRequestedApnType;
                        if(apnsetting3.canHandleType(s3) && apnsetting3.isDisable == 0)
                        {
                            StringBuilder stringbuilder2 = (new StringBuilder()).append("buildWaitingApns adding apn type ");
                            String s4 = apnsetting3.types[0];
                            String s5 = stringbuilder2.append(s4).toString();
                            log(s5);
                            boolean flag2 = arraylist.add(apnsetting3);
                        }
                    }
                }
            }
            return arraylist;
        } else {
            ApnSetting apnsetting = fetchDunApn();
            boolean flag;
            if(apnsetting != null)
                flag = arraylist.add(apnsetting);
            return arraylist;
        }
    }

    private void cleanUpConnection(boolean flag, String s)
    {
        String s1 = (new StringBuilder()).append("Clean up connection due to ").append(s).toString();
        log(s1);
        if(mReconnectIntent != null)
        {
            AlarmManager alarmmanager = (AlarmManager)phone.getContext().getSystemService("alarm");
            PendingIntent pendingintent = mReconnectIntent;
            alarmmanager.cancel(pendingintent);
            mReconnectIntent = null;
        }
        com.android.internal.telephony.DataConnectionTracker.State state = com.android.internal.telephony.DataConnectionTracker.State.DISCONNECTING;
        setState(state);
        boolean flag1 = false;
        mCleanupCount = 0;
        for(Iterator iterator = pdpList.iterator(); iterator.hasNext();)
        {
            DataConnection dataconnection = (DataConnection)iterator.next();
            int i = mCleanupCount + 1;
            mCleanupCount = i;
            GsmDataConnection gsmdataconnection = (GsmDataConnection)dataconnection;
            if(flag)
            {
                log("cleanUpConnection: teardown, call conn.disconnect");
                if(gsmdataconnection != null && gsmdataconnection.getApn() != null && gsmdataconnection.getApn().canHandleType("ims"))
                {
                    imsDeregistrationOnDisconnect(s);
                } else
                {
                    Message message = obtainMessage(25, s);
                    gsmdataconnection.disconnect(message);
                }
                flag1 = true;
            } else
            {
                log("cleanUpConnection: !tearDown, call conn.resetSynchronously");
                dataconnection.resetSynchronously();
                flag1 = false;
            }
        }

        stopNetStatPoll();
        if(flag1)
        {
            return;
        } else
        {
            log("cleanupConnection: !notificationDeferred");
            gotoIdleAndNotifyDataConnection(s);
            return;
        }
    }

    private void clearAllPendingApnRequest()
    {
        log("clearAllPendingApnRequest");
        if(mPendingRequestedApns == null)
            return;
        if(mPendingRequestedApns.isEmpty())
            return;
        synchronized(mPendingRequestedApns)
        {
            mPendingRequestedApns.clear();
        }
    }

    private void clearAllPendingIpv6DataCallList()
    {
        mPendingIpv6DataCallList.clear();
    }

    private void createAllApnList()
    {
        boolean flag;
        flag = true;
        if(allApns != null && (mIsImsEnabled && mIsAdminEnabled || mImsTestMode))
            flag = false;
        if(!flag)
        {
            ArrayList arraylist = allApns;
            ArrayList arraylist1 = new ArrayList(arraylist);
            backup_allApns = arraylist1;
        }
        ArrayList arraylist2 = new ArrayList();
        allApns = arraylist2;
        ContentResolver contentresolver = phone.getContext().getContentResolver();
        Uri uri = android.provider.Telephony.Carriers.CONTENT_URI;
        String as[] = null;
        String s = null;
        Cursor cursor = contentresolver.query(uri, null, "apn_bearer = 'LTE'", as, s);
        if(cursor != null)
        {
            if(cursor.getCount() > 0)
            {
                mIsImsEnabled = false;
                mIsAdminEnabled = false;
                ArrayList arraylist3 = createApnList(cursor);
                allApns = arraylist3;
                int i = mGsmPhone.mSST.getCurrentGprsState();
                cur_gprsState = i;
                boolean flag1;
                com.android.internal.telephony.DataConnection.FailCause failcause;
                if(!PreferenceManager.getDefaultSharedPreferences(phone.getContext()).getBoolean("disabled_on_boot_key", false) && (mIsImsEnabled && mIsAdminEnabled || mImsTestMode) && cur_gprsState != 0 && flag)
                {
                    sendPsAttachInfo();
                } else
                {
                    StringBuilder stringbuilder = (new StringBuilder()).append("createAllApnList not ready for attach,  ImsEnabled: ");
                    boolean flag2 = mIsImsEnabled;
                    StringBuilder stringbuilder1 = stringbuilder.append(flag2).append(" ADMINEnabled: ");
                    boolean flag3 = mIsAdminEnabled;
                    StringBuilder stringbuilder2 = stringbuilder1.append(flag3).append(" mImsTestMode: ");
                    boolean flag4 = mImsTestMode;
                    StringBuilder stringbuilder3 = stringbuilder2.append(flag4).append(" gprsState: ");
                    int j = cur_gprsState;
                    String s1 = stringbuilder3.append(j).append(" firstTime: ").append(flag).toString();
                    int k = Log.d("GSM", s1);
                }
            }
            cursor.close();
        }
        if (mIsImsEnabled && mIsAdminEnabled || getActiveApnCount() <= 0) {
            if (flag) {
                Iterator iterator;
                ApnSetting apnsetting;
                Iterator iterator1;
                ApnSetting apnsetting1;
                String s2;
                String s3;
                if(allApns.isEmpty())
                {
                    log("No APN found");
                    preferredApn = null;
                    failcause = com.android.internal.telephony.DataConnection.FailCause.MISSING_UNKNOWN_APN;
                    notifyNoData(failcause);
                    phone.notifyDataConnection("apnFailed");
                    return;
                } else
                {
                    ApnSetting apnsetting2 = getPreferredApn();
                    preferredApn = apnsetting2;
                    log("Get PreferredAPN");
                    return;
                }
            } else {
                iterator = backup_allApns.iterator();
                while (iterator.hasNext()) {
                    apnsetting = (ApnSetting)iterator.next();
                    iterator1 = allApns.iterator();
                    while (iterator1.hasNext()) {
                        apnsetting1 = (ApnSetting)iterator1.next();
                        s2 = apnsetting.types[0];
                        s3 = apnsetting1.types[0];
                        if(s2.equals(s3) || apnsetting.changeExceptStatus(apnsetting1)) {
                            String s4 = apnsetting.types[0];
                            if(!isApnTypeActive(s4))
                                break;
                            StringBuilder stringbuilder4 = (new StringBuilder()).append("Something has changed for the initially active APN ");
                            String s5 = apnsetting.types[0];
                            String s6 = stringbuilder4.append(s5).append(" so disconnecting.").toString();
                            log(s6);
                            String s7 = apnsetting.types[0];
                            disconnectByApntype(s7);
                        } 
                        String s8 = mAttachApnType;
                        if(apnsetting.canHandleType(s8))
                            sendPsAttachInfo();
                    }
                }
            }
        } else {
            flag1 = explicitDetach(0, 0);
            Iterator iterator;
            ApnSetting apnsetting;
            Iterator iterator1;
            ApnSetting apnsetting1;
            String s2;
            String s3;
            if(allApns.isEmpty())
            {
                log("No APN found");
                preferredApn = null;
                failcause = com.android.internal.telephony.DataConnection.FailCause.MISSING_UNKNOWN_APN;
                notifyNoData(failcause);
                phone.notifyDataConnection("apnFailed");
                return;
            } else {
                ApnSetting apnsetting2 = getPreferredApn();
                preferredApn = apnsetting2;
                log("Get PreferredAPN");
                return;
            }
        }
    }

    private void createAllPdpList()
    {
        ArrayList arraylist = new ArrayList();
        pdpList = arraylist;
        int i = 0;
        do
        {
            if(i >= 4)
                return;
            GsmDataConnection gsmdataconnection = GsmDataConnection.makeDataConnection(mGsmPhone);
            boolean flag = pdpList.add(gsmdataconnection);
            i++;
        } while(true);
    }

    private ArrayList createApnList(Cursor cursor)
    {
        ArrayList arraylist = new ArrayList();
        if (cursor.moveToFirst()) {
            String as[];
            ApnSetting apnsetting;
            int i9;
            int j9;
            Cursor cursor1 = cursor;
            String s = "type";
            int i = cursor1.getColumnIndexOrThrow(s);
            Cursor cursor2 = cursor;
            int j = i;
            String s1 = cursor2.getString(j);
            GsmMultiDataConnectionTracker gsmmultidataconnectiontracker = this;
            String s2 = s1;
            as = gsmmultidataconnectiontracker.parseTypes(s2);
            Cursor cursor3 = cursor;
            String s3 = "statustype";
            int k = cursor3.getColumnIndexOrThrow(s3);
            Cursor cursor4 = cursor;
            int l = k;
            int i1 = cursor4.getInt(l);
            Cursor cursor5 = cursor;
            String s4 = "inactivity_timer";
            int j1 = cursor5.getColumnIndexOrThrow(s4);
            Cursor cursor6 = cursor;
            int k1 = j1;
            int l1 = cursor6.getInt(k1);
            Cursor cursor7 = cursor;
            String s5 = "_id";
            int i2 = cursor7.getColumnIndexOrThrow(s5);
            Cursor cursor8 = cursor;
            int j2 = i2;
            int k2 = cursor8.getInt(j2);
            Cursor cursor9 = cursor;
            String s6 = "numeric";
            int l2 = cursor9.getColumnIndexOrThrow(s6);
            Cursor cursor10 = cursor;
            int i3 = l2;
            String s7 = cursor10.getString(i3);
            Cursor cursor11 = cursor;
            String s8 = "name";
            int j3 = cursor11.getColumnIndexOrThrow(s8);
            Cursor cursor12 = cursor;
            int k3 = j3;
            String s9 = cursor12.getString(k3);
            Cursor cursor13 = cursor;
            String s10 = "apn";
            int l3 = cursor13.getColumnIndexOrThrow(s10);
            Cursor cursor14 = cursor;
            int i4 = l3;
            String s11 = cursor14.getString(i4);
            Cursor cursor15 = cursor;
            String s12 = "proxy";
            int j4 = cursor15.getColumnIndexOrThrow(s12);
            Cursor cursor16 = cursor;
            int k4 = j4;
            String s13 = cursor16.getString(k4);
            Cursor cursor17 = cursor;
            String s14 = "port";
            int l4 = cursor17.getColumnIndexOrThrow(s14);
            Cursor cursor18 = cursor;
            int i5 = l4;
            String s15 = cursor18.getString(i5);
            Cursor cursor19 = cursor;
            String s16 = "mmsc";
            int j5 = cursor19.getColumnIndexOrThrow(s16);
            Cursor cursor20 = cursor;
            int k5 = j5;
            String s17 = cursor20.getString(k5);
            Cursor cursor21 = cursor;
            String s18 = "mmsproxy";
            int l5 = cursor21.getColumnIndexOrThrow(s18);
            Cursor cursor22 = cursor;
            int i6 = l5;
            String s19 = cursor22.getString(i6);
            Cursor cursor23 = cursor;
            String s20 = "mmsport";
            int j6 = cursor23.getColumnIndexOrThrow(s20);
            Cursor cursor24 = cursor;
            int k6 = j6;
            String s21 = cursor24.getString(k6);
            Cursor cursor25 = cursor;
            String s22 = "user";
            int l6 = cursor25.getColumnIndexOrThrow(s22);
            Cursor cursor26 = cursor;
            int i7 = l6;
            String s23 = cursor26.getString(i7);
            Cursor cursor27 = cursor;
            String s24 = "password";
            int j7 = cursor27.getColumnIndexOrThrow(s24);
            Cursor cursor28 = cursor;
            int k7 = j7;
            String s25 = cursor28.getString(k7);
            Cursor cursor29 = cursor;
            String s26 = "authtype";
            int l7 = cursor29.getColumnIndexOrThrow(s26);
            Cursor cursor30 = cursor;
            int i8 = l7;
            int j8 = cursor30.getInt(i8);
            Cursor cursor31 = cursor;
            String s27 = "ipversion";
            int k8 = cursor31.getColumnIndexOrThrow(s27);
            Cursor cursor32 = cursor;
            int l8 = k8;
            String s28 = cursor32.getString(l8);
            apnsetting = new ApnSetting(k2, s7, s9, s11, s13, s15, s17, s19, s21, s23, s25, j8, s28, l1, i1, as);
            i9 = i1;
            j9 = 1;
            if(i9 == j9) { goto _L4; else goto _L3
                String as1[] = as;
                int k9 = as1.length;
                int j10 = 0;
                do
                {
                    int i11 = j10;
                    int j11 = k9;
                    if(i11 >= j11)
                        break;
                    String s29 = as1[j10];
                    String s30 = s29;
                    String s31 = "ims";
                    if(s30.equals(s31))
                    {
                        boolean flag = false;
                        mIsImsEnabled = flag;
                    }
                    String s32 = s29;
                    String s33 = "admin";
                    if(s32.equals(s33))
                    {
                        boolean flag1 = false;
                        mIsAdminEnabled = flag1;
                    }
                    StringBuilder stringbuilder = (new StringBuilder()).append("mIsImsEnabled = ");
                    boolean flag2 = mIsImsEnabled;
                    StringBuilder stringbuilder1 = stringbuilder.append(flag2).append(" mIsAdminEnabled = ");
                    boolean flag3 = mIsAdminEnabled;
                    String s34 = stringbuilder1.append(flag3).toString();
                    int k11 = Log.d("GSM", s34);
                    GsmMultiDataConnectionTracker gsmmultidataconnectiontracker1 = this;
                    String s35 = s29;
                    if(gsmmultidataconnectiontracker1.isApnTypeActive(s35) && mIsImsEnabled && mIsAdminEnabled)
                    {
                        String s36 = "GSM";
                        StringBuilder stringbuilder2 = (new StringBuilder()).append("GSMDCT createApnList : isDisabled ");
                        boolean flag4;
                        StringBuilder stringbuilder3;
                        String s37;
                        StringBuilder stringbuilder4;
                        String s38;
                        String s39;
                        int l11;
                        GsmMultiDataConnectionTracker gsmmultidataconnectiontracker2;
                        String s40;
                        if(apnsetting.isDisable == 1)
                            flag4 = true;
                        else
                            flag4 = false;
                        stringbuilder3 = stringbuilder2.append(flag4).append(" disabling APN ");
                        s37 = apnsetting.apn;
                        stringbuilder4 = stringbuilder3.append(s37).append(" apnType ");
                        s38 = s29;
                        s39 = stringbuilder4.append(s38).toString();
                        l11 = Log.e(s36, s39);
                        gsmmultidataconnectiontracker2 = this;
                        s40 = s29;
                        gsmmultidataconnectiontracker2.disconnectByApntype(s40);
                    }
                    j10++;
                } while(true);
                String s41 = "GSM";
                StringBuilder stringbuilder5 = (new StringBuilder()).append("GSMDCT createApnList : isDisabled ");
                boolean flag5;
                StringBuilder stringbuilder6;
                String s42;
                StringBuilder stringbuilder7;
                int i12;
                String s43;
                int j12;
                if(apnsetting.isDisable == 1)
                    flag5 = true;
                else
                    flag5 = false;
                stringbuilder6 = stringbuilder5.append(flag5).append(" excluding APN ");
                s42 = apnsetting.apn;
                stringbuilder7 = stringbuilder6.append(s42).append(" inactivityTimer ");
                i12 = apnsetting.inactivityValue;
                s43 = stringbuilder7.append(i12).toString();
                j12 = Log.e(s41, s43);
                if(cursor.moveToNext())
                    continue; /* Loop/switch isn't completed */
                return arraylist;
            } else {
                String s44 = "GSM";
                StringBuilder stringbuilder8 = (new StringBuilder()).append("GSMDCT createApnList : isDisabled : ");
                String as2[];
                int l9;
                int k10;
                boolean flag6;
                StringBuilder stringbuilder9;
                String s45;
                StringBuilder stringbuilder10;
                int k12;
                String s46;
                int l12;
                if(apnsetting.isDisable == 1)
                    flag6 = true;
                else
                    flag6 = false;
                stringbuilder9 = stringbuilder8.append(flag6).append(" adding APN ");
                s45 = apnsetting.apn;
                stringbuilder10 = stringbuilder9.append(s45).append(" inactivityTimer ");
                k12 = apnsetting.inactivityValue;
                s46 = stringbuilder10.append(k12).toString();
                l12 = Log.e(s44, s46);
                as2 = as;
                l9 = as2.length;
                k10 = 0;
                do
                {
                    int i13 = k10;
                    int j13 = l9;
                    if(i13 >= j13)
                        break;
                    String s47 = as2[k10];
                    String s48 = s47;
                    String s49 = "ims";
                    if(s48.equals(s49))
                    {
                        boolean flag7 = true;
                        mIsImsEnabled = flag7;
                    }
                    String s50 = s47;
                    String s51 = "admin";
                    if(s50.equals(s51))
                    {
                        boolean flag8 = true;
                        mIsAdminEnabled = flag8;
                    }
                    StringBuilder stringbuilder11 = (new StringBuilder()).append("mIsImsEnabled = ");
                    boolean flag9 = mIsImsEnabled;
                    StringBuilder stringbuilder12 = stringbuilder11.append(flag9).append(" mIsAdminEnabled = ");
                    boolean flag10 = mIsAdminEnabled;
                    String s52 = stringbuilder12.append(flag10).toString();
                    int k13 = Log.d("GSM", s52);
                    int l13 = k10 + 1;
                } while(true);
                StringBuilder stringbuilder13 = (new StringBuilder()).append("isOnDemandEnable = ");
                boolean flag11 = isOnDemandEnable;
                StringBuilder stringbuilder14 = stringbuilder13.append(flag11).append(" mImsTestMode = ");
                boolean flag12 = mImsTestMode;
                StringBuilder stringbuilder15 = stringbuilder14.append(flag12).append(" isApnTypeInactive(Phone.APN_TYPE_DEFAULT) = ");
                GsmMultiDataConnectionTracker gsmmultidataconnectiontracker3 = this;
                String s53 = "default";
                boolean flag13 = gsmmultidataconnectiontracker3.isApnTypeInactive(s53);
                String s54 = stringbuilder15.append(flag13).toString();
                int i14 = Log.d("GSM", s54);
                if(isOnDemandEnable || !mImsTestMode) {
                    // Too much work for now...
                    return arraylist;
                } else {
                    // Too much work for now...
                    return arraylist;
                }
            }
        } else {
            return arraylist;
        }
    }

    private void createPendingIpv6DataCallList()
    {
        log("createPendingIpv6DataCallList");
        Hashtable hashtable = new Hashtable();
        mPendingIpv6DataCallList = hashtable;
    }

    private void createPendingRequestedApnList()
    {
        ArrayList arraylist = new ArrayList();
        mPendingRequestedApns = arraylist;
    }

    private void dequeOnDemandPdnRequest()
    {
        log("dequeOnDemandPdnRequest onDemand-IMS configuration dequeOnDemandPdnRequest LTE Attached");
        removeMessages(48);
        if(!qOnDemandPdnRequestQueue.isEmpty())
        {
            log("dequeOnDemandPdnRequest onDemand-IMS configuration queue is NOT empty");
            do
            {
                String s;
                int j;
                do
                {
                    do
                    {
                        do
                        {
                            if(qOnDemandPdnRequestQueue.isEmpty())
                                return;
                            s = (String)qOnDemandPdnRequestQueue.poll();
                        } while(s == null);
                        String s1 = (new StringBuilder()).append("dequeOnDemandPdnRequest pdn type :").append(s).append("Add it to the master req list").toString();
                        int i = Log.e("GSM", s1);
                    } while(!isApnTypeInactive(s));
                    j = apnTypeToId(s);
                } while(!isEnabled(j));
                addPendingApnRequest(s);
                Message message = obtainMessage(59);
                boolean flag = sendMessage(message);
            } while(true);
        } else
        {
            int k = Log.e("GSM", "onDemand-IMS configuration queue is empty");
            return;
        }
    }

    private void destroyAllPdpList()
    {
        if(pdpList == null)
        {
            return;
        } else
        {
            ArrayList arraylist = pdpList;
            ArrayList arraylist1 = pdpList;
            boolean flag = arraylist.removeAll(arraylist1);
            return;
        }
    }

    private void doRecovery()
    {
        if(!isAnyApnTypeActive())
            return;
        int i = android.provider.Settings.Secure.getInt(mResolver, "pdp_watchdog_max_pdp_reset_fail_count", 15);
        if(mIsVoiceCallConnected && mPdpResetCount >= i)
        {
            int j = i - 1;
            mPdpResetCount = j;
        }
        if(mPdpResetCount < i)
        {
            int k = mPdpResetCount + 1;
            mPdpResetCount = k;
            long l = sentSinceLastRecv;
            int i1 = EventLog.writeEvent(50102, l);
            cleanUpConnection(true, "pdpReset");
            return;
        } else
        {
            mPdpResetCount = 0;
            ((MultiModePhoneProxy)PhoneFactory.getDefaultPhone()).sendResetCommand("APGMDCTPOLL");
            return;
        }
    }

    private void emptyOnDemandPdnRequestQueue()
    {
        log("emptyOnDemandPdnRequestQueue");
        if(qOnDemandPdnRequestQueue.isEmpty())
        {
            return;
        } else
        {
            log("onDemand-IMS configuration queue is NOT empty so clear it");
            qOnDemandPdnRequestQueue.clear();
            return;
        }
    }

    private ApnSetting fetchDunApn()
    {
        Context context = phone.getContext();
        ApnSetting apnsetting = ApnSetting.fromString(android.provider.Settings.Secure.getString(context.getContentResolver(), "tether_dun_apn"));
        ApnSetting apnsetting1;
        if(apnsetting != null)
            apnsetting1 = apnsetting;
        else
            apnsetting1 = ApnSetting.fromString(context.getResources().getString(0x1040017));
        return apnsetting1;
    }

    private GsmDataConnection findFreePdp()
    {
        int i;
        Iterator iterator;
        i = 0;
        iterator = pdpList.iterator();
        while (iterator.hasNext()) {
            GsmDataConnection gsmdataconnection;
            String s;
            gsmdataconnection = (GsmDataConnection)(DataConnection)iterator.next();
            s = mCurrentRequestedApnType;
            if (gsmdataconnection.canHandleType(s)) {
                GsmDataConnection gsmdataconnection2;
                String s1 = (new StringBuilder()).append("Free pdp found: idx(").append(i).append(")").toString();
                log(s1);
                gsmdataconnection2 = gsmdataconnection;
                return gsmdataconnection2;
            }
            i++;
        }
        i = 0;
        iterator = pdpList.iterator();
        while (iterator.hasNext()) {
            i++;
            GsmDataConnection gsmdataconnection1 = (GsmDataConnection)(DataConnection)iterator.next();
            if (!gsmdataconnection1.isInactive())
                continue;
            StringBuilder stringbuilder = (new StringBuilder()).append("Free pdp found: idx(").append(i).append("), state(");
            String s2 = gsmdataconnection1.getStateAsString();
            String s3 = stringbuilder.append(s2).append(")").toString();
            log(s3);
            gsmdataconnection2 = gsmdataconnection1;
        }
        gsmdataconnection2 = null;
        return gsmdataconnection2;
    }

    private boolean findPendingIpv6DataCallStateByApn(String s)
    {
        boolean flag;
        if((DataCallState)mPendingIpv6DataCallList.get(s) == null)
            flag = false;
        else
            flag = true;
        return flag;
    }

    private void forceDataDormancy()
    {
        ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
        DataOutputStream dataoutputstream = new DataOutputStream(bytearrayoutputstream);
        inactivityPeriod = 0L;
        if(dormancyState)
            return;
        dormancyState = true;
        if(!capaDormancy)
            return;
        log("[FD] ======= ENTER DORMANCY =======");
        byte byte0 = 9;
        PhoneBase phonebase;
        byte abyte0[];
        Message message;
        try
        {
            dataoutputstream.writeByte(byte0);
            dataoutputstream.writeByte(1);
            dataoutputstream.writeShort(4);
        }
        catch(IOException ioexception)
        {
            log("IOException!!!");
            return;
        }
        phonebase = phone;
        abyte0 = bytearrayoutputstream.toByteArray();
        message = obtainMessage(1000);
        phonebase.invokeOemRilRequestRaw(abyte0, message);
    }

    private int getActiveApnCount()
    {
        int i = 0;
        Iterator iterator = pdpList.iterator();
        do
        {
            if(!iterator.hasNext())
                break;
            GsmDataConnection gsmdataconnection = (GsmDataConnection)(DataConnection)iterator.next();
            if(gsmdataconnection != null && gsmdataconnection.getApn() != null && (gsmdataconnection.isActive() || gsmdataconnection.isActivating()))
            {
                i++;
                String s = (new StringBuilder()).append("getActiveApnCount  ").append(i).toString();
                log(s);
            }
        } while(true);
        String s1 = (new StringBuilder()).append("getActiveApnCount  ").append(i).toString();
        log(s1);
        return i;
    }

    private String[] getApnTypeByApnName(String s)
    {
        if (s == null) {
            String as[] = null;
            return as;
        } else {
label0:
            {
                if (allApns == null) {
                    String as[] = null;
                    return as;
                } else {
                    Iterator iterator = allApns.iterator();
                    do {
                        if (!iterator.hasNext())
                            break label0;
                        apnsetting = (ApnSetting)iterator.next();
                    } while (!apnsetting.apn.equals(s))
                    as = apnsetting.types;
                    return as;
                }
            }
            as = null;
            return as;
        }
    }

    private GsmDataConnection getConnectionByApnType(String s)
    {
        GsmDataConnection gsmdataconnection = null;
        if (s == null) {
            return gsmdataconnection;
        } else {
            for(Iterator iterator = pdpList.iterator(); iterator.hasNext();)
            {
                GsmDataConnection gsmdataconnection1 = (GsmDataConnection)(DataConnection)iterator.next();
                if(gsmdataconnection1.canHandleType(s))
                {
                    gsmdataconnection = gsmdataconnection1;
                    return gsmdataconnection;
                }
            }
        }
    }

    private void getDormancyEnableFlag()
    {
        Cursor cursor;
        log("[FD] ON default: true");
        mDormFlag = "on";
        String s = SystemProperties.get("gsm.operator.numeric");
        String s1 = new String(s);
        mOperatorNumeric = s1;
        if(mOperatorNumeric == null)
        {
            mDormFlag = "on";
            log("[FD]: No op numeric");
            return;
        }
        if(mOperatorNumeric.equals("45001"))
        {
            mDormFlag = "off";
            return;
        }
        SharedPreferences sharedpreferences = phone.getContext().getSharedPreferences("fdormancy.preferences_name", 3);
        if(sharedpreferences != null)
        {
            String s2 = sharedpreferences.getString("fdormancy.key.mccmnc", "");
            boolean flag = sharedpreferences.getBoolean("fdormancy.key.state", true);
            if(s2 != null && s2.length() > 0)
            {
                String s3 = mOperatorNumeric;
                if(s2.equals(s3))
                {
                    String s4;
                    StringBuilder stringbuilder;
                    String s5;
                    String s6;
                    if(flag)
                        s4 = "on";
                    else
                        s4 = "off";
                    mDormFlag = s4;
                    stringbuilder = (new StringBuilder()).append("[FD] Dormant flag(");
                    s5 = mDormFlag;
                    s6 = stringbuilder.append(s5).append(") from key string").toString();
                    log(s6);
                    return;
                }
            }
        }
        File file = new File("/data/data/com.android.providers.telephony", "nwk_info.db");
        SQLiteDatabase sqlitedatabase;
        SQLiteDatabase sqlitedatabase1;
        StringBuilder stringbuilder2;
        String s9;
        String s10;
        int i;
        StringBuilder stringbuilder3;
        String s11;
        String s12;
        StringBuilder stringbuilder4;
        String s13;
        String s14;
        SQLiteException sqliteexception;
        if(!file.exists())
        {
            file = new File("/system/csc", "nwk_info.db");
            if(!file.exists())
            {
                log("[FD] no nwk info db");
                StringBuilder stringbuilder1 = (new StringBuilder()).append("[FD] Dormant flag(");
                String s7 = mDormFlag;
                String s8 = stringbuilder1.append(s7).append(")").toString();
                log(s8);
                return;
            }
            log("[FD] csc system area");
        } else
        {
            log("[FD] provider data area");
        }
        try
        {
            sqlitedatabase = SQLiteDatabase.openDatabase(file.getPath(), null, 1);
        }
        catch(SQLiteException sqliteexception1)
        {
            log("[FD] nwk info db open exception");
            return;
        }
        sqlitedatabase1 = sqlitedatabase;
        stringbuilder2 = (new StringBuilder()).append("plmn = '");
        s9 = mOperatorNumeric;
        s10 = stringbuilder2.append(s9).append("'").toString();
        cursor = sqlitedatabase1.query("nwkinfo", null, s10, null, null, null, null);
        if(cursor == null) goto _L2; else goto _L1
_L1:
        if(!cursor.moveToFirst()) goto _L4; else goto _L3
_L3:
        i = cursor.getColumnIndexOrThrow("dormancy");
        mDormFlag = cursor.getString(i);
        if(mDormFlag == null) goto _L6; else goto _L5
_L5:
        stringbuilder3 = (new StringBuilder()).append("[FD] read from DB: dormancy(");
        s11 = mDormFlag;
        s12 = stringbuilder3.append(s11).append(")").toString();
        log(s12);
        if(!mDormFlag.equals("off")) goto _L6; else goto _L4
_L4:
        cursor.close();
_L2:
        if(sqlitedatabase1 != null)
            sqlitedatabase1.close();
        stringbuilder4 = (new StringBuilder()).append("[FD] Dormant flag(");
        s13 = mDormFlag;
        s14 = stringbuilder4.append(s13).append(")").toString();
        log(s14);
        return;
_L6:
        boolean flag1;
        try
        {
            flag1 = cursor.moveToNext();
        }
        // Misplaced declaration of an exception variable
        catch(SQLiteException sqliteexception)
        {
            log("[FD] Exception during query");
            return;
        }
        if(flag1) goto _L3; else goto _L4
    }

    private ApnSetting getNextApn()
    {
        ArrayList arraylist = waitingApns;
        ApnSetting apnsetting = null;
        if(arraylist != null && !arraylist.isEmpty())
            apnsetting = (ApnSetting)arraylist.get(0);
        return apnsetting;
    }

    private String getNextPendingApnRequest()
    {
        if(mPendingRequestedApns != null) goto _L2; else goto _L1
_L1:
        String s = null;
_L5:
        return s;
_L2:
        ArrayList arraylist = mPendingRequestedApns;
        arraylist;
        JVM INSTR monitorenter ;
        String s1;
        do
        {
            if(mPendingRequestedApns.isEmpty())
                break MISSING_BLOCK_LABEL_99;
            s1 = (String)mPendingRequestedApns.remove(0);
            if(!isApnTypeActive(s1))
                break;
            String s2 = (new StringBuilder()).append("type ").append(s1).append(" is already active").toString();
            log(s2);
        } while(true);
          goto _L3
        Exception exception;
        exception;
        arraylist;
        JVM INSTR monitorexit ;
        throw exception;
_L3:
        arraylist;
        JVM INSTR monitorexit ;
        s = s1;
        continue; /* Loop/switch isn't completed */
        arraylist;
        JVM INSTR monitorexit ;
        s = null;
        if(true) goto _L5; else goto _L4
_L4:
    }

    private ApnSetting getPreferredApn()
    {
        ApnSetting apnsetting;
        if(allApns == null || allApns.isEmpty())
            apnsetting = null;
        else
            apnsetting = null;
        return apnsetting;
    }

    private void gotoIdleAndNotifyDataConnection(String s)
    {
        String s1 = (new StringBuilder()).append("gotoIdleAndNotifyDataConnection: reason=").append(s).toString();
        log(s1);
        com.android.internal.telephony.DataConnectionTracker.State state = com.android.internal.telephony.DataConnectionTracker.State.IDLE;
        setState(state);
        phone.notifyDataConnection(s);
        mActiveApn = null;
        if(waitingApns == null)
        {
            return;
        } else
        {
            ArrayList arraylist = waitingApns;
            ArrayList arraylist1 = waitingApns;
            boolean flag = arraylist.removeAll(arraylist1);
            return;
        }
    }

    private boolean handleAsyncConnect(String s)
    {
        log("handleAsyncConnect");
        int i = apnTypeToId(s);
        boolean flag1;
        if(isEnabled(i))
        {
            addPendingApnRequest(s);
            Message message = obtainMessage(59);
            boolean flag = sendMessage(message);
            flag1 = true;
        } else
        {
            String s1 = (new StringBuilder()).append("handleAsyncConnect: ").append(s).append(" is not enabled").toString();
            log(s1);
            flag1 = false;
        }
        return flag1;
    }

    private boolean handleAsyncDisconnect(int i, int j)
    {
        Iterator iterator;
        String s = (new StringBuilder()).append("handleAsyncDisconnect").append(i).append(" ").append(j).toString();
        log(s);
        iterator = pdpList.iterator();
_L4:
        if(!iterator.hasNext()) goto _L2; else goto _L1
_L1:
        GsmDataConnection gsmdataconnection;
        gsmdataconnection = (GsmDataConnection)(DataConnection)iterator.next();
        StringBuilder stringbuilder = (new StringBuilder()).append("handleAsyncDisconnect");
        int k = gsmdataconnection.getCid();
        StringBuilder stringbuilder1 = stringbuilder.append(k).append(" ");
        boolean flag = gsmdataconnection.isActive();
        String s1 = stringbuilder1.append(flag).toString();
        log(s1);
        if(!gsmdataconnection.isActive() || gsmdataconnection.getCid() == i) goto _L4; else goto _L3
_L3:
        log("handleAsyncDisconnect  Calling Disconnect now");
        if(j != 0) goto _L6; else goto _L5
_L5:
        log("handleAsyncDisconnect: Normal disconnect");
        if(gsmdataconnection.canHandleType("ims"))
        {
            mImsSMSInterface.unregisterImsOnImsPdnDetach();
            gsmdataconnection.resetSynchronously();
            phone.notifyDataConnection(null);
            com.android.internal.telephony.DataConnectionTracker.State state = com.android.internal.telephony.DataConnectionTracker.State.IDLE;
            setState(state);
            trySetupAllEnabledServices();
        } else
        {
            Message message = obtainMessage(25, "pdndroppedbyNetwork");
            gsmdataconnection.disconnect(message);
        }
_L2:
        return true;
_L6:
        if(j == 1)
        {
            log("handleAsyncDisconnect: Handover disconnect");
            Message message1 = obtainMessage(25, "handoverdisconncted");
            gsmdataconnection.disconnect(message1);
        } else
        if(j == 2)
        {
            log("handleAsyncDisconnect: Regular  disconnect");
            Message message2 = obtainMessage(25, "regulardisconncted");
            gsmdataconnection.disconnect(message2);
        }
        if(true) goto _L2; else goto _L7
_L7:
    }

    private void handleIpv6AddressConfigured(GsmDataConnection gsmdataconnection)
    {
        boolean flag;
        flag = false;
        if(gsmdataconnection == null || gsmdataconnection.getApn() == null)
        {
            log("handleIpv6AddressConfigured: conn is null");
            return;
        }
        gsmdataconnection;
        JVM INSTR monitorenter ;
        if(gsmdataconnection.getApn() != null)
            break MISSING_BLOCK_LABEL_45;
        log("handleIpv6AddressConfigured: apn is null");
        gsmdataconnection;
        JVM INSTR monitorexit ;
        return;
        Exception exception;
        exception;
        gsmdataconnection;
        JVM INSTR monitorexit ;
        throw exception;
        if(!gsmdataconnection.isActive())
        {
            StringBuilder stringbuilder = (new StringBuilder()).append("handleIpv6AddressConfigured: connection's state is not active, ");
            String s = gsmdataconnection.getStateAsString();
            String s1 = stringbuilder.append(s).toString();
            log(s1);
            return;
        }
        if(gsmdataconnection.isipv6configured == 1)
            break MISSING_BLOCK_LABEL_112;
        log("IPv4 is not yet configured");
        gsmdataconnection;
        JVM INSTR monitorexit ;
        return;
        setDefaultPdpContextProperty(gsmdataconnection);
        int i = Log.e("GSM", "starting InactivityTimer : handleIpv6AddressConfigured ");
        int j = gsmdataconnection.getApn().inactivityValue;
        String s2 = gsmdataconnection.getApn().types[0];
        String s3 = getInterfaceName(s2);
        startInactivityTimer(j, s2, s3, gsmdataconnection);
        mActivePdp = gsmdataconnection;
        ApnSetting apnsetting = gsmdataconnection.getApn();
        mActiveApn = apnsetting;
        String s4 = gsmdataconnection.getApn().types[0];
        resetRetryByType(s4);
        int k = Log.d("GSM", "reset IP address");
        String s5 = mActiveApn.types[0];
        resetallApnsaddressInfo(s5);
        if(gsmdataconnection.isipv4configured == 1)
            flag = true;
        gsmdataconnection;
        JVM INSTR monitorexit ;
        notifyDefaultData("ipv6addressconfigured");
        if(isOnDemandEnable)
            return;
        if(!flag)
        {
            return;
        } else
        {
            trySetupAllEnabledServices();
            return;
        }
    }

    private boolean isApnTypeInactive(String s)
    {
        Iterator iterator = pdpList.iterator();
_L4:
        if(!iterator.hasNext()) goto _L2; else goto _L1
_L1:
        GsmDataConnection gsmdataconnection = (GsmDataConnection)(DataConnection)iterator.next();
        if(!gsmdataconnection.canHandleType(s) || gsmdataconnection.isInactive()) goto _L4; else goto _L3
_L3:
        boolean flag = false;
_L6:
        return flag;
_L2:
        flag = true;
        if(true) goto _L6; else goto _L5
_L5:
    }

    private boolean isDataAllowed()
    {
        boolean flag = phone.getServiceState().getRoaming();
        String s = mCurrentRequestedApnType;
        int i = apnTypeToId(s);
        boolean flag1;
        if(isEnabled(i) && (!flag || getDataOnRoamingEnabled()) && mMasterDataEnabled)
            flag1 = true;
        else
            flag1 = false;
        return flag1;
    }

    private boolean isDuringActionOnAnyApnType()
    {
        Iterator iterator = pdpList.iterator();
_L4:
        if(!iterator.hasNext()) goto _L2; else goto _L1
_L1:
        GsmDataConnection gsmdataconnection = (GsmDataConnection)(DataConnection)iterator.next();
        if(gsmdataconnection.isInactive() || gsmdataconnection.isActive()) goto _L4; else goto _L3
_L3:
        boolean flag = true;
_L6:
        return flag;
_L2:
        flag = false;
        if(true) goto _L6; else goto _L5
_L5:
    }

    private boolean isDuringActionOnApnType(String s)
    {
        Iterator iterator = pdpList.iterator();
_L4:
        if(!iterator.hasNext()) goto _L2; else goto _L1
_L1:
        GsmDataConnection gsmdataconnection = (GsmDataConnection)(DataConnection)iterator.next();
        if(!gsmdataconnection.canHandleType(s) || gsmdataconnection.isInactive() || gsmdataconnection.isActive()) goto _L4; else goto _L3
_L3:
        boolean flag = true;
_L6:
        return flag;
_L2:
        flag = false;
        if(true) goto _L6; else goto _L5
_L5:
    }

    private void notifyDefaultData(String s)
    {
        com.android.internal.telephony.DataConnectionTracker.State state = com.android.internal.telephony.DataConnectionTracker.State.CONNECTED;
        setState(state);
        phone.notifyDataConnection(s);
        getDormancyEnableFlag();
        startNetStatPoll();
        sentSinceLastRecv = 0L;
        mReregisterOnReconnectFailure = false;
    }

    private void notifyNoData(com.android.internal.telephony.DataConnection.FailCause failcause)
    {
        com.android.internal.telephony.DataConnectionTracker.State state = com.android.internal.telephony.DataConnectionTracker.State.FAILED;
        setState(state);
    }

    private void onApnChanged()
    {
        boolean flag;
        boolean flag1;
        com.android.internal.telephony.DataConnectionTracker.State state;
        com.android.internal.telephony.DataConnectionTracker.State state1;
        int i;
        if(!isAllDataConnectionInactive())
            flag = true;
        else
            flag = false;
        flag1 = mGsmPhone.updateCurrentCarrierInProvider();
        createAllApnList();
        state = this.state;
        state1 = com.android.internal.telephony.DataConnectionTracker.State.DISCONNECTING;
        if(state == state1)
            return;
        if(flag)
            return;
        i = 0;
        do
        {
            int j = mRetryMgr.length;
            if(i < j)
            {
                mRetryMgr[i].resetRetryCount();
                i++;
            } else
            {
                isThrottleDefaultReq = false;
                mReregisterOnReconnectFailure = false;
                mRequestedApnType = "ims";
                trySetupAllEnabledServices();
                return;
            }
        } while(true);
    }

    private void onGprsAttached()
    {
        removeMessages(34);
        log("onGprsAttached");
        if(isAnyApnTypeActive())
        {
            startNetStatPoll();
            sentSinceLastRecv = 0L;
            phone.notifyDataConnection("gprsAttached");
            return;
        }
        com.android.internal.telephony.DataConnectionTracker.State state = this.state;
        com.android.internal.telephony.DataConnectionTracker.State state1 = com.android.internal.telephony.DataConnectionTracker.State.FAILED;
        if(state == state1)
        {
            cleanUpConnection(false, "gprsAttached");
            int i = 0;
            do
            {
                int j = mRetryMgr.length;
                if(i >= j)
                    break;
                mRetryMgr[i].resetRetryCount();
                i++;
            } while(true);
        }
        isThrottleDefaultReq = false;
        String s = mAttachApnType;
        mCurrentRequestedApnType = s;
        String s1 = mAttachApnType;
        mRequestedApnType = s1;
        StringBuilder stringbuilder = (new StringBuilder()).append("onGprsAttached ApnType requested is ");
        String s2 = mRequestedApnType;
        StringBuilder stringbuilder1 = stringbuilder.append(s2).append(" mCurrentRequestedApnType ");
        String s3 = mCurrentRequestedApnType;
        String s4 = stringbuilder1.append(s3).toString();
        log(s4);
        clearAllPendingApnRequest();
        boolean flag = trySetupData("gprsAttached");
        dequeOnDemandPdnRequest();
    }

    private String[] parseTypes(String s)
    {
        String as[];
        if(s == null || s.equals(""))
        {
            as = new String[1];
            as[0] = "*";
        } else
        {
            as = s.split(",");
        }
        return as;
    }

    private boolean pdpStatesHasActiveCID(ArrayList arraylist, int i)
    {
        log("pdpStatesHasActiveCID");
        int j = 0;
label0:
        for(int k = arraylist.size(); j < k; j++)
        {
            Iterator iterator = pdpList.iterator();
            do
            {
                if(!iterator.hasNext())
                    continue label0;
                GsmDataConnection gsmdataconnection = (GsmDataConnection)(DataConnection)iterator.next();
                if(gsmdataconnection.isActive())
                {
                    int l = ((DataCallState)arraylist.get(j)).cid;
                    int i1 = gsmdataconnection.getCid();
                    if(l != i1 && ((DataCallState)arraylist.get(j)).active != 1)
                    {
                        log("pdpStatesHasActiveCID Inconsistency found in the PDP State ");
                        int j1 = gsmdataconnection.getCid();
                        int k1 = ((DataCallState)arraylist.get(j)).reason;
                        boolean flag = handleAsyncDisconnect(j1, k1);
                    }
                }
            } while(true);
        }

        log("[LTEpsBearerTracker] defEpsBearerStatesHasActiveCID returning true ");
        return true;
    }

    private boolean pdpStatesHasCID(ArrayList arraylist, int i)
    {
        int j;
        int k;
        log("pdpStatesHasCID");
        j = 0;
        k = arraylist.size();
_L3:
        boolean flag;
        if(j >= k)
            break MISSING_BLOCK_LABEL_209;
        flag = false;
        Iterator iterator = pdpList.iterator();
        do
        {
            if(!iterator.hasNext())
                break;
            GsmDataConnection gsmdataconnection = (GsmDataConnection)(DataConnection)iterator.next();
            if(!gsmdataconnection.isActive())
                continue;
            int l = ((DataCallState)arraylist.get(j)).cid;
            int i1 = gsmdataconnection.getCid();
            if(l == i1)
                continue;
            flag = true;
            break;
        } while(true);
        if(flag || ((DataCallState)arraylist.get(j)).active != 1) goto _L2; else goto _L1
_L1:
        boolean flag2;
        log("pdpStatesHasCID PDP up from down ");
        String s = ((DataCallState)arraylist.get(j)).apn;
        String as[] = getApnTypeByApnName(s);
        if(as != null)
        {
            String s1 = as[0];
            if(s1.equals("*"))
                s1 = "default";
            boolean flag1 = handleAsyncConnect(s1);
        } else
        {
            log("pdpStatesHasCID apnTypes is null Unexpected behaviour ");
        }
        log("pdpStatesHasCID return false ");
        flag2 = false;
_L4:
        return flag2;
_L2:
        j++;
          goto _L3
        log("pdpStatesHasCID return true ");
        flag2 = true;
          goto _L4
    }

    private void printProperties(AsyncResult asyncresult)
    {
        GsmDataConnection gsmdataconnection = (GsmDataConnection)(GsmDataConnection)asyncresult.result;
        if(gsmdataconnection == null)
        {
            return;
        } else
        {
            String s = gsmdataconnection.getInterface();
            log("Connected properties...");
            String s1 = SystemProperties.get((new StringBuilder()).append("net.").append(s).append(".gw").toString());
            String s2 = (new StringBuilder()).append(" @net.").append(s).append(".gw: ").append(s1).toString();
            log(s2);
            String s3 = SystemProperties.get((new StringBuilder()).append("net.").append(s).append(".dns1").toString());
            String s4 = (new StringBuilder()).append(" @net.").append(s).append(".dns1: ").append(s3).toString();
            log(s4);
            String s5 = SystemProperties.get((new StringBuilder()).append("net.").append(s).append(".dns2").toString());
            String s6 = (new StringBuilder()).append(" @net.").append(s).append(".dns2: ").append(s5).toString();
            log(s6);
            return;
        }
    }

    private void processPendingIpv6DataCallState(GsmDataConnection gsmdataconnection)
    {
        if(mPendingIpv6DataCallList == null)
        {
            log("processPendingIpv6DataCallState Pending list is null ");
            return;
        }
        if(mPendingIpv6DataCallList.isEmpty())
        {
            log("processPendingIpv6DataCallState Pending list is empty ");
            return;
        }
        gsmdataconnection;
        JVM INSTR monitorenter ;
        if(gsmdataconnection == null)
            break MISSING_BLOCK_LABEL_453;
        String s;
        DataCallState datacallstate;
        if(gsmdataconnection.getApn() == null || gsmdataconnection.getApn().apn == null)
            break MISSING_BLOCK_LABEL_453;
        s = gsmdataconnection.getApn().apn;
        datacallstate = (DataCallState)mPendingIpv6DataCallList.get(s);
        if(datacallstate != null)
            break MISSING_BLOCK_LABEL_102;
        log("No DataCall State pertaining to the ApnName given");
        gsmdataconnection;
        JVM INSTR monitorexit ;
        return;
        Exception exception;
        exception;
        gsmdataconnection;
        JVM INSTR monitorexit ;
        throw exception;
        GsmDataConnection gsmdataconnection1;
        int i = datacallstate.cid;
        int j = gsmdataconnection.getCid();
        if(i != j)
        {
            if(datacallstate.apn == null)
                break MISSING_BLOCK_LABEL_443;
            String s1 = datacallstate.apn;
            String s2 = gsmdataconnection.getApn().apn;
            if(!s1.equals(s2))
                break MISSING_BLOCK_LABEL_443;
        }
        log("processPendingIpv6DataCallState Matching CallState found Update pdp list now");
        Iterator iterator = pdpList.iterator();
        int k;
        int l;
        do
        {
            if(!iterator.hasNext())
                break MISSING_BLOCK_LABEL_433;
            gsmdataconnection1 = (GsmDataConnection)(DataConnection)iterator.next();
            k = gsmdataconnection1.getCid();
            l = gsmdataconnection.getCid();
        } while(k == l);
        log("processPendingIpv6DataCallState Updating Master Pdp list now");
        if(datacallstate.active != 1)
            break MISSING_BLOCK_LABEL_306;
        log("processPendingIpv6DataCallState Updated Master Pdp list as ipv6 configured");
        gsmdataconnection1.isipv6configured = 1;
        String s3 = datacallstate.address;
        gsmdataconnection1.ipv6Address = s3;
        StringBuilder stringbuilder = (new StringBuilder()).append("[DSAC DEB] onIpv6AddrStatusChanged: ");
        String s4 = gsmdataconnection1.ipv6Address;
        String s5 = stringbuilder.append(s4).toString();
        int i1 = Log.d("GSM", s5);
        removePendingIpv6DataCallList(s);
        gsmdataconnection;
        JVM INSTR monitorexit ;
        return;
        log("processPendingIpv6DataCallState Updated Master Pdp list as ipv6 configured");
        gsmdataconnection1.isipv6configured = 0;
        removePendingIpv6DataCallList(s);
        if(gsmdataconnection1.getApn() == null || !gsmdataconnection1.getApn().canHandleType("ims")) goto _L2; else goto _L1
_L1:
        log("[DSAC DEB] onIpv6AddrStatusChanged IPv6 Address assignment failed for IMS so Detach ");
        boolean flag;
        if(isApnTypeActive("ims"))
            imsDeregistrationOnDisconnect("pdndroppedbyNetwork");
        else
            flag = explicitDetach();
_L4:
        gsmdataconnection;
        JVM INSTR monitorexit ;
        return;
_L2:
        log("[DSAC DEB] onIpv6AddrStatusChanged IPv6 Address assignment failed for other APN ");
        if(gsmdataconnection1.ipaddresstype != 2) goto _L4; else goto _L3
_L3:
        log("[DSAC DEB] onIpv6AddrStatusChanged IPv6 Address assignment failed and address is IPv6 alone");
        String s6 = gsmdataconnection1.getApn().types[0];
        int j1 = apnTypeToId(s6);
        onEnableApn(j1, 0);
        gsmdataconnection;
        JVM INSTR monitorexit ;
        return;
        log("processPendingIpv6DataCallState Mismatch of both given Connection and Master DataConnection list");
        gsmdataconnection;
        JVM INSTR monitorexit ;
        return;
        log("processPendingIpv6DataCallState Mismatch of both CID and Apn Generally should not occur");
        gsmdataconnection;
        JVM INSTR monitorexit ;
        return;
        gsmdataconnection;
        JVM INSTR monitorexit ;
    }

    private int readIntFromSocket(DataInputStream datainputstream)
        throws IOException
    {
        byte abyte0[] = new byte[4];
        int k;
        for(int i = 0; i < 4; i += k)
        {
            int j = 4 - i;
            k = datainputstream.read(abyte0, i, j);
            if(k == -1)
                log("[Manual Attach] readIntFromSocket ERROR");
        }

        int l = abyte0[0] & 0xff;
        int i1 = abyte0[1] & 0xff;
        int j1 = abyte0[2] & 0xff;
        int k1 = (abyte0[3] & 0xff) << 24;
        int l1 = j1 << 16;
        int i2 = k1 + l1;
        int j2 = i1 << 8;
        return i2 + j2 + l;
    }

    private void reconnectAfterFail(com.android.internal.telephony.DataConnection.FailCause failcause, String s)
    {
        String s1 = mCurrentRequestedApnType;
        com.android.internal.telephony.DataConnectionTracker.State state = getRequestedApnState(s1);
        StringBuilder stringbuilder = (new StringBuilder()).append(" reconnectAfterFail apnType ");
        String s2 = mCurrentRequestedApnType;
        String s3 = stringbuilder.append(s2).append(" state ").append(state).append(" cause ").append(failcause).append(" reason ").append(s).toString();
        log(s3);
        RetryManager retrymanager = null;
        int i = 0;
label0:
        do
        {
label1:
            {
                int j = mRetryMgr.length;
                if(i < j)
                {
                    String s4 = mRetryMgr[i].getApnType();
                    String s5 = mCurrentRequestedApnType;
                    if(!s4.equals(s5))
                        break label1;
                    retrymanager = mRetryMgr[i];
                }
                if(retrymanager == null)
                {
                    phone.notifyDataConnection("apnFailed");
                    String s6 = mCurrentRequestedApnType;
                    int k = apnTypeToId(s6);
                    onEnableApn(k, 0);
                    return;
                }
                break label0;
            }
            i++;
        } while(true);
        StringBuilder stringbuilder1 = (new StringBuilder()).append(" reconnectAfterFail selected retrymanager is :");
        String s7 = retrymanager.getApnType();
        String s8 = stringbuilder1.append(s7).toString();
        log(s8);
        if(!retrymanager.isRetryNeeded())
        {
            StringBuilder stringbuilder2 = (new StringBuilder()).append(" reconnectAfterFail No More Retry required for apnType ");
            String s9 = mActiveApn.types[0];
            String s10 = stringbuilder2.append(s9).toString();
            log(s10);
            if(!mActiveApn.canHandleType("ims"))
            {
                if(mActiveApn.canHandleType("default"))
                {
                    log("reconnectAfterFail : retry not needed : setting isThrottleDefaultReq false");
                    isThrottleDefaultReq = false;
                }
                phone.notifyDataConnection("apnFailed");
                String s11 = mCurrentRequestedApnType;
                int l = apnTypeToId(s11);
                onEnableApn(l, 0);
                return;
            } else
            {
                log("reconnectAfterFail send explicit detach");
                phone.notifyDataConnection("apnFailed");
                mImsSMSInterface.unregisterImsOnImsPdnDetach();
                return;
            }
        }
        int i1 = retrymanager.getRetryTimer();
        StringBuilder stringbuilder3 = (new StringBuilder()).append("PDP activate failed. Scheduling next attempt for ");
        int j1 = i1 / 1000;
        String s12 = stringbuilder3.append(j1).append("s").toString();
        log(s12);
        AlarmManager alarmmanager = (AlarmManager)phone.getContext().getSystemService("alarm");
        Intent intent = new Intent("com.android.internal.telephony.gsm-reconnect");
        Intent intent1 = intent.putExtra("com.android.internal.telephony.gsm.reason", s);
        String s13 = retrymanager.getApnType();
        Intent intent2 = intent.putExtra("com.android.internal.telephony.gsm.reqtype", s13);
        PendingIntent pendingintent = PendingIntent.getBroadcast(phone.getContext(), 0, intent, 0);
        mReconnectIntent = pendingintent;
        if(mActiveApn.canHandleType("default"))
        {
            log("reconnectAfterFail : retry needed : setting isThrottleDefaultReq true");
            isThrottleDefaultReq = true;
        }
        long l1 = SystemClock.elapsedRealtime();
        long l2 = i1;
        long l3 = l1 + l2;
        PendingIntent pendingintent1 = mReconnectIntent;
        alarmmanager.set(2, l3, pendingintent1);
        retrymanager.increaseRetryCount();
        if(!shouldPostNotification(failcause))
        {
            log("NOT Posting GPRS Unavailable notification -- likely transient error");
            return;
        } else
        {
            notifyNoData(failcause);
            return;
        }
    }

    private void removeFromPendingRequestedApns(String s)
    {
        if(mPendingRequestedApns == null)
            return;
        if(s == null)
            return;
        synchronized(mPendingRequestedApns)
        {
            if(mPendingRequestedApns.remove(s))
            {
                String s1 = (new StringBuilder()).append(s).append(" is removed from the pending list").toString();
                log(s1);
            } else
            {
                String s2 = (new StringBuilder()).append(s).append(" is not in the pending list").toString();
                log(s2);
            }
        }
    }

    private void removePendingIpv6DataCallList(String s)
    {
        if(mPendingIpv6DataCallList == null)
        {
            return;
        } else
        {
            String s1 = (new StringBuilder()).append("Entry with key  ").append(s).append(" is removed from list").toString();
            log(s1);
            Object obj = mPendingIpv6DataCallList.remove(s);
            return;
        }
    }

    private void resetPollStats()
    {
        txPkts = 65535L;
        rxPkts = 65535L;
        mifi_home_Transmitted = 0L;
        mifi_home_Received = 0L;
        mifi_home_polltime = 0L;
        netStatPollPeriod = 1000;
        mNoRecvPollCount = 0;
    }

    private void resetRetryByType(String s)
    {
        int i = 0;
        do
        {
            int j = mRetryMgr.length;
            if(i >= j)
                return;
            if(s != null)
            {
                String s1 = mRetryMgr[i].getApnType();
                if(s.equals(s1))
                {
                    StringBuilder stringbuilder = (new StringBuilder()).append("resetRetryByType ").append(s).append("retryManager ");
                    String s2 = mRetryMgr[i].getApnType();
                    String s3 = stringbuilder.append(s2).toString();
                    log(s3);
                    mRetryMgr[i].resetRetryCount();
                    if(!s.equals("default"))
                    {
                        return;
                    } else
                    {
                        isThrottleDefaultReq = false;
                        return;
                    }
                }
            }
            i++;
        } while(true);
    }

    private void resetallApnsaddressInfo(String s)
    {
        log("resetallApnsaddressInfo");
        if(allApns == null)
            return;
        Iterator iterator = allApns.iterator();
        do
        {
            ApnSetting apnsetting;
            do
            {
                if(!iterator.hasNext())
                    return;
                apnsetting = (ApnSetting)iterator.next();
            } while(!apnsetting.canHandleType(s));
            StringBuilder stringbuilder = (new StringBuilder()).append("[GSMMultiDCT]resetting IPaddress and IPtype for");
            String s1 = apnsetting.apn;
            String s2 = stringbuilder.append(s1).toString();
            log(s2);
            apnsetting.ipv4 = null;
            apnsetting.ipv6 = null;
        } while(true);
    }

    private boolean retryAfterDisconnected(String s)
    {
        boolean flag = true;
        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(phone.getContext());
        if("handoverdisconncted".equals(s) || "radioTurnedOff".equals(s) || sharedpreferences.getBoolean("disable_always_on_key", false))
            flag = false;
        return flag;
    }

    private void runPingTest()
    {
        int i = -1;
        int k;
        String s = android.provider.Settings.Secure.getString(mResolver, "pdp_watchdog_ping_address");
        int j = android.provider.Settings.Secure.getInt(mResolver, "pdp_watchdog_ping_deadline", 5);
        String s1 = (new StringBuilder()).append("pinging ").append(s).append(" for ").append(j).append("s").toString();
        log(s1);
        if(s == null || "0.0.0.0".equals(s))
            break MISSING_BLOCK_LABEL_136;
        Runtime runtime = Runtime.getRuntime();
        String s2 = (new StringBuilder()).append("ping -c 1 -i 1 -w ").append(j).append(" ").append(s).toString();
        k = runtime.exec(s2).waitFor();
        i = k;
_L1:
        IOException ioexception;
        Exception exception;
        if(i == 0)
        {
            int l = EventLog.writeEvent(50102, -1);
            mPdpResetCount = 0;
            Message message = obtainMessage(27);
            boolean flag = sendMessage(message);
            return;
        } else
        {
            Message message1 = obtainMessage(28);
            boolean flag1 = sendMessage(message1);
            return;
        }
        ioexception;
        log("ping failed: IOException");
          goto _L1
        exception;
        log("exception trying to ping");
          goto _L1
    }

    private void setDefaultPdpContextProperty(GsmDataConnection gsmdataconnection)
    {
        if(isApnTypeActive("default") || isApnTypeActive("*"))
        {
            SystemProperties.set("gsm.defaultpdpcontext.active", "true");
            log("setDefaultPdpContextProperty : gsm.defaultpdpcontext.active is true");
            return;
        } else
        {
            SystemProperties.set("gsm.defaultpdpcontext.active", "false");
            log("setDefaultPdpContextProperty : gsm.defaultpdpcontext.active is false");
            return;
        }
    }

    private void setPreferredApn(int i)
    {
        if(!canSetPreferApn)
            return;
        ContentResolver contentresolver = phone.getContext().getContentResolver();
        Uri uri = PREFERAPN_URI;
        int j = contentresolver.delete(uri, null, null);
        if(i < 0)
        {
            return;
        } else
        {
            ContentValues contentvalues = new ContentValues();
            Integer integer = Integer.valueOf(i);
            contentvalues.put("apn_id", integer);
            Uri uri1 = PREFERAPN_URI;
            Uri uri2 = contentresolver.insert(uri1, contentvalues);
            return;
        }
    }

    private boolean setupData(String s)
    {
        ApnSetting apnsetting = getNextApn();
        if(apnsetting != null) goto _L2; else goto _L1
_L1:
        boolean flag = false;
_L12:
        return flag;
_L2:
        if(true) goto _L4; else goto _L3
_L3:
        if(!mGsmPhone.IsACLEnabled()) goto _L6; else goto _L5
_L5:
        int i = Log.d("GSM", "IsACLEnabled() is true");
        if(mGsmPhone.mSIMRecords != null) goto _L8; else goto _L7
_L7:
        flag = false;
          goto _L9
_L8:
        SIMRecords simrecords;
        String s2;
        String s1 = apnsetting.apn;
        int j = Log.d("GSM", s1);
        simrecords = mGsmPhone.mSIMRecords;
        s2 = apnsetting.apn;
        if(simrecords.verifyACL(s2)) goto _L11; else goto _L10
_L10:
        int k = Log.d("GSM", "verifyACL is false");
        String s3 = apnsetting.types[0];
        if("admin".equals(s3))
        {
            int l = Log.d("GSM", "ADMIN APN requested and verifyACL is false. Calling explicitDetach()");
            boolean flag1;
            if(isApnTypeActive("ims"))
                imsDeregistrationOnDetach(0, 0);
            else
                flag1 = explicitDetach();
        }
        flag = false;
          goto _L9
_L11:
        int i1 = Log.d("GSM", "verifyACL is true");
_L4:
        GsmDataConnection gsmdataconnection = findFreePdp();
        int j1;
        if(gsmdataconnection == null)
        {
            log("setupData: No free GsmDataConnection found!");
            flag = false;
        } else
        {
            mActiveApn = apnsetting;
            mActivePdp = gsmdataconnection;
            Message message = obtainMessage();
            message.what = 1;
            message.obj = s;
            com.android.internal.telephony.DataConnectionTracker.State state;
            if(apnsetting.ipv4 != null || apnsetting.ipv6 != null)
            {
                int k1 = Log.d("GSM", "[GsmMultiDataConnectionTracker] resetting Handover Info before copying IP address for Handover Connect PDN");
                gsmdataconnection.resetHandoverIpInfo();
                int l1 = Log.d("GSM", "[GsmMultiDataConnectionTracker] setting Handover  IP address for Handover Connect PDN");
                String s4 = apnsetting.ipv6;
                String s5 = apnsetting.ipv4;
                gsmdataconnection.setHandoverIpInfo(s4, s5);
                int i2 = GsmDataConnection.CONN_TYPE_HANDOVER;
                gsmdataconnection.setConnType(i2);
                int j2 = Log.d("GSM", "[GsmMultiDataConnectionTracker] Handover Connect ");
            } else
            {
                int k2 = Log.d("GSM", "[GsmMultiDataConnectionTracker] resetting Handover IP address for Initial Connect PDN");
                gsmdataconnection.resetHandoverIpInfo();
                int l2 = GsmDataConnection.CONN_TYPE_INITIAL;
                gsmdataconnection.setConnType(l2);
                int i3 = Log.d("GSM", "[GsmMultiDataConnectionTracker] Initial Connect ");
            }
            gsmdataconnection.connect(message, apnsetting, 1);
            state = com.android.internal.telephony.DataConnectionTracker.State.INITING;
            setState(state);
            if(apnsetting.ipv4 == null && apnsetting.ipv6 == null)
                phone.notifyDataConnection(s);
            flag = true;
        }
_L9:
        if(true) goto _L12; else goto _L6
_L6:
        j1 = Log.d("GSM", "IsACLEnabled() is false");
          goto _L4
    }

    private boolean shouldPostNotification(com.android.internal.telephony.DataConnection.FailCause failcause)
    {
        com.android.internal.telephony.DataConnection.FailCause failcause1 = com.android.internal.telephony.DataConnection.FailCause.UNKNOWN;
        boolean flag;
        if(failcause != failcause1)
            flag = true;
        else
            flag = false;
        return flag;
    }

    private void startDelayedRetry(com.android.internal.telephony.DataConnection.FailCause failcause, String s)
    {
        notifyNoData(failcause);
        reconnectAfterFail(failcause, s);
    }

    private void startPeriodicPdpPoll()
    {
        removeMessages(7);
        Message message = obtainMessage(7);
        boolean flag = sendMessageDelayed(message, 5000L);
    }

    private void stringToFile(String s, String s1)
        throws IOException
    {
        FileWriter filewriter = new FileWriter(s);
        filewriter.write(s1);
        filewriter.close();
        return;
        Exception exception;
        exception;
        filewriter.close();
        throw exception;
    }

    private void trySetupAllEnabledServices()
    {
        clearAllPendingApnRequest();
        String s = mAttachApnType;
        if(isApnTypeInactive(s))
        {
            StringBuilder stringbuilder = new StringBuilder();
            String s1 = mAttachApnType;
            String s2 = stringbuilder.append(s1).append(" is disconnected, trySetup..").toString();
            log(s2);
            String s3 = mAttachApnType;
            addPendingApnRequest(s3);
        }
        for(int i = 0; i < 8; i++)
        {
            String s4 = mAttachApnType;
            int j = apnTypeToId(s4);
            if(i == j)
                continue;
            String s5 = apnIdToType(i);
            if(isEnabled(i) && isApnTypeInactive(s5))
            {
                String s6 = (new StringBuilder()).append(s5).append(" service is enabled but inactive").toString();
                int k = Log.d("GSM", s6);
                addPendingApnRequest(s5);
            }
        }

        if(!mPendingRequestedApns.isEmpty())
        {
            Message message = obtainMessage(59);
            boolean flag = sendMessageDelayed(message, 500L);
            return;
        } else
        {
            log("trySetupAllEnabledService(): Nothing to try");
            return;
        }
    }

    private boolean trySetupData(String s)
    {
        boolean flag1;
        StringBuilder stringbuilder = (new StringBuilder()).append("***trySetupData due to ");
        String s1;
        StringBuilder stringbuilder1;
        String s2;
        String s3;
        StringBuilder stringbuilder2;
        boolean flag;
        String s4;
        com.android.internal.telephony.DataConnectionTracker.State state;
        int i;
        if(s == null)
            s1 = "(unspecified)";
        else
            s1 = s;
        stringbuilder1 = stringbuilder.append(s1).append(", mCurrReqApnType(");
        s2 = mCurrentRequestedApnType;
        s3 = stringbuilder1.append(s2).append(")").toString();
        log(s3);
        stringbuilder2 = (new StringBuilder()).append("[DSAC DEB] trySetupData with mIsPsRestricted=");
        flag = mIsPsRestricted;
        s4 = stringbuilder2.append(flag).toString();
        log(s4);
        if(phone.getSimulatedRadioControl() == null) goto _L2; else goto _L1
_L1:
        state = com.android.internal.telephony.DataConnectionTracker.State.CONNECTED;
        setState(state);
        phone.notifyDataConnection(s);
        i = Log.i("GSM", "(fix?) We're on the simulator; assuming data is connected");
        flag1 = true;
_L4:
        return flag1;
_L2:
        if(mGsmPhone.mSST.getSuspend() != 0)
        {
            int j = Log.d("GSM", "Handover in progress,do not allow trySetupData");
            flag1 = false;
            continue; /* Loop/switch isn't completed */
        }
        if(!mCurrentRequestedApnType.equals("ims") && !mImsTestMode && !isApnTypeActive("ims"))
        {
            StringBuilder stringbuilder3 = (new StringBuilder()).append("GsmMultiDCT: Non-Ims PDN should be rejected if IMS is not up mCurrentRequestedApnType:");
            String s5 = mCurrentRequestedApnType;
            StringBuilder stringbuilder4 = stringbuilder3.append(s5).append("mImsTestMode");
            boolean flag2 = mImsTestMode;
            StringBuilder stringbuilder5 = stringbuilder4.append(flag2).append("isApnTypeActive(ims)");
            boolean flag3 = isApnTypeActive("ims");
            String s6 = stringbuilder5.append(flag3).toString();
            log(s6);
            flag1 = false;
            continue; /* Loop/switch isn't completed */
        }
        if(!mCurrentRequestedApnType.equals("default") && mImsTestMode && !isApnTypeActive("default"))
        {
            StringBuilder stringbuilder6 = (new StringBuilder()).append("GsmMultiDCT: Non-default PDN should be rejected if default pdn is not up in test mode: mCurrentRequestedApnType:");
            String s7 = mCurrentRequestedApnType;
            StringBuilder stringbuilder7 = stringbuilder6.append(s7).append("mImsTestMode");
            boolean flag4 = mImsTestMode;
            StringBuilder stringbuilder8 = stringbuilder7.append(flag4).append("isApnTypeActive(default)");
            boolean flag5 = isApnTypeActive("default");
            String s8 = stringbuilder8.append(flag5).toString();
            log(s8);
            flag1 = false;
            continue; /* Loop/switch isn't completed */
        }
        int k = mGsmPhone.mSST.getCurrentGprsState();
        boolean flag6 = mGsmPhone.mSST.getDesiredPowerState();
        GsmDataConnection gsmdataconnection = findFreePdp();
        boolean flag7;
        String s9;
        boolean flag8;
        if(gsmdataconnection == null)
            flag7 = false;
        else
            flag7 = true;
        s9 = android.provider.Settings.System.getString(phone.getContext().getContentResolver(), "mode_type");
        if(s9.equals("LTE") || s9.equals("GLOBAL"))
            flag8 = true;
        else
            flag8 = false;
        if(flag7 && gsmdataconnection.isInactive() && k == 0 && mGsmPhone.mSIMRecords.getRecordsLoaded())
        {
            com.android.internal.telephony.Phone.State state1 = phone.getState();
            com.android.internal.telephony.Phone.State state2 = com.android.internal.telephony.Phone.State.IDLE;
            if((state1 == state2 || mGsmPhone.mSST.isConcurrentVoiceAndData()) && isDataAllowed() && !mIsPsRestricted && flag6 && flag8)
            {
                com.android.internal.telephony.HandoverTracker.State state3 = mHandoverTracker.getState();
                com.android.internal.telephony.HandoverTracker.State state4 = com.android.internal.telephony.HandoverTracker.State.LTE_TO_CDMA;
                if(state3 != state4 && mIsImsEnabled && mIsAdminEnabled)
                {
                    if(waitingApns == null || waitingApns.isEmpty())
                    {
                        ArrayList arraylist = buildWaitingApns();
                        waitingApns = arraylist;
                        if(waitingApns.isEmpty())
                        {
                            log("No APN found");
                            com.android.internal.telephony.DataConnection.FailCause failcause = com.android.internal.telephony.DataConnection.FailCause.MISSING_UNKNOWN_APN;
                            notifyNoData(failcause);
                            flag1 = false;
                            continue; /* Loop/switch isn't completed */
                        }
                        StringBuilder stringbuilder9 = (new StringBuilder()).append("Create from allApns : ");
                        ArrayList arraylist1 = allApns;
                        String s10 = apnListToString(arraylist1);
                        String s11 = stringbuilder9.append(s10).toString();
                        log(s11);
                    }
                    StringBuilder stringbuilder10 = (new StringBuilder()).append("Setup waitngApns : ");
                    ArrayList arraylist2 = waitingApns;
                    String s12 = apnListToString(arraylist2);
                    String s13 = stringbuilder10.append(s12).toString();
                    log(s13);
                    boolean flag9 = setupData(s);
                    if(!flag9)
                    {
                        log("setupData() has returned false. Clearing waitingApns");
                        if(waitingApns != null && !waitingApns.isEmpty())
                        {
                            StringBuilder stringbuilder11 = (new StringBuilder()).append("Removing waiting apns: current size(");
                            int l = waitingApns.size();
                            StringBuilder stringbuilder12 = stringbuilder11.append(l).append(") and apn is ");
                            ArrayList arraylist3 = waitingApns;
                            String s14 = apnListToString(arraylist3);
                            String s15 = stringbuilder12.append(s14).toString();
                            log(s15);
                            Object obj = waitingApns.remove(0);
                        }
                    }
                    flag1 = flag9;
                    continue; /* Loop/switch isn't completed */
                }
            }
        }
        StringBuilder stringbuilder13 = (new StringBuilder()).append("trySetupData: Not ready for data: pdpslotAvailable=").append(flag7).append(" dataState=");
        String s16;
        StringBuilder stringbuilder14;
        boolean flag10;
        StringBuilder stringbuilder15;
        boolean flag11;
        StringBuilder stringbuilder16;
        com.android.internal.telephony.Phone.State state5;
        StringBuilder stringbuilder17;
        boolean flag12;
        StringBuilder stringbuilder18;
        String s17;
        int i1;
        boolean flag13;
        StringBuilder stringbuilder19;
        boolean flag14;
        StringBuilder stringbuilder20;
        boolean flag15;
        StringBuilder stringbuilder21;
        boolean flag16;
        StringBuilder stringbuilder22;
        com.android.internal.telephony.HandoverTracker.State state6;
        StringBuilder stringbuilder23;
        boolean flag17;
        StringBuilder stringbuilder24;
        boolean flag18;
        StringBuilder stringbuilder25;
        boolean flag19;
        String s18;
        if(gsmdataconnection == null)
            s16 = "N/A";
        else
            s16 = gsmdataconnection.getStateAsString();
        stringbuilder14 = stringbuilder13.append(s16).append(" gprsState=").append(k).append(" sim=");
        flag10 = mGsmPhone.mSIMRecords.getRecordsLoaded();
        stringbuilder15 = stringbuilder14.append(flag10).append(" UMTS=");
        flag11 = mGsmPhone.mSST.isConcurrentVoiceAndData();
        stringbuilder16 = stringbuilder15.append(flag11).append(" phoneState=");
        state5 = phone.getState();
        stringbuilder17 = stringbuilder16.append(state5).append(" isDataAllowed=");
        flag12 = isDataAllowed();
        stringbuilder18 = stringbuilder17.append(flag12).append(" dataEnabled=");
        s17 = mCurrentRequestedApnType;
        i1 = apnTypeToId(s17);
        flag13 = isEnabled(i1);
        stringbuilder19 = stringbuilder18.append(flag13).append(" roaming=");
        flag14 = phone.getServiceState().getRoaming();
        stringbuilder20 = stringbuilder19.append(flag14).append(" dataOnRoamingEnable=");
        flag15 = getDataOnRoamingEnabled();
        stringbuilder21 = stringbuilder20.append(flag15).append(" ps restricted=");
        flag16 = mIsPsRestricted;
        stringbuilder22 = stringbuilder21.append(flag16).append(" desiredPowerState=").append(flag6).append(" selectionMode=").append(s9).append(" handoverState=");
        state6 = mHandoverTracker.getState();
        stringbuilder23 = stringbuilder22.append(state6).append(" MasterDataEnabled=");
        flag17 = mMasterDataEnabled;
        stringbuilder24 = stringbuilder23.append(flag17).append(" mIsImsEnabled=");
        flag18 = mIsImsEnabled;
        stringbuilder25 = stringbuilder24.append(flag18).append(" mIsAdminEnabled=");
        flag19 = mIsAdminEnabled;
        s18 = stringbuilder25.append(flag19).toString();
        log(s18);
        flag1 = false;
        if(true) goto _L4; else goto _L3
_L3:
    }

    private void trySetupNextData()
    {
        if(!isDuringActionOnAnyApnType())
        {
            com.android.internal.telephony.DataConnectionTracker.State state = this.state;
            com.android.internal.telephony.DataConnectionTracker.State state1 = com.android.internal.telephony.DataConnectionTracker.State.INITING;
            if(state != state1)
            {
                String s = getNextPendingApnRequest();
                if(s != null)
                {
                    mCurrentRequestedApnType = s;
                    StringBuilder stringbuilder = (new StringBuilder()).append("trySetupNextData: mCurrReqApnType(");
                    String s1 = mCurrentRequestedApnType;
                    String s2 = stringbuilder.append(s1).append(")").toString();
                    log(s2);
                    boolean flag = trySetupData("pendingApnEnabled");
                    return;
                } else
                {
                    StringBuilder stringbuilder1 = (new StringBuilder()).append("trySetupNextData: Nothing to try(mCurrReqApnType: ");
                    String s3 = mCurrentRequestedApnType;
                    String s4 = stringbuilder1.append(s3).append(")").toString();
                    log(s4);
                    return;
                }
            }
        }
        StringBuilder stringbuilder2 = (new StringBuilder()).append("trySetupNextData: unable to try, state is ");
        com.android.internal.telephony.DataConnectionTracker.State state2 = this.state;
        String s5 = stringbuilder2.append(state2).toString();
        log(s5);
    }

    private void trySetupNextData(int i)
    {
        if(!mPendingRequestedApns.isEmpty())
        {
            Message message = obtainMessage(59, "pendingApnEnabled");
            long l = i;
            boolean flag = sendMessageDelayed(message, l);
            return;
        } else
        {
            StringBuilder stringbuilder = (new StringBuilder()).append("trySetupNextData: Nothing to try(mCurrReqApnType: ");
            String s = mCurrentRequestedApnType;
            String s1 = stringbuilder.append(s).append(")").toString();
            log(s1);
            return;
        }
    }

    private void updateMultiplePdpCapacity()
    {
        String s = mGsmPhone.mSIMRecords.getSIMOperatorNumeric();
        mIsSimSupportMultiPdp = true;
    }

    public void disconnectByApntype(String s)
    {
        com.android.internal.telephony.DataConnectionTracker.State state = getRequestedApnState(s);
        com.android.internal.telephony.DataConnectionTracker.State state1 = com.android.internal.telephony.DataConnectionTracker.State.DISCONNECTING;
        if(state == state1)
            return;
        Iterator iterator = pdpList.iterator();
        do
        {
            GsmDataConnection gsmdataconnection;
            do
            {
                if(!iterator.hasNext())
                    return;
                gsmdataconnection = (GsmDataConnection)(DataConnection)iterator.next();
            } while(!gsmdataconnection.canHandleType(s));
            if(getActiveApnCount() > 1)
            {
                int i = Log.e("GSM", " More than one connections active: Calling Disconnect");
                if(s.equals("ims"))
                {
                    imsDeregistrationOnDisconnect("apnSettingsChanged");
                } else
                {
                    Message message = obtainMessage(25, "apnSettingsChanged");
                    gsmdataconnection.disconnect(message);
                }
            } else
            {
                int j = Log.e("GSM", "One connection active: Calling explicitDetach");
                boolean flag;
                if(s.equals("ims"))
                    imsDeregistrationOnDetach(1, 0);
                else
                    flag = explicitDetach();
            }
        } while(true);
    }

    public void dispose()
    {
        phone.mCM.unregisterForAvailable(this);
        phone.mCM.unregisterForOffOrNotAvailable(this);
        mGsmPhone.mSIMRecords.unregisterForRecordsLoaded(this);
        phone.mCM.unregisterForDataStateChanged(this);
        mGsmPhone.mCT.unregisterForVoiceCallEnded(this);
        mGsmPhone.mCT.unregisterForVoiceCallStarted(this);
        mGsmPhone.mSST.unregisterForGprsAttached(this);
        mGsmPhone.mSST.unregisterForGprsDetached(this);
        mGsmPhone.mSST.unregisterForRoamingOn(this);
        mGsmPhone.mSST.unregisterForRoamingOff(this);
        mGsmPhone.mSST.unregisterForPsRestrictedEnabled(this);
        mGsmPhone.mSST.unregisterForPsRestrictedDisabled(this);
        mGsmPhone.mSST.unregisterForInitiate(this);
        mGsmPhone.mSST.unregisterForHome(this);
        ContentResolver contentresolver = mGsmPhone.getContext().getContentResolver();
        DataRoamingContentObserver dataroamingcontentobserver = mDataRoaming;
        contentresolver.unregisterContentObserver(dataroamingcontentobserver);
        Context context = phone.getContext();
        BroadcastReceiver broadcastreceiver = mIntentReceiver;
        context.unregisterReceiver(broadcastreceiver);
        ContentResolver contentresolver1 = phone.getContext().getContentResolver();
        ApnChangeObserver apnchangeobserver = apnObserver;
        contentresolver1.unregisterContentObserver(apnchangeobserver);
        destroyAllPdpList();
    }

    /**
     * @deprecated Method enableApnType is deprecated
     */

    public int enableApnType(String s)
    {
        this;
        JVM INSTR monitorenter ;
        int i = apnTypeToId(s);
        int j = i;
        if(j != -1) goto _L2; else goto _L1
_L1:
        byte byte0 = 3;
_L4:
        this;
        JVM INSTR monitorexit ;
        return byte0;
_L2:
        boolean flag;
        StringBuilder stringbuilder = (new StringBuilder()).append("enableApnType(").append(s).append("), isApnTypeActive = ");
        boolean flag1 = isApnTypeActive(s);
        StringBuilder stringbuilder1 = stringbuilder.append(flag1).append(" and state = ");
        com.android.internal.telephony.DataConnectionTracker.State state = this.state;
        String s1 = stringbuilder1.append(state).toString();
        log(s1);
        if(!isApnTypeAvailable(s))
        {
            log("type not available");
            byte0 = 2;
            continue; /* Loop/switch isn't completed */
        }
        if(isThrottleDefaultReq && "default".equals(s))
        {
            log("enableApnType :isThrottleDefaultReq  true & type default");
            byte0 = 3;
            continue; /* Loop/switch isn't completed */
        }
        setEnabled(j, true);
        flag = isApnTypeActive(s);
        if(flag)
            byte0 = 0;
        else
            byte0 = 1;
        if(true) goto _L4; else goto _L3
_L3:
        Exception exception;
        exception;
        throw exception;
    }

    public boolean explicitDetach()
    {
        log("explicitDetach(): teardown, call conn.disconnect");
        if(mReconnectIntent != null)
        {
            AlarmManager alarmmanager = (AlarmManager)phone.getContext().getSystemService("alarm");
            PendingIntent pendingintent = mReconnectIntent;
            alarmmanager.cancel(pendingintent);
            mReconnectIntent = null;
        }
        com.android.internal.telephony.DataConnectionTracker.State state = com.android.internal.telephony.DataConnectionTracker.State.DISCONNECTING;
        setState(state);
        Iterator iterator = pdpList.iterator();
        do
        {
            if(!iterator.hasNext())
                break;
            GsmDataConnection gsmdataconnection = (GsmDataConnection)(DataConnection)iterator.next();
            log("explicitDetach: teardown, call conn.disconnect");
            if(gsmdataconnection != null && gsmdataconnection.getApn() != null)
            {
                if(gsmdataconnection.getApn().canHandleType("ims"))
                    mImsSMSInterface.unregisterImsOnImsPdnDetach();
                Message message = obtainMessage(25, "regulardisconncted");
                gsmdataconnection.disconnect(message);
            }
        } while(true);
        return true;
    }

    public boolean explicitDetach(int i, int j)
    {
        String s = (new StringBuilder()).append("[DSAC DEB] explicitDetach(").append(i).append(" , ").append(j).append(" )").toString();
        int k = Log.d("GSM", s);
        Iterator iterator = pdpList.iterator();
        do
        {
label0:
            {
                if(iterator.hasNext())
                {
                    GsmDataConnection gsmdataconnection = (GsmDataConnection)(DataConnection)iterator.next();
                    if(!gsmdataconnection.isActive())
                        continue;
                    int l = Log.d("GSM", "[DSAC DEB] explicitDetach(int reAttachFlag,int reason) Active APN is found so Detach now");
                    if(gsmdataconnection.getApn() == null)
                        break label0;
                    if(gsmdataconnection.getApn().canHandleType("ims"))
                        mImsSMSInterface.unregisterImsOnImsPdnDetach();
                    String s1 = gsmdataconnection.getApn().types[0];
                    Message message = obtainMessage(46);
                    message.obj = s1;
                    message.arg1 = i;
                    message.arg2 = j;
                    boolean flag = sendMessage(message);
                }
                return true;
            }
            int i1 = Log.d("GSM", "[DSAC DEB] explicitDetach APN information not available");
        } while(true);
    }

    protected void finalize()
    {
        int i = Log.d("GSM", "GsmMultiDataConnectionTracker finalized");
    }

    protected String getActiveApnString()
    {
        String s = null;
        if(mActiveApn != null)
            s = mActiveApn.apn;
        return s;
    }

    public String[] getActiveApnTypes()
    {
        String as[];
        if(mActiveApn != null)
        {
            as = mActiveApn.types;
        } else
        {
            as = new String[1];
            as[0] = "default";
        }
        return as;
    }

    public com.android.internal.telephony.DataConnectionTracker.State getActiveState()
    {
        return state;
    }

    public ArrayList getAllDataConnections()
    {
        return (ArrayList)pdpList.clone();
    }

    public ArrayList getApnListForHandover()
    {
        int i = Log.d("GSM", "getApnListForHandover");
        ArrayList arraylist = new ArrayList();
        Iterator iterator = pdpList.iterator();
        do
        {
            if(!iterator.hasNext())
                break;
            GsmDataConnection gsmdataconnection = (GsmDataConnection)(DataConnection)iterator.next();
            int i1;
            if(gsmdataconnection.isActive())
            {
                if(gsmdataconnection.getApn() != null)
                {
                    ApnSetting apnsetting = gsmdataconnection.getApn();
                    ApnSetting apnsetting1 = new ApnSetting(apnsetting);
                    String s = gsmdataconnection.getIpAddress();
                    apnsetting1.ipv4 = s;
                    String s1 = gsmdataconnection.getIpv6Address();
                    apnsetting1.ipv6 = s1;
                    int l;
                    if(apnsetting1.ipv6 == null && apnsetting1.ipv4 == null)
                    {
                        String s2 = (new StringBuilder()).append("Not a valid APN for Handover since this do not have any IP address").append(apnsetting1).toString();
                        int j = Log.d("GSM", s2);
                    } else
                    if(gsmdataconnection.ipaddresstype == 1 && apnsetting1.ipv4 != null || gsmdataconnection.ipaddresstype == 2 && apnsetting1.ipv6 != null || gsmdataconnection.ipaddresstype == 3 && apnsetting1.ipv4 != null && apnsetting1.ipv6 != null)
                    {
                        boolean flag = arraylist.add(apnsetting1);
                        String s3 = (new StringBuilder()).append("Active APN for Handover").append(apnsetting1).toString();
                        int k = Log.d("GSM", s3);
                    } else
                    {
                        l = Log.d("GSM", "IPtype and address not proper.");
                    }
                }
            } else
            {
                i1 = Log.d("GSM", "Data Connection not active");
            }
        } while(true);
        if(allApns == null)
        {
            log("mAllApns is null, return");
        } else
        {
            for(Iterator iterator1 = allApns.iterator(); iterator1.hasNext();)
            {
                ApnSetting apnsetting2 = (ApnSetting)iterator1.next();
                if(apnsetting2.ipv4 != null || apnsetting2.ipv6 != null)
                {
                    if(!arraylist.contains(apnsetting2))
                    {
                        StringBuilder stringbuilder = (new StringBuilder()).append("adding to result");
                        String s4 = apnsetting2.apn;
                        String s5 = stringbuilder.append(s4).toString();
                        int j1 = Log.d("GSM", s5);
                        ApnSetting apnsetting3 = new ApnSetting(apnsetting2);
                        boolean flag1 = arraylist.add(apnsetting3);
                    } else
                    {
                        StringBuilder stringbuilder1 = (new StringBuilder()).append("already present in result");
                        String s6 = apnsetting2.apn;
                        String s7 = stringbuilder1.append(s6).append("not adding").toString();
                        int k1 = Log.d("GSM", s7);
                    }
                } else
                {
                    StringBuilder stringbuilder2 = (new StringBuilder()).append("AllApns address null for");
                    String s8 = apnsetting2.apn;
                    String s9 = stringbuilder2.append(s8).append("not adding").toString();
                    int l1 = Log.d("GSM", s9);
                }
            }

            String s10 = (new StringBuilder()).append("[Handover] **L2C getApnListForHandover: result [").append(arraylist).append("]").toString();
            int i2 = Log.d("GSM", s10);
        }
        return arraylist;
    }

    protected long getDataConnectedTime(String s)
    {
        long l1;
        if(mActivePdp != null && (s == null || mActiveApn != null && mActiveApn.canHandleType(s)))
        {
            StringBuilder stringbuilder = (new StringBuilder()).append("getConnectedTime ");
            long l = mActivePdp.getConnectionTime();
            String s1 = stringbuilder.append(l).toString();
            log(s1);
            l1 = mActivePdp.getConnectionTime();
        } else
        {
            log("getConnectedTime -1");
            l1 = 65535L;
        }
        return l1;
    }

    public boolean getDataOnDunEnabled()
    {
        return false;
    }

    protected String[] getDnsServers(String s)
    {
        if(s != null) goto _L2; else goto _L1
_L1:
        if(mActivePdp == null) goto _L4; else goto _L3
_L3:
        String as[] = mActivePdp.getDnsServers();
_L9:
        return as;
_L2:
        Iterator iterator = pdpList.iterator();
_L7:
        if(!iterator.hasNext()) goto _L4; else goto _L5
_L5:
        GsmDataConnection gsmdataconnection = (GsmDataConnection)(DataConnection)iterator.next();
        if(!gsmdataconnection.canHandleType(s)) goto _L7; else goto _L6
_L6:
        as = gsmdataconnection.getDnsServers();
        continue; /* Loop/switch isn't completed */
_L4:
        as = null;
        if(true) goto _L9; else goto _L8
_L8:
    }

    public boolean getDormancyCapability()
    {
        StringBuilder stringbuilder = (new StringBuilder()).append("[FD] Dormant flag - ");
        String s = mDormFlag;
        String s1 = stringbuilder.append(s).toString();
        log(s1);
        if(mDormFlag == null)
        {
            mDormFlag = "on";
            log("[FD] Dormant flag is NULL so change to on");
        }
        int i = TelephonyManager.getDefault().getNetworkType();
        boolean flag;
        if(i == 3 || i == 8 || i == 9 || i == 10)
        {
            if(mDormFlag.equals("off"))
                flag = false;
            else
                flag = true;
        } else
        {
            flag = false;
        }
        return flag;
    }

    public int getDunDataRxBarLevel()
    {
        return dunRxBarLevel;
    }

    public int getDunDataState()
    {
        return dunState;
    }

    public int getDunDataTxBarLevel()
    {
        return dunTxBarLevel;
    }

    public String getGateway(String s)
    {
        if(s != null) goto _L2; else goto _L1
_L1:
        if(mActivePdp == null) goto _L4; else goto _L3
_L3:
        String s3;
        StringBuilder stringbuilder = (new StringBuilder()).append("getGateway ");
        String s1 = mActivePdp.getGatewayAddress();
        String s2 = stringbuilder.append(s1).toString();
        log(s2);
        s3 = mActivePdp.getGatewayAddress();
_L9:
        return s3;
_L2:
        Iterator iterator = pdpList.iterator();
_L7:
        if(!iterator.hasNext()) goto _L4; else goto _L5
_L5:
        GsmDataConnection gsmdataconnection = (GsmDataConnection)(DataConnection)iterator.next();
        if(!gsmdataconnection.canHandleType(s)) goto _L7; else goto _L6
_L6:
        s3 = gsmdataconnection.getGatewayAddress();
        continue; /* Loop/switch isn't completed */
_L4:
        s3 = null;
        if(true) goto _L9; else goto _L8
_L8:
    }

    protected String getInterfaceName(String s)
    {
        if(s != null) goto _L2; else goto _L1
_L1:
        if(mActivePdp == null) goto _L4; else goto _L3
_L3:
        String s1 = mActivePdp.getInterface();
_L9:
        return s1;
_L2:
        Iterator iterator = pdpList.iterator();
_L7:
        if(!iterator.hasNext()) goto _L4; else goto _L5
_L5:
        GsmDataConnection gsmdataconnection = (GsmDataConnection)(DataConnection)iterator.next();
        if(!gsmdataconnection.canHandleType(s)) goto _L7; else goto _L6
_L6:
        s1 = gsmdataconnection.getInterface();
        continue; /* Loop/switch isn't completed */
_L4:
        s1 = null;
        if(true) goto _L9; else goto _L8
_L8:
    }

    protected String getIpAddress(String s)
    {
        if(s != null) goto _L2; else goto _L1
_L1:
        String s3;
        if(mActivePdp != null)
        {
            StringBuilder stringbuilder = (new StringBuilder()).append("getIpAddress ");
            String s1 = mActivePdp.getIpAddress();
            String s2 = stringbuilder.append(s1).toString();
            log(s2);
        }
        s3 = mActivePdp.getIpAddress();
_L4:
        return s3;
_L2:
        for(Iterator iterator = pdpList.iterator(); iterator.hasNext();)
        {
            GsmDataConnection gsmdataconnection = (GsmDataConnection)(DataConnection)iterator.next();
            if(gsmdataconnection.canHandleType(s))
            {
                s3 = gsmdataconnection.getIpAddress();
                continue; /* Loop/switch isn't completed */
            }
        }

        s3 = null;
        if(true) goto _L4; else goto _L3
_L3:
    }

    public int getIpAddressType(String s)
    {
        int i;
        if(mActivePdp != null && (s == null || mActiveApn != null && mActiveApn.canHandleType(s)))
            i = mActivePdp.ipaddresstype;
        else
            i = 1;
        return i;
    }

    public String getNetMask(String s)
    {
        String s3;
        if(mActivePdp != null && (s == null || mActiveApn != null && mActiveApn.canHandleType(s)))
        {
            StringBuilder stringbuilder = (new StringBuilder()).append("getNetMask ");
            String s1 = mActivePdp.getNetMask();
            String s2 = stringbuilder.append(s1).toString();
            log(s2);
            s3 = mActivePdp.getNetMask();
        } else
        {
            log("getNetMask null");
            s3 = null;
        }
        return s3;
    }

    protected com.android.internal.telephony.DataConnectionTracker.State getRequestedApnState(String s)
    {
        Iterator iterator = pdpList.iterator();
_L2:
        GsmDataConnection gsmdataconnection;
        com.android.internal.telephony.DataConnectionTracker.State state;
        do
        {
            if(!iterator.hasNext())
                break MISSING_BLOCK_LABEL_97;
            gsmdataconnection = (GsmDataConnection)(DataConnection)iterator.next();
        } while(gsmdataconnection == null || gsmdataconnection.getApn() == null || !gsmdataconnection.getApn().canHandleType(s));
        if(gsmdataconnection.isActive())
        {
            state = com.android.internal.telephony.DataConnectionTracker.State.CONNECTED;
        } else
        {
            if(!gsmdataconnection.isActivating())
                continue; /* Loop/switch isn't completed */
            state = com.android.internal.telephony.DataConnectionTracker.State.INITING;
        }
_L3:
        return state;
        if(!gsmdataconnection.isDisconnecting()) goto _L2; else goto _L1
_L1:
        state = com.android.internal.telephony.DataConnectionTracker.State.DISCONNECTING;
          goto _L3
        state = com.android.internal.telephony.DataConnectionTracker.State.IDLE;
          goto _L3
    }

    public com.android.internal.telephony.DataConnectionTracker.State getState()
    {
        boolean flag;
        boolean flag1;
        Iterator iterator;
        flag = false;
        flag1 = false;
        iterator = pdpList.iterator();
_L3:
        GsmDataConnection gsmdataconnection;
        do
        {
            if(!iterator.hasNext())
                break MISSING_BLOCK_LABEL_95;
            gsmdataconnection = (GsmDataConnection)(DataConnection)iterator.next();
        } while(gsmdataconnection.canHandleType("ims") || gsmdataconnection.canHandleType("admin"));
        if(!gsmdataconnection.isActive()) goto _L2; else goto _L1
_L1:
        com.android.internal.telephony.DataConnectionTracker.State state = com.android.internal.telephony.DataConnectionTracker.State.CONNECTED;
_L4:
        return state;
_L2:
        if(gsmdataconnection.isActivating())
            flag = true;
        if(gsmdataconnection.isDisconnecting())
            flag1 = true;
          goto _L3
        if(flag && flag1)
            state = com.android.internal.telephony.DataConnectionTracker.State.CONNECTED;
        else
        if(flag && !flag1)
            state = com.android.internal.telephony.DataConnectionTracker.State.CONNECTING;
        else
        if(!flag && flag1)
            state = com.android.internal.telephony.DataConnectionTracker.State.DISCONNECTING;
        else
        if(!flag && !flag1)
            state = com.android.internal.telephony.DataConnectionTracker.State.IDLE;
        else
            state = this.state;
          goto _L4
    }

    protected void handleIpv4Connect(GsmDataConnection gsmdataconnection, String s)
    {
        log("handleIpv4Connect");
        gsmdataconnection;
        JVM INSTR monitorenter ;
        if(gsmdataconnection == null)
            break MISSING_BLOCK_LABEL_119;
        mActivePdp = gsmdataconnection;
        ApnSetting apnsetting = gsmdataconnection.getApn();
        mActiveApn = apnsetting;
_L1:
        String s1 = mActiveApn.types[0];
        resetallApnsaddressInfo(s1);
        String s2 = gsmdataconnection.getApn().types[0];
        resetRetryByType(s2);
        notifyDefaultData(s);
        int i = gsmdataconnection.getApn().inactivityValue;
        String s3 = gsmdataconnection.getApn().types[0];
        String s4 = gsmdataconnection.getInterfaceName();
        startInactivityTimer(i, s3, s4, gsmdataconnection);
        gsmdataconnection;
        JVM INSTR monitorexit ;
        if(isOnDemandEnable)
        {
            return;
        } else
        {
            trySetupAllEnabledServices();
            return;
        }
        log("ar.result == null: This should not happen");
          goto _L1
        Exception exception;
        exception;
        gsmdataconnection;
        JVM INSTR monitorexit ;
        throw exception;
    }

    public void handleMessage(Message message)
    {
        StringBuilder stringbuilder = (new StringBuilder()).append("GSMDataConnTrack handleMessage ");
        Message message1 = message;
        String s = stringbuilder.append(message1).toString();
        GsmMultiDataConnectionTracker gsmmultidataconnectiontracker = this;
        String s1 = s;
        gsmmultidataconnectiontracker.log(s1);
        if(mGsmPhone.mIsTheCurrentActivePhone) goto _L2; else goto _L1
_L1:
        GsmMultiDataConnectionTracker gsmmultidataconnectiontracker1 = this;
        String s2 = "Ignore GSM msgs since GSM phone is inactive";
        gsmmultidataconnectiontracker1.log(s2);
_L29:
        return;
_L2:
        message.what;
        JVM INSTR lookupswitch 26: default 288
    //                   4: 294
    //                   6: 309
    //                   7: 367
    //                   11: 338
    //                   19: 299
    //                   26: 304
    //                   27: 372
    //                   28: 388
    //                   29: 393
    //                   32: 424
    //                   33: 485
    //                   42: 398
    //                   43: 977
    //                   46: 1164
    //                   47: 1681
    //                   48: 1460
    //                   49: 688
    //                   53: 1523
    //                   54: 1649
    //                   55: 1697
    //                   56: 1744
    //                   57: 1665
    //                   59: 2255
    //                   61: 2260
    //                   62: 2328
    //                   1000: 65;
           goto _L3 _L4 _L5 _L6 _L7 _L8 _L9 _L10 _L11 _L12 _L13 _L14 _L15 _L16 _L17 _L18 _L19 _L20 _L21 _L22 _L23 _L24 _L25 _L26 _L27 _L28 _L29
_L3:
        handleMessage(message);
        return;
_L4:
        onRecordsLoaded();
        return;
_L8:
        onGprsDetached();
        return;
_L9:
        onGprsAttached();
        return;
_L5:
        AsyncResult asyncresult = (AsyncResult)message.obj;
        GsmMultiDataConnectionTracker gsmmultidataconnectiontracker2 = this;
        AsyncResult asyncresult1 = asyncresult;
        boolean flag = false;
        gsmmultidataconnectiontracker2.onPdpStateChanged(asyncresult1, flag);
        return;
_L7:
        AsyncResult asyncresult2 = (AsyncResult)message.obj;
        GsmMultiDataConnectionTracker gsmmultidataconnectiontracker3 = this;
        AsyncResult asyncresult3 = asyncresult2;
        boolean flag1 = true;
        gsmmultidataconnectiontracker3.onPdpStateChanged(asyncresult3, flag1);
        return;
_L6:
        onPollPdp();
        return;
_L10:
        startNetStatPoll();
        long l = 0L;
        sentSinceLastRecv = l;
        return;
_L11:
        doRecovery();
        return;
_L12:
        onApnChanged();
        return;
_L15:
        int i = Log.d("GSM", "[DSAC DEB] EVENT_IPV6_ADDR_STATUS_CHANGED ");
        AsyncResult asyncresult4 = (AsyncResult)message.obj;
        onIpv6AddrStatusChanged(asyncresult4);
        return;
_L13:
        StringBuilder stringbuilder1 = (new StringBuilder()).append("[DSAC DEB] EVENT_PS_RESTRICT_ENABLED ");
        boolean flag2 = mIsPsRestricted;
        String s3 = stringbuilder1.append(flag2).toString();
        GsmMultiDataConnectionTracker gsmmultidataconnectiontracker4 = this;
        String s4 = s3;
        gsmmultidataconnectiontracker4.log(s4);
        stopNetStatPoll();
        boolean flag3 = true;
        mIsPsRestricted = flag3;
        return;
_L14:
        StringBuilder stringbuilder2 = (new StringBuilder()).append("[DSAC DEB] EVENT_PS_RESTRICT_DISABLED ");
        boolean flag4 = mIsPsRestricted;
        String s5 = stringbuilder2.append(flag4).toString();
        GsmMultiDataConnectionTracker gsmmultidataconnectiontracker5 = this;
        String s6 = s5;
        gsmmultidataconnectiontracker5.log(s6);
        boolean flag5 = false;
        mIsPsRestricted = flag5;
        com.android.internal.telephony.DataConnectionTracker.State state = this.state;
        com.android.internal.telephony.DataConnectionTracker.State state1 = com.android.internal.telephony.DataConnectionTracker.State.CONNECTED;
        if(state == state1)
        {
            startNetStatPoll();
            long l1 = 0L;
            sentSinceLastRecv = l1;
            return;
        }
        com.android.internal.telephony.DataConnectionTracker.State state2 = this.state;
        com.android.internal.telephony.DataConnectionTracker.State state3 = com.android.internal.telephony.DataConnectionTracker.State.FAILED;
        if(state2 == state3)
        {
            GsmMultiDataConnectionTracker gsmmultidataconnectiontracker6 = this;
            boolean flag6 = false;
            String s7 = "psRestrictEnabled";
            gsmmultidataconnectiontracker6.cleanUpConnection(flag6, s7);
            int j = 0;
            do
            {
                int i1 = mRetryMgr.length;
                int j1 = j;
                int k1 = i1;
                if(j1 >= k1)
                    break;
                mRetryMgr[j].resetRetryCount();
                int i2 = j + 1;
            } while(true);
            isThrottleDefaultReq = false;
            boolean flag7 = false;
            mReregisterOnReconnectFailure = flag7;
        }
        GsmMultiDataConnectionTracker gsmmultidataconnectiontracker7 = this;
        String s8 = "psRestrictEnabled";
        boolean flag8 = gsmmultidataconnectiontracker7.trySetupData(s8);
        return;
_L20:
        String s9;
        String s14;
        String s15;
        int j2 = Log.e("GSM", "stoping  InactivityTimer : EVENT_INACTIVITY_TIMER_EXPIRY");
        AsyncResult asyncresult5 = (AsyncResult)message.obj;
        s9 = null;
        if(asyncresult5.userObj instanceof String)
            s9 = (String)asyncresult5.userObj;
        StringBuilder stringbuilder3 = (new StringBuilder()).append("InactivityTimer : disabling ");
        String s11 = s9;
        String s12 = stringbuilder3.append(s11).toString();
        GsmMultiDataConnectionTracker gsmmultidataconnectiontracker8 = this;
        String s13 = s12;
        gsmmultidataconnectiontracker8.log(s13);
        s14 = "ims";
        s15 = s9;
        if(s14.equals(s15)) goto _L31; else goto _L30
_L30:
        String s16;
        String s17;
        s16 = "default";
        s17 = s9;
        if(!s16.equals(s17)) goto _L32; else goto _L31
_L31:
        Iterator iterator = pdpList.iterator();
        do
        {
            if(!iterator.hasNext())
                break;
            GsmDataConnection gsmdataconnection = (GsmDataConnection)(DataConnection)iterator.next();
            if(gsmdataconnection.isActive())
            {
                GsmDataConnection gsmdataconnection1 = gsmdataconnection;
                String s18 = s9;
                if(gsmdataconnection1.canHandleType(s18))
                {
                    GsmMultiDataConnectionTracker gsmmultidataconnectiontracker9 = this;
                    byte byte0 = 25;
                    String s19 = "pdndroppedbyNetwork";
                    Message message2 = gsmmultidataconnectiontracker9.obtainMessage(byte0, s19);
                    GsmDataConnection gsmdataconnection2 = gsmdataconnection;
                    Message message3 = message2;
                    gsmdataconnection2.disconnect(message3);
                }
            }
        } while(true);
          goto _L33
_L32:
        GsmMultiDataConnectionTracker gsmmultidataconnectiontracker10 = this;
        String s20 = s9;
        int k2 = gsmmultidataconnectiontracker10.apnTypeToId(s20);
        GsmMultiDataConnectionTracker gsmmultidataconnectiontracker11 = this;
        int l2 = k2;
        int i3 = 0;
        gsmmultidataconnectiontracker11.onEnableApn(l2, i3);
_L33:
        GsmMultiDataConnectionTracker gsmmultidataconnectiontracker12 = this;
        String s21 = s9;
        gsmmultidataconnectiontracker12.stopInactivityTimer(s21);
        sWakeLockConnect.setReferenceCounted(false);
        sWakeLockConnect.release();
        return;
_L16:
        GsmMultiDataConnectionTracker gsmmultidataconnectiontracker13 = this;
        String s22 = "ims";
        com.android.internal.telephony.DataConnectionTracker.State state4 = gsmmultidataconnectiontracker13.getRequestedApnState(s22);
        int j3 = Log.d("GSM", "EVENT_DEATCH_AFTER_IMS_DREG");
        com.android.internal.telephony.DataConnectionTracker.State state5 = com.android.internal.telephony.DataConnectionTracker.State.DISCONNECTING;
        com.android.internal.telephony.DataConnectionTracker.State state6 = state4;
        com.android.internal.telephony.DataConnectionTracker.State state7 = state5;
        if(state6 == state7)
            return;
        mImsSMSInterface.unregisterImsOnImsPdnDetach();
        if(message.obj != null)
        {
            int k3 = Log.d("GSM", " EVENT_DEATCH_AFTER_IMS_DREG :Calling disconnect");
            Iterator iterator1 = pdpList.iterator();
            do
            {
                GsmDataConnection gsmdataconnection3;
                GsmDataConnection gsmdataconnection4;
                String s23;
                do
                {
                    if(!iterator1.hasNext())
                        return;
                    gsmdataconnection3 = (GsmDataConnection)(DataConnection)iterator1.next();
                    gsmdataconnection4 = gsmdataconnection3;
                    s23 = "ims";
                } while(!gsmdataconnection4.canHandleType(s23));
                Object obj = message.obj;
                GsmMultiDataConnectionTracker gsmmultidataconnectiontracker14 = this;
                byte byte1 = 25;
                Object obj1 = obj;
                Message message4 = gsmmultidataconnectiontracker14.obtainMessage(byte1, obj1);
                GsmDataConnection gsmdataconnection5 = gsmdataconnection3;
                Message message5 = message4;
                gsmdataconnection5.disconnect(message5);
            } while(true);
        } else
        {
            int l3 = Log.d("GSM", " EVENT_DEATCH_AFTER_IMS_DREG :Calling explicitDetach");
            boolean flag9 = explicitDetach();
            return;
        }
_L17:
        String s10 = (String)message.obj;
        StringBuilder stringbuilder4 = (new StringBuilder()).append("EVENT_DETACH_REQ Received for ");
        String s24 = s10;
        String s25 = stringbuilder4.append(s24).toString();
        GsmMultiDataConnectionTracker gsmmultidataconnectiontracker15 = this;
        String s26 = s25;
        gsmmultidataconnectiontracker15.log(s26);
        GsmMultiDataConnectionTracker gsmmultidataconnectiontracker16 = this;
        String s27 = s10;
        if(!gsmmultidataconnectiontracker16.isApnTypeActive(s27))
            return;
        GsmMultiDataConnectionTracker gsmmultidataconnectiontracker17 = this;
        String s28 = s10;
        com.android.internal.telephony.DataConnectionTracker.State state8 = gsmmultidataconnectiontracker17.getRequestedApnState(s28);
        com.android.internal.telephony.DataConnectionTracker.State state9 = com.android.internal.telephony.DataConnectionTracker.State.DISCONNECTING;
        com.android.internal.telephony.DataConnectionTracker.State state10 = state8;
        com.android.internal.telephony.DataConnectionTracker.State state11 = state9;
        if(state10 != state11)
        {
            Iterator iterator2 = pdpList.iterator();
            GsmDataConnection gsmdataconnection6;
            ApnSetting apnsetting;
            String s29;
            do
            {
                do
                {
                    if(!iterator2.hasNext())
                        return;
                    gsmdataconnection6 = (GsmDataConnection)(DataConnection)iterator2.next();
                } while(gsmdataconnection6 == null || gsmdataconnection6.getApn() == null);
                apnsetting = gsmdataconnection6.getApn();
                s29 = s10;
            } while(!apnsetting.canHandleType(s29));
            StringBuilder stringbuilder5 = (new StringBuilder()).append("EVENT_DETACH_REQ Active DefEpsBearer found Disconnecting ..");
            String s30 = s10;
            String s31 = stringbuilder5.append(s30).toString();
            GsmMultiDataConnectionTracker gsmmultidataconnectiontracker18 = this;
            String s32 = s31;
            gsmmultidataconnectiontracker18.log(s32);
            GsmMultiDataConnectionTracker gsmmultidataconnectiontracker19 = this;
            byte byte2 = 25;
            Message message6 = gsmmultidataconnectiontracker19.obtainMessage(byte2);
            int i4 = message.arg1;
            message6.arg1 = i4;
            int j4 = message.arg2;
            message6.arg2 = j4;
            GsmDataConnection gsmdataconnection7 = gsmdataconnection6;
            Message message7 = message6;
            gsmdataconnection7.detach(message7);
            return;
        } else
        {
            GsmMultiDataConnectionTracker gsmmultidataconnectiontracker20 = this;
            String s33 = "EVENT_DETACH_REQ State found is DISCONNECTING";
            gsmmultidataconnectiontracker20.log(s33);
            return;
        }
_L19:
        int k4 = mGsmPhone.mSST.getCurrentGprsState();
        String s34 = (String)qOnDemandPdnRequestQueue.poll();
        if(k4 == 0)
        {
            GsmMultiDataConnectionTracker gsmmultidataconnectiontracker21 = this;
            String s35 = "onDemand-Attach is completed by this time so request will be served automatically";
            gsmmultidataconnectiontracker21.log(s35);
            return;
        } else
        {
            GsmMultiDataConnectionTracker gsmmultidataconnectiontracker22 = this;
            String s36 = "onDemand-Attach is not completed so we need to notify fail to applications";
            gsmmultidataconnectiontracker22.log(s36);
            return;
        }
_L21:
        if(mIsWifiConnected)
            return;
        if(!mIsScreenOn)
            return;
        GsmMultiDataConnectionTracker gsmmultidataconnectiontracker23 = this;
        String s37 = "default";
        if(!gsmmultidataconnectiontracker23.isApnTypeActive(s37))
        {
            GsmMultiDataConnectionTracker gsmmultidataconnectiontracker24 = this;
            String s38 = "*";
            if(!gsmmultidataconnectiontracker24.isApnTypeActive(s38))
            {
                String s39 = mRequestedApnType;
                GsmMultiDataConnectionTracker gsmmultidataconnectiontracker25 = this;
                String s40 = s39;
                int l4 = gsmmultidataconnectiontracker25.apnTypeToId(s40);
                GsmMultiDataConnectionTracker gsmmultidataconnectiontracker26 = this;
                int i5 = l4;
                int j5 = 1;
                gsmmultidataconnectiontracker26.onEnableApn(i5, j5);
                return;
            } else
            {
                GsmMultiDataConnectionTracker gsmmultidataconnectiontracker27 = this;
                String s41 = "APN Request for default Data is not sent because APN is already active";
                gsmmultidataconnectiontracker27.log(s41);
                return;
            }
        } else
        {
            GsmMultiDataConnectionTracker gsmmultidataconnectiontracker28 = this;
            String s42 = "APN Request for default Data is not sent because APN is already active";
            gsmmultidataconnectiontracker28.log(s42);
            return;
        }
_L22:
        GsmMultiDataConnectionTracker gsmmultidataconnectiontracker29 = this;
        String s43 = " sendPsAttachInfo EVENT_ATTACH_INFO_RECEIVED";
        gsmmultidataconnectiontracker29.log(s43);
        return;
_L25:
        GsmMultiDataConnectionTracker gsmmultidataconnectiontracker30 = this;
        String s44 = " setUpDedicatedBearer EVENT_SETUP_DEDICATED_BEARER_DONE";
        gsmmultidataconnectiontracker30.log(s44);
        return;
_L18:
        AsyncResult asyncresult6 = (AsyncResult)message.obj;
        onNetworkDisconnectReq(asyncresult6);
        return;
_L23:
        int k5 = Log.d("GSM", "gmsmULTI::handleMessage() case: EVENT_HANDOVER_INITIATE_START");
        GsmMultiDataConnectionTracker gsmmultidataconnectiontracker31 = this;
        String s45 = "[Handover]**L2C phase 2 : pass ho params to handovertracker";
        gsmmultidataconnectiontracker31.log(s45);
        HandoverTracker handovertracker = mHandoverTracker;
        ArrayList arraylist = getApnListForHandover();
        boolean flag10 = handovertracker.startHandoverFromLte(arraylist);
        return;
_L24:
        Iterator iterator3;
        int l5;
        String s48;
        GsmMultiDataConnectionTracker gsmmultidataconnectiontracker32 = this;
        String s46 = "[Handover] **C2L phase 4 : DCT is receiving params and start triggering";
        gsmmultidataconnectiontracker32.log(s46);
        GsmMultiDataConnectionTracker gsmmultidataconnectiontracker33 = this;
        String s47 = "EVENT_HANDOVER_PARAMS_RECEIVED";
        gsmmultidataconnectiontracker33.log(s47);
        ArrayList arraylist1 = mHandoverTracker.getFields();
        GsmMultiDataConnectionTracker gsmmultidataconnectiontracker34 = this;
        ArrayList arraylist2 = arraylist1;
        gsmmultidataconnectiontracker34.putApnListForHandover(arraylist2);
        l5 = 0;
        s48 = "ims";
        clearAllPendingApnRequest();
        if(allApns == null)
            return;
        iterator3 = allApns.iterator();
_L36:
        ApnSetting apnsetting1;
        String s49;
        String s50;
        String s51;
        String s52;
        int i6;
        int j6;
        String s53;
        String s54;
        if(!iterator3.hasNext())
            return;
        apnsetting1 = (ApnSetting)iterator3.next();
        s49 = apnsetting1.apn;
        s50 = apnsetting1.types[0];
        s51 = apnsetting1.user;
        s52 = apnsetting1.password;
        i6 = apnsetting1.authType;
        j6 = apnsetting1.ipType;
        s53 = apnsetting1.ipv4;
        s54 = apnsetting1.ipv6;
        ApnSetting apnsetting2 = apnsetting1;
        if(s53 != null || s54 != null) goto _L35; else goto _L34
_L34:
        j6 = 3;
        l5 = 0;
_L37:
        ApnSetting apnsetting3 = apnsetting1;
        String s55 = s48;
        if(apnsetting3.canHandleType(s55))
        {
            String s56 = (new StringBuilder()).append("Ims pdn values apn ").append(s49).append(" user ").append(s51).toString();
            int k6 = Log.i("GSM", s56);
            int l6;
            if(l5 == 0)
                l6 = Log.i("GSM", "IPtype changed to IPV4V6 since initial IMS");
            CommandsInterface commandsinterface = phone.mCM;
            String s57 = Integer.toString(1);
            String s58 = Integer.toString(0);
            String s59 = Integer.toString(i6);
            String s60 = Integer.toString(j6);
            String s61 = Integer.toString(1);
            String s62 = Integer.toString(l5);
            GsmMultiDataConnectionTracker gsmmultidataconnectiontracker35 = this;
            byte byte3 = 31;
            Message message8 = gsmmultidataconnectiontracker35.obtainMessage(byte3);
            commandsinterface.setupPsAttach(s57, s58, s49, s51, s52, s59, s60, s61, s62, s53, s54, s50, message8);
        } else
        {
            int i7 = j6;
            byte byte4 = 3;
            if(i7 != byte4)
            {
                int j7 = Log.i("GSM", "adding handover requests to pending list");
                String s63 = apnsetting1.types[0];
                GsmMultiDataConnectionTracker gsmmultidataconnectiontracker36 = this;
                String s64 = s63;
                gsmmultidataconnectiontracker36.addPendingApnRequest(s64);
            } else
            {
                StringBuilder stringbuilder6 = (new StringBuilder()).append("ignoring handover requests ");
                String s65 = apnsetting1.apn;
                String s66 = stringbuilder6.append(s65).toString();
                int k7 = Log.i("GSM", s66);
            }
        }
        if(true) goto _L36; else goto _L35
_L35:
        if(s53 != null && s54 != null)
        {
            j6 = 2;
            l5 = 1;
        } else
        if(s53 != null && s54 == null)
        {
            j6 = 0;
            l5 = 1;
        } else
        if(s53 == null && s54 != null)
        {
            j6 = 1;
            l5 = 1;
        }
          goto _L37
_L26:
        trySetupNextData();
        return;
_L27:
        GsmMultiDataConnectionTracker gsmmultidataconnectiontracker37 = this;
        String s67 = "[Handover] ** Resume Timeout or Handover Fail";
        gsmmultidataconnectiontracker37.log(s67);
        int k = 0;
        do
        {
            int l7 = mRetryMgr.length;
            int i8 = k;
            int j8 = l7;
            if(i8 < j8)
            {
                mRetryMgr[k].resetRetryCount();
                k++;
            } else
            {
                isThrottleDefaultReq = false;
                trySetupAllEnabledServices();
                return;
            }
        } while(true);
_L28:
        String s68 = android.provider.Settings.System.getString(phone.getContext().getContentResolver(), "mode_type");
        String s69 = s68;
        String s70 = "LTE";
        if(!s69.equals(s70))
        {
            String s71 = s68;
            String s72 = "GLOBAL";
            if(!s71.equals(s72))
                return;
        }
        GsmMultiDataConnectionTracker gsmmultidataconnectiontracker38 = this;
        String s73 = "selectioin mdoe is LTE or GLOBAL. sendPsAttachInfo()";
        gsmmultidataconnectiontracker38.log(s73);
        sendPsAttachInfo();
        return;
    }

    public void imsDeregistrationOnDetach(int i, int j)
    {
        String s = (new StringBuilder()).append("imsDeregistrationOnDetach : reason ").append(j).append("reattach_required = ").append(i).toString();
        int k = Log.d("GSM", s);
        Message message = obtainMessage(43);
        message.arg1 = i;
        message.arg2 = j;
        boolean flag = sendMessage(message);
    }

    public void imsDeregistrationOnDisconnect(String s)
    {
        String s1 = (new StringBuilder()).append("imsDeregistrationOnDisconnect : reason ").append(s).toString();
        int i = Log.d("GSM", s1);
        Message message = obtainMessage(43);
        message.obj = s;
        boolean flag = sendMessage(message);
    }

    public boolean isAllDataConnectionInactive()
    {
        Iterator iterator = pdpList.iterator();
_L4:
        if(!iterator.hasNext()) goto _L2; else goto _L1
_L1:
        if(((GsmDataConnection)(DataConnection)iterator.next()).isInactive()) goto _L4; else goto _L3
_L3:
        boolean flag = false;
_L6:
        return flag;
_L2:
        flag = true;
        if(true) goto _L6; else goto _L5
_L5:
    }

    public boolean isAnyApnTypeActive()
    {
        Iterator iterator = pdpList.iterator();
_L4:
        if(!iterator.hasNext()) goto _L2; else goto _L1
_L1:
        if(!((GsmDataConnection)(DataConnection)iterator.next()).isActive()) goto _L4; else goto _L3
_L3:
        boolean flag = true;
_L6:
        return flag;
_L2:
        flag = false;
        if(true) goto _L6; else goto _L5
_L5:
    }

    public boolean isApnTypeActive(String s)
    {
        Iterator iterator = pdpList.iterator();
_L4:
        if(!iterator.hasNext()) goto _L2; else goto _L1
_L1:
        GsmDataConnection gsmdataconnection = (GsmDataConnection)(DataConnection)iterator.next();
        if(!gsmdataconnection.canHandleType(s) || !gsmdataconnection.isActive()) goto _L4; else goto _L3
_L3:
        boolean flag = true;
_L6:
        return flag;
_L2:
        flag = false;
        if(true) goto _L6; else goto _L5
_L5:
    }

    protected boolean isApnTypeAvailable(String s)
    {
        if(!s.equals("dun")) goto _L2; else goto _L1
_L1:
        boolean flag;
        if(fetchDunApn() != null)
            flag = true;
        else
            flag = false;
_L4:
        return flag;
_L2:
label0:
        {
            if(allApns == null)
                break label0;
            Iterator iterator = allApns.iterator();
            do
                if(!iterator.hasNext())
                    break label0;
            while(!((ApnSetting)iterator.next()).canHandleType(s));
            flag = true;
            continue; /* Loop/switch isn't completed */
        }
        flag = false;
        if(true) goto _L4; else goto _L3
_L3:
    }

    public boolean isDataConnectionAsDesired()
    {
        boolean flag = phone.getServiceState().getRoaming();
        boolean flag1;
        if(mGsmPhone.mSIMRecords.getRecordsLoaded() && mGsmPhone.mSST.getCurrentGprsState() == 0 && (!flag || getDataOnRoamingEnabled()) && !mIsWifiConnected && !mIsPsRestricted)
            flag1 = isAnyApnTypeActive();
        else
            flag1 = true;
        return flag1;
    }

    protected boolean isDeferringDataRecovery()
    {
        com.android.internal.telephony.DataConnectionTracker.State state = getRequestedApnState("admin");
        com.android.internal.telephony.DataConnectionTracker.State state1 = com.android.internal.telephony.DataConnectionTracker.State.CONNECTED;
        boolean flag;
        if(state == state1)
            flag = true;
        else
            flag = false;
        return flag;
    }

    protected void log(String s)
    {
        String s1 = (new StringBuilder()).append("[GsmMultiDCT] ").append(s).toString();
        int i = Log.d("GSM", s1);
    }

    protected void onCleanUpConnection(boolean flag, String s)
    {
        cleanUpConnection(flag, s);
    }

    public void onDataRoamingChanged()
    {
        int i = Log.d("GSM", "GsmMiltiDataConnection onDataRoamingChanged");
        if(mGsmPhone.getServiceState().getRoaming())
        {
            if(IsDataOnRoamingApply())
            {
                boolean flag = getDataOnRoamingEnabled();
                if(flag)
                {
                    String s = (new StringBuilder()).append("onDataRoamingChanged => restore data call state = ").append(flag).toString();
                    int j = Log.d("GSM", s);
                    trySetupAllEnabledServices();
                    return;
                } else
                {
                    int k = Log.d("GSM", "onDataRoamingChanged => kill data call");
                    int l = Log.d("GSM", "Tear down data connection on roaming");
                    cleanUpConnection(true, "roamingOn");
                    return;
                }
            } else
            {
                int i1 = Log.d("GSM", "onDataRoamingChanged => Don't apply -USA- Do nothing");
                return;
            }
        } else
        {
            int j1 = Log.d("GSM", "onDataRoamingChanged => No Roaming - Do nothing");
            return;
        }
    }

    protected void onDataSetupComplete(AsyncResult asyncresult)
    {
        String s;
        com.android.internal.telephony.DataConnection.FailCause failcause;
        s = null;
        if(asyncresult.userObj instanceof String)
            s = (String)asyncresult.userObj;
        if(asyncresult.exception == null)
        {
            GsmDataConnection gsmdataconnection = (GsmDataConnection)(GsmDataConnection)asyncresult.result;
            if(gsmdataconnection == null)
            {
                Message message = obtainMessage(59);
                boolean flag = sendMessage(message);
                return;
            }
            if(isApnTypeActive("default"))
            {
                log("onDataSetupComplete gsm.defaultpdpcontext.active == true");
                SystemProperties.set("gsm.defaultpdpcontext.active", "true");
                if(canSetPreferApn && preferredApn == null)
                {
                    log("PREFERRED APN is null");
                    ApnSetting apnsetting = gsmdataconnection.getApn();
                    preferredApn = apnsetting;
                    int i = preferredApn.id;
                    setPreferredApn(i);
                }
            }
            Iterator iterator = pdpList.iterator();
            do
            {
                if(!iterator.hasNext())
                    break;
                GsmDataConnection gsmdataconnection1 = (GsmDataConnection)(DataConnection)iterator.next();
                int j = gsmdataconnection1.getCid();
                int k = gsmdataconnection.getCid();
                if(j != k)
                {
                    log("Updated Master PDP list as ipv4 is configured");
                    gsmdataconnection1.isipv4configured = 1;
                }
            } while(true);
            printProperties(asyncresult);
            if(gsmdataconnection.canHandleType("default"))
            {
                log("onDataSetupComplete :setting isThrottleDefaultReq false");
                isThrottleDefaultReq = false;
                mTrafficMonitor.start();
            }
            if(waitingApns != null)
            {
                ArrayList arraylist = waitingApns;
                ArrayList arraylist1 = waitingApns;
                boolean flag1 = arraylist.removeAll(arraylist1);
            }
            if(gsmdataconnection.ipaddresstype == 1)
            {
                log("onDataSetupComplete IpAddress Type is IPv4 alone so send notify");
                handleIpv4Connect(gsmdataconnection, s);
                return;
            }
            if(gsmdataconnection.ipaddresstype == 2)
            {
                log("onDataSetupComplete IpAddress Type is IPv6 ");
                processPendingIpv6DataCallState(gsmdataconnection);
                if(gsmdataconnection.isipv6configured != 1)
                {
                    return;
                } else
                {
                    handleIpv4Connect(gsmdataconnection, s);
                    return;
                }
            }
            if(gsmdataconnection.ipaddresstype != 3)
                return;
            log("onDataSetupComplete IpAddress Type is IPv4v6 ");
            if(gsmdataconnection.isipv6configured == 1)
            {
                handleIpv4Connect(gsmdataconnection, s);
                return;
            }
            String s1 = gsmdataconnection.getApn().apn;
            if(!findPendingIpv6DataCallStateByApn(s1))
                return;
            log("onDataSetupComplete IPv6 address configuration process is done ");
            processPendingIpv6DataCallState(gsmdataconnection);
            if(gsmdataconnection.isipv6configured == 1)
            {
                log("onDataSetupComplete IPv6 address configuration is successful so notify to APP ");
                handleIpv4Connect(gsmdataconnection, s);
                return;
            }
            if(gsmdataconnection.canHandleType("ims"))
            {
                return;
            } else
            {
                log("onDataSetupComplete IPv6 address configuration failed but apnType is non IMS ");
                handleIpv4Connect(gsmdataconnection, s);
                return;
            }
        }
        com.android.internal.telephony.DataConnection.FailResult failresult = (com.android.internal.telephony.DataConnection.FailResult)(com.android.internal.telephony.DataConnection.FailResult)asyncresult.result;
        failcause = failresult.getFailCause();
        GsmDataConnection gsmdataconnection2 = (GsmDataConnection)(GsmDataConnection)failresult.getConnection();
        if(gsmdataconnection2 != null)
        {
            log("Receive Failed result");
            mActivePdp = gsmdataconnection2;
            ApnSetting apnsetting1 = failresult.getApn();
            mActiveApn = apnsetting1;
        }
        String s2 = (new StringBuilder()).append("PDP setup failed ").append(failcause).toString();
        log(s2);
        if(mActiveApn != null)
        {
            String s3 = mActiveApn.types[0];
            resetallApnsaddressInfo(s3);
            String s4 = mActiveApn.apn;
            removePendingIpv6DataCallList(s4);
        }
        if(failcause.isEventLoggable())
        {
            GsmCellLocation gsmcelllocation = (GsmCellLocation)phone.getCellLocation();
            int l = 50105;
            Object aobj[] = new Object[3];
            Integer integer = Integer.valueOf(failcause.ordinal());
            aobj[0] = integer;
            int i1 = 1;
            int j1;
            Integer integer1;
            Integer integer2;
            int k1;
            String s5;
            int l1;
            if(gsmcelllocation != null)
                j1 = gsmcelllocation.getCid();
            else
                j1 = -1;
            integer1 = Integer.valueOf(j1);
            aobj[i1] = integer1;
            integer2 = Integer.valueOf(TelephonyManager.getDefault().getNetworkType());
            aobj[2] = integer2;
            k1 = EventLog.writeEvent(l, aobj);
        }
        if(failcause.isPermanentFail())
        {
            notifyNoData(failcause);
            phone.notifyDataConnection("apnFailed");
            s5 = mCurrentRequestedApnType;
            l1 = apnTypeToId(s5);
            onEnableApn(l1, 0);
            return;
        }
        if(waitingApns != null && !waitingApns.isEmpty())
        {
            StringBuilder stringbuilder = (new StringBuilder()).append("Removing waiting apns: current size(");
            int i2 = waitingApns.size();
            String s6 = stringbuilder.append(i2).append(")").toString();
            log(s6);
            Object obj = waitingApns.remove(0);
        }
        if(!waitingApns.isEmpty()) goto _L2; else goto _L1
_L1:
        if(mActiveApn == null) goto _L4; else goto _L3
_L3:
        if(!mActiveApn.canHandleType("default") || isOnDemandEnable) goto _L6; else goto _L5
_L5:
        log("onDataSetupComplete : Start delayed retry for INTERNET");
        startDelayedRetry(failcause, s);
_L4:
        if(mActivePdp != null)
            mActivePdp.resetSynchronously();
        Message message1 = obtainMessage(59);
        boolean flag2 = sendMessage(message1);
        return;
_L6:
        if(mActiveApn.canHandleType("ims"))
        {
            com.android.internal.telephony.DataConnection.FailCause failcause1 = com.android.internal.telephony.DataConnection.FailCause.NETWORK_FAILURE;
            if(failcause == failcause1)
            {
                imsDeregistrationOnDisconnect("pdndroppedbyNetwork");
                continue; /* Loop/switch isn't completed */
            }
        }
        if(!isOnDemandEnable)
            startDelayedRetry(failcause, s);
        else
        if(!mActiveApn.canHandleType("ims") && isOnDemandEnable)
        {
            com.android.internal.telephony.DataConnectionTracker.State state = com.android.internal.telephony.DataConnectionTracker.State.FAILED;
            setState(state);
        }
        if(true) goto _L4; else goto _L2
_L2:
        com.android.internal.telephony.DataConnectionTracker.State state1 = com.android.internal.telephony.DataConnectionTracker.State.SCANNING;
        setState(state1);
        Message message2 = obtainMessage(5, s);
        boolean flag3 = sendMessageDelayed(message2, 5000L);
        return;
    }

    protected void onDisconnectDone(AsyncResult asyncresult)
    {
        String s = null;
        log("EVENT_DISCONNECT_DONE");
        if(asyncresult.userObj instanceof String)
            s = (String)asyncresult.userObj;
        com.android.internal.telephony.DataConnectionTracker.State state;
        if(asyncresult.result != null)
        {
            if(asyncresult.result instanceof com.android.internal.telephony.DataConnection.DisconnectResult)
            {
                com.android.internal.telephony.DataConnection.DisconnectResult disconnectresult = (com.android.internal.telephony.DataConnection.DisconnectResult)(com.android.internal.telephony.DataConnection.DisconnectResult)asyncresult.result;
                GsmDataConnection gsmdataconnection = (GsmDataConnection)(GsmDataConnection)disconnectresult.getConnection();
                mActivePdp = gsmdataconnection;
                ApnSetting apnsetting = disconnectresult.getApn();
                mActiveApn = apnsetting;
            } else
            {
                log("ar.result is not DisconnectResult instance");
                GsmDataConnection gsmdataconnection1 = (GsmDataConnection)(GsmDataConnection)asyncresult.result;
                mActivePdp = gsmdataconnection1;
                ApnSetting apnsetting1 = mActivePdp.getApn();
                mActiveApn = apnsetting1;
            }
        } else
        {
            log("ar.result == null: This should not happen");
        }
        if(mActiveApn != null && mActiveApn.types != null)
        {
            String s1 = mActiveApn.types[0];
            stopInactivityTimer(s1);
        }
        mActivePdp.resetSynchronously();
        state = com.android.internal.telephony.DataConnectionTracker.State.IDLE;
        setState(state);
        if(!"handoverdisconncted".equals(s))
        {
            log("onDisconnectDone: Normal Disconnect send notify");
            phone.notifyDataConnection(s);
        } else
        {
            log("onDisconnectDone: Handover Disconnect ");
        }
        if(waitingApns != null)
        {
            ArrayList arraylist = waitingApns;
            ArrayList arraylist1 = waitingApns;
            boolean flag = arraylist.removeAll(arraylist1);
        }
        if(!isApnTypeActive("default"))
        {
            log("onDisconnectDone gsm.defaultpdpcontext.active == false");
            SystemProperties.set("gsm.defaultpdpcontext.active", "false");
            mTrafficMonitor.stop();
        } else
        {
            log("onDisconnectDone isApnTypeActive(Phone.APN_TYPE_DEFAULT) == true");
        }
        if(isAllDataConnectionInactive())
        {
            mActiveApn = null;
            stopNetStatPoll();
            SystemProperties.set("gsm.defaultpdpcontext.active", "false");
        }
        if(!retryAfterDisconnected(s))
        {
            return;
        } else
        {
            trySetupAllEnabledServices();
            return;
        }
    }

    /**
     * @deprecated Method onEnableApn is deprecated
     */

    protected void onEnableApn(int i, int j)
    {
        this;
        JVM INSTR monitorenter ;
        onEnableApn(i, j, "apnDisabled");
        this;
        JVM INSTR monitorexit ;
        return;
        Exception exception;
        exception;
        throw exception;
    }

    /**
     * @deprecated Method onEnableApn is deprecated
     */

    protected void onEnableApn(int i, int j, String s)
    {
        this;
        JVM INSTR monitorenter ;
        String s4;
        String s1 = (new StringBuilder()).append("EVENT_APN_ENABLE_REQUEST ").append(i).append(", ").append(j).append(",reason : ").append(s).toString();
        log(s1);
        StringBuilder stringbuilder = (new StringBuilder()).append(" dataEnabled = ");
        boolean flag = dataEnabled[i];
        StringBuilder stringbuilder1 = stringbuilder.append(flag).append(", enabledCount = ");
        int k = enabledCount;
        StringBuilder stringbuilder2 = stringbuilder1.append(k).append(", isApnTypeActive = ");
        String s2 = apnIdToType(i);
        boolean flag1 = isApnTypeActive(s2);
        String s3 = stringbuilder2.append(flag1).toString();
        log(s3);
        s4 = apnIdToType(i);
        if(j != 1) goto _L2; else goto _L1
_L1:
        if(dataEnabled[i] == null)
        {
            dataEnabled[i] = true;
            int l = enabledCount + 1;
            enabledCount = l;
        }
        if(!isApnTypeActive(s4))
        {
            mRequestedApnType = s4;
            onEnableNewApn();
        }
_L4:
        this;
        JVM INSTR monitorexit ;
        return;
_L2:
        if(dataEnabled[i] == null) goto _L4; else goto _L3
_L3:
        int k1;
        Iterator iterator;
        dataEnabled[i] = false;
        int i1 = enabledCount - 1;
        enabledCount = i1;
        StringBuilder stringbuilder3 = (new StringBuilder()).append("enabledCount to be checked is ");
        int j1 = enabledCount;
        String s5 = stringbuilder3.append(j1).toString();
        log(s5);
        String s6 = apnIdToType(i);
        removeFromPendingRequestedApns(s6);
        k1 = 0;
        iterator = pdpList.iterator();
_L15:
        if(!iterator.hasNext()) goto _L4; else goto _L5
_L5:
        GsmDataConnection gsmdataconnection;
        boolean flag3;
        int l1;
        gsmdataconnection = (GsmDataConnection)(DataConnection)iterator.next();
        String s7 = apnIdToType(i);
        boolean flag2 = gsmdataconnection.canHandleType(s7);
        StringBuilder stringbuilder4 = (new StringBuilder()).append("idx ").append(k1).append(": ");
        String s8 = gsmdataconnection.getStateAsString();
        StringBuilder stringbuilder5 = stringbuilder4.append(s8).append(", apn(");
        ApnSetting apnsetting = gsmdataconnection.getApn();
        String s9 = stringbuilder5.append(apnsetting).append("), ").append("canHandle(").append(flag2).append(")").toString();
        log(s9);
        if(gsmdataconnection.isInactive() || !flag2)
            break MISSING_BLOCK_LABEL_653;
        flag3 = false;
        l1 = 0;
_L16:
        if(l1 >= 8) goto _L7; else goto _L6
_L6:
        if(i == l1) goto _L9; else goto _L8
_L8:
        String s10 = apnIdToType(l1);
        if(!gsmdataconnection.canHandleType(s10) || dataEnabled[l1] == null) goto _L9; else goto _L10
_L10:
        log("Apn used by other connection");
        flag3 = true;
_L7:
        if(flag3) goto _L12; else goto _L11
_L11:
        if(!gsmdataconnection.isDisconnecting()) goto _L14; else goto _L13
_L13:
        log("Already in disconnecting state");
_L12:
        k1++;
          goto _L15
_L9:
        l1++;
          goto _L16
_L14:
        String s11 = (new StringBuilder()).append("Disconnect pdp(").append(k1).append(")").toString();
        log(s11);
        mRequestedApnType = s4;
        if(gsmdataconnection.canHandleType("ims") || !"apnDisabled".equals(s)) goto _L18; else goto _L17
_L17:
        Message message = obtainMessage(25, "nonImsAddressConfFail");
        gsmdataconnection.disconnect(message);
          goto _L12
        Exception exception;
        exception;
        throw exception;
_L18:
        byte byte0 = 25;
        Message message1 = obtainMessage(byte0, "apnDisabled");
        gsmdataconnection.disconnect(message1);
          goto _L12
        String s12 = (new StringBuilder()).append("idx ").append(k1).append(" pass!!").toString();
        log(s12);
          goto _L12
    }

    protected void onEnableNewApn()
    {
        StringBuilder stringbuilder = (new StringBuilder()).append("Enable new APN: mRequestedApnType(");
        String s = mRequestedApnType;
        String s1 = stringbuilder.append(s).append(")").toString();
        log(s1);
        if(!mIsSimSupportMultiPdp)
        {
            String s2 = mRequestedApnType;
            mCurrentRequestedApnType = s2;
            clearAllPendingApnRequest();
            cleanUpConnection(true, "apnSwitched");
            return;
        } else
        {
            log("onEnableNewApn: Adding to pending list and raising event for trySetupNextData().");
            String s3 = mRequestedApnType;
            resetRetryByType(s3);
            String s4 = mRequestedApnType;
            addPendingApnRequest(s4);
            Message message = obtainMessage(59);
            boolean flag = sendMessage(message);
            return;
        }
    }

    protected void onGprsDetached()
    {
        stopNetStatPoll();
        phone.notifyDataConnection("gprsDetached");
        log("onGprsDetached");
        emptyOnDemandPdnRequestQueue();
        isThrottleDefaultReq = false;
        int i = 0;
        do
        {
            int j = mRetryMgr.length;
            if(i < j)
            {
                mRetryMgr[i].resetRetryCount();
                i++;
            } else
            {
                clearAllPendingApnRequest();
                return;
            }
        } while(true);
    }

    public void onHOCleanupHOAPN()
    {
        int i = Log.d("GSM", "[onHOCleanupHOAPN]: Cleaning up APN information ");
        if(allApns != null)
        {
            for(Iterator iterator = allApns.iterator(); iterator.hasNext();)
            {
                ApnSetting apnsetting = (ApnSetting)iterator.next();
                StringBuilder stringbuilder = (new StringBuilder()).append("[GsmMultiDCT]: onHOCleanupHOAPN: resetting IPaddress and IPtype for");
                String s = apnsetting.apn;
                String s1 = stringbuilder.append(s).toString();
                log(s1);
                apnsetting.ipv4 = null;
                apnsetting.ipv6 = null;
            }

        }
        clearAllPendingApnRequest();
        int j = 0;
        do
        {
            int k = mRetryMgr.length;
            if(j < k)
            {
                mRetryMgr[j].resetRetryCount();
                j++;
            } else
            {
                isThrottleDefaultReq = false;
                return;
            }
        } while(true);
    }

    public void onHome()
    {
        int i = Log.d("GSM", "GsmMiltiDataConnection onHome - skip it");
    }

    protected void onIpv6AddrStatusChanged(AsyncResult asyncresult)
    {
        ArrayList arraylist;
        GsmDataConnection gsmdataconnection;
        String s1;
        String s2;
        boolean flag;
label0:
        {
label1:
            {
                int i = Log.d("GSM", "[DSAC DEB] onIpv6AddrStatusChanged");
                arraylist = (ArrayList)asyncresult.result;
                gsmdataconnection = null;
                String s = ((DataCallState)arraylist.get(0)).apn;
                int j = ((DataCallState)arraylist.get(0)).cid;
                s1 = ((DataCallState)arraylist.get(0)).address;
                s2 = null;
                flag = false;
                StringBuilder stringbuilder = (new StringBuilder()).append("[DSAC DEB] onIpv6AddrStatusChanged cid ");
                int k = j;
                StringBuilder stringbuilder1 = stringbuilder.append(k).append("apn ");
                String s3 = s;
                String s4 = stringbuilder1.append(s3).toString();
                int l = Log.d("GSM", s4);
                StringBuilder stringbuilder2 = (new StringBuilder()).append("defList : onIpv6AddrStatusChanged ");
                ArrayList arraylist1 = pdpList;
                String s5 = stringbuilder2.append(arraylist1).toString();
                int i1 = Log.e("GSM", s5);
                Iterator iterator = pdpList.iterator();
label2:
                do
                {
                    GsmDataConnection gsmdataconnection1;
label3:
                    {
                        if(iterator.hasNext())
                        {
                            gsmdataconnection1 = (GsmDataConnection)(DataConnection)iterator.next();
                            if(gsmdataconnection1.isInactive() || gsmdataconnection1.getApn() == null)
                                break label3;
                            String s6 = gsmdataconnection1.getApn().apn;
                            String s7 = s;
                            if(!s6.equals(s7) || gsmdataconnection1.ipaddresstype == 0)
                                break label3;
                            flag = true;
                            gsmdataconnection = gsmdataconnection1;
                            s2 = gsmdataconnection.ipv6Address;
                            gsmdataconnection.ipv6Address = s1;
                            StringBuilder stringbuilder3 = (new StringBuilder()).append("[DSAC DEB] onIpv6AddrStatusChanged: ");
                            String s8 = gsmdataconnection.ipv6Address;
                            String s9 = stringbuilder3.append(s8).toString();
                            int j1 = Log.d("GSM", s9);
                            int k1 = 1;
                            gsmdataconnection1.isipv6configured = k1;
                        }
                        int l1 = ((DataCallState)arraylist.get(0)).active;
                        int i2 = 1;
                        if(l1 != i2)
                            break label0;
                        int j2 = Log.d("GSM", "[DSAC DEB] onIpv6AddrStatusChanged Address assignment is successful ");
                        boolean flag1 = flag;
                        boolean flag2 = true;
                        if(flag1 != flag2)
                            break label1;
                        int k2 = gsmdataconnection.ipaddresstype;
                        byte byte0 = 2;
                        if(k2 == byte0)
                        {
                            GsmMultiDataConnectionTracker gsmmultidataconnectiontracker = this;
                            AsyncResult asyncresult1 = asyncresult;
                            GsmDataConnection gsmdataconnection2 = gsmdataconnection;
                            gsmmultidataconnectiontracker.onIpv6AddrStatusChangedIpv6Only(asyncresult1, gsmdataconnection2);
                            return;
                        }
                        break label2;
                    }
                    StringBuilder stringbuilder4 = (new StringBuilder()).append(" onIpv6AddrStatusChanged cid value is ");
                    int l2 = gsmdataconnection1.getCid();
                    StringBuilder stringbuilder5 = stringbuilder4.append(l2).append(" iptype is ");
                    int i3 = gsmdataconnection1.ipaddresstype;
                    String s10 = stringbuilder5.append(i3).toString();
                    GsmMultiDataConnectionTracker gsmmultidataconnectiontracker1 = this;
                    String s11 = s10;
                    gsmmultidataconnectiontracker1.log(s11);
                } while(true);
                int j3 = gsmdataconnection.ipaddresstype;
                byte byte1 = 3;
                if(j3 != byte1)
                {
                    return;
                } else
                {
                    GsmMultiDataConnectionTracker gsmmultidataconnectiontracker2 = this;
                    AsyncResult asyncresult2 = asyncresult;
                    GsmDataConnection gsmdataconnection3 = gsmdataconnection;
                    gsmmultidataconnectiontracker2.onIpv6AddrStatusChangedIpv6Ipv4(asyncresult2, gsmdataconnection3);
                    return;
                }
            }
            GsmMultiDataConnectionTracker gsmmultidataconnectiontracker3 = this;
            String s12 = "onIpv6AddrStatusChanged CID doesnt exists so add to pending list";
            gsmmultidataconnectiontracker3.log(s12);
            DataCallState datacallstate = (DataCallState)arraylist.get(0);
            addPendingIpv6DataCallList(datacallstate);
            return;
        }
        GsmMultiDataConnectionTracker gsmmultidataconnectiontracker4 = this;
        String s13 = "[DSAC DEB] onIpv6AddrStatusChanged Address assignment is not successful ";
        gsmmultidataconnectiontracker4.log(s13);
        boolean flag3 = flag;
        boolean flag4 = true;
        if(flag3 == flag4)
        {
            if(gsmdataconnection.getApn() == null)
            {
                GsmMultiDataConnectionTracker gsmmultidataconnectiontracker5 = this;
                String s14 = "apn is null";
                gsmmultidataconnectiontracker5.log(s14);
                return;
            }
            if(gsmdataconnection.getApn().canHandleType("ims"))
            {
                GsmMultiDataConnectionTracker gsmmultidataconnectiontracker6 = this;
                String s15 = "[DSAC DEB] onIpv6AddrStatusChanged IPv6 Address assignment failed for IMS so Detach ";
                gsmmultidataconnectiontracker6.log(s15);
                GsmMultiDataConnectionTracker gsmmultidataconnectiontracker7 = this;
                String s16 = "ims";
                if(gsmmultidataconnectiontracker7.isApnTypeActive(s16))
                {
                    GsmMultiDataConnectionTracker gsmmultidataconnectiontracker8 = this;
                    int k3 = 0;
                    int l3 = 1;
                    boolean flag5 = gsmmultidataconnectiontracker8.explicitDetach(k3, l3);
                    return;
                } else
                {
                    boolean flag6 = explicitDetach();
                    return;
                }
            }
            GsmMultiDataConnectionTracker gsmmultidataconnectiontracker9 = this;
            String s17 = "[DSAC DEB] onIpv6AddrStatusChanged IPv6 Address assignment failed for other APN ";
            gsmmultidataconnectiontracker9.log(s17);
            if(s2 != null)
            {
                String s18 = s2;
                String s19 = "";
                if(!s18.equals(s19))
                {
                    String s20 = s1;
                    String s21 = "";
                    if(s20.equals(s21))
                    {
                        GsmMultiDataConnectionTracker gsmmultidataconnectiontracker10 = this;
                        String s22 = "Need to send Notification to unset DNS entry in master";
                        gsmmultidataconnectiontracker10.log(s22);
                        GsmDataConnection gsmdataconnection4 = gsmdataconnection;
                        mActivePdp = gsmdataconnection4;
                        ApnSetting apnsetting = gsmdataconnection.getApn();
                        mActiveApn = apnsetting;
                        phone.notifyDataConnection("ipv6addressrefreshfailed");
                    }
                }
            }
            int i4 = gsmdataconnection.ipaddresstype;
            byte byte2 = 2;
            if(i4 == byte2)
            {
                String s23 = gsmdataconnection.getApn().types[0];
                String s24 = null;
                String s25 = "default";
                String s26 = s23;
                if(s25.equals(s26))
                    s24 = "nonImsAddressConfFail";
                StringBuilder stringbuilder6 = (new StringBuilder()).append("[DSAC DEB] onIpv6AddrStatusChanged IPv6 Address assignment failed and address is IPv6 alone for apnType ");
                String s27 = s23;
                String s28 = stringbuilder6.append(s27).toString();
                GsmMultiDataConnectionTracker gsmmultidataconnectiontracker11 = this;
                String s29 = s28;
                gsmmultidataconnectiontracker11.log(s29);
                if(!gsmdataconnection.isActive())
                {
                    GsmMultiDataConnectionTracker gsmmultidataconnectiontracker12 = this;
                    String s30 = s23;
                    int j4 = gsmmultidataconnectiontracker12.apnTypeToId(s30);
                    GsmMultiDataConnectionTracker gsmmultidataconnectiontracker13 = this;
                    int k4 = j4;
                    int l4 = 0;
                    String s31 = s24;
                    gsmmultidataconnectiontracker13.onEnableApn(k4, l4, s31);
                    return;
                }
                int i5 = gsmdataconnection.isipv4configured;
                int j5 = 1;
                if(i5 != j5)
                {
                    return;
                } else
                {
                    GsmMultiDataConnectionTracker gsmmultidataconnectiontracker14 = this;
                    GsmDataConnection gsmdataconnection5 = gsmdataconnection;
                    String s32 = "ipv6addressrefreshfailed";
                    gsmmultidataconnectiontracker14.handleIpv4Connect(gsmdataconnection5, s32);
                    return;
                }
            }
            int k5 = gsmdataconnection.ipaddresstype;
            byte byte3 = 2;
            if(k5 == byte3)
            {
                return;
            } else
            {
                GsmMultiDataConnectionTracker gsmmultidataconnectiontracker15 = this;
                AsyncResult asyncresult3 = asyncresult;
                GsmDataConnection gsmdataconnection6 = gsmdataconnection;
                gsmmultidataconnectiontracker15.onIpv6AddrStatusChangedIpv6Ipv4(asyncresult3, gsmdataconnection6);
                return;
            }
        } else
        {
            GsmMultiDataConnectionTracker gsmmultidataconnectiontracker16 = this;
            String s33 = "onIpv6AddrStatusChanged CID doesnt exists so add to pending list although it is unsuccessful";
            gsmmultidataconnectiontracker16.log(s33);
            DataCallState datacallstate1 = (DataCallState)arraylist.get(0);
            addPendingIpv6DataCallList(datacallstate1);
            return;
        }
    }

    protected void onIpv6AddrStatusChangedIpv6Ipv4(AsyncResult asyncresult, GsmDataConnection gsmdataconnection)
    {
        log("onIpv6AddrStatusChangedIpv6Ipv4");
        handleIpv6AddressConfigured(gsmdataconnection);
    }

    protected void onIpv6AddrStatusChangedIpv6Only(AsyncResult asyncresult, GsmDataConnection gsmdataconnection)
    {
        log("onIpv6AddrStatusChangedIpv6Only");
        handleIpv6AddressConfigured(gsmdataconnection);
    }

    protected void onNetworkDisconnectReq(AsyncResult asyncresult)
    {
        int i = Log.d("GSM", "[DSAC DEB] onNetworkDisconnectReq");
        int j = ((int[])(int[])asyncresult.result)[0];
        String s = (new StringBuilder()).append("[DSAC DEB] onNetworkDisconnectReq CID is ").append(j).toString();
        int k = Log.d("GSM", s);
        GsmDataConnection gsmdataconnection = null;
        Iterator iterator = pdpList.iterator();
        do
        {
            if(!iterator.hasNext())
                break;
            GsmDataConnection gsmdataconnection1 = (GsmDataConnection)(DataConnection)iterator.next();
            if(gsmdataconnection1.getCid() == j)
                continue;
            log("onDisconnectRequstRecv Found Matching EPS Bearer");
            gsmdataconnection = gsmdataconnection1;
            break;
        } while(true);
        if(gsmdataconnection != null && gsmdataconnection.getApn() != null && gsmdataconnection.getApn().types[0] != null)
        {
            StringBuilder stringbuilder = (new StringBuilder()).append("onDisconnectRequstRecv Found Matching EPS Bearer for the APN TYPE ");
            String s1 = gsmdataconnection.getApn().apn;
            String s2 = stringbuilder.append(s1).toString();
            log(s2);
            if(gsmdataconnection.getApn().types[0].equals("ims"))
                mImsSMSInterface.unregisterImsOnImsPdnDetach();
        }
        phone.mCM.confirmDeactivateDataCall(j, null);
        int l = Log.d("GSM", "[GsmMultiDct] No matching cid in the list");
    }

    protected void onPdpStateChanged(AsyncResult asyncresult, boolean flag)
    {
        int i = -1;
        log("onPdpStateChanged");
        ArrayList arraylist = (ArrayList)(ArrayList)asyncresult.result;
        if(asyncresult.exception != null)
            return;
        log("onPdpStateChanged Good to process the AsyncResult");
        int j = 0;
        if(!pdpStatesHasCID(arraylist, j))
        {
            log("Connection got up in lower layer, but not yet indicated to FW, usually Attach");
            GsmCellLocation gsmcelllocation = (GsmCellLocation)phone.getCellLocation();
            Object aobj[] = new Object[2];
            if(gsmcelllocation != null)
                i = gsmcelllocation.getCid();
            Integer integer = Integer.valueOf(i);
            aobj[0] = integer;
            Integer integer1 = Integer.valueOf(TelephonyManager.getDefault().getNetworkType());
            aobj[1] = integer1;
            int k = EventLog.writeEvent(50109, aobj);
            return;
        }
        if(pdpStatesHasActiveCID(arraylist, j))
            return;
        if(!flag)
        {
            CommandsInterface commandsinterface = phone.mCM;
            Message message = obtainMessage(11);
            commandsinterface.getPDPContextList(message);
            return;
        }
        log("PDP connection has dropped (active=false case).  Reconnecting");
        GsmCellLocation gsmcelllocation1 = (GsmCellLocation)phone.getCellLocation();
        Object aobj1[] = new Object[2];
        if(gsmcelllocation1 != null)
            i = gsmcelllocation1.getCid();
        Integer integer2 = Integer.valueOf(i);
        aobj1[0] = integer2;
        Integer integer3 = Integer.valueOf(TelephonyManager.getDefault().getNetworkType());
        aobj1[1] = integer3;
        int l = EventLog.writeEvent(50109, aobj1);
    }

    protected void onPollPdp()
    {
        if(!isAnyApnTypeActive())
        {
            return;
        } else
        {
            CommandsInterface commandsinterface = phone.mCM;
            Message message = obtainMessage(11);
            commandsinterface.getPDPContextList(message);
            Message message1 = obtainMessage(7);
            boolean flag = sendMessageDelayed(message1, 5000L);
            return;
        }
    }

    protected void onRadioAvailable()
    {
        if(phone.getSimulatedRadioControl() != null)
        {
            com.android.internal.telephony.DataConnectionTracker.State state = com.android.internal.telephony.DataConnectionTracker.State.CONNECTED;
            setState(state);
            phone.notifyDataConnection(null);
            int i = Log.i("GSM", "We're on the simulator; assuming data is connected");
        }
        if(isAllDataConnectionInactive())
        {
            return;
        } else
        {
            cleanUpConnection(true, null);
            return;
        }
    }

    protected void onRadioOffOrNotAvailable()
    {
        int i = 0;
        do
        {
            int j = mRetryMgr.length;
            if(i >= j)
                break;
            mRetryMgr[i].resetRetryCount();
            i++;
        } while(true);
        isThrottleDefaultReq = false;
        mReregisterOnReconnectFailure = false;
        int k;
        if(phone.getSimulatedRadioControl() != null)
        {
            k = Log.i("GSM", "We're on the simulator; assuming radio off is meaningless");
        } else
        {
            log("Radio is off and clean up all connection");
            cleanUpConnection(false, "radioTurnedOff");
        }
        allApns = null;
    }

    protected void onRecordsLoaded()
    {
        createAllApnList();
        com.android.internal.telephony.DataConnectionTracker.State state = this.state;
        com.android.internal.telephony.DataConnectionTracker.State state1 = com.android.internal.telephony.DataConnectionTracker.State.FAILED;
        if(state == state1)
            cleanUpConnection(false, null);
        trySetupAllEnabledServices();
    }

    protected void onResetDone(AsyncResult asyncresult)
    {
        log("EVENT_RESET_DONE");
        String s = null;
        if(asyncresult.userObj instanceof String)
            s = (String)asyncresult.userObj;
        gotoIdleAndNotifyDataConnection(s);
    }

    protected void onRoamingOff()
    {
        boolean flag = trySetupData("roamingOff");
    }

    protected void onRoamingOn()
    {
        if(getDataOnRoamingEnabled())
        {
            boolean flag = trySetupData("roamingOn");
            return;
        } else
        {
            log("Tear down data connection on roaming.");
            cleanUpConnection(true, "roamingOn");
            return;
        }
    }

    protected void onSetDataEnabled(boolean flag)
    {
        if(mMasterDataEnabled != flag)
            return;
        mMasterDataEnabled = flag;
        if(flag)
        {
            int i = 0;
            do
            {
                int j = mRetryMgr.length;
                if(i < j)
                {
                    mRetryMgr[i].resetRetryCount();
                    i++;
                } else
                {
                    isThrottleDefaultReq = false;
                    boolean flag1 = onTrySetupData("dataEnabled");
                    return;
                }
            } while(true);
        } else
        {
            clearAllPendingApnRequest();
            onCleanUpConnection(true, "dataDisabled");
            return;
        }
    }

    protected boolean onTrySetupData(String s)
    {
        return trySetupData(s);
    }

    protected void onVoiceCallEnded()
    {
        if(isAnyApnTypeActive())
        {
            if(!mGsmPhone.mSST.isConcurrentVoiceAndData())
            {
                startNetStatPoll();
                phone.notifyDataConnection("2GVoiceCallEnded");
            } else
            {
                resetPollStats();
            }
            trySetupNextData();
            return;
        }
        int i = 0;
        do
        {
            int j = mRetryMgr.length;
            if(i < j)
            {
                mRetryMgr[i].resetRetryCount();
                i++;
            } else
            {
                isThrottleDefaultReq = false;
                mReregisterOnReconnectFailure = false;
                boolean flag = trySetupData("2GVoiceCallEnded");
                return;
            }
        } while(true);
    }

    protected void onVoiceCallStarted()
    {
        if(!isAnyApnTypeActive())
            return;
        if(mGsmPhone.mSST.isConcurrentVoiceAndData())
        {
            return;
        } else
        {
            stopNetStatPoll();
            phone.notifyDataConnection("2GVoiceCallStarted");
            String s = mCurrentRequestedApnType;
            addPendingApnRequest(s);
            return;
        }
    }

    public void putApnListForHandover(ArrayList arraylist)
    {
        int i = Log.d("GSM", "[Handover] **C2L putApnListForHandover");
        if(arraylist == null)
        {
            int j = Log.d("GSM", "[Handover] **C2L Handover APN List is null, can't putApnListForHandover");
            return;
        }
        if(allApns == null)
        {
            int k = Log.d("GSM", "[Handover] **C2L allApn List is null, can't putApnListForHandover");
            return;
        }
        int l = Log.d("GSM", "[Handover] **C2L cleanup AllApns handover info before copying Handover Information");
        onHOCleanupHOAPN();
        Iterator iterator = arraylist.iterator();
        do
        {
            if(!iterator.hasNext())
                return;
            ApnSetting apnsetting = (ApnSetting)iterator.next();
            boolean flag = false;
            Iterator iterator1 = allApns.iterator();
            do
            {
                if(!iterator1.hasNext())
                    break;
                ApnSetting apnsetting1 = (ApnSetting)iterator1.next();
                String s = apnsetting1.apn;
                String s1 = apnsetting.apn;
                if(!s.equals(s1))
                    continue;
                String s2 = apnsetting.ipv4;
                apnsetting1.ipv4 = s2;
                String s3 = apnsetting.ipv6;
                apnsetting1.ipv6 = s3;
                String s4 = (new StringBuilder()).append("[Handover] **C2L handover info is copied for apn: ").append(apnsetting1).toString();
                int i1 = Log.d("GSM", s4);
                break;
            } while(true);
            if(!flag)
            {
                String s5 = (new StringBuilder()).append("[Handover] **C2L no APN found for: ").append(apnsetting).toString();
                int j1 = Log.d("GSM", s5);
            }
        } while(true);
    }

    protected void restartRadio()
    {
        log("************TURN OFF RADIO**************");
        cleanUpConnection(true, "radioTurnedOff");
        mGsmPhone.mSST.powerOffRadioSafely();
        String s = String.valueOf(Integer.parseInt(SystemProperties.get("net.ppp.reset-by-timeout", "0")) + 1);
        SystemProperties.set("net.ppp.reset-by-timeout", s);
    }

    public void sendPsAttachInfo()
    {
        int i = Log.d("GSM", "sendPsAttachInfo from LTE-DCT");
        String s = null;
        String s1 = null;
        String s2 = null;
        String s3 = null;
        int j = 65535;
        int k = 65535;
        if(allApns != null)
        {
            Iterator iterator = allApns.iterator();
            do
            {
                if(!iterator.hasNext())
                    break;
                ApnSetting apnsetting = (ApnSetting)iterator.next();
                String s4 = mAttachApnType;
                ApnSetting apnsetting1 = apnsetting;
                String s5 = s4;
                if(apnsetting1.canHandleType(s5))
                {
                    s = apnsetting.apn;
                    s1 = apnsetting.types[0];
                    s2 = apnsetting.user;
                    s3 = apnsetting.password;
                    j = apnsetting.authType;
                    k = apnsetting.ipType;
                }
            } while(true);
        }
        StringBuilder stringbuilder = (new StringBuilder()).append("sendPsAttachInfo apn ").append(s).append(" user ").append(s2).append(" password ").append(s3).append(" iptype ");
        int l = k;
        StringBuilder stringbuilder1 = stringbuilder.append(l).append(" for requested Type ");
        String s6 = mAttachApnType;
        String s7 = stringbuilder1.append(s6).toString();
        GsmMultiDataConnectionTracker gsmmultidataconnectiontracker = this;
        String s8 = s7;
        gsmmultidataconnectiontracker.log(s8);
        if(!mImsTestMode)
        {
            CommandsInterface commandsinterface = phone.mCM;
            String s9 = Integer.toString(1);
            String s10 = Integer.toString(0);
            String s11 = Integer.toString(j);
            String s12 = Integer.toString(k);
            String s13 = Integer.toString(1);
            String s14 = Integer.toString(0);
            GsmMultiDataConnectionTracker gsmmultidataconnectiontracker1 = this;
            byte byte0 = 54;
            Message message = gsmmultidataconnectiontracker1.obtainMessage(byte0);
            commandsinterface.setupPsAttach(s9, s10, s, s2, s3, s11, s12, s13, s14, null, null, s1, message);
            return;
        } else
        {
            CommandsInterface commandsinterface1 = phone.mCM;
            String s15 = Integer.toString(1);
            String s16 = Integer.toString(0);
            String s17 = Integer.toString(j);
            String s18 = Integer.toString(k);
            String s19 = Integer.toString(0);
            String s20 = Integer.toString(0);
            GsmMultiDataConnectionTracker gsmmultidataconnectiontracker2 = this;
            byte byte1 = 54;
            Message message1 = gsmmultidataconnectiontracker2.obtainMessage(byte1);
            commandsinterface1.setupPsAttach(s15, s16, s, s2, s3, s17, s18, s19, s20, null, null, s1, message1);
            return;
        }
    }

    public void setDataOnDunEnabled(boolean flag)
    {
    }

    protected void setState(com.android.internal.telephony.DataConnectionTracker.State state)
    {
        String s = (new StringBuilder()).append("setState: ").append(state).toString();
        log(s);
        if(this.state != state)
        {
            Object aobj[] = new Object[2];
            String s1 = this.state.toString();
            aobj[0] = s1;
            String s2 = state.toString();
            aobj[1] = s2;
            int i = EventLog.writeEvent(50113, aobj);
            this.state = state;
        }
        com.android.internal.telephony.DataConnectionTracker.State state1 = this.state;
        com.android.internal.telephony.DataConnectionTracker.State state2 = com.android.internal.telephony.DataConnectionTracker.State.FAILED;
        if(state1 != state2)
            return;
        if(waitingApns == null)
        {
            return;
        } else
        {
            waitingApns.clear();
            return;
        }
    }

    protected void setState(com.android.internal.telephony.DataConnectionTracker.State state, String s)
    {
        String s1 = (new StringBuilder()).append("setState: overloaded ").append(state).append(" apnType: ").append(s).toString();
        log(s1);
        if(s.equals("default"))
        {
            if(this.state == state)
            {
                return;
            } else
            {
                Object aobj[] = new Object[2];
                String s2 = this.state.toString();
                aobj[0] = s2;
                String s3 = state.toString();
                aobj[1] = s3;
                int i = EventLog.writeEvent(50113, aobj);
                this.state = state;
                return;
            }
        } else
        {
            log("setState ApnType is Inappropriate");
            return;
        }
    }

    public void setUpDedicatedBearer(String s)
    {
        int i = Log.d("GSM", "setUpDedicatedBearer from LTE-DCT");
        String s1 = null;
        String s2 = null;
        String s3 = null;
        String s4 = null;
        int j = -1;
        int k = -1;
        String s5 = s;
        if(s5 == null)
            s5 = mAttachApnType;
        if(allApns != null)
        {
            Iterator iterator = allApns.iterator();
            do
            {
                if(!iterator.hasNext())
                    break;
                ApnSetting apnsetting = (ApnSetting)iterator.next();
                if(apnsetting.canHandleType(s5))
                {
                    s1 = apnsetting.apn;
                    s2 = apnsetting.types[0];
                    s3 = apnsetting.user;
                    s4 = apnsetting.password;
                    j = apnsetting.authType;
                    k = apnsetting.ipType;
                }
            } while(true);
        }
        String s6 = (new StringBuilder()).append("setUpDedicatedBearer apn ").append(s1).append(" user ").append(s3).append(" password ").append(s4).append(" iptype ").append(k).append(" for requested Type ").append(s5).toString();
        log(s6);
        CommandsInterface commandsinterface = phone.mCM;
        String s7 = Integer.toString(1);
        String s8 = Integer.toString(0);
        String s9 = Integer.toString(j);
        Message message = obtainMessage(57);
        commandsinterface.setupDedicatedBearer(s7, s8, s1, s3, s4, s9, s2, message);
    }

    protected void startInactivityTimer(int i, String s, String s1, GsmDataConnection gsmdataconnection)
    {
        String s2 = (new StringBuilder()).append("InactivityTimer : startInactivityTimer : ").append(s).toString();
        log(s2);
        if(i <= 0)
        {
            log("Invalid Inactivity Threshhold: Not starting Inactivity timer");
            return;
        } else
        {
            Message message = obtainMessage();
            stopInactivityTimer(s);
            GsmMultiDataConnectionTracker gsmmultidataconnectiontracker = this;
            int j = i;
            String s3 = s;
            String s4 = s1;
            GsmDataConnection gsmdataconnection1 = gsmdataconnection;
            NetPollStatTimer netpollstattimer = new NetPollStatTimer(gsmmultidataconnectiontracker, j, s3, message, s4, gsmdataconnection1);
            Object obj = mInactivityTimerList.put(s, netpollstattimer);
            netpollstattimer.run();
            return;
        }
    }

    public void startInternetOnDemandThread()
    {
        Runnable runnable;
        Thread thread;
        try
        {
            ServerSocket serversocket = new ServerSocket();
            manualAttachServerSocket = serversocket;
            manualAttachServerSocket.setReuseAddress(true);
            ServerSocket serversocket1 = manualAttachServerSocket;
            InetSocketAddress inetsocketaddress = new InetSocketAddress("127.0.0.1", 49324);
            serversocket1.bind(inetsocketaddress);
            log(" manualAttachServerSocket init done ");
        }
        catch(IOException ioexception)
        {
            String s = (new StringBuilder()).append("IOException manualAttachServerSocket: ").append(ioexception).toString();
            log(s);
        }
        runnable = new Runnable() {

            public void run()
            {
                do
                    try
                    {
                        do
                        {
                            do
                            {
                                Socket socket = manualAttachServerSocket.accept();
                                log("Thread Server Accept !!");
                                java.io.InputStream inputstream = socket.getInputStream();
                                DataInputStream datainputstream = new DataInputStream(inputstream);
                                int i = GsmMultiDataConnectionTracker.currentAppPID = readIntFromSocket(datainputstream);
                                GsmMultiDataConnectionTracker gsmmultidataconnectiontracker = GsmMultiDataConnectionTracker.this;
                                StringBuilder stringbuilder = (new StringBuilder()).append("[Manual Attach] PID_int : ");
                                int j = GsmMultiDataConnectionTracker.currentAppPID;
                                String s1 = stringbuilder.append(j).toString();
                                gsmmultidataconnectiontracker.log(s1);
                            } while(!isOnDemandEnable);
                            GsmMultiDataConnectionTracker gsmmultidataconnectiontracker1 = GsmMultiDataConnectionTracker.this;
                            Message message = obtainMessage(53, null);
                            boolean flag = gsmmultidataconnectiontracker1.sendMessage(message);
                        } while(true);
                    }
                    catch(IOException ioexception1)
                    {
                        GsmMultiDataConnectionTracker gsmmultidataconnectiontracker2 = GsmMultiDataConnectionTracker.this;
                        String s2 = (new StringBuilder()).append("[Manual Attach] IOException: ").append(ioexception1).toString();
                        gsmmultidataconnectiontracker2.log(s2);
                    }
                while(true);
            }

            final GsmMultiDataConnectionTracker this$0;

            
            {
                this$0 = GsmMultiDataConnectionTracker.this;
                Object();
            }
        };
        thread = new Thread(runnable, "GSMPhone debug");
        manualAttachThread = thread;
        manualAttachThread.start();
    }

    protected void startNetStatPoll()
    {
        if(isAnyApnTypeActive() && !netStatPollEnabled)
        {
            log("GSM MultiDCT Start poll NetStat");
            resetPollStats();
            netStatPollEnabled = true;
            mPollNetStat.run();
        }
        com.android.internal.telephony.DataConnectionTracker.Activity activity = com.android.internal.telephony.DataConnectionTracker.Activity.NONE;
        this.activity = activity;
        phone.notifyDataActivity();
    }

    protected void stopInactivityTimer(String s)
    {
        String s1 = (new StringBuilder()).append("InactivityTimer : stopInactivityTimer : ").append(s).toString();
        log(s1);
        NetPollStatTimer netpollstattimer = (NetPollStatTimer)mInactivityTimerList.remove(s);
        if(netpollstattimer != null && netpollstattimer.am != null)
        {
            PendingIntent pendingintent = netpollstattimer.mInactivityIntent;
            netpollstattimer.am.cancel(pendingintent);
        } else
        if(netpollstattimer == null)
            log("InactivityTimer : pTimer is null");
        else
            log("InactivityTimer : pTimer.am is null");
        removeCallbacks(netpollstattimer);
    }

    protected void stopNetStatPoll()
    {
        netStatPollEnabled = false;
        Runnable runnable = mPollNetStat;
        removeCallbacks(runnable);
        log("GSMMultiDCT Stop poll NetStat");
    }

    protected static final int APN_DELAY_MILLIS = 5000;
    static final String APN_ID = "apn_id";
    private static final int CLEANUP_AFTER_GPRS_DETACHED_TIMER = 0x1d4c0;
    private static final int EVENT_ATTACH_SUCCESS = 31;
    static final int EVENT_FORCE_DORMANCY_DONE = 1000;
    private static final String FD_PREFERENCES_NAME = "fdormancy.preferences_name";
    private static final int IMS_DREG_TIMER = 30000;
    private static final String INTENT_INACTIVITY_ALARM_ADMIN = "com.android.internal.telephony.gsm-inactivity.admin";
    private static final String INTENT_INACTIVITY_ALARM_APN_TYPE = "com.android.internal.telephony.gsm.inactype";
    private static final String INTENT_INACTIVITY_ALARM_DEFAULT = "com.android.internal.telephony.gsm-inactivity.default";
    private static final String INTENT_INACTIVITY_ALARM_IMS = "com.android.internal.telephony.gsm-inactivity.ims";
    private static final String INTENT_INACTIVITY_ALARM_VZWAPP = "com.android.internal.telephony.gsm-inactivity.vzwapp";
    private static final String INTENT_RECONNECT_ALARM = "com.android.internal.telephony.gsm-reconnect";
    private static final String INTENT_RECONNECT_ALARM_APN_TYPE = "com.android.internal.telephony.gsm.reqtype";
    private static final String INTENT_RECONNECT_ALARM_EXTRA_REASON = "com.android.internal.telephony.gsm.reason";
    private static final int IP_TYPE_IPV4 = 0;
    private static final int IP_TYPE_IPV4V6 = 2;
    private static final int IP_TYPE_IPV6 = 1;
    private static final int IP_TYPE_NONE = 3;
    private static final String KEY_FD_MCCMNC = "fdormancy.key.mccmnc";
    private static final String KEY_FD_STATE = "fdormancy.key.state";
    protected static final String LTE_IMS_DATA_RETRY_CONFIG_CAUSECODE26 = "max_retries=1,50";
    protected static final String LTE_SPECIFIC_DATA_RETRY_CONFIG = "max_retries=infinite,50,60000,120000,240000,480000,900000";
    protected static final int NEXT_PDN_CONNECTION_DEALY = 3000;
    protected static final int NEXT_PDN_RECONNECTION_DEALY = 500;
    private static final int PDP_CONNECTION_POOL_SIZE = 4;
    private static final int POLL_NETSTAT_SCREEN_OFF_MILLIS_DORMANCY = 5000;
    private static final int POLL_PDP_MILLIS = 5000;
    static final Uri PREFERAPN_URI = Uri.parse("content://telephony/carriers/preferapn");
    protected static final String SECONDARY_DATA_RETRY_CONFIG = "max_retries=3,5000,5000,5000";
    private static boolean capaDormancy = false;
    private static int currentAppPID = 0;
    private static boolean dormancyState = false;
    private static boolean isThrottleDefaultReq = false;
    private static String mDormFlag = "on";
    static final int manualAttachThreadPort = 49324;
    private static android.os.PowerManager.WakeLock sWakeLockConnect = null;
    protected final String LOG_TAG;
    private ArrayList allApns;
    private ApnChangeObserver apnObserver;
    private boolean autoAttach;
    private ArrayList backup_allApns;
    private boolean canSetPreferApn;
    private int cur_gprsState;
    boolean failNextConnect;
    private long inactivityPeriod;
    private boolean isOnDemandEnable;
    protected ApnSetting mActiveApn;
    private GsmDataConnection mActivePdp;
    private String mAttachApnType;
    private int mCleanupCount;
    private CommandsInterface mCommandsInterfaceGSM;
    private String mCurrentRequestedApnType;
    private DataRoamingContentObserver mDataRoaming;
    private RetryManager mDefaultRetryManager;
    private GSMPhone mGsmPhone;
    private HandoverTracker mHandoverTracker;
    private ImsSMSInterface mImsSMSInterface;
    boolean mImsTestMode;
    HashMap mInactivityTimerList;
    BroadcastReceiver mIntentReceiver;
    private boolean mIsAdminEnabled;
    protected boolean mIsImsConfigured;
    private boolean mIsImsEnabled;
    private boolean mIsPsRestricted;
    private boolean mIsScreenOn;
    private boolean mIsSimSupportMultiPdp;
    private String mOperatorNumeric;
    private int mPdpResetCount;
    private Map mPendingIpv6DataCallList;
    private ArrayList mPendingRequestedApns;
    private Runnable mPollNetStat;
    private boolean mReregisterOnReconnectFailure;
    private ContentResolver mResolver;
    protected RetryManager mRetryMgr[];
    private RetryManager mSecondaryRetryManager;
    ServerSocket manualAttachServerSocket;
    Thread manualAttachThread;
    HashMap oldList;
    private long oldPollTime;
    private ArrayList pdpList;
    private PowerManager powerManager;
    private ApnSetting preferredApn;
    Queue qOnDemandPdnRequestQueue;
    private ArrayList waitingApns;

    static 
    {
        currentAppPID = 0;
        dormancyState = false;
        capaDormancy = false;
        isThrottleDefaultReq = false;
    }




/*
    static boolean access$1002(GsmMultiDataConnectionTracker gsmmultidataconnectiontracker, boolean flag)
    {
        gsmmultidataconnectiontracker.mIsWifiConnected = flag;
        return flag;
    }

*/



/*
    static int access$1102(int i)
    {
        currentAppPID = i;
        return i;
    }

*/







/*
    static int access$1702(GsmMultiDataConnectionTracker gsmmultidataconnectiontracker, int i)
    {
        gsmmultidataconnectiontracker.netStatPollPeriod = i;
        return i;
    }

*/


/*
    static int access$1802(GsmMultiDataConnectionTracker gsmmultidataconnectiontracker, int i)
    {
        gsmmultidataconnectiontracker.mPdpResetCount = i;
        return i;
    }

*/





/*
    static boolean access$202(GsmMultiDataConnectionTracker gsmmultidataconnectiontracker, boolean flag)
    {
        gsmmultidataconnectiontracker.mIsScreenOn = flag;
        return flag;
    }

*/


/*
    static int access$2102(GsmMultiDataConnectionTracker gsmmultidataconnectiontracker, int i)
    {
        gsmmultidataconnectiontracker.netStatPollPeriod = i;
        return i;
    }

*/







/*
    static long access$2702(GsmMultiDataConnectionTracker gsmmultidataconnectiontracker, long l)
    {
        gsmmultidataconnectiontracker.txPkts = l;
        return l;
    }

*/


/*
    static long access$2802(GsmMultiDataConnectionTracker gsmmultidataconnectiontracker, long l)
    {
        gsmmultidataconnectiontracker.rxPkts = l;
        return l;
    }

*/







/*
    static long access$3302(GsmMultiDataConnectionTracker gsmmultidataconnectiontracker, long l)
    {
        gsmmultidataconnectiontracker.sentSinceLastRecv = l;
        return l;
    }

*/




/*
    static long access$3614(GsmMultiDataConnectionTracker gsmmultidataconnectiontracker, long l)
    {
        long l1 = gsmmultidataconnectiontracker.sentSinceLastRecv + l;
        gsmmultidataconnectiontracker.sentSinceLastRecv = l1;
        return l1;
    }

*/


/*
    static long access$3702(GsmMultiDataConnectionTracker gsmmultidataconnectiontracker, long l)
    {
        gsmmultidataconnectiontracker.sentSinceLastRecv = l;
        return l;
    }

*/


/*
    static long access$3802(GsmMultiDataConnectionTracker gsmmultidataconnectiontracker, long l)
    {
        gsmmultidataconnectiontracker.sentSinceLastRecv = l;
        return l;
    }

*/


/*
    static long access$3902(GsmMultiDataConnectionTracker gsmmultidataconnectiontracker, long l)
    {
        gsmmultidataconnectiontracker.sentSinceLastRecv = l;
        return l;
    }

*/



/*
    static boolean access$402(boolean flag)
    {
        isThrottleDefaultReq = flag;
        return flag;
    }

*/



/*
    static int access$4208(GsmMultiDataConnectionTracker gsmmultidataconnectiontracker)
    {
        int i = gsmmultidataconnectiontracker.mNoRouteCount;
        int j = i + 1;
        gsmmultidataconnectiontracker.mNoRouteCount = j;
        return i;
    }

*/


/*
    static int access$4302(GsmMultiDataConnectionTracker gsmmultidataconnectiontracker, int i)
    {
        gsmmultidataconnectiontracker.mNoRouteCount = i;
        return i;
    }

*/


/*
    static int access$4402(GsmMultiDataConnectionTracker gsmmultidataconnectiontracker, int i)
    {
        gsmmultidataconnectiontracker.mNoRouteCount = i;
        return i;
    }

*/



/*
    static com.android.internal.telephony.DataConnectionTracker.Activity access$4602(GsmMultiDataConnectionTracker gsmmultidataconnectiontracker, com.android.internal.telephony.DataConnectionTracker.Activity activity)
    {
        gsmmultidataconnectiontracker.activity = activity;
        return activity;
    }

*/





/*
    static int access$5002(GsmMultiDataConnectionTracker gsmmultidataconnectiontracker, int i)
    {
        gsmmultidataconnectiontracker.netStatPollPeriod = i;
        return i;
    }

*/


/*
    static String access$502(GsmMultiDataConnectionTracker gsmmultidataconnectiontracker, String s)
    {
        gsmmultidataconnectiontracker.mRequestedApnType = s;
        return s;
    }

*/


/*
    static int access$5102(GsmMultiDataConnectionTracker gsmmultidataconnectiontracker, int i)
    {
        gsmmultidataconnectiontracker.netStatPollPeriod = i;
        return i;
    }

*/








/*
    static boolean access$902(GsmMultiDataConnectionTracker gsmmultidataconnectiontracker, boolean flag)
    {
        gsmmultidataconnectiontracker.mIsWifiConnected = flag;
        return flag;
    }

*/
}
