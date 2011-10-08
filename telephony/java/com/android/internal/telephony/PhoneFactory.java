/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.internal.telephony;

import android.content.Context;
import android.net.LocalServerSocket;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.os.SystemProperties;

import com.android.internal.telephony.cdma.CDMAPhone;
import com.android.internal.telephony.gsm.GSMPhone;
import com.android.internal.telephony.sip.SipPhone;
import com.android.internal.telephony.sip.SipPhoneFactory;

/**
 * {@hide}
 */
public class PhoneFactory {
    static final String LOG_TAG = "PHONE";
    static final int SOCKET_OPEN_RETRY_MILLIS = 2 * 1000;
    static final int SOCKET_OPEN_MAX_RETRY = 3;
    //***** Class Variables

    static private Phone sProxyPhone = null;
    static private Phone mMultiModePhoneProxy;
    static private Phone sGsmProxyPhone;
    static private Phone sCdmaProxyPhone;
    static private HandoverTracker sHandoverTracker;
    //static private CommandsInterface sCommandsInterface = null;
    static private CommandsInterface[] sCommandsInterfaces = null;
    static
    {
        mMultiModePhoneProxy = null;
        sGsmProxyPhone = null;
        sCdmaProxyPhone = null;
        CommandsInterface[] arrayOfCommandsInterface = new CommandsInterface[2];
        arrayOfCommandsInterface[0] = null;
        arrayOfCommandsInterface[1] = null;
        sCommandsInterfaces = arrayOfCommandsInterface;
    }
        

    static private boolean sMadeDefaults = false;
    static private PhoneNotifier sPhoneNotifier;
    static private Looper sLooper;
    static private Context sContext;

    static final int preferredNetworkMode = RILConstants.PREFERRED_NETWORK_MODE;

    static final int preferredCdmaSubscription = RILConstants.PREFERRED_CDMA_SUBSCRIPTION;

    //***** Class Methods

    public static void makeDefaultPhones(Context context) {
        makeDefaultPhone(context);
    }

    /**
     * FIXME replace this with some other way of making these
     * instances
     */
    public static void makeDefaultPhone(Context context) {
        synchronized(Phone.class) {
            if (!sMadeDefaults) {
                sLooper = Looper.myLooper();
                sContext = context;

                if (sLooper == null) {
                    throw new RuntimeException(
                        "PhoneFactory.makeDefaultPhone must be called from Looper thread");
                }

                int retryCount = 0;
                for(;;) {
                    boolean hasException = false;
                    retryCount ++;

                    try {
                        // use UNIX domain socket to
                        // prevent subsequent initialization
                        new LocalServerSocket("com.android.internal.telephony");
                    } catch (java.io.IOException ex) {
                        hasException = true;
                    }

                    if ( !hasException ) {
                        break;
                    } else if (retryCount > SOCKET_OPEN_MAX_RETRY) {
                        throw new RuntimeException("PhoneFactory probably already running");
                    } else {
                        try {
                            Thread.sleep(SOCKET_OPEN_RETRY_MILLIS);
                        } catch (InterruptedException er) {
                        }
                    }
                }

                sPhoneNotifier = new DefaultPhoneNotifier();

                //Get preferredNetworkMode from Settings.System
                int networkMode = Settings.Secure.getInt(context.getContentResolver(),
                        Settings.Secure.PREFERRED_NETWORK_MODE, preferredNetworkMode);
                Log.i(LOG_TAG, "Network Mode set to " + Integer.toString(networkMode));

                //Get preferredNetworkMode from Settings.System
                int cdmaSubscription = Settings.Secure.getInt(context.getContentResolver(),
                        Settings.Secure.PREFERRED_CDMA_SUBSCRIPTION, preferredCdmaSubscription);
                Log.i(LOG_TAG, "Cdma Subscription set to " + Integer.toString(cdmaSubscription));

                //reads the system properties and makes commandsinterface

                String sRILClassname = SystemProperties.get("ro.telephony.ril_class");
                Log.i(LOG_TAG, "RILClassname is " + sRILClassname);

                /**
                 * Complete hack to see if things work
                 */
                //if("samsung".equals(sRILClassname))
                //{
                    Log.i(LOG_TAG, "Using Samsung RIL");
                    sCommandsInterfaces[0] = new SamsungRIL(context, 3, 0);
                    sCommandsInterfaces[1] = new SamsungRIL(context, 4, cdmaSubscription);
                /*} else if ("htc".equals(sRILClassname)) {
                    Log.i(LOG_TAG, "Using HTC RIL");
                    sCommandsInterface = new HTCRIL(context, networkMode, cdmaSubscription);
                } else if("lgestar".equals(sRILClassname)) {
                    Log.i(LOG_TAG, "Using LGE Star RIL");
                    sCommandsInterface = new LGEStarRIL(context, networkMode, cdmaSubscription);
                } else if ("semc".equals(sRILClassname)) {
                    Log.i(LOG_TAG, "Using Semc RIL");
                    sCommandsInterface = new SemcRIL(context, networkMode, cdmaSubscription);
                } else {
                    sCommandsInterface = new RIL(context, networkMode, cdmaSubscription);
                }*/
                sHandoverTracker = new HandoverTracker(sCommandsInterfaces[0], sCommandsInterfaces[1]);
                sCdmaProxyPhone = new CDMAPhone(context, sCommandsInterfaces[1], sPhoneNotifier, sHandoverTracker);
                sGsmProxyPhone = new GSMPhone(context, sCommandsInterfaces[0], sPhoneNotifier, sHandoverTracker);
                mMultiModePhoneProxy = new MultiModePhoneProxy(sCdmaProxyPhone, sGsmProxyPhone, context);

                /*
                int phoneType = getPhoneType(networkMode);
                if (phoneType == Phone.PHONE_TYPE_GSM) {
                    Log.i(LOG_TAG, "Creating GSMPhone");
                    sProxyPhone = new PhoneProxy(new GSMPhone(context,
                            sCommandsInterface, sPhoneNotifier));
                } else if (phoneType == Phone.PHONE_TYPE_CDMA) {
                    Log.i(LOG_TAG, "Creating CDMAPhone");
                    sProxyPhone = new PhoneProxy(new CDMAPhone(context,
                            sCommandsInterface, sPhoneNotifier));
                }
                */

                sMadeDefaults = true;
            }
        }
    }

    /*
     * This function returns the type of the phone, depending
     * on the network mode.
     *
     * @param network mode
     * @return Phone Type
     */
    public static int getPhoneType(int networkMode) {
        switch(networkMode) {
        case RILConstants.NETWORK_MODE_CDMA:
        case RILConstants.NETWORK_MODE_CDMA_NO_EVDO:
        case RILConstants.NETWORK_MODE_EVDO_NO_CDMA:
            return Phone.PHONE_TYPE_CDMA;

        case RILConstants.NETWORK_MODE_WCDMA_PREF:
        case RILConstants.NETWORK_MODE_GSM_ONLY:
        case RILConstants.NETWORK_MODE_WCDMA_ONLY:
        case RILConstants.NETWORK_MODE_GSM_UMTS:
            return Phone.PHONE_TYPE_GSM;

        case RILConstants.NETWORK_MODE_GLOBAL:
            return Phone.PHONE_TYPE_CDMA;
        default:
            return Phone.PHONE_TYPE_GSM;
        }
    }

    public static Phone getDefaultPhone() {
        if (sLooper != Looper.myLooper()) {
            throw new RuntimeException(
                "PhoneFactory.getDefaultPhone must be called from Looper thread");
        }

        if (!sMadeDefaults) {
            throw new IllegalStateException("Default phones haven't been made yet!");
        }
       return mMultiModePhoneProxy;
    }

    public static Phone getCdmaPhone() {
        synchronized(PhoneProxy.lockForRadioTechnologyChange) {
            if (sCdmaProxyPhone == null)
                sCdmaProxyPhone = new CDMAPhone(sContext, sCommandsInterfaces[1], sPhoneNotifier);
            return sCdmaProxyPhone;
        }
    }

    public static Phone getGsmPhone() {
        synchronized(PhoneProxy.lockForRadioTechnologyChange) {
            if (sGsmProxyPhone == null)
                sGsmProxyPhone = new GSMPhone(sContext, sCommandsInterfaces[0], sPhoneNotifier);
            return sGsmProxyPhone;
        }
    }

    /**
     * Makes a {@link SipPhone} object.
     * @param sipUri the local SIP URI the phone runs on
     * @return the {@code SipPhone} object or null if the SIP URI is not valid
     */
    public static SipPhone makeSipPhone(String sipUri) {
        return SipPhoneFactory.makePhone(sipUri, sContext, sPhoneNotifier);
    }
}
