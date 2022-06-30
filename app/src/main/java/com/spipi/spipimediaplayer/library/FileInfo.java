
package com.spipi.spipimediaplayer.library;

import android.content.Context;
import android.net.Uri;

import java.io.Serializable;

/**
 * Abstraction of a "file". Several file types are supported.
 * Not all File methods implemented yet. Constructors are private.
 */
public abstract class FileInfo implements Serializable , Comparable {

    private static final long serialVersionUID = 2L;

    protected static final String TAG = "MetaFile2";
    private long mComputedLength = -1;

    /** the name of the underlying file */
    public abstract String getName();

    /** true if underlying entity is a directory */
    public abstract boolean isDirectory();

    /** true if underlying entity is a file */
    public abstract boolean isFile();

    /** date of last modification, -1 if unavailable */
    public abstract long lastModified();

    /** true if the underlying file is readable */
    public abstract boolean canRead();

    /** true if the underlying file writable */
    public abstract boolean canWrite();

    /** file size in bytes, -1 if unavailable */
    public abstract long length();

    public long getComputedLength(){
        if(mComputedLength==-1)
            return length();
        return mComputedLength;
    }

    /** Returns lister of this metafile */
    public abstract RawLister getRawListerInstance();
    
    /** Returns editor of this metafile
     * context can be nulled if not using file writing methods*/
    public abstract FileEditor getFileEditorInstance(Context ct);

    /**
     * Returns the Uri that describes this file, i.e. that is used to "open" this file
     * This Uri can be used to list the content if it is a directory
     * This Uri can be used to open the file with an application if it is a file 
     **/
    public abstract Uri getUri();

    /**
     * Usually this returns the same uri as getUri except for upnp where we need a streaming http:// uri
     * @return
     */
    public  Uri getStreamingUri(){
        return getUri();
    }

    /** false if the file is on local storage */
    public abstract boolean isRemote();

    public String getNameWithoutExtension(){
        return "";//Utils.stripExtensionFromName(getName());
    }

    /**
     * Get the lowercase file extension of this file. Can be null
     */
    public String getExtension() {
        if(getName().contains("."))
            return getName().substring(getName().lastIndexOf(".")+1);
        return "";
    }

    /**
     * when manually calculating file length
     * @param length
     */
    public void setLength(long length){
        mComputedLength = length;
    }
    /**
     * Get the MimeType. Can be null
     */
    public String getMimeType() {
        return "";//MimeUtils.guessMimeTypeFromExtension(getExtension());
    }

    public boolean exists(){
        return true;
    }

    @Override
    public int compareTo(Object another) {
        if(another instanceof FileInfo){
            return getName().compareTo(((FileInfo) another).getName());
        }
        return 0;
    }
}
