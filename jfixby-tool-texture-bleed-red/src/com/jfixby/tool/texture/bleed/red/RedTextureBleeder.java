package com.jfixby.tool.texture.bleed.red;

import java.awt.image.BufferedImage;
import java.io.IOException;

import com.jfixby.cmns.api.color.Colors;
import com.jfixby.cmns.api.filesystem.File;
import com.jfixby.cmns.api.geometry.Geometry;
import com.jfixby.cmns.api.geometry.Rectangle;
import com.jfixby.cmns.api.image.EditableColorMap;
import com.jfixby.cmns.api.image.ImageProcessing;
import com.jfixby.cmns.api.image.LambdaColorMap;
import com.jfixby.cmns.api.image.LambdaColorMapSpecs;
import com.jfixby.cmns.api.image.LambdaColoredImage;
import com.jfixby.cmns.api.log.L;
import com.jfixby.cmns.api.path.ChildrenList;
import com.jfixby.cv.api.gwt.ImageGWT;
import com.jfixby.tools.bleed.api.TextureBleedComponent;
import com.jfixby.tools.bleed.api.TextureBleedResult;
import com.jfixby.tools.bleed.api.TextureBleedSpecs;

public class RedTextureBleeder implements TextureBleedComponent {

	private int W;
	private int H;
	private int maxScans;

	float NORMAL_MODE_ALPHA = 0f;
	float DEBUG_MODE_ALPHA = 1f;
	float ALPHA = NORMAL_MODE_ALPHA;

	@Override
	public TextureBleedSpecs newTextureBleedSpecs() {
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

		L.d("maxScans: " + maxScans);

		for (File png : pngFiles) {
			process(png, result);
		}

		return result;
	}

	private void process(File png, TextureBleedResultImpl result) throws IOException {
		FileResultImpl fileResult = new FileResultImpl();
		fileResult.setProcessedFile(png);
		long start_time = System.currentTimeMillis();
		result.addFileResult(fileResult);
		System.out.println("Processing: " + png);
		BufferedImage image = ImageGWT.readFromFile(png);
		EditableColorMap input = ImageGWT.newGWTColorMap(image);
		W = input.getWidth();
		H = input.getHeight();

		LambdaColoredImage base = xy -> Colors.BLUE();

		LambdaColorMapSpecs lambda_specs = ImageProcessing.newLambdaColorMapSpecs();
		lambda_specs.setAlphaChannel(null);
		Rectangle rectangle = Geometry.newRectangle(W, H);
		lambda_specs.setLambdaArea(rectangle);
		lambda_specs.setColorMapWidth(W);
		lambda_specs.setColorMapHeight(H);
		lambda_specs.setLambdaColoredImage(base);

		LambdaColorMap output = ImageProcessing.newLambdaColorMap(lambda_specs);

		image = ImageGWT.toGWTImage(output);
		ImageGWT.writeToFile(image, png, "png");
		long mills = System.currentTimeMillis() - start_time;
		fileResult.setDoneInMills(mills);
	}

}
