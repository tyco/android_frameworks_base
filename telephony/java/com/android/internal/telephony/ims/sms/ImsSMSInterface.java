// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ImsSMSInterface.java

package com.android.internal.telephony.ims.sms;

import Lcom.android.internal.telephony.ims.sms.ImsSMSInterface;;
import android.content.Context;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.Registrant;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.Log;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.PhoneBase;
import com.android.internal.telephony.SmsResponse;
import com.android.internal.telephony.cdma.CDMAPhone;
import com.android.internal.telephony.cdma.SmsMessage;
import com.android.internal.telephony.gsm.GSMPhone;
import com.android.internal.util.HexDump;
import com.sec.android.ims.IMSEventListener;
import com.sec.android.ims.IMSManager;
import com.sec.android.ims.message.IMessageEnabler;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;
import org.apache.commons.codec.binary.Base64;

// Referenced classes of package com.android.internal.telephony.ims.sms:
//            AMIL, IMSICCSmsInterfaceManager, MTRecieveData, MOMessageData

public class ImsSMSInterface
{

    private ImsSMSInterface(Context context, PhoneBase phonebase)
    {
        mImsManager = null;
        mMsgEnabler = null;
        Hashtable hashtable = new Hashtable();
        sentMessageMap = hashtable;
        Vector vector = new Vector();
        recieveQueue = vector;
        Object obj = new Object();
        msgMapMonitor = obj;
        Object obj1 = new Object();
        msgVectorMTMonitor = obj1;
        isSMSBeingProcessed = false;
        noOffailResponse = 0;
        noOffailResponse403 = 0;
        noOfFailResponse404 = 0;
        condition = 0;
        isRegi = false;
        mPhone = null;
        IMSEventListener imseventlistener = new IMSEventListener() {

            private CommandException getCommandException(int i)
            {
                CommandException commandexception;
                if(i >= 400 || i < 500)
                {
                    com.android.internal.telephony.CommandException.Error error = com.android.internal.telephony.CommandException.Error.SMS_FAIL_RETRY;
                    commandexception = new CommandException(error);
                } else
                {
                    com.android.internal.telephony.CommandException.Error error1 = com.android.internal.telephony.CommandException.Error.GENERIC_FAILURE;
                    commandexception = new CommandException(error1);
                }
                return commandexception;
            }

            private int getSmsErrorCode(int i)
            {
                int j = 1;
                switch(i) {
                case 202:
                    j = 0;
                case 403:
                    j = 97;
                case 404:
                case 410:
                case 480:
                    j = 34;
                case 484:
                case 485:
                case 486:
                    j = 34;
                case 501:
                    j = 31;
                case 502:
                        j = 31;
                case 504:
                        j = 31;
                case 580:
                        j = 31;
                case 604:
                default:
                }
                return j;
            }

            private void handleSMSEvent(int i, int j, int k, byte abyte0[])
            {
                int l = Log.d("IMSSMSInterface ", "Recieve a SMS related event from IMS stack");
                switch(i)
                {
                default:
                    return;

                case 11: // '\013'
                    int i1 = Log.d("IMSSMSInterface ", "Processing SMSIP_CST_RECEIVED a new sms is recieved by the stack");
                    StringBuilder stringbuilder = (new StringBuilder()).append("  pdu format  recieved at ims framework is   : ");
                    String s1 = HexDump.toHexString(abyte0);
                    ImsSMSInterface.log(stringbuilder.append(s1).toString());
                    byte byte0 = 0;
                    if(k == 0)
                    {
                        int j1 = abyte0[0] & 0xff;
                        int k1 = j1 + 1;
                        byte0 = abyte0[k1];
                        ImsSMSInterface.log((new StringBuilder()).append("P-Id length:").append(j1).append("First Byte :").append(byte0).toString());
                    }
                    if(k == 1 || k == 0 && (byte0 & 7) == 1)
                    {
                        int l1 = Log.d("IMSSMSInterface ", "adding to the recieve Queue");
                        MTRecieveData mtrecievedata = new MTRecieveData(j, k, abyte0);
                        synchronized(msgVectorMTMonitor)
                        {
                            boolean flag = recieveQueue.add(mtrecievedata);
                            if(!isSMSBeingProcessed)
                            {
                                boolean flag1 = isSMSBeingProcessed = true;
                                handleNewSMS(j, k, abyte0);
                            }
                        }
                        return;
                    } else
                    {
                        handleNewSMS(j, k, abyte0);
                        return;
                    }

                case 9: // '\t'
                case 10: // '\n'
                    handleSendComplete(i, j, k);
                    return;
                }
            }

            private void handleSendComplete(int i, int j, int k)
            {
                byte byte0;
                MOMessageData momessagedata;
                int l = Log.d("IMSSMSInterface ", "Processing handleSendComplete");
                ImsSMSInterface.isMOSmsResponseOverIms = true;
                byte0 = 0;
                synchronized(msgMapMonitor)
                {
                    Map map = sentMessageMap;
                    Integer integer = Integer.valueOf(j);
                    momessagedata = (MOMessageData)map.get(integer);
                    int i1 = 0;
                    do
                    {
                        int j1 = sentMessageMap.size();
                        if(i1 >= j1)
                            break;
                        StringBuilder stringbuilder = (new StringBuilder()).append(" map value when sent ack comes at msgId ");
                        int k1 = j;
                        StringBuilder stringbuilder1 = stringbuilder.append(k1).append("is ");
                        Map map1 = sentMessageMap;
                        Integer integer1 = Integer.valueOf(j);
                        Object obj3 = map1.get(integer1);
                        String s1 = stringbuilder1.append(obj3).toString();
                        int l1 = Log.e("IMSSMSInterface", s1);
                        i1++;
                    } while(true);
                    StringBuilder stringbuilder2 = (new StringBuilder()).append("Message count : ");
                    int i2 = sentMessageMap.size();
                    ImsSMSInterface.log(stringbuilder2.append(i2).toString());
                    ImsSMSInterface.log((new StringBuilder()).append("MOMessageData message: ").append(momessagedata).toString());
                }
                if(momessagedata == null)
                {
                    int j2 = Log.e("IMSSMSInterface ", "Sorry, no matching Message ID found");
                    return;
                }
                int k2 = k;
                int l2 = getSmsErrorCode(k2);
                int i3 = k;
                char c = '\u0190';
                if(i3 >= c)
                {
                    int j3 = k;
                    char c1 = '\u0258';
                    if(j3 < c1)
                        byte0 = 2;
                }
                int k3 = momessagedata.pduMsgID;
                SmsResponse smsresponse = new SmsResponse(k3, "", l2, byte0);
                Message message = momessagedata.replyMsg;
                CommandException commandexception = null;
                if(i == 9)
                {
                    int l3 = k;
                    commandexception = getCommandException(l3);
                }
                ImsSMSInterface.log((new StringBuilder()).append("reply message =").append(message).toString());
                AsyncResult asyncresult = AsyncResult.forMessage(message, smsresponse, commandexception);
                Map map2 = sentMessageMap;
                Integer integer2 = Integer.valueOf(j);
                Object obj4 = map2.remove(integer2);
                message.sendToTarget();
                if(message.what == 97 || message.what == 98)
                {
                    ImsSMSInterface.log("trying to remove the message from RevQ for 3GPP");
                    removeFromRevQueue();
                }
                StringBuilder stringbuilder3 = (new StringBuilder()).append("Successfully Processed message with msgID: ");
                int i4 = j;
                String s2 = stringbuilder3.append(i4).toString();
                int j4 = Log.d("IMSSMSInterface ", s2);
                return;
            }

            public void CallForImsPdnDetach(int i)
            {
                int j = noOffailResponse403 = 0;
                int k = noOfFailResponse404 = 0;
                int l = noOffailResponse = 0;
                ImsSMSInterface imssmsinterface = ImsSMSInterface.this;
                ITelephony itelephony = com.android.internal.telephony.ITelephony.Stub.asInterface(ServiceManager.getService("phone"));
                ITelephony itelephony1 = imssmsinterface.mPhoneService = itelephony;
                if(mPhoneService == null) {
                    int l1 = Log.e("IMSSMSInterface ", "mPhoneService is null");
                    return;
                }
                if(i == 2)
                {
                    int i1 = Log.d("IMSSMSInterface ", "Detach with T3402 with IMS registeration failed");
                    boolean flag = mPhoneService.explicitDetach(0, 2);
                    return;
                }
                if(i == 3)
                {
                    int j1 = Log.d("IMSSMSInterface ", "Detach Only with IMS registeration failed");
                    boolean flag1 = mPhoneService.explicitDetach(0, 0);
                    return;
                }
                if(i != 4)
                    return;
                int k1 = Log.d("IMSSMSInterface ", "Detach and Re-Attach IMS registeration failed");
                boolean flag2 = mPhoneService.explicitDetach(1, 0);
                return;
            }

            public int checkForImsPdnDetachCondition(int i, boolean flag)
            {
                byte byte0;
                if(noOffailResponse == 3 && noOffailResponse403 != 3 && noOfFailResponse404 != 3)
                {
                    if(!flag)
                        byte0 = 2;
                    else
                    if(i == 503 || i == 408)
                        byte0 = 2;
                    else
                        byte0 = 4;
                } else
                if(noOfFailResponse404 == 6 || noOffailResponse == 6)
                    byte0 = 2;
                else
                if(noOffailResponse403 == 3)
                    byte0 = 3;
                else
                    byte0 = 1;
                return byte0;
            }

            public void handleEvent(int i, int j, int k, int l, byte abyte0[])
            {
                int i1 = Log.d("IMSSMSInterface ", ">>>>> IMS Event in ImsSMS telephony framework-->start:");
                String s1 = (new StringBuilder()).append("handleEvent: appType:").append(i).toString();
                int j1 = Log.d("IMSSMSInterface ", s1);
                String s2 = (new StringBuilder()).append("handleEvent: eventType:").append(j).toString();
                int k1 = Log.d("IMSSMSInterface ", s2);
                String s3 = (new StringBuilder()).append("handleEvent: arg1:").append(k).toString();
                int l1 = Log.d("IMSSMSInterface ", s3);
                String s4 = (new StringBuilder()).append("handleEvent: arg2:").append(l).toString();
                int i2 = Log.d("IMSSMSInterface ", s4);
                if(abyte0 != null)
                {
                    StringBuilder stringbuilder = (new StringBuilder()).append("handleEvent: data:");
                    String s5 = HexDump.toHexString(abyte0);
                    String s6 = stringbuilder.append(s5).toString();
                    int j2 = Log.d("IMSSMSInterface ", s6);
                }
                int k2 = sipRespCode = l;
                if(i == 3 && j == 10)
                {
                    ImsSMSInterface imssmsinterface = ImsSMSInterface.this;
                    long l2 = System.currentTimeMillis();
                    long l3 = imssmsinterface.endTimer = l2;
                    StringBuilder stringbuilder1 = (new StringBuilder()).append("Response Time : ");
                    long l4 = endTimer;
                    long l5 = startTimer;
                    double d = (double)(l4 - l5) / 1000D;
                    String s7 = stringbuilder1.append(d).toString();
                    int i3 = Log.d("IMSSMSInterface ", s7);
                }
                switch(i) {
                    case 3:
                        handleSMSEvent(j, k, l, abyte0);
                        ImsSMSInterface.log(">>>>> IMS Event in ImsSMS telephony framework-->end.");
                        return;
                    case 5:
                        ImsSMSInterface.log("recieved a ImsConstants.UI_APP_REG event from ims stack ");
                        int j3;
                        if(sipRespCode == 0)
                        {
                            j3 = Log.e("IMSSMSInterface ", "invalid sip response code");
                        } else
                        {
                            int j9;
                            if(j == 103 || j == 109 || j == 104 || j == 117)
                            {
                                StringBuilder stringbuilder2 = (new StringBuilder()).append("SIP response code : ");
                                int k3 = sipRespCode;
                                String s8 = stringbuilder2.append(k3).toString();
                                int i4 = Log.d("IMSSMSInterface ", s8);
                                switch(sipRespCode)
                                {
                                case 400: 
                                case 403: 
                                case 404: 
                                case 408: 
                                case 500: 
                                case 502: 
                                case 503: 
                                    ImsSMSInterface imssmsinterface1 = ImsSMSInterface.this;
                                    boolean flag = isReRegisteration(j);
                                    boolean flag1 = imssmsinterface1.isRegi = flag;
                                    ImsSMSInterface imssmsinterface2 = ImsSMSInterface.this;
                                    int j4 = sipRespCode;
                                    boolean flag2 = isRegi;
                                    int k4 = checkForImsPdnDetachCondition(j4, flag2);
                                    int i5 = imssmsinterface2.condition = k4;
                                    StringBuilder stringbuilder3 = (new StringBuilder()).append("sip condition for detach :");
                                    int j5 = condition;
                                    String s9 = stringbuilder3.append(j5).toString();
                                    int k5 = Log.d("IMSSMSInterface ", s9);
                                    int j6;
                                    StringBuilder stringbuilder4;
                                    int k6;
                                    int l6;
                                    int j7;
                                    StringBuilder stringbuilder5;
                                    char c;
                                    StringBuilder stringbuilder6;
                                    String s10;
                                    String s11;
                                    int k7;
                                    PhoneBase phonebase1;
                                    Message message;
                                    PhoneBase phonebase2;
                                    Message message1;
                                    int l7;
                                    int i8;
                                    if(condition == 3 || condition == 2 || condition == 4)
                                    {
                                        int i6 = condition;
                                        CallForImsPdnDetach(i6);
                                    } else
                                    {
                                        i8 = Log.e("IMSSMSInterface ", "neither detach or detach with reattach now");
                                    }
                                    break;

                                case 401: 
                                    j6 = Log.e("IMSSMSInterface ", "Registration failed, go for retrying if isim nounce present");
                                    abyte0 = Base64.decodeBase64(abyte0);
                                    stringbuilder4 = (new StringBuilder()).append("Data after decoding: ");
                                    k6 = abyte0.length;
                                    ImsSMSInterface.log(stringbuilder4.append(k6).toString());
                                    l6 = 0;
                                    do
                                    {
                                        j7 = abyte0.length;
                                        if(l6 >= j7)
                                            break;
                                        stringbuilder5 = (new StringBuilder()).append("ISIM >>> ");
                                        c = (char)abyte0[l6];
                                        stringbuilder6 = stringbuilder5.append(c).append("  hex: ");
                                        s10 = Integer.toHexString(abyte0[l6]);
                                        s11 = stringbuilder6.append(s10).toString();
                                        k7 = Log.d("ISIM", s11);
                                        l6++;
                                    } while(true);
                                    phonebase1 = mPhone;
                                    message = isimInterfaceHandler.obtainMessage(44, "REGISTER");
                                    phonebase1.requestIsimAuthentication(abyte0, message);
                                    if(ImsSMSInterface.GsmPhone != null)
                                    {
                                        phonebase2 = ImsSMSInterface.GsmPhone;
                                        message1 = isimInterfaceHandler.obtainMessage(44, "REGISTER");
                                        phonebase2.requestIsimAuthentication(abyte0, message1);
                                    } else
                                    {
                                        l7 = Log.e("SIM errors", "GSMPhoneInstance is null");
                                    }
                                    break;
                                }
                            } else
                            if(j == 102)
                            {
                                int j8 = Log.d("IMSSMSInterface ", "IMS registration successful.");
                                int k8 = noOffailResponse403 = 0;
                                int l8 = noOfFailResponse404 = 0;
                                int i9 = noOffailResponse = 0;
                                boolean flag3 = isRegi = false;
                            } else
                            if(j == 106)
                                j9 = Log.d("IMSSMSInterface ", "IMS dreregistration successful.");
                            else
                            if(j == 105)
                                switch(sipRespCode)
                                {
                                case 401: 
                                    int k9 = Log.d("IMSSMSInterface ", "De-Registration failed, go for retrying if isim nounce present");
                                    abyte0 = Base64.decodeBase64(abyte0);
                                    StringBuilder stringbuilder7 = (new StringBuilder()).append("Data after decoding: ");
                                    int l9 = abyte0.length;
                                    String s12 = stringbuilder7.append(l9).toString();
                                    int i10 = Log.d("IMSSMSInterface ", s12);
                                    int i7 = 0;
                                    do
                                    {
                                        int j10 = abyte0.length;
                                        if(i7 >= j10)
                                            break;
                                        StringBuilder stringbuilder8 = (new StringBuilder()).append("ISIM >>> ");
                                        char c1 = (char)abyte0[i7];
                                        StringBuilder stringbuilder9 = stringbuilder8.append(c1).append("  hex: ");
                                        String s13 = Integer.toHexString(abyte0[i7]);
                                        String s14 = stringbuilder9.append(s13).toString();
                                        int k10 = Log.d("ISIM", s14);
                                        int l10 = i7 + 1;
                                    } while(true);
                                    PhoneBase phonebase3 = mPhone;
                                    Message message2 = isimInterfaceHandler.obtainMessage(44, "DEREGISTER");
                                    phonebase3.requestIsimAuthentication(abyte0, message2);
                                    int i11;
                                    if(ImsSMSInterface.GsmPhone != null)
                                    {
                                        PhoneBase phonebase4 = ImsSMSInterface.GsmPhone;
                                        Message message3 = isimInterfaceHandler.obtainMessage(44, "DEREGISTER");
                                        phonebase4.requestIsimAuthentication(abyte0, message3);
                                    } else
                                    {
                                        i11 = Log.e("SIM errors", "GSMPhoneInstance is null");
                                    }
                                    break;
                                }
                        }
                        ImsSMSInterface.log(">>>>> IMS Event in ImsSMS telephony framework-->end.");
                        return;
                    case 1001:
                        int j11 = Log.d("IMSSMSInterface ", "IMS service Bound");
                        ImsSMSInterface imssmsinterface3 = ImsSMSInterface.this;
                        IMessageEnabler imessageenabler = mImsManager.getMessageEnabler();
                        IMessageEnabler imessageenabler1 = imssmsinterface3.mMsgEnabler = imessageenabler;
                        ImsSMSInterface.log(">>>>> IMS Event in ImsSMS telephony framework-->end.");
                        return;
                    default:
                        ImsSMSInterface.log(">>>>> IMS Event in ImsSMS telephony framework-->end.");
                        return;
                }
            }

            public boolean isReRegisteration(int i)
            {
                boolean flag;
                if(i == 117)
                    flag = true;
                else
                    flag = false;
                return flag;
            }
        };
        mImsEventListener = imseventlistener;
        Handler handler = new Handler() {

            public void handleMessage(Message message)
            {
                int i;
                switch(message.what)
                {
                default:
                    return;

                case 44: // ','
                    i = Log.d("ISIM", "EVENT_ISIM_AUTHENTICATION_DONE");
                    break;
                }
                AsyncResult asyncresult = (AsyncResult)message.obj;
                byte abyte0[] = (byte[])(byte[])asyncresult.result;
                String s1 = (String)asyncresult.userObj;
                if(asyncresult.exception != null)
                {
                    int j = Log.d("ISIM", "Exception in EVENT_ISIM_AUTHENTICATION_DONE");
                    StringBuilder stringbuilder = (new StringBuilder()).append("Exception ");
                    String s2 = asyncresult.exception.getMessage();
                    String s3 = stringbuilder.append(s2).toString();
                    int k = Log.d("ISIM", s3);
                    asyncresult.exception.printStackTrace();
                    return;
                }
                StringBuilder stringbuilder1 = (new StringBuilder()).append("Data: ");
                Object obj2;
                String s4;
                int l;
                String s5;
                String s6;
                int i1;
                if(abyte0 == null)
                    obj2 = "null";
                else
                    obj2 = Integer.valueOf(abyte0.length);
                s4 = stringbuilder1.append(obj2).toString();
                l = Log.d("ISIM", s4);
                s5 = SystemProperties.get("net.pdpbr0.ipv6.pcscf");
                s6 = (new StringBuilder()).append("pcscf addr : ").append(s5).toString();
                i1 = Log.d("IMSSMSInterface ", s6);
                if("REGISTER".equalsIgnoreCase(s1))
                {
                    mImsManager.register(s5, abyte0);
                    return;
                }
                if(!"DEREGISTER".equalsIgnoreCase(s1))
                {
                    return;
                } else
                {
                    mImsManager.unregister(s5, abyte0);
                    return;
                }
            }
        };
        isimInterfaceHandler = handler;
        mContext = context;
        mPhone = phonebase;
        noOfRegFailWithSameSipRespCode = 0;
        prevSipRespCode = 0;
        sipRespCode = 0;
        isMTSmsOverIms = false;
        isMOSmsResponseOverIms = false;
        String s = System.setProperty("isSmsRecvOverIms", "false");
        AMIL amil1 = new AMIL();
        amil = amil1;
        initIMSManagerInterfaces();
    }

    public static void TrackingReceivedSMS(int i, int j)
    {
        log("Tracking received SMS");
        String s = String.valueOf(Integer.parseInt(SystemProperties.get("ril.message.mt.count", "0")) + 1);
        SystemProperties.set("ril.message.mt.count", s);
        String s1;
        String s2;
        String s3;
        String s4;
        if(i == 0)
            s1 = "3GPP";
        else
            s1 = "3GPP2";
        SystemProperties.set("ril.msg.mo.contenttype", s1);
        s2 = "ril.message.mt.network";
        if(j == 0)
            s3 = "1x";
        else
            s3 = "IMS";
        SystemProperties.set(s2, s3);
        s4 = String.valueOf(System.currentTimeMillis());
        SystemProperties.set("ril.message.mt.time", s4);
    }

    public static void TrackingSentSMS(int i, int j)
    {
        log("Tracking Sent SMS");
        String s = String.valueOf(Integer.parseInt(SystemProperties.get("ril.message.mo.count", "0")) + 1);
        SystemProperties.set("ril.message.mo.count", s);
        String s1;
        String s2;
        String s3;
        String s4;
        if(i == 0)
            s1 = "3GPP";
        else
            s1 = "3GPP2";
        SystemProperties.set("ril.msg.mo.contenttype", s1);
        s2 = "ril.message.mo.network";
        if(j == 0)
            s3 = "1x";
        else
            s3 = "IMS";
        SystemProperties.set(s2, s3);
        s4 = String.valueOf(System.currentTimeMillis());
        SystemProperties.set("ril.message.mo.time", s4);
    }

    /**
     * @deprecated Method getInstance is deprecated
     */

    public static ImsSMSInterface getInstance(Context context, PhoneBase phonebase)
    {
        ImsSMSInterface imssmsinterface;
        if(phonebase instanceof CDMAPhone)
            CdmaPhone = phonebase;
        if(phonebase instanceof GSMPhone)
            GsmPhone = phonebase;
        if(imsSMSInterface == null)
            imsSMSInterface = new ImsSMSInterface(context, phonebase);
        imssmsinterface = imsSMSInterface;
        return imssmsinterface;
    }

    private void handleNewSMS(int i, int j, byte abyte0[])
    {
        StringBuilder stringbuilder = (new StringBuilder()).append("  pdu format  recieved at ims framework is : ");
        String s = HexDump.toHexString(abyte0);
        log(stringbuilder.append(s).toString());
        isMTSmsOverIms = true;
        if(ImsDebug)
            TrackingReceivedSMS(j, 1);
        String s1 = System.setProperty("isSmsRecvOverIms", "true");
        StringBuilder stringbuilder1 = (new StringBuilder()).append("set value of isSmsRecvOverIms ");
        String s2 = System.getProperty("isSmsRecvOverIms");
        log(stringbuilder1.append(s2).toString());
        if (j == 0) {
            // L1
            int k = Log.d("IMSSMSInterface ", "Processing 3GPP message");
            isMOSmsResponseOverIms = true;
            if(m3GPPSMSRegistrant != null)
            {
                Registrant registrant = m3GPPSMSRegistrant;
                Integer integer = Integer.valueOf(i);
                AsyncResult asyncresult = new AsyncResult(integer, abyte0, null);
                registrant.notifyRegistrant(asyncresult);
            }
            log("the new sms recieved is handled to the dispatcher");
            return;
        }
        // L2
        android.telephony.SmsMessage smsmessage;
        if(j != 1)
            continue; /* Loop/switch isn't completed */
        int l = Log.d("IMSSMSInterface ", "Processing 3GPP2 message");
        smsmessage = null;
        android.telephony.SmsMessage smsmessage2;
        SmsMessage smsmessage1 = SmsMessage.newFromStack(abyte0);
        smsmessage2 = new android.telephony.SmsMessage(smsmessage1);
        smsmessage = smsmessage2;
        log((new StringBuilder()).append("smsMsg: ").append(smsmessage).toString());
        log("the new sms recieved is handled to the dispatcher");
        return;
        if(m3GPP2SMSRegistrant != null)
        {
            Registrant registrant1 = m3GPP2SMSRegistrant;
            AsyncResult asyncresult1 = new AsyncResult(null, smsmessage, null);
            registrant1.notifyRegistrant(asyncresult1);
        }
    }

    private void initIMSManagerInterfaces()
    {
        if(mImsManager != null)
        {
            int i = Log.e("IMSSMSInterface ", " Already obtained the IMSInstance");
            return;
        } else
        {
            IMSManager imsmanager = IMSManager.getInstance(mContext);
            mImsManager = imsmanager;
            IMSManager imsmanager1 = mImsManager;
            IMSEventListener imseventlistener = mImsEventListener;
            int j = iEnablerID;
            imsmanager1.registerListener(imseventlistener, j);
            return;
        }
        printStackTrace();
    }

    public static boolean isOnIMS()
    {
        return Boolean.valueOf(SystemProperties.get("persist.sys.ims.reg")).booleanValue();
    }

    public static boolean isSmsRecvOverIms()
    {
        boolean flag;
        if(!isOnIMS())
        {
            flag = false;
        } else
        {
            String s = System.getProperty("isSmsRecvOverIms");
            boolean flag1 = Boolean.valueOf(s).booleanValue();
            String s1 = (new StringBuilder()).append("isSmsRecvOverIms ").append(flag1).append(" smsrecvoverims ").append(s).toString();
            int i = Log.d("ImsSMSInterface", s1);
            flag = flag1;
        }
        return flag;
    }

    private static void log(String s)
    {
        if(!IMSICCSmsInterfaceManager.isSMSLoggable())
        {
            return;
        } else
        {
            int i = Log.d("SMS:CDMA", s);
            return;
        }
    }

    private void removeFromRevQueue()
    {
        MTRecieveData mtrecievedata = (MTRecieveData)recieveQueue.firstElement();
        StringBuilder stringbuilder = (new StringBuilder()).append("the element being removed  from the Recieve Queue is ");
        int i = mtrecievedata.contentType;
        StringBuilder stringbuilder1 = stringbuilder.append(i).append(" ");
        int j = mtrecievedata.pduMsgID;
        String s = stringbuilder1.append(j).toString();
        int k = Log.d("IMSSMSInterface ", s);
        synchronized(msgVectorMTMonitor)
        {
            Object obj1 = recieveQueue.remove(0);
            isSMSBeingProcessed = false;
            if(!recieveQueue.isEmpty())
            {
                MTRecieveData mtrecievedata1 = (MTRecieveData)recieveQueue.firstElement();
                StringBuilder stringbuilder2 = (new StringBuilder()).append("the message waiting to be processed is   ");
                int l = mtrecievedata1.pduMsgID;
                StringBuilder stringbuilder3 = stringbuilder2.append(l).append("  ");
                int i1 = mtrecievedata1.contentType;
                String s1 = stringbuilder3.append(i1).toString();
                int j1 = Log.d("IMSSMSInterface ", s1);
                if(!isSMSBeingProcessed)
                {
                    isSMSBeingProcessed = true;
                    int k1 = mtrecievedata1.pduMsgID;
                    int l1 = mtrecievedata1.contentType;
                    byte abyte0[] = mtrecievedata1.pdu;
                    handleNewSMS(k1, l1, abyte0);
                }
            }
        }
    }

    public void acknowledgeLastIncomingCDMASms(boolean flag, int i, Message message)
    {
        MTRecieveData mtrecievedata;
        int j = Log.d("IMSSMSInterface ", "Entering acknowledgeLastIncomingSms");
        log((new StringBuilder()).append("success: ").append(flag).append(" cause: ").append(i).append("  response: ").append(message).toString());
        mtrecievedata = (MTRecieveData)recieveQueue.firstElement();
        IMessageEnabler imessageenabler = mMsgEnabler;
        int k = mtrecievedata.pduMsgID;
        int l = imessageenabler.sendSMSResponse(k, 200, "OK");
        isSMSBeingProcessed = false;
        while (!recieveQueue.isEmpty()) {
            synchronized(msgVectorMTMonitor)
            {
                Object obj1 = recieveQueue.remove(0);
                MTRecieveData mtrecievedata1 = (MTRecieveData)recieveQueue.firstElement();
                StringBuilder stringbuilder = (new StringBuilder()).append("the message waiting to be processed is   ");
                int i1 = mtrecievedata1.pduMsgID;
                StringBuilder stringbuilder1 = stringbuilder.append(i1).append("  ");
                int j1 = mtrecievedata1.contentType;
                log(stringbuilder1.append(j1).toString());
                if(!isSMSBeingProcessed)
                {
                    isSMSBeingProcessed = true;
                    int k1 = mtrecievedata1.pduMsgID;
                    int l1 = mtrecievedata1.contentType;
                    byte abyte0[] = mtrecievedata1.pdu;
                    handleNewSMS(k1, l1, abyte0);
                }
            }
        }
        log("Exiting acknowledgeLastIncomingSms");
        return;
    }

    public void dispose()
    {
        mImsManager = null;
        mMsgEnabler = null;
    }

    public void sendImsSms(byte abyte0[], String s, Message message, String s1, int i)
    {
        byte abyte1[];
        int j = Log.d("IMSSMSInterface ", "Sending SMS over IMS");
        long l = System.currentTimeMillis();
        startTimer = l;
        StringBuilder stringbuilder;
        String s2;
        String s3;
        int i1;
        String s4;
        int j1;
        if(s1.equalsIgnoreCase("application/vnd.3gpp2.sms"))
        {
            AMIL amil1 = amil;
            int k = MESSAGE_FORMAT_3GPP2;
            abyte1 = amil1.CreateForStack(k, abyte0);
        } else
        {
            abyte1 = abyte0;
        }
        stringbuilder = (new StringBuilder()).append("PDU  :");
        s2 = HexDump.toHexString(abyte1);
        log(stringbuilder.append(s2).toString());
        s3 = (new StringBuilder()).append("Dest Addr : ").append(s).toString();
        i1 = Log.d("IMSSMSInterface ", s3);
        s4 = (new StringBuilder()).append("pduID :  ").append(i).toString();
        j1 = Log.d("IMSSMSInterface ", s4);
        if(ImsDebug)
            if(s1.equalsIgnoreCase("application/vnd.3gpp2.sms"))
                TrackingSentSMS(1, 1);
            else
                TrackingSentSMS(0, 1);
        if(mMsgEnabler == null)
        {
            int k1 = Log.e("IMSSMSInterface ", "mMsgEnabler is null");
            return;
        }
        if(!isOnIMS())
        {
            int l1 = Log.e("IMSSMSInterface ", "IMS is not yet registered tried at a later time");
            return;
        }
        synchronized(msgMapMonitor)
        {
            int i2 = mMsgEnabler.sendSMS(s, abyte1, s1);
            log((new StringBuilder()).append("SIP Message ID : ").append(i2).toString());
            int l2;
            if(message != null)
            {
                MOMessageData momessagedata = new MOMessageData(i, message);
                Map map = sentMessageMap;
                Integer integer = Integer.valueOf(i2);
                Object obj1 = map.put(integer, momessagedata);
                int j2 = 0;
                do
                {
                    int k2 = sentMessageMap.size();
                    if(j2 >= k2)
                        break;
                    StringBuilder stringbuilder1 = (new StringBuilder()).append(" map value at msgId ").append(i2).append("is ");
                    Map map1 = sentMessageMap;
                    Integer integer1 = Integer.valueOf(i2);
                    Object obj2 = map1.get(integer1);
                    log(stringbuilder1.append(obj2).toString());
                    j2++;
                } while(true);
            } else
            {
                l2 = Log.e("IMSSMSInterface ", "mReply is null");
            }
        }
        return;
    }

    public void sendSMSResponse(int i, int j, String s)
    {
        int k = mMsgEnabler.sendSMSResponse(i, j, s);
        return;
        printStackTrace();
    }

    public void setOnNew3GPP2SMS(Handler handler, int i, Object obj)
    {
        Registrant registrant = new Registrant(handler, i, obj);
        m3GPP2SMSRegistrant = registrant;
    }

    public void setOnNew3GPPSMS(Handler handler, int i, Object obj)
    {
        Registrant registrant = new Registrant(handler, i, obj);
        m3GPPSMSRegistrant = registrant;
    }

    public void unSetOnNew3GPP2SMS()
    {
        m3GPP2SMSRegistrant.clear();
        m3GPP2SMSRegistrant = null;
    }

    public void unSetOnNew3GPPSMS()
    {
        m3GPPSMSRegistrant.clear();
        m3GPPSMSRegistrant = null;
    }
}
