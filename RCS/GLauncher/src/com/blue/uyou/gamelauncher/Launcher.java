package com.blue.uyou.gamelauncher;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.GridLayout.LayoutParams;

import com.blue.uyou.gamelauncher.app.ApplicationInfo;
import com.blue.uyou.gamelauncher.app.IconCache;
import com.blue.uyou.gamelauncher.app.LauncherModel;
import com.blue.uyou.gamelauncher.app.LauncherModel.Callbacks;
import com.blue.uyou.gamelauncher.app.PagedViewIcon;
import com.blue.uyou.gamelauncher.utils.AppUtils;
import com.blue.uyou.gamelauncher.views.Page;
import com.viewpagerindicator.LinePageIndicator;

public class Launcher extends Activity implements Callbacks {
	private static final String TAG = "game";
	private List<Page> mPageList = new ArrayList<Page>();
	private ViewPager mViewPager;
	private MPagerAdpater mPagerAdpater;
	private LayoutInflater mInflater;
	private static final int ROW = 4;
	private static final int COLOUMN = 7;
	private static final int MAX_SIZE_PAGE = ROW * COLOUMN;
	public static final int HD = 1;
	public static final int FHD = 2;
	private int fullWidth;
	private int fullHeight;
	// private Button uninstallBtn;
	private LauncherModel mModel;
	private IconCache mIconCache;
	public static int displayType = FHD;
	private DateFormat df = new SimpleDateFormat("HH:mm", Locale.CHINA);

	// private TextView mTxtView;
	// private ImageView mWifiImg;
	// private ImageView mEthImg;
	// private Timer timer;
	// private MTimeTask mTimeTask;
	// private ConnectivityManager mConnectivityManager;

	public static boolean isHD() {
		return displayType == HD;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// mConnectivityManager = (ConnectivityManager)
		// getSystemService(Context.CONNECTIVITY_SERVICE);
		mViewPager = (ViewPager) findViewById(R.id.viewpager);
		mPagerAdpater = new MPagerAdpater();
		mViewPager.setAdapter(mPagerAdpater);
		LinePageIndicator mIndicator = (LinePageIndicator) findViewById(R.id.indicator);
		mIndicator.setViewPager(mViewPager);
		mInflater = LayoutInflater.from(this);
		DisplayMetrics dm = getResources().getDisplayMetrics();
		fullWidth = dm.widthPixels;
		fullHeight = dm.heightPixels;
		Log.d(TAG, "fullWidth:" + fullWidth);
		Log.d(TAG, "fullHeight:" + fullHeight);
		Log.d(TAG, "densityDpi:" + dm.densityDpi);
		if (fullWidth == 1280) {
			displayType = HD;
		}
		if (fullWidth == 1920) {
			displayType = FHD;
		}
		// uninstallBtn = (Button) findViewById(R.id.uninstallbtn);
		// uninstallBtn.setOnDragListener(new MyDragListener());
		// uninstallBtn.setVisibility(View.INVISIBLE);
		// uninstallBtn.setBackgroundColor(Color.GREEN);

		mIconCache = new IconCache(this);
		mModel = new LauncherModel(this, mIconCache);
		IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
		filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
		filter.addDataScheme("package");
		registerReceiver(mModel, filter);
		filter = new IntentFilter();
		filter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
		filter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
		filter.addAction(Intent.ACTION_LOCALE_CHANGED);
		// filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
		registerReceiver(mModel, filter);

		// IntentFilter filter2 = new IntentFilter();
		// filter2.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		// filter2.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		// filter2.addAction(WifiManager.RSSI_CHANGED_ACTION);
		// filter2.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		// filter2.addAction(Intent.ACTION_TIME_CHANGED);
		// registerReceiver(receiver, filter2);

		mModel.initialize(this);
		mModel.startLoader(this, true);

		// mTxtView = (TextView) findViewById(R.id.time);
		// mWifiImg = (ImageView) findViewById(R.id.wifi);
		// mEthImg = (ImageView) findViewById(R.id.eth);
		// timer = new Timer();
		// mTimeTask = new MTimeTask();
		// timer.schedule(mTimeTask, 0, 1000 * 60);
		// bindWifi(NetworkUtils.getWifiSignalIntensity(this));
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	// private BroadcastReceiver receiver = new BroadcastReceiver() {
	//
	// @Override
	// public void onReceive(Context context, Intent intent) {
	// String action = intent.getAction();
	// Log.v(TAG, "action :" + action);
	// if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
	// Bundle bundle = intent.getExtras();
	// int newInt = bundle.getInt(WifiManager.EXTRA_WIFI_STATE);
	// if (newInt == WifiManager.WIFI_STATE_DISABLED) {
	// bindWifi(0);
	// } else if (newInt == WifiManager.WIFI_STATE_ENABLED) {
	// bindWifi(1);
	// } else {
	// bindWifi(0);
	// }
	// }
	// if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
	// NetworkInfo wifiInfo = (NetworkInfo) intent.getExtras().get(
	// WifiManager.EXTRA_NETWORK_INFO);
	// State wifistate = wifiInfo.getState();
	// Log.d(TAG, "wifistate:" + wifistate);
	// if (State.CONNECTED == wifistate) {
	// bindWifi(NetworkUtils.getWifiSignalIntensity(context));
	// } else if (State.DISCONNECTED == wifistate) {
	// bindWifi(0);
	// }
	// }
	// if (action.equals(WifiManager.RSSI_CHANGED_ACTION)) {
	// bindWifi(NetworkUtils.getWifiSignalIntensity(context));
	// }
	// if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
	// // int type = intent.getIntExtra(
	// // ConnectivityManager.EXTRA_NETWORK_TYPE, -1);
	// // boolean notConnect = intent.getBooleanExtra(
	// // ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
	// //
	// NetworkInfo activeNetworkInfo = mConnectivityManager
	// .getActiveNetworkInfo();
	// if (activeNetworkInfo == null) {
	// bindWifi(0);
	// return;
	// }
	// int type = activeNetworkInfo.getType();
	// boolean connected = activeNetworkInfo.isConnected();
	// Log.d(TAG, "type:" + type);
	// Log.d(TAG, "connected:" + connected);
	// Log.d(TAG, "activeNetworkInfo:" + activeNetworkInfo.toString());
	// if (ConnectivityManager.TYPE_ETHERNET == type) {
	// if (connected) {
	// mWifiImg.setImageResource(R.drawable.ethernet_connected);
	// }
	// }
	// if (ConnectivityManager.TYPE_WIFI == type) {
	// if (connected) {
	// bindWifi(NetworkUtils.getWifiSignalIntensity(context));
	// } else {
	// bindWifi(0);
	// }
	// }
	// }
	// }
	// };

	//
	// private void bindWifi(int signal) {
	// int imgResoures;
	// switch (signal) {
	// case 0:
	// imgResoures = R.drawable.ic_qs_wifi_no_network;
	// break;
	// case 1:
	// imgResoures = R.drawable.ic_qs_wifi_full_1;
	// break;
	// case 2:
	// imgResoures = R.drawable.ic_qs_wifi_full_2;
	// break;
	// case 3:
	// imgResoures = R.drawable.ic_qs_wifi_full_3;
	// break;
	// case 4:
	// imgResoures = R.drawable.ic_qs_wifi_full_4;
	// break;
	// default:
	// imgResoures = R.drawable.ic_qs_wifi_no_network;
	// break;
	// }
	// if (mWifiImg.getVisibility() != View.VISIBLE) {
	// mWifiImg.setVisibility(View.VISIBLE);
	// }
	// mWifiImg.setImageResource(imgResoures);
	// }

	// private Handler mHanlder = new Handler() {
	// public void handleMessage(android.os.Message msg) {
	// mTxtView.setText(String.valueOf(msg.obj));
	// }
	// };

	// private class MTimeTask extends TimerTask {
	// @Override
	// public void run() {
	// Message msg = Message.obtain();
	// msg.obj = df.format(new Date());
	// mHanlder.sendMessage(msg);
	// }
	// }

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mModel);
		// mTimeTask.cancel();
		// timer.cancel();
	}

	// private class MyDragListener implements OnDragListener {
	// @Override
	// public boolean onDrag(View v, DragEvent event) {
	// int action = event.getAction();
	// switch (action) {
	// case DragEvent.ACTION_DRAG_STARTED:
	// break;
	// case DragEvent.ACTION_DRAG_ENTERED:
	// v.setBackgroundColor(Color.RED);
	// break;
	// case DragEvent.ACTION_DRAG_EXITED:
	// v.setBackgroundColor(Color.GREEN);
	// break;
	// case DragEvent.ACTION_DROP:
	// PagedViewIcon view = (PagedViewIcon) event.getLocalState();
	// Intent intent = new Intent();
	// intent.setAction(Intent.ACTION_DELETE);
	// intent.setData(Uri.parse("package:"
	// + view.getAppInfo().getPackageName()));
	// AppUtils.startActivity(Launcher.this, intent);
	// break;
	// case DragEvent.ACTION_DRAG_ENDED:
	// v.setVisibility(View.INVISIBLE);
	// v.setBackgroundColor(Color.GREEN);
	// default:
	// break;
	// }
	// return true;
	// }
	// }

	private OnLongClickListener mOnLongClickListener = new OnLongClickListener() {
		@Override
		public boolean onLongClick(View v) {
			// uninstallBtn.setVisibility(View.VISIBLE);
			// ClipData data = ClipData.newPlainText("", "");
			// DragShadowBuilder shadowBuilder = new
			// View.DragShadowBuilder(view);
			// view.startDrag(data, shadowBuilder, view, 0);
			PagedViewIcon view = (PagedViewIcon) v;
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_DELETE);
			intent.setData(Uri.parse("package:"
					+ view.getAppInfo().getPackageName()));
			AppUtils.startActivity(Launcher.this, intent);
			return false;
		}
	};

	private class MPagerAdpater extends PagerAdapter {
		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {

			return arg0 == arg1;
		}

		@Override
		public int getCount() {
			return mPageList.size();
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			Log.d(TAG, "remove:" + position);
			container.removeView(mPageList.get(position));
		}

		@Override
		public int getItemPosition(Object object) {
			return super.getItemPosition(object);
		}

		@Override
		public CharSequence getPageTitle(int position) {

			return String.valueOf(position);
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			Log.d(TAG, "instantiateItem:" + position);
			View view = mPageList.get(position);
			if (view.getParent() != null) {
				container.removeView(view);
			}
			container.addView(view, 0);
			return view;
		}
	}

	@Override
	public boolean setLoadOnResume() {
		return false;
	}

	@Override
	public void bindAllApplications(ArrayList<ApplicationInfo> apps) {
		ArrayList<ApplicationInfo> data = mModel.getAllAppsList().data;
		mPageList.clear();
		mPagerAdpater.notifyDataSetChanged();

		int size = data.size();
		int index = 0;
		int pageIndex = 0;
		for (int i = 0; i < size; i++) {
			ApplicationInfo appItem = data.get(i);
			appItem.setIndex(index);
			appItem.setPageIndex(pageIndex);
			if (index == 0) {
				Page view = (Page) mInflater.inflate(R.layout.page, null);
				view.setGridLayout((GridLayout) view
						.findViewById(R.id.gridlayout));
				mPageList.add(view);
			}
			if (index >= MAX_SIZE_PAGE) {
				index = 0;
				pageIndex++;
				Page view = (Page) mInflater.inflate(R.layout.page, null);
				view.setGridLayout((GridLayout) view
						.findViewById(R.id.gridlayout));
				appItem.setIndex(index);
				appItem.setPageIndex(pageIndex);
				mPageList.add(view);
			}
			addView(appItem, pageIndex);
			Log.d(TAG, "bindall,index:" + index);
			index++;
			Log.d(TAG, "bindall,pageIndex:" + pageIndex);
			Log.d(TAG, "bindall,packageName:" + appItem.title);
		}
		mPagerAdpater.notifyDataSetChanged();
	}

	private void addView(ApplicationInfo appItem, int pageIndex) {
		GridLayout gridLayout = (GridLayout) mPageList.get(pageIndex)
				.getGridLayout();
		PagedViewIcon pagedViewIcon = new PagedViewIcon(this);
		LayoutParams layoutParams = new GridLayout.LayoutParams();
		int width = 260;
		int height = 230;
		if (displayType == HD) {
			width = Math.round(width / 1.5f);
			height = Math.round(height / 1.5f);
		}
		layoutParams.width = width;
		layoutParams.height = height;
		pagedViewIcon.setOnLongClickListener(mOnLongClickListener);
		pagedViewIcon.fastApplyFromAppInfo(appItem);
		pagedViewIcon.invalidate();
		gridLayout.addView(pagedViewIcon, layoutParams);

	}

	@Override
	public void bindAppsAdded(ArrayList<ApplicationInfo> apps) {
		int index = 0;
		int pageIndex = 0;
		// int width = 260;
		// int height = 230;
		int childCount = 0;
		for (ApplicationInfo app : apps) {
			GridLayout gridLayout = (GridLayout) mPageList.get(
					mPageList.size() - 1).getGridLayout();
			childCount = gridLayout.getChildCount();
			if (childCount < MAX_SIZE_PAGE) {
				index = childCount;
				pageIndex = mPageList.size() - 1;
				app.setIndex(index);
				app.setPageIndex(pageIndex);
			} else {
				Page view = (Page) mInflater.inflate(R.layout.page, null);
				view.setGridLayout((GridLayout) view
						.findViewById(R.id.gridlayout));
				mPageList.add(view);
				index = 0;
				pageIndex = mPageList.size() - 1;
				app.setIndex(index);
				app.setPageIndex(pageIndex);
			}
			Log.d(TAG, "bindadd reset index:" + index);
			Log.d(TAG, "bindadd reset pageIndex:" + pageIndex);
			Log.d(TAG, "bindadd reset packageName:" + app.title);
			addView(app, pageIndex);
		}
		mPagerAdpater.notifyDataSetChanged();
	}

	@Override
	public void bindAppsUpdated(ArrayList<ApplicationInfo> apps) {

	}

	@Override
	public void bindAppsRemoved(ArrayList<ApplicationInfo> apps,
			boolean permanent) {
		if (permanent) {
			boolean pageChange = false;
			int removeIndex = -1;
			for (ApplicationInfo app : apps) {
				Page page = mPageList.get(app.getPageIndex());
				GridLayout gridLayout = page.getGridLayout();
				removeIndex = app.getIndex();
				Log.d(TAG, "remove app index is " + removeIndex);
				gridLayout.removeViewAt(removeIndex);
				if (gridLayout.getChildCount() == 0) {
					mPageList.remove(page);
					pageChange = true;
				}
				if (gridLayout.getChildCount() == 1) {
					int lastPageindex = app.getPageIndex() - 1;
					if (lastPageindex >= 0 && lastPageindex < mPageList.size()) {
						GridLayout lastGridLayout = mPageList
								.get(lastPageindex).getGridLayout();
						if (lastGridLayout.getChildCount() < MAX_SIZE_PAGE) {
							lastGridLayout.addView(gridLayout.getChildAt(0));
							gridLayout.removeViewAt(0);
							mPageList.remove(page);
							pageChange = true;
						}
					}
				}
			}
			int pageIndex = 0;
			for (Page page : mPageList) {
				GridLayout gridLayout = page.getGridLayout();
				int childCount = gridLayout.getChildCount();
				for (int i = 0; i < childCount; i++) {
					PagedViewIcon childAt = (PagedViewIcon) gridLayout
							.getChildAt(i);
					ApplicationInfo appInfo = childAt.getAppInfo();
					appInfo.setPageIndex(pageIndex);
					appInfo.setIndex(i);
				}
				pageIndex++;
			}
			if (pageChange) {
				mPagerAdpater.notifyDataSetChanged();
				mViewPager.setCurrentItem(mPageList.size() - 1);
			}
		}
	}
}
