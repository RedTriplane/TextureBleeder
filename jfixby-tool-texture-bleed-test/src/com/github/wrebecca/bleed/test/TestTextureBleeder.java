package com.github.wrebecca.bleed.test;

import java.io.IOException;

import com.jfixby.cmns.api.collections.Collections;
import com.jfixby.cmns.api.color.Colors;
import com.jfixby.cmns.api.file.File;
import com.jfixby.cmns.api.file.LocalFileSystem;
import com.jfixby.cmns.api.image.ImageProcessing;
import com.jfixby.cmns.api.io.IO;
import com.jfixby.cmns.api.lambda.Lambda;
import com.jfixby.cmns.api.log.L;
import com.jfixby.cmns.api.math.FloatMath;
import com.jfixby.cmns.api.math.IntegerMath;
import com.jfixby.cmns.api.md5.MD5;
import com.jfixby.cmns.api.net.http.Http;
import com.jfixby.cmns.api.sys.Sys;
import com.jfixby.cmns.collections.DesktopCollections;
import com.jfixby.cv.api.cv.CV;
import com.jfixby.cv.api.gwt.ImageGWT;
import com.jfixby.cv.red.gwt.RedCV;
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
import com.jfixby.red.lambda.RedLambda;
import com.jfixby.red.util.md5.AlpaeroMD5;
import com.jfixby.tools.bleed.api.TextureBleed;
import com.jfixby.tools.bleed.api.TextureBleedResult;
import com.jfixby.tools.bleed.api.TextureBleedSpecs;
import com.jfixby.tools.bleed.lambda.LambdaTextureBleeder;

public class TestTextureBleeder {

	public static void main(String[] args) throws IOException {
		L.installComponent(new DesktopLogger());
		Collections.installComponent(new DesktopCollections());
		IO.installComponent(new RedIO());
		IntegerMath.installComponent(new RedIntegerMath());
		MD5.installComponent(new AlpaeroMD5());
		Sys.installComponent(new DesktopSystem());
		LocalFileSystem.installComponent(new WinFileSystem());
		Http.installComponent(new HttpDesktopComponent());
		FloatMath.installComponent(new DesktopFloatMath());
		TextureBleed.installComponent(new LambdaTextureBleeder());
		ImageProcessing.installComponent(new DesktopImageProcessing());
		Colors.installComponent(new RedColors());
		ImageGWT.installComponent(new RedImageGWT());
		CV.installComponent(new RedCV());
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
