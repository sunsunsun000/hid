package com.example.usbhidcom;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class USBSenderHandler extends Handler {
	public static final String TAG = USBSenderHandler.class.getSimpleName();
	private Context myContext;
	//这里操作USB设备
	HidReport hidOp = null;
	private byte[] myDataFromDevice;
	private boolean run = false;
	private boolean clean = false;
	
	public USBSenderHandler(Context context, HidReport hidOp,Looper myLooper) {
		super(myLooper);
		this.myContext = context;
		Log.d(TAG,"BlueToothHandler Constructor");
		//初始化硬件
		this.hidOp = hidOp;  //open usb hid device
	}

	@Override
	public void handleMessage(Message msg) {
		Bundle bundle = msg.getData();
		int type = msg.arg1;
		switch(type){
		case 1:
			String send = bundle.getString("send");
			Log.d(TAG,"准备发送数据"+send);
			hidOp.SendReport(send.getBytes());
			break;
		case 2:
			Log.d(TAG,"发送文件");
			//文件路径
			String txtPath = bundle.getString("send");
			Log.d(TAG,"发送文件路径:"+txtPath);
			txtPath="/sdcard/recv.txt";
			hidOp.sendFile(txtPath);
			break;
		}
		
	}


}
