package com.example.old_man.community;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.old_man.R;

import org.jitsi.meet.sdk.JitsiMeet;
import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;

import java.net.MalformedURLException;
import java.net.URL;

public class DashboardActivity extends AppCompatActivity {
    EditText codeBox;
    Button joinBtn,shareBtn,demoBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        codeBox=findViewById(R.id.codeBox);
        joinBtn=findViewById(R.id.joinBtn);
        shareBtn=findViewById(R.id.shareBtn);
        demoBtn=(Button) findViewById(R.id.demoBtn);
        URL serverURL;
        try{
            serverURL=new URL("https://meet.jit.si");
            JitsiMeetConferenceOptions defaultOptions=
                    new JitsiMeetConferenceOptions.Builder()
                            .setServerURL(serverURL)
                            .setFeatureFlag("welcomepage", false) // Disable the welcome page
                            .build();
            JitsiMeet.setDefaultConferenceOptions(defaultOptions);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }


        joinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JitsiMeetConferenceOptions options=new JitsiMeetConferenceOptions.Builder()
                        .setRoom(codeBox.getText().toString())
                        .setFeatureFlag("welcomepage", false) // Disable the welcome page
                        .build();
                JitsiMeetActivity.launch(DashboardActivity.this,options);
            }
        });
        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String string=codeBox.getText().toString();
                Intent intent=new Intent();
                intent.setAction(intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT,string);
                intent.setType("text/plain");
                startActivity(intent);
            }
        });

    }
}