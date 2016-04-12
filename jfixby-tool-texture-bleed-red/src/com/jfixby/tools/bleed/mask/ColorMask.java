
package com.jfixby.tools.bleed.mask;

import com.jfixby.cmns.api.color.Colors;
import com.jfixby.cmns.api.color.CustomColor;
import com.jfixby.cmns.api.err.Err;

class ColorMask {
	MaskState[] data;
	int[] waiting;
	int waitingSize = 0;
	int[] changing;
	int changingSize;
	CustomColor color = Colors.newColor();

	public ColorMask (final int[] rgb) {
		this.data = new MaskState[rgb.length];
		this.waiting = new int[rgb.length];
		this.changing = new int[rgb.length];

		for (int i = 0; i < rgb.length; i++) {
			final int pixel = rgb[i];
			this.color.setARGB(pixel);
			if (this.color.alpha() == 0) {
				this.data[i] = MaskState.toProcess;
				this.waiting[this.waitingSize] = i;
				this.waitingSize++;
			} else {
				this.data[i] = MaskState.original;
			}
		}
	}

	public MaskState getMask (final int index) {
		return this.data[index];
	}

	public int getWaitingSize () {
		return this.waitingSize;
	}

	public MaskScanner iterator () {
		return new MaskScanner(this);
	}

	int removeIndex (final int index) {
		if (index >= this.waitingSize) {
			Err.reportError(String.valueOf(index));
		}

		final int value = this.waiting[index];
		this.waitingSize--;
		this.waiting[index] = this.waiting[this.waitingSize];
		return value;
	}

}
