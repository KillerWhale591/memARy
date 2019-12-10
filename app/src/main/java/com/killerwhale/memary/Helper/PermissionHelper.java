package com.killerwhale.memary.Helper;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

/**
 * Helper class for checking permissions
 * @author Zeyu Fu
 */
public class PermissionHelper {

    public static final int PERMISSION_CODE_AR = 1002;
    public static final int PERMISSION_CODE_POST = 1003;
    public static final int PERMISSION_CODE_SPLASH = 1004;
    public static final int PERMISSION_CODE_PROFILE = 1005;
    public static final String[] PERMISSIONS_AR = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };
    public static final String[] PERMISSIONS_POST = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };
    public static final String[] PERMISSIONS_SPLASH = {
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    public static final String[] PERMISSION_PROFILE = {
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    /**
     * Check permissions
     * @param context activity
     * @param permissions permission group
     * @return has
     */
    public static boolean hasPermissions(Context context, String[] permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Check if all permissions are granted
     * @param grantResults permission results
     * @return has granted
     */
    public static boolean hasGrantedAll(int[] grantResults) {
        if (grantResults.length > 0) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        } else {
            return false;
        }
        return true;
    }
}
