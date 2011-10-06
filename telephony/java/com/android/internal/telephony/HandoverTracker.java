// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   HandoverTracker.java

package com.android.internal.telephony;

import Lcom.android.internal.telephony.HandoverTracker;;
import android.os.*;
import android.util.Log;
import com.android.internal.telephony.cdma.CDMAPhone;
import com.android.internal.telephony.cdma.eCdmaDataConnectionTracker;
import com.android.internal.telephony.gsm.GSMPhone;
import com.android.internal.telephony.gsm.GsmMultiDataConnectionTracker;
import java.util.ArrayList;

// Referenced classes of package com.android.internal.telephony:
//            CommandsInterface, PhoneFactory, HandoverInfo

public final class HandoverTracker extends Handler
{
    public static final class State extends Enum
    {

        public static State valueOf(String s)
        {
            return (State)Enum.valueOf(com/android/internal/telephony/HandoverTracker$State, s);
        }

        public static State[] values()
        {
            return (State[])$VALUES.clone();
        }

        public String toString()
        {
            int ai[];
            int i;
            class _cls1
            {

                static final int $SwitchMap$com$android$internal$telephony$HandoverTracker$State[];

                static 
                {
                    $SwitchMap$com$android$internal$telephony$HandoverTracker$State = new int[State.values().length];
                    try
                    {
                        int ai[] = $SwitchMap$com$android$internal$telephony$HandoverTracker$State;
                        int i = State.IDLE.ordinal();
                        ai[i] = 1;
                    }
                    catch(NoSuchFieldError nosuchfielderror2) { }
                    try
                    {
                        int ai1[] = $SwitchMap$com$android$internal$telephony$HandoverTracker$State;
                        int j = State.LTE_TO_CDMA.ordinal();
                        ai1[j] = 2;
                    }
                    catch(NoSuchFieldError nosuchfielderror1) { }
                    try
                    {
                        int ai2[] = $SwitchMap$com$android$internal$telephony$HandoverTracker$State;
                        int k = State.CDMA_TO_LTE.ordinal();
                        ai2[k] = 3;
                    }
                    catch(NoSuchFieldError nosuchfielderror)
                    {
                        return;
                    }
                }
            }

            ai = _cls1.SwitchMap.com.android.internal.telephony.HandoverTracker.State;
            i = ordinal();
            switch(ai[i]) {
            case 1:
                return "unknown";
            case 2:
                return "IDLE";
            case 3:
                return "L2C";
            default:
                return "unknown";
            }
        }

        private static final State $VALUES[];
        public static final State CDMA_TO_LTE;
        public static final State IDLE;
        public static final State LTE_TO_CDMA;

        static 
        {
            IDLE = new State("IDLE", 0);
            LTE_TO_CDMA = new State("LTE_TO_CDMA", 1);
            CDMA_TO_LTE = new State("CDMA_TO_LTE", 2);
            State astate[] = new State[3];
            State state1 = IDLE;
            astate[0] = state1;
            State state2 = LTE_TO_CDMA;
            astate[1] = state2;
            State state3 = CDMA_TO_LTE;
            astate[2] = state3;
            $VALUES = astate;
        }

        private State(String s, int i)
        {
            super(s, i);
        }
    }


    public HandoverTracker(CommandsInterface commandsinterface, CommandsInterface commandsinterface1)
    {
        String s = com/android/internal/telephony/HandoverTracker.getSimpleName();
        LOG_TAG = s;
        State state1 = State.IDLE;
        state = state1;
        lteRIL = commandsinterface;
        cdmaRIL = commandsinterface1;
        commandsinterface.registerForHandOver(this, 1, null);
        commandsinterface1.registerForHandOver(this, 2, null);
    }

    private void clearHandoverParams(int i)
    {
        CDMAPhone cdmaphone = (CDMAPhone)PhoneFactory.getCdmaPhone();
        GsmMultiDataConnectionTracker gsmmultidataconnectiontracker = (GsmMultiDataConnectionTracker)((GSMPhone)PhoneFactory.getGsmPhone()).mDataConnection;
        eCdmaDataConnectionTracker ecdmadataconnectiontracker = (eCdmaDataConnectionTracker)cdmaphone.mDataConnection;
        switch(i)
        {
        default:
            ecdmadataconnectiontracker.onHOCleanupHOAPN();
            gsmmultidataconnectiontracker.onHOCleanupHOAPN();
            return;

        case 2: // '\002'
            ecdmadataconnectiontracker.onHOCleanupHOAPN();
            return;

        case 1: // '\001'
            gsmmultidataconnectiontracker.onHOCleanupHOAPN();
            return;

        case 3: // '\003'
            ecdmadataconnectiontracker.onHOCleanupHOAPN();
            break;
        }
        gsmmultidataconnectiontracker.onHOCleanupHOAPN();
    }

    public static HandoverTracker defaultHandoverTracker()
    {
        com/android/internal/telephony/HandoverTracker;
        JVM INSTR monitorenter ;
        if(!sMadeDefault)
        {
            sDefaultHandoverTracker = new HandoverTracker(null, null);
            sMadeDefault = true;
        }
        com/android/internal/telephony/HandoverTracker;
        JVM INSTR monitorexit ;
        return sDefaultHandoverTracker;
        Exception exception;
        exception;
        com/android/internal/telephony/HandoverTracker;
        JVM INSTR monitorexit ;
        throw exception;
    }

    private void log(String s)
    {
        String s1 = LOG_TAG;
        String s2 = (new StringBuilder()).append("[Handover] ").append(s).toString();
        int i = Log.d(s1, s2);
    }

    private void setState(State state1)
    {
        state = state1;
    }

    public void clearFields()
    {
        mApnList = null;
    }

    public void dispose()
    {
    }

    public ArrayList getFields()
    {
        return mApnList;
    }

    public State getState()
    {
        return state;
    }

    public void handleMessage(Message message)
    {
        State state13;
        State state14;
        switch(message.what)
        {
        default:
            int i = Log.e(LOG_TAG, "Unexpected event");
            return;

        case 1: // '\001'
            String as[] = (String[])(String[])((AsyncResult)message.obj).result;
            if(as == null)
            {
                log("**L2C response is null! ignore it..");
                return;
            }
            if(as.length != 3)
            {
                log("**L2C response lenghth should be 3");
                return;
            }
            int j = Integer.parseInt(as[0]);
            int k = Integer.parseInt(as[1]);
            int i1 = Integer.parseInt(as[2]);
            String s = (new StringBuilder()).append("**L2C Response: CAUSE=").append(j).append(", STATE=").append(k).append(", ACT=").append(i1).toString();
            log(s);
            State state1 = state;
            State state2 = State.IDLE;
            if(state1 == state2)
            {
                log("**L2C phase 1: suspend from LTE");
                if(k != 0)
                {
                    log("**L2C recieved unexpected state, ignore it..");
                    return;
                } else
                {
                    log("**L2C state changed IDLE -> LTE_TO_CDMA");
                    State state3 = State.LTE_TO_CDMA;
                    state = state3;
                    GsmMultiDataConnectionTracker gsmmultidataconnectiontracker = (GsmMultiDataConnectionTracker)((GSMPhone)PhoneFactory.getGsmPhone()).mDataConnection;
                    clearFields();
                    ArrayList arraylist = gsmmultidataconnectiontracker.getApnListForHandover();
                    setFields(arraylist);
                    Message message1 = obtainMessage(3);
                    boolean flag = sendMessageDelayed(message1, 0x1d4c0L);
                    return;
                }
            }
            State state4 = state;
            State state5 = State.LTE_TO_CDMA;
            if(state4 == state5)
            {
                log("**L2C phase 5: resume from LTE");
                if(k != 1)
                {
                    log("**L2C recieved unexpected state, ignore it..");
                    return;
                }
                log("**L2C state changed LTE_TO_CDMA -> IDLE");
                State state6 = State.IDLE;
                state = state6;
                if(i1 == 13)
                {
                    clearHandoverParams(2);
                    if(mHandoverResumeTimeoutFromLteRegistrant != null)
                        mHandoverResumeTimeoutFromLteRegistrant.notifyRegistrant();
                } else
                if(i1 == 14)
                    clearHandoverParams(1);
                else
                    clearHandoverParams(3);
                removeMessages(3);
                return;
            } else
            {
                log("**L2C error abnormal state!");
                return;
            }

        case 2: // '\002'
            String as1[] = (String[])(String[])((AsyncResult)message.obj).result;
            if(as1 == null)
            {
                log("**C2L response is null! ignore it..");
                return;
            }
            if(as1.length != 3)
            {
                log("**C2L response lenghth should be 3");
                return;
            }
            int k1 = Integer.parseInt(as1[0]);
            int l = Integer.parseInt(as1[1]);
            int j1 = Integer.parseInt(as1[2]);
            String s1 = (new StringBuilder()).append("**C2L Response: CAUSE=").append(k1).append(", STATE=").append(l).append(", ACT=").append(j1).toString();
            log(s1);
            State state7 = state;
            State state8 = State.IDLE;
            if(state7 == state8)
                if(l != 0)
                {
                    log("**C2L recieved unexpected state, ignore it..");
                    return;
                } else
                {
                    log("**C2L phase 1: suspend from eHRPD");
                    log("**C2L state changed IDLE -> CDMA_TO_LTE");
                    State state9 = State.CDMA_TO_LTE;
                    state = state9;
                    ArrayList arraylist1 = ((eCdmaDataConnectionTracker)((CDMAPhone)PhoneFactory.getCdmaPhone()).mDataConnection).getApnListForHandover();
                    boolean flag1 = startHandoverFromEhrpd(arraylist1);
                    Message message2 = obtainMessage(3);
                    boolean flag2 = sendMessageDelayed(message2, 0x1d4c0L);
                    return;
                }
            State state10 = state;
            State state11 = State.CDMA_TO_LTE;
            if(state10 == state11)
            {
                if(l != 1)
                {
                    log("**C2L recieved unexpected state, ignore it..");
                    return;
                }
                log("**C2L phase 5: resume from eHRPD, Handover success");
                log("**C2L state changed CDMA_TO_LTE -> IDLE");
                State state12 = State.IDLE;
                state = state12;
                if(j1 == 14 || j1 == 6)
                {
                    clearHandoverParams(1);
                    if(mHandoverResumeTimeoutFromEhrpdRegistrant != null)
                        mHandoverResumeTimeoutFromEhrpdRegistrant.notifyRegistrant();
                } else
                if(j1 == 13)
                    clearHandoverParams(2);
                else
                    clearHandoverParams(3);
                removeMessages(3);
                return;
            } else
            {
                log("**C2L error abnormal state!");
                return;
            }

        case 3: // '\003'
            state13 = state;
            state14 = State.CDMA_TO_LTE;
            break;
        }
        if(state13 == state14)
        {
            log("**C2L Handover resume doesn't arrive!");
            State state15 = State.IDLE;
            state = state15;
            clearHandoverParams(3);
            return;
        }
        State state16 = state;
        State state17 = State.LTE_TO_CDMA;
        if(state16 == state17)
        {
            log("**L2C Handover resume doesn't arrive!");
            State state18 = State.IDLE;
            state = state18;
            clearHandoverParams(3);
            return;
        } else
        {
            log("**Handover resume timout occured in IDLE???");
            return;
        }
    }

    public boolean isDuringHandover()
    {
        State state1 = state;
        State state2 = State.IDLE;
        boolean flag;
        if(state1 == state2)
            flag = false;
        else
            flag = true;
        return flag;
    }

    public void registerForHandoverResumeTimeoutFromEhrpd(Handler handler, int i, Object obj)
    {
        Registrant registrant = new Registrant(handler, i, obj);
        mHandoverResumeTimeoutFromEhrpdRegistrant = registrant;
    }

    public void registerForHandoverResumeTimeoutFromLte(Handler handler, int i, Object obj)
    {
        Registrant registrant = new Registrant(handler, i, obj);
        mHandoverResumeTimeoutFromLteRegistrant = registrant;
    }

    public void registerForInitiateHandoverFromEhrpd(Handler handler, int i, Object obj)
    {
        Registrant registrant = new Registrant(handler, i, obj);
        mInitiateHandoverFromEhrpdRegistrant = registrant;
    }

    public void registerForInitiateHandoverFromLte(Handler handler, int i, Object obj)
    {
        Registrant registrant = new Registrant(handler, i, obj);
        mInitiateHandoverFromLteRegistrant = registrant;
    }

    public void registerForStartHandoverFromEhrpd(Handler handler, int i, Object obj)
    {
        Registrant registrant = new Registrant(handler, i, obj);
        mStartHandoverFromEhrpdRegistrant = registrant;
    }

    public void registerForStartHandoverFromLte(Handler handler, int i, Object obj)
    {
        Registrant registrant = new Registrant(handler, i, obj);
        mStartHandoverFromLteRegistrant = registrant;
    }

    public void resetState()
    {
        log("** reset state ");
        State state1 = State.IDLE;
        state = state1;
        clearFields();
    }

    public void setFields(ArrayList arraylist)
    {
        mApnList = arraylist;
    }

    public boolean startHandoverFromEhrpd(ArrayList arraylist)
    {
        log("**C2L phase 3 : wake up eHRPD DCT");
        State state1 = state;
        State state2 = State.CDMA_TO_LTE;
        boolean flag;
        if(state1 != state2)
        {
            log("**C2L HandoverTracker didn't receive EVENT_HANDOVER_INITIATED_LTE");
            flag = false;
        } else
        {
            setFields(arraylist);
            if(mStartHandoverFromEhrpdRegistrant != null)
                mStartHandoverFromEhrpdRegistrant.notifyRegistrant();
            flag = true;
        }
        return flag;
    }

    public boolean startHandoverFromLte(ArrayList arraylist)
    {
        log("**L2C phase 3 : wake up LTE DCT");
        State state1 = state;
        State state2 = State.LTE_TO_CDMA;
        boolean flag;
        if(state1 != state2)
        {
            log("**L2C HandoverTracker didn't receive EVENT_HANDOVER_INITIATED_EHRPD");
            flag = false;
        } else
        {
            setFields(arraylist);
            if(mStartHandoverFromLteRegistrant != null)
                mStartHandoverFromLteRegistrant.notifyRegistrant();
            flag = true;
        }
        return flag;
    }

    public void unregisterForHandoverResumeTimeoutFromEhrpd(Handler handler)
    {
        mHandoverResumeTimeoutFromEhrpdRegistrant = null;
    }

    public void unregisterForHandoverResumeTimeoutFromLte(Handler handler)
    {
        mHandoverResumeTimeoutFromLteRegistrant = null;
    }

    public void unregisterForInitiateFromLte(Handler handler)
    {
        mInitiateHandoverFromLteRegistrant = null;
    }

    public void unregisterForInitiateHandoverFromEhrpd(Handler handler)
    {
        mInitiateHandoverFromEhrpdRegistrant = null;
    }

    public void unregisterForStartHandoverFromEhrpd(Handler handler)
    {
        mStartHandoverFromEhrpdRegistrant = null;
    }

    public void unregisterForStartHandoverFromLte(Handler handler)
    {
        mStartHandoverFromLteRegistrant = null;
    }

    protected static final boolean DBG = true;
    private static final int EVENT_HANDOVER_INITIATED_EHRPD = 2;
    private static final int EVENT_HANDOVER_INITIATED_LTE = 1;
    private static final int EVENT_HANDOVER_RESUME_TIMEOUT = 3;
    private static final int HANDOVER_RESUME_WAITING_TIME = 0x1d4c0;
    public static final int RAT_ALL = 3;
    public static final int RAT_CDMA = 2;
    public static final int RAT_LTE = 1;
    private static HandoverTracker sDefaultHandoverTracker;
    private static boolean sMadeDefault = false;
    protected final String LOG_TAG;
    private CommandsInterface cdmaRIL;
    private CommandsInterface lteRIL;
    private ArrayList mApnList;
    private CDMAPhone mCdmaPhone;
    private HandoverInfo mHandoverInfo;
    private Registrant mHandoverResumeTimeoutFromEhrpdRegistrant;
    private Registrant mHandoverResumeTimeoutFromLteRegistrant;
    private Registrant mInitiateHandoverFromEhrpdRegistrant;
    private Registrant mInitiateHandoverFromLteRegistrant;
    private GSMPhone mLtePhone;
    private Registrant mStartHandoverFromEhrpdRegistrant;
    private Registrant mStartHandoverFromLteRegistrant;
    private State state;

}
