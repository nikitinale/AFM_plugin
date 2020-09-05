import java.io.*;
import ij.*;
import ij.io.*;
import ij.gui.*;
import ij.plugin.*;
import ij.process.*;
import java.util.*;

public class ASCII_reader implements PlugIn {
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
		try {
			File f = new File(directory+fileName); 
			FileInputStream fis = new FileInputStream(f); 
			BufferedInputStream bis = new BufferedInputStream(fis); 
			dis = new DataInputStream(bis);
            	}
	catch (Exception e) {
		IJ.error("Simple ASCII Reader FIRST", e.getMessage());
		return;
	}

		try {
			scan = new Scanner(dis);
			wII = scan.nextLine();
			filed = scan.nextLine();
			typeOfData = scan.nextLine();  //record=dis.readLine(); // Type
			nameOfData = scan.nextLine(); //record=dis.readLine(); // Name
			while(!scan.hasNextInt()){
                			scan.next();
			}
			width = scan.nextInt();
                    		
			while(!scan.hasNextInt()){
                			scan.next();
			}
			height = scan.nextInt();

			daTa = scan.nextLine();
			daTa = scan.nextLine();
			daTa = scan.nextLine();
			daTa = scan.nextLine();
			daTa = scan.nextLine();
			//daTa = scan.nextLine();

			img = NewImage.createFloatImage(nameOfData, width, height, 1, NewImage.FILL_BLACK);
			ip = img.getProcessor();	
			
			for(x = 0; x < height; x++) //ширина
			    for(y = 0; y < width; y++) //высота
				for(int i = 0; i < 3; i++)
				      switch(i) {
 					case 0: xxx = scan.nextDouble(); break;
					case 1: yyy = scan.nextDouble(); break;
					case 2: zzz = scan.nextDouble(); ip.setf(y, x, (float)zzz); break;
			    		}
			//img.show();
			img.updateAndDraw();

		}
		catch (Exception e) {
			IJ.error("Simple ASCII Reader"+wII+filed+typeOfData+nameOfData+daTa+ IJ.d2s(width, 0)+ IJ.d2s(height, 0), e.getMessage());
			return;
		}
	
		int [] www = {(int)(1000*xxx/width), (int)(1000*xxx/height), 1};
		IJ.setColumnHeadings("  Width coeff.  \t   Heigh coeff.   \t    Z coeff.    \t     N     ");
		IJ.write(IJ.d2s((double)www[0]/1000000, 5)+'\t'+ IJ.d2s((yyy/height)/1000, 5)+'\t'+ IJ.d2s(0.001, 5)+ '\t'+  IJ.d2s(summ, 0));
		
		img.setProperty(Key, www);
		
		//if (img==null) return;
		img.show();
		}


}

