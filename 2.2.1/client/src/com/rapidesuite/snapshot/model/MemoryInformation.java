package com.rapidesuite.snapshot.model;

public class MemoryInformation {

	private double totalSpaceInGB;
	private double totalFreeSpaceInGB;
	private double totalUsedSpaceInGB;
	
	public double getTotalSpaceInGB() {
		return totalSpaceInGB;
	}
	
	public void setTotalSpaceInGB(double totalSpaceInGB) {
		this.totalSpaceInGB = totalSpaceInGB;
	}
	
	public double getTotalFreeSpaceInGB() {
		return totalFreeSpaceInGB;
	}
	
	public void setTotalFreeSpaceInGB(double totalFreeSpaceInGB) {
		this.totalFreeSpaceInGB = totalFreeSpaceInGB;
	}
	
	public double getTotalUsedSpaceInGB() {
		return totalUsedSpaceInGB;
	}
	
	public void setTotalUsedSpaceInGB(double  totalUsedSpaceInGB) {
		this.totalUsedSpaceInGB = totalUsedSpaceInGB;
	}
	
}
