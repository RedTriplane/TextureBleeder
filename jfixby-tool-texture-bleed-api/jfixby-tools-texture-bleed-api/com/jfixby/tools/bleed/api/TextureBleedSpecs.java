
package com.jfixby.tools.bleed.api;

import com.jfixby.cmns.api.file.File;

public interface TextureBleedSpecs {

	public static final int DEFAULT_PADDING = 16;

	File getInputFolder ();

	void setInputFolder (File input_folder);

	boolean getDebugMode ();

	void setDebugMode (boolean flag);

	int getPaddingSize ();

	void setPaddingSize (int maxScans);

	void setOutputFolder (File output);

	File getOutputFolder ();

}
