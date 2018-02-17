package com.spipi.spipimediaplayer;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;


public class PermissionChecker {
    private static final int PERMISSION_REQUEST = 1;
    private static PermissionChecker sPermissionChecker;
    Activity mActivity;

    public boolean isDialogDisplayed = false;
    private PermissionListener mPermissionListener;


    interface PermissionListener{
        void onPermissionResult(boolean granted);
    }

    /**
     * will create checker only when permission isn't granted ?
     * @param activity
     */

    @TargetApi(Build.VERSION_CODES.M)
    public void checkAndRequestPermission(Activity activity, PermissionListener listener) {
        mPermissionListener = listener;
        mActivity = activity;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(mActivity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (!isDialogDisplayed) {
                new AlertDialog.Builder(mActivity).setTitle(R.string.error).setMessage(R.string.error_permission_storage).setPositiveButton(R.string.allow, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mActivity.requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST);
                        isDialogDisplayed = true;
                    }
                }).setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                    }
                })
                        .setNegativeButton(R.string.exit, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                android.os.Process.killProcess(android.os.Process.myPid());
                            }
                        }).setCancelable(false).show();
            }
        }
        else
            mPermissionListener.onPermissionResult(true);

    }



    @TargetApi(Build.VERSION_CODES.M)
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.M)
            return;
        if(isDialogDisplayed&& ContextCompat.checkSelfPermission(mActivity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
            new AlertDialog.Builder(mActivity).setTitle(R.string.error).setMessage(R.string.error_permission_storage).setPositiveButton(R.string.allow, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // finish();
                    isDialogDisplayed = false;
                    Intent in = new Intent();
                    in.setAction("android.intent.action.MANAGE_APP_PERMISSIONS");
                    in.putExtra("android.intent.extra.PACKAGE_NAME", mActivity.getPackageName());
                    try {
                        mActivity.startActivity(in);
                    }
                    catch(SecurityException e){
                        // Create intent to start new activity
                        in.setData(Uri.parse("package:" + mActivity.getPackageName()));
                        in.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        // start new activity to display extended information
                        mActivity.startActivity(in);

                    }
                }
            }).setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    isDialogDisplayed = false;
                }
            })
             .setNegativeButton(R.string.exit, new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialogInterface, int i) {
                     android.os.Process.killProcess(android.os.Process.myPid());
                 }
             }).setCancelable(false).show();


        }
        else {
            mPermissionListener.onPermissionResult(true);
        }
    }


}
