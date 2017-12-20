package com.example.usbhidcom;


import android.content.Context;
import android.os.Looper;
import android.util.Log;

public class USBSender extends Thread{
	private static final String TAG = USBSender.class.getSimpleName();
	private Context myContext;
	private Looper myLooper = null;
	private USBSenderHandler myUSBHandler; 
	HidReport hidOp = null;
	public USBSender(Context context) {
		this.myContext = context;
		this.hidOp = new HidReport(context);
		hidOp.openOutHid();
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		Log.d(TAG,"HttpSender run()");
		Log.d(TAG,"in run() id="+this.getId());
		// 1、初始化Looper
		Looper.prepare();
//		mLooper = Looper.myLooper();
		myUSBHandler = new USBSenderHandler(myContext,hidOp, Looper.myLooper());
		myLooper = Looper.myLooper();
		Looper.loop();
		Log.d(TAG,"run over");
	}
	public void release() {
		try {
			myLooper.quit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public USBSenderHandler getUSBSendHandler() {
		// TODO Auto-generated method stub
		return myUSBHandler;
	}
}
