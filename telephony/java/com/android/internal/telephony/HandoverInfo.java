// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   HandoverInfo.java

package com.android.internal.telephony;

import android.util.Log;
import java.util.ArrayList;

public class HandoverInfo
{

    public HandoverInfo()
    {
    }

    public HandoverInfo(ArrayList arraylist)
    {
        epsBearerList = arraylist;
    }

    public static void clearFields()
    {
        epsBearerList = null;
    }

    public static ArrayList getFields()
    {
        return epsBearerList;
    }

    protected static void log(String s)
    {
        String s1 = (new StringBuilder()).append("[HandoverInfo] ").append(s).toString();
        int i = Log.d("HandoverInfo", s1);
    }

    public static void setFields(ArrayList arraylist)
    {
        epsBearerList = arraylist;
    }

    protected static final String LOG_TAG = "HandoverInfo";
    private static ArrayList epsBearerList;
}
