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

package com.beftigre.band.mon;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;

public class BaseService extends Service {

    private static int counter = 0;
    private static float cpuTotal = 0f, memTotal = 0f;
    private String filename;
    private int count;
    private static String TAG = "Band";

    @Override
    public void onCreate() {
        super.onCreate();
        //get filename from shared preferences
        Persistence persist = new Persistence(this);
        this.filename = persist.getFilename();
        this.count = persist.getCount();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        counter++;
        cpuTotal += cpuAvail();
        memTotal += memAvail();
        Log.v(TAG, "cpuTotal:" + cpuTotal + " memTotal:" + memTotal + " filename:" + filename + " counter:" + counter);
        //for custom alarm
        if (count > 0 && counter == count) {
            //Log.v("Band", "final av: " + cpuTotal / counter + " " + memTotal / counter);
            save("mobileCPU " + cpuTotal / counter + "\nmobileMemory " + memTotal / counter); //av. available %
            Toast.makeText(BaseService.this, "Base Service finished.", Toast.LENGTH_SHORT).show();
            Log.v(TAG, "Base Service finished.");
            stopSelf(); //call onDestroy to stop alarm
        }
        //for default alarm
        else if (count <= 0 && counter == 10) {
            //Log.v(TAG, "final av: " + cpuTotal / counter + " " + memTotal / counter);
            save("mobileCPU " + cpuTotal / counter + "\nmobileMemory " + memTotal / counter); //av. available %
            Toast.makeText(BaseService.this, "Base Service finished.", Toast.LENGTH_SHORT).show();
            Log.v(TAG, "Base Service finished.");
            stopSelf(); //call onDestroy to stop alarm
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent(BaseService.this, BaseService.class);
        PendingIntent pintent = PendingIntent.getService(BaseService.this, 0, intent, 0);
        AlarmManager alarm = (AlarmManager) BaseService.this.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pintent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void save(String message) {
        try {
            File file = new File(
                    Environment.getExternalStorageDirectory(), "MarkerLog_" +
                    filename + ".log");
            FileOutputStream fileOut = new FileOutputStream(file, true); //append to appLog
            OutputStreamWriter writer = new OutputStreamWriter(fileOut);
            writer.write(message);
            writer.close();
            fileOut.close();
        } catch (IOException ex) {
            Log.v(TAG, ex.getMessage());
        }
    }

    private float cpuAvail() {
        RandomAccessFile reader;
        try {
            reader = new RandomAccessFile("/proc/stat", "r");
            String load = reader.readLine();
            String[] toks = load.split(" +");  // Split on one or more spaces
            long idle1 = Long.parseLong(toks[4]);
            long cpu1 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[5])
                    + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);
            try {
                Thread.sleep(360);
            } catch (InterruptedException ex) {
                Log.v(TAG, ex.getMessage());
            }
            reader.seek(0);
            load = reader.readLine();
            reader.close();
            toks = load.split(" +");
            long idle2 = Long.parseLong(toks[4]);
            long cpu2 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[5])
                    + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);
            float usage = (float) (cpu2 - cpu1) / ((cpu2 + idle2) - (cpu1 + idle1)); //usage
            return 100 - (usage * 100); //%available = 100-%usage
        } catch (IOException ex) {
            Log.v(TAG, ex.getMessage());
        }

        return 0f;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private float memAvail() {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) BaseService.this.getSystemService(ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        //Percentage can be calculated for API 16+
        float percentAvail = ((float) mi.availMem / mi.totalMem * 100);
        return percentAvail;
    }
}
