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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/*
 *  Adapter to provide view model for the list of channels
 */
public class ChannelAdapter extends ArrayAdapter<ChannelModel> {
    private final String TAG = ChannelAdapter.class.getSimpleName();

    private final Context context;
    private final ArrayList<ChannelModel> values;

    public ChannelAdapter(Context context, ArrayList<ChannelModel> values) {
        super(context, R.layout.channel_row, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int index, View view, final ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.channel_row, parent, false);
        TextView item = (TextView) rowView.findViewById(R.id.channelItem);

        // set channel name as the display test
        item.setText(values.get(index).name);

        return rowView;
    }

    public int getPosition(ChannelModel channel){
        for(int index = 0; index < values.size(); index++){
            if(values.get(index).id.equals(channel.id)){
                return  index;
            }
        }
        return -1;
    }
}