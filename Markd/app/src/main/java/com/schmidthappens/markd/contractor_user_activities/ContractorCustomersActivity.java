package com.schmidthappens.markd.contractor_user_activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.schmidthappens.markd.AdapterClasses.CustomerListRecyclerViewAdapter;
import com.schmidthappens.markd.R;
import com.schmidthappens.markd.account_authentication.FirebaseAuthentication;
import com.schmidthappens.markd.account_authentication.LoginActivity;
import com.schmidthappens.markd.customer_menu_activities.ServiceHistoryActivity;
import com.schmidthappens.markd.data_objects.Customer;
import com.schmidthappens.markd.data_objects.TempContractorData;
import com.schmidthappens.markd.utilities.ContractorUtilities;
import com.schmidthappens.markd.utilities.CustomerGetter;
import com.schmidthappens.markd.utilities.CustomerSelectedInterface;
import com.schmidthappens.markd.utilities.OnGetDataListener;
import com.schmidthappens.markd.view_initializers.ActionBarInitializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by joshua.schmidtibm.com on 9/30/17.
 */

public class ContractorCustomersActivity extends AppCompatActivity implements CustomerSelectedInterface{
    private final static String TAG = "ContractorCustomersActv";
    FirebaseAuthentication authentication;
    TempContractorData contractorData;
    private RecyclerView customerRecyclerView;
    private TextView noCustomerTextView;
    private EditText customerFilter;
    private Button messageAll;
    private List<Customer> customers;
    private List<String> customerReferences;
    private final String[] alertDialogOptions = {
            "Send Notification",
            "Edit Home Details",
            "Edit Service History"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contractor_customers_view);
        authentication = new FirebaseAuthentication(this);
        new ActionBarInitializer(this, false, "contractor");
        initializeXmlObjects();

    }
    @Override
    public void onStart() {
        super.onStart();
        if(!authentication.checkLogin()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        contractorData = new TempContractorData((authentication.getCurrentUser().getUid()), new ContractorCustomersActivity.ContractorCustomersGetDataListener());
    }
    @Override
    public void onStop() {
        super.onStop();
        authentication.detachListener();
    }

    private void initializeXmlObjects() {
        customerRecyclerView = (RecyclerView)findViewById(R.id.contractor_customers_recycler_view);
        noCustomerTextView = (TextView)findViewById(R.id.contractor_customers_empty_list);
        customerFilter = (EditText)findViewById(R.id.customer_filter);
        customerFilter.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                String lastNameFilter = textView.getText().toString();
                Log.d(TAG, "LastName - " + textView.getText().toString());
                customerRecyclerView.setAdapter(
                        new CustomerListRecyclerViewAdapter(
                                ContractorCustomersActivity.this,
                                customers,
                                customerReferences,
                                lastNameFilter));
                Log.d(TAG, "Customers size:" + customers.size());
                return false;
            }
        });
        messageAll = (Button)findViewById(R.id.contractor_customers_message_all);
        messageAll.setOnClickListener(messageAllClickListener);
    }
    private void setUpRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        customerRecyclerView.setLayoutManager(layoutManager);
        customerRecyclerView.setHasFixedSize(true);
        customerRecyclerView.addItemDecoration(new DividerItemDecoration(ContractorCustomersActivity.this, DividerItemDecoration.VERTICAL));
        customerReferences = contractorData.getCustomers();
        if(customerReferences != null && customerReferences.size() > 0) {
            noCustomerTextView.setVisibility(View.GONE);
            FirebaseDatabase.getInstance().getReference("users").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.v(TAG, dataSnapshot.toString());
                    final Map<String, Customer> customerMap = CustomerGetter
                            .getCustomersFromReferences(customerReferences, dataSnapshot);
                    customers = new ArrayList<>(customerMap.values());
                    customerReferences = new ArrayList<>(customerMap.keySet());
                    customerRecyclerView.setAdapter(
                            new CustomerListRecyclerViewAdapter(
                                    ContractorCustomersActivity.this,
                                    customers,
                                    customerReferences));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, databaseError.toString());
                    Toast.makeText(ContractorCustomersActivity.this, "Oops..something went wrong.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            noCustomerTextView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onCustomerSelected(String customerId) {
        showAlertDialog(customerId);
    }
    private void showAlertDialog(final String customerId) {
        new AlertDialog.Builder(ContractorCustomersActivity.this)
                .setTitle("What would you like to do?")
                .setItems(alertDialogOptions, (DialogInterface dialog, int which) -> {
                        // The 'which' argument contains the index position
                        // of the selected item
                        if(which == 0) {
                            Log.i(TAG, "Push Notification:" + customerId);
                            goToNotificationsPage(customerId);
                        } else if (which == 1) {
                            Log.i(TAG, "Go to page:" + customerId);
                            editHomeDetails(customerId);
                        } else if (which == 2) {
                            Log.i(TAG, "Edit Service History: " + customerId);
                            editServiceHistory(customerId);
                        }
                }).create().show();
    }
    private void goToNotificationsPage(final String customerId) {
        final Intent goToSendNotifactionsPage = new Intent(this, SendNotificationsActivity.class)
                .putExtra("customerId", customerId);
        startActivity(goToSendNotifactionsPage);
    }
    private void editHomeDetails(final String customerId) {
        final Class contractorClass = ContractorUtilities.getClassFromContractorType(contractorData.getType());
        if(contractorClass == null) {
            Log.e(TAG, "contractorClass is null");
        } else {
            Intent goToCustomerPage = new Intent(this, contractorClass)
                    .putExtra("isContractor", true)
                    .putExtra("customerId", customerId);
            startActivity(goToCustomerPage);
        }
    }
    private void editServiceHistory(final String customerId) {
        final Intent goToCustomerPage = new Intent(this, ServiceHistoryActivity.class)
                    .putExtra("contractorType", contractorData.getType())
                    .putExtra("isContractor", true)
                    .putExtra("customerId", customerId);
        startActivity(goToCustomerPage);
    }
    private class ContractorCustomersGetDataListener implements OnGetDataListener {
        @Override
        public void onStart() {
            Log.v(TAG, "Getting TempContractorData");
        }

        @Override
        public void onSuccess(DataSnapshot data) {
            Log.v(TAG, "Got TempContractorData");
            Log.d(TAG, data.toString());
            setUpRecyclerView();
        }

        @Override
        public void onFailed(DatabaseError databaseError) {
            Log.e(TAG, databaseError.toString());
        }
    }
    private View.OnClickListener messageAllClickListener = view -> {
        startActivity(new Intent(
                ContractorCustomersActivity.this,
                SendNotificationsActivity.class));
    };
}
