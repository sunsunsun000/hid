package com.example.usbhidcom;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

//USB堵塞方式接收数据
public class USBReceiver extends Thread {
	private static final String TAG = USBReceiver.class.getSimpleName();
	private Context myContext;
	HidReport hidOp = null;

	byte tempBuf[] = new byte[HidReport.REPORT_LEN];
	int RxCount = 0;	//每次接收数据长度
	public USBReceiver(Context context, HidReport hidOp) {
		super();
		myContext = context;
		this.hidOp = hidOp;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		while (true) {
			RxCount = hidOp.readReport(tempBuf); // 阻塞读报告
			if (RxCount > 0) {
				Log.d(TAG,"通知UI接受到数据"+RxCount);
				Intent myIntent = new Intent();
				myIntent.setAction("com.sdses.action.usbrecv");	//指定消息接收者
				byte[] recv = new byte[RxCount];
				System.arraycopy(tempBuf, 0, recv, 0, RxCount);
				myIntent.putExtra("USBdata", recv);
				myContext.sendBroadcast(myIntent);
			}
		}
	}

}
