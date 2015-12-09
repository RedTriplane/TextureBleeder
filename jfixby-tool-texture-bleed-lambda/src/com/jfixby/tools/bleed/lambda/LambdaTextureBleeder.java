package com.jfixby.tools.bleed.lambda;

import java.awt.image.BufferedImage;
import java.io.IOException;

import com.jfixby.cmns.api.collections.JUtils;
import com.jfixby.cmns.api.collections.List;
import com.jfixby.cmns.api.color.Color;
import com.jfixby.cmns.api.color.Colors;
import com.jfixby.cmns.api.color.CustomColor;
import com.jfixby.cmns.api.file.File;
import com.jfixby.cmns.api.image.ColorMap;
import com.jfixby.cmns.api.image.ImageProcessing;
import com.jfixby.cmns.api.image.LambdaColorMapSpecs;
import com.jfixby.cmns.api.lambda.Lambda;
import com.jfixby.cmns.api.lambda.λFunction;
import com.jfixby.cmns.api.lambda.λImage;
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
	private λImage emty = (x, y) -> Colors.PURPLE();
	private λFunction<Integer, λImage> STACK;

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

		λImage λimage = img.getLambdaImage();
		λFunction<Integer, λImage> layers_stack = buildStack(maxScans, λimage, W, H);
		STACK = layers_stack;

		λImage result = merge(layers_stack, W, H, maxScans);

		// λImage result = wrap(maxScans, λimage, W, H);
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

	private λImage merge(λFunction<Integer, λImage> layers_stack, int w, int h, int maxScans) {
		λImage result = (x, y) -> {

			for (int i = 0; i < maxScans; i++) {
				λImage fn = layers_stack.val(i);
				Color color = fn.val(x, y);
				if (color.alpha() == 1) {
					return color;
				}
			}
			return Colors.PURPLE();
		};
		return result;
	}

	private λFunction<Integer, λImage> buildStack(int maxScans, λImage λimage, int w, int h) {
		λFunction<Integer, λImage> stack = i -> {
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

	private λImage fn(int n, λFunction<Integer, λImage> STACK, int w, int h) {
		λImage fn = (x, y) -> {
			λImage fn_1 = STACK.val(n - 1);
			Color color = fn_1.val(x, y);
			if (color.alpha() == 1) {
				return color;
			}
			List<Color> collectedColors = JUtils.newList();
			collectNotNullNeighbours((long) x, (long) y, fn_1, w, h, collectedColors);
			if (collectedColors.size() > 0) {
				CustomColor average = Colors.newColor();
				CV.averageColor(collectedColors, average);
				// average = Colors.BLACK().customize();
				return average;
			}
			return Colors.NO();
		};
		return CV.cache(fn, CV.newImageCache(w, h));
		// return fn;
	}

	private void collectNotNullNeighbours(long x0, long y0, λImage λimage, int W, int H, List<Color> collectedColors) {
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

				Color neighbour_color = λimage.val(x, y);
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
