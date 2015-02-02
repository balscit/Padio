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

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/*
 *  Handle bluetooth connected/disconnected event
 *  On bluetooth connection: Play last selected channel
 *  On bluetooth disconnect: Stop playing the channel
 */
public class BluetoothHandler extends BroadcastReceiver {
    private final String TAG = BluetoothHandler.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
            Log.d(TAG, "Bluetooth connected");

            if(MainActivity.AppContext == null) {
                // Start the main activity
                Intent activity = new Intent(context, MainActivity.class);
                activity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(activity);
            }

            // Play the selected channel
            Intent playerIntent = new Intent(context, Player.class);
            context.startService(playerIntent);
        }
        else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
            Log.d(TAG, "Bluetooth disconnected");
            // Stop player if the app is running
            Player.StopPlayer();
        }
    }
}
