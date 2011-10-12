// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   IMSICCSmsInterfaceManager.java

package com.android.internal.telephony.ims.sms;

import android.app.PendingIntent;
import android.content.*;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.*;
import android.util.Log;
import com.android.internal.telephony.IccSmsInterfaceManager;
import com.android.internal.telephony.PhoneBase;
import com.android.internal.telephony.cdma.CDMAPhone;
import com.android.internal.telephony.cdma.RuimSmsInterfaceManager;
import com.android.internal.telephony.gsm.GSMPhone;
import com.android.internal.telephony.gsm.SimSmsInterfaceManager;
import java.util.List;

// Referenced classes of package com.android.internal.telephony.ims.sms:
//            ImsSMSInterface

public class IMSICCSmsInterfaceManager extends IccSmsInterfaceManager
{
    private class SmsSettingsObserver extends ContentObserver
    {

        public void onChange(boolean flag)
        {
            onChange(flag);
            IMSICCSmsInterfaceManager.mSmsFormat = (String)(String)IMSICCSmsInterfaceManager.readSmsSetting("smsformat");
            IMSICCSmsInterfaceManager.mSmsStorageToUICC = (String)(String)IMSICCSmsInterfaceManager.readSmsSetting("sms_storage_to_uicc");
            IMSICCSmsInterfaceManager.mHomeDomainName = (String)IMSICCSmsInterfaceManager.readSmsSetting("HomeDomainName");
            IMSICCSmsInterfaceManager.mSmsSendOnIms = (String)IMSICCSmsInterfaceManager.readSmsSetting("IsSmsOverIms");
            IMSICCSmsInterfaceManager.mSmsSipTimerT1 = Long.getLong(IMSICCSmsInterfaceManager.readSmsSetting("SipT1Timer").toString(), 3L);
            IMSICCSmsInterfaceManager.mSmsSipTimerT2 = Long.getLong(IMSICCSmsInterfaceManager.readSmsSetting("SipT2Timer").toString(), 16L);
            IMSICCSmsInterfaceManager.mSmsSipTimerTF = Long.getLong(IMSICCSmsInterfaceManager.readSmsSetting("SipTFTimer").toString(), 30L);
            String s = (String)IMSICCSmsInterfaceManager.readSmsSetting("smsprefered");
            if(s != null)
                IMSICCSmsInterfaceManager.mSMSprefered = Boolean.valueOf(s).booleanValue();
            IMSICCSmsInterfaceManager imsiccsmsinterfacemanager = IMSICCSmsInterfaceManager.this;
            StringBuilder stringbuilder = (new StringBuilder()).append("updated home domain : ");
            String s1 = IMSICCSmsInterfaceManager.mHomeDomainName;
            String s2 = stringbuilder.append(s1).toString();
            imsiccsmsinterfacemanager.log(s2);
            IMSICCSmsInterfaceManager imsiccsmsinterfacemanager1 = IMSICCSmsInterfaceManager.this;
            StringBuilder stringbuilder1 = (new StringBuilder()).append("updated sms over ims : ");
            String s3 = IMSICCSmsInterfaceManager.mSmsSendOnIms;
            String s4 = stringbuilder1.append(s3).toString();
            imsiccsmsinterfacemanager1.log(s4);
            IMSICCSmsInterfaceManager imsiccsmsinterfacemanager2 = IMSICCSmsInterfaceManager.this;
            StringBuilder stringbuilder2 = (new StringBuilder()).append("updated writeToUicc value : ");
            String s5 = IMSICCSmsInterfaceManager.mSmsStorageToUICC;
            String s6 = stringbuilder2.append(s5).toString();
            imsiccsmsinterfacemanager2.log(s6);
            IMSICCSmsInterfaceManager imsiccsmsinterfacemanager3 = IMSICCSmsInterfaceManager.this;
            StringBuilder stringbuilder3 = (new StringBuilder()).append("updated smsPrefered value  : ");
            boolean flag1 = IMSICCSmsInterfaceManager.mSMSprefered;
            String s7 = stringbuilder3.append(flag1).toString();
            imsiccsmsinterfacemanager3.log(s7);
        }

        public SmsSettingsObserver(Handler handler)
        {
            this$0 = IMSICCSmsInterfaceManager.this;
            super(handler);
        }
    }


    private IMSICCSmsInterfaceManager(PhoneBase phonebase)
    {
        IccSmsInterfaceManager(phonebase);
        mSmsContext = phonebase.getContext();
        Handler handler = new Handler();
        SmsSettingsObserver smssettingsobserver = new SmsSettingsObserver(handler);
        mSmsSettingsObserver = smssettingsobserver;
        StringBuilder stringbuilder = (new StringBuilder()).append("IMSICCSmsInterfaceManager() :mSmsContext :");
        Context context = mSmsContext;
        String s = stringbuilder.append(context).toString();
        int i = Log.e("IMSICCSmsInterfaceManager", s);
    }

    private static void checkIfSmsInterfaceCreated(PhoneBase phonebase)
    {
        if(gsmSmsInterfaceManager == null && phonebase.getPhoneType() == 1)
        {
            GSMPhone gsmphone = (GSMPhone)phonebase;
            gsmSmsInterfaceManager = new SimSmsInterfaceManager(gsmphone);
            int i = Log.e("IMSICCSmsInterfaceManager", "created the gsmSMS interface");
        }
        if(cdmaSmsInterfaceManager != null)
            return;
        if(phonebase.getPhoneType() != 2)
        {
            return;
        } else
        {
            CDMAPhone cdmaphone = (CDMAPhone)phonebase;
            cdmaSmsInterfaceManager = new RuimSmsInterfaceManager(cdmaphone);
            return;
        }
    }

    public static int getCdmaMsgId()
    {
        int i = Integer.valueOf(readSmsSetting("TPRefNum").toString()).intValue();
        int j;
        if(i == 0)
            j = 1;
        else
            j = i;
        return j;
    }

    /**
     * @deprecated Method getInstance is deprecated
     */

    public static IccSmsInterfaceManager getInstance(PhoneBase phonebase)
    {
        IccSmsInterfaceManager iccsmsinterfacemanager;
        if(imsSmsInterfaceManager == null)
            imsSmsInterfaceManager = new IMSICCSmsInterfaceManager(phonebase);
        checkIfSmsInterfaceCreated(phonebase);
        iccsmsinterfacemanager = imsSmsInterfaceManager;
        return iccsmsinterfacemanager;
    }

    public static boolean getSMSPrefered()
    {
        return mSMSprefered;
    }

    private boolean is3GPP2()
    {
        StringBuilder stringbuilder = (new StringBuilder()).append(" is3GPP2():SMS Format value: :mSmsFormat :");
        String s = mSmsFormat;
        String s1 = stringbuilder.append(s).toString();
        int i = Log.e("IMSICCSmsInterfaceManager", s1);
        String s2 = mSmsFormat;
        boolean flag;
        if("3GPP".equalsIgnoreCase(s2))
        {
            com.android.internal.telephony.SMSDispatcher smsdispatcher = gsmSmsInterfaceManager.getDispatcher();
            mDispatcher = smsdispatcher;
            sendOn3GPP2 = false;
            flag = false;
        } else
        {
            com.android.internal.telephony.SMSDispatcher smsdispatcher1 = cdmaSmsInterfaceManager.getDispatcher();
            mDispatcher = smsdispatcher1;
            sendOn3GPP2 = true;
            flag = true;
        }
        return flag;
    }

    public static boolean isOnIMS()
    {
        return ImsSMSInterface.isOnIMS();
    }

    public static boolean isSMSLoggable()
    {
        String s = SystemProperties.get("ril.log.SMS");
        boolean flag;
        if(s != null)
        {
            if(s.equals("DEBUG"))
                flag = true;
            else
                flag = false;
        } else
        {
            flag = false;
        }
        return flag;
    }

    public static Object readSmsSetting(String s)
    {
        String res;
        if (s != "smsformat" && s != "HomeDomainName" && s != "IsSmsOverIms" &&
                s != "SipT1Timer" && s != "SipT2Timer" && s != "SipTFTimer" &&
                s != "sms_storage_to_uicc" && s != "RPRefNum" && s !=
                "TPRefNum" && s != "impi" && s != "impu" && s != "smsprefered") {
            res = null;
            return res;
        } else {
            Cursor cursor;
            ContentResolver contentresolver = mSmsContext.getContentResolver();
            android.net.Uri uri = android.provider.Telephony.IMSSettings.CONTENT_URI;
            String as[] = new String[2];
            as[0] = "name";
            as[1] = "value";
            String s1 = (new StringBuilder()).append("name = '").append(s).append("'").toString();
            cursor = contentresolver.query(uri, as, s1, null, null);
            if (cursor == null) {
                res = null;
                return res;
            }
            if (cursor.getCount() > 0) {
                res = cursor.getString(1);
                cursor.close();
                return res;
            }
            res = null;
            return res;
        }
    }

    public static void setCdmaMsgId(String s)
    {
        updateSmsSetting(mSmsContext, "TPRefNum", s);
    }

    public static void setSMSLoggable(boolean flag)
    {
        if(flag)
        {
            SystemProperties.set("ril.log.SMS", "DEBUG");
            return;
        } else
        {
            SystemProperties.set("ril.log.SMS", "SUPPRESS");
            return;
        }
    }

    public static void updateSmsSetting(Context context, String s, Object obj)
    {
        if(s != "smsformat" && s != "HomeDomainName" && s != "IsSmsOverIms" && s != "SipT1Timer" && s != "SipT2Timer" && s != "SipTFTimer" && s != "sms_storage_to_uicc" && s != "RPRefNum" && s != "TPRefNum" && s != "impi" && s != "impu" && s != "smsprefered")
        {
            return;
        } else
        {
            ContentValues contentvalues = new ContentValues();
            String s1 = obj.toString();
            contentvalues.put("value", s1);
            ContentResolver contentresolver = context.getContentResolver();
            android.net.Uri uri = android.provider.Telephony.IMSSettings.CONTENT_URI;
            String s2 = (new StringBuilder()).append("name = '").append(s).append("'").toString();
            int i = contentresolver.update(uri, contentvalues, s2, null);
            return;
        }
    }

    public int copyMessageToIccEf(int i, byte abyte0[], byte abyte1[])
        throws RemoteException
    {
        int j = Log.d("IMSICCSmsInterfaceManager", "Copy Message to SIM ");
        int k;
        if(is3GPP2())
            k = cdmaSmsInterfaceManager.copyMessageToIccEf(i, abyte0, abyte1);
        else
            k = gsmSmsInterfaceManager.copyMessageToIccEf(i, abyte0, abyte1);
        return k;
    }

    public boolean disableCellBroadcast(int i)
    {
        int j = Log.e("IMSICCSmsInterfaceManager", "Error! Not implemented for CDMA.");
        return false;
    }

    public boolean disableCellBroadcastRange(int i, int j)
    {
        int k = Log.e("IMSICCSmsInterfaceManager", "Error! Not implemented for CDMA.");
        return false;
    }

    public boolean enableCellBroadcast(int i)
    {
        int j = Log.e("IMSICCSmsInterfaceManager", "Error! Not implemented for CDMA.");
        return false;
    }

    public boolean enableCellBroadcastRange(int i, int j)
    {
        int k = Log.e("IMSICCSmsInterfaceManager", "Error! Not implemented for CDMA.");
        return false;
    }

    public List getAllMessagesFromIccEf()
        throws RemoteException
    {
        List list;
        if(is3GPP2())
            list = cdmaSmsInterfaceManager.getAllMessagesFromIccEf();
        else
            list = gsmSmsInterfaceManager.getAllMessagesFromIccEf();
        return list;
    }

    public byte[] getCbSettings()
        throws RemoteException
    {
        byte abyte0[];
        if(is3GPP2())
            abyte0 = cdmaSmsInterfaceManager.getCbSettings();
        else
            abyte0 = gsmSmsInterfaceManager.getCbSettings();
        return abyte0;
    }

    public RuimSmsInterfaceManager getCdmaSmsInterfaceManager()
    {
        return cdmaSmsInterfaceManager;
    }

    public SimSmsInterfaceManager getGsmSmsInterfaceManager()
    {
        return gsmSmsInterfaceManager;
    }

    protected void log(String s)
    {
        int i = Log.d("IMSICCSmsInterfaceManager", s);
    }

    public void sendData(String s, String s1, int i, byte abyte0[], PendingIntent pendingintent, PendingIntent pendingintent1)
    {
        log("inside IMSICCSmsInterfaceManager sendData");
        if(is3GPP2())
        {
            com.android.internal.telephony.SMSDispatcher smsdispatcher = cdmaSmsInterfaceManager.getDispatcher();
            mDispatcher = smsdispatcher;
        } else
        {
            com.android.internal.telephony.SMSDispatcher smsdispatcher1 = gsmSmsInterfaceManager.getDispatcher();
            mDispatcher = smsdispatcher1;
        }
        sendData(s, s1, i, abyte0, pendingintent, pendingintent1);
    }

    public void sendMultipartText(String s, String s1, List list, List list1, List list2)
    {
        log("inside IMSICCSmsInterfaceManager sendMultipartText");
        if(is3GPP2())
        {
            com.android.internal.telephony.SMSDispatcher smsdispatcher = cdmaSmsInterfaceManager.getDispatcher();
            mDispatcher = smsdispatcher;
        } else
        {
            com.android.internal.telephony.SMSDispatcher smsdispatcher1 = gsmSmsInterfaceManager.getDispatcher();
            mDispatcher = smsdispatcher1;
        }
        sendMultipartText(s, s1, list, list1, list2);
    }

    public void sendMultipartTextwithOptions(String s, String s1, List list, List list1, List list2, boolean flag, int i, 
            int j, int k)
    {
        log("inside IMSICCSmsInterfaceManager sendMultipartTextwithOptions");
        if(is3GPP2())
        {
            com.android.internal.telephony.SMSDispatcher smsdispatcher = cdmaSmsInterfaceManager.getDispatcher();
            mDispatcher = smsdispatcher;
        } else
        {
            com.android.internal.telephony.SMSDispatcher smsdispatcher1 = gsmSmsInterfaceManager.getDispatcher();
            mDispatcher = smsdispatcher1;
        }
        sendMultipartTextwithOptions(s, s1, list, list1, list2, flag, i, j, k);
    }

    public void sendRawPduSat(byte abyte0[], byte abyte1[], PendingIntent pendingintent, PendingIntent pendingintent1)
    {
        log("inside IMSICCSmsInterfaceManager sendRawPduSat");
        if(is3GPP2())
        {
            com.android.internal.telephony.SMSDispatcher smsdispatcher = cdmaSmsInterfaceManager.getDispatcher();
            mDispatcher = smsdispatcher;
        } else
        {
            com.android.internal.telephony.SMSDispatcher smsdispatcher1 = gsmSmsInterfaceManager.getDispatcher();
            mDispatcher = smsdispatcher1;
        }
        sendRawPduSat(abyte0, abyte1, pendingintent, pendingintent1);
    }

    public void sendText(String s, String s1, String s2, PendingIntent pendingintent, PendingIntent pendingintent1)
    {
        log("inside IMSICCSmsInterfaceManager sendText");
        if(is3GPP2())
        {
            com.android.internal.telephony.SMSDispatcher smsdispatcher = cdmaSmsInterfaceManager.getDispatcher();
            mDispatcher = smsdispatcher;
        } else
        {
            com.android.internal.telephony.SMSDispatcher smsdispatcher1 = gsmSmsInterfaceManager.getDispatcher();
            mDispatcher = smsdispatcher1;
        }
        sendText(s, s1, s2, pendingintent, pendingintent1);
    }

    public void sendTextwithOptions(String s, String s1, String s2, PendingIntent pendingintent, PendingIntent pendingintent1, boolean flag, int i, 
            int j, int k)
    {
        log("inside IMSICCSmsInterfaceManager sendTextwithOptions");
        if(is3GPP2())
        {
            com.android.internal.telephony.SMSDispatcher smsdispatcher = cdmaSmsInterfaceManager.getDispatcher();
            mDispatcher = smsdispatcher;
        } else
        {
            com.android.internal.telephony.SMSDispatcher smsdispatcher1 = gsmSmsInterfaceManager.getDispatcher();
            mDispatcher = smsdispatcher1;
        }
        sendTextwithOptions(s, s1, s2, pendingintent, pendingintent1, flag, i, j, k);
    }

    public void setCbConfig(byte byte0, byte byte1, int i, byte abyte0[], int ai[])
        throws RemoteException
    {
        if(is3GPP2())
        {
            RuimSmsInterfaceManager ruimsmsinterfacemanager = cdmaSmsInterfaceManager;
            byte byte2 = byte0;
            byte byte3 = byte1;
            int j = i;
            byte abyte1[] = abyte0;
            int ai1[] = ai;
            ruimsmsinterfacemanager.setCbConfig(byte2, byte3, j, abyte1, ai1);
            return;
        } else
        {
            SimSmsInterfaceManager simsmsinterfacemanager = gsmSmsInterfaceManager;
            byte byte4 = byte0;
            byte byte5 = byte1;
            int k = i;
            byte abyte2[] = abyte0;
            int ai2[] = ai;
            simsmsinterfacemanager.setCbConfig(byte4, byte5, k, abyte2, ai2);
            return;
        }
    }

    public boolean updateMessageOnIccEf(int i, int j, byte abyte0[])
        throws RemoteException
    {
        boolean flag;
        if(is3GPP2())
            flag = cdmaSmsInterfaceManager.updateMessageOnIccEf(i, j, abyte0);
        else
            flag = gsmSmsInterfaceManager.updateMessageOnIccEf(i, j, abyte0);
        return flag;
    }

    public boolean updateSmsServiceCenterOnSimEf(byte abyte0[])
        throws RemoteException
    {
        boolean flag;
        if(is3GPP2())
            flag = cdmaSmsInterfaceManager.updateSmsServiceCenterOnSimEf(abyte0);
        else
            flag = gsmSmsInterfaceManager.updateSmsServiceCenterOnSimEf(abyte0);
        return flag;
    }

    private static final String DEFAULT_HOME_DOMAIN_NAME = "registrar.vzw.net";
    private static final String DEFAULT_SEND_ON_IMS = "true";
    private static final String DEFAULT_SMS_FORMAT = "3GPP2";
    private static final long DEFAULT_SMS_SIPTIMERT1 = 3L;
    private static final long DEFAULT_SMS_SIPTIMERT2 = 16L;
    private static final long DEFAULT_SMS_SIPTIMERTF = 30L;
    public static final int DEFAULT_SMS_WRITE_ToUICC = 0;
    private static final String LOG_TAG = "IMSICCSmsInterfaceManager";
    private static RuimSmsInterfaceManager cdmaSmsInterfaceManager = null;
    private static SimSmsInterfaceManager gsmSmsInterfaceManager = null;
    private static IccSmsInterfaceManager imsSmsInterfaceManager = null;
    public static String mHomeDomainName;
    public static boolean mSMSprefered = true;
    private static Context mSmsContext;
    public static String mSmsFormat;
    public static String mSmsSendOnIms;
    public static Long mSmsSipTimerT1;
    public static Long mSmsSipTimerT2;
    public static Long mSmsSipTimerTF;
    public static String mSmsStorageToUICC;
    public static boolean sendOn3GPP2 = false;
    private SmsSettingsObserver mSmsSettingsObserver;


}
