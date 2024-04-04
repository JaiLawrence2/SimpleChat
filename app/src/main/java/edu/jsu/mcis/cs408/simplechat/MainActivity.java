package edu.jsu.mcis.cs408.simplechat;

import androidx.appcompat.app.AppCompatActivity;


import android.os.Bundle;
import android.util.Log;
import android.view.View;


import org.json.JSONException;
import org.json.JSONObject;

import java.beans.PropertyChangeEvent;

import java.util.concurrent.Future;


import edu.jsu.mcis.cs408.simplechat.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements AbstractView {
    //public static ActivityMainBinding binding;
    private ActivityMainBinding binding;
    private static final String TAG = "ExampleWebServiceModel";
    public static final String name = "Jailon Lawrence";
    private static final String GET_URL = "https://testbed.jaysnellen.com:8443/SimpleChat/board" ;
    private static final String POST_URL = "https://testbed.jaysnellen.com:8443/SimpleChat/board";
    private static final String DELETE_URL = "https://testbed.jaysnellen.com:8443/SimpleChat/board";



    private Future<?> pending;
    Controller controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        controller = new Controller();
        ChatServer model = new ChatServer();
        //httpGetRequestThread.run();
        controller.addView(this);
        controller.addModel(model);

        controller.sendGetRequest();


        binding.Post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = binding.messageInput.getText().toString();
                JSONObject json = new JSONObject();
                try{
                    json.put("name", name);
                    json.put("message", message);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                model.sendPostRequest();
            }
        });
        binding.Delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controller.sendDeleteRequest();
            }
        });
    }

    @Override
    public void modelPropertyChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();
        String propertyValue = evt.getNewValue().toString();

        Log.i(TAG, "New " + propertyName + " Value from Model: " + propertyValue);

        if ( propertyName.equals(Controller.ELEMENT_OUTPUT_PROPERTY) ) {

            String oldPropertyValue = binding.output.getText().toString();

            if ( !oldPropertyValue.equals(propertyValue) ) {
                binding.output.setText(propertyValue);
            }

        }
    }

}