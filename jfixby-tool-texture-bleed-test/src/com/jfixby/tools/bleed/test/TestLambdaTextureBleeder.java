
package com.jfixby.tools.bleed.test;

import java.io.IOException;

import com.jfixby.cmns.api.file.File;
import com.jfixby.cmns.api.file.LocalFileSystem;
import com.jfixby.cmns.api.lambda.Lambda;
import com.jfixby.red.desktop.DesktopAssembler;
import com.jfixby.red.lambda.RedLambda;
import com.jfixby.tools.bleed.api.TextureBleed;
import com.jfixby.tools.bleed.api.TextureBleedResult;
import com.jfixby.tools.bleed.api.TextureBleedSpecs;
import com.jfixby.tools.bleed.lambda.LambdaTextureBleeder;

public class TestLambdaTextureBleeder {

	public static void main (final String[] args) throws IOException {
		DesktopAssembler.setup();
		TextureBleed.installComponent(new LambdaTextureBleeder());
		Lambda.installComponent(new RedLambda());

		final TextureBleedSpecs bleedSpecs = TextureBleed.newSpecs();
		bleedSpecs.setDebugMode(true);
		bleedSpecs.setPaddingSize(16);
		final File examples_folder = LocalFileSystem.ApplicationHome().child("examples");
		final File tmp_folder = LocalFileSystem.ApplicationHome().child("lambda");
		bleedSpecs.setInputFolder(examples_folder);
		bleedSpecs.setOutputFolder(tmp_folder);
		tmp_folder.delete();

// LocalFileSystem.copyFolderContentsToFolder(examples_folder, tmp_folder);

		final TextureBleedResult result = TextureBleed.process(bleedSpecs);
		result.print();

	}

}
