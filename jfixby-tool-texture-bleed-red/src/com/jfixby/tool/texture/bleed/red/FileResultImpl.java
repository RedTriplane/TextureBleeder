package com.jfixby.tool.texture.bleed.red;

import com.jfixby.cmns.api.filesystem.File;

public class FileResultImpl {

	private long mills;
	private int scans;
	private File png;

	public void setDoneInMills(long mills) {
		// TODO Auto-generated method stub
		this.mills = mills;

	}

	public void setProcessedFile(File png) {
		// TODO Auto-generated method stub
		this.png = png;

	}

	public void setScansPerformed(int k) {
		// TODO Auto-generated method stub
		this.scans = k;
	}

	@Override
	public String toString() {
		return png + " was processed with " + scans + " scans in " + mills
				+ " milliseconds";
	}

}
