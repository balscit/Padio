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

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MusicIntentReceiver extends android.content.BroadcastReceiver {
    private final String TAG = MusicIntentReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context ctx, Intent intent) {
        if (intent.getAction().equals(
                android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
            Log.d(TAG, "Audio becoming noisy intent");
            Player.StopPlayer();
        }
    }
}
