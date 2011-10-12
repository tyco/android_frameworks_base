// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   AMIL.java

package com.android.internal.telephony.ims.sms;

import android.util.Log;
import com.android.internal.telephony.cdma.sms.CdmaSmsAddress;
import com.android.internal.telephony.cdma.sms.SmsEnvelope;
import com.android.internal.util.BitwiseOutputStream;
import com.android.internal.util.HexDump;
import java.io.*;

public class AMIL
{

    public AMIL()
    {
        SmsEnvelope smsenvelope = new SmsEnvelope();
        envSend = smsenvelope;
        CdmaSmsAddress cdmasmsaddress = new CdmaSmsAddress();
        addrSend = cdmasmsaddress;
    }

    private byte[] createCommon3GPP(byte abyte0[])
    {
        return abyte0;
    }

    private byte[] createCommon3GPP2(byte abyte0[])
    {
        return abyte0;
    }

    private byte[] createRPDU(byte abyte0[])
    {
        return abyte0;
    }

    private byte[] createSMSTransportLayer(byte abyte0[])
    {
        ByteArrayOutputStream bytearrayoutputstream;
        DataOutputStream dataoutputstream;
        bytearrayoutputstream = new ByteArrayOutputStream(100);
        dataoutputstream = new DataOutputStream(bytearrayoutputstream);
        if (reAssemblyData(abyte0)) {
            int i = 0;
            byte abyte3[];
            dataoutputstream.write(i);
            dataoutputstream.write(0);
            dataoutputstream.write(2);
            int j = envSend.teleService;
            dataoutputstream.writeChar(j);
            byte abyte1[] = encodeCdmaAddress(addrSend, 4);
            dataoutputstream.write(abyte1);
            dataoutputstream.write(8);
            int k = bearerDataLength;
            dataoutputstream.write(k);
            byte abyte2[] = envSend.bearerData;
            int l = bearerDataLength;
            dataoutputstream.write(abyte2, 0, l);
            if(envSend.bearerData != null)
                dataoutputstream.close();
            abyte3 = bytearrayoutputstream.toByteArray();
            byte abyte4[] = abyte3;
            return abyte4;
        } else {
            int j1 = Log.e("Common/AMIL", "Reassembly is failed ");
            dataoutputstream.close();
            abyte4 = null;
            return abyte4;
        }
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
        } catch (com.android.internal.util.BitwiseOutputStream.AccessException accessexception) {
            log("bitwise exception is thrown");
            accessexception.printStackTrace();
            abyte1 = null;
            return abyte1;
        }
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

    private static void log(String s)
    {
        String s1 = (new StringBuilder()).append("IMSLog   >>>>> ").append(s).toString();
        int i = Log.d("Common/AMIL", s1);
    }

    public byte[] CreateForPlatform(int i, byte abyte0[])
    {
        byte abyte1[];
        if(i == 1)
            abyte1 = createCommon3GPP(abyte0);
        else
            abyte1 = createCommon3GPP2(abyte0);
        return abyte1;
    }

    public byte[] CreateForStack(int i, byte abyte0[])
    {
        byte abyte1[];
        if(i == 1)
            abyte1 = createRPDU(abyte0);
        else
            abyte1 = createSMSTransportLayer(abyte0);
        return abyte1;
    }

    public boolean reAssemblyData(byte abyte0[])
    {
        try {
            DataInputStream datainputstream;
            ByteArrayInputStream bytearrayinputstream = new ByteArrayInputStream(abyte0);
            datainputstream = new DataInputStream(bytearrayinputstream);
            int i = Log.d("Common/AMIL", "reAssemblyData ");
            StringBuilder stringbuilder = (new StringBuilder()).append("reAssemblyData : ");
            String s = HexDump.toHexString(abyte0);
            String s1 = stringbuilder.append(s).toString();
            int j = Log.d("Common/AMIL", s1);
            SmsEnvelope smsenvelope = envSend;
            int k = datainputstream.readInt();
            smsenvelope.teleService = k;
            int l = datainputstream.readInt();
            SmsEnvelope smsenvelope1 = envSend;
            int i1 = datainputstream.readInt();
            smsenvelope1.serviceCategory = i1;
            CdmaSmsAddress cdmasmsaddress = addrSend;
            int j1 = datainputstream.readByte();
            cdmasmsaddress.digitMode = j1;
            CdmaSmsAddress cdmasmsaddress1 = addrSend;
            int k1 = datainputstream.readByte();
            cdmasmsaddress1.numberMode = k1;
            CdmaSmsAddress cdmasmsaddress2 = addrSend;
            int l1 = datainputstream.readByte();
            cdmasmsaddress2.ton = l1;
            CdmaSmsAddress cdmasmsaddress3 = addrSend;
            int i2 = datainputstream.readByte();
            cdmasmsaddress3.numberPlan = i2;
            int j2 = datainputstream.readByte();
            addrSend.numberOfDigits = j2;
            CdmaSmsAddress cdmasmsaddress4 = addrSend;
            byte abyte1[] = new byte[j2];
            cdmasmsaddress4.origBytes = abyte1;
            byte abyte2[] = addrSend.origBytes;
            int k2 = datainputstream.read(abyte2, 0, j2);
            int l2 = datainputstream.skipBytes(3);
            int i3 = datainputstream.read();
            bearerDataLength = i3;
            SmsEnvelope smsenvelope2 = envSend;
            byte abyte3[] = new byte[bearerDataLength];
            smsenvelope2.bearerData = abyte3;
            byte abyte4[] = envSend.bearerData;
            int j3 = bearerDataLength;
            int k3 = datainputstream.read(abyte4, 0, j3);
            datainputstream.close();
            boolean flag = true;
            return flag;
        } catch (IOException ioexception) {
            String s2 = (new StringBuilder()).append("createFromPdu: conversion from byte array to object failed: ").append(ioexception).toString();
            int l3 = Log.e("Common/AMIL", s2);
            datainputstream.close();
            flag = false;
            printStackTrace();
            return flag;
        }
    }

    private static final String LOG_TAG = "Common/AMIL";
    protected static final int MESSAGE_FORMAT_3GPP = 1;
    protected static final int MESSAGE_FORMAT_3GPP2 = 2;
    protected static final int PARAM_ID_BEARER_DATA = 8;
    protected static final int PARAM_ID_BEARER_REPLY_OPTION = 6;
    protected static final int PARAM_ID_CAUSE_CODES = 7;
    protected static final int PARAM_ID_DESTINATION_ADDRESS = 4;
    protected static final int PARAM_ID_DESTINATION_SUBADDRESS = 5;
    protected static final int PARAM_ID_ORIGINATING_ADDRESS = 2;
    protected static final int PARAM_ID_ORIGINATING_SUBADDRESS = 3;
    protected static final int PARAM_ID_SERVICE_CATEGORY = 1;
    protected static final int PARAM_ID_TELESERVICE = 0;
    protected static final int PARAM_LENGTH_SERVICE_CATEGORY = 2;
    protected static final int PARAM_LENGTH_TELESERVICE = 2;
    private CdmaSmsAddress addrRecv;
    private CdmaSmsAddress addrSend;
    private int bearerDataLength;
    private SmsEnvelope envRecv;
    private SmsEnvelope envSend;
    private int mFormat;
}
