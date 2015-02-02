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

import android.os.AsyncTask;

interface FunctionCall{
    void invoke();
}

/*
 *  Helper class to run a task in background
 *  and follow up with ui function
 */
public class AsyncCall extends AsyncTask<Void, Void, Void> {
    final FunctionCall backgroundFunction;
    final FunctionCall uiFunction;

    public AsyncCall(FunctionCall bg, FunctionCall ui){
        backgroundFunction = bg;
        uiFunction = ui;
    }

    @Override
    protected void onPostExecute(Void result) {
        if(uiFunction != null){
            try {
                uiFunction.invoke();
            }
            catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }

    @Override
    protected Void doInBackground(Void... params) {
        if(backgroundFunction != null){
            try {
                backgroundFunction.invoke();
            }
            catch (Exception ex){
                ex.printStackTrace();
            }
        }
        return null;
    }
}
