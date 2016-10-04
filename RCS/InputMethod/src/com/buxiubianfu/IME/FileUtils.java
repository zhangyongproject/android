package com.buxiubianfu.IME;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;

/**
 * 描述:文件相关的工具类
 */
@SuppressLint("DefaultLocale")
public class FileUtils {

	private static final String TAG = "FileUtil";

	// /**
	// * 写文�?
	// * @param inStream
	// * @param filename
	// */
	// public static void writeFile(InputStream inStream, String filename) {
	// OutputStream outStream = null;
	// try {
	// outStream = new FileOutputStream(filename);
	// // 创建�?��Buffer字符�?
	// byte[] buffer = new byte[1024];
	// // 每次读取的字符串长度，如果为-1，代表全部读取完�?
	// int len = 0;
	// // 使用�?��输入流从buffer里把数据读取出来
	// while ((len = inStream.read(buffer)) != -1) {
	// // 用输出流�?uffer里写入数据，中间参数代表从哪个位置开始读，len代表读取的长�?
	// outStream.write(buffer, 0, len);
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// } finally {
	// try {
	// if (inStream != null)
	// inStream.close();
	// if (outStream != null)
	// outStream.close();
	// } catch (Exception e2) {
	// e2.printStackTrace();
	// }
	// }
	// }

	/**
	 * 写文�?
	 * 
	 * @param in
	 *            �?
	 * @param filePath
	 *            目标文件
	 */
	public static void writeFile(InputStream in, String filePath) {
		int size;
		byte[] buffer = new byte[1000];
		BufferedOutputStream bufferedOutputStream = null;
		try {
			bufferedOutputStream = new BufferedOutputStream(
					new FileOutputStream(new File(filePath)));
			while ((size = in.read(buffer)) > -1) {
				bufferedOutputStream.write(buffer, 0, size);
			}
			bufferedOutputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 写入文件
	 * 
	 * @param file
	 *            文件
	 * @param datas
	 *            写入的内�?
	 * @return 是否成功
	 */
	public static boolean writeFile(File file, List<byte[]> datas) {
		BufferedOutputStream bos = null;
		try {
			bos = new BufferedOutputStream(new FileOutputStream(file));
			for (byte[] data : datas) {
				bos.write(data);
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, e.getMessage());
		} finally {
			if (bos != null) {
				try {
					bos.close();
				} catch (IOException e) {
					e.printStackTrace();
					Log.e(TAG, e.getMessage());
				}
			}
		}
		return false;
	}

	/**
	 * 追加数据到文件中
	 * 
	 * @param file
	 *            文件
	 * @param datas
	 *            写入的内�?
	 * @return
	 */
	public static boolean writeFile(File file, byte[]... datas) {
		RandomAccessFile rfile = null;
		try {
			rfile = new RandomAccessFile(file, "rw");
			rfile.seek(file.length());
			for (byte[] data : datas) {
				rfile.write(data);
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (rfile != null) {
				try {
					rfile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	/**
	 * 写文�?
	 * 
	 * @param path
	 *            文件路径
	 * @param content
	 *            内容
	 * @param append
	 *            是否追加
	 */
	public static void writeFile(String path, String content, boolean append) {
		try {
			File f = new File(path);
			if (!f.getParentFile().exists()) {
				f.getParentFile().mkdirs();
			}
			if (!f.exists()) {
				f.createNewFile();
				f = new File(path);
			}
			FileWriter fw = new FileWriter(f, append);
			if ((content != null) && !"".equals(content)) {
				fw.write(content);
				fw.flush();
			}
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 创建文件目录
	 * 
	 * @param dir
	 *            目录
	 */
	public static void createDir(String dir) {
		File f = new File(dir);
		if (!f.exists()) {
			f.mkdirs();
		}

	}

	/**
	 * 创建文件
	 * 
	 * @param filePath
	 *            文件路径
	 * @return
	 */
	public static File createFile(String filePath) {
		File f = new File(filePath);
		if (!f.getParentFile().exists()) {
			f.getParentFile().mkdirs();
		}
		if (!f.exists()) {
			try {
				f.createNewFile();
				f = new File(filePath); // 重新实例�?
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		return f;
	}

	/**
	 * 删除某个文件
	 * 
	 * @param path
	 *            文件路径
	 */
	public static void delFile(String path) {
		try {
			File f = new File(path);
			if (f.exists()) {
				f.delete();
			}
		} catch (Exception e) {
		}
	}

	/**
	 * 删除文件�?
	 * 
	 * @param folderPath
	 *            文件夹路�?
	 */
	public static void delFolder(String folderPath) {
		try {
			delAllFile(folderPath); // 删除完里面所有内�?
			String filePath = folderPath;
			filePath = filePath.toString();
			File f = new File(filePath);
			f.delete(); // 删除空文件夹
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 删除文件�?
	 * 
	 * @param folderPath
	 *            文件夹路�?
	 */
	public static boolean delAllFile(String path) {
		boolean flag = false;
		File file = new File(path);
		if (!file.exists()) {
			return flag;
		}
		if (!file.isDirectory()) {
			return flag;
		}
		String[] tempList = file.list();
		File temp = null;
		for (int i = 0; i < tempList.length; i++) {
			if (path.endsWith(File.separator)) {
				temp = new File(path + tempList[i]);
			} else {
				temp = new File(path + File.separator + tempList[i]);
			}
			if (temp.isFile()) {
				temp.delete();
			}
			if (temp.isDirectory()) {
				delAllFile(path + "/" + tempList[i]);// 先删除文件夹里面的文�?
				delFolder(path + "/" + tempList[i]);// 再删除空文件�?
				flag = true;
			}
		}
		return flag;
	}

	/**
	 * 拷贝文件
	 * 
	 * @param srcFile
	 *            源文件路�?
	 * @param destFile
	 *            目标文件路径
	 * @return
	 */
	public static boolean copyFile(String srcFile, String destFile) {
		try {
			FileInputStream in = new FileInputStream(srcFile);
			FileOutputStream out = new FileOutputStream(destFile);
			byte[] bytes = new byte[1024];
			int c;
			while ((c = in.read(bytes)) != -1) {
				out.write(bytes, 0, c);
			}
			in.close();
			out.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 拷贝文件�?
	 * 
	 * @param oldPath
	 *            旧文件夹路径
	 * @param newPath
	 *            新文件夹路径
	 */
	public static void copyFolder(String oldPath, String newPath) {
		(new File(newPath)).mkdirs();
		File a = new File(oldPath);
		String[] file = a.list();
		if (null == file)
			return;
		File temp = null;
		for (int i = 0; i < file.length; i++) {
			try {
				if (oldPath.endsWith(File.separator)) {
					temp = new File(oldPath + file[i]);
				} else {
					temp = new File(oldPath + File.separator + file[i]);
				}

				if (temp.isFile()) {
					FileInputStream input = new FileInputStream(temp);
					FileOutputStream output = new FileOutputStream(newPath
							+ "/" + (temp.getName()).toString());
					byte[] b = new byte[1024 * 5];
					int len;
					while ((len = input.read(b)) != -1) {
						output.write(b, 0, len);
					}
					output.flush();
					output.close();
					input.close();
				}
				if (temp.isDirectory()) {// 如果是子文件�?
					copyFolder(oldPath + "/" + file[i], newPath + "/" + file[i]);
				}
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
		}
	}

	/**
	 * 移动文件
	 * 
	 * @param oldPath
	 *            旧文件路�?
	 * @param newPath
	 *            就文件路�?
	 */
	public static void moveFile(String oldPath, String newPath) {
		copyFile(oldPath, newPath);
		delFile(oldPath);

	}

	/**
	 * 移动文件�?
	 * 
	 * @param oldPath
	 *            旧文件夹路径
	 * @param newPath
	 *            就文件夹路径
	 */
	public static void moveFolder(String oldPath, String newPath) {
		copyFolder(oldPath, newPath);
		delFolder(oldPath);
	}

	/**
	 * 重命名文�?
	 * 
	 * @param resFilePath
	 *            文件
	 * @param newFilePath
	 *            被命名的文件
	 * @return
	 */
	public static boolean renameFile(String resFilePath, String newFilePath) {
		File resFile = new File(resFilePath);
		File newFile = new File(newFilePath);
		return resFile.renameTo(newFile);
	}

	// /**
	// * 获取图片反射�?
	// */
	// public static FileFilter imagefileFilter = new FileFilter() {
	// @Override
	// public boolean accept(File pathname) {
	// String tmp = pathname.getName().toLowerCase();
	// if (tmp.endsWith(".png") || tmp.endsWith(".jpg") || tmp.endsWith(".bmp")
	// || tmp.endsWith(".gif") || tmp.endsWith(".jpeg")) {
	// return true;
	// }
	// return false;
	// }
	// };
	//
	// /**
	// * @Fields mp3fileFilter : 获取MP3反射�?
	// */
	// @SuppressLint("DefaultLocale")
	// public static FileFilter mp3fileFilter = new FileFilter() {
	// @Override
	// public boolean accept(File pathname) {
	// String tmp = pathname.getName().toLowerCase();
	// if (tmp.endsWith(".mp3")) {
	// return true;
	// }
	// return false;
	// }
	// };

	/**
	 * 获取路径下所有的 文件
	 * 
	 * @param dirPath
	 *            文件夹路�?
	 * @param fileFilter
	 * @return
	 */
	public static File[] getFilesFromDir(String dirPath, FileFilter fileFilter) {
		File dir = new File(dirPath);
		if (dir.isDirectory()) {
			if (fileFilter != null)
				return dir.listFiles(fileFilter);
			else
				return dir.listFiles();
		}
		return null;
	}

	/**
	 * 获取�?��的文件名�?
	 * 
	 * @param dir
	 *            文件夹路�?
	 * @param fileFilter
	 * @param hasSuffix
	 * @return
	 */
	public static List<String> getExistsFileNames(String dir,
			FileFilter fileFilter, boolean hasSuffix) {
		String path = dir;
		File file = new File(path);
		File[] files = file.listFiles(fileFilter);
		List<String> fileNameList = new ArrayList<String>();
		if (null != files) {
			for (File tmpFile : files) {
				String tmppath = tmpFile.getAbsolutePath();
				String fileName = getFileName(tmppath, hasSuffix);
				fileNameList.add(fileName);
			}
		}
		return fileNameList;
	}

	/**
	 * 获取�?��的文件名�?
	 * 
	 * @param dir
	 * @param hasSuffix
	 * @param suffix
	 * @return
	 */
	public static List<String> getAllExistsFileNames(String dir,
			boolean hasSuffix, String[] suffix) {
		String path = dir;
		File file = new File(path);
		File[] files = file.listFiles();
		List<String> fileNameList = new ArrayList<String>();
		if (null != files) {
			for (File tmpFile : files) {
				if (tmpFile.isDirectory()) {
					fileNameList.addAll(getAllExistsFileNames(
							tmpFile.getPath(), hasSuffix, suffix));
				} else {
					String tmp = tmpFile.getName().toLowerCase();
					if (suffix != null) {
						for (String s : suffix) {
							if (tmp.endsWith(s)) {
								fileNameList.add(tmpFile.getAbsolutePath());
							}
						}
					} else {
						fileNameList.add(tmpFile.getAbsolutePath());
					}
				}
			}
		}
		return fileNameList;
	}

	/**
	 * 获取文件�?
	 * 
	 * @param path
	 * @param hasSuffix
	 * @return
	 */
	public static String getFileName(String path, boolean hasSuffix) {
		if (null == path || -1 == path.lastIndexOf("/")
				|| -1 == path.lastIndexOf("."))
			return null;
		if (!hasSuffix)
			return path.substring(path.lastIndexOf("/") + 1,
					path.lastIndexOf("."));
		else
			return path.substring(path.lastIndexOf("/") + 1);
	}

	/**
	 * 获取目录
	 * 
	 * @param path
	 * @return
	 */
	public static String getPath(String path) {
		File file = new File(path);

		try {
			if (!file.exists() || !file.isDirectory())
				file.mkdirs();
			return file.getAbsolutePath();

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 指定目录下是否存在指定名称的文件
	 * 
	 * @param dir
	 * @param fileName
	 * @return
	 */
	public static boolean isFileExits(String dir, String fileName) {
		fileName = fileName == null ? "" : fileName;
		dir = dir == null ? "" : dir;
		int index = dir.lastIndexOf("/");
		String filePath;
		if (index == dir.length() - 1)
			filePath = dir + fileName;
		else
			filePath = dir + "/" + fileName;
		File file = new File(filePath);
		return file.exists();
	}

	/**
	 * 文件是否存在
	 * 
	 * @param filePath
	 * @return
	 */
	public static boolean isFileExits(String filePath) {
		try {
			File file = new File(filePath);
			if (file.exists())
				return true;
		} catch (Exception e) {

		}
		return false;
	}

	/**
	 * 保存图片
	 * 
	 * @param path
	 * @param fileName
	 * @param bmp
	 * @return
	 */
	public static boolean saveImageFile(String dirPath, String fileName,
			Bitmap bmp) {
		try {
			File dir = new File(dirPath);

			// 目录不存时创建目�?
			if (!dir.exists()) {
				boolean flag = dir.mkdirs();
				if (flag == false)
					return false;
			}

			// 未指定文件名时取当前毫秒作为文件�?
			if (fileName == null || fileName.trim().length() == 0)
				fileName = System.currentTimeMillis() + ".jpg";
			File picPath = new File(dirPath, fileName);
			FileOutputStream fos = new FileOutputStream(picPath);
			bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
			fos.flush();
			fos.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 保存图片
	 * 
	 * @Description:
	 * @param @param dirPath 目的目录
	 * @param @param fileName 文件�?
	 * @param @param bmp 图片
	 * @param @param format
	 * @param @return
	 * @return boolean
	 * @throws
	 */
	public static boolean saveImageFile(String dirPath, String fileName,
			Bitmap bmp, Bitmap.CompressFormat format) {
		try {
			File dir = new File(dirPath);

			// 目录不存时创建目�?
			if (!dir.exists()) {
				boolean flag = dir.mkdirs();
				if (flag == false)
					return false;
			}

			format = format == null ? Bitmap.CompressFormat.JPEG : format;
			// 未指定文件名时取当前毫秒作为文件�?
			if (fileName == null || fileName.trim().length() == 0) {
				fileName = System.currentTimeMillis() + "";
				if (format.equals(Bitmap.CompressFormat.PNG))
					fileName += ".png";
				else
					fileName += ".jpg";
			}
			File picPath = new File(dirPath, fileName);
			FileOutputStream fos = new FileOutputStream(picPath);
			bmp.compress(format, 100, fos);
			fos.flush();
			fos.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 获取文件长度
	 * 
	 * @Description:
	 * @param @param path
	 * @param @return
	 * @return long
	 * @throws
	 */
	public static long getFileAllSize(String path) {
		File file = new File(path);
		if (file.exists()) {
			if (file.isDirectory()) {
				File[] children = file.listFiles();
				long size = 0;
				for (File f : children)
					size += getFileAllSize(f.getPath());
				return size;
			} else {
				long size = file.length();
				return size;
			}
		} else {
			return 0;
		}

	}

	/**
	 * 读取文件内容
	 * 
	 * @Description:
	 * @param @param path
	 * @param @return
	 * @return String
	 * @throws
	 */

	public static String readFileContent(String path) {
		StringBuffer sb = new StringBuffer();
		if (!isFileExits(path)) {
			return sb.toString();
		}
		InputStream ins = null;
		try {
			ins = new FileInputStream(new File(path));
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					ins));
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (ins != null) {
				try {
					ins.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return sb.toString();
	}

	/**
	 * 拷贝Assets文件到其他地�?
	 * 
	 * @param context
	 * @param srcFileName
	 * @param targetDir
	 * @param targetFileName
	 * @return
	 */
	public static boolean copyAssetsFile(Context context, String srcFileName,
			String targetDir, String targetFileName) {
		AssetManager asm = null;
		FileOutputStream fos = null;
		DataInputStream dis = null;
		try {
			asm = context.getAssets();
			dis = new DataInputStream(asm.open(srcFileName));
			createDir(targetDir);
			File targetFile = new File(targetDir, targetFileName);
			if (targetFile.exists()) {
				targetFile.delete();
			}

			fos = new FileOutputStream(targetFile);
			byte[] buffer = new byte[1024];
			int len = 0;
			while ((len = dis.read(buffer)) != -1) {
				fos.write(buffer, 0, len);
			}
			fos.flush();
			return true;
		} catch (Exception e) {
			Log.w(TAG, "copy assets file failed:" + e.toString());
		} finally {
			try {
				if (fos != null)
					fos.close();
				if (dis != null)
					dis.close();
			} catch (Exception e2) {
			}
		}

		return false;
	}

	/**
	 * 读取Assets文件
	 * 
	 * @param context
	 * @param srcFileName
	 * @return
	 */
	@SuppressWarnings({ "unused", "null" })
	public static String readAssetsFile(Context context, String srcFileName) {
		AssetManager asm = null;
		FileOutputStream fos = null;
		DataInputStream dis = null;
		try {
			asm = context.getAssets();
			dis = new DataInputStream(asm.open(srcFileName));
			StringBuffer sb = new StringBuffer();
			byte[] buffer = new byte[1024];
			int len = 0;
			while ((len = dis.read(buffer)) != -1) {
				sb.append(buffer);
			}
			fos.flush();
			return sb.toString();
		} catch (Exception e) {
			Log.w(TAG, "copy assets file failed:" + e.toString());
		} finally {
			try {
				if (fos != null)
					fos.close();
				if (dis != null)
					dis.close();
			} catch (Exception e2) {
			}
		}

		return null;
	}

	/**
	 * 解压缩功�? 将ZIP_FILENAME文件解压到ZIP_DIR目录�?
	 * 
	 * @param zipPath
	 * @param folderPath
	 * @return
	 * @throws ZipException
	 * @throws IOException
	 */
	public static int upZipFile(String zipPath, String folderPath)
			throws ZipException, IOException {
		File zipFile = new File(zipPath);
		if (!zipFile.exists()) {
			return 1;
		}
		File file = new File(folderPath);
		if (file.exists()) {
			file.delete();
		}
		// public static void upZipFile() throws Exception{
		ZipFile zfile = new ZipFile(zipFile);
		Enumeration zList = zfile.entries();
		ZipEntry ze = null;
		byte[] buf = new byte[1024];
		while (zList.hasMoreElements()) {
			ze = (ZipEntry) zList.nextElement();
			if (ze.isDirectory()) {
				Log.d("upZipFile", "ze.getName() = " + ze.getName());
				String dirstr = folderPath + ze.getName();
				// dirstr.trim();
				dirstr = new String(dirstr.getBytes("8859_1"), "GB2312");
				Log.d("upZipFile", "str = " + dirstr);
				File f = new File(dirstr);
				f.mkdirs();
				continue;
			}
			Log.d("upZipFile", "ze.getName() = " + ze.getName());

			OutputStream os = new BufferedOutputStream(new FileOutputStream(
					getRealFileName(folderPath, ze.getName())));
			InputStream is = new BufferedInputStream(zfile.getInputStream(ze));
			int readLen = 0;
			while ((readLen = is.read(buf, 0, 1024)) != -1) {
				os.write(buf, 0, readLen);
			}
			is.close();
			os.close();
		}
		zfile.close();
		return 0;
	}

	/**
	 * @param baseDir
	 * @param absFileName
	 * @return
	 */
	public static File getRealFileName(String baseDir, String absFileName) {
		File ret = new File(baseDir);
		if (!ret.exists()) {
			ret.mkdirs();
		}
		String suffixStr = absFileName.substring(absFileName.lastIndexOf("."),
				absFileName.length());
		// 实在不想转码�?
		absFileName = "a" + suffixStr;

		// absFileName = new String(absFileName.getBytes("GBK"), "UTF-8");
		ret = new File(ret, absFileName);
		return ret;

	}
}
