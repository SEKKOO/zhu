package com.computer.mazhihuapp.utils.net;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.computer.mazhihuapp.model.AppInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DownloadManager {
	public static final int STATE_NONE = 0;
	public static final int STATE_WAITING = 1;
	public static final int STATE_DOWNLOADING = 2;
	public static final int STATE_PAUSED = 3;
	public static final int STATE_DOWNLOADED = 4;
	public static final int STATE_ERROR = 5;


	private static DownloadManager instance;

	private DownloadManager() {
	}

	private Map<Long, DownloadInfo> mDownloadMap = new ConcurrentHashMap<Long, DownloadInfo>();
	private List<DownloadObserver> mObservers = new ArrayList<DownloadObserver>();
	private Map<Long, DownloadTask> mTaskMap = new ConcurrentHashMap<Long, DownloadTask>();
	public static synchronized DownloadManager getInstance() {
		if (instance == null) {
			instance = new DownloadManager();
		}
		return instance;
	}

	public void registerObserver(DownloadObserver observer) {
		synchronized (mObservers) {
			if (!mObservers.contains(observer)) {
				mObservers.add(observer);
			}
		}
	}

	public void unRegisterObserver(DownloadObserver observer) {
		synchronized (mObservers) {
			if (mObservers.contains(observer)) {
				mObservers.remove(observer);
			}
		}
	}

	public void notifyDownloadStateChanged(DownloadInfo info) {
		synchronized (mObservers) {
			for (DownloadObserver observer : mObservers) {
				observer.onDownloadStateChanged(info);
			}
		}
	}

	public void notifyDownloadProgressed(DownloadInfo info) {
		synchronized (mObservers) {
			for (DownloadObserver observer : mObservers) {
				observer.onDownloadProgressed(info);
			}
		}
	}

	public synchronized void download(AppInfo appInfo) {
		DownloadInfo info = mDownloadMap.get(appInfo.getId());
		if (info == null) {
			info = DownloadInfo.clone(appInfo);
			mDownloadMap.put(appInfo.getId(), info);
		}
		if (info.getDownloadState() == STATE_NONE
				|| info.getDownloadState() == STATE_PAUSED
				|| info.getDownloadState() == STATE_ERROR) {
			info.setDownloadState(STATE_WAITING);
			notifyDownloadStateChanged(info);
			DownloadTask task = new DownloadTask(info);
			mTaskMap.put(info.getId(), task);
			ThreadManager.getDownloadPool().execute(task);
		}
	}

	public synchronized void pause(AppInfo appInfo) {
		stopDownload(appInfo);
		DownloadInfo info = mDownloadMap.get(appInfo.getId());
		if (info != null) {
			info.setDownloadState(STATE_PAUSED);
			notifyDownloadStateChanged(info);
		}
	}

	public synchronized void cancel(AppInfo appInfo) {
		stopDownload(appInfo);
		DownloadInfo info = mDownloadMap.get(appInfo.getId());
		if (info != null) {
			info.setDownloadState(STATE_NONE);
			notifyDownloadStateChanged(info);
			info.setCurrentSize(0);
			File file = new File(info.getPath());
			file.delete();
		}
	}


	public synchronized void install(AppInfo appInfo) {
		stopDownload(appInfo);
		DownloadInfo info = mDownloadMap.get(appInfo.getId());
		if (info != null) {
			Intent installIntent = new Intent(Intent.ACTION_VIEW);
			installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			installIntent.setDataAndType(Uri.parse("file://" + info.getPath()),
					"application/vnd.android.package-archive");
			AppUtil.getContext().startActivity(installIntent);
		}
		notifyDownloadStateChanged(info);
	}

	public synchronized void open(AppInfo appInfo) {
		try {
			Context context = AppUtil.getContext();
			Intent intent = context.getPackageManager()
					.getLaunchIntentForPackage(appInfo.getBaike_name());
			context.startActivity(intent);
		} catch (Exception e) {
		}
	}


	private void stopDownload(AppInfo appInfo) {
		DownloadTask task = mTaskMap.remove(appInfo.getId());
		if (task != null) {
			ThreadManager.getDownloadPool().cancel(task);
		}
	}

	public synchronized DownloadInfo getDownloadInfo(long id) {
		return mDownloadMap.get(id);
	}

	public synchronized void setDownloadInfo(long id, DownloadInfo info) {
		mDownloadMap.put(id, info);
	}

	public class DownloadTask implements Runnable {
		private DownloadInfo info;

		public DownloadTask(DownloadInfo info) {
			this.info = info;
		}

		@Override
		public void run() {
			info.setDownloadState(STATE_DOWNLOADING);
			notifyDownloadStateChanged(info);
			File file = new File(info.getPath());
			HttpHelper.HttpResult httpResult = null;
			InputStream stream = null;
			if (info.getCurrentSize() == 0 || !file.exists()
					|| file.length() != info.getCurrentSize()) {


				info.setCurrentSize(0);
				file.delete();
			}
			httpResult = HttpHelper.download(info.getUrl());

			if (httpResult == null
					|| (stream = httpResult.getInputStream()) == null) {
				info.setDownloadState(STATE_ERROR);
				notifyDownloadStateChanged(info);
			} else {
				try {
					skipBytesFromStream(stream, info.getCurrentSize());
				} catch (Exception e1) {
					e1.printStackTrace();
				}

				FileOutputStream fos = null;
				try {
					fos = new FileOutputStream(file, true);
					int count = -1;
					byte[] buffer = new byte[1024];
					while (((count = stream.read(buffer)) != -1)
							&& info.getDownloadState() == STATE_DOWNLOADING) {

						fos.write(buffer, 0, count);
						fos.flush();
						info.setCurrentSize(info.getCurrentSize() + count);
						notifyDownloadProgressed(info);
					}
				} catch (Exception e) {
					info.setDownloadState(STATE_ERROR);
					notifyDownloadStateChanged(info);
					info.setCurrentSize(0);
					file.delete();
				} finally {
					IOUtils.close(fos);
					if (httpResult != null) {
						httpResult.close();
					}
				}

				if (info.getCurrentSize() == info.getAppSize()) {
					info.setDownloadState(STATE_DOWNLOADED);
					notifyDownloadStateChanged(info);
				} else if (info.getDownloadState() == STATE_PAUSED) {
					notifyDownloadStateChanged(info);
				} else {
					info.setDownloadState(STATE_ERROR);
					notifyDownloadStateChanged(info);
					info.setCurrentSize(0);
					file.delete();
				}
			}
			mTaskMap.remove(info.getId());
		}
	}

	public interface DownloadObserver {

		public void onDownloadStateChanged(DownloadInfo info);

		public void onDownloadProgressed(DownloadInfo info);
	}

	private long skipBytesFromStream(InputStream inputStream, long n) {
		long remaining = n;
		// SKIP_BUFFER_SIZE is used to determine the size of skipBuffer
		int SKIP_BUFFER_SIZE = 10000;
		// skipBuffer is initialized in skip(long), if needed.
		byte[] skipBuffer = null;
		int nr = 0;
		if (skipBuffer == null) {
			skipBuffer = new byte[SKIP_BUFFER_SIZE];
		}
		byte[] localSkipBuffer = skipBuffer;
		if (n <= 0) {
			return 0;
		}
		while (remaining > 0) {
			try {
				long skip = inputStream.skip(10000);
				nr = inputStream.read(localSkipBuffer, 0,
						(int) Math.min(SKIP_BUFFER_SIZE, remaining));
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (nr < 0) {
				break;
			}
			remaining -= nr;
		}
		return n - remaining;
	}
}
