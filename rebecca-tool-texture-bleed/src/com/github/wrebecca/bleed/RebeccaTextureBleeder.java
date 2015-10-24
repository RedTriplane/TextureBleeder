package com.github.wrebecca.bleed;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashSet;

import com.jfixby.cmns.api.collections.JUtils;
import com.jfixby.cmns.api.collections.ZxZ_Functuion;
import com.jfixby.cmns.api.color.Color;
import com.jfixby.cmns.api.color.Colors;
import com.jfixby.cmns.api.filesystem.File;
import com.jfixby.cmns.api.filesystem.FileInputStream;
import com.jfixby.cmns.api.image.ColorFunction;
import com.jfixby.cmns.api.image.ImageProcessing;
import com.jfixby.cmns.api.io.Buffer;
import com.jfixby.cmns.api.io.IO;
import com.jfixby.cmns.api.math.Int2;
import com.jfixby.cmns.api.math.IntegerMath;
import com.jfixby.cmns.api.path.ChildrenList;
import com.jfixby.cmns.api.sys.Sys;
import com.jfixby.tools.bleed.api.TextureBleedComponent;
import com.jfixby.tools.bleed.api.TextureBleedResult;
import com.jfixby.tools.bleed.api.TextureBleedSpecs;

/**
 * 
 * @author WRebecca (https://github.com/WRebecca)
 * 
 *         This is free and unencumbered software released into the public
 *         domain.
 * 
 *         Anyone is free to copy, modify, publish, use, compile, sell, or
 *         distribute this software, either in source code form or as a compiled
 *         binary, for any purpose, commercial or non-commercial, and by any
 *         means.
 * 
 *         In jurisdictions that recognize copyright laws, the author or authors
 *         of this software dedicate any and all copyright interest in the
 *         software to the public domain. We make this dedication for the
 *         benefit of the public at large and to the detriment of our heirs and
 *         successors. We intend this dedication to be an overt act of
 *         relinquishment in perpetuity of all present and future rights to this
 *         software under copyright law.
 * 
 *         THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *         EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *         MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *         NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY
 *         CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 *         TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 *         SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 *         For more information, please refer to <http://unlicense.org/>
 *
 */

public class RebeccaTextureBleeder implements TextureBleedComponent {

	private int W;
	private int H;
	private int maxScans;

	@Override
	public TextureBleedSpecs newGemserkProcessorSpecs() {
		// TODO Auto-generated method stub
		return new TextureBleedSpecsImpl();
	}

	@Override
	public TextureBleedResult process(TextureBleedSpecs specs)
			throws IOException {
		TextureBleedResultImpl result = new TextureBleedResultImpl();

		File folder = specs.getInputFolder();
		ChildrenList pngFiles = folder.listChildren().filter(n -> {
			return n.getName().toLowerCase().endsWith(".png");
		});

		boolean debug_mode = specs.getDebugMode();
		if (debug_mode) {
			this.ALPHA = this.DEBUG_MODE_ALPHA;
		}

		maxScans = specs.getMaxScans();
		if (maxScans < 0) {
			maxScans = 4;
		}

		System.out.println("maxScans: " + maxScans);

		for (File png : pngFiles) {
			process(png, result);
		}

		return result;

	}

	private void process(File png, TextureBleedResultImpl result)
			throws IOException {
		FileResultImpl fileResult = new FileResultImpl();
		fileResult.setProcessedFile(png);
		long start_time = System.currentTimeMillis();
		result.addFileResult(fileResult);
		System.out.println("Processing: " + png);
		FileInputStream is = png.newInputStream();
		Buffer buffer = IO.readStreamToBuffer(is);
		is.close();
		BufferedImage image = ImageProcessing.readJavaImage(png);
		ColorFunction img = ImageProcessing.newColorFunction(buffer);
		W = img.getWidth();
		H = img.getHeight();

		// Set<Integer> colors = JUtils.newSet();
		HashSet<Int2> border = new HashSet<Int2>();

		ZxZ_Functuion<Color> function = JUtils.newZxZ_Function();

		for (int x = 0; x < W; x++) {
			for (int y = 0; y < H; y++) {
				Int2 pointer = IntegerMath.newInt2(x, y);
				Color color = img.getValue(x, y);
				if (color.alpha() > 0.999f) {
					function.setValueAt(x, y, color);
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
		// int maxDistance = colors.size();
		for (int x = 0; x < W; x++) {
			for (int y = 0; y < H; y++) {
				Color colorValue = null;
				colorValue = function.getValueAt(x, y);
				if (colorValue == null) {
					colorValue = Colors.PURPLE();
				}
				img.setValue(x, y, colorValue);
			}
		}
		image = img.toJavaImage();
		ImageProcessing.writeJavaFile(image, png, "png");
		long mills = System.currentTimeMillis() - start_time;
		fileResult.setDoneInMills(mills);
	}

	private boolean hasNonTransparentNeighbour(int x0, int y0, ColorFunction img) {
		// TODO Auto-generated method stub
		int D = 1;
		for (int k = -D; k <= D; k++) {
			for (int p = -D; p <= D; p++) {
				if (k == 0 && p == 0) {
					continue;
				}
				int x = (int) (k + x0);
				int y = (int) (p + y0);
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

				Color neighbour = img.getValue(x, y);
				if (neighbour.alpha() > 0.999f) {
					return true;
				}
			}
		}
		return false;
	}

	private HashSet<Int2> scan(ZxZ_Functuion<Color> function,
			Integer borderIndex, ColorFunction img, HashSet<Int2> border) {
		HashSet<Int2> newBorder = new HashSet<Int2>();
		for (Int2 pointer : border) {
			long x = pointer.getX();
			long y = pointer.getY();
			if (function.getValueAt(x, y) != null) {
				continue;
			}
			Color bestColor = addNUllNeighbours(x, y, newBorder, border,
					function);
			function.setValueAt(x, y, bestColor);
		}
		newBorder.removeAll(border);
		return newBorder;
	}

	Color addNUllNeighbours(long x0, long y0, HashSet<Int2> newBorder,
			HashSet<Int2> border, ZxZ_Functuion<Color> function) {
		// TODO Auto-generated method stub
		HashSet<Int2> coloredNeighbours = new HashSet<Int2>();
		int D = 1;
		for (int k = -D; k <= D; k++) {
			for (int p = -D; p <= D; p++) {
				if (k == 0 && p == 0) {
					continue;
				}
				int x = (int) (k + x0);
				int y = (int) (p + y0);
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

				Color neighbour = function.getValueAt(x, y);
				Int2 pointer = IntegerMath.newInt2(x, y);
				if (neighbour == null) {
					newBorder.add(pointer);
				} else if (!border.contains(pointer)) {
					coloredNeighbours.add(pointer);

				}
			}
		}
		float r = 0;
		float g = 0;
		float b = 0;
		for (Int2 neighbour : coloredNeighbours) {
			Color color = function.getValueAt(neighbour.getX(),
					neighbour.getY());
			r = r + color.red();
			g = g + color.green();
			b = b + color.blue();
		}
		r = r / coloredNeighbours.size();
		g = g / coloredNeighbours.size();
		b = b / coloredNeighbours.size();

		return Colors.newColor(ALPHA, r, g, b);

	}

	float NORMAL_MODE_ALPHA = 0f;
	float DEBUG_MODE_ALPHA = 1f;
	float ALPHA = NORMAL_MODE_ALPHA;

}
