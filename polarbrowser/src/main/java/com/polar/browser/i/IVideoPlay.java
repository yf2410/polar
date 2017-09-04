package com.polar.browser.i;

/**
 * 与视频模块通信接口
 *
 * @author dpk
 */
public interface IVideoPlay {
	// 接收视频播放完毕的通知
	public void onNotifyPlayEnd();

	// 通知视频播放器销毁
	public void onNotifyVideoPlayerDestroy();
}
