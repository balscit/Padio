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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

/*
 * Main activity which provide actionbar and host channel list fragment
 */
public class MainActivity extends ActionBarActivity {

    private static final String TAG = "MainActivity";

    public static Context AppContext = null;

    private MenuItem playerControlMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppContext = this;

        Initialize();
    }

    @Override
    public  void onResume(){
        super.onResume();

        // register for player state change
        LocalBroadcastManager.getInstance(this).registerReceiver(playerStatusReceiver,
                new IntentFilter(Player.PlayerStatusBroadcast));
    }

    @Override
    protected void onPause() {
        // Unregister player receiver since the activity is not visible
        LocalBroadcastManager.getInstance(this).unregisterReceiver(playerStatusReceiver);
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        playerControlMenu = menu.findItem(R.id.action_player);

        // update player icon
        updtePlayerIcon(Player.isPlaying());

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_player:
                Player.TogglePlayer();
                return true;
            case R.id.action_refresh:
                AsyncCall asyncCall = new AsyncCall(new FunctionCall() {
                        @Override
                        public void invoke() {
                            Settings.updateChannel();
                        }
                    },
                    new FunctionCall() {
                        @Override
                        public void invoke() {
                            ChannelListFragment.refreshList();
                        }
                    });
                asyncCall.execute();

                return true;
            case R.id.action_settings:
                // help action
                return true;
            case R.id.action_checkupdate:
                ManageUpdate.updateApp();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void Initialize() {
        new Thread(new Runnable() {
            public void run() {
                if(Settings.prepareFirstRun()){
                    refreshChannel();
                }

                if (Settings.getDefaultLanguage() == null) {
                    Log.i(TAG, "Default language not set");
                    Settings.updateChannel();

                    if (Settings.getDefaultLanguage() != null) {
                        refreshChannel();
                    }
                }

                if (Settings.getDefaultLanguage() == null) {
                    Log.e(TAG, "Default language not set");
                    return;
                }
            }
        }).start();
    }

    private void refreshChannel(){
        Runnable run = new Runnable(){
            public void run(){
                ChannelListFragment.refreshList();
            }
        };
        runOnUiThread(run);
    }


    // handle player state change
    private BroadcastReceiver playerStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isPlaying = intent.getBooleanExtra(Player.PlayerStatusMessage, false);
            Log.d(TAG, "playerStatusReceiver-isPlaying: " + isPlaying);
            updtePlayerIcon(isPlaying);
        }
    };

    // update the player control icon
    private void updtePlayerIcon(boolean isPlaying){
        if(isPlaying){
            playerControlMenu.setIcon(R.drawable.stop);
        }else{
            playerControlMenu.setIcon(R.drawable.play);
        }
    }
}
