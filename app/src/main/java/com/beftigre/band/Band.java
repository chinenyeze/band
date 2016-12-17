/*
 * Copyright (c) 2016 Samuel Chinenyeze <sjchinenyeze@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.beftigre.band;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import com.beftigre.band.exceptions.DuplicateLabelException;
import com.beftigre.band.exceptions.UnevenMarkersException;
import com.beftigre.band.mon.BaseService;
import com.beftigre.band.mon.Persistence;
import com.beftigre.band.mon.UMLoggerService;
import com.beftigre.band.service.ICounterService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Band {
    private Intent serviceIntent;
    private ICounterService counterService;
    private CounterServiceConnection conn;
    private Activity activity;
    private static String TAG = "Band";
    private static HashMap<String, Marker> labelMap = new HashMap<>();
    private String appPackage;
    private static long filename;
    public static Object testObject;

    /**
     * Initialize Band with test activity and test object
     *
     * @param activity necessary for the test process
     * @param object used to get annotations
     */
    public Band(Activity activity, Object object) {
        this.activity = activity;
        this.appPackage = activity.getApplication().getApplicationContext().getPackageName();
        testObject = object;
        Band.setFilename();
        new Persistence(activity).setFilename(Long.toString(Band.getFilename()));
    }

    public Band() {
    }

    public static long getFilename() {
        return filename;
    }

    public static void setFilename() {
        if (filename == 0) {
            filename = System.currentTimeMillis();
        }
    }

    /**
     * Register markers
     *
     * @param array
     * @throws DuplicateLabelException
     */
    public final void registerMarkers(Marker... array) throws DuplicateLabelException {
        Set<Marker> set = new HashSet<Marker>(Arrays.asList(array)); //avoids duplicate marker in parsed array
        int i = Marker.hashMap.size() / 2; //divide by 2 as label and identifier is put in each run
        for (Marker marker : set) {
            //throw exception if label is already registered
            if (Marker.hashMap.containsValue(marker.label)) {
                throw new DuplicateLabelException();
            }
            Marker.hashMap.put(marker.label, "M" + ++i);           //identifier, deleted later
            Marker.hashMap.put("M" + i + "_label", marker.label);  //M1_label
        }
        int j = 0;
        for (Marker marker : array) {
            j = labelMap.size();
            labelMap.put("" + ++j, marker);
        }
    }

    /**
     * Writes the instrumentation data to file
     *
     * @throws UnevenMarkersException
     */
    public final void saveMarkers() throws UnevenMarkersException {
        //remove identifier
        for (Marker marker : labelMap.values()) {
            Marker.hashMap.remove(marker.label);
        }
        //throw exception if markers are incomplete
        if ((Marker.hashMap.size() % 4) != 0) {
            throw new UnevenMarkersException();
        }
        /*
            app rs.pedjaapps.Linpack
            M1_start 1459029740369
            M1_label Linpack
            M1_finish 1459029762226
            M1_anno na
            mobileCPU 96.7659
            mobileMemory 20.849531
        */
        try {
            File file = new File(
                    Environment.getExternalStorageDirectory(), "MarkerLog_" +
                    /*getFilename()*/ new Persistence(activity).getFilename() + ".log");
            FileOutputStream fileOut = new FileOutputStream(file);
            OutputStreamWriter writer = new OutputStreamWriter(fileOut);
            //begin write
            writer.write("app " + appPackage + "\n");
            //iterate through markers
            Iterator iterator = Marker.hashMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry pair = (Map.Entry) iterator.next();
                writer.write(pair.getKey() + " " + pair.getValue() + "\n");
                iterator.remove();
            }
            //close resources
            writer.close();
            fileOut.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * Starts PowerTutor model
     */
    public final void startPowerMonitoring() {
        conn = new CounterServiceConnection();
        serviceIntent = new Intent(activity, UMLoggerService.class);
        if (conn != null) {
            this.activity.startService(serviceIntent);
            Log.i(TAG, "Profiler started");
        }
    }

    /**
     * Stops PowerTutor model
     */
    public final void stopPowerMonitoring() {
        this.activity.stopService(serviceIntent);
        Log.i(TAG, "Profiler stopped");
    }

    /**
     * PowerTutor model service
     */
    private class CounterServiceConnection implements ServiceConnection {
        public void onServiceConnected(ComponentName className,
                                       IBinder boundService) {
            counterService = ICounterService.Stub
                    .asInterface((IBinder) boundService);
        }

        public void onServiceDisconnected(ComponentName className) {
            counterService = null;
        }
    }

    /**
     * Launch BaseService to get available CPU and memory [default]
     */
    public final void getBaseStatus() {
        //store filename in shared preferences
        Persistence persist = new Persistence(activity);
        persist.setFilename(Long.toString(getFilename()));
        persist.setCount(0);
        //alarm for BaseService
        Intent intent = new Intent(activity, BaseService.class);
        PendingIntent pintent = PendingIntent.getService(activity, 0, intent, 0);
        AlarmManager alarm = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 10 * 1000, pintent);
    }

    /**
     * Launch BaseService to get available CPU and memory [custom]
     *
     * @param i interleave in seconds
     * @param c count
     */
    public final void getBaseStatus(int i, int c) {
        //store filename in shared preferences
        Persistence persist = new Persistence(activity);
        persist.setFilename(Long.toString(getFilename()));
        persist.setInterleave(i);
        persist.setCount(c);
        //alarm for BaseService
        Intent intent = new Intent(activity, BaseService.class);
        PendingIntent pintent = PendingIntent.getService(activity, 0, intent, 0);
        AlarmManager alarm = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), persist.getInterleave() * 1000, pintent);
    }

}
