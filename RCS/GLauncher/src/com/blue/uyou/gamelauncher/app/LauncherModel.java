/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.blue.uyou.gamelauncher.app;

import java.lang.ref.WeakReference;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import android.appwidget.AppWidgetProviderInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.os.SystemClock;
import android.util.Log;

import com.blue.uyou.gamelauncher.R;

/**
 * Maintains in-memory state of the Launcher. It is expected that there should
 * be only one LauncherModel object held in a static. Also provide APIs for
 * updating the database state for the Launcher.
 */
public class LauncherModel extends BroadcastReceiver {
	private static final boolean DEBUG_LOADERS = true;
	private static final String TAG = "Launcher.Model";

	private int mBatchSize; // 0 is all apps at once
	private final Context mApp;
	private LoaderTask mLoaderTask;
	private boolean mAllAppsLoaded;
	private IconCache mIconCache;
	private Bitmap mDefaultIcon;
	// private int mPreviousConfigMcc;
	private WeakReference<Callbacks> mCallbacks;
	private AllAppsList mAllAppsList;
	private final Object mLock = new Object();
	private DeferredHandler mHandler = new DeferredHandler();
	private static final HandlerThread sWorkerThread = new HandlerThread(
			"launcher-loader");
	private static List<String> mFilterPackageName = new ArrayList<String>();
	static {
		sWorkerThread.start();
		mFilterPackageName.add("com.starcor.hunan");
	}
	private static final Handler sWorker = new Handler(
			sWorkerThread.getLooper());

	public synchronized AllAppsList getAllAppsList() {
		return mAllAppsList;
	}

	public interface Callbacks {
		public boolean setLoadOnResume();

		public void bindAllApplications(ArrayList<ApplicationInfo> apps);

		public void bindAppsAdded(ArrayList<ApplicationInfo> apps);

		public void bindAppsUpdated(ArrayList<ApplicationInfo> apps);

		public void bindAppsRemoved(ArrayList<ApplicationInfo> apps,
				boolean permanent);

	}

	public LauncherModel(Context app, IconCache iconCache) {
		mApp = app;
		mAllAppsList = new AllAppsList(iconCache);
		mIconCache = iconCache;
		mDefaultIcon = Utilities.createIconBitmap(
				mIconCache.getFullResDefaultActivityIcon(), app);
		final Resources res = app.getResources();
		// mAllAppsLoadDelay = res
		// .getInteger(R.integer.config_allAppsBatchLoadDelay);
		mBatchSize = res.getInteger(R.integer.config_allAppsBatchSize);
		// Configuration config = res.getConfiguration();
		// mPreviousConfigMcc = config.mcc;
	}

	public Bitmap getFallbackIcon() {
		return Bitmap.createBitmap(mDefaultIcon);
	}

	/**
	 * Set this as the current Launcher activity object for the loader.
	 */
	public void initialize(Callbacks callbacks) {
		synchronized (mLock) {
			mCallbacks = new WeakReference<Callbacks>(callbacks);
		}
	}

	/**
	 * Call from the handler for ACTION_PACKAGE_ADDED, ACTION_PACKAGE_REMOVED
	 * and ACTION_PACKAGE_CHANGED.
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		if (DEBUG_LOADERS)
			Log.d(TAG, "onReceive intent=" + intent);

		final String action = intent.getAction();

		if (Intent.ACTION_PACKAGE_CHANGED.equals(action)
				|| Intent.ACTION_PACKAGE_REMOVED.equals(action)
				|| Intent.ACTION_PACKAGE_ADDED.equals(action)) {
			final String packageName = intent.getData().getSchemeSpecificPart();
			final boolean replacing = intent.getBooleanExtra(
					Intent.EXTRA_REPLACING, false);

			int op = PackageUpdatedTask.OP_NONE;

			if (packageName == null || packageName.length() == 0) {
				// they sent us a bad intent
				Log.e(TAG, "packageName is null");
				return;
			}

			if (Intent.ACTION_PACKAGE_CHANGED.equals(action)) {
				op = PackageUpdatedTask.OP_UPDATE;
			} else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
				if (!replacing) {
					op = PackageUpdatedTask.OP_REMOVE;
				}
				// else, we are replacing the package, so a PACKAGE_ADDED will
				// be sent
				// later, we will update the package at this time
			} else if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
				if (!replacing) {
					op = PackageUpdatedTask.OP_ADD;
				} else {
					op = PackageUpdatedTask.OP_UPDATE;
				}
			}

			if (op != PackageUpdatedTask.OP_NONE) {
				enqueuePackageUpdated(new PackageUpdatedTask(op,
						new String[] { packageName }));
			}

		} else if (Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE.equals(action)) {
			// First, schedule to add these apps back in.
			String[] packages = intent
					.getStringArrayExtra(Intent.EXTRA_CHANGED_PACKAGE_LIST);
			enqueuePackageUpdated(new PackageUpdatedTask(
					PackageUpdatedTask.OP_ADD, packages));
			// Then, rebind everything.
			startLoaderFromBackground();
		} else if (Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE
				.equals(action)) {
			String[] packages = intent
					.getStringArrayExtra(Intent.EXTRA_CHANGED_PACKAGE_LIST);
			enqueuePackageUpdated(new PackageUpdatedTask(
					PackageUpdatedTask.OP_UNAVAILABLE, packages));
		} else if (Intent.ACTION_LOCALE_CHANGED.equals(action)) {
			forceReload();
		}
	}

	private void forceReload() {
		synchronized (mLock) {
			stopLoaderLocked();
			mAllAppsLoaded = false;
		}
		startLoaderFromBackground();
	}

	/**
	 * When the launcher is in the background, it's possible for it to miss
	 * paired configuration changes. So whenever we trigger the loader from the
	 * background tell the launcher that it needs to re-run the loader when it
	 * comes back instead of doing it now.
	 */
	public void startLoaderFromBackground() {
		boolean runLoader = false;
		if (mCallbacks != null) {
			Callbacks callbacks = mCallbacks.get();
			if (callbacks != null) {
				// Only actually run the loader if they're not paused.
				if (!callbacks.setLoadOnResume()) {
					runLoader = true;
				}
			}
		}
		if (runLoader) {
			startLoader(mApp, false);
		}
	}

	// If there is already a loader task running, tell it to stop.
	// returns true if isLaunching() was true on the old task
	private boolean stopLoaderLocked() {
		boolean isLaunching = false;
		LoaderTask oldTask = mLoaderTask;
		if (oldTask != null) {
			if (oldTask.isLaunching()) {
				isLaunching = true;
			}
			oldTask.stopLocked();
		}
		return isLaunching;
	}

	public void startLoader(Context context, boolean isLaunching) {
		synchronized (mLock) {
			if (DEBUG_LOADERS) {
				Log.d(TAG, "startLoader isLaunching=" + isLaunching);
			}

			// Don't bother to start the thread if we know it's not going to do
			// anything
			if (mCallbacks != null && mCallbacks.get() != null) {
				// If there is already one running, tell it to stop.
				// also, don't downgrade isLaunching if we're already running
				isLaunching = isLaunching || stopLoaderLocked();
				mLoaderTask = new LoaderTask(context, isLaunching);
				sWorkerThread.setPriority(Thread.NORM_PRIORITY);
				sWorker.post(mLoaderTask);
			}
		}
	}

	public void stopLoader() {
		synchronized (mLock) {
			if (mLoaderTask != null) {
				mLoaderTask.stopLocked();
			}
		}
	}

	public boolean isAllAppsLoaded() {
		return mAllAppsLoaded;
	}

	/**
	 * Runnable for the thread that loads the contents of the launcher: -
	 * workspace icons - widgets - all apps icons
	 */
	private class LoaderTask implements Runnable {
		private Context mContext;
		private Thread mWaitThread;
		private boolean mIsLaunching;
		private boolean mStopped;
		private boolean mLoadAndBindStepFinished;
		private HashMap<Object, CharSequence> mLabelCache;

		LoaderTask(Context context, boolean isLaunching) {
			mContext = context;
			mIsLaunching = isLaunching;
			mLabelCache = new HashMap<Object, CharSequence>();
		}

		boolean isLaunching() {
			return mIsLaunching;
		}

		// private void waitForIdle() {
		// // Wait until the either we're stopped or the other threads are
		// // done.
		// // This way we don't start loading all apps until the workspace has
		// // settled
		// // down.
		// synchronized (LoaderTask.this) {
		// final long workspaceWaitTime = DEBUG_LOADERS ? SystemClock
		// .uptimeMillis() : 0;
		//
		// mHandler.postIdle(new Runnable() {
		// public void run() {
		// synchronized (LoaderTask.this) {
		// mLoadAndBindStepFinished = true;
		// if (DEBUG_LOADERS) {
		// Log.d(TAG, "done with previous binding step");
		// }
		// LoaderTask.this.notify();
		// }
		// }
		// });
		//
		// while (!mStopped && !mLoadAndBindStepFinished) {
		// try {
		// this.wait();
		// } catch (InterruptedException ex) {
		// // Ignore
		// }
		// }
		// if (DEBUG_LOADERS) {
		// Log.d(TAG, "waited "
		// + (SystemClock.uptimeMillis() - workspaceWaitTime)
		// + "ms for previous step to finish binding");
		// }
		// }
		// }

		public void run() {
			// Optimize for end-user experience: if the Launcher is up and //
			// running with the
			// All Apps interface in the foreground, load All Apps first.
			// Otherwise, load the
			// workspace first (default).
			final Callbacks cbk = mCallbacks.get();
			// final boolean loadWorkspaceFirst = cbk != null ? (!cbk
			// .isAllAppsVisible()) : true;

			final boolean loadWorkspaceFirst = cbk != null ? true : true;

			keep_running: {
				// Elevate priority when Home launches for the first time to
				// avoid
				// starving at boot time. Staring at a blank home is not cool.
				synchronized (mLock) {
					if (DEBUG_LOADERS)
						Log.d(TAG, "Setting thread priority to "
								+ (mIsLaunching ? "DEFAULT" : "BACKGROUND"));
					android.os.Process
							.setThreadPriority(mIsLaunching ? Process.THREAD_PRIORITY_DEFAULT
									: Process.THREAD_PRIORITY_BACKGROUND);
				}

				if (mStopped) {
					break keep_running;
				}

				// Whew! Hard work done. Slow us down, and wait until the UI
				// thread has
				// settled down.
				synchronized (mLock) {
					if (mIsLaunching) {
						if (DEBUG_LOADERS)
							Log.d(TAG, "Setting thread priority to BACKGROUND");
						android.os.Process
								.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
					}
				}
				// waitForIdle();

				// second step
				if (loadWorkspaceFirst) {
					if (DEBUG_LOADERS)
						Log.d(TAG, "step 1: loading all apps");
					loadAndBindAllApps();
				}
				// else {
				// if (DEBUG_LOADERS)
				// Log.d(TAG, "step 2: special: loading workspace");
				// loadAndBindWorkspace();
				// }

				// Restore the default thread priority after we are done loading
				// items
				synchronized (mLock) {
					android.os.Process
							.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
				}
			}

			// Update the saved icons if necessary
			if (DEBUG_LOADERS)
				Log.d(TAG, "Comparing loaded icons to database icons");
			mContext = null;

			synchronized (mLock) {
				// If we are still the last one to be scheduled, remove
				// ourselves.
				if (mLoaderTask == this) {
					mLoaderTask = null;
				}
			}
		}

		public void stopLocked() {
			synchronized (LoaderTask.this) {
				mStopped = true;
				this.notify();
			}
		}

		/**
		 * Gets the callbacks object. If we've been stopped, or if the launcher
		 * object has somehow been garbage collected, return null instead. Pass
		 * in the Callbacks object that was around when the deferred message was
		 * scheduled, and if there's a new Callbacks object around then also
		 * return null. This will save us from calling onto it with data that
		 * will be ignored.
		 */
		Callbacks tryGetCallbacks(Callbacks oldCallbacks) {
			synchronized (mLock) {
				if (mStopped) {
					return null;
				}

				if (mCallbacks == null) {
					return null;
				}

				final Callbacks callbacks = mCallbacks.get();
				if (callbacks != oldCallbacks) {
					return null;
				}
				if (callbacks == null) {
					Log.w(TAG, "no mCallbacks");
					return null;
				}

				return callbacks;
			}
		}

		private void loadAndBindAllApps() {
			if (DEBUG_LOADERS) {
				Log.d(TAG, "loadAndBindAllApps mAllAppsLoaded="
						+ mAllAppsLoaded);
			}
			if (!mAllAppsLoaded) {
				loadAllAppsByBatch();
				synchronized (LoaderTask.this) {
					if (mStopped) {
						return;
					}
					mAllAppsLoaded = true;
				}
			} else {
				onlyBindAllApps();
			}
		}

		private void onlyBindAllApps() {
			final Callbacks oldCallbacks = mCallbacks.get();
			if (oldCallbacks == null) {
				// This launcher has exited and nobody bothered to tell us. Just
				// bail.
				Log.w(TAG,
						"LoaderTask running with no launcher (onlyBindAllApps)");
				return;
			}

			final ArrayList<ApplicationInfo> list = (ArrayList<ApplicationInfo>) mAllAppsList.data
					.clone();

			mHandler.post(new Runnable() {
				public void run() {
					final long t = SystemClock.uptimeMillis();
					final Callbacks callbacks = tryGetCallbacks(oldCallbacks);
					if (callbacks != null) {
						callbacks.bindAllApplications(list);
						// if (mFirstAppsList.size() > 0) {
						// callbacks.bindFirstApp(mFirstAppsList);
						// }
					}
					if (DEBUG_LOADERS) {
						Log.d(TAG,
								"bound all " + list.size()
										+ " apps from cache in "
										+ (SystemClock.uptimeMillis() - t)
										+ "ms");
					}
				}
			});

		}

		public void loadAllAppsByBatch() {
			final long t = DEBUG_LOADERS ? SystemClock.uptimeMillis() : 0;

			// Don't use these two variables in any of the callback runnables.
			// Otherwise we hold a reference to them.
			final Callbacks oldCallbacks = mCallbacks.get();
			if (oldCallbacks == null) {
				// This launcher has exited and nobody bothered to tell us. Just
				// bail.
				Log.w(TAG,
						"LoaderTask running with no launcher (loadAllAppsByBatch)");
				return;
			}

			final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
			mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

			final PackageManager packageManager = mContext.getPackageManager();
			List<ResolveInfo> apps = null;

			int N = Integer.MAX_VALUE;

			int startIndex;
			int i = 0;
			int batchSize = -1;
			while (i < N && !mStopped) {
				if (i == 0) {
					mAllAppsList.clear();
					final long qiaTime = DEBUG_LOADERS ? SystemClock
							.uptimeMillis() : 0;
					apps = packageManager.queryIntentActivities(mainIntent, 0);
					if (DEBUG_LOADERS) {
						Log.d(TAG,
								"queryIntentActivities took "
										+ (SystemClock.uptimeMillis() - qiaTime)
										+ "ms");
					}
					if (apps == null) {
						return;
					}
					N = apps.size();
					if (DEBUG_LOADERS) {
						Log.d(TAG, "queryIntentActivities got " + N + " apps");
					}
					if (N == 0) {
						// There are no apps?!?
						return;
					}
					if (mBatchSize == 0) {
						batchSize = N;
					} else {
						batchSize = mBatchSize;
					}
					// final long sortTime = DEBUG_LOADERS ? SystemClock
					// .uptimeMillis() : 0;
					// Collections.sort(apps,
					// new LauncherModel.ShortcutNameComparator(
					// packageManager, mLabelCache));
					// if (DEBUG_LOADERS) {
					// Log.d(TAG, "sort took "
					// + (SystemClock.uptimeMillis() - sortTime)
					// + "ms");
					// }
				}

				final long t2 = DEBUG_LOADERS ? SystemClock.uptimeMillis() : 0;
				startIndex = i;
				for (int j = 0; i < N && j < batchSize; j++) {
					// This builds the icon bitmaps.
					ResolveInfo info = apps.get(i);
					String packageName = info.activityInfo.applicationInfo.packageName;
					if (!mFilterPackageName.contains(packageName)) {
						ApplicationInfo applicationInfo = new ApplicationInfo(
								packageManager, info, mIconCache, mLabelCache);
						mAllAppsList.add(applicationInfo);
					}
					i++;
				}
				sort();

				final boolean first = i <= batchSize;
				final Callbacks callbacks = tryGetCallbacks(oldCallbacks);
				final ArrayList<ApplicationInfo> added = mAllAppsList.added;
				mAllAppsList.added = new ArrayList<ApplicationInfo>();

				mHandler.post(new Runnable() {
					public void run() {
						final long t = SystemClock.uptimeMillis();
						if (callbacks != null) {
							if (first) {
								callbacks.bindAllApplications(added);
								// if (mFirstAppsList.size() > 0) {
								// callbacks.bindFirstApp(mFirstAppsList);
								// }
							} else {
								callbacks.bindAppsAdded(added);
							}
							if (DEBUG_LOADERS) {
								Log.d(TAG,
										"bound "
												+ added.size()
												+ " apps in "
												+ (SystemClock.uptimeMillis() - t)
												+ "ms");
							}
						} else {
							Log.i(TAG, "not binding apps: no Launcher activity");
						}
					}
				});

				if (DEBUG_LOADERS) {
					Log.d(TAG,
							"batch of " + (i - startIndex)
									+ " icons processed in "
									+ (SystemClock.uptimeMillis() - t2) + "ms");
				}
			}

			if (DEBUG_LOADERS) {
				Log.d(TAG,
						"cached all " + N + " apps in "
								+ (SystemClock.uptimeMillis() - t) + "ms");
			}
		}

		public void dumpState() {
			Log.d(TAG, "mLoaderTask.mContext=" + mContext);
			Log.d(TAG, "mLoaderTask.mWaitThread=" + mWaitThread);
			Log.d(TAG, "mLoaderTask.mIsLaunching=" + mIsLaunching);
			Log.d(TAG, "mLoaderTask.mStopped=" + mStopped);
			Log.d(TAG, "mLoaderTask.mLoadAndBindStepFinished="
					+ mLoadAndBindStepFinished);
		}
	}

	void enqueuePackageUpdated(PackageUpdatedTask task) {
		sWorker.post(task);
	}

	private void sort() {
		Collections.sort(mAllAppsList.data,
				LauncherModel.APP_INSRALL_RECENT_TIME);

		// List<ApplicationInfo> preApps = new ArrayList<ApplicationInfo>();
		// ArrayList<App> preInstallApps = PreInstallManager.getInstance()
		// .getPreInstallApps();
		// if (preInstallApps != null) {
		// int size = preInstallApps.size();
		// if (size > 0) {
		// for (int i = 0; i < size; i++) {
		// App app = preInstallApps.get(i);
		// if (app == null) {
		// continue;
		// }
		// for (ApplicationInfo appinfo : mAllAppsList.data) {
		// String packageName = appinfo.getPackageName();
		// if (StringUtils.equals(packageName,
		// app.getPackageName())) {
		// preApps.add(0, appinfo);
		// }
		// }
		// }
		// }
		// }
		// for (ApplicationInfo appinfo : preApps) {
		// mAllAppsList.data.remove(appinfo);
		// mAllAppsList.data.add(0, appinfo);
		// }
	}

	private class PackageUpdatedTask implements Runnable {
		int mOp;
		String[] mPackages;

		public static final int OP_NONE = 0;
		public static final int OP_ADD = 1;
		public static final int OP_UPDATE = 2;
		public static final int OP_REMOVE = 3; // uninstlled
		public static final int OP_UNAVAILABLE = 4; // external media unmounted

		// public static final int OP_SORT = 5; // external media unmounted

		public PackageUpdatedTask(int op, String[] packages) {
			mOp = op;
			mPackages = packages;
		}

		public void run() {
			final Context context = mApp;

			final String[] packages = mPackages;
			final int N = packages.length;
			switch (mOp) {
			case OP_ADD:
				for (int i = 0; i < N; i++) {
					if (DEBUG_LOADERS)
						Log.d(TAG, "mAllAppsList.addPackage " + packages[i]);
					mAllAppsList.addPackage(context, packages[i]);
				}
				break;
			case OP_UPDATE:
				for (int i = 0; i < N; i++) {
					if (DEBUG_LOADERS)
						Log.d(TAG, "mAllAppsList.updatePackage " + packages[i]);
					mAllAppsList.updatePackage(context, packages[i]);
				}
				break;
			case OP_REMOVE:
			case OP_UNAVAILABLE:
				for (int i = 0; i < N; i++) {
					if (DEBUG_LOADERS)
						Log.d(TAG, "mAllAppsList.removePackage " + packages[i]);
					mAllAppsList.removePackage(packages[i]);
				}
				break;
			}

			ArrayList<ApplicationInfo> added = null;
			ArrayList<ApplicationInfo> removed = null;
			ArrayList<ApplicationInfo> modified = null;

			if (mAllAppsList.added.size() > 0) {
				added = mAllAppsList.added;
				mAllAppsList.added = new ArrayList<ApplicationInfo>();
			}
			if (mAllAppsList.removed.size() > 0) {
				removed = mAllAppsList.removed;
				mAllAppsList.removed = new ArrayList<ApplicationInfo>();
				for (ApplicationInfo info : removed) {
					mIconCache.remove(info.intent.getComponent());
				}
			}
			if (mAllAppsList.modified.size() > 0) {
				modified = mAllAppsList.modified;
				mAllAppsList.modified = new ArrayList<ApplicationInfo>();
			}

			final Callbacks callbacks = mCallbacks != null ? mCallbacks.get()
					: null;
			if (callbacks == null) {
				Log.w(TAG,
						"Nobody to tell about the new app.  Launcher is probably loading.");
				return;
			}
			sort();

			if (added != null) {
				final ArrayList<ApplicationInfo> addedFinal = added;
				mHandler.post(new Runnable() {
					public void run() {
						Callbacks cb = mCallbacks != null ? mCallbacks.get()
								: null;
						if (callbacks == cb && cb != null) {
							callbacks.bindAppsAdded(addedFinal);
						}
					}
				});
			}
			if (modified != null) {
				final ArrayList<ApplicationInfo> modifiedFinal = modified;
				mHandler.post(new Runnable() {
					public void run() {
						Callbacks cb = mCallbacks != null ? mCallbacks.get()
								: null;
						if (callbacks == cb && cb != null) {
							callbacks.bindAppsUpdated(modifiedFinal);
						}
					}
				});
			}
			if (removed != null) {
				final boolean permanent = mOp != OP_UNAVAILABLE;
				final ArrayList<ApplicationInfo> removedFinal = removed;
				mHandler.post(new Runnable() {
					public void run() {
						Callbacks cb = mCallbacks != null ? mCallbacks.get()
								: null;
						if (callbacks == cb && cb != null) {
							callbacks.bindAppsRemoved(removedFinal, permanent);
						}
					}
				});
			}

			// mHandler.post(new Runnable() {
			// @Override
			// public void run() {
			// Callbacks cb = mCallbacks != null ? mCallbacks.get() : null;
			// if (callbacks == cb && cb != null) {
			// callbacks.bindPackagesUpdated();
			// }
			// }
			// });
		}
	}

	private static final Collator sCollator = Collator.getInstance();
	public static final Comparator<ApplicationInfo> APP_NAME_COMPARATOR = new Comparator<ApplicationInfo>() {
		public final int compare(ApplicationInfo a, ApplicationInfo b) {
			int result = sCollator.compare(a.title.toString(),
					b.title.toString());
			if (result == 0) {
				result = a.componentName.compareTo(b.componentName);
			}
			return result;
		}
	};
	public static final Comparator<ApplicationInfo> APP_INSTALL_TIME_COMPARATOR = new Comparator<ApplicationInfo>() {
		public final int compare(ApplicationInfo a, ApplicationInfo b) {
			if (a.firstInstallTime < b.firstInstallTime)
				return 1;
			if (a.firstInstallTime > b.firstInstallTime)
				return -1;
			return 0;
		}
	};
	public static final Comparator<ApplicationInfo> APP_INSRALL_RECENT_TIME = new Comparator<ApplicationInfo>() {
		public final int compare(ApplicationInfo a, ApplicationInfo b) {
			if (a.firstInstallTime < b.firstInstallTime)
				return -1;
			if (a.firstInstallTime > b.firstInstallTime)
				return 1;
			return 0;
		}
	};
	public static final Comparator<AppWidgetProviderInfo> WIDGET_NAME_COMPARATOR = new Comparator<AppWidgetProviderInfo>() {
		public final int compare(AppWidgetProviderInfo a,
				AppWidgetProviderInfo b) {
			return sCollator.compare(a.label.toString(), b.label.toString());
		}
	};

	static ComponentName getComponentNameFromResolveInfo(ResolveInfo info) {
		if (info.activityInfo != null) {
			return new ComponentName(info.activityInfo.packageName,
					info.activityInfo.name);
		} else {
			return new ComponentName(info.serviceInfo.packageName,
					info.serviceInfo.name);
		}
	}

	public static class ShortcutNameComparator implements
			Comparator<ResolveInfo> {
		private PackageManager mPackageManager;
		private HashMap<Object, CharSequence> mLabelCache;

		ShortcutNameComparator(PackageManager pm) {
			mPackageManager = pm;
			mLabelCache = new HashMap<Object, CharSequence>();
		}

		ShortcutNameComparator(PackageManager pm,
				HashMap<Object, CharSequence> labelCache) {
			mPackageManager = pm;
			mLabelCache = labelCache;
		}

		public final int compare(ResolveInfo a, ResolveInfo b) {
			CharSequence labelA, labelB;
			ComponentName keyA = LauncherModel
					.getComponentNameFromResolveInfo(a);
			ComponentName keyB = LauncherModel
					.getComponentNameFromResolveInfo(b);
			if (mLabelCache.containsKey(keyA)) {
				labelA = mLabelCache.get(keyA);
			} else {
				labelA = a.loadLabel(mPackageManager).toString();

				mLabelCache.put(keyA, labelA);
			}
			if (mLabelCache.containsKey(keyB)) {
				labelB = mLabelCache.get(keyB);
			} else {
				labelB = b.loadLabel(mPackageManager).toString();

				mLabelCache.put(keyB, labelB);
			}
			return sCollator.compare(labelA, labelB);
		}
	};

	public static class WidgetAndShortcutNameComparator implements
			Comparator<Object> {
		private PackageManager mPackageManager;
		private HashMap<Object, String> mLabelCache;

		WidgetAndShortcutNameComparator(PackageManager pm) {
			mPackageManager = pm;
			mLabelCache = new HashMap<Object, String>();
		}

		public final int compare(Object a, Object b) {
			String labelA, labelB;
			if (mLabelCache.containsKey(a)) {
				labelA = mLabelCache.get(a);
			} else {
				labelA = (a instanceof AppWidgetProviderInfo) ? ((AppWidgetProviderInfo) a).label
						: ((ResolveInfo) a).loadLabel(mPackageManager)
								.toString();
				mLabelCache.put(a, labelA);
			}
			if (mLabelCache.containsKey(b)) {
				labelB = mLabelCache.get(b);
			} else {
				labelB = (b instanceof AppWidgetProviderInfo) ? ((AppWidgetProviderInfo) b).label
						: ((ResolveInfo) b).loadLabel(mPackageManager)
								.toString();
				mLabelCache.put(b, labelB);
			}
			return sCollator.compare(labelA, labelB);
		}
	};

	public void dumpState() {
		Log.d(TAG, "mCallbacks=" + mCallbacks);
		ApplicationInfo.dumpApplicationInfoList(TAG, "mAllAppsList.data",
				mAllAppsList.data);
		ApplicationInfo.dumpApplicationInfoList(TAG, "mAllAppsList.added",
				mAllAppsList.added);
		ApplicationInfo.dumpApplicationInfoList(TAG, "mAllAppsList.removed",
				mAllAppsList.removed);
		ApplicationInfo.dumpApplicationInfoList(TAG, "mAllAppsList.modified",
				mAllAppsList.modified);
		if (mLoaderTask != null) {
			mLoaderTask.dumpState();
		} else {
			Log.d(TAG, "mLoaderTask=null");
		}
	}
}
