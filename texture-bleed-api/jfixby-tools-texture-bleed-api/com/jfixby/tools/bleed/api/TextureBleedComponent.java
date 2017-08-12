package com.jfixby.tools.bleed.api;

import java.io.IOException;

public interface TextureBleedComponent {

	TextureBleedSpecs newTextureBleedSpecs();

	TextureBleedResult process(TextureBleedSpecs specs) throws IOException;

}
