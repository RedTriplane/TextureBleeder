package com.jfixby.tool.texture.bleed.red;

import java.awt.image.BufferedImage;
import java.io.IOException;

import com.jfixby.cmns.api.collections.JUtils;
import com.jfixby.cmns.api.collections.List;
import com.jfixby.cmns.api.color.Color;
import com.jfixby.cmns.api.color.Colors;
import com.jfixby.cmns.api.filesystem.File;
import com.jfixby.cmns.api.floatn.FixedFloat2;
import com.jfixby.cmns.api.floatn.Float2;
import com.jfixby.cmns.api.geometry.Geometry;
import com.jfixby.cmns.api.geometry.Rectangle;
import com.jfixby.cmns.api.image.EditableColorMap;
import com.jfixby.cmns.api.image.ImageProcessing;
import com.jfixby.cmns.api.image.LambdaColorMap;
import com.jfixby.cmns.api.image.LambdaColorMapSpecs;
import com.jfixby.cmns.api.image.LambdaImage;
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

		LambdaImage colors = input.getLambdaColoredImage();

		Rectangle rectangle = Geometry.newRectangle(W, H);
		LambdaImage base = wrap(colors, 1, rectangle);

		LambdaColorMapSpecs lambda_specs = ImageProcessing.newLambdaColorMapSpecs();

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

	private LambdaImage wrap(LambdaImage colors, int n, Rectangle area) {
		if (n == 0) {
			return colors;
		}

		LambdaImage wrapped = wrap(colors, n - 1, area);

		Color random = Colors.newRandomColor(1);
		LambdaImage base = xy -> {
			// L.d("xy", xy);
			final Color color = wrapped.value(xy);
			if (color.alpha() == 1) {
				return color;
			} else {
				List<FixedFloat2> colored_neighbours = JUtils.newList();
				collect_colored_neighbours(xy, colored_neighbours, wrapped, area);
				if (colored_neighbours.size() == 0) {
					return wrapped.value(xy);
				} else {
					return random;
				}
			}

		};
		return base;
	}

	static final private void collect_colored_neighbours(FixedFloat2 xy, List<FixedFloat2> colored_neighbours, LambdaImage colors, Rectangle area) {
		double x0 = xy.getX();
		double y0 = xy.getY();
		int D = 1;
		for (int k = -D; k <= D; k++) {
			for (int p = -D; p <= D; p++) {
				if (k == 0 && p == 0) {
					continue;
				}
				final double x = (k + x0);
				final double y = (p + y0);

				if (!area.containsPoint(x, y)) {
					continue;
				}
				Float2 neighbour = Geometry.newFloat2(x, y);
				if (colors.value(neighbour).alpha() == 1) {
					colored_neighbours.add(neighbour);
					return;
				}
			}
		}

	}

	private boolean hasNonTransparentNeighbour(int x0, int y0, EditableColorMap img) {
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

}
