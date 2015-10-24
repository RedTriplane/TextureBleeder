package com.github.wrebecca.bleed;

import java.util.LinkedList;

import com.jfixby.tools.bleed.api.TextureBleedResult;

public class TextureBleedResultImpl implements TextureBleedResult {

	LinkedList<FileResultImpl> fileResultList = new LinkedList<FileResultImpl>();

	public void addFileResult(FileResultImpl fileResult) {
		// TODO Auto-generated method stub
		fileResultList.add(fileResult);

	}

	@Override
	public void print() {
		// TODO Auto-generated method stub
		System.out.println("TextureBleedResult:");
		for (FileResultImpl item : fileResultList) {
			System.out.println("   " + item);
		}
	}

}
