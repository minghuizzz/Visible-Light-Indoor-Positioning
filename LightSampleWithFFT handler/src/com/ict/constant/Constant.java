package com.ict.constant;

import com.ict.utils.Utils;

import android.os.Environment;

public class Constant {
	public static final int stepSize =1;
	public static final int length=1024;
	public static final int FFTSize=256;
	public static int frequencypoint1=38;
	public static final int frequencypoint2=31;
	public static final int frequencypoint3=93;
	public static final int frequencypoint4=148;
	public static final int frequencypoint5=148;
	public static final String fingerPrint="2015-10-12-0-5//";
	public static final String lightSamplefilesDir= "lightSamplefiles//"+fingerPrint;
	public static final String locationFingerPrint= "locationFingerPrint//"+fingerPrint;
	public static final String locationFingerPrint2= "locationFingerPrint2//"+fingerPrint;
	public static final double distanceconstant=6852;
	public static final double A=410.37;
	public static final double AAA=159.55;
	public static boolean isfirst=true;
	public static final String 	foldername = Environment.getExternalStorageDirectory().getPath()
			+ "/PhoneSensor/" + Utils.getCurrentTime() + "/";
	public static final String 	projectfolder = Environment.getExternalStorageDirectory().getPath()
			+ "/PhoneSensor/" ;
	
}
