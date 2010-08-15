package com.neophob.sematrix.generator;

import com.neophob.sematrix.effect.BoxFilter;
import com.neophob.sematrix.glue.Collector;

import processing.core.PConstants;
import processing.core.PImage;


public class TextureDeformation extends Generator {	

	private int w, h;
	private int[] mLUT;
	private int[] tmp;
	private PImage textureImg;
	private int timeDisplacement;
	private int lut;

	public TextureDeformation(String filename) {
		super(GeneratorName.TEXTURE_DEFORMATION);
		w = getInternalBufferXSize();
		h = getInternalBufferYSize();
		mLUT =  new int[3 * w * h];
		tmp = new int[this.internalBuffer.length];
		// use higher resolution textures if things get to pixelated
		textureImg=Collector.getInstance().getPapplet().loadImage(filename);
		
		lut=9;
		createLUT(lut);
	}

	public void changeLUT(int lut) {
		this.lut = lut;
		createLUT(lut);
	}
	
	public void loadFile(String fileName) {
		textureImg=Collector.getInstance().getPapplet().loadImage(fileName);
		createLUT(lut);
	}
	
	@Override
	public void update() {
		textureImg.loadPixels();
		for (int pixelCount = 0; pixelCount < getInternalBufferSize(); pixelCount++)
		{
			int o = (pixelCount << 1) + pixelCount;  // equivalent to 3 * pixelCount
			int u = mLUT[o+0] + timeDisplacement;    // to look like its animating, add timeDisplacement
			int v = mLUT[o+1] + timeDisplacement;
			int adjustBrightness = mLUT[o+2];

			// get the R,G,B values from texture
			int currentPixel = textureImg.pixels[textureImg.width * (v & textureImg.height-1) + (u & textureImg.width-1)];

			// only apply brightness if it was calculated
			if (adjustBrightness != 0) {       
				int r,g,b;

				// disassemble pixel using bit mask to remove color components for greater speed
				r = currentPixel >> 16 & 0xFF;  
				g = currentPixel >> 8 & 0xFF;   
				b = currentPixel & 0xFF;              
		
				// make darker or brighter
				r += adjustBrightness;
				g += adjustBrightness;
				b += adjustBrightness;
		
				// constrain RGB to make sure they are within 0-255 color range
				r = constrain(r,0,255);
				g = constrain(g,0,255);
				b = constrain(b,0,255);
		
				// reassemble colors back into pixel
				currentPixel = (r << 16) | (g << 8) | (b);
			}

			// put texture pixel on buffer screen
			tmp[pixelCount] = currentPixel;
		}
		textureImg.updatePixels();
		this.internalBuffer = BoxFilter.applyBoxFilter(0, 1, tmp, getInternalBufferXSize());
		timeDisplacement++;
	}

	static public final int constrain(int amt, int low, int high) {
		return (amt < low) ? low : ((amt > high) ? high : amt);
	}

	static public final float constrain(float amt, float low, float high) {
		return (amt < low) ? low : ((amt > high) ? high : amt);
	}
	
	private void createLUT(int effectStyle){
		// increment placeholder
		int k = 0;

		// u and v are euclidean coordinates  
		float u,v,bright = 0; 

		for( int j=0; j < h; j++ )
		{
			float y = -1.00f + 2.00f*(float)j/(float)h;
			for( int i=0; i < w; i++ )
			{
				float x = -1.00f + 2.00f*(float)i/(float)w;
				float d = (float)Math.sqrt( x*x + y*y );
				float a = (float)Math.atan2( y, x );
				float r = d;
				switch(effectStyle) {
				case 1:   // stereographic projection / anamorphosis 
					u = (float)Math.cos( a )/d;
					v = (float)Math.sin( a )/d;
					bright = -10 * (2/(6*r + 3*x));
					break;
				case 2:  // hypnotic rainbow spiral
					v = (float)Math.sin(a+(float)Math.cos(3*r))/(float)(Math.pow(r,.2));
					u = (float)Math.cos(a+(float)Math.cos(3*r))/(float)(Math.pow(r,.2));
					bright = 1;
					break;
				case 3:  // rotating tunnel of wonder
					v = 2/(6*r + 3*x);
					u = a*3/PConstants.PI;
					bright = 15 * -v;
					break;
				case 4:  // wavy star-burst
					v = (-0.4f/r)+.1f*(float)Math.sin(8*a);
					u = .5f + .5f*a/PConstants.PI;
					bright=0;
					break;
				case 5:  // hyper-space travel
					u = (0.02f*y+0.03f)*(float)Math.cos(a*3)/r;
					v = (0.02f*y+0.03f)*(float)Math.sin(a*3)/r;
					bright=0;
					break;
				case 6:  // five point magnetic flare
					u = 1f/(r+0.5f+0.5f*(float)Math.sin(5*a));
					v = a*3/PConstants.PI;
					bright = 0;
					break;
				case 7:  // cloud like dream scroll
					u = 0.1f*x/(0.11f+r*0.5f);
					v = 0.1f*y/(0.11f+r*0.5f);
					bright=0;
					break;
				case 8:  // floor and ceiling with fade to dark horizon
					u = x/(float)Math.abs(y);
					v = 1/(float)Math.abs(y);
					bright = 10* -v;
					break;
				case 9:  // hot magma liquid swirl
					u = 0.5f*(a)/PConstants.PI;
					v = (float)Math.sin(2*r);
					bright = 0;
					break;
				case 10:  // clockwise flush down the toilet
					v = (float)Math.pow(r,0.1);
					u = (1*a/PConstants.PI)+r;
					bright=0;
					break;
				case 11:  // 3D ball
					v = x*(3-(float)Math.sqrt(4-5*r*r))/(r*r+1);
					u = y*(3-(float)Math.sqrt(4-5*r*r))/(r*r+1);
					bright = 7f * -18.7f*(x+y+r*r-(x+y-1)*(float)Math.sqrt(4-5*r*r)/3)/(r*r+1);
					break;
				default:  // show texture with no deformation or lighting
					u = x;
					v = y;
					bright = 0;
					break;
				}
				mLUT[k++] = (int)(textureImg.width*u) & textureImg.width-1;
				mLUT[k++] = (int)(textureImg.height*v) & textureImg.height-1;
				mLUT[k++] = (int)(bright);
			}
		}
	}


	@Override
	public void close() {
		
	}
}