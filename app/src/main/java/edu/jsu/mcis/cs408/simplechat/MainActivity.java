package edu.jsu.mcis.cs408.simplechat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.QuickContactBadge;

import org.json.JSONException;
import org.json.JSONObject;

import java.beans.PropertyChangeEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.net.ssl.HttpsURLConnection;

import edu.jsu.mcis.cs408.simplechat.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    EditText input;
    private static final String TAG = "ExampleWebServiceModel";
    public static final String name = "Jailon Lawrence";
    private static final String GET_URL = "https://testbed.jaysnellen.com:8443/SimpleChat/board" ;
    private static final String POST_URL = "https://testbed.jaysnellen.com:8443/SimpleChat/board";
    private MutableLiveData<JSONObject> jsonData;

    private String outputText;

    private ExecutorService requestThreadExecutor;
    private Runnable httpGetRequestThread;
    private Runnable httpPostRequestThread;

    private Future<?> pending;
    ChatController controller;

    public MainActivity(ExecutorService requestThreadExecutor, Runnable httpGetRequestThread, Runnable httpPostRequestThread) {
        this.requestThreadExecutor = requestThreadExecutor;
        this.httpGetRequestThread = httpGetRequestThread;
        this.httpPostRequestThread = httpPostRequestThread;
    }
    public MainActivity(){

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        controller = new ChatController();
        SimpleChatServer model = new SimpleChatServer();
        //controller.addView(this);
        //controller.addModel(model);
        //controller.sendGetRequest();

        binding.Post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               String message = binding.messageInput.getText().toString();
                httpPostRequestThread.run();

                //binding.output.setText();
            }
        });
        binding.Delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                model.sendDeleteRequest();
                controller.sendGetRequest();
            }
        });
        requestThreadExecutor = Executors.newSingleThreadExecutor();
        httpGetRequestThread = new Runnable() {

            @Override
            public void run() {

                /* If a previous request is still pending, cancel it */

                if (pending != null) { pending.cancel(true); }

                /* Begin new request now, but don't wait for it */

                try {
                    pending = requestThreadExecutor.submit(new HTTPRequestTask("GET", GET_URL));
                }
                catch (Exception e) { Log.e(TAG, " Exception: ", e); }

            }

        };
        httpPostRequestThread = new Runnable() {

            @Override
            public void run() {

                /* If a previous request is still pending, cancel it */

                if (pending != null) { pending.cancel(true); }

                /* Begin new request now, but don't wait for it */

                try {
                    pending = requestThreadExecutor.submit(new HTTPRequestTask("POST", POST_URL));
                }
                catch (Exception e) { Log.e(TAG, " Exception: ", e); }

            }

        };
    }

    public void setOutputText(String newText) {

        String oldText = this.outputText;
        this.outputText = newText;

        Log.i(TAG, "Output Text Change: From " + oldText + " to " + newText);

        binding.output.setText(newText);

    }
    private void setJsonData(JSONObject json) {

        this.getJsonData().postValue(json);

        setOutputText(json.toString());
        Log.i(MainActivity.TAG,"Message: " + json);

    }

    public MutableLiveData<JSONObject> getJsonData() {
        if (jsonData == null) {
            jsonData = new MutableLiveData<>();
        }
        return jsonData;
    }



    private class HTTPRequestTask implements Runnable {

        private static final String TAG = "HTTPRequestTask";
        private final String method, urlString;

        HTTPRequestTask(String method, String urlString) {
            this.method = method;
            this.urlString = urlString;
        }

        @Override
        public void run() {
            JSONObject results = doRequest(urlString);
            setJsonData(results);
        }

        /* Create and Send Request */

        private JSONObject doRequest(String urlString) {

            StringBuilder r = new StringBuilder();
            String line;

            HttpURLConnection conn = null;
            JSONObject results = null;

            /* Log Request Data */

            try {

                /* Check if task has been interrupted */

                if (Thread.interrupted())
                    throw new InterruptedException();

                /* Create Request */

                URL url = new URL(urlString);

                conn = (HttpURLConnection)url.openConnection();

                conn.setReadTimeout(10000); /* ten seconds */
                conn.setConnectTimeout(15000); /* fifteen seconds */

                conn.setRequestMethod(method);
                conn.setDoInput(true);

                /* Add Request Parameters (if any) */

                if (method.equals("POST") ) {

                    conn.setDoOutput(true);

                    // Create request parameters (these will be echoed back by the example API)
                    String messages = binding.messageInput.getText().toString();
                    JSONObject message = new JSONObject();
                    message.put("name", name);
                    message.put("message", messages);
                    String p = message.toString();
                    //message.get(messages);
                    //binding.output.setText(messages);

                    // Write parameters to request body

                    OutputStream out = conn.getOutputStream();
                    out.write(p.getBytes());
                    out.flush();
                    out.close();

                }
                /* Send Request */

                conn.connect();

                /* Check if task has been interrupted */

                if (Thread.interrupted())
                    throw new InterruptedException();

                /* Get Reader for Results */

                int code = conn.getResponseCode();

                if (code == HttpsURLConnection.HTTP_OK || code == HttpsURLConnection.HTTP_CREATED) {

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    /* Read Response Into StringBuilder */

                    do {
                        line = reader.readLine();
                        if (line != null)
                            r.append(line);
                    }
                    while (line != null);

                }

                /* Check if task has been interrupted */

                if (Thread.interrupted())
                    throw new InterruptedException();

                /* Parse Response as JSON */

                results = new JSONObject(r.toString());

            }
            catch (Exception e) {
                Log.e(TAG, " Exception: ", e);
            }
            finally {
                if (conn != null) { conn.disconnect(); }
            }

            /* Finished; Log and Return Results */

            Log.d(TAG, " JSON: " + r.toString());

            return results;

        }

    }
    /*@Override
    public void modelPropertyChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();
        String propertyValue = evt.getNewValue().toString();

        Log.i(TAG, "New " + propertyName + " Value from Model: " + propertyValue);

        if ( propertyName.equals(ChatController.ELEMENT_OUTPUT_PROPERTY) ) {

            String oldPropertyValue = binding.output.getText().toString();

            if ( !oldPropertyValue.equals(propertyValue) ) {
                binding.output.setText(propertyValue);
            }

        }
    }*/

}