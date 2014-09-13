/*
 * Copyright 2014 Randy McEoin
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

package net.mceoin.cominghome;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.location.Geofence;

import net.mceoin.cominghome.oauth.OAuthFlowApp;

import java.util.List;

/**
 * Process entering and exiting of fences
 */
public class FenceHandling {

    public final static String TAG = FenceHandling.class.getSimpleName();
    public final static boolean debug = true;

    private static SharedPreferences prefs;
    private static FenceHandlingAlarm alarm= new FenceHandlingAlarm();

    public static void process(int transition, List<Geofence> geofences, Context context) {
        for (Geofence geofence : geofences) {
            if (geofence.getRequestId().equals(MainActivity.FENCE_HOME)) {
                switch (transition) {
                    case Geofence.GEOFENCE_TRANSITION_ENTER:
                        arrivedHome(context);
                        break;
                    case Geofence.GEOFENCE_TRANSITION_EXIT:
                        leftHome(context);
                        break;
                    default:
                        Log.e(TAG, "unknown transition: " + transition);
                }
            } else if (geofence.getRequestId().equals(MainActivity.FENCE_WORK)) {
                switch (transition) {
                    case Geofence.GEOFENCE_TRANSITION_EXIT:
                        leftWork(context);
                        break;
                    default:
                        Log.e(TAG, "unknown transition: " + transition);
                }
            }
        }

    }

    public static void arrivedHome(Context context) {
        if (debug) Log.d(TAG, "arrived home");

        // make sure there isn't an alarm set from a leftHome event
        alarm.CancelAlarm(context);

        HistoryUpdate.add(context,"Geofence arrived home");
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String structure_id = prefs.getString(MainActivity.PREFS_STRUCTURE_ID, "");
        String access_token = prefs.getString(OAuthFlowApp.PREF_ACCESS_TOKEN, "");
        boolean tellNest = prefs.getBoolean(PrefsFragment.key_tell_nest_on_arrival_home, true);

        if (!access_token.isEmpty()) {
            if (!structure_id.isEmpty()) {
                BackendUtils.updateStatus(context, structure_id, "home",tellNest);
            } else {
                if (debug) Log.d(TAG, "arrived home, but no structure_id");
            }

        } else {
            Log.e(TAG, "no access_token");
        }
    }

    public static void leftHome(Context context) {
        if (debug) Log.d(TAG, "left home");

        HistoryUpdate.add(context,"Geofence left home");

        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String structure_id = prefs.getString(MainActivity.PREFS_STRUCTURE_ID, "");

        if (!structure_id.isEmpty()) {
            // set an alarm to wait before we tell nest we're home.
            // sometimes the phone's geolocation will bounce out of an area and
            // then come back.
            alarm.SetAlarm(context);

        } else {
            Log.e(TAG, "missing structure_id");
        }
    }

    public static void leftWork(Context context) {
        if (debug) Log.d(TAG, "left work");

        HistoryUpdate.add(context,"Geofence left work");

        if (LocationService.isRunning(context)) {
            if (debug) Log.d(TAG,"LocationService is running");
        } else {
            if (debug) Log.d(TAG,"LocationService is not running");
        }
    }
}
