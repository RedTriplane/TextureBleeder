
package com.github.wrebecca.bleed;

import java.io.IOException;
import java.util.HashSet;

import com.jfixby.scarabei.api.color.Color;
import com.jfixby.scarabei.api.color.Colors;
import com.jfixby.scarabei.api.desktop.ImageAWT;
import com.jfixby.scarabei.api.file.File;
import com.jfixby.scarabei.api.image.ColorMap;
import com.jfixby.scarabei.api.image.ColoredλImage;
import com.jfixby.scarabei.api.image.EditableColorMap;
import com.jfixby.scarabei.api.image.ImageProcessing;
import com.jfixby.scarabei.api.log.L;
import com.jfixby.scarabei.api.math.Int2;
import com.jfixby.scarabei.api.math.IntegerMath;
import com.jfixby.scarabei.api.sys.Sys;
import com.jfixby.tools.bleed.AbstractTextureBleeder;
import com.jfixby.tools.bleed.FileResult;

/** @author WRebecca (https://github.com/WRebecca)
 *
 *         This is free and unencumbered software released into the public domain.
 *
 *         Anyone is free to copy, modify, publish, use, compile, sell, or distribute this software, either in source code form or
 *         as a compiled binary, for any purpose, commercial or non-commercial, and by any means.
 *
 *         In jurisdictions that recognize copyright laws, the author or authors of this software dedicate any and all copyright
 *         interest in the software to the public domain. We make this dedication for the benefit of the public at large and to
 *         the detriment of our heirs and successors. We intend this dedication to be an overt act of relinquishment in perpetuity
 *         of all present and future rights to this software under copyright law.
 *
 *         THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 *         WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS BE
 *         LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *         OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 *         For more information, please refer to <http://unlicense.org/> */

public class RebeccaTextureBleeder extends AbstractTextureBleeder {

	@Override
	public void bleed (final File inputFile, final File outputFile, final FileResult fileResult) throws IOException {
		final EditableColorMap img = ImageAWT.readAWTColorMap(inputFile);

		this.W = img.getWidth();
		this.H = img.getHeight();

		// Set<Integer> colors = JUtils.newSet();
		HashSet<Int2> border = new HashSet<Int2>();

		final Color[][] function = new Color[this.W][this.H];

		for (int x = 0; x < this.W; x++) {
			for (int y = 0; y < this.H; y++) {
				final Int2 pointer = IntegerMath.newInt2(x, y);
				final Color color = img.valueAt(x, y);
				if (!isInvisible(color)) {
					function[x][y] = color.customize().setAlpha(1);
					// colors.add(0);
				} else if (this.hasNonTransparentNeighbour(x, y, img)) {
					border.add(pointer);

				}

			}
		}
		int k = 1;
		long timer_start = Sys.SystemTime().currentTimeMillis();
		long timer = 0;
		final long DELTA = 100;
		for (; border.size() > 0; k++) {

			if (k >= this.maxScans()) {
				break;
			}
			final long current = Sys.SystemTime().currentTimeMillis();
			final long delta = current - timer_start;

			timer_start = current;
			timer = timer + delta;
			while (timer > DELTA) {
				System.out.print('.');
				timer = timer - DELTA;
			}
			//
			border = this.scan(function, k, img, border);
		}
		L.d();
		// L.d("Scans performed: " + k);
		fileResult.setScansPerformed(k);

		final ColoredλImage lambda = (x, y) -> {
			Color colorValue = function[(int)x][(int)y];
			if (colorValue == null) {
				colorValue = Colors.PURPLE();
			}
			{
				final float original_alpha = img.valueAt(x, y).alpha();
				colorValue = colorValue.customize().setAlpha(original_alpha);
			}
			return colorValue;
		};

		final ColorMap result_image = ImageProcessing.newColorMap(lambda, this.W, this.H);
		L.d("writing", outputFile);
		ImageAWT.writeToFile(result_image, outputFile, "png");
	}

	private int W;
	private int H;

	Color addNUllNeighbours (final int x0, final int y0, final HashSet<Int2> newBorder, final HashSet<Int2> border,
		final Color[][] function, final Color original) {
		// TODO Auto-generated method stub
		final HashSet<Int2> coloredNeighbours = new HashSet<Int2>();
		final int D = 1;
		for (int k = -D; k <= D; k++) {
			for (int p = -D; p <= D; p++) {
				if (k == 0 && p == 0) {
					continue;
				}
				final int x = k + x0;
				final int y = p + y0;
				if (x < 0) {
					continue;
				}
				if (y < 0) {
					continue;
				}
				if (x >= this.W) {
					continue;
				}
				if (y >= this.H) {
					continue;
				}

				final Color neighbour = function[x][y];
				final Int2 pointer = IntegerMath.newInt2(x, y);
				if (neighbour == null) {
					newBorder.add(pointer);
				} else if (!border.contains(pointer)) {
					coloredNeighbours.add(pointer);

				}
			}
		}
		float r = 0;
		final float a = 1;
		float g = 0;
		float b = 0;
		for (final Int2 neighbour : coloredNeighbours) {
			final Color color = function[(int)neighbour.getX()][(int)neighbour.getY()];
			r = r + color.red();
			g = g + color.green();
			b = b + color.blue();
		}
		r = r / coloredNeighbours.size();
		g = g / coloredNeighbours.size();
		b = b / coloredNeighbours.size();
// a = original.alpha();

		return Colors.newColor(a, r, g, b);

	}

	private boolean hasNonTransparentNeighbour (final int x0, final int y0, final EditableColorMap img) {
		// TODO Auto-generated method stub
		final int D = 1;
		for (int k = -D; k <= D; k++) {
			for (int p = -D; p <= D; p++) {
				if (k == 0 && p == 0) {
					continue;
				}
				final int x = k + x0;
				final int y = p + y0;
				if (x < 0) {
					continue;
				}
				if (y < 0) {
					continue;
				}
				if (x >= this.W) {
					continue;
				}
				if (y >= this.H) {
					continue;
				}

				final Color neighbour = img.valueAt(x, y);
				if (!isInvisible(neighbour)) {
					return true;
				}
			}
		}
		return false;
	}

	private HashSet<Int2> scan (final Color[][] function, final Integer borderIndex, final EditableColorMap img,
		final HashSet<Int2> border) {
		final HashSet<Int2> newBorder = new HashSet<Int2>();
		for (final Int2 pointer : border) {
			final int x = (int)pointer.getX();
			final int y = (int)pointer.getY();
			if (function[x][y] != null) {
				continue;
			}
			final Color original = img.valueAt(x, y);
			final Color bestColor = this.addNUllNeighbours(x, y, newBorder, border, function, original);
			function[x][y] = bestColor;
		}
		newBorder.removeAll(border);
		return newBorder;
	}

}
