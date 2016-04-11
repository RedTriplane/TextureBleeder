
package com.github.wrebecca.bleed;

import java.io.IOException;
import java.util.HashSet;

import com.jfixby.cmns.api.color.Color;
import com.jfixby.cmns.api.color.Colors;
import com.jfixby.cmns.api.desktop.ImageAWT;
import com.jfixby.cmns.api.file.ChildrenList;
import com.jfixby.cmns.api.file.File;
import com.jfixby.cmns.api.image.ColorMap;
import com.jfixby.cmns.api.image.ColoredλImage;
import com.jfixby.cmns.api.image.EditableColorMap;
import com.jfixby.cmns.api.image.ImageProcessing;
import com.jfixby.cmns.api.math.Int2;
import com.jfixby.cmns.api.math.IntegerMath;
import com.jfixby.cmns.api.sys.Sys;
import com.jfixby.tools.bleed.api.TextureBleedComponent;
import com.jfixby.tools.bleed.api.TextureBleedResult;
import com.jfixby.tools.bleed.api.TextureBleedSpecs;

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

public class RebeccaTextureBleeder implements TextureBleedComponent {

	private int W;
	private int H;
	private int maxScans;
	private boolean debug_mode;

	@Override
	public TextureBleedSpecs newTextureBleedSpecs () {
		// TODO Auto-generated method stub
		return new TextureBleedSpecsImpl();
	}

	@Override
	public TextureBleedResult process (TextureBleedSpecs specs) throws IOException {
		TextureBleedResultImpl result = new TextureBleedResultImpl();

		File folder = specs.getInputFolder();
		ChildrenList pngFiles = folder.listChildren().filterFiles(n -> {
			return n.getName().toLowerCase().endsWith(".png");
		});

		debug_mode = specs.getDebugMode();

		maxScans = specs.getPaddingSize();
		if (maxScans < 0) {
			maxScans = TextureBleedSpecs.DEFAULT_PADDING;
		}

		System.out.println("maxScans: " + maxScans);

		for (File png : pngFiles) {
			process(png, result);
		}

		return result;

	}

	private void process (File png, TextureBleedResultImpl result) throws IOException {
		FileResultImpl fileResult = new FileResultImpl();
		fileResult.setProcessedFile(png);
		long start_time = System.currentTimeMillis();
		result.addFileResult(fileResult);
		System.out.println("Processing: " + png);
		EditableColorMap img = ImageAWT.readAWTColorMap(png);

		W = img.getWidth();
		H = img.getHeight();

		// Set<Integer> colors = JUtils.newSet();
		HashSet<Int2> border = new HashSet<Int2>();

		Color[][] function = new Color[W][H];

		for (int x = 0; x < W; x++) {
			for (int y = 0; y < H; y++) {
				Int2 pointer = IntegerMath.newInt2(x, y);
				Color color = img.valueAt(x, y);
				if (!this.isInvisible(color)) {
					function[x][y] = color;
					if (debug_mode) {
						function[x][y] = color.customize().setAlpha(1);
					}
					// colors.add(0);
				} else if (hasNonTransparentNeighbour(x, y, img)) {
					border.add(pointer);

				}

			}
		}
		int k = 1;
		long timer_start = Sys.SystemTime().currentTimeMillis();
		long timer = 0;
		long DELTA = 100;
		for (; border.size() > 0; k++) {

			if (k >= maxScans) {
				break;
			}
			long current = Sys.SystemTime().currentTimeMillis();
			long delta = current - timer_start;

			timer_start = current;
			timer = timer + delta;
			while (timer > DELTA) {
				System.out.print('.');
				timer = timer - DELTA;
			}
			//
			border = scan(function, k, img, border);
		}
		System.out.println();
		// System.out.println("Scans performed: " + k);
		fileResult.setScansPerformed(k);

		ColoredλImage lambda = (x, y) -> {
			Color colorValue = null;
			colorValue = function[(int)x][(int)y];
			if (colorValue == null) {
				if (debug_mode) {
					colorValue = Colors.PURPLE();
				} else {
					Color original = img.valueAt(x, y);
					colorValue = original;
				}
			}
			return colorValue;
		};
		ColorMap result_image = ImageProcessing.newColorMap(lambda, W, H);

		ImageAWT.writeToFile(result_image, png, "png");
		long mills = System.currentTimeMillis() - start_time;
		fileResult.setDoneInMills(mills);
	}

	private boolean hasNonTransparentNeighbour (int x0, int y0, EditableColorMap img) {
		// TODO Auto-generated method stub
		int D = 1;
		for (int k = -D; k <= D; k++) {
			for (int p = -D; p <= D; p++) {
				if (k == 0 && p == 0) {
					continue;
				}
				int x = (int)(k + x0);
				int y = (int)(p + y0);
				if (x < 0) {
					continue;
				}
				if (y < 0) {
					continue;
				}
				if (x >= W) {
					continue;
				}
				if (y >= H) {
					continue;
				}

				Color neighbour = img.valueAt(x, y);
				if (!isInvisible(neighbour)) {
					return true;
				}
			}
		}
		return false;
	}

	final private boolean isInvisible (final Color color) {
		return color.alpha() <= 1f / 128f;
	}

	final private boolean isVisible (final Color color) {
		return color.alpha() >= 1f - 1f / 128f;
	}

	final private boolean isHalfTransparent (final Color color) {
		return (color.alpha() > 1f / 128f) && (color.alpha() < 1f - 1f / 128f);
	}

	private HashSet<Int2> scan (Color[][] function, Integer borderIndex, EditableColorMap img, HashSet<Int2> border) {
		HashSet<Int2> newBorder = new HashSet<Int2>();
		for (Int2 pointer : border) {
			int x = (int)pointer.getX();
			int y = (int)pointer.getY();
			if (function[x][y] != null) {
				continue;
			}
			Color original = img.valueAt(x, y);
			Color bestColor = addNUllNeighbours(x, y, newBorder, border, function, original);
			function[x][y] = bestColor;
		}
		newBorder.removeAll(border);
		return newBorder;
	}

	Color addNUllNeighbours (int x0, int y0, HashSet<Int2> newBorder, HashSet<Int2> border, Color[][] function, Color original) {
		// TODO Auto-generated method stub
		HashSet<Int2> coloredNeighbours = new HashSet<Int2>();
		int D = 1;
		for (int k = -D; k <= D; k++) {
			for (int p = -D; p <= D; p++) {
				if (k == 0 && p == 0) {
					continue;
				}
				int x = (int)(k + x0);
				int y = (int)(p + y0);
				if (x < 0) {
					continue;
				}
				if (y < 0) {
					continue;
				}
				if (x >= W) {
					continue;
				}
				if (y >= H) {
					continue;
				}

				Color neighbour = function[x][y];
				Int2 pointer = IntegerMath.newInt2(x, y);
				if (neighbour == null) {
					newBorder.add(pointer);
				} else if (!border.contains(pointer)) {
					coloredNeighbours.add(pointer);

				}
			}
		}
		float r = 0;
		float a = 0;
		float g = 0;
		float b = 0;
		for (Int2 neighbour : coloredNeighbours) {
			Color color = function[(int)neighbour.getX()][(int)neighbour.getY()];
			r = r + color.red();
			g = g + color.green();
			b = b + color.blue();
		}
		r = r / coloredNeighbours.size();
		g = g / coloredNeighbours.size();
		b = b / coloredNeighbours.size();
		if (debug_mode) {
			a = 1;
		} else {
			a = original.alpha();
		}

		return Colors.newColor(a, r, g, b);

	}

// float NORMAL_MODE_ALPHA = 0.0f;
// float DEBUG_MODE_ALPHA = 1f;
// float ALPHA = NORMAL_MODE_ALPHA;

}
