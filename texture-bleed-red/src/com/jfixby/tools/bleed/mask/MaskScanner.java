
package com.jfixby.tools.bleed.mask;

import com.jfixby.scarabei.api.err.Err;

class MaskScanner {
	int index;
	private final ColorMask mask;

	public MaskScanner (final ColorMask mask) {
		this.mask = mask;
	}

	public boolean hasNext () {
		return this.index < this.mask.waitingSize;
	}

	public int next () {
		if (this.index >= this.mask.waitingSize) {
			Err.reportError(String.valueOf(this.index));
		}
		return this.mask.waiting[this.index++];
	}

	public void mark () {
		this.index--;
		final int removed = this.mask.removeIndex(this.index);
		this.mask.changing[this.mask.changingSize] = removed;
		this.mask.changingSize++;
	}

	public void reset () {
		this.index = 0;
		for (int i = 0; i < this.mask.changingSize; i++) {
			final int index = this.mask.changing[i];
			this.mask.data[index] = MaskState.original;
		}
		this.mask.changingSize = 0;
	}
}
