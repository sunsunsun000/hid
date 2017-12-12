package com.example.usbhidcom;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private static final String TAG = MainActivity.class.getSimpleName();
	// 每20毫秒检查一下USB设备？
	//HidReport hidOp = null;
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
		//hidOp = new HidReport(context);
		//hidOp.open(); // open usb hid device

		// 创建USB发现线程
		sender = new USBSender(context);
		sender.start();
		// 创建USB接收线程
		recv = new USBReceiver(context);
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
		//hidOp.close(); // 关闭usb hid
		// TODO Auto-generated method stub
		super.onDestroy();
		sender.release();
		recv.release();
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
//			Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
//			intent.setType("image/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
//			intent.addCategory(Intent.CATEGORY_OPENABLE);
//			startActivityForResult(intent,1);
			Log.d(TAG,"开始发送文件");
			USBSenderHandler myUSBHandler = sender.getUSBSendHandler();
			Message msg_bluetooth = myUSBHandler.obtainMessage();
			Bundle myBundle = new Bundle();
			msg_bluetooth.arg1 = 2;
			myBundle.putString("send", "");
			msg_bluetooth.setData(myBundle);
			myUSBHandler.sendMessage(msg_bluetooth);

			recvCount = 0;
			total = 0;
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {//是否选择，没选择就不会继续
			Uri uri = data.getData();//得到uri，后面就是将uri转化成file的过程。
			Log.d(TAG,"uri="+uri);
			Log.d(TAG,"uri.getScheme()="+uri.getScheme());
			Log.d(TAG,"uri.getPathSegments="+uri.getPathSegments());
			Log.d(TAG,"uri.getPath="+uri.getPath());
			String img_path = getRealFilePath(context,uri);
			Log.d(TAG,"img_path="+img_path);
//			String[] proj = {MediaStore.Images.Media.DATA};
//			Cursor actualimagecursor = managedQuery(uri, proj, null, null, null);
//			int actual_image_column_index = actualimagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
//			actualimagecursor.moveToFirst();
//			String img_path = actualimagecursor.getString(actual_image_column_index);
			String filepath= getPath(context,uri);
Log.d(TAG,"filepath="+filepath);
			Toast.makeText(MainActivity.this, "发送文件路径"+filepath.toString(), Toast.LENGTH_SHORT).show();
			//		    //文件路径.
			USBSenderHandler myUSBHandler = sender.getUSBSendHandler();
			Message msg_bluetooth = myUSBHandler.obtainMessage();
			Bundle myBundle = new Bundle();
			msg_bluetooth.arg1 = 2;
			myBundle.putString("send", filepath.toString());
			msg_bluetooth.setData(myBundle);
			myUSBHandler.sendMessage(msg_bluetooth);
		}
	}

	/**
	 * 专为Android4.4以上设计的从Uri获取文件路径
	 */
	public static String getPath(final Context context, final Uri uri) {

		final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

		// DocumentProvider
		if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
			Log.d(TAG,">=19");
			// ExternalStorageProvider
			Log.d(TAG,"isExternalStorageDocument(uri)="+isExternalStorageDocument(uri));
			Log.d(TAG,"isDownloadsDocument(uri)="+isDownloadsDocument(uri));
			Log.d(TAG,"isMediaDocument(uri)="+isMediaDocument(uri));
			Log.d(TAG,"\"content\".equalsIgnoreCase(uri.getScheme())="+"content".equalsIgnoreCase(uri.getScheme()));
			Log.d(TAG,"");

			if (isExternalStorageDocument(uri)) {

				final String docId = DocumentsContract.getDocumentId(uri);
				Log.d(TAG,"docId="+docId);
				final String[] split = docId.split(":");
				final String type = split[0];
				Log.d(TAG,"type="+type);

				if ("7C03-13DB".equalsIgnoreCase(type)) {
					return Environment.getExternalStorageDirectory() + "/" + split[1];
				}

				// TODO handle non-primary volumes
			}
			// DownloadsProvider
			else if (isDownloadsDocument(uri)) {
				final String id = DocumentsContract.getDocumentId(uri);
				final Uri contentUri = ContentUris.withAppendedId(
						Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

				return getDataColumn(context, contentUri, null, null);
			}
			// MediaProvider
			else if (isMediaDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				Uri contentUri = null;
				if ("image".equals(type)) {
					contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				} else if ("video".equals(type)) {
					contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				} else if ("audio".equals(type)) {
					contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				}

				final String selection = "_id=?";
				final String[] selectionArgs = new String[]{split[1]};

				return getDataColumn(context, contentUri, selection, selectionArgs);
			}
			else if ("content".equalsIgnoreCase(uri.getScheme())) {
				Log.d(TAG,"content");
				return getDataColumn(context, uri, null, null);
			}
		}
		// MediaStore (and general)
		else if ("content".equalsIgnoreCase(uri.getScheme())) {
			Log.d(TAG,"content");
			return getDataColumn(context, uri, null, null);
		}
		// File
		else if ("file".equalsIgnoreCase(uri.getScheme())) {
			return uri.getPath();
		}

		return null;
	}

	/**
	 * Get the value of the data column for this Uri. This is useful for
	 * MediaStore Uris, and other file-based ContentProviders.
	 *
	 * @param context       The context.
	 * @param uri           The Uri to query.
	 * @param selection     (Optional) Filter used in the query.
	 * @param selectionArgs (Optional) Selection arguments used in the query.
	 * @return The value of the _data column, which is typically a file path.
	 */
	public static String getDataColumn(Context context, Uri uri, String selection,
									   String[] selectionArgs) {

		Log.d(TAG,"in getDataColumn()");
		Cursor cursor = null;
		final String column = "_data";
		final String[] projection = {column};

		try {
			cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
					null);
			if (cursor != null && cursor.moveToFirst()) {
				final int column_index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(column_index);
			}
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return null;
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is ExternalStorageProvider.
	 */
	public static boolean isExternalStorageDocument(Uri uri) {
		return "com.android.externalstorage.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is DownloadsProvider.
	 */
	public static boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is MediaProvider.
	 */
	public static boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri.getAuthority());
	}
	/**
	 * Try to return the absolute file path from the given Uri
	 *
	 * @param context
	 * @param uri
	 * @return the file path or null
	 */
	public static String getRealFilePath( final Context context, final Uri uri ) {
		if ( null == uri ) return null;
		final String scheme = uri.getScheme();
		String data = null;
		if ( scheme == null )
			data = uri.getPath();
		else if ( ContentResolver.SCHEME_FILE.equals( scheme ) ) {
			data = uri.getPath();
		} else if ( ContentResolver.SCHEME_CONTENT.equals( scheme ) ) {
			Cursor cursor = context.getContentResolver().query( uri, new String[] { MediaStore.Images.ImageColumns.DATA }, null, null, null );
			if ( null != cursor ) {
				if ( cursor.moveToFirst() ) {
					int index = cursor.getColumnIndex( MediaStore.Images.ImageColumns.DATA );
					if ( index > -1 ) {
						data = cursor.getString( index );
					}
				}
				cursor.close();
			}
		}
		return data;
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	static int recvCount = 0;
	static int total = 0;
	public class MyBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent intent) {
			if (intent.getAction().equals("com.sdses.action.usbrecv")) {
				//byte[] recv = intent.getByteArrayExtra("USBdata");
				recvCount += intent.getIntExtra("USBdata",0);
				//Log.d(TAG,"recv="+Util.toHexStringWithSpace(recv, recv.length));
				String temp = ""+DateFormat.format("HH:mm:ss", System.currentTimeMillis())+"包数"+recvCount;
                //messageRecv.append(temp+"\n");
				messageRecv.setText(temp);
			}
		}
		
	}


}
