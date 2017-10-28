package com.schmidthappens.markd.account_authentication;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.schmidthappens.markd.customer_subactivities.ProfileEditActivity;

/**
 * Created by joshua.schmidtibm.com on 10/7/17.
 */

public class FirebaseAuthentication {
    private static final String TAG = "FirebaseAuthentication";

    private Activity activity;
    private FirebaseUser currentUser;
    private static FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    private FirebaseAuth.AuthStateListener authStateListener = new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            currentUser = firebaseAuth.getCurrentUser();
            if (currentUser != null) {
                // User is signed in
                Log.d(TAG, "onAuthStateChanged:signed_in:" + currentUser.getUid());
            } else {
                // User is signed out
                Log.d(TAG, "onAuthStateChanged:signed_out");
                if(!(activity instanceof LoginActivity || activity instanceof ProfileEditActivity) && activity != null) {
                    Intent intent = new Intent(activity, LoginActivity.class);
                    activity.startActivity(intent);
                    activity.finish();
                }
            }
        }
    };

    public FirebaseAuthentication(Activity activity) {
        if(firebaseAuth == null) {
            firebaseAuth = FirebaseAuth.getInstance();
        }
        this.activity = activity;
        attachListener();
    }

    public FirebaseUser getCurrentUser() {
        if(currentUser == null) {
            setCurrentUser();
        }
        return currentUser;
    }
    private void setCurrentUser() {
        currentUser = firebaseAuth.getCurrentUser();
    }

    private void attachListener() {
        firebaseAuth.addAuthStateListener(authStateListener);
    }
    public void detachListener() {
        firebaseAuth.removeAuthStateListener(authStateListener);
    }

    //Should be called during the onStart of Activities
    // Returns true if logged in else returns false
    public boolean checkLogin() {
        currentUser = firebaseAuth.getCurrentUser();
        return (currentUser != null);
    }

    public AuthCredential getAuthCredential(String email, String password) {
        return EmailAuthProvider.getCredential(email, password);

    }

    @NonNull
    public Task<AuthResult> createAccount(final Activity ctx, String email, String password) {
        return firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(ctx, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            currentUser = firebaseAuth.getCurrentUser();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        }
                    }
                });
    }

    @NonNull
    public Task<AuthResult> signIn(final Activity ctx, final String email, final String password) {
        return firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(ctx, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithEmail:success");
                            currentUser = firebaseAuth.getCurrentUser();
                            if(currentUser != null) {
                                String userId = currentUser.getUid();
                                //TODO: Get customer from db who has this userId and load into TempCustomerData
                            }
                        }
                    }
                });
    }

    public void signOut(Activity ctx) {
        firebaseAuth.signOut();
    }
}
