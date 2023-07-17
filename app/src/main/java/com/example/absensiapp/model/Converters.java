package com.example.absensiapp.model;

import static android.util.Base64.DEFAULT;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import androidx.room.TypeConverter;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class Converters {
    @TypeConverter
    public String bitmapToBase64(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
        byte[] byteArray = stream.toByteArray();
        return Base64.encodeToString(byteArray, DEFAULT);
    }

    @TypeConverter
    public Bitmap base64ToBitmap(String base64String){
        byte[] byteArray = Base64.decode(base64String, DEFAULT);
        return BitmapFactory.decodeByteArray(byteArray,
                0, byteArray.length);
    }

}
