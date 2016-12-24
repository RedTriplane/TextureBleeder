
package com.jfixby.tools.bleed.test;

import java.io.IOException;

import com.jfixby.cmns.api.lambda.Lambda;
import com.jfixby.red.lambda.RedLambda;
import com.jfixby.scarabei.api.desktop.DesktopSetup;
import com.jfixby.scarabei.api.file.File;
import com.jfixby.scarabei.api.file.LocalFileSystem;
import com.jfixby.tools.bleed.api.TextureBleed;
import com.jfixby.tools.bleed.api.TextureBleedResult;
import com.jfixby.tools.bleed.api.TextureBleedSpecs;
import com.jfixby.tools.bleed.mask.MaskTextureBleeder;

public class TesMaskTextureBleeder {

	public static void main (final String[] args) throws IOException {
		DesktopSetup.deploy();
		TextureBleed.installComponent(new MaskTextureBleeder());
		Lambda.installComponent(new RedLambda());

		final TextureBleedSpecs bleedSpecs = TextureBleed.newSpecs();
		bleedSpecs.setDebugMode(true);
		bleedSpecs.setPaddingSize(16);
		final File examples_folder = LocalFileSystem.ApplicationHome().child("examples");
		final File tmp_folder = LocalFileSystem.ApplicationHome().child("mask");
		bleedSpecs.setInputFolder(examples_folder);
		bleedSpecs.setOutputFolder(tmp_folder);
		tmp_folder.delete();

// LocalFileSystem.copyFolderContentsToFolder(examples_folder, tmp_folder);

		final TextureBleedResult result = TextureBleed.process(bleedSpecs);
		result.print();

	}

}
