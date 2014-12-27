
import com.sun.jna.Library;
import com.sun.jna.Native;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.Checksum;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

public class UUAPI
{
	public static String USERNAME ;
	public static String PASSWORD ;
	public static String DLLPATH ;

	public static int SOFTID ;
	public static String SOFTKEY ;
	public static String DLLVerifyKey ;
	public static boolean checkStatus;

	/*
	public static String USERNAME = "naihtools";
	public static String PASSWORD = "naihtools";
	public static String DLLPATH = "lib\\UUWiseHelper";

	public static int SOFTID = 101998;
	public static String SOFTKEY = "c12249f8a4b74a9ca66add8870fb7816";
	public static String DLLVerifyKey = "4F298F5A-B725-4BA9-A390-191C1E33FBD5";
	public static boolean checkStatus = false;
	
	*/
	
	public static int getScore()
	{
		return UUAPI.UUDLL.INSTANCE.uu_getScoreA(USERNAME, PASSWORD);
	}

	public static String[] easyDecaptcha(String picPath, int codeType) throws IOException
	{
		if (!checkStatus)
		{
			String[] rs = { "-19004", "API校验失败,或未校验" };
			return rs;
		}

		byte[] resultBtye = new byte[100];
		int codeID = UUAPI.UUDLL.INSTANCE.uu_easyRecognizeFileA(SOFTID, SOFTKEY, USERNAME, PASSWORD, picPath, codeType, resultBtye);
		String resultResult = null;
		try 
		{
			resultResult = new String(resultBtye, "GB2312");
		}
		catch (UnsupportedEncodingException e) 
		{
			e.printStackTrace();
		}
		resultResult = resultResult.trim();

		String[] rs = { String.valueOf(codeID), checkResult(resultResult, codeID) };
		return rs;
	}

	public static boolean checkAPI()
			throws IOException
			{
		
		
		
		
		
		
		try
		{
			File tmpFile = new File("sysconfig.txt");
			InputStreamReader tmpFileInputSteamReadTmp = new InputStreamReader(new FileInputStream(tmpFile));
			BufferedReader tmpFileBufferedReader = new BufferedReader(tmpFileInputSteamReadTmp);
			String StringLineTmp="";
			while((StringLineTmp=tmpFileBufferedReader.readLine())!=null)
			{
				//StringLineTmp = StringLineTmp.toUpperCase();
				if(StringLineTmp.startsWith("USERNAME:"))
				{
					System.out.println();
					USERNAME= (StringLineTmp.split("USERNAME:")[1]);
				}
				else if(StringLineTmp.startsWith("PASSWORD:"))
				{
					System.out.println(StringLineTmp);
					PASSWORD = (StringLineTmp.split("PASSWORD:")[1]);
				}
				else if(StringLineTmp.startsWith("DLLPATH:"))
				{
					System.out.println(StringLineTmp);
					DLLPATH = (StringLineTmp.split("DLLPATH:")[1]);
				}
				else if(StringLineTmp.startsWith("SOFTID:"))
				{
					System.out.println(StringLineTmp);
					SOFTID = Integer.valueOf(StringLineTmp.split("SOFTID:")[1]);
				}
				else if(StringLineTmp.startsWith("SOFTKEY:"))
				{
					System.out.println(StringLineTmp);
					SOFTKEY = (StringLineTmp.split("SOFTKEY:")[1]);
				}
				else if(StringLineTmp.startsWith("DLLVerifyKey:"))
				{
					System.out.println(StringLineTmp);
					DLLVerifyKey = (StringLineTmp.split("DLLVerifyKey:")[1]);
				}
				else if(StringLineTmp.startsWith("CheckStatus:"))
				{
					System.out.println(StringLineTmp);
					String tmp = (StringLineTmp.split("CheckStatus:")[1]).toLowerCase();
					if(tmp.equals("false"))
					{
						checkStatus = false;
					}
					else if(tmp.equals("true"))
					{
						checkStatus = true;
					}
					else
					{
						checkStatus = false;
					}
				}
			}
			tmpFileInputSteamReadTmp.close();
			tmpFileBufferedReader.close();
		}
		catch(Exception e)
		{
			JOptionPane.showMessageDialog(null, "找不到sysconfig.txt文件", "Warning", 2);
		}
		
		
		
		
		
		
		
		
		
		
		
		
		
		String FILEMD5 = GetFileMD5(DLLPATH + ".dll");
		String FILECRC = doChecksum(DLLPATH + ".dll");
		String GUID = Md5(Long.toString(Math.round(Math.random() * 11111.0D + 99999.0D)));

		String okStatus = Md5(SOFTID + DLLVerifyKey.toUpperCase() + GUID.toUpperCase() + FILEMD5.toUpperCase() + FILECRC.toUpperCase());

		byte[] CheckResultBtye = new byte[512];

		UUAPI.UUDLL.INSTANCE.uu_CheckApiSignA(SOFTID, SOFTKEY.toUpperCase(), GUID.toUpperCase(), FILEMD5.toUpperCase(), FILECRC.toUpperCase(), CheckResultBtye);

		String checkResultResult = new String(CheckResultBtye, "UTF-8");
		checkResultResult = checkResultResult.trim();

		checkStatus = true;
		return checkResultResult.equals(okStatus);
	}

	public static String checkResult(String dllResult, int CodeID)
	{
		if (dllResult.indexOf("_") < 0)
		{
			return dllResult;
		}

		String[] re = dllResult.split("_");
		String verify = re[0];
		String code = re[1];
		String localMd5 = null;
		try
		{
			localMd5 = Md5(SOFTID + DLLVerifyKey + CodeID + code.toUpperCase()).toUpperCase();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		if (localMd5.equals(verify)) 
		{
			return code;
		}
		return "校验失败";
	}

	public static byte[] toByteArray(File imageFile) throws Exception
	{
		BufferedImage img = ImageIO.read(imageFile);
		ByteArrayOutputStream buf = new ByteArrayOutputStream((int)imageFile.length());
		try
		{
			ImageIO.write(img, "jpg", buf);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
		return buf.toByteArray();
	}

	public static byte[] toByteArrayFromFile(String imageFile) throws Exception
	{
		InputStream is = null;

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try
		{
			is = new FileInputStream(imageFile);
			byte[] b = new byte[1024];
			int n;
			while ((n = is.read(b)) != -1)
			{
				out.write(b, 0, n);
			}
		}
		catch (Exception e)
		{
			throw new Exception("System error,SendTimingMms.getBytesFromFile", e);
		}
		finally
		{
			if (is != null)
			{
				try
				{
					is.close();
				}
				catch (Exception localException1)
				{
				}
			}
		}
		return out.toByteArray();
	}

	public static String doChecksum(String fileName)
	{
		try
		{
			CheckedInputStream cis = null;
			try
			{
				cis = new CheckedInputStream(
					new FileInputStream(fileName), new CRC32());
			}
			catch (FileNotFoundException localFileNotFoundException)
			{
			}

			byte[] buf = new byte[''];
			while (cis.read(buf) >= 0);
			long checksum = cis.getChecksum().getValue();
			cis.close();

			return Integer.toHexString(new Long(checksum).intValue());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return null;
	}

	public static String GetFileMD5(String inputFile)
			throws IOException
    {
		int bufferSize = 262144;
		FileInputStream fileInputStream = null;
		DigestInputStream digestInputStream = null;
		try 
		{
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			fileInputStream = new FileInputStream(inputFile);
			digestInputStream = new DigestInputStream(fileInputStream, messageDigest);
			byte[] buffer = new byte[bufferSize];
			while (digestInputStream.read(buffer) > 0);
			messageDigest = digestInputStream.getMessageDigest();
			byte[] resultByteArray = messageDigest.digest();
			return byteArrayToHex(resultByteArray);
		} 
		catch (NoSuchAlgorithmException e)
		{
			return null;
		} 
		finally {
			try 
			{
				digestInputStream.close();
			}
			catch (Exception localException4)
			{
			}
			try
			{
				fileInputStream.close();
			}
			catch (Exception localException5)
			{
			}
		}
    }

	public static String Md5(String s) throws IOException 
	{
		try 
		{
			byte[] btInput = s.getBytes();
			MessageDigest mdInst = MessageDigest.getInstance("MD5");
			mdInst.update(btInput);
			byte[] md = mdInst.digest();
			return byteArrayToHex(md);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return null;
	}

	public static String byteArrayToHex(byte[] byteArray)
	{
		char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		char[] resultCharArray = new char[byteArray.length * 2];
		int index = 0;
		byte[] arrayOfByte = byteArray; int j = byteArray.length; 
		for (int i = 0; i < j; i++) 
		{ 
			byte b = arrayOfByte[i];
			resultCharArray[(index++)] = hexDigits[(b >>> 4 & 0xF)];
			resultCharArray[(index++)] = hexDigits[(b & 0xF)];
		}
		return new String(resultCharArray);
	}

	public static abstract interface UUDLL extends Library
	{
		public static final UUDLL INSTANCE = (UUDLL)Native.loadLibrary(UUAPI.DLLPATH, UUDLL.class);

		public abstract int uu_reportError(int paramInt);

		public abstract int uu_setTimeOut(int paramInt);

		public abstract int uu_loginA(String paramString1, String paramString2);

		public abstract int uu_recognizeByCodeTypeAndBytesA(byte[] paramArrayOfByte1, int paramInt1, int paramInt2, byte[] paramArrayOfByte2);

		public abstract void uu_getResultA(int paramInt, String paramString);

		public abstract int uu_getScoreA(String paramString1, String paramString2);

		public abstract int uu_easyRecognizeFileA(int paramInt1, String paramString1, String paramString2, String paramString3, String paramString4, int paramInt2, byte[] paramArrayOfByte);

		public abstract int uu_easyRecognizeBytesA(int paramInt1, String paramString1, String paramString2, String paramString3, byte[] paramArrayOfByte1, int paramInt2, int paramInt3, byte[] paramArrayOfByte2);

		public abstract void uu_CheckApiSignA(int paramInt, String paramString1, String paramString2, String paramString3, String paramString4, byte[] paramArrayOfByte);
	}
}