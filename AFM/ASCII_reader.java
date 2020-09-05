import java.io.*;
import ij.*;
import ij.io.*;
import ij.gui.*;
import ij.plugin.*;
import ij.process.*;
import java.util.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class ASCII_reader implements PlugIn {
	private static Pattern pattern = null;
	private static String regex = "^[0-9]*.?[0-9]*$";

	ImagePlus img;
	ImageProcessor ip;
	DataInputStream dis;
	String record = null;
	Scanner scan = null;
	//Scanner scan2 = null;
	String wII = "oyo";
	String filed;
	String typeOfData;
	String nameOfData;
	int width = 0;
	int height = 0;
	String daTa = "ugu";
	double xxx, yyy, zzz;
	int x, y;
	public static final String Key = "COEFS";
	
	int summ = 0;

	public ASCII_reader() {
		img = null;
	}

	public void run(String arg) {
		OpenDialog od = new OpenDialog("Open image ...", arg);
		String directory = od.getDirectory();
		String fileName = od.getFileName();
		if (fileName==null) return;
		BufferedReader fp;
		StringTokenizer st;
		try {
			fp = new BufferedReader(new FileReader(directory+fileName));
            }
	catch (Exception e) {
		IJ.error("Simple ASCII Reader FIRST", e.getMessage());
		return;
	}

		try {
			
			String line = fp.readLine();
			st = new StringTokenizer(line," \t\n\r\f:");
			wII = st.nextToken();
			
			line = fp.readLine();
			st = new StringTokenizer(line," \t\n\r\f:");
			filed = st.nextToken();
			filed = st.nextToken();
			
			line = fp.readLine();
			st = new StringTokenizer(line," \t\n\r\f:");
			typeOfData = st.nextToken();  //record=dis.readLine(); // Type
			typeOfData = st.nextToken();
			
			line = fp.readLine();
			st = new StringTokenizer(line," \t\n\r\f:");
			nameOfData = st.nextToken(); //record=dis.readLine(); // Name
			nameOfData = st.nextToken();
			
			line = fp.readLine();
			st = new StringTokenizer(line," \t\n\r\f:");
			String str;
			do {
				str = st.nextToken(); 
			}while(!isInteger(str));
			width = atoi(str);
			
			line = fp.readLine();
			st = new StringTokenizer(line," \t\n\r\f:");
			do {
				str = st.nextToken(); 
			}while(!isInteger(str));
			height = atoi(str);
			
			//IJ.showMessage("START", "widtn = " + width + "\nheight = " + height);
	
			daTa = fp.readLine();
			daTa = fp.readLine();
			daTa = fp.readLine();
			daTa = fp.readLine();
			String name = typeOfData+"_"+fileName;
			st = new StringTokenizer(name," \t\n\r\f:.;");
			img = NewImage.createFloatImage(st.nextToken(), width, height, 1, NewImage.FILL_BLACK);
			ip = img.getProcessor();	
			
			for(x = 0; x < height; x++) //ширина
			    for(y = 0; y < width; y++) { //высота
					line = fp.readLine();
					st = new StringTokenizer(line," \t\n\r\f:");
					//st.parseNumbers();
					xxx = atof(st.nextToken());
					yyy = atof(st.nextToken());
					zzz = atof(st.nextToken());

					ip.setf(y, x, (float)zzz);
			    }
				      			//img.show();
			img.updateAndDraw();

		}
		catch (Exception e) {
			IJ.error("Reader"+wII+filed+typeOfData+nameOfData+width + height, e.getMessage());
			return;
		}
	
		int [] www = {(int)(1000*xxx/width), (int)(1000*xxx/height), 1};
		//IJ.setColumnHeadings("  Width coeff.  \t   Heigh coeff.   \t    Z coeff.    \t     N     ");
		//IJ.write(IJ.d2s((double)www[0]/1000000, 5)+'\t'+ IJ.d2s((yyy/height)/1000, 5)+'\t'+ IJ.d2s(0.001, 5)+ '\t'+  IJ.d2s(summ, 0));
		
		img.setProperty(Key, www);
		
		//if (img==null) return;
		img.show();
		}


	private static double atof(String s)
	{
		double d = Double.valueOf(s).doubleValue();
		if (Double.isNaN(d) || Double.isInfinite(d))
		{
			IJ.showMessage("Error!", "NaN or Infinity in input");
			System.exit(1);
		}
		return(d);
	}

	private static int atoi(String s)
	{
		return Integer.parseInt(s);
	}
	


	private static final boolean isInteger(final String s) {
	  boolean flag = false;
	  for (int x = 0; x < s.length(); x++) {
	    final char c = s.charAt(x);
	    if (x == 0 && (c == '-')) continue;  // negative
	    if ((c >= '0') && (c <= '9')) {flag=true; continue;}  // 0 - 9
	    return false; // invalid
	  }
	  return flag; // valid
	}
	
	 public boolean isNumber( String value )
	    {

	        boolean result = false;


	        if( pattern == null )
	            pattern = Pattern.compile( regex );

	        Matcher matcher = pattern.matcher( value );

	        if( matcher.matches() )
	            result = true;

	        return result;
	    }



}

