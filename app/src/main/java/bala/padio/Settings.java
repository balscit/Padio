package bala.padio;

/**
 *
 * Copyright 2015 Apache License
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

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Map;

/*
 * Provide utlity methods to manage application settings
 * and store the channel information
 */
public class Settings {
    private static  final String TAG = "Settings";
    private static final String appSetting = "AppSetting";
    private static final String ConfIsFirstRun = "IsFirstRun";
    private static final String ConfSettingUrl = "SettingUrl";
    private static  final String ConfLanguageList = "LanguageList";
    private static final String ConfDefaultLanguage = "DefaultLanguage";
    private static final String ConfSelectedChannel = "SelectedChannel";
    private static final String SettingUrl = "http://padio.ddns.net/config.json";

    public static final String ServerVersionUrl = "http://padio.ddns.net/version.txt";
    public static final String ServerApkUrl = "http://padio.ddns.net/padio.apk";
    public static final String ApkFilePath
            = Environment.getExternalStorageDirectory() + "/Download/padio.apk";
    public static final String AppName = "application/vnd.android.package-archive";

    // Prepare the app for first time run
    public static boolean prepareFirstRun(){
        if(getConfig(ConfIsFirstRun, true) == true){
            setConfig(ConfSettingUrl, SettingUrl);
            updateChannel();
            setConfig(ConfIsFirstRun, false);
            return true;
        }
        else {
            return false;
        }
    }

    // Get the channel list from server and save it in local storage
    public static void updateChannel(){
        // find the update url
        String settingUrl = getConfig(ConfSettingUrl, null);
        if(settingUrl == null){
            Log.e(TAG, "Setting url not found");
            return;
        }

        // get the channel config
        String jsonString = DownloadFromUrl(settingUrl);
        Log.d(TAG, "Remote config: " + jsonString);
        if(jsonString == null){
            Log.e(TAG, "Unable to get settings from server");
            return;
        }

        // persist channel data
        try {
            JSONObject jsonSetting = new JSONObject(jsonString);
            JSONArray languages = jsonSetting.getJSONArray("language");
            JSONArray jsonLanguageName = new JSONArray();
            for(int l =0; l<languages.length(); l++)
            {
                JSONObject jsonLanguage = languages.getJSONObject(l);
                String languageName = jsonLanguage.getString("name");

                // clear existing channel list
                clearConfig(languageName);

                // save each channel to the storage
                JSONArray channels = jsonLanguage.getJSONArray("channel");
                for(int c = 0; c< channels.length(); c++)
                {
                    JSONObject jsonChannel = channels.getJSONObject(c);
                    setConfig(jsonChannel.getString("id"), jsonChannel.toString(), languageName);
                }
                jsonLanguageName.put(languageName);
            }
            setConfig(ConfLanguageList, jsonLanguageName.toString());

            // set default language, first language in the configuration
            if(getDefaultLanguage() == null){
                setDefaultLanguage(languages.getJSONObject(0).getString("name"));
            }
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    // Update the server url for the channel list
    public static void updateChannelSource(String newUrl){
        setConfig(ConfSettingUrl, newUrl);
    }

    // Get the default language
    public static String getDefaultLanguage(){
        return getConfig(ConfDefaultLanguage, null);
    }

    // Set the default language
    public static void setDefaultLanguage(String value){
        setConfig(ConfDefaultLanguage, value);
    }

    // Get list of channels available for the selected language
    public static ArrayList<ChannelModel> GetChannelList(){
        // update channel list
        String defaultLanguage = getConfig(ConfDefaultLanguage, null);
        SharedPreferences preference = MainActivity.AppContext.getSharedPreferences(defaultLanguage, Context.MODE_PRIVATE);
        Map<String, ?> channelList = preference.getAll();
        ArrayList<ChannelModel> list = new ArrayList<>();
        for(Map.Entry<String, ?> channel: channelList.entrySet()){
            try {
                ChannelModel cm = new ChannelModel(channel.getValue().toString());
                if(cm.id != null) {
                    list.add(cm);
                }
            }catch (Exception ex){
                Log.e(TAG, ex.toString());
            }
        }
        return list;
    }

    // get the last selected channel
    public static String getSelectedChannel(){
        return getConfig(ConfSelectedChannel, null);
    }

    // set the user selected channel
    public static void setSelectedChannel(String value){
        setConfig(ConfSelectedChannel, value);
    }

    // get the url for the last selected channel
    public static String getSelectedChannelUrl(){
        String jsonChannel = getConfig(ConfSelectedChannel, null);
        try {

            if (jsonChannel != null) {
                return (new JSONObject(jsonChannel)).getString("url");
            }
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    // Get data from the default setting
    private static boolean getConfig(String name, boolean defValue){
        return getConfig(name, defValue, appSetting);
    }

    // Get data from the provided setting
    private static boolean getConfig(String name, boolean defValue, String setting){
        SharedPreferences preference = MainActivity.AppContext.getSharedPreferences(setting , Context.MODE_PRIVATE);
        return  preference.getBoolean(name, defValue);
    }

    // Set config in the default setting
    private static void setConfig(String name, boolean value){
        Log.d(TAG, "setConfig: name-" + name + ", value-" + value);
        SharedPreferences preference = MainActivity.AppContext.getSharedPreferences(appSetting , Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preference.edit();
        editor.putBoolean(name, value);
        editor.commit();
    }

    private static String getConfig(String name, String defValue){
        return getConfig(name, defValue, appSetting);
    }

    private static String getConfig(String name, String defValue, String setting){
        SharedPreferences preference = MainActivity.AppContext.getSharedPreferences(setting , Context.MODE_PRIVATE);
        return  preference.getString(name, defValue);
    }

    private static void setConfig(String name, String value){
        setConfig(name, value, appSetting);
    }

    private static void setConfig(String name, String value, String setting){
        Log.d(TAG, "setConfig: name-" + name + ", value-" + value +", setting-"+setting);
        SharedPreferences preference = MainActivity.AppContext.getSharedPreferences(setting , Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preference.edit();
        editor.putString(name, value);
        editor.commit();
    }

    // clear all the data from the fiven setting
    private static void clearConfig(String setting){
        SharedPreferences preference = MainActivity.AppContext.getSharedPreferences(setting , Context.MODE_PRIVATE);
        if(preference != null) {
            SharedPreferences.Editor editor = preference.edit();
            editor.clear();
            editor.commit();
        }
    }

    // download text data from the given url
    public static String DownloadFromUrl(String u) {
        try {
            URL url = new URL(u);
            URLConnection ucon = url.openConnection();
            InputStream is = ucon.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            ByteArrayBuffer baf = new ByteArrayBuffer(50);
            int current = 0;
            while ((current = bis.read()) != -1) {
                baf.append((byte) current);
            }

            return  new String(baf.toByteArray());

        } catch (Exception e) {
            Log.e(TAG, "Error: " + e);
        }
        return  null;
    }

    // Download a file from the remote web server
    // and save it in a file
    public static boolean DownloadFile(String httpUrl, String filePath){
        OutputStream outputStream;
        try{
            Log.d(TAG, "Downloading url: " + httpUrl + ", to file: " + filePath);
            URL url = new URL(httpUrl);
            URLConnection ucon = url.openConnection();
            InputStream is = ucon.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);

            // file to save
            File file = new File(filePath);
            if(file.exists()){
                file.delete();
            }
            outputStream = new BufferedOutputStream(new FileOutputStream(file));
            int current = 0;
            while ((current = bis.read()) != -1) {
                outputStream.write((byte) current);
            }
            is.close();
            outputStream.close();

            return true;
        }
        catch (Exception ex){
            ex.printStackTrace();
            return  false;
        }
    }
}
