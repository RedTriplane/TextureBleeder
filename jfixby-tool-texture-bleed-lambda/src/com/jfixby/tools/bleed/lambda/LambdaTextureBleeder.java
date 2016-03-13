package com.jfixby.tools.bleed.lambda;

import java.awt.image.BufferedImage;
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
import com.jfixby.cmns.api.image.ColorMapSpecs;
import com.jfixby.cmns.api.image.ColoredλImage;
import com.jfixby.cmns.api.image.ImageProcessing;
import com.jfixby.cmns.api.lambda.Lambda;
import com.jfixby.cmns.api.lambda.λFunction;
import com.jfixby.cv.api.CV;
import com.jfixby.cv.api.λOperator;
import com.jfixby.tools.bleed.api.TextureBleedComponent;
import com.jfixby.tools.bleed.api.TextureBleedResult;
import com.jfixby.tools.bleed.api.TextureBleedSpecs;

public class LambdaTextureBleeder implements TextureBleedComponent {

    private int W;
    private int H;
    private int maxScans;
    private ColoredλImage emty = (x, y) -> Colors.PURPLE();
    private λFunction<Integer, ColoredλImage> STACK;

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
	BufferedImage image = ImageAWT.readFromFile(png);
	ColorMap img = ImageAWT.newAWTColorMap(image);
	W = img.getWidth();
	H = img.getHeight();

	ColoredλImage λimage = img.getLambdaImage();
	λFunction<Integer, ColoredλImage> layers_stack = buildStack(maxScans, λimage, W, H);
	STACK = layers_stack;

	ColoredλImage result = merge(layers_stack, W, H, maxScans);

	// λImage result = wrap(maxScans, λimage, W, H);
	int k = 0;
	System.out.println();
	// System.out.println("Scans performed: " + k);
	fileResult.setScansPerformed(k);
	// int maxDistance = colors.size();
	ColorMapSpecs lambda_specs = ImageProcessing.newColorMapSpecs();
	lambda_specs.setColorMapWidth(W);
	lambda_specs.setColorMapHeight(H);
	lambda_specs.setLambdaColoredImage(result);
	img = ImageProcessing.newColorMap(lambda_specs);
	image = ImageAWT.toAWTImage(img);
	ImageAWT.writeToFile(image, png, "png");
	long mills = System.currentTimeMillis() - start_time;
	fileResult.setDoneInMills(mills);
    }

    private ColoredλImage merge(λFunction<Integer, ColoredλImage> layers_stack, int w, int h, int maxScans) {
	ColoredλImage result = (x, y) -> {

	    for (int i = 0; i < maxScans; i++) {
		ColoredλImage fn = layers_stack.val(i);
		Color color = fn.valueAt(x, y);
		if (color.alpha() == 1) {
		    return color;
		}
	    }
	    return Colors.PURPLE();
	};
	return result;
    }

    private λFunction<Integer, ColoredλImage> buildStack(int maxScans, ColoredλImage λimage, int w, int h) {
	λFunction<Integer, ColoredλImage> stack = i -> {
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

    private ColoredλImage fn(int n, λFunction<Integer, ColoredλImage> STACK, int w, int h) {
	ColoredλImage fn = (x, y) -> {
	    ColoredλImage fn_1 = STACK.val(n - 1);
	    Color color = fn_1.valueAt(x, y);
	    if (color.alpha() == 1) {
		return color;
	    }
	    List<Color> collectedColors = Collections.newList();
	    collectNotNullNeighbours((long) x, (long) y, fn_1, w, h, collectedColors);
	    if (collectedColors.size() > 0) {
		CustomColor average = Colors.newColor();
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

    private void collectNotNullNeighbours(long x0, long y0, ColoredλImage λimage, int W, int H,
	    List<Color> collectedColors) {
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

		Color neighbour_color = λimage.valueAt(x, y);
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
