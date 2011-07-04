package plugins.nherve.browser.cache;

import java.awt.image.BufferedImage;
import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.imageio.ImageIO;

public class DefaultThumbnailCache implements ThumbnailCache {
	private String name;
	private File cacheDirectory;

	private final static byte EQUALS_SIGN = (byte) '_';

	private final static byte[] _STANDARD_ALPHABET = { (byte) 'A', (byte) 'B', (byte) 'C', (byte) 'D', (byte) 'E', (byte) 'F', (byte) 'G', (byte) 'H', (byte) 'I', (byte) 'J', (byte) 'K', (byte) 'L', (byte) 'M', (byte) 'N', (byte) 'O', (byte) 'P', (byte) 'Q', (byte) 'R', (byte) 'S', (byte) 'T', (byte) 'U', (byte) 'V', (byte) 'W', (byte) 'X', (byte) 'Y', (byte) 'Z', (byte) 'a', (byte) 'b', (byte) 'c', (byte) 'd', (byte) 'e', (byte) 'f', (byte) 'g', (byte) 'h', (byte) 'i', (byte) 'j', (byte) 'k', (byte) 'l', (byte) 'm', (byte) 'n', (byte) 'o', (byte) 'p', (byte) 'q', (byte) 'r', (byte) 's', (byte) 't', (byte) 'u', (byte) 'v', (byte) 'w', (byte) 'x', (byte) 'y', (byte) 'z', (byte) '0', (byte) '1', (byte) '2', (byte) '3', (byte) '4', (byte) '5', (byte) '6', (byte) '7', (byte) '8', (byte) '9', (byte) '+', (byte) '-' };

	public DefaultThumbnailCache(String name) {
		super();

		this.name = name;
	}

	private File cacheFile(String s) {
		return new File(cacheDirectory, hash(s) + ".jpg");
	}

	@Override
	public void clear() throws CacheException {
		for (File f : cacheDirectory.listFiles()) {
			f.delete();
		}
	}

	private byte[] encode3to4(byte[] source, int srcOffset, int numSigBytes, byte[] destination, int destOffset) {
		byte[] ALPHABET = _STANDARD_ALPHABET;

		int inBuff = (numSigBytes > 0 ? ((source[srcOffset] << 24) >>> 8) : 0) | (numSigBytes > 1 ? ((source[srcOffset + 1] << 24) >>> 16) : 0) | (numSigBytes > 2 ? ((source[srcOffset + 2] << 24) >>> 24) : 0);

		switch (numSigBytes) {
		case 3:
			destination[destOffset] = ALPHABET[(inBuff >>> 18)];
			destination[destOffset + 1] = ALPHABET[(inBuff >>> 12) & 0x3f];
			destination[destOffset + 2] = ALPHABET[(inBuff >>> 6) & 0x3f];
			destination[destOffset + 3] = ALPHABET[(inBuff) & 0x3f];
			return destination;

		case 2:
			destination[destOffset] = ALPHABET[(inBuff >>> 18)];
			destination[destOffset + 1] = ALPHABET[(inBuff >>> 12) & 0x3f];
			destination[destOffset + 2] = ALPHABET[(inBuff >>> 6) & 0x3f];
			destination[destOffset + 3] = EQUALS_SIGN;
			return destination;

		case 1:
			destination[destOffset] = ALPHABET[(inBuff >>> 18)];
			destination[destOffset + 1] = ALPHABET[(inBuff >>> 12) & 0x3f];
			destination[destOffset + 2] = EQUALS_SIGN;
			destination[destOffset + 3] = EQUALS_SIGN;
			return destination;

		default:
			return destination;
		}
	}

	private String encodeBytes(byte[] source, int off, int len, String preferredEncoding) {
		byte[] encoded = encodeBytesToBytes(source, off, len);

		try {
			return new String(encoded, preferredEncoding);
		} catch (java.io.UnsupportedEncodingException uue) {
			return new String(encoded);
		}

	}

	private byte[] encodeBytesToBytes(byte[] source, int off, int len) {

		if (source == null) {
			throw new NullPointerException("Cannot serialize a null array.");
		}

		if (off < 0) {
			throw new IllegalArgumentException("Cannot have negative offset: " + off);
		}

		if (len < 0) {
			throw new IllegalArgumentException("Cannot have length offset: " + len);
		}

		if (off + len > source.length) {
			throw new IllegalArgumentException("Cannot have offset of " + off + " and length of " + len + " with array of length " + source.length);
		}

		int encLen = (len / 3) * 4 + (len % 3 > 0 ? 4 : 0);
		byte[] outBuff = new byte[encLen];

		int d = 0;
		int e = 0;
		int len2 = len - 2;
		int lineLength = 0;
		for (; d < len2; d += 3, e += 4) {
			encode3to4(source, d + off, 3, outBuff, e);

			lineLength += 4;
		}

		if (d < len) {
			encode3to4(source, d + off, len - d, outBuff, e);
			e += 4;
		}

		if (e <= outBuff.length - 1) {
			byte[] finalOut = new byte[e];
			System.arraycopy(outBuff, 0, finalOut, 0, e);
			return finalOut;
		} else {
			return outBuff;
		}

	}

	@Override
	public BufferedImage get(String s) throws CacheException {
		File f = cacheFile(s);
		if (!f.exists()) {
			return null;
		}

		try {
			return ImageIO.read(f);
		} catch (Exception e) {
			System.err.println("Unable to open cahed file for " + s + " (" + f.getName() + ") " + e.getClass().getName() + " : " + e.getMessage());
			return null;
		}
	}

	private String hash(String f) {
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			byte[] bts = digest.digest(f.getBytes());
			return encodeBytes(bts, 0, bts.length, "US-ASCII");
		} catch (NoSuchAlgorithmException e) {
			return null;
		}

	}

	@Override
	public void init() throws CacheException {
		try {
			MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new CacheException(e);
		}
		String dir = System.getProperty("java.io.tmpdir");
		cacheDirectory = new File(dir + File.separator + name + File.separator);

		cacheDirectory.mkdirs();
	}

	@Override
	public void store(String s, BufferedImage bi) throws CacheException {
		if (bi != null) {
			File f = cacheFile(s);
			try {
				ImageIO.write(bi, "JPG", f);
			} catch (Exception e) {
				System.err.println("Unable to write cached file for " + s + " (" + f.getName() + ") " + e.getClass().getName() + " : " + e.getMessage());
			}
		}
	}

	@Override
	public String getSizeInfo() {
		long s = 0;
		for (File f : cacheDirectory.listFiles()) {
			s += f.length();
		}
		int mo = (int)(s / (1024 * 1024));
		return "("+mo +" Mo)";
	}
}
