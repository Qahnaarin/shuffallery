package com.jojastream.shuffallery.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
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

import static com.jojastream.shuffallery.util.Constant.PERMISSIONS;

public class SingUpActivity extends AppCompatActivity {


    @BindView(R.id.imageViewClose)
    ImageView imageViewClose;
    @BindView(R.id.editTextUserName)
    EditText editTextUserName;
    @BindView(R.id.editTextEmail)
    EditText editTextEmail;
    @BindView(R.id.editTextConfirmEmail)
    EditText editTextConfirmEmail;
    @BindView(R.id.editTextPassword)
    EditText editTextPassword;
    @BindView(R.id.editTextConfirmPassword)
    EditText editTextConfirmPassword;
    @BindView(R.id.buttonCreateAccount)
    Button buttonCreateAccount;
    @BindView(R.id.linearLayoutMain)
    LinearLayout linearLayoutMain;
    @BindView(R.id.textViewPermission)
    TextView textViewPermission;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_sing_up);
        ButterKnife.bind(this);
        linearLayoutMain.setVisibility(View.GONE);
        textViewPermission.setVisibility(View.VISIBLE);
        initClickListeners();
    }



    @Override
    protected void onResume() {
        if (!checkPermission()) {
            ActivityCompat.requestPermissions(this,
                    PERMISSIONS,
                    LoginActivity.PERMISSION_REQUEST_CODE);
        }
        super.onResume();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LoginActivity.PERMISSION_REQUEST_CODE) {
            for (String permission : PERMISSIONS) {
                if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    checkPermission();
                    return;
                }
            }
        }
    }

    private void initialize() {
        textViewPermission.setVisibility(View.GONE);
        linearLayoutMain.setVisibility(View.VISIBLE);
        initClickListeners();

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

    private void initClickListeners() {
        imageViewClose.setOnClickListener(v -> finish());
        buttonCreateAccount.setOnClickListener(v -> {
            if (Util.isEditTextEmpty(SingUpActivity.this, editTextUserName)
                    && Util.isEditTextEmpty(SingUpActivity.this, editTextEmail)
                    && Util.isValidEmail(SingUpActivity.this, editTextEmail)
                    && Util.isEditTextEmpty(SingUpActivity.this, editTextConfirmEmail)
                    && Util.isValidEmail(SingUpActivity.this, editTextConfirmEmail)
                    && Util.isConfirmEditText(SingUpActivity.this, editTextEmail, editTextConfirmEmail)
                    && Util.isEditTextEmpty(SingUpActivity.this, editTextPassword)
                    && Util.isValidPassword(SingUpActivity.this, editTextPassword)
                    && Util.isEditTextEmpty(SingUpActivity.this, editTextConfirmPassword)
                    && Util.isValidPassword(SingUpActivity.this, editTextConfirmPassword)
                    && Util.isConfirmEditText(SingUpActivity.this, editTextPassword, editTextConfirmPassword)) {
                Util.showAlertDialog(SingUpActivity.this, getString(R.string.please_wait_login));
                if (NetworkUtil.getConnectivityStatus(SingUpActivity.this)) {
                    FirebaseDatabase.getInstance().getReference().child(Constant.USERS).child(Util.getDeviceId(this)).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Util.showAlertDialog(SingUpActivity.this, getString(R.string.while_preparing));

                                User user = new User(editTextEmail.getText().toString(), editTextUserName.getText().toString(), Constant.COIN_NUMBER, Constant.LIKE_STATUS, Util.getDeviceId(SingUpActivity.this));
                                FirebaseDatabase.getInstance().getReference().child(Constant.USERS).
                                        child(Util.getDeviceId(SingUpActivity.this)).setValue(user).addOnSuccessListener(aVoid -> {
                                    FirebaseStorage.getInstance().getReference().child("Wallpapers").child(Util.getDeviceId(SingUpActivity.this))
                                            .putBytes(Util.wallpaperToByte(SingUpActivity.this)).addOnFailureListener(e -> {
                                        Util.hideAlertDialog();
                                        Toast.makeText(SingUpActivity.this, R.string.cant_upload, Toast.LENGTH_SHORT).show();
                                    }).addOnSuccessListener(taskSnapshot -> {
                                        FirebaseStorage.getInstance().getReference().child("Wallpapers").child(Util.getDeviceId(SingUpActivity.this)).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {

                                                Wallpaper wallpaper = new Wallpaper("0", "0", "0", uri.toString());
                                                FirebaseDatabase.getInstance().getReference().child(Constant.WALLPAPER_INFO).
                                                        child(Util.getDeviceId(SingUpActivity.this)).setValue(wallpaper).addOnSuccessListener(aVoid1 ->
                                                {
                                                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(editTextEmail.getText().toString(),
                                                            editTextPassword.getText().toString()).addOnCompleteListener(authResult -> {
                                                        if (authResult.isSuccessful()) {
                                                            Intent intent = new Intent(SingUpActivity.this, MainActivity.class);
                                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                            startActivity(intent);
                                                            finish();
                                                        } else {
                                                            Util.hideAlertDialog();
                                                            try {
                                                                throw authResult.getException();
                                                            } catch (FirebaseAuthInvalidUserException e) {
                                                                Toast.makeText(SingUpActivity.this, getString(R.string.invalid_email), Toast.LENGTH_SHORT).show();
                                                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                                                Toast.makeText(SingUpActivity.this, getString(R.string.check_pass), Toast.LENGTH_SHORT).show();
                                                            } catch (FirebaseNetworkException e) {
                                                                Util.showAlertDialog(SingUpActivity.this, getString(R.string.check_pass));
                                                            } catch (FirebaseAuthUserCollisionException e) {
                                                                Toast.makeText(SingUpActivity.this, getString(R.string.another_email), Toast.LENGTH_SHORT).show();
                                                            } catch (Exception e) {
                                                                Toast.makeText(SingUpActivity.this, getString(R.string.didnot_login), Toast.LENGTH_SHORT).show();
                                                            }

                                                        }

                                                    });
                                                }).addOnFailureListener(e -> Util.hideAlertDialog());
                                            }
                                        }).addOnFailureListener(e -> Util.hideAlertDialog());
                                    }).addOnFailureListener(e -> Util.hideAlertDialog());
                                }).addOnFailureListener(e -> Util.hideAlertDialog());


                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Util.hideAlertDialog();
                        }
                    });
                } else {
                    Util.hideAlertDialog();
                }
            } else {
                Util.hideAlertDialog();
            }
        });
    }
}
