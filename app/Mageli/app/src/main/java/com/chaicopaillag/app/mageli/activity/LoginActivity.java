package com.chaicopaillag.app.mageli.activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.chaicopaillag.app.mageli.R;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    EditText txt_correo,txt_contrasenia;
    Button btn_iniciar;
    TextView btn_registro;
    TextInputLayout inputcorreo,inputcontrasenia;

    SignInButton btn_google;
    private LoginButton btn_facebook;
    private CallbackManager callbackManager;

    private GoogleApiClient googleApiClient;

    public static final int SIGN_IN_CODE = 9001;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        callbackManager = CallbackManager.Factory.create();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        txt_correo=(EditText)findViewById(R.id.txtcorreo);
        txt_contrasenia=(EditText)findViewById(R.id.txtcontrasenia);

        btn_iniciar=(Button)findViewById(R.id.btnlogin);
        btn_registro=(TextView)findViewById(R.id.btnregistrateahora);

        btn_facebook=(LoginButton) findViewById(R.id.btnfacebook);
        btn_google=(SignInButton)findViewById(R.id.btngoogle);

        btn_registro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(LoginActivity.this,RegistroUsuarioActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        btn_iniciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Validarlogeo();
            }
        });


        btn_google.setColorScheme(SignInButton.COLOR_DARK);

        btn_google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                startActivityForResult(intent, SIGN_IN_CODE);
            }
        });

        btn_facebook.setReadPermissions(Arrays.asList("email"));

        btn_facebook.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
                Ir_a_inicio();
            }

            @Override
            public void onCancel() {
            Toast.makeText(getApplicationContext(),getString(R.string.cancela_sesion_facebook),Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(getApplicationContext(),getString(R.string.error_sesion_facebook),Toast.LENGTH_LONG).show();
            }
        });

        firebaseAuth = FirebaseAuth.getInstance();

        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser usuario = firebaseAuth.getCurrentUser();
                if (usuario != null) {
                    Ir_a_inicio();
                }
            }
        };



    }
    private void handleFacebookAccessToken(AccessToken accessToken) {
//        progressBar.setVisibility(View.VISIBLE);
//        loginButton.setVisibility(View.GONE);

        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), R.string.error_login_firebase, Toast.LENGTH_LONG).show();
                }
//                progressBar.setVisibility(View.GONE);
//                loginButton.setVisibility(View.VISIBLE);
            }
        });
    }
    private void Ir_a_inicio() {
        Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public  void Validarlogeo(){

        String correo,contrasenia;
        correo=txt_correo.getText().toString();
        contrasenia=txt_contrasenia.getText().toString();

        if (correo.equals("")){
            txt_correo.setError(getString(R.string.error_correo));
        }

        if(contrasenia.equals("")){
            txt_contrasenia.setError(getString(R.string.error_contrasenia));
        }
        else if (!validarcorreo(correo)){
            txt_correo.setError(getString(R.string.error_correo_no_valido));
            return;
        }else {
            firebaseAuth.signInWithEmailAndPassword(correo,contrasenia)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                Ir_a_inicio();
                            }else {
                                Toast.makeText(LoginActivity.this, getText(R.string.error_ingreso), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }

    }
    public static boolean validarcorreo(String correo){

        String regex = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(correo);

        return matcher.matches();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SIGN_IN_CODE) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }else{

            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }
    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            firebaseAuthWithGoogle(result.getSignInAccount());
        }else {
            Toast.makeText(this, R.string.error_conexion_google, Toast.LENGTH_SHORT).show();
        }

    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount signInAccount) {

//        progressBar.setVisibility(View.VISIBLE);
//        signInButton.setVisibility(View.GONE);

        AuthCredential credential = GoogleAuthProvider.getCredential(signInAccount.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

//                progressBar.setVisibility(View.GONE);
//                signInButton.setVisibility(View.VISIBLE);

                if (!task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), R.string.error_login_firebase, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(firebaseAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
//        firebaseAuth.removeAuthStateListener(firebaseAuthListener);

        if (firebaseAuthListener != null) {
            firebaseAuth.removeAuthStateListener(firebaseAuthListener);
        }
    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, R.string.error_conexion_google, Toast.LENGTH_SHORT).show();
    }
}