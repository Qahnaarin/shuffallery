package com.jojastream.shuffallery.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.jojastream.shuffallery.R;
import com.jojastream.shuffallery.util.Constant;
import com.jojastream.shuffallery.util.NetworkUtil;
import com.jojastream.shuffallery.util.Util;
import com.yarolegovich.slidingrootnav.SlidingRootNav;
import com.yarolegovich.slidingrootnav.SlidingRootNavBuilder;

import java.util.Iterator;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.jojastream.shuffallery.util.Constant.PERMISSIONS;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.imageButtonMenu)
    ImageButton imageButtonMenu;

    @BindView(R.id.buttonShuffale)
    Button buttonShuffale;
    @BindView(R.id.textViewCoin)
    TextView textViewCoin;
    @BindView(R.id.imageViewMain)
    ImageView imageViewMain;
    @BindView(R.id.textViewDislikeWallpaper)
    TextView textViewDislikeWallpaper;
    @BindView(R.id.textViewLikeWallpaper)
    TextView textViewLikeWallpaper;
    @BindView(R.id.textViewUsedWallpaper)
    TextView textViewUsedWallpaper;
    @BindView(R.id.lottieLike)
    LottieAnimationView lottieLike;
    @BindView(R.id.lottieDislike)
    LottieAnimationView lottieDislike;
    @BindView(R.id.relativeLayoutMain)
    RelativeLayout relativeLayoutMain;
    @BindView(R.id.textViewPermission)
    TextView textViewPermission;
    @BindView(R.id.imageViewAdd)
    ImageView imageViewAdd;

    private SlidingRootNav slidingRootNav;
    private TextView textViewDislike;
    private TextView textViewUsed;
    private TextView textViewLike;
    private TextView textViewUserName;
    private TextView textViewEmail;
    private TextView textViewLogOut;
    private SweetAlertDialog mDialog;
    private ImageView imageViewMostLiked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);


        relativeLayoutMain.setVisibility(View.GONE);
        textViewPermission.setVisibility(View.VISIBLE);

        slidingRootNav = new SlidingRootNavBuilder(this)
                .withMenuOpened(false)
                .withContentClickableWhenMenuOpened(false)
                .withSavedState(savedInstanceState)
                .withMenuLayout(R.layout.menu_left)
                .inject();

        textViewUsed = findViewById(R.id.textViewUsed);
        textViewDislike = findViewById(R.id.textViewDislike);
        textViewLike = findViewById(R.id.textViewLike);
        textViewLogOut = findViewById(R.id.textViewLogOut);
        textViewEmail = findViewById(R.id.textViewEmail);
        textViewUserName = findViewById(R.id.textViewUserName);
        imageViewMostLiked = findViewById(R.id.imageViewMostLiked);
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
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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


    private void initialize() {
        textViewPermission.setVisibility(View.GONE);
        relativeLayoutMain.setVisibility(View.VISIBLE);
        if (Util.getPreference(this, Constant.WALLPAPER_PREF) == null) {
            Util.showAlertDialog(this, getString(R.string.while_preparing));
        }
        setMostLiked();
        setCoinNumber();
        setLikeStatus();
        setClickListeners();
    }

    private void setMostLiked() {
        DatabaseReference mDatabasePlayers = FirebaseDatabase.getInstance().getReference().child(Constant.WALLPAPER_INFO);
        Query mDatabaseHighest = mDatabasePlayers.orderByChild("likeCount").limitToLast(1);
        mDatabaseHighest.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    String Key = childSnapshot.getKey();
                    try {
                        Glide.with(MainActivity.this).load(childSnapshot.child("baseURL").getValue().toString()).into(imageViewMostLiked);
                    } catch (Exception e) {
                        Log.i("exception", e.toString());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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


    private void setCoinNumber() {
        if (NetworkUtil.getConnectivityStatus(MainActivity.this)) {
            FirebaseDatabase.getInstance().getReference().child(Constant.USERS).child(Util.getDeviceId(MainActivity.this)).child("coinNumber").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Util.savePreference(MainActivity.this, Constant.COIN_COUNT_PREF, dataSnapshot.getValue().toString());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Util.hideAlertDialog();
                }
            });
        } else {
            Util.showAlertDialogError(MainActivity.this, getString(R.string.check_connection), getString(R.string.no_internet));
        }
    }

    private void setLikeStatus() {
        String likeStatus = Util.getPreference(MainActivity.this, Constant.USER_LIKE_STATUS);
        if (likeStatus == null) {
            if (NetworkUtil.getConnectivityStatus(MainActivity.this)) {
                FirebaseDatabase.getInstance().getReference().child(Constant.USERS).child(Util.getDeviceId(this)).child("likeStatus").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Util.savePreference(MainActivity.this, Constant.USER_LIKE_STATUS, String.valueOf(dataSnapshot.getValue()));
                        Util.savePreference(MainActivity.this, Constant.PREV_LIKE_PREF, String.valueOf(dataSnapshot.getValue()));
                        setScreen();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Util.hideAlertDialog();
                    }
                });
            } else {
                Util.showAlertDialogError(MainActivity.this, getString(R.string.check_connection), getString(R.string.no_internet));
            }
        } else {
            try {
                setScreen();
            } catch (Exception e) {
                Util.hideAlertDialog();
            }
        }
    }

    private void setPreference(String userName, String email, String likeCount, String disLikeCount, String usedCount, String likeWallpaper, String dislikeWallpaper, String usedWallpaper,
                               String coinNumber) {
        try {
            Util.savePreference(this, Constant.USER_NAME_PREF, userName);
            Util.savePreference(this, Constant.USER_EMAIL_PREF, email);
            Util.savePreference(this, Constant.LIKE_COUNT_PREF, likeCount);
            Util.savePreference(this, Constant.DISLIKE_COUNT_PREF, disLikeCount);
            Util.savePreference(this, Constant.USED_COUNT_PREF, usedCount);
            Util.savePreference(this, Constant.LIKE_WALLPAPER_PREF, likeWallpaper);
            Util.savePreference(this, Constant.DISLIKE_WALLPAPER_PREF, dislikeWallpaper);
            Util.savePreference(this, Constant.USED_WALLPAPER_PREF, usedWallpaper);
            Util.savePreference(this, Constant.COIN_COUNT_PREF, coinNumber);
        } catch (Exception e) {
        }
    }

    private void setInfo() {
        try {
            textViewUserName.setText(Util.getPreference(MainActivity.this, Constant.USER_NAME_PREF));
            textViewEmail.setText(Util.getPreference(MainActivity.this, Constant.USER_EMAIL_PREF));
            textViewLike.setText(Util.getPreference(MainActivity.this, Constant.LIKE_COUNT_PREF));
            textViewDislike.setText(Util.getPreference(MainActivity.this, Constant.DISLIKE_COUNT_PREF));
            textViewCoin.setText(Util.getPreference(MainActivity.this, Constant.COIN_COUNT_PREF));
            textViewUsed.setText(Util.getPreference(MainActivity.this, Constant.USED_COUNT_PREF));
            textViewUsedWallpaper.setText(Util.getPreference(MainActivity.this, Constant.USED_WALLPAPER_PREF));
            textViewDislikeWallpaper.setText(Util.getPreference(MainActivity.this, Constant.DISLIKE_WALLPAPER_PREF));
            textViewLikeWallpaper.setText(Util.getPreference(MainActivity.this, Constant.LIKE_WALLPAPER_PREF));
            Util.hideAlertDialog();
        } catch (Exception e) {
            Util.hideAlertDialog();
        }

    }

    private void getWallpaperAndInfo() {
        check = true;
        FirebaseDatabase.getInstance().getReference().child(Constant.USERS).child(Util.getDeviceId(this)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String coinNumber = dataSnapshot.child("coinNumber").getValue().toString();
                FirebaseDatabase.getInstance().getReference().child(Constant.WALLPAPER_INFO).child(dataSnapshot.child("wallpaper").getValue().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot wallpaper) {
                        FirebaseDatabase.getInstance().getReference().child(Constant.WALLPAPER_INFO).child(Util.getDeviceId(MainActivity.this)).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {
                                runOnUiThread(() -> Util.showAlertDialog(MainActivity.this, getString(R.string.while_preparing_time)));

                                Util.setWallpaper(wallpaper.child("baseURL").getValue().toString(), MainActivity.this);
                                Util.setView(MainActivity.this, wallpaper.child("baseURL").getValue().toString(), imageViewMain);
                                Util.savePreference(MainActivity.this, Constant.WALLPAPER_PREF, wallpaper.child("baseURL").getValue().toString());
                                String likeWallpaper = (String) wallpaper.child("likeCount").getValue();
                                String dislikeWallpaper = (String) wallpaper.child("dislikeCount").getValue();
                                String usedWallpaper = (String) wallpaper.child("usedCount").getValue();
                                String like = dataSnapshot1.child("likeCount").getValue().toString();
                                String dislike = dataSnapshot1.child("dislikeCount").getValue().toString();
                                String used = dataSnapshot1.child("usedCount").getValue().toString();
                                setPreference(dataSnapshot.child("userName").getValue().toString(), dataSnapshot.child("emailAddress").getValue().toString(),
                                        like, dislike, used, likeWallpaper, dislikeWallpaper, usedWallpaper, coinNumber);
                                try {
                                    textViewUserName.setText(Util.getPreference(MainActivity.this, Constant.USER_NAME_PREF));
                                    textViewEmail.setText(Util.getPreference(MainActivity.this, Constant.USER_EMAIL_PREF));
                                    textViewLike.setText(Util.getPreference(MainActivity.this, Constant.LIKE_COUNT_PREF));
                                    textViewDislike.setText(Util.getPreference(MainActivity.this, Constant.DISLIKE_COUNT_PREF));
                                    textViewCoin.setText(Util.getPreference(MainActivity.this, Constant.COIN_COUNT_PREF));
                                    textViewUsed.setText(Util.getPreference(MainActivity.this, Constant.USED_COUNT_PREF));
                                    textViewUsedWallpaper.setText(Util.getPreference(MainActivity.this, Constant.USED_WALLPAPER_PREF));
                                    textViewDislikeWallpaper.setText(Util.getPreference(MainActivity.this, Constant.DISLIKE_WALLPAPER_PREF));
                                    textViewLikeWallpaper.setText(Util.getPreference(MainActivity.this, Constant.LIKE_WALLPAPER_PREF));
                                } catch (Exception e) {
                                }


                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Util.hideAlertDialog();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Util.hideAlertDialog();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Util.hideAlertDialog();
            }
        });


    }


    private void setScreen() {
        if (Util.getPreference(MainActivity.this, Constant.WALLPAPER_PREF) == null) {
            if (NetworkUtil.getConnectivityStatus(MainActivity.this)) {
                getWallpaperAndInfo();
            } else {
                Util.showAlertDialogError(MainActivity.this, getString(R.string.check_connection), getString(R.string.no_internet));
            }
        } else {
            try {
                Util.setView(MainActivity.this, Util.getPreference(MainActivity.this, Constant.WALLPAPER_PREF), imageViewMain);
            } catch (Exception e) {
                Util.hideAlertDialog();
            } finally {
                setInfo();
            }
        }
    }

    private void setWallpaperInfo() {
        if (!Util.getPreference(MainActivity.this, Constant.PREV_LIKE_PREF).equals(Util.getPreference(MainActivity.this, Constant.USER_LIKE_STATUS))
                && NetworkUtil.getConnectivityStatus(MainActivity.this)) {
            FirebaseDatabase.getInstance().getReference().child(Constant.USERS).child(Util.getDeviceId(MainActivity.this)).child("wallpaper").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String wall = dataSnapshot.getValue().toString();
                    FirebaseDatabase.getInstance().getReference().child(Constant.WALLPAPER_INFO).child(wall).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String dislike = dataSnapshot.child("dislikeCount").getValue().toString();
                            String like = dataSnapshot.child("likeCount").getValue().toString();
                            if (Util.getPreference(MainActivity.this, Constant.PREV_LIKE_PREF).equals("DEFAULT")) {
                                if (Util.getPreference(MainActivity.this, Constant.USER_LIKE_STATUS).equals("LIKE")) {
                                    like = String.valueOf(Integer.parseInt(like) + 1);
                                } else if ((Util.getPreference(MainActivity.this, Constant.USER_LIKE_STATUS).equals("DISLIKE"))) {
                                    dislike = String.valueOf(Integer.parseInt(dislike) + 1);
                                }
                            } else if (Util.getPreference(MainActivity.this, Constant.PREV_LIKE_PREF).equals("LIKE")) {
                                dislike = String.valueOf(Integer.parseInt(dislike) + 1);
                                like = String.valueOf(Integer.parseInt(like) - 1);
                            } else if (Util.getPreference(MainActivity.this, Constant.PREV_LIKE_PREF).equals("DISLIKE")) {
                                dislike = String.valueOf(Integer.parseInt(dislike) - 1);
                                like = String.valueOf(Integer.parseInt(like) + 1);
                            }

                            String finalLike = like;
                            FirebaseDatabase.getInstance().getReference().child(Constant.WALLPAPER_INFO).child(wall).child("dislikeCount").setValue(dislike)
                                    .addOnSuccessListener(task -> FirebaseDatabase.getInstance().getReference().child(Constant.WALLPAPER_INFO).child(wall).child("likeCount").setValue(finalLike).addOnSuccessListener(task1 -> resetInfo()));


                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });

        }
    }

    private void resetInfo() {
        FirebaseDatabase.getInstance().getReference().child(Constant.USERS).child(Util.getDeviceId(MainActivity.this)).child("likeStatus").setValue("DEFAULT").
                addOnSuccessListener(task -> {
                    Util.removeAllPreference(MainActivity.this);
                    Util.savePreference(MainActivity.this, Constant.USER_LIKE_STATUS, "DEFAULT");
                    Util.savePreference(MainActivity.this, Constant.PREV_LIKE_PREF, "DEFAULT");
                }).addOnSuccessListener(task -> {
        });
    }

    private boolean check = true;

    private void setClickListeners() {

        buttonShuffale.setOnClickListener(v -> {
            if (check) {
                if (NetworkUtil.getConnectivityStatus(MainActivity.this)) {
                    check = false;
                    Util.showAlertDialog(this, getString(R.string.while_preparing));
                    final int[] coinNumber = {0};
                    FirebaseDatabase.getInstance().getReference().child(Constant.USERS).child(Util.getDeviceId(MainActivity.this)).child("coinNumber").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            coinNumber[0] = Integer.valueOf(dataSnapshot.getValue().toString());
                            if (coinNumber[0] > 0) {
                                setWallpaperInfo();
                                FirebaseDatabase.getInstance().getReference().child(Constant.WALLPAPER_INFO).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        int childNumber = Util.generateRandomInt((int) dataSnapshot.getChildrenCount());
                                        Iterator itr = dataSnapshot.getChildren().iterator();
                                        for (int i = 0; i < childNumber - 1; i++) {
                                            itr.next();
                                        }
                                        DataSnapshot childSnapShot = (DataSnapshot) itr.next();
                                        try {
                                            FirebaseDatabase.getInstance().getReference().child(Constant.USERS).child(Util.getDeviceId(MainActivity.this)).child("wallpaper").setValue(childSnapShot.getKey()).addOnFailureListener(a -> check = true)
                                                    .addOnSuccessListener(task -> FirebaseDatabase.getInstance()
                                                            .getReference().child(Constant.USERS).child(Util.getDeviceId(MainActivity.this)).child("coinNumber").setValue(coinNumber[0] - 1)
                                                            .addOnFailureListener(e -> {
                                                                check = true;
                                                                Toast.makeText(MainActivity.this, R.string.retry, Toast.LENGTH_SHORT).show();
                                                            }).addOnSuccessListener(aVoid -> getWallpaperAndInfo())).addOnFailureListener(a -> check = true).addOnSuccessListener(task ->
                                                    FirebaseDatabase.getInstance().getReference().child(Constant.WALLPAPER_INFO).child(childSnapShot.getKey()).child("usedCount").addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {
                                                            String val = dataSnapshot1.getValue().toString();
                                                            FirebaseDatabase.getInstance().getReference().child(Constant.WALLPAPER_INFO).child(childSnapShot.getKey()).child("usedCount").setValue(String.valueOf(Integer.valueOf(val) + 1));
                                                            check = true;
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                                            Util.hideAlertDialog();
                                                            check = true;
                                                        }
                                                    }));
                                        } catch (Exception e) {
                                            Util.hideAlertDialog();
                                            Toast.makeText(MainActivity.this, R.string.prepare_wallpaper, Toast.LENGTH_SHORT).show();
                                            check = true;
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        Util.hideAlertDialog();
                                        check = true;

                                    }
                                });
                            } else if (coinNumber[0] == 0) {
                                Util.hideAlertDialog();
                                check = true;
                                mDialog = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.WARNING_TYPE)
                                        .setTitleText(getString(R.string.coin_number))
                                        .setContentText(getString(R.string.watch_add))
                                        .setCancelText(getString(R.string.cancel))
                                        .setConfirmText(getString(R.string.watch))
                                        .showCancelButton(true)
                                        .setCancelClickListener(sDialog -> sDialog.cancel())
                                        .setConfirmClickListener(sweetAlertDialog -> {

                                        });
                                mDialog.show();
                            } else {
                                check = true;
                                FirebaseDatabase.getInstance().getReference().child(Constant.USERS).child(Util.getDeviceId(MainActivity.this)).child("coinNumber").setValue(0);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            check = true;
                            Util.showAlertDialogError(MainActivity.this, getString(R.string.check_connection), getString(R.string.no_internet));
                        }
                    });
                } else {
                    check = true;
                    Util.showAlertDialogError(MainActivity.this, getString(R.string.check_connection), getString(R.string.no_internet));
                }
            }
        });

        lottieDislike.setOnClickListener(v -> {
            if (!Util.getPreference(MainActivity.this, Constant.USER_LIKE_STATUS).equals(Constant.DISLIKE)) {
                if (Util.getPreference(MainActivity.this, Constant.USER_LIKE_STATUS).equals(Constant.LIKE)) {
                    Util.savePreference(MainActivity.this, Constant.LIKE_WALLPAPER_PREF, String.valueOf(Integer.valueOf(Util.getPreference(MainActivity.this, Constant.LIKE_WALLPAPER_PREF)) - 1));
                    Util.savePreference(MainActivity.this, Constant.DISLIKE_WALLPAPER_PREF, String.valueOf(Integer.valueOf(Util.getPreference(MainActivity.this, Constant.DISLIKE_WALLPAPER_PREF)) + 1));

                    lottieDislike.playAnimation();
                    lottieLike.playAnimation();
                } else {
                    Util.savePreference(MainActivity.this, Constant.DISLIKE_WALLPAPER_PREF, String.valueOf(Integer.valueOf(Util.getPreference(MainActivity.this, Constant.DISLIKE_WALLPAPER_PREF)) + 1));
                    lottieDislike.playAnimation();
                }
                Util.savePreference(MainActivity.this, Constant.USER_LIKE_STATUS, Constant.DISLIKE);
            } else {
                Util.savePreference(MainActivity.this, Constant.DISLIKE_WALLPAPER_PREF, String.valueOf(Integer.valueOf(Util.getPreference(MainActivity.this, Constant.DISLIKE_WALLPAPER_PREF)) - 1));
                Util.savePreference(MainActivity.this, Constant.USER_LIKE_STATUS, "DEFAULT");
                lottieDislike.playAnimation();
            }
            setInfo();
        });


        lottieLike.setOnClickListener(v -> {
            if (!Util.getPreference(MainActivity.this, Constant.USER_LIKE_STATUS).equals(Constant.LIKE)) {
                if (Util.getPreference(MainActivity.this, Constant.USER_LIKE_STATUS).equals(Constant.DISLIKE)) {
                    Util.savePreference(MainActivity.this, Constant.DISLIKE_WALLPAPER_PREF, String.valueOf(Integer.valueOf(Util.getPreference(MainActivity.this, Constant.DISLIKE_WALLPAPER_PREF)) - 1));
                    Util.savePreference(MainActivity.this, Constant.LIKE_WALLPAPER_PREF, String.valueOf(Integer.valueOf(Util.getPreference(MainActivity.this, Constant.LIKE_WALLPAPER_PREF)) + 1));
                    lottieDislike.playAnimation();
                    lottieLike.playAnimation();
                } else {
                    Util.savePreference(MainActivity.this, Constant.LIKE_WALLPAPER_PREF, String.valueOf(Integer.valueOf(Util.getPreference(MainActivity.this, Constant.LIKE_WALLPAPER_PREF)) + 1));
                    lottieLike.playAnimation();
                }
                Util.savePreference(MainActivity.this, Constant.USER_LIKE_STATUS, Constant.LIKE);
            } else {
                Util.savePreference(MainActivity.this, Constant.LIKE_WALLPAPER_PREF, String.valueOf(Integer.valueOf(Util.getPreference(MainActivity.this, Constant.LIKE_WALLPAPER_PREF)) - 1));
                Util.savePreference(MainActivity.this, Constant.USER_LIKE_STATUS, "DEFAULT");
                lottieLike.playAnimation();
            }
            setInfo();
        });

        imageButtonMenu.setOnClickListener(v -> slidingRootNav.openMenu());
        textViewLogOut.setOnClickListener(v -> {
            if (LoginActivity.mGoogleSignInClient != null) {
                LoginActivity.mGoogleSignInClient.signOut();
            }
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();

        });

        imageViewAdd.setOnClickListener(v -> {
            Util.hideAlertDialog();
            mDialog = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Heyooo..")
                    .setContentText(getString(R.string.watch_add))
                    .setCancelText(getString(R.string.cancel))
                    .setConfirmText(getString(R.string.watch))
                    .showCancelButton(true)
                    .setCancelClickListener(sDialog -> sDialog.cancel())
                    .setConfirmClickListener(sweetAlertDialog -> {

                    });
            mDialog.show();
        });
    }

    @Override
    public void onBackPressed() {
        Util.hideAlertDialog();
        if (mDialog != null) {
            mDialog.dismiss();
        }
        mDialog = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.WARNING_TYPE)
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
}