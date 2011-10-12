// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ImsSMSInterface.java

package com.android.internal.telephony.ims.sms;


class MTRecieveData
{

    public MTRecieveData(int i, int j, byte abyte0[])
    {
        pduMsgID = i;
        contentType = j;
        pdu = abyte0;
    }

    int contentType;
    byte pdu[];
    int pduMsgID;
}
