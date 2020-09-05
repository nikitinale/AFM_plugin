import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.filter.*;

public class Image_Param_Calculator implements PlugInFilter {
	ImagePlus imp;

	public int setup(String arg, ImagePlus imp) {
		this.imp = imp;
		return DOES_RGB+NO_CHANGES;
	}

	public void run(ImageProcessor ip) {
		int[] color = (int[]) ip.getPixels();
		int width = ip.getWidth();
		int height = ip.getHeight();
		int size = width*height;
		int[][] red = new int[height][width];
		int[] green = new int[size];
		int[] blue = new int[size];
		for(int i = 0; i < size; i++){
		     //red[i] = (int)(color[i] & 0xff0000) >> 16;
		     green[i] = (int)(color[i] & 0x00ff00) >> 8;
		     blue[i] = (int)(color[i] & 0x0000ff) ;
		}

		for(int i = 0; i < height; i++)
		      for(int j = 0; j < width; j++)
		            red[i][j] = (int)(color[i*width+j] & 0xff0000) >> 16;

		int radius;
		radius = 30;
		int sum = 0;
		int n = 0;
		int[][] red_average = new int[height-2*radius][width-2*radius];
		
		for(int i = radius; i < height-radius; i++)
		     for(int j = radius; j <width-radius; j++){
		           n = 0; sum = 0;
		           for(int ii = i-radius; ii < i+radius; ii++)
		                 for(int jj = j-radius; jj < j+radius; jj++)
		                       if(sqrt(sqr(i-ii)+sqr(j-jj)) < radius){
			     n++;
		                             sum += red[ii][jj];
		                       }
		            red_average[i][j] = (int)(sum/n);
		      }
		ImagePlus rd_im = NewImage.createByteImage("RED", width, height, 1, NewImage.FILL_BLACK);
		ImageProcessor rd_prc = rd_im.getProcessor();
		
		for(int i = 0; i < height; i++)
		      for(int j = 0; j < width; j++)
		          rd_prc.set(j, i, red[i][j]);
		
		rd_im.show();
		rd_im.updateAndDraw();
	}

}
