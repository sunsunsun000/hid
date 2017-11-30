package com.sdses.tool;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;

/**
 * util类主要实现常规操作
 * 
 * @author 山东神思电子系统有限公司
 * @version v1.0.0
 */
public class Util {
	public Util() {
	}

	/**
	 * 将字符串生成的数组pBCD转换成16进制的数组
	 * 
	 * @param pBCD
	 *            字符串生成的数组
	 * @param len
	 *            字符串数组的程度
	 * @param hex
	 *            输出16进制数组的缓冲区
	 * @return hex的长度
	 * */
	public static int BCDtoHex(byte[] pBCD, int len, byte[] hex) {
		int i, nlen;
		nlen = len;
		i = 0;
		while (i < nlen / 2) {
			hex[i] = (byte) (((pBCD[i * 2] > 0x39) ? (pBCD[i * 2] - 0x37)
					: pBCD[i * 2] - 0x30) << 4);
			hex[i] |= (byte) ((pBCD[i * 2 + 1] > 0x39) ? pBCD[i * 2 + 1] - 0x37
					: pBCD[i * 2 + 1] - 0x30);
			i++;
		}
		return i;
	}

	/**
	 * 将字符串生成的数组pBCD转换成16进制的数组
	 * 
	 * @param hex
	 *            输出16进制数组的缓冲区
	 * @return hex的长度
	 * */
	public static int HexString2Bytes(String sSrc, byte[] hex) {
		int i, n;
		byte[] tmp;
		n = sSrc.length();
		tmp = sSrc.getBytes();
		for (i = 0; i < n; i += 2) {
			hex[i / 2] = (byte) (((tmp[i] > 0x39) ? (tmp[i] - 0x37)
					: (tmp[i] - 0x30)) << 4);
			hex[i / 2] |= (byte) ((tmp[i + 1] > 0x39) ? (tmp[i + 1] - 0x37)
					: (tmp[i + 1] - 0x30));
		}
		return n / 2;
	}
	
	public static int HexString2Bytes(String sSrc) {
		int i, n;
		byte[] tmp;
		n = sSrc.length();
		byte[] hex = new byte[n/2];
		tmp = sSrc.getBytes();
		for (i = 0; i < n; i += 2) {
			hex[i / 2] = (byte) (((tmp[i] > 0x39) ? (tmp[i] - 0x37)
					: (tmp[i] - 0x30)) << 4);
			hex[i / 2] |= (byte) ((tmp[i + 1] > 0x39) ? (tmp[i + 1] - 0x37)
					: (tmp[i + 1] - 0x30));
		}
		return HexString2Bytes(hex);
	}

	public static int HexString2Bytes(byte[] hex) {
		return ((hex[0]&0xFF)<<8)|(hex[1]&0xFF);
	}
	
	public static void writeLogToSD(String context) {
		return;
		/*
		 * 0316File sdDir = null; String sdStatus =
		 * Environment.getExternalStorageState(); if
		 * (!sdStatus.equals(Environment.MEDIA_MOUNTED)) { Log.d("TestFile",
		 * "SD card is not avaiable/writeable right now."); return; } try {
		 * String pathName = "/mnt/sdcard/ApkFiles/";//0316
		 * "/mnt/sdcard2/";external_sd 没有后面的/时 写日志不能成功 //sdDir=
		 * Environment.getExternalStorageDirectory(); //String pathName =
		 * sdDir.toString(); String fileName = "wusdk.txt"; File path = new
		 * File(pathName); File file = new File(pathName + fileName); if
		 * (!path.exists()) { Log.d("TestFile", "Create the path:" + pathName);
		 * path.mkdir(); } if (!file.exists()) { Log.d("TestFile",
		 * "Create the file:" + fileName); file.createNewFile(); }
		 * RandomAccessFile raf = new RandomAccessFile(file, "rw");
		 * raf.seek(file.length()); raf.write(context.getBytes());
		 * raf.writeBytes("\r\n"); raf.close(); } catch (Exception e) {
		 * Log.e("TestFile", "Error on writeFilToSD."+e.toString()); }
		 */
	}

	/**
	 * 拷贝byte数组
	 * 
	 * @param Dst
	 *            目标数组缓冲区
	 * @param dstStartPos
	 *            目标数组拷贝起始位置
	 * @param Src
	 *            源数据缓冲区
	 * @param srcStartPos
	 *            源数据开始拷贝起始位置
	 * @param data_len
	 *            源数据开始拷贝数据长度
	 * */
	public static void memcpy(byte[] Dst, int dstStartPos, byte[] Src,
			int srcStartPos, int data_len) {
		int i = 0;
		for (i = 0; i < data_len; i++) {
			Dst[dstStartPos + i] = Src[srcStartPos + i];
		}
	}

	/**
	 * 比较两个数组
	 * 
	 * @param buf1
	 *            第一个数组
	 * @param buf2
	 *            第二个数组
	 * @param len
	 *            两个数组要比较的长度
	 * @return 两个数组在比较区域相同，返回0，反之返回1
	 * */
	public static int memcmp(byte[] buf1, byte[] buf2, int len) {
		int i = 0;
		while (i < len) {
			if (buf1[i] != buf2[i]) {
				return 1;// 不一致
			}
			i++;
		}
		return 0;
	}

	/**
	 * 初始化数组
	 * 
	 * @param Dst
	 *            初始化目标缓冲区，byte[]类型
	 * @param value
	 *            初始化值
	 * @param count
	 *            目标缓冲区要初始化的长度
	 * */
	public static void memset(byte[] Dst, int value, int count) {
		int i = 0;
		for (; i < count; i++) {
			Dst[i] = (byte) value;
		}
	}

	static String toHexStringNoSpace(byte b) {
		char[] buffer = new char[2];
		buffer[0] = Character.forDigit((b >> 4) & 0x0F, 16);
		buffer[1] = Character.forDigit(b & 0x0F, 16);
		return new String(buffer);
	}

	/**
	 * 将byte[]转换成16进制字符串，中间没有空格
	 * 
	 * @param b
	 *            byte数组
	 * @param len
	 *            需要转换的数组长度
	 * @return 转换后的16进制字符串（不带空格），例如：0FABDC....
	 * */
	public static String toHexStringNoSpace(byte[] b, int len) {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < len; ++i) {
			buffer.append(toHexStringNoSpace(b[i]));
		}
		return buffer.toString().toUpperCase();
	}

	private static String toHexStringWithSpace(byte b) {
		char[] buffer = new char[2];
		buffer[0] = Character.forDigit((b >> 4) & 0x0F, 16);
		buffer[1] = Character.forDigit(b & 0x0F, 16);
		return new String(buffer) + " ";
	}

	private static String toHexStringWithSpace(char b) {
		char[] buffer = new char[2];
		buffer[0] = Character.forDigit((b >> 4) & 0x0F, 16);
		buffer[1] = Character.forDigit(b & 0x0F, 16);
		return new String(buffer) + " ";
	}

	/**
	 * 将byte[]转换成16进制字符串，中间有空格
	 * 
	 * @param b
	 *            byte数组
	 * @param len
	 *            需要转换的数组长度
	 * @return 转换后的16进制字符串（带空格），例如：0F AB DC....
	 * */
	public static String toHexStringWithSpace(byte[] b, int len) {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < len; ++i) {
			buffer.append(toHexStringWithSpace(b[i]));
		}
		return buffer.toString().toUpperCase();
	}

	public static String toHexStringWithSpace(char[] b, int len) {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < len; ++i) {
			buffer.append(toHexStringWithSpace(b[i]));
		}
		return buffer.toString().toUpperCase();
	}

	/**
	 * 将16进制转换成整型
	 * 
	 * @param b
	 *            16进制
	 * @return 转换后的整型
	 * */
	public static int HexToInt(byte b) {
		return Integer.parseInt(toHexStringNoSpace(b), 16);
	}

	// 未使用的函数
	public static byte[] long2Byte(long x) {
		byte[] bb = new byte[8];
		bb[0] = (byte) (x >> 56);
		bb[1] = (byte) (x >> 48);
		bb[2] = (byte) (x >> 40);
		bb[3] = (byte) (x >> 32);
		bb[4] = (byte) (x >> 24);
		bb[5] = (byte) (x >> 16);
		bb[6] = (byte) (x >> 8);
		bb[7] = (byte) (x >> 0);
		return bb;
	}
	
	public static boolean ByteToFile(byte[] in,String path){
		OutputStream out  = null;
		try {
			File myCaptureFile = new File(path);
			if (!myCaptureFile.exists()) {
				String filepath = myCaptureFile.getParent();
//			log.debug("filepath="+filepath);
				File dir = new File(filepath);
				dir.mkdirs();
				myCaptureFile.createNewFile();
			}
			out = new FileOutputStream(myCaptureFile);
			out.write(in);
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return true;
	}
	
	public static byte[] ByteArrayFormFile(String filePath) {
		InputStream in = null;
		byte[] data = null;
		// 读取图片字节数组
		try {
			in = new FileInputStream(filePath);
			data = new byte[in.available()];
			in.read(data);
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			try {
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// 对字节数组Base64编码
//		return Base64.encodeToString(data, Base64.DEFAULT);// 返回Base64编码过的字节数组字符串
		return data;
	}
	
	public static boolean saveBmpToLocal(String fileName, Bitmap bitmap) {
//		String fileName = getFileName(strFilePath);
//		log.debug("fileName=" + fileName);
		Bitmap bm = Bitmap.createScaledBitmap(bitmap, 240, 320, true);
		try {
			File myCaptureFile = new File(fileName);
			if (!myCaptureFile.exists()) {
				String filepath = myCaptureFile.getParent();
//				log.debug("filepath="+filepath);
				File dir = new File(filepath);
				dir.mkdirs();
				myCaptureFile.createNewFile();
			}
			BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream(myCaptureFile));
			/* 采用压缩转档方法 */
			bm.compress(Bitmap.CompressFormat.JPEG, 60, bos);

			/* 调用flush()方法，更新BufferStream */
			bos.flush();

			/* 结束OutputStream */
			bos.close();

			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	
	public static void sendDelayMessage(Context context, Intent intent, int milliseconds) {
		AlarmManager am = (AlarmManager) context.getSystemService(android.content.Context.ALARM_SERVICE);
		PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent,
				PendingIntent.FLAG_CANCEL_CURRENT);
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.add(Calendar.MILLISECOND, milliseconds);
		am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
	}
	public static String getSAMSN(byte[] Src,int from) {
		if(Src==null)
			return null;
		short head1 = 0, head2 = 0;
		long data1 = 0, data2 = 0, data3 = 0;
		String dstStr = null;
		for (int i =0;i<Src.length-from;i++) {
			Src[i] = Src[i+from];
		}
		head1 = (short) (((Src[1] & 0xFF) << 8) | (Src[0] & 0xFF));
		head2 = (short) (((Src[3] & 0xFF) << 8) | (Src[2] & 0xFF));

		data1 = (long) ((Src[4] & 0xFF) | ((Src[5] & 0xFF) << 8)
				| ((Src[6] & 0xFF) << 16) | ((Src[7] & 0xFF) << 24));
		data2 = (long) ((Src[8] & 0xFF) | ((Src[9] & 0xFF) << 8)
				| ((Src[10] & 0xFF) << 16) | ((Src[11] & 0xFF) << 24));
		data3 = (long) ((Src[12] & 0xFF) | ((Src[13] & 0xFF) << 8)
				| ((Src[14] & 0xFF) << 16) | ((Src[15] & 0xFF) << 24));
//		dstStr = String.format("%02d.%02d-%08d-%010d-%010d", head1, head2,
//				data1, data2, data3);
		dstStr = String.format("%02d.%02d-%08d-%010d", head1, head2,
				data1, data2);
		return dstStr;
	}
}
