package com.jfixby.tools.bleed.lambda;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashSet;

import com.jfixby.cmns.api.collections.JUtils;
import com.jfixby.cmns.api.collections.List;
import com.jfixby.cmns.api.color.Color;
import com.jfixby.cmns.api.color.Colors;
import com.jfixby.cmns.api.color.CustomColor;
import com.jfixby.cmns.api.filesystem.File;
import com.jfixby.cmns.api.image.ColorMap;
import com.jfixby.cmns.api.image.EditableColorMap;
import com.jfixby.cmns.api.image.ImageProcessing;
import com.jfixby.cmns.api.image.LambdaColorMapSpecs;
import com.jfixby.cmns.api.lambda.Lambda;
import com.jfixby.cmns.api.lambda.λFunction;
import com.jfixby.cmns.api.log.L;
import com.jfixby.cmns.api.math.FixedInt2;
import com.jfixby.cmns.api.math.Int2;
import com.jfixby.cmns.api.math.IntegerMath;
import com.jfixby.cmns.api.path.ChildrenList;
import com.jfixby.cv.api.cv.CV;
import com.jfixby.cv.api.cv.λOperator;
import com.jfixby.cv.api.gwt.ImageGWT;
import com.jfixby.tools.bleed.api.TextureBleedComponent;
import com.jfixby.tools.bleed.api.TextureBleedResult;
import com.jfixby.tools.bleed.api.TextureBleedSpecs;

public class LambdaTextureBleeder implements TextureBleedComponent {

	private int W;
	private int H;
	private int maxScans;
	private λFunction<FixedInt2, Color> emty = xy -> Colors.PURPLE();
	private λFunction<Integer, λFunction<FixedInt2, Color>> STACK;

	@Override
	public TextureBleedSpecs newTextureBleedSpecs() {
		// TODO Auto-generated method stub
		return new TextureBleedSpecsImpl();
	}

	@Override
	public TextureBleedResult process(TextureBleedSpecs specs) throws IOException {
		TextureBleedResultImpl result = new TextureBleedResultImpl();

		File folder = specs.getInputFolder();
		ChildrenList pngFiles = folder.listChildren().filter(n -> {
			return n.getName().toLowerCase().endsWith(".png");
		});

		boolean debug_mode = specs.getDebugMode();
		if (debug_mode) {
			this.ALPHA = this.DEBUG_MODE_ALPHA;
		}

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

	public static final λOperator wrap = null;

	private void process(File png, TextureBleedResultImpl process_result) throws IOException {
		FileResultImpl fileResult = new FileResultImpl();
		fileResult.setProcessedFile(png);
		long start_time = System.currentTimeMillis();
		process_result.addFileResult(fileResult);
		System.out.println("Processing: " + png);
		BufferedImage image = ImageGWT.readFromFile(png);
		ColorMap img = ImageGWT.newGWTColorMap(image);
		W = img.getWidth();
		H = img.getHeight();

		λFunction<FixedInt2, Color> λimage = img.getLambdaImage();
		λFunction<Integer, λFunction<FixedInt2, Color>> layers_stack = buildStack(maxScans, λimage, W, H);
		STACK = layers_stack;

		λFunction<FixedInt2, Color> result = merge(layers_stack, W, H, maxScans);

		// λFunction<FixedInt2, Color> result = wrap(maxScans, λimage, W, H);
		int k = 0;
		System.out.println();
		// System.out.println("Scans performed: " + k);
		fileResult.setScansPerformed(k);
		// int maxDistance = colors.size();
		LambdaColorMapSpecs lambda_specs = ImageProcessing.newLambdaColorMapSpecs();
		lambda_specs.setColorMapWidth(W);
		lambda_specs.setColorMapHeight(H);
		lambda_specs.setLambdaColoredImage(result);
		img = ImageProcessing.newLambdaColorMap(lambda_specs);
		image = ImageGWT.toGWTImage(img);
		ImageGWT.writeToFile(image, png, "png");
		long mills = System.currentTimeMillis() - start_time;
		fileResult.setDoneInMills(mills);
	}

	private λFunction<FixedInt2, Color> merge(λFunction<Integer, λFunction<FixedInt2, Color>> layers_stack, int w, int h, int maxScans) {
		λFunction<FixedInt2, Color> result = xy -> {

			for (int i = 0; i < maxScans; i++) {
				λFunction<FixedInt2, Color> fn = layers_stack.val(i);
				Color color = fn.val(xy);
				if (color.alpha() == 1) {
					return color;
				}
			}
			return Colors.PURPLE();
		};
		return result;
	}

	private λFunction<Integer, λFunction<FixedInt2, Color>> buildStack(int maxScans, λFunction<FixedInt2, Color> λimage, int w, int h) {
		λFunction<Integer, λFunction<FixedInt2, Color>> stack = i -> {
			if (i == 0) {
				return λimage;
			}
			if (i >= maxScans) {
				return emty;
			}
			return fn(i, STACK, w, h);

		};
		stack = Lambda.cache(stack);
		return stack;
	}

	private λFunction<FixedInt2, Color> fn(int n, λFunction<Integer, λFunction<FixedInt2, Color>> STACK, int w, int h) {
		λFunction<FixedInt2, Color> fn = xy -> {
			λFunction<FixedInt2, Color> fn_1 = STACK.val(n - 1);
			Color color = fn_1.val(xy);
			if (color.alpha() == 1) {
				return color;
			}
			List<Color> collectedColors = JUtils.newList();
			collectNotNullNeighbours(xy.getX(), xy.getY(), fn_1, w, h, collectedColors);
			if (collectedColors.size() > 0) {
				CustomColor average = Colors.newColor();
				CV.averageColor(collectedColors, average);
				// average = Colors.BLACK().customize();
				return average;
			}
			return Colors.NO();
		};
		return Lambda.cache(fn, CV.newImageCache(w, h));
		// return fn;
	}

	private void collectNotNullNeighbours(long x0, long y0, λFunction<FixedInt2, Color> λimage, int W, int H, List<Color> collectedColors) {
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
				FixedInt2 neighbour = IntegerMath.newInt2(x, y);
				Color neighbour_color = λimage.val(neighbour);
				if (neighbour_color.alpha() == 1f) {
					collectedColors.add(neighbour_color);
				}
			}
		}
	}

	float NORMAL_MODE_ALPHA = 0f;
	float DEBUG_MODE_ALPHA = 1f;
	float ALPHA = NORMAL_MODE_ALPHA;

}
