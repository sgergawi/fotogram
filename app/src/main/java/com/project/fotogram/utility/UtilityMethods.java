package com.project.fotogram.utility;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.android.volley.VolleyError;
import com.project.fotogram.dialogs.MyDialog;
import com.project.fotogram.dialogs.MyPermissionsDialog;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UtilityMethods {

    public static void manageCommunicationError(FragmentActivity activity, VolleyError error) {
        Integer statusCode = error.networkResponse != null ? error.networkResponse.statusCode : null;
        String errorMsg = error.networkResponse != null ? new String(error.networkResponse.data) : null;
        String displayMsg = getRelativeMessage(errorMsg);
        Log.d("fotogramLogs", "Error occured: " + statusCode + ", " + errorMsg);

        if (statusCode != null) {
            MyDialog alert = new MyDialog();
            alert.setMsg(displayMsg);
            alert.show(activity.getSupportFragmentManager(), "MyDialog");
        } else {
            Log.d("fotogramLogs", "Status code null!");
        }

    }

    public static String getRelativeMessage(String errorMsg) {
        return (errorMsg != null && !errorMsg.trim().equals("")) ? Constants.ERROR_CONVERSIONS.get(errorMsg) : "Unexpected error";
    }

    public static void checkPermissions(FragmentActivity activity, int requestCode) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            MyPermissionsDialog permissionsDialog = new MyPermissionsDialog();
            permissionsDialog.configurePermissionsDialog("To import a new photo we need the external storage permissions.", activity, requestCode);
            permissionsDialog.show(activity.getSupportFragmentManager(), "MyDialog");

        } else {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    requestCode);
        }

    }

    public static byte[] resizePhoto(int maxFileSize, Bitmap image) {
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 50, byteArray);
        float scaleFactor = 0.95f;
        if (byteArray.size() > maxFileSize) {
            while (byteArray.size() > maxFileSize) {
                image = Bitmap.createScaledBitmap(image, (int) (image.getWidth() * scaleFactor), (int) (image.getHeight() * scaleFactor), false);
                byteArray.reset();
                image.compress(Bitmap.CompressFormat.JPEG, 50, byteArray);
            }
        }
        return byteArray.toByteArray();
    }

    public static byte[] resizePhoto(Bitmap image) {
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        image = Bitmap.createScaledBitmap(image, 200, 200, false);
        byteArray.reset();
        image.compress(Bitmap.CompressFormat.JPEG, 100, byteArray);
        return byteArray.toByteArray();
    }

    public static String formatDate(String date) {
        String fromServerPattern = "yyyy-MM-dd HH:mm:ss.SSS";
        Date parsed = null;
        String parsedString = "";
        try {
            parsed = new SimpleDateFormat(fromServerPattern).parse(date);
            String outputPattern = "dd-MM-yyyy HH:mm:ss";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(outputPattern);
            parsedString = simpleDateFormat.format(parsed);

        } catch (Exception e) {
        }
        return parsedString;
    }

    public static Bitmap getRoundedBitmapToDisplay(Bitmap profilePhotoBitmap, int width, int height) {
        Bitmap rounded = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(rounded);

        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, profilePhotoBitmap.getWidth(), profilePhotoBitmap.getHeight());
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawCircle(profilePhotoBitmap.getWidth() / 2, profilePhotoBitmap.getHeight() / 2, profilePhotoBitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(profilePhotoBitmap, rect, rect, paint);
        return rounded;
    }
}
