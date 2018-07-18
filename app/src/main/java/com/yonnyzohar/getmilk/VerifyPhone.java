package com.yonnyzohar.getmilk;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import com.yonnyzohar.getmilk.customers.OrderConsultant;
import com.yonnyzohar.getmilk.providers.ProviderMain;

public class VerifyPhone extends AppCompatActivity {

    private boolean mVerificationInProgress = false;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private FirebaseAuth mAuth;
    TextView t1,t2;
    ImageView i1;
    EditText e1,e2;
    Button b1,b2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_phone);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


    }

    @Override
    public void onStart() {
        super.onStart();

        e1 = findViewById(R.id.Phonenoedittext);
        b1 = findViewById(R.id.PhoneVerify);
        t1 = findViewById(R.id.textView2Phone);
        i1 = findViewById(R.id.imageView2Phone);
        e2 = findViewById(R.id.OTPeditText);
        b2 = findViewById(R.id.OTPVERIFY);
        t2 = findViewById(R.id.textViewVerified);
        mAuth = FirebaseAuth.getInstance();

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    "+972" + e1.getText().toString(),
                    60,
                    java.util.concurrent.TimeUnit.SECONDS,
                    VerifyPhone.this,
                    new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                        @Override
                        public void onVerificationCompleted(PhoneAuthCredential credential) {
                            // Log.d(TAG, "onVerificationCompleted:" + credential);
                            mVerificationInProgress = false;
                            Toast.makeText(VerifyPhone.this,R.string.verification_complete,Toast.LENGTH_SHORT).show();
                            signInWithPhoneAuthCredential(credential);
                        }

                        @Override
                        public void onVerificationFailed(FirebaseException e) {
                            // Log.w(TAG, "onVerificationFailed", e);
                            Toast.makeText(VerifyPhone.this,R.string.verification_failed,Toast.LENGTH_SHORT).show();
                            if (e instanceof FirebaseAuthInvalidCredentialsException) {
                                // Invalid request
                                Toast.makeText(VerifyPhone.this,R.string.invalid_number,Toast.LENGTH_SHORT).show();
                                // ...
                            } else if (e instanceof FirebaseTooManyRequestsException) {
                            }

                        }

                        @Override
                        public void onCodeSent(String verificationId,
                                               PhoneAuthProvider.ForceResendingToken token) {
                            // Log.d(TAG, "onCodeSent:" + verificationId);
                            Toast.makeText(VerifyPhone.this,R.string.verification_sent,Toast.LENGTH_SHORT).show();
                            // Save verification ID and resending token so we can use them later
                            mVerificationId = verificationId;
                            mResendToken = token;
                            e1.setVisibility(View.GONE);
                            b1.setVisibility(View.GONE);
                            t1.setVisibility(View.GONE);
                            i1.setVisibility(View.GONE);
                            t2.setVisibility(View.VISIBLE);
                            e2.setVisibility(View.VISIBLE);
                            b2.setVisibility(View.VISIBLE);
                            // ...
                        }
                    });
            }
        });

        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, e2.getText().toString());
                // [END verify_with_code]
                signInWithPhoneAuthCredential(credential);
            }
        });


        Log.d(Model.TAG,  "Verifiy phone displayed!!");

    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {

        mAuth.getCurrentUser().linkWithCredential(credential).addOnCompleteListener(VerifyPhone.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful())
                {
                    if(Model.userType == Model.DBRefs.CUSTOMERS)
                    {
                        Intent intent = new Intent(VerifyPhone.this, OrderConsultant.class);//OrderConsultant
                        //intent.putExtra("userName", "fucking moron");
                        startActivity(intent);
                    }
                    else
                    {
                        Intent intent = new Intent(VerifyPhone.this, ProviderMain.class);//ProviderMapActivity
                        //intent.putExtra("userName", "fucking moron");
                        startActivity(intent);
                    }
                }
                else
                {
                    Log.w(Model.TAG, "linkWithCredential:failure", task.getException());
                    Toast.makeText(VerifyPhone.this,"Invalid Verification",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
