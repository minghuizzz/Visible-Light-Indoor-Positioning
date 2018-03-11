package com.ict.calculateThread;

import java.util.ArrayList;
import java.util.Arrays;

import android.os.Handler;
import android.os.Message;

import com.ict.constant.Constant;
import com.ict.lighttrainer.LightTrainer;
import com.ict.utils.Utils;

public class DistanceWithAngle implements Runnable {
	public DistanceWithAngle(Handler mHandler,
			ArrayList<Double> lightArrayList, ArrayList<Double> oYArrayList) {
		super();
		this.mHandler = mHandler;
		LightArrayList = lightArrayList;
		OYArrayList = oYArrayList;
	}

	Handler mHandler;
	ArrayList<Double> LightArrayList;
	ArrayList<Double> OYArrayList;
	
	public  DistanceWithAngle( Handler handler) {
		mHandler=handler;
	}
	@Override
	public void run() {
		calDistanceOfLedIWithAngle(1.4) ;
	}
	
	public double calDistanceOfLedIWithAngle(double height) {
		ArrayList<Double> LedarrayList = new ArrayList<Double>();
		LightTrainer lightTrainer = new LightTrainer();
		ArrayList<Double> smoothLightaArrayList = new ArrayList<Double>();
		for (int i = 0; i < LightArrayList.size(); i++) {
			double[] dataforFFT = Arrays.copyOfRange(
					Utils.ArraylstToArrays(LightArrayList), i, i
							+ Constant.FFTSize);

			double sum = 0;
			for (int j = 0; j < dataforFFT.length; j++) {
				sum = sum + dataforFFT[j];
			}
			smoothLightaArrayList.add(sum / dataforFFT.length);
			String dataFFTMold256poit = lightTrainer
					.getDataFFTMold256poit(dataforFFT);
			LedarrayList.add(Double.parseDouble(dataFFTMold256poit));// 这儿值考虑一个灯

		}
		Utils.writeToFile(smoothLightaArrayList, Constant.foldername
				+ "smoothLightaArrayList" + Utils.getCurrentTime()+ ".txt");
		System.out.println("LightArrayList"+LightArrayList);
		String maxAndmaxid = Utils.getMaxOfArrayList(LightArrayList);// 必须是取FFT变换之前的
		Utils.writeToFile(LedarrayList, Constant.foldername + "LedarrayList"
				+ Utils.getCurrentTime() + ".txt");
		double FFTValue = Double.parseDouble(maxAndmaxid.split("maxid")[0]);
		int maxid = Integer.parseInt(maxAndmaxid.split("maxid")[1]);
		double angle = OYArrayList.get(maxid);
		double angdeg = Math.toRadians(angle);// 角度转弧度
		System.out.println("angle" + angle);
//		double distanceToLedI = Math.pow(
//				(Constant.A * height * Math.cos(angdeg)) / FFTValue, 1.0 / 3);
//		double dthree = (Constant.A * height * Math.cos(angdeg)) / FFTValue;

		double distanceToLedI22 = Math
				.pow((Constant.AAA * Math.cos(angdeg) * Math.cos(angdeg))
						/ FFTValue, 1.0 / 2);
	//	double dthree22 = (Constant.A * height * Math.cos(angdeg)) / FFTValue;
		OYArrayList.clear();
		LightArrayList.clear();

 
		System.out.println("distanceToLedI" + distanceToLedI22);

		Message message = new Message();
		message.what = 2;		 
		message.obj = "FFTValue:" + FFTValue + "入射角：" + angle
				+ "距离：" + distanceToLedI22;
		 mHandler.sendMessage(message);
		return distanceToLedI22;

	}

}