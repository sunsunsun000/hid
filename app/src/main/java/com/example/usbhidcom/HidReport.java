package com.example.usbhidcom;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class HidReport {

	private static final String TAG = HidReport.class.getSimpleName();
	private FileOutputStream outHid = null;
	private FileInputStream inHid = null;

	final static short REPORT_LEN = 512; // 报告的长度，由驱动定义
	Context context = null;
	
	
	public HidReport(Context context) {
		super();
		this.context = context;
	}

	public void open() // 打开文件
	{
		File fd = new File("/dev/hidg0");

		try {
			outHid = new FileOutputStream(fd);
			inHid = new FileInputStream(fd);
		} catch (FileNotFoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

	}

	public void close() {
		try {
			outHid.close();
			inHid.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean sendFile(String fileName) {
		FileInputStream reader = null;
		// 1. 读取文件输入流
		try {
//			reader = context.getAssets().open(fileName);
			reader = new FileInputStream(fileName);
			byte[] buf = new byte[REPORT_LEN+1];
			int read = 0;
			// 将文件输入流 循环 读入 Socket的输出流中
			while ((read = reader.read(buf, 1, REPORT_LEN)) != -1) {
				buf[0] = (byte) read;
				Log.d(TAG,"从文件读取字节数:"+read);
				outHid.write(buf, 0, read);
				outHid.flush();
			}
			Log.i(TAG, "发送数据结束");
			reader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	// 按包进行发送，一个包长度是255字节（这由hid驱动确定）,包得第一个字节存放包的长度（可自定义）
	public boolean SendReport(byte sendBuf[]) {
		byte tempBuf[] = new byte[REPORT_LEN];
		int tempLeft = sendBuf.length;
		int offset = 0;
		int i = 0;

		while (tempLeft > 0) {
			if (tempLeft > (REPORT_LEN - 1)) {
				System.arraycopy(sendBuf, offset, tempBuf, 1, REPORT_LEN - 1);
				tempLeft -= (REPORT_LEN - 1);
				tempBuf[0] = (byte) (REPORT_LEN - 1);// 保存传输的字节
				offset += REPORT_LEN - 1;
			} else {
				System.arraycopy(sendBuf, offset, tempBuf, 1, tempLeft);
				tempBuf[0] = (byte) tempLeft; // 保存传输的字节，
				for (i = 1 + tempLeft; i < REPORT_LEN; i++) // 补0
				{
					tempBuf[i] = 0;
				}
				tempLeft = -1;
			}
			try {
				outHid.write(tempBuf);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		}

		return true;
	}

	// 读报告，自定义报告首字节存包的数据长度
	public int readReport(byte RxBuf[]) {
		int count = 0;
		byte tempBuf[] = new byte[REPORT_LEN];

		try {
			if (inHid.read(tempBuf, 0, REPORT_LEN) <= 0) { // 读取报告
				return -1;
			} else {
				count = tempBuf[0] & 0xff; // 注意 ，最大是REPORT_LEN
											// -1，因为用一个当做自定义存放长度
				System.arraycopy(tempBuf, 1, RxBuf, 0, count);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return count;
	}

	// 返回设备连接状态
	public String getHidState() {
		return execShellCmd("cat /sys/class/hidg/hidg0/usb_hid_state");
	}

	//
	public static String execShellCmd(String command) {
		String result = "1";
		// Log.i("execShellCmd", command);
		// try {
		// Process process = Runtime.getRuntime().exec(command + "\n");
		// DataOutputStream stdin = new DataOutputStream(
		// process.getOutputStream());
		// DataInputStream stdout = new DataInputStream(
		// process.getInputStream());
		// DataInputStream stderr = new DataInputStream(
		// process.getErrorStream());
		// String line;
		// while ((line = stdout.readLine()) != null) {
		// result += line + "\n";
		// }
		// if (result.length() > 0) {
		// result = result.substring(0, result.length() - 1);
		// }
		// while ((line = stderr.readLine()) != null) {
		// Log.e("EXEC", line);
		// }
		// process.waitFor();
		// } catch (Exception e) {
		// e.getMessage();
		// }
		return result;
	}
}
