package com.jfixby.tools.bleed.gemserk;

import com.jfixby.cmns.api.filesystem.File;
import com.jfixby.tools.bleed.api.TextureBleedSpecs;

public class GemserkProcessorSpecsImpl implements TextureBleedSpecs {

	private File folder;

	@Override
	public File getInputFolder() {
		return folder;
	}

	@Override
	public void setInputFolder(File input_folder) {
		folder = input_folder;
	}

}
