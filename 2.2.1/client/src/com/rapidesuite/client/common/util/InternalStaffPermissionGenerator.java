package com.rapidesuite.client.common.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

//WARNING: Do not include external class here. This class is supposed to be runnable alone

public class InternalStaffPermissionGenerator {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws NoSuchPaddingException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 * @throws Exception 
	 */
	public static void main(String[] args) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IOException {
		//the permission file will expire in 48 hours
		final Date expiration = new Date(System.currentTimeMillis()+ 2*24*60*60*1000L);
		final DateFormat df = getDateFormat();
		final String content = InternalStaffPermissionGenerator.INTERNAL_STAFF_PERMISSION_PREFIX + df.format(expiration);
		final String fileName = "internal_staff_permission.dat";
		final File file = new File(fileName);
		byte inputBytes[] = content.getBytes(CHARACTER_SET_ENCODING);
		byte[] ciphertextBytes = encrypt(inputBytes, INTERNAL_STAFF_PERMISSION_KEY);
		OutputStream os = null;
		try {
			os = new FileOutputStream(file);
			os.write(ciphertextBytes);
		} finally {
			if (os != null) {
				os.close();
			}
		}
		System.out.println("New internal staff permission file '"+fileName+"' has been generated. It will expire at "+df.format(expiration));
	}
	
	public static final byte[] INTERNAL_STAFF_PERMISSION_KEY = {
		(byte) 0x51, (byte) 0x48, (byte) 0x47, (byte) 0x5d, 
		(byte) 0x46, (byte) 0x18, (byte) 0x32, (byte) 0x14, 
		(byte) 0x3b, (byte) 0x0a, (byte) 0x4a, (byte) 0x16, 
		(byte) 0x55, (byte) 0x67, (byte) 0x5c, (byte) 0x41 };    

	private static final String ENCRYPTION_ALGORITHM = "AES";
	
	private static final String CHARACTER_SET_ENCODING = "UTF-8";
	
	public static final String INTERNAL_STAFF_PERMISSION_PREFIX = "INTERNAL_STAFF_PERMISSION_PREFIX ";
	
    public static DateFormat getDateFormat() {
    	return new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
    }
	
    private static byte[] encrypt(final byte[] input, final byte[] key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException
    {
    	final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final SecretKeySpec skeySpec = new SecretKeySpec(key, ENCRYPTION_ALGORITHM);
        final Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
    	CipherOutputStream cout = null;
    	try {
        	cout = new CipherOutputStream(baos, cipher);
        	cout.write(input);
    	} finally {
    		if (cout != null) {
    			cout.close();
    		}
    	}
    	return baos.toByteArray();
    }    

}
