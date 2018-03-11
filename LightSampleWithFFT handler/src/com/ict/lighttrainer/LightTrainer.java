package com.ict.lighttrainer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;

import com.ict.FFT.Complex;
import com.ict.FFT.FFT;
import com.ict.constant.Constant;
import com.ict.utils.Utils;

import android.os.Environment;

public class LightTrainer {

	/**
	 * generate location fingerprint for all grid and write to file
	 * 
	 * @return
	 */
	public static ArrayList<String> generateAllLocationFingerPrint() {
		File folder = new File(Constant.locationFingerPrint);
		if (folder == null || !folder.exists()) {
			folder.mkdir();
		}
		File folder2 = new File(Constant.locationFingerPrint2);
		if (folder2 == null || !folder2.exists()) {
			folder2.mkdir();
		}
		ArrayList<String> locationFingerPrintArrayList = new ArrayList<String>();
		ArrayList<String> flielist = Utils
				.getTXTInDirectory(Constant.lightSamplefilesDir);
		ArrayList<String> firstRecordAtAllPlaceaArrayList = new ArrayList<String>();
		for (int i = 0; i < flielist.size(); i++) {
			String filepathString = Constant.lightSamplefilesDir
					+ flielist.get(i);
			double[] currentrecorddata = Utils
					.readTxtfileTodoubleArr(filepathString);
			// .getArrayfromAndroid(filepathString);

			ArrayList<String> oneplaceArrayList = getTheFFTCoefficientList(currentrecorddata);
			String oneplaceString = oneplaceArrayList.toString();
			// System.out.println(oneplaceArrayList.size() + "oneplaceString"
			// + oneplaceString);

			locationFingerPrintArrayList.add(oneplaceString);
			Utils.writeToFile(oneplaceArrayList, Constant.locationFingerPrint
					+ "location" + (i + 1) + ".txt");
		}
		Utils.writeToFile(locationFingerPrintArrayList,
				Constant.locationFingerPrint2
						+ "locationFingerPrintArrayList.txt");

		return locationFingerPrintArrayList;
	}

	/**
	 * build FFT coefficient vector
	 * 
	 * @param recorddataii
	 * @return
	 */
	public static ArrayList<String> getTheFFTCoefficientList(
			double[] recorddataii) {
		Constant.isfirst = true;
		ArrayList<String> the88ArrList = new ArrayList<String>();

		double[] effectiveRecorddata = Arrays.copyOfRange(recorddataii, 0,
				recorddataii.length);
		for (int i = 0; i < Constant.length - Constant.FFTSize; i = i
				+ Constant.stepSize) {
			double[] recordfiledataslice = new double[Constant.length];

			recordfiledataslice = Arrays.copyOfRange(effectiveRecorddata, i, i
					+ Constant.FFTSize);

			the88ArrList
					.add(getDataFFTMold256poit(recordfiledataslice));

		}

		return the88ArrList;
	}

	/**
	 * extract frequency point(81 87 93)
	 * 
	 * @param orginalData
	 * @return
	 */
	public static String getDataFFTMold256poit(double[] orginalData) {

		ArrayList<Complex[]> orginarrayList = new ArrayList<Complex[]>();

		// Utils.writeToFile(orginalData, "orginalDataslice.txt");
		Complex[] tempdataFFTMold = FFTTransform(orginalData);
		Complex[] from418to464 = new Complex[256];
		ArrayList<Complex> extractfrequencypointList = new ArrayList<Complex>();
		ArrayList<String> extractfrequencypointLogMoldList = new ArrayList<String>();
		ArrayList<Double> AllArrayList = new ArrayList<Double>();

		// extractfrequencypointList.add(tempdataFFTMold[10]);
		// extractfrequencypointList.add(tempdataFFTMold[81]);
		// extractfrequencypointList.add(tempdataFFTMold[87]);
		for (int i = 0; i < tempdataFFTMold.length; i++) {
			AllArrayList.add(Math.sqrt(Math.pow(tempdataFFTMold[i].re(), 2)
					+ Math.pow(tempdataFFTMold[i].im(), 2)));
		}
		
		// if (Constant.isfirst) {
		AllArrayList.remove(0);
		System.out.println("max of AllArrayList"
				+ Utils.getMaxOfArrayList(AllArrayList));
		Constant.isfirst = false;
		String maxandMaxidString = Utils.getMaxOfArrayList(AllArrayList);
		// String pointString=maxandMaxidString.split("maxid")[1] + 1)
		Constant.frequencypoint1 = Integer.parseInt(maxandMaxidString.split("maxid")[1]) + 1;
//		System.out.println("frequencypoint1 " + Constant.frequencypoint1 + "  "
//				+ AllArrayList.get(Constant.frequencypoint1 - 1));

		// System.out.println(AllArrayList.get(37) + "  "
		// + AllArrayList.get(217));
		// System.out.println();
		// System.out.println("第一段数据FFT变换之后："+AllArrayList);
		// AllArrayList.remove(0);
		// Collections.sort(AllArrayList);
		// System.out.println(AllArrayList);
		// }

		extractfrequencypointList.add(tempdataFFTMold[Constant.frequencypoint1]);
		extractfrequencypointList.add(tempdataFFTMold[Constant.frequencypoint2]);
		extractfrequencypointList.add(tempdataFFTMold[Constant.frequencypoint3]);
		String extractfrequencypoint = "";
		String aaa = "";
		for (int i = 0; i < extractfrequencypointList.size(); i++) {

			// extractfrequencypoint = extractfrequencypoint
			// + String.valueOf(Math.log10(Math.sqrt(Math.pow(
			// extractfrequencypointList.get(i).re(), 2)
			// + Math.pow(extractfrequencypointList.get(i).im(), 2))))
			// + "inner";
			// 取对数
			// extractfrequencypoint = extractfrequencypoint
			// + String.valueOf(Math.log10(Math.sqrt(Math.pow(
			// extractfrequencypointList.get(i).re(), 2)
			// + Math.pow(extractfrequencypointList.get(i).im(), 2))));
			// 不取对数
			if (extractfrequencypointList.size() == 1) {
				extractfrequencypoint = extractfrequencypoint
						+ String.valueOf(Math.sqrt(Math.pow(
								extractfrequencypointList.get(i).re(), 2)
								+ Math.pow(extractfrequencypointList.get(i)
										.im(), 2)));
			} else {
				extractfrequencypoint = extractfrequencypoint
						+ "inner"
						+ String.valueOf(Math.sqrt(Math.pow(
								extractfrequencypointList.get(i).re(), 2)
								+ Math.pow(extractfrequencypointList.get(i)
										.im(), 2)));
			}

			aaa = Math.sqrt(Math.pow(extractfrequencypointList.get(i).re(), 2)
					+ Math.pow(extractfrequencypointList.get(i).im(), 2))
					+ "";
			if (aaa.equals("0.0")) {
				aaa = "1";
			}

			// aaa=String.valueOf(Math.log10(Double.parseDouble(aaa)));
			// System.out.println(aaa);
		}
		// extractfrequencypoint = extractfrequencypoint;
//		System.out.println("extractfrequencypoint" + extractfrequencypoint);
		return extractfrequencypoint;
	}

	
	
	/**
	 * extract frequency point(81 87 93)
	 * 
	 * @param orginalData
	 * @return
	 */
	public static String getDataFFTMold256poit0704(double[] orginalData, String writeCount) {
		
		
		Complex[] tempdataFFTMold = FFTTransform(orginalData);
		ArrayList<Complex> extractfrequencypointList = new ArrayList<Complex>();
		
		// FFT数据写入文件
		String fileStr = Constant.foldername + "FFT" + writeCount + ".txt";
		File file = new File(fileStr);

		for(int i = 0; i < tempdataFFTMold.length; i++) {
			String result = String.valueOf(Math.sqrt(Math.pow(
					tempdataFFTMold[i].re(), 2)
					+ Math.pow(tempdataFFTMold[i].im(), 2))) + "\n";
			SavedToText(file, result);
		}
		
		// 计算频点
		int point = (38 * 2048) / 11025;
		
		// tempdataFFTMold -> 光强
		extractfrequencypointList.add(tempdataFFTMold[point]);
		extractfrequencypointList.add(tempdataFFTMold[87]);
		extractfrequencypointList.add(tempdataFFTMold[93]);
		
		String extractfrequencypoint = "";
		
		
		for (int i = 0; i < extractfrequencypointList.size(); i++) {
			extractfrequencypoint = extractfrequencypoint
					// 转换成需要的光强
					+ String.valueOf(Math.sqrt(Math.pow(
							extractfrequencypointList.get(i).re(), 2)
							+ Math.pow(extractfrequencypointList.get(i).im(), 2))) + "inner";
		}
		
		return  String.valueOf(Math.sqrt(Math.pow(
				tempdataFFTMold[6].re(), 2)
				+ Math.pow(tempdataFFTMold[6].im(), 2)));
		
	}
	
	/*
	 * 文件操作
	*/
		
	/**
	 * 保存数据
	 * @param targetFile
	 * @param stringToWrite
	 */
	private static void SavedToText(File targetFile, String stringToWrite) {

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
	/**
	 * FFTTransform
	 * 
	 * @param data
	 * @return
	 */
	public static Complex[] FFTTransform(double[] data) {
		int N = 2048;
		Complex[] x = new Complex[N];
		for (int i = 0; i < N; i++) {
			x[i] = new Complex(data[i], 0);
		}
		FFT ffttest = new FFT();
		Complex[] y = ffttest.fft(x);

		return y;

	}

	/**
	 * calculate Multidimensional euclidian space distance
	 * 
	 * @param xList
	 * @param yList
	 * @return
	 */
	public static double getEuclideaDistanceMulti(
			ArrayList<ArrayList<String>> xList,
			ArrayList<ArrayList<String>> yList) {

		double EuclideaDistance = 0;
		for (int j = 0; j < xList.size(); j++) {
			ArrayList<String> AarrayList = xList.get(j);
			ArrayList<String> BarrayList = yList.get(j);

			double sum = 0;
			for (int i = 0; i < AarrayList.size(); i++) {

				sum = sum
						+ (Double.parseDouble(AarrayList.get(i)) - Double
								.parseDouble(BarrayList.get(i)))
						* (Double.parseDouble(AarrayList.get(i)) - Double
								.parseDouble(BarrayList.get(i)));
			}
			EuclideaDistance = EuclideaDistance + Math.sqrt(sum);
			// EuclideaDistance = EuclideaDistance
			// + (Double.parseDouble(xList.get(j)) - Double
			// .parseDouble(yList.get(j)))
			// * (Double.parseDouble(xList.get(j)) - Double
			// .parseDouble(yList.get(j)));
		}

		return EuclideaDistance;

	}

	// public static double getDTWMulti(
	// ArrayList<ArrayList<String>> xList,
	// ArrayList<ArrayList<String>> yList) {
	// // //
	// // System.out.println(xList.toArray().length);
	// // dtw_test dtw_test=new dtw_test();
	// // dtw_test.getDTW(xList.toArray(), xList.toArray());
	//
	// ArrayList<Double []> AList=new ArrayList<Double[]>();
	// ArrayList<Double []> BList=new ArrayList<Double[]>();
	// // System.out.println(Arrays.toString(xList.toArray()));
	// Double [] ADTW=new Double[xList.get(0).size()];
	// Double [] BDTW=new Double[xList.get(0).size()];
	// double EuclideaDistance = 0;
	// for (int j = 0; j < xList.size(); j++) {
	// ArrayList<String> AarrayList = xList.get(j);
	// ArrayList<String> BarrayList = yList.get(j);
	// ADTW=Utils.arrlistToDoubleArr(AarrayList);
	// BDTW=Utils.arrlistToDoubleArr(BarrayList);
	// AList.add(ADTW);
	// BList.add(BDTW);
	//
	// }
	// dtw_test dtw_test=new dtw_test();
	//
	// return dtw_test.getDTW(AList, BList);
	//
	// }
	public static void getSimilarity(String filename) {
		ArrayList<String> flielist = Utils
				.getTXTInDirectory(Constant.locationFingerPrint);
		for (int i = 0; i < flielist.size(); i++) {

			ArrayList<ArrayList<String>> locationA = new ArrayList<ArrayList<String>>();
			ArrayList<ArrayList<String>> locationB = new ArrayList<ArrayList<String>>();
			locationA = parseFrequencyPoint(Constant.locationFingerPrint
					+ filename);
			locationB = parseFrequencyPoint(Constant.locationFingerPrint
					+ flielist.get(i));
			System.out.println(getEuclideaDistanceMulti(locationA, locationB)
					+ "  " + filename + "and" + flielist.get(i));

			// System.out.println(getDTWMulti(locationA, locationB));

		}
	}

	public static ArrayList<ArrayList<String>> parseFrequencyPoint(
			String filename) {
		ArrayList<String> locationAi = new ArrayList<String>();

		ArrayList<ArrayList<String>> locationA = new ArrayList<ArrayList<String>>();
		locationAi = Utils.readTxtFileToString(filename);

		for (int j = 0; j < locationAi.size(); j++) {
			ArrayList<String> multiFrequencepoit = new ArrayList<String>();
			String multiFrequencepoitsString = locationAi.get(j);
			if (multiFrequencepoitsString.contains("inner")) {
				for (int h = 1; h < multiFrequencepoitsString.split("inner").length; h++) {
					multiFrequencepoit.add(multiFrequencepoitsString
							.split("inner")[h]);
				}
			} else {
				multiFrequencepoit.add(multiFrequencepoitsString);
			}

			locationA.add(multiFrequencepoit);

		}
		return locationA;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		generateAllLocationFingerPrint();
		getSimilarity("location8.txt");
	}

}
