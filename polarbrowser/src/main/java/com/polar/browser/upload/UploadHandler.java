/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.polar.browser.upload;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.widget.AdapterView;

import com.polar.browser.JuziApp;
import com.polar.browser.R;
import com.polar.browser.common.data.GoogleConfigDefine;
import com.polar.browser.statistics.Statistics;
import com.polar.browser.utils.FileUtils;

import java.io.File;
import java.util.List;

public class UploadHandler extends IUploadHandler {

    public static final int FILE_SELECTED = 12;

    private ValueCallback<Uri> mUploadMessage;
    /** 照相机拍照 **/
    private String mCameraFilePath;
    /** 摄像机录制的MP4 **/
    private String mCamcorderFilePath;
    private boolean mCaughtActivityNotFoundException;
    private Activity mActivity;
    private ValueCallback<Uri> mUploadMsg;
    private ValueCallback<Uri[]> mUploadMsgArray;
    private String mAcceptType;
    private String mCapture;
    private WebChromeClient.FileChooserParams mFileChooserParams;

    public UploadHandler(Activity activity, ValueCallback<Uri> uploadMsg, String acceptType,
            String capture) {
        mActivity = activity;
        mAcceptType = acceptType;
        mCapture = capture;
    }

    public UploadHandler(Activity activity, ValueCallback<Uri[]> uploadMsg, WebChromeClient.FileChooserParams fileChooserParams) {
        mActivity = activity;
        mFileChooserParams = fileChooserParams;
    }


    public void onResult(int resultCode, Intent intent) {
        if (resultCode == Activity.RESULT_CANCELED && mCaughtActivityNotFoundException) {
            // Couldn't resolve an activity, we are going to try again so skip
            // this result.
            mCaughtActivityNotFoundException = false;
            return;
        }

        Uri result = intent == null || resultCode != Activity.RESULT_OK ? null
                : intent.getData();

        // As we ask the camera to save the result of the user taking
        // a picture, the camera application does not return anything other
        // than RESULT_OK. So we need to check whether the file we expected
        // was written to disk in the in the case that we
        // did not get an intent returned but did get a RESULT_OK. If it was,
        // we assume that this result has came back from the camera.
        if (result == null && resultCode == Activity.RESULT_OK) {
            File cameraFile = null;
            if (!TextUtils.isEmpty(mCameraFilePath)) {
                cameraFile = new File(mCameraFilePath);
            }
            File camcorderFile = null;
            if (!TextUtils.isEmpty(mCamcorderFilePath)) {
                camcorderFile = new File(mCamcorderFilePath);
            }
            if (cameraFile != null && cameraFile.exists()) {
                result = Uri.fromFile(cameraFile);
                // Broadcast to the media scanner that we have a new photo
                // so it will be added into the gallery for the user.
                mActivity.sendBroadcast(
                        new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, result));
            } else if (camcorderFile != null && camcorderFile.exists()) {
                result = Uri.fromFile(camcorderFile);
                // Broadcast to the media scanner that we have a new photo
                // so it will be added into the gallery for the user.
                mActivity.sendBroadcast(
                        new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, result));
            }
        }

        if (mUploadMessage != null) {
            mUploadMessage.onReceiveValue(result);
        } else if (mUploadMsgArray != null) {
            if (result != null) {
                mUploadMsgArray.onReceiveValue(new Uri[]{result});

//                String path = FileUtils.getPath(mActivity, result);
//                if (TextUtils.isEmpty(path)) {
//                    mUploadMsgArray.onReceiveValue(null);
//                    return;
//                }
//                Uri uri = Uri.fromFile(new File(path));
//                mUploadMsgArray.onReceiveValue(new Uri[]{uri});
            } else {
                mUploadMsgArray.onReceiveValue(null);
            }
        }
        mCaughtActivityNotFoundException = false;
    }

    public void openFileChooser(ValueCallback<Uri[]> uploadMsg, WebChromeClient.FileChooserParams fileChooserParams) {
        final String imageMimeType = "image/*";
        final String videoMimeType = "video/*";
        final String audioMimeType = "audio/*";
        final String mediaSourceKey = "capture";
        final String mediaSourceValueCamera = "camera";
        final String mediaSourceValueFileSystem = "filesystem";
        final String mediaSourceValueCamcorder = "camcorder";
        final String mediaSourceValueMicrophone = "microphone";

        // According to the spec, media source can be 'filesystem' or 'camera'
        // or 'camcorder'
        // or 'microphone' and the default value should be 'filesystem'.
        String mediaSource = mediaSourceValueFileSystem;

        if (mUploadMsgArray != null) {
            // Already a file picker operation in progress.
            return;
        }

        mUploadMsgArray = uploadMsg;

        // Parse the accept type.
        String params[] = fileChooserParams.getAcceptTypes();
        String mimeType = params[0];

        if (fileChooserParams.isCaptureEnabled()) {
            // To maintain backwards compatibility with the previous
            // implementation
            // of the media capture API, if the value of the 'capture' attribute
            // is
            // "filesystem", we should examine the accept-type for a MIME type
            // that
            // may specify a different capture value.
            for (String p : params) {
                String[] keyValue = p.split("=");
                if (keyValue.length == 2) {
                    // Process key=value parameters.
                    if (mediaSourceKey.equals(keyValue[0])) {
                        mediaSource = keyValue[1];
                    }
                }
            }
        }

        // Ensure it is not still set from a previous upload.
        mCameraFilePath = null;
        mCamcorderFilePath = null;

        if (mimeType.equals(imageMimeType)) {
            if (mediaSource.equals(mediaSourceValueCamera)) {
                // Specified 'image/*' and requested the camera, so go ahead and
                // launch the
                // camera directly.
                startActivity(createCameraIntent());
                return;
            } else {
                // Specified just 'image/*', capture=filesystem, or an invalid
                // capture parameter.
                // In all these cases we show a traditional picker filetered on
                // accept type
                // so launch an intent for both the Camera and image/* OPENABLE.

                // 20160909 接管系统的图片选择Intent
//                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                intent.addCategory(Intent.CATEGORY_OPENABLE);
//                intent.setType("image/*");
//                showIntentList(intent, imageMimeType);

//                Intent chooser = createChooserIntent(createCameraIntent());
//                chooser.putExtra(Intent.EXTRA_INTENT, createOpenableIntent(imageMimeType));
//                startActivity(chooser);
//                printSupportIntent(chooser);

                showIntentList(mimeType);

                return;
            }
        } else if (mimeType.equals(videoMimeType)) {
            if (mediaSource.equals(mediaSourceValueCamcorder)) {
                // Specified 'video/*' and requested the camcorder, so go ahead
                // and launch the
                // camcorder directly.
                startActivity(createCamcorderIntent());
                return;
            } else {
                // Specified just 'video/*', capture=filesystem or an invalid
                // capture parameter.
                // In all these cases we show an intent for the traditional file
                // picker, filtered
                // on accept type so launch an intent for both camcorder and
                // video/* OPENABLE.

//                Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
//                Intent intent = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

//                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                intent.addCategory(Intent.CATEGORY_OPENABLE);

//                intent.setType("video/*");
//                showIntentList(intent, videoMimeType);

//                showIntentList(createOpenableIntent(mimeType), videoMimeType);

//                Intent chooser = createChooserIntent(createCamcorderIntent());
//                chooser.putExtra(Intent.EXTRA_INTENT, createOpenableIntent(videoMimeType));
//                startActivity(chooser);

                showIntentList(mimeType);

                return;
            }
        } else if (mimeType.equals(audioMimeType)) {
            if (mediaSource.equals(mediaSourceValueMicrophone)) {
                // Specified 'audio/*' and requested microphone, so go ahead and
                // launch the sound
                // recorder.
                startActivity(createSoundRecorderIntent());
                return;
            } else {
                // Specified just 'audio/*', capture=filesystem of an invalid
                // capture parameter.
                // In all these cases so go ahead and launch an intent for both
                // the sound
                // recorder and audio/* OPENABLE.
                Intent chooser = createChooserIntent(createSoundRecorderIntent());
                chooser.putExtra(Intent.EXTRA_INTENT, createOpenableIntent(audioMimeType));
                startActivity(chooser);
                return;
            }
        } else if (mimeType.equals("image/*,video/*")) {
            // facebook  ---  "image/*,video/*"
            showIntentList(mimeType);
            return;
        }

        // No special handling based on the accept type was necessary, so
        // trigger the default
        // file upload chooser.
        startActivity(createDefaultOpenableIntent());
    }

    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {

        final String imageMimeType = "image/*";
        final String videoMimeType = "video/*";
        final String audioMimeType = "audio/*";
        final String mediaSourceKey = "capture";
        final String mediaSourceValueCamera = "camera";
        final String mediaSourceValueFileSystem = "filesystem";
        final String mediaSourceValueCamcorder = "camcorder";
        final String mediaSourceValueMicrophone = "microphone";

        // According to the spec, media source can be 'filesystem' or 'camera'
        // or 'camcorder'
        // or 'microphone' and the default value should be 'filesystem'.
        String mediaSource = mediaSourceValueFileSystem;

        if (mUploadMessage != null) {
            // Already a file picker operation in progress.
            return;
        }

        mUploadMessage = uploadMsg;

        // Parse the accept type.
        String params[] = acceptType.split(";");
        String mimeType = params[0];

        if (capture != null && capture.length() > 0) {
            mediaSource = capture;
        }

        if (mediaSource.equals(mediaSourceValueFileSystem)) {
            // To maintain backwards compatibility with the previous
            // implementation
            // of the media capture API, if the value of the 'capture' attribute
            // is
            // "filesystem", we should examine the accept-type for a MIME type
            // that
            // may specify a different capture value.
            for (String p : params) {
                String[] keyValue = p.split("=");
                if (keyValue.length == 2) {
                    // Process key=value parameters.
                    if (mediaSourceKey.equals(keyValue[0])) {
                        mediaSource = keyValue[1];
                    }
                }
            }
        }

        // Ensure it is not still set from a previous upload.
        mCameraFilePath = null;
        mCamcorderFilePath = null;

        if (mimeType.equals(imageMimeType)) {
            if (mediaSource.equals(mediaSourceValueCamera)) {
                // Specified 'image/*' and requested the camera, so go ahead and
                // launch the
                // camera directly.
                startActivity(createCameraIntent());
                return;
            } else {
                // Specified just 'image/*', capture=filesystem, or an invalid
                // capture parameter.
                // In all these cases we show a traditional picker filetered on
                // accept type
                // so launch an intent for both the Camera and image/* OPENABLE.
//                Intent chooser = createChooserIntent(createCameraIntent());
//                chooser.putExtra(Intent.EXTRA_INTENT, createOpenableIntent(imageMimeType));
//                startActivity(chooser);
                // TODO: 17/3/17
                showIntentList(mimeType);
                return;
            }
        } else if (mimeType.equals(videoMimeType)) {
            if (mediaSource.equals(mediaSourceValueCamcorder)) {
                // Specified 'video/*' and requested the camcorder, so go ahead
                // and launch the
                // camcorder directly.
                startActivity(createCamcorderIntent());
                return;
            } else {
                // Specified just 'video/*', capture=filesystem or an invalid
                // capture parameter.
                // In all these cases we show an intent for the traditional file
                // picker, filtered
                // on accept type so launch an intent for both camcorder and
                // video/* OPENABLE.
//                Intent chooser = createChooserIntent(createCamcorderIntent());
//                chooser.putExtra(Intent.EXTRA_INTENT, createOpenableIntent(videoMimeType));
//                startActivity(chooser);
                // TODO: 17/3/17
                showIntentList(mimeType);
                return;
            }
        } else if (mimeType.equals(audioMimeType)) {
            if (mediaSource.equals(mediaSourceValueMicrophone)) {
                // Specified 'audio/*' and requested microphone, so go ahead and
                // launch the sound
                // recorder.
                startActivity(createSoundRecorderIntent());
                return;
            } else {
                // Specified just 'audio/*', capture=filesystem of an invalid
                // capture parameter.
                // In all these cases so go ahead and launch an intent for both
                // the sound
                // recorder and audio/* OPENABLE.
                Intent chooser = createChooserIntent(createSoundRecorderIntent());
                chooser.putExtra(Intent.EXTRA_INTENT, createOpenableIntent(audioMimeType));
                startActivity(chooser);
                return;
            }
        } else if (mimeType.equals("image/*,video/*")) {
            // facebook  ---  "image/*,video/*"
            showIntentList(mimeType);
            return;
        }

        // No special handling based on the accept type was necessary, so
        // trigger the default
        // file upload chooser.
        startActivity(createDefaultOpenableIntent());
    }

    private void startActivity(Intent intent) {
        try {
            mActivity.startActivityForResult(intent, FILE_SELECTED);
        } catch (ActivityNotFoundException e) {
            // No installed app was able to handle the intent that
            // we sent, so fallback to the default file upload control.
            try {
                mCaughtActivityNotFoundException = true;
                mActivity.startActivityForResult(createDefaultOpenableIntent(),
                        FILE_SELECTED);
            } catch (ActivityNotFoundException e2) {
                // Nothing can return us a file, so file upload is effectively
                // disabled.
                // Toast.makeText(mController), R.string.uploads_disabled,
                // Toast.LENGTH_LONG).show();
            }
        }
    }

    private Intent createDefaultOpenableIntent() {
        // Create and return a chooser with the default OPENABLE
        // actions including the camera, camcorder and sound
        // recorder where available.
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");

        Intent chooser = createChooserIntent(createCameraIntent(), createCamcorderIntent(),
                createSoundRecorderIntent());
        chooser.putExtra(Intent.EXTRA_INTENT, i);
        return chooser;
    }

    private Intent createChooserIntent(Intent... intents) {
        Intent chooser = new Intent(Intent.ACTION_CHOOSER);
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents);
        chooser.putExtra(Intent.EXTRA_TITLE,
                mActivity.getResources()
                        .getString(R.string.choose_upload));
        return chooser;
    }

    private Intent createOpenableIntent(String type) {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType(type);
        return i;
    }

    private Intent createCameraIntent() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File externalDataDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM);
        File cameraDataDir = new File(externalDataDir.getAbsolutePath() +
                File.separator + "browser-photos");
        cameraDataDir.mkdirs();
        mCameraFilePath = cameraDataDir.getAbsolutePath() + File.separator +
                System.currentTimeMillis() + ".jpg";
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(mCameraFilePath)));
        return cameraIntent;
    }

    private Intent createCamcorderIntent() {
        Intent camcorderIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        File externalDataDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM);
        File cameraDataDir = new File(externalDataDir.getAbsolutePath() +
                File.separator + "browser-photos");
        cameraDataDir.mkdirs();
        mCamcorderFilePath = cameraDataDir.getAbsolutePath() + File.separator +
                System.currentTimeMillis() + ".mp4";
        camcorderIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(mCamcorderFilePath)));

        return camcorderIntent;
    }

    private Intent createSoundRecorderIntent() {
        return new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
    }

    public void dimiss() {
        onResult(Activity.RESULT_CANCELED, null);
    }

    public void onActivityResult(int requestCode, int resultCode,
            Intent intent) {
        switch (requestCode) {
            case FILE_SELECTED:
                // Chose a file from the file picker.
                onResult(resultCode, intent);
                break;
            default:
                break;
        }
    }

    /**
     * 显示处理页面中文件选取的列表对话框
     * @param intent
     */
    private void showIntentList(Intent intent, final String mimeType) {
        PackageManager pManager = JuziApp.getAppContext().getPackageManager();
        final List<ResolveInfo> resolveList = pManager.queryIntentActivities(intent, 0);
        if (resolveList == null || resolveList.isEmpty()) {
            return;
        }
        // 添加相机项
        boolean hasCamera = false;
        List<ResolveInfo> cameraIntentList = null;
        if (TextUtils.equals(mimeType, "image/*")) {
            cameraIntentList = pManager.queryIntentActivities(createCameraIntent(), 0);
        } else if (TextUtils.equals(mimeType, "video/*")) {
            cameraIntentList = pManager.queryIntentActivities(createCamcorderIntent(), 0);
        }
        if (cameraIntentList != null && cameraIntentList.size() > 0) {
            resolveList.add(0, cameraIntentList.get(0));
            hasCamera = true;
        }
        final boolean hasCameraFinal = hasCamera;
        // 添加其他图片上传选项
        final IntentListDialog dialog = new IntentListDialog(mActivity);
        String[] nameArray = new String[resolveList.size()];
        ResolveInfo info;
        for (int i = 0; i < resolveList.size(); i++) {
            info = resolveList.get(i);
            nameArray[i] = info.loadLabel(pManager).toString();
        }
        dialog.setItems(nameArray);
//        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (position == 0 && hasCameraFinal) {
                    handleCameraItemClick(resolveList.get(position), mimeType);
                    return;
                }
                ResolveInfo info = resolveList.get(position);
                startActivity(info, mimeType);
            }
        });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (dialog.isDismissByBackPress()) {
                    FileUtils.dismissFileUploadHandler();
                    FileUtils.resetFileUploadHandler();
                }
            }
        });
        dialog.show();
    }

    private void startActivity(ResolveInfo info, String mimeType) {
        String pkg = info.activityInfo.packageName;
        String cls = info.activityInfo.name;
        Intent intent = createOpenableIntent(mimeType);
        intent.setComponent(new ComponentName(pkg, cls));
        startActivity(intent);
    }

    private void handleCameraItemClick(ResolveInfo info, String mimeType) {
        // 6.0相机权限申请。。。好像不用申请,也可以,暂时不做申请
        if (TextUtils.equals(mimeType, "image/*")) {
            startActivity(createCameraIntent());
        } else if (TextUtils.equals(mimeType, "video/*")) {
            startActivity(createCamcorderIntent());
        } else if (mimeType.equals("image/*,video/*")) {
            // facebook 打开相机 ---  "image/*,video/*"
            startActivity(createCameraIntent());
            return;
        }
    }

    private void showIntentList(final String mimeType) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType(mimeType);

        PackageManager pManager = JuziApp.getAppContext().getPackageManager();
        final List<ResolveInfo> resolveList = pManager.queryIntentActivities(intent, 0);
        if (resolveList == null || resolveList.isEmpty()) {
            // 没有可选的？....
            return;
        }

        // 获取相册项
        ResolveInfo album = resolveList.get(0);

        // 添加相机项
        ResolveInfo camera = null;
        boolean hasCamera = false;
        List<ResolveInfo> cameraIntentList = null;
        if (TextUtils.equals(mimeType, "image/*")) {
            cameraIntentList = pManager.queryIntentActivities(createCameraIntent(), 0);
        } else if (TextUtils.equals(mimeType, "video/*")) {
            cameraIntentList = pManager.queryIntentActivities(createCamcorderIntent(), 0);
        } else if (TextUtils.equals(mimeType, "image/*,video/*")) {
            cameraIntentList = pManager.queryIntentActivities(createCameraIntent(), 0);
        }
        if (cameraIntentList != null && cameraIntentList.size() > 0) {
            camera = cameraIntentList.get(0);
            hasCamera = true;
        }

        String[] nameArray;
        if (hasCamera) {
            nameArray = new String[]{mActivity.getString(R.string.album), mActivity.getString(R.string.camera), mActivity.getString(R.string.cancel)};
        } else {
            nameArray = new String[]{mActivity.getString(R.string.album), mActivity.getString(R.string.cancel)};
        }

        final boolean finalHasCamera = hasCamera;
        // 添加其他图片上传选项
        final IntentListDialog dialog = new IntentListDialog(mActivity);
        dialog.setItems(nameArray);
//        dialog.setCanceledOnTouchOutside(false);
        final ResolveInfo finalAlbum = album;
        final ResolveInfo finalCamera = camera;
        dialog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (position == 0) {
                    startActivity(finalAlbum, mimeType);
                    Statistics.sendOnceStatistics(GoogleConfigDefine.WEBPAGE, GoogleConfigDefine.WEBPAGE_ALBUM);
                } else if (position == 1) {
                    if (finalHasCamera) {
                        handleCameraItemClick(finalCamera, mimeType);
                        Statistics.sendOnceStatistics(GoogleConfigDefine.WEBPAGE, GoogleConfigDefine.WEBPAGE_CAMERA);
                    } else {
                        dialog.dismiss();
                    }
                } else if (position == 2) {
                    dialog.dismiss();
                }
            }
        });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (dialog.isDismissByBackPress()) {
                    FileUtils.dismissFileUploadHandler();
                    FileUtils.resetFileUploadHandler();
                }
            }
        });
        dialog.show();
        Statistics.sendOnceStatistics(GoogleConfigDefine.WEBPAGE, GoogleConfigDefine.WEBPAGE_PUBLISH_MENU_OPEN);
    }

}
