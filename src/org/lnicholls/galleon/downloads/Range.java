package org.lnicholls.galleon.downloads;

/*
 *
 * Created on 21 de abril de 2003, 05:43 PM
 */

public class Range {

	long start = 0;

	long end = 0;

	/** Creates a new instance of Range */
	public Range(long start, long end) {

		if (end < start) {
			long b = end;
			end = start;
			start = b;
		}

		this.start = start;
		this.end = end;
	}

	public long getStart() {
		return start;
	}

	public long getEnd() {
		return end;
	}

	public Range getSubRange(long start) {
		if (start < this.start)
			start = this.start;

		if (start > end)
			start = end;

		return new Range(start, getEnd());
	}

	public Range getSubRange(long start, long end) {
		if (start < this.start)
			start = this.start;

		if (start > this.end)
			start = this.end;

		if (end > this.end)
			end = this.end;

		if (end < this.start)
			end = this.start;

		return new Range(start, end);
	}

	public Range split(long point) {
		if (point < start)
			point = start;

		if (point > end)
			point = end;

		if (point > end)
			point = end;

		if (point < start)
			point = start;

		start = getSubRange(point + 1).getStart();

		return new Range(start, point);
	}

	public String toString() {
		return start + "-" + end;
	}
}