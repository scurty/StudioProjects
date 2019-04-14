package com.odata.syngenta.synodata;



import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.sap.smp.client.httpc.HttpConversationManager;
import com.sap.smp.client.httpc.IHttpConversation;
import com.sap.smp.client.httpc.events.IReceiveEvent;

import java.net.URL;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        HttpConversationManager manager = new HttpConversationManager(this);
        IHttpConversation conv = manager.create(Uri.parse("http://services.odata.org/V3/OData/OData.svc/"));

        conv.setResponseListener(event -> {
            Log.i("MyFirstProject", IReceiveEvent.Util.getResponseBody(event.getReader()));
        });
        conv.start();

    }



}
