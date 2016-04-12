
package com.jfixby.tools.bleed.mask;

import java.awt.image.BufferedImage;
import java.io.IOException;

import com.jfixby.cmns.api.desktop.ImageAWT;
import com.jfixby.cmns.api.file.File;
import com.jfixby.cmns.api.log.L;
import com.jfixby.tools.bleed.AbstractTextureBleeder;
import com.jfixby.tools.bleed.FileResult;

public class MaskTextureBleeder extends AbstractTextureBleeder {

	@Override
	public void bleed (final File inputFile, final File outputFile, final FileResult fileResult) throws IOException {
		final BufferedImage img = ImageAWT.readFromFile(inputFile);
		final BufferedImage result_image = MaskBleeder.bleedImage(img, this.maxScans());
		L.d("writing", outputFile);
		ImageAWT.writeToFile(result_image, outputFile, "png");
	}

}
