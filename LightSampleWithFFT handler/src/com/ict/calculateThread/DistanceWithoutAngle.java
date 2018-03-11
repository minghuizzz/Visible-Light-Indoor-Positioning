package com.ict.calculateThread;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import com.ict.constant.Constant;
import com.ict.lighttrainer.LightTrainer;
import com.ict.utils.Utils;

import android.os.Handler;
import android.os.Message;
/**
 * 不考虑休眠时间，计算一次距离需要5ms
 * @author Admin
 *
 */
public class DistanceWithoutAngle implements Runnable {
	LinkedBlockingQueue<Double>  lqueue;
	Handler mHandler;
	public DistanceWithoutAngle(LinkedBlockingQueue<Double>  lqBlockingQueue,Handler handler)
	{
		lqueue=lqBlockingQueue;
		mHandler=handler;
	}
 
	@Override
	public void run() {
		
			
		while (true) {
			long threadstart=System.currentTimeMillis();
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			 System.out.println("start lqueue");
			 double[] dataforFFT=new double[lqueue.size()];
			 ArrayList<Double> arrayList=new ArrayList<Double>();
			 for (Double string : lqueue) {  
				 arrayList.add(string);			
		            System.out.print(string +" ");  
		        } 
			 System.out.println();
			 System.out.println("lqueue.size"+lqueue.size());
			 System.out.println("end lqueue");
			 dataforFFT=Utils.ArraylstToArrays(arrayList);
			 
			 if (dataforFFT.length==Constant.FFTSize) {
					LightTrainer lightTrainer = new LightTrainer();
					String dataFFTMold256poit = lightTrainer.getDataFFTMold256poit(dataforFFT);
				//	averageLight.add(Double.parseDouble(dataFFTMold256poit));
				
				
				Message message = new Message();
				message.what = 1;
				double powerofdistance = Constant.distanceconstant
						/ Double.parseDouble(dataFFTMold256poit);
				message.obj = "FFT:" + dataFFTMold256poit + "\n distance:"
						+ Math.sqrt(powerofdistance);
				System.out.println("FFT:" + dataFFTMold256poit + "\n distance:"
						+ Math.sqrt(powerofdistance));
				 mHandler.sendMessage(message);
			}
		    
			 long threadend=System.currentTimeMillis();
			 System.out.println("time of distanceWithoutAngle:"+(threadend-threadstart));
		}

 
	}
}
