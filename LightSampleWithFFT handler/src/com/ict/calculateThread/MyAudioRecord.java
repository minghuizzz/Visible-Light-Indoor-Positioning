package com.ict.calculateThread;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import com.ict.constant.Constant;
import com.ict.lighttrainer.LightTrainer;
import com.ict.utils.Utils;
import com.ict.calculateThread.MeanFilterSmoothing;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class MyAudioRecord implements Runnable {

	// 变量
	public Handler handler;
	
	// 采样坐标
	public String locationX, locationY;
	
	// 是否可以录制
	private boolean isRecord = false;
	
	// 定时器
	private Timer mTimer;
	
	// 常量
	
	private int rate = 2000;  // 采样时间，默认4秒
	
	public String sameLocationCount; // 写入文件次数

	/**
	 * 音频相关
	 */
	
	private static int audioSource = AudioSource.MIC;
		
	// 采样频率, 默认采样率44100,22050，16000，11025
	private static int sampleRateInHz = 11025; 
		
	// 音频通道设置
	private static int channelConfig = AudioFormat.CHANNEL_IN_MONO;
		
	// 音频数据保证支持此格式
	private static int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
	
	// 缓存区
	private static int bufferSizeInBytes = 0;
		
	private AudioRecord audioRecord = null; 
		
	
	public MyAudioRecord(Handler handler, String locationX, String locationY, String sameLocationCount) {
		
		this.handler = handler;
		this.locationX = locationX;
		this.locationY = locationY;
		this.sameLocationCount = sameLocationCount;
	}
	
	@Override
	public void run() {
		createAudioRecord();
		startAudioRecord();
	}
	
	// 初始化音频录制
	public void createAudioRecord() {
			
		// 在录制过程中，音频数据写入缓冲区的总数（字节）。 从缓冲区读取的新音频数据总会小于此值。
		bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
			
		// 初始化
		audioRecord = new AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes);

		System.out.println("AudioRecord initialized");	
	}
	
	
	// 音频开始采集
	public void startAudioRecord() {
			
		audioRecord.startRecording();
		
		 // 开启音频文件写入线程  
	       new Thread(new AudioRecordThread()).start();  
	       
	       this.mTimer = new Timer();
		   this.mTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					
					// 采样完毕
					endAudioRecord();
				}
			}, rate);  				
	}
		
	// 音频录制停止	
	public void endAudioRecord() {
		
		this.mTimer.cancel();
		this.mTimer = null;
		audioRecord.stop();
		isRecord = false;
	    audioRecord.release();//释放资源  
	   
	    changeAudioDataToNumData();
	}
	
	class AudioRecordThread implements Runnable {  
        @Override  
        public void run() {  
        	writeAudioDataToFile();//往文件中写入裸数据  
        }  
    } 
	
    // 存储音频信息
	private void writeAudioDataToFile() {
		
		Log.d("bufferSizeInBytes",""+ bufferSizeInBytes);

		byte data[] = new byte[bufferSizeInBytes];
		
		String filename = Constant.foldername + "/" + "X" + locationX + "Y" + locationY + sameLocationCount + ".pcm";
		DataOutputStream bos = null;

		try {
			bos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(filename)));
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		}

		int read = 0;
		// setRecordStartedTime(System.currentTimeMillis());
		
		// start to record sound data
		isRecord = true;
		
		if (null != bos) {
			
			while (isRecord) {
				read = audioRecord.read(data, 0, bufferSizeInBytes);

				if (AudioRecord.ERROR_INVALID_OPERATION != read) {
					try {
						
						bos.write(data);
						
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			
			try {
				bos.flush();
				bos.close();

			} catch (IOException e) {
				
				e.printStackTrace();
			}
		}
	}	
	
	// 将音频数据转换成数字数据
	public void changeAudioDataToNumData() {
				
		double[] resultData = Utils.getArrayfromAndroid(Constant.foldername + "/" + "X" + locationX + "Y" + locationY + sameLocationCount + ".pcm");
		
		double[] handleData = Arrays.copyOfRange(resultData, 10000,
				10000 + 2148);
		
		String fileStr1 = Constant.foldername + "Light" + "X" + locationX + "Y" + locationY + sameLocationCount + ".txt";
		
		File file1 = new File(fileStr1); 
		for (int i = 0; i < handleData.length; i++) {
				
			String result = String.valueOf(handleData[i]) + "\n";
			SavedToText(file1, result);
		}	
		
		MeanFilterSmoothing filter = new MeanFilterSmoothing();
		filter.setTimeConstant(200);
		double[] filterHandleData = filter.addSamples(handleData);
		
		String fileStr = Constant.foldername + "FilterLight" + "X" + locationX + "Y" + locationY + sameLocationCount + ".txt";
		
		File file = new File(fileStr); 
		for (int i = 0; i < filterHandleData.length; i++) {
				
			String result = String.valueOf(filterHandleData[i]) + "\n";
			SavedToText(file, result);
		}			
		
		
		//得到用字符串连接的三个光强
		String getLightDataWithFFT = LightTrainer.getDataFFTMold256poit0704(filterHandleData, sameLocationCount);
		
//		String[] lightDatas = getLightDataWithFFT.split("inner"); 
//		String lightDataOne = lightDatas[0];
//		String lightDataTwo = lightDatas[1];
//		String lightDataThree = lightDatas[2];
			
		Message message = new Message();
		message.what = 10;
		double lightOneDistance = Math.sqrt(Constant.distanceconstant	/ Double.parseDouble(getLightDataWithFFT));
//		double lightTwoDistance =  Math.sqrt(Constant.distanceconstant	/ Double.parseDouble(lightDataTwo));
//		double lightThreeDistance =  Math.sqrt(Constant.distanceconstant / Double.parseDouble(lightDataThree));
//		
		
		message.obj = "LightFFT1: " + getLightDataWithFFT + "\n distance: " + String.valueOf(lightOneDistance);
		
//		+"\n\n LightFFT2: " + lightDataTwo + "\n distance: " + String.valueOf(lightTwoDistance)
//
//		+"\n\n LightFFT3: " + lightDataThree + "\n distance: " +  String.valueOf(lightThreeDistance);
//		
		
		handler.sendMessage(message);
	}
		
	/*
	 * 文件操作
	*/
		
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
			} catch (Exception e) {}
		}
	}
}
