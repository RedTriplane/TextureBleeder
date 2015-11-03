package com.jfixby.tool.texture.bleed.test;

import java.io.IOException;

import com.jfixby.cmns.api.collections.JUtils;
import com.jfixby.cmns.api.color.Colors;
import com.jfixby.cmns.api.filesystem.File;
import com.jfixby.cmns.api.filesystem.LocalFileSystem;
import com.jfixby.cmns.api.image.ImageProcessing;
import com.jfixby.cmns.api.io.IO;
import com.jfixby.cmns.api.log.L;
import com.jfixby.cmns.api.math.FloatMath;
import com.jfixby.cmns.api.math.IntegerMath;
import com.jfixby.cmns.api.md5.MD5;
import com.jfixby.cmns.api.net.http.Http;
import com.jfixby.cmns.api.sys.Sys;
import com.jfixby.cmns.desktop.DesktopAssembler;
import com.jfixby.cmns.jutils.desktop.DesktopUtils;
import com.jfixby.cv.api.gwt.ImageGWT;
import com.jfixby.cv.red.gwt.RedImageGWT;
import com.jfixby.red.color.RedColors;
import com.jfixby.red.desktop.filesystem.win.WinFileSystem;
import com.jfixby.red.desktop.img.processing.DesktopImageProcessing;
import com.jfixby.red.desktop.log.DesktopLogger;
import com.jfixby.red.desktop.math.DesktopFloatMath;
import com.jfixby.red.desktop.math.RedIntegerMath;
import com.jfixby.red.desktop.net.HttpDesktopComponent;
import com.jfixby.red.desktop.sys.DesktopSystem;
import com.jfixby.red.io.RedIO;
import com.jfixby.red.util.md5.AlpaeroMD5;
import com.jfixby.tool.texture.bleed.red.RedTextureBleeder;
import com.jfixby.tools.bleed.api.TextureBleed;
import com.jfixby.tools.bleed.api.TextureBleedResult;
import com.jfixby.tools.bleed.api.TextureBleedSpecs;

public class TestRedTextureBleeder {

	public static void main(String[] args) throws IOException {
		DesktopAssembler.setup();
		ImageGWT.installComponent(new RedImageGWT());
		TextureBleed.installComponent(new RedTextureBleeder());

		TextureBleedSpecs bleedSpecs = TextureBleed.newSpecs();
		bleedSpecs.setDebugMode(true);
		bleedSpecs.setPaddingSize(Integer.MAX_VALUE);
		File examples_folder = LocalFileSystem.ApplicationHome().child("examples");
		File tmp_folder = LocalFileSystem.ApplicationHome().child("tmp");
		bleedSpecs.setInputFolder(tmp_folder);

		LocalFileSystem.copyFolderContentsToFolder(examples_folder, tmp_folder);

		TextureBleedResult result = TextureBleed.process(bleedSpecs);
		result.print();

	}

}
