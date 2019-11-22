package com.braincloud.basic_java;

//android specific includes
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

//braincloud specific includes.
import com.bitheads.braincloud.client.*;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.*;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.json.JSONObject;


import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.IOException;

import static com.google.android.gms.auth.api.Auth.GoogleSignInApi;

public class Login extends AppCompatActivity implements  IServerCallback
{
    IServerCallback theCallback;

    //Create brainCloud Wrapper.
    public static BCClient _bc;

    private GoogleSignInClient mGoogleSignInClient;

    private GoogleApiClient mGoogleApiClient;

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        //Create brainCloud Wrapper.
        _bc = new BCClient();

        //give it context within the main activity.
        _bc.setApplicationContext(Login.this);

        //set the callback to this class
        theCallback = this;

        //get a reference to the button of the app
        Button loginButton = findViewById(R.id.loginButton);

        Log.i("onCreate", "tttt----before signIn");

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();


        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        Log.i("onCreate", "tttt----after signIn");
//        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

//        Log.i("client account tttt", account.getIdToken());

        Intent singInIntent = mGoogleSignInClient.getSignInIntent();

        startActivityForResult(singInIntent, 1001);//pass the declared request code here


        Log.i("onCreate----tttt", singInIntent.toString());
        Log.i("onCreate--user--tttt", singInIntent.getAction());

//        GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(singInIntent);
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(singInIntent);
        Log.i("task--tttt", "tttt----before acct");

        try{
            GoogleSignInAccount acct = task.getResult(ApiException.class);
        }catch(ApiException e){
            // Google Sign In failed, update UI appropriately
            Log.w("task--tttt", "Google sign in failed", e);
        }


//        GoogleSignInAccount acct = result.getSignInAccount();

        Log.i("onCreate--tttt", "tttt----after acct");

//        Log.i("onCreate--acct--tttt", acct.getEmail());

//        Log.i("client signin result tttt", result.getStatus().toString());


//
//        Log.i("client signin tttt", mGoogleSignInClient.getApi().getName());
//
//        Log.i("onCreate tttt", "after signIn----google singIn client:    "+ mGoogleSignInClient);


//        Intent singInIntent = mGoogleSignInClient.getSignInIntent();
//
//
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

//
        Log.i("onCreate signin tttt- token", account.getIdToken());
        Log.i("onCreate signin tttt - email", account.getEmail());
        Log.i("onCreate signin tttt - id", account.getId());

        _bc.GetWrapper().authenticateGoogle(account.getId(), account.getIdToken(), true, theCallback);



//        try{
//            String token = GoogleAuthUtil.getToken(this, "jasonl@bitheads.com", "https://www.googleapis.com/auth/plus.login");
//            Log.i("inside try token--tttt", token);
//
//        }catch(Exception e){
//            Log.w("task--tttt", "Google get token failed", e);
//        }
//
//
//        Log.i("onCreate--tttt", "tttt----after token");








        //when this button is clicked create an inline class so that we can keep the keep unique to this class.
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                //get reference to the objects on screen.
                EditText usernameEditText = findViewById(R.id.usernameEditText);
                EditText passwordEditText = findViewById(R.id.passwordEditText);

                //get what was entered into the text fields.
                final String emailEntered = usernameEditText.getText().toString();
                String passwordEntered = passwordEditText.getText().toString();

                //attempt to authenticate
                TextView statusTextView = findViewById(R.id.statusTextView);
                //convert the result to a string
                statusTextView.setText("Authenticating");

                //Authenticate with e-mail and password
//                _bc.GetWrapper().authenticateEmailPassword(emailEntered, passwordEntered, false, theCallback);
                //_bc.GetWrapper().authenticateUniversal(emailEntered, passwordEntered, true, theCallback);














                //this is the new way to get the firebase token.
                FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener( Login.this,  new OnSuccessListener<InstanceIdResult>() {
                    @Override
                    public void onSuccess(InstanceIdResult instanceIdResult) {
                        MyFirebaseMessagingService.FirebaseTokenID = instanceIdResult.getToken();
                        Log.e("NEW_TOKEN", MyFirebaseMessagingService.FirebaseTokenID );

                        //MyFirebaseMessagingService.FirebaseTokenID.

//                        String clients = GoogleSignInAccount.
//                        Log.i("clients", clients);
                        Log.i("user input email tttt", emailEntered);

                        _bc.GetWrapper().authenticateGoogle(emailEntered, MyFirebaseMessagingService.FirebaseTokenID, false, theCallback);

                        String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + MyFirebaseMessagingService.FirebaseTokenID;













                        //set the device up to receive pushnotifications
                        _bc.GetWrapper().getPushNotificationService().registerPushNotificationToken(Platform.GooglePlayAndroid, MyFirebaseMessagingService.FirebaseTokenID, theCallback);
                    }
                });

            }
        });
    }


    protected void onStart() {
        super.onStart();
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
//        Log.i("user Id Token tttt", "---from on Start method----"+account.getIdToken());
    }





    //callback functions
    public void serverCallback(ServiceName serviceName, ServiceOperation serviceOperation, JSONObject jsonData)
    {
        TextView statusTextView = findViewById(R.id.statusTextView);
        //convert the result to a string
        statusTextView.setText("Success!");

        //change the app activity
//        Intent loadApp = new Intent(getApplication(), theGame.class);
//        startActivity(loadApp);

        Log.e("AUTHENTICATED", MyFirebaseMessagingService.FirebaseTokenID );
    }

    public void serverError(ServiceName serviceName, ServiceOperation serviceOperation, int statusCode, int reasonCode, String jsonError)
    {
        TextView statusTextView = findViewById(R.id.statusTextView);
        statusTextView.setText("Fail!");

        Log.e("AUTHENTICATED---servererror---ttt", "fail to login to braindCloud. ");
    }
}
