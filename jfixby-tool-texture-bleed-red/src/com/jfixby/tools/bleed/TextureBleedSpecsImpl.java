
package com.jfixby.tools.bleed;

import com.jfixby.cmns.api.file.File;
import com.jfixby.tools.bleed.api.TextureBleedSpecs;

public class TextureBleedSpecsImpl implements TextureBleedSpecs {
	File folder;
	private boolean debugMode;
	private int maxScans = -1;
	private File output;

	@Override
	public boolean getDebugMode () {
		// TODO Auto-generated method stub
		return this.debugMode;
	}

	@Override
	public File getInputFolder () {
		// TODO Auto-generated method stub
		return this.folder;
	}

	@Override
	public File getOutputFolder () {
		return this.output;
	}

	@Override
	public int getPaddingSize () {
		// TODO Auto-generated method stub
		return this.maxScans;
	}

	@Override
	public void setDebugMode (final boolean flag) {
		// TODO Auto-generated method stub
		this.debugMode = flag;

	}

	@Override
	public void setInputFolder (final File input_folder) {
		// TODO Auto-generated method stub
		this.folder = input_folder;

	}

	@Override
	public void setOutputFolder (final File output) {
		this.output = output;
	}

	@Override
	public void setPaddingSize (final int maxScans) {
		// TODO Auto-generated method stub
		this.maxScans = maxScans;

	}

}
