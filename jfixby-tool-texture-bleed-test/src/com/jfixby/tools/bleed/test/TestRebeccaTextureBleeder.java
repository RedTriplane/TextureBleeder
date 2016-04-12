
package com.jfixby.tools.bleed.test;

import java.io.IOException;

import com.github.wrebecca.bleed.RebeccaTextureBleeder;
import com.jfixby.cmns.api.file.File;
import com.jfixby.cmns.api.file.LocalFileSystem;
import com.jfixby.red.desktop.DesktopAssembler;
import com.jfixby.tools.bleed.api.TextureBleed;
import com.jfixby.tools.bleed.api.TextureBleedResult;
import com.jfixby.tools.bleed.api.TextureBleedSpecs;

public class TestRebeccaTextureBleeder {

	public static void main (final String[] args) throws IOException {
		DesktopAssembler.setup();
		TextureBleed.installComponent(new RebeccaTextureBleeder());

		final TextureBleedSpecs bleedSpecs = TextureBleed.newSpecs();
		bleedSpecs.setDebugMode(true);
		bleedSpecs.setPaddingSize(16);
		final File examples_folder = LocalFileSystem.ApplicationHome().child("examples");
		final File tmp_folder = LocalFileSystem.ApplicationHome().child("rebecca");
		bleedSpecs.setInputFolder(examples_folder);
		bleedSpecs.setOutputFolder(tmp_folder);
		tmp_folder.delete();

// LocalFileSystem.copyFolderContentsToFolder(examples_folder, tmp_folder);

		final TextureBleedResult result = TextureBleed.process(bleedSpecs);
		result.print();

	}

}
