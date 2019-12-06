package com.killerwhale.memary.ARComponent.Utils;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.killerwhale.memary.ARComponent.Model.Stroke;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;


/**
 * Helper class for AR object storage
 * @author Boyang Zhou
 * @author Zeyu Fu
 */
public class StrokeStorageHelper {

    private Context context;

    public StrokeStorageHelper() { }

    public StrokeStorageHelper(Context aContext, List<Stroke> strokes) {
        context = aContext;
    }

    public void serializeStorkes(List<Stroke> mStrokes) {
        try {
            FileOutputStream fileOutputStream = this.context.openFileOutput("strokeFile.ser", context.MODE_PRIVATE);
            ObjectOutputStream out = new ObjectOutputStream(fileOutputStream);
            out.writeObject(mStrokes);
            out.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Helper method to deserialize Stroke objects
     * @return The fetched object
     */
    public List<Stroke> fetchStrokes() {
        try {
            FileInputStream fileInputStream = this.context.openFileInput("strokeFile.ser");
            ObjectInputStream in = new ObjectInputStream(fileInputStream);
            List<Stroke> fetchStrokes = (ArrayList<Stroke>) in.readObject();
            in.close();
            return fetchStrokes;
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Helper method to evaluate whether the restore object is the same as the original one.
     * @param newStrokes restore stroke list
     * @param oldStrokes original stroke list
     */
    public void compareStrokes(List<Stroke> newStrokes, List<Stroke> oldStrokes) {
        Stroke newStroke;
        Stroke oldStroke;
        boolean flag = true;

        for(int i = 0; i < oldStrokes.size(); i++){
            newStroke = newStrokes.get(i);
            oldStroke = oldStrokes.get(i);
            for(int j = 0; j < oldStroke.getPoints().size(); j++){
                //here we compare all the coordinate of the points that construct a stroke.
                boolean equal = (oldStroke.getPoints().get(j).getX() == newStroke.getPoints().get(j).getX()) &&
                        (oldStroke.getPoints().get(j).getY() == newStroke.getPoints().get(j).getY()) &&
                        (oldStroke.getPoints().get(j).getZ() == newStroke.getPoints().get(j).getZ());
                flag = flag && equal;
            }
        }
        Log.i("test equal", "equals result: " + flag);
        Log.i("test equal", "Strokes count: " + oldStrokes.size());
        Log.i("test equal", String.valueOf(newStrokes.get(0).getPoints().get(0).getX() == oldStrokes.get(0).getPoints().get(0).getX()));
    }

    public void uploadStrokeFile(Uri uri) {
        StorageReference strokeRef = FirebaseStorage.getInstance().getReference().child("strokes");
        final StorageReference mStrokeRef = strokeRef.child("strokeFile.ser");
        UploadTask uploadTask = mStrokeRef.putFile(uri);
        Task<Uri> task = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw Objects.requireNonNull(task.getException());
                }
                // Continue with the task to get the download URL
                return mStrokeRef.getDownloadUrl();
            }
        });
        task.addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Log.i("strokestring", "Url: " + task.getResult().toString());
                } else {
                    Log.i("strokestring", "Store failed");
                }
            }
        });
    }
}
