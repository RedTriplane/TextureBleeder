
package com.github.wrebecca.bleed;

import com.jfixby.cmns.api.file.File;

public class FileResultImpl {

	private long mills;
	private int scans;
	private File inputFile;
	private File outputFile;

	public void setDoneInMills (long mills) {
		// TODO Auto-generated method stub
		this.mills = mills;

	}

	public void setInputFile (File png) {
		// TODO Auto-generated method stub
		this.inputFile = png;

	}

	public void setScansPerformed (int k) {
		// TODO Auto-generated method stub
		this.scans = k;
	}

	@Override
	public String toString () {
		return inputFile + " was processed with " + scans + " scans in " + mills + " milliseconds output: " + this.outputFile;
	}

	public void setOutputFile (File outputFile) {
		this.outputFile = outputFile;
	}

	public File getOutputFile () {
		return this.outputFile;
	}

}
