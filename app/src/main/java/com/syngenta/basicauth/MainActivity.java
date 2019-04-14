package com.syngenta.basicauth;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sap.smp.client.httpc.HttpConversationManager;
import com.sap.smp.client.httpc.authflows.CommonAuthFlowsConfigurator;
import com.sap.smp.client.httpc.authflows.UsernamePasswordToken;
import com.sap.smp.client.httpc.events.IReceiveEvent;
import com.sap.smp.client.httpc.listeners.IConversationFlowListener;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

public class MainActivity extends AppCompatActivity {



    /**
     * This is the Uri which points to the protected resource.
     */
    private String requestUri;
    /**
     * username for authentication
     */
    private String username;

    /**
     * password for authentication
     */
    private String password;

    //application log tag which can be used for filtering LogCat.
    private final String LOG_TAG = "BASIC_AUTH_SAMPLE";

    //flag for marking that credentials have already been tried to use.
    private static final String TRIED_ALREADY_FLAG = "HardCodedCredentialsTried";


    private Handler handler = new Handler();
    private ProgressBar progressBar;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_main);


        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        setContentView(R.layout.activity_main);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        textView = (TextView) findViewById(R.id.textView);

    }




    public void doBasicAuth(View view) {
        //getting the config params.
        requestUri = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("requestUri", "http://httpbin.org/basic-auth/user/passwd");
        username = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("username", "user");
        password = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("password", "passwd");

        Map<String, ?> all = PreferenceManager.getDefaultSharedPreferences(this).getAll();
        //Configurator to be used for configuring HttpConversationManager.
        new CommonAuthFlowsConfigurator(this)
                .supportBasicAuthUsing(event -> {
                    // This username password provider has a hardcoded wrong username/pwd.
                    // Get the context to read conversation-scoped information from.
                    // Note that the hard-coded credential provider implementation still
                    // has to account for whether the credentials have been tried already

                    Map<String, Object> convContext = event.getConversationContext().getStateMap(this.getClass().getName(), true);
                    if (!convContext.containsKey(TRIED_ALREADY_FLAG)) {
                        convContext.put(TRIED_ALREADY_FLAG, true);
                        //hard coded username and password
                        return new UsernamePasswordToken("usr", "passwd");
                    }
                    // returns null which leads to cancellation. See the UsernamePasswordOnChallengeProvider interface doc.
                    return null;
                }).supportBasicAuthUsing(event -> {
            // more than one UsernamePasswordOnChallengeProvider can be added.
            // This username provider has a hardcoded valid username/pwd.
            return new UsernamePasswordToken(username, password);
        }).configure(new HttpConversationManager(this))
                // Create the URL and the conversation. The returned 'IHttpConversation' object can be
                // used to configure a lot more things, including request parameters, headers and
                // listeners to be called when the request is being sent and the response is being received.
                .create(Uri.parse(requestUri))
                // response listener must be set if you want to handle the response. The below
                // example uses Java 8 lambdas but with earlier Java versions you'd be using an anonymous
                // nested class.
                .setResponseListener(event -> {
                    hideProgressBar();
                    // Process the response.
                    Log.i(LOG_TAG, "HTTP response received.");
                    Log.i(LOG_TAG, "HTTP response status code = " + event.getResponseStatusCode());
                    Log.i(LOG_TAG, "HTTP response body = \n" + IReceiveEvent.Util.getResponseBody(event.getReader()));

                     showMessage();

                    // Send the request by simply starting the conversation. Request execution will take place
                    // on a new thread.
                }).setFlowListener(IConversationFlowListener.prepareFor().communicationError(e -> {
            Log.i(LOG_TAG, "IOException got = ", e);

            //these are the main listeners handling the various error cases.
            // See the appropriate listener API documentations for further details.
        }).cancellationByRequestFilter(iCancellationEvent -> {
            hideProgressBar();
            Log.i(LOG_TAG, "iCancellationEvent got ByRequestFilter = " + iCancellationEvent.getResult().toString());
        }).cancellationByRequestListener(iCancellationEvent -> {
            hideProgressBar();
            Log.i(LOG_TAG, "iCancellationEvent got ByRequestListener = " + iCancellationEvent.getResult().toString());
        }).cancellationByResponseFilter(iCancellationEvent -> {
            hideProgressBar();
            Log.i(LOG_TAG, "iCancellationEvent got  ByResponseFilter= " + iCancellationEvent.getResult().toString());
        }).maximumRestartsReached(event -> {
            hideProgressBar();
            // if username or password is wrong HttpConversation restart the conversation only the max restart times.
            // @see com.sap.smp.client.httpc.IHttpConversation#setMaximumRestarts(int)
            Log.i(LOG_TAG, "maximumRestartsReached event got = " + event.getResult());
        }).build()).start();
        progressBar.setVisibility(View.VISIBLE);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu;
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void hideProgressBar(){
        // hide progress bar
        handler.post(new Runnable() {
            @Override
            public void run() {
                // Hide the progress bar from layout after finishing task
                progressBar.setVisibility(View.GONE);
            }
        });

    }

    private void showMessage(){
        // hide progress bar
        handler.post(new Runnable() {
            @Override
            public void run() {
                // Hide the progress bar from layout after finishing task
                textView.setText("Ok");


            }
        });

    }
}
