package com.example.usbhidcom;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.sdses.tool.Util;

public class MainActivity extends Activity {
	private static final String TAG = MainActivity.class.getSimpleName();
	// 每20毫秒检查一下USB设备？
	HidReport hidOp = null;
	USBSender sender = null;
	USBReceiver recv = null;
	// String HIDTag = new String("lyx");
	// String RxBufflag = new String("hidRead");
	// String RxSize = new String("RxSize");
	// String DevStateFlag = new String("conState");
	TextView messageRecv;
	EditText editSM;
	EditText editRM;
	Button sendFile = null;
	// boolean HIDavaliable = true;
	// ConStateHandler ConStateH;
	// GetReportHandler GetReportH;
	Context context = null;

	MyBroadcastReceiver myBroadcastReceiver = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		editSM = (EditText) findViewById(R.id.EditWrite);
		editRM = (EditText) findViewById(R.id.EditRead);
		Button sendBtn = (Button) findViewById(R.id.buttonSend);
		sendBtn.setOnClickListener(new usbSend());
		sendFile = (Button) findViewById(R.id.sendFile);
		sendFile.setOnClickListener(new fileSend());

        messageRecv = (TextView)findViewById(R.id.textView);
        messageRecv.setText(messageRecv.getText(), TextView.BufferType.EDITABLE);
		context = getApplicationContext();
		hidOp = new HidReport(context);
		hidOp.open(); // open usb hid device

		// 创建USB发现线程
		sender = new USBSender(context, hidOp);
		sender.start();
		// 创建USB接收线程
		recv = new USBReceiver(context, hidOp);
		recv.start();

		Log.d(TAG, "App 启动");
        myBroadcastReceiver = new MyBroadcastReceiver();
        IntentFilter filter=new IntentFilter();
        filter.addAction("com.sdses.action.usbrecv");
        //注册receiver
        registerReceiver(myBroadcastReceiver, filter);
	}

	@Override
	protected void onDestroy() {
		hidOp.close(); // 关闭usb hid
		// TODO Auto-generated method stub
		super.onDestroy();
		sender.release();
		Log.d(TAG, "App 退出");
		unregisterReceiver(myBroadcastReceiver);
	}

	class usbSend implements OnClickListener {

		@Override
		public void onClick(View arg0) {
			USBSenderHandler myUSBHandler = sender.getUSBSendHandler();
			Message msg_bluetooth = myUSBHandler.obtainMessage();
			msg_bluetooth.arg1 = 1;
			Bundle myBundle = new Bundle();

			myBundle.putString("send", editSM.getText().toString());
			msg_bluetooth.setData(myBundle);
			myUSBHandler.sendMessage(msg_bluetooth);
		}

	}

	class fileSend implements OnClickListener {

		@Override
		public void onClick(View arg0) {
		    //文件路径
            String path = editRM.getText().toString();
            Log.d(TAG,"发送的文件路径="+path);
			USBSenderHandler myUSBHandler = sender.getUSBSendHandler();
			Message msg_bluetooth = myUSBHandler.obtainMessage();
			Bundle myBundle = new Bundle();
			msg_bluetooth.arg1 = 2;
			myBundle.putString("send", path);
			msg_bluetooth.setData(myBundle);
			myUSBHandler.sendMessage(msg_bluetooth);
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public class MyBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent intent) {
			if (intent.getAction().equals("com.sdses.action.usbrecv")) {
				byte[] recv = intent.getByteArrayExtra("USBdata");
				Log.d(TAG,"recv="+Util.toHexStringWithSpace(recv, recv.length));
				String temp = DateFormat.format("HH:mm:ss", System.currentTimeMillis())+new String(recv);
                Editable text = (Editable) messageRecv.getText();
                text.append(temp);
                messageRecv.setText(text);
			}
		}
		
	}


}
