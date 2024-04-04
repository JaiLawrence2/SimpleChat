package edu.jsu.mcis.cs408.simplechat;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.net.ssl.HttpsURLConnection;

public class ChatServer extends AbstractModel {

    private static final String TAG = "ExampleWebServiceModel";

    private static final String GET_URL = "https://testbed.jaysnellen.com:8443/SimpleChat/board";
    private static final String POST_URL = "https://testbed.jaysnellen.com:8443/SimpleChat/board";
    private static final String DELETE_URL = "https://testbed.jaysnellen.com:8443/SimpleChat/board";
    private static final String name = "Jailon Lawrence";
    private MutableLiveData<JSONObject> jsonData;
    private String outputText;

    private final ExecutorService requestThreadExecutor;
    private final Runnable httpGetRequestThread, httpPostRequestThread, httpDeleteRequestThread;
    private Future<?> pending;

    private String postData; // Added to store JSON data for POST request

    public ChatServer() {
        requestThreadExecutor = Executors.newSingleThreadExecutor();

        httpGetRequestThread = new Runnable() {
            @Override
            public void run() {
                if (pending != null) {
                    pending.cancel(true);
                }
                try {
                    pending = requestThreadExecutor.submit(new HTTPRequestTask("GET", GET_URL, null));
                } catch (Exception e) {
                    Log.e(TAG, " Exception: ", e);
                }
            }
        };
        httpPostRequestThread = new Runnable() {
            @Override
            public void run() {
                if (pending != null) { pending.cancel(true); }
                try {
                    pending = requestThreadExecutor.submit(new HTTPRequestTask("POST", POST_URL, postData));
                } catch (Exception e) { Log.e(TAG, " Exception: ", e); }
            }
        };
        httpDeleteRequestThread = new Runnable() {
            @Override
            public void run() {
                if (pending != null) { pending.cancel(true); }
                try {
                    pending = requestThreadExecutor.submit(new HTTPRequestTask("DELETE", DELETE_URL, null));
                } catch (Exception e) {
                    Log.e(TAG, " Exception: ", e);
                }
            }
        };
    }

    public void initDefault() {
        sendGetRequest();
    }

    public String getOutputText() {
        return outputText;
    }

    public void setOutputText(String newText) {

        String oldText = this.outputText;
        this.outputText = newText;
        Log.i(TAG, "Output Text Change: From " + oldText + " to " + newText);
        firePropertyChange(Controller.ELEMENT_OUTPUT_PROPERTY, oldText, newText);
    }

    public void sendGetRequest() {
        httpGetRequestThread.run();
    }

    public void sendPostRequest() {
        this.postData = String.valueOf(jsonData); // Set JSON data to postData
        httpPostRequestThread.run();
    }

    public void sendDeleteRequest() {
        httpDeleteRequestThread.run();
    }

    private void setJsonData(JSONObject json) {
        this.getJsonData().postValue(json);
        setOutputText(json.toString());
    }

    public MutableLiveData<JSONObject> getJsonData() {
        if (jsonData == null) {
            jsonData = new MutableLiveData<>();
        }
        return jsonData;
    }

    private class HTTPRequestTask implements Runnable {

        private final String method, urlString;
        private final String jsonData;

        HTTPRequestTask(String method, String urlString, String jsonData) {
            this.method = method;
            this.urlString = urlString;
            this.jsonData = jsonData;
        }

        @Override
        public void run() {
            JSONObject results = doRequest(urlString, jsonData);
            setJsonData(results);
        }

        private JSONObject doRequest(String urlString, String jsonData) {
            StringBuilder r = new StringBuilder();
            String line;
            HttpURLConnection conn = null;
            JSONObject results = null;

            try {
                if (Thread.interrupted()) throw new InterruptedException();

                URL url = new URL(urlString);
                conn = (HttpURLConnection) url.openConnection();

                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);

                // Set the request method based on the 'method' parameter
                conn.setRequestMethod(method);
                conn.setDoInput(true);

                // Check if it's a POST request to include JSON data
                if (method.equals("POST")) {
                    conn.setDoOutput(true);
                    /*String messages = MainActivity.name;
                    JSONObject chat = new JSONObject();
                    chat.put("name", name);
                    chat.put("message", messages);
                    String p = chat.toString();*/

                    OutputStream out = conn.getOutputStream();
                    out.write(jsonData.getBytes());
                    out.flush();
                    out.close();
                }

                // Connect to the URL
                conn.connect();

                // Get the response code
                int code = conn.getResponseCode();

                // Handle DELETE
                if (method.equals("DELETE")) {
                    if (code == HttpURLConnection.HTTP_OK) {
                        Log.d(TAG, "Delete success");
                        setOutputText("Message Board Cleared");
                    } else {
                        Log.e(TAG, "Delete failed");
                    }
                } else {
                    if (code == HttpsURLConnection.HTTP_OK || code == HttpsURLConnection.HTTP_CREATED) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                        do {
                            line = reader.readLine();
                            if (line != null) r.append(line);
                        } while (line != null);
                    }

                    results = new JSONObject(r.toString());
                }

            } catch (Exception e) {
                Log.e(TAG, " Exception: ", e);
            } finally {
                if (conn != null) { conn.disconnect(); }
            }

            Log.d(TAG, " JSON: " + r.toString());
            return results;
        }

    }

}

