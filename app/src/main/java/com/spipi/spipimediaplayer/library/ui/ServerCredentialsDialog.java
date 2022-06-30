package com.spipi.spipimediaplayer.library.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.spipi.spipimediaplayer.R;
import com.spipi.spipimediaplayer.library.NetworkCredentialsDatabase;


public  class ServerCredentialsDialog extends DialogFragment {
    private AlertDialog mDialog;
    protected SharedPreferences mPreferences;
    private String mUsername="";
    private String mPassword="";
    private int mPort=-1;
    private int mType=-1;
    private String mRemote="";
    final private static String FTP_LATEST_URI = "LATEST_URI";

    private OnConnectClickListener mOnConnectClick;
    final private static String FTP_LATEST_USERNAME = "LATEST_USERNAME";

    final public static String USERNAME = "username";
    final public static String PASSWORD = "password";
    final public static String URI = "uri";
    private OnClickListener mOnCancelClickListener;
    private String mPath;
    private CheckBox mShowPasswordButton;
    protected EditText mPathView;
    protected EditText mPasswordView;
    protected EditText mUsernameView;
    protected Uri mUri;
    protected Spinner mTypeSp;
    protected EditText mAddressView;
    protected EditText mPortView;


    public interface OnConnectClickListener {
        public void onConnectClick(String username, Uri uri, String password);
    }
    public ServerCredentialsDialog(){ }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args  = getArguments();
        if(args != null){
            mUsername = args.getString(USERNAME,"");
            mPassword = args.getString(PASSWORD,"");
            mUri = args.getParcelable(URI);

        }
        mPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if(mUsername.isEmpty()&&mPassword.isEmpty()&&mUri==null){
            mUsername = mPreferences.getString(FTP_LATEST_USERNAME, "");
            String lastUri = mPreferences.getString(FTP_LATEST_URI, "");
            if(!lastUri.isEmpty()){
                mUri = Uri.parse(lastUri);
            }
        }

        if(mUri!=null){
            mPort = mUri.getPort();
            mType = "ftp".equals(mUri.getScheme())?0:"sftp".equals(mUri.getScheme())?1:2;
            mPath = mUri.getPath();
            mRemote = mUri.getHost();
        }
        else{
            mPort=-1;
            mType = 0;
            mPath = "";
            mRemote = "";
            mPassword = "";
        }
        if(mUri!=null){
            NetworkCredentialsDatabase database = NetworkCredentialsDatabase.getInstance();
            NetworkCredentialsDatabase.Credential cred = database.getCredential(mUri.toString());
            if(cred!=null){
                mPassword= cred.getPassword();
            }
        }
        final View v = getActivity().getLayoutInflater().inflate(R.layout.ssh_credential_layout, null);
        mTypeSp = (Spinner)v.findViewById(R.id.ssh_spinner);
        mAddressView = (EditText)v.findViewById(R.id.remote);
        mPortView = (EditText)v.findViewById(R.id.port);
        mUsernameView = (EditText)v.findViewById(R.id.username);
        mPasswordView = (EditText)v.findViewById(R.id.password);
        mPathView = (EditText)v.findViewById(R.id.path);
        mShowPasswordButton = (CheckBox)v.findViewById(R.id.show_password_checkbox);
        mShowPasswordButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (!b)
                    mPasswordView.setTransformationMethod(PasswordTransformationMethod.getInstance());
                else
                    mPasswordView.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            }
        });
        int type = mType;
        if (type==0 || type==1 || type==2  || type==3) {
            mTypeSp.setSelection(type);
        }
        mTypeSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 3) {
                    mPortView.setVisibility(View.INVISIBLE);
                } else
                    mPortView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                mPortView.setVisibility(View.VISIBLE);
            }
        });
        mAddressView.setText(mRemote);
        mPathView.setText(mPath);
        int portInt =  mPort;
        String portString = (portInt!=-1) ? Integer.toString(portInt) : "";
        mPortView.setText(portString);
        mUsernameView.setText(mUsername);
        mPasswordView.setText(mPassword);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
        .setView(v)
        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(mOnCancelClickListener!=null)
                    mOnCancelClickListener.onClick(null);
            }
        })
        .setPositiveButton(android.R.string.ok,new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int id) {
                if(!mAddressView.getText().toString().isEmpty()){
                    final String username = mUsernameView.getText().toString();
                    final String password = mPasswordView.getText().toString();

                    String uriToBuild = createUri();
                    onConnectClick(username, Uri.parse(uriToBuild), password);
                    NetworkCredentialsDatabase.getInstance().saveCredential(new NetworkCredentialsDatabase.Credential(username, password, uriToBuild,true));
                     if(mOnConnectClick!=null){
                        mOnConnectClick.onConnectClick(username, Uri.parse(uriToBuild), password);
                    }

                }
                else
                    Toast.makeText(getActivity(), getString(R.string.remote_address_error), Toast.LENGTH_SHORT).show();
            }});
        mDialog = builder.create();

        return mDialog;

    }

    public void onConnectClick(String username, Uri uri, String password) {
        // Store new values to preferences
        mPreferences.edit()
                .putString(FTP_LATEST_URI, uri.toString())
                .putString(FTP_LATEST_USERNAME, username)
                .apply();
    }


    public String createUri() {
        final int type = mTypeSp.getSelectedItemPosition();
        final String address = mAddressView.getText().toString();
        String path = mPathView.getText().toString();
        int port = -1;
        try{
            port = Integer.parseInt(mPortView.getText().toString());
        } catch(NumberFormatException e){ }

        // get default port if it's wrong
        switch(type){
            case 0: if (port == -1)  port=21; break;
            case 1: if (port == -1)  port=22; break;
            case 2: if (port == -1)  port=21; break;
            case 3: port=-1; break;
            default:
                throw new IllegalArgumentException("Invalid FTP type "+type);
        }
        String uriToBuild = "";
        switch(type){
            case 0: uriToBuild = "ftp"; break;
            case 1: uriToBuild = "sftp"; break;
            case 2: uriToBuild = "ftps"; break;
            case 3: uriToBuild = "smb"; break;
            default:
                throw new IllegalArgumentException("Invalid FTP type "+type);
        }
        //path needs to start by a "/"
        if(path.isEmpty()||!path.startsWith("/"))
            path = "/"+path;
        uriToBuild +="://"+(!address.isEmpty()?address+(port!=-1?":"+port:""):"")+path;

        return uriToBuild;

    }

    public void setOnConnectClickListener(OnConnectClickListener onConnectClick) {
        mOnConnectClick = onConnectClick;
    }
    public void setOnCancelClickListener(OnClickListener onClickListener) {
        mOnCancelClickListener = onClickListener;
    }

}
