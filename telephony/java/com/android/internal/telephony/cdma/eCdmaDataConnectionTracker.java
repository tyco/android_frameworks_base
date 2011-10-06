// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   eCdmaDataConnectionTracker.java

package com.android.internal.telephony.cdma;

import Lcom.android.internal.telephony.cdma.eCdmaDataConnectionTracker;;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.*;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.*;
import android.os.*;
import android.preference.PreferenceManager;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.util.EventLog;
import android.util.Log;
import com.android.internal.telephony.*;
import com.android.internal.telephony.gsm.*;
import java.io.IOException;
import java.util.*;

// Referenced classes of package com.android.internal.telephony.cdma:
//            CdmaDataConnectionTracker, CDMAPhone, CdmaServiceStateTracker, RuimRecords, 
//            CdmaDataConnection

public final class eCdmaDataConnectionTracker extends CdmaDataConnectionTracker
{
    /* member class not found */
    class ApnChangeObserver {}


    eCdmaDataConnectionTracker(CDMAPhone cdmaphone)
    {
        CdmaDataConnectionTracker(cdmaphone);
        allApns = null;
        backup_allApns = null;
        mCurrentRequestedApnType = "ims";
        mIsApnActive = false;
        mIsSimSupportMultiPdp = true;
        legacyToEhrpd = false;
        mPdpResetCount = 0;
        mCleanupCount = 0;
        waitingApns = null;
        preferredApn = null;
        canSetPreferApn = false;
        bExplicitStateSet = false;
        mIsEhrpdSyncDone = false;
        eventAssociatedApnName = null;
        ApnSetting aapnsetting[] = new ApnSetting[4];
        mActiveApns = aapnsetting;
        GsmDataConnection agsmdataconnection[] = new GsmDataConnection[4];
        mActiveDefEpsBearers = agsmdataconnection;
        int ai[] = new int[4];
        mCidActive = ai;
        mPendingIpv6DataCallList = null;
        mPendingRequestedApns = null;
        mReregisterOnReconnectFailure = false;
        HashMap hashmap = new HashMap();
        mReconnectIntentEhrpd = hashmap;
        mAttachApnType = "ims";
        mIsImsEnabled = false;
        mIsAdminEnabled = false;
        HashMap hashmap1 = new HashMap();
        oldList = hashmap1;
        cur_cdmaState = 1;
        mIsSimRecordsLoaded = false;
        isOnDemandEnable = false;
        inactivityPeriod = 0L;
        oldPollTime = 0L;
        HashMap hashmap2 = new HashMap();
        mInactivityTimerList = hashmap2;
        RetryManager aretrymanager[] = new RetryManager[6];
        mRetryEhrpd = aretrymanager;
        BroadcastReceiver broadcastreceiver = new BroadcastReceiver() ;
        mEhrpdIntentReceiver = broadcastreceiver;
        Runnable runnable = new Runnable() ;
        mEhrpdPollNetStat = runnable;
        mCdmaPhone = cdmaphone;
        HandoverTracker handovertracker = cdmaphone.mHOT;
        mHandoverTracker = handovertracker;
        cdmaphone.mCM.registerForIpv6AddrStatusChanged(this, 42, null);
        cdmaphone.mSST.registerForEHRPDSyncStatusCompleted(this, 50, null);
        cdmaphone.mSST.registerForEHRPDSyncStatusBroken(this, 51, null);
        cdmaphone.mSST.registerForInitiateHandover(this, 55, null);
        cdmaphone.mSST.registerForeHRPDHandoverNotification(this, 56, null);
        mHandoverTracker.registerForInitiateHandoverFromEhrpd(this, 55, null);
        mHandoverTracker.registerForStartHandoverFromLte(this, 56, null);
        mHandoverTracker.registerForHandoverResumeTimeoutFromEhrpd(this, 61, null);
        createAllDefEpsBearerList();
        createPendingRequestedApnList();
        createPendingIpv6DataCallList();
        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(phone.getContext());
        ContentResolver contentresolver = phone.getContext().getContentResolver();
        mResolver = contentresolver;
        ApnChangeObserver apnchangeobserver = new ApnChangeObserver();
        apnObserver = apnchangeobserver;
        ContentResolver contentresolver1 = cdmaphone.getContext().getContentResolver();
        Uri uri = android.provider.Telephony.Carriers.CONTENT_URI;
        ApnChangeObserver apnchangeobserver1 = apnObserver;
        contentresolver1.registerContentObserver(uri, true, apnchangeobserver1);
        boolean flag = sharedpreferences.getBoolean("enable_ims_test_mode", false);
        mImsTestMode = flag;
        StringBuilder stringbuilder = (new StringBuilder()).append("eCdmaDataConnectionTracker constrcutor mImsTestMode ");
        boolean flag1 = mImsTestMode;
        String s = stringbuilder.append(flag1).toString();
        log(s);
        boolean flag2 = sharedpreferences.getBoolean("ondemand_mode_on_key", false);
        isOnDemandEnable = flag2;
        StringBuilder stringbuilder1 = (new StringBuilder()).append("eCdmaDataConnectionTracker constrcutor isOnDemandEnable ");
        boolean flag3 = isOnDemandEnable;
        String s1 = stringbuilder1.append(flag3).toString();
        log(s1);
        String s2;
        StringBuilder stringbuilder2;
        boolean flag4;
        String s3;
        int i;
        StringBuilder stringbuilder3;
        String s4;
        String s5;
        int j;
        String s6;
        RetryManager aretrymanager1[];
        RetryManager retrymanager;
        RetryManager aretrymanager2[];
        RetryManager retrymanager1;
        RetryManager aretrymanager3[];
        RetryManager retrymanager2;
        RetryManager aretrymanager4[];
        RetryManager retrymanager3;
        RetryManager aretrymanager5[];
        RetryManager retrymanager4;
        RetryManager aretrymanager6[];
        RetryManager retrymanager5;
        IntentFilter intentfilter;
        Context context;
        BroadcastReceiver broadcastreceiver1;
        Intent intent;
        if(mImsTestMode)
        {
            mAttachApnType = "default";
        } else
        {
            mAttachApnType = "ims";
            dataEnabled[5] = true;
            int i2 = enabledCount + 1;
            enabledCount = i2;
        }
        s2 = mAttachApnType;
        mRequestedApnType = s2;
        stringbuilder2 = (new StringBuilder()).append("[eCdmaDataConnectionTracker] valueof Ims @ boot is ");
        flag4 = dataEnabled[5];
        s3 = stringbuilder2.append(flag4).toString();
        i = Log.d("eCDMA", s3);
        stringbuilder3 = (new StringBuilder()).append("[eCdmaDataConnectionTracker] mAttachApnType = ");
        s4 = mAttachApnType;
        s5 = stringbuilder3.append(s4).toString();
        j = Log.d("eCDMA", s5);
        s6 = mRequestedApnType;
        mCurrentRequestedApnType = s6;
        aretrymanager1 = mRetryEhrpd;
        retrymanager = new RetryManager("ims");
        aretrymanager1[0] = retrymanager;
        if(!mRetryEhrpd[0].configure("max_retries=infinite,10000,10000,60000,120000,240000,480000,900000"))
        {
            int k = Log.e("eCDMA", "Could not configure using EHRPD_SPECIFIC_DATA_RETRY_CONFIG=max_retries=infinite,10000,10000,60000,120000,240000,480000,900000");
            boolean flag5 = mRetryEhrpd[0].configure(20, 2000, 1000);
        }
        aretrymanager2 = mRetryEhrpd;
        retrymanager1 = new RetryManager("default");
        aretrymanager2[1] = retrymanager1;
        if(!mRetryEhrpd[1].configure("max_retries=infinite,10000,10000,60000,120000,240000,480000,900000"))
        {
            int l = Log.e("eCDMA", "Could not configure using EHRPD_SPECIFIC_DATA_RETRY_CONFIG=max_retries=infinite,10000,10000,60000,120000,240000,480000,900000");
            boolean flag6 = mRetryEhrpd[1].configure(20, 2000, 1000);
        }
        aretrymanager3 = mRetryEhrpd;
        retrymanager2 = new RetryManager("mms");
        aretrymanager3[2] = retrymanager2;
        if(!mRetryEhrpd[2].configure("max_retries=3, 5000, 5000, 5000"))
        {
            int i1 = Log.e("eCDMA", "Could not configure using SECONDARY_DATA_RETRY_CONFIG=max_retries=3, 5000, 5000, 5000");
            boolean flag7 = mRetryEhrpd[2].configure(3, 5000, 5000);
        }
        aretrymanager4 = mRetryEhrpd;
        retrymanager3 = new RetryManager("hipri");
        aretrymanager4[3] = retrymanager3;
        if(!mRetryEhrpd[3].configure("max_retries=3, 5000, 5000, 5000"))
        {
            int j1 = Log.e("eCDMA", "Could not configure using SECONDARY_DATA_RETRY_CONFIG=max_retries=3, 5000, 5000, 5000");
            boolean flag8 = mRetryEhrpd[3].configure(3, 5000, 5000);
        }
        aretrymanager5 = mRetryEhrpd;
        retrymanager4 = new RetryManager("admin");
        aretrymanager5[4] = retrymanager4;
        if(!mRetryEhrpd[4].configure("max_retries=3, 5000, 5000, 5000"))
        {
            int k1 = Log.e("eCDMA", "Could not configure using SECONDARY_DATA_RETRY_CONFIG=max_retries=3, 5000, 5000, 5000");
            boolean flag9 = mRetryEhrpd[4].configure(3, 5000, 5000);
        }
        aretrymanager6 = mRetryEhrpd;
        retrymanager5 = new RetryManager("vzwapp");
        aretrymanager6[5] = retrymanager5;
        if(!mRetryEhrpd[5].configure("max_retries=3, 5000, 5000, 5000"))
        {
            int l1 = Log.e("eCDMA", "Could not configure using SECONDARY_DATA_RETRY_CONFIG=max_retries=3, 5000, 5000, 5000");
            boolean flag10 = mRetryEhrpd[5].configure(3, 5000, 5000);
        }
        mCdmaPhone.eHRPDCapable = false;
        intentfilter = new IntentFilter();
        intentfilter.addAction("com.android.internal.telephony.ehrpd-reconnect.ims");
        intentfilter.addAction("com.android.internal.telephony.ehrpd-reconnect.default");
        intentfilter.addAction("android.intent.action.SCREEN_ON");
        intentfilter.addAction("android.intent.action.SCREEN_OFF");
        context = cdmaphone.getContext();
        broadcastreceiver1 = mEhrpdIntentReceiver;
        intent = context.registerReceiver(broadcastreceiver1, intentfilter, null, cdmaphone);
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
        if(mCurrentRequestedApnType.equals("default") && canSetPreferApn && preferredApn != null)
        {
            StringBuilder stringbuilder = (new StringBuilder()).append("[eCDMADataConnectionTracker] Preferred APN:").append("dummyoperator").append(":");
            String s = preferredApn.numeric;
            StringBuilder stringbuilder1 = stringbuilder.append(s).append(":");
            ApnSetting apnsetting = preferredApn;
            String s1 = stringbuilder1.append(apnsetting).toString();
            int i = Log.i("eCDMA", s1);
            int j = Log.i("eCDMA", "[eCDMADataConnectionTracker] Waiting APN set to preferred APN");
            ApnSetting apnsetting1 = preferredApn;
            boolean flag = arraylist.add(apnsetting1);
        } else
        if(allApns != null)
        {
            Iterator iterator = allApns.iterator();
            while(iterator.hasNext()) 
            {
                ApnSetting apnsetting2 = (ApnSetting)iterator.next();
                String s2 = mCurrentRequestedApnType;
                if(apnsetting2.canHandleType(s2) && apnsetting2.isDisable == 0)
                {
                    String s3 = (new StringBuilder()).append("[eCDMADataConnectionTracker] apn added to List: ").append(apnsetting2).toString();
                    int k = Log.d("eCDMA", s3);
                    boolean flag1 = arraylist.add(apnsetting2);
                }
            }
        }
        return arraylist;
    }

    private void clearAllPendingApnRequest()
    {
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
        boolean flag = true;
        if(allApns != null)
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
        Cursor cursor = contentresolver.query(uri, null, "apn_bearer = 'EHRPD'", as, s);
        if(cursor != null)
        {
            if(cursor.getCount() > 0)
            {
                mIsImsEnabled = false;
                mIsAdminEnabled = false;
                ArrayList arraylist3 = createApnList(cursor);
                allApns = arraylist3;
            }
            cursor.close();
        }
        if((!mIsImsEnabled || !mIsAdminEnabled) && getActiveApnCount() > 0)
        {
            boolean flag1 = explicitDetach(0, 0);
            return;
        }
        if(flag)
            return;
        Iterator iterator = backup_allApns.iterator();
label0:
        do
        {
            ApnSetting apnsetting;
            String s1;
            do
            {
                if(!iterator.hasNext())
                    return;
                apnsetting = (ApnSetting)iterator.next();
                s1 = apnsetting.types[0];
            } while(!isApnTypeActive(s1));
            Iterator iterator1 = allApns.iterator();
            ApnSetting apnsetting1;
            String s2;
            String s3;
            do
            {
                if(!iterator1.hasNext())
                    continue label0;
                apnsetting1 = (ApnSetting)iterator1.next();
                s2 = apnsetting.types[0];
                s3 = apnsetting1.types[0];
            } while(!s2.equals(s3) || !apnsetting.changeExceptStatus(apnsetting1));
            StringBuilder stringbuilder = (new StringBuilder()).append("Something has changed for the initially active APN ");
            String s4 = apnsetting.types[0];
            String s5 = stringbuilder.append(s4).append(" so disconnecting.").toString();
            log(s5);
            String s6 = apnsetting.types[0];
            disconnectByApntype(s6);
        } while(true);
    }

    private void createAllDefEpsBearerList()
    {
        ArrayList arraylist = new ArrayList();
        pdpList = arraylist;
        int i = 0;
        do
        {
            if(i >= 4)
                return;
            GsmDataConnection gsmdataconnection = GsmDataConnection.makeDataConnection(mCdmaPhone);
            boolean flag = pdpList.add(gsmdataconnection);
            i++;
        } while(true);
    }

    private ArrayList createApnList(Cursor cursor)
    {
        ArrayList arraylist = new ArrayList();
        if(!cursor.moveToFirst()) goto _L2; else goto _L1
_L1:
        String as[];
        ApnSetting apnsetting;
        int k10;
        int l10;
        Cursor cursor1 = cursor;
        String s = "type";
        int i = cursor1.getColumnIndexOrThrow(s);
        Cursor cursor2 = cursor;
        int j = i;
        String s1 = cursor2.getString(j);
        eCdmaDataConnectionTracker ecdmadataconnectiontracker = this;
        String s2 = s1;
        as = ecdmadataconnectiontracker.parseTypes(s2);
        Cursor cursor3 = cursor;
        String s3 = "_id";
        int k = cursor3.getColumnIndexOrThrow(s3);
        Cursor cursor4 = cursor;
        int l = k;
        int i1 = cursor4.getInt(l);
        Cursor cursor5 = cursor;
        String s4 = "numeric";
        int j1 = cursor5.getColumnIndexOrThrow(s4);
        Cursor cursor6 = cursor;
        int k1 = j1;
        String s5 = cursor6.getString(k1);
        Cursor cursor7 = cursor;
        String s6 = "name";
        int l1 = cursor7.getColumnIndexOrThrow(s6);
        Cursor cursor8 = cursor;
        int i2 = l1;
        String s7 = cursor8.getString(i2);
        Cursor cursor9 = cursor;
        String s8 = "apn";
        int j2 = cursor9.getColumnIndexOrThrow(s8);
        Cursor cursor10 = cursor;
        int k2 = j2;
        String s9 = cursor10.getString(k2);
        Cursor cursor11 = cursor;
        String s10 = "proxy";
        int l2 = cursor11.getColumnIndexOrThrow(s10);
        Cursor cursor12 = cursor;
        int i3 = l2;
        String s11 = cursor12.getString(i3);
        Cursor cursor13 = cursor;
        String s12 = "port";
        int j3 = cursor13.getColumnIndexOrThrow(s12);
        Cursor cursor14 = cursor;
        int k3 = j3;
        String s13 = cursor14.getString(k3);
        Cursor cursor15 = cursor;
        String s14 = "mmsc";
        int l3 = cursor15.getColumnIndexOrThrow(s14);
        Cursor cursor16 = cursor;
        int i4 = l3;
        String s15 = cursor16.getString(i4);
        Cursor cursor17 = cursor;
        String s16 = "mmsproxy";
        int j4 = cursor17.getColumnIndexOrThrow(s16);
        Cursor cursor18 = cursor;
        int k4 = j4;
        String s17 = cursor18.getString(k4);
        Cursor cursor19 = cursor;
        String s18 = "mmsport";
        int l4 = cursor19.getColumnIndexOrThrow(s18);
        Cursor cursor20 = cursor;
        int i5 = l4;
        String s19 = cursor20.getString(i5);
        Cursor cursor21 = cursor;
        String s20 = "user";
        int j5 = cursor21.getColumnIndexOrThrow(s20);
        Cursor cursor22 = cursor;
        int k5 = j5;
        String s21 = cursor22.getString(k5);
        Cursor cursor23 = cursor;
        String s22 = "password";
        int l5 = cursor23.getColumnIndexOrThrow(s22);
        Cursor cursor24 = cursor;
        int i6 = l5;
        String s23 = cursor24.getString(i6);
        Cursor cursor25 = cursor;
        String s24 = "authtype";
        int j6 = cursor25.getColumnIndexOrThrow(s24);
        Cursor cursor26 = cursor;
        int k6 = j6;
        int l6 = cursor26.getInt(k6);
        Cursor cursor27 = cursor;
        String s25 = "ipversion";
        int i7 = cursor27.getColumnIndexOrThrow(s25);
        Cursor cursor28 = cursor;
        int j7 = i7;
        String s26 = cursor28.getString(j7);
        Cursor cursor29 = cursor;
        String s27 = "inactivity_timer";
        int k7 = cursor29.getColumnIndexOrThrow(s27);
        Cursor cursor30 = cursor;
        int l7 = k7;
        int i8 = cursor30.getInt(l7);
        Cursor cursor31 = cursor;
        String s28 = "statustype";
        int j8 = cursor31.getColumnIndexOrThrow(s28);
        Cursor cursor32 = cursor;
        int k8 = j8;
        int l8 = cursor32.getInt(k8);
        apnsetting = new ApnSetting(i1, s5, s7, s9, s11, s13, s15, s17, s19, s21, s23, l6, s26, i8, l8, as);
        Cursor cursor33 = cursor;
        String s29 = "statustype";
        int i9 = cursor33.getColumnIndexOrThrow(s29);
        Cursor cursor34 = cursor;
        int j9 = i9;
        int k9 = cursor34.getInt(j9);
        Cursor cursor35 = cursor;
        String s30 = "inactivity_timer";
        int l9 = cursor35.getColumnIndexOrThrow(s30);
        Cursor cursor36 = cursor;
        int i10 = l9;
        int j10 = cursor36.getInt(i10);
        k10 = k9;
        l10 = 1;
        if(k10 != l10) goto _L4; else goto _L3
_L3:
        String as1[] = as;
        int i11 = as1.length;
        int i12 = 0;
        do
        {
            int i13 = i12;
            int j13 = i11;
            if(i13 >= j13)
                break;
            String s31 = as1[i12];
            String s32 = s31;
            String s33 = "ims";
            if(s32.equals(s33))
            {
                boolean flag = false;
                mIsImsEnabled = flag;
            }
            String s34 = s31;
            String s35 = "admin";
            if(s34.equals(s35))
            {
                boolean flag1 = false;
                mIsAdminEnabled = flag1;
            }
            StringBuilder stringbuilder = (new StringBuilder()).append("mIsImsEnabled = ");
            boolean flag2 = mIsImsEnabled;
            StringBuilder stringbuilder1 = stringbuilder.append(flag2).append(" mIsAdminEnabled = ");
            boolean flag3 = mIsAdminEnabled;
            String s36 = stringbuilder1.append(flag3).toString();
            int k13 = Log.d("eCDMA", s36);
            eCdmaDataConnectionTracker ecdmadataconnectiontracker1 = this;
            String s37 = s31;
            if(ecdmadataconnectiontracker1.isApnTypeActive(s37) && mIsImsEnabled && mIsAdminEnabled)
            {
                String s38 = "eCDMA";
                StringBuilder stringbuilder2 = (new StringBuilder()).append("eCDMADCT createApnList : isDisabled ");
                boolean flag4;
                StringBuilder stringbuilder3;
                String s39;
                StringBuilder stringbuilder4;
                String s40;
                String s41;
                int l13;
                eCdmaDataConnectionTracker ecdmadataconnectiontracker2;
                String s42;
                if(apnsetting.isDisable == 1)
                    flag4 = true;
                else
                    flag4 = false;
                stringbuilder3 = stringbuilder2.append(flag4).append(" disabling APN ");
                s39 = apnsetting.apn;
                stringbuilder4 = stringbuilder3.append(s39).append(" apnType ");
                s40 = s31;
                s41 = stringbuilder4.append(s40).toString();
                l13 = Log.e(s38, s41);
                ecdmadataconnectiontracker2 = this;
                s42 = s31;
                ecdmadataconnectiontracker2.disconnectByApntype(s42);
            }
            i12++;
        } while(true);
        String s43 = "eCDMA";
        StringBuilder stringbuilder5 = (new StringBuilder()).append("eCDMADCT createApnList : isDisabled ");
        boolean flag5;
        StringBuilder stringbuilder6;
        String s44;
        StringBuilder stringbuilder7;
        int i14;
        String s45;
        int j14;
        if(apnsetting.isDisable == 1)
            flag5 = true;
        else
            flag5 = false;
        stringbuilder6 = stringbuilder5.append(flag5).append(" excluding APN ");
        s44 = apnsetting.apn;
        stringbuilder7 = stringbuilder6.append(s44).append(" inactivityTimer ");
        i14 = apnsetting.inactivityValue;
        s45 = stringbuilder7.append(i14).toString();
        j14 = Log.e(s43, s45);
_L6:
        if(cursor.moveToNext())
            continue; /* Loop/switch isn't completed */
_L2:
        return arraylist;
_L4:
label0:
        {
            String s46 = "eCDMA";
            StringBuilder stringbuilder8 = (new StringBuilder()).append("eCDMADCT createApnList : isDisabled : ");
            String as2[];
            int j11;
            int j12;
            boolean flag6;
            StringBuilder stringbuilder9;
            String s47;
            StringBuilder stringbuilder10;
            int k14;
            String s48;
            int l14;
            if(apnsetting.isDisable == 1)
                flag6 = true;
            else
                flag6 = false;
            stringbuilder9 = stringbuilder8.append(flag6).append(" adding APN ");
            s47 = apnsetting.apn;
            stringbuilder10 = stringbuilder9.append(s47).append(" inactivityTimer ");
            k14 = apnsetting.inactivityValue;
            s48 = stringbuilder10.append(k14).toString();
            l14 = Log.e(s46, s48);
            as2 = as;
            j11 = as2.length;
            j12 = 0;
            do
            {
                int i15 = j12;
                int j15 = j11;
                if(i15 >= j15)
                    break;
                String s49 = as2[j12];
                String s50 = s49;
                String s51 = "ims";
                if(s50.equals(s51))
                {
                    boolean flag7 = true;
                    mIsImsEnabled = flag7;
                }
                String s52 = s49;
                String s53 = "admin";
                if(s52.equals(s53))
                {
                    boolean flag8 = true;
                    mIsAdminEnabled = flag8;
                }
                StringBuilder stringbuilder11 = (new StringBuilder()).append("mIsImsEnabled = ");
                boolean flag9 = mIsImsEnabled;
                StringBuilder stringbuilder12 = stringbuilder11.append(flag9).append(" mIsAdminEnabled = ");
                boolean flag10 = mIsAdminEnabled;
                String s54 = stringbuilder12.append(flag10).toString();
                int k15 = Log.d("eCDMA", s54);
                int l15 = j12 + 1;
            } while(true);
            String s55 = "eCDMA";
            StringBuilder stringbuilder13 = (new StringBuilder()).append("isOnDemandEnable = ");
            boolean flag11 = isOnDemandEnable;
            StringBuilder stringbuilder14 = stringbuilder13.append(flag11).append(" mImsTestMode = ");
            boolean flag12 = mImsTestMode;
            StringBuilder stringbuilder15 = stringbuilder14.append(flag12).append(" !isApnTypeActive(Phone.APN_TYPE_DEFAULT) = ");
            eCdmaDataConnectionTracker ecdmadataconnectiontracker3 = this;
            String s56 = "default";
            boolean flag13;
            String s57;
            int i16;
            if(!ecdmadataconnectiontracker3.isApnTypeActive(s56))
                flag13 = true;
            else
                flag13 = false;
            s57 = stringbuilder15.append(flag13).toString();
            i16 = Log.d(s55, s57);
            if(!isOnDemandEnable || mImsTestMode)
            {
                eCdmaDataConnectionTracker ecdmadataconnectiontracker4 = this;
                String s58 = "default";
                if(!ecdmadataconnectiontracker4.isApnTypeActive(s58))
                {
                    String as3[] = as;
                    int k11 = as3.length;
                    int k12 = 0;
                    do
                    {
                        int j16 = k12;
                        int k16 = k11;
                        if(j16 >= k16)
                            break;
                        String s59 = as3[k12];
                        StringBuilder stringbuilder16 = (new StringBuilder()).append("apnType.equals(default) = ");
                        String s60 = s59;
                        String s61 = "default";
                        boolean flag14 = s60.equals(s61);
                        String s62 = stringbuilder16.append(flag14).toString();
                        int l16 = Log.d("eCDMA", s62);
                        String s63 = s59;
                        String s64 = "default";
                        if(s63.equals(s64))
                        {
                            int i17 = Log.d("eCDMA", "apn type is default ");
                            eCdmaDataConnectionTracker ecdmadataconnectiontracker5 = this;
                            String s65 = s59;
                            com.android.internal.telephony.DataConnectionTracker.State state = ecdmadataconnectiontracker5.getRequestedApnState(s65);
                            StringBuilder stringbuilder17 = (new StringBuilder()).append("Got the state, it is ");
                            com.android.internal.telephony.DataConnectionTracker.State state1 = state;
                            String s66 = stringbuilder17.append(state1).toString();
                            int j17 = Log.d("eCDMA", s66);
                            com.android.internal.telephony.DataConnectionTracker.State state2 = com.android.internal.telephony.DataConnectionTracker.State.CONNECTED;
                            com.android.internal.telephony.DataConnectionTracker.State state3 = state;
                            com.android.internal.telephony.DataConnectionTracker.State state4 = state2;
                            if(state3 != state4)
                            {
                                com.android.internal.telephony.DataConnectionTracker.State state5 = com.android.internal.telephony.DataConnectionTracker.State.CONNECTING;
                                com.android.internal.telephony.DataConnectionTracker.State state6 = state;
                                com.android.internal.telephony.DataConnectionTracker.State state7 = state5;
                                if(state6 != state7)
                                {
                                    StringBuilder stringbuilder18 = (new StringBuilder()).append("Need to up the internet: apnType ");
                                    String s67 = s59;
                                    StringBuilder stringbuilder19 = stringbuilder18.append(s67).append("state = ");
                                    com.android.internal.telephony.DataConnectionTracker.State state8 = state;
                                    String s68 = stringbuilder19.append(state8).toString();
                                    int k17 = Log.d("eCDMA", s68);
                                    int l17 = mCdmaPhone.mSST.getCurrentCdmaDataConnectionState();
                                    cur_cdmaState = l17;
                                    StringBuilder stringbuilder20 = (new StringBuilder()).append("DEFAULT :cur_cdmaState = ");
                                    int i18 = cur_cdmaState;
                                    String s69 = stringbuilder20.append(i18).toString();
                                    int j18 = Log.d("eCDMA", s69);
                                    if(cur_cdmaState == 0 && mCdmaPhone.eHRPDCapable)
                                    {
                                        int k18 = Log.d("eCDMA", "add default into pending queue");
                                        eCdmaDataConnectionTracker ecdmadataconnectiontracker6 = this;
                                        String s70 = "default";
                                        ecdmadataconnectiontracker6.addPendingApnRequest(s70);
                                    }
                                }
                            }
                        }
                        int l18 = k12 + 1;
                    } while(true);
                    break label0;
                }
            }
            int i19 = Log.d("eCDMA", "ON-DEMAND is enabled or it is not mImsTestMode: Not checking if status is on");
        }
label1:
        {
            String s71 = "eCDMA";
            StringBuilder stringbuilder21 = (new StringBuilder()).append("mImsTestMode = ");
            boolean flag15 = mImsTestMode;
            StringBuilder stringbuilder22 = stringbuilder21.append(flag15).append(" !isApnTypeActive(Phone.APN_TYPE_IMS)");
            eCdmaDataConnectionTracker ecdmadataconnectiontracker7 = this;
            String s72 = "ims";
            boolean flag16;
            String s73;
            int j19;
            if(!ecdmadataconnectiontracker7.isApnTypeActive(s72))
                flag16 = true;
            else
                flag16 = false;
            s73 = stringbuilder22.append(flag16).toString();
            j19 = Log.d(s71, s73);
            if(!mImsTestMode)
            {
                eCdmaDataConnectionTracker ecdmadataconnectiontracker8 = this;
                String s74 = "ims";
                if(!ecdmadataconnectiontracker8.isApnTypeActive(s74))
                {
                    String as4[] = as;
                    int l11 = as4.length;
                    int l12 = 0;
                    do
                    {
                        int k19 = l12;
                        int l19 = l11;
                        if(k19 >= l19)
                            break;
                        String s75 = as4[l12];
                        StringBuilder stringbuilder23 = (new StringBuilder()).append("apnType.equals(ims) = ");
                        String s76 = s75;
                        String s77 = "ims";
                        boolean flag17 = s76.equals(s77);
                        String s78 = stringbuilder23.append(flag17).toString();
                        int i20 = Log.d("eCDMA", s78);
                        String s79 = s75;
                        String s80 = "ims";
                        if(s79.equals(s80))
                        {
                            int j20 = Log.d("eCDMA", "apn type is IMS ");
                            eCdmaDataConnectionTracker ecdmadataconnectiontracker9 = this;
                            String s81 = s75;
                            com.android.internal.telephony.DataConnectionTracker.State state9 = ecdmadataconnectiontracker9.getRequestedApnState(s81);
                            StringBuilder stringbuilder24 = (new StringBuilder()).append("Got the state, it is ");
                            com.android.internal.telephony.DataConnectionTracker.State state10 = state9;
                            String s82 = stringbuilder24.append(state10).toString();
                            int k20 = Log.d("eCDMA", s82);
                            com.android.internal.telephony.DataConnectionTracker.State state11 = com.android.internal.telephony.DataConnectionTracker.State.CONNECTED;
                            com.android.internal.telephony.DataConnectionTracker.State state12 = state9;
                            com.android.internal.telephony.DataConnectionTracker.State state13 = state11;
                            if(state12 != state13)
                            {
                                com.android.internal.telephony.DataConnectionTracker.State state14 = com.android.internal.telephony.DataConnectionTracker.State.CONNECTING;
                                com.android.internal.telephony.DataConnectionTracker.State state15 = state9;
                                com.android.internal.telephony.DataConnectionTracker.State state16 = state14;
                                if(state15 != state16)
                                {
                                    StringBuilder stringbuilder25 = (new StringBuilder()).append("Need to up the IMS: apnType ");
                                    String s83 = s75;
                                    StringBuilder stringbuilder26 = stringbuilder25.append(s83).append("state = ");
                                    com.android.internal.telephony.DataConnectionTracker.State state17 = state9;
                                    String s84 = stringbuilder26.append(state17).toString();
                                    int l20 = Log.d("eCDMA", s84);
                                    int i21 = mCdmaPhone.mSST.getCurrentCdmaDataConnectionState();
                                    cur_cdmaState = i21;
                                    StringBuilder stringbuilder27 = (new StringBuilder()).append("IMS:cur_cdmaState = ");
                                    int j21 = cur_cdmaState;
                                    String s85 = stringbuilder27.append(j21).toString();
                                    int k21 = Log.d("eCDMA", s85);
                                    if(cur_cdmaState == 0 && mCdmaPhone.eHRPDCapable)
                                    {
                                        int l21 = Log.d("eCDMA", "add ims into pending queue");
                                        eCdmaDataConnectionTracker ecdmadataconnectiontracker10 = this;
                                        String s86 = "ims";
                                        ecdmadataconnectiontracker10.addPendingApnRequest(s86);
                                    }
                                }
                            }
                        }
                        int i22 = l12 + 1;
                    } while(true);
                    break label1;
                }
            }
            int j22 = Log.d("eCDMA", "IMS not required");
        }
        ArrayList arraylist1 = arraylist;
        ApnSetting apnsetting1 = apnsetting;
        boolean flag18 = arraylist1.add(apnsetting1);
        if(true) goto _L6; else goto _L5
_L5:
        if(true) goto _L1; else goto _L7
_L7:
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

    private void destroyAllDefEpsBearerList()
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
        if(mPdpResetCount < i)
        {
            int j = mPdpResetCount + 1;
            mPdpResetCount = j;
            long l = sentSinceLastRecv;
            int k = EventLog.writeEvent(50102, l);
            cleanUpConnection(true, "pdpReset");
            return;
        } else
        {
            mPdpResetCount = 0;
            ((MultiModePhoneProxy)PhoneFactory.getDefaultPhone()).sendResetCommand("APECDCTPOLL");
            return;
        }
    }

    private void explicitDetach(String s)
    {
        String s1 = (new StringBuilder()).append("Explicit Detach Called: due to:").append(s).toString();
        int i = Log.d("eCDMA", s1);
        Iterator iterator = pdpList.iterator();
        do
        {
            if(!iterator.hasNext())
                break;
            GsmDataConnection gsmdataconnection = (GsmDataConnection)(DataConnection)iterator.next();
            String s2 = mAttachApnType;
            if(!gsmdataconnection.canHandleType(s2))
                continue;
            int j = Log.d("eCDMA", "Detach Initiated");
            com.android.internal.telephony.DataConnectionTracker.State state = com.android.internal.telephony.DataConnectionTracker.State.DISCONNECTING;
            setState(state);
            Message message = obtainMessage(25, s);
            gsmdataconnection.detach(message);
            break;
        } while(true);
        clearAllPendingApnRequest();
    }

    private GsmDataConnection findFreePdp()
    {
        int i;
        Iterator iterator;
        i = 0;
        iterator = pdpList.iterator();
_L5:
        if(!iterator.hasNext()) goto _L2; else goto _L1
_L1:
        GsmDataConnection gsmdataconnection;
        String s;
        gsmdataconnection = (GsmDataConnection)(DataConnection)iterator.next();
        s = mCurrentRequestedApnType;
        if(!gsmdataconnection.canHandleType(s)) goto _L4; else goto _L3
_L3:
        GsmDataConnection gsmdataconnection2;
        String s1 = (new StringBuilder()).append("Free pdp found: idx(").append(i).append(")").toString();
        log(s1);
        gsmdataconnection2 = gsmdataconnection;
_L6:
        return gsmdataconnection2;
_L4:
        i++;
          goto _L5
_L2:
        i = 0;
        iterator = pdpList.iterator();
_L7:
label0:
        {
            if(!iterator.hasNext())
                break MISSING_BLOCK_LABEL_201;
            GsmDataConnection gsmdataconnection1 = (GsmDataConnection)(DataConnection)iterator.next();
            if(!gsmdataconnection1.isInactive())
                break label0;
            StringBuilder stringbuilder = (new StringBuilder()).append("Free pdp found: idx(").append(i).append("), state(");
            String s2 = gsmdataconnection1.getStateAsString();
            String s3 = stringbuilder.append(s2).append(")").toString();
            log(s3);
            gsmdataconnection2 = gsmdataconnection1;
        }
          goto _L6
        i++;
          goto _L7
        gsmdataconnection2 = null;
          goto _L6
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

    private GsmDataConnection getConnectionByApnType(String s)
    {
        if(s != null) goto _L2; else goto _L1
_L1:
        GsmDataConnection gsmdataconnection = null;
_L4:
        return gsmdataconnection;
_L2:
        for(Iterator iterator = pdpList.iterator(); iterator.hasNext();)
        {
            GsmDataConnection gsmdataconnection1 = (GsmDataConnection)(DataConnection)iterator.next();
            if(gsmdataconnection1.canHandleType(s))
            {
                gsmdataconnection = gsmdataconnection1;
                continue; /* Loop/switch isn't completed */
            }
        }

        gsmdataconnection = null;
        if(true) goto _L4; else goto _L3
_L3:
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
        if (mPendingRequestedApns != null) {
        if(mPendingRequestedApns != null) goto _L2; else goto _L1
_L1:
        String s;
        log("mPendingRequestedApns null in getNext ");
        s = null;
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
                break MISSING_BLOCK_LABEL_134;
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
        String s3 = (new StringBuilder()).append("type returned is ").append(s1).toString();
        log(s3);
        arraylist;
        JVM INSTR monitorexit ;
        s = s1;
        continue; /* Loop/switch isn't completed */
        arraylist;
        JVM INSTR monitorexit ;
        log("return type null");
        s = null;
        if(true) goto _L5; else goto _L4
_L4:
    }

    private ApnSetting getPreferredApn()
    {
        ApnSetting apnsetting;
        if(allApns.isEmpty())
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

    private void initializeForUnitTesting()
    {
        createAllApnList();
        mCdmaPhone.eHRPDCapable = false;
    }

    private boolean isApnTypeActivating(String s)
    {
        Iterator iterator = pdpList.iterator();
_L4:
        if(!iterator.hasNext()) goto _L2; else goto _L1
_L1:
        GsmDataConnection gsmdataconnection = (GsmDataConnection)(DataConnection)iterator.next();
        if(!gsmdataconnection.canHandleType(s) || !gsmdataconnection.isActivating()) goto _L4; else goto _L3
_L3:
        boolean flag = true;
_L6:
        return flag;
_L2:
        flag = false;
        if(true) goto _L6; else goto _L5
_L5:
    }

    private boolean isApnTypeDisconnecting(String s)
    {
        Iterator iterator = pdpList.iterator();
_L4:
        if(!iterator.hasNext()) goto _L2; else goto _L1
_L1:
        GsmDataConnection gsmdataconnection = (GsmDataConnection)(DataConnection)iterator.next();
        if(!gsmdataconnection.canHandleType(s) || !gsmdataconnection.isDisconnecting()) goto _L4; else goto _L3
_L3:
        boolean flag = true;
_L6:
        return flag;
_L2:
        flag = false;
        if(true) goto _L6; else goto _L5
_L5:
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

    private boolean isEhrpdAttached()
    {
        String s = mAttachApnType;
        return isApnTypeActive(s);
    }

    private void notifyDefaultData(String s)
    {
        com.android.internal.telephony.DataConnectionTracker.State state = com.android.internal.telephony.DataConnectionTracker.State.CONNECTED;
        setState(state);
        phone.notifyDataConnection(s);
        startNetStatPoll();
        sentSinceLastRecv = 0L;
        mRetryMgr.resetRetryCount();
        mReregisterOnReconnectFailure = false;
    }

    private void notifyDefaultData(String s, String s1)
    {
        com.android.internal.telephony.DataConnectionTracker.State state = com.android.internal.telephony.DataConnectionTracker.State.CONNECTED;
        setState(state, s1);
        phone.notifyDataConnection(s);
        startNetStatPoll();
        sentSinceLastRecv = 0L;
        mRetryMgr.resetRetryCount();
        mReregisterOnReconnectFailure = false;
    }

    private void notifyNoData(com.android.internal.telephony.DataConnection.FailCause failcause)
    {
        com.android.internal.telephony.DataConnectionTracker.State state = com.android.internal.telephony.DataConnectionTracker.State.FAILED;
        setState(state);
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

    private boolean pdpStatesDormant(ArrayList arraylist)
    {
        boolean flag = true;
        ((DataCallState)arraylist.get(0)).active;
        JVM INSTR tableswitch 10 12: default 40
    //                   10 53
    //                   11 66
    //                   12 97;
           goto _L1 _L2 _L3 _L4
_L1:
        int i = Log.d("eCDMA", "pdpStatesDormant: Not a Dormant Notification, proceed with regular PDP states");
        flag = false;
_L6:
        return flag;
_L2:
        int j = Log.d("eCDMA", "pdpStatesDormant: Dormant State = INACTIVE. not expected to receive from Modem, ignore");
        continue; /* Loop/switch isn't completed */
_L3:
        int k = Log.d("eCDMA", "pdpStatesDormant: Dormant State = DORMANT");
        com.android.internal.telephony.DataConnectionTracker.Activity activity = com.android.internal.telephony.DataConnectionTracker.Activity.DORMANT;
        this.activity = activity;
        phone.notifyDataActivity();
        continue; /* Loop/switch isn't completed */
_L4:
        int l = Log.d("eCDMA", "pdpStatesDormant: Dormant State = ACTIVE");
        com.android.internal.telephony.DataConnectionTracker.Activity activity1 = com.android.internal.telephony.DataConnectionTracker.Activity.NONE;
        this.activity = activity1;
        phone.notifyDataActivity();
        if(true) goto _L6; else goto _L5
_L5:
    }

    private boolean pdpStatesHasActiveCID(ArrayList arraylist, int i)
    {
        int k;
        int l;
        String s = (new StringBuilder()).append("pdpStatesHasActiveCID: processing cid:").append(i).toString();
        int j = Log.d("eCDMA", s);
        k = 0;
        l = arraylist.size();
_L3:
        if(k >= l)
            break MISSING_BLOCK_LABEL_407;
        if(((DataCallState)arraylist.get(k)).cid == i || ((DataCallState)arraylist.get(k)).active != 0) goto _L2; else goto _L1
_L1:
        boolean flag;
        for(Iterator iterator = pdpList.iterator(); iterator.hasNext();)
        {
            GsmDataConnection gsmdataconnection = (GsmDataConnection)(DataConnection)iterator.next();
            if(gsmdataconnection.isActive() && gsmdataconnection.getCid() != i)
            {
                String s1 = (new StringBuilder()).append("Deactivated from Modem for Cid").append(i).append(": RESET").toString();
                int i1 = Log.d("eCDMA", s1);
                String s2;
                Message message;
                if(((DataCallState)arraylist.get(k)).reason == 0)
                    s2 = "pdndroppedbyNetwork";
                else
                    s2 = "handoverdisconncted";
                message = obtainMessage(25, s2);
                gsmdataconnection.disconnect(3, message);
            } else
            {
                StringBuilder stringbuilder = (new StringBuilder()).append("pdpStatesHasActiveCID: pdp.getApn()");
                ApnSetting apnsetting = gsmdataconnection.getApn();
                StringBuilder stringbuilder1 = stringbuilder.append(apnsetting).append("pdp.getCid()");
                int j1 = gsmdataconnection.getCid();
                String s3 = stringbuilder1.append(j1).append("cid").append(i).toString();
                int k1 = Log.d("eCDMA", s3);
            }
        }

        flag = true;
_L4:
        return flag;
_L2:
        StringBuilder stringbuilder2 = (new StringBuilder()).append("pdpStatesHasActiveCID: states.get(").append(k).append(").cid:");
        int l1 = ((DataCallState)arraylist.get(k)).cid;
        StringBuilder stringbuilder3 = stringbuilder2.append(l1).append("states.get(").append(k).append(").active");
        int i2 = ((DataCallState)arraylist.get(k)).active;
        String s4 = stringbuilder3.append(i2).toString();
        int j2 = Log.d("eCDMA", s4);
        k++;
          goto _L3
        flag = true;
          goto _L4
    }

    private boolean pdpStatesHasCID(ArrayList arraylist, int i)
    {
        int k;
        int l;
        String s = (new StringBuilder()).append("pdpStatesHasCID: processing cid:").append(i).toString();
        int j = Log.d("eCDMA", s);
        k = 0;
        l = arraylist.size();
_L3:
        if(k >= l)
            break MISSING_BLOCK_LABEL_107;
        if(((DataCallState)arraylist.get(k)).cid == i) goto _L2; else goto _L1
_L1:
        boolean flag;
        String s1 = (new StringBuilder()).append("pdpStatesHasCID: states have cid:").append(i).toString();
        int i1 = Log.d("eCDMA", s1);
        flag = true;
_L4:
        return flag;
_L2:
        k++;
          goto _L3
        flag = false;
          goto _L4
    }

    private void processPendingIpv6DataCallState(GsmDataConnection gsmdataconnection)
    {
label0:
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
            if(gsmdataconnection == null)
                return;
            if(gsmdataconnection.getApn() == null)
                return;
            if(gsmdataconnection.getApn().apn == null)
                return;
            String s = gsmdataconnection.getApn().apn;
            DataCallState datacallstate = (DataCallState)mPendingIpv6DataCallList.get(s);
            if(datacallstate == null)
            {
                log("No DataCall State pertaining to the ApnName given");
                return;
            }
            int i = datacallstate.cid;
            int j = gsmdataconnection.getCid();
            if(i != j)
            {
                if(datacallstate.apn == null)
                    break label0;
                String s1 = datacallstate.apn;
                String s2 = gsmdataconnection.getApn().apn;
                if(!s1.equals(s2))
                    break label0;
            }
            log("processPendingIpv6DataCallState Matching CallState found Update pdp list now");
            for(Iterator iterator = pdpList.iterator(); iterator.hasNext();)
            {
                GsmDataConnection gsmdataconnection1 = (GsmDataConnection)(DataConnection)iterator.next();
                int k = gsmdataconnection1.getCid();
                int l = gsmdataconnection.getCid();
                if(k != l)
                {
                    log("processPendingIpv6DataCallState Updating Master Pdp list now");
                    if(datacallstate.active == 1)
                    {
                        log("processPendingIpv6DataCallState Updated Master Pdp list as ipv6 configured");
                        gsmdataconnection1.isipv6configured = 1;
                        String s3 = datacallstate.address;
                        gsmdataconnection1.ipv6Address = s3;
                        StringBuilder stringbuilder = (new StringBuilder()).append("[DSAC DEB] onIpv6AddrStatusChanged: ");
                        String s4 = gsmdataconnection1.ipv6Address;
                        String s5 = stringbuilder.append(s4).toString();
                        int i1 = Log.d("eCDMA", s5);
                        removePendingIpv6DataCallList(s);
                        return;
                    }
                    log("processPendingIpv6DataCallState Updated Master Pdp list as ipv6 configured");
                    gsmdataconnection1.isipv6configured = 0;
                    removePendingIpv6DataCallList(s);
                    if(gsmdataconnection1.getApn() != null && gsmdataconnection1.getApn().canHandleType("ims"))
                    {
                        log("[DSAC DEB] onIpv6AddrStatusChanged IPv6 Address assignment failed for IMS so Detach ");
                        boolean flag = explicitDetach(1, 1);
                        return;
                    }
                    log("[DSAC DEB] onIpv6AddrStatusChanged IPv6 Address assignment failed for other APN ");
                    if(gsmdataconnection1.ipaddresstype != 2)
                    {
                        return;
                    } else
                    {
                        log("[DSAC DEB] onIpv6AddrStatusChanged IPv6 Address assignment failed and address is IPv6 alone");
                        String s6 = gsmdataconnection1.getApn().types[0];
                        int j1 = apnTypeToId(s6);
                        onEnableApn(j1, 0);
                        return;
                    }
                }
            }

            log("processPendingIpv6DataCallState Mismatch of both given Connection and Master DataConnection list");
            return;
        }
        log("processPendingIpv6DataCallState Mismatch of both CID and Apn Generally should not occur");
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
        netStatPollPeriod = 1000;
        mNoRecvPollCount = 0;
    }

    private void resetRetryByType(String s)
    {
        int i = 0;
        do
        {
            int j = mRetryEhrpd.length;
            if(i >= j)
                return;
            if(s != null)
            {
                String s1 = mRetryEhrpd[i].getApnType();
                if(s.equals(s1))
                {
                    StringBuilder stringbuilder = (new StringBuilder()).append("resetRetryByType ").append(s).append("retryManager ");
                    String s2 = mRetryEhrpd[i].getApnType();
                    String s3 = stringbuilder.append(s2).toString();
                    log(s3);
                    mRetryEhrpd[i].resetRetryCount();
                    return;
                }
            }
            i++;
        } while(true);
    }

    private boolean retryAfterDisconnected(String s)
    {
        boolean flag = true;
        boolean flag1;
        if(mCdmaPhone.mSST.getCurrentEhrpdDataConnectionState() == 0)
            flag1 = true;
        else
            flag1 = false;
        if("radioTurnedOff".equals(s) || "handoverdisconncted".equals(s) || "setPreferredNetwork".equals(s) || !flag1)
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

    private void setCurrentDefEpsBearer(GsmDataConnection gsmdataconnection)
    {
        log("[eCDMADataConnectionTracker] setCurrentDefEpsBearer is called");
        if(gsmdataconnection != null && gsmdataconnection.getApn() != null)
        {
            StringBuilder stringbuilder = (new StringBuilder()).append("[eCDMADataConnectionTracker] setCurrentDefEpsBearer defEpsBearer.getApn.apn is ");
            String s = gsmdataconnection.getApn().apn;
            String s1 = stringbuilder.append(s).toString();
            log(s1);
        } else
        {
            log("[eCDMADataConnectionTracker] setCurrentDefEpsBearer defEpsBearer.getApn.apn is null");
        }
        mCurrentdefEpsBearer = gsmdataconnection;
    }

    private void setDefaultPdpContextProperty(GsmDataConnection gsmdataconnection)
    {
        if(isApnTypeActive("default") || isApnTypeActive("*"))
        {
            SystemProperties.set("gsm.defaultpdpcontext.active", "true");
            int i = Log.d("eCDMA", "[Manual Attach] true onDataSetupComplete");
            return;
        } else
        {
            SystemProperties.set("gsm.defaultpdpcontext.active", "false");
            int j = Log.d("eCDMA", "[Manual Attach] false onDataSetupComplete");
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

    private void startDelayedRetry(com.android.internal.telephony.DataConnection.FailCause failcause, String s, String s1)
    {
        notifyNoData(failcause);
        reconnectAfterFail(failcause, s, s1);
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
                int k = Log.d("eCDMA", s6);
                addPendingApnRequest(s5);
            }
        }

        if(!mPendingRequestedApns.isEmpty())
        {
            Message message = obtainMessage(59);
            boolean flag = sendMessageDelayed(message, 1000L);
            return;
        } else
        {
            log("trySetupAllEnabledService(): Nothing to try");
            return;
        }
    }

    private void trySetupNextData()
    {
        StringBuilder stringbuilder = (new StringBuilder()).append("trySetupNextData: state is ");
        com.android.internal.telephony.DataConnectionTracker.State state = this.state;
        String s = stringbuilder.append(state).toString();
        log(s);
        if(!isDuringActionOnAnyApnType())
        {
            com.android.internal.telephony.DataConnectionTracker.State state1 = this.state;
            com.android.internal.telephony.DataConnectionTracker.State state2 = com.android.internal.telephony.DataConnectionTracker.State.INITING;
            if(state1 != state2)
            {
                String s1 = getNextPendingApnRequest();
                if(s1 != null)
                {
                    mCurrentRequestedApnType = s1;
                    StringBuilder stringbuilder1 = (new StringBuilder()).append("trySetupNextData: mCurrReqApnType(");
                    String s2 = mCurrentRequestedApnType;
                    String s3 = stringbuilder1.append(s2).append(")").toString();
                    log(s3);
                    boolean flag = trySetupData("pendingApnEnabled");
                    return;
                } else
                {
                    StringBuilder stringbuilder2 = (new StringBuilder()).append("trySetupNextData: Nothing to try(mCurrReqApnType: ");
                    String s4 = mCurrentRequestedApnType;
                    String s5 = stringbuilder2.append(s4).append(")").toString();
                    log(s5);
                    return;
                }
            }
        }
        StringBuilder stringbuilder3 = (new StringBuilder()).append("trySetupNextData: unable to try, state is ");
        com.android.internal.telephony.DataConnectionTracker.State state3 = this.state;
        String s6 = stringbuilder3.append(state3).toString();
        log(s6);
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

    private void writeEventLogCdmaDataDrop()
    {
        CdmaCellLocation cdmacelllocation = (CdmaCellLocation)(CdmaCellLocation)phone.getCellLocation();
        Object aobj[] = new Object[2];
        int i;
        Integer integer;
        Integer integer1;
        int j;
        if(cdmacelllocation != null)
            i = cdmacelllocation.getBaseStationId();
        else
            i = -1;
        integer = Integer.valueOf(i);
        aobj[0] = integer;
        integer1 = Integer.valueOf(TelephonyManager.getDefault().getNetworkType());
        aobj[1] = integer1;
        j = EventLog.writeEvent(50111, aobj);
    }

    protected boolean IsEventApnTypeEqualsApnSettingType(String as[], String as1[])
    {
        if(as != null && as1 != null) goto _L2; else goto _L1
_L1:
        boolean flag;
        log(" IsEventApnTypeEqualsApnSettingType Input APNs are null");
        flag = false;
_L4:
        return flag;
_L2:
        String as2[] = as1;
        int i = as2.length;
        int j = 0;
        do
        {
            if(j >= i)
                break;
            if(as2[j].equals("*"))
            {
                flag = true;
                continue; /* Loop/switch isn't completed */
            }
            j++;
        } while(true);
        as2 = as;
        i = as2.length;
label0:
        for(int l = 0; l < i; l++)
        {
            String s = as2[l];
            boolean flag1 = false;
            String as3[] = as1;
            int i1 = as3.length;
            int k = 0;
            do
            {
                if(k >= i1 || as3[k].equals(s))
                {
                    if(flag1)
                        continue label0;
                    String s1 = (new StringBuilder()).append(" IsEventApnTypeEqualsApnSettingType Cannot find the ApnType ").append(s).append(" so return false").toString();
                    log(s1);
                    flag = false;
                    continue; /* Loop/switch isn't completed */
                }
                k++;
            } while(true);
        }

        log("IsEventApnTypeEqualsApnSettingType All Apns are matching so return true");
        flag = true;
        if(true) goto _L4; else goto _L3
_L3:
    }

    protected void cleanUpConnection(boolean flag, String s)
    {
        if(mCdmaPhone.eHRPDCapable)
        {
            String s1 = (new StringBuilder()).append("Clean up connection due to ").append(s).toString();
            log(s1);
            Iterator iterator = mReconnectIntentEhrpd.values().iterator();
            do
            {
                if(!iterator.hasNext())
                    break;
                PendingIntent pendingintent = (PendingIntent)iterator.next();
                if(pendingintent != null)
                    ((AlarmManager)phone.getContext().getSystemService("alarm")).cancel(pendingintent);
            } while(true);
            mReconnectIntentEhrpd.clear();
            com.android.internal.telephony.DataConnectionTracker.State state = com.android.internal.telephony.DataConnectionTracker.State.DISCONNECTING;
            setState(state);
            boolean flag1 = false;
            mCleanupCount = 0;
            Iterator iterator1 = pdpList.iterator();
            do
            {
                if(!iterator1.hasNext())
                    break;
                DataConnection dataconnection = (DataConnection)iterator1.next();
                int i = mCleanupCount + 1;
                mCleanupCount = i;
                if(flag)
                {
                    log("cleanUpConnection: teardown, call conn.disconnect");
                    GsmDataConnection gsmdataconnection = (GsmDataConnection)dataconnection;
                    if(gsmdataconnection != null && gsmdataconnection.getApn() != null)
                    {
                        if(gsmdataconnection.getApn().canHandleType("ims"))
                        {
                            Message message = obtainMessage(25, s);
                            gsmdataconnection.detach(message);
                        } else
                        {
                            Message message1 = obtainMessage(25, s);
                            gsmdataconnection.disconnect(3, message1);
                        }
                        flag1 = true;
                    }
                } else
                {
                    log("cleanUpConnection: !tearDown, call conn.resetSynchronously");
                    dataconnection.resetSynchronously();
                    flag1 = false;
                }
            } while(true);
            stopNetStatPoll();
            if(!flag1)
            {
                log("cleanupConnection: !notificationDeferred");
                gotoIdleAndNotifyDataConnection(s);
            }
            clearAllPendingApnRequest();
            return;
        } else
        {
            cleanUpConnection(flag, s);
            return;
        }
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
            } while(gsmdataconnection == null || !gsmdataconnection.canHandleType(s));
            if(getActiveApnCount() > 1)
            {
                int i = Log.e("eCDMA", " More than one connections active: Calling Disconnect");
                Message message = obtainMessage(25);
                gsmdataconnection.disconnect(3, message);
            } else
            {
                int j = Log.e("eCDMA", " One connection active: Calling explicitDetach");
                Message message1 = obtainMessage(25);
                gsmdataconnection.detach(message1);
            }
        } while(true);
    }

    public void dispose()
    {
        ContentResolver contentresolver = phone.getContext().getContentResolver();
        ApnChangeObserver apnchangeobserver = apnObserver;
        contentresolver.unregisterContentObserver(apnchangeobserver);
        mCdmaPhone.mSST.unregisterForInitiateHandover(this);
        mCdmaPhone.mSST.unregisterForeHRPDHandoverNotification(this);
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
        int k = Log.d("eCDMA", s1);
        if(!isApnTypeAvailable(s))
        {
            log("type not available");
            byte0 = 2;
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

    public boolean explicitDetach(int i, int j)
    {
        if(!mCdmaPhone.eHRPDCapable) goto _L2; else goto _L1
_L1:
        int k = Log.d("eCDMA", "Explicit Detach Called");
        Iterator iterator = pdpList.iterator();
        do
        {
            if(!iterator.hasNext())
                break;
            GsmDataConnection gsmdataconnection = (GsmDataConnection)(DataConnection)iterator.next();
            String s = mAttachApnType;
            if(!gsmdataconnection.canHandleType(s))
                continue;
            int l = Log.d("eCDMA", "Detach Initiated");
            com.android.internal.telephony.DataConnectionTracker.State state = com.android.internal.telephony.DataConnectionTracker.State.DISCONNECTING;
            setState(state);
            Message message = obtainMessage(25, "apnDisabled");
            gsmdataconnection.detach(message);
            break;
        } while(true);
        clearAllPendingApnRequest();
_L4:
        return true;
_L2:
        Message message1 = obtainMessage(28);
        boolean flag = sendMessage(message1);
        if(true) goto _L4; else goto _L3
_L3:
    }

    protected int getActiveApnCount()
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
                String s = (new StringBuilder()).append("eCdmaDataConnectionTracker getActiveApnCount  ").append(i).toString();
                log(s);
            }
        } while(true);
        String s1 = (new StringBuilder()).append("eCdmaDataConnectionTracker getActiveApnCount  ").append(i).toString();
        log(s1);
        return i;
    }

    protected String getActiveApnString()
    {
        String s1;
        if(mCdmaPhone.eHRPDCapable)
        {
            String s = null;
            if(mActiveApn != null)
                s = mActiveApn.apn;
            s1 = s;
        } else
        {
            s1 = getActiveApnString();
        }
        return s1;
    }

    protected String[] getActiveApnTypes()
    {
        String as1[];
        if(mCdmaPhone.eHRPDCapable)
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
            as1 = as;
        } else
        {
            as1 = getActiveApnTypes();
        }
        return as1;
    }

    public com.android.internal.telephony.DataConnectionTracker.State getActiveState()
    {
        return state;
    }

    public ArrayList getAllDataConnections()
    {
        ArrayList arraylist;
        if(mCdmaPhone.eHRPDCapable)
            arraylist = (ArrayList)pdpList.clone();
        else
            arraylist = getAllDataConnections();
        return arraylist;
    }

    public ArrayList getApnListForHandover()
    {
        int i = Log.d("eCDMA", "getApnListForHandover");
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
                    if(apnsetting1.ipv4 == null && apnsetting1.ipv6 == null)
                    {
                        String s2 = (new StringBuilder()).append("This is not a active APN, so not adding to HandoverInfo").append(apnsetting1).toString();
                        int j = Log.d("eCDMA", s2);
                    } else
                    if(gsmdataconnection.ipaddresstype == 1 && apnsetting1.ipv4 != null || gsmdataconnection.ipaddresstype == 2 && apnsetting1.ipv6 != null || gsmdataconnection.ipaddresstype == 3 && apnsetting1.ipv4 != null && apnsetting1.ipv6 != null)
                    {
                        boolean flag = arraylist.add(apnsetting1);
                        String s3 = (new StringBuilder()).append("Active APN for Handover").append(apnsetting1).toString();
                        int k = Log.d("eCDMA", s3);
                    } else
                    {
                        l = Log.d("eCDMA", "IPtype and address not proper.");
                    }
                }
            } else
            {
                i1 = Log.d("eCDMA", "Data Connection not active");
            }
        } while(true);
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
                    int j1 = Log.d("eCDMA", s5);
                    ApnSetting apnsetting3 = new ApnSetting(apnsetting2);
                    boolean flag1 = arraylist.add(apnsetting3);
                } else
                {
                    StringBuilder stringbuilder1 = (new StringBuilder()).append("already present in result");
                    String s6 = apnsetting2.apn;
                    String s7 = stringbuilder1.append(s6).append("not adding").toString();
                    int k1 = Log.d("eCDMA", s7);
                }
            } else
            {
                StringBuilder stringbuilder2 = (new StringBuilder()).append("AllApns address null for");
                String s8 = apnsetting2.apn;
                String s9 = stringbuilder2.append(s8).append("not adding").toString();
                int l1 = Log.d("eCDMA", s9);
            }
        }

        String s10 = (new StringBuilder()).append("getApnListForHandover: result [").append(arraylist).append("]").toString();
        int i2 = Log.d("eCDMA", s10);
        return arraylist;
    }

    protected String[] getDnsServers(String s)
    {
        if(!mCdmaPhone.eHRPDCapable) goto _L2; else goto _L1
_L1:
        if(s != null) goto _L4; else goto _L3
_L3:
        if(mActivePdp == null) goto _L6; else goto _L5
_L5:
        String as[] = mActivePdp.getDnsServers();
_L11:
        return as;
_L4:
        Iterator iterator = pdpList.iterator();
_L9:
        if(!iterator.hasNext()) goto _L6; else goto _L7
_L7:
        GsmDataConnection gsmdataconnection = (GsmDataConnection)(DataConnection)iterator.next();
        if(!gsmdataconnection.canHandleType(s)) goto _L9; else goto _L8
_L8:
        as = gsmdataconnection.getDnsServers();
        continue; /* Loop/switch isn't completed */
_L6:
        as = null;
        continue; /* Loop/switch isn't completed */
_L2:
        as = getDnsServers(s);
        if(true) goto _L11; else goto _L10
_L10:
    }

    public String getGateway(String s)
    {
        if(!mCdmaPhone.eHRPDCapable) goto _L2; else goto _L1
_L1:
        if(s != null) goto _L4; else goto _L3
_L3:
        if(mActivePdp == null) goto _L6; else goto _L5
_L5:
        String s1 = mActivePdp.getGatewayAddress();
_L11:
        return s1;
_L4:
        Iterator iterator = pdpList.iterator();
_L9:
        if(!iterator.hasNext()) goto _L6; else goto _L7
_L7:
        GsmDataConnection gsmdataconnection = (GsmDataConnection)(DataConnection)iterator.next();
        if(!gsmdataconnection.canHandleType(s)) goto _L9; else goto _L8
_L8:
        s1 = gsmdataconnection.getGatewayAddress();
        continue; /* Loop/switch isn't completed */
_L6:
        s1 = null;
        continue; /* Loop/switch isn't completed */
_L2:
        s1 = getGateway(s);
        if(true) goto _L11; else goto _L10
_L10:
    }

    protected String getInterfaceName(String s)
    {
        if(!mCdmaPhone.eHRPDCapable) goto _L2; else goto _L1
_L1:
        if(s != null) goto _L4; else goto _L3
_L3:
        if(mActivePdp == null) goto _L6; else goto _L5
_L5:
        String s1 = mActivePdp.getInterface();
_L11:
        return s1;
_L4:
        Iterator iterator = pdpList.iterator();
_L9:
        if(!iterator.hasNext()) goto _L6; else goto _L7
_L7:
        GsmDataConnection gsmdataconnection = (GsmDataConnection)(DataConnection)iterator.next();
        if(!gsmdataconnection.canHandleType(s)) goto _L9; else goto _L8
_L8:
        s1 = gsmdataconnection.getInterface();
        continue; /* Loop/switch isn't completed */
_L6:
        s1 = null;
        continue; /* Loop/switch isn't completed */
_L2:
        s1 = getInterfaceName(s);
        if(true) goto _L11; else goto _L10
_L10:
    }

    protected String getIpAddress(String s)
    {
        if(!mCdmaPhone.eHRPDCapable) goto _L2; else goto _L1
_L1:
        if(s != null) goto _L4; else goto _L3
_L3:
        if(mActivePdp == null) goto _L6; else goto _L5
_L5:
        String s1 = mActivePdp.getIpAddress();
_L11:
        return s1;
_L4:
        Iterator iterator = pdpList.iterator();
_L9:
        if(!iterator.hasNext()) goto _L6; else goto _L7
_L7:
        GsmDataConnection gsmdataconnection = (GsmDataConnection)(DataConnection)iterator.next();
        if(!gsmdataconnection.canHandleType(s)) goto _L9; else goto _L8
_L8:
        s1 = gsmdataconnection.getIpAddress();
        continue; /* Loop/switch isn't completed */
_L6:
        s1 = null;
        continue; /* Loop/switch isn't completed */
_L2:
        s1 = getIpAddress(s);
        if(true) goto _L11; else goto _L10
_L10:
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

    protected com.android.internal.telephony.DataConnectionTracker.State getRequestedApnState(String s)
    {
        if(mCdmaPhone.eHRPDCapable) goto _L2; else goto _L1
_L1:
        com.android.internal.telephony.DataConnectionTracker.State state = getState();
_L4:
        return state;
_L2:
label0:
        {
            Iterator iterator = pdpList.iterator();
            GsmDataConnection gsmdataconnection;
            do
            {
                do
                {
                    if(!iterator.hasNext())
                        break label0;
                    gsmdataconnection = (GsmDataConnection)(DataConnection)iterator.next();
                } while(gsmdataconnection == null || gsmdataconnection.getApn() == null || !gsmdataconnection.getApn().canHandleType(s));
                if(gsmdataconnection.isActive())
                {
                    state = com.android.internal.telephony.DataConnectionTracker.State.CONNECTED;
                } else
                {
                    if(!gsmdataconnection.isActivating())
                        continue;
                    state = com.android.internal.telephony.DataConnectionTracker.State.INITING;
                }
                continue; /* Loop/switch isn't completed */
            } while(!gsmdataconnection.isDisconnecting());
            state = com.android.internal.telephony.DataConnectionTracker.State.DISCONNECTING;
            continue; /* Loop/switch isn't completed */
        }
        state = com.android.internal.telephony.DataConnectionTracker.State.IDLE;
        if(true) goto _L4; else goto _L3
_L3:
    }

    public com.android.internal.telephony.DataConnectionTracker.State getState()
    {
        if(mCdmaPhone.eHRPDCapable) goto _L2; else goto _L1
_L1:
        com.android.internal.telephony.DataConnectionTracker.State state = this.state;
_L4:
        return state;
_L2:
        boolean flag = false;
        boolean flag1 = false;
        Iterator iterator = pdpList.iterator();
        do
        {
            if(!iterator.hasNext())
                break;
            GsmDataConnection gsmdataconnection = (GsmDataConnection)(DataConnection)iterator.next();
            if(gsmdataconnection.canHandleType("ims") || gsmdataconnection.canHandleType("admin"))
                continue;
            if(gsmdataconnection.isActive())
            {
                state = com.android.internal.telephony.DataConnectionTracker.State.CONNECTED;
                continue; /* Loop/switch isn't completed */
            }
            if(gsmdataconnection.isActivating())
                flag = true;
            if(gsmdataconnection.isDisconnecting())
                flag1 = true;
        } while(true);
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
        if(true) goto _L4; else goto _L3
_L3:
    }

    public void handleMessage(Message message)
    {
        StringBuilder stringbuilder = (new StringBuilder()).append("[eCdmaDataConnectionTracker]handle Message Event: ");
        int i = message.what;
        String s = stringbuilder.append(i).toString();
        int j = Log.d("eCDMA", s);
        message.what;
        JVM INSTR lookupswitch 17: default 188
    //                   6: 727
    //                   27: 1307
    //                   28: 1319
    //                   29: 722
    //                   42: 744
    //                   46: 1224
    //                   49: 972
    //                   50: 447
    //                   51: 706
    //                   52: 770
    //                   55: 798
    //                   56: 827
    //                   58: 873
    //                   59: 1161
    //                   61: 1166
    //                   63: 204
    //                   68: 392;
           goto _L1 _L2 _L3 _L4 _L5 _L6 _L7 _L8 _L9 _L10 _L11 _L12 _L13 _L14 _L15 _L16 _L17 _L18
_L1:
        int k = Log.d("eCDMA", "[eCDMADataConnectionTracker] Calling super.handleMessage");
        handleMessage(message);
        return;
_L17:
        if(!mCdmaPhone.eHRPDCapable) goto _L20; else goto _L19
_L19:
        String s1 = mAttachApnType;
        if(isApnTypeActive(s1)) goto _L22; else goto _L21
_L21:
        String s2 = mAttachApnType;
        if(!isApnTypeActivating(s2)) goto _L23; else goto _L22
_L22:
        int l = Log.d("eCDMA", "[eCdmaDataConnectionTracker]HRPD activated, explicit detach eHRPD");
        explicitDetach("hrpdenabled");
        return;
_L23:
        String s3 = mAttachApnType;
        if(isApnTypeDisconnecting(s3))
        {
            int i1 = Log.d("eCDMA", "[eCdmaDataConnectionTracker] IMS PDN over eHRPD is disconnecting, wait before initiating CDMA Data Call");
            return;
        }
        if(!isAllDataConnectionInactive()) goto _L25; else goto _L24
_L24:
        cleanUpConnection(false, "hrpdenabled");
        onHOCleanupHOAPN();
        stopNetStatPoll();
        mCdmaPhone.eHRPDCapable = false;
        removeMessages(52);
        removeMessages(68);
_L20:
        String s4 = null;
        if(message.obj instanceof String)
            s4 = (String)message.obj;
        boolean flag = onTrySetupData(s4);
        return;
_L25:
        int j1 = Log.d("eCDMA", "[eCdmaDataConnectionTracker] wait for pdn disconnect");
        Message message1 = obtainMessage(68);
        boolean flag1 = sendMessageDelayed(message1, 30000L);
        return;
_L18:
        if(!mCdmaPhone.eHRPDCapable)
        {
            return;
        } else
        {
            int k1 = Log.d("eCDMA", "[eCdmaDataConnectionTracker] clean up eHRPD PDNs and try 1x/EVDO");
            cleanUpConnection(false, "hrpdenabled");
            onHOCleanupHOAPN();
            stopNetStatPoll();
            mCdmaPhone.eHRPDCapable = false;
            boolean flag2 = onTrySetupData("hrpdenabled");
            return;
        }
_L9:
        com.android.internal.telephony.HandoverTracker.State state = mHandoverTracker.getState();
        com.android.internal.telephony.HandoverTracker.State state1 = com.android.internal.telephony.HandoverTracker.State.LTE_TO_CDMA;
        if(state == state1)
        {
            log("[Handover]**L2C phase 4 : DCT is receiving params and start triggering");
            mCdmaPhone.eHRPDCapable = true;
            ArrayList arraylist = mHandoverTracker.getFields();
            putApnListForHandover(arraylist);
            ArrayList arraylist1 = mHandoverTracker.getFields();
            startHandoverConnection(arraylist1);
            return;
        }
        if(mCdmaPhone.eHRPDCapable) goto _L27; else goto _L26
_L26:
        com.android.internal.telephony.DataConnectionTracker.State state2;
        com.android.internal.telephony.DataConnectionTracker.State state3;
        state2 = this.state;
        state3 = com.android.internal.telephony.DataConnectionTracker.State.CONNECTED;
        if(state2 == state3) goto _L29; else goto _L28
_L28:
        com.android.internal.telephony.DataConnectionTracker.State state4;
        com.android.internal.telephony.DataConnectionTracker.State state5;
        state4 = this.state;
        state5 = com.android.internal.telephony.DataConnectionTracker.State.INITING;
        if(state4 != state5) goto _L30; else goto _L29
_L29:
        int l1 = Log.d("eCDMA", "[eCdmaDataConnectionTracker]eHRPD activated, explicit detach HRPD , setting legacyToEhrpd");
        onCleanUpConnection(true, "ehrpdsyncdone");
        return;
_L30:
        com.android.internal.telephony.DataConnectionTracker.State state8;
        com.android.internal.telephony.DataConnectionTracker.State state9;
        com.android.internal.telephony.DataConnectionTracker.State state6 = this.state;
        com.android.internal.telephony.DataConnectionTracker.State state7 = com.android.internal.telephony.DataConnectionTracker.State.DISCONNECTING;
        if(state6 == state7)
        {
            int i2 = Log.d("eCDMA", "[eCdmaDataConnectionTracker] HRPD is disconnecting, wait before initiating EHRPD data call");
            return;
        }
        state8 = this.state;
        state9 = com.android.internal.telephony.DataConnectionTracker.State.FAILED;
        if(state8 == state9) goto _L32; else goto _L31
_L31:
        com.android.internal.telephony.DataConnectionTracker.State state10;
        com.android.internal.telephony.DataConnectionTracker.State state11;
        state10 = this.state;
        state11 = com.android.internal.telephony.DataConnectionTracker.State.IDLE;
        if(state10 != state11) goto _L33; else goto _L32
_L32:
        onCleanUpConnection(true, "ehrpdsyncdone");
        int j2 = Log.d("eCDMA", "[eCdmaDataConnectionTracker] To make sure cleanup PPP, sent DTR off.");
_L33:
        int k2 = Log.d("eCDMA", "[eCdmaDataConnectionTracker] HRPD is not active. Can go ahead with normal operation in eHRPD");
_L27:
        removeMessages(68);
        mCdmaPhone.eHRPDCapable = true;
        int l2 = Log.d("eCDMA", "[eCDMADataConnectionTracker] EVENT_EHRPD_SYNC_COMPLETED ");
        mIsEhrpdSyncDone = true;
        trySetupAllEnabledServices();
        return;
_L10:
        int i3 = Log.d("eCDMA", "[eCDMADataConnectionTracker] EVENT_EHRPD_SYNC_BROKEN ");
        mIsEhrpdSyncDone = false;
        return;
_L5:
        onApnChanged();
        return;
_L2:
        AsyncResult asyncresult = (AsyncResult)message.obj;
        onPdpStateChanged(asyncresult, false);
        return;
_L6:
        int j3 = Log.d("eCDMA", "EVENT_IPV6_ADDR_STATUS_CHANGED ");
        AsyncResult asyncresult1 = (AsyncResult)message.obj;
        onIpv6AddrStatusChanged(asyncresult1);
        return;
_L11:
        int k3 = message.arg1;
        cidActive = k3;
        AsyncResult asyncresult2 = (AsyncResult)message.obj;
        onDataSetupCompleteEhrpd(asyncresult2);
        return;
_L12:
        log("[Handover]**C2L phase 2 : pass ho params to handovertracker");
        HandoverTracker handovertracker = mHandoverTracker;
        ArrayList arraylist2 = getApnListForHandover();
        boolean flag3 = handovertracker.startHandoverFromLte(arraylist2);
        return;
_L13:
        log("[Handover]**L2C phase 4 : DCT is receiving params and start triggering");
        mCdmaPhone.eHRPDCapable = true;
        ArrayList arraylist3 = mHandoverTracker.getFields();
        putApnListForHandover(arraylist3);
        ArrayList arraylist4 = mHandoverTracker.getFields();
        startHandoverConnection(arraylist4);
        return;
_L14:
        int l3 = Log.d("eCDMA", "[eCdmaDataConnectionTracker] USIM Records Loaded");
        int l4;
        if(message.obj instanceof AsyncResult)
        {
            int i4 = Log.d("eCDMA", "msg obj is instance of SIM records");
            SIMRecords simrecords = (SIMRecords)((AsyncResult)message.obj).userObj;
            mSimRecords = simrecords;
            int j4;
            int k4;
            if(mSimRecords == null)
                j4 = Log.d("eCDMA", "mSimRecords  is null");
            else
                k4 = Log.d("eCDMA", "mSimRecords  is not null");
        } else
        {
            l4 = Log.d("eCDMA", "msg obj is not instance of SIM records");
        }
        onSimRecordsLoaded();
        return;
_L8:
        int i5 = Log.e("eCDMA", "stoping  InactivityTimer : EVENT_INACTIVITY_TIMER_EXPIRY");
        AsyncResult asyncresult3 = (AsyncResult)message.obj;
        String s5 = null;
        if(asyncresult3.userObj instanceof String)
            s5 = (String)asyncresult3.userObj;
        stopInactivityTimer(s5);
        String s6 = (new StringBuilder()).append("InactivityTimer : disabling ").append(s5).toString();
        log(s6);
        if("ims".equals(s5) || "default".equals(s5))
        {
            Iterator iterator = pdpList.iterator();
            do
            {
                GsmDataConnection gsmdataconnection;
                do
                {
                    if(!iterator.hasNext())
                        return;
                    gsmdataconnection = (GsmDataConnection)(DataConnection)iterator.next();
                } while(!gsmdataconnection.isActive() || !gsmdataconnection.canHandleType(s5));
                Message message2 = obtainMessage(25, "pdndroppedbyNetwork");
                gsmdataconnection.disconnect(message2);
            } while(true);
        } else
        {
            int j5 = apnTypeToId(s5);
            onEnableApn(j5, 0);
            return;
        }
_L15:
        trySetupNextData();
        return;
_L16:
        int k5 = Log.d("eCDMA", "[Handover] **L2C Resume Timeout or handover fail");
        mRetryMgr.resetRetryCount();
        int l5 = 0;
        do
        {
            int i6 = mRetryEhrpd.length;
            if(l5 < i6)
            {
                mRetryEhrpd[l5].resetRetryCount();
                l5++;
            } else
            {
                trySetupAllEnabledServices();
                return;
            }
        } while(true);
_L7:
        String s7 = (String)message.obj;
        boolean flag4;
        String s8;
        int j6;
        if(message.arg1 == 0)
            flag4 = false;
        else
            flag4 = true;
        s8 = (new StringBuilder()).append("EVENT_DETACH_REQ: reason = ").append(s7).toString();
        j6 = Log.d("eCDMA", s8);
        if(mCdmaPhone.eHRPDCapable)
        {
            explicitDetach(s7);
            return;
        } else
        {
            cleanUpConnection(flag4, s7);
            return;
        }
_L3:
        startNetStatPoll();
        sentSinceLastRecv = 0L;
        return;
_L4:
        doRecovery();
        return;
    }

    public boolean isAllDataConnectionInactive()
    {
        if(!mCdmaPhone.eHRPDCapable) goto _L2; else goto _L1
_L1:
        Iterator iterator = pdpList.iterator();
_L6:
        if(!iterator.hasNext()) goto _L4; else goto _L3
_L3:
        if(((GsmDataConnection)(DataConnection)iterator.next()).isInactive()) goto _L6; else goto _L5
_L5:
        boolean flag = false;
_L8:
        return flag;
_L4:
        flag = true;
        continue; /* Loop/switch isn't completed */
_L2:
        flag = isAllDataConnectionInactive();
        if(true) goto _L8; else goto _L7
_L7:
    }

    public boolean isAnyApnTypeActive()
    {
        if(!mCdmaPhone.eHRPDCapable) goto _L2; else goto _L1
_L1:
        Iterator iterator = pdpList.iterator();
_L6:
        if(!iterator.hasNext()) goto _L4; else goto _L3
_L3:
        GsmDataConnection gsmdataconnection = (GsmDataConnection)(DataConnection)iterator.next();
        if(!gsmdataconnection.isActive()) goto _L6; else goto _L5
_L5:
        boolean flag;
        StringBuilder stringbuilder = (new StringBuilder()).append("isAnyApnTypeActive(): active pdp found: ");
        ApnSetting apnsetting = gsmdataconnection.getApn();
        String s = stringbuilder.append(apnsetting).toString();
        int i = Log.d("eCDMA", s);
        flag = true;
_L8:
        return flag;
_L4:
        flag = false;
        continue; /* Loop/switch isn't completed */
_L2:
        flag = isAnyApnTypeActive();
        if(true) goto _L8; else goto _L7
_L7:
    }

    public boolean isApnTypeActive(String s)
    {
        if(!mCdmaPhone.eHRPDCapable) goto _L2; else goto _L1
_L1:
        Iterator iterator = pdpList.iterator();
_L6:
        if(!iterator.hasNext()) goto _L4; else goto _L3
_L3:
        GsmDataConnection gsmdataconnection = (GsmDataConnection)(DataConnection)iterator.next();
        if(!gsmdataconnection.canHandleType(s) || !gsmdataconnection.isActive()) goto _L6; else goto _L5
_L5:
        boolean flag = true;
_L8:
        return flag;
_L4:
        flag = false;
        continue; /* Loop/switch isn't completed */
_L2:
        flag = isApnTypeActive(s);
        if(true) goto _L8; else goto _L7
_L7:
    }

    protected boolean isApnTypeAvailable(String s)
    {
        if(!mCdmaPhone.eHRPDCapable) goto _L2; else goto _L1
_L1:
        if(allApns == null) goto _L4; else goto _L3
_L3:
        Iterator iterator = allApns.iterator();
_L7:
        if(!iterator.hasNext()) goto _L4; else goto _L5
_L5:
        if(!((ApnSetting)iterator.next()).canHandleType(s)) goto _L7; else goto _L6
_L6:
        boolean flag = true;
_L9:
        return flag;
_L4:
        flag = false;
        continue; /* Loop/switch isn't completed */
_L2:
        flag = isApnTypeAvailable(s);
        if(true) goto _L9; else goto _L8
_L8:
    }

    protected boolean isDataAllowed()
    {
        boolean flag1;
        if(mCdmaPhone.eHRPDCapable)
        {
            boolean flag = phone.getServiceState().getRoaming();
            String s = mCurrentRequestedApnType;
            int i = apnTypeToId(s);
            if(isEnabled(i) && (!flag || getDataOnRoamingEnabled()) && mMasterDataEnabled)
                flag1 = true;
            else
                flag1 = false;
        } else
        {
            flag1 = isDataAllowed();
        }
        return flag1;
    }

    protected boolean isDeferringDataRecovery()
    {
        boolean flag;
        if(mCdmaPhone.eHRPDCapable)
        {
            com.android.internal.telephony.DataConnectionTracker.State state = getRequestedApnState("admin");
            com.android.internal.telephony.DataConnectionTracker.State state1 = com.android.internal.telephony.DataConnectionTracker.State.CONNECTED;
            if(state == state1)
                flag = true;
            else
                flag = false;
        } else
        {
            flag = isDeferringDataRecovery();
        }
        return flag;
    }

    protected void log(String s)
    {
        String s1 = (new StringBuilder()).append("[eCdmaDataConnectionTracker] ").append(s).toString();
        int i = Log.d("eCDMA", s1);
    }

    protected void onApnChanged()
    {
        log("On Apn Changed");
        boolean flag;
        CDMAPhone cdmaphone;
        String s;
        boolean flag1;
        com.android.internal.telephony.DataConnectionTracker.State state;
        com.android.internal.telephony.DataConnectionTracker.State state1;
        int i;
        if(!isAllDataConnectionInactive())
            flag = true;
        else
            flag = false;
        cdmaphone = mCdmaPhone;
        s = mCdmaPhone.mRuimRecords.getRUIMOperatorNumeric();
        flag1 = cdmaphone.updateCurrentCarrierInProvider(s);
        createAllApnList();
        state = this.state;
        state1 = com.android.internal.telephony.DataConnectionTracker.State.DISCONNECTING;
        if(state == state1)
            return;
        if(flag)
            return;
        mRetryMgr.resetRetryCount();
        i = 0;
        do
        {
            int j = mRetryEhrpd.length;
            if(i < j)
            {
                mRetryEhrpd[i].resetRetryCount();
                i++;
            } else
            {
                mReregisterOnReconnectFailure = false;
                mRequestedApnType = "ims";
                trySetupAllEnabledServices();
                return;
            }
        } while(true);
    }

    protected void onDataSetupCompleteEhrpd(AsyncResult asyncresult)
    {
        String s;
        com.android.internal.telephony.DataConnection.FailCause failcause;
label0:
        {
            s = null;
            if(asyncresult.userObj instanceof String)
                s = (String)asyncresult.userObj;
            int i = Log.e("eCDMA", "handleMessage:EVENT_DATA_SETUP_COMPLETE_EHRPD");
            if(asyncresult.exception == null)
            {
                GsmDataConnection gsmdataconnection = (GsmDataConnection)(GsmDataConnection)asyncresult.result;
                StringBuilder stringbuilder;
                ArrayList arraylist;
                String s1;
                int j;
                GsmDataConnection gsmdataconnection1;
                Object obj;
                if(gsmdataconnection != null)
                {
                    mActivePdp = gsmdataconnection;
                    ApnSetting apnsetting = gsmdataconnection.getApn();
                    mActiveApn = apnsetting;
                } else
                {
                    log("ar.result == null: This should not happen");
                }
                stringbuilder = (new StringBuilder()).append("defList : onDataSetUpCompleted ");
                arraylist = pdpList;
                s1 = stringbuilder.append(arraylist).toString();
                j = Log.e("eCDMA", s1);
                gsmdataconnection1 = gsmdataconnection;
                setCurrentDefEpsBearer(gsmdataconnection);
                gsmdataconnection1.isipv4configured = 1;
                if(waitingApns != null && !waitingApns.isEmpty())
                    obj = waitingApns.remove(0);
                if(gsmdataconnection1 == null)
                {
                    int k = Log.d("eCDMA", "onDataSetupComplete: the pdp was not found");
                    return;
                }
                if(gsmdataconnection1.canHandleType("default"))
                    mTrafficMonitor.start();
                if(gsmdataconnection1.ipaddresstype == 1)
                {
                    onDataSetupCompleteIpv4(s, gsmdataconnection1);
                    return;
                }
                if(gsmdataconnection1.ipaddresstype == 2)
                {
                    int l = Log.d("eCDMA", "[OnDataSetupComplete] IP Address Type is IPV6");
                    processPendingIpv6DataCallState(gsmdataconnection);
                    onDataSetupCompleteIpv6(asyncresult, gsmdataconnection1);
                    return;
                }
                if(gsmdataconnection1.ipaddresstype != 3)
                    return;
                int i1 = Log.d("eCDMA", "[OnDataSetupComplete] IP Address Type is IPV4V6");
                if(gsmdataconnection.isipv6configured == 1)
                {
                    onDataSetupCompleteIpv4v6(asyncresult, gsmdataconnection1);
                    return;
                }
                String s2 = gsmdataconnection.getApn().apn;
                if(!findPendingIpv6DataCallStateByApn(s2))
                    return;
                log("onDataSetupComplete IPv6 address configuration process is done ");
                processPendingIpv6DataCallState(gsmdataconnection);
                if(gsmdataconnection.isipv6configured == 1)
                {
                    log("onDataSetupComplete IPv6 address configuration is successful so notify to APP ");
                    onDataSetupCompleteIpv4(s, gsmdataconnection1);
                    return;
                }
                if(gsmdataconnection.getApn().canHandleType("ims"))
                {
                    return;
                } else
                {
                    log("onDataSetupComplete IPv6 address configuration failed but apnType is non IMS ");
                    onDataSetupCompleteIpv4(s, gsmdataconnection1);
                    return;
                }
            }
            com.android.internal.telephony.DataConnection.FailResult failresult = (com.android.internal.telephony.DataConnection.FailResult)(com.android.internal.telephony.DataConnection.FailResult)asyncresult.result;
            failcause = failresult.getFailCause();
            GsmDataConnection gsmdataconnection2 = (GsmDataConnection)(GsmDataConnection)failresult.getConnection();
            if(gsmdataconnection2 != null)
            {
                int j1 = Log.d("eCDMA", "Receive Failed result");
                mActivePdp = gsmdataconnection2;
                ApnSetting apnsetting1 = failresult.getApn();
                mActiveApn = apnsetting1;
            }
            String s3 = (new StringBuilder()).append("PDP setup failed ").append(failcause).toString();
            log(s3);
            if(mActiveApn != null)
            {
                String s4 = mActiveApn.types[0];
                resetallApnsaddressInfo(s4);
                String s5 = mActiveApn.apn;
                removePendingIpv6DataCallList(s5);
            }
            if(failcause.isEventLoggable())
            {
                CdmaCellLocation cdmacelllocation = (CdmaCellLocation)(CdmaCellLocation)phone.getCellLocation();
                int k1 = 50110;
                Object aobj[] = new Object[2];
                int l1 = 0;
                int i2;
                Integer integer;
                Integer integer1;
                int j2;
                String s6;
                int k2;
                if(cdmacelllocation != null)
                    i2 = cdmacelllocation.getBaseStationId();
                else
                    i2 = -1;
                integer = Integer.valueOf(i2);
                aobj[l1] = integer;
                integer1 = Integer.valueOf(TelephonyManager.getDefault().getNetworkType());
                aobj[1] = integer1;
                j2 = EventLog.writeEvent(k1, aobj);
            }
            if(failcause.isPermanentFail())
            {
                notifyNoData(failcause);
                if(mCurrentRequestedApnType.equals("default"))
                {
                    return;
                } else
                {
                    phone.notifyDataConnection("apnFailed");
                    s6 = mCurrentRequestedApnType;
                    k2 = apnTypeToId(s6);
                    onEnableApn(k2, 0);
                    return;
                }
            }
            if(waitingApns != null && !waitingApns.isEmpty())
            {
                StringBuilder stringbuilder1 = (new StringBuilder()).append("Removing waiting apns: current size(");
                int l2 = waitingApns.size();
                String s7 = stringbuilder1.append(l2).append(")").toString();
                log(s7);
                Object obj1 = waitingApns.remove(0);
            }
            if(!waitingApns.isEmpty())
                break MISSING_BLOCK_LABEL_945;
            if(mActiveApn == null || mCdmaPhone.mSST.getCurrentEhrpdDataConnectionState() != 0)
                break MISSING_BLOCK_LABEL_885;
            String s8 = mAttachApnType;
            if(!isApnTypeActive(s8))
            {
                ApnSetting apnsetting2 = mActiveApn;
                String s9 = mAttachApnType;
                if(!apnsetting2.canHandleType(s9))
                    break label0;
            }
            String s10 = mActiveApn.types[0];
            startDelayedRetry(failcause, s, s10);
        }
        notifyNoData(failcause);
        phone.notifyDataConnection("apnFailed");
_L1:
        if(mActivePdp != null)
            mActivePdp.resetSynchronously();
        String s11 = mCurrentRequestedApnType;
        String s12 = mAttachApnType;
        if(s11.equals(s12))
        {
            clearAllPendingApnRequest();
            return;
        } else
        {
            Message message = obtainMessage(59);
            boolean flag = sendMessage(message);
            return;
        }
        if(mActiveApn != null)
        {
            notifyNoData(failcause);
            phone.notifyDataConnection("apnFailed");
            log("Not starting delayed retry");
        } else
        {
            log("PDN Connection failed: Doing Nothing");
        }
          goto _L1
        com.android.internal.telephony.DataConnectionTracker.State state = com.android.internal.telephony.DataConnectionTracker.State.SCANNING;
        setState(state);
        Message message1 = obtainMessage(5, s);
        boolean flag1 = sendMessageDelayed(message1, 5000L);
        return;
    }

    protected void onDataSetupCompleteIpv4(String s, GsmDataConnection gsmdataconnection)
    {
        if(gsmdataconnection == null)
        {
            int i = Log.d("eCDMA", "Current bearer is null");
            return;
        }
        if(gsmdataconnection.getApn() == null)
        {
            int j = Log.d("eCDMA", "Current bearer apn is null");
            return;
        }
        if(!gsmdataconnection.isActive())
        {
            int k = Log.d("eCDMA", "Current bearer is not active");
            return;
        }
        int l = Log.d("eCDMA", "[OnDataSetupComplete] IP Address Type is IPV4");
        if(gsmdataconnection != null)
        {
            mActivePdp = gsmdataconnection;
            ApnSetting apnsetting = gsmdataconnection.getApn();
            mActiveApn = apnsetting;
        }
        String s1 = mActiveApn.types[0];
        resetallApnsaddressInfo(s1);
        String s2 = gsmdataconnection.getApn().types[0];
        resetRetryByType(s2);
        notifyDefaultData(s);
        int i1 = gsmdataconnection.getApn().inactivityValue;
        String s3 = gsmdataconnection.getApn().types[0];
        String s4 = gsmdataconnection.getInterfaceName();
        startInactivityTimer(i1, s3, s4, gsmdataconnection);
        if(isOnDemandEnable)
        {
            return;
        } else
        {
            trySetupAllEnabledServices();
            return;
        }
    }

    protected void onDataSetupCompleteIpv4v6(AsyncResult asyncresult, GsmDataConnection gsmdataconnection)
    {
        String s;
        if(asyncresult.userObj instanceof String)
            s = (String)asyncresult.userObj;
        if(gsmdataconnection == null)
        {
            int i = Log.d("eCDMA", "Current bearer is null");
            return;
        }
        if(gsmdataconnection.getApn() == null)
        {
            int j = Log.d("eCDMA", "Current bearer apn is null");
            return;
        }
        if(!gsmdataconnection.isActive())
        {
            int k = Log.d("eCDMA", "Current bearer is not active");
            return;
        }
        int l = Log.d("eCDMA", "[OnDataSetupComplete] IP Address Type is IPV4V6");
        if(gsmdataconnection.isipv6configured == 1)
        {
            int i1 = Log.d("eCDMA", "[OnDataSetupComplete] IPv6 Address is configured");
            if(gsmdataconnection != null)
            {
                mActivePdp = gsmdataconnection;
                ApnSetting apnsetting = gsmdataconnection.getApn();
                mActiveApn = apnsetting;
            }
            String s1 = mActiveApn.types[0];
            resetallApnsaddressInfo(s1);
            String s2 = gsmdataconnection.getApn().types[0];
            resetRetryByType(s2);
            notifyDefaultData("ehrpdconnected");
            int j1 = gsmdataconnection.getApn().inactivityValue;
            String s3 = gsmdataconnection.getApn().types[0];
            String s4 = gsmdataconnection.getInterfaceName();
            startInactivityTimer(j1, s3, s4, gsmdataconnection);
            if(isOnDemandEnable)
            {
                return;
            } else
            {
                trySetupAllEnabledServices();
                return;
            }
        } else
        {
            int k1 = Log.d("eCDMA", "[OnDataSetupComplete] IPv6 Address is not yet configured");
            return;
        }
    }

    protected void onDataSetupCompleteIpv6(AsyncResult asyncresult, GsmDataConnection gsmdataconnection)
    {
        int i = Log.d("eCDMA", "[OnDataSetupComplete] IP Address Type is IPV6");
        String s;
        if(asyncresult.userObj instanceof String)
            s = (String)asyncresult.userObj;
        if(gsmdataconnection == null)
        {
            int j = Log.d("eCDMA", "Current bearer is null");
            return;
        }
        if(gsmdataconnection.getApn() == null)
        {
            int k = Log.d("eCDMA", "Current bearer apn is null");
            return;
        }
        if(!gsmdataconnection.isActive())
        {
            int l = Log.d("eCDMA", "Current bearer is not active");
            return;
        }
        int i1 = Log.d("eCDMA", "[OnDataSetupComplete] IP Address Type is IPV4V6");
        if(gsmdataconnection.isipv6configured == 1)
        {
            int j1 = Log.d("eCDMA", "[OnDataSetupComplete] IPv6 Address is configured");
            if(gsmdataconnection != null)
            {
                mActivePdp = gsmdataconnection;
                ApnSetting apnsetting = gsmdataconnection.getApn();
                mActiveApn = apnsetting;
            }
            String s1 = mActiveApn.types[0];
            resetallApnsaddressInfo(s1);
            String s2 = gsmdataconnection.getApn().types[0];
            resetRetryByType(s2);
            notifyDefaultData("ehrpdconnected");
            int k1 = gsmdataconnection.getApn().inactivityValue;
            String s3 = gsmdataconnection.getApn().types[0];
            String s4 = gsmdataconnection.getInterfaceName();
            startInactivityTimer(k1, s3, s4, gsmdataconnection);
            if(isOnDemandEnable)
            {
                return;
            } else
            {
                trySetupAllEnabledServices();
                return;
            }
        } else
        {
            int l1 = Log.d("eCDMA", "[OnDataSetupComplete] IPv6 Address is not yet configured");
            return;
        }
    }

    protected void onDisconnectDone(AsyncResult asyncresult)
    {
        String s = null;
        log("EVENT_DISCONNECT_DONE");
        if(asyncresult.userObj instanceof String)
            s = (String)asyncresult.userObj;
        if(mCdmaPhone.eHRPDCapable)
        {
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
                if(asyncresult.result instanceof GsmDataConnection)
                {
                    log("ar.result is not DisconnectResult instance");
                    GsmDataConnection gsmdataconnection1 = (GsmDataConnection)(GsmDataConnection)asyncresult.result;
                    mActivePdp = gsmdataconnection1;
                    ApnSetting apnsetting1 = mActivePdp.getApn();
                    mActiveApn = apnsetting1;
                } else
                {
                    log("ar.result type error");
                    return;
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
                log("broadcasting normal disconnection event..");
                phone.notifyDataConnection(s);
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
                log("All data connection are inactive, process pending set preferred.");
                boolean flag1 = mCdmaPhone.processPendingSetPreferredNetworkAfterDataOff();
                boolean flag2;
                if(mCdmaPhone.mSST.processPendingRadioPowerOffAfterDataOff())
                    mPendingRestartRadio = false;
                else
                    onRestartRadio();
                if(mCdmaPhone.mSST.getCurrentHrpdDataConnectionState() == 0)
                    flag2 = true;
                else
                    flag2 = false;
                if(flag2)
                {
                    log(" all Data Connections Inactive in eHRPD and HRPD is in service, post EVENT_CDMA_DATA_ATTACHED ");
                    Message message = obtainMessage(63, "cdmaDataAttached");
                    boolean flag3 = sendMessage(message);
                }
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
        onDisconnectDone(asyncresult);
        boolean flag4;
        if(mCdmaPhone.mSST.getCurrentEhrpdDataConnectionState() == 0)
            flag4 = true;
        else
            flag4 = false;
        if(!flag4)
        {
            return;
        } else
        {
            log(" HRPD disconnected and eHRPD is in service, post EVENT_EHRPD_SYNC_COMPLETED ");
            Message message1 = obtainMessage(50, "ehrpdsyncdone");
            boolean flag5 = sendMessage(message1);
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
        String s = (new StringBuilder()).append("EVENT_APN_ENABLE_REQUEST ").append(i).append(", ").append(j).toString();
        log(s);
        StringBuilder stringbuilder = (new StringBuilder()).append(" dataEnabled = ");
        boolean flag = dataEnabled[i];
        StringBuilder stringbuilder1 = stringbuilder.append(flag).append(", enabledCount = ");
        int k = enabledCount;
        StringBuilder stringbuilder2 = stringbuilder1.append(k).append(", isApnTypeActive = ");
        String s1 = apnIdToType(i);
        boolean flag1 = isApnTypeActive(s1);
        String s2 = stringbuilder2.append(flag1).toString();
        log(s2);
        if(!mCdmaPhone.eHRPDCapable)
            break MISSING_BLOCK_LABEL_611;
        if(j != 1) goto _L2; else goto _L1
_L1:
        if(dataEnabled[i] == null)
        {
            dataEnabled[i] = true;
            int l = enabledCount + 1;
            enabledCount = l;
        }
        String s3 = apnIdToType(i);
        if(!isApnTypeActive(s3))
        {
            mRequestedApnType = s3;
            onEnableNewApn();
        }
_L4:
        this;
        JVM INSTR monitorexit ;
        return;
_L2:
        if(!mCdmaPhone.eHRPDCapable || dataEnabled[i] == null) goto _L4; else goto _L3
_L3:
        int j1;
        Iterator iterator;
        dataEnabled[i] = false;
        int i1 = enabledCount - 1;
        enabledCount = i1;
        String s4 = apnIdToType(i);
        removeFromPendingRequestedApns(s4);
        j1 = 0;
        iterator = pdpList.iterator();
_L11:
        if(!iterator.hasNext()) goto _L4; else goto _L5
_L5:
        GsmDataConnection gsmdataconnection;
        boolean flag3;
        int k1;
        gsmdataconnection = (GsmDataConnection)(DataConnection)iterator.next();
        String s5 = apnIdToType(i);
        boolean flag2 = gsmdataconnection.canHandleType(s5);
        StringBuilder stringbuilder3 = (new StringBuilder()).append("idx ").append(j1).append(": ");
        String s6 = gsmdataconnection.getStateAsString();
        StringBuilder stringbuilder4 = stringbuilder3.append(s6).append(", apn(");
        ApnSetting apnsetting = gsmdataconnection.getApn();
        String s7 = stringbuilder4.append(apnsetting).append("), ").append("canHandle(").append(flag2).append(")").toString();
        log(s7);
        if(gsmdataconnection.isInactive() || !flag2)
            break MISSING_BLOCK_LABEL_573;
        flag3 = false;
        k1 = 0;
_L12:
        if(k1 >= 8) goto _L7; else goto _L6
_L6:
        if(i == k1) goto _L9; else goto _L8
_L8:
        String s8 = apnIdToType(k1);
        if(!gsmdataconnection.canHandleType(s8) || dataEnabled[k1] == null) goto _L9; else goto _L10
_L10:
        log("Apn used by other connection");
        flag3 = true;
_L7:
        if(!flag3)
            if(gsmdataconnection.isDisconnecting())
            {
                log("Already in disconnecting state");
            } else
            {
                String s9 = (new StringBuilder()).append("Disconnect pdp(").append(j1).append(")").toString();
                log(s9);
                Message message = obtainMessage(25, "apnDisabled");
                gsmdataconnection.disconnect(3, message);
            }
_L13:
        j1++;
          goto _L11
_L9:
        k1++;
          goto _L12
        Exception exception;
        exception;
        throw exception;
        String s10 = (new StringBuilder()).append("idx ").append(j1).append(" pass!!").toString();
        log(s10);
          goto _L13
        onEnableApn(i, j);
          goto _L4
    }

    protected void onEnableNewApn()
    {
        if(!mCdmaPhone.eHRPDCapable)
        {
            onEnableNewApn();
            return;
        }
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
            mRetryMgr.resetRetryCount();
            String s3 = mRequestedApnType;
            resetRetryByType(s3);
            String s4 = mRequestedApnType;
            addPendingApnRequest(s4);
            Message message = obtainMessage(59);
            boolean flag = sendMessage(message);
            return;
        }
    }

    public void onHOCleanupHOAPN()
    {
        int i = Log.d("eCDMA", "[onHOCleanupHOAPN]: Cleaning up APN information ");
        if(allApns != null)
        {
            for(Iterator iterator = allApns.iterator(); iterator.hasNext();)
            {
                ApnSetting apnsetting = (ApnSetting)iterator.next();
                StringBuilder stringbuilder = (new StringBuilder()).append("[eCDMADCT]: onHOCleanupHOAPN: resetting IPaddress and IPtype for");
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
            int k = mRetryEhrpd.length;
            if(j >= k)
                return;
            mRetryEhrpd[j].resetRetryCount();
            j++;
        } while(true);
    }

    protected void onIpv6AddrStatusChanged(AsyncResult asyncresult)
    {
        GsmDataConnection gsmdataconnection;
        String s2;
        boolean flag;
label0:
        {
            ArrayList arraylist;
label1:
            {
                int i = Log.d("eCDMA", "onIpv6AddrStatusChanged");
                arraylist = (ArrayList)asyncresult.result;
                gsmdataconnection = null;
                String s = ((DataCallState)arraylist.get(0)).apn;
                int j = ((DataCallState)arraylist.get(0)).cid;
                String s1 = ((DataCallState)arraylist.get(0)).address;
                s2 = null;
                flag = false;
                StringBuilder stringbuilder = (new StringBuilder()).append("[DSAC DEB] onIpv6AddrStatusChanged cid ");
                int k = j;
                StringBuilder stringbuilder1 = stringbuilder.append(k).append("apn ");
                String s3 = s;
                String s4 = stringbuilder1.append(s3).toString();
                int l = Log.d("eCDMA", s4);
                StringBuilder stringbuilder2 = (new StringBuilder()).append("defList : onIpv6AddrStatusChanged ");
                ArrayList arraylist1 = pdpList;
                String s5 = stringbuilder2.append(arraylist1).toString();
                int i1 = Log.e("eCDMA", s5);
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
                            if(gsmdataconnection1.isInactive() || gsmdataconnection1.getApn() == null || gsmdataconnection1.getApn().apn == null)
                                break label3;
                            String s6 = gsmdataconnection1.getApn().apn;
                            String s7 = s;
                            if(!s6.equals(s7))
                                break label3;
                            flag = true;
                            gsmdataconnection = gsmdataconnection1;
                            s2 = gsmdataconnection.ipv6Address;
                            gsmdataconnection.ipv6Address = s1;
                            StringBuilder stringbuilder3 = (new StringBuilder()).append("[DSAC DEB] onIpv6AddrStatusChanged: ");
                            String s8 = gsmdataconnection.ipv6Address;
                            String s9 = stringbuilder3.append(s8).toString();
                            int j1 = Log.d("eCDMA", s9);
                            int k1 = 1;
                            gsmdataconnection1.isipv6configured = k1;
                            if(gsmdataconnection1.isActivating())
                            {
                                eCdmaDataConnectionTracker ecdmadataconnectiontracker = this;
                                String s10 = "onIpv6AddrStatusChanged isActivating()";
                                ecdmadataconnectiontracker.log(s10);
                                DataCallState datacallstate = (DataCallState)arraylist.get(0);
                                eCdmaDataConnectionTracker ecdmadataconnectiontracker1 = this;
                                DataCallState datacallstate1 = datacallstate;
                                ecdmadataconnectiontracker1.addPendingIpv6DataCallList(datacallstate1);
                            }
                        }
                        int l1 = ((DataCallState)arraylist.get(0)).active;
                        int i2 = 1;
                        if(l1 != i2)
                            break label0;
                        int j2 = Log.d("eCDMA", "[DSAC DEB] onIpv6AddrStatusChanged Address assignment is successful ");
                        boolean flag1 = flag;
                        boolean flag2 = true;
                        if(flag1 != flag2)
                            break label1;
                        int k2 = gsmdataconnection.ipaddresstype;
                        byte byte0 = 2;
                        if(k2 == byte0)
                        {
                            eCdmaDataConnectionTracker ecdmadataconnectiontracker2 = this;
                            AsyncResult asyncresult1 = asyncresult;
                            GsmDataConnection gsmdataconnection2 = gsmdataconnection;
                            ecdmadataconnectiontracker2.onIpv6AddrStatusChangedIpv6Only(asyncresult1, gsmdataconnection2);
                            return;
                        }
                        break label2;
                    }
                    StringBuilder stringbuilder4 = (new StringBuilder()).append("[DSAC DEB] onIpv6AddrStatusChanged cid value is ");
                    int l2 = gsmdataconnection1.getCid();
                    StringBuilder stringbuilder5 = stringbuilder4.append(l2).append(" iptype is ");
                    int i3 = gsmdataconnection1.ipaddresstype;
                    String s11 = stringbuilder5.append(i3).toString();
                    int j3 = Log.d("eCDMA", s11);
                } while(true);
                int k3 = gsmdataconnection.ipaddresstype;
                byte byte1 = 3;
                if(k3 == byte1)
                {
                    eCdmaDataConnectionTracker ecdmadataconnectiontracker3 = this;
                    AsyncResult asyncresult2 = asyncresult;
                    GsmDataConnection gsmdataconnection3 = gsmdataconnection;
                    ecdmadataconnectiontracker3.onIpv6AddrStatusChangedIpv6Ipv4(asyncresult2, gsmdataconnection3);
                    return;
                } else
                {
                    int l3 = Log.e("eCDMA", "Unexpected IPv6 Address configured received !!!!!");
                    return;
                }
            }
            eCdmaDataConnectionTracker ecdmadataconnectiontracker4 = this;
            String s12 = " onIpv6AddrStatusChanged CID doesnt exists so add to pending list -> active==1 ";
            ecdmadataconnectiontracker4.log(s12);
            DataCallState datacallstate2 = (DataCallState)arraylist.get(0);
            addPendingIpv6DataCallList(datacallstate2);
            return;
        }
        eCdmaDataConnectionTracker ecdmadataconnectiontracker5 = this;
        String s13 = "[DSAC DEB] onIpv6AddrStatusChanged Address assignment is not successful ";
        ecdmadataconnectiontracker5.log(s13);
        boolean flag3 = flag;
        boolean flag4 = true;
        if(flag3 != flag4)
            return;
        if(gsmdataconnection.getApn() == null)
        {
            eCdmaDataConnectionTracker ecdmadataconnectiontracker6 = this;
            String s14 = "apn is null";
            ecdmadataconnectiontracker6.log(s14);
            return;
        }
        if(gsmdataconnection.getApn().canHandleType("ims"))
        {
            eCdmaDataConnectionTracker ecdmadataconnectiontracker7 = this;
            String s15 = "[DSAC DEB] onIpv6AddrStatusChanged IPv6 Address assignment failed for IMS so Detach ";
            ecdmadataconnectiontracker7.log(s15);
            eCdmaDataConnectionTracker ecdmadataconnectiontracker8 = this;
            int i4 = 1;
            int j4 = 1;
            boolean flag5 = ecdmadataconnectiontracker8.explicitDetach(i4, j4);
            return;
        }
        eCdmaDataConnectionTracker ecdmadataconnectiontracker9 = this;
        String s16 = "[DSAC DEB] onIpv6AddrStatusChanged IPv6 Address assignment failed for other APN ";
        ecdmadataconnectiontracker9.log(s16);
        StringBuilder stringbuilder6 = (new StringBuilder()).append("old ipv6 address = ");
        String s17 = s2;
        StringBuilder stringbuilder7 = stringbuilder6.append(s17).append("current ipv6 address = ");
        String s18 = gsmdataconnection.ipv6Address;
        String s19 = stringbuilder7.append(s18).toString();
        eCdmaDataConnectionTracker ecdmadataconnectiontracker10 = this;
        String s20 = s19;
        ecdmadataconnectiontracker10.log(s20);
        if(s2 != null)
        {
            String s21 = s2;
            String s22 = "";
            if(!s21.equals(s22) && gsmdataconnection.ipv6Address != null)
            {
                String s23 = gsmdataconnection.ipv6Address;
                String s24 = s2;
                if(s23.equals(s24))
                {
                    int k4 = gsmdataconnection.ipaddresstype;
                    byte byte2 = 2;
                    if(k4 == byte2)
                    {
                        eCdmaDataConnectionTracker ecdmadataconnectiontracker11 = this;
                        String s25 = "Need to send notification to unset DNS configuration";
                        ecdmadataconnectiontracker11.log(s25);
                        GsmDataConnection gsmdataconnection4 = gsmdataconnection;
                        mActivePdp = gsmdataconnection4;
                        ApnSetting apnsetting = gsmdataconnection.getApn();
                        mActiveApn = apnsetting;
                        phone.notifyDataConnection("ipv6addressrefreshfailed");
                    }
                }
            }
        }
        int l4 = gsmdataconnection.ipaddresstype;
        byte byte3 = 2;
        if(l4 == byte3)
        {
            String s26 = gsmdataconnection.getApn().types[0];
            String s27 = "default";
            String s28 = s26;
            if(!s27.equals(s28));
            StringBuilder stringbuilder8 = (new StringBuilder()).append("[DSAC DEB] onIpv6AddrStatusChanged IPv6 Address assignment failed and address is IPv6 alone for apnType ");
            String s29 = s26;
            String s30 = stringbuilder8.append(s29).toString();
            eCdmaDataConnectionTracker ecdmadataconnectiontracker12 = this;
            String s31 = s30;
            ecdmadataconnectiontracker12.log(s31);
            if(!gsmdataconnection.isActive())
            {
                eCdmaDataConnectionTracker ecdmadataconnectiontracker13 = this;
                String s32 = s26;
                int i5 = ecdmadataconnectiontracker13.apnTypeToId(s32);
                eCdmaDataConnectionTracker ecdmadataconnectiontracker14 = this;
                int j5 = i5;
                int k5 = 0;
                ecdmadataconnectiontracker14.onEnableApn(j5, k5);
                return;
            }
            int l5 = gsmdataconnection.isipv4configured;
            int i6 = 1;
            if(l5 != i6)
            {
                return;
            } else
            {
                eCdmaDataConnectionTracker ecdmadataconnectiontracker15 = this;
                String s33 = "ipv6addressrefreshfailed";
                GsmDataConnection gsmdataconnection5 = gsmdataconnection;
                ecdmadataconnectiontracker15.onDataSetupCompleteIpv4(s33, gsmdataconnection5);
                return;
            }
        }
        int j6 = gsmdataconnection.ipaddresstype;
        byte byte4 = 2;
        if(j6 == byte4)
        {
            return;
        } else
        {
            eCdmaDataConnectionTracker ecdmadataconnectiontracker16 = this;
            AsyncResult asyncresult3 = asyncresult;
            GsmDataConnection gsmdataconnection6 = gsmdataconnection;
            ecdmadataconnectiontracker16.onIpv6AddrStatusChangedIpv6Ipv4(asyncresult3, gsmdataconnection6);
            return;
        }
    }

    protected void onIpv6AddrStatusChangedIpv6Ipv4(AsyncResult asyncresult, GsmDataConnection gsmdataconnection)
    {
        gsmdataconnection;
        JVM INSTR monitorenter ;
        if(gsmdataconnection != null)
            break MISSING_BLOCK_LABEL_18;
        int i = Log.d("eCDMA", "Current bearer is null");
        gsmdataconnection;
        JVM INSTR monitorexit ;
        return;
        if(gsmdataconnection.getApn() != null)
            break MISSING_BLOCK_LABEL_45;
        int j = Log.d("eCDMA", "Current bearer apn is null");
        gsmdataconnection;
        JVM INSTR monitorexit ;
        return;
        Exception exception;
        exception;
        gsmdataconnection;
        JVM INSTR monitorexit ;
        throw exception;
        if(gsmdataconnection.isActive())
            break MISSING_BLOCK_LABEL_65;
        int k = Log.d("eCDMA", "Current bearer is not active");
        gsmdataconnection;
        JVM INSTR monitorexit ;
        return;
        int l;
        l = gsmdataconnection.isipv4configured;
        if(l == 1)
        {
            if(gsmdataconnection != null)
            {
                mActivePdp = gsmdataconnection;
                ApnSetting apnsetting = gsmdataconnection.getApn();
                mActiveApn = apnsetting;
            }
            setDefaultPdpContextProperty(gsmdataconnection);
            String s = gsmdataconnection.getApn().types[0];
            resetRetryByType(s);
            notifyDefaultData("ehrpdconnected");
            int i1 = gsmdataconnection.getApn().inactivityValue;
            String s1 = gsmdataconnection.getApn().types[0];
            String s2 = gsmdataconnection.getInterfaceName();
            startInactivityTimer(i1, s1, s2, gsmdataconnection);
        }
        gsmdataconnection;
        JVM INSTR monitorexit ;
        if(l != 1)
            return;
        if(isOnDemandEnable)
        {
            return;
        } else
        {
            trySetupAllEnabledServices();
            return;
        }
    }

    protected void onIpv6AddrStatusChangedIpv6Only(AsyncResult asyncresult, GsmDataConnection gsmdataconnection)
    {
        log("onIpv6AddrStatusChangedIpv6Only");
        gsmdataconnection;
        JVM INSTR monitorenter ;
        if(gsmdataconnection != null)
            break MISSING_BLOCK_LABEL_25;
        int i = Log.d("eCDMA", "Current bearer is null");
        gsmdataconnection;
        JVM INSTR monitorexit ;
        return;
        if(gsmdataconnection.getApn() != null)
            break MISSING_BLOCK_LABEL_52;
        int j = Log.d("eCDMA", "Current bearer apn is null");
        gsmdataconnection;
        JVM INSTR monitorexit ;
        return;
        Exception exception;
        exception;
        gsmdataconnection;
        JVM INSTR monitorexit ;
        throw exception;
        if(gsmdataconnection.isActive())
            break MISSING_BLOCK_LABEL_72;
        int k = Log.d("eCDMA", "Current bearer is not active");
        gsmdataconnection;
        JVM INSTR monitorexit ;
        return;
        int l;
        l = gsmdataconnection.isipv4configured;
        if(l == 1)
        {
            if(gsmdataconnection != null)
            {
                mActivePdp = gsmdataconnection;
                ApnSetting apnsetting = gsmdataconnection.getApn();
                mActiveApn = apnsetting;
            }
            setDefaultPdpContextProperty(gsmdataconnection);
            String s = gsmdataconnection.getApn().types[0];
            resetRetryByType(s);
            notifyDefaultData("ehrpdconnected");
            int i1 = gsmdataconnection.getApn().inactivityValue;
            String s1 = gsmdataconnection.getApn().types[0];
            String s2 = gsmdataconnection.getInterfaceName();
            startInactivityTimer(i1, s1, s2, gsmdataconnection);
        }
        gsmdataconnection;
        JVM INSTR monitorexit ;
        if(l != 1)
            return;
        if(isOnDemandEnable)
        {
            return;
        } else
        {
            trySetupAllEnabledServices();
            return;
        }
    }

    protected void onNVReady()
    {
        onNVReady();
        createAllApnList();
    }

    protected void onPdpStateChanged(AsyncResult asyncresult, boolean flag)
    {
        if(mCdmaPhone.eHRPDCapable)
        {
            ArrayList arraylist = (ArrayList)(ArrayList)asyncresult.result;
            if(asyncresult.exception != null)
                return;
            if(!isAnyApnTypeActive())
                return;
            if(pdpStatesDormant(arraylist))
            {
                int i = Log.d("eCDMA", "onPdpStateChanged: This is a Dormant Mode Status Notification.");
                return;
            }
            Iterator iterator = pdpList.iterator();
            do
            {
                GsmDataConnection gsmdataconnection;
                do
                {
                    if(!iterator.hasNext())
                        return;
                    gsmdataconnection = (GsmDataConnection)(DataConnection)iterator.next();
                } while(!gsmdataconnection.isActive() || gsmdataconnection.getCid() == -1);
                int j = gsmdataconnection.getCid();
                StringBuilder stringbuilder = (new StringBuilder()).append("onPdpStateChanged: active pdp ");
                ApnSetting apnsetting = gsmdataconnection.getApn();
                String s = stringbuilder.append(apnsetting).append("cid:").append(j).toString();
                int k = Log.d("eCDMA", s);
                if(!pdpStatesHasCID(arraylist, j))
                {
                    log("PDP connection has dropped. Reconnecting");
                    writeEventLogCdmaDataDrop();
                } else
                if(!pdpStatesHasActiveCID(arraylist, j))
                    if(!flag)
                    {
                        flag = true;
                    } else
                    {
                        log("PDP connection has dropped (active=false case).  Reconnecting");
                        writeEventLogCdmaDataDrop();
                    }
            } while(true);
        } else
        {
            onDataStateChanged(asyncresult);
            return;
        }
    }

    protected void onSimRecordsLoaded()
    {
        int i = Log.d("eCDMA", "[eCdmaDataConnectionTracker] onSimRecordsLoaded calling CreateALLAPnList");
        createAllApnList();
        mIsSimRecordsLoaded = true;
        Message message = obtainMessage(5, "simLoaded");
        boolean flag = sendMessage(message);
    }

    protected void onVoiceCallEnded()
    {
        log("Overriding onVoiceCallEnded() in eCDMADCT");
        com.android.internal.telephony.DataConnectionTracker.State state = getState();
        com.android.internal.telephony.DataConnectionTracker.State state1 = com.android.internal.telephony.DataConnectionTracker.State.CONNECTED;
        if(state == state1)
        {
            if(!mCdmaPhone.mSST.isConcurrentVoiceAndData())
            {
                startNetStatPoll();
                phone.notifyDataConnection("2GVoiceCallEnded");
            } else
            {
                resetPollStats();
            }
            trySetupAllEnabledServices();
            return;
        }
        mRetryMgr.resetRetryCount();
        int i = 0;
        do
        {
            int j = mRetryEhrpd.length;
            if(i < j)
            {
                mRetryEhrpd[i].resetRetryCount();
                i++;
            } else
            {
                trySetupAllEnabledServices();
                return;
            }
        } while(true);
    }

    protected void onVoiceCallStarted()
    {
        log("Overriding onVoiceCallStarted() in eCDMADCT");
        com.android.internal.telephony.DataConnectionTracker.State state = getState();
        com.android.internal.telephony.DataConnectionTracker.State state1 = com.android.internal.telephony.DataConnectionTracker.State.CONNECTED;
        if(state == state1 && !mCdmaPhone.mSST.isConcurrentVoiceAndData())
        {
            stopNetStatPoll();
            phone.notifyDataConnection("2GVoiceCallStarted");
        }
        String s = mCurrentRequestedApnType;
        addPendingApnRequest(s);
    }

    public void putApnListForHandover(ArrayList arraylist)
    {
        int i = Log.d("eCDMA", "[HANDOVER] putApnListForHandover");
        if(arraylist == null)
        {
            int j = Log.d("eCDMA", "[HANDOVER] Handover APN List is null, can't putApnListForHandover we proceed with initial attach");
            return;
        }
        if(allApns == null)
        {
            int k = Log.d("eCDMA", "[HANDOVER] allApn List is null, can't putApnListForHandover");
            return;
        }
        int l = Log.d("eCDMA", "[Handover] **L2C cleanup AllApns handover info before copying Handover Information");
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
                String s4 = (new StringBuilder()).append("[HANDOVER] handover info is copied for apn: ").append(apnsetting1).toString();
                int i1 = Log.d("eCDMA", s4);
                break;
            } while(true);
            if(!flag)
            {
                String s5 = (new StringBuilder()).append("[HANDOVER] no APN found for: ").append(apnsetting).toString();
                int j1 = Log.d("eCDMA", s5);
            }
        } while(true);
    }

    protected void reconnectAfterFail(com.android.internal.telephony.DataConnection.FailCause failcause, String s, String s1)
    {
        com.android.internal.telephony.DataConnectionTracker.State state = this.state;
        com.android.internal.telephony.DataConnectionTracker.State state1 = com.android.internal.telephony.DataConnectionTracker.State.FAILED;
        if(state != state1 || !mCdmaPhone.eHRPDCapable)
            break MISSING_BLOCK_LABEL_555;
        if(s1 == null)
        {
            int i = Log.d("eCDMA", " reconnectAfterFail returning since type is null");
            return;
        }
        RetryManager retrymanager = null;
        int j = 0;
label0:
        do
        {
label1:
            {
                int k = mRetryEhrpd.length;
                if(j < k)
                {
                    if(!mRetryEhrpd[j].getApnType().equals(s1))
                        break label1;
                    retrymanager = mRetryEhrpd[j];
                }
                if(retrymanager == null)
                {
                    phone.notifyDataConnection("apnFailed");
                    int l = apnTypeToId(s1);
                    onEnableApn(l, 0);
                    return;
                }
                break label0;
            }
            j++;
        } while(true);
label2:
        {
            StringBuilder stringbuilder = (new StringBuilder()).append(" reconnectAfterFail selected retrymanager is :");
            String s2 = retrymanager.getApnType();
            String s3 = stringbuilder.append(s2).toString();
            log(s3);
            if(!retrymanager.isRetryNeeded())
            {
                String s4 = (new StringBuilder()).append(" reconnectAfterFail No More Retry required for mCurrentRequestedApnType ").append(s1).toString();
                log(s4);
                if(!s1.equals("default") && !s1.equals("ims"))
                {
                    phone.notifyDataConnection("apnFailed");
                    int i1 = apnTypeToId(s1);
                    onEnableApn(i1, 0);
                    return;
                }
                if(!mReregisterOnReconnectFailure)
                    break label2;
                retrymanager.retryForeverUsingLastTimeout();
            }
            int j1 = retrymanager.getRetryTimer();
            StringBuilder stringbuilder1 = (new StringBuilder()).append("PDP activate failed. Scheduling next attempt for");
            int k1 = j1 / 1000;
            String s5 = stringbuilder1.append(k1).append("s").toString();
            int l1 = Log.d("eCDMA", s5);
            AlarmManager alarmmanager = (AlarmManager)phone.getContext().getSystemService("alarm");
            String s6 = (new StringBuilder()).append("com.android.internal.telephony.ehrpd-reconnect.").append(s1).toString();
            Intent intent = new Intent(s6);
            Intent intent1 = intent.putExtra("com.android.internal.telephony.cdma.ehrpd-reason", s);
            Intent intent2 = intent.putExtra("com.android.internal.telephony.cdma.ehrpd-reqtype", s1);
            String s7 = (new StringBuilder()).append(" sending reconnect for ").append(s1).append(" reason ").append(s).toString();
            int i2 = Log.d("eCDMA", s7);
            PendingIntent pendingintent = PendingIntent.getBroadcast(phone.getContext(), 0, intent, 0);
            long l2 = SystemClock.elapsedRealtime();
            long l3 = j1;
            long l4 = l2 + l3;
            alarmmanager.set(2, l4, pendingintent);
            Object obj = mReconnectIntentEhrpd.put(s1, pendingintent);
            retrymanager.increaseRetryCount();
            int k2;
            if(!shouldPostNotification(failcause))
            {
                int j2 = Log.d("eCDMA", "NOT Posting GPRS Unavailable notification -- likely transient error");
                return;
            } else
            {
                notifyNoData(failcause);
                return;
            }
        }
        k2 = Log.d("eCDMA", "PDP activate failed, Reregistering to the network");
        mReregisterOnReconnectFailure = true;
        phone.notifyDataConnection("apnFailed");
        mCdmaPhone.mSST.reRegisterNetwork(null);
        retrymanager.resetRetryCount();
        return;
        reconnectAfterFail(failcause, s);
        return;
    }

    public void resetallApnsaddressInfo(String s)
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
            StringBuilder stringbuilder = (new StringBuilder()).append("[eCDMADCT]resetting IPaddress and IPtype for");
            String s1 = apnsetting.apn;
            String s2 = stringbuilder.append(s1).toString();
            log(s2);
            apnsetting.ipv4 = null;
            apnsetting.ipv6 = null;
        } while(true);
    }

    public void sendPsAttachInfo()
    {
        int i = Log.d("eCDMA", "sendPsAttachInfo Called from eCDMADCT");
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
                this.state = state;
                return;
            }
        } else
        {
            log("setState Else Case");
            return;
        }
    }

    public void setUpDedicatedBearer(String s)
    {
    }

    protected boolean setupData(String s)
    {
        if(!mCdmaPhone.eHRPDCapable) goto _L2; else goto _L1
_L1:
        ApnSetting apnsetting = getNextApn();
        if(apnsetting != null) goto _L4; else goto _L3
_L3:
        boolean flag = false;
_L10:
        return flag;
_L4:
        GsmDataConnection gsmdataconnection;
        Message message;
        gsmdataconnection = findFreePdp();
        if(gsmdataconnection == null)
        {
            log("setupData: No free GsmDataConnection found!");
            flag = false;
            continue; /* Loop/switch isn't completed */
        }
        StringBuilder stringbuilder;
        boolean flag1;
        StringBuilder stringbuilder1;
        boolean flag2;
        String s4;
        int j1;
        int i2;
        com.android.internal.telephony.DataConnectionTracker.State state;
        int j2;
        if(false)
            if(mSimRecords.getACLStatus())
            {
                int i = Log.d("eCDMA", "getACLStatus() is true");
                if(mSimRecords == null)
                {
                    flag = false;
                    continue; /* Loop/switch isn't completed */
                }
                String s1 = apnsetting.apn;
                int j = Log.d("eCDMA", s1);
                SIMRecords simrecords = mSimRecords;
                String s2 = apnsetting.apn;
                if(!simrecords.verifyACL(s2))
                {
                    int k = Log.d("eCDMA", "verifyACL is false");
                    String s3 = apnsetting.types[0];
                    if("admin".equals(s3))
                    {
                        int l = Log.d("eCDMA", "ADMIN APN requested and verifyACL is false. Calling explicitDetach()");
                        explicitDetach(null);
                    }
                    flag = false;
                    continue; /* Loop/switch isn't completed */
                }
                int i1 = Log.d("eCDMA", "verifyACL is true");
            } else
            {
                j2 = Log.d("eCDMA", "IsACLEnabled() is false");
            }
        mActiveApn = apnsetting;
        mActivePdp = gsmdataconnection;
        message = obtainMessage();
        message.what = 52;
        message.obj = s;
        stringbuilder = (new StringBuilder()).append("[eCDMADataConnectionTracker]: mIsImsEnabled = ");
        flag1 = mIsImsEnabled;
        stringbuilder1 = stringbuilder.append(flag1).append("mIsAdminEnabled = ");
        flag2 = mIsAdminEnabled;
        s4 = stringbuilder1.append(flag2).toString();
        j1 = Log.d("eCDMA", s4);
        if(apnsetting.ipv6 != null || apnsetting.ipv4 != null)
        {
            gsmdataconnection.resetHandoverIpInfo();
            StringBuilder stringbuilder2 = (new StringBuilder()).append("[Handover] **L2C IPv6:");
            String s5 = apnsetting.ipv6;
            StringBuilder stringbuilder3 = stringbuilder2.append(s5).append(" IPv4:");
            String s6 = apnsetting.ipv4;
            String s7 = stringbuilder3.append(s6).toString();
            int k1 = Log.d("eCDMA", s7);
            String s8 = apnsetting.ipv6;
            String s9 = apnsetting.ipv4;
            gsmdataconnection.setHandoverIpInfo(s8, s9);
            int l1 = GsmDataConnection.CONN_TYPE_HANDOVER;
            gsmdataconnection.setConnType(l1);
        } else
        {
            gsmdataconnection.resetHandoverIpInfo();
            int k2 = GsmDataConnection.CONN_TYPE_INITIAL;
            gsmdataconnection.setConnType(k2);
            int l2 = Log.d("eCDMA", "[eCDMADataConnectionTracker] Initial Connect ");
        }
        if(!isEhrpdAttached()) goto _L6; else goto _L5
_L5:
        i2 = Log.d("eCDMA", "[eCDMADataConnectionTracker] connect called ");
        gsmdataconnection.connect(message, apnsetting, 3, false);
_L8:
        state = com.android.internal.telephony.DataConnectionTracker.State.INITING;
        setState(state);
        if(apnsetting.ipv4 == null && apnsetting.ipv6 == null)
            phone.notifyDataConnection(s);
        flag = true;
        continue; /* Loop/switch isn't completed */
_L6:
        if(!mIsImsEnabled || !mIsAdminEnabled)
            break; /* Loop/switch isn't completed */
        int i3 = Log.d("eCDMA", "[eCDMADataConnectionTracker] attach called ");
        gsmdataconnection.connect(message, apnsetting, 3, true);
        if(true) goto _L8; else goto _L7
_L7:
        flag = false;
        continue; /* Loop/switch isn't completed */
_L2:
        flag = setupData(s);
        if(true) goto _L10; else goto _L9
_L9:
    }

    protected boolean shouldPostNotification(com.android.internal.telephony.DataConnection.FailCause failcause)
    {
        if(false) goto _L2; else goto _L1
_L1:
        com.android.internal.telephony.DataConnection.FailCause failcause1 = com.android.internal.telephony.DataConnection.FailCause.UNKNOWN;
        if(failcause == failcause1) goto _L2; else goto _L3
_L3:
        boolean flag = true;
_L5:
        return flag;
_L2:
        flag = false;
        if(true) goto _L5; else goto _L4
_L4:
    }

    public void startHandoverConnection(ArrayList arraylist)
    {
        int i = Log.d("eCDMA", "[HANDOVER] startHandoverConnection");
        int j;
        if(arraylist == null)
            j = Log.d("eCDMA", "[HANDOVER] Handover APN List is null, we are going for initial attach");
        if(allApns == null)
        {
            int k = Log.d("eCDMA", "[HANDOVER] allApn List is null, can't startHandoverConnection");
            return;
        }
        clearAllPendingApnRequest();
        ApnSetting apnsetting = null;
        if(arraylist != null)
        {
            Iterator iterator = arraylist.iterator();
            do
            {
                if(!iterator.hasNext())
                    break;
                ApnSetting apnsetting1 = (ApnSetting)iterator.next();
                String s = mAttachApnType;
                if(!apnsetting1.canHandleType(s))
                    continue;
                mIsEhrpdSyncDone = true;
                apnsetting = apnsetting1;
                String s1 = mAttachApnType;
                addPendingApnRequest(s1);
                Message message = obtainMessage(59);
                boolean flag = sendMessage(message);
                break;
            } while(true);
            if(apnsetting == null)
            {
                int l = Log.d("eCDMA", "[HANDOVER] IMS APN not present, cannot startHandoverConnection..proceeding with initial");
                Iterator iterator1 = allApns.iterator();
                ApnSetting apnsetting3;
                String s2;
                do
                {
                    if(!iterator1.hasNext())
                        return;
                    apnsetting3 = (ApnSetting)iterator1.next();
                    s2 = mAttachApnType;
                } while(!apnsetting3.canHandleType(s2));
                int i1 = Log.d("eCDMA", "[HANDOVER] triggering for IMS APN in AllApns");
                mIsEhrpdSyncDone = true;
                String s3 = mAttachApnType;
                addPendingApnRequest(s3);
                Message message1 = obtainMessage(59);
                boolean flag1 = sendMessage(message1);
                return;
            }
            Iterator iterator2 = arraylist.iterator();
            do
            {
                ApnSetting apnsetting2;
                String s4;
                do
                {
                    if(!iterator2.hasNext())
                        return;
                    apnsetting2 = (ApnSetting)iterator2.next();
                    s4 = mAttachApnType;
                } while(apnsetting2.canHandleType(s4));
                String s5 = apnsetting2.types[0];
                addPendingApnRequest(s5);
            } while(true);
        } else
        {
            int j1 = Log.d("eCDMA", "[HANDOVER] Searching for IMS APN in AllApns since no APN found in HandoverInfo");
            Iterator iterator3 = allApns.iterator();
            ApnSetting apnsetting4;
            String s6;
            do
            {
                if(!iterator3.hasNext())
                    return;
                apnsetting4 = (ApnSetting)iterator3.next();
                s6 = mAttachApnType;
            } while(!apnsetting4.canHandleType(s6));
            int k1 = Log.d("eCDMA", "[HANDOVER] triggering for IMS APN in AllApns");
            mIsEhrpdSyncDone = true;
            String s7 = mAttachApnType;
            addPendingApnRequest(s7);
            Message message2 = obtainMessage(59);
            boolean flag2 = sendMessage(message2);
            return;
        }
    }

    protected void startInactivityTimer(int i, String s, String s1, GsmDataConnection gsmdataconnection)
    {
        String s2 = (new StringBuilder()).append("InactivityTimer : startInactivityTimer for ").append(s).toString();
        log(s2);
        Message message = obtainMessage();
        eCdmaDataConnectionTracker ecdmadataconnectiontracker = this;
        int j = i;
        String s3 = s;
        String s4 = s1;
        GsmDataConnection gsmdataconnection1 = gsmdataconnection;
        NetPollStatTimer netpollstattimer = ecdmadataconnectiontracker. new NetPollStatTimer(j, s3, message, s4, gsmdataconnection1);
        Object obj = mInactivityTimerList.put(s, netpollstattimer);
        netpollstattimer.run();
    }

    protected void startNetStatPoll()
    {
        if(mCdmaPhone.eHRPDCapable)
        {
            if(isAnyApnTypeActive() && !netStatPollEnabled)
            {
                int i = Log.d("eCDMA", "eCdma Start poll NetStat");
                resetPollStats();
                com.android.internal.telephony.DataConnectionTracker.Activity activity = this.activity;
                com.android.internal.telephony.DataConnectionTracker.Activity activity1 = com.android.internal.telephony.DataConnectionTracker.Activity.DORMANT;
                if(activity == activity1)
                {
                    com.android.internal.telephony.DataConnectionTracker.Activity activity2 = com.android.internal.telephony.DataConnectionTracker.Activity.DORMANT;
                    this.activity = activity2;
                } else
                {
                    com.android.internal.telephony.DataConnectionTracker.Activity activity3 = com.android.internal.telephony.DataConnectionTracker.Activity.NONE;
                    this.activity = activity3;
                }
                netStatPollEnabled = true;
                mEhrpdPollNetStat.run();
                return;
            } else
            {
                StringBuilder stringbuilder = (new StringBuilder()).append("Can't start poll NetStat: isConnected-");
                boolean flag = isAnyApnTypeActive();
                StringBuilder stringbuilder1 = stringbuilder.append(flag).append(" Ended-");
                boolean flag1 = netStatPollEnabled;
                String s = stringbuilder1.append(flag1).toString();
                int j = Log.d("eCDMA", s);
                return;
            }
        } else
        {
            startNetStatPoll();
            return;
        }
    }

    protected void stopInactivityTimer(String s)
    {
        String s1 = (new StringBuilder()).append("InactivityTimer : stopInactivityTimer for ").append(s).toString();
        log(s1);
        Runnable runnable = (Runnable)mInactivityTimerList.remove(s);
        removeCallbacks(runnable);
    }

    protected void stopNetStatPoll()
    {
        if(mCdmaPhone.eHRPDCapable)
        {
            netStatPollEnabled = false;
            Runnable runnable = mEhrpdPollNetStat;
            removeCallbacks(runnable);
            log("eCDMA Stop poll NetStat");
            return;
        } else
        {
            stopNetStatPoll();
            return;
        }
    }

    protected boolean trySetupData(String s)
    {
        if(!mCdmaPhone.eHRPDCapable) goto _L2; else goto _L1
_L1:
        boolean flag;
        StringBuilder stringbuilder = (new StringBuilder()).append("***trySetupData due to ");
        String s1;
        StringBuilder stringbuilder1;
        String s2;
        String s3;
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
        if(phone.getSimulatedRadioControl() == null) goto _L4; else goto _L3
_L3:
        state = com.android.internal.telephony.DataConnectionTracker.State.CONNECTED;
        setState(state);
        phone.notifyDataConnection(s);
        i = Log.i("eCDMA", "(fix?) We're on the simulator; assuming data is connected");
        flag = true;
_L6:
        return flag;
_L4:
        if(!mCurrentRequestedApnType.equals("ims") && !mImsTestMode && !isApnTypeActive("ims"))
        {
            StringBuilder stringbuilder2 = (new StringBuilder()).append("eCdmaDCT: Non-Ims PDN should be rejected if IMS is not up mCurrentRequestedApnType:");
            String s4 = mCurrentRequestedApnType;
            StringBuilder stringbuilder3 = stringbuilder2.append(s4).append("mImsTestMode");
            boolean flag1 = mImsTestMode;
            StringBuilder stringbuilder4 = stringbuilder3.append(flag1).append("isApnTypeActive(ims)");
            boolean flag2 = isApnTypeActive("ims");
            String s5 = stringbuilder4.append(flag2).toString();
            log(s5);
            flag = false;
            continue; /* Loop/switch isn't completed */
        }
        if(!mCurrentRequestedApnType.equals("default") && mImsTestMode && !isApnTypeActive("default"))
        {
            StringBuilder stringbuilder5 = (new StringBuilder()).append("eCdmaDCT: Non-default PDN should be rejected if default pdn is not up in test mode: mCurrentRequestedApnType:");
            String s6 = mCurrentRequestedApnType;
            StringBuilder stringbuilder6 = stringbuilder5.append(s6).append("mImsTestMode");
            boolean flag3 = mImsTestMode;
            StringBuilder stringbuilder7 = stringbuilder6.append(flag3).append("isApnTypeActive(default)");
            boolean flag4 = isApnTypeActive("default");
            String s7 = stringbuilder7.append(flag4).toString();
            log(s7);
            flag = false;
            continue; /* Loop/switch isn't completed */
        }
        String s8 = android.provider.Settings.System.getString(phone.getContext().getContentResolver(), "mode_type");
        boolean flag5;
        boolean flag6;
        boolean flag7;
        GsmDataConnection gsmdataconnection;
        boolean flag8;
        boolean flag9;
        if(s8.equals("CDMA") || s8.equals("GLOBAL"))
            flag5 = true;
        else
            flag5 = false;
        flag6 = phone.getServiceState().getRoaming();
        flag7 = mCdmaPhone.mSST.getDesiredPowerState();
        gsmdataconnection = findFreePdp();
        if(gsmdataconnection == null)
            flag8 = false;
        else
            flag8 = true;
        flag9 = SystemProperties.getBoolean("gsm.default.esn", false);
        if(flag8 && gsmdataconnection.isInactive())
        {
            com.android.internal.telephony.CommandsInterface.RadioState radiostate = phone.mCM.getRadioState();
            com.android.internal.telephony.CommandsInterface.RadioState radiostate1 = com.android.internal.telephony.CommandsInterface.RadioState.NV_READY;
            if(radiostate == radiostate1 || mCdmaPhone.mRuimRecords.getRecordsLoaded())
            {
                com.android.internal.telephony.Phone.State state1 = phone.getState();
                com.android.internal.telephony.Phone.State state2 = com.android.internal.telephony.Phone.State.IDLE;
                if((state1 == state2 || mCdmaPhone.mSST.isConcurrentVoiceAndData()) && isDataAllowed() && flag7 && !mPendingRestartRadio && flag5 && !getDataOnDunEnabled() && mIsEhrpdSyncDone && mCdmaPhone.mSST.getCurrentEhrpdDataConnectionState() == 0 && !flag9)
                {
                    com.android.internal.telephony.HandoverTracker.State state3 = mHandoverTracker.getState();
                    com.android.internal.telephony.HandoverTracker.State state4 = com.android.internal.telephony.HandoverTracker.State.CDMA_TO_LTE;
                    if(state3 != state4 && mIsSimRecordsLoaded && mIsImsEnabled && mIsAdminEnabled)
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
                                flag = false;
                                continue; /* Loop/switch isn't completed */
                            }
                            StringBuilder stringbuilder8 = (new StringBuilder()).append("Create from allApns : ");
                            ArrayList arraylist1 = allApns;
                            String s9 = apnListToString(arraylist1);
                            String s10 = stringbuilder8.append(s9).toString();
                            log(s10);
                        }
                        StringBuilder stringbuilder9 = (new StringBuilder()).append("Setup waitngApns : ");
                        ArrayList arraylist2 = waitingApns;
                        String s11 = apnListToString(arraylist2);
                        String s12 = stringbuilder9.append(s11).toString();
                        log(s12);
                        boolean flag10 = setupData(s);
                        if(!flag10)
                        {
                            log("setupData() has returned false. Clearing waitingApns");
                            if(waitingApns != null && !waitingApns.isEmpty())
                            {
                                StringBuilder stringbuilder10 = (new StringBuilder()).append("Removing waiting apns: current size(");
                                int j = waitingApns.size();
                                StringBuilder stringbuilder11 = stringbuilder10.append(j).append(") and apn is ");
                                ArrayList arraylist3 = waitingApns;
                                String s13 = apnListToString(arraylist3);
                                String s14 = stringbuilder11.append(s13).toString();
                                log(s14);
                                Object obj = waitingApns.remove(0);
                            }
                        }
                        flag = flag10;
                        continue; /* Loop/switch isn't completed */
                    }
                }
            }
        }
        StringBuilder stringbuilder12 = (new StringBuilder()).append("trySetupData: Not ready for data: pdpslotAvailable=").append(flag8).append(" dataState=");
        String s15;
        StringBuilder stringbuilder13;
        com.android.internal.telephony.CommandsInterface.RadioState radiostate2;
        StringBuilder stringbuilder14;
        boolean flag11;
        StringBuilder stringbuilder15;
        com.android.internal.telephony.Phone.State state5;
        StringBuilder stringbuilder16;
        String s16;
        int k;
        boolean flag12;
        StringBuilder stringbuilder17;
        boolean flag13;
        StringBuilder stringbuilder18;
        boolean flag14;
        StringBuilder stringbuilder19;
        boolean flag15;
        StringBuilder stringbuilder20;
        int l;
        StringBuilder stringbuilder21;
        com.android.internal.telephony.HandoverTracker.State state6;
        StringBuilder stringbuilder22;
        boolean flag16;
        StringBuilder stringbuilder23;
        boolean flag17;
        StringBuilder stringbuilder24;
        boolean flag18;
        String s17;
        if(gsmdataconnection == null)
            s15 = "NA";
        else
            s15 = gsmdataconnection.getStateAsString();
        stringbuilder13 = stringbuilder12.append(s15).append(" radioState=");
        radiostate2 = phone.mCM.getRadioState();
        stringbuilder14 = stringbuilder13.append(radiostate2).append(" sim=");
        flag11 = mCdmaPhone.mRuimRecords.getRecordsLoaded();
        stringbuilder15 = stringbuilder14.append(flag11).append(" phoneState=");
        state5 = phone.getState();
        stringbuilder16 = stringbuilder15.append(state5).append(" dataEnabled=");
        s16 = mCurrentRequestedApnType;
        k = apnTypeToId(s16);
        flag12 = isEnabled(k);
        stringbuilder17 = stringbuilder16.append(flag12).append(" roaming=").append(flag6).append(" dataOnRoamingEnable=");
        flag13 = getDataOnRoamingEnabled();
        stringbuilder18 = stringbuilder17.append(flag13).append(" desiredPowerState=").append(flag7).append(" MasterDataEnabled=");
        flag14 = mMasterDataEnabled;
        stringbuilder19 = stringbuilder18.append(flag14).append(" selectionMode=").append(s8).append(" mIsEhrpdSyncDone= ");
        flag15 = mIsEhrpdSyncDone;
        stringbuilder20 = stringbuilder19.append(flag15).append(" eHRPDDataState(in service = 0, out service = 1) =");
        l = mCdmaPhone.mSST.getCurrentEhrpdDataConnectionState();
        stringbuilder21 = stringbuilder20.append(l).append(" isDefaultEsn=").append(flag9).append(" handoverState=");
        state6 = mHandoverTracker.getState();
        stringbuilder22 = stringbuilder21.append(state6).append(" isUSimRecordsLoaded");
        flag16 = mIsSimRecordsLoaded;
        stringbuilder23 = stringbuilder22.append(flag16).append(" mIsImsEnabled=");
        flag17 = mIsImsEnabled;
        stringbuilder24 = stringbuilder23.append(flag17).append(" mIsAdminEnabled=");
        flag18 = mIsAdminEnabled;
        s17 = stringbuilder24.append(flag18).toString();
        log(s17);
        flag = false;
        continue; /* Loop/switch isn't completed */
_L2:
        flag = trySetupData(s);
        if(true) goto _L6; else goto _L5
_L5:
    }

    protected static final int APN_DELAY_MILLIS = 5000;
    static final String APN_ID = "apn_id";
    protected static final String EHRPD_SPECIFIC_DATA_RETRY_CONFIG = "max_retries=infinite,10000,10000,60000,120000,240000,480000,900000";
    protected static final int FORCE_TRY_CDMA_TIMEOUT = 30000;
    private static final String INTENT_RECONNECT_ALARM_EXTRA_REASON_EHRPD = "com.android.internal.telephony.cdma.ehrpd-reason";
    private static final String INTENT_RECONNECT_ALARM_EXTRA_TYPE_EHRPD = "com.android.internal.telephony.cdma.ehrpd-reqtype";
    protected static final int NEXT_PDN_CONNECTION_DELAY = 3000;
    protected static final int NEXT_PDN_RECONNECTION_DELAY = 1000;
    private static final int PDP_CONNECTION_POOL_SIZE = 4;
    static final Uri PREFERAPN_URI = Uri.parse("content://telephony/carriers/preferapn");
    private final String INTENT_RECONNECT_ALARM_EHRPD = "com.android.internal.telephony.ehrpd-reconnect.";
    protected final String LOG_TAG = "eCDMA";
    ArrayList allApns;
    private ApnChangeObserver apnObserver;
    protected boolean bExplicitStateSet;
    private ArrayList backup_allApns;
    protected boolean canSetPreferApn;
    private int cur_cdmaState;
    protected String eventAssociatedApnName;
    private long inactivityPeriod;
    private boolean isOnDemandEnable;
    private boolean legacyToEhrpd;
    private ApnSetting mActiveApn;
    protected ApnSetting mActiveApns[];
    private CdmaDataConnection mActiveDataConnection;
    protected GsmDataConnection mActiveDefEpsBearer;
    protected GsmDataConnection mActiveDefEpsBearers[];
    private GsmDataConnection mActivePdp;
    protected String mAttachApnType;
    private CDMAPhone mCdmaPhone;
    protected int mCidActive[];
    private int mCleanupCount;
    private Context mContext;
    private String mCurrentRequestedApnType;
    protected GsmDataConnection mCurrentdefEpsBearer;
    GsmDataConnection mDefEps;
    BroadcastReceiver mEhrpdIntentReceiver;
    private Runnable mEhrpdPollNetStat;
    private HandoverTracker mHandoverTracker;
    boolean mImsTestMode;
    HashMap mInactivityTimerList;
    private boolean mIsAdminEnabled;
    private boolean mIsApnActive;
    private boolean mIsEhrpdSyncDone;
    private boolean mIsImsEnabled;
    boolean mIsSimRecordsLoaded;
    private boolean mIsSimSupportMultiPdp;
    private int mNetworkMode;
    private int mPdpResetCount;
    private Map mPendingIpv6DataCallList;
    private ArrayList mPendingRequestedApns;
    private HashMap mReconnectIntentEhrpd;
    private boolean mReregisterOnReconnectFailure;
    private ContentResolver mResolver;
    protected RetryManager mRetryEhrpd[];
    SIMRecords mSimRecords;
    HashMap oldList;
    private long oldPollTime;
    private ArrayList pdpList;
    protected ApnSetting preferredApn;
    boolean sDataAllowed;
    protected ArrayList waitingApns;







/*
    static long access$1102(eCdmaDataConnectionTracker ecdmadataconnectiontracker, long l)
    {
        ecdmadataconnectiontracker.inactivityPeriod = l;
        return l;
    }

*/


/*
    static int access$1202(eCdmaDataConnectionTracker ecdmadataconnectiontracker, int i)
    {
        ecdmadataconnectiontracker.netStatPollPeriod = i;
        return i;
    }

*/





/*
    static int access$1602(eCdmaDataConnectionTracker ecdmadataconnectiontracker, int i)
    {
        ecdmadataconnectiontracker.netStatPollPeriod = i;
        return i;
    }

*/







/*
    static long access$2102(eCdmaDataConnectionTracker ecdmadataconnectiontracker, long l)
    {
        ecdmadataconnectiontracker.txPkts = l;
        return l;
    }

*/


/*
    static long access$2202(eCdmaDataConnectionTracker ecdmadataconnectiontracker, long l)
    {
        ecdmadataconnectiontracker.rxPkts = l;
        return l;
    }

*/





/*
    static long access$2602(eCdmaDataConnectionTracker ecdmadataconnectiontracker, long l)
    {
        ecdmadataconnectiontracker.sentSinceLastRecv = l;
        return l;
    }

*/



/*
    static long access$2814(eCdmaDataConnectionTracker ecdmadataconnectiontracker, long l)
    {
        long l1 = ecdmadataconnectiontracker.sentSinceLastRecv + l;
        ecdmadataconnectiontracker.sentSinceLastRecv = l1;
        return l1;
    }

*/


/*
    static long access$2902(eCdmaDataConnectionTracker ecdmadataconnectiontracker, long l)
    {
        ecdmadataconnectiontracker.sentSinceLastRecv = l;
        return l;
    }

*/



/*
    static long access$3002(eCdmaDataConnectionTracker ecdmadataconnectiontracker, long l)
    {
        ecdmadataconnectiontracker.sentSinceLastRecv = l;
        return l;
    }

*/




/*
    static long access$3302(eCdmaDataConnectionTracker ecdmadataconnectiontracker, long l)
    {
        ecdmadataconnectiontracker.sentSinceLastRecv = l;
        return l;
    }

*/





/*
    static com.android.internal.telephony.DataConnectionTracker.Activity access$3702(eCdmaDataConnectionTracker ecdmadataconnectiontracker, com.android.internal.telephony.DataConnectionTracker.Activity activity)
    {
        ecdmadataconnectiontracker.activity = activity;
        return activity;
    }

*/









/*
    static int access$4408(eCdmaDataConnectionTracker ecdmadataconnectiontracker)
    {
        int i = ecdmadataconnectiontracker.mNoRouteCount;
        int j = i + 1;
        ecdmadataconnectiontracker.mNoRouteCount = j;
        return i;
    }

*/


/*
    static int access$4502(eCdmaDataConnectionTracker ecdmadataconnectiontracker, int i)
    {
        ecdmadataconnectiontracker.mNoRouteCount = i;
        return i;
    }

*/


/*
    static int access$4602(eCdmaDataConnectionTracker ecdmadataconnectiontracker, int i)
    {
        ecdmadataconnectiontracker.mNoRouteCount = i;
        return i;
    }

*/


/*
    static int access$4702(eCdmaDataConnectionTracker ecdmadataconnectiontracker, int i)
    {
        ecdmadataconnectiontracker.netStatPollPeriod = i;
        return i;
    }

*/


/*
    static int access$4802(eCdmaDataConnectionTracker ecdmadataconnectiontracker, int i)
    {
        ecdmadataconnectiontracker.netStatPollPeriod = i;
        return i;
    }

*/




/*
    static int access$502(eCdmaDataConnectionTracker ecdmadataconnectiontracker, int i)
    {
        ecdmadataconnectiontracker.mPdpResetCount = i;
        return i;
    }

*/





}
