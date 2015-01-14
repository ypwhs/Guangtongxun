package com.instabtbu.guangtongxun;

import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.bluetooth.BluetoothAdapter;
import app.akexorcist.bluetoothspp.BluetoothSPP;
import app.akexorcist.bluetoothspp.BluetoothSPP.BluetoothConnectionListener;
import app.akexorcist.bluetoothspp.BluetoothSPP.OnDataReceivedListener;
import app.akexorcist.bluetoothspp.BluetoothState;
import app.akexorcist.bluetoothspp.DeviceList;
public class MainActivity extends Activity {
BluetoothSPP bt;
	
	TextView textRead,textStatus,textSend;
	EditText editSend;
	Menu menu;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		textSend = (TextView)findViewById(R.id.textView_send);
		textRead = (TextView)findViewById(R.id.textView_rec );
		textStatus = (TextView)findViewById(R.id.textView_state);
		editSend = (EditText)findViewById(R.id.editText_send);
		bt = new BluetoothSPP(this);

		if(!bt.isBluetoothAvailable()) {
			Toast.makeText(getApplicationContext()
					, "蓝牙不可用"
					, Toast.LENGTH_SHORT).show();
            finish();
		}
		

		
		bt.setBluetoothConnectionListener(new BluetoothConnectionListener() {
			public void onDeviceDisconnected() {
				textStatus.setText("状态:未连接");
				menu.clear();
				getMenuInflater().inflate(R.menu.connection, menu);
				
			}
			
			public void onDeviceConnectionFailed() {
				textStatus.setText("状态:连接失败");
			}
			
			public void onDeviceConnected(String name, String address) {
				textStatus.setText("状态:已连接上" + name);
				menu.clear();
				getMenuInflater().inflate(R.menu.disconnection, menu);
				bt.send("北京工商大学\n光通讯原理演示", true);
			}
		});
		
		bt.setOnDataReceivedListener(new OnDataReceivedListener() {
			public void onDataReceived(byte[] data, String message) {
				textRead.setText(message);
			}
		});
		
	}
	public void sendlogo(View v)
	{
		byte[] buffer = null;
		try {
			InputStream in = getResources().getAssets().open("1.png");
			int lenght = in.available();
			buffer = new byte[lenght];
			in.read(buffer);
			in.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		byte[] a = {(byte)0xAA};
		bt.send(a, false);
		bt.send("[size="+buffer.length+"]", true);
		bt.send(buffer, false);
	}
	
	public void send(View v)
	{
		String sendString = editSend.getText().toString();
			if(bt.getServiceState()==BluetoothState.STATE_CONNECTED)
			{
			bt.send(sendString, true);
			textSend.append(sendString + "\n\n");
			ScrollView scroll = (ScrollView)findViewById(R.id.scroll);
			scroll.fullScroll(ScrollView.FOCUS_DOWN);
			}else {
				xuanze();
			}
	}
	public void xuanze()
	{
		bt.setDeviceTarget(BluetoothState.DEVICE_OTHER);
		Intent intent = new Intent(getApplicationContext(), DeviceList.class);
		intent.putExtra("bluetooth_devices", "蓝牙设备");
		intent.putExtra("no_devices_found", "没有发现设备");
		intent.putExtra("scanning", "搜索中");
		intent.putExtra("scan_for_devices", "搜索");
		intent.putExtra("select_device", "选择");
		startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
	}
	public boolean onCreateOptionsMenu(Menu menu) {
		this.menu = menu;
		getMenuInflater().inflate(R.menu.connection, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if(id == R.id.menu_device_connect) {
			xuanze();  
		} else if(id == R.id.menu_disconnect) {
			if(bt.getServiceState() == BluetoothState.STATE_CONNECTED)
    			bt.disconnect();
		}
		return super.onOptionsItemSelected(item);
	}

	public void onDestroy() {
        super.onDestroy();
        bt.stopService();
    }
	
	public void onStart() {
        super.onStart();
        if (!bt.isBluetoothEnabled()) {
        	Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        } else {
            if(!bt.isServiceAvailable()) { 
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_ANDROID);
                setup();
            }
        }
    }
	
	public void setup() {
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
			if(resultCode == Activity.RESULT_OK)
                bt.connect(data);
		} else if(requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if(resultCode == Activity.RESULT_OK) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_ANDROID);
                setup();
            } else {
                Toast.makeText(getApplicationContext()
                		, "Bluetooth was not enabled."
                		, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}  