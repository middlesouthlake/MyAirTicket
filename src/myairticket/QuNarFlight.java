package myairticket;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;

class QuNarFlight implements Comparable<QuNarFlight>{
	String flightCode; //example: AC31|YYZ-PEK|2018-07-02_AC32|PEK-YYZ|2018-08-23
	String listPrice; //the price in the html code, it is encrypted by font file
	int price;
	String priceFontName;
	
	static HashMap<String, String[]> fontLibrary = new HashMap<String, String[]>();
	static String dirLib = "./myairticket";

	
	void setFlightCode(String code) {
		flightCode = new String(code);
	}
	
	String getFlightCode() {
		return flightCode;
	}
	
	void setListPrice(String priceEncrypted) {
		listPrice = priceEncrypted;
	}
	
	int decryptPrice() {
		String[] ss =fontLibrary.get(priceFontName);
		if(ss==null) return -1;
			
		char[] fontmap = ss[0].toCharArray();
		char[] charListPrice = listPrice.toCharArray();
		price = 0;
		for(int i=0;i<charListPrice.length;i++) {
			if(charListPrice[i]<'0' || charListPrice[i]>'9') break;
			price = price*10 + fontmap[charListPrice[i]-'0']-'0';
		}
		return price;
	}
	
	int getPrice() {
		return price;
	}
	
	void setPriceFontName(String s) {
		priceFontName = new String(s);
	}
	
	String getPriceFontName() {
		return priceFontName;
	}

	@Override
	public int compareTo(QuNarFlight arg0) {
		return price-arg0.getPrice();
	}

	public static void buildFontLibrary(String fontName, String fontUrl) {
    	/*style tag format
    	 * @font-face {font-family: 'ddv1c4021eb338srt';
        src: url('//s.qunarzz.com/flight_touch/ddvfonts/20180117020000/ddv1c4021eb338srt.ttf') format('truetype');
    	}.ddv1c4021eb338srt{
        	font-family: 'ddv1c4021eb338srt'!important;
    	}*/

		String fontText = decodeFontFile(fontName, fontUrl);
		String[] ss = new String[] {fontText, fontUrl};
		fontLibrary.put(fontName, ss);

	}
	public static String decodeFontFile(String filename, String url) {
		
		File libPath = new File(dirLib);
		if (!libPath.exists()) {
			libPath.mkdir();
		}
		
		File f = new File(dirLib+"/"+filename+".txt");
		if(f.exists()) {
			try {
				BufferedReader br = new BufferedReader(new FileReader(dirLib+"/"+filename+".txt"));
				String s = br.readLine();
				br.close();
				return s;
			}catch (Exception e) {
				return null;
			}
		}
		
		Runtime run = Runtime.getRuntime();
		String cmd;
		Process pr;

		//download the .ttf file
		f = new File(dirLib+"/"+filename+".ttf");
		if(f.exists()==false) {
			cmd = "wget -O "+dirLib+"/"+filename+".ttf "+url;
			try {
				pr = run.exec(cmd);
				pr.waitFor();
			}catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		
		f = new File(dirLib+"/"+filename+".ttf");
		if(!f.exists()) return null;
			
		f = new File(dirLib+"/"+filename+".png");
		if(!f.exists()) {
			try {
				//convert .ttf to .png
				cmd = "convert "+dirLib+"/"+filename+".ttf "+dirLib+"/"+filename+".png";
				pr = run.exec(cmd);
				pr.waitFor();
			}catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		f = new File(dirLib+"/"+filename+".png");
		if(!f.exists()) return null;
			
		try {
			//OCR the .png to .txt
			cmd = "tesseract "+dirLib+"/"+filename+".png "+dirLib+"/"+filename;
			pr = run.exec(cmd);
			pr.waitFor();		
			f = new File(dirLib+"/"+filename+".txt");
			if(!f.exists()) return null;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		try {
			BufferedReader br = new BufferedReader(new FileReader(dirLib+"/"+filename+".txt"));
			String s = br.readLine();
			br.close();
			return s;
		}catch (Exception e) {
			return null;
		}
		
	}

	public static void main(String[] args) {
		String workingDir = System.getProperty("user.dir");
		System.out.println("Current working directory : " + workingDir);
		buildFontLibrary("ddv1c4021eb338srt", "http://s.qunarzz.com/flight_touch/ddvfonts/20180117020000/ddv1c4021eb338srt.ttf");
		System.out.println(fontLibrary.get("ddv1c4021eb338srt")[0]);
	}

}
