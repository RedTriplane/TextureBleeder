package com.gemserk.tools.bleed;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Magic {

	// /**
	// * @param args
	// * @throws IOException
	// */
	// public static void main(String[] args) throws IOException {
	// // TODO Auto-generated method stub
	// String path = "output";
	// processDir(path);
	//
	// // process(path);
	// }

	public static void processDir(String input_dir, String output_dir)
			throws IOException {
		// TODO Auto-generated method stub
		System.out.println("Gemserk processing dir: " + input_dir);
		File dir = new File(input_dir);
		FilenameFilter filter = new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				// TODO Auto-generated method stub
				if (name.endsWith(".png")) {
					System.out.println("accepting file: " + dir + "@" + name);
					return true;
				} else {
					System.out.println("skiping file: " + dir + "@" + name);
					return false;
				}
			}
		};

		String[] l = dir.list(filter);
		for (int i = 0; i < l.length; i++) {
			// System.out.println(l[i]);
			File input_file = new File(input_dir
					+ System.getProperty("file.separator") + l[i]);
			File output_file = new File(output_dir
					+ System.getProperty("file.separator") + l[i]);
			output_file.mkdirs();
			output_file.createNewFile();
			process(input_file, output_file);
		}
	}

	public static void process(File inputFile, File output_file)
			throws IOException {
		
		BufferedImage image = ImageIO.read(inputFile);

		int width = image.getWidth();
		int height = image.getHeight();

		// setBounds(100, 100, width * 3, height + 50);

		ColorBleedingEffect colorBleedingEffect = new ColorBleedingEffect();
		BufferedImage processedImage = colorBleedingEffect.processImage(image);

		BufferedImage processedImageOpaque = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);

		int[] rgba = processedImage.getRGB(0, 0, width, height, null, 0, width);
		processedImageOpaque.setRGB(0, 0, width, height, rgba, 0, width);
		String output = output_file.getAbsolutePath();
		System.out.println("writing: " + output);
		ImageIO.write(processedImage, "png", new File(output));

	}

}
