package com.jfixby.tools.bleed.api;

import java.io.IOException;

public interface TextureBleedComponent {

	TextureBleedSpecs newGemserkProcessorSpecs();

	void process(TextureBleedSpecs specs) throws IOException;

}
