/*
 * Copyright 2014 KC Ochibili
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 *  The "‚‗‚" character is not a comma, it is the SINGLE LOW-9 QUOTATION MARK unicode 201A
 *  and unicode 2017 that are used for separating the items in a list.
 */

package com.example.school.organizer.settings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import com.example.school.organizer.models.SearchSession;
import com.google.gson.Gson;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;


public class TinyDB {

    private final SharedPreferences preferences;

    public TinyDB(Context appContext) {
        preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
    }

    /**
     * Get parsed ArrayList of String from SharedPreferences at 'key'
     * @param key SharedPreferences key
     * @return ArrayList of String
     */
    public ArrayList<String> getListString(String key) {
        return new ArrayList<>(Arrays.asList(TextUtils.split(preferences.getString(key, ""), "‚‗‚")));
    }

    /**
     * Get boolean value from SharedPreferences at 'key'. If key not found, return 'defaultValue'
     * @param key SharedPreferences key
     * defaultValue boolean value returned if key was not found
     * @return boolean value at 'key' or 'defaultValue' if key not found
     */
    public boolean getBoolean(String key) {
        return preferences.getBoolean(key, false);
    }

    public ArrayList<SearchSession> getListSearchSession(String key, Class<?> mClass){
        Gson gson = new Gson();

        ArrayList<String> objStrings = getListString(key);
        ArrayList<SearchSession> objects =  new ArrayList<>();

        for(String jObjString : objStrings){
            SearchSession value  = (SearchSession) gson.fromJson(jObjString,  mClass);
            objects.add(value);
        }
        return objects;
    }

    /**
     * Put ArrayList of String into SharedPreferences with 'key' and save
     * @param key SharedPreferences key
     * @param stringList ArrayList of String to be added
     */
    public void putListString(String key, ArrayList<String> stringList) {
        String[] myStringList = stringList.toArray(new String[stringList.size()]);
        preferences.edit().putString(key, TextUtils.join("‚‗‚", myStringList)).apply();
    }

    /**
     * Put boolean value into SharedPreferences with 'key' and save
     * @param key SharedPreferences key
     * @param value boolean value to be added
     */
    public void putBoolean(String key, boolean value) {
        preferences.edit().putBoolean(key, value).apply();
    }

    public void putListSearchSession(String key, ArrayList<SearchSession> objArray){
        Gson gson = new Gson();
        ArrayList<String> objStrings = new ArrayList<String>();
        for(Object obj : objArray){
            objStrings.add(gson.toJson(obj));
        }
        putListString(key, objStrings);
    }
}