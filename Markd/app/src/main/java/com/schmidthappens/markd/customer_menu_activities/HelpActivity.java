package com.schmidthappens.markd.customer_menu_activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.schmidthappens.markd.R;
import com.schmidthappens.markd.account_authentication.FirebaseAuthentication;
import com.schmidthappens.markd.utilities.SendEmail;
import com.schmidthappens.markd.utilities.StringUtilities;
import com.schmidthappens.markd.view_initializers.ActionBarInitializer;

import org.json.JSONObject;

/**
 * Created by joshua.schmidtibm.com on 1/9/18.
 */

public class HelpActivity extends AppCompatActivity {
    private final static String TAG = "HelpActivity";
    private FirebaseAuthentication authentication;
    private String userEmail;

    private EditText userMessage;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_activity_help_view);
        authentication = new FirebaseAuthentication(this);
        initializeXmlObjects();
    }
    @Override
    public void onStart() {
        super.onStart();
        if(!authentication.checkLogin()) {
            Intent intent = new Intent(this, HelpActivity.class);
            startActivity(intent);
            finish();
        } else {
            userEmail = authentication.getCurrentUser().getEmail();
            authentication.getUserType(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot != null && dataSnapshot.getValue() != null) {
                        String userType = dataSnapshot.getValue().toString();
                        if (userType.equalsIgnoreCase("customer")) {
                            new ActionBarInitializer(HelpActivity.this, true, "customer");
                        } else {
                            new ActionBarInitializer(HelpActivity.this, false, "contractor");
                        }
                    } else {
                        somethingWentWrong("Null value from onDataChange");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    somethingWentWrong(databaseError.toString());
                }
            });
        }
    }
    @Override
    public void onStop() {
        super.onStop();
        authentication.detachListener();
    }
    private void initializeXmlObjects() {
        userMessage = (EditText)findViewById(R.id.help_message);
        saveButton = (Button)findViewById(R.id.help_save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(StringUtilities.isNullOrEmpty(userMessage.getText().toString())) {
                    Toast.makeText(HelpActivity.this, "Message is empty", Toast.LENGTH_SHORT).show();
                } else {
                    sendMessage();
                }
            }
        });
    }

    private void sendMessage() {
        SendEmail.sendMessage(this, userEmail, userMessage.getText().toString(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                // display response
                Log.d(TAG, response.toString());
                Toast.makeText(HelpActivity.this, "Message sent.", Toast.LENGTH_SHORT).show();
                HelpActivity.this.finish();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                HelpActivity.this.somethingWentWrong(error.toString());
                error.printStackTrace();
            }
        });

    }

    private void somethingWentWrong(String logMessage) {
        Log.d(TAG, logMessage);
        Toast.makeText(HelpActivity.this, "Oops...something went wrong.", Toast.LENGTH_SHORT).show();
        HelpActivity.this.finish();
    }
}
