package com.example.usbhidcom;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

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
		Log.d(TAG,"in open()");
		File fd = new File("/dev/hidg0");

		try {
			if(inHid==null) {
				Log.d(TAG,"创建inHid");
				inHid = new FileInputStream(fd);
			}
			if(outHid==null)
				outHid = new FileOutputStream(fd);
		} catch (FileNotFoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

	}

	public void openInHid() // 打开文件
	{
		Log.d(TAG,"in openInHid()");
		File fd = new File("/dev/hidg0");

		try {
			if(inHid==null) {
				Log.d(TAG,"创建inHid");
				inHid = new FileInputStream(fd);
			}
		} catch (FileNotFoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

	}

	public void openOutHid() // 打开文件
	{
		Log.d(TAG,"in openOutHid()");
		File fd = new File("/dev/hidg0");

		try {
			if(outHid==null) {
				Log.d(TAG,"new outHid");
				outHid = new FileOutputStream(fd);
			}
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
static int count = 0;
	public boolean sendFile(String fileName) {
		FileInputStream reader = null;
		// 1. 读取文件输入流
		try {
//			reader = context.getAssets().open(fileName);
			File myfile =new File(fileName);
			Log.d(TAG,"myfile.getPath()="+myfile.getPath());
			reader = new FileInputStream(myfile);
			byte[] buf = new byte[REPORT_LEN];
			int read = 0;
			count =0;
			// 将文件输入流 循环 读入 Socket的输出流中
			while ((read = reader.read(buf, 2, REPORT_LEN-2)) != -1) {
				count++;
				//Log.d(TAG,"从文件读取字节数:"+read);
				buf[0] = (byte) ((read>>8)&0xFF);
				buf[1] = (byte) (read&0xFF);
				Log.d(TAG,"getHidState()="+getHidState());
				//Log.d(TAG,"buf="+ Util.toHexStringWithSpace(buf,REPORT_LEN));
				outHid.write(buf, 0,REPORT_LEN);
				outHid.flush();
				Arrays.fill(buf, (byte) 0x00);

				//				Log.d(TAG,"通知UI接受到数据"+RxCount);
//				Intent myIntent = new Intent();
//				myIntent.setAction("com.sdses.action.usbrecv");	//指定消息接收者
//				myIntent.putExtra("USBdata", read);
//				context.sendBroadcast(myIntent);
			}
			Log.i(TAG, "发送数据结束count="+count);
			Intent myIntent = new Intent();
			myIntent.setAction("com.sdses.action.usbrecv");	//指定消息接收者
			myIntent.putExtra("USBdata", count);
			context.sendBroadcast(myIntent);
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
				Arrays.fill(tempBuf, (byte) 0x00);
				tempBuf[0] = (byte) (REPORT_LEN - 1);// 保存传输的字节
				System.arraycopy(sendBuf, offset, tempBuf, 1, REPORT_LEN - 1);
				tempLeft -= (REPORT_LEN - 1);
				offset += REPORT_LEN - 1;
			} else {
				Arrays.fill(tempBuf, (byte) 0x00);
				tempBuf[0] = (byte) tempLeft; // 保存传输的字节，
				System.arraycopy(sendBuf, offset, tempBuf, 1, tempLeft);
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

	byte tempBuf[] = new byte[REPORT_LEN];
	// 读报告，自定义报告首字节存包的数据长度
	public int readReport(byte RxBuf[]) {
		//Log.d(TAG,"in readReport()");
		int count = 0;
		Arrays.fill(tempBuf, (byte) 0x00);
		try {
			//if(inHid.available()<0)
			//	return -1;
			if (inHid.read(tempBuf, 0, REPORT_LEN) <=0) { // 读取报告
				Log.d(TAG,"未读取到数据");
				return -1;
			} else {
				count = ((tempBuf[0] & 0xFF)<<8)|(tempBuf[1] & 0xFF); // 注意 ，最大是REPORT_LEN
				//count = (tempBuf[0] & 0xFF);
				//Log.d(TAG,"count="+count);
				// -1，因为用一个当做自定义存放长度
				System.arraycopy(tempBuf, 2, RxBuf, 0, count);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return count;
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
