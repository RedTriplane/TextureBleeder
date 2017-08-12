
package com.jfixby.tools.bleed;

import com.jfixby.scarabei.api.file.File;

public class FileResult {

	private long mills;
	private int scans;
	private File inputFile;
	private File outputFile;

	public File getOutputFile () {
		return this.outputFile;
	}

	public void setDoneInMills (final long mills) {
		// TODO Auto-generated method stub
		this.mills = mills;

	}

	public void setInputFile (final File png) {
		// TODO Auto-generated method stub
		this.inputFile = png;

	}

	public void setOutputFile (final File outputFile) {
		this.outputFile = outputFile;
	}

	public void setScansPerformed (final int k) {
		// TODO Auto-generated method stub
		this.scans = k;
	}

	@Override
	public String toString () {
		return this.inputFile + " was processed with " + this.scans + " scans in " + this.mills + " milliseconds output: "
			+ this.outputFile;
	}

}
