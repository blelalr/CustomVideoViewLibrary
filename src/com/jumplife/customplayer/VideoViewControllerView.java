/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jumplife.customplayer;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.XmlResourceParser;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.VideoView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Locale;
import com.example.customvideoviewlibrary.R;
import com.jumplife.videoloader.BiliLoader;
import com.jumplife.videoloader.DailymotionLoader;
import com.jumplife.videoloader.YoutubeLoader;

import net.londatiga.android.QuickAction;




/**
 * A view containing controls for a MediaPlayer. Typically contains the
 * buttons like "Play/Pause", "Rewind", "Fast Forward" and a progress
 * slider. It takes care of synchronizing the controls with the state
 * of the MediaPlayer.
 * <p>
 * The way to use this class is to instantiate it programatically.
 * The MediaController will create a default set of controls
 * and put them in a window floating above your application. Specifically,
 * the controls will float above the view specified with setAnchorView().
 * The window will disappear if left idle for three seconds and reappear
 * when the user touches the anchor view.
 * <p>
 * Functions like show() and hide() have no effect when MediaController
 * is created in an xml layout.
 * 
 * MediaController will hide and
 * show the buttons according to these rules:
 * <ul>
 * <li> The "previous" and "next" buttons are hidden until setPrevNextListeners()
 *   has been called
 * <li> The "previous" and "next" buttons are visible but disabled if
 *   setPrevNextListeners() was called with null listeners
 * <li> The "rewind" and "fastforward" buttons are shown unless requested
 *   otherwise by using the MediaController(Context, boolean) constructor
 *   with the boolean set to false
 * </ul>
 */
public class VideoViewControllerView extends FrameLayout {
    private static final String TAG = "VideoViewControllerView";
    
    private VideoView  			mPlayer;
    //private MediaPlayerControl  mPlayer;
    private Context            mContext;
    private ViewGroup           mAnchor;
    private View                mRoot;
    private SeekBar         mProgress;
    private SeekBar         mProgressVoice;
    private TextView            mEndTime, mCurrentTime;
    
    private AudioManager 		audioManager;
    private static final int	volumnRate = 10;
    private boolean             mShowing;
    private boolean             mDragging;
    private static final int    sDefaultTimeout = 3000;
    private static final int    FADE_OUT = 1;
    private static final int    SHOW_PROGRESS = 2;
    private boolean             mUseFastForward;
    private boolean             mFromXml;
    //private boolean             mListenersSet;
    private View.OnClickListener mNextListener, mPrevListener;
    StringBuilder               mFormatBuilder;
    Formatter                   mFormatter;
    private ImageButton         mPauseButton;
    private ImageButton         mNextButton;
    private ImageButton         mPrevButton;
    //private ImageButton         mFullscreenButton;
    private Handler             mHandler = new MessageHandler(this);
    public ImageButton         	mFfwdButton;
    public ImageButton         	mRewButton;
    public ImageButton         	mFullscreenButton;
    public ImageView 			ivNextPart;
    public ImageView 			ivPrePart;
    public TextView				tvPart;
    public TextView				tvTime;
    //public ImageButton			mYoutubeQualitySwitch;
    public LinearLayout			mQualitySwitchButton;
    public ImageButton 			ivQualityArrow;
    public TextView				mQualitySwitch;
    public QuickAction      	qaQuality;
    
    private RewTask				rewTask;
	private FwdTask				fwdTask;
	
	private int mPlayResId;
	private int mStopResId;
    
    public VideoViewControllerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRoot = null;
        mContext = context;
        mUseFastForward = true;
        mFromXml = true;
        
        Log.i(TAG, TAG);
    }

    public VideoViewControllerView(Context context, boolean useFastForward) {
        super(context);
        mContext = context;
        mUseFastForward = useFastForward;
        
        Log.i(TAG, TAG);
    }

    public VideoViewControllerView(Context context) {
        this(context, true);

        Log.i(TAG, TAG);
    }

    @Override
    public void onFinishInflate() {
        if (mRoot != null)
            initControllerView(mRoot);
    }
    
    //public void setMediaPlayer(MediaPlayerControl player) {
    public void setMediaPlayer(VideoView player) {
        mPlayer = player;
        updatePausePlay();
        updateFullScreen();
    }
    
    public void setBtnsResource (int playResId, int stopResId, int ffwdResId, int rewResId, int fullScreenResId, 
    		int nextPartResId, int prePartResId, int ivQualityResId, int tvQualityColor, int tvQualityBgResId,
    		int progressPD, int progressTumb,int voicePD, int voiceTumb ){
    	mPlayResId = playResId;
    	mStopResId = stopResId;
    	mFfwdButton.setImageResource(ffwdResId);
       	mRewButton.setImageResource(rewResId);
        mFullscreenButton.setImageResource(fullScreenResId);
        ivNextPart.setImageResource(nextPartResId);
        ivPrePart.setImageResource(prePartResId);
        ivQualityArrow.setImageResource(ivQualityResId);
        mQualitySwitch.setBackgroundResource(tvQualityBgResId);
        XmlResourceParser xrp = getResources().getXml(tvQualityColor);  
        try {  
            ColorStateList csl = ColorStateList.createFromXml(getResources(), xrp);  
            mQualitySwitch.setTextColor(csl);  
        } catch (Exception e) {  } 
        mProgress.setProgressDrawable(mContext.getResources().getDrawable(progressPD));
        mProgress.setThumb( mContext.getResources().getDrawable(progressTumb));
        mProgressVoice.setProgressDrawable(mContext.getResources().getDrawable(voicePD));
        mProgressVoice.setThumb( mContext.getResources().getDrawable(voiceTumb));
        
    }
    public SparseArray<String> parseLink(String videoUrl, SparseArray<String> mVideoQuiltyLink){
    	String videoId = null;
    	
    	if (videoUrl.contains("dailymotion")) {
        	if(videoUrl.contains("embed/video/")) {
				String url = videoUrl.substring(39);
    			String[] tmpUrls = url.split("\\?");	            			
    			if(tmpUrls.length > 0)
    				videoId = tmpUrls[0];
			} else if(videoUrl.contains("touch")) {
				String url = videoUrl.substring(35);
    			String[] tmpUrls = url.split("&");	            			
    			if(tmpUrls.length > 0)
    				videoId = tmpUrls[0];
			}else {
				String url = videoUrl.substring(33);
    			String[] tmpUrls = url.split("&");	            			
    			if(tmpUrls.length > 0)
    				videoId = tmpUrls[0];
			}
			if(videoId != null) {
				videoUrl = "http://www.dailymotion.com/embed/video/" + videoId;
				return DailymotionLoader.Loader(videoUrl);
			}
		} else if (videoUrl.contains("youtube")) {
			if(videoUrl.contains("youtube-nocookie")) {
				String[] tmpUrls = videoUrl.split("\\/");
    			if(tmpUrls.length > 0)
    				videoId = tmpUrls[tmpUrls.length-1];
			} else if(videoUrl.contains("embed")) {
				String[] tmpUrls = videoUrl.split("\\/");	            			
    			if(tmpUrls.length > 0) {
    				String[] tmpId = tmpUrls[tmpUrls.length-1].split("\\?");
    				if(tmpId.length > 0)
    					videoId = tmpId[0];
    			}
			} else {
    			String[] tmpUrls = videoUrl.split("v=");
    			if(tmpUrls.length > 1)
    				videoId = tmpUrls[1];
			}
			if(videoId != null) {
				return YoutubeLoader.Loader(true, videoId);
			}
		} else if (videoUrl.toLowerCase(Locale.getDefault()).contains(".mp4")) {
			mVideoQuiltyLink.put(QuickAction.QUALITY_NORMAL, videoUrl);
			return mVideoQuiltyLink;
		} else if (videoUrl.toLowerCase(Locale.getDefault()).contains(".tudou") ||
				videoUrl.toLowerCase(Locale.getDefault()).contains("youku")) {
			mVideoQuiltyLink.put(QuickAction.QUALITY_NORMAL, videoUrl);
			return mVideoQuiltyLink;
		} else if (videoUrl.toLowerCase(Locale.getDefault()).contains("bilibili")) {
			mVideoQuiltyLink = BiliLoader.Loader(videoUrl);
			return mVideoQuiltyLink;
		}
		return mVideoQuiltyLink;
        
    }

    public void timeToast(int stopPosition, int currentPart) {
    	String timeStr = "";
    	int hou = stopPosition / (1000 * 60 * 60);    	
    	if(hou != 0)
    		timeStr = timeStr + hou + "時";
    	
    	int min = (stopPosition - hou * (1000 * 60 * 60)) / (1000 * 60);
    	if(min != 0)
    		timeStr = timeStr + min + "分";
    	
    	int sec = (stopPosition - hou * (1000 * 60 * 60) - min * (1000 * 60)) / 1000;
    	if(sec != 0)
    		timeStr = timeStr + sec + "秒";
    	
    	if(timeStr == "")
    		timeStr = "頭";
    		
    	String message = "Part" + currentPart + " 將從 " + timeStr + " 開始撥放";
    	Toast.makeText(mContext, message,  Toast.LENGTH_SHORT).show();
    }
    
    public void setPartBtns(int currentPart ,ArrayList<String> videoIds){
    	if(currentPart > videoIds.size()-1)
        	ivNextPart.setVisibility(View.INVISIBLE);
        else
        	ivNextPart.setVisibility(View.VISIBLE);
        
        if(currentPart < 2)
        	ivPrePart.setVisibility(View.INVISIBLE);
        else
        	ivPrePart.setVisibility(View.VISIBLE);
    }


    /**
     * Set the view that acts as the anchor for the control view.
     * This can for example be a VideoView, or your Context's main view.
     * @param view The view to which to anchor the controller when it is visible.
     */
    public void setAnchorView(ViewGroup view) {
        mAnchor = view;

        FrameLayout.LayoutParams frameParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );

        removeAllViews();
        View v = makeControllerView();
        addView(v, frameParams);
    }

    /**
     * Create the view that holds the widgets that control playback.
     * Derived classes can override this to create their own.
     * @return The controller view.
     * @hide This doesn't work as advertised
     */
    protected View makeControllerView() {
        LayoutInflater inflate = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRoot = inflate.inflate(R.layout.media_controller, null);
        
        initControllerView(mRoot);

        return mRoot;
    }

    private void initControllerView(View v) {
        mPauseButton = (ImageButton)v.findViewById(R.id.pause);
        if (mPauseButton != null) {
            mPauseButton.requestFocus();
            mPauseButton.setOnClickListener(mPauseListener);
        }
        
        /*mYoutubeQualitySwitch = (ImageButton)v.findViewById(R.id.quality);
        if (mYoutubeQualitySwitch != null) {
        	mYoutubeQualitySwitch.requestFocus();
        }*/
        mQualitySwitchButton = (LinearLayout)v.findViewById(R.id.ll_quality);
        if (mQualitySwitchButton != null) {
        	mQualitySwitchButton.requestFocus();
        }
        ivQualityArrow  = (ImageButton)v.findViewById(R.id.iv_quality);
        if (ivQualityArrow != null) {
        	ivQualityArrow.requestFocus();
        }
        mQualitySwitch = (TextView)v.findViewById(R.id.tv_quality);
        if (mQualitySwitch != null) {
        	mQualitySwitch.requestFocus();
        }
        
        mFullscreenButton = (ImageButton)v.findViewById(R.id.fullscreen);
        if (mFullscreenButton != null) {
            mFullscreenButton.requestFocus();
            mFullscreenButton.setOnClickListener(mFullscreenListener);
        }

        mFfwdButton = (ImageButton)v.findViewById(R.id.ffwd);
        if (mFfwdButton != null) {
            mFfwdButton.setOnClickListener(mFfwdListener);
            if (!mFromXml) {
                mFfwdButton.setVisibility(mUseFastForward ? View.VISIBLE : View.GONE);
            }
        }

        mRewButton = (ImageButton)v.findViewById(R.id.rew);
        if (mRewButton != null) {
            mRewButton.setOnClickListener(mRewListener);
            if (!mFromXml) {
                mRewButton.setVisibility(mUseFastForward ? View.VISIBLE : View.GONE);
            }
        }

        ivPrePart = (ImageView)v.findViewById(R.id.iv_prepart);
        if (ivPrePart != null) {
        	ivPrePart.requestFocus();
        }
        
        ivNextPart = (ImageView)v.findViewById(R.id.iv_nextpart);
        if (ivNextPart != null) {
        	ivNextPart.requestFocus();
        }
        
        // By default these are hidden. They will be enabled when setPrevNextListeners() is called 
        /*mNextButton = (ImageButton) v.findViewById(R.id.next);
        if (mNextButton != null && !mFromXml && !mListenersSet) {
            mNextButton.setVisibility(View.GONE);
        }
        mPrevButton = (ImageButton) v.findViewById(R.id.prev);
        if (mPrevButton != null && !mFromXml && !mListenersSet) {
            mPrevButton.setVisibility(View.GONE);
        }*/

        mProgress = (SeekBar) v.findViewById(R.id.mediacontroller_progress);
        if (mProgress != null) {
            if (mProgress instanceof SeekBar) {
                SeekBar seeker = (SeekBar) mProgress;
                seeker.setOnSeekBarChangeListener(mSeekListener);
            }
            mProgress.setMax(1000);
        }
        
        audioManager = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
        mProgressVoice = (SeekBar) v.findViewById(R.id.mediacontroller_voice);
        if (mProgressVoice != null) {
            if (mProgressVoice instanceof SeekBar) {
                SeekBar seeker = (SeekBar) mProgressVoice;
                seeker.setOnSeekBarChangeListener(mVoiceListener);
            }
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            int curVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            mProgressVoice.setMax(maxVolume * volumnRate);
            mProgressVoice.setProgress(curVolume * volumnRate);
        }

        mEndTime = (TextView) v.findViewById(R.id.time);
        mCurrentTime = (TextView) v.findViewById(R.id.time_current);  	
        tvPart = (TextView) v.findViewById(R.id.tv_part);
        tvTime = (TextView) v.findViewById(R.id.tv_time);
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
    	
        installPrevNextListeners();
    }

    /**
     * Show the controller on screen. It will go away
     * automatically after 3 seconds of inactivity.
     */
    public void show() {
        show(sDefaultTimeout);
    }

    /**
     * Disable pause or seek buttons if the stream cannot be paused or seeked.
     * This requires the control interface to be a MediaPlayerControlExt
     */
    private void disableUnsupportedButtons() {
        if (mPlayer == null) {
            return;
        }
        
        try {
            if (mPauseButton != null && !mPlayer.canPause()) {
                mPauseButton.setEnabled(false);
            }
            if (mRewButton != null && !mPlayer.canSeekBackward()) {
                mRewButton.setEnabled(false);
            }
            if (mFfwdButton != null && !mPlayer.canSeekForward()) {
                mFfwdButton.setEnabled(false);
            }
        } catch (IncompatibleClassChangeError ex) {
            // We were given an old version of the interface, that doesn't have
            // the canPause/canSeekXYZ methods. This is OK, it just means we
            // assume the media can be paused and seeked, and so we don't disable
            // the buttons.
        }
    }
    
    /**
     * Show the controller on screen. It will go away
     * automatically after 'timeout' milliseconds of inactivity.
     * @param timeout The timeout in milliseconds. Use 0 to show
     * the controller until hide() is called.
     */
    public void show(int timeout) {
        if (!mShowing && mAnchor != null) {
            setProgress();
            if (mPauseButton != null) {
                mPauseButton.requestFocus();
            }
            disableUnsupportedButtons();

            FrameLayout.LayoutParams tlp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM
            );
            
            mAnchor.addView(this, tlp);
            mShowing = true;
        }
        updatePausePlay();
        updateFullScreen();
        
        // cause the progress bar to be updated even if mShowing
        // was already true.  This happens, for example, if we're
        // paused with the progress bar showing the user hits play.
        mHandler.sendEmptyMessage(SHOW_PROGRESS);

        Message msg = mHandler.obtainMessage(FADE_OUT);
        if (timeout != 0) {
            mHandler.removeMessages(FADE_OUT);
            mHandler.sendMessageDelayed(msg, timeout);
        }
    }
    
    public boolean isShowing() {
        return mShowing;
    }

    /**
     * Remove the controller from the screen.
     */
    public void hide() {
        if (mAnchor == null) {
            return;
        }
        if(qaQuality.onShow()) {
        	show(sDefaultTimeout);
        	return;
        }
        	
        try {
        	mAnchor.removeView(this);
            mHandler.removeMessages(SHOW_PROGRESS);
        } catch (IllegalArgumentException ex) {
            Log.w("MediaController", "already removed");
        }
        mShowing = false;
    }

    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours   = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    private int setProgress() {
        if (mPlayer == null || mDragging) {
            return 0;
        }
        
        int position = (int) mPlayer.getCurrentPosition();
        int duration = (int) mPlayer.getDuration();
        if (mProgress != null) {
            if (duration > 0) {
                // use long to avoid overflow
                long pos = 1000L * position / duration;
                mProgress.setProgress( (int) pos);
            }
            int percent = mPlayer.getBufferPercentage();
            mProgress.setSecondaryProgress(percent * 10);
        }
        
        if (mEndTime != null)
            mEndTime.setText(stringForTime(duration));
        if (mCurrentTime != null)
            mCurrentTime.setText(stringForTime(position));

        return position;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        show(sDefaultTimeout);
        return true;
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        show(sDefaultTimeout);
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (mPlayer == null) {
            return true;
        }
        
        int keyCode = event.getKeyCode();
        final boolean uniqueDown = event.getRepeatCount() == 0
                && event.getAction() == KeyEvent.ACTION_DOWN;
        if (keyCode ==  KeyEvent.KEYCODE_HEADSETHOOK
                || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                || keyCode == KeyEvent.KEYCODE_SPACE) {
            if (uniqueDown) {
                doPauseResume();
                show(sDefaultTimeout);
                if (mPauseButton != null) {
                    mPauseButton.requestFocus();
                }
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
            if (uniqueDown && !mPlayer.isPlaying()) {
                mPlayer.start();
                updatePausePlay();
                show(sDefaultTimeout);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
            if (uniqueDown && mPlayer.isPlaying()) {
                mPlayer.pause();
                updatePausePlay();
                show(sDefaultTimeout);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
                || keyCode == KeyEvent.KEYCODE_VOLUME_UP
                || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE) {
            // don't show the controls for volume adjustment
            return super.dispatchKeyEvent(event);
        } else if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_MENU) {
            if (uniqueDown) {
                hide();
            }
            return true;
        }

        show(sDefaultTimeout);
        return super.dispatchKeyEvent(event);
    }

    private View.OnClickListener mPauseListener = new View.OnClickListener() {
        public void onClick(View v) {
            doPauseResume();
            show(sDefaultTimeout);
        }
    };
    
    private View.OnClickListener mFullscreenListener = new View.OnClickListener() {
        public void onClick(View v) {
            doToggleFullscreen();
            show(sDefaultTimeout);
        }
    };

    public void updatePausePlay() {
        if (mRoot == null || mPauseButton == null || mPlayer == null) {
            return;
        }

        if (mPlayer.isPlaying()) {
            mPauseButton.setImageResource(mStopResId);
        } else {
            mPauseButton.setImageResource(mPlayResId);
        }
    }

    public void updateFullScreen() {
        if (mRoot == null || mFullscreenButton == null || mPlayer == null) {
            return;
        }
        
        /*if (mPlayer.isFullScreen()) {
            mFullscreenButton.setImageResource(R.drawable.button_fullscreen);
        }
        else {
            mFullscreenButton.setImageResource(R.drawable.button_fullscreen);
        }*/
    }

    private void doPauseResume() {
        if (mPlayer == null) {
            return;
        }
        
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
        } else {
            mPlayer.start();
        }
        updatePausePlay();
    }

    private void doToggleFullscreen() {
        if (mPlayer == null) {
            return;
        }
        
        //mPlayer.toggleFullScreen();
    }

    // There are two scenarios that can trigger the seekbar listener to trigger:
    //
    // The first is the user using the touchpad to adjust the posititon of the
    // seekbar's thumb. In this case onStartTrackingTouch is called followed by
    // a number of onProgressChanged notifications, concluded by onStopTrackingTouch.
    // We're setting the field "mDragging" to true for the duration of the dragging
    // session to avoid jumps in the position in case of ongoing playback.
    //
    // The second scenario involves the user operating the scroll ball, in this
    // case there WON'T BE onStartTrackingTouch/onStopTrackingTouch notifications,
    // we will simply apply the updated position without suspending regular updates.
    private OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {
    	
    	public void onStartTrackingTouch(SeekBar bar) {
            show(3600000);

            mDragging = true;

            // By removing these pending progress messages we make sure
            // that a) we won't update the progress while the user adjusts
            // the seekbar and b) once the user is done dragging the thumb
            // we will post one of these messages to the queue again and
            // this ensures that there will be exactly one message queued up.
            mHandler.removeMessages(SHOW_PROGRESS);
        }

        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
        	if (mPlayer == null) {
                return;
            }
            
            if (!fromuser) {
                // We're not interested in programmatically generated changes to
                // the progress bar's position.
                return;
            }

            long duration = mPlayer.getDuration();
            long newposition = (duration * progress) / 1000L;
            mPlayer.seekTo( (int) newposition);
            if (mCurrentTime != null)
                mCurrentTime.setText(stringForTime( (int) newposition));
        }

        public void onStopTrackingTouch(SeekBar bar) {
        	mDragging = false;
            setProgress();
            updatePausePlay();
            show(sDefaultTimeout);

            // Ensure that progress is properly updated in the future,
            // the call to show() does not guarantee this because it is a
            // no-op if we are already showing.
            mHandler.sendEmptyMessage(SHOW_PROGRESS);
        }
    };
    
    private OnSeekBarChangeListener mVoiceListener = new OnSeekBarChangeListener() {
    	
    	public void onStartTrackingTouch(SeekBar bar) {
            show(3600000);

            mDragging = true;

            // By removing these pending progress messages we make sure
            // that a) we won't update the progress while the user adjusts
            // the seekbar and b) once the user is done dragging the thumb
            // we will post one of these messages to the queue again and
            // this ensures that there will be exactly one message queued up.
            mHandler.removeMessages(SHOW_PROGRESS);
        }

        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
        	if (mPlayer == null) {
                return;
            }
            
            if (!fromuser) {
                // We're not interested in programmatically generated changes to
                // the progress bar's position.
                return;
            }
            
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress / volumnRate, 0);
        }

        public void onStopTrackingTouch(SeekBar bar) {
        	mDragging = false;
            show(sDefaultTimeout);

            // Ensure that progress is properly updated in the future,
            // the call to show() does not guarantee this because it is a
            // no-op if we are already showing.
            mHandler.sendEmptyMessage(SHOW_PROGRESS);
        }
    };

    @Override
    public void setEnabled(boolean enabled) {
        if (mPauseButton != null) {
            mPauseButton.setEnabled(enabled);
        }
        if (mFfwdButton != null) {
            mFfwdButton.setEnabled(enabled);
        }
        if (mRewButton != null) {
            mRewButton.setEnabled(enabled);
        }
        if (mNextButton != null) {
            mNextButton.setEnabled(enabled && mNextListener != null);
        }
        if (mPrevButton != null) {
            mPrevButton.setEnabled(enabled && mPrevListener != null);
        }
        if (mProgress != null) {
            mProgress.setEnabled(enabled);
        }
        if (mProgressVoice != null) {
        	mProgressVoice.setEnabled(enabled);
        }        
        
        disableUnsupportedButtons();
        super.setEnabled(enabled);
    }
    
    private View.OnClickListener mRewListener = new View.OnClickListener() {
        public void onClick(View v) {
        	if (mPlayer == null) {
                return;
            }

        	if(rewTask != null && !rewTask.isCancelled())
        		rewTask.cancel(true);
        	rewTask = new RewTask();
	        if(Build.VERSION.SDK_INT < 11)
	        	rewTask.execute();
	        else
	        	rewTask.executeOnExecutor(RewTask.THREAD_POOL_EXECUTOR);
        }
    };

    private View.OnClickListener mFfwdListener = new View.OnClickListener() {
        public void onClick(View v) {
        	if (mPlayer == null) {
                return;
            }
            

        	if(fwdTask != null && !fwdTask.isCancelled())
        		fwdTask.cancel(true);
        	fwdTask = new FwdTask();
	        if(Build.VERSION.SDK_INT < 11)
	        	fwdTask.execute();
	        else
	        	fwdTask.executeOnExecutor(FwdTask.THREAD_POOL_EXECUTOR);
        }
    };

    private void installPrevNextListeners() {
        if (mNextButton != null) {
            mNextButton.setOnClickListener(mNextListener);
            mNextButton.setEnabled(mNextListener != null);
        }

        if (mPrevButton != null) {
            mPrevButton.setOnClickListener(mPrevListener);
            mPrevButton.setEnabled(mPrevListener != null);
        }
    }

    public void setPrevNextListeners(View.OnClickListener next, View.OnClickListener prev) {
        mNextListener = next;
        mPrevListener = prev;
        //mListenersSet = true;

        if (mRoot != null) {
            installPrevNextListeners();
            
            if (mNextButton != null && !mFromXml) {
                mNextButton.setVisibility(View.VISIBLE);
            }
            if (mPrevButton != null && !mFromXml) {
                mPrevButton.setVisibility(View.VISIBLE);
            }
        }
    }
    
    public interface MediaPlayerControl {
        void    start();
        void    pause();
        int     getDuration();
        int     getCurrentPosition();
        void    seekTo(int pos);
        boolean isPlaying();
        int     getBufferPercentage();
        boolean canPause();
        boolean canSeekBackward();
        boolean canSeekForward();
        boolean isFullScreen();
        void    toggleFullScreen();
    }
    
    private static class MessageHandler extends Handler {
        private final WeakReference<VideoViewControllerView> mView; 

        MessageHandler(VideoViewControllerView view) {
            mView = new WeakReference<VideoViewControllerView>(view);
        }
        @Override
        public void handleMessage(Message msg) {
            VideoViewControllerView view = mView.get();
            if (view == null || view.mPlayer == null) {
                return;
            }
            
            int pos;
            switch (msg.what) {
                case FADE_OUT:
                    view.hide();
                    break;
                case SHOW_PROGRESS:
                    pos = view.setProgress();
                    if (!view.mDragging && view.mShowing && view.mPlayer.isPlaying()) {
                        msg = obtainMessage(SHOW_PROGRESS);
                        sendMessageDelayed(msg, 1000 - (pos % 1000));
                    }
                    break;
            }
        }
    }
    
    class RewTask extends AsyncTask<Void, Void, Void> {

    	int position;
    	int duration;
    	
		@Override
		protected Void doInBackground(Void... params) {
            
			int pos = (int) mPlayer.getCurrentPosition();
            pos -= 15000; // milliseconds
            mPlayer.seekTo(pos);

            if (mPlayer == null || mDragging) {
                return null;
            }
            
            position = (int) mPlayer.getCurrentPosition();
            duration = (int) mPlayer.getDuration();
            if (mProgress != null) {
                if (duration > 0) {
                    // use long to avoid overflow
                    long pos_tmp = 1000L * position / duration;
                    mProgress.setProgress( (int) pos_tmp);
                }
                int percent = mPlayer.getBufferPercentage();
                mProgress.setSecondaryProgress(percent * 10);
            }
            return null;
		}
        
        protected void onPostExecute() {

            if (mEndTime != null)
                mEndTime.setText(stringForTime(duration));
            if (mCurrentTime != null)
                mCurrentTime.setText(stringForTime(position));
            
            show(sDefaultTimeout);
        	super.onPostExecute(null);
        }

    }
    
    class FwdTask extends AsyncTask<Void, Void, Void> {

    	int position;
    	int duration;
    	
		@Override
		protected Void doInBackground(Void... params) {
            
			int pos = (int) mPlayer.getCurrentPosition();
            pos += 15000; // milliseconds
            mPlayer.seekTo(pos);

            if (mPlayer == null || mDragging) {
                return null;
            }
            
            position = (int) mPlayer.getCurrentPosition();
            duration = (int) mPlayer.getDuration();
            if (mProgress != null) {
                if (duration > 0) {
                    // use long to avoid overflow
                    long pos_tmp = 1000L * position / duration;
                    mProgress.setProgress( (int) pos_tmp);
                }
                int percent = mPlayer.getBufferPercentage();
                mProgress.setSecondaryProgress(percent * 10);
            }
            return null;
		}
        
        protected void onPostExecute() {

            if (mEndTime != null)
                mEndTime.setText(stringForTime(duration));
            if (mCurrentTime != null)
                mCurrentTime.setText(stringForTime(position));
            
            show(sDefaultTimeout);
        	super.onPostExecute(null);
        }

    }
    
    public void setVolumn() {
    	if (mProgressVoice != null) {
            int curVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            mProgressVoice.setProgress(curVolume * volumnRate);
    	}
    }
}