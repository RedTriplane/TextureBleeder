package com.jfixby.tools.bleed.gemserk;

import java.io.IOException;

import com.jfixby.tools.bleed.api.TextureBleedComponent;
import com.jfixby.tools.bleed.api.TextureBleedResult;
import com.jfixby.tools.bleed.api.TextureBleedSpecs;

public class GemserkBleed implements TextureBleedComponent {

	@Override
	public TextureBleedSpecs newTextureBleedSpecs() {
		return new GemserkProcessorSpecsImpl();
	}

	@Override
	public TextureBleedResult process(TextureBleedSpecs specs)
			throws IOException {
		GemserkProcessorImpl p = new GemserkProcessorImpl(specs);
		p.process();
		return null;
	}

}
