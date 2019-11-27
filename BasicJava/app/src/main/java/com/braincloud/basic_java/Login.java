package com.braincloud.basic_java;

//android specific includes
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.*;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.json.JSONObject;


import com.google.firebase.analytics.FirebaseAnalytics;

import com.google.android.gms.tasks.OnCompleteListener;



























import java.io.IOException;

import static com.google.android.gms.auth.api.Auth.GoogleSignInApi;

public class Login extends AppCompatActivity implements  IServerCallback
{
    IServerCallback theCallback;

    //Create brainCloud Wrapper.
    public static BCClient _bc;

//    private GoogleSignInClient mGoogleSignInClient;

    private GoogleApiClient mGoogleApiClient;

    private FirebaseAnalytics mFirebaseAnalytics;




    private static final String TAG = Login.class.getSimpleName();

    private static final int RC_SIGN_IN = 111;//google sign in request code

    private GoogleSignInClient mGoogleSignInClient;//google sign in client

    private SignInButton defaultSignInButton;

    private TextView userDetailLabel;

    private TextView statusTextView;


    private void findViews() {

        defaultSignInButton = findViewById(R.id.default_google_sign_in_button);
//        customSignInButton = findViewById(R.id.custom_sign_in_button);
//
        userDetailLabel = findViewById(R.id.user_details_label);
//        userProfileImageView = findViewById(R.id.user_profile_image_view);

        statusTextView = findViewById(R.id.statusTextView);

        defaultSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doSignInSignOut();
            }
        });
    }




    private void configureGoogleSignIn() {
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()//request email id
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check for existing Google Sign In account, if the user is already signed in the GoogleSignInAccount will be non-null.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        //update the UI if user has already sign in with the google for this app
        getProfileInformation(account);

        if (account == null){
            Log.i("onStart", "tttt----lastsigned is null");
        }else{
            Log.i("onStart", "tttt----lastsigned"+account.getServerAuthCode());
        }
    }

    public void customGoogleSignIn(View view) {
        doSignInSignOut();
    }

    /**
     * method to do Sign In or Sign Out on the basis of account exist or not
     */
    private void doSignInSignOut() {

        //get the last sign in account
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(Login.this);

        //if account doesn't exist do login else do sign out
        if (account == null)
            doGoogleSignIn();
        else
            doGoogleSignOut();
    }

    /**
     * do google sign in
     */
    private void doGoogleSignIn() {
        Log.i("doGoogleSignIn", "tttt----first");
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        Log.i("doGoogleSignIn", "tttt----second");
        startActivityForResult(signInIntent, RC_SIGN_IN);//pass the declared request code here
        Log.i("doGoogleSignIn", "tttt----third");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            //method to handle google sign in result
            handleSignInResult(task);
        }
    }


    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            getProfileInformation(account);

            //show toast
            Toast.makeText(this, "Google Sign In Successful.", Toast.LENGTH_SHORT).show();

        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.e(TAG, "signInResult:failed code=" + e.getStatusCode());

            //show toast
            Toast.makeText(this, "Failed to do Sign In : " + e.getStatusCode(), Toast.LENGTH_SHORT).show();

            //update Ui for this
            getProfileInformation(null);
        }
    }



    private void getProfileInformation(GoogleSignInAccount acct) {
        //if account is not null fetch the information
        if (acct != null) {

            //user display name
            String personName = acct.getDisplayName();

            //user first name
            String personGivenName = acct.getGivenName();

            //user last name
            String personFamilyName = acct.getFamilyName();

            //user email id
            String personEmail = acct.getEmail();

            //user unique id
            String personId = acct.getId();

//            //user profile pic
//            Uri personPhoto = acct.getPhotoUrl();

            //show the user details
            userDetailLabel.setText("ID : " + personId + "\nDisplay Name : " + personName + "\nFull Name : " + personGivenName + " " + personFamilyName + "\nEmail : " + personEmail);

//            //show the user profile pic
//            Picasso.with(this).load(personPhoto).fit().placeholder(R.mipmap.ic_launcher_round).into(userProfileImageView);

            //change the text of Custom Sign in button to sign out
            statusTextView.setText(getResources().getString(R.string.sign_out));

            //show the label and image view
            userDetailLabel.setVisibility(View.VISIBLE);
//            userProfileImageView.setVisibility(View.VISIBLE);

        } else {

            //if account is null change the text back to Sign In and hide the label and image view
            statusTextView.setText(getResources().getString(R.string.sign_in));
            userDetailLabel.setVisibility(View.GONE);
//            userProfileImageView.setVisibility(View.GONE);

        }
    }


    private void doGoogleSignOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(Login.this, "Google Sign Out done.", Toast.LENGTH_SHORT).show();
                        revokeAccess();
                    }
                });
    }

    private void revokeAccess() {
        mGoogleSignInClient.revokeAccess()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(Login.this, "Google access revoked.", Toast.LENGTH_SHORT).show();
                        getProfileInformation(null);
                    }
                });
    }

















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



        findViews();
        configureGoogleSignIn();

        Log.i("onCreate", "tttt----before signIn");






//        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken(getString(R.string.default_web_client_id))
//                .requestEmail()
//                .build();
//
//
//        // Build a GoogleSignInClient with the options specified by gso.
//        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
//
//        Log.i("onCreate", "tttt----after signIn");
////        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
//
////        Log.i("client account tttt", account.getIdToken());
//
//        Intent singInIntent = mGoogleSignInClient.getSignInIntent();
//
//        startActivityForResult(singInIntent, 1001);//pass the declared request code here
//
//
//        Log.i("onCreate----tttt", singInIntent.toString());
//        Log.i("onCreate--user--tttt", singInIntent.getAction());
//
////        GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(singInIntent);
//        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(singInIntent);
//        Log.i("task--tttt", "tttt----before acct");
//
//        try{
//            GoogleSignInAccount acct = task.getResult(ApiException.class);
//        }catch(ApiException e){
//            // Google Sign In failed, update UI appropriately
//            Log.w("task--tttt", "Google sign in failed", e);
//        }
//
//
////        GoogleSignInAccount acct = result.getSignInAccount();
//
//        Log.i("onCreate--tttt", "tttt----after acct");
//
////        Log.i("onCreate--acct--tttt", acct.getEmail());
//
////        Log.i("client signin result tttt", result.getStatus().toString());
//
//
////
////        Log.i("client signin tttt", mGoogleSignInClient.getApi().getName());
////
////        Log.i("onCreate tttt", "after signIn----google singIn client:    "+ mGoogleSignInClient);
//
//
////        Intent singInIntent = mGoogleSignInClient.getSignInIntent();
////
////
//
//        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
//
////        String token = account.getIdToken();
//
////        Log.i("onCreate signin tttt- token", token);
////        Log.i("onCreate signin tttt - email", account.getEmail());
////        Log.i("onCreate signin tttt - id", account.getId());
////
////        _bc.GetWrapper().authenticateGoogle("jasonbitheads@gamil.com", "eyJhbGciOiJSUzI1NiIsImtpZCI6ImRiMDJhYjMwZTBiNzViOGVjZDRmODE2YmI5ZTE5NzhmNjI4NDk4OTQiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhenAiOiI2MzgzMDMwNjg1OTctZmU5aGtiZnVpcDBqN242dW1nNnAyMms0N2I4cGR0aGcuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdWQiOiI2MzgzMDMwNjg1OTctZm04bDNuYTRhb2E1NGlxa3FsazJoZHAyMGtyYm9udWMuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJzdWIiOiIxMDM2MzIwMDA4NTA0MjYzMTg5OTYiLCJlbWFpbCI6Imphc29uYml0aGVhZHNAZ21haWwuY29tIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsIm5hbWUiOiJqYXNvbiBsaWFuZyIsInBpY3R1cmUiOiJodHRwczovL2xoNC5nb29nbGV1c2VyY29udGVudC5jb20vLW5OVTRyUTRjMVlVL0FBQUFBQUFBQUFJL0FBQUFBQUFBQUFBL0FDSGkzcmN0anhmbTdKc2lwRFdORTVSc0FacVJ1czdnUVEvczk2LWMvcGhvdG8uanBnIiwiZ2l2ZW5fbmFtZSI6Imphc29uIiwiZmFtaWx5X25hbWUiOiJsaWFuZyIsImxvY2FsZSI6ImVuIiwiaWF0IjoxNTc0NDUyNjMxLCJleHAiOjE1NzQ0NTYyMzF9.LWkDVwtTtoxC3_881vuNqwN1sqvTDk8RQOvXT4HEoJe0GFhOTr_Tu8Xqv7bxDder46ZRiMcv5fmdHlOaubRhzu1psOi_g2xI1pgussxDVZmYdcmxjzLp_wZh9OseO3X-XqUDJNgZx9juDqK8HMROyxsmIQj0zd6OF0_JQJWCjuQTzNBvk9wDtmkIHirMAr5hsRBK3bJ0FzsxTVwhz1B2OAkX8LvzmDJX1CDvQ1mHIilYGV2yaTaI-2bK_4hxJY2zesNoo4YrA7LT6FWs6kP0lDTt-dZq8TO_bRL0O2LOb7noykQ3VLYwIUsG29PHB92vGVumhckmgqjrB8ahyIVB1A", false, theCallback);
//
//        _bc.GetWrapper().authenticateGoogle("g103632000850426318996", "eyJhbGciOiJSUzI1NiIsImtpZCI6ImRiMDJhYjMwZTBiNzViOGVjZDRmODE2YmI5ZTE5NzhmNjI4NDk4OTQiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhenAiOiI2MzgzMDMwNjg1OTctZmU5aGtiZnVpcDBqN242dW1nNnAyMms0N2I4cGR0aGcuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdWQiOiI2MzgzMDMwNjg1OTctZm04bDNuYTRhb2E1NGlxa3FsazJoZHAyMGtyYm9udWMuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJzdWIiOiIxMDM2MzIwMDA4NTA0MjYzMTg5OTYiLCJlbWFpbCI6Imphc29uYml0aGVhZHNAZ21haWwuY29tIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsIm5hbWUiOiJqYXNvbiBsaWFuZyIsInBpY3R1cmUiOiJodHRwczovL2xoNC5nb29nbGV1c2VyY29udGVudC5jb20vLW5OVTRyUTRjMVlVL0FBQUFBQUFBQUFJL0FBQUFBQUFBQUFBL0FDSGkzcmN0anhmbTdKc2lwRFdORTVSc0FacVJ1czdnUVEvczk2LWMvcGhvdG8uanBnIiwiZ2l2ZW5fbmFtZSI6Imphc29uIiwiZmFtaWx5X25hbWUiOiJsaWFuZyIsImxvY2FsZSI6ImVuIiwiaWF0IjoxNTc0NDUyNjMxLCJleHAiOjE1NzQ0NTYyMzF9.LWkDVwtTtoxC3_881vuNqwN1sqvTDk8RQOvXT4HEoJe0GFhOTr_Tu8Xqv7bxDder46ZRiMcv5fmdHlOaubRhzu1psOi_g2xI1pgussxDVZmYdcmxjzLp_wZh9OseO3X-XqUDJNgZx9juDqK8HMROyxsmIQj0zd6OF0_JQJWCjuQTzNBvk9wDtmkIHirMAr5hsRBK3bJ0FzsxTVwhz1B2OAkX8LvzmDJX1CDvQ1mHIilYGV2yaTaI-2bK_4hxJY2zesNoo4YrA7LT6FWs6kP0lDTt-dZq8TO_bRL0O2LOb7noykQ3VLYwIUsG29PHB92vGVumhckmgqjrB8ahyIVB1A", false, theCallback);
//
////        try{
////            String token = GoogleAuthUtil.getToken(this, "jasonl@bitheads.com", "https://www.googleapis.com/auth/plus.login");
////            Log.i("inside try token--tttt", token);
////
////        }catch(Exception e){
////            Log.w("task--tttt", "Google get token failed", e);
////        }
////
////
////        Log.i("onCreate--tttt", "tttt----after token");








        //when this button is clicked create an inline class so that we can keep the keep unique to this class.
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
//                //get reference to the objects on screen.
//                EditText usernameEditText = findViewById(R.id.usernameEditText);
//                EditText passwordEditText = findViewById(R.id.passwordEditText);
//
//                //get what was entered into the text fields.
//                final String emailEntered = usernameEditText.getText().toString();
//                String passwordEntered = passwordEditText.getText().toString();

                //attempt to authenticate
                TextView statusTextView = findViewById(R.id.statusTextView);
                //convert the result to a string
                statusTextView.setText("Authenticating");

                //Authenticate with e-mail and password
//                _bc.GetWrapper().authenticateEmailPassword(emailEntered, passwordEntered, false, theCallback);
                //_bc.GetWrapper().authenticateUniversal(emailEntered, passwordEntered, true, theCallback);

                Log.i("onclick--tttt", "login button----111");

                doGoogleSignIn();

                Log.i("onclick--tttt", "login button----222");








//                //this is the new way to get the firebase token.
//                FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener( Login.this,  new OnSuccessListener<InstanceIdResult>() {
//                    @Override
//                    public void onSuccess(InstanceIdResult instanceIdResult) {
//                        MyFirebaseMessagingService.FirebaseTokenID = instanceIdResult.getToken();
//                        Log.e("NEW_TOKEN", MyFirebaseMessagingService.FirebaseTokenID );
//
//                        //MyFirebaseMessagingService.FirebaseTokenID.
//
////                        String clients = GoogleSignInAccount.
////                        Log.i("clients", clients);
////                        Log.i("user input email tttt", emailEntered);
//
////                        _bc.GetWrapper().authenticateGoogle(emailEntered, MyFirebaseMessagingService.FirebaseTokenID, false, theCallback);
//
//                        String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + MyFirebaseMessagingService.FirebaseTokenID;
//
//
//                        _bc.GetWrapper().authenticateGoogle("jasonbitheads@gamil.com", "eyJhbGciOiJSUzI1NiIsImtpZCI6ImRiMDJhYjMwZTBiNzViOGVjZDRmODE2YmI5ZTE5NzhmNjI4NDk4OTQiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhenAiOiI2MzgzMDMwNjg1OTctZmU5aGtiZnVpcDBqN242dW1nNnAyMms0N2I4cGR0aGcuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdWQiOiI2MzgzMDMwNjg1OTctZm04bDNuYTRhb2E1NGlxa3FsazJoZHAyMGtyYm9udWMuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJzdWIiOiIxMDkxNzE0NTgyOTk5ODc1ODIzODQiLCJlbWFpbCI6InBzZmlsbGlvbkBnbWFpbC5jb20iLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwibmFtZSI6IlBhc2NhbCBGaWxsaW9uIiwicGljdHVyZSI6Imh0dHBzOi8vbGg1Lmdvb2dsZXVzZXJjb250ZW50LmNvbS8tZllSOVhYVkRLUXMvQUFBQUFBQUFBQUkvQUFBQUFBQUFBQUEvQUNIaTNyZEc5cnNuU1Jhby1oWmh3WXVabjJuQTdVWm5oZy9zOTYtYy9waG90by5qcGciLCJnaXZlbl9uYW1lIjoiUGFzY2FsIiwiZmFtaWx5X25hbWUiOiJGaWxsaW9uIiwibG9jYWxlIjoiZW4iLCJpYXQiOjE1NzQ0NTY2NDUsImV4cCI6MTU3NDQ2MDI0NX0.GRtxZCaplfCbYVbjh92tN1x8ZbLRSS-5HlyUaU-iST02jJAha4AdzjGrP28e40X0Kw1SIu2TQqGAUeEKGSeGTW_6A8-4d7m2E_wuUo05rquI4CGlzcxP4u2tVGE537FBpB0y4J-9eoiAPqJhH6iR33XmCVghZaSdaRdfxT-y-1bz4e575BgwpC9kIBDMYAKa9p7G522rPw3pwpw7c2d3WQ_tMQZS4jnlu7WuxEpa0fs__qLXjeoVSoChESRcLdFDWW5k_fNEHZMfqVn-NAL4DvIfULquXpivaOPOgdC1jfiHstZB88JTj0ZaklYzV4aM54k9DBX8WuERrTd_YLpepA", false, theCallback);
//
//
//
//                        //set the device up to receive pushnotifications
//                        _bc.GetWrapper().getPushNotificationService().registerPushNotificationToken(Platform.GooglePlayAndroid, MyFirebaseMessagingService.FirebaseTokenID, theCallback);
//                    }
//                });

            }
        });
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
