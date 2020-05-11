package com.jojastream.shuffallery.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.jojastream.shuffallery.R;
import com.jojastream.shuffallery.object.User;
import com.jojastream.shuffallery.object.Wallpaper;
import com.jojastream.shuffallery.util.Constant;
import com.jojastream.shuffallery.util.NetworkUtil;
import com.jojastream.shuffallery.util.Util;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.jojastream.shuffallery.util.Constant.PERMISSIONS;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.textViewResetPassword)
    TextView textViewResetPassword;
    @BindView(R.id.buttonSignUp)
    Button buttonSignUp;
    @BindView(R.id.buttonLogIn)
    Button buttonLogIn;
    @BindView(R.id.editTextPassword)
    EditText editTextPassword;
    @BindView(R.id.editTextEmailAddress)
    EditText editTextEmailAddress;
    @BindView(R.id.imageViewGoogle)
    ImageView imageViewGoogle;
    @BindView(R.id.linearLayoutMain)
    LinearLayout linearLayoutMain;
    @BindView(R.id.textViewPermission)
    TextView textViewPermission;

    public static final int PERMISSION_REQUEST_CODE = 3241;
    private GoogleSignInOptions gso;
    public static GoogleSignInClient mGoogleSignInClient;
    private final int RC_SIGN_IN = 10024;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        linearLayoutMain.setVisibility(View.GONE);
        textViewPermission.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        if (!checkPermission()) {
            ActivityCompat.requestPermissions(this,
                    PERMISSIONS,
                    PERMISSION_REQUEST_CODE);
        }

        super.onResume();

    }

    private boolean checkPermission() {
        for (String permission : PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        initialize();


        return true;
    }

    private SweetAlertDialog mDialog;

    @Override
    public void onBackPressed() {
        Util.hideAlertDialog();
        if (mDialog != null) {
            mDialog.dismiss();
        }

        mDialog = new SweetAlertDialog(LoginActivity.this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText(getString(R.string.exit))
                .setContentText(getString(R.string.are_you_sure))
                .setCancelText(getString(R.string.yes))
                .setConfirmText(getString(R.string.no))
                .showCancelButton(true)
                .setCancelClickListener(sDialog -> {
                    super.onBackPressed();
                })
                .setConfirmClickListener(sweetAlertDialog -> {
                    mDialog.dismissWithAnimation();
                });
        mDialog.show();
    }

    private void initialize() {
        textViewPermission.setVisibility(View.GONE);
        linearLayoutMain.setVisibility(View.VISIBLE);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.edit().putString(Constant.USER_LIKE_STATUS, Constant.LIKE_STATUS).apply();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            Util.showAlertDialog(this, "Please Wait!");
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
        initClickListeners();
        initGSO();
    }

    private void initClickListeners() {
        textViewResetPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ResetPasswordActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
        buttonSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SingUpActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        buttonLogIn.setOnClickListener(v -> {
            if (NetworkUtil.getConnectivityStatus(LoginActivity.this)
                    && Util.isEditTextEmpty(LoginActivity.this, editTextEmailAddress)
                    && Util.isValidEmail(LoginActivity.this, editTextEmailAddress)
                    && Util.isEditTextEmpty(LoginActivity.this, editTextPassword)
                    && Util.isValidPassword(LoginActivity.this, editTextPassword)
            ) {
                Util.showAlertDialog(this,getString(R.string.please_wait_login));
                FirebaseDatabase.getInstance().getReference().child(Constant.WALLPAPER_INFO).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.child(Util.getDeviceId(LoginActivity.this)).exists()) {
                            FirebaseAuth.getInstance().signInWithEmailAndPassword(editTextEmailAddress.getText().toString(),
                                    editTextPassword.getText().toString()).addOnSuccessListener(authResult -> {
                                FirebaseStorage.getInstance().getReference().child("Wallpapers").child(Util.getDeviceId(LoginActivity.this))
                                        .putBytes(Util.wallpaperToByte(LoginActivity.this)).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Util.hideAlertDialog();
                                        Toast.makeText(LoginActivity.this, R.string.cant_upload, Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnSuccessListener(taskSnapshot -> {
                                    FirebaseStorage.getInstance().getReference().child("Wallpapers").child(Util.getDeviceId(LoginActivity.this)).
                                            getDownloadUrl().addOnSuccessListener(uri -> {
                                        Wallpaper wallpaper = new Wallpaper("0", "0", "0", uri.toString());
                                        FirebaseDatabase.getInstance().getReference().child(Constant.WALLPAPER_INFO).
                                                child(Util.getDeviceId(LoginActivity.this)).setValue(wallpaper).addOnSuccessListener(aVoid1 ->
                                        {
                                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                            finish();
                                        });
                                    });
                                });
                            }).addOnFailureListener(e -> {
                                Util.showAlertDialogError(LoginActivity.this, getString(R.string.didnot_login), "Oops...");
                            });

                        } else {
                            login();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Util.hideAlertDialog();
                        Toast.makeText(LoginActivity.this, R.string.cant_upload, Toast.LENGTH_SHORT).show();
                    }
                });

            } else {
                Util.hideAlertDialog();
            }
        });

        imageViewGoogle.setOnClickListener(v -> {
            if (NetworkUtil.getConnectivityStatus(LoginActivity.this)) {
                sigInWithGoogle();
            }
        });


    }

    private void initGSO() {
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

    }

    private void login() {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(editTextEmailAddress.getText().toString(),
                editTextPassword.getText().toString()).addOnCompleteListener(authResult -> {
                    if (authResult.isSuccessful()){
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }else{
                        Util.showAlertDialogError(LoginActivity.this, getString(R.string.didnot_login), "Oops...");
                    }

        });
    }

    private void sigInWithGoogle() {
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mGoogleSignInClient.signOut();
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account);
                }
            } catch (ApiException e) {
                Util.hideAlertDialog();
                Toast.makeText(this, R.string.google_signin_failed, Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (String permission : PERMISSIONS) {
                if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    checkPermission();
                    return;
                }
            }
        }
    }


    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Util.showAlertDialog(LoginActivity.this, getString(R.string.please_wait_login));
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        FirebaseDatabase.getInstance().getReference().child(Constant.USERS).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(Util.getDeviceId(LoginActivity.this)).exists()) {
                    FirebaseDatabase.getInstance().getReference().child(Constant.USERS).child(Util.getDeviceId(LoginActivity.this)).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (acct.getEmail() != null
                                    && acct.getEmail().equals(dataSnapshot.child("emailAddress").getValue().toString())) {
                                FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        {
                                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                            finish();
                                        }
                                    } else {
                                        Toast.makeText(LoginActivity.this, R.string.failed_auth, Toast.LENGTH_SHORT).show();
                                        Util.hideAlertDialog();
                                    }
                                });
                            } else {
                                Util.hideAlertDialog();
                                Toast.makeText(LoginActivity.this, R.string.have_account, Toast.LENGTH_SHORT).show();
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Util.hideAlertDialog();
                        }
                    });

                } else {
                    Util.showAlertDialog(LoginActivity.this, getString(R.string.please_wait));
                    FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            {
                                User user = new User(acct.getEmail(), acct.getDisplayName(), Constant.COIN_NUMBER, Constant.LIKE_STATUS, Util.getDeviceId(LoginActivity.this));
                                FirebaseDatabase.getInstance().getReference().child(Constant.USERS).
                                        child(Util.getDeviceId(LoginActivity.this)).setValue(user).addOnSuccessListener(aVoid -> {
                                    FirebaseStorage.getInstance().getReference().child("Wallpapers").child(Util.getDeviceId(LoginActivity.this))
                                            .putBytes(Util.wallpaperToByte(LoginActivity.this)).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(LoginActivity.this, R.string.cant_upload, Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnSuccessListener(taskSnapshot -> {
                                        FirebaseStorage.getInstance().getReference().child("Wallpapers").child(Util.getDeviceId(LoginActivity.this)).
                                                getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                Wallpaper wallpaper = new Wallpaper("0", "0", "0", uri.toString());
                                                FirebaseDatabase.getInstance().getReference().child(Constant.WALLPAPER_INFO).
                                                        child(Util.getDeviceId(LoginActivity.this)).setValue(wallpaper).addOnSuccessListener(aVoid1 ->
                                                {
                                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    startActivity(intent);
                                                    finish();
                                                });
                                            }
                                        });

                                    });
                                }).addOnFailureListener(e -> {
                                    Util.hideAlertDialog();
                                    Toast.makeText(LoginActivity.this, R.string.cant_upload, Toast.LENGTH_SHORT).show();
                                });

                            }
                        } else {
                            Util.hideAlertDialog();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Util.hideAlertDialog();
            }
        });
    }
}
