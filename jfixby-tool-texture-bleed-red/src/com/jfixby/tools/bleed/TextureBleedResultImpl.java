
package com.jfixby.tools.bleed;

import java.util.LinkedList;

import com.jfixby.tools.bleed.api.TextureBleedResult;

public class TextureBleedResultImpl implements TextureBleedResult {

	LinkedList<FileResult> fileResultList = new LinkedList<FileResult>();

	public void addFileResult (final FileResult fileResult) {
		// TODO Auto-generated method stub
		this.fileResultList.add(fileResult);

	}

	@Override
	public void print () {
		// TODO Auto-generated method stub
		System.out.println("TextureBleedResult:");
		for (final FileResult item : this.fileResultList) {
			System.out.println("   " + item);
		}
	}

}
