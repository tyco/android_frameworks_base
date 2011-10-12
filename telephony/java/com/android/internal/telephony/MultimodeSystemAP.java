// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   MultimodeSystemAP.java

package com.android.internal.telephony;

import Lcom.android.internal.telephony.MultimodeSystemAP;;
import android.content.*;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

// Referenced classes of package com.android.internal.telephony:
//            MultiModeInterface

public class MultimodeSystemAP extends Handler
{

    public MultimodeSystemAP()
    {
        mMMSMode = null;
        mContext = null;
        BroadcastReceiver broadcastreceiver = new BroadcastReceiver() {

            public void onReceive(Context context, Intent intent)
            {
                String s = intent.getAction();
                if ("android.intent.action.MULTI_MODE_CHANGED ".equals(s)) {
                    String s1 = intent.getStringExtra("MULTI_MODE");
                    if (s1.equals("LTE")) {
                        mMMSMode = "LTE";
                    }
                    if (s1.equals("CDMA")) {
                        mMMSMode = "CDMA";
                    }
                    if (s1.equals("GLOBAL")) {
                        mMMSMode = "GLOBAL";
                    }
                }
                mMultiModeCB.NotifyMultimodechange(mMMSMode);
                return;
            }
        };
        mBroadcastReceiver = broadcastreceiver;
    }

    public MultimodeSystemAP(Context context, MultiModeInterface multimodeinterface)
    {
        mMMSMode = null;
        mContext = null;
        //BroadcastReceiver broadcastreceiver = new BroadcastReceiver();
        //mBroadcastReceiver = broadcastreceiver;
        mContext = context;
        IntentFilter intentfilter = new IntentFilter();
        intentfilter.addAction("android.intent.action.MULTI_MODE_CHANGED ");
        BroadcastReceiver broadcastreceiver1 = mBroadcastReceiver;
        Intent intent = context.registerReceiver(broadcastreceiver1, intentfilter);
        mMultiModeCB = multimodeinterface;
    }

    private void HandleUICCResponse()
    {
    }

    private void logd(String s)
    {
        String s1 = (new StringBuilder()).append("[PhoneProxy] ").append(s).toString();
        int i = Log.d("PHONE", s1);
    }

    private void loge(String s)
    {
        String s1 = (new StringBuilder()).append("[PhoneProxy] ").append(s).toString();
        int i = Log.e("PHONE", s1);
    }

    private void logv(String s)
    {
        String s1 = (new StringBuilder()).append("[PhoneProxy] ").append(s).toString();
        int i = Log.v("PHONE", s1);
    }

    private void logw(String s)
    {
        String s1 = (new StringBuilder()).append("[PhoneProxy] ").append(s).toString();
        int i = Log.w("PHONE", s1);
    }

    public String getModeType()
    {
        return android.provider.Settings.System.getString(mContext.getContentResolver(), "mode_type");
    }

    public void handleMessage(Message message)
    {
        switch(message.what)
        {
        default:
            StringBuilder stringbuilder = (new StringBuilder()).append("Error! This handler was not registered for this message type. Message: ");
            int i = message.what;
            String s = stringbuilder.append(i).toString();
            int j = Log.e("PHONE", s);
            // fall through

        case 1: // '\001'
            return;
        }
    }

    public String selectedMode()
    {
        return mMMSMode;
    }

    private static final int EVENT_RADIO_TECHNOLOGY_CHANGED = 1;
    private static final String LOG_TAG = "PHONE";
    private BroadcastReceiver mBroadcastReceiver;
    Context mContext;
    private String mMMSMode;
    MultiModeInterface mMultiModeCB;



/*
    static String access$002(MultimodeSystemAP multimodesystemap, String s)
    {
        multimodesystemap.mMMSMode = s;
        return s;
    }

*/
}
