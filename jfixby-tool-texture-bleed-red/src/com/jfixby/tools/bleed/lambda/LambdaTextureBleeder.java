
package com.jfixby.tools.bleed.lambda;

import java.io.IOException;

import com.jfixby.cmns.api.collections.Collections;
import com.jfixby.cmns.api.collections.List;
import com.jfixby.cmns.api.color.Color;
import com.jfixby.cmns.api.color.Colors;
import com.jfixby.cmns.api.color.CustomColor;
import com.jfixby.cmns.api.desktop.ImageAWT;
import com.jfixby.cmns.api.file.ChildrenList;
import com.jfixby.cmns.api.file.File;
import com.jfixby.cmns.api.image.ColorMap;
import com.jfixby.cmns.api.image.ColoredλImage;
import com.jfixby.cmns.api.image.ImageProcessing;
import com.jfixby.cmns.api.lambda.Lambda;
import com.jfixby.cmns.api.lambda.λFunction;
import com.jfixby.cv.api.CV;
import com.jfixby.tools.bleed.FileResult;
import com.jfixby.tools.bleed.TextureBleedResultImpl;
import com.jfixby.tools.bleed.TextureBleedSpecsImpl;
import com.jfixby.tools.bleed.api.TextureBleedComponent;
import com.jfixby.tools.bleed.api.TextureBleedResult;
import com.jfixby.tools.bleed.api.TextureBleedSpecs;

public class LambdaTextureBleeder implements TextureBleedComponent {

	public LambdaTextureBleeder () {
		super();
		throw new Error("Bad code, don't use it. Use RebeccaTextureBleeder instead.");
	}

	private int W;
	private int H;
	private int maxScans;
	private final ColoredλImage emty = (x, y) -> Colors.PURPLE();
	private λFunction<Integer, ColoredλImage> STACK;

	@Override
	public TextureBleedSpecs newTextureBleedSpecs () {
		// TODO Auto-generated method stub
		return new TextureBleedSpecsImpl();
	}

	@Override
	public TextureBleedResult process (final TextureBleedSpecs specs) throws IOException {

		final TextureBleedResultImpl result = new TextureBleedResultImpl();

		final File folder = specs.getInputFolder();
		final ChildrenList pngFiles = folder.listChildren().filterByExtension("png");

		final boolean debug_mode = specs.getDebugMode();
		if (debug_mode) {
			this.ALPHA = this.DEBUG_MODE_ALPHA;
		}

		this.maxScans = specs.getPaddingSize();
		if (this.maxScans < 0) {
			this.maxScans = TextureBleedSpecs.DEFAULT_PADDING;
		}

		System.out.println("maxScans: " + this.maxScans);

		for (final File png : pngFiles) {
			this.process(png, result);
		}

		return result;

	}

	private void process (final File png, final TextureBleedResultImpl process_result) throws IOException {

		final FileResult fileResult = new FileResult();
// fileResult.setProcessedFile(png);
		final long start_time = System.currentTimeMillis();
		process_result.addFileResult(fileResult);
		System.out.println("Processing: " + png);
		final ColorMap img = ImageAWT.readAWTColorMap(png);
		this.W = img.getWidth();
		this.H = img.getHeight();

		// λImage result = wrap(maxScans, λimage, W, H);
		final int k = 0;
		System.out.println();
		// System.out.println("Scans performed: " + k);
		fileResult.setScansPerformed(k);

		final ColoredλImage lambda = (x, y) -> {
			final Color original_color = img.valueAt(x, y);
			if (this.isVisible(original_color)) {
				return original_color;
			}
			if (this.isInvisible(original_color)) {
				return Colors.PURPLE();
			}
			if (this.isHalfTransparent(original_color)) {
				return original_color.customize().setAlpha(0f);
			}
			return Colors.FUCHSIA();
		};
		final ColorMap result_image = ImageProcessing.newColorMap(lambda, this.W, this.H);
		ImageAWT.writeToFile(result_image, png, "png");

		final long mills = System.currentTimeMillis() - start_time;
		fileResult.setDoneInMills(mills);
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

	private ColoredλImage merge (final λFunction<Integer, ColoredλImage> layers_stack, final int w, final int h,
		final int maxScans) {
		final ColoredλImage result = (x, y) -> {

			for (int i = 0; i < maxScans; i++) {
				final ColoredλImage fn = layers_stack.val(i);
				final Color color = fn.valueAt(x, y);
				if (color.alpha() == 1) {
					return color;
				}
			}
			return Colors.PURPLE();
		};
		return result;
	}

	private λFunction<Integer, ColoredλImage> buildStack (final int maxScans, final ColoredλImage λimage, final int w,
		final int h) {
		λFunction<Integer, ColoredλImage> stack = i -> {
			if (i == 0) {
				return λimage;
			}
			if (i >= maxScans) {
				return this.emty;
			}
			return this.fn(i, this.STACK, w, h);

		};
		stack = Lambda.cache(stack);
		return stack;
	}

	private ColoredλImage fn (final int n, final λFunction<Integer, ColoredλImage> STACK, final int w, final int h) {
		final ColoredλImage fn = (x, y) -> {
			final ColoredλImage fn_1 = STACK.val(n - 1);
			final Color color = fn_1.valueAt(x, y);
			if (color.alpha() == 1) {
				return color;
			}
			final List<Color> collectedColors = Collections.newList();
			this.collectNotNullNeighbours((long)x, (long)y, fn_1, w, h, collectedColors);
			if (collectedColors.size() > 0) {
				final CustomColor average = Colors.newColor();
				CV.averageColor(collectedColors, average);
				// average = Colors.BLACK().customize();
				return average;
			}
			return Colors.NO();
		};
		// return CV.cache(fn, ImageProcessing.newImageCache(w, h));
		return fn;
		// return fn;
	}

	private void collectNotNullNeighbours (final long x0, final long y0, final ColoredλImage λimage, final int W, final int H,
		final List<Color> collectedColors) {
		final int D = 1;
		for (int k = -D; k <= D; k++) {
			for (int p = -D; p <= D; p++) {
				if (k == 0 && p == 0) {
					continue;
				}
				final int x = (int)(k + x0);
				final int y = (int)(p + y0);
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

				final Color neighbour_color = λimage.valueAt(x, y);
				if (neighbour_color.alpha() == 1f) {
					collectedColors.add(neighbour_color);
				}
			}
		}
	}

	float NORMAL_MODE_ALPHA = 0f;
	float DEBUG_MODE_ALPHA = 1f;
	float ALPHA = this.NORMAL_MODE_ALPHA;

}
