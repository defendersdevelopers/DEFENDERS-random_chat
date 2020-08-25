package com.kyle.random.other.helpers;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kyle.random.R;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


import id.zelory.compressor.Compressor;

import static com.kyle.random.other.Libs.getUserId;
public class FilesUploader {
    public interface OnImageUploadListener {
        public void OnImageUploaded(String file);
        public void OnError();
    }
    public FilesUploader(Context context, Uri file, String folder, String extention, final OnImageUploadListener onImageUploadListener) {
        final ProgressDialog mProgress = new ProgressDialog(context);

        mProgress.setMessage(context.getString(R.string.id_246));
        mProgress.setCancelable(false);
        mProgress.show();
        try {
            String uid = getUserId().isEmpty() ? "ghost" : getUserId();
            final StorageReference path = FirebaseStorage.getInstance().getReference(folder).child(uid).child(System.currentTimeMillis() + extention);
            UploadTask uploadTask;
            if (extention.equals("jpg") || extention.equals("jpeg") || extention.equals("png")) {
                final File thumb_filepath = getFile(context, file, System.currentTimeMillis() + extention);
                Bitmap thumb_bitmap = new Compressor(context).setMaxHeight(512).setMaxWidth(512).setQuality(75).compressToBitmap(thumb_filepath);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] thumb_byte = baos.toByteArray();
                uploadTask = path.putBytes(thumb_byte);
            } else {
                uploadTask = path.putFile(file);
            }
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    path.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri image) {
                            mProgress.dismiss();
                            onImageUploadListener.OnImageUploaded(image.toString());
                        }
                    });

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                    mProgress.dismiss();
                    onImageUploadListener.OnError();
                }
            }).addOnCanceledListener(new OnCanceledListener() {
                @Override
                public void onCanceled() {
                    FirebaseCrashlytics.getInstance().log("Upload canceled");
                    mProgress.dismiss();
                    onImageUploadListener.OnError();
                }
            });
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            mProgress.dismiss();
            onImageUploadListener.OnError();
        }
    }
    public  File getFile(Context context, Uri sourceuri, String name) {
        File file = getTemp(name);
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(sourceuri);
            assert inputStream != null;
            bis = new BufferedInputStream(inputStream);
            bos = new BufferedOutputStream(new FileOutputStream(file, false));
            byte[] buf = new byte[1024];
            bis.read(buf);
            do {
                bos.write(buf);
            } while (bis.read(buf) != -1);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (bis != null) bis.close();
                if (bos != null) bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }
    public  File getTemp(String name) {
        File tempDir = new File(Environment.getExternalStorageDirectory() + File.separator + "temp" + File.separator);
        tempDir.mkdirs();
        File file = new File(tempDir, name + ".png");
        if (file.exists() && !file.delete()) {
            file = new File(tempDir, name + "_" + System.currentTimeMillis() + ".png");
        }
        return file;
    }
}
