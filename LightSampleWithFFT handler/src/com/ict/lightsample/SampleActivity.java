package com.ict.lightsample;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Timer;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;

import com.ict.calculateThread.DistanceWithAngle;
import com.ict.calculateThread.MyAudioRecord;
import com.ict.constant.Constant;
import com.ict.utils.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SampleActivity extends Activity implements SensorEventListener {
	final String tag = "SampleActivity";
	
	SensorManager sm = null;
	TextView sampleraTextView = null;
	TextView DistanceTV = null;
	TextView DistanceWithAngleTV = null;
	TextView sensorTV = null;
	Button sampleButton = null;
	Button pauseButton = null;
	Button clearButton = null;
	EditText locationXEditText = null;
	EditText locationYEditText = null;
	EditText samplesizeEditText = null;
	private ArrayList<Float> xArrayList = new ArrayList<Float>();
	private ArrayList<Float> OXArrayList = new ArrayList<Float>();
	private ArrayList<Double> OYArrayList = new ArrayList<Double>();
	private ArrayList<Float> OZArrayList = new ArrayList<Float>();
	private ArrayList<Double> LightArrayList = new ArrayList<Double>();
	 
	private ArrayList<String> sycroArrayList = new ArrayList<String>();// 测试同步情况
	private ArrayList<Double> averageLight = new ArrayList<Double>();  // 测试同步情况
 
	LinkedBlockingQueue<Double> lqueue = new LinkedBlockingQueue<Double>( );  
	 
	boolean isSampled = false;
	boolean startgetAngle = false;
	boolean stopgetAngle = false;
	boolean caldistanceWithAngle = false;
	private Timer mTimer;
	public static Handler mHandler;
	private final ScheduledExecutorService forCollection = Executors
			.newScheduledThreadPool(2);

	String str = "";

	String   locationcoordinateX, locationcoordinateY, samplesize;
	
	boolean startWrite = false; // 是否写入数据
	// boolean register = true;
	int count = 0;

	File targetFile;

	int rate = SensorManager.SENSOR_DELAY_FASTEST;  // 传感器采样频率

	int n = 0;
	
	float OriX, OriY, OriZ;
	float AccX, AccY, AccZ, LightX;

	// 测试样例
	private int SampleCount = 0;
	
	// 同一位置多次采样
	public String sameLocationCount = "0";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 初始化相关控件
	    initInfomation();
	    
	    // 初始化文件信息
	    initFileInfo();
		
		mHandler = new RegHandler(sensorTV, DistanceTV,DistanceWithAngleTV);

		// 添加按钮的响应事件
		addListenrToBtn();

		
//	    DistanceWithoutAngle distanceWithoutAngle=new DistanceWithoutAngle(lqueue,mHandler);
//	    Thread thread=new Thread(distanceWithoutAngle);
//	    thread.start();
	}

    /*
     * 初始化相关控件
     */
	private void initInfomation() {
		
		sm = (SensorManager) getSystemService(SENSOR_SERVICE);
		setContentView(R.layout.main);
		
		sensorTV = (TextView) findViewById(R.id.sensordata_tv);
		locationXEditText = (EditText) findViewById(R.id.loaction_x);   // 设定的x坐标
		locationYEditText = (EditText) findViewById(R.id.location_y);   // 设定的y坐标
		samplesizeEditText = (EditText) findViewById(R.id.samplesize);  // 采集样本的数量
		sampleraTextView = (TextView) findViewById(R.id.rate);
		DistanceTV = (TextView) findViewById(R.id.fftanddistance_tv);
		DistanceWithAngleTV = (TextView) findViewById(R.id.angle__tv);
		sampleButton = (Button) findViewById(R.id.savebutton);
		// pauseButton = (Button) findViewById(R.id.pausebutton);
		clearButton = (Button) findViewById(R.id.clearbutton);
	}
	
	/*
	 * 初始化文件信息
	 */
	private void initFileInfo() {
		
		File projectfolder = new File(Constant.projectfolder);

		if (projectfolder == null || !projectfolder.exists()) {
			projectfolder.mkdir();
		}
		File folder = new File(Constant.foldername);

		if (folder == null || !folder.exists()) {
			folder.mkdir();
		}
	}
	
	/*
	 * 添加按钮的响应事件
	 */
	private void addListenrToBtn() {
		
		sampleButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				isSampled = false;

				// 判断输入数据是否合法
				if (locationXEditText.getText().toString().trim().equals("")
						|| locationYEditText.getText().toString().trim().equals("")
						|| samplesizeEditText.getText().toString().trim().equals("")) {
					
					Toast.makeText(SampleActivity.this, "parameter error", Toast.LENGTH_LONG).show();
					System.out.println("parameter error");
				} else {
					
					locationcoordinateX = locationXEditText.getText().toString().trim();
					locationcoordinateY = locationYEditText.getText().toString().trim();
					samplesize = samplesizeEditText.getText().toString().trim();
					System.out.println("foldername:" + Constant.foldername);
					ArrayList<String> filelist = Utils.getTXTInDirectory(Constant.foldername);
					System.out.println(filelist);
					for (int i = 0; i < filelist.size(); i++) {
						if (filelist.get(i).equals(
								"R" + rate + "X" + locationcoordinateX + "Y" + locationcoordinateY + ".txt")) {

							Toast.makeText(SampleActivity.this,
									"this position has been sampled，please type a new coordinate",
									Toast.LENGTH_LONG).show();
							isSampled = true;
						}
					}
					
					// isSampled 为true，则当前的坐标已经采集过，不在采集
					if (!isSampled) {
						sampleButton.setEnabled(false);
						startWrite = true;
						
						onResume(); 
						
						// 开始采集声音信号						
						MyAudioRecord myAudioRecord = new MyAudioRecord(mHandler, locationcoordinateX, locationcoordinateY, sameLocationCount);
						Thread thread=new Thread(myAudioRecord);
					    thread.start();
					    
						
					}
				} 
			}
		});

		clearButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				ClearContext();
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		// register this class as a listener for the orientation and
		// accelerometer sensors
		
		System.out.println("调用resume 方法");
		switch (rate) {
		case 0:
			sampleraTextView.setText("sample rate ：fastest");
			break;
		case 1:
			sampleraTextView.setText("sample rate ：game");
			break;
		case 2:
			sampleraTextView.setText("sample rate ：ui");
			break;
		case 3:
			sampleraTextView.setText("sample rate ：normal");
			break;
		default:
			break;
		}
		 
		//sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_LIGHT), rate);
		// sm.registerListener(this,
		// sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), rate);
		sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_ORIENTATION),
				rate);
	}

	@Override
	protected void onStop() {
		// unregister listener
		sm.unregisterListener(this);
		super.onStop();
	}

	// java.text.DateFormat df = new java.text.SimpleDateFormat(
	// "yyyy:MM:dd,hh:mm:ss:SSS");
	// String dateTime;
	// java.text.DateFormat df2 = new java.text.SimpleDateFormat(
	// "yyyy:MM:dd:hh:mm");
	// String dateTime2;
	

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	
	
	/*
	 * 文件操作
	 */
	
	/**
	 * 创建新文件
	 * @param actionname
	 * @param statename
	 */
	
	public void CreatNewFile(String actionname, String statename) {
		String fileName = "/" + "R" + rate + "X" + actionname + "Y" + statename
				+ ".txt";

		targetFile = new File(Constant.foldername + fileName);
	}

	/**
	 * 写入数据
	 * @param data
	 */
	public void Writedata(String data) {

		CreatNewFile(locationcoordinateX, locationcoordinateY);

		SavedToText(targetFile, data);
	}

	/**
	 * 保存数据
	 * @param targetFile
	 * @param stringToWrite
	 */
	private void SavedToText(File targetFile, String stringToWrite) {

		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {

			OutputStreamWriter osw;
			BufferedWriter out = null;
			try {
				if (!targetFile.exists()) {
					targetFile.createNewFile();
					osw = new OutputStreamWriter(new FileOutputStream(
							targetFile), "utf-8");
					out = new BufferedWriter(osw);
					out.write(stringToWrite);
					out.close();
				} else {
					osw = new OutputStreamWriter(new FileOutputStream(
							targetFile, true), "utf-8");
					out = new BufferedWriter(osw);
					out.write(stringToWrite);
					out.flush();
					out.close();
				}
			} catch (Exception e) {

			}
		}
	}

	public void ClearContext() {
		File f = new File(Constant.foldername + "/R" + rate + "X" + locationcoordinateX
				+ "Y" + locationcoordinateY + ".txt");
		try { 
			f.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_sensor, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_normal:
			rate = SensorManager.SENSOR_DELAY_NORMAL;
			// sampleraTextView.setText("normal");
			break;
		case R.id.menu_game:
			rate = SensorManager.SENSOR_DELAY_GAME;
			// sampleraTextView.setText("game");
			break;
		case R.id.menu_ui:
			rate = SensorManager.SENSOR_DELAY_UI;
			// sampleraTextView.setText("UI");
			break;
		case R.id.menu_fast:
			rate = SensorManager.SENSOR_DELAY_FASTEST;
			// sampleraTextView.setText("fastest");
			
			break;
		}
		
	//	rate=SensorManager.SENSOR_DELAY_GAME;
		System.out.println("修改了采样速率" + rate);
		onStop();
		onResume();
		return super.onOptionsItemSelected(item);
	}

/*
 * 多线程类，用于实时展示数据，刷新控件
 * */
	 
	private class RegHandler extends Handler {
		
		private TextView sensordatatTextView;   // 传感器信息
		private TextView fftanddistanceTextView;  // FFT变换后的距离
		private TextView distanceWithAngleTextView;  // 有角度的距离

		public RegHandler(TextView textView1, TextView textView2, TextView textView3) {
			
			sensordatatTextView = textView1;
			fftanddistanceTextView = textView2;
			distanceWithAngleTextView = textView3;
		}

		@Override
		public void handleMessage(Message msg) {
			int count = 0;
			count = count + msg.what;
			if (count == 1) {
//				System.out.println("time of update：" + System.currentTimeMillis());
			}
			switch (msg.what) {
			case 1:
				
				String fftanddistanceString = (String) msg.obj;
				fftanddistanceTextView.setText(fftanddistanceString);
				break;
				
			case 2:
				String distanceWithAngle = (String) msg.obj;
				distanceWithAngleTextView.setText(distanceWithAngle);
				break;
				
			case 10:
				
				String disdance = (String) msg.obj;
				fftanddistanceTextView.setText(disdance);
				showDialog();
				sampleButton.setEnabled(true);
				sameLocationCount = String.valueOf(Integer.valueOf(sameLocationCount).intValue() + 1);

				onStop();
				break;
			
			case 11:
				
				double[] sensordata = (double[]) msg.obj;
				StringBuilder sb = new StringBuilder();
				sb.append("Oriention\n");
				sb.append("Oriention X: " + sensordata[0] + "\n");
				sb.append("Oriention Y: " + sensordata[1] + "\n");
				sb.append("Oriention Z: " + sensordata[2] + "\n");
				sb.append("Light\n");
				sb.append("Light Z: " + sensordata[3] + "\n");
				sensordatatTextView.setText(sb.toString());
		//		System.out.println("sensordata" + Arrays.toString(sensordata));
				break;
			}
			super.handleMessage(msg);
		}
	}
  
	 //显示基本的AlertDialog  
    private void showDialog() {  
        AlertDialog.Builder builder = new AlertDialog.Builder(this);  
        builder.setTitle("光强写入完毕");  
        builder.setNegativeButton("关闭",  
                new DialogInterface.OnClickListener() {  
                    public void onClick(DialogInterface dialog, int whichButton) {  
                        setTitle("");  
                    }  
                });  
     
        builder.show();  
    }  
	
	/*
	 * 检测传感器改变的监听器
	 * @see android.hardware.SensorEventListener#onSensorChanged(android.hardware.SensorEvent)
	 */
	@Override
	public void onSensorChanged(SensorEvent event) {		
		synchronized (this) {

			str = "";
			double[] sensorData = new double[4];// 用于存储方向传感器和光传感器的读数
			// dateTime = df.format(new Date()).toString();
			// dateTime2 = df2.format(new Date()).toString();
			
			
			
			if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
				// xViewO.setText("Orientation X: " + event.values[0]);
				// yViewO.setText("Orientation Y: " + event.values[1]);
				// zViewO.setText("Orientation Z: " + event.values[2]);
				sensorData[0] = event.values[0];
				sensorData[1] = event.values[1];
				sensorData[2] = event.values[2];
				sensorData[3] = LightX;
				// if (startWrite) {
				// str = "dateTime" + dateTime + "AccX" + AccX + "AccY" + AccY
				// + "AccZ" + AccZ + "OriX" + event.values[0] + "OriY"
				// + event.values[1] + "OriZ" + event.values[2]
				// + "LightX" + LightX + "\n";
				OriX = event.values[0];
				OriY = event.values[1];
				OriZ = event.values[2];
				//
				// Writedata(str);
				// }
				if (Math.abs(event.values[0] - 270) < 5) {// 与正北方向小于5度时候触发定位功能 ,  abs取绝对值的方法
					startgetAngle = true;

				}
				if (Math.abs(event.values[1] - 80) < 5 && startgetAngle) {// 手机俯角达到70度之后停止记录光照和俯角度数（因为led的有效照明范围为60度）
					System.out.println("停止获取入射角。。。");
					startgetAngle = false;
					Utils.writeToFile(sycroArrayList, Constant.foldername
							+ "sycroArrayList" + Utils.getCurrentTime() + ".txt");
					System.out.println("角度"
							+ Collections.frequency(sycroArrayList, "角度")
							+ "光照"
							+ Collections.frequency(sycroArrayList, "光照")
							+ "  " + sycroArrayList.size());
					sycroArrayList.clear();

					Utils.writeToFile(OYArrayList, Constant.foldername + "OYArrayList"
							+ Utils.getCurrentTime() + ".txt");

					Utils.writeToFile(LightArrayList, Constant.foldername
							+ "LightArrayList" + Utils.getCurrentTime() + ".txt");
			//		double distance = calDistanceOfLedIWithAngle(1.4);
				    DistanceWithAngle distanceWithoutAngle=new DistanceWithAngle(mHandler,LightArrayList,OYArrayList);
				    Thread thread=new Thread(distanceWithoutAngle);
				    thread.start();
//					OYArrayList.clear();
//					LightArrayList.clear();
				
				}
				if (startgetAngle) {// 触发定位之后记录Y轴读数

					OYArrayList.add(Double.parseDouble(String
							.valueOf(event.values[1])));
					sycroArrayList.add("角度");
				}

			}
			
			SampleCount++;
			if (SampleCount%100==0) {
				long timestamp=System.currentTimeMillis();
				Log.d("timestamp",""+ timestamp);
			}
			
			if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
				sensorData[0] = OriX;
				sensorData[1] = OriY;
				sensorData[2] = OriZ;
				sensorData[3] = event.values[0];
				LightX = event.values[0]; 
			 
				if (lqueue.size()>=Constant.FFTSize) {
					lqueue.poll();
					lqueue.offer((double) event.values[0]);
				}else {
					lqueue.offer((double) event.values[0]);
				}
				
			  
				
	 
				// xViewLight.setText("Light X:" + event.values[0] + "\n " );
				if (startWrite) {// 光信号采样并写入文件
					// str = "dateTime"+dateTime + "AccX"+ AccX + "AccY"+AccY +
					// "AccZ"+AccZ + "OriX"+ OriX + "OriY" + OriY
					// + "OriZ" + OriZ + "LightX"+event.values[0] +"\n" ;
					str = event.values[0] + "\n";
					LightX = event.values[0]; 
					Writedata(str);
					count++;

					if (count == Integer.parseInt(samplesize)) {
						Toast.makeText(getApplicationContext(), "采样结束",
								Toast.LENGTH_LONG).show();
						startWrite = false;
						count = 0;
						sampleButton.setEnabled(true);
						onStop();

					}
				}

				xArrayList.add(event.values[0]);
				if (xArrayList.size() == 100) {// 用于统计光传感器一百次采样用时
					System.out.println("sample "+System.currentTimeMillis());

					xArrayList.clear();
				}
	 
				if (startgetAngle) {// 开始定位的时候开始记录光传感器读数
					LightArrayList.add(Double.parseDouble(String
							.valueOf(event.values[0])));
					sycroArrayList.add("光照");
				}
			}
			Message msg = mHandler.obtainMessage();
			msg.what = 11;
			msg.obj = sensorData;
			mHandler.sendMessage(msg);
		}
		
		
		
	}

 
}
