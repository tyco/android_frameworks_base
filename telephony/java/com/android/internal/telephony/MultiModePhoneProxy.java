// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   MultiModePhoneProxy.java

package com.android.internal.telephony;

import android.app.ActivityManagerNative;
import android.content.*;
import android.os.*;
import android.telephony.*;
import android.util.EventLog;
import android.util.Log;
import com.android.internal.telephony.cdma.CDMAPhone;
import com.android.internal.telephony.gsm.GSMPhone;
import com.android.internal.telephony.gsm.NetworkInfo;
import com.android.internal.telephony.gsm.SIMRecords;
import com.android.internal.telephony.ims.sms.IMSICCSmsInterfaceManager;
import java.io.*;
import java.util.List;

// Referenced classes of package com.android.internal.telephony:
//            PhoneProxy, MultiModeInterface, MultimodeSystemAP, PhoneBase, 
//            CommandsInterface, IccCard, DataConnectionTracker, Phone, 
//            HandoverTracker, CommandException, IccSmsInterfaceManager

public class MultiModePhoneProxy extends PhoneProxy
    implements MultiModeInterface
{

    public MultiModePhoneProxy(Phone phone, Phone phone1, Context context)
    {
        super(phone);
        mPendingPreferredNetworkCnt = 0;
        oldBatteryPlugStatus = -1;
        oldBatteryLevel = -1;
        Handler handler = new Handler() {

            public void handleMessage(Message message)
            {
                switch(message.what)
                {
                case 34: // '"'
                default:
                    return;

                case 33: // '!'
                    if(mPendingPreferredNetworkCnt != 0) {
                        return;
                    } else {
                        AsyncResult asyncresult = (AsyncResult)message.obj;
                        Message message1 = (Message)asyncresult.userObj;
                        Object obj = asyncresult.result;
                        Throwable throwable = asyncresult.exception;
                        AsyncResult asyncresult1 = AsyncResult.forMessage(message1, obj, throwable);
                        message1.sendToTarget();
                        DataConnectionTracker dataconnectiontracker1 = mLTEPhone.mDataConnection;
                        Message message2 = dataconnectiontracker1.obtainMessage(62);
                        boolean flag = dataconnectiontracker1.sendMessage(message2);
                        return;
                    }

                case 35: // '#'
                    int k = Log.d("MultiModePhoneProxy", "LTE_RESET_DONE");
                    return;

                case 36: // '$'
                    int l = Log.d("MultiModePhoneProxy", "VIA_RESET_DONE");
                    return;
                }
            }

            final MultiModePhoneProxy this$0;

            
            {
                this$0 = MultiModePhoneProxy.this;
                super();
            }
        };
        mHandler = handler;
        BroadcastReceiver broadcastreceiver = new BroadcastReceiver() {

            public void onReceive(Context context1, Intent intent1)
            {
                if(intent1.getAction().equals("android.intent.action.BATTERY_CHANGED"))
                {
                    sendBatteryInfo(intent1);
                    return;
                }
                if(!intent1.getAction().equals("android.intent.action.RILD_CRASH"))
                {
                    return;
                } else
                {
                    int j = Log.d("MultiModePhoneProxy", "RILD msg");
                    refreshRild(intent1);
                    return;
                }
            }

            final MultiModePhoneProxy this$0;

            
            {
                this$0 = MultiModePhoneProxy.this;
                super();
            }
        };
        mIntentReceiver = broadcastreceiver;
        PhoneStateListener phonestatelistener = new PhoneStateListener() {

            public void onDataConnectionStateChanged(int j, int k)
            {
                int l = meCDMAPhone.getDataRegistrationState();
                int i1 = mLTEPhone.getDataRegistrationState();
                MultiModePhoneProxy multimodephoneproxy = MultiModePhoneProxy.this;
                String s = (new StringBuilder()).append("MultiModePhoneProxy: cdmaDataState=").append(l).append(" lteDataState=").append(i1).toString();
                multimodephoneproxy.logd(s);
                if(i1 == 0)
                {
                    MultiModePhoneProxy multimodephoneproxy1 = MultiModePhoneProxy.this;
                    GSMPhone gsmphone1 = mLTEPhone;
                    Phone phone2 = multimodephoneproxy1.mDataPhone = gsmphone1;
                } else
                if(l == 0)
                {
                    MultiModePhoneProxy multimodephoneproxy2 = MultiModePhoneProxy.this;
                    CDMAPhone cdmaphone1 = meCDMAPhone;
                    Phone phone3 = multimodephoneproxy2.mDataPhone = cdmaphone1;
                } else
                {
                    logd("MultiModePhoneProxy: both cdma and lte are not in service");
                }
                if(j != 2)
                    return;
                else
                    return;
            }

            public void onServiceStateChanged(ServiceState servicestate)
            {
                MultiModePhoneProxy multimodephoneproxy = MultiModePhoneProxy.this;
                int j = servicestate.getRadioTechnology();
                int k = multimodephoneproxy.mRadioTechnology = j;
                TelephonyManager telephonymanager1 = TelephonyManager.getDefault();
                String s = telephonymanager1.getNetworkTypeName();
                StringBuilder stringbuilder = (new StringBuilder()).append("Setting TelephonyProperties.PROPERTY_DATA_NETWORK_TYPE: ");
                int l = telephonymanager1.getNetworkType();
                String s1 = stringbuilder.append(l).append(" / ").append(s).toString();
                int i1 = Log.d("MultiModePhoneProxy", s1);
                SystemProperties.set("gsm.network.type", s);
            }

            final MultiModePhoneProxy this$0;

            
            {
                this$0 = MultiModePhoneProxy.this;
                super();
            }
        };
        mPhoneStateListener = phonestatelistener;
        setLtePhone(phone1);
        mActivePhone = phone;
        CDMAPhone cdmaphone = (CDMAPhone)phone;
        meCDMAPhone = cdmaphone;
        GSMPhone gsmphone = (GSMPhone)phone1;
        mLTEPhone = gsmphone;
        MultimodeSystemAP multimodesystemap = new MultimodeSystemAP(context, this);
        mmsAp = multimodesystemap;
        CommandsInterface commandsinterface = ((PhoneBase)phone).mCM;
        mCommandsInterfaceCDMA = commandsinterface;
        CommandsInterface commandsinterface1 = ((PhoneBase)phone1).mCM;
        mCommandsInterfaceGSM = commandsinterface1;
        mCommandsInterfaceGSM.registerForRadioTechnologyChanged(this, 1, null);
        mCommandsInterfaceGSM.registerForSIMReady(this, 28, null);
        mCommandsInterfaceCDMA.registerForRUIMReady(this, 27, null);
        int i = handleActivePhoneSelection();
        IccCard icccard = mLTEPhone.getIccCard();
        PhoneBase phonebase = (PhoneBase)phone1;
        PhoneBase phonebase1 = (PhoneBase)phone;
        icccard.setDualPhones(phonebase, phonebase1);
        IccCard icccard1 = meCDMAPhone.getIccCard();
        PhoneBase phonebase2 = (PhoneBase)phone1;
        PhoneBase phonebase3 = (PhoneBase)phone;
        icccard1.setDualPhones(phonebase2, phonebase3);
        mCommandsInterfaceCDMA.registerForNVReady(this, 29, null);
        TelephonyManager telephonymanager = (TelephonyManager)context.getSystemService("phone");
        PhoneStateListener phonestatelistener1 = mPhoneStateListener;
        telephonymanager.listen(phonestatelistener1, 65);
        meCDMAPhone.mDataConnection.registerForHandoverInitiated(this, 30, null);
        mLTEPhone.registerForeHRPDHOfailResumeLTE(this, 31, null);
        meCDMAPhone.registerForLTEHOfailResumeeHRPD(this, 32, null);
        SIMRecords simrecords = mLTEPhone.mSIMRecords;
        DataConnectionTracker dataconnectiontracker = meCDMAPhone.mDataConnection;
        SIMRecords simrecords1 = mLTEPhone.mSIMRecords;
        simrecords.registerForRecordsLoaded(dataconnectiontracker, 58, simrecords1);
        mCommandsInterfaceGSM.setOnCpCrash(this, 34, null);
        mCommandsInterfaceCDMA.setOnCpCrash(this, 34, null);
        IntentFilter intentfilter = new IntentFilter();
        intentfilter.addAction("android.intent.action.BATTERY_CHANGED");
        intentfilter.addAction("android.intent.action.RILD_CRASH");
        BroadcastReceiver broadcastreceiver1 = mIntentReceiver;
        Intent intent = context.registerReceiver(broadcastreceiver1, intentfilter);
    }

    private void SelectActivePhone(String s)
    {
        String s1 = (new StringBuilder()).append("bCastReceived").append(s).toString();
        loge(s1);
        loge("ACtive Phone call from SelectActivePhone :");
        int i = handleActivePhoneSelection();
        meCDMAPhone.setPreferredNetworkType(i, null);
        mLTEPhone.setPreferredNetworkType(i, null);
    }

    private int handleActivePhoneSelection() {
        int i = getNetworkSelectionMode();
        switch(i) {
            case 7:
                mActivePhone = meCDMAPhone;
                mDataPhone = meCDMAPhone;
                loge("ACtive Phone in MMproxy is meCDMAPhone");
                setActivePhone(mActivePhone);
            case 8:
                mActivePhone = mLTEPhone;
                mDataPhone = mLTEPhone;
                loge("ACtive Phone in MMproxy is mLTEPhone:");
                setActivePhone(mActivePhone);
            case 9:
                mActivePhone = meCDMAPhone;
                mDataPhone = meCDMAPhone;
                loge("ACtive Phone in MMproxy is meCDMAPhone");
                setActivePhone(mActivePhone);
            default:
                loge("Not supported Network Mode");
                setActivePhone(phone);
        }
        return i;
    }

    private boolean isSMSFormat3GPP2()
    {
        String s = (String)IMSICCSmsInterfaceManager.readSmsSetting("smsformat");
        boolean flag;
        if("3GPP".equalsIgnoreCase(s))
            flag = false;
        else
            flag = true;
        return flag;
    }

    private void logd(String s)
    {
        String s1 = (new StringBuilder()).append("[PhoneProxy] ").append(s).toString();
        int i = Log.d("MultiModePhoneProxy", s1);
    }

    private void loge(String s)
    {
        String s1 = (new StringBuilder()).append("[PhoneProxy] ").append(s).toString();
        int i = Log.e("MultiModePhoneProxy", s1);
    }

    private int mapDataType(ServiceState servicestate)
    {
        int i;
        int j;
        i = servicestate.getRadioTechnology();
        j = 0;
        switch (j) {
            case 6:
                j = 4;
            case 7:
                j = 3;
            case 8:
                j = 3;
            case 13:
                j = 4;
            case 14:
                j = 2;
            default:
                loge("Not supported Technology");
        }
        return j;
    }

    private void refreshRild(Intent intent)
    {
        if(intent.getIntExtra("PHONE_TYPE", 0) == 1)
        {
            mCommandsInterfaceCDMA.setRadioPower(false, null);
            return;
        }
        if(intent.getIntExtra("PHONE_TYPE", 0) != 2)
        {
            return;
        } else
        {
            mCommandsInterfaceGSM.setRadioPower(false, null);
            return;
        }
    }

    private void sendBatteryInfo(Intent intent)
    {
        int j;
        DataOutputStream dataoutputstream;
        int i = intent.getIntExtra("plugged", 0);
        j = intent.getIntExtra("level", 100);
        StringBuilder stringbuilder = (new StringBuilder()).append("BATTERY_CHANGED: plugged ").append(i).append("  level ").append(j).append(" old_level ");
        int k = oldBatteryLevel;
        String s = stringbuilder.append(k).toString();
        int l = Log.d("MultiModePhoneProxy", s);
        int i1 = oldBatteryPlugStatus;
        boolean flag;
        ByteArrayOutputStream bytearrayoutputstream;
        byte byte0;
        CommandsInterface commandsinterface;
        byte abyte0[];
        if(i != i1)
            flag = true;
        else
            flag = false;
        if (flag) {
            //L1
            bytearrayoutputstream = new ByteArrayOutputStream();
            dataoutputstream = new DataOutputStream(bytearrayoutputstream);
            boolean flag1;
            if(i == 1)
                flag1 = true;
            else
                flag1 = false;
            byte0 = 23;
            dataoutputstream.writeByte(byte0);
            dataoutputstream.writeByte(1);
            dataoutputstream.writeShort(5);
            if (flag1) {
                //L3
                dataoutputstream.writeByte(1);
                commandsinterface = mCommandsInterfaceCDMA;
                abyte0 = bytearrayoutputstream.toByteArray();
                commandsinterface.invokeOemRilRequestRaw(abyte0, null);
                try {
                    dataoutputstream.close();
                } catch(IOException ioexception1) { }
                oldBatteryPlugStatus = i;
                int j1;
                IOException ioexception;
                int k1;
                Exception exception;
                if(oldBatteryLevel < 0)
                {
                    oldBatteryLevel = j;
                    return;
                }
                if(oldBatteryLevel >= 5 || j < 5)
                    return;
                bytearrayoutputstream1 = new ByteArrayOutputStream();
                dataoutputstream = new DataOutputStream(bytearrayoutputstream1);
                l1 = Log.d("MultiModePhoneProxy", "Battery becomes to normal level.");
                byte1 = 23;
                dataoutputstream.writeByte(byte1);
                dataoutputstream.writeByte(2);
                dataoutputstream.writeShort(5);
                dataoutputstream.writeByte(j);
                commandsinterface1 = mCommandsInterfaceGSM;
                abyte1 = bytearrayoutputstream1.toByteArray();
                commandsinterface1.invokeOemRilRequestRaw(abyte1, null);
                try {
                    dataoutputstream.close();
                }
                catch(IOException ioexception3) { }
                oldBatteryLevel = j;
                return;
            } else {
                //L4
                j1 = 0;
                dataoutputstream.writeByte(j1);
                oldBatteryPlugStatus = i;
                int j1;
                IOException ioexception;
                int k1;
                Exception exception;
                if(oldBatteryLevel < 0) {
                    oldBatteryLevel = j;
                    return;
                }
                if(oldBatteryLevel >= 5 || j < 5)
                    return;
                bytearrayoutputstream1 = new ByteArrayOutputStream();
                dataoutputstream = new DataOutputStream(bytearrayoutputstream1);
                l1 = Log.d("MultiModePhoneProxy", "Battery becomes to normal level.");
                byte1 = 23;
                dataoutputstream.writeByte(byte1);
                dataoutputstream.writeByte(2);
                dataoutputstream.writeShort(5);
                dataoutputstream.writeByte(j);
                commandsinterface1 = mCommandsInterfaceGSM;
                abyte1 = bytearrayoutputstream1.toByteArray();
                commandsinterface1.invokeOemRilRequestRaw(abyte1, null);
                try {
                    dataoutputstream.close();
                }
                // Misplaced declaration of an exception variable
                catch(IOException ioexception3) { }
                oldBatteryLevel = j;
                return;
            }
        } else {
            oldBatteryPlugStatus = i;
            int j1;
            IOException ioexception;
            int k1;
            Exception exception;
            if(oldBatteryLevel < 0) {
                oldBatteryLevel = j;
                return;
            }
            if(oldBatteryLevel >= 5 || j < 5)
                return;
            bytearrayoutputstream1 = new ByteArrayOutputStream();
            dataoutputstream = new DataOutputStream(bytearrayoutputstream1);
            l1 = Log.d("MultiModePhoneProxy", "Battery becomes to normal level.");
            byte1 = 23;
            dataoutputstream.writeByte(byte1);
            dataoutputstream.writeByte(2);
            dataoutputstream.writeShort(5);
            dataoutputstream.writeByte(j);
            commandsinterface1 = mCommandsInterfaceGSM;
            abyte1 = bytearrayoutputstream1.toByteArray();
            commandsinterface1.invokeOemRilRequestRaw(abyte1, null);
            try {
                dataoutputstream.close();
            }
            // Misplaced declaration of an exception variable
            catch(IOException ioexception3) { }
            oldBatteryLevel = j;
            return;
        }
    }

    public boolean IsEmergencyCallingSupported()
    {
        boolean flag;
        if(getNetworkSelectionMode() == 8)
            flag = false;
        else
            flag = true;
        return flag;
    }

    public void NotifyMultimodechange(String s)
    {
        SelectActivePhone(s);
    }

    public int disableApnType(String s)
    {
        return mDataPhone.disableApnType(s);
    }

    public boolean disableDataConnectivity()
    {
        return mDataPhone.disableDataConnectivity();
    }

    public int enableApnType(String s)
    {
        return mDataPhone.enableApnType(s);
    }

    public boolean enableDataConnectivity()
    {
        return mDataPhone.enableDataConnectivity();
    }

    public boolean explicitDetach(int i, int j)
    {
        return mDataPhone.explicitDetach(i, j);
    }

    public String getActiveApn()
    {
        return mDataPhone.getActiveApn();
    }

    public String[] getActiveApnTypes()
    {
        return mDataPhone.getActiveApnTypes();
    }

    public Phone.DataState getActiveDataConnectionState()
    {
        return getDataConnectionState();
    }

    public ServiceState getActiveServiceState()
    {
        return mDataPhone.getServiceState();
    }

    public boolean getAutoConnectEnable()
    {
        return mDataPhone.getAutoConnectEnable();
    }

    public void getAvailableNetworks(Message message)
    {
        mLTEPhone.getAvailableNetworks(message);
    }

    public CDMAPhone getCdmaPhone()
    {
        return meCDMAPhone;
    }

    public List getCurrentDataConnectionList()
    {
        return mDataPhone.getCurrentDataConnectionList();
    }

    public Phone.DataActivityState getDataActivityState()
    {
        return mDataPhone.getDataActivityState();
    }

    public long getDataConnectedTime()
    {
        return mDataPhone.getDataConnectedTime();
    }

    public Phone.DataState getDataConnectionState()
    {
        return mDataPhone.getDataConnectionState();
    }

    public Phone getDataPhone()
    {
        Object obj;
        if(mRadioTechnology == 13)
            obj = mLTEPhone;
        else
            obj = meCDMAPhone;
        return ((Phone) (obj));
    }

    public int getDataPhoneType()
    {
        ServiceState servicestate = mActivePhone.getServiceState();
        int i = mapDataType(servicestate);
        mDataPhoneType = i;
        return mDataPhoneType;
    }

    public void getDataProfile(Message message)
    {
        mDataPhone.getDataProfile(message);
    }

    public boolean getDataRoamingEnabled()
    {
        return mDataPhone.getDataRoamingEnabled();
    }

    public String[] getDnsServers(String s)
    {
        return mDataPhone.getDnsServers(s);
    }

    public String getGateway(String s)
    {
        return mDataPhone.getGateway(s);
    }

    public GSMPhone getGsmPhone()
    {
        return mLTEPhone;
    }

    public String getIccSerialNumber()
    {
        int i = Log.d("MultiModePhoneProxy", "MultiModePhoneProxy::getIccSerialNumber()");
        return mLTEPhone.getIccSerialNumber();
    }

    public IccSmsInterfaceManager getIccSmsInterfaceManager()
    {
        IccSmsInterfaceManager iccsmsinterfacemanager;
        if(isSMSFormat3GPP2())
            iccsmsinterfacemanager = meCDMAPhone.getIccSmsInterfaceManager();
        else
            iccsmsinterfacemanager = mLTEPhone.getIccSmsInterfaceManager();
        return iccsmsinterfacemanager;
    }

    public String getInterfaceName(String s)
    {
        return mDataPhone.getInterfaceName(s);
    }

    public String getIpAddress(String s)
    {
        return mDataPhone.getIpAddress(s);
    }

    public int getIpAddressType(String s)
    {
        return mDataPhone.getIpAddressType(s);
    }

    public String getNetMask(String s)
    {
        return mDataPhone.getNetMask(s);
    }

    public int getNetworkSelectionMode()
    {
        byte byte0;
        byte0 = -1;
        String s = mmsAp.getModeType();
        setNetworkSelectionMode(s);
        if (mNetworkSelectionMode.equals("LTE")) {
            byte0 = 8;
        } else {
            if(mNetworkSelectionMode.equals("CDMA"))
                byte0 = 9;
            else
            if(mNetworkSelectionMode.equals("GLOBAL"))
                byte0 = 7;
        }
        return byte0;
    }

    public int getRadioTechnology()
    {
        return mRadioTechnology;
    }

    public boolean getSMSavailable()
    {
        boolean flag;
        if(isSMSFormat3GPP2())
            flag = meCDMAPhone.getSMSavailable();
        else
            flag = mLTEPhone.getSMSavailable();
        return flag;
    }

    public void getSmscAddress(Message message)
    {
        if(isSMSFormat3GPP2())
        {
            meCDMAPhone.getSmscAddress(message);
            return;
        } else
        {
            mLTEPhone.getSmscAddress(message);
            return;
        }
    }

    public String getSubscriberId()
    {
        int i = Log.d("MultiModePhoneProxy", "MultiModePhoneProxy::getSubscriberId()");
        return mLTEPhone.getSubscriberId();
    }

    public long getTraficConnectedTime()
    {
        return mDataPhone.getTraficConnectedTime();
    }

    public long getTraficRxBytes()
    {
        return mDataPhone.getTraficRxBytes();
    }

    public long getTraficStartTime()
    {
        return mDataPhone.getTraficStartTime();
    }

    public long getTraficTotalRxBytes()
    {
        return mDataPhone.getTraficTotalRxBytes();
    }

    public long getTraficTotalTxBytes()
    {
        return mDataPhone.getTraficTotalTxBytes();
    }

    public long getTraficTxBytes()
    {
        return mDataPhone.getTraficTxBytes();
    }

    public boolean getVPNPassthroughEnable()
    {
        return mDataPhone.getVPNPassthroughEnable();
    }

    public void handleMessage(Message message)
    {
        AsyncResult asyncresult;
        switch(message.what)
        {
        default:
            StringBuilder stringbuilder = (new StringBuilder()).append("calling super[PhoneProxy.java] ");
            int i = message.what;
            String s = stringbuilder.append(i).toString();
            int j = Log.e("MultiModePhoneProxy", s);
            super.handleMessage(message);
            return;

        case 28: // '\034'
            CommandsInterface commandsinterface = mCommandsInterfaceGSM;
            int k = handleActivePhoneSelection();
            commandsinterface.setPreferredNetworkType(k, null);
            return;

        case 27: // '\033'
            CommandsInterface commandsinterface1 = mCommandsInterfaceCDMA;
            int l = getNetworkSelectionMode();
            commandsinterface1.setPreferredNetworkType(l, null);
            return;

        case 29: // '\035'
            CommandsInterface commandsinterface2 = mCommandsInterfaceCDMA;
            int i1 = getNetworkSelectionMode();
            commandsinterface2.setPreferredNetworkType(i1, null);
            return;

        case 30: // '\036'
            mCommandsInterfaceGSM.processLTEHandover();
            return;

        case 1: // '\001'
            String s1 = ((PhoneBase)mActivePhone).getPhoneName();
            mOutgoingPhone = s1;
            StringBuilder stringbuilder1 = (new StringBuilder()).append("Switching phone from ");
            String s2 = mOutgoingPhone;
            StringBuilder stringbuilder2 = stringbuilder1.append(s2).append("Phone to ");
            String s3;
            String s4;
            Intent intent;
            String s5;
            Intent intent1;
            if(mOutgoingPhone.equals("GSM"))
                s3 = "CDMAPhone";
            else
                s3 = "GSMPhone";
            s4 = stringbuilder2.append(s3).toString();
            logd(s4);
            intent = new Intent("android.intent.action.RADIO_TECHNOLOGY");
            s5 = mActivePhone.getPhoneName();
            intent1 = intent.putExtra("phoneName", s5);
            ActivityManagerNative.broadcastStickyIntent(intent, null);
            return;

        case 31: // '\037'
            int j1 = Log.d("MultiModePhoneProxy", "[MMPhoneProxy] EVENT_EHRPD_FAIL_LTE_RESUME calling onHOCleanupHOAPN ");
            meCDMAPhone.mDataConnection.onHOCleanupHOAPN();
            int k1 = Log.d("MultiModePhoneProxy", "[MMPhoneProxy] returned from onHOCleanupHOAPN");
            return;

        case 32: // ' '
            int l1 = Log.d("MultiModePhoneProxy", "[MMPhoneProxy] EVENT_LTE_FAIL_EHRPD_RESUME calling onHOCleanupHOAPN ");
            mLTEPhone.mDataConnection.onHOCleanupHOAPN();
            int i2 = Log.d("MultiModePhoneProxy", "[MMPhoneProxy] returned from onHOCleanupHOAPN");
            return;

        case 34: // '"'
            int j2 = Log.d("MultiModePhoneProxy", "Radio refresh");
            asyncresult = (AsyncResult)message.obj;
            break;
        }
        String s6;
        String s7;
        if(asyncresult.exception == null)
            s6 = (String)asyncresult.result;
        else
            s6 = "Unknown";
        s7 = SystemProperties.get("ril.nv_rebuild");
        if("1".equals(s7) || "2".equals(s7)) {
            logd("Don't reset Modem while CP is rebuilding NV");
            return;
        } else {
            mLTEPhone.mHOT.resetState();
            sendResetCommand(s6);
            return;
        }
    }

    public boolean isDataConnected()
    {
        return mDataPhone.isDataConnected();
    }

    public boolean isDataConnectivityEnabled()
    {
        return mDataPhone.isDataConnectivityEnabled();
    }

    public boolean isDataConnectivityPossible()
    {
        boolean flag;
        if(mLTEPhone.isDataConnectivityPossible() || meCDMAPhone.isDataConnectivityPossible())
            flag = true;
        else
            flag = false;
        return flag;
    }

    public void notifyDataActivity()
    {
        mDataPhone.notifyDataActivity();
    }

    public void queryCdmaRoamingPreference(Message message)
    {
        meCDMAPhone.queryCdmaRoamingPreference(message);
    }

    public void resetTraficCounter()
    {
        mDataPhone.resetTraficCounter();
    }

    public void resumeDataChannels(Message message)
    {
        mActivePhone.resumeDataChannels(message);
    }

    public void selectNetworkManually(NetworkInfo networkinfo, Message message)
    {
        mLTEPhone.selectNetworkManually(networkinfo, message);
    }

    public void sendPsAttachInfo()
    {
        mDataPhone.sendPsAttachInfo();
    }

    public void sendResetCommand(String s)
    {
        ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
        DataOutputStream dataoutputstream = new DataOutputStream(bytearrayoutputstream);
        int i = s.length() + 4 + 1;
        int j;
        CommandsInterface commandsinterface;
        byte abyte0[];
        Message message;
        CommandsInterface commandsinterface1;
        byte abyte1[];
        Message message1;
        try
        {
            dataoutputstream.writeByte(16);
            dataoutputstream.writeByte(2);
            dataoutputstream.writeShort(i);
            dataoutputstream.writeBytes(s);
            dataoutputstream.writeByte(0);
        }
        catch(IOException ioexception)
        {
            return;
        }
        j = EventLog.writeEvent(0xf4242, s);
        commandsinterface = mCommandsInterfaceGSM;
        abyte0 = bytearrayoutputstream.toByteArray();
        message = mHandler.obtainMessage(35);
        commandsinterface.invokeOemRilRequestRaw(abyte0, message);
        commandsinterface1 = mCommandsInterfaceCDMA;
        abyte1 = bytearrayoutputstream.toByteArray();
        message1 = mHandler.obtainMessage(36);
        commandsinterface1.invokeOemRilRequestRaw(abyte1, message1);
    }

    public void setAutoConnectEnable(boolean flag)
    {
        mDataPhone.setAutoConnectEnable(flag);
    }

    public void setCdmaRoamingPreference(int i, Message message)
    {
        meCDMAPhone.setCdmaRoamingPreference(i, message);
    }

    public boolean setDataConnected(boolean flag)
    {
        return mDataPhone.setDataConnected(flag);
    }

    public void setDataProfile(int i, Message message)
    {
        mDataPhone.setDataProfile(i, message);
    }

    public void setDataRoamingEnabled(boolean flag)
    {
        mDataPhone.setDataRoamingEnabled(flag);
    }

    public void setNetworkSelectionMode(String s)
    {
        mNetworkSelectionMode = s;
    }

    public void setNetworkSelectionModeAutomatic(Message message)
    {
        mLTEPhone.setNetworkSelectionModeAutomatic(message);
    }

    public void setPreferredNetworkType(int i, Message message)
    {
        if(mPendingPreferredNetworkCnt != 0) {
            CommandException.Error error = CommandException.Error.OP_NOT_ALLOWED_BEFORE_REG_NW;
            CommandException commandexception = new CommandException(error);
            AsyncResult asyncresult = AsyncResult.forMessage(message, null, commandexception);
            message.sendToTarget();
            return;
        } else {
            mPendingPreferredNetworkCnt = 2;
            CDMAPhone cdmaphone = meCDMAPhone;
            Message message1 = mHandler.obtainMessage(33, message);
            cdmaphone.setPreferredNetworkType(i, message1);
            GSMPhone gsmphone = mLTEPhone;
            Message message2 = mHandler.obtainMessage(33, message);
            gsmphone.setPreferredNetworkType(i, message2);
            return;
        }
    }

    public void setRadioPower(boolean flag)
    {
        String s = (new StringBuilder()).append("setRadioPower, Power:").append(flag).toString();
        int i = Log.d("MultiModePhoneProxy", s);
        if(mNetworkSelectionMode.equals("GLOBAL")) {
            int j = Log.d("MultiModePhoneProxy", "setRadioPower, GLOBAL Mode");
            mActivePhone.setRadioPower(flag);
            mLTEPhone.setRadioPower(flag);
            return;
        }
        if(mNetworkSelectionMode.equals("LTE")) {
            int k = Log.d("MultiModePhoneProxy", "setRadioPower, LTE Mode");
            mLTEPhone.setRadioPower(flag);
            return;
        }
        if(mNetworkSelectionMode.equals("CDMA")) {
            int l = Log.d("MultiModePhoneProxy", "setRadioPower, CDMA Mode");
            mActivePhone.setRadioPower(flag);
            mLTEPhone.setRadioPower(flag);
            return;
        } else {
            int i1 = Log.e("MultiModePhoneProxy", "setRadioPower, Invalid Mode");
            return;
        }
    }

    public void setSmscAddress(String s, Message message)
    {
        if(isSMSFormat3GPP2())
        {
            meCDMAPhone.setSmscAddress(s, message);
            return;
        } else
        {
            mLTEPhone.setSmscAddress(s, message);
            return;
        }
    }

    public void setUpDedicatedBearer(String s)
    {
        mDataPhone.setUpDedicatedBearer(s);
    }

    public boolean setVPNPassthroughEnable(boolean flag)
    {
        return mDataPhone.setVPNPassthroughEnable(flag);
    }

    public void suspendDataChannels(Message message)
    {
        mActivePhone.suspendDataChannels(message);
    }

    private static final int EVENT_CP_CRASH = 34;
    protected static final int EVENT_CSIM_READY = 27;
    private static final int EVENT_EHRPD_FAIL_LTE_RESUME = 31;
    private static final int EVENT_HANDOVER_FROM_EHRPD_INITIATED = 30;
    private static final int EVENT_LTE_FAIL_EHRPD_RESUME = 32;
    private static final int EVENT_LTE_RESET_DONE = 35;
    protected static final int EVENT_NV_READY = 29;
    private static final int EVENT_RADIO_TECHNOLOGY_CHANGED = 1;
    private static final int EVENT_SET_PREFERRED_NETWORK = 33;
    protected static final int EVENT_USIM_READY = 28;
    private static final int EVENT_VIA_RESET_DONE = 36;
    private static final String LOG_TAG = "MultiModePhoneProxy";
    public static final Object lockForRadioTechnologyChange = new Object();
    static final int preferredCdmaSubscription = 1;
    static final int preferredNetworkMode = 4;
    private Phone mActivePhone;
    private CommandsInterface mCommandsInterfaceCDMA;
    private CommandsInterface mCommandsInterfaceGSM;
    private Phone mDataPhone;
    private int mDataPhoneType;
    private Handler mHandler;
    BroadcastReceiver mIntentReceiver;
    private GSMPhone mLTEPhone;
    private String mOutgoingPhone;
    private String mNetworkSelectionMode;
    private int mPendingPreferredNetworkCnt;
    private PhoneStateListener mPhoneStateListener;
    private int mRadioTechnology;
    private CDMAPhone meCDMAPhone;
    MultimodeSystemAP mmsAp;
    private int oldBatteryLevel;
    private int oldBatteryPlugStatus;




/*
    static int access$010(MultiModePhoneProxy multimodephoneproxy)
    {
        int i = multimodephoneproxy.mPendingPreferredNetworkCnt;
        int j = i - 1;
        multimodephoneproxy.mPendingPreferredNetworkCnt = j;
        return i;
    }

*/





/*
    static int access$402(MultiModePhoneProxy multimodephoneproxy, int i)
    {
        multimodephoneproxy.mRadioTechnology = i;
        return i;
    }

*/




/*
    static Phone access$702(MultiModePhoneProxy multimodephoneproxy, Phone phone)
    {
        multimodephoneproxy.mDataPhone = phone;
        return phone;
    }

*/
}
