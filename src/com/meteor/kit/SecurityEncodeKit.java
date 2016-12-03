package com.meteor.kit;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Zip;
import org.apache.tools.ant.types.FileSet;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * 
 * @ClassName SecurityEncode
 * @author Meteor
 * @date 2015年8月9日 上午10:53:40
 * @category 安全的编码类
 */
public class SecurityEncodeKit {

	private final static String[] hexDigits = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d",
			"e", "f" };

	/**
	 * 
	 * @author Meteor
	 * @Cdate 2015年8月9日 上午10:57:57
	 * @Title byteArrayToHexString
	 * @param b
	 * @return String 返回类型
	 * @category 转换字节数组为16进制字串
	 */
	public static String byteArrayToHexString(byte[] b) {
		StringBuffer resultSb = new StringBuffer();
		for (int i = 0; i < b.length; i++) {
			resultSb.append(byteToHexString(b[i]));
		}
		return resultSb.toString();
	}

	private static String byteToHexString(byte b) {
		int n = b;
		if (n < 0)
			n = 256 + n;
		int d1 = n / 16;
		int d2 = n % 16;
		return hexDigits[d1] + hexDigits[d2];
	}
	
	/**
	 * 
	 * @author Meteor
	 * @Cdate 2015年8月8日 下午10:31:56
	 * @Title toByteArray
	 * @param s
	 * @return byte[] 返回类型
	 * @category 将16进制字串转换为字节数组
	 */
	public static byte[] toByteArray(String s) {
		byte[] buf = new byte[s.length() / 2];
		int j = 0;
		for (int i = 0; i < buf.length; i++) {
			buf[i] = (byte) ((Character.digit(s.charAt(j++), 16) << 4) | Character.digit(s.charAt(j++), 16));
		}
		return buf;
	}

	/**
	 * 
	 * @author Meteor
	 * @Cdate 2015年8月9日 上午10:55:11
	 * @Title MD5Encode
	 * @param origin
	 * @return String 返回类型
	 * @category md5加密
	 */
	public static String MD5Encode(String origin) {
		String resultString = null;

		try {
			resultString = new String(origin);
			MessageDigest md = MessageDigest.getInstance("MD5");
			resultString = byteArrayToHexString(md.digest(resultString.getBytes()));
		} catch (Exception ex) {

		}
		return resultString;
	}

	/**
	 * 
	 * @author Meteor
	 * @Cdate 2015年8月9日 上午10:55:29
	 * @Title SHA1
	 * @param origin
	 * @return String 返回类型
	 * @category SHA1加密
	 */
	public static String SHA1(String origin) {
		try {
			MessageDigest sha = MessageDigest.getInstance("SHA-1");
			byte[] data1 = origin.getBytes();

			sha.update(data1);
			byte[] msgDigest = sha.digest();

			return new String(msgDigest);
		} catch (Exception ex) {

		}
		return "";
	}


	/**
	 * 
	 * @author Meteor
	 * @Cdate 2015年8月9日 上午10:56:32
	 * @Title encryptDes
	 * @param message 需要加密的内容
	 * @param keyBytes 密匙必须是24字节
	 * @param ivBytes 向量必须是8字节
	 * @return
	 * @throws Exception byte[] 返回类型
	 * @category Des编码
	 */
	public static byte[] encryptDes(String message, byte[] keyBytes, byte[] ivBytes) throws Exception {
		final SecretKey key = new SecretKeySpec(keyBytes, "DESede");
		final IvParameterSpec iv = new IvParameterSpec(ivBytes);
		final Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, key, iv);
		final byte[] plainTextBytes = message.getBytes("utf-8");
		final byte[] cipherText = cipher.doFinal(plainTextBytes);
		return cipherText;
	}

	/**
	 * 
	 * @author Meteor
	 * @Cdate 2015年8月9日 上午10:56:32
	 * @Title encryptDes
	 * @param message 需要加密的内容
	 * @param keyBytes 密匙必须是24字节
	 * @param ivBytes 向量必须是8字节
	 * @return
	 * @throws Exception byte[] 返回类型
	 * @category Des反编码
	 */
	public static String decryptDes(byte[] message, byte[] keyBytes, byte[] ivBytes) throws Exception {
		final SecretKey key = new SecretKeySpec(keyBytes, "DESede");
		final IvParameterSpec iv = new IvParameterSpec(ivBytes);
		final Cipher decipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
		decipher.init(Cipher.DECRYPT_MODE, key, iv);
		final byte[] plainText = decipher.doFinal(message);
		return new String(plainText, "UTF-8");
	}
   
   
	/**
	 * 
	 * @author Meteor
	 * @Cdate 2015年8月10日 下午4:44:27
	 * @Title compressToByte
	 * @param str
	 * @param encoding
	 * @return String 返回类型
	 * @category 使用gzip压缩数据
	 */
    public static String Gzipcompress(String str,String encoding){
    	try {
	    	if(StringUtils.isBlank(encoding)){
	        	encoding="UTF-8";
	        }
	        if (str == null || str.length() == 0) {
	            return null;
	        }
	        ByteArrayOutputStream out = new ByteArrayOutputStream();
	        GZIPOutputStream gzip;
	        try {
	            gzip = new GZIPOutputStream(out);
	            gzip.write(str.getBytes(encoding));
	            gzip.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        return out.toString("ISO-8859-1");			
		} catch (Exception e) {
			return null;
		}
    }

    /**
     * 
     * @author Meteor
     * @Cdate 2015年8月10日 下午4:44:47
     * @Title uncompressToString
     * @param b
     * @param encoding
     * @return String 返回类型
     * @category 使用gzip解压缩数据
     */
    public static String Gzipdecompress(String str, String encoding){
    	try {
    		if(StringUtils.isBlank(encoding)){
            	encoding="UTF-8";
            }
	    	byte[] b= str.getBytes("ISO-8859-1");
	    	if (b == null || b.length == 0) {
	            return null;
	        }
	        ByteArrayOutputStream out = new ByteArrayOutputStream();
	        ByteArrayInputStream in = new ByteArrayInputStream(b);
	      
            GZIPInputStream gunzip = new GZIPInputStream(in);
            byte[] buffer = new byte[256];
            int n;
            while ((n = gunzip.read(buffer)) >= 0) {
                out.write(buffer, 0, n);
            }
            return out.toString(encoding);	
		} catch (Exception e) {
			return null;
		}       
    }

	// 图片转化成base64字符串
	public static String GetImageStr(String imgFile) {// 将图片文件转化为字节数组字符串，并对其进行Base64编码处理
		try {
			InputStream in = null;
			byte[] data = null;
			// 读取图片字节数组
			try {
				in = new FileInputStream(imgFile);
				data = new byte[in.available()];
				in.read(data);
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			// 对字节数组Base64编码
			BASE64Encoder encoder = new BASE64Encoder();
			return encoder.encode(data);// 返回Base64编码过的字节数组字符串
		}catch (Exception e) {
			return "";
		}
	}

	//base64字符转为图片
	public static boolean GenerateImage(String imgStr, String imgFilePath) {// 对字节数组字符串进行Base64解码并生成图片
		if (imgStr == null) // 图像数据为空
			return false;
		BASE64Decoder decoder = new BASE64Decoder();
		try {
			// Base64解码
			byte[] bytes = decoder.decodeBuffer(imgStr);
			for (int i = 0; i < bytes.length; ++i) {
				if (bytes[i] < 0) {// 调整异常数据
					bytes[i] += 256;
				}
			}
			// 生成jpeg图片
//			OutputStream out = new FileOutputStream(imgFilePath);
//			out.write(bytes);
//			out.flush();
//			out.close();			
			FileUtils.writeByteArrayToFile(new File(imgFilePath), bytes);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	//base64字符转为图片字节
	public static byte[] GenerateImage(String imgStr) {// 对字节数组字符串进行Base64解码并生成图片
		BASE64Decoder decoder = new BASE64Decoder();
		try {
			// Base64解码
			byte[] bytes = decoder.decodeBuffer(imgStr);
			for (int i = 0; i < bytes.length; ++i) {
				if (bytes[i] < 0) {// 调整异常数据
					bytes[i] += 256;
				}
			}
			return bytes;
		} catch (Exception e) {
			return null;
		}
	}

	public static void compressZip(String srcPathName,String zipname) {
		File zipf=new File(srcPathName+"/"+zipname+".zip");
		if(!zipf.exists()) {
			File srcdir = new File(srcPathName);
			if (!srcdir.exists())
				throw new RuntimeException(srcPathName + "不存在！");

			Project prj = new Project();
			Zip zip = new Zip();
			zip.setProject(prj);
			zip.setDestFile(zipf);
			zip.setZip64Mode(Zip.Zip64ModeAttribute.AS_NEEDED);
			FileSet fileSet = new FileSet();
			fileSet.setProject(prj);
			fileSet.setDir(srcdir);
			//fileSet.setIncludes("**/*.java"); 包括哪些文件或文件夹 eg:zip.setIncludes("*.java");
			//fileSet.setExcludes(...); 排除哪些文件或文件夹
			zip.addFileset(fileSet);
			zip.execute();
		}
	}

}