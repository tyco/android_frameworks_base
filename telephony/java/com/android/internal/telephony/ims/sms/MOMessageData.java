// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ImsSMSInterface.java

package com.android.internal.telephony.ims.sms;

import android.os.Message;

class MOMessageData
{

    public MOMessageData(int i, Message message)
    {
        pduMsgID = i;
        replyMsg = message;
    }

    int pduMsgID;
    Message replyMsg;
}
