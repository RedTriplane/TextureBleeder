
package com.jfixby.tools.bleed;

import java.io.IOException;

import com.jfixby.cmns.api.color.Color;
import com.jfixby.cmns.api.desktop.ImageAWT;
import com.jfixby.cmns.api.file.ChildrenList;
import com.jfixby.cmns.api.file.File;
import com.jfixby.cmns.api.image.ColorMap;
import com.jfixby.cmns.api.image.ColoredλImage;
import com.jfixby.cmns.api.image.ImageProcessing;
import com.jfixby.cmns.api.log.L;
import com.jfixby.tools.bleed.api.TextureBleedComponent;
import com.jfixby.tools.bleed.api.TextureBleedResult;
import com.jfixby.tools.bleed.api.TextureBleedSpecs;

public abstract class AbstractTextureBleeder implements TextureBleedComponent {
	private int maxScans;
	private boolean debug_mode;
	private File outputFolder;

	public int maxScans () {
		return this.maxScans;
	}

	@Override
	public TextureBleedResult process (final TextureBleedSpecs specs) throws IOException {
		final TextureBleedResultImpl result = new TextureBleedResultImpl();

		final File input = specs.getInputFolder();
		final ChildrenList pngFiles = input.listDirectChildren().filterFiles(n -> {
			return n.getName().toLowerCase().endsWith(".png");
		});

		this.debug_mode = specs.getDebugMode();

		this.maxScans = specs.getPaddingSize();
		if (this.maxScans < 0) {
			this.maxScans = TextureBleedSpecs.DEFAULT_PADDING;
		}

		this.outputFolder = specs.getOutputFolder();
		if (this.outputFolder == null) {
			this.outputFolder = input;
		}

		L.d("maxScans: " + this.maxScans);
		this.outputFolder.makeFolder();
		for (final File png : pngFiles) {
			this.processFile(png, result);
		}

		return result;

	}

	private void processFile (final File inputFile, final TextureBleedResultImpl result) throws IOException {
		final File outputFile = this.outputFolder.child(inputFile.getName());

		final FileResult fileResult = new FileResult();
		fileResult.setInputFile(inputFile);
		fileResult.setOutputFile(outputFile);
		final long start_time = System.currentTimeMillis();
		result.addFileResult(fileResult);
		L.d("Processing: " + inputFile);

		this.bleed(inputFile, outputFile, fileResult);

		if (this.debug_mode) {
			this.debugImage(outputFile);
		}
		final long mills = System.currentTimeMillis() - start_time;
		fileResult.setDoneInMills(mills);

	}

	private void debugImage (final File outputFile) throws IOException {
		final ColorMap image = ImageAWT.readAWTColorMap(outputFile);
		final ColoredλImage debug_lambda = (x, y) -> image.valueAt(x, y).customize().setAlpha(1);
		final ColorMap debug_image = ImageProcessing.newColorMap(debug_lambda, image.getWidth(), image.getHeight());
		final File debugFile = outputFile.parent().child(outputFile.nameWithoutExtension() + "-debug.png");
		L.d("  debug", debugFile);
		ImageAWT.writeToFile(debug_image, debugFile, "png");
	}

	public abstract void bleed (final File inputFile, File outputFile, FileResult fileResult) throws IOException;

	@Override
	public TextureBleedSpecs newTextureBleedSpecs () {
		return new TextureBleedSpecsImpl();
	}

	final public static boolean isHalfTransparent (final Color color) {
		return (color.alpha() > 1f / 128f) && (color.alpha() < 1f - 1f / 128f);
	}

	final public static boolean isInvisible (final Color color) {
		return color.alpha() <= 1f / 128f;
	}

	final public static boolean isVisible (final Color color) {
		return color.alpha() >= 1f - 1f / 128f;
	}
}
