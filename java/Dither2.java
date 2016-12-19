import java.io.*;
import java.util.*;

import java.awt.*;
import javax.imageio.*;
import java.awt.image.BufferedImage;

public class Dither2 {

	public enum DitheringAlgorithm {
		FLOYD_STEINBERG, ATKINSON, JJN, SIERRA
	}

	public static void main(String[] args) throws IOException {
		File file = new File("../images/originals/vincent.jpg");
		BufferedImage originalImage = ImageIO.read(file);

		int width = originalImage.getWidth();
		int height = originalImage.getHeight();

		System.out.println();
		System.out.println("width: "+width);
		System.out.println("height: "+height);
		System.out.println();

		System.out.println(originalImage);
		BufferedImage ditheredImage = ditherImage(originalImage,DitheringAlgorithm.FLOYD_STEINBERG);
		BufferedImage invertedImage = invertImage(originalImage);
		try {
			ImageIO.write(ditheredImage, "png", new File("../images/dithered/vincentDith.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			ImageIO.write(invertedImage, "png", new File("../images/dithered/vincentInv.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static double[][][] imageToBrokenImage(BufferedImage image) {
		int width = image.getWidth();
		int height = image.getHeight();

		double[][][] brokenImage = new double[width][height][3];
		for (int y = 0; y<height; y++) {
			for (int x = 0; x<width; x++) {
				Color color = new Color(image.getRGB(x,y),true);
				brokenImage[x][y][0] = color.getRed();
				brokenImage[x][y][1] = color.getGreen();
				brokenImage[x][y][2] = color.getBlue();
			}
		}
		return brokenImage;
	}

	public static BufferedImage brokenImageToImage(double[][][] brokenImage) {
		int width = brokenImage.length;
		int height = brokenImage[0].length;

		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics g = image.getGraphics();

		for (int y = 0; y<height; y++) {
			for (int x = 0; x<width; x++) {
				double[] color = brokenImage[x][y];
				g.setColor(new Color((int)color[0], (int)color[1],(int)color[2]));
				g.fillRect(x,y,1,1);
			}
		}

		return image;
	}

	public static ArrayList<double[]> getDitheringAlgorithm(DitheringAlgorithm algorithmName) {
		ArrayList<double[]> errorDistributions = new ArrayList<double[]>();
		switch (algorithmName) {
			case FLOYD_STEINBERG:
				errorDistributions.add(new double[]{1,0,7.0/16.0});
				errorDistributions.add(new double[]{-1,1,3.0/16.0});
				errorDistributions.add(new double[]{0,1,5.0/16.0});
				errorDistributions.add(new double[]{1,1,1.0/16.0});
				break;
			case ATKINSON:
				errorDistributions.add(new double[]{1,0,1.0/8.0});
				errorDistributions.add(new double[]{2,0,1.0/8.0});
				errorDistributions.add(new double[]{-1,1,1.0/8.0});
				errorDistributions.add(new double[]{0,1,1.0/8.0});
				errorDistributions.add(new double[]{1,1,1.0/8.0});
				errorDistributions.add(new double[]{0,2,1.0/8.0});
				break;
			default:
				System.out.println();
				break;
		}
		return errorDistributions;
	}

	public static double[][][] cloneBrokenImage(double[][][] brokenImage) {
		int width = brokenImage.length;
		int height = brokenImage[0].length;

		double[][][] brokenImageCopy = new double[width][height][3];

		for (int y = 0; y<height; y++) {
			for (int x = 0; x<width; x++) {
				for (int i = 0; i<3; i++) {
					brokenImageCopy[x][y][i] = brokenImage[x][y][i];
				}
			}
		}

		return brokenImageCopy;
	}

	public static double[][][] ditherBrokenImage(double[][][] brokenImage, ArrayList<double[]> errorDistributions) {
		int width = brokenImage.length;
		int height = brokenImage[0].length;

		double[][][] ditheredBrokenImage = cloneBrokenImage(brokenImage);

		for (int y = 0; y<height; y++) {
			for (int x = 0; x<width; x++) {
				double[] originalColor = ditheredBrokenImage[x][y];
				double[] quantizedColor = findClosestColor(originalColor);

				double errorR = originalColor[0]-quantizedColor[0];
				double errorG = originalColor[1]-quantizedColor[1];
				double errorB = originalColor[2]-quantizedColor[2];

				ditheredBrokenImage[x][y] = quantizedColor;

				for (int i = 0; i<errorDistributions.size(); i++) {
					double[] errorDistribution = errorDistributions.get(i);
					try {
						ditheredBrokenImage[x+(int)errorDistribution[0]][y+(int)errorDistribution[1]][0]+=errorR*errorDistribution[2];
						ditheredBrokenImage[x+(int)errorDistribution[0]][y+(int)errorDistribution[1]][1]+=errorG*errorDistribution[2];
						ditheredBrokenImage[x+(int)errorDistribution[0]][y+(int)errorDistribution[1]][2]+=errorB*errorDistribution[2];
					} catch (Exception e) {}
				}
			}
		}

		return ditheredBrokenImage;
	}

	public static double[][][] invertBrokenImage(double[][][] brokenImage) {
		int width = brokenImage.length;
		int height = brokenImage[0].length;

		double[][][] invertedBrokenImage = cloneBrokenImage(brokenImage);

		for (int y = 0; y<height; y++) {
			for (int x = 0; x<width; x++) {
				invertedBrokenImage[x][y][0]=Math.abs(invertedBrokenImage[x][y][0]-255);
				invertedBrokenImage[x][y][1]=Math.abs(invertedBrokenImage[x][y][1]-255);
				invertedBrokenImage[x][y][2]=Math.abs(invertedBrokenImage[x][y][2]-255);
			}
		}

		return invertedBrokenImage;
	}

	public static BufferedImage invertImage(BufferedImage originalImage) {
		double[][][] brokenOriginalImage = imageToBrokenImage(originalImage);

		double[][][] brokenInvertedImage = invertBrokenImage(brokenOriginalImage);

		BufferedImage invertedImage = brokenImageToImage(brokenInvertedImage);

		return invertedImage;
	}

	public static BufferedImage ditherImage(BufferedImage originalImage, DitheringAlgorithm algorithmName) {
		// Creating image color table
		double[][][] brokenOriginalImage = imageToBrokenImage(originalImage);

		// Gets directions and portion of error to be distributed for a given algorithm
		ArrayList<double[]> errorDistributions = getDitheringAlgorithm(algorithmName);

		// Dithering Process
		double[][][] brokenDitheredImage = ditherBrokenImage(brokenOriginalImage, errorDistributions);

		// Makes new image based off image color table
		BufferedImage ditheredImage = brokenImageToImage(brokenDitheredImage);

		return ditheredImage;
	}

	public static double[] findClosestColor(double[] c) {
		int quantizedR = ((int)c[0]<128)?0:255;
		int quantizedG = ((int)c[1]<128)?0:255;
		int quantizedB = ((int)c[2]<128)?0:255;

		double[] quantizedColor = new double[]{quantizedR,quantizedG,quantizedB};
		return quantizedColor;
	}

}
