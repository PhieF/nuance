package com.spipi.spipimediaplayer.network;

import android.net.Uri;

import java.util.List;

/**
 * Created by alexandre on 19/08/15.
 */
public interface OperationEngineListener {
    /**
     *
     */
    public void onStart();
    /**
     * Progress status of current copy
     * currentFile : current file in complete list of files to copy
     * currentFileProgress : its progress
     * currentRootFile : currently copying rootFile as first set in the engine
     * currentRootProgress : its progress
     *
     */
    public void onProgress(int currentFile, long currentFileProgress, int currentRootFile, long currentRootProgress, long totalProgress, double currentSpeed);
    /**
     * copy success for a particular file
     */
    public void onSuccess(Uri file);

    /**
     * send new file list
     *
     * copyingMetaFiles : every metafile being copied
     * rootMetaFiles : files as first set by user of the engine (for example : copy(List<Uri>) Uri -> Metafile2) associated with there total length
     *
     *
     */
    public void onFilesListUpdate(List<FileInfo> copyingMetaFiles, List<FileInfo> rootMetaFiles);
    /**
     * Copy is finished
     */
    public void onEnd();

    /**
     * When an error occurred
     */
    public void onFatalError(Exception e);

    /**
     * When action is canceled
     */
    public void onCanceled();
}
