/*
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

package bala.padio;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

/*
 * Manage application update from server
 */
public class ManageUpdate {
    private static final String TAG = ManageUpdate.class.getSimpleName();

    // check for update on server
    // and install the app if new version found
    public static void updateApp(){
        AsyncCall asyncCall = new AsyncCall(
                new FunctionCall() {
                    @Override
                    public void invoke() {
                        // check the app version
                        int versionCode = BuildConfig.VERSION_CODE;
                        String serverVersion = Settings.DownloadFromUrl(Settings.ServerVersionUrl);
                        if(serverVersion == null){
                            toast("Error getting app version from server");
                            return;
                        }

                        Log.i(TAG, "Server version: " + serverVersion + ", Local version: " + versionCode);
                        serverVersion = serverVersion.trim();
                        if(serverVersion.compareTo(Integer.toString(versionCode)) > 0){
                            toast("Updating Padio");

                            // server version is higher, download the apk
                            boolean status = Settings.DownloadFile(Settings.ServerApkUrl,
                                    Settings.ApkFilePath);
                            if(status == true){
                                callUpdate(Uri.fromFile(new File(Settings.ApkFilePath)),
                                        Settings.AppName);
                            }
                            else
                            {
                                toast("Error downloading apk from server");
                            }
                        }
                        else{
                            toast("Already on latest version");
                        }
                    }
                },
                null
        );
        asyncCall.execute();
    }

    // show message
    private static void toast(final String message){
        Log.i(TAG, message);
        ((Activity)MainActivity.AppContext).runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(MainActivity.AppContext, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // start intent to ask the user to install the downloaded app
    private static void callUpdate(final Uri data, final String type){
        ((Activity)MainActivity.AppContext).runOnUiThread(new Runnable() {
            public void run() {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(data, type);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    MainActivity.AppContext.startActivity(intent);
                }catch (Exception ex){
                    ex.printStackTrace();
                    toast("Error updating app");
                }
            }
        });
    }
}
