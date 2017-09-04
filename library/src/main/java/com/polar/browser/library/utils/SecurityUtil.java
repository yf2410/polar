package com.polar.browser.library.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class SecurityUtil {
	/**
	 * 计算给定 byte [] 串的 MD5
	 */
	public static byte[] MD5(byte[] input) {
		if (input == null) {
			return null;
		}
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
		}
		if (md != null) {
			md.update(input);
			return md.digest();
		} else
			return null;
	}

	public static byte[] getMD5Bytes(String input) {
		return MD5(input.getBytes());
	}

	public static String getMD5(byte[] input) {
		return ByteConvertor.bytesToHexString(MD5(input));
	}

	public static String getMD5(String input) {
		if (input == null) {
			return "";
		}
		return getMD5(input.getBytes());
	}

	/**
	 * 计算文件 MD5，返回十六进制串
	 */
	public static String getFileMD5(String filename) {
		byte[] digest = MD5(filename);
		if (digest == null) {
			return null;
		} else {
			return ByteConvertor.bytesToHexString(digest);
		}
	}

	public static String getMD5(InputStream inputStream) {
		byte[] digest = null;
		BufferedInputStream in = null;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			in = new BufferedInputStream(inputStream);
			int theByte = 0;
			byte[] buffer = new byte[1024];
			while ((theByte = in.read(buffer)) != -1) {
				md.update(buffer, 0, theByte);
			}
			digest = md.digest();
		} catch (Exception e) {
		} finally {
			if (in != null)
				try {
					in.close();
				} catch (Exception e) {
				}
		}
		if (digest == null) {
			return null;
		} else {
			return ByteConvertor.bytesToHexString(digest);
		}
	}

	/**
	 * 计算文件 MD5，返回 byte []. 如果文件不存在，返回 null.
	 */
	public static byte[] MD5(String filename) {
		return MD5(new File(filename));
	}

	public static byte[] MD5(File file) {
		BufferedInputStream in = null;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			in = new BufferedInputStream(new FileInputStream(file));
			int theByte = 0;
			byte[] buffer = new byte[1024];
			while ((theByte = in.read(buffer)) != -1) {
				md.update(buffer, 0, theByte);
			}
			in.close();
			return md.digest();
		} catch (Exception e) {
		} finally {
			if (in != null)
				try {
					in.close();
				} catch (Exception e) {
				}
		}
		return null;
	}

	public static String DES_encrypt(String plain, String key) {
		try {
			// DES算法要求有一个可信任的随机数源
			SecureRandom sr = new SecureRandom();
			// 从原始密钥数据创建DESKeySpec对象
			// 由于 DES 要求秘钥是 64bit 的，而用户直接输入的 key 可能长度不够，这里简单点，先对 key 进行 md5，截断取前
			// 8 个字节
			DESKeySpec dks = new DESKeySpec(MD5(key.getBytes()));
			// 创建一个密匙工厂，然后用它把DESKeySpec转换成
			// 一个SecretKey对象
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
			SecretKey secretKey = keyFactory.generateSecret(dks);
			// using DES in ECB mode
			Cipher cipher = Cipher.getInstance("DES");
			// 用密匙初始化Cipher对象
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, sr);
			// 执行加密操作
			byte encryptedData[] = cipher.doFinal(plain.getBytes());
			return ByteConvertor.bytesToHexString(encryptedData);
		} catch (Exception e) {
		}
		return "";
	}

	public static String DES_decrypt(String encrypted, String key) {
		try {
			// DES算法要求有一个可信任的随机数源
			SecureRandom sr = new SecureRandom();
			// 从原始密钥数据创建DESKeySpec对象
			// 由于 DES 要求秘钥是 64bit 的，而用户直接输入的 key 可能长度不够，这里简单点，先对 key 进行 md5，截断取前
			// 8 个字节
			DESKeySpec dks = new DESKeySpec(MD5(key.getBytes()));
			// 创建一个密匙工厂，然后用它把DESKeySpec对象转换成
			// 一个SecretKey对象
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
			SecretKey secretKey = keyFactory.generateSecret(dks);
			// using DES in ECB mode
			Cipher cipher = Cipher.getInstance("DES");
			// 用密匙初始化Cipher对象
			cipher.init(Cipher.DECRYPT_MODE, secretKey, sr);
			// 正式执行解密操作
			byte decryptedData[] = cipher.doFinal(ByteConvertor.hexStringToBytes(encrypted));
			return new String(decryptedData);
		} catch (Exception e) {
			// SimpleLog.e(e);
			// 如果口令错误，解密失败，就会抛出异常
		}
		return "";
	}

	public static Key createDesKey() throws Exception {
		SecureRandom sr = new SecureRandom();
		KeyGenerator kg = KeyGenerator.getInstance("DES");
		kg.init(sr);
		Key key = kg.generateKey();
		return key;
	}

	public static File DES_encrypt(File file, String key, File outFile) throws IOException {
		FileInputStream in = null;
		FileWriter fw = null;
		int len;// = (int) file.length();
		byte[] buff = new byte[1024];
		try {
			in = new FileInputStream(file);
			fw = new FileWriter(outFile);
			while ((len = in.read(buff)) > 0) {
				String en = DES_encrypt(new String(buff, 0, len), key);
				fw.write(en);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw e;
		} finally {
			try {
				fw.flush();
				in.close();
				fw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
			}
		}
		return outFile;
	}


	public static File DES_decrypt(File encryptedFile, String key, File outFile) throws IOException {
		FileInputStream in = null;
		byte[] buff = new byte[1024];
		int len;
		FileWriter fw = null;
		try {
			in = new FileInputStream(encryptedFile);
			fw = new FileWriter(outFile);
			while ((len = in.read(buff)) > 0) {
				String en = DES_decrypt(new String(buff, 0, len), key);
				fw.write(en);
			}
			fw.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw e;
		} finally {
			try {
				in.close();
				fw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
			}
		}
		return outFile;
	}
}
