package com.rapidesuite.build.utils;

public class IterationCountAndHumanReadableFileSize {
	private final int iterationCount;
	private final String humanReadableFileSize;
	
	public IterationCountAndHumanReadableFileSize(final int iterationCount, final String humanReadableFileSize) {
		this.iterationCount = iterationCount;
		this.humanReadableFileSize = humanReadableFileSize;
	}
	
	public int getIterationCount() {
		return iterationCount;
	}
	public String getHumanReadableFileSize() {
		return humanReadableFileSize;
	}
}
