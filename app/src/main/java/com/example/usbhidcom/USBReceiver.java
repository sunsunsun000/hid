package com.example.usbhidcom;

import android.content.Context;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

//USB堵塞方式接收数据
public class USBReceiver extends Thread {
	private static final String TAG = USBReceiver.class.getSimpleName();
	private Context myContext;
	HidReport hidOp = null;

	byte tempBuf[] = new byte[HidReport.REPORT_LEN];
	int RxCount = 0;	//每次接收数据长度
	public USBReceiver(Context context) {
		super();
		myContext = context;
		this.hidOp = new HidReport(context);
		hidOp.open();
	}

	boolean run =true;
	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		while (run) {
			//Log.d(TAG,"in USBReceiver()");
			Arrays.fill(tempBuf,(byte)0x00);
			RxCount = hidOp.readReport(tempBuf); // 阻塞读报告
			if (RxCount > 0) {
				try {
					FileOutputStream fos = new FileOutputStream("/sdcard/recv.txt",true);
					fos.write(tempBuf,0,RxCount);
					fos.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}


//				Log.d(TAG,"通知UI接受到数据"+RxCount);
//				Intent myIntent = new Intent();
//				myIntent.setAction("com.sdses.action.usbrecv");	//指定消息接收者
//				byte[] recv = new byte[RxCount];
//				System.arraycopy(tempBuf, 0, recv, 0, RxCount);
//				myIntent.putExtra("USBdata", recv);
//				myContext.sendBroadcast(myIntent);
			}else{
				Log.d(TAG,"读取hid的流关闭");
				break;
			}
		}
		Log.d(TAG,"线程退出");
	}

	public void release() {
		run =false;
	}
}
