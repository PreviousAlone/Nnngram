/*
 * Copyright (C) 2019-2024 qwq233 <qwq233@qwq2333.top>
 * https://github.com/qwq233/Nullgram
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this software.
 *  If not, see
 * <https://www.gnu.org/licenses/>
 */

package org.telegram.ui.web;


import android.os.AsyncTask;

import org.telegram.messenger.Utilities;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class HttpPostTask extends AsyncTask<String, Void, String> {

    private final String dataMime;
    private final String data;
    private final Utilities.Callback<String> callback;
    private final HashMap<String, String> headers = new HashMap<>();

    private Exception exception;

    public HttpPostTask(
        String mime, String data,
        Utilities.Callback<String> callback
    ) {
        this.dataMime = mime;
        this.data = data;
        this.callback = callback;
    }

    public HttpPostTask setHeader(String key, String value) {
        headers.put(key, value);
        return this;
    }

    @Override
    protected String doInBackground(String... params) {
        String urlString = params[0];

        try {
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            for (Map.Entry<String, String> e : headers.entrySet()) {
                if (e.getKey() == null || e.getValue() == null) continue;
                urlConnection.setRequestProperty(e.getKey(), e.getValue());
            }
            urlConnection.setDoOutput(true);

            urlConnection.setRequestProperty("Content-Type", dataMime);
            try (OutputStream os = urlConnection.getOutputStream()) {
                byte[] input = data.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int statusCode = urlConnection.getResponseCode();
            BufferedReader in;
            if (statusCode >= 200 && statusCode < 300) {
                in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            } else {
                in = new BufferedReader(new InputStreamReader(urlConnection.getErrorStream()));
            }

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();

            return response.toString();

        } catch (Exception e) {
            this.exception = e;
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if (callback != null) {
            if (exception == null) {
                callback.run(result);
            } else {
                callback.run(null);
            }
        }
    }
}
