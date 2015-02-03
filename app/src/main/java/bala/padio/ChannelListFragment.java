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

import android.content.Intent;
import android.os.Bundle;
import android.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

/*
 *  Fragment to display the list of channels
 */
public class ChannelListFragment extends ListFragment {
    private final static String TAG = ChannelListFragment.class.getSimpleName();

    private static ChannelAdapter adapter;
    private static ChannelListFragment instance;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        instance = this;

        adapter = new ChannelAdapter(getActivity(), Settings.GetChannelList());
        setListAdapter(adapter);

        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        highlightChannel();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        v.setSelected(true);
        ChannelModel selected = adapter.getItem(position);
        Settings.setSelectedChannel(selected.json);

        // Play the selected channel
        Intent playerIntent = new Intent(MainActivity.AppContext, Player.class);
        MainActivity.AppContext.startService(playerIntent);
    }

    /*
     * Reload the channel list from settings
     */
    public static void refreshList(){
        if(adapter != null){
            adapter.clear();
            adapter.addAll(Settings.GetChannelList());
            adapter.notifyDataSetChanged();
            highlightChannel();
        }
    }

    // highlight selected channel
    private static void highlightChannel() {
        String selectedChannel = Settings.getSelectedChannel();
        if(instance != null && selectedChannel != null){
            try {
                ChannelModel selectedChannelModel = new ChannelModel(selectedChannel);
                if(selectedChannelModel.id != null ){
                    Log.d(TAG, "Selected channel: " + selectedChannelModel.name);

                    instance.getListView().setItemChecked(adapter.getPosition(selectedChannelModel), true);
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }
}
