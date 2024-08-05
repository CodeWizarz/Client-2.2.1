package com.rapidesuite.client.common;

import java.io.File;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.util.Assert;

public final class EbsServerUrlAndBwpCombination {

	private final String ebsServerAddress;
	private final File bwp;
	
	public EbsServerUrlAndBwpCombination(final String rawEbsServerUrl, final File bwp) {
		Assert.notNull(rawEbsServerUrl);
		Assert.notNull(bwp);
		this.bwp = bwp;
		
		String url = rawEbsServerUrl;
		url = url.replaceAll("^.*://", "");
		url = url.replaceAll("/.*$", "");
		this.ebsServerAddress = url;	
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		} else if (!(obj instanceof EbsServerUrlAndBwpCombination)) {
			return false;
		} else {
			EbsServerUrlAndBwpCombination c = (EbsServerUrlAndBwpCombination) obj;
			return (this.ebsServerAddress.equals(c.ebsServerAddress) && this.bwp.equals(c.bwp));
		}
	}
	
	@Override
	public int hashCode() {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			final String s = String.valueOf(ebsServerAddress.hashCode())+"-"+String.valueOf(bwp.hashCode());
			byte[] hash = md.digest(s.getBytes());
			ByteBuffer bb = ByteBuffer.wrap(hash);
			final int output = bb.getInt();
			return output;
		} catch (NoSuchAlgorithmException e) {
			throw new Error(e);
		}
	}
	
	public final String getEbsServerAddress() {
		return ebsServerAddress;
	}

	public final File getBwp() {
		return bwp;
	}	
	
}
