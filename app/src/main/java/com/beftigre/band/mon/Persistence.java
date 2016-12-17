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

import android.content.Context;
import android.content.SharedPreferences;

public class Persistence {

    private static final String PREFERENCES = "beftigre_preferences";
    private SharedPreferences settings;
    private SharedPreferences.Editor editor;
    private Context context;


    public Persistence(Context context) {
        this.context = context;
        settings = context.getSharedPreferences(PREFERENCES, 0);
        editor = settings.edit();
    }

    public String getFilename() {
        return settings.getString("filename", "");
    }

    public void setFilename(String filename) {
        editor.putString("filename", filename);
        editor.commit();
    }

    public int getCount() {
        return settings.getInt("count", 0);
    }

    public void setCount(int count) {
        editor.putInt("count", count);
        editor.commit();
    }

    public int getInterleave() {
        return settings.getInt("interleave", 0);
    }

    public void setInterleave(int interleave) {
        editor.putInt("interleave", interleave);
        editor.commit();
    }
}
