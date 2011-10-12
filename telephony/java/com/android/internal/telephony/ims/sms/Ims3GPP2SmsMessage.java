// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Ims3GPP2SmsMessage.java

package com.android.internal.telephony.ims.sms;

import android.telephony.PhoneNumberUtils;
import android.util.Log;
import com.android.internal.telephony.SmsHeader;
import com.android.internal.telephony.cdma.SmsMessage;
import com.android.internal.telephony.cdma.sms.*;
import com.android.internal.util.*;
import java.io.*;

public class Ims3GPP2SmsMessage extends SmsMessage
{

    public Ims3GPP2SmsMessage()
    {
    }

    public static SmsMessage createFromPdu(byte abyte0[])
    {
        Ims3GPP2SmsMessage ims3gpp2smsmessage = new Ims3GPP2SmsMessage();
        log("createFromPdu : calling parsePdu");
        ims3gpp2smsmessage.parsePdu(abyte0);
        Ims3GPP2SmsMessage ims3gpp2smsmessage1 = ims3gpp2smsmessage;
        return ims3gpp2smsmessage1;
    }

    public static byte[] encodeCdmaAddress(CdmaSmsAddress cdmasmsaddress, int i)
    {
        BitwiseOutputStream bitwiseoutputstream;
        log("encodeAddress");
        bitwiseoutputstream = new BitwiseOutputStream(50);
        byte abyte0[];
        bitwiseoutputstream.write(8, i);
        int j = getAddressParameterLength(cdmasmsaddress);
        bitwiseoutputstream.write(8, j);
        int k = cdmasmsaddress.digitMode;
        bitwiseoutputstream.write(1, k);
        int l = cdmasmsaddress.numberMode;
        bitwiseoutputstream.write(1, l);
        if(cdmasmsaddress.digitMode == 1)
        {
            int i1 = cdmasmsaddress.ton;
            bitwiseoutputstream.write(3, i1);
        }
        if(cdmasmsaddress.digitMode == 1 && cdmasmsaddress.numberMode == 0)
        {
            int j1 = cdmasmsaddress.numberPlan;
            bitwiseoutputstream.write(4, j1);
        }
        int k1 = cdmasmsaddress.numberOfDigits;
        bitwiseoutputstream.write(8, k1);
        byte byte0;
        int l1;
        if(cdmasmsaddress.digitMode == 0)
            byte0 = 4;
        else
            byte0 = 8;
        l1 = 0;
        do
        {
            int i2 = cdmasmsaddress.numberOfDigits;
            if(l1 >= i2)
                break;
            byte byte1 = cdmasmsaddress.origBytes[l1];
            bitwiseoutputstream.write(byte0, byte1);
            l1++;
        } while(true);
        StringBuilder stringbuilder = (new StringBuilder()).append(" address parameter value with id ").append(i).append(" is :");
        String s = HexDump.toHexString(bitwiseoutputstream.toByteArray());
        log(stringbuilder.append(s).toString());
        try {
            abyte0 = bitwiseoutputstream.toByteArray();
            byte abyte1[] = abyte0;
            return abyte1;
        } catch (AccessException accessexception) {
            log("bitwise exception is thrown");
            accessexception.printStackTrace();
            abyte1 = null;
            return abyte1;
        }
    }

    public static byte[] get3GPP2SMSAckPdu(String s, int i, int j, int k)
    {
        ByteArrayOutputStream bytearrayoutputstream;
        CdmaSmsAddress cdmasmsaddress;
        byte byte0;
        bytearrayoutputstream = new ByteArrayOutputStream();
        cdmasmsaddress = null;
        if(s != null)
            cdmasmsaddress = CdmaSmsAddress.parse(s);
        byte0 = 2;
        byte abyte2[];
        bytearrayoutputstream.write(byte0);
        if(s != null)
        {
            byte abyte0[] = encodeCdmaAddress(cdmasmsaddress, 4);
            bytearrayoutputstream.write(abyte0);
        }
        bytearrayoutputstream.write(7);
        bytearrayoutputstream.write(3);
        if(i > 63)
            log("reply seq no is greater than 63");
        int l = i << 2;
        int i1 = j & 3;
        int j1 = l | i1;
        bytearrayoutputstream.write(j1);
        bytearrayoutputstream.write(k);
        StringBuilder stringbuilder = (new StringBuilder()).append("sms ack pdu format ");
        byte abyte1[] = bytearrayoutputstream.toByteArray();
        log(stringbuilder.append(abyte1).toString());
        abyte2 = bytearrayoutputstream.toByteArray();
        byte abyte3[] = abyte2;
        return abyte3;
    }

    public static int getAddressParameterLength(CdmaSmsAddress cdmasmsaddress)
    {
        log((new StringBuilder()).append("getAddressParameterLength address passed is ").append(cdmasmsaddress).toString());
        int i = 0 + 1 + 1;
        if(cdmasmsaddress.digitMode == 1)
            i += 3;
        if(cdmasmsaddress.digitMode == 1 && cdmasmsaddress.numberMode == 0)
            i += 4;
        int j = i + 8;
        byte byte0;
        int k;
        int l;
        int i1;
        if(cdmasmsaddress.digitMode == 0)
            byte0 = 4;
        else
            byte0 = 8;
        k = cdmasmsaddress.origBytes.length * byte0;
        l = j + k;
        if(l % 8 == 0)
            i1 = l / 8;
        else
            i1 = l / 8 + 1;
        return i1;
    }

    public static BearerData getBearerData(boolean flag, UserData userdata, boolean flag1)
    {
        BearerData bearerdata = new BearerData();
        int i;
        if(flag1)
            bearerdata.messageType = 1;
        else
            bearerdata.messageType = 2;
        i = SmsMessage.getNextMessageId();
        bearerdata.messageId = i;
        bearerdata.deliveryAckReq = flag;
        bearerdata.userAckReq = false;
        bearerdata.readAckReq = false;
        bearerdata.reportReq = false;
        bearerdata.userData = userdata;
        return bearerdata;
    }

    public static com.android.internal.telephony.cdma.SmsMessage.SubmitPdu getDeliverPdu(com.android.internal.telephony.cdma.SmsMessage.SubmitPdu submitpdu)
    {
        return null;
    }

    public static int getMessageID()
    {
        return messageID;
    }

    public static com.android.internal.telephony.SmsMessageBase.DeliverPduBase getSimDeliverPdu(String s, String s1, String s2, String s3, byte abyte0[])
    {
        String s4 = s1;
        String s5 = s2;
        String s6 = s3;
        com.android.internal.telephony.cdma.SmsMessage.DeliverPdu deliverpdu = privateGetDeliverPduforSim(s4, s5, s6, false, null, true);
        Object obj;
        if(deliverpdu == null)
        {
            log("sim deliver pdu formed is null");
            obj = null;
        } else
        {
            com.android.internal.telephony.cdma.SmsMessage.DeliverPdu deliverpdu1 = new com.android.internal.telephony.cdma.SmsMessage.DeliverPdu();
            deliverpdu1.encodedScAddress = null;
            byte abyte1[] = deliverpdu.encodedMessage;
            deliverpdu1.encodedMessage = abyte1;
            obj = deliverpdu1;
        }
        return ((com.android.internal.telephony.SmsMessageBase.DeliverPduBase) (obj));
    }

    public static com.android.internal.telephony.SmsMessageBase.SubmitPduBase getSimSubmitPdu(String s, String s1, String s2, byte abyte0[])
    {
        com.android.internal.telephony.cdma.SmsMessage.SubmitPdu submitpdu = privateGetSubmitPduforSim(s1, s2, false, null, false);
        Object obj;
        if(submitpdu == null)
        {
            log("sim submit pdu formed is null");
            obj = null;
        } else
        {
            com.android.internal.telephony.cdma.SmsMessage.SubmitPdu submitpdu1 = new com.android.internal.telephony.cdma.SmsMessage.SubmitPdu();
            byte abyte1[] = submitpdu.encodedMessage;
            submitpdu1.encodedMessage = abyte1;
            obj = submitpdu1;
        }
        return ((com.android.internal.telephony.SmsMessageBase.SubmitPduBase) (obj));
    }

    public static com.android.internal.telephony.cdma.SmsMessage.SubmitPdu getSubmitPdu(String s, UserData userdata, boolean flag)
    {
        log("getSubmitPdu: calling privateGetSubmitPdu");
        return privateGetSubmitPdu(s, flag, userdata);
    }

    public static com.android.internal.telephony.cdma.SmsMessage.SubmitPdu getSubmitPdu(String s, String s1, String s2, boolean flag, SmsHeader smsheader)
    {
        log("getSubmitPdu");
        com.android.internal.telephony.cdma.SmsMessage.SubmitPdu submitpdu;
        if(s2 == null || s1 == null)
        {
            submitpdu = null;
        } else
        {
            UserData userdata = new UserData();
            userdata.payloadStr = s2;
            userdata.userDataHeader = smsheader;
            log("getSubmitPdu:uData is calling privateGetSubmitPdu");
            submitpdu = privateGetSubmitPdu(s1, flag, userdata);
        }
        return submitpdu;
    }

    private static void log(String s)
    {
        String s1 = (new StringBuilder()).append("IMSLog").append(s).toString();
        int i = Log.d("IMS/Ims3GPP2SmsMessage", s1);
    }

    public static CdmaSmsAddress parseCdmaAddress(byte abyte0[])
    {
        CdmaSmsAddress cdmasmsaddress;
        BitwiseInputStream bitwiseinputstream;
        log("parseAddress");
        cdmasmsaddress = new CdmaSmsAddress();
        bitwiseinputstream = new BitwiseInputStream(abyte0);
        int i = bitwiseinputstream.read(1);
        cdmasmsaddress.digitMode = i;
        int j = bitwiseinputstream.read(1);
        cdmasmsaddress.numberMode = j;
        byte byte0;
        int i1;
        byte abyte1[];
        int j1;
        if(cdmasmsaddress.digitMode == 0)
            byte0 = 4;
        else
            byte0 = 8;
        if(cdmasmsaddress.digitMode == 1)
        {
            int k = bitwiseinputstream.read(3);
            cdmasmsaddress.ton = k;
        }
        if(cdmasmsaddress.digitMode == 1 && cdmasmsaddress.numberMode == 0)
        {
            int l = bitwiseinputstream.read(4);
            cdmasmsaddress.numberPlan = l;
        }
        i1 = bitwiseinputstream.read(8);
        cdmasmsaddress.numberOfDigits = i1;
        abyte1 = new byte[cdmasmsaddress.numberOfDigits];
        cdmasmsaddress.origBytes = abyte1;
        j1 = 0;
        do
        {
            int k1 = cdmasmsaddress.numberOfDigits;
            if(j1 >= k1)
                break;
            byte abyte2[] = cdmasmsaddress.origBytes;
            byte byte1 = (byte)(bitwiseinputstream.read(byte0) + 48);
            abyte2[j1] = byte1;
            if(cdmasmsaddress.origBytes[j1] == 58)
                cdmasmsaddress.origBytes[j1] = 48;
            j1++;
        } while(true);
        StringBuilder stringbuilder = (new StringBuilder()).append("address received :");
        byte abyte3[] = cdmasmsaddress.origBytes;
        String s = new String(abyte3);
        log(stringbuilder.append(s).toString());
        CdmaSmsAddress cdmasmsaddress1 = cdmasmsaddress;
        return cdmasmsaddress1;
    }

    private static com.android.internal.telephony.cdma.SmsMessage.DeliverPdu privateGetDeliverPduforSim(String s, String s1, String s2, boolean flag, SmsHeader smsheader, boolean flag1)
    {
        log("privateGetSubmitPduforSim:");
        if(s1 != null && s != null) { //goto _L2; else goto _L1
            UserData userdata = new UserData();
            userdata.payloadStr = s1;
            userdata.userDataHeader = smsheader;
            if(s.charAt(0) == '+' && s.length() > 1)
            {
                StringBuilder stringbuilder = (new StringBuilder()).append("011");
                String s3 = s.substring(1);
                String s4 = stringbuilder.append(s3).toString();
                s = new String(s4);
            }
            CdmaSmsAddress cdmasmsaddress = CdmaSmsAddress.parse(s);
            if(cdmasmsaddress == null)
            {
                obj = null;
                return obj; /* Loop/switch isn't completed */
            }
            s1 = getBearerData(flag, userdata, flag1);
            s1.msgDeliveryTime = s2;
            flag = BearerData.encode(s1);
            if(flag == null)
            {
                obj = null;
                return obj; /* Loop/switch isn't completed */
            }
            int i;
            SmsEnvelope smsenvelope;
            ByteArrayOutputStream bytearrayoutputstream;
            int j;
            int k;
            int l;
            int i1;
            int j1;
            int k1;
            byte abyte0[];
            int l1;
            int i2;
            int j2;
            byte abyte1[];
            StringBuilder stringbuilder1;
            String s5;
            String s6;
            int k2;
            if(((BearerData) (s1)).hasUserDataHeader)
                i = 4101;
            else
                i = 4098;
            smsenvelope = new SmsEnvelope();
            smsenvelope.messageType = 0;
            smsenvelope.teleService = i;
            smsenvelope.serviceCategory = 0;
            smsenvelope.origAddress = cdmasmsaddress;
            smsenvelope.bearerReply = 1;
            smsenvelope.bearerData = flag;
            bytearrayoutputstream = new ByteArrayOutputStream(100);
            s2 = new DataOutputStream(bytearrayoutputstream);
            j = smsenvelope.teleService;
            s2.writeInt(j);
            s2.writeInt(0);
            s2.writeInt(0);
            k = cdmasmsaddress.digitMode;
            s2.write(k);
            l = cdmasmsaddress.numberMode;
            s2.write(l);
            i1 = cdmasmsaddress.ton;
            s2.write(i1);
            j1 = cdmasmsaddress.numberPlan;
            s2.write(j1);
            k1 = cdmasmsaddress.numberOfDigits;
            s2.write(k1);
            abyte0 = cdmasmsaddress.origBytes;
            l1 = cdmasmsaddress.origBytes.length;
            s2.write(abyte0, 0, l1);
            s2.write(0);
            s2.write(0);
            s2.write(0);
            i2 = flag.length;
            s2.write(i2);
            j2 = flag.length;
            s2.write(flag, 0, j2);
            flag = new com.android.internal.telephony.cdma.SmsMessage.DeliverPdu();
            abyte1 = bytearrayoutputstream.toByteArray();
            flag.encodedMessage = abyte1;
            stringbuilder1 = (new StringBuilder()).append("Sim deliver pdu in hex string is ");
            s5 = HexDump.toHexString(bytearrayoutputstream.toByteArray());
            s6 = stringbuilder1.append(s5).toString();
            k2 = Log.d("IMS/Ims3GPP2SmsMessage", s6);
            flag.encodedScAddress = null;
            messageID = ((BearerData) (s1)).messageId;
            log((new StringBuilder()).append("the final sim deliver pdu is ").append(flag).toString());
            s2.close();
            obj = flag;
            return obj;
        } else {
            Object obj = null;
            return ((com.android.internal.telephony.cdma.SmsMessage.DeliverPdu) (obj));
        }

    }

    private static com.android.internal.telephony.cdma.SmsMessage.SubmitPdu privateGetSubmitPdu(String s, boolean flag, UserData userdata)
    {
        CdmaSmsAddress cdmasmsaddress;
        String s1 = PhoneNumberUtils.cdmaCheckAndProcessPlusCodeByNumberFormat(s, 1, 1);
        String s2 = (new StringBuilder()).append("NANP : ").append(s1).toString();
        int i = Log.d("IMS/Ims3GPP2SmsMessage", s2);
        cdmasmsaddress = CdmaSmsAddress.parse(s1);
        if (cdmasmsaddress == null) {
            com.android.internal.telephony.cdma.SmsMessage.SubmitPdu submitpdu = null;
            return submitpdu;
        }
        flag = getBearerData(flag, userdata, false);
        messageID = ((BearerData) (flag)).messageId;
        byte abyte0[] = BearerData.encode(flag);
        if(abyte0 == null)
        {
            submitpdu = null;
            continue; /* Loop/switch isn't completed */
        }
        com.android.internal.telephony.cdma.SmsMessage.SubmitPdu submitpdu1;
        int j;
        SmsEnvelope smsenvelope;
        StringBuilder stringbuilder;
        byte byte0;
        ByteArrayOutputStream bytearrayoutputstream;
        int k;
        int l;
        byte abyte1[];
        int i1;
        int j1;
        byte abyte2[];
        StringBuilder stringbuilder1;
        String s3;
        String s4;
        int k1;
        int l1;
        if(((BearerData) (flag)).hasUserDataHeader)
            j = 4101;
        else
            j = 4098;
        smsenvelope = new SmsEnvelope();
        smsenvelope.messageType = 0;
        smsenvelope.teleService = j;
        smsenvelope.serviceCategory = 0;
        smsenvelope.destAddress = cdmasmsaddress;
        smsenvelope.bearerData = abyte0;
        stringbuilder = (new StringBuilder()).append("reply sequence no in sms submit pdu is : ");
        byte0 = smsenvelope.replySeqNo;
        log(stringbuilder.append(byte0).toString());
        bytearrayoutputstream = new ByteArrayOutputStream(100);
        userdata = new DataOutputStream(bytearrayoutputstream);
        k = smsenvelope.messageType;
        userdata.write(k);
        userdata.write(0);
        userdata.write(2);
        l = smsenvelope.teleService;
        userdata.writeChar(l);
        abyte1 = encodeCdmaAddress(smsenvelope.destAddress, 4);
        userdata.write(abyte1);
        userdata.write(8);
        i1 = abyte0.length;
        userdata.write(i1);
        j1 = abyte0.length;
        userdata.write(abyte0, 0, j1);
        submitpdu1 = new com.android.internal.telephony.cdma.SmsMessage.SubmitPdu();
        abyte2 = bytearrayoutputstream.toByteArray();
        submitpdu1.encodedMessage = abyte2;
        stringbuilder1 = (new StringBuilder()).append("submit pdu in hex string is ");
        s3 = HexDump.toHexString(bytearrayoutputstream.toByteArray());
        s4 = stringbuilder1.append(s3).toString();
        k1 = Log.e("IMS/Ims3GPP2SmsMessage", s4);
        submitpdu1.encodedScAddress = null;
        if(flag != null)
        {
            l1 = ((BearerData) (flag)).messageId;
            submitpdu1.mMsgRef = l1;
        }
        userdata.close();
        submitpdu = submitpdu1;
        return submitpdu;
    }

    private static com.android.internal.telephony.cdma.SmsMessage.SubmitPdu privateGetSubmitPduforSim(String s, String s1, boolean flag, SmsHeader smsheader, boolean flag1)
    {
        log("privateGetSubmitPduforSim:");
        if (s1 == null || s == null) {
            Object obj = null;
            return ((com.android.internal.telephony.cdma.SmsMessage.SubmitPdu) (obj));
        }
        Object obj1 = new UserData();
        obj1.payloadStr = s1;
        obj1.userDataHeader = smsheader;
        if(s.charAt(0) == '+' && s.length() > 1)
        {
            StringBuilder stringbuilder = (new StringBuilder()).append("011");
            String s2 = s.substring(1);
            String s3 = stringbuilder.append(s2).toString();
            s = new String(s3);
        }
        smsheader = CdmaSmsAddress.parse(s);
        if(smsheader == null)
        {
            obj = null;
            return ((com.android.internal.telephony.cdma.SmsMessage.SubmitPdu) (obj));
        }
        s1 = getBearerData(flag, ((UserData) (obj1)), flag1);
        flag1 = BearerData.encode(s1);
        if(flag1 == null)
        {
            obj = null;
            return ((com.android.internal.telephony.cdma.SmsMessage.SubmitPdu) (obj));
        }
        int i;
        ByteArrayOutputStream bytearrayoutputstream;
        int j;
        int k;
        int l;
        int i1;
        int j1;
        int k1;
        byte abyte0[];
        int l1;
        int i2;
        int j2;
        byte abyte1[];
        StringBuilder stringbuilder1;
        String s4;
        String s5;
        int k2;
        if(((BearerData) (s1)).hasUserDataHeader)
            i = 4101;
        else
            i = 4098;
        obj1 = new SmsEnvelope();
        obj1.messageType = 0;
        obj1.teleService = i;
        obj1.serviceCategory = 0;
        obj1.destAddress = smsheader;
        obj1.bearerReply = 1;
        obj1.bearerData = flag1;
        bytearrayoutputstream = new ByteArrayOutputStream(100);
        flag = new DataOutputStream(bytearrayoutputstream);
        j = ((SmsEnvelope) (obj1)).teleService;
        flag.writeInt(j);
        flag.writeInt(0);
        flag.writeInt(0);
        k = ((CdmaSmsAddress) (smsheader)).digitMode;
        flag.write(k);
        l = ((CdmaSmsAddress) (smsheader)).numberMode;
        flag.write(l);
        i1 = ((CdmaSmsAddress) (smsheader)).ton;
        flag.write(i1);
        j1 = ((CdmaSmsAddress) (smsheader)).numberPlan;
        flag.write(j1);
        k1 = ((CdmaSmsAddress) (smsheader)).numberOfDigits;
        flag.write(k1);
        abyte0 = ((CdmaSmsAddress) (smsheader)).origBytes;
        l1 = ((CdmaSmsAddress) (smsheader)).origBytes.length;
        flag.write(abyte0, 0, l1);
        flag.write(0);
        flag.write(0);
        flag.write(0);
        i2 = flag1.length;
        flag.write(i2);
        j2 = flag1.length;
        flag.write(flag1, 0, j2);
        smsheader = new com.android.internal.telephony.cdma.SmsMessage.SubmitPdu();
        abyte1 = bytearrayoutputstream.toByteArray();
        smsheader.encodedMessage = abyte1;
        stringbuilder1 = (new StringBuilder()).append("Sim submit pdu in hex string is ");
        s4 = HexDump.toHexString(bytearrayoutputstream.toByteArray());
        s5 = stringbuilder1.append(s4).toString();
        k2 = Log.d("IMS/Ims3GPP2SmsMessage", s5);
        smsheader.encodedScAddress = null;
        messageID = ((BearerData) (s1)).messageId;
        log((new StringBuilder()).append("the final sim deliver pdu is ").append(smsheader).toString());
        flag.close();
        obj = smsheader;
        return ((com.android.internal.telephony.cdma.SmsMessage.SubmitPdu) (obj));
    }

    public SmsEnvelope getSmsEnvelope()
    {
        return mEnvelope;
    }

    protected void parsePdu(byte abyte0[])
    {
        int i1;
        DataInputStream datainputstream;
        SmsEnvelope smsenvelope;
        byte abyte1[] = abyte0;
        mPdu = abyte1;
        byte abyte2[] = new byte[abyte0.length - 1];
        int i = abyte0.length - 1;
        byte abyte3[] = abyte0;
        int j = 0;
        byte abyte4[] = abyte2;
        int k = 0;
        int l = i;
        System.arraycopy(abyte3, j, abyte4, k, l);
        StringBuilder stringbuilder = (new StringBuilder()).append("pdu to parse : ");
        String s = HexDump.toHexString(abyte2);
        log(stringbuilder.append(s).toString());
        i1 = abyte2.length;
        ByteArrayInputStream bytearrayinputstream = new ByteArrayInputStream();
        ByteArrayInputStream bytearrayinputstream1 = bytearrayinputstream;
        byte abyte5[] = abyte2;
        bytearrayinputstream1.ByteArrayInputStream(abyte5);
        datainputstream = new DataInputStream(bytearrayinputstream);
        smsenvelope = new SmsEnvelope();
        boolean flag = false;
        mIsBearerReplyOptionPresent = flag;
        int k1;
        int j1 = datainputstream.read();
        smsenvelope.messageType = j1;
        k1 = 0 + 1;
//_L10:
        int l1;
        int j2;
        while (k1 < i1) {
            l1 = datainputstream.read();
            int i2 = k1 + 1;
            j2 = datainputstream.read();
            k1 = i2 + 1;
            switch (l1) {
                case 0:
                    //L2
                    break;
                case 1:
                    //L3
                    int i3 = datainputstream.readChar();
                    smsenvelope.serviceCategory = i3;
                case 2:
                    //L4
                    byte abyte6[] = new byte[j2];
                    int j3 = datainputstream.read(abyte6);
                    CdmaSmsAddress cdmasmsaddress = parseCdmaAddress(abyte6);
                    smsenvelope.origAddress = cdmasmsaddress;
                case 3:
                    //L1
                    break;
                case 4:
                    //L5
                    byte abyte7[] = new byte[j2];
                    int k3 = datainputstream.read(abyte7);
                    CdmaSmsAddress cdmasmsaddress1 = parseCdmaAddress(abyte7);
                    smsenvelope.destAddress = cdmasmsaddress1;
                case 5:
                    //L1
                    break;
                case 6:
                    //L6
                    boolean flag1 = true;
                    mIsBearerReplyOptionPresent = flag1;
                    int l3 = datainputstream.read() >>> 2;
                    smsenvelope.bearerReply = l3;
                case 7:
                    //L7
                    byte byte0 = datainputstream.readByte();
                    byte byte1 = (byte)(byte0 & 0xfc);
                    smsenvelope.replySeqNo = byte1;
                    int i4 = smsenvelope.bearerReply;
                    mReplySeqNo = i4;
                    byte byte2 = (byte)(byte0 & 2);
                    smsenvelope.errorClass = byte2;
                    if(smsenvelope.errorClass != null)
                    {
                        byte byte3 = datainputstream.readByte();
                        smsenvelope.causeCode = byte3;
                        int j4 = smsenvelope.causeCode;
                        mCauseCode = j4;
                    }
                    int k4 = 65534;
                    mCauseCode = k4;
                    StringBuilder stringbuilder2 = (new StringBuilder()).append("mt 3gpp2 sms ack params  --- cause code : ");
                    int l4 = mCauseCode;
                    StringBuilder stringbuilder3 = stringbuilder2.append(l4).append(" error class : ");
                    byte byte4 = smsenvelope.errorClass;
                    StringBuilder stringbuilder4 = stringbuilder3.append(byte4).append("  reply seq no : ");
                    int i5 = mReplySeqNo;
                    log(stringbuilder4.append(i5).toString());
                case 8:
                    //L8
                    break;
                default:
                    // L1;
                    break;
            }
            k1 += j2;
        }
        SmsEnvelope smsenvelope1 = smsenvelope;
        mEnvelope = smsenvelope1;
        CdmaSmsAddress cdmasmsaddress2 = mEnvelope.origAddress;
        originatingAddress = cdmasmsaddress2;
        CdmaSmsAddress cdmasmsaddress3 = mEnvelope.destAddress;
        recipientAddress = cdmasmsaddress3;
        boolean flag2 = true;
        mImsHeaderPresent = flag2;
        datainputstream.close();
        parseSms();
        return;
    }

    static final String LOG_TAG = "IMS/Ims3GPP2SmsMessage";
    public static final int NO_ERROR = 254;
    static final int PARAM_ID_BEARER_DATA = 8;
    static final int PARAM_ID_BEARER_REPLY_OPTION = 6;
    static final int PARAM_ID_CAUSE_CODES = 7;
    static final int PARAM_ID_DESTINATION_ADDRESS = 4;
    static final int PARAM_ID_DESTINATION_SUBADDRESS = 5;
    static final int PARAM_ID_ORIGINATING_ADDRESS = 2;
    static final int PARAM_ID_ORIGINATING_SUBADDRESS = 3;
    static final int PARAM_ID_SERVICE_CATEGORY = 1;
    static final int PARAM_ID_TELESERVICE = 0;
    static final int PARAM_LENGTH_SERVICE_CATEGORY = 2;
    static final int PARAM_LENGTH_TELESERVICE = 2;
    private static int messageID = 0;

}
