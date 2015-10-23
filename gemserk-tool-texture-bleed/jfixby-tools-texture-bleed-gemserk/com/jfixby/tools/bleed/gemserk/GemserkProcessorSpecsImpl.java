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

	@Override
	public boolean getDebugMode() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setDebugMode(boolean flag) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getMaxScans() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setMaxScans(int maxScans) {
		// TODO Auto-generated method stub
		
	}

}
