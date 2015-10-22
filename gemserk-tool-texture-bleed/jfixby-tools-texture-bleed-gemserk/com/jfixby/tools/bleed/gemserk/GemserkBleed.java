package com.jfixby.tools.bleed.gemserk;

import java.io.IOException;

import com.jfixby.tools.bleed.api.TextureBleedComponent;
import com.jfixby.tools.bleed.api.TextureBleedSpecs;

public class GemserkBleed implements TextureBleedComponent {

	@Override
	public TextureBleedSpecs newGemserkProcessorSpecs() {
		return new GemserkProcessorSpecsImpl();
	}

	@Override
	public void process(TextureBleedSpecs specs) throws IOException {
		GemserkProcessorImpl p = new GemserkProcessorImpl(specs);
		p.process();
	}

}
