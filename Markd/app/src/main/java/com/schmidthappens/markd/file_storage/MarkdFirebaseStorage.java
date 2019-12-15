package com.schmidthappens.markd.file_storage;

import android.app.Activity;
import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.schmidthappens.markd.utilities.StringUtilities;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * Created by joshua.schmidtibm.com on 12/19/17.
 */

public class MarkdFirebaseStorage {
    private static final String TAG = "MarkdFirebaseStorage";
    private static FirebaseStorage storage = FirebaseStorage.getInstance();

    public static void getFileType(final String path, final StorageMetadataListener listener) {
        if(listener != null) {
            listener.onStart();
        }
        final StorageReference storageReference = storage.getReference().child("images/" + path);
        storageReference.getMetadata().addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if(listener != null) {
                    listener.onFailed(e);
                }
            }
        }).addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                listener.onSuccess(storageMetadata);
            }
        });
    }

    public static void saveImage(String path, Uri file, ContentResolver resolver, final ImageLoadingListener listener) {
        if(StringUtilities.isNullOrEmpty(path)) {
            return;
        }
        if(listener != null) {
            listener.onStart();
        }

        final byte[] compressedBytes = compress(file, resolver);

        final StorageReference reference = storage.getReference().child("images/" + path);
        if(compressedBytes == null) {
            reference.putFile(file).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    if(listener != null) {
                        listener.onSuccess();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    if(listener != null) {
                        listener.onFailed(e);
                    }
                }
            });
        } else {
            reference.putBytes(compressedBytes).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    if (listener != null) {
                        listener.onSuccess();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    if (listener != null) {
                        listener.onFailed(e);
                    }
                }
            });
        }
    }
    public static void loadImage(final Activity context, final String path, final ImageView imageView, final ImageLoadingListener listener) {
        if(listener != null) {
            listener.onStart();
        }
        if(StringUtilities.isNullOrEmpty(path)) {
            Log.d("Storage", "Not loading image");
            if(listener != null) {
                listener.onFailed(new IllegalArgumentException("Path is null or empty"));
            }
            return;
        }

        final StorageReference storageReference = storage.getReference().child("images/" + path);
        storageReference.getMetadata().addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if(listener != null) {
                    listener.onFailed(e);
                }
            }
        }).addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                if (!(storageMetadata.getContentType().equals("image/jpeg") || storageMetadata.getContentType().equals("image/png"))) {
                    if (listener != null) {
                        Log.e(TAG, "Wrong Content Type:" + storageMetadata.getContentType());
                        listener.onFailed(new IllegalStateException("Invalid Content Type"));
                    }
                } else {
                    // Load the image using Glide
                    if (!context.isDestroyed()) {
                        Log.d(TAG, storageReference.toString());
                        Glide.with(context)
                                .using(new FirebaseImageLoader())
                                .load(storageReference)
                                .into(imageView);
                        if (listener != null) {
                            listener.onSuccess();
                            Log.d(TAG, "Success Listener");
                        }
                    }
                }
            }
        });
    }
    public static void updateImage(
            final Activity context,
            final String path,
            final Uri file,
            final ImageView imageView,
            final ImageLoadingListener listener) {
        Log.d(TAG, path);
        if(listener != null) {
            Log.d(TAG, "Started listener");
            listener.onStart();
        }
        final UploadTask uploadTask = saveImage(path, file, context.getContentResolver());
        if(uploadTask != null) {
            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    loadImage(context, path, imageView, listener);
                }
            });
        }
    }

    public static void getFile(String path, File localFile, OnSuccessListener<FileDownloadTask.TaskSnapshot> onSuccess, OnFailureListener onFailure) {
        StorageReference storageReference = storage.getReference().child("images/" + path);
        storageReference.getFile(localFile).addOnSuccessListener(onSuccess).addOnFailureListener(onFailure);
    }

    private static UploadTask saveImage(String path, Uri file, ContentResolver resolver) {
        if(StringUtilities.isNullOrEmpty(path)) {
            return null;
        }

        final byte[] compressedBytes = compress(file, resolver);
        final StorageReference reference = storage.getReference().child("images/" + path);
        if(compressedBytes == null) {
            return reference.putFile(file);
        }
        return reference.putBytes(compressedBytes);
    }
    private static byte[] compress(Uri file, ContentResolver resolver) {
        try {
            Bitmap original = MediaStore.Images.Media.getBitmap(resolver, file);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            original.compress(Bitmap.CompressFormat.JPEG, 50, out);
            return out.toByteArray();
        } catch (IOException | NullPointerException e) {
            return null;
        }
    }
}
