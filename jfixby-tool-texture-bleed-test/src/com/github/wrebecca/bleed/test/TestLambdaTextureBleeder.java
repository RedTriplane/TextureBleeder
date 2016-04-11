
package com.github.wrebecca.bleed.test;

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

	public static void main (String[] args) throws IOException {
		DesktopAssembler.setup();
		TextureBleed.installComponent(new LambdaTextureBleeder());
		Lambda.installComponent(new RedLambda());

		TextureBleedSpecs bleedSpecs = TextureBleed.newSpecs();
		bleedSpecs.setDebugMode(true);
		bleedSpecs.setPaddingSize(16);
		File examples_folder = LocalFileSystem.ApplicationHome().child("examples");
		File tmp_folder = LocalFileSystem.ApplicationHome().child("tmp");
		bleedSpecs.setInputFolder(tmp_folder);

		LocalFileSystem.copyFolderContentsToFolder(examples_folder, tmp_folder);

		TextureBleedResult result = TextureBleed.process(bleedSpecs);
		result.print();

	}

}
