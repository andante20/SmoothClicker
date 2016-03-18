/*
    Copyright 2016 Pierre-Yves Lapersonne (aka. "pylapp",  pylapp(dot)pylapp(at)gmail(dot)com)

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */
// ✿✿✿✿ ʕ •ᴥ•ʔ/ ︻デ═一

package com.pylapp.smoothclicker.clicker;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.pylapp.smoothclicker.notifiers.NotificationsManager;
import com.pylapp.smoothclicker.tools.Logger;
import com.pylapp.smoothclicker.utils.Config;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Service to call our to start from the outside (e.g. a third party app) so as to trigger the cliking process
 * without using the GUI and its dedicated Activity instances.
 *
 * @author pylapp
 * @version 1.0.0
 * @since 18/03/2016
 * @see IntentService
 * @ßee ATClicker
 */
public class ServiceClicker extends IntentService {


     /* ********** *
     * ATTRIBUTES *
     * ********** */

    /**
     * Represents an external process. Enables writing to, reading from, destroying,
     * and waiting for it, as well as querying its exit value.
     * It is sued to get a Super User access.
     */
    private Process mProcess;

    /**
     * The output stream of the {@link Process} object
     */
    private DataOutputStream mOutputStream;

    /**
     * The application context
     */
    private Context mContext;

    /**
     * The list of points to click on as {x0, y0, x1, y1, ..., xN, yN}
     */
    private ArrayList<Integer> mPoints;
    /**
     * The type of start
     */
    private boolean mIsStartDelayed;
    /**
     * The delay of the start
     */
    private int mDelay;
    /**
     * The time to wait between each click
     */
    private int mTimeGap;
    /**
     * The amount of repeat to do
     */
    private int mRepeat;
    /**
     * If the repeat is endless
     */
    private boolean mIsRepeatEndless;
    /**
     * Vibrate on start
     */
    private boolean mVibrateOnStart;
    /**
     * Vibrate on click
     */
    private boolean mVibrateOnClick;
    /**
     * Display notifications on click
     */
    private boolean mNotif;


    /* ********* *
     * CONSTANTS *
     * ********* */

    /**
     * Action to awake the service (i.e. from the broadcast receiver listening broadcasts of BOOT, etc.)
     */
    public static final String SERVICE_CLICKER_INTENT_FILTER_NAME_WAKEUP = "com.pylapp.smoothclicker.clicker.ServiceClicker.WAKEUP";
    /**
     * Action to start the service
     */
    public static final String SERVICE_CLICKER_INTENT_FILTER_NAME_START = "com.pylapp.smoothclicker.clicker.ServiceClicker.START";
    /**
     * Action to stop the service
     */
    public static final String SERVICE_CLICKER_INTENT_FILTER_NAME_STOP = "com.pylapp.smoothclicker.clicker.ServiceClicker.STOP";

    // Should be equal as R.string.service_label_serviceclicker
    private static final String SERVICE_LABEL_SERVICECLICKER = "Service Clicker of Smooth Clicker";

    /**
     * The key to use to store the delayed start (boolean value) in a bundle
     */
    public static final String BUNDLE_KEY_DELAYED_START = "0x000011";
    /**
     * The key to use to store the delay (integer value) in a bundle
     */
    public static final String BUNDLE_KEY_DELAY = "0x000012";
    /**
     * The key to use to store the time gap to wait between clicks (integer value) in a bundle
     */
    public static final String BUNDLE_KEY_TIME_GAP = "0x000013";
    /**
     * The key to use to store the amount of repetition (integer value) in a bundle
     */
    public static final String BUNDLE_KEY_REPEAT = "0x000021";
    /**
     * The key to use to store the repeat endless (boolean value) in a bundle
     */
    public static final String BUNDLE_KEY_REPEAT_ENDLESS = "0x000022";
    /**
     * The key to use to store the vibrate on start (boolean value) in a bundle
     */
    public static final String BUNDLE_KEY_VIBRATE_ON_START = "0x000031";
    /**
     * The key to use to store the vibrate on each click (boolean value) in a bundle
     */
    public static final String BUNDLE_KEY_VIBRATE_ON_CLICK = "0x000032";
    /**
     * The key to use to store the notification (boolean value) in a bundle
     */
    public static final String BUNDLE_KEY_NOTIFICATIONS = "0x000041";
    /**
     * The key to use to store the points to click on (ArrayList of Integers value) in a bundle
     */
    public static final String BUNDLE_KEY_POINTS = "0x000051";

    /**
     * The key for the status of the service sent within a broadcast
     */
    public static final String BROADCAST_KEY_STATUS = "com.pylapp.smoothclicker.clicker.ServiceClicker.STATUS";

    private static final String BROADCAST_ACTION = "com.pylapp.smoothclicker.clicker.ServiceClicker.BROADCAST";


    private static final String LOG_TAG = "ServiceClicker";


    /* *********** *
     * CONSTRUCTOR *
     * *********** */

    /**
     * Default constructor
     */
    public ServiceClicker() {
        super(SERVICE_LABEL_SERVICECLICKER);
    }


    /* ************************** *
     * METHODS FROM IntentService *
     * ************************** */

    /**
     * Triggered when somebody wants to bind to the service.
     * It checks the action the service received : wake up, stop or startn and then does nothing, stops or continues.
     * After that it gets from the intent the configuration, and fills it with default valeus if needed ni the SharedPreferences.
     * Then, it triggers the clicking task.
     *
     * @param intent -
     */
    @Override
    protected void onHandleIntent( Intent intent ){

        if ( intent == null){
            broadcastStatus(StatusTypes.BAD_CONFIG);
            return;
        }

        Logger.d(LOG_TAG, "Starts of the ServiceClicker");

        /*
         * Step 1 : Get the intent : should we start or stop ?
         */
        final String action = intent.getAction();

         /*
         * Step 2 : Broadcast the status
         */
        switch ( action ){
            case SERVICE_CLICKER_INTENT_FILTER_NAME_WAKEUP:
                broadcastStatus(StatusTypes.AWAKE);
                return;
            case SERVICE_CLICKER_INTENT_FILTER_NAME_STOP:
                broadcastStatus(StatusTypes.TERMINATED);
                stopSelf();
                break;
            case SERVICE_CLICKER_INTENT_FILTER_NAME_START:
                broadcastStatus(StatusTypes.STARTED);
                break;
            default:
                broadcastStatus(StatusTypes.BAD_CONFIG);
                stopSelf();
                break;
        }

        /*
         * Step 3a : Saves the config
         */

        mIsStartDelayed = intent.getBooleanExtra(BUNDLE_KEY_DELAYED_START, Config.DEFAULT_START_DELAYED);

        mDelay = intent.getIntExtra(BUNDLE_KEY_DELAY, Integer.parseInt(Config.DEFAULT_DELAY));

        mTimeGap = intent.getIntExtra(BUNDLE_KEY_TIME_GAP, Integer.parseInt(Config.DEFAULT_TIME_GAP));

        mRepeat = intent.getIntExtra(BUNDLE_KEY_REPEAT, Integer.parseInt(Config.DEFAULT_REPEAT));

        mIsRepeatEndless = intent.getBooleanExtra(BUNDLE_KEY_REPEAT_ENDLESS, Config.DEFAULT_REPEAT_ENDLESS);

        mVibrateOnStart = intent.getBooleanExtra(BUNDLE_KEY_VIBRATE_ON_START, Config.DEFAULT_VIBRATE_ON_START);

        mVibrateOnClick = intent.getBooleanExtra(BUNDLE_KEY_VIBRATE_ON_CLICK, Config.DEFAULT_VIBRATE_ON_CLICK);

        mNotif = intent.getBooleanExtra(BUNDLE_KEY_NOTIFICATIONS, Config.DEFAULT_NOTIF_ON_CLICK);

        mPoints = intent.getIntegerArrayListExtra(BUNDLE_KEY_POINTS);

        if ( mPoints == null || mPoints.size() <= 0 ){
            broadcastStatus(StatusTypes.BAD_CONFIG);
            return;
        }

        /*
         * Step 3b : Broadcast the status
         */
        broadcastStatus(StatusTypes.WORKING);

        /*
         * Step 4 : Starts the clicking process
         */
        executeTaps();

        /*
         * Step 5 : Broadcast the status when the process is done
         */
        broadcastStatus(StatusTypes.TERMINATED);

        /*
         * Step 6 : Finish !
         */


    }


    /* ************* *
     * OTHER METHODS *
     * ************* */

    /**
     * Execute the clicks with the same way as for the ATClicker
     */
    // FIXME Find a way to factorise the sources, design patterns !
    private void executeTaps(){

        /*
         * Step 1 : Get the process as "su"
         */
        try {
            Logger.d(LOG_TAG, "Get 'su' process...");
            mProcess = Runtime.getRuntime().exec("su");
        } catch ( IOException e ){
            Logger.e(LOG_TAG, "Exception thrown during 'su' : " + e.getMessage());
            e.printStackTrace();
        }

        /*
         * Step 2 : Get the process output stream
         */
        Logger.d(LOG_TAG, "Get 'su' process data output stream...");
        mOutputStream = new DataOutputStream(mProcess.getOutputStream());

        /*
         * Step 3 : Execute the command, the same we can execute from ADB within a terminal and deal with the configuration
             $ adb devices
             > ...
             $ adb shell
                $ input tap XXX YYY
         */

        // Should we delay the execution ?
        if ( mIsStartDelayed ){
            Logger.d(LOG_TAG, "The start is delayed, will sleep : "+mDelay);
            final int count = mDelay;
            // Loop for each second
            for ( int i = 1; i <= count; i++ ){
                try {
                    NotificationsManager.getInstance(mContext).makeCountDownNotification(mDelay-i);
                    Thread.sleep(1000); // Sleep of 1 second
                } catch ( InterruptedException ie ){}
            }
            NotificationsManager.getInstance(mContext).stopAllNotifications();
        }

        NotificationsManager.getInstance(mContext).makeClicksOnGoingNotification();

        /*
         * Is the execution endless ?
         */
        if (mIsRepeatEndless) {

            while (true) {
                Logger.d(LOG_TAG, "Should repeat the process ENDLESSLY");
                executeTap();
                // Should we wait before the next action ?
                if ( mTimeGap > 0 ){
                    try {
                        Logger.d(LOG_TAG, "Should wait before each process occurrences : "+mTimeGap);
                        Thread.sleep(mTimeGap*1000);
                    } catch ( InterruptedException ie ){}
                } else {
                    Logger.d(LOG_TAG, "Should NOT wait before each process occurrences : "+mTimeGap);
                }
            }

        /*
         * Should we repeat the execution ?
         */
        } else if ( mRepeat > 1 ){

            Logger.d(LOG_TAG, "Should repeat the process : " + mRepeat);
            for ( int i = 0; i < mRepeat; i++ ){
                executeTap();
                // Should we wait before the next action ?
                if ( mTimeGap > 0 ){
                    try {
                        Logger.d(LOG_TAG, "Should wait before each process occurrences : "+mTimeGap);
                        Thread.sleep(mTimeGap*1000);
                    } catch ( InterruptedException ie ){}
                } else {
                    Logger.d(LOG_TAG, "Should NOT wait before each process occurrences : "+mTimeGap);
                }
            }

        /*
         * Just one execution
         */
        } else {
            Logger.d(LOG_TAG, "Should NOT repeat the process : "+mRepeat);
            executeTap();
        }

        NotificationsManager.getInstance(mContext).stopAllNotifications();
        NotificationsManager.getInstance(mContext).makeClicksOverNotification();
        Logger.d(LOG_TAG, "The input event seems to be triggered");

    }

    /**
     * Executes the tap action
     */
    private void executeTap(){

        for ( int i = 0; i < mPoints.size(); i+=2 ){


            int x = mPoints.get(i);
            int y = mPoints.get(i+1);

            String shellCmd = "/system/bin/input tap " + x + " " + y + "\n";
            Logger.d(LOG_TAG, "The system command will be executed : " + shellCmd);
            try {
                if ( mProcess == null || mOutputStream == null ) throw new IllegalStateException("The process or its stream is not defined !");
                mOutputStream.writeBytes(shellCmd);
                NotificationsManager.getInstance(mContext).makeNewClickNotifications(x, y);
            } catch ( IOException ioe ){
                Logger.e(LOG_TAG, "Exception thrown during tap execution : " + ioe.getMessage());
                ioe.printStackTrace();
            }

            // Should we wait before the next action ?
            if ( mTimeGap > 0 ){
                try {
                    Logger.d(LOG_TAG, "Should wait before each process occurrences : "+mTimeGap);
                    Thread.sleep(mTimeGap*1000);
                } catch ( InterruptedException ie ){}
            } else {
                Logger.d(LOG_TAG, "Should NOT wait before each process occurrences : "+mTimeGap);
            }

        } // End of for ( PointsListAdapter.Point p : mPoints )

    }

    /**
     * Sends a broadcast with the status of the service
     * @param status - The status to broadcast
     */
    private void broadcastStatus( StatusTypes status ){
        Logger.d(LOG_TAG, "Status of the ServiceClicker : "+status.mCode);
        Intent i = new Intent(BROADCAST_ACTION);
        i.putExtra(BROADCAST_KEY_STATUS, status.mCode);
        sendBroadcast(i);
    }


    /* *********** *
     * INNER ENUMS *
     * *********** */

    /**
     * The list of status
     */
    public enum StatusTypes {

        /**
         * The status the service can be: it has received a bad configuration
         */
        BAD_CONFIG("0x001000"),
        /**
         * The status the service can be: it has been triggered
         */
        AWAKE("0x001001"),
        /**
         * The status the service can be: it has been triggered / started by another app
         */
        STARTED("0x001002"),
        /**
         * The status the service can be: its is working on the click process
         */
        WORKING("0x001003"),
        /**
         * The status the service can be: the click process is over
         */
        TERMINATED("0x001004");

        String mCode;

        StatusTypes( String s ){
            mCode = s;
        }

    }

}
