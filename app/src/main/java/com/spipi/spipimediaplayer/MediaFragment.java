package com.spipi.spipimediaplayer;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.spipi.spipimediaplayer.mediaplayer.MediaPlayerService;


public class MediaFragment extends Fragment implements MyApplication.Mp3ServiceBindListener, MediaPlayerService.OnChangeListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private MediaPlayerService mMediaPlayerService;
    private View mRoot;
    private ImageView mPlayButton;
    private View.OnClickListener mOnPlayClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mMediaPlayerService.play();
        }
    };
    private View.OnClickListener mOnPauseClickListener= new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mMediaPlayerService.pause();
        }
    };
    private TextView mTitle;
    private ImageView mNextButton;
    private ImageView mPreviousButton;
    private ImageView mMenuButton;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MediaFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MediaFragment newInstance(String param1, String param2) {
        MediaFragment fragment = new MediaFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public MediaFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }
    public static void simulateKey(final int KeyCode) {

        new Thread() {
            @Override
            public void run() {
                try {
                    Instrumentation inst = new Instrumentation();
                    inst.sendKeyDownUpSync(KeyCode);
                } catch (Exception e) {
                }
            }

        }.start();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mRoot = inflater.inflate(R.layout.fragment_media, container, false);
        mPlayButton = (ImageView)mRoot.findViewById(R.id.playButton);
        mNextButton = (ImageView)mRoot.findViewById(R.id.nextButton);
        mPreviousButton = (ImageView)mRoot.findViewById(R.id.previousButton);
        mMenuButton = (ImageView)mRoot.findViewById(R.id.menuButton);
        mMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                simulateKey(KeyEvent.KEYCODE_MENU);
            }
        });
        mRoot.findViewById(R.id.launchPlayerButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), PlayerActivity.class);
                startActivity(intent);
            }
        });
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMediaPlayerService.next();
            }
        });
        mPreviousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMediaPlayerService.previous();
            }
        });
        mTitle = (TextView)mRoot.findViewById(R.id.title);
        return mRoot;
    }
    @Override
    public void onViewCreated(View v, Bundle save){
        ((MyApplication)getActivity().getApplication()).bindToMp3Service(this);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    @Override
    public void onDetach() {
        super.onDetach();
        if(mMediaPlayerService!=null)
            mMediaPlayerService.removeOnChangeListener(this);
    }


    @Override
    public void onBind(MediaPlayerService lu) {
        mMediaPlayerService = lu;
        mMediaPlayerService.addOnChangeListener(this);
        onServiceChange();
    }

    @Override
    public void onServiceChange() {
    refreshPlayPause();
    }

    private void refreshPlayPause() {
        if(mMediaPlayerService.isPlaying()) {
            mPlayButton.setImageResource(R.drawable.pause);
            mPlayButton.setOnClickListener(mOnPauseClickListener);
        }
        else {
            mPlayButton.setImageResource(R.drawable.play);
            mPlayButton.setOnClickListener(mOnPlayClickListener);

        }
        mTitle.setText(mMediaPlayerService.getCurrentMusicItem()!=null?mMediaPlayerService.getCurrentMusicItem().getTitle():"no music");
    }
}
