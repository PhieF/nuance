package com.spipi.spipimediaplayer;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.net.Uri;
import android.os.Environment;
import android.text.Editable;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.spipi.spipimediaplayer.library.FileEditor;
import com.spipi.spipimediaplayer.library.FileEditorFactory;
import com.spipi.spipimediaplayer.library.FileInfo;
import com.spipi.spipimediaplayer.library.FileInfoFactory;
import com.spipi.spipimediaplayer.library.RawListerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FilePickerDialog
{
    private final List<String> m_extensions;
    private final boolean m_is_folder_picker;
    private boolean m_isNewFolderEnabled = true;
    private String m_sdcardDirectory = "";
    private Context m_context;
    private TextView m_titleView;

    private FileInfo m_dir = null;
    private List<FileInfo> m_subdirs = null;
    private FilePickerListener m_filePickerListener = null;
    private ArrayAdapter<String> m_listAdapter = null;
    private ArrayList<String> m_subdirs_string;

    //////////////////////////////////////////////////////
    // Callback interface for selected directory
    //////////////////////////////////////////////////////
    public interface FilePickerListener
    {
        public void onChosenDir(FileInfo chosenDir);
    }

    public FilePickerDialog(Context context, FilePickerListener filePickerListener, boolean isFolderPicker, List<String> extensions)
    {
        m_context = context;
        m_is_folder_picker = isFolderPicker;
        m_extensions = extensions;
        m_sdcardDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();
        m_filePickerListener = filePickerListener;

        try
        {
            m_sdcardDirectory = new File(m_sdcardDirectory).getCanonicalPath();
        }
        catch (IOException ioe)
        {
        }
    }

    ///////////////////////////////////////////////////////////////////////
    // setNewFolderEnabled() - enable/disable new folder button
    ///////////////////////////////////////////////////////////////////////

    public void setNewFolderEnabled(boolean isNewFolderEnabled)
    {
        m_isNewFolderEnabled = isNewFolderEnabled;
    }

    public boolean getNewFolderEnabled()
    {
        return m_isNewFolderEnabled;
    }

    ///////////////////////////////////////////////////////////////////////
    // chooseDirectory() - load directory chooser dialog for initial
    // default sdcard directory
    ///////////////////////////////////////////////////////////////////////

    public void chooseDirectory()
    {
        // Initial directory is sdcard directory
        chooseDirectory(Uri.parse(m_sdcardDirectory));
    }


    ////////////////////////////////////////////////////////////////////////////////
    // chooseDirectory(String dir) - load directory chooser dialog for initial 
    // input 'dir' directory
    ////////////////////////////////////////////////////////////////////////////////

    public void chooseDirectory(Uri dir)
    {
        try {
            m_dir = FileInfoFactory.getFileInfoForUrl(dir);

            m_subdirs = new ArrayList<>();
            m_subdirs_string = new ArrayList<>();

            class DirectoryOnClickListener implements DialogInterface.OnClickListener
            {
                public void onClick(DialogInterface dialog, int item)
                {
                    // Navigate into the sub-directory
                    m_dir = m_subdirs.get(item);
                    if(m_dir.isDirectory())
                        updateDirectory();
                    else{
                        m_filePickerListener.onChosenDir(m_dir);
                        dialog.dismiss();
                    }
                }
            }

            AlertDialog.Builder dialogBuilder =
                    createDirectoryChooserDialog(m_dir.getName(), m_subdirs, new DirectoryOnClickListener());

            dialogBuilder.setPositiveButton("OK", new OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    // Current directory chosen
                    if (m_filePickerListener != null)
                    {
                        // Call registered listener supplied with the chosen directory
                        m_filePickerListener.onChosenDir(m_dir);
                    }
                }
            }).setNegativeButton("Cancel", null);

            final AlertDialog dirsDialog = dialogBuilder.create();

            dirsDialog.setOnKeyListener(new OnKeyListener()
            {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event)
                {
                    if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN)
                    {
                        // Back button pressed
                        if ( m_dir.equals(m_sdcardDirectory) )
                        {
                            // The very top level directory, do nothing
                            return false;
                        }
                        else
                        {
                            // Navigate back to an upper directory
                            m_dir = m_dir.getParent();
                            updateDirectory();
                        }

                        return true;
                    }
                    else
                    {
                        return false;
                    }
                }
            });

            // Show directory chooser dialog
            dirsDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean createSubDir(String dirname)
    {
        FileEditor newDir = FileEditorFactory.getFileEditorForUrl(m_dir.withAppendedName(dirname), m_context);
        if (! newDir.exists() )
        {
            return newDir.mkdir();
        }

        return false;
    }

    private List<FileInfo> getDirectories(FileInfo dir)
    {
        List<FileInfo> dirs = new ArrayList<>();
        List<FileInfo> files = new ArrayList<>();
        List<FileInfo> all = new ArrayList<>();

        try
        {
            if (! dir.exists() || ! dir.isDirectory())
            {
                return all;
            }

            for (FileInfo file : RawListerFactory.getRawListerForUrl(dir.getUri()).getFileList())
            {
                if ( file.isDirectory() )
                {
                    dirs.add( file );
                }
                else if(!m_is_folder_picker){
                    boolean add = m_extensions == null;
                    if(!add){
                        add = m_extensions.contains(file.getExtension());
                    }
                    if(add)
                        files.add(file);
                }

            }
        }
        catch (Exception e)
        {
        }
        Collections.sort(dirs, new Comparator<FileInfo>() {
            public int compare(FileInfo o1, FileInfo o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        Collections.sort(files, new Comparator<FileInfo>() {
            public int compare(FileInfo o1, FileInfo o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        all.addAll(dirs);
        all.addAll(files);
        return all;
    }

    private AlertDialog.Builder createDirectoryChooserDialog(String title, List<FileInfo> listItems,
                                                             DialogInterface.OnClickListener onClickListener)
    {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(m_context);

        // Create custom view for AlertDialog title containing
        // current directory TextView and possible 'New folder' button.
        // Current directory TextView allows long directory path to be wrapped to multiple lines.
        LinearLayout titleLayout = new LinearLayout(m_context);
        titleLayout.setOrientation(LinearLayout.VERTICAL);

        m_titleView = new TextView(m_context);
        m_titleView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        m_titleView.setTextAppearance(m_context, android.R.style.TextAppearance_Large);
        m_titleView.setTextColor( m_context.getResources().getColor(android.R.color.white) );
        m_titleView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        m_titleView.setText(title);

        Button newDirButton = new Button(m_context);
        newDirButton.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        newDirButton.setText("New folder");
        newDirButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final EditText input = new EditText(m_context);

                // Show new folder name input dialog
                new AlertDialog.Builder(m_context).
                        setTitle("New folder name").
                        setView(input).setPositiveButton("OK", new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int whichButton)
                            {
                                Editable newDir = input.getText();
                                String newDirName = newDir.toString();
                                // Create new directory
                                if ( createSubDir(newDirName) )
                                {
                                    // Navigate into the new directory
                                    try {
                                        m_dir = FileInfoFactory.getFileInfoForUrl(m_dir.withAppendedName(newDirName));
                                        updateDirectory();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                }
                                else
                                {
                                    Toast.makeText(
                                            m_context, "Failed to create '" + newDirName +
                                                    "' folder", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).setNegativeButton("Cancel", null).show();
            }
        });

        if (! m_isNewFolderEnabled)
        {
            newDirButton.setVisibility(View.GONE);
        }

        titleLayout.addView(m_titleView);
        titleLayout.addView(newDirButton);

        dialogBuilder.setCustomTitle(titleLayout);

        m_listAdapter = createListAdapter(m_subdirs_string);

        dialogBuilder.setSingleChoiceItems(m_listAdapter, -1, onClickListener);
        dialogBuilder.setCancelable(false);
        updateDirectory();
        return dialogBuilder;
    }

    private void updateDirectory()
    {
        m_subdirs.clear();
        m_subdirs.addAll( getDirectories(m_dir) );
        m_subdirs_string.clear();
        for(FileInfo info: m_subdirs){
            String name = info.getName();
            if(info.isDirectory())
                name+="/";
            m_subdirs_string.add(name);
        }
        m_titleView.setText(m_dir.getName());
        m_listAdapter.notifyDataSetChanged();
    }

    private ArrayAdapter<String> createListAdapter(List<String> items)
    {

        return new ArrayAdapter<String>(m_context,
                android.R.layout.select_dialog_item, android.R.id.text1, items)
        {
            @Override
            public View getView(int position, View convertView,
                                ViewGroup parent)
            {
                View v = super.getView(position, convertView, parent);

                if (v instanceof TextView)
                {
                    // Enable list item (directory) text wrapping
                    TextView tv = (TextView) v;
                    tv.getLayoutParams().height = LayoutParams.WRAP_CONTENT;
                    tv.setEllipsize(null);
                }
                return v;
            }
        };
    }
} 