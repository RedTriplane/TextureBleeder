
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
import com.jfixby.cmns.api.log.L;
import com.jfixby.cmns.api.math.Int2;
import com.jfixby.cmns.api.math.IntegerMath;
import com.jfixby.cmns.api.sys.Sys;
import com.jfixby.tools.bleed.FileResult;
import com.jfixby.tools.bleed.TextureBleedResultImpl;
import com.jfixby.tools.bleed.TextureBleedSpecsImpl;
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
	private File output;

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

	private void debugImage (final File outputFile) throws IOException {

		final ColorMap image = ImageAWT.readAWTColorMap(outputFile);
		final ColoredλImage debug_lambda = (x, y) -> image.valueAt(x, y).customize().setAlpha(1);
		final ColorMap debug_image = ImageProcessing.newColorMap(debug_lambda, this.W, this.H);

		final File debugFile = outputFile.parent().child(outputFile.nameWithoutExtension() + "-debug.png");
		L.d("  debug", debugFile);
		ImageAWT.writeToFile(debug_image, debugFile, "png");
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
				if (!this.isInvisible(neighbour)) {
					return true;
				}
			}
		}
		return false;
	}

	final private boolean isHalfTransparent (final Color color) {
		return (color.alpha() > 1f / 128f) && (color.alpha() < 1f - 1f / 128f);
	}

	final private boolean isInvisible (final Color color) {
		return color.alpha() <= 1f / 128f;
	}

	final private boolean isVisible (final Color color) {
		return color.alpha() >= 1f - 1f / 128f;
	}

	@Override
	public TextureBleedSpecs newTextureBleedSpecs () {
		// TODO Auto-generated method stub
		return new TextureBleedSpecsImpl();
	}

	private void process (final File inputFile, final TextureBleedResultImpl result) throws IOException {

		final File outputFile = this.output.child(inputFile.getName());

		final FileResult fileResult = new FileResult();
		fileResult.setInputFile(inputFile);
		fileResult.setOutputFile(outputFile);
		final long start_time = System.currentTimeMillis();
		result.addFileResult(fileResult);
		System.out.println("Processing: " + inputFile);
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
				if (!this.isInvisible(color)) {
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

			if (k >= this.maxScans) {
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
		System.out.println();
		// System.out.println("Scans performed: " + k);
		fileResult.setScansPerformed(k);

		final ColoredλImage lambda = (x, y) -> {
			Color colorValue = function[(int)x][(int)y];
			if (colorValue == null) {
				colorValue = Colors.PURPLE();
			}
			{
				final float original_alpha = img.valueAt(x, y).alpha();
// if (original_alpha < 1f / 128f) {
// original_alpha = 1f / 128f;
// }
// original_alpha = 1;
				colorValue = colorValue.customize().setAlpha(original_alpha);
			}
			return colorValue;
		};
		final ColorMap result_image = ImageProcessing.newColorMap(lambda, this.W, this.H);
		L.d("writing", outputFile);
		ImageAWT.writeToFile(result_image, outputFile, "png");

		if (this.debug_mode) {
			this.debugImage(outputFile);
		}
		final long mills = System.currentTimeMillis() - start_time;
		fileResult.setDoneInMills(mills);
	}

	@Override
	public TextureBleedResult process (final TextureBleedSpecs specs) throws IOException {
		final TextureBleedResultImpl result = new TextureBleedResultImpl();

		final File input = specs.getInputFolder();
		final ChildrenList pngFiles = input.listChildren().filterFiles(n -> {
			return n.getName().toLowerCase().endsWith(".png");
		});

		this.debug_mode = specs.getDebugMode();

		this.maxScans = specs.getPaddingSize();
		if (this.maxScans < 0) {
			this.maxScans = TextureBleedSpecs.DEFAULT_PADDING;
		}

		this.output = specs.getOutputFolder();
		if (this.output == null) {
			this.output = input;
		}

		System.out.println("maxScans: " + this.maxScans);
		this.output.makeFolder();
		for (final File png : pngFiles) {
			this.process(png, result);
		}

		return result;

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

// float NORMAL_MODE_ALPHA = 0.0f;
// float DEBUG_MODE_ALPHA = 1f;
// float ALPHA = NORMAL_MODE_ALPHA;

}
