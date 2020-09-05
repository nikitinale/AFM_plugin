import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.filter.*;
import ij.measure.Calibration;
//import java.io.*;
import java.util.*;


public class Volume_Meter implements PlugInFilter {
	ImagePlus imp;

	public int setup(String arg, ImagePlus imp) {
		this.imp = imp;
		return DOES_32+SUPPORTS_MASKING;
	}

	public void run(ImageProcessor ip) {
		double v = 0;
		double a = 0;
		double maxim = 0;
		double minu;
		double adher = 0; //Площадь проекции
		double s1, s2, s3, s4, h, st1, st2, p;	
		double ss1, ss2, ss3, ss4;
		double averH = 0, stdDev = 0, sherhov = 0, mom3 = 0, mom4 = 0;
		
		String volume;
		String area;
		String maximum;
		String diametr;
		String totArea;
		String adherion;
		Rectangle r = new Rectangle(0, 0, ip.getWidth(), ip.getHeight());
		if(ip.getRoi() != null) r = ip.getRoi();
		minu = ip.getf(r.x,r.y);
		double kW, kH;
		String key1 = "COEFS";
		int [] wd = (int[]) imp.getProperty(key1);
		kW = (double)(wd[0]+wd[1])/2000000; // площадь занимаемая пикселом (мкм)
		kH = (double)wd[2]/1000; // Высота одной еденицы (мкм)

		boolean hasMask;
		ImageProcessor mask = ip.getMask();
		if(mask != null)
			hasMask = true;
		else hasMask = false;
		
		// В цикле поиск максимума и минимума на выделении
		// определение площади проекции
		for (int y=r.y; y<(r.y+r.height); y++)
		   for (int x=r.x; x<(r.x+r.width); x++)
	 	       if (!hasMask || (mask.getPixel(x-r.x, y-r.y) > 0)){
				if(maxim < ip.getf(x,y)) maxim = ip.getf(x,y);
				if(minu > ip.getf(x,y)) minu = ip.getf(x,y);
				adher++;
				averH+=ip.getf(x,y)*kH;
			}
		adher -=(ip.getWidth()+ip.getHeight());
		averH = averH/adher;
		// Расчет стандартного квадратичного отклонени, 2-4 моментов
		for (int y=r.y; y<(r.y+r.height); y++)
		   for (int x=r.x; x<(r.x+r.width); x++)
	 	       if (!hasMask || (mask.getPixel(x-r.x, y-r.y) > 0)){
	 	    	sherhov += Math.abs(averH-ip.getf(x,y)*kH);
				stdDev+=(averH-ip.getf(x,y)*kH)*(averH-ip.getf(x,y)*kH);
				mom3+=(averH-ip.getf(x,y)*kH)*(averH-ip.getf(x,y)*kH)*(averH-ip.getf(x,y)*kH);
				mom4+=(averH-ip.getf(x,y)*kH)*(averH-ip.getf(x,y)*kH)*(averH-ip.getf(x,y)*kH)*(averH-ip.getf(x,y)*kH);
			}
			sherhov = sherhov/adher;
			stdDev = Math.sqrt(stdDev/adher);
			mom3 = mom3/(adher*stdDev*stdDev*stdDev);
			mom4 = mom4/(adher*stdDev*stdDev*stdDev*stdDev);
		
		
		//В цикле подщет объема (в каждом пикселе умножается высота на занимаемую им площадь.
		//Затем идет расчет площади поверхности: площадь каждого пикселя делится на четыре треугольника
		// высота центра - средняя от высоты четырех соседних пикселов - углов
		// расчитывается площадь каждого треугольника и складывается
		//!! надо изменить: расчет градиента в каждом пикселе и умножения sin/cos угла 
		// на площадь проекции пиксела
		for (int y=r.y; y<(r.y+r.height); y++)
		   for (int x=r.x; x<(r.x+r.width); x++)
	 	       if (!hasMask || mask.getPixel(x-r.x, y-r.y) > 0){
				v += kW*kW*kH*(ip.getf(x,y)-minu);
			
			if((y > r.y) && (x > r.x)){
			
			h = kH*((ip.getf(x,y)+ip.getf(x-1,y)+ip.getf(x,y-1)+ip.getf(x-1,y-1))/4);
			
			s1 =Math.sqrt(kW*kW+(kH*ip.getf(x,y) - kH*ip.getf(x-1,y))*(kH*ip.getf(x,y) - kH*ip.getf(x-1,y)));
			s2 = Math.sqrt(kW*kW+(kH*ip.getf(x,y) - kH*ip.getf(x,y-1))*(kH*ip.getf(x,y) - kH*ip.getf(x,y-1)));
			s3 = Math.sqrt(kW*kW+(kH*ip.getf(x-1,y-1) - kH*ip.getf(x-1,y))*(kH*ip.getf(x-1,y-1) - kH*ip.getf(x-1,y)));
			s4 = Math.sqrt(kW*kW+(kH*ip.getf(x-1,y-1) - kH*ip.getf(x,y-1))*(kH*ip.getf(x-1,y-1) - kH*ip.getf(x,y-1)));
			
			st1=Math.sqrt(kW*kW*0.5*0.5+kW*kW*0.5*0.5+(kH*ip.getf(x,y)-h)*(kH*ip.getf(x,y)-h));
			st2=Math.sqrt(kW*kW*0.5*0.5+kW*kW*0.5*0.5+(kH*ip.getf(x-1,y)-h)*(kH*ip.getf(x-1,y)-h));
			p = (s1+st1+st2)/2;
			ss1 = Math.sqrt(p*(p-s1)*(p-st1)*(p-st2));

			st1=Math.sqrt(kW*kW*0.5*0.5+kW*kW*0.5*0.5+(kH*ip.getf(x,y)-h)*(kH*ip.getf(x,y)-h));
			st2=Math.sqrt(kW*kW*0.5*0.5+kW*kW*0.5*0.5+(kH*ip.getf(x,y-1)-h)*(kH*ip.getf(x,y-1)-h));
			p = (s2+st1+st2)/2;
			ss2 = Math.sqrt(p*(p-s2)*(p-st1)*(p-st2));

			st1=Math.sqrt(kW*kW*0.5*0.5+kW*kW*0.5*0.5+(kH*ip.getf(x-1,y)-h)*(kH*ip.getf(x-1,y)-h));
			st2=Math.sqrt(kW*kW*0.5*0.5+kW*kW*0.5*0.5+(kH*ip.getf(x-1,y-1)-h)*(kH*ip.getf(x-1,y-1)-h));
			p = (s3+st1+st2)/2;
			ss3 = Math.sqrt(p*(p-s3)*(p-st1)*(p-st2));

			st1=Math.sqrt(kW*kW*0.5*0.5+kW*kW*0.5*0.5+(kH*ip.getf(x,y-1)-h)*(kH*ip.getf(x,y-1)-h));
			st2=Math.sqrt(kW*kW*0.5*0.5+kW*kW*0.5*0.5+(kH*ip.getf(x-1,y-1)-h)*(kH*ip.getf(x-1,y-1)-h));
			p = (s4+st1+st2)/2;
			ss4 = Math.sqrt(p*(p-s4)*(p-st1)*(p-st2));

			a += ss1;
			a += ss2;
			a += ss3;
			a += ss4;
			}
		}
		
		//run("Find Maxima...");
		
		ImageProcessor mask2 = new FloatProcessor(0, 0);
		if(hasMask)
			mask2 = mask.duplicate();
		ImageProcessor ip2 = ip.duplicate();
		
		int[] hist = new int[1001];
		int sum = 0, tsum=0;
		int maxs = 0, mins = 0;
		
		for (int y=r.y; y<(r.y+r.height); y++)
			   for (int x=r.x; x<(r.x+r.width); x++)
		 	       if (!hasMask ||( mask.getPixel(x-r.x, y-r.y) > 0)){
		 	    	  hist[(int)(1000*(ip.getf(x,y)-minu)/(maxim-minu))]++;
		 	    	  sum++;
				}
		int yy;
		for(yy = 0; yy < 1001; yy++) {
			tsum+=hist[yy];
			if(tsum>((int)(0.025*(double)sum))) break;
		}
		double min95 = minu+((double)yy/1000f*(maxim-minu));
		for(yy = yy++; yy < 1001; yy++) {
			tsum+=hist[yy];
			if(tsum>((int)(0.975*(double)sum))) break;
		}
		double max95 = minu+((double)yy/1000f*(maxim-minu));
		max95-=min95;
		max95 *=kH;
		
		double sumh = 0;
		int counth = 0, cou = 0;
		double tan;
		double gamma;
		Vector<Double> gamv = new Vector();
		Vector<Double> ahv = new Vector();
		
		for(int ah = 2; ah < (r.width+r.height)/25; ah++) {
			for (int y=r.y; y<(r.y+r.height-ah); y++)
			   for (int x=r.x; x<(r.x+r.width-ah); x++)
		 	       if (!hasMask || ((mask.getPixel(x-r.x, y-r.y) > 0) && (mask.getPixel(x-r.x, y-r.y+ah) > 0) && (mask.getPixel(x-r.x+ah, y-r.y) > 0))){
		 	    	  sumh += kH*kH*(ip.getf(x,y)-ip.getf(x+ah,y))*(ip.getf(x,y)-ip.getf(x+ah,y)) + kH*kH*(ip.getf(x,y)-ip.getf(x,y+ah))*(ip.getf(x,y)-ip.getf(x,y+ah));
		 	    	  counth++;
				}
			gamma = Math.log(sumh/((double)(2*counth)));
			//log-log D = 3 - slope/2  a = (n [xy] - [y][x])/(n[x2] - ([x])2) 
			tan = gamma/Math.log(kW*ah);
			gamv.addElement(gamma);
			ahv.addElement(Math.log(kW*ah));
			cou++;
			//IJ.showMessage("Semivariogramm", "h = " +Math.log(ah)+ " Y = " + gamma + " TAN = " + tan);
			
			
		}
		double XY = 0, YYY = 0, XXX = 0, X2 = 0;
		for(int ah = 0; ah < cou; ah++) {
			YYY += gamv.elementAt(ah);
			XY += gamv.elementAt(ah)*ahv.elementAt(ah);
			XXX += ahv.elementAt(ah);
			X2 += ahv.elementAt(ah)*ahv.elementAt(ah);
		}
		gamma = (cou*XY-XXX*YYY)/(cou*X2-XXX*XXX);
		gamma = 3-gamma/2;
		//IJ.showMessage("Semivariogramm", " Y = " + gamma);
		
		float al = 0.0001f*(float)(maxim-minu);
		GaussianBlur gb = new GaussianBlur();
		gb.blur(ip2, 0.5);
		ip2.setRoi(r);
		ip2.setMask(mask2);	
		for (int y=r.y+1; y<(r.y+r.height-1); y++)
			   for (int x=r.x+1; x<(r.x+r.width-1); x++)
		 	       if (!hasMask || ((mask2.getPixel(x-r.x, y-r.y) > 0))){
		 	    	  if(((ip2.getf(x,y)-al)>ip2.getf(x+1,y)) && ((ip2.getf(x,y)-al)>ip2.getf(x+1,y+1)) && ((ip2.getf(x,y)-al)>ip2.getf(x,y+1)) && ((ip2.getf(x,y)-al)>ip2.getf(x-1,y)) &&((ip2.getf(x,y)-al)>ip2.getf(x-1,y-1)) && ((ip2.getf(x,y)-al)>ip2.getf(x,y-1)) && ((ip2.getf(x,y)-al)>ip2.getf(x+1,y-1)) && ((ip2.getf(x,y)-al)>ip2.getf(x-1,y+1)))
		 	    		  maxs++;
		 	    	  if(((ip2.getf(x,y)+al)<ip2.getf(x+1,y)) && ((ip2.getf(x,y)+al)<ip2.getf(x+1,y+1)) && ((ip2.getf(x,y)+al)<ip2.getf(x,y+1)) && ((ip2.getf(x,y)+al)<ip2.getf(x-1,y)) &&((ip2.getf(x,y)+al)<ip2.getf(x-1,y-1)) && ((ip2.getf(x,y)+al)<ip2.getf(x,y-1)) && ((ip2.getf(x,y)+al)<ip2.getf(x+1,y-1)) && ((ip2.getf(x,y)+al)<ip2.getf(x-1,y+1)))
		 	    		  mins++;
				}
				
		ip2.setRoi(r);
		
		
		
		//ImagePlus ipp = new ImagePlus("new", ip2);
		//ipp.setRoi(r);
		//ipp.setMask(mask2);
		//HistogramWindow hw = new HistogramWindow("Histogram", ipp, 50);
		//ipp.show();
		//ipp.updateAndDraw();
		
		
	

		volume = IJ.d2s(v,3);
		area = IJ.d2s(a,3);
		maxim *= kH;
		maximum = IJ.d2s(maxim-kH*minu, 3);
		adher = adher*kW*kW;
		adherion = IJ.d2s(adher, 3);
		diametr = IJ.d2s((2*Math.sqrt(adher/3.14)), 2);
		totArea = IJ.d2s(adher+a);
		double maxsd = (double)maxs/adher;
		double minsd = (double)mins/adher;


	if(!IJ.isResultsWindow()){
		IJ.setColumnHeadings("name\theight\tdiametr\tvolume\ttop area\tbase area\ttotal area\tarea index\t volume index\tStd. deviation\tAssimetry\tExcess\tSherhov\tRange 95\tNpiks\tNpits\tDf\tthres level\taver heigh");
		IJ.write(imp.getTitle()+'\t'+maximum+'\t'+diametr+'\t'+volume+'\t'+area+'\t'+adherion+'\t'+totArea+'\t'+ IJ.d2s(a/adher, 4)+'\t'+ IJ.d2s(v/(a+adher), 4)+'\t'+ IJ.d2s(stdDev, 3)+'\t'+ IJ.d2s(mom3, 3)+'\t'+ IJ.d2s(mom4, 3)+'\t'+ IJ.d2s(sherhov, 3)+'\t'+ IJ.d2s(max95, 4)+'\t'+ IJ.d2s(maxsd, 2)+'\t'+ IJ.d2s(minsd, 2)+'\t'+ IJ.d2s(gamma, 3)+'\t'+ IJ.d2s(minu*kH, 3)+'\t'+ IJ.d2s(averH, 3));
		} else IJ.write(imp.getTitle()+'\t'+maximum+'\t'+diametr+'\t'+volume+'\t'+area+'\t'+adherion+'\t'+totArea+'\t'+ IJ.d2s(a/adher, 4)+'\t'+ IJ.d2s(v/(a+adher), 4)+'\t'+ IJ.d2s(stdDev, 3)+'\t'+ IJ.d2s(mom3, 3)+'\t'+ IJ.d2s(mom4, 3)+'\t'+ IJ.d2s(sherhov, 3)+'\t'+ IJ.d2s(max95, 4)+'\t'+ IJ.d2s(maxsd, 2)+'\t'+ IJ.d2s(minsd, 2)+'\t'+ IJ.d2s(gamma, 3)+'\t'+ IJ.d2s(minu*kH, 3)+'\t'+ IJ.d2s(averH, 3));
	}

}
