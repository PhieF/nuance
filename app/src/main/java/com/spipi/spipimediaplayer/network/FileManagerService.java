package com.spipi.spipimediaplayer.network;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.Toast;

import com.spipi.spipimediaplayer.MainActivity;
import com.spipi.spipimediaplayer.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class FileManagerService extends Service implements OperationEngineListener{

    private static final String TAG = "FileManagerService";
    private ArrayList<FileInfo> mProcessedFiles = null;
    private IBinder localBinder;
    private static final int PASTE_NOTIFICATION_ID = 1;
    private static final int OPEN_NOTIFICATION_ID = 2;
    private static String OPEN_AT_THE_END_KEY= "open_at_the_end_key";
    public static FileManagerService fileManagerService = null;

    private NotificationManager mNotificationManager;
    private Builder mNotificationBuilder;
    private long mPasteTotalSize = 0;
    private int mCurrentFile = 0;
    private BroadcastReceiver receiver;
    private long mLastUpdate = 0;
    private long mLastStatusBarUpdate =0;
    private boolean mOpenAtTheEnd;
    private boolean mHasOpenAtTheEndBeenSet;
    private ActionStatusEnum mLastStatus;
    private HashMap<FileInfo, Long> mProgress;
    private long mPasteTotalProgress;
    private Downloader mCopyCutEngine;
    private ArrayList<ServiceListener> mListeners;
    private boolean mIsActionRunning;
    private Uri mTarget;
    private PowerManager.WakeLock mWakeLock;


    public enum FileActionEnum {
        NONE, COPY, CUT, DELETE, COMPRESSION, EXTRACTION
    };

    public enum ActionStatusEnum {
        PROGRESS, START, STOP, CANCELED, ERROR, NONE
    };

    public interface  ServiceListener {
        void onActionStart();
        void onActionStop();
        void onActionError();
        void onActionCanceled();
        void onProgressUpdate();
    }


    public FileManagerService() {
        super();
        fileManagerService = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mLastStatus = ActionStatusEnum.NONE;
        localBinder = new FileManagerServiceBinder();
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mOpenAtTheEnd = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(OPEN_AT_THE_END_KEY, true);
        mHasOpenAtTheEndBeenSet = false;
        mListeners = new ArrayList<>();
        mProcessedFiles = new ArrayList<>();
        mProgress = new HashMap<>();
        mCopyCutEngine = new Downloader(this);
        mCopyCutEngine.setListener(this);
        mIsActionRunning = false;
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction()!=null&&intent.getAction().equals("CANCEL"))
                    stopPasting();

            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction("CANCEL");
        filter.addAction("OPEN");
        registerReceiver(receiver, filter);
    }


    /**
     * progress hashmap wasn't in the right order when iterating, this one represents the real copy order
     * list of the files being operated by copy/delete/cut
     * @return
     */
    public List<FileInfo> getFilesToPaste() {
        return mProcessedFiles;
    }

    /**
     * return how many files are currently being operated
     * @return
     */
    public int getPasteTotalFiles() {
        return mProcessedFiles.size();
    }

    /**
     * give the index of current file in list getFilesToPaste
     * @return
     */
    public int getCurrentFile() {
        return mCurrentFile;
    }

    /**
     * give the total size (useful when copying multiple files). Size in B
     * @return
     */
    public long getPasteTotalSize() {
        return mPasteTotalSize;
    }

    /**
     * give the total progress (useful when copying multiple files). Size in B
     * @return
     */
    public long getPasteTotalProgress() {
        return mPasteTotalProgress;
    }

    /**
     * Get progress by file. Size in B
     * @return
     */
    public  HashMap<FileInfo, Long> getFilesProgress() {
        return mProgress;
    }


    @Override
    public void onDestroy() {
    	super.onDestroy();
    	unregisterReceiver(receiver);
    }




    public void copyUri(List<Uri> FilesToPaste, Uri target) {
        if(!mIsActionRunning){
            mIsActionRunning = true;
            mTarget = target;
            mHasOpenAtTheEndBeenSet = false;
            mProgress.clear();
            mPasteTotalSize = (long) 0;
            ArrayList<Uri> sources = new ArrayList<Uri>(FilesToPaste.size());
            sources.addAll(FilesToPaste);
            mCopyCutEngine.copyUri(sources, target, false);
            startStatusbarNotification();
        }
    }



    /*
     * is currently pasting files
     * 
     */
    public boolean isPastingInProgress() {
        return mIsActionRunning;
    }


    public void addListener(ServiceListener listener) {
        if(!mListeners.contains(listener))
            mListeners.add(listener);
    }

    private PendingIntent getCancelIntent() {
        Intent intent = new Intent("CANCEL");
        return PendingIntent.getBroadcast(this, 0, intent, 0);
    }

    private Intent getOpenIntent() {
        Intent intent = new Intent("OPEN");
        return intent;
    }

    private void updateStatusbarNotification(String text) {
        if (text != null) {
            // Update the notification text
            mNotificationBuilder.setContentText(text);
        }

        // Tell the notification manager about the changes
        mNotificationManager.notify(PASTE_NOTIFICATION_ID, mNotificationBuilder.build());
    }

    protected void removeStatusbarNotification() {
        if (mNotificationBuilder != null) {
            mNotificationManager.cancel(PASTE_NOTIFICATION_ID);
            mNotificationBuilder = null;
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return localBinder;
    }

    public class FileManagerServiceBinder extends Binder {
        public FileManagerService getService() {
            return FileManagerService.this;
        }
    }

    public void stopPasting() {
        if(mIsActionRunning){
             if(mCopyCutEngine!=null)
                  mCopyCutEngine.stop();

        }
    }

    public void deleteObserver(ServiceListener listener) {
        mListeners.remove(listener);
    }

    @Override
    public void onStart() {
        acquireWakeLock();
        mLastStatus = ActionStatusEnum.START;
        mIsActionRunning = true;
        startStatusbarNotification();
        long totalSize = 0;
        for (FileInfo mf : mProcessedFiles) {
            totalSize  += mf.length();
        }
        updateStatusbarNotification(0, totalSize, 0, mProcessedFiles.size());
        for(ServiceListener fl :mListeners){
            fl.onActionStart();
        }
    }

    private void acquireWakeLock() {

        releaseWakeLock();
        Log.d(TAG, "acquireWakeLock");
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "FileManagerWakeLock");
        mWakeLock.acquire();
    }
    private void releaseWakeLock(){
        Log.d(TAG, "releaseWakeLock");
        if(mWakeLock!=null&&mWakeLock.isHeld())
            mWakeLock.release();
    }
    @Override
    public void onEnd() {
        releaseWakeLock();
        mLastStatus = ActionStatusEnum.STOP;
        mIsActionRunning = false;
        removeStatusbarNotification();

            String message;
            message = "Download success";

            Toast.makeText(this, message, Toast.LENGTH_LONG).show();

        for(ServiceListener fl :mListeners){
            fl.onActionStop();
        }
    }

    @Override
    public void onFatalError(Exception e) {
        releaseWakeLock();
        Toast.makeText(this, "error", Toast.LENGTH_LONG).show();
        mLastStatus = ActionStatusEnum.ERROR;
        mIsActionRunning = false;
        removeStatusbarNotification();
        for (ServiceListener lis : mListeners){
            lis.onActionError();
        }
    }


    @Override
    public void onProgress(int currentFile, long currentFileProgress,int currentRootFile, long currentRootFileProgress, long totalProgress, double currentSpeed) {
        mLastStatus = ActionStatusEnum.PROGRESS;
        if(currentFile< mProcessedFiles.size()){
            mProgress.put(mProcessedFiles.get(currentFile), currentFileProgress);
        }
        mCurrentFile = currentFile;
        mPasteTotalProgress = totalProgress;
        if(System.currentTimeMillis()-mLastUpdate>200){
            mLastUpdate = System.currentTimeMillis();
            notifyListeners();
        }
        if(System.currentTimeMillis()-mLastStatusBarUpdate>1000){ //updating too often would prevent user from touching the cancel button
            mLastStatusBarUpdate = System.currentTimeMillis();
            updateStatusbarNotification(totalProgress, getPasteTotalSize(), currentFile + 1, mProcessedFiles.size());
        }
    }

    private void notifyListeners() {
        for (ServiceListener lis : mListeners){
            lis.onProgressUpdate();
        }
    }

    @Override
    public void onFilesListUpdate(List<FileInfo> copyingMetaFiles,List<FileInfo> rootFiles) {
        mProcessedFiles.clear();
        mProcessedFiles.addAll(copyingMetaFiles);
        mProgress.clear();
        mPasteTotalSize = (long) 0;
        mPasteTotalProgress = 0;
        for(FileInfo mf : mProcessedFiles){
            mProgress.put(mf, (long) 0);
            mPasteTotalSize+=mf.length();
        }
        long totalSize = 0;
        for(FileInfo mf : mProcessedFiles){
            totalSize  += mf.length();
        }
        updateStatusbarNotification(0, totalSize, 0, mProcessedFiles.size());
    }

    /* Notification */

    public void startStatusbarNotification() {
        mNotificationManager.cancel(OPEN_NOTIFICATION_ID);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
       // int message =  R.string.downloading;
        //CharSequence title = getResources().getText(message);
        long when = System.currentTimeMillis();

        // Build the intent to send when the user clicks on the notification in the notification panel
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.putExtra("LAUNCH_DIALOG",true);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        // Create a new notification builder
        mNotificationBuilder = new NotificationCompat.Builder(this);
        int icon = R.drawable.ic_launcher;

        mNotificationBuilder.setSmallIcon(icon);
        mNotificationBuilder.setTicker(null);

        mNotificationBuilder.setOnlyAlertOnce(true);
        //mNotificationBuilder.setContentTitle(title);
        mNotificationBuilder.setContentIntent(contentIntent);
        mNotificationBuilder.setWhen(when);
        mNotificationBuilder.setOngoing(true);
        mNotificationBuilder.setDefaults(0); // no sound, no light, no vibrate

        // Set the info to display in the notification panel and attach the notification to the notification manager
        updateStatusbarNotification(null);
        mNotificationManager.notify(PASTE_NOTIFICATION_ID, mNotificationBuilder.build());
    }


    private void updateStatusbarNotification(long currentSize, long totalSize, int currentFiles, int totalFiles) {

        if (mNotificationBuilder != null) {
            String formattedCurrentSize = Formatter.formatShortFileSize(this, currentSize);
            String formattedTotalSize = Formatter.formatShortFileSize(this, totalSize);
            int textId;
            String formattedString;

               // textId = R.string.downloading_files;
                // Display the progress in number of files and bytes
                /*formattedString = getResources().getString(textId, currentFiles, totalFiles,
                        formattedCurrentSize, formattedTotalSize);*/

            mNotificationBuilder.setProgress((int) totalSize, (int) currentSize, false);
           // updateStatusbarNotification(formattedString);
        }
    }
    private void setCanceledStatus(){
        mLastStatus = ActionStatusEnum.CANCELED;
        mIsActionRunning = false;
        removeStatusbarNotification();
        for (ServiceListener lis : mListeners){
            lis.onActionCanceled();
        }
        mNotificationManager.cancel(OPEN_NOTIFICATION_ID);
    }
    @Override
    public void onCanceled() {
        releaseWakeLock();
        Toast.makeText(this, "Download canceled", Toast.LENGTH_LONG).show();
        setCanceledStatus();
    }

    @Override
    public void onSuccess(Uri target) {}


}
