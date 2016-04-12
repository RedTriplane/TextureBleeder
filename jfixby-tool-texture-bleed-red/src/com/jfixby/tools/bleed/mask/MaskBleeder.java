
package com.jfixby.tools.bleed.mask;

import java.awt.image.BufferedImage;

import com.jfixby.cmns.api.color.Colors;
import com.jfixby.cmns.api.color.CustomColor;

class MaskBleeder {

	static int[][] shifts = {//
		{-1, -1}, {+0, -1}, {+1, -1}, //
		{-1, +0}, /*     */ {+1, +0}, //
		{-1, +1}, {+0, +1}, {+1, +1}};//

	public static BufferedImage bleedImage (final BufferedImage image, final int maxScans) {

		final int width = image.getWidth();
		final int height = image.getHeight();

		final BufferedImage processedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		final int[] rgb_data = image.getRGB(0, 0, width, height, null, 0, width);
		final ColorMask mask = new ColorMask(rgb_data);

		int scans = 0;
		int lastWaiting = -1;
		while (mask.getWaitingSize() > 0) {
			if (scans >= maxScans) {
				break;
			}

			lastWaiting = mask.getWaitingSize();
			scan(rgb_data, mask, width, height);
			scans++;

			if (mask.getWaitingSize() == lastWaiting) {
				break;
			}
		}

		processedImage.setRGB(0, 0, width, height, rgb_data, 0, width);

		return processedImage;
	}

	static private void scan (final int[] rgb_data, final ColorMask mask, final int width, final int height) {

		final CustomColor color = Colors.newColor();
		final MaskScanner scanner = mask.iterator();
		while (scanner.hasNext()) {
			final int pixelIndex = scanner.next();

			final int x = pixelIndex % width;
			final int y = pixelIndex / width;

			float r = 0;
			float g = 0;
			float b = 0;
			int colors_number = 0;

			for (int i = 0; i < shifts.length; i++) {
				final int[] offset = shifts[i];
				final int column = x + offset[0];
				final int row = y + offset[1];

				if (column < 0 || column >= width || row < 0 || row >= height) {
					continue;
				}

				final int index = toIndex(width, column, row);
				final int intColor = rgb_data[index];
				color.setARGB(intColor);
				if (mask.getMask(index) == MaskState.original) {
					r += color.red();
					g += color.green();
					b += color.blue();
					colors_number++;
				}
			}

			if (colors_number != 0) {
				color.setRed(r / colors_number).setGreen(g / colors_number).setBlue(b / colors_number).setAlpha(0);
				rgb_data[pixelIndex] = color.toInteger();
				scanner.mark();
			}
		}

		scanner.reset();
	}

	final static private int toIndex (final int width, final int x, final int y) {
		return y * width + x;
	}
}
