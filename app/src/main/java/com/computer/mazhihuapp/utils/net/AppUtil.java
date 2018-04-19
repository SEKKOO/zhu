package com.computer.mazhihuapp.utils.net;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;

import com.computer.mazhihuapp.R;
import com.computer.mazhihuapp.activity.DownloadActivity;
import com.computer.mazhihuapp.application.myApplication;

import java.util.List;


public class AppUtil {


	public static String getVersionName(Context context) {

		PackageManager packageManager = context.getPackageManager();

		String version = "";
		try {
			PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
			version = packInfo.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return version;

	}

	public static int getVersionCode(Context context) {
		PackageManager packageManager = context.getPackageManager();
		int versionCode = 0;
		try {
			PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
			versionCode = packInfo.versionCode;
		} catch (NameNotFoundException e) {
			versionCode = 12;
			e.printStackTrace();
		}
		return versionCode;

	}


	public static String getChannel(Context context) {
		String channel = "bbliveBYD";
		try {
			ApplicationInfo info = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
			if (info != null && info.metaData != null) {
				String metaData = info.metaData.getString("UMENG_CHANNEL");
				if (!StringUtil.isBlank(metaData)) {
					channel = metaData;
				}
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return channel;
	}


	public static boolean hasShortcut(Context context) {
		boolean isInstallShortcut = false;
		final ContentResolver cr = context.getContentResolver();

		int versionLevel = android.os.Build.VERSION.SDK_INT;
		String AUTHORITY = "com.android.launcher2.settings";

		if (versionLevel >= 8) {
			AUTHORITY = "com.android.launcher2.settings";
		} else {
			AUTHORITY = "com.android.launcher.settings";
		}

		final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/favorites?notify=true");
		Cursor c = cr.query(CONTENT_URI, new String[] { "title", "iconResource" }, "title=?", new String[] { context.getString(R.string.app_name) }, null);

		if (c != null && c.getCount() > 0) {
			isInstallShortcut = true;
			c.close();
		}
		return isInstallShortcut;
	}


	public static void addShortCut(Context context) {
		if (hasShortcut(context)) {
			return;
		}
		Intent shortcut = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
		shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, context.getResources().getString(R.string.app_name));
		shortcut.putExtra("duplicate", false);

		Parcelable icon = Intent.ShortcutIconResource.fromContext(context, R.mipmap.ic_launcher);
		shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);


		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.setClass(context, DownloadActivity.class);

		shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);

		context.sendBroadcast(shortcut);
	}


	public static boolean isStartedService(Context context, String serviceName) {
		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

		List<RunningServiceInfo> serviceList = activityManager.getRunningServices(50);
		for (RunningServiceInfo serviceInfo : serviceList) {
			if (serviceName.equals(serviceInfo.service.getClassName())) {
				return true;
			}
		}
		return false;
	}
	
	
	public static Context getContext() {
		return myApplication.getApplication();
	}


	public static Handler getHandler() {
		Looper mainLooper = myApplication.getMainThreadLooper();
		Handler handler = new Handler(mainLooper);
		return handler;
	}

	public static boolean post(Runnable runnable) {
		return getHandler().post(runnable);
	}

	public static View inflate(int resId) {
		return LayoutInflater.from(getContext()).inflate(resId, null);
	}
}
