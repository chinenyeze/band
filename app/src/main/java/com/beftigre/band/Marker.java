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

import android.util.Log;

import com.beftigre.band.annotations.Given;
import com.beftigre.band.annotations.Then;
import com.beftigre.band.annotations.When;
import com.beftigre.band.exceptions.DuplicateFinishMarkerException;
import com.beftigre.band.exceptions.DuplicateStartMarkerException;

import java.lang.reflect.Method;
import java.util.HashMap;

public class Marker {
    private static String TAG = "Band";
    public String label;
    public static HashMap<String, String> hashMap = new HashMap<>();

    //Any method which is not private, static, or final can be overridden

    /**
     * ensures no spaces
     * ensures max length is 20
     */
    public Marker(String label) {
        if (label.length() > 20) {
            this.label = label.replaceAll("[^A-Za-z0-9]", "_").substring(0, 20);
        } else {
            this.label = label.replaceAll("[^A-Za-z0-9]", "_");
        }
    }

    /**
     * Assigns a starting timestamp to a marker
     *
     * @throws DuplicateStartMarkerException
     */
    public final void start() throws DuplicateStartMarkerException {
        String key = hashMap.get(this.label);
        //throw exception if marker already started
        if (hashMap.containsKey(key + "_start"))
            throw new DuplicateStartMarkerException();
        hashMap.put(key + "_start", Long.toString(getCurrentTime()));
    }

    /**
     * Assigns a finishing timestamp to a marker
     *
     * @throws DuplicateFinishMarkerException
     */
    public final void finish() throws DuplicateFinishMarkerException {
        String key = hashMap.get(this.label);
        //throw exception if marker already finished
        if (hashMap.containsKey(key + "_finish"))
            throw new DuplicateFinishMarkerException();

        int mobileCPU = 0, mobileMemory = 0, bandwidth = 0;
        double mUsedEnergy = 0.0;
        int latency = 0, cloud_cpu = 0, cloudMemory = 0;
        int mElapsedTime = 0, cUsedCPU = 0, cUsedMemory = 0;
        Method method = getCurrentMethod(Band.testObject);
        //Log.v(TAG, "Method is:" + method);

        try {
            mobileCPU = method.getAnnotation(Given.class).mobileCPU();
            mobileMemory = method.getAnnotation(Given.class).mobileMemory();
            System.out.println("mobileCPU:" + mobileCPU + " mobileMemory:" + mobileMemory);
        } catch (Exception ex) {
            Log.v(TAG, "@Given annotation missing.");
        }

        if (mobileCPU > 0 && mobileMemory > 0) {
            try {
                bandwidth = method.getAnnotation(When.class).bandwidth();
                latency = method.getAnnotation(When.class).latency();
                cloud_cpu = method.getAnnotation(When.class).cloudCPU();
                cloudMemory = method.getAnnotation(When.class).cloudMemory();
                System.out.println("bandwidth:" + bandwidth + " latency:" + latency + " cloud_cpu:" + cloud_cpu + " cloudMemory:" + cloudMemory);
            } catch (Exception ex) {
                Log.v(TAG, "@When annotation missing.");
            }

            try {
                mElapsedTime = method.getAnnotation(Then.class).mElapsedTime();
                mUsedEnergy = method.getAnnotation(Then.class).mUsedEnergy();
                cUsedCPU = method.getAnnotation(Then.class).cUsedCPU();
                cUsedMemory = method.getAnnotation(Then.class).cUsedMemory();
                System.out.println("mElapsedTime:" + mElapsedTime + " mUsedEnergy:" + mUsedEnergy + " cUsedCPU:" + cUsedCPU + " cUsedMemory:" + cUsedMemory);
            } catch (Exception ex) {
                Log.v(TAG, "@Then annotation missing.");
            }
        }

        hashMap.put(key + "_finish", Long.toString(getCurrentTime()));
        String annoValues = "na";
        if (mobileCPU > 0 && mobileMemory > 0) {
            annoValues = mobileCPU + " " + mobileMemory + " " + bandwidth + " " + latency + " " + cloud_cpu + " " +
                    cloudMemory + " " + mElapsedTime + " " + mUsedEnergy + " " + cUsedCPU + " " + cUsedMemory;
        }
        hashMap.put(key + "_anno", annoValues);
    }

    /**
     * Gets current time in milliseconds: can be overriden
     *
     * @return
     */
    public long getCurrentTime() {
        return System.currentTimeMillis();
    }

    /**
     * Gets current method
     *
     * @return
     */
    public static final Method getCurrentMethod(Object o) {
        //[1]:getStackTrace[2]:getCurrentMethod[3]:finish[4]:theTestMethod
        String s = Thread.currentThread().getStackTrace()[4].getMethodName();
        Method cm = null;
        for (Method m : o.getClass().getMethods()) {
            if (m.getName().equals(s)) {
                cm = m;
                break;
            }
        }
        return cm;
    }

}