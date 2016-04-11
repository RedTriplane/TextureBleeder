
package com.github.wrebecca.bleed;

import com.jfixby.cmns.api.file.File;
import com.jfixby.tools.bleed.api.TextureBleedSpecs;

public class TextureBleedSpecsImpl implements TextureBleedSpecs {
	File folder;
	private boolean debugMode;
	private int maxScans = -1;
	private File output;

	@Override
	public File getInputFolder () {
		// TODO Auto-generated method stub
		return folder;
	}

	@Override
	public void setInputFolder (File input_folder) {
		// TODO Auto-generated method stub
		folder = input_folder;

	}

	@Override
	public boolean getDebugMode () {
		// TODO Auto-generated method stub
		return debugMode;
	}

	@Override
	public void setDebugMode (boolean flag) {
		// TODO Auto-generated method stub
		this.debugMode = flag;

	}

	@Override
	public int getPaddingSize () {
		// TODO Auto-generated method stub
		return maxScans;
	}

	@Override
	public void setPaddingSize (int maxScans) {
		// TODO Auto-generated method stub
		this.maxScans = maxScans;

	}

	@Override
	public void setOutputFolder (File output) {
		this.output = output;
	}

	@Override
	public File getOutputFolder () {
		return output;
	}

}
