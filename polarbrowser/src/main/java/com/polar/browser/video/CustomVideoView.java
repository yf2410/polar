package com.polar.browser.video;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.common.data.CommonData;
import com.polar.browser.common.ui.CommonDialog;
import com.polar.browser.env.AppEnv;
import com.polar.browser.i.IVideoControl;
import com.polar.browser.library.utils.NetWorkUtils;
import com.polar.browser.manager.ConfigManager;
import com.polar.browser.manager.ThreadManager;
import com.polar.browser.utils.AnimListenerAdapter;
import com.polar.browser.utils.ConfigWrapper;
import com.polar.browser.utils.CustomToastUtils;
import com.polar.browser.utils.DensityUtil;
import com.polar.browser.utils.SimpleLog;
import com.polar.browser.utils.SysUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class CustomVideoView extends RelativeLayout implements OnClickListener, IVideoControl {

	private static final String TAG = "CustomVideoView";
	// 自动隐藏顶部和底部View的时间
	private static final int HIDE_TIME = 5000;
	private static final int MARGIN_FROM_MIDDLE = 100;
	private static final float NIGHT_MODE_BRIGHTNESS = 0.01f;
	// 自定义VideoView
	private FullScreenVideoView mVideo;
	// 顶部View
	private View mTopView;
	private View mUpperView;
	// 底部View
	private View mBottomView;
	// 视频播放拖动条
	private SeekBar mSeekBar;
	private ImageView mPlay;
	private ImageView mStart;
	private ImageView mVideoNext;
	private TextView mPlayTime;
	private TextView mDurationTime;
	/**
	 * 锁定屏幕时显示的进度条
	 **/
	private ProgressBar mProgressBar;
	/**
	 * 锁定
	 **/
	private ImageView mLock;
	/**
	 * 是否锁定了
	 **/
	private boolean mIsLocked;
	private TextView mTvTitle;
	private TextView mTvTime;
	private ImageView mTvNetType;
	private ImageView mTvBattery;
	/**
	 * 亮度
	 **/
	private VideoBrightnessView mBrightnessView;
	/**
	 * 音量
	 **/
	private VideoVoiceView mVoiceView;
	/**
	 * 前进or后退
	 **/
	private VideoRateView mRateView;
	// 音频管理器
	private AudioManager mAudioManager;
	// 屏幕宽高
	private float width;
	private float height;
	// 视频播放时间(暂停时做一个播放记录)
	private int playTime;
	// 视频路径
	private String mVideoPath;
	/**
	 * 是否正在前进or后退
	 **/
	private boolean mIsSeeking;
	private Timer mTimer;
	/**
	 * 上一次进度
	 **/
	private int mLastProgress;
	/**
	 * 视频加载loading
	 **/
	private ImageView mLoading;
	private Animation mLoadingAnim;
	/**
	 * 亮度
	 **/
	private float mBrightness;
	/**
	 * 上次亮度
	 **/
	private float mLastBrightness;
	/**
	 * 是否按着seekbar
	 **/
	private boolean mTouchingSeekBar;
	/**
	 * 显示网络状况改变对话框
	 **/
	private CommonDialog mNetWorkChangedDialog;
	/**
	 * 弹框后点击确定了播放
	 **/
	private IPlay mIPlay;
	/**
	 * 监听网络状态改变 & 电池电量变化
	 **/
	private BroadcastReceiver mReciver;
	/**
	 * 要过滤的广播
	 **/
	private IntentFilter mFilter;
	//崩溃类型统计
//	private String type;
	private Context mContext;
	private OnTouchListener mEmptyTouchListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			return true;
		}
	};
	private Runnable hideRunnable = new Runnable() {

		@Override
		public void run() {
			showOrHide();
		}
	};
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
				case 1:
					if (mIsSeeking) {
						break;
					}
					if (mVideo.getCurrentPosition() > 0) {
						int progress = mVideo.getCurrentPosition();
						mPlayTime.setText(formatTime(progress));
						mSeekBar.setProgress(progress);
						mProgressBar.setProgress(progress);
						int secondaryProgress = (int) (mVideo.getBufferPercentage() / 100.0 * mVideo.getDuration());
						mSeekBar.setSecondaryProgress(secondaryProgress);
						mProgressBar.setSecondaryProgress(secondaryProgress);
					} else {
						mPlayTime.setText("00:00");
						mSeekBar.setProgress(0);
						mProgressBar.setProgress(0);
						mSeekBar.setSecondaryProgress(0);
						mProgressBar.setSecondaryProgress(0);
					}
					break;
				case 2:
					showOrHide();
					break;
				default:
					break;
			}
		}
	};
	private OnPreparedListener mOnPreparedListener = new OnPreparedListener() {

		@Override
		public void onPrepared(MediaPlayer mp) {
			if (!isShown()) {
				onPause();
				return;
			}
			if (mp == null) {
				showErrorToast();
//				type = ConfigDefine.CRASH_TYPE_MP_NULL;
//				sendCrashUMStat(type);
				return;
			}
			mVideo.setBackgroundResource(0);
			mVideo.setVideoWidth(mp.getVideoWidth());
			mVideo.setVideoHeight(mp.getVideoHeight());
			mLoading.clearAnimation();
			mLoading.setVisibility(View.GONE);
			if (playTime != 0) {
				mVideo.seekTo(playTime);
			}
			mPlay.setImageResource(R.drawable.video_stop);
			mStart.setVisibility(View.GONE);
			int duration = mVideo.getDuration();
			SimpleLog.e(TAG, "onPrepared :: duration == " + duration);
			mSeekBar.setMax(duration);
			mProgressBar.setMax(duration);
			if (mHandler != null) {
				mHandler.removeCallbacks(hideRunnable);
				mHandler.postDelayed(hideRunnable, HIDE_TIME);
			}
			mDurationTime.setText(formatTime(mVideo.getDuration()));
			if (mTimer != null) {
				mTimer.cancel();
			}
			mTimer = new Timer();
			mTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					if (mHandler != null) {
						if (mTouchingSeekBar) {
							return;
						}
						mHandler.sendEmptyMessage(1);
					}
				}
			}, 0, 1000);
			// 数据流量下要做判断
//			if (VideoManager.getInstance().canPlay()) {
			play();
//			} else {
//				pause();
//				VideoManager.getInstance().showNetWorkChangedDialog(getContext(), mIPlay);
//			}
		}
	};
	private float mLastMotionX;
	private float mLastMotionY;
	private int startX;
	private int startY;
	private int threshold;
	private boolean isClick = true;
	private OnTouchListener mTouchListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			final float x = event.getX();
			final float y = event.getY();
			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					mLastMotionX = x;
					mLastMotionY = y;
					startX = (int) x;
					startY = (int) y;
					break;
				case MotionEvent.ACTION_MOVE:
					float deltaX = x - mLastMotionX;
					float deltaY = y - mLastMotionY;
					float absDeltaX = Math.abs(deltaX);
					float absDeltaY = Math.abs(deltaY);
					// 声音调节标识
					boolean isAdjustAudio = false;
					if (absDeltaX > threshold && absDeltaY > threshold) {
						if (absDeltaX < absDeltaY) {
							if (mRateView.isShown()) {
								isAdjustAudio = false;
							} else if (mVoiceView.isShown()
									|| mBrightnessView.isShown()) {
								isAdjustAudio = true;
							} else {
								isAdjustAudio = true;
							}
						} else {
							if (mRateView.isShown()) {
								isAdjustAudio = false;
							} else if (mVoiceView.isShown()
									|| mBrightnessView.isShown()) {
								isAdjustAudio = true;
							} else {
								isAdjustAudio = false;
							}
						}
					} else if (absDeltaX < threshold && absDeltaY > threshold) {
						if (mRateView.isShown()) {
							isAdjustAudio = false;
						} else if (mVoiceView.isShown()
								|| mBrightnessView.isShown()) {
							isAdjustAudio = true;
						} else {
							isAdjustAudio = true;
						}
					} else if (absDeltaX > threshold && absDeltaY < threshold) {
						if (mRateView.isShown()) {
							isAdjustAudio = false;
						} else if (mVoiceView.isShown()
								|| mBrightnessView.isShown()) {
							isAdjustAudio = true;
						} else {
							isAdjustAudio = false;
						}
					} else {
						return true;
					}
					if (isAdjustAudio) {
						if (x < width / 2) {
							if (deltaY > 0) {
								lightDown(absDeltaY);
							} else if (deltaY < 0) {
								lightUp(absDeltaY);
							}
						} else {
							if (deltaY > 0) {
								volumeDown(absDeltaY);
							} else if (deltaY < 0) {
								volumeUp(absDeltaY);
							}
						}
					} else {
						if (deltaX > 0) {
							forward(absDeltaX);
						} else if (deltaX < 0) {
							backward(absDeltaX);
						}
					}
					mLastMotionX = x;
					mLastMotionY = y;
					break;
				case MotionEvent.ACTION_UP:
					if (Math.abs(x - startX) > threshold
							|| Math.abs(y - startY) > threshold) {
						isClick = false;
					}
					mLastMotionX = 0;
					mLastMotionY = 0;
					if (mIsSeeking) {
						seekVideo(x - startX);
						mIsSeeking = false;
					}
					startX = (int) 0;
					startY = 0;
					if (isClick) {
						showOrHide();
					}
					isClick = true;
					break;
				default:
					break;
			}
			return true;
		}
	};

	public CustomVideoView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
		inflate();
	}

	public CustomVideoView(Context context) {
		this(context, null);
	}

	private void inflate() {
		inflate(getContext(), R.layout.view_video, this);
	}

	public void init() {
		initView();
		initListener();
		initData();
		initReceiver();
	}

	private void initReceiver() {
		mReciver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				if (TextUtils.equals(action, CommonData.ACTION_CONNECTIVITY_CHANGE)) {
					int networkState = NetWorkUtils.getNetworkState(getContext());
					if (!isShown()) {
						if (NetWorkUtils.NETWORN_WIFI == networkState) {
							mTvNetType.setImageResource(R.drawable.icon_wifi);
						} else {
							mTvNetType.setImageResource(R.drawable.icon_gprs);
						}
						return;
					}
					// 网络状态改变
					updateNetTypeIcon(networkState);
				} else if (TextUtils.equals(action, Intent.ACTION_BATTERY_CHANGED)) {
					// 电池状态改变
					// 电池电量，数字
					updateBatteryIcon(intent.getIntExtra("level", 0));
				}
			}
		};
		mFilter = new IntentFilter();
		mFilter.addAction(CommonData.ACTION_CONNECTIVITY_CHANGE);
		mFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
		getContext().registerReceiver(mReciver, mFilter);
	}

	private void updateNetTypeIcon(int networkState) {
		if (NetWorkUtils.NETWORN_WIFI == networkState) {
			mTvNetType.setImageResource(R.drawable.icon_wifi);
			if (mNetWorkChangedDialog != null && mNetWorkChangedDialog.isShowing()) {
				mNetWorkChangedDialog.dismiss();
			}
		} else {
			mTvNetType.setImageResource(R.drawable.icon_gprs);
			if (NetWorkUtils.NETWORN_NONE == networkState) {
				// 数据流量
				// 提示
				SimpleLog.e(TAG, "-------------- >>>>>>  切了gprs..... nettype == " + networkState);
				if (!VideoManager.getInstance().canPlay()) {
					pause();
					// 显示弹框
					VideoManager.getInstance().showNetWorkChangedDialog(getContext(), mIPlay);
				}
			}
		}
	}

	private void updateBatteryIcon(int level) {
		if (level <= 10) {
			// 设置电池图标为10
			mTvBattery.setImageResource(R.drawable.icon_battery0);
		} else if (level <= 35) {
			// 设置电池图标为20
			mTvBattery.setImageResource(R.drawable.icon_battery20);
		} else if (level <= 65) {
			// 50
			mTvBattery.setImageResource(R.drawable.icon_battery50);
		} else if (level <= 85) {
			// 80
			mTvBattery.setImageResource(R.drawable.icon_battery80);
		} else {
			// 100
			mTvBattery.setImageResource(R.drawable.icon_battery100);
		}
	}

	public void playVideo(VideoItem item) {
		// 全屏
		SysUtils.setFullScreen((Activity) getContext(), true);
		((Activity) getContext()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
		initBrightness();
		String videoUrl = item.url;
		if (!TextUtils.isEmpty(videoUrl)) {
			playVideo(videoUrl);
			SimpleLog.e("", "videoUrl == " + videoUrl);
		}
		String title = item.title;
		if (!TextUtils.isEmpty(title)) {
			mTvTitle.setText(title);
		}
	}

	public void playCustomVideo(String filePath, String fileName) {
//		// 全屏
//		SysUtils.setFullScreen((Activity)getContext(), true);
//		((Activity)getContext()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
		initBrightness();
		if (!TextUtils.isEmpty(filePath)) {
			playVideoByFileName(filePath);
		}
		if (!TextUtils.isEmpty(fileName)) {
			mTvTitle.setText(fileName);
		}
	}

	private void initView() {
		mVideo = (FullScreenVideoView) findViewById(R.id.videoview);
		mPlayTime = (TextView) findViewById(R.id.play_time);
		mDurationTime = (TextView) findViewById(R.id.total_time);
		mPlay = (ImageView) findViewById(R.id.play_btn);
		mStart = (ImageView) findViewById(R.id.video_start);
		mLock = (ImageView) findViewById(R.id.video_lock);
		mVideoNext = (ImageView) findViewById(R.id.video_next);
		mSeekBar = (SeekBar) findViewById(R.id.seekbar);
		mProgressBar = (ProgressBar) findViewById(R.id.progressbar);
		mTopView = findViewById(R.id.top_layout);
		mUpperView = findViewById(R.id.upper_layout);
		mBottomView = findViewById(R.id.bottom_layout);
		mTvTitle = (TextView) findViewById(R.id.tv_title);
		mTvTime = (TextView) findViewById(R.id.tv_time);
		mTvNetType = (ImageView) findViewById(R.id.icon_net_type);
		mTvBattery = (ImageView) findViewById(R.id.icon_battery);
		mLoading = (ImageView) findViewById(R.id.video_loading);
		mLoadingAnim = AnimationUtils.loadAnimation(getContext(),
				R.anim.loading);
		mBrightnessView = (VideoBrightnessView) findViewById(R.id.view_brightness);
		mVoiceView = (VideoVoiceView) findViewById(R.id.view_voice);
		mRateView = (VideoRateView) findViewById(R.id.view_rate);
		RelativeLayout.LayoutParams paramsRate = (LayoutParams) mRateView.getLayoutParams();
		RelativeLayout.LayoutParams paramsBright = (LayoutParams) mBrightnessView.getLayoutParams();
		RelativeLayout.LayoutParams paramsVoice = (LayoutParams) mVoiceView.getLayoutParams();
		int height = AppEnv.SCREEN_HEIGHT > AppEnv.SCREEN_WIDTH ? AppEnv.SCREEN_WIDTH : AppEnv.SCREEN_HEIGHT;
		paramsRate.setMargins(0, height / 2 - DensityUtil.dip2px(getContext(), MARGIN_FROM_MIDDLE), 0, 0);
		paramsBright.setMargins(0, height / 2 - DensityUtil.dip2px(getContext(), MARGIN_FROM_MIDDLE), 0, 0);
		paramsVoice.setMargins(0, height / 2 - DensityUtil.dip2px(getContext(), MARGIN_FROM_MIDDLE), 0, 0);
		mRateView.setLayoutParams(paramsRate);
		mBrightnessView.setLayoutParams(paramsBright);
		mVoiceView.setLayoutParams(paramsVoice);
	}

	private void initListener() {
		mPlay.setOnClickListener(this);
		mStart.setOnClickListener(this);
		mLock.setOnClickListener(this);
		mVideoNext.setOnClickListener(this);
		VideoManager.getInstance().registerPlayControl(this);
		findViewById(R.id.common_img_back).setOnClickListener(this);
		findViewById(R.id.video_root).setOnTouchListener(mTouchListener);
		mBottomView.setOnTouchListener(mEmptyTouchListener);
		mUpperView.setOnTouchListener(mEmptyTouchListener);
		mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				mHandler.postDelayed(hideRunnable, HIDE_TIME);
				mVideo.seekTo(seekBar.getProgress());
				mTouchingSeekBar = false;
				play();
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				mHandler.removeCallbacks(hideRunnable);
				mLastProgress = seekBar.getProgress();
				mTouchingSeekBar = true;
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
										  boolean fromUser) {
				if (fromUser) {
					String formatTime = formatTime(progress);
					mPlayTime.setText(formatTime);
					boolean back = (mLastProgress >= progress);
					mRateView.setProgreess(back, formatTime);
					mLastProgress = progress;
				}
				mProgressBar.setProgress(progress);
			}
		});
		mVideo.setOnErrorListener(new OnErrorListener() {
			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				// TODO Auto-generated method stub
				SimpleLog.e(TAG, "MediaPlayer onError --- what = " + what);
				SimpleLog.e(TAG, "MediaPlayer onError --- extra = " + extra);
				if (what != 0 && what == 1) {
					showErrorToast();
				}
//				CustomToastUtil.getInstance().showDurationToast("视频播放出错!");
				ThreadManager.postDelayedTaskToUIHandler(new Runnable() {
					@Override
					public void run() {
						restartVideo();
					}
				}, 2000);
				return true;
			}
		});
		mVideo.setOnPreparedListener(mOnPreparedListener);
		mVideo.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				if (mp == null) {
					showErrorToast();
//					type = ConfigDefine.CRASH_TYPE_MP_NULL;
//					sendCrashUMStat(type);
					return;
				}
				onVideoCompletion(mp);
			}
		});
	}

	protected void restartVideo() {
		if (!isShown()) {
			return;
		}
		SimpleLog.e("", "restartVideo --- -- mVideoPath---- --- == " + mVideoPath);
		if (TextUtils.isEmpty(mVideoPath)) {
			return;
		}
		mVideo.suspend();
//		mVideo.setVideoPath(mVideoPath);
//		mVideo.resume();
		mVideo.setVideoPath(mVideoPath);
		mVideo.requestFocus();
//		mVideo.seekTo(playTime);
//		play();
	}

	private void initData() {
		mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
		if (AppEnv.SCREEN_WIDTH > AppEnv.SCREEN_HEIGHT) {
			width = AppEnv.SCREEN_WIDTH;
			height = AppEnv.SCREEN_HEIGHT;
		} else {
			width = AppEnv.SCREEN_HEIGHT;
			height = AppEnv.SCREEN_WIDTH;
		}
		threshold = DensityUtil.dip2px(getContext(), 8);
		updateTime();
		mLastBrightness = ((Activity) getContext()).getWindow()
				.getAttributes().screenBrightness;
		mIPlay = new IPlay() {
			@Override
			public void play() {
				mVideo.start();
				mPlay.setImageResource(R.drawable.video_stop);
				mStart.setVisibility(View.GONE);
			}
		};
	}

	private void initBrightness() {
		mBrightness = ConfigWrapper.get(CommonData.KEY_BRIGHTNESS, 0.5f);
		setBrightness(mBrightness);
	}

	private void updateTime() {
		String formatStr = "HH:mm";
		DateFormat formatter = new SimpleDateFormat(formatStr);
		String timeStr = formatter.format(new Date());
		mTvTime.setText(timeStr);
	}

	private void backward(float delataX) {
		if (mIsLocked) {
			return;
		}
		mIsSeeking = true;
		int current = mSeekBar.getProgress();
		int currentTime = (int) (current - delataX / width * 100 * 1000);
		SimpleLog.e(TAG, "backward : currentTime == " + currentTime);
		if (currentTime < 0) {
			currentTime = 0;
		} else if (currentTime > mVideo.getDuration()) {
			currentTime = mVideo.getDuration();
		}
		// mVideo.seekTo(currentTime);
		mSeekBar.setProgress(currentTime);
		mProgressBar.setProgress(currentTime);
		String formatTime = formatTime(currentTime);
		mPlayTime.setText(formatTime);
		mRateView.setProgreess(true, formatTime);
	}

	private void forward(float delataX) {
		if (mIsLocked) {
			return;
		}
		mIsSeeking = true;
		int current = mSeekBar.getProgress();
		// width : 100s
		// 20 : ?s
		SimpleLog.e(TAG, "forward : delataX == " + delataX);
		SimpleLog.e(TAG, "forward : current == " + current);
		SimpleLog.e(TAG, "forward : delataX/width == " + delataX / width);
		int currentTime = (int) (current + delataX / width * 100 * 1000);
		if (currentTime < 0) {
			currentTime = 0;
		} else if (currentTime > mVideo.getDuration()) {
			currentTime = mVideo.getDuration();
		}
		// mVideo.seekTo(currentTime);
		mSeekBar.setProgress(currentTime);
		mProgressBar.setProgress(currentTime);
		String formatTime = formatTime(currentTime);
		mPlayTime.setText(formatTime);
		mRateView.setProgreess(false, formatTime);
	}

	private void seekVideo(float delataX) {
		int current = mVideo.getCurrentPosition();
		int currentTime = (int) (current + delataX / width * 100 * 1000);
		if (currentTime < 0) {
			currentTime = 0;
		} else if (currentTime > mVideo.getDuration()) {
			currentTime = mVideo.getDuration();
		}
		mVideo.seekTo(currentTime);
	}

	private void volumeDown(float delatY) {
		if (mIsLocked) {
			return;
		}
		int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		int current = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		int down = (int) (delatY / height * max * 3);
		int volume = Math.max(current - down, 0);
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
		int transformatVolume = volume * 100 / max;
		mBrightnessView.setVisibility(View.GONE);
		mVoiceView.setProgreess(transformatVolume);
	}

	private void volumeUp(float delatY) {
		if (mIsLocked) {
			return;
		}
		int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		int current = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		int up = (int) ((delatY / height) * max * 3);
		int volume = Math.min(current + up, max);
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
		int transformatVolume = volume * 100 / max;
		mBrightnessView.setVisibility(View.GONE);
		mVoiceView.setProgreess(transformatVolume);
	}

	private void lightDown(float delatY) {
		if (mIsLocked) {
			return;
		}
		float bright = ((Activity) getContext()).getWindow().getAttributes().screenBrightness;
		if (bright < NIGHT_MODE_BRIGHTNESS) {
			setBrightness(NIGHT_MODE_BRIGHTNESS);
		} else {
			bright = bright - delatY / height / 2;
			if (bright < NIGHT_MODE_BRIGHTNESS) {
				bright = NIGHT_MODE_BRIGHTNESS;
			}
			setBrightness(bright);
		}
		mVoiceView.setVisibility(View.GONE);
		mBrightnessView.setProgreess((int) (bright * 100));
	}

	private void lightUp(float delatY) {
		if (mIsLocked) {
			return;
		}
		float bright = ((Activity) getContext()).getWindow().getAttributes().screenBrightness;
		if (bright < 1) {
			bright = bright + delatY / height / 2;
			if (bright > 1) {
				bright = 1;
			}
			setBrightness(bright);
		} else {
			setBrightness(1);
		}
		mVoiceView.setVisibility(View.GONE);
		mBrightnessView.setProgreess((int) (bright * 100));
	}

	protected void setBrightness(float f) {
		WindowManager.LayoutParams lp = ((Activity) getContext()).getWindow()
				.getAttributes();
		lp.screenBrightness = f;
		((Activity) getContext()).getWindow().setAttributes(lp);
		mBrightness = f;
	}

	private void playVideo(String videoUrl) {
		try {
			Uri.parse(videoUrl);
		} catch (Exception e) {
			showErrorToast();
//			type = ConfigDefine.CRASH_TYPE_VIDEOURL;
//			sendCrashUMStat(type);
			SimpleLog.e(e);
			return;
		}
		mVideoPath = videoUrl;
		VideoManager.getInstance().setVideoPlayerRunning(true);
		mLoading.setVisibility(View.VISIBLE);
		mLoading.startAnimation(mLoadingAnim);
		mVideo.setVideoPath(videoUrl);
		mVideo.requestFocus();
	}

	private void playVideoByFileName(String filePath) {
		try {
			mVideo.setVideoURI(Uri.parse("file://" + filePath));
		} catch (Exception e) {
			showErrorToast();
//			type = ConfigDefine.CRASH_TYPE_VIDEOURL;
//			sendCrashUMStat(type);
			SimpleLog.e(e);
			return;
		}
//		mVideoPath = videoUrl;
//		VideoManager.getInstance().setVideoPlayerRunning(true);
//		mLoading.setVisibility(View.VISIBLE);
//		mLoading.startAnimation(mLoadingAnim);
//		mVideo.setVideoPath(videoUrl);
		mVideo.requestFocus();
	}

	@SuppressLint("SimpleDateFormat")
	private String formatTime(long time) {
		float tmp = time / 1000.0f;
		time = (long) (Math.round(tmp) * 1000);
		String formatStr = "mm:ss";
		DateFormat formatter = new SimpleDateFormat(formatStr);
		String timeStr = formatter.format(new Date(time));
		if (time > 60 * 60 * 1000) {
			timeStr = time / (60 * 60 * 1000) + ":" + timeStr;
		}
		return timeStr;
	}

	/**
	 * 播放结束
	 */
	private void onVideoCompletion(MediaPlayer mp) {
		mp.setDisplay(null);
		mp.reset();
		mp.setDisplay(mVideo.getHolder());
		playTime = 0;
//		mVideoPath = null;
		mPlay.setImageResource(R.drawable.video_play);
//		mStart.setVisibility(View.VISIBLE);
		mVideo.pause();
		SimpleLog.e(TAG, "-------------   --- --  onCompletion");
		VideoManager.getInstance().onCompletion();
	}

	private void pause() {
		mVideo.pause();
		mPlay.setImageResource(R.drawable.video_play);
		mStart.setVisibility(View.VISIBLE);
	}

	private void play() {
		try {
			mVideo.start();
		} catch (Exception e) {
			SimpleLog.e(e);
			CustomToastUtils.getInstance().showTextToast("系统播放器挂了！！");
		}
		mPlay.setImageResource(R.drawable.video_stop);
		mStart.setVisibility(View.GONE);
	}

	/**
	 * 点击左侧的锁定屏幕按钮
	 */
	private void onLockClick() {
		mIsLocked = !mIsLocked;
		showControlView();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.play_btn:
			case R.id.video_start:
				if (mVideo.isPlaying()) {
					pause();
				} else {
					play();
				}
				break;
			case R.id.video_next:
				//	onVideoCompletion(mMediaPlayer);
				break;
			case R.id.video_lock:
				onLockClick();
				break;
			case R.id.common_img_back:
				if (mContext instanceof VideoActivity) {
					((VideoActivity) mContext).finish();
				} else {
					onBackPressed();
				}
				break;
			default:
				break;
		}
	}

	@Override
	public void onBackPressed() {
		if (mIsLocked) {
			showControlView();
			return;
		}
		onExit();
	}

	@Override
	public void onExit() {
		setVisibility(View.GONE);
		if (mVideo != null) {
			mVideo.stopPlayback();
		}
		finish();
	}

	public void onCustomVideoExit() {
		if (mVideo != null) {
			mVideo.stopPlayback();
		}
		finish();
	}

	private void showControlView() {
		mLock.setVisibility(View.VISIBLE);
		if (mIsLocked) {
			mTopView.setVisibility(View.GONE);
			mUpperView.setVisibility(View.VISIBLE);
			mBottomView.setVisibility(View.GONE);
			mProgressBar.setVisibility(View.VISIBLE);
			mLock.setImageResource(R.drawable.video_lock);
		} else {
			mProgressBar.setVisibility(View.GONE);
			updateTime();
			mUpperView.setVisibility(View.VISIBLE);
			mLock.setVisibility(View.VISIBLE);
			mLock.setImageResource(R.drawable.video_unlock);
			mTopView.setVisibility(View.VISIBLE);
			mUpperView.clearAnimation();
			Animation animation = AnimationUtils.loadAnimation(getContext(),
					R.anim.option_entry_from_top);
			mUpperView.startAnimation(animation);
			mBottomView.setVisibility(View.VISIBLE);
			mBottomView.clearAnimation();
			Animation animation1 = AnimationUtils.loadAnimation(getContext(),
					R.anim.option_entry_from_bottom);
			mBottomView.startAnimation(animation1);
		}
		mHandler.removeCallbacks(hideRunnable);
		mHandler.postDelayed(hideRunnable, HIDE_TIME);
	}

	private void showOrHide() {
		if (mIsLocked) {
			// 锁定状态
			mUpperView.setVisibility(View.GONE);
			mBottomView.setVisibility(View.GONE);
			if (mProgressBar.isShown()) {
				mLock.setVisibility(View.GONE);
				mProgressBar.setVisibility(View.GONE);
			} else {
				mLock.setVisibility(View.VISIBLE);
				mLock.setImageResource(R.drawable.video_lock);
				mProgressBar.setVisibility(View.VISIBLE);
				mUpperView.setVisibility(View.VISIBLE);
				mHandler.removeCallbacks(hideRunnable);
				mHandler.postDelayed(hideRunnable, HIDE_TIME);
			}
		} else {
			// 非锁定状态
			mProgressBar.setVisibility(View.GONE);
			if (mTopView.isShown()) {
				mLock.setVisibility(View.GONE);
				mUpperView.clearAnimation();
				mHandler.removeCallbacks(hideRunnable);
				Animation animation = AnimationUtils.loadAnimation(getContext(),
						R.anim.option_leave_from_top);
				animation.setAnimationListener(new AnimListenerAdapter() {
					@Override
					public void onAnimationEnd(Animation animation) {
						super.onAnimationEnd(animation);
						mUpperView.setVisibility(View.GONE);
					}
				});
				mUpperView.startAnimation(animation);
				mBottomView.clearAnimation();
				Animation animation1 = AnimationUtils.loadAnimation(getContext(),
						R.anim.option_leave_from_bottom);
				animation1.setAnimationListener(new AnimListenerAdapter() {
					@Override
					public void onAnimationEnd(Animation animation) {
						super.onAnimationEnd(animation);
						mBottomView.setVisibility(View.GONE);
					}
				});
				mBottomView.startAnimation(animation1);
			} else {
				updateTime();
				mLock.setVisibility(View.VISIBLE);
				mLock.setImageResource(R.drawable.video_unlock);
				mUpperView.setVisibility(View.VISIBLE);
				mUpperView.clearAnimation();
				Animation animation = AnimationUtils.loadAnimation(getContext(),
						R.anim.option_entry_from_top);
				mUpperView.startAnimation(animation);
				mBottomView.setVisibility(View.VISIBLE);
				mBottomView.clearAnimation();
				Animation animation1 = AnimationUtils.loadAnimation(getContext(),
						R.anim.option_entry_from_bottom);
				mBottomView.startAnimation(animation1);
				mHandler.removeCallbacks(hideRunnable);
				mHandler.postDelayed(hideRunnable, HIDE_TIME);
			}
		}
	}

	/**
	 * 暂停videoView
	 */
	@Override
	public void onPause() {
		if (mVideo != null) {
			playTime = mVideo.getCurrentPosition();
			mVideo.pause();
			mPlay.setImageResource(R.drawable.video_play);
			mStart.setVisibility(View.VISIBLE);
		}
		SimpleLog.e("", "-CustomVideoView------->>>>>-onPause()");
	}

	public void finish() {
		// 重置进度条和进度
		playTime = 0;
		mPlayTime.setText("00:00");
		mDurationTime.setText("00:00");
		mSeekBar.setProgress(0);
		mSeekBar.setSecondaryProgress(0);
//		mVideoPath = null;
		if (mVideo != null) {
			mVideo.stopPlayback();
			mVideo.setBackgroundResource(R.color.common_font_color_1);
		}
		if(mHandler!=null){
			mHandler.removeCallbacksAndMessages(null);
		}
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
		}
		ConfigWrapper.put(CommonData.KEY_BRIGHTNESS, mBrightness);
		ConfigWrapper.apply();
		VideoManager.getInstance().onDestory();
		VideoManager.getInstance().setVideoPlayerRunning(false);
		VideoManager.getInstance().unregisterPlayControl();
		ThreadManager.postTaskToUIHandler(new Runnable() {
			@Override
			public void run() {
				setBrightness(mLastBrightness);
				SysUtils.setFullScreen((Activity) getContext(), ConfigManager.getInstance().isFullScreen());
			}
		});
		// 横竖屏锁定判断
		boolean configScreenLock = ConfigManager.getInstance().isScreenLock();
		if (configScreenLock) {
			// 设置竖屏
			((Activity) getContext()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		} else {
			// 设置跟随系统
			((Activity) getContext()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
		}
	}

	public int getCurrentPosition() {
		return mVideo.getCurrentPosition();
	}

	public void resumePosition(final int stopPos) {
		ThreadManager.postDelayedTaskToUIHandler(new Runnable() {
			@Override
			public void run() {
//				mVideo.resume();
//				mVideo.seekTo(stopPos);
				playTime = stopPos;
			}
		}, 300);
		SimpleLog.e("", "-CustomVideoView------->>>>>-------------resumePosition()");
	}

	private void showErrorToast() {
//			CustomToastUtil.getInstance().showDurationToast(getContext().getResources().getString(R.string.play_video_error));
		Toast toast = Toast.makeText(JuziApp.getAppContext(), getContext().getResources().getString(R.string.play_download_video_error), Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}
}
