package com.jojastream.shuffallery.util;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.provider.Settings;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.jojastream.shuffallery.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class Util {
    private static SweetAlertDialog pDialog;

    public static boolean isValidEmail(Context context, EditText editText) {
        if (!Patterns.EMAIL_ADDRESS.matcher(editText.getText().toString()).matches()) {
            Toast.makeText(context, R.string.please_enter_valid_email, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public static boolean isEditTextEmpty(Context context, EditText editText) {
        if (editText.getText().length() == 0) {
            Toast.makeText(context, context.getString(R.string.please_check) + " " + editText.getHint() + " " + context.getString(R.string.check_in), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public static boolean isConfirmEditText(Context context, EditText editText, EditText editTextConfirm) {
        if (!editText.getText().toString().equals(editTextConfirm.getText().toString())) {
            Toast.makeText(context, editText.getHint() + " " + context.getString(R.string.and) + " " + editTextConfirm.getHint() + " " + context.getString(R.string.doesnt_match), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public static String getDeviceId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID) == null ? getUniquePsuedoID() : Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    private static String getUniquePsuedoID() {

        String m_szDevIDShort = "35" + (Build.BOARD.length() % 10) + (Build.BRAND.length() % 10) + (Build.CPU_ABI.length() % 10) + (Build.DEVICE.length() % 10) + (Build.MANUFACTURER.length() % 10) + (Build.MODEL.length() % 10) + (Build.PRODUCT.length() % 10);
        String serial;
        try {
            serial = android.os.Build.class.getField("SERIAL").get(null).toString();
            return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
        } catch (Exception exception) {
            serial = "serial"; // some value
        }
        return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
    }

    public static byte[] wallpaperToByte(Context context) {
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
        Bitmap bitmap = ((BitmapDrawable) wallpaperManager.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, baos);
        return baos.toByteArray();
    }


    public static boolean isValidPassword(Context context, EditText editText) {
        if (editText.getText().length() < 8) {
            Toast.makeText(context, R.string.password_valid, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public static int generateRandomInt(int max) {
        return (int) (Math.random() * ((max) + 1));
    }

    public static void setView(Context context, String inputUri, ImageView imageView) {
        Glide.with(context)
                .load(inputUri).
                diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView);
    }

    public static void setWallpaper(String inputUri, Context context) {
        WallpaperManager manager = WallpaperManager.getInstance(context);

        Glide.with(context).load(inputUri).into(new CustomTarget<Drawable>() {
            @Override
            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                try {
                    manager.setBitmap(((BitmapDrawable) resource).getBitmap());
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    Util.hideAlertDialog();
                    Toast.makeText(context, context.getString(R.string.wonderful), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {
                Util.hideAlertDialog();
            }
        });
    }


    public static void savePreference(Context context, String key, String value) {
        SharedPreferences prefs = context.getSharedPreferences(
                Constant.PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(key, value).apply();
    }

    public static String getPreference(Context context, String key) {
        SharedPreferences prefs = context.getSharedPreferences(
                Constant.PREF_NAME, Context.MODE_PRIVATE);
        if (key.equals(Constant.COIN_COUNT_PREF)) {
            return prefs.getString(key, "0");
        } else {
            return prefs.getString(key, null);
        }
    }

    public static void removeAllPreference(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                Constant.PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }
    public static void showAlertDialog(Context context, String title) {
        if (pDialog != null) {
            pDialog.dismissWithAnimation();
        }
        pDialog = new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText(title);
        pDialog.setCancelable(false);
        pDialog.show();
    }

    public static void showAlertDialogError(Context context, String title, String header) {
        if (pDialog != null) {
            pDialog.dismissWithAnimation();
        }
        pDialog = new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE);
        pDialog.setCancelable(true);
        pDialog.setTitle(header);
        pDialog.setContentText(title);
        pDialog.show();
    }


    public static void hideAlertDialog() {
        if (pDialog != null) {
            pDialog.dismissWithAnimation();
        }
    }

}
