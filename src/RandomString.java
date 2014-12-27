
import java.io.PrintStream;

public class RandomString
{
  private final String splitStr = " ";

  private String getNumberString()
  {
    StringBuffer buf = new StringBuffer();
    for (int i = 0; i < 10; i++)
    {
      buf.append(String.valueOf(i));
      buf.append(" ");
    }
    return buf.toString();
  }

  private String getUppercase()
  {
    StringBuffer buf = new StringBuffer();
    for (int i = 0; i < 26; i++)
    {
      buf.append(String.valueOf((char)(65 + i)));
      buf.append(" ");
    }
    return buf.toString();
  }

  private String getLowercase()
  {
    StringBuffer buf = new StringBuffer();
    for (int i = 0; i < 26; i++)
    {
      buf.append(String.valueOf((char)(97 + i)));
      buf.append(" ");
    }
    return buf.toString();
  }

  private String getSpecialString()
  {
    String str = "~@#$%^&*()_+|\\=-`";
    StringBuffer buf = new StringBuffer();
    for (int i = 0; i < str.length(); i++)
    {
      buf.append(str.substring(i, i + 1));
      buf.append(" ");
    }
    return buf.toString();
  }

  private String getString(String type)
  {
    StringBuffer pstr = new StringBuffer();
    if (type.length() > 0)
    {
      if (type.indexOf('i') != -1)
        pstr.append(getNumberString());
      if (type.indexOf('l') != -1)
        pstr.append(getLowercase());
      if (type.indexOf('u') != -1)
        pstr.append(getUppercase());
      if (type.indexOf('s') != -1) {
        pstr.append(getSpecialString());
      }
    }
    return pstr.toString();
  }

  public String getRandomString(int length, String type)
  {
    String allStr = getString(type);
    String[] arrStr = allStr.split(" ");
    StringBuffer pstr = new StringBuffer();
    if (length > 0)
    {
      for (int i = 0; i < length; i++)
      {
        pstr.append(arrStr[new java.util.Random().nextInt(arrStr.length)]);
      }
    }
    return pstr.toString();
  }

  public static void main(String[] args)
  {
	  /*
    System.out.println("type=i:" + 
      new RandomString().getRandomString(10, "i"));
    System.out.println("type=il:" + 
      new RandomString().getRandomString(10, "il"));
    System.out.println("type=ilu:" + 
      new RandomString().getRandomString(10, "ilu"));
    System.out.println("type=ilus:" + 
      new RandomString().getRandomString(10, "ilus"));
      */
	  
	  System.out.println(getRandomStringFromDefineString("qweqrt234",5));
  }


 public static String getRandomStringFromDefineString(String Tmp,int length)
 {
	 int defineStringLength = Tmp.length();
	 String returnString="";
	 for(int i=0;i<length;i++)
	 {
		// System.out.println((int)(Math.random()*defineStringLength));
		 int tmpInt = (int)(Math.random()*defineStringLength);
		 //System.out.println(Tmp.substring(tmpInt,tmpInt+1));
		 returnString = returnString+Tmp.substring(tmpInt,tmpInt+1);
	 }
	 
	 return returnString;
 }
}