import java.io.*;
import java.util.*;

import java.awt.*;
import javax.imageio.*;
import java.awt.image.BufferedImage;

public class Dither {
	public static void main(String[] args) throws IOException {
		File file = new File("../images/originals/insta.jpg");
		BufferedImage originalImage = ImageIO.read(file);
		System.out.println("width: "+originalImage.getWidth());
		System.out.println("height: "+originalImage.getHeight());
		//
		//Jet_Point jp = new Jet_Point(3,4,5,6,7.654,3.1415926);
		//jp.set(1,10);
		//System.out.println(jp);
		//System.out.println(jp.dimensions());
		//System.out.println(jp.get(4));

		//System.out.println(findClosestColor(new Jet_Point(-500,130,280)));
		BufferedImage ditheredImage = dither(originalImage);
		try {
			ImageIO.write(ditheredImage, "png", new File("../images/dithered/instaDith.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public enum DitheringAlgorithm {
		FLOYD_STEINBERG
	}

	public static BufferedImage dither(BufferedImage originalImage) {
		int width = originalImage.getWidth();
		int height = originalImage.getHeight();

		BufferedImage ditheredImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics graphics = ditheredImage.getGraphics();

		Jet_Point[][] quantizedColors = new Jet_Point[width][height];
		for (int y = 0; y<height; y++) {
			for (int x = 0; x<width; x++) {

				Color color = new Color(originalImage.getRGB(x,y),true);
				int r  = color.getRed();
				int g = color.getGreen();
				int b = color.getBlue();

				quantizedColors[x][y] = new Jet_Point(r, g, b);
			}
		}

		//System.out.println("corner "+quantizedColors[0][0]);
		//System.out.println("corner+458 "+quantizedColors[0][458]);
		//System.out.println("corner+458 "+quantizedColors[458][0]);

		ArrayList<Jet_Point> distributionDirectionAndPortion = getDitheringAlgorithm(DitheringAlgorithm.FLOYD_STEINBERG);
		System.out.println("FLOYD_STEINBERG: "+distributionDirectionAndPortion);

		for (int y = 0; y<height; y++) {
			for (int x = 0; x<width; x++) {
				Jet_Point originalColor = quantizedColors[x][y];
				Jet_Point quantizedColor = findClosestColor(quantizedColors[x][y]);

				double r = originalColor.get(0);
				double g = originalColor.get(1);
				double b = originalColor.get(2);
				double errorR = r-quantizedColor.get(0);
				double errorG = g-quantizedColor.get(1);
				double errorB = b-quantizedColor.get(2);

				originalColor.set(0,quantizedColor.get(0));
				originalColor.set(1,quantizedColor.get(1));
				originalColor.set(2,quantizedColor.get(2));

				for (int i = 0; i<distributionDirectionAndPortion.size(); i++) {
					Jet_Point locData = distributionDirectionAndPortion.get(i);
					try {
						Jet_Point neighborColor = quantizedColors[x+(int)locData.get(0)][y+(int)locData.get(1)];
						neighborColor.set(0,neighborColor.get(0)+(errorR*locData.get(2)));
						neighborColor.set(1,neighborColor.get(1)+(errorG*locData.get(2)));
						neighborColor.set(2,neighborColor.get(2)+(errorB*locData.get(2)));
						//System.out.println(quantizedColors[x+(int)locData.get(0)][y+(int)locData.get(1)]);
					} catch (Exception e) {}
				}
			}
		}

		for (int y = 0; y<height; y++) {
			for (int x = 0; x<width; x++) {
				Jet_Point color = quantizedColors[x][y];
				graphics.setColor(new Color((int)color.get(0), (int)color.get(1),(int)color.get(2)));
				graphics.fillRect(x,y,1,1);
			}
		}

		return ditheredImage;
	}

	public static ArrayList<Jet_Point> getDitheringAlgorithm(DitheringAlgorithm algo) {
		ArrayList<Jet_Point> distributionDirectionAndPortion = new ArrayList<Jet_Point>();
		switch (algo) {
			case FLOYD_STEINBERG:
				distributionDirectionAndPortion.add(new Jet_Point(1,0,7.0/16.0));
				distributionDirectionAndPortion.add(new Jet_Point(-1,1,3.0/16.0));
				distributionDirectionAndPortion.add(new Jet_Point(0,1,5.0/16.0));
				distributionDirectionAndPortion.add(new Jet_Point(1,1,1.0/16.0));
				break;
			default:
				System.out.println();
				break;
		}
		return distributionDirectionAndPortion;
	}

	public static Jet_Point findClosestColor(Jet_Point jp) {
		double r = jp.get(0);
		double g = jp.get(1);
		double b = jp.get(2);
		//System.out.println(jp);
		int quantizedR = ((int)r<128)?0:255;
		int quantizedG = ((int)g<128)?0:255;
		int quantizedB = ((int)b<128)?0:255;

		Jet_Point quantizedColor = new Jet_Point(quantizedR,quantizedG,quantizedB);
		return quantizedColor;
	}

	public static Jet_Point findClosestColor2(Jet_Point jp) {
		double r = jp.get(0);
		double g = jp.get(1);
		double b = jp.get(2);
		//System.out.println(jp);
		int quantizedR = ((int)r<32)?0:((int)r<96)?64:((int)r<160)?128:((int)r<224)?192:255;
		int quantizedG = ((int)g<32)?0:((int)g<96)?64:((int)g<160)?128:((int)g<224)?192:255;
		int quantizedB = ((int)b<32)?0:((int)b<96)?64:((int)b<160)?128:((int)b<224)?192:255;

		Jet_Point quantizedColor = new Jet_Point(quantizedR,quantizedG,quantizedB);
		return quantizedColor;
	}
}
