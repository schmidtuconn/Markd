package com.schmidthappens.markd.customer_subactivities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.schmidthappens.markd.AdapterClasses.EditHomeRecyclerViewAdapter;
import com.schmidthappens.markd.R;
import com.schmidthappens.markd.account_authentication.FirebaseAuthentication;
import com.schmidthappens.markd.customer_menu_activities.MainActivity;
import com.schmidthappens.markd.data_objects.TempCustomerData;

public class HomeEditActivityV2 extends AppCompatActivity {
    private static final String TAG = "HomeEditActivity";
    private FirebaseAuthentication authentication;
    private TempCustomerData customerData;

    private Boolean isNewAccount;
    public String street;
    public String city;
    public String state;
    public String zipCode;
    public Double bedrooms;
    public Double bathrooms;
    public Integer squareFootage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_view_home_v2);
        authentication = new FirebaseAuthentication(this);
    }
    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        isNewAccount = !authentication.checkLogin();
        if(!isNewAccount) {
            customerData = new TempCustomerData(authentication, null);
            setTitle("Edit Home");
        } else {
            setUpActionBar();
        }

        processIntent(getIntent());
        initializeXMLObjects();
    }
    @Override
    public void onStop() {
        super.onStop();
        saveHome();
        authentication.detachListener();
        if(customerData != null) {
            customerData.removeListeners();
        }
    }

    //Mark:- Set up functions
    private void setUpActionBar() {
        Log.d(TAG, "setUpActionBar");
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setCustomView(R.layout.view_action_bar);
        } else {
            Log.e(TAG, "ActionBar is null");
        }

        //Set up actionBarButtons
        final ImageView menuButton = findViewById(R.id.burger_menu);
        menuButton.setClickable(false);
        menuButton.setVisibility(View.GONE);
    }
    private void initializeXMLObjects() {
        Log.d(TAG, "initializeXMLObjects");
        final RecyclerView recyclerView = findViewById(R.id.edit_home_recycler);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(false);
        recyclerView
                .addItemDecoration(
                        new DividerItemDecoration(
                                HomeEditActivityV2.this,
                                DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(
                new EditHomeRecyclerViewAdapter(this)
        );
    }
    private void processIntent(final Intent intentToProcess) {
        Log.d(TAG, "processIntent");
        if(intentToProcess != null && !isNewAccount) {
            if(intentToProcess.hasExtra("street")) {
                street = intentToProcess.getStringExtra("street");
            }
            if(intentToProcess.hasExtra("city")) {
                city = intentToProcess.getStringExtra("city");
            }
            if(intentToProcess.hasExtra("state")) {
                state = intentToProcess.getStringExtra("state");
            }
            if(intentToProcess.hasExtra("zipcode")) {
                zipCode = intentToProcess.getStringExtra("zipcode");
            }
            if(intentToProcess.hasExtra("bedrooms")) {
                String bedroomString = intentToProcess.getStringExtra("bedrooms");
                bedrooms = Double.valueOf(bedroomString);
            }
            if(intentToProcess.hasExtra("bathrooms")) {
                String bathroomString = intentToProcess.getStringExtra("bathrooms");
                bathrooms = Double.valueOf(bathroomString);
            }
            if(intentToProcess.hasExtra("squareFootage")) {
                String squareFootageString = intentToProcess.getStringExtra("squareFootage");
                squareFootage = Integer.valueOf(squareFootageString);
            }
        }
    }

    //Mark:- Helper functions
    private void saveHome() {
        customerData.updateHome(
                street,
                city,
                state,
                zipCode,
                bedrooms,
                bathrooms,
                squareFootage
        );
    }
    private void closeActivity() {
        startActivity(new Intent(
                HomeEditActivityV2.this,
                MainActivity.class));
        finish();
    }

    //Mark:- NumberPicker Arrays
    private static final String[] stateArray = {
            "AK", "AL", "AZ", "AR", "CA", "CO", "CT", "DE", "FL", "GA",
            "HI", "ID", "IL", "IN", "IA", "KS", "KY", "LA", "ME", "MD",
            "MA", "MI", "MN", "MS", "MO", "MT", "NE", "NV", "NH", "NJ",
            "NM", "NY", "NC", "ND", "OH", "OK", "OR", "PA", "RI", "SC",
            "SD", "TN", "TX", "UT", "VT", "VA", "WA", "WV", "WI", "WY"
    };
}
