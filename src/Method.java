

import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.swing.JFrame;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class Method {
	public static String replaceHostPort(String requestData,String Host,int port)
	{
		String[] requestBuffer = requestData.split("\r\n");
		String returnString = "";
		int requestBufferLength = requestBuffer.length;
		for(int i=0;i<requestBufferLength;i++)
		{
			requestBuffer[i] = requestBuffer[i]+"\r\n";
			String tmp = requestBuffer[i];
			if(tmp.startsWith("Host"))
			{
				//System.out.println(tmp);
				String HostPort = "Host: "+Host+":"+port+"\r\n";
				requestBuffer[i] = HostPort;
				
			}
		}	
		returnString = stringsToString(requestBuffer)+"\r\n";
		return returnString;
	}
	
	public static String stringsToString(String[] strings)
	{
		if(strings!=null)
		{
			String returnStr="";
			int StrsLength = strings.length;
			for(int i=0;i<StrsLength;i++)
			{
				returnStr = returnStr+strings[i];
			}
			//System.out.println(returnStr);
			
			return returnStr;
		}
		
		return null;
		
	}
		
	public static void copyFile(String oldPath, String newPath)
	{
		try
		{
			int bytesum = 0;
			int byteread = 0;
			File oldfile = new File(oldPath);
			if (oldfile.exists())
			{
				InputStream inStream = new FileInputStream(oldPath);
				FileOutputStream fs = new FileOutputStream(newPath);
				byte[] buffer = new byte[1444];

				while ((byteread = inStream.read(buffer)) != -1)
				{
					bytesum += byteread;
					//System.out.println(bytesum);
					fs.write(buffer, 0, byteread);
				}
				inStream.close();
				fs.close();
			}

		}
		catch (Exception e)
		{
			//System.out.println("复制单个文件操作出错");
			e.printStackTrace();
		}
	}
	
	static void center(JFrame aFrame)
	{
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		Point centerPoint = ge.getCenterPoint();
		Rectangle bounds = ge.getMaximumWindowBounds();
		int w = Math.min(aFrame.getWidth(), bounds.width);
		int h = Math.min(aFrame.getHeight(), bounds.height);
		int x = centerPoint.x - w / 2;
		int y = centerPoint.y - h / 2;
		aFrame.setBounds(x, y, w, h);
		if ((w == bounds.width) && (h == bounds.height)) 
		{
			aFrame.setExtendedState(6);
		}
		aFrame.validate();
	}
	
	public static String bytesToString(byte[] bytes) 
	{
		if(bytes!=null)
		{
			try
			{
				int length = bytes.length;
				String returnStr = "";
				for (int i = 0; i < length; i++)
				{
					returnStr = returnStr + (char)bytes[i];
				}
				return returnStr;
			}
			catch(Exception e)
			{
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}

	public static char asciiToChar(int i)
  	{
		return (char)i;
  	}

	public static String intsToCharsToString(int[] ascii)
	{
		int length = ascii.length;
		int[] asciiTmp = ascii;
		String tmp = "";
		for (int i = 0; i < length; i++)
		{
			tmp = tmp + asciiToChar(asciiTmp[i]);
		}
		return tmp;
	}
	
	private static byte charToByte(char c)
	{
		return (byte)"0123456789ABCDEF".indexOf(c);
	}

	public static byte[] HexStringTobytes(String hexString)
	{
		if ((hexString == null) || (hexString.equals("")))
		{
			return null;
		}
		hexString = hexString.toUpperCase();
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) 
		{
			int pos = i * 2;
			d[i] = ((byte)(charToByte(hexChars[pos]) << 4 | charToByte(hexChars[(pos + 1)])));
		}

		return d;
	}
	
	public static boolean writeHexStringsToFile(String fileTmp, String[] StrsTmp)
	{
		int StrsLength = StrsTmp.length;
		try
		{
			FileWriter writer = new FileWriter(fileTmp);
			int i = 0; for (int j = 1; i < StrsLength; j++)
			{
				writer.write(StrsTmp[i] + "-");
				if (j % 16 == 0)
				{
					writer.write("\r\n");
				}
				i++;
			}

			writer.flush();
			writer.close();
			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}
	
	public static String[] bytesToHexStrings(byte[] b)
	{
		String[] StrTmp = new String[b.length];
		for (int i = 0; i < b.length; i++)
		{
			String hex = Integer.toHexString(b[i] & 0xFF);
			if (hex.length() == 1)
			{
				hex = '0' + hex;
			}
			StrTmp[i] = hex;
		}
		return StrTmp;
	}

	public static String[] getHTTPHeadersParams(String httpHeader)
	{
		if ((httpHeader == null) || (httpHeader.equals(null)) || (httpHeader.length() == 0))
			return null;
		String[] StrTmp = httpHeader.split("\r\n");
		int headerLength = StrTmp.length;

		if (headerLength > 1)
		{
			//System.out.println("-----------------------" + headerLength);
			
				String HeaderStrTmp = "";
				int paramCount = 0;
				for (int i = 0; i < headerLength; i++)
				{
					if (StrTmp[i].length() == 0)
					{
						paramCount = i;

						break;
					}
				
					paramCount = headerLength;
				}

				String[] returnStrs = new String[paramCount * 2];
				int j = StrTmp[0].indexOf(" ");

				try
				{
					returnStrs[0] = StrTmp[0].substring(0, j);
					returnStrs[1] = StrTmp[0].substring(j + 1);

					int site = 0;

					for (int i = 1; i < paramCount; i++)
					{
						if (StrTmp[i].length() <= 2)
						{
							//System.out.println("IIIIIIII"+i);
							break;
						}
						site = StrTmp[i].indexOf(":");

						//System.out.println("StrTmp[i]"+StrTmp[i]+"StrTmp[i]"+StrTmp[i].length());
						
						returnStrs[(i * 2)] = StrTmp[i].substring(0, site);
						returnStrs[(i * 2 + 1)] = StrTmp[i].substring(site + 1);
					}
				
					return StringsToStrings(returnStrs);
				}
				catch(Exception e)
				{
					e.printStackTrace();
					return null;
				}

			
		}

		return null;
	}
	
	public static String[] StringsToStrings(String[] resource)
	{
	
		List<String> tmp = new ArrayList<String>();
		int count = resource.length;
		for(int i=0;i<count;i++)
		{
			if(resource[i]!=null&&!resource[i].equals(null))
			{
				tmp.add(resource[i]);
			}
		}
		
		int returncount = tmp.size();
		String[] returnStrings = new String[returncount];
		for(int i=0;i<returncount;i++)
		{
			returnStrings[i] = tmp.get(i);
		}
		
		return returnStrings;
	}
	
	
	public static String addHeader(String requestData, String name, String value)
	{
		String[] requestDataStrings = requestData.split("\r\n");
		int length = requestDataStrings.length;
		String[] tmp = new String[length + 1];
		int i = 0; for (int j = 0; i < length; j++)
		{
			if (requestDataStrings[i].length() == 0)
			{
				tmp[j] = (name + ": " + value + "\r\n");
				j++;
				tmp[j] = (requestDataStrings[i] + "\r\n");
			}
			else
			{
				tmp[j] = (requestDataStrings[i] + "\r\n");
			}
			i++;
		}

		return stringsToString(tmp);
	}

	public static byte[] getHttPResponseData(String host, int port, String RequestData)
	{
		//System.out.print(RequestData);
		//System.out.println();
		//System.out.println();
		
		int count = RequestData.split("\r\n").length;
		String[] tmpstrs = RequestData.split("\r\n");
		//System.out.println(count);
		for(int i=0;i<count;i++)
		{
			//System.out.println(RequestData.split("\r\n")[i]);
			//System.out.println("-------------++++++++++++++------------");
		}
		
		byte[] bytests = RequestData.getBytes();
		for(int i=0;i<bytests.length;i++)
		{
			//System.out.println(bytests[i]);
		}
		
		//System.exit(1);
		//System.out.print("--");
		try
		{
			String ResponseData = "";

			Socket s = new Socket(host, port);
			s.setSoTimeout(3000);
			OutputStreamWriter osw = new OutputStreamWriter(s.getOutputStream());
			
			osw.write(RequestData);
			osw.flush();
			InputStream inputStr = s.getInputStream();
			
			
			
            
            
            
            
            
            
            
            
            
            
            
			
			byte[] tmp = readInputStream(inputStr);

			s.close();
			return tmp;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return "wahaha,访问不了哦--By Naih".getBytes();
	}
	
	public static int getHttPResponseData(boolean HTTPSSocket, String host, int port, String RequestData, OutputStream Out, String[] proxyHistoryItem)
	{
		int HttpResponseHeaderStatus = -1;
		try
		{
			if (HTTPSSocket)
			{
				SSLSocketFactory factory = (SSLSocketFactory)SSLSocketFactory.getDefault();
				SSLSocket s = (SSLSocket)factory.createSocket(host, port);
				s.setSoTimeout(3000);
				OutputStreamWriter osw = new OutputStreamWriter(s.getOutputStream());

				osw.write(RequestData);
				osw.flush();

				InputStream inputStr = s.getInputStream();
				HttpResponseHeaderStatus = readInputStream(inputStr, Out, proxyHistoryItem);

				s.close();
			}
			else
			{
				
				try
				{
					Socket ss  = new Socket(host, port);
					ss.close();
					
					
					Socket s=null;
					try
					{
						s = new Socket(host, port);
						s.setSoTimeout(3000);
						OutputStreamWriter osw = new OutputStreamWriter(s.getOutputStream());

						
						//System.out.println(RequestData);
						
						
						osw.write(RequestData);
						
						osw.flush();
				
						
						InputStream inputStr = s.getInputStream();
					
						HttpResponseHeaderStatus = readInputStream(inputStr, Out, proxyHistoryItem);

						s.close();
					}
					catch (Exception ee)
					{
						Out.close();
						
						s.close();
						
						ee.printStackTrace();
						//System.out.println(host+":"+port);
						//System.out.println(RequestData);
					}
					
					
					
				}
				catch(Exception es)
				{
					//es.printStackTrace();
					Out.close();
				}
				
				

			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
			//System.out.println(host+":"+port);
			//System.out.println(RequestData);
		}
		
		return HttpResponseHeaderStatus;
	}

	public static String[] readFileToHexStrings(String fileTmp)
	{
		FileReader fileReader = null;
		BufferedReader bufferedReader = null;
		String bufferStr = "";
		String StrTmp = "";
		try
		{
			fileReader = new FileReader(fileTmp);
			bufferedReader = new BufferedReader(fileReader);
			while ((bufferStr = bufferedReader.readLine()) != null)
			{
				StrTmp = StrTmp + bufferStr;
			}

			bufferedReader.close();
			fileReader.close();
			return StrTmp.split("-");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
	}

	public static String readLine(InputStream is, int contentLe) throws IOException
	{
		ArrayList lineByteList = new ArrayList();
    
		int total = 0;
		if (contentLe != 0) 
		{
			do 
			{
				byte readByte = (byte)is.read();
				lineByteList.add(Byte.valueOf(readByte));
				total++;
			}
			while (total < contentLe);
		} 
		else 
		{
			byte readByte;
			do
			{
				readByte = (byte)is.read();
				if (is == null)
				{
					if (-1 != readByte)
						lineByteList.add(Byte.valueOf(readByte)); 
				}
			}
			while (readByte != 10);
		}

		byte[] tmpByteArr = new byte[lineByteList.size()];
		for (int i = 0; i < lineByteList.size(); i++) 
		{
			tmpByteArr[i] = ((Byte)lineByteList.get(i)).byteValue();
		}
		lineByteList.clear();

		return new String(tmpByteArr);
	}

	public static byte[] readInputStream(InputStream inStream) throws Exception 
	{
		/* --------------------------------
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();

		byte[] buffer = new byte[1024];

		int len = 0;

		while ((len = inStream.read(buffer)) != -1)
		{
			outStream.write(buffer, 0, len);
		}

		inStream.close();

		return outStream.toByteArray();
		----------------------------------------------*/
		
		
		
		
		String ResponseheaderStr = "";
		String ResponseDataStr = "";
		byte[] buffer = null;

		//System.out.println("proxyHistoryItemproxyHistoryItemproxyHistoryItemproxyHistoryItemproxyHistoryItem"+proxyHistoryItem[0]);
		int len = 0;
		int HttpResponseHeaderStatus = -1;
		try 
		{
			int judgebyte;
			do 
			{
				do 
				{
					do 
					{ 
						do 
						{ 
							judgebyte = inStream.read();
							ResponseheaderStr = ResponseheaderStr + (char)judgebyte; 
						}
						while (judgebyte != 13);

						judgebyte = inStream.read();
						ResponseheaderStr = ResponseheaderStr + (char)judgebyte;
					}
					while (judgebyte != 10);

					judgebyte = inStream.read();
					ResponseheaderStr = ResponseheaderStr + (char)judgebyte;
				}
				while (judgebyte != 13);

				judgebyte = inStream.read();
				ResponseheaderStr = ResponseheaderStr + (char)judgebyte;
			}
			while (judgebyte != 10);

			//Out.write(ResponseheaderStr.getBytes());
			HttpResponseHeaderStatus = getHttpResponseHeaderStatus(ResponseheaderStr);

			//System.out.println(ResponseheaderStr);
			int contentLength = getHttpResponseHeaderContentLength(ResponseheaderStr);
			boolean  ischunked =getHttpResonseHeaderTransferEncoding(ResponseheaderStr);//Transfer-Encoding: chunked
			int bufferLength = 1024;
			buffer = new byte[bufferLength];

			int bufferLengthReadNum = contentLength / bufferLength;

			int bufferReadSome = contentLength % bufferLength;

			if (contentLength < 0)
			{
				
				
				
				if(HttpResponseHeaderStatus==304)
				{
					
				}
				else
				{
					
					if(ischunked)
					{
						//method 1 ++++++++++++++++++++++
						
						//int len;
						while ((len = inStream.read(buffer)) != -1)
						{
							//Out.write(buffer, 0, len);
							ResponseDataStr = ResponseDataStr + Method.bytesToString(buffer);
						}
						//Out.write(buffer, 0, len);
						//ResponseDataStr = ResponseDataStr + Method.bytesToString(buffer);
						//method 1 ---------------------
						
					}
					else
					{
						int tmp;
						while ((tmp = inStream.read()) != -1)
						{
							//System.out.println("-"+tmp+"-");
							//System.out.println("-");
							if(tmp==13)
							{
								//System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
								//Out.write(tmp);
								ResponseDataStr = ResponseDataStr + (char)(tmp);
								tmp = inStream.read();
								//System.out.println(tmp);
								if(tmp==10)
								{
									//Out.write(tmp);
									ResponseDataStr = ResponseDataStr + (char)(tmp);
									tmp = inStream.read();
									//System.out.println(tmp);
									if(tmp==13)
									{
										//Out.write(tmp);
										ResponseDataStr = ResponseDataStr + (char)(tmp);
										tmp = inStream.read();
										//System.out.println(tmp);
										if(tmp==10)
										{
										//	Out.write(tmp);
											ResponseDataStr = ResponseDataStr + (char)(tmp);
											//System.out.println(ResponseheaderStr);
											//System.out.println("breadk");
											
											inStream.close();
											//System.out.println(ResponseDataStr);
											//Out.flush();
										//	Out.close();
											
											break;
											
											
										}
										else
										{
											//Out.write(tmp);
											ResponseDataStr = ResponseDataStr + (char)(tmp);
										}
										
									}
									else
									{
										//Out.write(tmp);
										ResponseDataStr = ResponseDataStr + (char)(tmp);
									}
								}
								
								else
								{
								//	Out.write(tmp);
									ResponseDataStr = ResponseDataStr + (char)(tmp);
								}
							}
							else
							{
							//	Out.write(tmp);
								ResponseDataStr = ResponseDataStr + (char)(tmp);
							}
							//Out.write(buffer, 0, len);
							//ResponseDataStr = ResponseDataStr + Method.bytesToString(buffer);
						}
						
						//method 2 ---------------------
					}
					
					//System.out.println(ResponseheaderStr);
					
					
					
					
					
					//method 2 ++++++++++++++++++++++
					
				}
				
				

			}
			else if (contentLength > 0)
			{
				for (int i = 0; i < bufferLengthReadNum; i++)
				{
					inStream.read(buffer);
					//Out.write(buffer, 0, bufferLength);
					ResponseDataStr = ResponseDataStr + Method.bytesToString(buffer);
				}

				byte[] bufferSome = new byte[bufferReadSome];

				inStream.read(bufferSome);
				//Out.write(bufferSome, 0, bufferReadSome);
				ResponseDataStr = ResponseDataStr + Method.bytesToString(bufferSome);
			}

			inStream.close();
			//System.out.println(ResponseDataStr);
			//Out.flush();
			//Out.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			//System.out.println("proxyHistoryItemproxyHistoryItemproxyHistoryItemproxyHistoryItem"+proxyHistoryItem[0]);
			//System.out.println(ResponseDataStr);
			//System.out.println(len+"proxyHist+"+"||"+buffer[0]+buffer[1]+"||"+"oryItem[1]"+proxyHistoryItem[0]);
			//System.exit(1);
			try
			{
				//Out.write(buffer, 0, len);
				inStream.close();
				//Out.flush();
				//Out.close();
				
				
				
			}
			catch (Exception localException1)
			{
				localException1.printStackTrace();
			}

		}
		
		
		return (ResponseheaderStr+ResponseDataStr).getBytes();
		
		
		
		
		
		
		
		
		
	}

	public static String addSignRegex(String tmp)
	{
		return "\\$" + tmp + "\\$";
 	}

	public static String replaceCookie(String oldCookie, String newCookie)
	{
		String[] oldCookies = oldCookie.split(";");
		String[] newCookies = newCookie.split(";");

		String Tmp = "";

		for (int j = 0; j < newCookies.length; j++)
		{
			if (newCookies[j].split("=").length > 1)
			{
				newCookies[j] = (newCookies[j] + ";");
				for (int i = 0; i < oldCookies.length; i++)
				{
					oldCookies[i] = (oldCookies[i] + ";");
					if (oldCookies[i].split("=")[0].toLowerCase().trim().equals(newCookies[j].split("=")[0].toLowerCase().trim()))
					{
						oldCookies[i] = newCookies[j];
					}
				}

			}

		}

		return stringsToString(oldCookies) + stringsToString(newCookies);
	}

	public static int readInputStream(InputStream inStream, OutputStream Out, String[] proxyHistoryItem)
	{
		String ResponseheaderStr = "";
		String ResponseDataStr = "";
		byte[] buffer = null;

		//System.out.println("proxyHistoryItemproxyHistoryItemproxyHistoryItemproxyHistoryItemproxyHistoryItem"+proxyHistoryItem[0]);
		int len = 0;
		int HttpResponseHeaderStatus = -1;
		try 
		{
			int judgebyte;
			do 
			{
				do 
				{
					do 
					{ 
						do 
						{ 
							judgebyte = inStream.read();
							ResponseheaderStr = ResponseheaderStr + (char)judgebyte; 
						}
						while (judgebyte != 13);

						judgebyte = inStream.read();
						ResponseheaderStr = ResponseheaderStr + (char)judgebyte;
					}
					while (judgebyte != 10);

					judgebyte = inStream.read();
					ResponseheaderStr = ResponseheaderStr + (char)judgebyte;
				}
				while (judgebyte != 13);

				judgebyte = inStream.read();
				ResponseheaderStr = ResponseheaderStr + (char)judgebyte;
			}
			while (judgebyte != 10);

			Out.write(ResponseheaderStr.getBytes());
			HttpResponseHeaderStatus = getHttpResponseHeaderStatus(ResponseheaderStr);

			//System.out.println(ResponseheaderStr);
			int contentLength = getHttpResponseHeaderContentLength(ResponseheaderStr);
			boolean  ischunked =getHttpResonseHeaderTransferEncoding(ResponseheaderStr);//Transfer-Encoding: chunked
			int bufferLength = 1024;
			buffer = new byte[bufferLength];

			int bufferLengthReadNum = contentLength / bufferLength;

			int bufferReadSome = contentLength % bufferLength;

			if (contentLength < 0)
			{
				
				
				
				if(HttpResponseHeaderStatus==304)
				{
					
				}
				else
				{
					
					if(ischunked)
					{
						//method 1 ++++++++++++++++++++++
						
						//int len;
						while ((len = inStream.read(buffer)) != -1)
						{
							Out.write(buffer, 0, len);
							ResponseDataStr = ResponseDataStr + Method.bytesToString(buffer);
						}
						//Out.write(buffer, 0, len);
						//ResponseDataStr = ResponseDataStr + Method.bytesToString(buffer);
						//method 1 ---------------------
						
					}
					else
					{
						int tmp;
						while ((tmp = inStream.read()) != -1)
						{
							//System.out.println("-"+tmp+"-");
							//System.out.println("-");
							if(tmp==13)
							{
								//System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
								Out.write(tmp);
								ResponseDataStr = ResponseDataStr + (char)(tmp);
								tmp = inStream.read();
								//System.out.println(tmp);
								if(tmp==10)
								{
									Out.write(tmp);
									ResponseDataStr = ResponseDataStr + (char)(tmp);
									tmp = inStream.read();
									//System.out.println(tmp);
									if(tmp==13)
									{
										Out.write(tmp);
										ResponseDataStr = ResponseDataStr + (char)(tmp);
										tmp = inStream.read();
										//System.out.println(tmp);
										if(tmp==10)
										{
											Out.write(tmp);
											ResponseDataStr = ResponseDataStr + (char)(tmp);
											//System.out.println(ResponseheaderStr);
											//System.out.println("breadk");
											
											inStream.close();
											//System.out.println(ResponseDataStr);
											Out.flush();
											Out.close();
											
											break;
											
											
										}
										else
										{
											Out.write(tmp);
											ResponseDataStr = ResponseDataStr + (char)(tmp);
										}
										
									}
									else
									{
										Out.write(tmp);
										ResponseDataStr = ResponseDataStr + (char)(tmp);
									}
								}
								
								else
								{
									Out.write(tmp);
									ResponseDataStr = ResponseDataStr + (char)(tmp);
								}
							}
							else
							{
								Out.write(tmp);
								ResponseDataStr = ResponseDataStr + (char)(tmp);
							}
							//Out.write(buffer, 0, len);
							//ResponseDataStr = ResponseDataStr + Method.bytesToString(buffer);
						}
						
						//method 2 ---------------------
					}
					
					//System.out.println(ResponseheaderStr);
					
					
					
					
					
					//method 2 ++++++++++++++++++++++
					
				}
				
				

			}
			else if (contentLength > 0)
			{
				for (int i = 0; i < bufferLengthReadNum; i++)
				{
					inStream.read(buffer);
					Out.write(buffer, 0, bufferLength);
					ResponseDataStr = ResponseDataStr + Method.bytesToString(buffer);
				}

				byte[] bufferSome = new byte[bufferReadSome];

				inStream.read(bufferSome);
				Out.write(bufferSome, 0, bufferReadSome);
				ResponseDataStr = ResponseDataStr + Method.bytesToString(bufferSome);
			}

			inStream.close();
			//System.out.println(ResponseDataStr);
			Out.flush();
			Out.close();
		}
		catch (Exception e)
		{
			//e.printStackTrace();
			//System.out.println("proxyHistoryItemproxyHistoryItemproxyHistoryItemproxyHistoryItem"+proxyHistoryItem[0]);
			//System.out.println(ResponseDataStr);
			//System.out.println(len+"proxyHist+"+"||"+buffer[0]+buffer[1]+"||"+"oryItem[1]"+proxyHistoryItem[0]);
			//System.exit(1);
			try
			{
				//Out.write(buffer, 0, len);
				inStream.close();
				Out.flush();
				Out.close();
				
				
				
			}
			catch (Exception localException1)
			{
				localException1.printStackTrace();
			}

		}

		//System.out.println(ResponseDataStr);
		proxyHistoryItem[6] = (ResponseheaderStr + ResponseDataStr);

	    return HttpResponseHeaderStatus;
	}

	public static String getImgFromURL(String url, String saveFileName)
	{
		try
		{
			URL imgURL = new URL(url);
			DataInputStream dis = new DataInputStream(imgURL.openStream());
			File tmpFile = new File(saveFileName);
			FileOutputStream fos = new FileOutputStream(tmpFile);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = dis.read(buffer)) > 0)
			{
        
				fos.write(buffer, 0, length);
			}
			dis.close();
			fos.close();
			return tmpFile.getAbsolutePath();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public static int getHttpResponseHeaderStatus(String strTmp)
	{
		String[] Strs = strTmp.split("\r\n");

		int returnInt = -1;
		try
		{
			returnInt = Integer.valueOf(Strs[0].split(" ")[1]).intValue();
			if (returnInt > 0)
				return returnInt;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return returnInt;
		}
		return returnInt;
	}

	public static int getHttpResponseHeaderContentLength(byte[] bytesTmp)
	{
		String[] Strs = Method.bytesToString(bytesTmp).split("\r\n");
		int length = Strs.length;
		int returnInt = -1;
		for (int i = 0; i < length; i++)
		{
			if (Strs[i].length() == 0)
				break;
			if (Strs[i].startsWith("Content-Length:"))
			{
				returnInt = Integer.valueOf(Strs[i].split("Content-Length:")[1].trim()).intValue();
			}
		}

		return returnInt;
	}
	
	public static boolean getHttpResonseHeaderTransferEncoding(String strTmp)
	{
		String[] Strs = strTmp.split("\r\n");
		int length = Strs.length;
		String returnStr ="";
		for (int i = 0; i < length; i++)
		{
			if (Strs[i].length() == 0)
				break;
			if (Strs[i].startsWith("Transfer-Encoding:"))
			{
				returnStr = Strs[i].split("Transfer-Encoding:")[1].trim();
				if(returnStr.equals("chunked"))
				{
					//System.out.println("OKOKOK");
					return true;
				}
			}

		}
		return false;
	}

	public static int getHttpResponseHeaderContentLength(String strTmp)
	{
		String[] Strs = strTmp.split("\r\n");
		int length = Strs.length;
		int returnInt = -1;
		for (int i = 0; i < length; i++)
		{
			if (Strs[i].length() == 0)
				break;
			if (Strs[i].startsWith("Content-Length:"))
			{
				returnInt = Integer.valueOf(Strs[i].split("Content-Length:")[1].trim()).intValue();
			}

		}

		return returnInt;
	}

	public static int getHTTPDataHeaderLength(byte[] data)
	{
		String[] tmp = Method.bytesToString(data).split("\r\n");
		int i = 0;
		String StrTmp = "";
		while ((i < tmp.length) && (tmp[i].length() > 0))
		{
			StrTmp = StrTmp + tmp[i];
			i++;
		}
		return StrTmp.getBytes().length;
	}

	public static String addSign(String tmp)
	{
		return "$" + tmp + "$";
	}
	
	public static String removeSign(String tmp)
	{
		int tmpLength = tmp.length();
		if (tmpLength > 2)
		{
			return tmp.substring(1, tmpLength - 1);
		}
		return null;
	}

	public static String getBase64(String tmp)
	{
		if (tmp == null)
		{
			return null;
		}

		return new BASE64Encoder().encode(tmp.getBytes()).replaceAll("\r\n", "");
	}

	public static String getFromBase64(String tmp)
	{
		if (tmp == null)
			return null;
		BASE64Decoder decoder = new BASE64Decoder();
		try
		{
			byte[] b = decoder.decodeBuffer(tmp);
			return new String(b);
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public static String addPreviousURL(String request,String previousURL)
	{
		String[] tmp = request.split("\r\n");
		
		String headerFirstLine = tmp[0];
		String[] headerFirstLines = headerFirstLine.split(" ");
		headerFirstLines[1] = "/"+previousURL+headerFirstLines[1];
		tmp[0] = headerFirstLines[0]+" "+headerFirstLines[1]+" "+headerFirstLines[2];
		
		//System.out.println(tmp[0]);
		int count = tmp.length;
		String retrunStr = "";
		for(int i=0;i<count;i++)
		{
			retrunStr =retrunStr+ tmp[i]+"\r\n";
		}
		retrunStr = retrunStr+"\r\n"+"\r\n";
		//System.out.println("tihuan   host port");
		//System.out.print(retrunStr);
		
		return retrunStr;
	}
	
	
	
	
	
	
	public static boolean deleteDirectory(String sPath) {  
	    //如果sPath不以文件分隔符结尾，自动添加文件分隔符  
	    if (!sPath.endsWith(File.separator)) {  
	        sPath = sPath + File.separator;  
	    }  
	    File dirFile = new File(sPath);  
	    //如果dir对应的文件不存在，或者不是一个目录，则退出  
	    if (!dirFile.exists() || !dirFile.isDirectory()) {  
	        return false;  
	    }  
	    boolean flag = true;  
	    //删除文件夹下的所有文件(包括子目录)  
	    File[] files = dirFile.listFiles();  
	    for (int i = 0; i < files.length; i++) {  
	        //删除子文件  
	        if (files[i].isFile()) { 
	        	//System.out.println(files[i]);
	            flag = deleteFile(files[i].getAbsolutePath());  
	            if (!flag) break;  
	        } //删除子目录  
	        else {  
	            flag = deleteDirectory(files[i].getAbsolutePath());  
	            if (!flag) break;  
	        }  
	    }  
	   // System.out.println("this"+flag);
	    if (!flag) return false;  
	    //删除当前目录  
	    if (dirFile.delete()) {  
	        return true;  
	    } else {  
	        return false;  
	    }  
	} 
	
	public static boolean deleteFile(String sPath) {  
	    boolean flag = false;  
	    File file = new File(sPath);  
	    // 路径为文件且不为空则进行删除  
	    if (file.isFile() && file.exists()) {  
	    	try
	    	{
	    		//System.out.println(file.getName());
	    		boolean success = file.delete();  
	    		//System.out.println(success);
	    	}
	    	catch(Exception e)
	    	{
	    		e.printStackTrace();
	    	}
	        flag = true;  
	    }  
	    return flag;  
	} 
	
	
	public static String replace10To1310(String tmp)
	{
		//System.out.println("[[[[[[[[[[[[[[[[[[[[[[[[[[["+tmp);
		tmp = tmp.replaceAll("\n", "\r\n");
		//tmp = tmp.replaceAll("\n", "\r\n");
		//tmp = tmp.replaceAll("\r\r\n", "111");
		tmp = tmp.replaceAll("\r\r\n", "\r\n");
		tmp = tmp.replaceAll("\r\r\r\n", "\r\n");
		tmp = tmp.replaceAll("\r\r\n", "\r\n");
		//tmp = tmp.replaceAll("\r\n\n", "\r\n");
		return tmp;
	}
	
	
}
