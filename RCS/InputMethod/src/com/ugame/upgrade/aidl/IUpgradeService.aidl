package  com.ugame.upgrade.aidl;
 

interface IUpgradeService {
	/**
	 *升级结果
	 * 
	 * @return
	 */
	 void onResult(String result);
	
	/**
	 * 当升级完成给U游APP设置版本号
	 * 
	 * @return
	 */
	 void onUpgradeFinish(String versionName);
	 
	 /**
	 * 获得U游版本号
	 * 
	 * @return
	 */
	 String getPcVersion();
}
