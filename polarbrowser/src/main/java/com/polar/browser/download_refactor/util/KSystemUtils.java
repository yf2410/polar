/**
 * @brief     Package com.polar.browser.utils
 * @author    zhouchenguang
 * @since     1.0.0.0
 * @version   1.0.0.0
 * @date      2012-12-23
 */

package com.polar.browser.download_refactor.util;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.util.DisplayMetrics;

import com.polar.browser.R;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//import android.os.storage.StorageVolume;

/** 
 * @file      KSystemUtils.java
 * @brief     This file is part of the Utils module of KBrowser project. \n
 *            This file serves as "java" source file that presents global 
 *            system definitions that would be required by all of
 *            the modules. \n
 *
 * @author    zhouchenguang
 * @since     1.0.0.0
 * @version   1.0.0.0
 * @date      2012-12-23
 *
 * \if TOSPLATFORM_CONFIDENTIAL_PROPRIETARY
 * ============================================================================\n
 *\n
 *           Copyright (c) 2012 zhouchenguang.  All Rights Reserved.\n
 *\n
 * ============================================================================\n
 *\n
 *                              Update History\n
 *\n
 * Author (Name[WorkID]) | Modification | Tracked Id | Description\n
 * --------------------- | ------------ | ---------- | ------------------------\n
 * zhouchenguang[7897]   |  2012-12-23  | <xxxxxxxx> | Initial Created.\n
 *\n
 * \endif
 *
 * <tt>
 *\n
 * Release History:\n
 *\n
 * Author (Name[WorkID]) | ModifyDate | Version | Description \n
 * --------------------- | ---------- | ------- | -----------------------------\n
 * zhouchenguang[7897]   | 2012-12-23 | 1.0.0.0 | Initial created. \n
 *\n
 * </tt>
 */
//=============================================================================
//                                  IMPORT PACKAGES
//=============================================================================

//=============================================================================
//                                 CLASS DEFINITIONS
//=============================================================================
/**
 * @class KSystemUtils.java
 * @brief Class that defines all system utils. \n
 * @author zhouchenguang
 * @since 1.0.0.0
 * @version 1.0.0.0
 * @date 2012-12-23
 * @par Applied: External
 */
public class KSystemUtils {

    // -------------------------------------------------------------------------
    // Public static members
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // Private static members
    // -------------------------------------------------------------------------
    /**
     * TAG
     */
    private static final String TAG = "KSystemUtils";
    /**
     * @brief Screen width in pixels. \n
     */
    private static int SCREEN_WIDTH = 480;
    /**
     * @brief Screen height in pixels. \n
     */
    private static int SCREEN_HEIGHT = 800;

    // -------------------------------------------------------------------------
    // Public static member methods
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * @brief Initialize system settings.
     * @par Sync (or) Async: This is a Synchronous function.
     * @return None \par Important Notes: - Notes: It will initialize the global
     *         variable SCREEN_WIDTH and SCREEN_HEIGHT. The values are assigned
     *         as device in vertical orientation. \n - Notes: It should be
     *         performed when the first activity is running. \n
     * @author zhouchenguang
     * @since 1.0.0.0
     * @version 1.0.0.0
     * @par Prospective Clients: External Classes
     */
    public static void initSysSettings(Activity activity) {
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        if (activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            SCREEN_WIDTH = dm.heightPixels;
            SCREEN_HEIGHT = dm.widthPixels;
        } else {
            SCREEN_WIDTH = dm.widthPixels;
            SCREEN_HEIGHT = dm.heightPixels;
        }
    }

    // -------------------------------------------------------------------------
    /**
     * @brief Get screen width.
     * @par Sync (or) Async: This is a Synchronous function.
     * @return Screen width in pixels. \n \par Important Notes: - Notes: It will
     *         return current screen width in pixels with assuming current
     *         device is placed in vertical orientation. \n
     * @author zhouchenguang
     * @since 1.0.0.0
     * @version 1.0.0.0
     * @par Prospective Clients: External Classes
     */
    public static int getScreenWidth() {
        return SCREEN_WIDTH;
    }

    // -------------------------------------------------------------------------
    /**
     * @brief Get screen height.
     * @par Sync (or) Async: This is a Synchronous function.
     * @return Screen height in pixels. \n \par Important Notes: - Notes: It
     *         will return current screen height in pixels with assuming current
     *         device is placed in vertical orientation. \n
     * @author zhouchenguang
     * @since 1.0.0.0
     * @version 1.0.0.0
     * @par Prospective Clients: External Classes
     */
    public static int getScreenHeight() {
        return SCREEN_HEIGHT;
    }

    // -------------------------------------------------------------------------
    /**
     * @brief Get android sdk version of current mobile.
     * @par Sync (or) Async: This is a Synchronous function.
     * @return sdk version in integer. \n
     * @author zhouchenguang
     * @since 1.0.0.0
     * @version 1.0.0.0
     * @par Prospective Clients: External Classes
     */
    public static int getSdkVersion() {
        return VERSION.SDK_INT;
    }

    /**
     * 由于线程竞争或进程竞争， 某些版本的android系统上 context.getFilesDir() 创建目录会失败，并返回null，
     * 为解决该问题， 当发现返回值为null时，try again。
     * 
     * @param context
     * @return files directory
     */
    public static File getFilesDir(Context context) {
        File filesDir = context.getFilesDir();
        return filesDir != null ? filesDir : context.getFilesDir();
    }

    // -------------------------------------------------------------------------
    /**
     * @brief Get internal sdcard path.
     * @par Sync (or) Async: This is a Synchronous function.
     * @return path for internal sdcard. \n
     * @author huangzongming
     * @since 1.0.0.0
     * @version 1.0.0.0
     * @par Prospective Clients: External Classes
     */
    public static String getInternalSdcardPath(Context context) {
        String path = null;
        if (getSdkVersion() >= 14) {
            StorageManager mStorageManager = null;
            mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
//            String[] storagePathList = mStorageManager.getVolumePaths();
            String[] storagePathList = getVolumePaths(mStorageManager);
            if (storagePathList != null) {
                if (storagePathList.length >= 1) {
                    if (checkSDCardMount(context, storagePathList[0]))
                        path = storagePathList[0];
                }
            }
        } else {
            // for lower than android 4.0 , still using /mnt/sdcard
            path = Environment.getExternalStorageDirectory().getAbsolutePath();
        }
        return path;
    }

    // -------------------------------------------------------------------------
    /**
     * @brief Get external sdcard path.
     * @par Sync (or) Async: This is a Synchronous function.
     * @return path for external sdcard. \n
     * @author huangzongming
     * @since 1.0.0.0
     * @version 1.0.0.0
     * @par Prospective Clients: External Classes
     */
    public static String getExternalSdcardPath(Context context) {
        String path = null;
        if (getSdkVersion() >= 14) {
            StorageManager mStorageManager = null;
            mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
//            String[] storagePathList = mStorageManager.getVolumePaths();
            String[] storagePathList = getVolumePaths(mStorageManager);
            if (storagePathList != null) {
                if (storagePathList.length >= 2) {
                    if (checkSDCardMount(context, storagePathList[1]))
                        path = storagePathList[1];
                }
            }
        } else {
            // for lower than android 4.0 , still using /mnt/sdcard
            path = Environment.getExternalStorageDirectory().getAbsolutePath();
        }
        return path;
    }

    /**
     * 一张SD卡的相关信息
     * 
     * @author caisenchuan
     */
    public static class SDCard {
        private int index;
        private String path;
        private boolean mount;
        private boolean removeable;

        public SDCard(int index, String path, boolean mount, boolean removeable) {
            this.index = index;
            this.path = path;
            this.mount = mount;
            this.removeable = removeable;
        }

        /**
         * 获取SD卡的名字
         */
        public String getName(Context context) {
            String ret = "";

            if (context != null) {
                if (!removeable) {
                    // 手机存储
                    if (index > 1) {
                        ret = String.format("%s%s",
                                context.getString(R.string.download_folder_phone), index);
                    } else {
                        ret = context.getString(R.string.download_folder_phone);
                    }
                } else {
                    // SD卡
                    if (index > 1) {
                        ret = String.format("%s%s",
                                context.getString(R.string.download_folder_sd), index);
                    } else {
                        ret = context.getString(R.string.download_folder_sd);
                    }
                }
            }

            return ret;
        }

        /**
         * @return the path
         */
        public String getPath() {
            return path;
        }

        /**
         * @return the mount
         */
        public boolean isMount() {
            return mount;
        }

        /**
         * @return the removeable
         */
        public boolean isRemoveable() {
            return removeable;
        }
    }

    /**
     * 读取所有SD卡的状态
     * 
     * @param context
     * @return
     * @author caisenchuan
     */

    public static List<SDCard> getAllSdcardState(Context context) {
        List<SDCard> ret = new ArrayList<SDCard>();
        if (context != null) {
            if (getSdkVersion() >= 14) {
                StorageManager mStorageManager = null;
                mStorageManager = (StorageManager) context
                        .getSystemService(Context.STORAGE_SERVICE);
//                StorageVolume[] storagePathList = mStorageManager.getVolumeList();
//                StorageVolume[] storagePathList = getVolumeList(mStorageManager);
//                int innerSDCardIndex = 1;
//                int outerSDCardIndex = 1;
//                if (storagePathList != null) {
//                    for (StorageVolume volume : storagePathList) {
//                        String path = volume.getPath();
//                        boolean mount = checkSDCardMount(context, path);
//                        boolean isRemoveable = volume.isRemovable();
//                        int index = 0;
//                        if (!isRemoveable) {
//                            // 手机存储
//                            index = innerSDCardIndex;
//                            innerSDCardIndex++;
//                        } else {
//                            // SD卡
//                            index = outerSDCardIndex;
//                            outerSDCardIndex++;
//                        }
//                        SDCard state = new SDCard(index, path, mount, isRemoveable);
//                        SimpleLog.d(TAG, String.format( "index : %s , path : %s , mount : %s , isRemoveable : %s",
//                                   index, path, mount, isRemoveable));
//                        ret.add(state);
//                    }
//                }
            } else {
                // for lower than android 4.0 , still using /mnt/sdcard
            }
        }
        return ret;
    }

    /*
    private static StorageVolume[] getVolumeList(StorageManager storageManager) {
    	try {
    		StorageVolume[] paths = (StorageVolume[]) storageManager.getClass().getMethod("getVolumeList", null).invoke(storageManager, null);
//			for (int i = 0; i < paths.length; i++) {
//				boolean canWrite = new File(paths[i]).canWrite();
//				SimpleLog.e("", "paths["+ i + "] == " + paths[i]);
//				
//				if (canWrite && !TextUtils.equals(externalStorageDirectory, paths[i])) {
//					return paths[i];
//				}
//			}
			return paths;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}*/
    
    private static String[] getVolumePaths(StorageManager storageManager) {
    	try {
    		String[] paths = (String[]) storageManager.getClass().getMethod("getVolumePaths", new Class<?>[]{}).invoke(storageManager, new Object[]{});
//			for (int i = 0; i < paths.length; i++) {
//				boolean canWrite = new File(paths[i]).canWrite();
//				SimpleLog.e("", "paths["+ i + "] == " + paths[i]);
//				
//				if (canWrite && !TextUtils.equals(externalStorageDirectory, paths[i])) {
//					return paths[i];
//				}
//			}
    		return paths;
    	} catch (IllegalAccessException e) {
    		e.printStackTrace();
    	} catch (IllegalArgumentException e) {
    		e.printStackTrace();
    	} catch (InvocationTargetException e) {
    		e.printStackTrace();
    	} catch (NoSuchMethodException e) {
    		e.printStackTrace();
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return null;
    }
    
    // -------------------------------------------------------------------------
    /**
     * @brief Check sdcard whether mounted.
     * @par Sync (or) Async: This is a Synchronous function.
     * @return true if sdcard been mounted \n
     * @author huangzongming
     * @since 1.0.0.0
     * @version 1.0.0.0
     * @par Prospective Clients: External Classes
     */
    public static boolean checkSDCardMount(Context context, String mountPoint) {
        if (mountPoint == null) {
            return false;
        }

        if (getSdkVersion() >= 14) {
            String state = null;
            StorageManager mStorageManager = null;
            mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
            try {
            	// 反射获取...
//                state = mStorageManager.getVolumeState(mountPoint);
            	state = (String) mStorageManager.getClass().getMethod("getVolumeState", String.class).invoke(mStorageManager, mountPoint);
            } catch (Exception e) {
                return false;
            }
            return Environment.MEDIA_MOUNTED.equals(state);
        } else {
            return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        }

    }

    // -------------------------------------------------------------------------
    public static boolean isClipDataFromCMB(Context context) {
        String app_name = context.getResources().getString(R.string.app_name);
        return checkClipDataLabel(context, app_name);
    }

    public static boolean checkClipDataLabel(Context context, String label) {
        ClipboardManager clp_mgr = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);

        ClipDescription dsp = clp_mgr.getPrimaryClipDescription();

        boolean matcher = true;
        if (dsp != null && label != null) {
            matcher = label.equals(dsp.getLabel());
        }

        return matcher;
    }

    public static boolean checkClipDataMimeType(Context context, String type) {
        ClipboardManager clp_mgr = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);

        ClipDescription dsp = clp_mgr.getPrimaryClipDescription();

        boolean matcher = true;
        if (dsp != null && type != null) {
            matcher = dsp.hasMimeType(type);
        }

        return matcher;
    }

    // set or get clipboard text
    public static boolean setTextToClipBoard(Context context, String text) {
        if (text == null || text.isEmpty())
            return false;
        
        String app_name = context.getResources().getString(R.string.app_name);
        ClipboardManager clp_mgr = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        
        ClipData clp_d = ClipData.newPlainText(app_name, text);
        
        clp_mgr.setPrimaryClip(clp_d);
        
        return true;
    }
    
    public static String getClipBoardText(Context context) {
        ClipboardManager clp_mgr = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        
        ClipData clp_d = clp_mgr.getPrimaryClip();
        if (clp_d == null || clp_d.getItemCount() == 0 
                || clp_d.getItemAt(0) == null 
                || clp_d.getItemAt(0).getText() == null)
            return null;
        
        return  clp_d.getItemAt(0).getText().toString();
    }
    
// TODO Remove unused code found by UCDetector
//     public static boolean setTextToClipBoard(Context context, List<String> list) {
//         if (list == null || list.isEmpty())
//             return false;
//         
//         String app_name = context.getResources().getString(R.string.app_name);
//         ClipboardManager clp_mgr = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
//         
//         ClipData clp_d = ClipData.newPlainText(app_name, list.get(0));
//         
//         for(int i = 1; i < list.size(); i ++) {
//             clp_d.addItem(new ClipData.Item(list.get(i)));
//         }
//         
//         clp_mgr.setPrimaryClip(clp_d);
//         return true;
//     }
    
// TODO Remove unused code found by UCDetector
//     public static boolean getClipBoardText(Context context, List<String> list) {
//         if (list == null)
//             return false;
//         
//         ClipboardManager clp_mgr = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
//        
//         ClipData clp_d = clp_mgr.getPrimaryClip();
//         
//         if (clp_d == null || clp_d.getItemCount() == 0)
//             return false;
//         
//         for(int i = 0; i < clp_d.getItemCount(); i ++) {
//             list.add(clp_d.getItemAt(i).getText().toString());
//         }
//         
//         return true;
//     }
    
    public static void resetClipBoardDataLabel(Context context) {
        String app_name = context.getResources().getString(R.string.app_name);
        markClipData(context, app_name, null);
    }

    public static void markClipData(Context context, String label, String type) {
        ClipboardManager clp_mgr = (ClipboardManager) context
                .getSystemService(Context.CLIPBOARD_SERVICE);

        final ClipData clp_d = clp_mgr.getPrimaryClip();

        if (clp_d == null || clp_d.getItemCount() == 0)
            return;

        ClipDescription clp_dsp = clp_d.getDescription();

        if ((clp_dsp.getLabel() != null && clp_dsp.getLabel().equals(label))
                || (type != null && clp_dsp.hasMimeType(type))) {
            return;
        }

        List<String> list = new ArrayList<String>();
        for (int i = 0; i < clp_dsp.getMimeTypeCount(); i++) {
            list.add(clp_dsp.getMimeType(i));
        }

        if (type != null) {
            list.add(type);
        }

        label = (clp_dsp.getLabel() != null) ? clp_dsp.getLabel().toString() : label;
        ClipDescription new_clp_dsp = new ClipDescription(label, list.toArray(new String[list.size()]));

        ClipData new_clp_d = new ClipData(new_clp_dsp, clp_d.getItemAt(0));

        for (int i = 1; i < clp_d.getItemCount(); i++) {
            new_clp_d.addItem(clp_d.getItemAt(i));
        }

        try {
            clp_mgr.setPrimaryClip(new_clp_d);
        } catch (Exception e) {

        }
    }
    
    // -------------------------------------------------------------------------
// TODO Remove unused code found by UCDetector
//     /**
//      * @brief Send string to others. Such as share page Url with others via
//      *        Message or Email, etc.
//      * @par Sync (or) Async: This is a Synchronous function.
//      * @param [IN] context Context to launch chooser activity. \n
//      * @param [IN] str The string which user want to share. \n
//      * @return None. \n
//      * @author zhouchenguang
//      * @since 1.0.0.0
//      * @version 1.0.0.0
//      * @par Prospective Clients: External Classes
//      */
//     public static void sendString(Context context, String str) {
//         Intent send = new Intent("android.intent.action.SEND_STRING");
//         send.putExtra(Intent.EXTRA_TEXT, str);
// 
//         try {
//             context.startActivity(Intent.createChooser(send,
//                     context.getText(R.string.sendText)));
//         } catch (android.content.ActivityNotFoundException e) {
//             KLog.e("sendString", "not activity handle android.intent.action.SEND_STRING", e);
//         }
//     }

    // -------------------------------------------------------------------------
    /**
     * @brief Create intent for add telephone number to contacts.
     * @par Sync (or) Async: This is a Synchronous function.
     * @param [IN] telNumber telephone number to be handled. \n
     * @return Intent. \n
     * @author zhouchenguang
     * @since 1.0.0.0
     * @version 1.0.0.0
     * @par Prospective Clients: External Classes
     */
    // public static Intent doAddTelContact(String telNumber) {
    // Intent newIntent = new Intent(Intent.ACTION_INSERT,
    // Contacts.CONTENT_URI);
    // newIntent.putExtra(Intents.Insert.PHONE, telNumber);
    // return newIntent;
    // }

    // -------------------------------------------------------------------------
    /**
     * @brief Create intent for add telephone number to contacts.
     * @par Sync (or) Async: This is a Synchronous function.
     * @param [IN] telNumber telephone number to be handled. \n
     * @return Intent. \n
     * @author zhouchenguang
     * @since 1.0.0.0
     * @version 1.0.0.0
     * @par Prospective Clients: External Classes
     */
    // public static Intent doAddEmaliToExistContact(String email) {
    // Intent newIntent = new Intent(Intent.ACTION_INSERT_OR_EDIT,
    // Contacts.CONTENT_URI);
    // newIntent.setType(Contacts.CONTENT_ITEM_TYPE);
    // newIntent.putExtra(Intents.Insert.EMAIL, email);
    // return newIntent;
    // }

    // -------------------------------------------------------------------------
    /**
     * @brief Create intent for add email to contacts.
     * @par Sync (or) Async: This is a Synchronous function.
     * @param [IN] email email address to be handled. \n
     * @return Intent. \n
     * @author zhouchenguang
     * @since 1.0.0.0
     * @version 1.0.0.0
     * @par Prospective Clients: External Classes
     */
    // public static Intent doAddEmailToContact(String email) {
    // Intent newIntent = new Intent(Intent.ACTION_INSERT,
    // Contacts.CONTENT_URI);
    // newIntent.putExtra(Intents.Insert.EMAIL, email);
    // return newIntent;
    // }

    // -------------------------------------------------------------------------
    /**
     * @brief Create intent for add telephone number to exist contact.
     * @par Sync (or) Async: This is a Synchronous function.
     * @param [IN] telNumber telephone number to be handled. \n
     * @return Intent. \n
     * @author zhouchenguang
     * @since 1.0.0.0
     * @version 1.0.0.0
     * @par Prospective Clients: External Classes
     */
    // public static Intent doAddTelToExistContact(String telNumber) {
    // Intent newIntent = new Intent(Intent.ACTION_INSERT_OR_EDIT,
    // Contacts.CONTENT_URI);
    // newIntent.setType(Contacts.CONTENT_ITEM_TYPE);
    // newIntent.putExtra(Intents.Insert.PHONE, telNumber);
    // return newIntent;
    // }

    // -------------------------------------------------------------------------
    /**
     * @brief Create intent for sent email.
     * @par Sync (or) Async: This is a Synchronous function.
     * @param [IN] email email address to be sent to. \n
     * @return Intent. \n
     * @author zhouchenguang
     * @since 1.0.0.0
     * @version 1.0.0.0
     * @par Prospective Clients: External Classes
     */
    // public static Intent doEmail(String email) {
    // Uri uri = Uri.parse("mailto:" + email);
    // return new Intent(Intent.ACTION_SENDTO, uri);
    // }

    // -------------------------------------------------------------------------
    /**
     * @brief Create intent for make a call.
     * @par Sync (or) Async: This is a Synchronous function.
     * @param [IN] telNumber telephone number to be called. \n
     * @return Intent. \n
     * @author zhouchenguang
     * @since 1.0.0.0
     * @version 1.0.0.0
     * @par Prospective Clients: External Classes
     */
    // public static Intent doCall(String telNumber) {
    // Uri uri = Uri.parse("tel:" + telNumber);
    // Intent intent = new Intent(Intent.ACTION_CALL);
    // intent.setData(uri);
    // return intent;
    // }

    // -------------------------------------------------------------------------
    /**
     * @brief Create intent for send sms.
     * @par Sync (or) Async: This is a Synchronous function.
     * @param [IN] telNumber telephone number to be sent to. \n
     * @return Intent. \n
     * @author zhouchenguang
     * @since 1.0.0.0
     * @version 1.0.0.0
     * @par Prospective Clients: External Classes
     */
    // public static Intent doSendSms(String telNumber) {
    // Uri uri = Uri.parse("smsto:" + telNumber);
    // return new Intent(Intent.ACTION_SENDTO, uri);
    // }

    public static ArrayList<ResolveInfo> getHasDefValueBrowser(Context context) {
        ArrayList<ResolveInfo> list_browsers = new ArrayList<ResolveInfo>();
        PackageManager packageManager = context.getPackageManager();

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setData(Uri.parse("http://vcapp.cn/"));

        List<ResolveInfo> activities = null;
        try {
            activities = packageManager.queryIntentActivities(intent, 0);
        } catch (Exception e) {
            activities = null;
            e.printStackTrace();
        }
        if (activities != null) {
            List<ComponentName> list_comp = new ArrayList<ComponentName>();
            List<IntentFilter> list_filter = new ArrayList<IntentFilter>();

            Iterator<ResolveInfo> activity_iter = activities.iterator();
            while (activity_iter.hasNext()) {

                ResolveInfo resolveInfo = activity_iter.next();
                packageManager.getPreferredActivities(list_filter, list_comp,
                        resolveInfo.activityInfo.packageName);

                Iterator<IntentFilter> filter_iter = list_filter.iterator();
                while (filter_iter.hasNext()) {
                    IntentFilter fil = filter_iter.next();
                    if ((fil.hasCategory(Intent.CATEGORY_BROWSABLE)
                            || fil.hasCategory(Intent.CATEGORY_DEFAULT))
                            && fil.hasDataScheme("http")) {
                        list_browsers.add(resolveInfo);
                    }
                }
            }
        }

        return list_browsers;
    }

    public static boolean isSelfDefaultBrowser(Context context) {
        boolean flag = false;

        ArrayList<ResolveInfo> arr = getHasDefValueBrowser(context);
        Iterator<ResolveInfo> iter = arr.iterator();
        while (iter.hasNext()) {
            ResolveInfo resolve = iter.next();
            if (resolve.activityInfo.packageName.equals("com.polar.browser")) {
                return true;
            }
        }

        return flag;
    }

    private static final String SCHEME = "package";
    /**
     * 调用系统InstalledAppDetails界面所需的Extra名称(用于Android 2.1及之前版本)
     */
    private static final String APP_PKG_NAME_21 = "com.android.settings.ApplicationPkgName";
    /**
     * 调用系统InstalledAppDetails界面所需的Extra名称(用于Android 2.2)
     */
    private static final String APP_PKG_NAME_22 = "pkg";

    private static final String ACTION_APPLICATION_DETAILS_SETTINGS_23 = "android.settings.APPLICATION_DETAILS_SETTINGS";
    /**
     * InstalledAppDetails所在包名
     */
    private static final String APP_DETAILS_PACKAGE_NAME = "com.android.settings";
    /**
     * InstalledAppDetails类名
     */
    private static final String APP_DETAILS_CLASS_NAME = "com.android.settings.InstalledAppDetails";

    /**
     * 调用系统InstalledAppDetails界面显示已安装应用程序的详细信息。 对于Android 2.3（Api Level
     * 9）以上，使用SDK提供的接口； 2.3以下，使用非公开的接口（查看InstalledAppDetails源码）。
     * 
     * @param packageName 应用程序的包名
     */
    public static Intent getPackageDetailsIntent(String packageName) {
        Intent intent = new Intent();
        int apiLevel = 0;
        try {
            apiLevel = VERSION.SDK_INT;
        } catch (Exception ex) {
        }
        if (apiLevel >= 9) { // 2.3（ApiLevel 9）以上，使用SDK提供的接口
            intent.setAction(ACTION_APPLICATION_DETAILS_SETTINGS_23);
            Uri uri = Uri.fromParts(SCHEME, packageName, null);
            intent.setData(uri);
        } else { // 2.3以下，使用非公开的接口（查看InstalledAppDetails源码）
            // 2.2和2.1中，InstalledAppDetails使用的APP_PKG_NAME不同。
            final String appPkgName = (apiLevel > 7 ? APP_PKG_NAME_22 : APP_PKG_NAME_21);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setClassName(APP_DETAILS_PACKAGE_NAME, APP_DETAILS_CLASS_NAME);
            intent.putExtra(appPkgName, packageName);
        }
        return intent;
    }

    /**
     * 在新窗口中打开浏览器
     * 
     * @param context 上下文
     * @param url 地址
     * @param from 会附带在Extra的Info.LOADURL_FROM_INTERNAL字段中
     */
//    public static void openNewWindowInKBrowser(Context context, String url, String from) {
//        openNewWindowInKBrowser(context, url, from, null, 0);
//    }

    /**
     * 在新窗口中打开浏览器
     * 
     * @param context 上下文
     * @param url 地址
     * @param from 会附带在Extra的Info.LOADURL_FROM_INTERNAL字段中
     * @param extra 要附带的其他参数，by caisenchuan
     */
//    public static void openNewWindowInKBrowser(Context context, String url, String from,
//            Bundle extra, int flags) {
//        Intent intent = new Intent();
//        intent.setAction(Intent.ACTION_VIEW);
//        if(!(context instanceof Activity)) {
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        }
//        Uri content_uri_browsers = Uri.parse(url);
//        if (from == Info.LOADURL_FROM_OPEN_GP) {
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        } else {
//            intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
//        }
//        intent.putExtra(Info.LOADURL_FROM_INTERNAL, from);
//        intent.setData(content_uri_browsers);
//        intent.setPackage(context.getPackageName());
//        // TODO 20160726 Fix me
////        intent.setClassName(AppEnv.PACKAGE_NAME, BrowserActivity.class.getName());
//
//        // 添加Bundle中的值
//        if (extra != null) {
//            intent.putExtras(extra);
//        }
//        if (flags != 0)
//            intent.putExtra(Info.LOADURL_FLAGS, flags);
//
//        context.startActivity(intent);
//    }

// TODO Remove unused code found by UCDetector
//     /**
//      * 读取系统的铃声设置
//      * 
//      * @param context
//      * @return true - 普通模式，有声音； false - 静音或者震动，无声音；
//      * @author caisenchuan
//      */
//     public static boolean getDeviceRingerState(Context context) {
//         boolean ret = false;
// 
//         if (context != null) {
//             AudioManager mAudioManager = (AudioManager) context
//                     .getSystemService(Context.AUDIO_SERVICE);
//             int state = mAudioManager.getRingerMode();
//             KLog.d(TAG, "ringer mode : " + state);
//             switch (state) {
//                 case AudioManager.RINGER_MODE_SILENT:
//                 case AudioManager.RINGER_MODE_VIBRATE:
//                     ret = false;
//                     break;
// 
//                 case AudioManager.RINGER_MODE_NORMAL:
//                 default:
//                     ret = true;
//                     break;
//             }
//         }
// 
//         return ret;
//     }

    /**
     * 清除所有通知
     * 
     * @author caisenchuan
     */
    public static void clearAllNotification(Context context) {
        NotificationManager manager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancelAll();
    }
    
    
    // 判断APK是否安装
    // *** 如果安装了，但是已经被系统停用，也被认为是没有安装 ***
    public static boolean isAPPInstalled(Context context, String pkg_name) {
        PackageManager pm = context.getPackageManager();
        boolean installed =false;
        try {
            PackageInfo info = pm.getPackageInfo(pkg_name,PackageManager.GET_ACTIVITIES);
            if (info != null)
                installed = info.applicationInfo.enabled;
            
        } catch(PackageManager.NameNotFoundException e) {
            installed =false;
        } catch(RuntimeException e) {
            installed =false;
        }
        return installed;
    }
    
    public static boolean isAPPInstalled(Context context, String[] pkg_name) {
        for (String pkg : pkg_name) {
            if (isAPPInstalled(context, pkg)) {
                return true;
            }
        }
        
        return false;
    }
    
    public static boolean isSdkVersionJellyBean() {
        return getSdkVersion() >= Build.VERSION_CODES.JELLY_BEAN;
    }

    public static boolean isSDKVersionKitKat() {
        return getSdkVersion() >= Build.VERSION_CODES.KITKAT;
    }

// TODO Remove unused code found by UCDetector
//     public static int getStatusBarHeight(Context context){
//         Class<?> c = null;
//         Object obj = null;
//         Field field = null;
//         int x = 0, statusBarHeight = 0;
//         try {
//             c = Class.forName("com.android.internal.R$dimen");
//             obj = c.newInstance();
//             field = c.getField("status_bar_height");
//             x = Integer.parseInt(field.get(obj).toString());
//             statusBarHeight = context.getResources().getDimensionPixelSize(x);
//         } catch (Exception e1) {
//             e1.printStackTrace();
//         }
//         return statusBarHeight;
//     }
    

    /**
     * @Note: 获取状态栏的高度
     */
    public static int getStatusBarHeight(Activity activity) {
        int height = 0;
        do {
            if (activity == null)
                break;
            if (activity.getWindow() == null)
                break;
            if (activity.getWindow().getDecorView() == null) 
                break;
            Rect rect = new Rect();
            activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
            height = rect.top;
        } while (false);

        return height;
    }
    
    public static List<ResolveInfo> getResponseApps(Context context, Intent intent) {
        List<ResolveInfo> apps = new ArrayList<ResolveInfo>();

        PackageManager pManager = context.getPackageManager();
        apps = pManager.queryIntentActivities(intent,
                PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
        return apps;
    }

// TODO Remove unused code found by UCDetector
//     /**
//      * 调用系统发邮件
//      * 
//      * @param activity
//      * @param mailto[]
//      * @param subject
//      * @param body
//      */
//     public static void sendMail(Context activity, String[] mailto,
//             String subject, String body) {
//         Intent email = new Intent(Intent.ACTION_SEND);
//         email.setType("text/plain");
//         email.putExtra(Intent.EXTRA_EMAIL, mailto);
//         email.putExtra(Intent.EXTRA_SUBJECT, subject);
//         email.putExtra(Intent.EXTRA_TEXT, body);
//         activity.startActivity(email);
//     }
    
    // gpu info
// TODO Remove unused code found by UCDetector
//     public static boolean isEGLContextOK() {
//         return !((EGL10) EGLContext.getEGL()).eglGetCurrentContext().equals(
//                 EGL10.EGL_NO_CONTEXT);
//     }
    
// TODO Remove unused code found by UCDetector
//     public static int getTextureMaxSize(GL10 gl) {
//         int[] max = new int[1];
//         gl.glGetIntegerv(GL10.GL_MAX_TEXTURE_SIZE, max, 0); 
//         return max[0];
//     }
    
// TODO Remove unused code found by UCDetector
//     public static boolean checkIfContextSupportsNPOT(GL10 gl) {
//         return checkIfContextSupportsExtension(gl, "GL_OES_texture_npot");
//     }
    
// TODO Remove unused code found by UCDetector
//     public static boolean checkIfContextSupportsFrameBufferObject(GL10 gl) {
//         return checkIfContextSupportsExtension(gl, "GL_OES_framebuffer_object");
//     }
    
// TODO Remove unused code found by UCDetector
//     public static boolean checkIfContextSupportsExtension(GL10 gl, String extension) {
//         String extensions = " " + gl.glGetString(GL10.GL_EXTENSIONS) + " ";
//         return extensions.indexOf(" " + extension + " ") >= 0;
//     }
}
