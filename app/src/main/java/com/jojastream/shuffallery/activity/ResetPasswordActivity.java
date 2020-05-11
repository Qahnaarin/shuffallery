package com.jojastream.shuffallery.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jojastream.shuffallery.R;
import com.jojastream.shuffallery.util.Constant;
import com.jojastream.shuffallery.util.NetworkUtil;
import com.jojastream.shuffallery.util.Util;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.jojastream.shuffallery.util.Constant.PERMISSIONS;

public class ResetPasswordActivity extends AppCompatActivity {

    @BindView(R.id.imageViewClose)
    ImageView imageViewClose;
    @BindView(R.id.editTextEmailAddress)
    EditText editTextEmailAddress;
    @BindView(R.id.buttonResetPassword)
    Button buttonResetPassword;
    @BindView(R.id.textViewSingUp)
    TextView textViewSingUp;
    @BindView(R.id.linearLayoutMain)
    LinearLayout linearLayoutMain;
    @BindView(R.id.textViewPermission)
    TextView textViewPermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_reset_password);
        ButterKnife.bind(this);
        linearLayoutMain.setVisibility(View.GONE);
        textViewPermission.setVisibility(View.VISIBLE);

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
        imageViewClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ResetPasswordActivity.this.finish();
            }
        });

        textViewSingUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ResetPasswordActivity.this, SingUpActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                ResetPasswordActivity.this.startActivity(intent);
                ResetPasswordActivity.this.finish();
            }
        });

        buttonResetPassword.setOnClickListener(v -> {
            if (NetworkUtil.getConnectivityStatus(ResetPasswordActivity.this)
                    && Util.isEditTextEmpty(ResetPasswordActivity.this, editTextEmailAddress)
                    && Util.isValidEmail(ResetPasswordActivity.this, editTextEmailAddress)) {
                Util.showAlertDialog(this, getString(R.string.please_wait_login));
                FirebaseDatabase.getInstance().getReference().child(Constant.USERS).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child(Util.getDeviceId(ResetPasswordActivity.this)).exists()) {
                            FirebaseAuth.getInstance().sendPasswordResetEmail(editTextEmailAddress.getText().toString())
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(ResetPasswordActivity.this, R.string.check_in_email, Toast.LENGTH_SHORT).show();
                                            editTextEmailAddress.setText("");
                                        } else {
                                            Toast.makeText(ResetPasswordActivity.this, R.string.not_reset_password, Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(e -> {
                                Util.hideAlertDialog();
                                Toast.makeText(ResetPasswordActivity.this, R.string.not_reset_password, Toast.LENGTH_SHORT).show();

                            });
                        } else {
                            Util.hideAlertDialog();
                            Toast.makeText(ResetPasswordActivity.this, R.string.not_reset_password, Toast.LENGTH_SHORT).show();

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Util.hideAlertDialog();
                        Toast.makeText(ResetPasswordActivity.this, R.string.not_reset_password, Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

    }
}
