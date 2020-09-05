import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.filter.*;

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
		double adher = 0;
		double s1, s2, s3, s4, h, st1, st2, p;	
		double ss1, ss2, ss3, ss4;
		
		String volume;
		String area;
		String maximum;
		String diametr;
		String totArea;
		String adherion;
		Rectangle r = ip.getRoi();
		minu = ip.getf(r.x,r.y);
		double kW, kH;
		String key1 = "COEFS";
		int [] wd = (int[]) imp.getProperty(key1);
		kW = (double)(wd[0]+wd[1])/2000000;
		kH = (double)wd[2]/1000;

		ImageProcessor mask = ip.getMask();
		boolean hasMask = (mask != null);
		
		for (int y=r.y; y<(r.y+r.height); y++)
		   for (int x=r.x; x<(r.x+r.width); x++)
	 	       if (!hasMask || mask.getPixel(x-r.x, y-r.y) > 0){
			if(maxim < ip.getf(x,y)) maxim = ip.getf(x,y);
			if(minu > ip.getf(x,y)) minu = ip.getf(x,y);
			adher++;
			}
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

		volume = IJ.d2s(v,3);
		area = IJ.d2s(a,3);
		maxim *= kH;
		maximum = IJ.d2s(maxim-kH*minu, 3);
		adher = adher*kW*kW;
		adherion = IJ.d2s(adher, 3);
		diametr = IJ.d2s(Math.sqrt((adher*3.14)/4), 2);
		totArea = IJ.d2s(adher+a);


	if(!IJ.isResultsWindow()){
		IJ.setColumnHeadings("height\tdiametr\tvolume\ttop area\tbase area\ttotal area\tarea index\t volume index\tthres level");
		IJ.write(maximum+'\t'+diametr+'\t'+volume+'\t'+area+'\t'+adherion+'\t'+totArea+'\t'+ IJ.d2s(a/adher, 3)+'\t'+ IJ.d2s(v/adher, 3)+'\t'+ IJ.d2s(minu*kH, 3));
		} else IJ.write(maximum+'\t'+diametr+'\t'+volume+'\t'+area+'\t'+adherion+'\t'+totArea+'\t'+ IJ.d2s(a/adher, 3)+'\t'+ IJ.d2s(v/adher, 3)+'\t'+ IJ.d2s(minu*kH, 3));
	}

}
