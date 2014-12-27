
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.MenuItem;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDesktopPane;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.MouseInputListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class Main
{
	private JFrame frame;        //主窗口
	public JTabbedPane JPtab;    //主窗口的 tab  用来添加 Porxy Editor Detection Settings Others
	private static int EditorStepNum = 0; //用来累计 Editor 模块 添加 Step 个数

	private boolean proxyState = false; //识别 代理的状态   true 代理开始http代理， false 关闭http代理
	private boolean InterceptState = false;// 识别 http 代理之后数据包截断的状态 ， true 代表开启截断   false 关闭截断
	public static ServerSocket server;//http 代理开启之后     作为接收浏览器传来的数据的   主服监听
	public Thread ProxyThread;      //  http 代理的主线程    start  为开启代理  
	private int listenPort = 8087; // http 代理的端口   

	public List<String[]> proxyHistoryList = new ArrayList();  // http 代理的 历史数据 list 

	public String[] proxyHistoryItem = new String[9];   //http 代理的 数据包的 Item 数组
	//0、累计 当前http代理的  个数   也是当前为 http代理的第几个  
	//1、  http 的  主机地址  host
	//2、 http 使用的方法  get post  header CONNECT
	//3、http 访问的 URL 连接 
	//4、http 请求之后 返回的http 状态值     response status 
	//5、http 访问 请求的 完整数据包  包括  header content
	//6、http 访问请求之后   返回的 http 完整数据包  包括  返回的 header 、 response
	//7、http 访问的 端口  默认为 80 端口
	//8、当http 截断之后  如果有修改http 数据包 ，存储在这个字段里 ，   即这个字段存储的是 http 截断之后修改的http 请求包
	
	
	

	public int proxyHistoryNum = 1; //累计 http 代理的个数  

	boolean portUserable = false; // 识别代理的端口是否可用  即为 代理的端口是否被占用
	public static JTable ProxyHistoryListTable;  //http 代理历史的  Table 表格   Proxy - Histor - 上面的表格
	public static JTextArea JPtabProxyHistoryDetailRequestRawText; //用来显示 http 代理历史的 http 请求报文 文本   Proxy History Request Raw
	public static JTextArea JPtabProxyHistoryDetailResponseRawText; //用来显示http 代理历史的http 返回的 数据包的文本  Proxy History Response Raw
	public static JTable JPtabProxyHistoryDetailRequestHeadersTable; //用来显示 htp 代理历史的http 请求报文中的 http header 的JTable 表格  Proxy History Request Headers
	public static JTable JPtabProxyHistoryDetailResponseHeadersTable;//用来显示http 代理历史的http 返回的数据包的 header 的Jtable 表格 Porxy History Response Headers
	public static JTable JPtabProxyHistoryDetailRequestHexTable; // 用来显示 http 代理历史的http 请求报文的  16进制数据 的Jtable 表格 Proxy History Request Hex
	public static JTable JPtabProxyHistoryDetailResponseHexTable;// 用来显示http 代理历史的http 返回的数据包的 16进制数据的Jtable 表格 Proxy History Response Hex
	public static JTextArea InterceptDaraRawTextArea;// 用来显示 http 代理截断时  当前 http 请求的文本  Proxy Intercept Raw
	
	
	//三个队列  分为代表 HTTPS 的请求（如果有的话）、当前代理的输出流对象、当前代理的http 数据 数组proxyHistoryItem
	//队列的实现是为了 让http 请求截断 先进先出   即为先请求的先响应操作
	public Queue<Integer> queueHTTPSSocket = new LinkedList();
	public Queue<OutputStream> queueOut = new LinkedList();
	public Queue<String[]> queueproxyHistoryItem = new LinkedList();
	
	//这三个分别对应上面三个队列的 Item
	public Integer queueHTTPSSocketTmp;
	public String[] queueproxyHistoryItemTmp;
	public OutputStream queueOutTmp;
	
	

	public JButton ForwardButton;//截断时  Proxy Intercept 的Forward 按钮
	public JButton DropButton; // 截断时 Proxy Intercept 的Drop 按钮
	public JButton SendToEditorButton; //截断时 Proxy Intercept 的Send To Editor 
	
	
	public static String InterceptShowString = "";//截断时 用来存储http 请求报文的字符串   这个字符串 是为了  在  Raw Headers Hex 三者之间的 中间过度 ，如果三方有一方修改数据时 ，同时“上传”到这边来， 相当于一个存储   
	public static JTable JPtabInterceptDataShowHeadersTable; // 用来显示 截断时  http 的 header 的JTable 表格  Proxy Intercept Headers
	public static JTable JPtabProxyInterceptDataHexTable; //用来显示 截断时 http 的报文的16进制数据  的 JTable 表格Proxy Intercept Hex
	public static JTabbedPane JPtabProxyHistoryDetail; //用来承载 http 代理历史的 Item 的  request 和 response 的 Tab   Proxy History  下面的  tab
	public static JDesktopPane JPtabProxyHistoryDetailEditedRequestPanel;//如果 截断时候 有出现修改源数据包的情况 ， 在历史栏目点击当前http 请求时，会多一个  编辑后的  面板
	public List<String[]> editorStepList = new ArrayList();
	public JTabbedPane JPtabEditorPanel;// 用来承载 主窗口的Editor下的tab
  
  
  
  
	public List<String> expsFileNameList = new ArrayList<String>();//用来存储 Detection 的 Exp 列表的文件名  ，因为在搜索的时候  总不能一直渎职table 表格获取读取本地文件
  
	public List<String[]> expHTTPHistoryList = new ArrayList<String[]>();
  
  
  
	public JTextField JPtabEditorPanelSentToDetectionTargetText;
	
	

	public static void main(String[] args) // 主函数  主进程
	{
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try
				{
					Main window = new Main();
					window.frame.setVisible(true); //主窗口可视化
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
				}
			}
		});
	}

	public Main() throws UnsupportedEncodingException
	{
		initialize();//初始化 界面
		try
		{
			//当程序启动时  先判断 当前默认的端口是否被占用   方式是通过 监听该端口 ，如果监听启动成功，说明该端口不被占用， 如果失败说明该端口被占用
			ServerSocket server = new ServerSocket(listenPort);
			server.close();//关闭 监听成功的 服务
			//System.out.println("端口没占用");
			portUserable = true; //设置 端口可用的状态
		}
		catch (IOException e)
		{
			//System.out.println("端口占用");
			portUserable = false;//设置端口可用的状态
		}	
	}

	private void initialize() throws UnsupportedEncodingException
	{
		this.frame = new JFrame("追梦                                                                                                                                                                                                                                          Exp Manager by Naih");//设置主窗口的标题

		// 这个暂时不清楚啊作用是什么   因为当前这个项目 中间出现了两次代码删除   代码都是jar 反编译过来的 ，所以出现了 少数不知道是什么的东西 
		this.frame.setExtendedState(6);
		
		this.frame.setDefaultCloseOperation(3); //设置主窗口 关闭按钮的 状态 ， 即为 退出程序
		this.frame.setMinimumSize(new Dimension(1200, 600)); //设置主窗口 最小化 的大小

		Method.center(this.frame);//设置主窗口的位置    center 设置窗口为中心位置

		GridBagLayout GBL = new GridBagLayout();// 设置一个布局对象   此程序中  大部分布局都是使用  这个布局 

		JPanel MainPanel = new JPanel();  //主窗口的主面板   

		MainPanel.setLayout(new BorderLayout()); //设置主面板的 布局  
		this.frame.add(MainPanel, "Center");  //添加主面板到 主窗口

		this.JPtab = new JTabbedPane(1);//初始化 主窗口的tab 对象   用来添加 Porxy Editor Detection Settings Others
		this.JPtab.setFocusable(false);// java 空间有个特性 ，当点击的时候会出现 小方框代表 选中 ，这个 为不现实那个小方框      为了好看
		this.JPtab.setForeground(new Color(Integer.decode("#4B4B4B"))); // 设置 主窗口 tab 的背景颜色
		MainPanel.add(this.JPtab, "Center"); // 在主面板上 添加  主Tab   用来添加 Porxy Editor Detection Settings Others

		JDesktopPane JDPProxy = new JDesktopPane();  //Proxy 代理的模块的  Desktop      每个tab 都是先添加一个  desktop 面板
		JDPProxy.setLayout(new BorderLayout()); // 布局

		JPanel ProxyMainPane = new JPanel(); // Proxy 的主面板

		ProxyMainPane.setLayout(new BorderLayout());
		JDPProxy.add(ProxyMainPane, "Center");

		JTabbedPane JPtabProxy = new JTabbedPane(1);  //Proxy 的 Tab   用来承载  Intercept 、 History 、 Options
		JPtabProxy.setFocusable(false);
		JPtabProxy.setForeground(new Color(Integer.decode("#4B4B4B")));
		ProxyMainPane.add(JPtabProxy, "Center");

		JDesktopPane JDPIntercept = new JDesktopPane(); //Proxy Intercept 的Desktop
		JPtabProxy.add(JDPIntercept, "Intercept");

		JDesktopPane JDPHistory = new JDesktopPane(); //Proxy History 的Desktop
		JDPHistory.setLayout(new BorderLayout());
		JPtabProxy.add(JDPHistory, "History");

		JDesktopPane JDPOptions = new JDesktopPane(); //Proxy Options 的 Desktop 
		JDPOptions.setLayout(new BorderLayout());
		JPtabProxy.add(JDPOptions, "Options");

		JPanel JDPOptionsMainPanel = new JPanel();  //Proxy Options 的 主面板
		JDPOptionsMainPanel.setLayout(new BorderLayout());
		JDPOptions.add(JDPOptionsMainPanel, "Center");

		JCheckBox proxyOPtionsRuningCheckBox = new JCheckBox();
		JDPOptionsMainPanel.setLocation(100, 400);
		JDPOptionsMainPanel.setSize(new Dimension(100, 100));

		JPanel proxyOptionsEditPanel = new JPanel();
		proxyOptionsEditPanel.setLayout(null);
		//proxyOptionsEditPanel.setBackground(Color.yellow);
		proxyOptionsEditPanel.setPreferredSize(new Dimension(0,100));
		JDPOptionsMainPanel.add(proxyOptionsEditPanel,BorderLayout.NORTH);
		
		JPanel proxyOptionsNullPanel = new JPanel();
		proxyOptionsNullPanel.setBackground(Color.white);
		proxyOptionsNullPanel.setSize(0,300);		
		JDPOptionsMainPanel.add(proxyOptionsNullPanel,BorderLayout.CENTER);
		
		JLabel proxyOptionsPortLabel = new JLabel("Port :");
		proxyOptionsPortLabel.setSize(70,30);
		proxyOptionsPortLabel.setLocation(7, 7);
		proxyOptionsEditPanel.add(proxyOptionsPortLabel);
		
		final JTextField proxyOptioinsPortText = new JTextField("8087");
		proxyOptioinsPortText.setSize(40,25);
		proxyOptioinsPortText.setLocation(50, 10);
		proxyOptionsEditPanel.add(proxyOptioinsPortText);
		
		final JButton testbutton = new JButton("Start"); //Proxy Options Start Button
		testbutton.setFocusable(false);
		testbutton.setLocation(110, 10);
		testbutton.setSize(new Dimension(100, 25));
		proxyOptionsEditPanel.add(testbutton);

		testbutton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (!proxyState) // 通过先前代理状态来判断开启或者关闭 代理 
				{
					// 当 proxyState false  代理关闭时  点击    则 进入程序 开启代理
					
					try
					{
						listenPort = Integer.valueOf(proxyOptioinsPortText.getText().trim());
						if(listenPort>65535||listenPort<=0)
						{
							JOptionPane.showMessageDialog(null, "请输入一个正确的端口号:1-65535", "Warning", 2);
						}
						else
						{
							try
							{
								ServerSocket server = new ServerSocket(listenPort); //先判断 端口可用
								server.close();
								//System.out.println("端口没占用");
								portUserable = true; //设置标志
							}
							catch (IOException eee)
							{
								//System.out.println("端口占用");
								JOptionPane.showMessageDialog(null, "该端口已占用", "Warning", 2);
								portUserable = false;
							}

							if (portUserable) // 端口可用
							{
								try
								{
									testbutton.setText("Stop"); //修改按钮的 title 
									proxyState = true;  //修改代理的 状态
									server = new ServerSocket(listenPort); //初始化 代理的监听服务    
									ProxyThread = new Thread(new Runnable() //初始化 代理的 线程
														{
															public void run()
																	{
																		while (proxyState) //proxystate true  代理开启时   一直监听该端口
																		{
																			Socket socket = null;
																			try
																			{
																				socket = server.accept(); //开始监听 
																			}
																			catch (IOException localIOException)
																			{
																			}

																			class ActionSocket extends Thread   //定义线程来响应 浏览器发过来的 请求   
																			{
																				private Socket socket = null; //该socket 其实就是上面的服务socket  来与浏览器 响应
																				private String host = ""; //用来存储目标主机地址
																				private int port = 80;// 默认目标端口是 80 
																				private boolean GETOrPOST = true; //用来标志 使用的方法是  GetOrPost  只有Get 或者 Post  才 截断 ，其他放行

																				  
																				private String HTTPRequestHead = "";// Http 请求的  Header  

																				public ActionSocket(Socket s)
																				{
																					this.socket = s ;           
																				}
		                      
		                      
																				public void run()
																				{
																					try
																					{
																						action();
																					}
																					catch (Exception localException)
																					{
																					}
																				}

																				public void action() throws Exception
																				{
		                    	  
		                    	  
																				
																					BufferedReader brIn = new BufferedReader(new InputStreamReader(socket.getInputStream(),"utf-8")); // 获取socket 的输入流 放入缓冲区
																					String StrBuffer = brIn.readLine(); //读取该 socket输入流 的第一行   第一行通常是   GET HTTP://www.baidu.com/xxx http/1.1  	这里目的有两个 一 、获取http 方法  因为get 和 post 请求 不一样 一个有
																					//content 一个 没有content   当post 请求的时候  有些请求 没有在最后加入字符串结束符的时候  该socket 会一直等待输入流来结束   就会影响整个网页响应的速度   通过判断  post  获取 content-length 或者 boundary=（上传表单的分割标志） 来识别字符串结束符
																					boolean HTTPSSocket = false; //用来标志是否使用 https    因为使用https  的目标端口是443  与  http  不一样
																					OutputStream Out = this.socket.getOutputStream(); // socket 的 输出流
																					if (StrBuffer.length() >= 1) // 通过判断第一行  先 模糊识别这是不是一个正常的http 请求
																					{
																						String[] tempStr = StrBuffer.split(" "); // 根据空格划分   分别取出  GET   和    http：// www.baid......
																						
																						if (!InterceptState) //这里判断截断的状态  ，  如果是不截断的话  直接把 socket 的 输出流 out 传过去  ，让 http 响应的数据直接写入  这样加快了 响应速度 ， 而如果 等数据全部取出来 在写入浏览器 ， 当数据量大的时候  会出现 明显的延时情况
																						{
																							if (tempStr[0].equals("POST"))// Post 方法
																							{
																								int dataLength = 0; // 存储 content-length 的长度 
																								String endString=""; //如果是上传表单 会有一个  分隔符  在  Content-Type 参数里    
		                              
																								for (String temp = brIn.readLine(); temp != null; temp = brIn.readLine()) //循环获取浏览器传过来的数据   （缓冲期里）
																								{
																									StrBuffer = StrBuffer + "\r\n" + temp; //每一行 都添加一个 回车 换行  因为读取出来的时候  回车换行都没了    ，而 http 标准 是每一行结尾都是 回车换行 13 10 0d 0a 
																									if (temp.startsWith("Content-Length:"))  // 获取content-length 的值
																									{
																										dataLength = Integer.valueOf(temp.split("Content-Length:")[1].trim());
																									} 
																									else if (temp.startsWith("Host:"))  //获取host 和 port 
																									{
																										String hostPortTmp = temp.split("Host:")[1];
																										String[] hostPort = hostPortTmp.split(":");
																										host = hostPort[0].trim();
																										if (hostPort.length > 1)
																										{
																											port = Integer.valueOf(hostPort[1].trim());
																										}
																									}
																									else if(temp.startsWith("Content-Type:")) //上传类型 
																									{
																										try
																										{
																											String[] tmpmp =  temp.split("boundary="); // 如果是表单的上传类型  获取 分隔符 
																											if(tmpmp.length>1)
																											{
																												//System.out.println(tmpmp[0]+":"+tmpmp[1]);
																												endString = tmpmp[1].trim();
																												endString = "--"+endString+"--";  //结束符都是  分隔符的前后 加上 两个  -
																											}
																										}
																										catch(Exception es)
																										{
																											es.printStackTrace();
																										}
																										
																									}
																									
																									if (temp.length() <= 0)// 因为每个http 请求的 header 与 content 的分割都是以连续两个 13 10 （0d、0a）作为header 的结束  因此  header 与 content 之间的第一个 空行 就是他们的分割界限    遇到空行 代表 header 结束 进入 post 的 上传数据
																									{
																										
																										List<Byte> tmpbytes = new ArrayList<Byte>(); //定义一个list 来存储 获取的每个byte （浏览器传来的数据）  为什么要一个字节一个字节的读取 因为 读取一行一行的话 不容易判断 是否读取到content-length 长度了， 有时候 socket 会一直等待结束符
																										
																										
																										///////////
																										String requestData="";
																										for (String temp2 = brIn.readLine(); temp2 != null; temp2 = brIn.readLine()) //循环获取浏览器传过来的数据   （缓冲期里）
																										{
																											//System.out.println(temp2);
																											requestData = requestData+temp2+"\r\n";
																											if(temp2.equals(endString))
																											{
																												break;
																											}
																										}
																										
																										StrBuffer = StrBuffer + "\r\n";
																										StrBuffer = StrBuffer + requestData; //byte 转成字符串
																										StrBuffer = StrBuffer + "\r\n"+"\r\n";
																										
																										//System.exit(1);
																										///////////
																										/*
																										int k=0;
																										for (int i = 0; i < dataLength; i++) //循环读取 content-length 长度额字节
																										{
																											
																											byte tmp = (byte)brIn.read();
																											tmpbytes.add(tmp); //加入 list 
																											System.out.println(tmp);
																											
																											if(endString.length()>0) //这里很关键  ，如果是表单上传  content-length 来判断 字符串结束符是不准确的  因为如果上传 图片或者什么二进制 容易造成字节数的不正确  ， 通过content-length 来判断会造成 socket 一直在等待   因此
																												//通过读取的字节与上传表单格式的结束字符串进行匹配 来判断 数据流的读取结束 
																											{
																												
																												if(endString.substring(k, k+1).equals(String.valueOf(((char)tmp)))) //如果一个字符 与 endString匹配 则 k+1 标志成功匹配多了一个 ，如果不匹配则 设置k=0
																													//这样只有当 连续匹配 endString.length 长度的时候 即 k=endString.length  说明结束
																												{
																													k++;
																													
																													if(k==endString.length())
																													{
																														
																														break;
																													}
																												}
																												else
																												{
																													k=0;
																												}
																											}
																											
																										}
																										
																										
																										// 把 list 的byte 转成  数组的byte 
																										int listcount = tmpbytes.size();
																										byte[] tmtmp = new byte[listcount];
																										for(int i=0;i<listcount;i++)
																										{
																											tmtmp[i] = tmpbytes.get(i);
																										}
																										
																										StrBuffer = StrBuffer + "\r\n";
																										StrBuffer = StrBuffer + Method.bytesToString(tmtmp); //byte 转成字符串
																										StrBuffer = StrBuffer + "\r\n"+"\r\n";
																										*/
																										
																										
																										
																										break;
																									}
																								}
																							}
																							else if (tempStr[0].equals("CONNECT")) //为什么要独立开 这个 connect   https 使用的方法前 都是用 conntet 连接目标   如果使用这个方法  说明是 https
																							{
																								HTTPSSocket = true;
																								String hostPortTmp = tempStr[1];
																								String[] hostPort = hostPortTmp.split(":");
																								host = hostPort[0].trim();
																								if (hostPort.length > 1)
																								{
																									port = Integer.valueOf(hostPort[1].trim()).intValue();
																								}

																							}
																							else//如果不是 post  不是  connect  就直接 全部读取 通过
																							{
																								for (String temp = brIn.readLine(); temp != null; temp = brIn.readLine())
																								{
																									StrBuffer = StrBuffer + "\r\n" + temp;
																									if (temp.length() <= 0)
																									{
																										HTTPRequestHead = StrBuffer;
																										break;
																									}
																									if (temp.startsWith("Host:"))
																									{
																										String hostPortTmp = temp.split("Host:")[1];
																										String[] hostPort = hostPortTmp.split(":");
																										host = hostPort[0].trim();
																										if (hostPort.length > 1)
																										{
																											port = Integer.valueOf(hostPort[1].trim()).intValue();
																										}
																									}
																								}
																								StrBuffer = StrBuffer + "\r\n";
																							}

																							proxyHistoryItem = new String[9]; //初始化 每个proxy 的Item 
																							proxyHistoryItem[0] = String.valueOf(proxyHistoryNum); //统计 proxy history 的 个数  即为当前是第几个

																							proxyHistoryItem[1] = host; // 目标的主机地址 
																							proxyHistoryItem[2] = tempStr[0];// http 使用的方法  get post connnect 
																							proxyHistoryItem[3] = tempStr[1];//http 访问的路径      /www/new/123.html
																							proxyHistoryItem[5] = StrBuffer; //http 请求的 完整报文
																							proxyHistoryItem[7] = String.valueOf(port); //http 请求的目标端口
																							int proxyHistoryNowNum = ProxyHistoryListTable.getRowCount(); // 其实这个才是真正的统计 proxy history 的item 个数的      在多个线程同时运行的时候 proxyHistoryNum 会出现同时调用的情况 因为没有做限制 ，出现参数统计错误  而通过获取table的行数 就是准备统计proxy 个数  因为每个proxy 请求都会添加到table上
																							proxyHistoryItem[0] = String.valueOf(proxyHistoryNowNum); //
																							proxyHistoryList.add(proxyHistoryItem);//把proxy 的Item 放入  list 中  ，    这个list 的作用是  相当于存储 所有代理的历史 当后面要查看每个历史数据的时候 就可以从这边获取

																							DefaultTableModel tableModel = (DefaultTableModel)ProxyHistoryListTable.getModel();//获取table 的model 添加 item 数据，因为要做到实时，所以不等全部获取到response data 就添加上 table了  ，   待后面的得到 response data 时候在修改 table 的值  更新 
																							tableModel.addRow(new Object[] { Integer.valueOf(proxyHistoryNowNum + 1), proxyHistoryItem[1], proxyHistoryItem[2], proxyHistoryItem[3] });
																							ProxyHistoryListTable.invalidate();// 更新table 空间

																							//getHttPResponseData 这个返回值是这个http请求的返回状态值           参数 （  https是否使用标志    、目标主机地址、 目标端口  、 请求的数据报文 、 当前请求的输出流对象  、 当前http 的 Item 
																							int HttpResponseHeaderStatus =Method.getHttPResponseData(HTTPSSocket, host,port, StrBuffer, Out, proxyHistoryItem);

																							if (HttpResponseHeaderStatus > 0) //当http请求 有返回值 时 、  更新table 表格 
																							{
																								proxyHistoryItem[4] = String.valueOf(HttpResponseHeaderStatus);
																								tableModel.setValueAt(Integer.valueOf(HttpResponseHeaderStatus), proxyHistoryNowNum, 4);
																								ProxyHistoryListTable.invalidate();
																							}

																							proxyHistoryNum += 1; // item 熟 累计 +1 
																						}
																						else // 截断开启     截断数据包  
																						{
																							//截断只截断 get 和  post 的方法   而 区分开 get 和 post 是因为 防止有些数据结尾没有结束符 造成的 输入流的一直等待
																							if (tempStr[0].equals("GET"))  //GET 
																							{
																								for (String temp = brIn.readLine(); temp != null; temp = brIn.readLine())
																								{
																									
																									StrBuffer = StrBuffer + "\r\n" + temp;
																									
																									if (temp.length() <= 0)//GET 方法  遇到一个空行代理header 结束   get 方法请求 只有头部 
																									{
																										HTTPRequestHead = StrBuffer;
																										break;
																									}
																									if (temp.startsWith("Host:")) //取出 主机和端口
																									{
																										String hostPortTmp = temp.split("Host:")[1];
																										String[] hostPort = hostPortTmp.split(":");
																										host = hostPort[0].trim();
																										if (hostPort.length > 1)
																										{
																											port = Integer.valueOf(hostPort[1].trim()).intValue();
																										}
																									}
																								}
																								StrBuffer = StrBuffer + "\r\n"; //
																								GETOrPOST = true;
																							}
																							else if (tempStr[0].equals("POST"))
																							{
																								int dataLength = 0;
																								String endString="";
																								for (String temp = brIn.readLine(); temp != null; temp = brIn.readLine())
																								{
																									StrBuffer = StrBuffer + "\r\n" + temp;
																									if (temp.startsWith("Host:")) //获取目标主机的地址和端口
																									{
																										String hostPortTmp = temp.split("Host:")[1];
																										String[] hostPort = hostPortTmp.split(":");
																										host = hostPort[0].trim();
																										if (hostPort.length > 1)
																										{
																											port = Integer.valueOf(hostPort[1].trim()).intValue();
																										}
																									}																		
																									else if (temp.startsWith("Content-Length:"))//获取 post 方法中的  content-length 长度
																										dataLength = Integer.valueOf(temp.split("Content-Length:")[1].trim()).intValue();
																									else if(temp.startsWith("Content-Type:"))/// 如果post是 上传数据  则需要获取  上传参数的分隔符   
																									{
																										try
																										{
																											String[] tmpmp =  temp.split("boundary=");// 分隔符在  Content-Type:  boundary=
																											if(tmpmp.length>1)
																											{
																												//System.out.println(tmpmp[0]+":"+tmpmp[1]);
																												endString = tmpmp[1].trim();
																												endString = "--"+endString+"--"; // post 上传的结束符号是  分隔符的前后都加上两个-
																											}
																										}
																										catch(Exception es)
																										{
																											es.printStackTrace();
																										}
																									
																									}
																									if (temp.length() <= 0)// header 结束 进入 content  这部分与上面 不截断数据都是一样的 参考上面的解析
																									{
																										//
																										
																										
																										
																										
																										
																										///////////
																										String requestData="";
																										for (String temp2 = brIn.readLine(); temp2 != null; temp2 = brIn.readLine()) //循环获取浏览器传过来的数据   （缓冲期里）
																										{
																											//System.out.println(temp2);
																											requestData = requestData+temp2+"\r\n";
																											if(temp2.equals(endString))
																											{
																												break;
																											}
																										}
																										
																										StrBuffer = StrBuffer + "\r\n";
																										StrBuffer = StrBuffer + requestData; //byte 转成字符串
																										StrBuffer = StrBuffer + "\r\n"+"\r\n";
																										
																										
																										/*
																										
																										
																										
																										
																										int k=0;
																										List<Byte> tmpbytes = new ArrayList<Byte>();
																										for (int i = 0; i < dataLength; i++)
																										{
																											byte tmp = (byte)brIn.read();
																											tmpbytes.add(tmp);
																											if(tmp==-1)
																											{
																												break;
																											}
																											
																											if(endString.length()>0)
																											{
																												if(endString.substring(k, k+1).equals(String.valueOf(((char)tmp))))
																												{
																													k++;
																													
																													if(k==endString.length())
																													{
																														
																														break;
																													}
																												}
																												else
																												{
																													k=0;
																												}
																											}
																										}

																										int listcount = tmpbytes.size();
																										byte[] tmtmp = new byte[listcount];
																										for(int i=0;i<listcount;i++)
																										{
																											tmtmp[i] = tmpbytes.get(i);
																										}
																										StrBuffer = StrBuffer + "\r\n";
																										StrBuffer = StrBuffer + Method.bytesToString(tmtmp);
																										StrBuffer = StrBuffer + "\r\n"+"\r\n";
																										*/
																										break;
																									}
																								}
																								GETOrPOST = true;
																							}
																							else
																							{
																								for (String temp = brIn.readLine(); temp != null; temp = brIn.readLine())
																								{
																									StrBuffer = StrBuffer + "\r\n" + temp;
																									if (temp.length() <= 0)
																									{
																										HTTPRequestHead = StrBuffer;
																										break;
																									}
																									if (temp.startsWith("Host:"))
																									{
																										String hostPortTmp = temp.split("Host:")[1];
																										String[] hostPort = hostPortTmp.split(":");
																										host = hostPort[0].trim();
																										if (hostPort.length > 1)
																										{
																											port = Integer.valueOf(hostPort[1].trim()).intValue();
																										}
																									}
																								}
																								StrBuffer = StrBuffer + "\r\n";
																								GETOrPOST = false;
																							}
																							
																							
																							
																							
																							proxyHistoryItem = new String[9];
																							proxyHistoryItem[0] = String.valueOf(proxyHistoryNum);

																							proxyHistoryItem[1] = host;
																							proxyHistoryItem[2] = tempStr[0];
																							proxyHistoryItem[3] = tempStr[1];
																							proxyHistoryItem[5] = StrBuffer;
																							proxyHistoryItem[7] = String.valueOf(port);
																							int proxyHistoryNowNum = ProxyHistoryListTable.getRowCount();

																							proxyHistoryList.add(proxyHistoryItem);

																							DefaultTableModel tableModel = (DefaultTableModel)ProxyHistoryListTable.getModel();
																							tableModel.addRow(new Object[] { Integer.valueOf(proxyHistoryNowNum + 1), proxyHistoryItem[1], proxyHistoryItem[2], proxyHistoryItem[3] });
																							ProxyHistoryListTable.invalidate();

																							proxyHistoryNum += 1;

																							
																							//这里标志出 是否使用https 请求   0 没有 ，，，1 有
																							int HTTPSSocketInt = 0;
																							if (HTTPSSocket)
																							{
																								HTTPSSocketInt = 1;
																							}

																							// 当截断之后的 http 请求放入队列里     队列的作用就是先进先出  也就是  截断的请求 先请求的  先响应我们的操作
																							queueHTTPSSocket.offer(Integer.valueOf(HTTPSSocketInt)); //代表是否使用 https 的 队列
																							queueOut.offer(Out);// 代表当前http 请求的输出流对象   
																							queueproxyHistoryItem.offer(proxyHistoryItem); //代表当前http 请求的相关参数数据

																								
																							//这里的作用是 在 Proxy Intercept Raw 显示截断的数据  
																							//但是为什么要 多两个if  是因为如果请求的时候 突然 关闭截断 就不要再让它显示在这个 面板上了  ，不然就德等于多了一个请求
																							if (proxyState)
																							{
																								if (InterceptState)
																								{
																									if (InterceptShowString.length() == 0)//通过判断当前 InterceptShowString 是否有数据                     、、、InterceptShowString 这个作用就是一个中间过度的   raw headers hex 三个之间的修改 都要同时操作这个数据  ，相当于的 存储中心
																									{
																										//分别取出 三个队列里的数据    放入存储中心  就是个临时的 数据中心 存储当前的 请求的相关数据 
																										queueproxyHistoryItemTmp = ((String[])queueproxyHistoryItem.poll());
																										queueOutTmp = ((OutputStream)queueOut.poll());
																										queueHTTPSSocketTmp = ((Integer)queueHTTPSSocket.poll());

																										//传给raw headers hex 数据中心
																										InterceptShowString = queueproxyHistoryItemTmp[5];
																										
																										//显示在  proxy Intercept 的raw 上
																										InterceptDaraRawTextArea.setText(InterceptShowString);
																										InterceptDaraRawTextArea.setSelectionStart(1);
																										InterceptDaraRawTextArea.setSelectionEnd(1);
																										InterceptDaraRawTextArea.invalidate();
																										
																									}
																								}
																							}
																							
																						
																						}
																					}
																				}
																			};
																			ActionSocket ap = new ActionSocket(socket); //初始化  监听的 线程 并传入 监听的服务  
																			ap.start();
																		}
																	}
														});
									ProxyThread.start(); // 主监听线程 启动
								}
								catch (IOException eee)
								{
									eee.printStackTrace();
								}
							}
						}
					}
					catch(Exception es)
					{
						JOptionPane.showMessageDialog(null, "请输入一个正确的数字", "Warning", 2);
					}
				}
				else //当 原本代理状态是开启的时候  点击该控件则关闭 监听的 socket 服务
				{
					try
					{
						testbutton.setText("Start");
						proxyState = false; 
						server.close();
					}
					catch (Exception eeee)
					{
						eeee.printStackTrace();
					}
				}
			}
		});
		
		
		this.JPtab.add(JDPProxy, "Proxy");// 主 tab 添加 proxy DesktopPanel

		JDesktopPane JDPEditor = new JDesktopPane();
		
	
		JDPEditor.setLayout(new BorderLayout());

		this.JPtab.add(JDPEditor, "Editor"); //主tab 上 添加 editor desktopPanel 

		
		/////////////////////////////////////////////////////////////////////////////////////////////start   //////////////////
		JDesktopPane JDPDetection = new JDesktopPane();
		JDPDetection.setLayout(new BorderLayout());
		this.JPtab.add(JDPDetection, "Detection");// 主tab 上 添加 Detetion desktopPanel 

		
		
		JPanel JDPDetectionLeftPanel = new JPanel(); //这个 为  Detection 左侧Exp 列表的 主面板
		JDPDetectionLeftPanel.setLayout(new BorderLayout());
		//JDPDetectionLeftPanel.setBackground(Color.blue);
		JDPDetectionLeftPanel.setPreferredSize(new Dimension(150,0));
		
		// 这个 为  Detection 左侧Exp 列表的 主面板 上面的   exp 列表的表格
		String[] JDPDetectionLeftPanelTableHeaders = { "Name", "Value" };
	    Object[][] JDPDetectionLeftPanelTableCellData = null;
	    DefaultTableModel JDPDetectionLeftPanelTableModel = new DefaultTableModel(JDPDetectionLeftPanelTableCellData, JDPDetectionLeftPanelTableHeaders) {
	    	public boolean isCellEditable(int row, int column) 
	    	{
	    		if ((column == 1))
	 				  return false;
	    		return true;
	    	}
	    };
	    final JTable JDPDetectionLeftPanelTable = new JTable(JDPDetectionLeftPanelTableModel);
	    
	    
	    
	    //RolloverRenderer renderer = new RolloverRenderer(JDPDetectionLeftPanelTable.getModel(),frame);
	    //JDPDetectionLeftPanelTable.setDefaultRenderer(Object.class, renderer);
	    //JDPDetectionLeftPanelTable.addMouseListener(renderer);
	    //JDPDetectionLeftPanelTable.addMouseMotionListener(renderer);
	    
	    
	    
	    
	    //隐藏 table 表格的头部
	    JDPDetectionLeftPanelTable.getTableHeader().setVisible(false);  
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();  
        renderer.setPreferredSize(new Dimension(0, 0));  
        JDPDetectionLeftPanelTable.getTableHeader().setDefaultRenderer(renderer); 
	    
        //exp 列表的表格 承载于 滚动面板 当exp 数据量大的时候可以 调整
		JScrollPane JDPDetectionLeftScrollPanel = new JScrollPane(JDPDetectionLeftPanelTable);
		JDPDetectionLeftPanel.add(JDPDetectionLeftScrollPanel,BorderLayout.CENTER);
		
		//设置 exp 列表的表格 的  checkbox 默认没有选中 
		TableColumn   aColumn   =   JDPDetectionLeftPanelTable.getColumnModel().getColumn(0);   
		aColumn.setCellEditor(JDPDetectionLeftPanelTable.getDefaultEditor(Boolean.class));   
		aColumn.setCellRenderer(JDPDetectionLeftPanelTable.getDefaultRenderer(Boolean.class));
		aColumn.setPreferredWidth(20);
		aColumn.setMaxWidth(20);
		
		final JDialog JDPDetectionLeftPanelTableReadAboutDailog = new JDialog();
		JDPDetectionLeftPanelTableReadAboutDailog.setSize(100,300);
		//System.out.println();
		
		
		JDPDetectionLeftPanelTable.addMouseListener(new MouseListener()
	 	  {
	 		  public void mouseReleased(MouseEvent e)
	 		  {
	 		  }

	 		  public void mousePressed(MouseEvent e)
	 		  {
	 		  }

	 		  public void mouseExited(MouseEvent e)
	 		  {
	 			 
	 		  }

	 		  public void mouseEntered(MouseEvent e)
	 		  {
	 			  
	 			 
	 			 
	 		  }

	 		  public void mouseClicked(final MouseEvent e)
	 		  {
	 			  
	 			  
	 			 
	 			  
	 			  
	 			  
	 			 if((e.getModifiers()&InputEvent.BUTTON3_MASK)!=0&&JDPDetectionLeftPanelTable.getSelectedRow()>=0)
	 			 {
	 				final String tmp2 = expsFileNameList.get(JDPDetectionLeftPanelTable.getSelectedRow());
	 				
	 				
	 				PopupMenu expFileEditMenu = new PopupMenu();	
	 				
	 				MenuItem expFileEditDeleteItem = new MenuItem();
	 				expFileEditDeleteItem.setLabel("Delete");
	 				
	 				MenuItem expFileEditChangeNameItem = new MenuItem();
	 				expFileEditChangeNameItem.setLabel("ChangeName");
	 				
	 				MenuItem expFileEditViewDetailItem = new MenuItem();
	 				expFileEditViewDetailItem.setLabel("ViewDetail");
	 				
	 				MenuItem expFileEditSendToEditorItem = new MenuItem();
	 				expFileEditSendToEditorItem.setLabel("SendToEditor");
	 				
	 				MenuItem expFileEditAboutItem = new MenuItem();
	 				expFileEditAboutItem.setLabel("ReadAbout");
	 				
	 				//expFileEditMenu.add(expFileEditViewDetailItem);
	 				expFileEditMenu.add(expFileEditAboutItem);
	 				expFileEditMenu.add(expFileEditSendToEditorItem);
	 				expFileEditMenu.add(expFileEditChangeNameItem);
	 				expFileEditMenu.add(expFileEditDeleteItem);
	 				JDPDetectionLeftPanelTable.add(expFileEditMenu);				
	 				expFileEditMenu.show(JDPDetectionLeftPanelTable, e.getX(), e.getY());
	 				
	 				expFileEditSendToEditorItem.addActionListener(new ActionListener(){
	 					@Override
	 					public void actionPerformed(ActionEvent e)
	 					{
	 						//System.out.println("sendtoeditor");
	 						String requestDat2a = "GET /previousURL/scada/Login.aspx HTTP/1.1"+"\r\n"+
									 "Host: takipmobil.com"+"\r\n"+
									 "Proxy-Connection: keep-alive"+"\r\n"+
									 "Cache-Control: max-age=0"+"\r\n"+
									 "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp;q=0.8"+"\r\n"+
									 "User-Agent: Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.131 Safari/537.36"+"\r\n"+
									 "Referer: http://www.gfsoso.com/?q=inurl%3Ascada/login&pn=10"+"\r\n"+
									 "Accept-Encoding: gzip,deflate,sdch"+"\r\n"+
									 "Accept-Language: zh-CN,zh;q=0.8,en;q=0.6"+"\r\n"+
									 "Cookie: ASP.NET_SessionId=4prxqtx5hupq3iimnvzejsll"+"\r\n"+
									 "\r\n";
	 						int EditorStepCount = JPtabEditorPanel.getTabCount();
	 						boolean clear=true;
	 						if(EditorStepCount>0)
	 						{
	 							//System.out.println(EditorStepCount+".................");
	 							int n=JOptionPane.showConfirmDialog(null, "是否清空Editor上原有的数据", "Warning", JOptionPane.YES_NO_OPTION);  
	 							if (n == JOptionPane.YES_OPTION)
	 							{
	 								//System.out.println("清空");
	 								EditorStep EditorStepTmp = new EditorStep(new JTabbedPane());
	 								EditorStepTmp.GrobalFileInStep = -1;
	 								EditorStepTmp.FileForCycleWithEXP = "";
	 								EditorStepTmp.FileForCycleWithEXPStrings[0] = "";
	 								EditorStepTmp.FileForCycleWithEXPStrings[1] = "-1";
	 								for(int i=0;i<EditorStepCount;i++)
	 								{	 										 									
	 									//System.out.println(EditorStepTmp.host);								 									
	 									JPtabEditorPanel.remove(0);
	 								}
	 								EditorStepNum=0;
	 								clear=true;
	 							}
	 							else if (n == JOptionPane.NO_OPTION)
	 							{
	 								//System.out.println("不清空");
	 								clear=false;
	 							}
	 						}
	 						else
	 						{
	 							//System.out.println(EditorStepCount+".................");
	 							//AddTabPanel(JPtabEditorPanel,"127.0.0.1",80,requestDat2a);
	 						}
	 						
	 						//System.out.println(tmp2);
	 						
	 						String ExpDiceory = "Exps/"+tmp2+"/";
	 						String ExpMainStep = ExpDiceory+"Step.txt";
	 						File ExpMainStepFile = new File(ExpMainStep);
	 						InputStreamReader ExpMainStepFileInputSteamReadTmp;
							try 
							{
								ExpMainStepFileInputSteamReadTmp = new InputStreamReader(new FileInputStream(ExpMainStepFile));
								BufferedReader ExpMainStepFileBufferedReader = new BufferedReader(ExpMainStepFileInputSteamReadTmp);
								String tmpLine="";
								while((tmpLine=ExpMainStepFileBufferedReader.readLine())!=null)
								{
									//System.out.println(tmpLine);
									String StepResourceName="";
									String StepName="";
									String host = "";
			 						int port;
									
			 						boolean replaceHostPort = true;
			 						boolean usePreviousSetCookie =true;
			 						
			 						List<String[]> randomList = new ArrayList<String[]>();
			 						List<String[]> captchaList = new ArrayList<String[]>();
			 						List<String[]> GoaheadOrStopFromStatusList = new ArrayList<String[]>();
			 						List<String[]> GoaheadOrStopFromDataList = new ArrayList<String[]>();
			 						
			 						String[] cycleForPackage = new String[4];
			 						String cycleForExp="";
			 						
			 						String[] tmplines = tmpLine.split("-");
			 					//	System.out.println(tmplines[0]);			 						
			 						StepName = tmplines[0];
			 						
			 						
			 						
			 						String requestDataTmp  = Method.bytesToString(Method.HexStringTobytes(Method.stringsToString(Method.readFileToHexStrings(ExpDiceory+"/"+StepName))));
			 						
			 						
			 						
			 						
			 						//System.out.println(tmplines[1]);
			 						try
			 						{	
			 							host = Method.getFromBase64(tmplines[1].split("host:")[1]);
			 						}
			 						catch(Exception es)
			 						{
			 							//es.printStackTrace();
			 							host = "";
			 						}
			 					//	System.out.println(host);
			 						
			 						//System.out.println(tmplines[2]);
			 						try
			 						{
			 							port = Integer.valueOf(Method.getFromBase64(tmplines[2].split("port:")[1]));
			 						}
			 						catch(Exception es)
			 						{
			 							es.printStackTrace();
			 							port=0;
			 						}
			 						//System.out.println(port);
			 						
			 						
			 						
			 						
			 						AddTabPanel(JPtabEditorPanel,host,port,requestDataTmp);
			 						
			 						int indexStep;
			 						if(clear)
			 						{
			 							indexStep= Integer.valueOf(StepName.substring(4,StepName.indexOf(".txt")));
			 						}
			 						else
			 						{
			 							indexStep= Integer.valueOf(StepName.substring(4,StepName.indexOf(".txt")))+EditorStepCount;
			 						}
			 						
			 					//	System.out.println(indexStep);
			 						EditorStep EditorStepTmp = (EditorStep)JPtabEditorPanel.getComponentAt(indexStep);
			 						//System.out.println(EditorStepTmp.host);
			 						//System.exit(1);
			 						
			 						
			 						EditorStepTmp.currentExpName = tmp2;
			 						EditorStepTmp.resourceStepString = Method.bytesToString(Method.HexStringTobytes(Method.stringsToString(Method.readFileToHexStrings(ExpDiceory+"/"+"Step"+indexStep+"_Resource.txt"))));
			 						
			 					
			 					//	System.out.println(Method.getFromBase64(tmplines[3].split("stepName:")[1]));
			 						
			 						String replaceHostPortStr = (tmplines[4].split("replaceHostPort:")[1]);
			 						if(replaceHostPortStr.equals("true"))
			 						{
			 							replaceHostPort = true;
			 							EditorStepTmp.EditorStepReplaceHostPort.setLabel("CencleReplaceHostPort");
			 						}
			 						else if(replaceHostPortStr.equals("false"))
			 						{
			 							replaceHostPort = false;
			 							EditorStepTmp.EditorStepReplaceHostPort.setLabel("ReplaceHostPort");
			 						}
			 						//System.out.println(replaceHostPortStr);
			 						EditorStepTmp.replaceHostPort = replaceHostPort;
			 						
			 						
			 						
			 						String usePreviousSetCookieStr = (tmplines[5].split("useSetCookieFromPrevious:")[1]);
			 						if(usePreviousSetCookieStr.equals("true"))
			 						{
			 							usePreviousSetCookie = true;
			 							EditorStepTmp.EditorStepUsepreviousSetCookie.setLabel("CencleUsepreviousSetCookie");
			 						}
			 						else if(usePreviousSetCookieStr.equals("false"))
			 						{
			 							usePreviousSetCookie = false;
			 							EditorStepTmp.EditorStepUsepreviousSetCookie.setLabel("UsepreviousSetCookie");
			 						}
			 						//System.out.println(usePreviousSetCookieStr);
			 						EditorStepTmp.useSetCookieFromPrevious = usePreviousSetCookie;
			 						
			 						
			 						
			 						String[] haveRandomString = tmplines[6].split("randomString:");
			 						if(haveRandomString.length>1)
			 						{
			 							//System.out.println("you");
			 							String[] randomStrings = haveRandomString[1].split(",");
			 							//System.out.println(randomStrings.length);
			 							for(int i=0;i<randomStrings.length;i++)
			 							{
			 								//System.out.println(randomStrings[i]);
			 								//System.out.println(Method.getFromBase64(randomStrings[i]));
			 								String[] randomStringItem = new String[5];
			 								String[] randomStringItemTmp = Method.getFromBase64(randomStrings[i]).split(",");
			 								if(randomStringItemTmp[1].equals("3"))
			 								{
			 									randomStringItem[0] = randomStringItemTmp[0];
			 									randomStringItem[1] = randomStringItemTmp[1];
			 									randomStringItem[2] = randomStringItemTmp[2].substring(0,1);
			 									randomStringItem[3] = randomStringItemTmp[2].substring(1);
			 									randomStringItem[4] = randomStringItemTmp[3];
			 									
			 									//System.out.println(randomStringItem[0]);
			 									//System.out.println(randomStringItem[1]);
			 									//System.out.println(randomStringItem[2]);
			 									//System.out.println(randomStringItem[3]);
			 									EditorStepTmp.randomStringList.add(randomStringItem);
			 									randomList.add(randomStringItem);
			 								}
			 								else
			 								{
			 									randomStringItem[0] = randomStringItemTmp[0];
			 									randomStringItem[1] = randomStringItemTmp[1];
			 									randomStringItem[2] = randomStringItemTmp[2].substring(0,1);
			 									randomStringItem[4] = randomStringItemTmp[3];
			 									
			 									//System.out.println(randomStringItem[0]);
			 									//System.out.println(randomStringItem[1]);
			 									//System.out.println(randomStringItem[2]);
			 									EditorStepTmp.randomStringList.add(randomStringItem);
			 									randomList.add(randomStringItem);
			 								}
			 							}
			 							//fuck
			 						}
			 						else
			 						{
			 							//System.out.println("meiyou");
			 						}
			 						
			 						
			 						
			 						String[] haveCaptcha = tmplines[7].split("captcha:");
			 						if(haveCaptcha.length>1)
			 						{
			 							//System.out.println("you");
			 							String[] captchas = haveCaptcha[1].split(",");
			 							
			 							for(int i=0;i<captchas.length;i++)
			 							{
			 								//System.out.println(captchas[i]);
			 								//System.out.println(Method.getFromBase64(captchas[i]));
			 								String[] captchasItem = new String[4];
			 								String[] captchasItemTmp = Method.getFromBase64(captchas[i]).split(",");
			 								
			 								captchasItem[0] = captchasItemTmp[0];
			 								captchasItem[1] = captchasItemTmp[1];
			 								captchasItem[2] = captchasItemTmp[2];
			 								captchasItem[3] = captchasItemTmp[3];
			 								EditorStepTmp.captchaList.add(captchasItem);
			 								captchaList.add(captchasItem);
			 							}
			 						}
			 						else
			 						{
			 							//System.out.println("meiyou");
			 						}
			 						
			 						
			 						
			 						//System.out.println(tmplines[9]);
			 						String[] haveSetGoaheadOrStopFromStatus = tmplines[9].split("setGoAheadOrStopFromStatus:");
			 						if(haveSetGoaheadOrStopFromStatus.length>1)
			 						{
			 							//System.out.println("you");
			 							String[] setGoaheadOrStopFromStatus = haveSetGoaheadOrStopFromStatus[1].split(",");
			 							for(int i=0;i<setGoaheadOrStopFromStatus.length;i++)
			 							{
			 							//	System.out.println(setGoaheadOrStopFromStatus[i]);
			 								
			 								String[] setGoaheadOrStopFromStatusItemTmp = Method.getFromBase64(setGoaheadOrStopFromStatus[i]).split(",");
			 								String[] setGoaheadOrStopFromStatusItem = new String[2];
			 								setGoaheadOrStopFromStatusItem[0] = setGoaheadOrStopFromStatusItemTmp[0];
			 								setGoaheadOrStopFromStatusItem[1] = setGoaheadOrStopFromStatusItemTmp[1];
			 									
			 								EditorStepTmp.setGoAheadOrStopFromStatusList.add(setGoaheadOrStopFromStatusItem);
			 								DefaultTableModel tableModel = (DefaultTableModel)EditorStepTmp.JPtabEditorSetGoAheadOrStopFromStatusTable.getModel();
			 								tableModel.addRow(new Object[] { Integer.valueOf(setGoaheadOrStopFromStatusItem[0]), setGoaheadOrStopFromStatusItem[1] });
			 							}
			 						}
			 						else
			 						{
			 							//System.out.println("meiyou");
			 						}
			 						
			 						
			 						//System.out.println(tmplines[10]);
			 						String[] haveSetGoAheadOrStopFromData = tmplines[10].split("setGoAheadOrStopFromData:");
			 						if(haveSetGoAheadOrStopFromData.length>1)
			 						{
			 						//	System.out.println("you");
			 							String[] setGoAheadOrStopFromData = haveSetGoAheadOrStopFromData[1].split(",");
			 							for(int i=0;i<setGoAheadOrStopFromData.length;i++)
			 							{
			 							//	System.out.println(setGoAheadOrStopFromData[i]);
			 								
			 								String[] setGoAheadOrStopFromDataItemTmp = Method.getFromBase64(setGoAheadOrStopFromData[i]).split(",");
			 								String[] setGoAheadOrStopFromDataItem = new String[2];
			 								setGoAheadOrStopFromDataItem[0] = Method.getFromBase64(setGoAheadOrStopFromDataItemTmp[0]);
			 								setGoAheadOrStopFromDataItem[1] = setGoAheadOrStopFromDataItemTmp[1];
			 									
			 								
			 								
			 								EditorStepTmp.setGoAheadOrStopFromDataList.add(setGoAheadOrStopFromDataItem);
			 								DefaultTableModel tableModel = (DefaultTableModel)EditorStepTmp.JPtabEditorSetGoAheadOrStopFromDataTable.getModel();
			 								tableModel.addRow(new Object[] { setGoAheadOrStopFromDataItem[0], setGoAheadOrStopFromDataItem[1] });
			 								
			 								
			 								//System.out.println(setGoAheadOrStopFromDataItem[0]+"///////////////////////////////////");
			 								
			 							}
			 						}
			 						else
			 						{
			 							//System.out.println("meiyou");
			 						}
			 						
			 						
			 						
			 						
			 						
			 						String[] haveUseFileForCycleWithPackage = tmplines[11].split("useFileForCycleWithPackage:");
			 						if(haveUseFileForCycleWithPackage.length>1)
			 						{
			 						//	System.out.println("you");
			 							//System.out.println(haveUseFileForCycleWithPackage[1]);
			 							//System.out.println(Method.getFromBase64(haveUseFileForCycleWithPackage[1]));
			 							String[] cycleWithPackages = Method.getFromBase64(haveUseFileForCycleWithPackage[1]).split(",");
			 						//	System.out.println(cycleWithPackages.length);
			 							String[] cycleWithPackage = new String[6];
			 							if(cycleWithPackages[0].equals("1"))
			 							{
			 								cycleWithPackage[0] = cycleWithPackages[0];
			 								cycleWithPackage[1] = ExpDiceory+Method.getFromBase64(cycleWithPackages[1]);
			 								cycleWithPackage[4] = Method.getFromBase64(cycleWithPackages[4]);
			 								EditorStepTmp.useFileForCycleWithPackageItem = cycleWithPackage;
			 								//System.out.println(cycleWithPackage[1]);
			 							}
			 							else if(cycleWithPackages[0].equals("2"))
			 							{
			 								cycleWithPackage[0] = cycleWithPackages[0];
			 								cycleWithPackage[1] = ExpDiceory+Method.getFromBase64(cycleWithPackages[1]);
			 								cycleWithPackage[2] = ExpDiceory+Method.getFromBase64(cycleWithPackages[2]);
			 								cycleWithPackage[3] = cycleWithPackages[3];
			 								cycleWithPackage[4] = Method.getFromBase64(cycleWithPackages[4]);
			 								cycleWithPackage[5] = Method.getFromBase64(cycleWithPackages[5]);
			 								EditorStepTmp.useFileForCycleWithPackageItem = cycleWithPackage;
			 								
			 								//System.out.println(cycleWithPackage[1]);
			 							}
			 						}
			 						else
			 						{
			 							//System.out.println("meiyou");
			 						}
			 						
			 						
			 						String[] haveUseFileForCycleWithExp = tmplines[12].split("fileForCycleWithEXP:");
			 						if(haveUseFileForCycleWithExp.length>1)
			 						{
			 							//System.out.println(Method.getFromBase64(haveUseFileForCycleWithExp[1])+"+++++++++++++++++");
			 							EditorStepTmp.FileForCycleWithEXP = ExpDiceory+Method.getFromBase64(haveUseFileForCycleWithExp[1]).split(",")[0];
			 							EditorStepTmp.GrobalFileInStep = Integer.valueOf(Method.getFromBase64(haveUseFileForCycleWithExp[1]).split(",")[1]);
			 							EditorStepTmp.FileForCycleWithEXPStrings[0] = EditorStepTmp.FileForCycleWithEXP;
			 							EditorStepTmp.FileForCycleWithEXPStrings[1] = Method.getFromBase64(haveUseFileForCycleWithExp[1]).split(",")[1];
			 							EditorStepTmp.FileForCycleWithEXPStrings[2] = Method.getFromBase64(haveUseFileForCycleWithExp[1]).split(",")[2];
			 						}
								}
								
								
								
								
								Thread setFontThread = new Thread(new Runnable()
					    		{
					    			public void run()
					    			{
					    				JLabel labelTmp = new JLabel("Editor");
					    				labelTmp.setForeground(Color.red);
					    				JPtab.setTabComponentAt(1, labelTmp);
					    				try
					    				{
					    					Thread.sleep(3000);
					    				}
					    				catch (Exception e)
					    				{
					    					e.printStackTrace();
					    				}
					    				labelTmp.setForeground(null);
					    				JPtab.setTabComponentAt(1, labelTmp);
					    			}
					    		});
					    		setFontThread.start();
								
								
								
								
							} 
							catch (Exception e1) 
							{
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							
	 					}
	 				});
	 				
	 				expFileEditDeleteItem.addActionListener(new ActionListener (){
	 					@Override
	 					public void actionPerformed(ActionEvent e)
	 					{
	 						String ExpName = "Exps/"+tmp2;
	 						//System.out.println(ExpName);
	 						boolean deleted = Method.deleteDirectory(ExpName);
	 						//System.out.println("deleted"+deleted);
	 						
	 						//refresh
							DefaultTableModel expstablemodel = (DefaultTableModel) JDPDetectionLeftPanelTable.getModel();	
							expsFileNameList.clear();
							expstablemodel.setRowCount(0);
							try
							{
								File ExpsDictory = new File("Exps");
								if(ExpsDictory.exists())
								{
									if(ExpsDictory.isDirectory())
									{
										//System.out.println("OK");
										File[] ExpsFiles = ExpsDictory.listFiles();
										for(File f:ExpsFiles)
										{
											if(f.isDirectory())
											{
												expsFileNameList.add(f.getName());
												expstablemodel.addRow(new Object[]{false,f.getName()});
											}
										}
									}
									else
									{
										//System.out.println("Exps不是目录");
									}
								}
								else
								{
									ExpsDictory.mkdir();
								}
							}
							catch(Exception es)
							{
								es.printStackTrace();
							}
	 						
	 						
	 					}
	 				});
	 				
	 				
	 				expFileEditAboutItem.addActionListener(new ActionListener() {
						
						@Override
						public void actionPerformed(ActionEvent ef) {
							
							String aboutFileString = "Exps/"+tmp2+"/about.txt";
							//System.out.println(aboutFileString);
							File aboutFile = new File(aboutFileString);
							String aboutFileBufferString="";
							String readLineTmp="";
							try
							{
								InputStreamReader aboutFileInputSteamReadTmp = new InputStreamReader(new FileInputStream(aboutFile));
								BufferedReader aboutFileBufferedReader = new BufferedReader(aboutFileInputSteamReadTmp);
								while((readLineTmp=aboutFileBufferedReader.readLine())!=null)
								{
									aboutFileBufferString = aboutFileBufferString+readLineTmp+"\n";
								}
								//System.out.println(aboutFileBufferString);
								
								final JDialog siteEditDialog = new JDialog();
								siteEditDialog.setAlwaysOnTop(true);
								siteEditDialog.setTitle(tmp2);
						 		siteEditDialog.setLayout(new BorderLayout());
						 		siteEditDialog.setLocation(e.getX(), e.getY());
						 		siteEditDialog.setSize(300, 180);
						 		siteEditDialog.setVisible(true);
						 		
						 		JTextArea aboutStrings = new JTextArea();
						 		aboutStrings.setLineWrap(true);
						 		aboutStrings.setText(aboutFileBufferString);
						 		siteEditDialog.add(aboutStrings,BorderLayout.CENTER);
							}
							catch(Exception es)
							{
								JOptionPane.showMessageDialog(null, "找不到about.txt文件", "Warning", 2);
							}
						}
	 				});
	 				
	 				
	 				expFileEditChangeNameItem.addActionListener(new ActionListener() {
						
						@Override
						public void actionPerformed(ActionEvent ef) {
							String ExpName = JOptionPane.showInputDialog(null,"Exp 名称",tmp2);
							
							if(ExpName!=null)
							{
								//System.out.println("fuck");
								//System.out.println(ExpName);
								//System.out.println(tmp2);
								String oldExpName = "Exps/"+tmp2;
								String newExpName = "Exps/"+ExpName;
								//System.out.println(newExpName);
								//System.out.println(oldExpName);
								File oldExpFile = new File(oldExpName);
								oldExpFile.renameTo(new File(newExpName));
								
								
								
								//refresh
								DefaultTableModel expstablemodel = (DefaultTableModel) JDPDetectionLeftPanelTable.getModel();	
								expsFileNameList.clear();
								expstablemodel.setRowCount(0);
								try
								{
									File ExpsDictory = new File("Exps");
									if(ExpsDictory.exists())
									{
										if(ExpsDictory.isDirectory())
										{
											//System.out.println("OK");
											File[] ExpsFiles = ExpsDictory.listFiles();
											for(File f:ExpsFiles)
											{
												if(f.isDirectory())
												{
													expsFileNameList.add(f.getName());
													expstablemodel.addRow(new Object[]{false,f.getName()});
												}
											}
										}
										else
										{
											//System.out.println("Exps不是目录");
										}
									}
									else
									{
										ExpsDictory.mkdir();
									}
								}
								catch(Exception es)
								{
									es.printStackTrace();
								}
							}
						}
	 				});
	 				
	 			 }
	 		  }
	 	  });
		
		//初始化 exp 列表的table 数据   
		final DefaultTableModel expstablemodel = (DefaultTableModel) JDPDetectionLeftPanelTable.getModel();	
		
		try
		{
			File ExpsDictory = new File("Exps"); //获取当前目录下 exps 目录下所有文件名 作为exp 的列表       规定  exps 是存放exp 列表的目录 通过 文件夹来区分exp
			if(ExpsDictory.exists()) //判断该文件是否存在
			{
				if(ExpsDictory.isDirectory())// 当文件存在的时候 判断是否是目录
				{
					//System.out.println("OK");
					File[] ExpsFiles = ExpsDictory.listFiles(); //目录就列出该目录下所有的文件夹 
					for(File f:ExpsFiles)
					{
						if(f.isDirectory())
						{
							expsFileNameList.add(f.getName()); //添加到 expsFileNameList 方便后面  做查询用途  省得每次 都要读取table 列表
							expstablemodel.addRow(new Object[]{false,f.getName()});//添加到表格上
						}
					}
				}
				else
				{
					//System.out.println("Exps不是目录");
				}
			}
			else
			{
				ExpsDictory.mkdir();//如果不存在这个文件  则创建 目录
			}
		}
		catch(Exception es)
		{
			es.printStackTrace();
		}
		
		
		
		JPanel JDPDetectionRightPanel = new JPanel();// Detection 的右侧主 面板  有 查询exp  验证 exp  显示请求和回显
		JDPDetectionRightPanel.setLayout(new BorderLayout());
		JDPDetectionRightPanel.setBackground(Color.yellow);
		JDPDetectionRightPanel.setSize(1000,0);
		
		
		JPanel JDPDetectionRightPanelConfigPanel = new JPanel();//Detection 的右侧 exp 查询 和 更新 导出的面板
		JDPDetectionRightPanelConfigPanel.setPreferredSize(new Dimension(0,70));
		JDPDetectionRightPanelConfigPanel.setMaximumSize(new Dimension(0,70));
		JDPDetectionRightPanelConfigPanel.setLayout(null);
		
		JLabel JDPDetectionRightPanelConfigPanelSearchLabel = new JLabel("Search :");
		JDPDetectionRightPanelConfigPanelSearchLabel.setSize(60,30);
		JDPDetectionRightPanelConfigPanelSearchLabel.setLocation(10,10);
		
		//作为 Detection 的右侧 exp 查询 和 更新 导出的面板 上输入正则的  文本框
		final JTextField JDPDetectionRightPanelConfigPanelText = new  JTextField();
		JDPDetectionRightPanelConfigPanelText.setLocation(70, 10);
		JDPDetectionRightPanelConfigPanelText.setSize(300,30);
		
		
		//只要有按键就触发  实时
		JDPDetectionRightPanelConfigPanelText.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
				String ExpsNameRegex = JDPDetectionRightPanelConfigPanelText.getText().trim();//获取 文本
				List<String> expsShowList = new ArrayList<String>();//用来存储可以显示的匹配到 exps 文件名
				Pattern p=Pattern.compile(ExpsNameRegex); //匹配模版
				int expsFileCount = expsFileNameList.size();
				for(int i=0;i<expsFileCount;i++) //循环匹配  原来的 List  匹配成功的加入 expsShowList
				{
					String ExpName = expsFileNameList.get(i);
					Matcher m=p.matcher(ExpName);
					if(m.find())
					{
						expsShowList.add(ExpName);
					}
				}
				
				
				DefaultTableModel expstablemodel = (DefaultTableModel) JDPDetectionLeftPanelTable.getModel();
				expstablemodel.setRowCount(0);// 先清空原来的table 数据 
				
				int expsShowCount = expsShowList.size();
				for(int i=0;i<expsShowCount;i++) //把匹配到的 列表 重新添加到table
				{
					expstablemodel.addRow(new Object[]{false,expsShowList.get(i)});
				}
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
				
				
				
			}
		});
		
		//刷新按钮 ， 如果 exps 目录 的exp 更新之后 ，点击这个按钮 就不用重启程序 
		JButton JDPDetectionRightPanelConfigPanelRefreshButton = new JButton("Refresh");
		JDPDetectionRightPanelConfigPanelRefreshButton.setFocusable(false);
		JDPDetectionRightPanelConfigPanelRefreshButton.setLocation(410, 10);
		JDPDetectionRightPanelConfigPanelRefreshButton.setSize(100,30);
		
		//这个过程就是初始化table 的完全一样   只是先清空 table 的数据 再添加上去
		JDPDetectionRightPanelConfigPanelRefreshButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				
						
				
				expsFileNameList.clear();
				expstablemodel.setRowCount(0);
				try
				{
					File ExpsDictory = new File("Exps");
					if(ExpsDictory.exists())
					{
						if(ExpsDictory.isDirectory())
						{
							//System.out.println("OK");
							File[] ExpsFiles = ExpsDictory.listFiles();
							for(File f:ExpsFiles)
							{
								if(f.isDirectory())
								{
									expsFileNameList.add(f.getName());
									expstablemodel.addRow(new Object[]{false,f.getName()});
								}
							}
						}
						else
						{
							//System.out.println("Exps不是目录");
						}
					}
					else
					{
						ExpsDictory.mkdir();
					}
				}
				catch(Exception es)
				{
					es.printStackTrace();
				}
			}
		});
		
		//Export 按钮 目的 是 可以选中某些exp 导出作为你单独的模块来测试运行 
		JButton JDPDetectionRightPanelConfigPanelExportButton = new JButton("Export");
		JDPDetectionRightPanelConfigPanelExportButton.setFocusable(false);
		JDPDetectionRightPanelConfigPanelExportButton.setLocation(480, 10);
		JDPDetectionRightPanelConfigPanelExportButton.setSize(100,30);
		
		//把 exp 和 Detection封装到 jar里面
		JDPDetectionRightPanelConfigPanelExportButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				try {
					String strline="";
					Process p=Runtime.getRuntime().exec("cmd /c jar -cvfm hellojar.jar config.txt example");
				   	BufferedReader br=new BufferedReader(new InputStreamReader(p.getInputStream()));
				   	while((strline=br.readLine())!=null)
				   	{
				   	  //	System.out.println(strline);
						
				   	}    
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		
		JDPDetectionRightPanelConfigPanel.add(JDPDetectionRightPanelConfigPanelSearchLabel);
		JDPDetectionRightPanelConfigPanel.add(JDPDetectionRightPanelConfigPanelText);
		JDPDetectionRightPanelConfigPanel.add(JDPDetectionRightPanelConfigPanelRefreshButton);
		//JDPDetectionRightPanelConfigPanel.add(JDPDetectionRightPanelConfigPanelExportButton);
		
		//Detection 的右侧检测面板   URL Detection 和 request response
		JPanel JDPDetectionRightPanelTargetGoPanel = new JPanel();
		JDPDetectionRightPanelTargetGoPanel.setBackground(Color.white);
		JDPDetectionRightPanelTargetGoPanel.setPreferredSize(new Dimension(0,70));
		JDPDetectionRightPanelTargetGoPanel.setMaximumSize(new Dimension(0,70));
		JDPDetectionRightPanelTargetGoPanel.setLayout(new BorderLayout());
		
		//Detection 的 URL  Detecion按钮面板
		JPanel JDPDetectionRightPanelTargetGoPanelEditPanel = new JPanel();
		//JDPDetectionRightPanelTargetGoPanelEditPanel.setBackground(Color.blue);
		JDPDetectionRightPanelTargetGoPanelEditPanel.setPreferredSize(new Dimension(0,70));
		JDPDetectionRightPanelTargetGoPanelEditPanel.setMaximumSize(new Dimension(0,70));
		JDPDetectionRightPanelTargetGoPanelEditPanel.setLayout(null);

		JLabel JDPDetectionRightPanelTargetGoPanelEditPanelTargetLabel = new JLabel("Target :");
		JDPDetectionRightPanelTargetGoPanelEditPanelTargetLabel.setSize(60,30);
		JDPDetectionRightPanelTargetGoPanelEditPanelTargetLabel.setLocation(10, 10);
		
		//填写检测URL 的text控件
		final JTextField JDPDetectionRightPanelTargetGoPanelEditPanelText = new JTextField();
		JDPDetectionRightPanelTargetGoPanelEditPanelText.setLocation(70, 10);
		JDPDetectionRightPanelTargetGoPanelEditPanelText.setSize(300,30);
		
		//检测目标的 按钮
		final JButton JDPDetectionRightPanelTargetGoPanelEditPanelButton = new JButton("Detection");
		JDPDetectionRightPanelTargetGoPanelEditPanelButton.setFocusable(false);
		JDPDetectionRightPanelTargetGoPanelEditPanelButton.setLocation(410, 10);
		JDPDetectionRightPanelTargetGoPanelEditPanelButton.setSize(100,30);
		
		
		
		String[] JDPDetectionRightPanelTargetGoPanelShowPanelTableHeaders = { "ExpName", "TextForExpCycle","TextForCyclePackage","WhyToStop","HTTP Data"};
	    Object[][] JDPDetectionRightPanelTargetGoPanelShowPanelTableCellData = null;
	    final DefaultTableModel JDPDetectionRightPanelTargetGoPanelShowPanelTableModel = new DefaultTableModel(JDPDetectionRightPanelTargetGoPanelShowPanelTableCellData, JDPDetectionRightPanelTargetGoPanelShowPanelTableHeaders) {
	    	public boolean isCellEditable(int row, int column) 
	    	{
	    		if ((column == 4) )
	 				  return false;
	    		return true;
	    	}
	    };
	    final JTable JDPDetectionRightPanelTargetGoPanelShowPanelTable = new JTable(JDPDetectionRightPanelTargetGoPanelShowPanelTableModel);
	   
	    
	    JDPDetectionRightPanelTargetGoPanelShowPanelTable.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
				if((e.getModifiers()&InputEvent.BUTTON3_MASK)!=0)
	 			 {
					
	 				
	 				
	 				PopupMenu DetectionItemPopupMenu = new PopupMenu();				
	 				MenuItem DetectionShowTalbeRightItemDelete = new MenuItem();
	 				DetectionShowTalbeRightItemDelete.setLabel("Delete This Item");
	 				MenuItem DetectionShowTalbeRightClear = new MenuItem();
	 				DetectionShowTalbeRightClear.setLabel("Clear");
	 				
	 				DetectionItemPopupMenu.add(DetectionShowTalbeRightItemDelete);
	 				DetectionItemPopupMenu.add(DetectionShowTalbeRightClear);
	 				JDPDetectionRightPanelTargetGoPanelShowPanelTable.add(DetectionItemPopupMenu);				
	 				DetectionItemPopupMenu.show(JDPDetectionRightPanelTargetGoPanelShowPanelTable, e.getX(), e.getY());
	 				final DefaultTableModel tableModel = (DefaultTableModel)JDPDetectionRightPanelTargetGoPanelShowPanelTable.getModel();
	 				
	 				
	 				DetectionShowTalbeRightItemDelete.addActionListener(new ActionListener() {
						
						@Override
						public void actionPerformed(ActionEvent e) {
							int index = JDPDetectionRightPanelTargetGoPanelShowPanelTable.getSelectedRow();
							expHTTPHistoryList.remove(index);  //////////  清楚的时候 记得 清楚 list的数据   。。。。。。。。。。。。。。。。。。。。   clear 的还没做
							//System.out.println(index);
							tableModel.removeRow(index);
						}
	 				});
	 				
	 				DetectionShowTalbeRightClear.addActionListener(new ActionListener(){
	 					
	 					@Override
	 					public void actionPerformed(ActionEvent e){
	 						 expHTTPHistoryList.clear();
	 		 				 tableModel.setRowCount(0); 
	 					}
	 				});
	 			 }
			}
		});
	   
	    
	    TableColumn JDPDetectionRightPanelTargetGoPanelShowPanelTableFirsetColumn = JDPDetectionRightPanelTargetGoPanelShowPanelTable.getColumnModel().getColumn(0);
	    JDPDetectionRightPanelTargetGoPanelShowPanelTableFirsetColumn.setPreferredWidth(90);
	    //JDPDetectionRightPanelTargetGoPanelShowPanelTableFirsetColumn.setWidth(90);
	    //JDPDetectionRightPanelTargetGoPanelShowPanelTableFirsetColumn.setMaxWidth(90);
	    //JDPDetectionRightPanelTargetGoPanelShowPanelTableFirsetColumn.setMinWidth(30);
        
        TableColumn JDPDetectionRightPanelTargetGoPanelShowPanelTableSecondColumn = JDPDetectionRightPanelTargetGoPanelShowPanelTable.getColumnModel().getColumn(1);
        JDPDetectionRightPanelTargetGoPanelShowPanelTableSecondColumn.setPreferredWidth(120);
        //JDPDetectionRightPanelTargetGoPanelShowPanelTableSecondColumn.setMaxWidth(110);
        //JDPDetectionRightPanelTargetGoPanelShowPanelTableSecondColumn.setMinWidth(30);
        
        TableColumn JDPDetectionRightPanelTargetGoPanelShowPanelTableThirdColumn = JDPDetectionRightPanelTargetGoPanelShowPanelTable.getColumnModel().getColumn(2);
        JDPDetectionRightPanelTargetGoPanelShowPanelTableThirdColumn.setPreferredWidth(130);
        //JDPDetectionRightPanelTargetGoPanelShowPanelTableThirdColumn.setMaxWidth(120);
        //JDPDetectionRightPanelTargetGoPanelShowPanelTableThirdColumn.setMinWidth(30);
        
        TableColumn JDPDetectionRightPanelTargetGoPanelShowPanelTableFourColumn = JDPDetectionRightPanelTargetGoPanelShowPanelTable.getColumnModel().getColumn(3);
        JDPDetectionRightPanelTargetGoPanelShowPanelTableFourColumn.setPreferredWidth(600);
        //JDPDetectionRightPanelTargetGoPanelShowPanelTableFourColumn.setMaxWidth(120);
        
        TableColumn JDPDetectionRightPanelTargetGoPanelShowPanelTableFiveColumn = JDPDetectionRightPanelTargetGoPanelShowPanelTable.getColumnModel().getColumn(4);
        JDPDetectionRightPanelTargetGoPanelShowPanelTableFiveColumn.setPreferredWidth(40);
        //JDPDetectionRightPanelTargetGoPanelShowPanelTableFiveColumn.setMaxWidth(90);
        //JDPDetectionRightPanelTargetGoPanelShowPanelTableFiveColumn.setMinWidth(30);
	    
	    JScrollPane JDPDetectionRightPanelTargetGoPanelShowPanelScollPanel = new JScrollPane(JDPDetectionRightPanelTargetGoPanelShowPanelTable);
		
	    JDPDetectionRightPanelTargetGoPanelShowPanelTable.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
				
				int row = JDPDetectionRightPanelTargetGoPanelShowPanelTable.getSelectedRow();
			    int column = JDPDetectionRightPanelTargetGoPanelShowPanelTable.getSelectedColumn();
		    	
			    if(column==4)
			    {
			    	
				    
				    JFrame DataShowFrame  =new  JFrame();
			    	DataShowFrame.setExtendedState(6);
			    	DataShowFrame.setLayout(new BorderLayout());
			    	//DataShowFrame.setDefaultCloseOperation(3); //设置主窗口 关闭按钮的 状态 ， 即为 退出程序
			    	DataShowFrame.setPreferredSize(new Dimension(600,400));
			    	DataShowFrame.setMinimumSize(new Dimension(600, 400)); //设置主窗口 最小化 的大小
			    	DataShowFrame.setVisible(true);
			    	JTextArea showDataText = new JTextArea("");
			    	JScrollPane showDataScroll = new JScrollPane(showDataText);
			    	DataShowFrame.add(showDataScroll,BorderLayout.CENTER);
			    	
			    	String[] HTTPData = expHTTPHistoryList.get(row);
			    	String requestData = HTTPData[1];
			    	String responseData = HTTPData[3];
			    	
			    	showDataText.setText(String.valueOf(row));
			    	//System.out.println(responseData);
			    	
			    	//System.out.println(row);
			    	showDataText.setText("Request::"+"\r\n\r\n\r\n"+requestData+"\r\n\r\n\r\n\r\n\r\n\r\n\r\n"+"Response"+"\r\n\r\n\r\n"+responseData);
			    	showDataText.setSelectionStart(1);
			    	showDataText.setSelectionEnd(1);
			    	
			    }
			}
		});
		
		
		//通过先获取URL 和  要检测exp   来一个一个 发包
		JDPDetectionRightPanelTargetGoPanelEditPanelButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				
				Thread startThread = new Thread(new Runnable() {
					public void run() {

						JDPDetectionRightPanelTargetGoPanelEditPanelButton.setEnabled(false);
						//虚拟的目标主机 
						String targetHost = "www.baidu.com";
						//虚拟的目标端口
						int targetPort = 80;
						
						String previousURL="";
						
						String targetTmp = JDPDetectionRightPanelTargetGoPanelEditPanelText.getText().trim();// 将要检测是字符串   example：www.baidu.com
						targetTmp = targetTmp.replaceAll("\\\\","/");// 替换掉  \ 为  /
						
						//分为是否有  http://
						if(targetTmp.toLowerCase().startsWith("http://"))
						{
							//如果有 http:// 直接去掉 
							targetTmp = targetTmp.substring(7);
							
							//后续操作就和没有http:// 一样
							
							//以 冒号来分割   判断是否存在端口号
							String[] targetStringsTmp = targetTmp.split(":");
							try
							{
								int previousIntTmp=0;
								//这部分是测试 是否存在  /  存在  / 这个就判断为 有 url前缀
								try
								{
									previousIntTmp = targetTmp.indexOf("/");
								}
								catch(Exception es)
								{
									es.printStackTrace();
								}
								
								
								if(targetStringsTmp.length>1)// 有端口
								{
									targetHost = (targetStringsTmp[0]);
									if(previousIntTmp>0) //有url 前缀
									{
										targetPort = Integer.valueOf(targetStringsTmp[1].substring(0,targetStringsTmp[1].indexOf("/")));
										previousURL = targetStringsTmp[1].substring(targetStringsTmp[1].indexOf("/"));
									}
									else //没有url qianzhui 
									{
										targetPort = Integer.valueOf(targetStringsTmp[1]);
										previousURL="";
									}
									
								}
								else //没有端口
								{
									if(previousIntTmp>0)//有url 前缀
									{
										targetHost = targetTmp.substring(0,previousIntTmp);
										previousURL = targetTmp.substring(previousIntTmp);
									}
									else//没有url 前缀
									{
										targetHost = targetTmp;
										previousURL="";
									}
								}
								
								
							}
							catch(Exception eg2)
							{
								targetPort=80;
								eg2.printStackTrace();
								
							}
						}
						else
						{
							String[] targetStringsTmp = targetTmp.split(":");
							try
							{
								int previousIntTmp=0;
								try
								{
									previousIntTmp = targetTmp.indexOf("/");
								}
								catch(Exception es)
								{
									es.printStackTrace();
								}
								
								if(targetStringsTmp.length>1)// 有端口
								{
									targetHost = (targetStringsTmp[0]);
									if(previousIntTmp>0) //有url 前缀
									{
										targetPort = Integer.valueOf(targetStringsTmp[1].substring(0,targetStringsTmp[1].indexOf("/")));
										previousURL = targetStringsTmp[1].substring(targetStringsTmp[1].indexOf("/"));
									}
									else //没有url qianzhui 
									{
										targetPort = Integer.valueOf(targetStringsTmp[1]);
										previousURL="";
									}
									
								}
								else //没有端口
								{
									if(previousIntTmp>0)//有url 前缀
									{
										targetHost = targetTmp.substring(0,previousIntTmp);
										previousURL = targetTmp.substring(previousIntTmp);
									}
									else//没有url 前缀
									{
										targetHost = targetTmp;
										previousURL="";
									}
								}
								
								
							}
							catch(Exception eg2)
							{
								targetPort=80;
								eg2.printStackTrace();
								
							}
						}
						
						/*
						String requestDat2a = "GET /previousURL/scada/Login.aspx HTTP/1.1"+"\r\n"+
											 "Host: takipmobil.com"+"\r\n"+
											 "Proxy-Connection: keep-alive"+"\r\n"+
											 "Cache-Control: max-age=0"+"\r\n"+
											 "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp;q=0.8"+"\r\n"+
											 "User-Agent: Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.131 Safari/537.36"+"\r\n"+
											 "Referer: http://www.gfsoso.com/?q=inurl%3Ascada/login&pn=10"+"\r\n"+
											 "Accept-Encoding: gzip,deflate,sdch"+"\r\n"+
											 "Accept-Language: zh-CN,zh;q=0.8,en;q=0.6"+"\r\n"+
											 "Cookie: ASP.NET_SessionId=4prxqtx5hupq3iimnvzejsll"+"\r\n"+
											 "\r\n";

		 
						
						
						System.out.println(requestDat2a);
						System.out.println(previousURL.length());
						
						if(previousURL.length()>0)
						{
							requestDat2a = Method.addPreviousURL(requestDat2a,previousURL);
						}
						
						//System.exit(1);
						*/
						
						
						
						
						
						//System.out.println(previousURL);
						//System.out.println(targetHost);
						//System.out.println(targetPort);
						
						
						
						
						//主tab  Detection 左侧 table 的行数
						int ExpsListCount =  JDPDetectionLeftPanelTable.getRowCount();
						DefaultTableModel  tablemodel = (DefaultTableModel) JDPDetectionLeftPanelTable.getModel();
						//循环获取 table 中被选中的 exp 来执行
						for(int h=0;h<ExpsListCount;h++)
						{
							//选中
							if((boolean)tablemodel.getValueAt(h, 0))
							{
								//默认的exp 都是存放在 exps 这个目录里的
								String ExpName=(String) tablemodel.getValueAt(h, 1);
								String fileDir = "Exps/"+ExpName+"/";

								
								//这是每个Exp 里 step 步骤的  文件名   例如   Step1、Step2
								String StepFileNameTmp = "";

								//这个是统计每个 Step 里 随机字符串设置    的 个数
								List randomStringList = new ArrayList();
								//这个是统计每个 Step 里验证码设置    的 个数
								List captchaList = new ArrayList();
								//这个是统计每个 Step 里。。。设置    的 个数
								List setParamsFromDataPackageList = new ArrayList();
								//这个是统计每个 Step 里设置停止前进 匹配    从返回的 状态 的 个数
								List setGoAheadOrStopFromStatusList = new ArrayList();
								//这个是统计每个 Step 里设置停止前进 匹配    从返回的文本 的 个数
								List setGoAheadOrStopFromDataList = new ArrayList();
								//这个是统计每个 Step 里使用 package  的 个数  最大两个
								List useFileForCycleWithPackageList = new ArrayList();
								//这个为每个Exp中  如果存在 expFileCycle  则 这个字符串就是该文件名
								String fileForCycleWithEXP = "";
				        
								
				        
								//开始每个exp 的检测
								try
								{
									//读取  Exp 的 主配置文件  记录每个步骤的详细
									File file = new File(fileDir + "Step.txt");
									if ((file.isFile()) && (file.exists()))
									{
										InputStreamReader read = new InputStreamReader(new FileInputStream(file));
										BufferedReader bufferedReader = new BufferedReader(read);
										String lineTxt = null;
										
										//先把 exp 的所有步骤全部读取出来存放到  tmplist 里面
										List tmplist = new ArrayList();

										while ((lineTxt = bufferedReader.readLine()) != null)
										{
											tmplist.add(lineTxt);
										}							
										read.close();
										
										//Exp 的步骤数     等下循环是根据步骤数来循环的
										int StepNums = tmplist.size();
										// 
										if (StepNums > 0)
										{
											String tmpStr = (String)tmplist.get(0);//获取第一行  、 这个的目的是为了判断  获取 存在的  CycleWithExp 的文件名，  每个步骤都有记录 这个文件名  所以读取一行就够了
											fileForCycleWithEXP = "";//初始化 存储cyclewithexp 的字符串对象
											String[] lineTxtTmp = tmpStr.split("-");//每个步骤中的 每个参数之间用 - 隔开
											int lineTxtTmpLength = lineTxtTmp.length;//
											if (lineTxtTmpLength == 13)//每个步骤的参数都是固定 13个
											{
												String[] fileForCycleWithEXPStrings = lineTxtTmp[12].trim().split(":");// CycleWithExp  固定位置  读取文件名
												if (fileForCycleWithEXPStrings.length > 1)
												{
													fileForCycleWithEXP = Method.getFromBase64(fileForCycleWithEXPStrings[1].trim()).split(",")[0];
												}									
											}
										}

										// 有 CycleWithExp 文件  
										if (fileForCycleWithEXP.length() > 0)
										{
											File fileForCycleWithEXPFile = new File(fileDir+fileForCycleWithEXP);
											InputStreamReader inputSteamReadTmp = new InputStreamReader(new FileInputStream(fileForCycleWithEXPFile));
											BufferedReader expFileBufferedReader = new BufferedReader(inputSteamReadTmp);
											String expFileLineTxt = null;

											boolean StopExp = false; //停止exp 的标志
											boolean StopCurrent =false;//这个暂时没用
											boolean GoAheadCurrent = false;//这个是停止步骤当前循环 继续下一个步骤
											boolean GoAheadExp = false;//这个是停止所有步骤  继续下一个 exp 的循环
											List ExpPackageHistoryList = new ArrayList();//所有 http 的 发送记录
											        
											int expNum=0;//通过这个来排序   
											//StopExp  标志来判断 是否停止
											while((expFileLineTxt = expFileBufferedReader.readLine()) != null&&!StopExp)
											{					
												expNum++;//累计 个数
												String[] ExpPackagePreviousItem = new String[7];//用来记录上一个数据包的数据 Item  

												//根据步骤数来循环  StopExp 来跳出循环
												for (int i = 0; i < StepNums&&!StopExp; i++)
												{
													//循环获取步骤的相关参数 
													String StringForEachStep = (String)tmplist.get(i);
													if (StringForEachStep.length() > 0)
													{
														StepFileNameTmp = "";//记录  步骤中  要发送的数据的 http报文的  保存文件名		                      
														String requestData = "";//记录 要发送的http 报文
														String responseData = "";//记录 返回的 http 报文
														int responseStatus = 0;//记录 返回的http 状态值
														String whyToStop = "Run Over";//记录 Stop 的原因
														String host = "";//记录 目标主机地址
														int port = 80;//记录目标主机端口  默认为80端口
														String[] ExpPackageHistoryItem = new String[7]; //历史 list 的一个item
				                      											
														String previousData=""; //记录上一个步骤中的 返回的response data  即 ExpPackagePreviousItem【3】
														
														//每个步骤的  list 都不一定一样  
														randomStringList = new ArrayList();
														captchaList = new ArrayList();
														setParamsFromDataPackageList = new ArrayList();
														setGoAheadOrStopFromStatusList = new ArrayList();
														setGoAheadOrStopFromDataList = new ArrayList();
														useFileForCycleWithPackageList = new ArrayList();
														
														//fileForCycleWithEXP = "";
														
														
														//每个步骤中的 每个参数之间用 - 隔开
														String[] lineTxtTmp = StringForEachStep.split("-");
														//每个步骤的参数都是固定 13个
														int lineTxtTmpLength = lineTxtTmp.length;	                      
														if (lineTxtTmpLength == 13)
														{		              
															//先读取  CycleWithPackage  如果有  用来循环
															int useFileForCycleWithPackageTmpLength = lineTxtTmp[11].trim().split(":").length;
															
															if (useFileForCycleWithPackageTmpLength > 1)
															{
																// 有 CyclewithPackage 文件 
																
																
																String useFileForCycleWithPackageString = lineTxtTmp[11].trim().split(":")[1].trim();
																String[] useFileForCycleWithPackageItemStrings = useFileForCycleWithPackageString.split(",");
																//其实这边最多只有一个   参数  useFileForCycleWithPackageItemCount  如果有就是 1
																
																//存储格式     ： base64( 文件个数、 file1、file2、组合方式      )
																int useFileForCycleWithPackageItemCount = useFileForCycleWithPackageItemStrings.length;
																for (int k = 0; k < useFileForCycleWithPackageItemCount; k++)
																{
																	String[] useFileForCycleWithPackageItem = new String[6];
																	useFileForCycleWithPackageItem = Method.getFromBase64(useFileForCycleWithPackageItemStrings[k]).split(",");

																	// 1  说明 只有一个文件  
																	if (Integer.valueOf(useFileForCycleWithPackageItem[0]) == 1)
																	{
																		useFileForCycleWithPackageItem[1] = Method.getFromBase64(useFileForCycleWithPackageItem[1].trim());														
																	}
																	//2  两个文件  
																	else if(Integer.valueOf(useFileForCycleWithPackageItem[0]) == 2)
																	{
																		useFileForCycleWithPackageItem[1] = Method.getFromBase64(useFileForCycleWithPackageItem[1].trim());//file1
																		useFileForCycleWithPackageItem[2] = Method.getFromBase64(useFileForCycleWithPackageItem[2].trim());//file2
																		useFileForCycleWithPackageItem[3] = useFileForCycleWithPackageItem[3].trim();//组合方式
																	}
																	//最多两个文件
																	
																	//添加到 list 里面
																	useFileForCycleWithPackageList.add(useFileForCycleWithPackageItem);
																}
				                              
																
				                              
																//获取 list 的个数   其实 最多只有一个
																int useFileForCycleWithPackageListCount = useFileForCycleWithPackageList.size();
																
																for (int t = 0; t < useFileForCycleWithPackageListCount; t++)
																{
																	//获取 cycleWithPackage 参数
																	String[] useFileForCycleWithPackageItem = (String[])useFileForCycleWithPackageList.get(t);
																	// 一个文件 
																	if (Integer.valueOf(useFileForCycleWithPackageItem[0]) == 1)
																	{
																		// 一个文件  声明文件对象
																		File useFileForCycleWithPackageFile1 = new File(fileDir+useFileForCycleWithPackageItem[1]);
																		InputStreamReader useFileForCycleWithPackageFile1InputSteamReadTmp = new InputStreamReader(new FileInputStream(useFileForCycleWithPackageFile1));
																		BufferedReader useFileForCycleWithPackageFile1ExpFileBufferedReader = new BufferedReader(useFileForCycleWithPackageFile1InputSteamReadTmp);
																		String useFileForCycleWithPackageLineText = null;
																		
																		int PackageNum=0;//这个用来统计累计
																		
																		while ((useFileForCycleWithPackageLineText = useFileForCycleWithPackageFile1ExpFileBufferedReader.readLine()) != null)
																		{																
																			PackageNum++;
																			
																			//第一个参数   存储目标主机地址																	
																			if (lineTxtTmp[1].trim().split(":").length > 1)
																			{
																				host = Method.getFromBase64(lineTxtTmp[1].trim().split(":")[1]);																		
																			}
																			else
																			{
																				//没有主机地址的  直接跳过   到下一个步骤
																				break;
																			}

																			//第二个参数  存储目标端口
																			if (lineTxtTmp[2].trim().split(":").length > 1)
																			{
																				port = Integer.valueOf(Method.getFromBase64(lineTxtTmp[2].trim().split(":")[1]));
																			}
																			else
																			{
																				//没有目标端口的  默认为 80端口
																				port =80;
																			}

																			//第三个参数  存储 http请求的文件名
																			StepFileNameTmp = Method.getFromBase64(lineTxtTmp[3].trim().split(":")[1]);									
																			if(StepFileNameTmp.length()>0)
																			{
																				requestData = Method.bytesToString(Method.HexStringTobytes(Method.stringsToString(Method.readFileToHexStrings(fileDir+"\\"+StepFileNameTmp))));
																			}																
																			//如果目标通过映射访问    也就是 www.xxx.com/phpcms/   这个是跟目录的话   对 exp 来说  需要一个  url 前缀 phpcms/
																			if(previousURL.length()>0)
																			{
																				// 添加 phpcms/  到request 的 GET  phpcms/。。。。  http/1.1
																				requestData = Method.addPreviousURL(requestData,previousURL);
																			}
																										
																			//第四个参数  是否替换 host 和 port   默认都是替换 为目标地址 和端口的
																			String replaceHostPortString = lineTxtTmp[4].trim().split(":")[1].trim();
																			if (replaceHostPortString.equals("true"))
																			{
																				host = targetHost;
																				port = targetPort;
																				requestData = Method.replaceHostPort(requestData,host,port);
																			}
																			else
																			{
																				
																			}
																			
																			//第五个参数  是否使用前一个 response 的  set-cookie  登录的时候用    默认是 使用
																			String useSetCookieFromPreviousString = lineTxtTmp[5].trim().split(":")[1].trim();
																			if (useSetCookieFromPreviousString.equals("true"))
																			{
																				/*
																				previousData = "HTTP/1.1 200 OK"+"\r\n"+
																									  "Date: Sun, 30 Nov 2014 09:16:33 GMT"+"\r\n"+
																									  "Server: Apache/2.2.9 (APMServ) PHP/5.2.6+"+"\r\n"+
																									  "X-Powered-By: PHP/5.2.6"+"\r\n"+
																									  "Set-Cookie: SYSZC=d2d4a91a4e41bea2078b09c29cd6a44a"+"\r\n"+
																									  "Set-Cookie: SYSUSER=admin"+"\r\n"+
																									  "Set-Cookie: SYSNAME=%E7%B3%BB%E7%BB%9F%E7%AE%A1%E7%90%86%E5%91%98"+"\r\n"+
																									  "Set-Cookie: SYSUSERID=3"+"\r\n"+
																									  "Set-Cookie: SYSTM=1417338994"+"\r\n"+
																									  "Content-Length: 2"+"\r\n"+
																									  "Content-Type: text/html"+"\r\n"+
																									  "\r\n"+							  
																									  "OK";
				                                        						*/
				                                        
																				//获取 set-cookie的相关参数
																				String[] tmpmp = Method.getHTTPHeadersParams(previousData);	                                		
																				String setCookieStr = "";
																				try
																				{
																				for(int n=0;n<tmpmp.length;n++)
																				{
																					if(n%2==0)
																					{
																						if(tmpmp[n].startsWith("Set-Cookie"))
																						{
																							setCookieStr =setCookieStr+tmpmp[n+1].trim()+";";
																						}
																					}
																				}
																				String OKrequestData="";		                                		
																				String[] requestDatas = requestData.split("\r\n");
																				//cookie 中如果有参数一样的    要替换掉  比如 原本有 name=admin   而 set-cookie 有 name=manange  要替换
																				boolean addCookie = true;																		
																				for(int n=0;n<requestDatas.length;n++)
																				{		
																					requestDatas[n] = requestDatas[n]+"\r\n";
																					if(requestDatas[n].toLowerCase().trim().startsWith("cookie:"))
																					{
																						String[] tmpCookie = requestDatas[n].toLowerCase().trim().split("cookie:");
																						if(tmpCookie.length>1)
																						{
																							tmpCookie[1] = Method.replaceCookie(tmpCookie[1].trim(),setCookieStr);
																							requestDatas[n] = "Cookie: "+Method.stringsToString(tmpCookie)+"\r\n";
																							addCookie = false;
																						}
																					}
																				}
				                                    	  
																				//
																				if(addCookie)
																				{
																					OKrequestData =Method.addHeader(requestData,"Cookie: ",setCookieStr);			                                			
																				}
																				else
																				{
																					OKrequestData = Method.stringsToString(requestDatas);
																				}
																				requestData = OKrequestData;  
																				}
																				catch(Exception e5)
																				{
																					
																				}
				                                		
				                                		
																				 
				                                        
																			}
																			else
																			{
																				
																			}
				                                      
																			//第六个参数  存储随机字符串的相关参数  
																			int randomStringTmpLength = lineTxtTmp[6].trim().split(":").length;
																			if (randomStringTmpLength > 1)
																			{
																				String randomStringString = lineTxtTmp[6].trim().split(":")[1].trim();
																				String[] randomStringItemStrings = randomStringString.split(",");
																				int randomStringItemCount = randomStringItemStrings.length;
																				//根据设置随机字符串的  个数循环替换request
																				for (int k = 0; k < randomStringItemCount; k++)
																				{
																					//先读取出相关参数
																					String[] randomStringItem = new String[5];
																					randomStringItem[0] = Method.getFromBase64(randomStringItemStrings[k]).split(",")[0];
																					randomStringItem[1] = Method.getFromBase64(randomStringItemStrings[k]).split(",")[1];
																					randomStringItem[2] =Method.getFromBase64(randomStringItemStrings[k]).split(",")[2].substring(0, 1);
																					randomStringItem[4] =  Method.getFromBase64(randomStringItemStrings[k]).split(",")[3];
 																					
																					
																					
																					//randomStringItem[1] ==3  即 选择的类型是  自定义随机数母本的值          
																					//存储随机数设置的格式                 位置（第几个）、类型 、长度             而当类型为3 的时候   长度是和 自定义的母本放在一样的  如          6qwer123  6为长度  后面的为母本
																					//
																					if (Integer.valueOf(randomStringItem[1])== 3)
																					{
																						randomStringItem[3] = randomStringItem[2].substring(1);
																						randomStringItem[2] = randomStringItem[2].substring(0, 1);
																					}

																					// rSI+Num    为随机字符串的位置  即第几个  
																					String strTmp = "rSI"+randomStringItem[0];
																					strTmp = Method.getBase64(strTmp);
																					strTmp = Method.addSignRegex(strTmp);
																					String randomStrTmp = "";
																					
																					switch(Integer.valueOf(randomStringItem[1]))
																					{
																						//0  数字
																						case 0:randomStrTmp =new RandomString().getRandomString(Integer.valueOf(randomStringItem[2]),"i");requestData =requestData.replaceAll(strTmp, randomStrTmp);break;
																						//字母
																						case 1:randomStrTmp =new RandomString().getRandomString(Integer.valueOf(randomStringItem[2]),"l");requestData =requestData.replaceAll(strTmp, randomStrTmp);break;
																						//数字和字母
																						case 2:randomStrTmp =new RandomString().getRandomString(Integer.valueOf(randomStringItem[2]),"il");requestData =requestData.replaceAll(strTmp, randomStrTmp);break;
																						case 3:randomStrTmp =new RandomString().getRandomStringFromDefineString(randomStringItem[3],Integer.valueOf(randomStringItem[2]));requestData =requestData.replaceAll(strTmp, randomStrTmp);break;																		
																					
																					
																					}
																					//存储到list 里面  其实没有用处  为了可能会用到
																					randomStringList.add(randomStringItem);
																				}
																				
																			}
				                                      		                                      	
																			//第七个参数  存储 验证码的相关参数
																			int captchaTmpLength = lineTxtTmp[7].trim().split(":").length;
																			if (captchaTmpLength > 1)
																			{
																				String captchaString = lineTxtTmp[7].trim().split(":")[1].trim();
																				String[] captchaItemStrings = captchaString.split(",");
																				int captchaItemCount = captchaItemStrings.length;
																				//根据设置 验证码的个数来  循环替换request
																				for (int k = 0; k < captchaItemCount; k++)
																				{
																					String[] captchaItem = new String[4];
																					captchaItem = Method.getFromBase64(captchaItemStrings[k]).split(",");
																					
																					captchaList.add(captchaItem);//这个list 同样也是没有用到  
																					
																					//sPFC+Num  验证码的累计个数
																					String captchaStrTmp = "sPFC"+captchaItem[0];
																					captchaStrTmp = Method.getBase64(captchaStrTmp);
																					captchaStrTmp = Method.addSignRegex(captchaStrTmp);
				                                            
																					//这里使用 uu 的云验证码  虽然速度慢了点  但又不是要批量  单个检测 不影响 效果
																					boolean status = UUAPI.checkAPI();

																					if (!status)//一定要的    
																					{
																						//System.out.print("API文件校验失败，无法使用打码服务");
																						JOptionPane.showMessageDialog(null, "API文件校验失败，无法使用打码服务", "Warning", 2);
																					}
																					else
																					{
																						final String ImgURL = "http://"+host+previousURL+captchaItem[1];  //获取验证码的url  
																					//	System.out.println(ImgURL);
																						final int CaptchaNumbersTextInt = Integer.valueOf(captchaItem[2]);//验证码的位数
																						final String saveFileName = "img/tmp.jpg";//临时图片保存的地址
																						
																						String saveAbsolutePath = Method.getImgFromURL(ImgURL,saveFileName);//获取图片  返回保存的绝对路径
																						if (saveAbsolutePath != null)
																						{
																							if (CaptchaNumbersTextInt != 0)
																							{
																								try 
																								{
																									//调用api   获取识别结果      1000 + CaptchaNumbersTextInt  为他们的一种格式      1000   验证码位数几位+几
																									String[] captchaResult = UUAPI.easyDecaptcha(saveFileName, 1000 + CaptchaNumbersTextInt);
																									if (captchaResult[1].length() > 0)
																									{
																										requestData = requestData.replaceAll(captchaStrTmp, captchaResult[1]);   
																									}
																								} 
																								catch (IOException eee) 
																								{																		
																									eee.printStackTrace();
																								}	                                                    
																							}
																							else if(CaptchaNumbersTextInt==0)
																							{
																								//这里会用  不确定的 验证码位数      
																							//	System.out.println("这里会用  不确定的 验证码位数  ");
																							}		                                      					
																						}																				
																					}		                                            
																				}
																			}
				                                                                             
				                                        
																			//这里替换 cyclewithExp      字符串 默认都是  FileForCycleWithEXP
																			String FileForCycleWithEXPStrTmp = "FileForCycleWithEXP";
																			FileForCycleWithEXPStrTmp = Method.getBase64(FileForCycleWithEXPStrTmp);
																			FileForCycleWithEXPStrTmp = Method.addSignRegex(FileForCycleWithEXPStrTmp);
																			requestData = requestData.replaceAll(FileForCycleWithEXPStrTmp, expFileLineTxt);
				                                      		
				                                        
				                                        
																			//这里替换   cyclewithpackage   字符串规则是  File-1  File-2  useFileForCycleWithPackageLineText
																			String useFileForCycleWithPackageLineStrTmp = "File-1";
																			useFileForCycleWithPackageLineStrTmp = Method.getBase64(useFileForCycleWithPackageLineStrTmp);
																			useFileForCycleWithPackageLineStrTmp = Method.addSignRegex(useFileForCycleWithPackageLineStrTmp);
																			requestData = requestData.replaceAll(useFileForCycleWithPackageLineStrTmp, useFileForCycleWithPackageLineText);
				                                      		
				                                      		
																			//发送http 数据
																			responseData = Method.bytesToString(Method.getHttPResponseData(host,port,requestData));
																		//	System.out.println(requestData);
																		//	System.out.println(responseData);
																			responseStatus = Method.getHttpResponseHeaderStatus(responseData);
																			
																			//第九个参数  存储 设置 go or stop  从 response status
																			int setGoAheadOrStopFromStatusTmpLength = lineTxtTmp[9].trim().split(":").length;
																			if (setGoAheadOrStopFromStatusTmpLength > 1)
																			{
																				String setGoAheadOrStopFromStatusString = lineTxtTmp[9].trim().split(":")[1].trim();
																				String[] setGoAheadOrStopFromStatusItemStrings = setGoAheadOrStopFromStatusString.split(",");
																				int setGoAheadOrStopFromStatusItemCount = setGoAheadOrStopFromStatusItemStrings.length;
																				//根据 设置规则的 个数  先取出来  放到 list 里面
																				for (int k = 0; k < setGoAheadOrStopFromStatusItemCount; k++)
																				{
																					String[] setGoAheadOrStopFromStatusItem = new String[2];
																					setGoAheadOrStopFromStatusItem = Method.getFromBase64(setGoAheadOrStopFromStatusItemStrings[k]).split(",");
																					setGoAheadOrStopFromStatusList.add(setGoAheadOrStopFromStatusItem);
																				}
																			}
																			
																			//根据 list 的个数
																			int setGoAheadOrStopFromStatusListCount = setGoAheadOrStopFromStatusList.size();
																			if (setGoAheadOrStopFromStatusListCount > 0)
																			{
																				//循环
																				for (int j = 0; j < setGoAheadOrStopFromStatusListCount; j++)
																				{
																					String[] setGoAheadOrStopFromStatusListItem = (String[])setGoAheadOrStopFromStatusList.get(j);
																					
																					//这个是根据  response status  来判断   如果相等  就匹配 触发规则
																					if (responseStatus == Integer.valueOf(setGoAheadOrStopFromStatusListItem[0]))
																					{
																						String actionForStatus = setGoAheadOrStopFromStatusListItem[1];	                                            			
				                                            			
																						//                         GO ahead current   stop current 
																						///                                               ||
																						//																		
																						///											Go Exp    Stop Exp
																					
																						if(actionForStatus.equals("Go Ahead EXP"))
																						{
																							GoAheadExp  = true;
																							whyToStop = "responseStatus:" + responseStatus + " -- Rule; " + setGoAheadOrStopFromStatusListItem[0] + " --Action; "+ setGoAheadOrStopFromStatusListItem[1];
																							break;
																						}
																						else if(actionForStatus.equals("Stop EXP"))
																						{
																							StopExp = true;		                                            				
																							whyToStop = "responseStatus:" + responseStatus + " -- Rule; " + setGoAheadOrStopFromStatusListItem[0] + " --Action; "+ setGoAheadOrStopFromStatusListItem[1];
																							break;
																						}
																						else if(actionForStatus.equals("Go Ahead Current"))
																						{
																							GoAheadCurrent = true;
																							whyToStop = "responseStatus:" + responseStatus + " -- Rule; " + setGoAheadOrStopFromStatusListItem[0] + " --Action; "+ setGoAheadOrStopFromStatusListItem[1];
																							break;
																						}
																						else if(actionForStatus.equals("Stop Current"))//这个暂时没有想到用途
																						{
																							//StopCurrent = true;
																							//whyToStop = "responseStatus:" + responseStatus + " -- Rule" + setGoAheadOrStopFromStatusListItem[0] + setGoAheadOrStopFromStatusListItem[1];
																							//System.out.println(whyToStop);
																							//break;
																						}                                      			
																						
																					}
																				}
																			}
																			else
																			{
																				//没有   setGoAheadOrStopFromStatusListCount
																			}									
																																		
																			
																			if ((StopExp) )
																			{
																				
																				JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,expFileLineTxt,useFileForCycleWithPackageLineText,whyToStop,"show"});
																				ExpPackageHistoryItem[0] = StepFileNameTmp;
																				ExpPackageHistoryItem[1] = requestData;
																				ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																				previousData = responseData;
																				ExpPackageHistoryItem[3] = responseData;
																				ExpPackageHistoryItem[4] = whyToStop;
																				ExpPackageHistoryList.add(ExpPackageHistoryItem);
																				expHTTPHistoryList.add(ExpPackageHistoryItem);
																				ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																				ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																				ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																				ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																				ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																				ExpPackageHistoryItem = new String[7];
																				responseStatus = 0;
																				responseData = "";
																				whyToStop = "Run Over";
																				break;
																			}
				                                            
																			if(GoAheadCurrent)
																			{
																				JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,expFileLineTxt,useFileForCycleWithPackageLineText,whyToStop,"show"});
																				//当是  Go Aheade 的时候 就放弃循环 继续下一个   Step Num
																				ExpPackageHistoryItem[0] = StepFileNameTmp;
																				ExpPackageHistoryItem[1] = requestData;
																				ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																				previousData = responseData;
																				ExpPackageHistoryItem[3] = responseData;
																				ExpPackageHistoryItem[4] = whyToStop;
																				ExpPackageHistoryList.add(ExpPackageHistoryItem);
																				expHTTPHistoryList.add(ExpPackageHistoryItem);
																				ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																				ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																				ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																				ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																				ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																				ExpPackageHistoryItem = new String[7];
																				responseStatus = 0;
																				responseData = "";
																				whyToStop = "Run Over";
																				GoAheadCurrent = false;
																				break;
																			}
				                                            
																			if(GoAheadExp)
																			{
																				JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,expFileLineTxt,useFileForCycleWithPackageLineText,whyToStop,"show"});
																				ExpPackageHistoryItem[0] = StepFileNameTmp;
																				ExpPackageHistoryItem[1] = requestData;
																				ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																				previousData = responseData;
																				ExpPackageHistoryItem[3] = responseData;
																				ExpPackageHistoryItem[4] = whyToStop;
																				ExpPackageHistoryList.add(ExpPackageHistoryItem);
																				expHTTPHistoryList.add(ExpPackageHistoryItem);
																				ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																				ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																				ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																				ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																				ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																				ExpPackageHistoryItem = new String[7];
																				responseStatus = 0;
																				responseData = "";
																				whyToStop = "Run Over";
																				
																				break;
																			}

																	
																			//根据 response data  来判断 是否stop or go
																			int setGoAheadOrStopFromDataTmpLength = lineTxtTmp[10].trim().split(":").length;
																			if (setGoAheadOrStopFromDataTmpLength > 1)
																			{
																				String setGoAheadOrStopFromDataString = lineTxtTmp[10].trim().split(":")[1].trim();
																				String[] setGoAheadOrStopFromDataItemStrings = setGoAheadOrStopFromDataString.split(",");
																				int setGoAheadOrStopFromDataItemCount = setGoAheadOrStopFromDataItemStrings.length;
																				for (int k = 0; k < setGoAheadOrStopFromDataItemCount; k++)
																				{
																					String[] setGoAheadOrStopFromDataItem = new String[2];
																					setGoAheadOrStopFromDataItem = Method.getFromBase64(setGoAheadOrStopFromDataItemStrings[k]).split(",");
																					setGoAheadOrStopFromDataList.add(setGoAheadOrStopFromDataItem);																			
																				}
																			}
																			
																																			
																			int j;
																			int setGoAheadOrStopFromDataListCount = setGoAheadOrStopFromDataList.size();
																			if (setGoAheadOrStopFromDataListCount > 0)
																			{
																				for (j = 0; j < setGoAheadOrStopFromDataListCount; j++)
																				{
																					String[] setGoAheadOrStopFromDataListItem = (String[])setGoAheadOrStopFromDataList.get(j);
																					Pattern p=Pattern.compile(Method.getFromBase64(setGoAheadOrStopFromDataListItem[0]));
																					Matcher m=p.matcher(responseData);
																					//System.out.println(m.find());
																					
																					if (m.find())
																					{
																						String actionForData = setGoAheadOrStopFromDataListItem[1];
																						
																						if(actionForData.equals("Go Ahead EXP"))
																						{
																							GoAheadExp  = true;
																							whyToStop = " -- Rule; " + Method.getFromBase64(setGoAheadOrStopFromDataListItem[0]) +"  --result; "+m.group()+ " --Action; "+setGoAheadOrStopFromDataListItem[1];
																							break;
																						}
																						else if(actionForData.equals("Stop EXP"))
																						{
																							StopExp = true;
																							whyToStop = " -- Rule; " + Method.getFromBase64(setGoAheadOrStopFromDataListItem[0]) +"  --result; "+m.group()+ " --Action; "+setGoAheadOrStopFromDataListItem[1];
																							break;
																						}
																						else if(actionForData.equals("Go Ahead Current"))
																						{
																							GoAheadCurrent = true;
																							whyToStop = " -- Rule; " + Method.getFromBase64(setGoAheadOrStopFromDataListItem[0]) +"  --result; "+m.group()+ " --Action; "+setGoAheadOrStopFromDataListItem[1];
																							break;
																						}
																						else if(actionForData.equals("Stop Current"))
																						{
																							//StopCurrent = true;
																							//whyToStop = "responseStatus:" + responseStatus + " -- Rule" + setGoAheadOrStopFromStatusListItem[0] + setGoAheadOrStopFromStatusListItem[1];
																							//System.out.println(whyToStop);
																							//break;
																						} 																			
																					}
																				}
																			}
																			else
																			{
																				// 没有  setGoAheadOrStopFromDataList");
																			}

																			if ((StopExp) )
																			{
																				JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,expFileLineTxt,useFileForCycleWithPackageLineText,whyToStop,"show"});
																				ExpPackageHistoryItem[0] = StepFileNameTmp;
																				ExpPackageHistoryItem[1] = requestData;
																				ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																				previousData = responseData;
																				ExpPackageHistoryItem[3] = responseData;
																				ExpPackageHistoryItem[4] = whyToStop;
																				ExpPackageHistoryList.add(ExpPackageHistoryItem);
																				expHTTPHistoryList.add(ExpPackageHistoryItem);
																				ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																				ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																				ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																				ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																				ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																				ExpPackageHistoryItem = new String[7];
																				responseStatus = 0;
																				responseData = "";
																				whyToStop = "Run Over";
																				
																				break;
																			}
				                                            
																			if(GoAheadCurrent)
																			{
																				JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,expFileLineTxt,useFileForCycleWithPackageLineText,whyToStop,"show"});
																				ExpPackageHistoryItem[0] = StepFileNameTmp;
																				ExpPackageHistoryItem[1] = requestData;
																				ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																				previousData = responseData;
																				ExpPackageHistoryItem[3] = responseData;
																				ExpPackageHistoryItem[4] = whyToStop;
																				ExpPackageHistoryList.add(ExpPackageHistoryItem);
																				expHTTPHistoryList.add(ExpPackageHistoryItem);
																				ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																				ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																				ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																				ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																				ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																				ExpPackageHistoryItem = new String[7];
																				responseStatus = 0;
																				responseData = "";
																				whyToStop = "Run Over";
																				GoAheadCurrent = false;
																				break;
																			}
				                                            
																			if(GoAheadExp)
																			{
																				JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,expFileLineTxt,useFileForCycleWithPackageLineText,whyToStop,"show"});
																				ExpPackageHistoryItem[0] = StepFileNameTmp;
																				ExpPackageHistoryItem[1] = requestData;
																				ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																				previousData = responseData;
																				ExpPackageHistoryItem[3] = responseData;
																				ExpPackageHistoryItem[4] = whyToStop;
																				ExpPackageHistoryList.add(ExpPackageHistoryItem);
																				expHTTPHistoryList.add(ExpPackageHistoryItem);
																				ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																				ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																				ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																				ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																				ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																				ExpPackageHistoryItem = new String[7];
																				responseStatus = 0;
																				responseData = "";
																				whyToStop = "Run Over";
																				break;
																			}

																			
																			JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,expFileLineTxt,useFileForCycleWithPackageLineText,whyToStop,"show"});
																			ExpPackageHistoryItem[0] = StepFileNameTmp;
																			ExpPackageHistoryItem[1] = requestData;
																			ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																			previousData = responseData;
																			ExpPackageHistoryItem[3] = responseData;
																			ExpPackageHistoryItem[4] = whyToStop;
																			ExpPackageHistoryList.add(ExpPackageHistoryItem);
																			expHTTPHistoryList.add(ExpPackageHistoryItem);
																			ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																			ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																			ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																			ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																			ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																			ExpPackageHistoryItem = new String[7];
																			responseStatus = 0;
																			responseData = "";
																			whyToStop = "Run Over";
				                                      	
																		}
																	}
																	// 两个 cyclewithpackage  
																	else if(Integer.valueOf(useFileForCycleWithPackageItem[0])==2)
																	{																
																		//组合方式1   一对一
																		if(Integer.valueOf(useFileForCycleWithPackageItem[3])==1)
																		{
																			//
																			File useFileForCycleWithPackageFile1 = new File(fileDir+useFileForCycleWithPackageItem[1]);
																			File useFileForCycleWithPackageFile2 = new File(fileDir+useFileForCycleWithPackageItem[2]);
																			
																			InputStreamReader useFileForCycleWithPackageFile1InputSteamReadTmp = new InputStreamReader(new FileInputStream(useFileForCycleWithPackageFile1));
																			InputStreamReader useFileForCycleWithPackageFile2InputSteamReadTmp = new InputStreamReader(new FileInputStream(useFileForCycleWithPackageFile2));
																			
																			BufferedReader useFileForCycleWithPackageFile1ExpFileBufferedReader = new BufferedReader(useFileForCycleWithPackageFile1InputSteamReadTmp);
																			BufferedReader useFileForCycleWithPackageFile2ExpFileBufferedReader = new BufferedReader(useFileForCycleWithPackageFile2InputSteamReadTmp);
																			
																			String useFileForCycleWithPackageLineText1 = null;
																			String useFileForCycleWithPackageLineText2 = null;
																			
																			int PackageNum=0;
																			
																			//组合方式1   一对一
																			while (((useFileForCycleWithPackageLineText1 = useFileForCycleWithPackageFile1ExpFileBufferedReader.readLine()) != null)&&((useFileForCycleWithPackageLineText2 = useFileForCycleWithPackageFile2ExpFileBufferedReader.readLine()) != null))
																			{								
																				PackageNum++;
																				
																				if (lineTxtTmp[1].trim().split(":").length > 1)
																				{
																					host = Method.getFromBase64(lineTxtTmp[1].trim().split(":")[1]);																			
																				}
																				else
																				{
																					
																					break;
																				}

																				
																				if (lineTxtTmp[2].trim().split(":").length > 1)
																				{
																					port = Integer.valueOf(Method.getFromBase64(lineTxtTmp[2].trim().split(":")[1]));
																					
																				}
																				else
																				{
																					port=80;
																				}

				                                      																	
																				StepFileNameTmp = Method.getFromBase64(lineTxtTmp[3].trim().split(":")[1]);									
																				if(StepFileNameTmp.length()>0)
																				{
																					requestData = Method.bytesToString(Method.HexStringTobytes(Method.stringsToString(Method.readFileToHexStrings(fileDir+"\\"+StepFileNameTmp))));
																				}
																						 
																				if(previousURL.length()>0)
																				{
																					// 添加 phpcms/  到request 的 GET  phpcms/。。。。  http/1.1
																					requestData = Method.addPreviousURL(requestData,previousURL);
																				}

																																						
																				String replaceHostPortString = lineTxtTmp[4].trim().split(":")[1].trim();
																				if (replaceHostPortString.equals("true"))
																				{
																					
																					host = targetHost;
																					port = targetPort;
																					requestData = Method.replaceHostPort(requestData,host,port);
																				}
																				else
																				{
																					
																				}
																				                                    
					                                      
																				String useSetCookieFromPreviousString = lineTxtTmp[5].trim().split(":")[1].trim();
																				if (useSetCookieFromPreviousString.equals("true"))
																				{
																					/*
																					previousData = "HTTP/1.1 200 OK"+"\r\n"+
																										  "Date: Sun, 30 Nov 2014 09:16:33 GMT"+"\r\n"+
																										  "Server: Apache/2.2.9 (APMServ) PHP/5.2.6+"+"\r\n"+
																										  "X-Powered-By: PHP/5.2.6"+"\r\n"+
																										  "Set-Cookie: SYSZC=d2d4a91a4e41bea2078b09c29cd6a44a"+"\r\n"+
																										  "Set-Cookie: SYSUSER=admin"+"\r\n"+
																										  "Set-Cookie: SYSNAME=%E7%B3%BB%E7%BB%9F%E7%AE%A1%E7%90%86%E5%91%98"+"\r\n"+
																										  "Set-Cookie: SYSUSERID=3"+"\r\n"+
																										  "Set-Cookie: SYSTM=1417338994"+"\r\n"+
																										  "Content-Length: 2"+"\r\n"+
																										  "Content-Type: text/html"+"\r\n"+
																										  "\r\n"+							  
																										  "OK";
					                                        
					                                        						*/
					                                        
																					String[] tmpmp = Method.getHTTPHeadersParams(previousData);
					                                		
																					String setCookieStr = "";
																					try
																					{
																					for(int n=0;n<tmpmp.length;n++)
																					{
																						if(n%2==0)
																						{																					
																							if(tmpmp[n].startsWith("Set-Cookie"))
																							{
																								setCookieStr =setCookieStr+tmpmp[n+1].trim()+";";
																							}
																						}
																					}
																					String OKrequestData="";			                                		
																					String[] requestDatas = requestData.split("\r\n");
																					boolean addCookie = true;
																					
																					for(int n=0;n<requestDatas.length;n++)
																					{		
																						requestDatas[n] = requestDatas[n]+"\r\n";
																						if(requestDatas[n].toLowerCase().trim().startsWith("cookie:"))
																						{																					
																							String[] tmpCookie = requestDatas[n].toLowerCase().trim().split("cookie:");
																							if(tmpCookie.length>1)
																							{																			
																								tmpCookie[1] = Method.replaceCookie(tmpCookie[1].trim(),setCookieStr);
																								requestDatas[n] = "Cookie: "+Method.stringsToString(tmpCookie)+"\r\n";
																								addCookie = false;
																							}
																						}
																					}
					                                    	  
																					if(addCookie)
																					{
																						OKrequestData =Method.addHeader(requestData,"Cookie: ",setCookieStr);
						                                			
																					}
																					else
																					{
																						OKrequestData = Method.stringsToString(requestDatas);
																					}
																					requestData = OKrequestData;  
																					}
																					catch(Exception e5)
																					{
																						
																					}
					                                		
																					 
					                                        
																				}
																				else
																				{
																					//useSetCookieFromPrevious = false;
																				}
																				
					                                      	
																				int randomStringTmpLength = lineTxtTmp[6].trim().split(":").length;
																				if (randomStringTmpLength > 1)
																				{
																					String randomStringString = lineTxtTmp[6].trim().split(":")[1].trim();
																					String[] randomStringItemStrings = randomStringString.split(",");
																					int randomStringItemCount = randomStringItemStrings.length;

																					for (int k = 0; k < randomStringItemCount; k++)
																					{
																						String[] randomStringItem = new String[5];
																						randomStringItem[0] = Method.getFromBase64(randomStringItemStrings[k]).split(",")[0];
																						randomStringItem[1] = Method.getFromBase64(randomStringItemStrings[k]).split(",")[1];
																						randomStringItem[2] =Method.getFromBase64(randomStringItemStrings[k]).split(",")[2].substring(0, 1);
																						randomStringItem[4] = Method.getFromBase64(randomStringItemStrings[k]).split(",")[3];
																						if (Integer.valueOf(randomStringItem[1]).intValue() == 3)
																						{
																							randomStringItem[3] = randomStringItem[2].substring(1);
																							randomStringItem[2] = randomStringItem[2].substring(0, 1);
																						}									
					                                        		
																						String strTmp = "rSI"+randomStringItem[0];
																						strTmp = Method.getBase64(strTmp);
																						strTmp = Method.addSignRegex(strTmp);
																						String randomStrTmp = "";
																						switch(Integer.valueOf(randomStringItem[1]))
																						{
																							case 0:randomStrTmp =new RandomString().getRandomString(Integer.valueOf(randomStringItem[2]),"i");requestData =requestData.replaceAll(strTmp, randomStrTmp);break;
																							case 1:randomStrTmp =new RandomString().getRandomString(Integer.valueOf(randomStringItem[2]),"l");requestData =requestData.replaceAll(strTmp, randomStrTmp);break;
																							case 2:randomStrTmp =new RandomString().getRandomString(Integer.valueOf(randomStringItem[2]),"il");requestData =requestData.replaceAll(strTmp, randomStrTmp);break;
																							case 3:randomStrTmp =new RandomString().getRandomStringFromDefineString(randomStringItem[3],Integer.valueOf(randomStringItem[2]));requestData =requestData.replaceAll(strTmp, randomStrTmp);break;																																																								
																						}
					                                        		
																						randomStringList.add(randomStringItem);
																						
																					}
																					
																				}			                                      																			
																				
																				int captchaTmpLength = lineTxtTmp[7].trim().split(":").length;
																				if (captchaTmpLength > 1)
																				{
																					String captchaString = lineTxtTmp[7].trim().split(":")[1].trim();
																					String[] captchaItemStrings = captchaString.split(",");
																					int captchaItemCount = captchaItemStrings.length;
																					for (int k = 0; k < captchaItemCount; k++)
																					{
																						String[] captchaItem = new String[4];
																						captchaItem = Method.getFromBase64(captchaItemStrings[k]).split(",");
																						captchaList.add(captchaItem);																			
																						
																						String captchaStrTmp = "sPFC"+captchaItem[0];
																						captchaStrTmp = Method.getBase64(captchaStrTmp);
																						captchaStrTmp = Method.addSignRegex(captchaStrTmp);
					                                            			                                            
																						boolean status = UUAPI.checkAPI();

																						if (!status)
																						{
																							JOptionPane.showMessageDialog(null, "API文件校验失败，无法使用打码服务", "Warning", 2);
																						}
																						else
																						{
																							final String ImgURL = "http://"+host+previousURL+captchaItem[1];  //获取验证码的url  
																					//		System.out.println(ImgURL);                                           
																							final int CaptchaNumbersTextInt = Integer.valueOf(captchaItem[2]);
																							final String saveFileName = "img/tmp.jpg";
																							
																							String saveAbsolutePath = Method.getImgFromURL(ImgURL,saveFileName);
																							if (saveAbsolutePath != null)
																							{
																								if (CaptchaNumbersTextInt != 0)
																								{
																									try 
																									{
																										String[] captchaResult = UUAPI.easyDecaptcha(saveFileName, 1000 + CaptchaNumbersTextInt);
																										if (captchaResult[1].length() > 0)
																										{
																											requestData = requestData.replaceAll(captchaStrTmp, captchaResult[1]);   
																										}
																									} 
																									catch (IOException eee) 
																									{																								
																										eee.printStackTrace();
																									}	                                                    
																								}			                                      					
																							}																				
																						}		                                            
																					}
																				}
					                                        		                                        
																				//
																				String FileForCycleWithEXPStrTmp = "FileForCycleWithEXP";
																				FileForCycleWithEXPStrTmp = Method.getBase64(FileForCycleWithEXPStrTmp);
																				FileForCycleWithEXPStrTmp = Method.addSignRegex(FileForCycleWithEXPStrTmp);
																				requestData = requestData.replaceAll(FileForCycleWithEXPStrTmp, expFileLineTxt);		                                      					                                        
					                                        
																				//useFileForCycleWithPackageLineText
																				String useFileForCycleWithPackageLineStrTmp1 = "File-1";
																				useFileForCycleWithPackageLineStrTmp1 = Method.getBase64(useFileForCycleWithPackageLineStrTmp1);
																				useFileForCycleWithPackageLineStrTmp1 = Method.addSignRegex(useFileForCycleWithPackageLineStrTmp1);
																				requestData = requestData.replaceAll(useFileForCycleWithPackageLineStrTmp1, useFileForCycleWithPackageLineText1);
																																					
																				String useFileForCycleWithPackageLineStrTmp2 = "File-2";
																				useFileForCycleWithPackageLineStrTmp2 = Method.getBase64(useFileForCycleWithPackageLineStrTmp2);
																				useFileForCycleWithPackageLineStrTmp2 = Method.addSignRegex(useFileForCycleWithPackageLineStrTmp2);
																				requestData = requestData.replaceAll(useFileForCycleWithPackageLineStrTmp2, useFileForCycleWithPackageLineText2);
					                                      		
					                                      		
																				// 发送http 数据
																				
																				responseData = Method.bytesToString(Method.getHttPResponseData(host,port,requestData));
																			//	System.out.println(requestData);
																			//	System.out.println("--");
																			//	System.out.println(responseData);
																				responseStatus = Method.getHttpResponseHeaderStatus(responseData);																
					                                      		
																				int setGoAheadOrStopFromStatusTmpLength = lineTxtTmp[9].trim().split(":").length;
																				if (setGoAheadOrStopFromStatusTmpLength > 1)
																				{
																					String setGoAheadOrStopFromStatusString = lineTxtTmp[9].trim().split(":")[1].trim();
																					String[] setGoAheadOrStopFromStatusItemStrings = setGoAheadOrStopFromStatusString.split(",");
																					int setGoAheadOrStopFromStatusItemCount = setGoAheadOrStopFromStatusItemStrings.length;
																					for (int k = 0; k < setGoAheadOrStopFromStatusItemCount; k++)
																					{
																						String[] setGoAheadOrStopFromStatusItem = new String[2];
																						setGoAheadOrStopFromStatusItem = Method.getFromBase64(setGoAheadOrStopFromStatusItemStrings[k]).split(",");
																						setGoAheadOrStopFromStatusList.add(setGoAheadOrStopFromStatusItem);								
																					}
																				}
					                                      				                                      		
																				int setGoAheadOrStopFromStatusListCount = setGoAheadOrStopFromStatusList.size();
																				if (setGoAheadOrStopFromStatusListCount > 0)
																				{
																					for (int j = 0; j < setGoAheadOrStopFromStatusListCount; j++)
																					{
																						String[] setGoAheadOrStopFromStatusListItem = (String[])setGoAheadOrStopFromStatusList.get(j);																				
																						if (responseStatus == Integer.valueOf(setGoAheadOrStopFromStatusListItem[0]))
																						{
																							String actionForStatus = setGoAheadOrStopFromStatusListItem[1];
					                                            			
					                                            			
																							//                         GO ahead current   stop current 
																							///                                               ||
																							//																		
																							///											Go Exp    Stop Exp
																							
																							
																							if(actionForStatus.equals("Go Ahead EXP"))
																							{
																								GoAheadExp  = true;
																								whyToStop = "responseStatus; " + responseStatus + " -- Rule; " + setGoAheadOrStopFromStatusListItem[0] + " --Action; "+ setGoAheadOrStopFromStatusListItem[1];
																								break;
																							}
																							else if(actionForStatus.equals("Stop EXP"))
																							{
																								StopExp = true;                         				
																								whyToStop = "responseStatus; " + responseStatus + " -- Rule; " + setGoAheadOrStopFromStatusListItem[0] + " --Action; "+ setGoAheadOrStopFromStatusListItem[1];
																								//System.out.println(whyToStop);
																								break;
																							}
																							else if(actionForStatus.equals("Go Ahead Current"))
																							{
																								GoAheadCurrent = true;
																								whyToStop = "responseStatus; " + responseStatus + " -- Rule; " + setGoAheadOrStopFromStatusListItem[0] + " --Action; "+ setGoAheadOrStopFromStatusListItem[1];
																								break;
																							}
																							else if(actionForStatus.equals("Stop Current"))
																							{
																								//StopCurrent = true;
																								//whyToStop = "responseStatus:" + responseStatus + " -- Rule" + setGoAheadOrStopFromStatusListItem[0] + setGoAheadOrStopFromStatusListItem[1];
																								//System.out.println(whyToStop);
																								//break;
																							}                                      																								
																						}
																					}
																				}
																				else
																				{
																				//	System.out.println("mei you setGoAheadOrStopFromStatusListCount");
																				}

																				if ((StopExp) )
																				{
																					JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,expFileLineTxt,useFileForCycleWithPackageLineText1+"-|-"+useFileForCycleWithPackageLineText2,whyToStop,"show"});
																					ExpPackageHistoryItem[0] = StepFileNameTmp;
																					ExpPackageHistoryItem[1] = requestData;
																					ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																					previousData = responseData;
																					ExpPackageHistoryItem[3] = responseData;
																					ExpPackageHistoryItem[4] = whyToStop;
																					ExpPackageHistoryList.add(ExpPackageHistoryItem);
																					expHTTPHistoryList.add(ExpPackageHistoryItem);
																					ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																					ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																					ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																					ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																					ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																					ExpPackageHistoryItem = new String[7];
																					responseStatus = 0;
																					responseData = "";
																					whyToStop = "Run Over";
																					break;
																				}
					                                            
																				if(GoAheadCurrent)
																				{
																					JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,expFileLineTxt,useFileForCycleWithPackageLineText1+"-|-"+useFileForCycleWithPackageLineText2,whyToStop,"show"});
																					ExpPackageHistoryItem[0] = StepFileNameTmp;
																					ExpPackageHistoryItem[1] = requestData;
																					ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																					previousData = responseData;
																					ExpPackageHistoryItem[3] = responseData;
																					ExpPackageHistoryItem[4] = whyToStop;
																					ExpPackageHistoryList.add(ExpPackageHistoryItem);
																					expHTTPHistoryList.add(ExpPackageHistoryItem);
																					ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																					ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																					ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																					ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																					ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																					ExpPackageHistoryItem = new String[7];
																					responseStatus = 0;
																					responseData = "";
																					whyToStop = "Run Over";
																					GoAheadCurrent = false;
																					break;
																				}
					                                            
																				if(GoAheadExp)
																				{
																					JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,expFileLineTxt,useFileForCycleWithPackageLineText1+"-|-"+useFileForCycleWithPackageLineText2,whyToStop,"show"});
																					ExpPackageHistoryItem[0] = StepFileNameTmp;
																					ExpPackageHistoryItem[1] = requestData;
																					ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																					previousData = responseData;
																					ExpPackageHistoryItem[3] = responseData;
																					ExpPackageHistoryItem[4] = whyToStop;
																					ExpPackageHistoryList.add(ExpPackageHistoryItem);
																					expHTTPHistoryList.add(ExpPackageHistoryItem);
																					ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																					ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																					ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																					ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																					ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																					ExpPackageHistoryItem = new String[7];
																					responseStatus = 0;
																					responseData = "";
																					whyToStop = "Run Over";
																					break;
																				}
					                                            
																				int setGoAheadOrStopFromDataTmpLength = lineTxtTmp[10].trim().split(":").length;
																				if (setGoAheadOrStopFromDataTmpLength > 1)
																				{
																					String setGoAheadOrStopFromDataString = lineTxtTmp[10].trim().split(":")[1].trim();
																					String[] setGoAheadOrStopFromDataItemStrings = setGoAheadOrStopFromDataString.split(",");
																					int setGoAheadOrStopFromDataItemCount = setGoAheadOrStopFromDataItemStrings.length;
																					for (int k = 0; k < setGoAheadOrStopFromDataItemCount; k++)
																					{
																						String[] setGoAheadOrStopFromDataItem = new String[2];
																						setGoAheadOrStopFromDataItem = Method.getFromBase64(setGoAheadOrStopFromDataItemStrings[k]).split(",");
																						setGoAheadOrStopFromDataList.add(setGoAheadOrStopFromDataItem);																			
																					}
																				}
																					                                            
																				
																				int j;
																				int setGoAheadOrStopFromDataListCount = setGoAheadOrStopFromDataList.size();
																				if (setGoAheadOrStopFromDataListCount > 0)
																				{
																					for (j = 0; j < setGoAheadOrStopFromDataListCount; j++)
																					{
																						String[] setGoAheadOrStopFromDataListItem = (String[])setGoAheadOrStopFromDataList.get(j);

																						Pattern p=Pattern.compile(Method.getFromBase64(setGoAheadOrStopFromDataListItem[0]));
																						Matcher m=p.matcher(responseData);
																					//	System.out.println(m.find());
																						
																						if (m.find())
																						{
																							String actionForData = setGoAheadOrStopFromDataListItem[1];
																							
																							if(actionForData.equals("Go Ahead EXP"))
																							{
																								GoAheadExp  = true;
																								whyToStop = " -- Rule; " + Method.getFromBase64(setGoAheadOrStopFromDataListItem[0]) +"  --result; "+m.group()+ " --Action; "+ setGoAheadOrStopFromDataListItem[1];
																								break;
																							}
																							else if(actionForData.equals("Stop EXP"))
																							{
																								StopExp = true;
																								whyToStop = " -- Rule; " + Method.getFromBase64(setGoAheadOrStopFromDataListItem[0]) +"  --result; "+m.group()+ " --Action; "+ setGoAheadOrStopFromDataListItem[1];
																								break;
																							}
																							else if(actionForData.equals("Go Ahead Current"))
																							{
																								GoAheadCurrent = true;
																								whyToStop = " -- Rule; " + Method.getFromBase64(setGoAheadOrStopFromDataListItem[0]) +"  --result; "+m.group()+ " --Action; "+ setGoAheadOrStopFromDataListItem[1];
																								break;
																							}
																							else if(actionForData.equals("Stop Current"))
																							{
																								//StopCurrent = true;
																								//whyToStop = "responseStatus:" + responseStatus + " -- Rule" + setGoAheadOrStopFromStatusListItem[0] + setGoAheadOrStopFromStatusListItem[1];
																								//System.out.println(whyToStop);
																								//break;
																							} 								
																						}
																					}
																				}
																				else
																				{
																			//		System.out.println("meiyou setGoAheadOrStopFromDataList");
																				}

																				if ((StopExp) )
																				{
																					JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,expFileLineTxt,useFileForCycleWithPackageLineText1+"-|-"+useFileForCycleWithPackageLineText2,whyToStop,"show"});
																					ExpPackageHistoryItem[0] = StepFileNameTmp;
																					ExpPackageHistoryItem[1] = requestData;
																					ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																					previousData = responseData;
																					ExpPackageHistoryItem[3] = responseData;
																					ExpPackageHistoryItem[4] = whyToStop;
																					ExpPackageHistoryList.add(ExpPackageHistoryItem);
																					expHTTPHistoryList.add(ExpPackageHistoryItem);
																					ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																					ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																					ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																					ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																					ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																					ExpPackageHistoryItem = new String[7];
																					responseStatus = 0;
																					responseData = "";
																					whyToStop = "Run Over";
																					break;
																				}
					                                            
																				if(GoAheadCurrent)
																				{
																					JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,expFileLineTxt,useFileForCycleWithPackageLineText1+"-|-"+useFileForCycleWithPackageLineText2,whyToStop,"show"});
																					ExpPackageHistoryItem[0] = StepFileNameTmp;
																					ExpPackageHistoryItem[1] = requestData;
																					ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																					previousData = responseData;
																					ExpPackageHistoryItem[3] = responseData;
																					ExpPackageHistoryItem[4] = whyToStop;
																					ExpPackageHistoryList.add(ExpPackageHistoryItem);
																					expHTTPHistoryList.add(ExpPackageHistoryItem);
																					ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																					ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																					ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																					ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																					ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																					ExpPackageHistoryItem = new String[7];
																					responseStatus = 0;
																					responseData = "";
																					whyToStop = "Run Over";
																					GoAheadCurrent = false;
																					break;
																				}
					                                            
																				if(GoAheadExp)
																				{
																					JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,expFileLineTxt,useFileForCycleWithPackageLineText1+"-|-"+useFileForCycleWithPackageLineText2,whyToStop,"show"});
																					ExpPackageHistoryItem[0] = StepFileNameTmp;
																					ExpPackageHistoryItem[1] = requestData;
																					ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																					previousData = responseData;
																					ExpPackageHistoryItem[3] = responseData;
																					ExpPackageHistoryItem[4] = whyToStop;
																					ExpPackageHistoryList.add(ExpPackageHistoryItem);
																					expHTTPHistoryList.add(ExpPackageHistoryItem);
																					ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																					ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																					ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																					ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																					ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																					ExpPackageHistoryItem = new String[7];
																					responseStatus = 0;
																					responseData = "";
																					whyToStop = "Run Over";
																					break;
																				}

																				JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,expFileLineTxt,useFileForCycleWithPackageLineText1+"-|-"+useFileForCycleWithPackageLineText2,whyToStop,"show"});
																				ExpPackageHistoryItem[0] = StepFileNameTmp;
																				ExpPackageHistoryItem[1] = requestData;
																				ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																				previousData = responseData;
																				ExpPackageHistoryItem[3] = responseData;
																				ExpPackageHistoryItem[4] = whyToStop;
																				ExpPackageHistoryList.add(ExpPackageHistoryItem);
																				expHTTPHistoryList.add(ExpPackageHistoryItem);
																				ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																				ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																				ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																				ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																				ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																				ExpPackageHistoryItem = new String[7];
																				responseStatus = 0;
																				responseData = "";
																				whyToStop = "Run Over";
																			
																			}
																		}
																		//组合方式二  一对多
																		else if(Integer.valueOf(useFileForCycleWithPackageItem[3])==2)
																		{
																			
																			File useFileForCycleWithPackageFile1 = new File(fileDir+useFileForCycleWithPackageItem[1]);																																
																			InputStreamReader useFileForCycleWithPackageFile1InputSteamReadTmp = new InputStreamReader(new FileInputStream(useFileForCycleWithPackageFile1));																															
																			BufferedReader useFileForCycleWithPackageFile1ExpFileBufferedReader = new BufferedReader(useFileForCycleWithPackageFile1InputSteamReadTmp);																															
																			String useFileForCycleWithPackageLineText1 = null;
														
																			int PackageNum=0;
																			
																			while (((useFileForCycleWithPackageLineText1 = useFileForCycleWithPackageFile1ExpFileBufferedReader.readLine()) != null))
																			{																	
																				File useFileForCycleWithPackageFile2 = new File(fileDir+useFileForCycleWithPackageItem[2]);
																				InputStreamReader useFileForCycleWithPackageFile2InputSteamReadTmp = new InputStreamReader(new FileInputStream(useFileForCycleWithPackageFile2));
																				BufferedReader useFileForCycleWithPackageFile2ExpFileBufferedReader = new BufferedReader(useFileForCycleWithPackageFile2InputSteamReadTmp);
																				String useFileForCycleWithPackageLineText2 = null;
																				while(((useFileForCycleWithPackageLineText2 = useFileForCycleWithPackageFile2ExpFileBufferedReader.readLine()) != null))
																				{																		
																					
																					PackageNum++;
																					if (lineTxtTmp[1].trim().split(":").length > 1)
																					{
																						host = Method.getFromBase64(lineTxtTmp[1].trim().split(":")[1]);																		
																					}
																					else
																					{
																						break;
																					}

																					
																					if (lineTxtTmp[2].trim().split(":").length > 1)
																					{
																						port = Integer.valueOf(Method.getFromBase64(lineTxtTmp[2].trim().split(":")[1]));
																					}
																					else
																					{
																						port= 80;
																					}
			                                      				                                      
																					
																					StepFileNameTmp = Method.getFromBase64(lineTxtTmp[3].trim().split(":")[1]);									
																					if(StepFileNameTmp.length()>0)
																					{
																						requestData = Method.bytesToString(Method.HexStringTobytes(Method.stringsToString(Method.readFileToHexStrings(fileDir+"\\"+StepFileNameTmp))));
																					}
																					
																					
																					if(previousURL.length()>0)
																					{
																						// 添加 phpcms/  到request 的 GET  phpcms/。。。。  http/1.1
																						requestData = Method.addPreviousURL(requestData,previousURL);
																					}
						                                     
						                                      
																					String replaceHostPortString = lineTxtTmp[4].trim().split(":")[1].trim();
																					if (replaceHostPortString.equals("true"))
																					{																				
																						host = targetHost;
																						port = targetPort;
																						requestData = Method.replaceHostPort(requestData,host,port);
																					}
																					else
																					{
																						
																					}
						                                      
						                                      
																					String useSetCookieFromPreviousString = lineTxtTmp[5].trim().split(":")[1].trim();
																					if (useSetCookieFromPreviousString.equals("true"))
																					{
																						/*
																						previousData = "HTTP/1.1 200 OK"+"\r\n"+
																											  "Date: Sun, 30 Nov 2014 09:16:33 GMT"+"\r\n"+
																											  "Server: Apache/2.2.9 (APMServ) PHP/5.2.6+"+"\r\n"+
																											  "X-Powered-By: PHP/5.2.6"+"\r\n"+
																											  "Set-Cookie: SYSZC=d2d4a91a4e41bea2078b09c29cd6a44a"+"\r\n"+
																											  "Set-Cookie: SYSUSER=admin"+"\r\n"+
																											  "Set-Cookie: SYSNAME=%E7%B3%BB%E7%BB%9F%E7%AE%A1%E7%90%86%E5%91%98"+"\r\n"+
																											  "Set-Cookie: SYSUSERID=3"+"\r\n"+
																											  "Set-Cookie: SYSTM=1417338994"+"\r\n"+
																											  "Content-Length: 2"+"\r\n"+
																											  "Content-Type: text/html"+"\r\n"+
																											  "\r\n"+							  
																											  "OK";
						                                        
						                                        						*/
																						String[] tmpmp = Method.getHTTPHeadersParams(previousData);
						                                		
																						String setCookieStr = "";
																						try
																						{
																						for(int n=0;n<tmpmp.length;n++)
																						{
																							if(n%2==0)
																							{
																								if(tmpmp[n].startsWith("Set-Cookie"))
																								{
																									setCookieStr =setCookieStr+tmpmp[n+1].trim()+";";
																								}
																							}
																						}
																						String OKrequestData="";		                                		
																						String[] requestDatas = requestData.split("\r\n");
																						boolean addCookie = true;
																						
																						for(int n=0;n<requestDatas.length;n++)
																						{		
																							requestDatas[n] = requestDatas[n]+"\r\n";
																							if(requestDatas[n].toLowerCase().trim().startsWith("cookie:"))
																							{
																								String[] tmpCookie = requestDatas[n].toLowerCase().trim().split("cookie:");
																								if(tmpCookie.length>1)
																								{
																									tmpCookie[1] = Method.replaceCookie(tmpCookie[1].trim(),setCookieStr);
																									requestDatas[n] = "Cookie: "+Method.stringsToString(tmpCookie)+"\r\n";
																									addCookie = false;
																								}
																							}
																						}
						                                    	  
																						if(addCookie)
																						{
																							OKrequestData =Method.addHeader(requestData,"Cookie: ",setCookieStr);				                                			
																						}
																						else
																						{
																							OKrequestData = Method.stringsToString(requestDatas);
																						}
																						requestData = OKrequestData;   
																						}
																						catch(Exception e5)
																						{
																							
																						}
						                                		
																						
						                                        
																					}
																					else
																					{
																						
																					}
																																							
						                                      	
																					int randomStringTmpLength = lineTxtTmp[6].trim().split(":").length;
																					if (randomStringTmpLength > 1)
																					{
																						String randomStringString = lineTxtTmp[6].trim().split(":")[1].trim();
																						String[] randomStringItemStrings = randomStringString.split(",");
																						int randomStringItemCount = randomStringItemStrings.length;
																						for (int k = 0; k < randomStringItemCount; k++)
																						{
																							String[] randomStringItem = new String[5];
																							randomStringItem[0] = Method.getFromBase64(randomStringItemStrings[k]).split(",")[0];
																							randomStringItem[1] = Method.getFromBase64(randomStringItemStrings[k]).split(",")[1];
																							randomStringItem[2] =Method.getFromBase64(randomStringItemStrings[k]).split(",")[2].substring(0, 1);
																							randomStringItem[4] = Method.getFromBase64(randomStringItemStrings[k]).split(",")[3];
																				
																							if (Integer.valueOf(randomStringItem[1]).intValue() == 3)
																							{
																								randomStringItem[3] = randomStringItem[2].substring(1);
																								randomStringItem[2] = randomStringItem[2].substring(0, 1);
																							}																					
						                                        		
																							String strTmp = "rSI"+randomStringItem[0];
																							strTmp = Method.getBase64(strTmp);
																							strTmp = Method.addSignRegex(strTmp);
																							String randomStrTmp = "";
																							switch(Integer.valueOf(randomStringItem[1]))
																							{
																								case 0:randomStrTmp =new RandomString().getRandomString(Integer.valueOf(randomStringItem[2]),"i");requestData =requestData.replaceAll(strTmp, randomStrTmp);break;
																								case 1:randomStrTmp =new RandomString().getRandomString(Integer.valueOf(randomStringItem[2]),"l");requestData =requestData.replaceAll(strTmp, randomStrTmp);break;
																								case 2:randomStrTmp =new RandomString().getRandomString(Integer.valueOf(randomStringItem[2]),"il");requestData =requestData.replaceAll(strTmp, randomStrTmp);break;
																								case 3:randomStrTmp =new RandomString().getRandomStringFromDefineString(randomStringItem[3],Integer.valueOf(randomStringItem[2]));requestData =requestData.replaceAll(strTmp, randomStrTmp);break;																		
																							
																							
																							}				                                        		
																							randomStringList.add(randomStringItem);
																							
																						}
																						
																					}
						                                      	
																					
																					int captchaTmpLength = lineTxtTmp[7].trim().split(":").length;
																					if (captchaTmpLength > 1)
																					{
																						String captchaString = lineTxtTmp[7].trim().split(":")[1].trim();
																						String[] captchaItemStrings = captchaString.split(",");
																						int captchaItemCount = captchaItemStrings.length;
																						for (int k = 0; k < captchaItemCount; k++)
																						{
																							String[] captchaItem = new String[3];
																							captchaItem = Method.getFromBase64(captchaItemStrings[k]).split(",");
																							captchaList.add(captchaItem);																					
																							
																							String captchaStrTmp = "sPFC"+captchaItem[0];
																							captchaStrTmp = Method.getBase64(captchaStrTmp);
																							captchaStrTmp = Method.addSignRegex(captchaStrTmp);				                                            
						                                            
																							boolean status = UUAPI.checkAPI();

																							if (!status)
																							{
																								JOptionPane.showMessageDialog(null, "API文件校验失败，无法使用打码服务", "Warning", 2);
																							}
																							else
																							{
																								final String ImgURL = "http://"+host+previousURL+captchaItem[1];  //获取验证码的url  
																						//		System.out.println(ImgURL);                                         
																								final int CaptchaNumbersTextInt = Integer.valueOf(captchaItem[2]);
																								final String saveFileName = "img/tmp.jpg";
																								
																								String saveAbsolutePath = Method.getImgFromURL(ImgURL,saveFileName);
																								if (saveAbsolutePath != null)
																								{
																									if (CaptchaNumbersTextInt != 0)
																									{
																										try 
																										{
																											String[] captchaResult = UUAPI.easyDecaptcha(saveFileName, 1000 + CaptchaNumbersTextInt);
																											if (captchaResult[1].length() > 0)
																											{
																												requestData = requestData.replaceAll(captchaStrTmp, captchaResult[1]);   
																											}
																										} 
																										catch (IOException eee) 
																										{																								
																											eee.printStackTrace();
																										}	                                                    
																									}				                                      					
																								}																						
																							}				                                            
																						}
																					}
						                                        
						                                        
																					//
																					String FileForCycleWithEXPStrTmp = "FileForCycleWithEXP";
																					FileForCycleWithEXPStrTmp = Method.getBase64(FileForCycleWithEXPStrTmp);
																					FileForCycleWithEXPStrTmp = Method.addSignRegex(FileForCycleWithEXPStrTmp);
																					requestData = requestData.replaceAll(FileForCycleWithEXPStrTmp, expFileLineTxt);
						                                      		
						                                        
						                                        
																					//useFileForCycleWithPackageLineText
																					String useFileForCycleWithPackageLineStrTmp1 = "File-1";
																					useFileForCycleWithPackageLineStrTmp1 = Method.getBase64(useFileForCycleWithPackageLineStrTmp1);
																					useFileForCycleWithPackageLineStrTmp1 = Method.addSignRegex(useFileForCycleWithPackageLineStrTmp1);
																					requestData = requestData.replaceAll(useFileForCycleWithPackageLineStrTmp1, useFileForCycleWithPackageLineText1);
																					
																					
																					String useFileForCycleWithPackageLineStrTmp2 = "File-2";
																					useFileForCycleWithPackageLineStrTmp2 = Method.getBase64(useFileForCycleWithPackageLineStrTmp2);
																					useFileForCycleWithPackageLineStrTmp2 = Method.addSignRegex(useFileForCycleWithPackageLineStrTmp2);
																					requestData = requestData.replaceAll(useFileForCycleWithPackageLineStrTmp2, useFileForCycleWithPackageLineText2);
						                                      		
						                                      		
																					// 发送http 数据
																					
																					responseData = Method.bytesToString(Method.getHttPResponseData(host,port,requestData));
																				//	System.out.println(requestData);
																				//	System.out.println("--");
																				//	System.out.println(responseData);
																					responseStatus = Method.getHttpResponseHeaderStatus(responseData);
																																							
						                                      		
																					int setGoAheadOrStopFromStatusTmpLength = lineTxtTmp[9].trim().split(":").length;
																					if (setGoAheadOrStopFromStatusTmpLength > 1)
																					{
																						String setGoAheadOrStopFromStatusString = lineTxtTmp[9].trim().split(":")[1].trim();
																						String[] setGoAheadOrStopFromStatusItemStrings = setGoAheadOrStopFromStatusString.split(",");
																						int setGoAheadOrStopFromStatusItemCount = setGoAheadOrStopFromStatusItemStrings.length;
																						for (int k = 0; k < setGoAheadOrStopFromStatusItemCount; k++)
																						{
																							String[] setGoAheadOrStopFromStatusItem = new String[2];
																							setGoAheadOrStopFromStatusItem = Method.getFromBase64(setGoAheadOrStopFromStatusItemStrings[k]).split(",");
																							setGoAheadOrStopFromStatusList.add(setGoAheadOrStopFromStatusItem);										
																						}
																					}
						                                      				                                      		
																					int setGoAheadOrStopFromStatusListCount = setGoAheadOrStopFromStatusList.size();
																					if (setGoAheadOrStopFromStatusListCount > 0)
																					{
																						for (int j = 0; j < setGoAheadOrStopFromStatusListCount; j++)
																						{
																							String[] setGoAheadOrStopFromStatusListItem = (String[])setGoAheadOrStopFromStatusList.get(j);															
																							if (responseStatus == Integer.valueOf(setGoAheadOrStopFromStatusListItem[0]))
																							{
																								String actionForStatus = setGoAheadOrStopFromStatusListItem[1];
						                                            			
						                                            			
																								//                         GO ahead current   stop current 
																								///                                               ||
																								//																		
																								///											Go Exp    Stop Exp
																								///									
																								
																								if(actionForStatus.equals("Go Ahead EXP"))
																								{
																									GoAheadExp  = true;
																									whyToStop = "responseStatus; " + responseStatus + " -- Rule; " + setGoAheadOrStopFromStatusListItem[0] + " --Action; "+ setGoAheadOrStopFromStatusListItem[1];
																									break;
																								}
																								else if(actionForStatus.equals("Stop EXP"))
																								{
																									StopExp = true;                                    				
																									whyToStop = "responseStatus; " + responseStatus + " -- Rule; " + setGoAheadOrStopFromStatusListItem[0] + " --Action; "+ setGoAheadOrStopFromStatusListItem[1];
																									break;
																								}
																								else if(actionForStatus.equals("Go Ahead Current"))
																								{
																									GoAheadCurrent = true;
																									whyToStop = "responseStatus; " + responseStatus + " -- Rule; " + setGoAheadOrStopFromStatusListItem[0] + " --Action; "+ setGoAheadOrStopFromStatusListItem[1];
																									break;
																								}
																								else if(actionForStatus.equals("Stop Current"))
																								{
																									//StopCurrent = true;
																									//whyToStop = "responseStatus:" + responseStatus + " -- Rule" + setGoAheadOrStopFromStatusListItem[0] + setGoAheadOrStopFromStatusListItem[1];
																									//System.out.println(whyToStop);
																									//break;
																								}                                      			
																								
																							}
																						}
																					}
																					else
																					{
																						//System.out.println("mei you setGoAheadOrStopFromStatusListCount");
																					}

																					if ((StopExp) )
																					{
																						JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,expFileLineTxt,useFileForCycleWithPackageLineText1+"-|-"+useFileForCycleWithPackageLineText2,whyToStop,"show"});
																						ExpPackageHistoryItem[0] = StepFileNameTmp;
																						ExpPackageHistoryItem[1] = requestData;
																						ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																						previousData = responseData;
																						ExpPackageHistoryItem[3] = responseData;
																						ExpPackageHistoryItem[4] = whyToStop;
																						ExpPackageHistoryList.add(ExpPackageHistoryItem);
																						expHTTPHistoryList.add(ExpPackageHistoryItem);
																						ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																						ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																						ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																						ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																						ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																						ExpPackageHistoryItem = new String[7];
																						responseStatus = 0;
																						responseData = "";
																						whyToStop = "Run Over";
																						break;
																					}
						                                            
																					if(GoAheadCurrent)
																					{
																						JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,expFileLineTxt,useFileForCycleWithPackageLineText1+"-|-"+useFileForCycleWithPackageLineText2,whyToStop,"show"});
																						ExpPackageHistoryItem[0] = StepFileNameTmp;
																						ExpPackageHistoryItem[1] = requestData;
																						ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																						previousData = responseData;
																						ExpPackageHistoryItem[3] = responseData;
																						ExpPackageHistoryItem[4] = whyToStop;
																						ExpPackageHistoryList.add(ExpPackageHistoryItem);
																						expHTTPHistoryList.add(ExpPackageHistoryItem);
																						ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																						ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																						ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																						ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																						ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																						ExpPackageHistoryItem = new String[7];
																						responseStatus = 0;
																						responseData = "";
																						whyToStop = "Run Over";
																						GoAheadCurrent = false;
																						break;
																					}
						                                            
																					if(GoAheadExp)
																					{
																						JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,expFileLineTxt,useFileForCycleWithPackageLineText1+"-|-"+useFileForCycleWithPackageLineText2,whyToStop,"show"});
																						ExpPackageHistoryItem[0] = StepFileNameTmp;
																						ExpPackageHistoryItem[1] = requestData;
																						ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																						previousData = responseData;
																						ExpPackageHistoryItem[3] = responseData;
																						ExpPackageHistoryItem[4] = whyToStop;
																						ExpPackageHistoryList.add(ExpPackageHistoryItem);
																						expHTTPHistoryList.add(ExpPackageHistoryItem);
																						ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																						ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																						ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																						ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																						ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																						ExpPackageHistoryItem = new String[7];
																						responseStatus = 0;
																						responseData = "";
																						whyToStop = "Run Over";
																						break;
																					}
						                                            																																					
																					
																					int setGoAheadOrStopFromDataTmpLength = lineTxtTmp[10].trim().split(":").length;
																					if (setGoAheadOrStopFromDataTmpLength > 1)
																					{
																						String setGoAheadOrStopFromDataString = lineTxtTmp[10].trim().split(":")[1].trim();
																						String[] setGoAheadOrStopFromDataItemStrings = setGoAheadOrStopFromDataString.split(",");
																						int setGoAheadOrStopFromDataItemCount = setGoAheadOrStopFromDataItemStrings.length;
																						for (int k = 0; k < setGoAheadOrStopFromDataItemCount; k++)
																						{
																							String[] setGoAheadOrStopFromDataItem = new String[2];
																							setGoAheadOrStopFromDataItem = Method.getFromBase64(setGoAheadOrStopFromDataItemStrings[k]).split(",");
																							setGoAheadOrStopFromDataList.add(setGoAheadOrStopFromDataItem);										
																						}
																					}
																					
																		
																					int j;																	
																					int setGoAheadOrStopFromDataListCount = setGoAheadOrStopFromDataList.size();
																					if (setGoAheadOrStopFromDataListCount > 0)
																					{
																						for (j = 0; j < setGoAheadOrStopFromDataListCount; j++)
																						{
																							String[] setGoAheadOrStopFromDataListItem = (String[])setGoAheadOrStopFromDataList.get(j);

																							Pattern p=Pattern.compile(Method.getFromBase64(setGoAheadOrStopFromDataListItem[0]));
																							Matcher m=p.matcher(responseData);
																						//	System.out.println(m.find());
																							
																							if (m.find())
																							{
																								String actionForData = setGoAheadOrStopFromDataListItem[1];
																								
																								if(actionForData.equals("Go Ahead EXP"))
																								{
																									GoAheadExp  = true;
																									whyToStop = " -- Rule; " + Method.getFromBase64(setGoAheadOrStopFromDataListItem[0]) +"  --result; "+m.group()+ " --Action; "+ setGoAheadOrStopFromDataListItem[1];
																									break;
																								}
																								else if(actionForData.equals("Stop EXP"))
																								{
																									StopExp = true;
																									whyToStop = " -- Rule; " + Method.getFromBase64(setGoAheadOrStopFromDataListItem[0]) +"  --result; "+m.group()+ " --Action; "+ setGoAheadOrStopFromDataListItem[1];
																									break;
																								}
																								else if(actionForData.equals("Go Ahead Current"))
																								{
																									GoAheadCurrent = true;
																									whyToStop = " -- Rule; " + Method.getFromBase64(setGoAheadOrStopFromDataListItem[0]) +"  --result; "+m.group()+ " --Action; "+ setGoAheadOrStopFromDataListItem[1];
																									break;
																								}
																								else if(actionForData.equals("Stop Current"))
																								{
																									//StopCurrent = true;
																									//whyToStop = "responseStatus:" + responseStatus + " -- Rule" + setGoAheadOrStopFromStatusListItem[0] + setGoAheadOrStopFromStatusListItem[1];
																									//System.out.println(whyToStop);
																									//break;
																								} 
																								
																							}
																						}
																					}
																					else
																					{
																						//System.out.println("meiyou setGoAheadOrStopFromDataList");
																					}

																					if ((StopExp) )
																					{
																						JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,expFileLineTxt,useFileForCycleWithPackageLineText1+"-|-"+useFileForCycleWithPackageLineText2,whyToStop,"show"});
																						ExpPackageHistoryItem[0] = StepFileNameTmp;
																						ExpPackageHistoryItem[1] = requestData;
																						ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																						previousData = responseData;
																						ExpPackageHistoryItem[3] = responseData;
																						ExpPackageHistoryItem[4] = whyToStop;
																						ExpPackageHistoryList.add(ExpPackageHistoryItem);
																						expHTTPHistoryList.add(ExpPackageHistoryItem);
																						ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																						ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																						ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																						ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																						ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																						ExpPackageHistoryItem = new String[7];
																						responseStatus = 0;
																						responseData = "";
																						whyToStop = "Run Over";
																						break;
																					}
						                                            
																					if(GoAheadCurrent)
																					{
																						JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,expFileLineTxt,useFileForCycleWithPackageLineText1+"-|-"+useFileForCycleWithPackageLineText2,whyToStop,"show"});
																						ExpPackageHistoryItem[0] = StepFileNameTmp;
																						ExpPackageHistoryItem[1] = requestData;
																						ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																						previousData = responseData;
																						ExpPackageHistoryItem[3] = responseData;
																						ExpPackageHistoryItem[4] = whyToStop;
																						ExpPackageHistoryList.add(ExpPackageHistoryItem);
																						expHTTPHistoryList.add(ExpPackageHistoryItem);
																						ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																						ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																						ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																						ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																						ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																						ExpPackageHistoryItem = new String[7];
																						responseStatus = 0;
																						responseData = "";
																						whyToStop = "Run Over";
																						GoAheadCurrent = false;
																						break;
																					}
						                                            
																					if(GoAheadExp)
																					{
																						JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,expFileLineTxt,useFileForCycleWithPackageLineText1+"-|-"+useFileForCycleWithPackageLineText2,whyToStop,"show"});
																						ExpPackageHistoryItem[0] = StepFileNameTmp;
																						ExpPackageHistoryItem[1] = requestData;
																						ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																						previousData = responseData;
																						ExpPackageHistoryItem[3] = responseData;
																						ExpPackageHistoryItem[4] = whyToStop;
																						ExpPackageHistoryList.add(ExpPackageHistoryItem);
																						expHTTPHistoryList.add(ExpPackageHistoryItem);
																						ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																						ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																						ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																						ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																						ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																						ExpPackageHistoryItem = new String[7];
																						responseStatus = 0;
																						responseData = "";
																						whyToStop = "Run Over";
																						break;
																					}
																					JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,expFileLineTxt,useFileForCycleWithPackageLineText1+"-|-"+useFileForCycleWithPackageLineText2,whyToStop,"show"});
																					ExpPackageHistoryItem[0] = StepFileNameTmp;
																					ExpPackageHistoryItem[1] = requestData;
																					ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																					previousData = responseData;
																					ExpPackageHistoryItem[3] = responseData;
																					ExpPackageHistoryItem[4] = whyToStop;
																					ExpPackageHistoryList.add(ExpPackageHistoryItem);
																					expHTTPHistoryList.add(ExpPackageHistoryItem);
																					ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																					ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																					ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																					ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																					ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																					ExpPackageHistoryItem = new String[7];
																					responseStatus = 0;
																					responseData = "";
																					whyToStop = "Run Over";
				                                        																			
																				}
																			}
																		}
																	}
																}            
															}
															else
															{
																//System.out.println("meiyou package file cyle");

																if (lineTxtTmp[1].trim().split(":").length > 1)
																{
																	host = Method.getFromBase64(lineTxtTmp[1].trim().split(":")[1]);
																}
																else
																{
																	break;
																}

																
																if (lineTxtTmp[2].trim().split(":").length > 1)
																{
																	port = Integer.valueOf(Method.getFromBase64(lineTxtTmp[2].trim().split(":")[1]));
																}
																else
																{
																	port=80;
																}
		                    
																
																StepFileNameTmp = Method.getFromBase64(lineTxtTmp[3].trim().split(":")[1]);									
																if(StepFileNameTmp.length()>0)
																{
																	requestData = Method.bytesToString(Method.HexStringTobytes(Method.stringsToString(Method.readFileToHexStrings(fileDir+"\\"+StepFileNameTmp))));
																}
																
																if(previousURL.length()>0)
																{
																	// 添加 phpcms/  到request 的 GET  phpcms/。。。。  http/1.1
																	requestData = Method.addPreviousURL(requestData,previousURL);
																}
			                          
																String replaceHostPortString = lineTxtTmp[4].trim().split(":")[1].trim();
																if (replaceHostPortString.equals("true"))
																{
																	host = targetHost;
																	port = targetPort;
																	requestData = Method.replaceHostPort(requestData,host,port);
																}
																else
																{
																	
																}
																				
				                          
				                          
																String useSetCookieFromPreviousString = lineTxtTmp[5].trim().split(":")[1].trim();
																if (useSetCookieFromPreviousString.equals("true"))
																{
																	/*
																	previousData = "HTTP/1.1 200 OK"+"\r\n"+
																						  "Date: Sun, 30 Nov 2014 09:16:33 GMT"+"\r\n"+
																						  "Server: Apache/2.2.9 (APMServ) PHP/5.2.6+"+"\r\n"+
																						  "X-Powered-By: PHP/5.2.6"+"\r\n"+
																						  "Set-Cookie: SYSZC=d2d4a91a4e41bea2078b09c29cd6a44a"+"\r\n"+
																						  "Set-Cookie: SYSUSER=admin"+"\r\n"+
																						  "Set-Cookie: SYSNAME=%E7%B3%BB%E7%BB%9F%E7%AE%A1%E7%90%86%E5%91%98"+"\r\n"+
																						  "Set-Cookie: SYSUSERID=3"+"\r\n"+
																						  "Set-Cookie: SYSTM=1417338994"+"\r\n"+
																						  "Content-Length: 2"+"\r\n"+
																						  "Content-Type: text/html"+"\r\n"+
																						  "\r\n"+							  
																						  "OK";
				                            
				                            						*/
																	String[] tmpmp = Method.getHTTPHeadersParams(previousData);
				                    		
																	String setCookieStr = "";
																	try
																	{
																	for(int n=0;n<tmpmp.length;n++)
																	{
																		if(n%2==0)
																		{
																			if(tmpmp[n].startsWith("Set-Cookie"))
																			{
																				setCookieStr =setCookieStr+tmpmp[n+1].trim()+";";
																			}
																		}
																	}
																	String OKrequestData="";	                    		
																	String[] requestDatas = requestData.split("\r\n");
																	boolean addCookie = true;
																	
																	for(int n=0;n<requestDatas.length;n++)
																	{		
																		requestDatas[n] = requestDatas[n]+"\r\n";
																		if(requestDatas[n].toLowerCase().trim().startsWith("cookie:"))
																		{
																			String[] tmpCookie = requestDatas[n].toLowerCase().trim().split("cookie:");
																			if(tmpCookie.length>1)
																			{
																				tmpCookie[1] = Method.replaceCookie(tmpCookie[1].trim(),setCookieStr);
																				requestDatas[n] = "Cookie: "+Method.stringsToString(tmpCookie)+"\r\n";
																				addCookie = false;
																			}
																		}
																	}
				                        	  
																	if(addCookie)
																	{
																		OKrequestData =Method.addHeader(requestData,"Cookie: ",setCookieStr);	                        			
																	}
																	else
																	{
																		OKrequestData = Method.stringsToString(requestDatas);
																	}
																	requestData = OKrequestData;  
																	}
																	catch(Exception e5)
																	{
																		
																	}
																	
																	 
				                            
																}
																else
																{
																	
																}
																
				                          	
																int randomStringTmpLength = lineTxtTmp[6].trim().split(":").length;
																if (randomStringTmpLength > 1)
																{
																	String randomStringString = lineTxtTmp[6].trim().split(":")[1].trim();
																	String[] randomStringItemStrings = randomStringString.split(",");
																	int randomStringItemCount = randomStringItemStrings.length;
																	for (int k = 0; k < randomStringItemCount; k++)
																	{
																		String[] randomStringItem = new String[5];
																		randomStringItem[0] = Method.getFromBase64(randomStringItemStrings[k]).split(",")[0];
																		randomStringItem[1] = Method.getFromBase64(randomStringItemStrings[k]).split(",")[1];
																		randomStringItem[2] =Method.getFromBase64(randomStringItemStrings[k]).split(",")[2].substring(0, 1);
																		randomStringItem[4] = Method.getFromBase64(randomStringItemStrings[k]).split(",")[3];

																		if (Integer.valueOf(randomStringItem[1]).intValue() == 3)
																		{
																			randomStringItem[3] = randomStringItem[2].substring(1);
																			randomStringItem[2] = randomStringItem[2].substring(0, 1);
																		}										
				                            		
																		String strTmp = "rSI"+randomStringItem[0];
																		strTmp = Method.getBase64(strTmp);
																		strTmp = Method.addSignRegex(strTmp);
																		String randomStrTmp = "";
																		switch(Integer.valueOf(randomStringItem[1]))
																		{
																			case 0:randomStrTmp =new RandomString().getRandomString(Integer.valueOf(randomStringItem[2]),"i");requestData =requestData.replaceAll(strTmp, randomStrTmp);break;
																			case 1:randomStrTmp =new RandomString().getRandomString(Integer.valueOf(randomStringItem[2]),"l");requestData =requestData.replaceAll(strTmp, randomStrTmp);break;
																			case 2:randomStrTmp =new RandomString().getRandomString(Integer.valueOf(randomStringItem[2]),"il");requestData =requestData.replaceAll(strTmp, randomStrTmp);break;
																			case 3:randomStrTmp =new RandomString().getRandomStringFromDefineString(randomStringItem[3],Integer.valueOf(randomStringItem[2]));requestData =requestData.replaceAll(strTmp, randomStrTmp);break;																																															
																		}                       		
																		randomStringList.add(randomStringItem);
																	}														
																}                      	
																
																int captchaTmpLength = lineTxtTmp[7].trim().split(":").length;
																if (captchaTmpLength > 1)
																{
																	String captchaString = lineTxtTmp[7].trim().split(":")[1].trim();
																	String[] captchaItemStrings = captchaString.split(",");
																	int captchaItemCount = captchaItemStrings.length;
																	for (int k = 0; k < captchaItemCount; k++)
																	{
																		String[] captchaItem = new String[4];
																		captchaItem = Method.getFromBase64(captchaItemStrings[k]).split(",");
																		captchaList.add(captchaItem);
																		
																		String captchaStrTmp = "sPFC"+captchaItem[0];
																		captchaStrTmp = Method.getBase64(captchaStrTmp);
																		captchaStrTmp = Method.addSignRegex(captchaStrTmp);	                                
																		boolean status = UUAPI.checkAPI();

																		if (!status)
																		{
																			JOptionPane.showMessageDialog(null, "API文件校验失败，无法使用打码服务", "Warning", 2);
																		}
																		else
																		{
																			final String ImgURL = "http://"+host+previousURL+captchaItem[1];  //获取验证码的url   
																	//		System.out.println(ImgURL);                                       
																			final int CaptchaNumbersTextInt = Integer.valueOf(captchaItem[2]);
																			final String saveFileName = "img/tmp.jpg";
																			
																			String saveAbsolutePath = Method.getImgFromURL(ImgURL,saveFileName);
																			if (saveAbsolutePath != null)
																			{
																				if (CaptchaNumbersTextInt != 0)
																				{
																					try 
																					{
																						String[] captchaResult = UUAPI.easyDecaptcha(saveFileName, 1000 + CaptchaNumbersTextInt);
																						if (captchaResult[1].length() > 0)
																						{
																							requestData = requestData.replaceAll(captchaStrTmp, captchaResult[1]);   
																						}
																					} 
																					catch (IOException eee) 
																					{
																						eee.printStackTrace();
																					}	                                                    
																				}	                          					
																			}																	
																		}	                                
																	}
																}	                            
																
				                            
																//
																String FileForCycleWithEXPStrTmp = "FileForCycleWithEXP";
																FileForCycleWithEXPStrTmp = Method.getBase64(FileForCycleWithEXPStrTmp);
																FileForCycleWithEXPStrTmp = Method.addSignRegex(FileForCycleWithEXPStrTmp);
																requestData = requestData.replaceAll(FileForCycleWithEXPStrTmp, expFileLineTxt);	                          			                         
				                          		
																// 发送http 数据
																
																responseData = Method.bytesToString(Method.getHttPResponseData(host,port,requestData));
														//		System.out.println(requestData);
														//		System.out.println("--");
														//		System.out.println(responseData);
																responseStatus = Method.getHttpResponseHeaderStatus(responseData);
																													
				                          		
																int setGoAheadOrStopFromStatusTmpLength = lineTxtTmp[9].trim().split(":").length;
																if (setGoAheadOrStopFromStatusTmpLength > 1)
																{
																	String setGoAheadOrStopFromStatusString = lineTxtTmp[9].trim().split(":")[1].trim();
																	String[] setGoAheadOrStopFromStatusItemStrings = setGoAheadOrStopFromStatusString.split(",");
																	int setGoAheadOrStopFromStatusItemCount = setGoAheadOrStopFromStatusItemStrings.length;
																	for (int k = 0; k < setGoAheadOrStopFromStatusItemCount; k++)
																	{
																		String[] setGoAheadOrStopFromStatusItem = new String[2];
																		setGoAheadOrStopFromStatusItem = Method.getFromBase64(setGoAheadOrStopFromStatusItemStrings[k]).split(",");
																		setGoAheadOrStopFromStatusList.add(setGoAheadOrStopFromStatusItem);															
																	}

																}      		
				                          		
																int setGoAheadOrStopFromStatusListCount = setGoAheadOrStopFromStatusList.size();
																if (setGoAheadOrStopFromStatusListCount > 0)
																{
																	for (int j = 0; j < setGoAheadOrStopFromStatusListCount; j++)
																	{
																		String[] setGoAheadOrStopFromStatusListItem = (String[])setGoAheadOrStopFromStatusList.get(j);
																		
																		if (responseStatus == Integer.valueOf(setGoAheadOrStopFromStatusListItem[0]))
																		{
																			String actionForStatus = setGoAheadOrStopFromStatusListItem[1];
				                                			
				                                			
																			//                         GO ahead current   stop current 
																			///                                               ||
																			//																		
																			///											Go Exp    Stop Exp
																		
																			
																			if(actionForStatus.equals("Go Ahead EXP"))
																			{
																				GoAheadExp  = true;
																				whyToStop = "responseStatus; " + responseStatus + " -- Rule; " + setGoAheadOrStopFromStatusListItem[0] + " --Action; "+ setGoAheadOrStopFromStatusListItem[1];
																				break;
																			}
																			else if(actionForStatus.equals("Stop EXP"))
																			{
																				StopExp = true; 				
																				whyToStop = "responseStatus; " + responseStatus + " -- Rule; " + setGoAheadOrStopFromStatusListItem[0] + " --Action; "+ setGoAheadOrStopFromStatusListItem[1];
																				break;
																			}
																			else if(actionForStatus.equals("Go Ahead Current"))
																			{
																				GoAheadCurrent = true;
																				whyToStop = "responseStatus; " + responseStatus + " -- Rule; " + setGoAheadOrStopFromStatusListItem[0] + " --Action; "+ setGoAheadOrStopFromStatusListItem[1];
																				break;
																			}
																			else if(actionForStatus.equals("Stop Current"))
																			{
																				//StopCurrent = true;
																				//whyToStop = "responseStatus:" + responseStatus + " -- Rule" + setGoAheadOrStopFromStatusListItem[0] + setGoAheadOrStopFromStatusListItem[1];
																				//System.out.println(whyToStop);
																				//break;
																			}                                      			
																			
																		}
																	}
																}
																else
																{
																	//System.out.println("mei you setGoAheadOrStopFromStatusListCount");
																}

																if ((StopExp) )
																{
																	JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,expFileLineTxt,"- -",whyToStop,"show"});
																	ExpPackageHistoryItem[0] = StepFileNameTmp;
																	ExpPackageHistoryItem[1] = requestData;
																	ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																	previousData = responseData;
																	ExpPackageHistoryItem[3] = responseData;
																	ExpPackageHistoryItem[4] = whyToStop;
																	ExpPackageHistoryList.add(ExpPackageHistoryItem);
																	expHTTPHistoryList.add(ExpPackageHistoryItem);
																	ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																	ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																	ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																	ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																	ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																	ExpPackageHistoryItem = new String[7];
																	responseStatus = 0;
																	responseData = "";
																	whyToStop = "Run Over";
																	break;
																}
				                                
																if(GoAheadCurrent)
																{
																	JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,expFileLineTxt,"- -",whyToStop,"show"});
																	ExpPackageHistoryItem[0] = StepFileNameTmp;
																	ExpPackageHistoryItem[1] = requestData;
																	ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																	previousData = responseData;
																	ExpPackageHistoryItem[3] = responseData;
																	ExpPackageHistoryItem[4] = whyToStop;
																	ExpPackageHistoryList.add(ExpPackageHistoryItem);
																	expHTTPHistoryList.add(ExpPackageHistoryItem);
																	ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																	ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																	ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																	ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																	ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																	ExpPackageHistoryItem = new String[7];
																	responseStatus = 0;
																	responseData = "";
																	whyToStop = "Run Over";
																	GoAheadCurrent = false;
																	break;
																}
				                                
																if(GoAheadExp)
																{
																	JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,expFileLineTxt,"- -",whyToStop,"show"});
																	ExpPackageHistoryItem[0] = StepFileNameTmp;
																	ExpPackageHistoryItem[1] = requestData;
																	ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																	previousData = responseData;
																	ExpPackageHistoryItem[3] = responseData;
																	ExpPackageHistoryItem[4] = whyToStop;
																	ExpPackageHistoryList.add(ExpPackageHistoryItem);
																	expHTTPHistoryList.add(ExpPackageHistoryItem);
																	ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																	ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																	ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																	ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																	ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																	ExpPackageHistoryItem = new String[7];
																	responseStatus = 0;
																	responseData = "";
																	whyToStop = "Run Over";
																	break;
																}
				                               										
																
																int setGoAheadOrStopFromDataTmpLength = lineTxtTmp[10].trim().split(":").length;
																if (setGoAheadOrStopFromDataTmpLength > 1)
																{
																	String setGoAheadOrStopFromDataString = lineTxtTmp[10].trim().split(":")[1].trim();
																	String[] setGoAheadOrStopFromDataItemStrings = setGoAheadOrStopFromDataString.split(",");
																	int setGoAheadOrStopFromDataItemCount = setGoAheadOrStopFromDataItemStrings.length;
																	for (int k = 0; k < setGoAheadOrStopFromDataItemCount; k++)
																	{
																		String[] setGoAheadOrStopFromDataItem = new String[2];
																		setGoAheadOrStopFromDataItem = Method.getFromBase64(setGoAheadOrStopFromDataItemStrings[k]).split(",");
																		setGoAheadOrStopFromDataList.add(setGoAheadOrStopFromDataItem);													
																	}

																}
																						
																
				                                
																
																int j;
																int setGoAheadOrStopFromDataListCount = setGoAheadOrStopFromDataList.size();
																if (setGoAheadOrStopFromDataListCount > 0)
																{
																	for (j = 0; j < setGoAheadOrStopFromDataListCount; j++)
																	{
																		String[] setGoAheadOrStopFromDataListItem = (String[])setGoAheadOrStopFromDataList.get(j);
																		Pattern p=Pattern.compile(Method.getFromBase64(setGoAheadOrStopFromDataListItem[0]));
																		Matcher m=p.matcher(responseData);
																	//	System.out.println(m.find());
																		
																		if (m.find())
																		{
																			String actionForData = setGoAheadOrStopFromDataListItem[1];
																			
																			if(actionForData.equals("Go Ahead EXP"))
																			{
																				GoAheadExp  = true;
																				whyToStop = " -- Rule; " + Method.getFromBase64(setGoAheadOrStopFromDataListItem[0]) +"  --result; "+m.group()+ " --Action; "+ setGoAheadOrStopFromDataListItem[1];
																				break;
																			}
																			else if(actionForData.equals("Stop EXP"))
																			{
																				StopExp = true;
																				whyToStop = " -- Rule; " + Method.getFromBase64(setGoAheadOrStopFromDataListItem[0]) +"  --result; "+m.group()+ " --Action; "+ setGoAheadOrStopFromDataListItem[1];
																				break;
																			}
																			else if(actionForData.equals("Go Ahead Current"))
																			{
																				GoAheadCurrent = true;
																				whyToStop = " -- Rule; " + Method.getFromBase64(setGoAheadOrStopFromDataListItem[0]) +"  --result; "+m.group()+ " --Action; "+ setGoAheadOrStopFromDataListItem[1];
																				break;
																			}
																			else if(actionForData.equals("Stop Current"))
																			{
																				//StopCurrent = true;
																				//whyToStop = "responseStatus:" + responseStatus + " -- Rule" + setGoAheadOrStopFromStatusListItem[0] + setGoAheadOrStopFromStatusListItem[1];
																				//System.out.println(whyToStop);
																				//break;
																			} 
																			
																		}
																	}
																}
																else
																{
																	//System.out.println("meiyou setGoAheadOrStopFromDataList");
																}

																if ((StopExp) )
																{
																	JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,expFileLineTxt,"- -",whyToStop,"show"});
																	ExpPackageHistoryItem[0] = StepFileNameTmp;
																	ExpPackageHistoryItem[1] = requestData;
																	ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																	previousData = responseData;
																	ExpPackageHistoryItem[3] = responseData;
																	ExpPackageHistoryItem[4] = whyToStop;
																	ExpPackageHistoryList.add(ExpPackageHistoryItem);
																	expHTTPHistoryList.add(ExpPackageHistoryItem);
																	ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																	ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																	ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																	ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																	ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																	ExpPackageHistoryItem = new String[7];
																	responseStatus = 0;
																	responseData = "";
																	whyToStop = "Run Over";
																	break;
																}
				                                
																if(GoAheadCurrent)
																{
																	JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,expFileLineTxt,"- -",whyToStop,"show"});
																	ExpPackageHistoryItem[0] = StepFileNameTmp;
																	ExpPackageHistoryItem[1] = requestData;
																	ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																	previousData = responseData;
																	ExpPackageHistoryItem[3] = responseData;
																	ExpPackageHistoryItem[4] = whyToStop;
																	ExpPackageHistoryList.add(ExpPackageHistoryItem);
																	expHTTPHistoryList.add(ExpPackageHistoryItem);
																	ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																	ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																	ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																	ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																	ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																	ExpPackageHistoryItem = new String[7];
																	responseStatus = 0;
																	responseData = "";
																	whyToStop = "Run Over";
																	GoAheadCurrent = false;
																	break;
																}
				                                
																if(GoAheadExp)
																{
																	JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,expFileLineTxt,"- -",whyToStop,"show"});
																	ExpPackageHistoryItem[0] = StepFileNameTmp;
																	ExpPackageHistoryItem[1] = requestData;
																	ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																	previousData = responseData;
																	ExpPackageHistoryItem[3] = responseData;
																	ExpPackageHistoryItem[4] = whyToStop;
																	ExpPackageHistoryList.add(ExpPackageHistoryItem);
																	expHTTPHistoryList.add(ExpPackageHistoryItem);
																	ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																	ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																	ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																	ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																	ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																	ExpPackageHistoryItem = new String[7];
																	responseStatus = 0;
																	responseData = "";
																	whyToStop = "Run Over";
																	break;
																}

																JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,expFileLineTxt,"- -",whyToStop,"show"});
																ExpPackageHistoryItem[0] = StepFileNameTmp;
																ExpPackageHistoryItem[1] = requestData;
																ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																previousData = responseData;
																ExpPackageHistoryItem[3] = responseData;
																ExpPackageHistoryItem[4] = whyToStop;
																ExpPackageHistoryList.add(ExpPackageHistoryItem);
																expHTTPHistoryList.add(ExpPackageHistoryItem);
																ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																ExpPackageHistoryItem = new String[7];
																responseStatus = 0;
																responseData = "";
																whyToStop = "Run Over";
				                          		
																
															}                    
														}
														else
														{
															//System.out.println("lineTxtTmpLength  !===== 131311313");
														}                                   
													}
													else
													{
														//System.out.println("lineTxtTmpLength>>>>>>>>>>>..0");
													}
												} 
												if(GoAheadExp)
												{								
													GoAheadExp  = false;
													continue;
												}
											}
				            
										}
										else
										{
											//System.out.println("meiyou fileForCycleWithEXP");
										
											boolean StopExp = false;
											boolean StopCurrent =false;
											boolean GoAheadCurrent = false;
											boolean GoAheadExp = false;
											List ExpPackageHistoryList = new ArrayList();
											
											String[] ExpPackagePreviousItem = new String[5];
											for (int i = 0; i < StepNums&&!StopExp; i++)
											{
												String StringForEachStep = (String)tmplist.get(i);									
												
												if (StringForEachStep.length() > 0)
												{
													StepFileNameTmp = "";
				                  
													String requestData = "";
													String responseData = "";
													int responseStatus = 0;
													String whyToStop = "Run Over";

													String[] ExpPackageHistoryItem = new String[5];
				                  
													String host = "";
													int port = 80;
													
													String previousData="";
													
													randomStringList = new ArrayList();
													captchaList = new ArrayList();
													setParamsFromDataPackageList = new ArrayList();
													setGoAheadOrStopFromStatusList = new ArrayList();
													setGoAheadOrStopFromDataList = new ArrayList();
													useFileForCycleWithPackageList = new ArrayList();
													
													String[] lineTxtTmp = StringForEachStep.split("-");
													int lineTxtTmpLength = lineTxtTmp.length;								
				                  
													if (lineTxtTmpLength == 13)
													{
				                	  
														//System.out.println("useFileForCycleWithPackage");
														int useFileForCycleWithPackageTmpLength = lineTxtTmp[11].trim().split(":").length;
														if (useFileForCycleWithPackageTmpLength > 1)
														{
															String useFileForCycleWithPackageString = lineTxtTmp[11].trim().split(":")[1].trim();
															String[] useFileForCycleWithPackageItemStrings = useFileForCycleWithPackageString.split(",");
															int useFileForCycleWithPackageItemCount = useFileForCycleWithPackageItemStrings.length;
															for (int k = 0; k < useFileForCycleWithPackageItemCount; k++)
															{
																String[] useFileForCycleWithPackageItem = new String[6];
																useFileForCycleWithPackageItem = Method.getFromBase64(useFileForCycleWithPackageItemStrings[k]).split(",");

																if (Integer.valueOf(useFileForCycleWithPackageItem[0]).intValue() == 1)
																{
																	useFileForCycleWithPackageItem[1] = Method.getFromBase64(useFileForCycleWithPackageItem[1].trim());									
																}
																else
																{
																	useFileForCycleWithPackageItem[1] = Method.getFromBase64(useFileForCycleWithPackageItem[1].trim());
																	useFileForCycleWithPackageItem[2] = Method.getFromBase64(useFileForCycleWithPackageItem[2].trim());
																	useFileForCycleWithPackageItem[3] = useFileForCycleWithPackageItem[3].trim();						
																}
																useFileForCycleWithPackageList.add(useFileForCycleWithPackageItem);
															}										
				                          
															int useFileForCycleWithPackageListCount = useFileForCycleWithPackageList.size();												
															for (int t = 0; t < useFileForCycleWithPackageListCount; t++)
															{
																String[] useFileForCycleWithPackageItem = (String[])useFileForCycleWithPackageList.get(t);
																if (Integer.valueOf(useFileForCycleWithPackageItem[0]) == 1)
																{
																	//System.out.println("package 一个文件");
																	
																	File useFileForCycleWithPackageFile1 = new File(fileDir+useFileForCycleWithPackageItem[1]);
																	InputStreamReader useFileForCycleWithPackageFile1InputSteamReadTmp = new InputStreamReader(new FileInputStream(useFileForCycleWithPackageFile1));
																	BufferedReader useFileForCycleWithPackageFile1ExpFileBufferedReader = new BufferedReader(useFileForCycleWithPackageFile1InputSteamReadTmp);
																	String useFileForCycleWithPackageLineText = null;
																	
																	int PackageNum=0;
																	
																	while ((useFileForCycleWithPackageLineText = useFileForCycleWithPackageFile1ExpFileBufferedReader.readLine()) != null)
																	{
																		
																		PackageNum++;
																		if (lineTxtTmp[1].trim().split(":").length > 1)
																		{
																			host = Method.getFromBase64(lineTxtTmp[1].trim().split(":")[1]);															
																		}
																		else
																		{
																			break;
																		}

																		
																		if (lineTxtTmp[2].trim().split(":").length > 1)
																		{
																			port = Integer.valueOf(Method.getFromBase64(lineTxtTmp[2].trim().split(":")[1]));
																		}
																		else
																		{
																			port=80;
																		}
																
																		StepFileNameTmp = Method.getFromBase64(lineTxtTmp[3].trim().split(":")[1]);									
																		if(StepFileNameTmp.length()>0)
																		{
																			requestData = Method.bytesToString(Method.HexStringTobytes(Method.stringsToString(Method.readFileToHexStrings(fileDir+"\\"+StepFileNameTmp))));
																		}
																		       
																		
																		if(previousURL.length()>0)
																		{
																			// 添加 phpcms/  到request 的 GET  phpcms/。。。。  http/1.1
																			requestData = Method.addPreviousURL(requestData,previousURL);
																		}
				                                  
																		String replaceHostPortString = lineTxtTmp[4].trim().split(":")[1].trim();
																		if (replaceHostPortString.equals("true"))
																		{
																			host = targetHost;
																			port = targetPort;
																			requestData = Method.replaceHostPort(requestData,host,port);
																		}
																		else
																		{
																		}
																		
																		
																		
				                                  
																		
				                                  
				                                  
																		String useSetCookieFromPreviousString = lineTxtTmp[5].trim().split(":")[1].trim();
																		if (useSetCookieFromPreviousString.equals("true"))
																		{
																			/*
																			previousData = "HTTP/1.1 200 OK"+"\r\n"+
																								  "Date: Sun, 30 Nov 2014 09:16:33 GMT"+"\r\n"+
																								  "Server: Apache/2.2.9 (APMServ) PHP/5.2.6+"+"\r\n"+
																								  "X-Powered-By: PHP/5.2.6"+"\r\n"+
																								  "Set-Cookie: SYSZC=d2d4a91a4e41bea2078b09c29cd6a44a"+"\r\n"+
																								  "Set-Cookie: SYSUSER=admin"+"\r\n"+
																								  "Set-Cookie: SYSNAME=%E7%B3%BB%E7%BB%9F%E7%AE%A1%E7%90%86%E5%91%98"+"\r\n"+
																								  "Set-Cookie: SYSUSERID=3"+"\r\n"+
																								  "Set-Cookie: SYSTM=1417338994"+"\r\n"+
																								  "Content-Length: 2"+"\r\n"+
																								  "Content-Type: text/html"+"\r\n"+
																								  "\r\n"+							  
																								  "OK";
				                                    						*/
				                                    
																			String[] tmpmp = Method.getHTTPHeadersParams(previousData);                          		
																			String setCookieStr = "";
																			try
																			{
																			for(int n=0;n<tmpmp.length;n++)
																			{
																				if(n%2==0)
																				{
																					if(tmpmp[n].startsWith("Set-Cookie"))
																					{
																						setCookieStr =setCookieStr+tmpmp[n+1].trim()+";";
																					}
																				}
																			}
																			String OKrequestData="";	                            		
																			String[] requestDatas = requestData.split("\r\n");
																			boolean addCookie = true;
																			
																			for(int n=0;n<requestDatas.length;n++)
																			{		
																				requestDatas[n] = requestDatas[n]+"\r\n";
																				if(requestDatas[n].toLowerCase().trim().startsWith("cookie:"))
																				{
																					String[] tmpCookie = requestDatas[n].toLowerCase().trim().split("cookie:");
																					if(tmpCookie.length>1)
																					{
																						tmpCookie[1] = Method.replaceCookie(tmpCookie[1].trim(),setCookieStr);
																						requestDatas[n] = "Cookie: "+Method.stringsToString(tmpCookie)+"\r\n";
																						addCookie = false;
																					}
																				}
																			}
				                                	  
																			if(addCookie)
																			{
																				OKrequestData =Method.addHeader(requestData,"Cookie: ",setCookieStr);	                                			
																			}
																			else
																			{
																				OKrequestData = Method.stringsToString(requestDatas);
																			}
																			requestData = OKrequestData; 
																			}
																			catch(Exception e5)
																			{}
				                            		
																			  
				                                    
																		}
																		else
																		{
																			//useSetCookieFromPrevious = false;
																		}
																		
				                                  	
																		int randomStringTmpLength = lineTxtTmp[6].trim().split(":").length;
																		if (randomStringTmpLength > 1)
																		{
																			String randomStringString = lineTxtTmp[6].trim().split(":")[1].trim();
																			String[] randomStringItemStrings = randomStringString.split(",");
																			int randomStringItemCount = randomStringItemStrings.length;
																			for (int k = 0; k < randomStringItemCount; k++)
																			{
																				String[] randomStringItem = new String[5];
																				randomStringItem[0] = Method.getFromBase64(randomStringItemStrings[k]).split(",")[0];
																				randomStringItem[1] = Method.getFromBase64(randomStringItemStrings[k]).split(",")[1];
																				randomStringItem[2] =Method.getFromBase64(randomStringItemStrings[k]).split(",")[2].substring(0, 1);
																				randomStringItem[4] = Method.getFromBase64(randomStringItemStrings[k]).split(",")[3];
																		
																				if (Integer.valueOf(randomStringItem[1]).intValue() == 3)
																				{
																					randomStringItem[3] = randomStringItem[2].substring(1);
																					randomStringItem[2] = randomStringItem[2].substring(0, 1);
																				}																	
				                                    		
																				String strTmp = "rSI"+randomStringItem[0];
																				strTmp = Method.getBase64(strTmp);
																				strTmp = Method.addSignRegex(strTmp);
																				String randomStrTmp = "";
																				switch(Integer.valueOf(randomStringItem[1]))
																				{
																					case 0:randomStrTmp =new RandomString().getRandomString(Integer.valueOf(randomStringItem[2]),"i");requestData =requestData.replaceAll(strTmp, randomStrTmp);break;
																					case 1:randomStrTmp =new RandomString().getRandomString(Integer.valueOf(randomStringItem[2]),"l");requestData =requestData.replaceAll(strTmp, randomStrTmp);break;
																					case 2:randomStrTmp =new RandomString().getRandomString(Integer.valueOf(randomStringItem[2]),"il");requestData =requestData.replaceAll(strTmp, randomStrTmp);break;
																					case 3:randomStrTmp =new RandomString().getRandomStringFromDefineString(randomStringItem[3],Integer.valueOf(randomStringItem[2]));requestData =requestData.replaceAll(strTmp, randomStrTmp);break;																																																
																				}		                                    		
																				randomStringList.add(randomStringItem);
																			}																	
																		}                                  		
				                                  	
																		
																		int captchaTmpLength = lineTxtTmp[7].trim().split(":").length;
																		if (captchaTmpLength > 1)
																		{
																			String captchaString = lineTxtTmp[7].trim().split(":")[1].trim();
																			String[] captchaItemStrings = captchaString.split(",");
																			int captchaItemCount = captchaItemStrings.length;
																			for (int k = 0; k < captchaItemCount; k++)
																			{
																				String[] captchaItem = new String[4];
																				captchaItem = Method.getFromBase64(captchaItemStrings[k]).split(",");
																				captchaList.add(captchaItem);
																				
																				String captchaStrTmp = "sPFC"+captchaItem[0];
																				captchaStrTmp = Method.getBase64(captchaStrTmp);
																				captchaStrTmp = Method.addSignRegex(captchaStrTmp);
				                                        
				                                        
																				boolean status = UUAPI.checkAPI();

																				if (!status)
																				{
																					JOptionPane.showMessageDialog(null, "API文件校验失败，无法使用打码服务", "Warning", 2);
																				}
																				else
																				{
																					final String ImgURL = "http://"+host+previousURL+captchaItem[1];  //获取验证码的url  
																			//		System.out.println(ImgURL);                                           
																					final int CaptchaNumbersTextInt = Integer.valueOf(captchaItem[2]);
																					final String saveFileName = "img/tmp.jpg";
																					
																					String saveAbsolutePath = Method.getImgFromURL(ImgURL,saveFileName);
																					if (saveAbsolutePath != null)
																					{
																						if (CaptchaNumbersTextInt != 0)
																						{
																							try 
																							{
																								String[] captchaResult = UUAPI.easyDecaptcha(saveFileName, 1000 + CaptchaNumbersTextInt);
																								if (captchaResult[1].length() > 0)
																								{
																									requestData = requestData.replaceAll(captchaStrTmp, captchaResult[1]);   
																								}
																							} 
																							catch (IOException eee) 
																							{
																								eee.printStackTrace();
																							}	                                                    
																						}		                                  					
																					}																			
																				}		                                        
																			}
																		}
				                                    	                                 
				                                    
																		//useFileForCycleWithPackageLineText
																		String useFileForCycleWithPackageLineStrTmp = "File-1";
																		useFileForCycleWithPackageLineStrTmp = Method.getBase64(useFileForCycleWithPackageLineStrTmp);
																		useFileForCycleWithPackageLineStrTmp = Method.addSignRegex(useFileForCycleWithPackageLineStrTmp);
																		requestData = requestData.replaceAll(useFileForCycleWithPackageLineStrTmp, useFileForCycleWithPackageLineText);
				                                  		
				                                  	
				                                  		
																		// 发送http 数据
																		
																		responseData = Method.bytesToString(Method.getHttPResponseData(host,port,requestData));
																	//	System.out.println(requestData);
																	//	System.out.println("--");
																	//	System.out.println(responseData);
																		responseStatus = Method.getHttpResponseHeaderStatus(responseData);
																		                                  		
																		int setGoAheadOrStopFromStatusTmpLength = lineTxtTmp[9].trim().split(":").length;
																		if (setGoAheadOrStopFromStatusTmpLength > 1)
																		{
																			String setGoAheadOrStopFromStatusString = lineTxtTmp[9].trim().split(":")[1].trim();
																			String[] setGoAheadOrStopFromStatusItemStrings = setGoAheadOrStopFromStatusString.split(",");
																			int setGoAheadOrStopFromStatusItemCount = setGoAheadOrStopFromStatusItemStrings.length;
																			for (int k = 0; k < setGoAheadOrStopFromStatusItemCount; k++)
																			{
																				String[] setGoAheadOrStopFromStatusItem = new String[2];
																				setGoAheadOrStopFromStatusItem = Method.getFromBase64(setGoAheadOrStopFromStatusItemStrings[k]).split(",");
																				setGoAheadOrStopFromStatusList.add(setGoAheadOrStopFromStatusItem);																
																			}
																		}
				                                  		
				                                  		
				                                  		
				                                  		
																		int setGoAheadOrStopFromStatusListCount = setGoAheadOrStopFromStatusList.size();
																		if (setGoAheadOrStopFromStatusListCount > 0)
																		{
																			for (int j = 0; j < setGoAheadOrStopFromStatusListCount; j++)
																			{
																				String[] setGoAheadOrStopFromStatusListItem = (String[])setGoAheadOrStopFromStatusList.get(j);
																				if (responseStatus == Integer.valueOf(setGoAheadOrStopFromStatusListItem[0]))
																				{
																					String actionForStatus = setGoAheadOrStopFromStatusListItem[1];
				                                        				
																					//                         GO ahead current   stop current 
																					///                                               ||
																					//																		
																					///											Go Exp    Stop Exp
																					///
																					//	
																					
																					if(actionForStatus.equals("Go Ahead EXP"))
																					{
																						GoAheadExp  = true;
																						whyToStop = "responseStatus; " + responseStatus + " -- Rule; " + setGoAheadOrStopFromStatusListItem[0] + " --Action; "+ setGoAheadOrStopFromStatusListItem[1];
																						break;
																					}
																					else if(actionForStatus.equals("Stop EXP"))
																					{
																						StopExp = true;				
																						whyToStop = "responseStatus; " + responseStatus + " -- Rule; " + setGoAheadOrStopFromStatusListItem[0] + " --Action; "+ setGoAheadOrStopFromStatusListItem[1];
																						break;
																					}
																					else if(actionForStatus.equals("Go Ahead Current"))
																					{
																						GoAheadCurrent = true;
																						whyToStop = "responseStatus; " + responseStatus + " -- Rule; " + setGoAheadOrStopFromStatusListItem[0] + " --Action; "+ setGoAheadOrStopFromStatusListItem[1];
																						break;
																					}
																					else if(actionForStatus.equals("Stop Current"))
																					{
																						//StopCurrent = true;
																						//whyToStop = "responseStatus:" + responseStatus + " -- Rule" + setGoAheadOrStopFromStatusListItem[0] + setGoAheadOrStopFromStatusListItem[1];
																						//System.out.println(whyToStop);
																						//break;
																					}                                      															
																				}
																			}
																		}
																		else
																		{
																			//System.out.println("mei you setGoAheadOrStopFromStatusListCount");
																		}

																		if ((StopExp) )
																		{
																			JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,"- -",useFileForCycleWithPackageLineText,whyToStop,"show"});
																			ExpPackageHistoryItem[0] = StepFileNameTmp;
																			ExpPackageHistoryItem[1] = requestData;
																			ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																			previousData = responseData;
																			ExpPackageHistoryItem[3] = responseData;
																			ExpPackageHistoryItem[4] = whyToStop;
																			ExpPackageHistoryList.add(ExpPackageHistoryItem);
																			expHTTPHistoryList.add(ExpPackageHistoryItem);
																			ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																			ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																			ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																			ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																			ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																			ExpPackageHistoryItem = new String[7];
																			responseStatus = 0;
																			responseData = "";
																			whyToStop = "Run Over";
																			break;
																		}
				                                        
																		if(GoAheadCurrent)
																		{
																			JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,"- -",useFileForCycleWithPackageLineText,whyToStop,"show"});
																			ExpPackageHistoryItem[0] = StepFileNameTmp;
																			ExpPackageHistoryItem[1] = requestData;
																			ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																			previousData = responseData;
																			ExpPackageHistoryItem[3] = responseData;
																			ExpPackageHistoryItem[4] = whyToStop;
																			ExpPackageHistoryList.add(ExpPackageHistoryItem);
																			expHTTPHistoryList.add(ExpPackageHistoryItem);
																			ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																			ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																			ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																			ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																			ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																			ExpPackageHistoryItem = new String[7];
																			responseStatus = 0;
																			responseData = "";
																			whyToStop = "Run Over";
																			GoAheadCurrent = false;
																			break;
																		}
				                                        
																		if(GoAheadExp)
																		{
																			JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,"- -",useFileForCycleWithPackageLineText,whyToStop,"show"});
																			ExpPackageHistoryItem[0] = StepFileNameTmp;
																			ExpPackageHistoryItem[1] = requestData;
																			ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																			previousData = responseData;
																			ExpPackageHistoryItem[3] = responseData;
																			ExpPackageHistoryItem[4] = whyToStop;
																			ExpPackageHistoryList.add(ExpPackageHistoryItem);
																			expHTTPHistoryList.add(ExpPackageHistoryItem);
																			ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																			ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																			ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																			ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																			ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																			ExpPackageHistoryItem = new String[7];
																			responseStatus = 0;
																			responseData = "";
																			whyToStop = "Run Over";
																			break;
																		}
																		
																		int setGoAheadOrStopFromDataTmpLength = lineTxtTmp[10].trim().split(":").length;
																		if (setGoAheadOrStopFromDataTmpLength > 1)
																		{
																			String setGoAheadOrStopFromDataString = lineTxtTmp[10].trim().split(":")[1].trim();
																			String[] setGoAheadOrStopFromDataItemStrings = setGoAheadOrStopFromDataString.split(",");
																			int setGoAheadOrStopFromDataItemCount = setGoAheadOrStopFromDataItemStrings.length;
																			for (int k = 0; k < setGoAheadOrStopFromDataItemCount; k++)
																			{
																				String[] setGoAheadOrStopFromDataItem = new String[2];
																				setGoAheadOrStopFromDataItem = Method.getFromBase64(setGoAheadOrStopFromDataItemStrings[k]).split(",");
																				setGoAheadOrStopFromDataList.add(setGoAheadOrStopFromDataItem);
																			}
																		}
			                                        
																		
																		int j;
																		int setGoAheadOrStopFromDataListCount = setGoAheadOrStopFromDataList.size();
																		if (setGoAheadOrStopFromDataListCount > 0)
																		{
																			for (j = 0; j < setGoAheadOrStopFromDataListCount; j++)
																			{
																				String[] setGoAheadOrStopFromDataListItem = (String[])setGoAheadOrStopFromDataList.get(j);
																				Pattern p=Pattern.compile(Method.getFromBase64(setGoAheadOrStopFromDataListItem[0]));
																				Matcher m=p.matcher(responseData);
																			//	System.out.println(m.find());
																				
																				if (m.find())
																				{
																					String actionForData = setGoAheadOrStopFromDataListItem[1];
																					
																					if(actionForData.equals("Go Ahead EXP"))
																					{
																						GoAheadExp  = true;
																						whyToStop = " -- Rule; " + Method.getFromBase64(setGoAheadOrStopFromDataListItem[0]) +"  --result; "+m.group()+ " --Action; "+ setGoAheadOrStopFromDataListItem[1];
																						break;
																					}
																					else if(actionForData.equals("Stop EXP"))
																					{
																						StopExp = true;
																						whyToStop = " -- Rule; " + Method.getFromBase64(setGoAheadOrStopFromDataListItem[0]) +"  --result; "+m.group()+ " --Action; "+ setGoAheadOrStopFromDataListItem[1];
																						break;
																					}
																					else if(actionForData.equals("Go Ahead Current"))
																					{
																						GoAheadCurrent = true;
																						whyToStop = " -- Rule; " + Method.getFromBase64(setGoAheadOrStopFromDataListItem[0]) +"  --result; "+m.group()+ " --Action; "+ setGoAheadOrStopFromDataListItem[1];
																						break;
																					}
																					else if(actionForData.equals("Stop Current"))
																					{
																						//StopCurrent = true;
																						//whyToStop = "responseStatus:" + responseStatus + " -- Rule" + setGoAheadOrStopFromStatusListItem[0] + setGoAheadOrStopFromStatusListItem[1];
																						//System.out.println(whyToStop);
																						//break;
																					} 
																					
																				}
																			}
																		}
																		else
																		{
																			//System.out.println("meiyou setGoAheadOrStopFromDataList");
																		}

																		if ((StopExp) )
																		{
																			JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,"- -",useFileForCycleWithPackageLineText,whyToStop,"show"});
																			ExpPackageHistoryItem[0] = StepFileNameTmp;
																			ExpPackageHistoryItem[1] = requestData;
																			ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																			previousData = responseData;
																			ExpPackageHistoryItem[3] = responseData;
																			ExpPackageHistoryItem[4] = whyToStop;
																			ExpPackageHistoryList.add(ExpPackageHistoryItem);
																			expHTTPHistoryList.add(ExpPackageHistoryItem);
																			ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																			ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																			ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																			ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																			ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																			ExpPackageHistoryItem = new String[7];
																			responseStatus = 0;
																			responseData = "";
																			whyToStop = "Run Over";
																			break;
																		}
				                                        
																		if(GoAheadCurrent)
																		{
																			JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,"- -",useFileForCycleWithPackageLineText,whyToStop,"show"});
																			ExpPackageHistoryItem[0] = StepFileNameTmp;
																			ExpPackageHistoryItem[1] = requestData;
																			ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																			previousData = responseData;
																			ExpPackageHistoryItem[3] = responseData;
																			ExpPackageHistoryItem[4] = whyToStop;
																			ExpPackageHistoryList.add(ExpPackageHistoryItem);
																			expHTTPHistoryList.add(ExpPackageHistoryItem);
																			ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																			ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																			ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																			ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																			ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																			ExpPackageHistoryItem = new String[7];
																			responseStatus = 0;
																			responseData = "";
																			whyToStop = "Run Over";
																			GoAheadCurrent = false;
																			break;
																		}
				                                        
																		if(GoAheadExp)
																		{
																			JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,"- -",useFileForCycleWithPackageLineText,whyToStop,"show"});
																			ExpPackageHistoryItem[0] = StepFileNameTmp;
																			ExpPackageHistoryItem[1] = requestData;
																			ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																			previousData = responseData;
																			ExpPackageHistoryItem[3] = responseData;
																			ExpPackageHistoryItem[4] = whyToStop;
																			ExpPackageHistoryList.add(ExpPackageHistoryItem);
																			expHTTPHistoryList.add(ExpPackageHistoryItem);
																			ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																			ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																			ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																			ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																			ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																			ExpPackageHistoryItem = new String[7];
																			responseStatus = 0;
																			responseData = "";
																			whyToStop = "Run Over";
																			break;
																		}

																		JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,"- -",useFileForCycleWithPackageLineText,whyToStop,"show"});
																		ExpPackageHistoryItem[0] = StepFileNameTmp;
																		ExpPackageHistoryItem[1] = requestData;
																		ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																		previousData = responseData;
																		ExpPackageHistoryItem[3] = responseData;
																		ExpPackageHistoryItem[4] = whyToStop;
																		ExpPackageHistoryList.add(ExpPackageHistoryItem);
																		expHTTPHistoryList.add(ExpPackageHistoryItem);
																		ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																		ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																		ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																		ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																		ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																		ExpPackageHistoryItem = new String[7];
																		responseStatus = 0;
																		responseData = "";
																		whyToStop = "Run Over";
				                                  	
				                                  	
																	}
																}
																else if(Integer.valueOf(useFileForCycleWithPackageItem[0])==2)
																{
																	//System.out.println("两个文件");												
																	
																	//组合方式1   一对一
																	if(Integer.valueOf(useFileForCycleWithPackageItem[3])==1)
																	{
																		File useFileForCycleWithPackageFile1 = new File(fileDir+useFileForCycleWithPackageItem[1]);
																		File useFileForCycleWithPackageFile2 = new File(fileDir+useFileForCycleWithPackageItem[2]);
																		
																		InputStreamReader useFileForCycleWithPackageFile1InputSteamReadTmp = new InputStreamReader(new FileInputStream(useFileForCycleWithPackageFile1));
																		InputStreamReader useFileForCycleWithPackageFile2InputSteamReadTmp = new InputStreamReader(new FileInputStream(useFileForCycleWithPackageFile2));
																		
																		BufferedReader useFileForCycleWithPackageFile1ExpFileBufferedReader = new BufferedReader(useFileForCycleWithPackageFile1InputSteamReadTmp);
																		BufferedReader useFileForCycleWithPackageFile2ExpFileBufferedReader = new BufferedReader(useFileForCycleWithPackageFile2InputSteamReadTmp);
																		
																		String useFileForCycleWithPackageLineText1 = null;
																		String useFileForCycleWithPackageLineText2 = null;
																		
																		int PackageNum=0;
																		while (((useFileForCycleWithPackageLineText1 = useFileForCycleWithPackageFile1ExpFileBufferedReader.readLine()) != null)&&((useFileForCycleWithPackageLineText2 = useFileForCycleWithPackageFile2ExpFileBufferedReader.readLine()) != null))
																		{							
																			PackageNum++;
																			if (lineTxtTmp[1].trim().split(":").length > 1)
																			{
																				host = Method.getFromBase64(lineTxtTmp[1].trim().split(":")[1]);																	
																			}
																			else
																			{
																				break;
																			}

																			
																			if (lineTxtTmp[2].trim().split(":").length > 1)
																			{
																				port = Integer.valueOf(Method.getFromBase64(lineTxtTmp[2].trim().split(":")[1]));
																			}
																			else
																			{
																				port=80;
																			}                       
																			
																			StepFileNameTmp = Method.getFromBase64(lineTxtTmp[3].trim().split(":")[1]);									
																			if(StepFileNameTmp.length()>0)
																			{
																				requestData = Method.bytesToString(Method.HexStringTobytes(Method.stringsToString(Method.readFileToHexStrings(fileDir+"\\"+StepFileNameTmp))));
																			}
																			
		                                      
																			if(previousURL.length()>0)
																			{
																				// 添加 phpcms/  到request 的 GET  phpcms/。。。。  http/1.1
																				requestData = Method.addPreviousURL(requestData,previousURL);
																			}
																			
																			String replaceHostPortString = lineTxtTmp[4].trim().split(":")[1].trim();
																			if (replaceHostPortString.equals("true"))
																			{
																				host = targetHost;
																				port = targetPort;
																				requestData = Method.replaceHostPort(requestData,host,port);
																			}
																			else
																			{
																			
																			}

				                                      
																			String useSetCookieFromPreviousString = lineTxtTmp[5].trim().split(":")[1].trim();
																			if (useSetCookieFromPreviousString.equals("true"))
																			{
																				/*
																				previousData = "HTTP/1.1 200 OK"+"\r\n"+
																									  "Date: Sun, 30 Nov 2014 09:16:33 GMT"+"\r\n"+
																									  "Server: Apache/2.2.9 (APMServ) PHP/5.2.6+"+"\r\n"+
																									  "X-Powered-By: PHP/5.2.6"+"\r\n"+
																									  "Set-Cookie: SYSZC=d2d4a91a4e41bea2078b09c29cd6a44a"+"\r\n"+
																									  "Set-Cookie: SYSUSER=admin"+"\r\n"+
																									  "Set-Cookie: SYSNAME=%E7%B3%BB%E7%BB%9F%E7%AE%A1%E7%90%86%E5%91%98"+"\r\n"+
																									  "Set-Cookie: SYSUSERID=3"+"\r\n"+
																									  "Set-Cookie: SYSTM=1417338994"+"\r\n"+
																									  "Content-Length: 2"+"\r\n"+
																									  "Content-Type: text/html"+"\r\n"+
																									  "\r\n"+							  
																									  "OK";
				                                        
				                                        						*/
																				String[] tmpmp = Method.getHTTPHeadersParams(previousData);
				                                		
																				String setCookieStr = "";
																				try
																				{
																				for(int n=0;n<tmpmp.length;n++)
																				{
																					if(n%2==0)
																					{
																						if(tmpmp[n].startsWith("Set-Cookie"))
																						{
																							setCookieStr =setCookieStr+tmpmp[n+1].trim()+";";
																						}
																					}
																				}
																				String OKrequestData="";
																				String[] requestDatas = requestData.split("\r\n");
																				boolean addCookie = true;
																				
																				for(int n=0;n<requestDatas.length;n++)
																				{		
																					requestDatas[n] = requestDatas[n]+"\r\n";
																					if(requestDatas[n].toLowerCase().trim().startsWith("cookie:"))
																					{
																						String[] tmpCookie = requestDatas[n].toLowerCase().trim().split("cookie:");
																						if(tmpCookie.length>1)
																						{
																							tmpCookie[1] = Method.replaceCookie(tmpCookie[1].trim(),setCookieStr);
																							requestDatas[n] = "Cookie: "+Method.stringsToString(tmpCookie)+"\r\n";
																							addCookie = false;
																						}
																					}
																				}
				                                    	  
																				if(addCookie)
																				{
																					OKrequestData =Method.addHeader(requestData,"Cookie: ",setCookieStr);
																				}
																				else
																				{
																					OKrequestData = Method.stringsToString(requestDatas);
																				}
																				requestData = OKrequestData; 
																				}
																				catch(Exception e5)
																				{
																					
																				}
				                                		
																				  
				                                        
																			}
																			else
																			{
																			
																			}
																			
				                                      	
																			int randomStringTmpLength = lineTxtTmp[6].trim().split(":").length;
																			if (randomStringTmpLength > 1)
																			{
																				String randomStringString = lineTxtTmp[6].trim().split(":")[1].trim();
																				String[] randomStringItemStrings = randomStringString.split(",");
																				int randomStringItemCount = randomStringItemStrings.length;
																				for (int k = 0; k < randomStringItemCount; k++)
																				{
																					String[] randomStringItem = new String[5];
																					randomStringItem[0] = Method.getFromBase64(randomStringItemStrings[k]).split(",")[0];
																					randomStringItem[1] = Method.getFromBase64(randomStringItemStrings[k]).split(",")[1];
																					randomStringItem[2] =Method.getFromBase64(randomStringItemStrings[k]).split(",")[2].substring(0, 1);
																					randomStringItem[4] = Method.getFromBase64(randomStringItemStrings[k]).split(",")[3];
												
																					if (Integer.valueOf(randomStringItem[1]).intValue() == 3)
																					{
																						randomStringItem[3] = randomStringItem[2].substring(1);
																						randomStringItem[2] = randomStringItem[2].substring(0, 1);
																					}																	
				                                        		
																					String strTmp = "rSI"+randomStringItem[0];
																					strTmp = Method.getBase64(strTmp);
																					strTmp = Method.addSignRegex(strTmp);
																					String randomStrTmp = "";
																					switch(Integer.valueOf(randomStringItem[1]))
																					{
																						case 0:randomStrTmp =new RandomString().getRandomString(Integer.valueOf(randomStringItem[2]),"i");requestData =requestData.replaceAll(strTmp, randomStrTmp);break;
																						case 1:randomStrTmp =new RandomString().getRandomString(Integer.valueOf(randomStringItem[2]),"l");requestData =requestData.replaceAll(strTmp, randomStrTmp);break;
																						case 2:randomStrTmp =new RandomString().getRandomString(Integer.valueOf(randomStringItem[2]),"il");requestData =requestData.replaceAll(strTmp, randomStrTmp);break;
																						case 3:randomStrTmp =new RandomString().getRandomStringFromDefineString(randomStringItem[3],Integer.valueOf(randomStringItem[2]));requestData =requestData.replaceAll(strTmp, randomStrTmp);break;																		
																					}	                                        		
																					randomStringList.add(randomStringItem);
																				}
																				
																			}
															
																			
																			int captchaTmpLength = lineTxtTmp[7].trim().split(":").length;
																			if (captchaTmpLength > 1)
																			{
																				String captchaString = lineTxtTmp[7].trim().split(":")[1].trim();
																				String[] captchaItemStrings = captchaString.split(",");
																				int captchaItemCount = captchaItemStrings.length;
																				for (int k = 0; k < captchaItemCount; k++)
																				{
																					String[] captchaItem = new String[4];
																					captchaItem = Method.getFromBase64(captchaItemStrings[k]).split(",");
																					captchaList.add(captchaItem);
																					
																					String captchaStrTmp = "sPFC"+captchaItem[0];
																					captchaStrTmp = Method.getBase64(captchaStrTmp);
																					captchaStrTmp = Method.addSignRegex(captchaStrTmp);
				                                            	                                            
																					boolean status = UUAPI.checkAPI();
																					if (!status)
																					{
																						JOptionPane.showMessageDialog(null, "API文件校验失败，无法使用打码服务", "Warning", 2);
																					}
																					else
																					{
																						final String ImgURL = "http://"+host+previousURL+captchaItem[1];  //获取验证码的url  
																				//		System.out.println(ImgURL);                                           
																						final int CaptchaNumbersTextInt = Integer.valueOf(captchaItem[2]);
																						final String saveFileName = "img/tmp.jpg";
																						
																						String saveAbsolutePath = Method.getImgFromURL(ImgURL,saveFileName);
																						if (saveAbsolutePath != null)
																						{
																							if (CaptchaNumbersTextInt != 0)
																							{
																								try 
																								{
																									String[] captchaResult = UUAPI.easyDecaptcha(saveFileName, 1000 + CaptchaNumbersTextInt);
																									if (captchaResult[1].length() > 0)
																									{
																										requestData = requestData.replaceAll(captchaStrTmp, captchaResult[1]);   
																									}
																								} 
																								catch (IOException eee) 
																								{
																									eee.printStackTrace();
																								}	                                                    
																							}		                                      					
																						}																				
																					}		                                            
																				}
																			}
				                                                                             	
				                                        
																			//useFileForCycleWithPackageLineText
																			String useFileForCycleWithPackageLineStrTmp1 = "File-1";
																			useFileForCycleWithPackageLineStrTmp1 = Method.getBase64(useFileForCycleWithPackageLineStrTmp1);
																			useFileForCycleWithPackageLineStrTmp1 = Method.addSignRegex(useFileForCycleWithPackageLineStrTmp1);
																			requestData = requestData.replaceAll(useFileForCycleWithPackageLineStrTmp1, useFileForCycleWithPackageLineText1);
																			
																			
																			String useFileForCycleWithPackageLineStrTmp2 = "File-2";
																			useFileForCycleWithPackageLineStrTmp2 = Method.getBase64(useFileForCycleWithPackageLineStrTmp2);
																			useFileForCycleWithPackageLineStrTmp2 = Method.addSignRegex(useFileForCycleWithPackageLineStrTmp2);
																			requestData = requestData.replaceAll(useFileForCycleWithPackageLineStrTmp2, useFileForCycleWithPackageLineText2);
				                                      		
				                                      		
																			// 发送http 数据
																			
																			responseData = Method.bytesToString(Method.getHttPResponseData(host,port,requestData));
																		//	System.out.println(requestData);
																		//	System.out.println("--");
																		//	System.out.println(responseData);
																			responseStatus = Method.getHttpResponseHeaderStatus(responseData);
																															
				                                      		
																			int setGoAheadOrStopFromStatusTmpLength = lineTxtTmp[9].trim().split(":").length;
																			if (setGoAheadOrStopFromStatusTmpLength > 1)
																			{
																				String setGoAheadOrStopFromStatusString = lineTxtTmp[9].trim().split(":")[1].trim();
																				String[] setGoAheadOrStopFromStatusItemStrings = setGoAheadOrStopFromStatusString.split(",");
																				int setGoAheadOrStopFromStatusItemCount = setGoAheadOrStopFromStatusItemStrings.length;
																				for (int k = 0; k < setGoAheadOrStopFromStatusItemCount; k++)
																				{
																					String[] setGoAheadOrStopFromStatusItem = new String[2];
																					setGoAheadOrStopFromStatusItem = Method.getFromBase64(setGoAheadOrStopFromStatusItemStrings[k]).split(",");
																					setGoAheadOrStopFromStatusList.add(setGoAheadOrStopFromStatusItem);													
																				}
																			}
				                                      		
																			int setGoAheadOrStopFromStatusListCount = setGoAheadOrStopFromStatusList.size();
																			if (setGoAheadOrStopFromStatusListCount > 0)
																			{
																				for (int j = 0; j < setGoAheadOrStopFromStatusListCount; j++)
																				{
																					String[] setGoAheadOrStopFromStatusListItem = (String[])setGoAheadOrStopFromStatusList.get(j);
																					
																					if (responseStatus == Integer.valueOf(setGoAheadOrStopFromStatusListItem[0]))
																					{
																						String actionForStatus = setGoAheadOrStopFromStatusListItem[1];
				                                            					                                            			
																						//                         GO ahead current   stop current 
																						///                                               ||
																						//																		
																						///											Go Exp    Stop Exp
																						///																		
																						
																						
																						if(actionForStatus.equals("Go Ahead EXP"))
																						{
																							GoAheadExp  = true;
																							whyToStop = "responseStatus; " + responseStatus + " -- Rule; " + setGoAheadOrStopFromStatusListItem[0] + " --Action; "+ setGoAheadOrStopFromStatusListItem[1];
																							break;
																						}
																						else if(actionForStatus.equals("Stop EXP"))
																						{
																							StopExp = true;
				                                            				
																							whyToStop = "responseStatus; " + responseStatus + " -- Rule; " + setGoAheadOrStopFromStatusListItem[0] + " --Action; "+ setGoAheadOrStopFromStatusListItem[1];
																							break;
																						}
																						else if(actionForStatus.equals("Go Ahead Current"))
																						{
																							GoAheadCurrent = true;
																							whyToStop = "responseStatus; " + responseStatus + " -- Rule; " + setGoAheadOrStopFromStatusListItem[0]+ " --Action; " + setGoAheadOrStopFromStatusListItem[1];
																							break;
																						}
																						else if(actionForStatus.equals("Stop Current"))
																						{
																							//StopCurrent = true;
																							//whyToStop = "responseStatus:" + responseStatus + " -- Rule" + setGoAheadOrStopFromStatusListItem[0] + setGoAheadOrStopFromStatusListItem[1];
																							//System.out.println(whyToStop);
																							//break;
																						}                                      			
																						
																					}
																				}
																			}
																			else
																			{
																				//System.out.println("mei you setGoAheadOrStopFromStatusListCount");
																			}

																			if ((StopExp) )
																			{
																				JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,"- -",useFileForCycleWithPackageLineText1+"-|-"+useFileForCycleWithPackageLineText2,whyToStop,"show"});
																				ExpPackageHistoryItem[0] = StepFileNameTmp;
																				ExpPackageHistoryItem[1] = requestData;
																				ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																				previousData = responseData;
																				ExpPackageHistoryItem[3] = responseData;
																				ExpPackageHistoryItem[4] = whyToStop;
																				ExpPackageHistoryList.add(ExpPackageHistoryItem);
																				expHTTPHistoryList.add(ExpPackageHistoryItem);
																				ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																				ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																				ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																				ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																				ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																				ExpPackageHistoryItem = new String[7];
																				responseStatus = 0;
																				responseData = "";
																				whyToStop = "Run Over";
																				break;
																			}
				                                            
																			if(GoAheadCurrent)
																			{
																				JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,"- -",useFileForCycleWithPackageLineText1+"-|-"+useFileForCycleWithPackageLineText2,whyToStop,"show"});
																				ExpPackageHistoryItem[0] = StepFileNameTmp;
																				ExpPackageHistoryItem[1] = requestData;
																				ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																				previousData = responseData;
																				ExpPackageHistoryItem[3] = responseData;
																				ExpPackageHistoryItem[4] = whyToStop;
																				ExpPackageHistoryList.add(ExpPackageHistoryItem);
																				expHTTPHistoryList.add(ExpPackageHistoryItem);
																				ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																				ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																				ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																				ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																				ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																				ExpPackageHistoryItem = new String[7];
																				responseStatus = 0;
																				responseData = "";
																				whyToStop = "Run Over";
																				GoAheadCurrent = false;
																				break;
																			}
				                                            
																			if(GoAheadExp)
																			{
																				JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,"- -",useFileForCycleWithPackageLineText1+"-|-"+useFileForCycleWithPackageLineText2,whyToStop,"show"});
																				ExpPackageHistoryItem[0] = StepFileNameTmp;
																				ExpPackageHistoryItem[1] = requestData;
																				ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																				previousData = responseData;
																				ExpPackageHistoryItem[3] = responseData;
																				ExpPackageHistoryItem[4] = whyToStop;
																				ExpPackageHistoryList.add(ExpPackageHistoryItem);
																				expHTTPHistoryList.add(ExpPackageHistoryItem);
																				ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																				ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																				ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																				ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																				ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																				ExpPackageHistoryItem = new String[7];
																				responseStatus = 0;
																				responseData = "";
																				whyToStop = "Run Over";
																				break;
																			}
				                                            
																		
																			
																			int setGoAheadOrStopFromDataTmpLength = lineTxtTmp[10].trim().split(":").length;
																			if (setGoAheadOrStopFromDataTmpLength > 1)
																			{
																				String setGoAheadOrStopFromDataString = lineTxtTmp[10].trim().split(":")[1].trim();
																				String[] setGoAheadOrStopFromDataItemStrings = setGoAheadOrStopFromDataString.split(",");
																				int setGoAheadOrStopFromDataItemCount = setGoAheadOrStopFromDataItemStrings.length;
																				for (int k = 0; k < setGoAheadOrStopFromDataItemCount; k++)
																				{
																					String[] setGoAheadOrStopFromDataItem = new String[2];
																					setGoAheadOrStopFromDataItem = Method.getFromBase64(setGoAheadOrStopFromDataItemStrings[k]).split(",");
																					setGoAheadOrStopFromDataList.add(setGoAheadOrStopFromDataItem);
																				}
																			}
																			                                           
																			
																			int j;
																			int setGoAheadOrStopFromDataListCount = setGoAheadOrStopFromDataList.size();
																			if (setGoAheadOrStopFromDataListCount > 0)
																			{
																				for (j = 0; j < setGoAheadOrStopFromDataListCount; j++)
																				{
																					String[] setGoAheadOrStopFromDataListItem = (String[])setGoAheadOrStopFromDataList.get(j);

																					Pattern p=Pattern.compile(Method.getFromBase64(setGoAheadOrStopFromDataListItem[0]));
																					Matcher m=p.matcher(responseData);
																				//	System.out.println(m.find());
																					
																					if (m.find())
																					{
																						String actionForData = setGoAheadOrStopFromDataListItem[1];
																						
																						if(actionForData.equals("Go Ahead EXP"))
																						{
																							GoAheadExp  = true;
																							whyToStop = " -- Rule; " + Method.getFromBase64(setGoAheadOrStopFromDataListItem[0]) +"  --result; "+m.group()+ " --Action; "+ setGoAheadOrStopFromDataListItem[1];
																							break;
																						}
																						else if(actionForData.equals("Stop EXP"))
																						{
																							StopExp = true;
																							whyToStop = " -- Rule; " + Method.getFromBase64(setGoAheadOrStopFromDataListItem[0]) +"  --result; "+m.group()+ " --Action; "+ setGoAheadOrStopFromDataListItem[1];
																							break;
																						}
																						else if(actionForData.equals("Go Ahead Current"))
																						{
																							GoAheadCurrent = true;
																							whyToStop = " -- Rule; " + Method.getFromBase64(setGoAheadOrStopFromDataListItem[0]) +"  --result; "+m.group()+ " --Action; "+ setGoAheadOrStopFromDataListItem[1];
																							break;
																						}
																						else if(actionForData.equals("Stop Current"))
																						{
																							//StopCurrent = true;
																							//whyToStop = "responseStatus:" + responseStatus + " -- Rule" + setGoAheadOrStopFromStatusListItem[0] + setGoAheadOrStopFromStatusListItem[1];
																							//System.out.println(whyToStop);
																							//break;
																						} 
																						
																					}
																				}
																			}
																			else
																			{
																				//System.out.println("meiyou setGoAheadOrStopFromDataList");
																			}

																			if ((StopExp) )
																			{
																				JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,"- -",useFileForCycleWithPackageLineText1+"-|-"+useFileForCycleWithPackageLineText2,whyToStop,"show"});
																				ExpPackageHistoryItem[0] = StepFileNameTmp;
																				ExpPackageHistoryItem[1] = requestData;
																				ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																				previousData = responseData;
																				ExpPackageHistoryItem[3] = responseData;
																				ExpPackageHistoryItem[4] = whyToStop;
																				ExpPackageHistoryList.add(ExpPackageHistoryItem);
																				expHTTPHistoryList.add(ExpPackageHistoryItem);
																				ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																				ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																				ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																				ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																				ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																				ExpPackageHistoryItem = new String[7];
																				responseStatus = 0;
																				responseData = "";
																				whyToStop = "Run Over";
																				break;
																			}
				                                            
																			if(GoAheadCurrent)
																			{
																				JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,"- -",useFileForCycleWithPackageLineText1+"-|-"+useFileForCycleWithPackageLineText2,whyToStop,"show"});
																				ExpPackageHistoryItem[0] = StepFileNameTmp;
																				ExpPackageHistoryItem[1] = requestData;
																				ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																				previousData = responseData;
																				ExpPackageHistoryItem[3] = responseData;
																				ExpPackageHistoryItem[4] = whyToStop;
																				ExpPackageHistoryList.add(ExpPackageHistoryItem);
																				expHTTPHistoryList.add(ExpPackageHistoryItem);
																				ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																				ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																				ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																				ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																				ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																				ExpPackageHistoryItem = new String[7];
																				responseStatus = 0;
																				responseData = "";
																				whyToStop = "Run Over";
																				GoAheadCurrent = false;
																				break;
																			}
				                                            
																			if(GoAheadExp)
																			{
																				JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,"- -",useFileForCycleWithPackageLineText1+"-|-"+useFileForCycleWithPackageLineText2,whyToStop,"show"});
																				ExpPackageHistoryItem[0] = StepFileNameTmp;
																				ExpPackageHistoryItem[1] = requestData;
																				ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																				previousData = responseData;
																				ExpPackageHistoryItem[3] = responseData;
																				ExpPackageHistoryItem[4] = whyToStop;
																				ExpPackageHistoryList.add(ExpPackageHistoryItem);
																				expHTTPHistoryList.add(ExpPackageHistoryItem);
																				ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																				ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																				ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																				ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																				ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																				ExpPackageHistoryItem = new String[7];
																				responseStatus = 0;
																				responseData = "";
																				whyToStop = "Run Over";
																				break;
																			}

																			JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,"- -",useFileForCycleWithPackageLineText1+"-|-"+useFileForCycleWithPackageLineText2,whyToStop,"show"});
																			ExpPackageHistoryItem[0] = StepFileNameTmp;
																			ExpPackageHistoryItem[1] = requestData;
																			ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																			previousData = responseData;
																			ExpPackageHistoryItem[3] = responseData;
																			ExpPackageHistoryItem[4] = whyToStop;
																			ExpPackageHistoryList.add(ExpPackageHistoryItem);
																			expHTTPHistoryList.add(ExpPackageHistoryItem);
																			ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																			ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																			ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																			ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																			ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																			ExpPackageHistoryItem = new String[7];
																			responseStatus = 0;
																			responseData = "";
																			whyToStop = "Run Over";
				                                      		
																		}
																	}
																	//组合方式二  一对多
																	else if(Integer.valueOf(useFileForCycleWithPackageItem[3])==2)
																	{
																		
																		File useFileForCycleWithPackageFile1 = new File(fileDir+useFileForCycleWithPackageItem[1]);
																		
																		InputStreamReader useFileForCycleWithPackageFile1InputSteamReadTmp = new InputStreamReader(new FileInputStream(useFileForCycleWithPackageFile1));					
																		
																		BufferedReader useFileForCycleWithPackageFile1ExpFileBufferedReader = new BufferedReader(useFileForCycleWithPackageFile1InputSteamReadTmp);
																		
																		String useFileForCycleWithPackageLineText1 = null;
																							
																		int PackageNum=0;
																		while (((useFileForCycleWithPackageLineText1 = useFileForCycleWithPackageFile1ExpFileBufferedReader.readLine()) != null))
																		{
																			File useFileForCycleWithPackageFile2 = new File(fileDir+useFileForCycleWithPackageItem[2]);
																			InputStreamReader useFileForCycleWithPackageFile2InputSteamReadTmp = new InputStreamReader(new FileInputStream(useFileForCycleWithPackageFile2));
																			BufferedReader useFileForCycleWithPackageFile2ExpFileBufferedReader = new BufferedReader(useFileForCycleWithPackageFile2InputSteamReadTmp);
																			String useFileForCycleWithPackageLineText2 = null;
																			while(((useFileForCycleWithPackageLineText2 = useFileForCycleWithPackageFile2ExpFileBufferedReader.readLine()) != null))
																			{
																		
																				PackageNum++;
																				if (lineTxtTmp[1].trim().split(":").length > 1)
																				{
																					host = Method.getFromBase64(lineTxtTmp[1].trim().split(":")[1]);																		
																				}
																				else
																				{
																					break;
																				}

																				
																				if (lineTxtTmp[2].trim().split(":").length > 1)
																				{
																					port = Integer.valueOf(Method.getFromBase64(lineTxtTmp[2].trim().split(":")[1]));
																				}
																				else
																				{
																					port=80;
																				}
																				
																				StepFileNameTmp = Method.getFromBase64(lineTxtTmp[3].trim().split(":")[1]);									
																				if(StepFileNameTmp.length()>0)
																				{
																					requestData = Method.bytesToString(Method.HexStringTobytes(Method.stringsToString(Method.readFileToHexStrings(fileDir+"\\"+StepFileNameTmp))));
																				}																	
					                                      
																				
																				if(previousURL.length()>0)
																				{
																					// 添加 phpcms/  到request 的 GET  phpcms/。。。。  http/1.1
																					requestData = Method.addPreviousURL(requestData,previousURL);
																				}
																				
																				String replaceHostPortString = lineTxtTmp[4].trim().split(":")[1].trim();
																				if (replaceHostPortString.equals("true"))
																				{
																					host = targetHost;
																					port = targetPort;
																					requestData = Method.replaceHostPort(requestData,host,port);
																				}
																				else
																				{
																				}
																				
					                                      
																				String useSetCookieFromPreviousString = lineTxtTmp[5].trim().split(":")[1].trim();
																				if (useSetCookieFromPreviousString.equals("true"))
																				{
																					/*
																					previousData = "HTTP/1.1 200 OK"+"\r\n"+
																										  "Date: Sun, 30 Nov 2014 09:16:33 GMT"+"\r\n"+
																										  "Server: Apache/2.2.9 (APMServ) PHP/5.2.6+"+"\r\n"+
																										  "X-Powered-By: PHP/5.2.6"+"\r\n"+
																										  "Set-Cookie: SYSZC=d2d4a91a4e41bea2078b09c29cd6a44a"+"\r\n"+
																										  "Set-Cookie: SYSUSER=admin"+"\r\n"+
																										  "Set-Cookie: SYSNAME=%E7%B3%BB%E7%BB%9F%E7%AE%A1%E7%90%86%E5%91%98"+"\r\n"+
																										  "Set-Cookie: SYSUSERID=3"+"\r\n"+
																										  "Set-Cookie: SYSTM=1417338994"+"\r\n"+
																										  "Content-Length: 2"+"\r\n"+
																										  "Content-Type: text/html"+"\r\n"+
																										  "\r\n"+							  
																										  "OK";
					                                        
					                                        						*/
																					
																					String[] tmpmp = Method.getHTTPHeadersParams(previousData);                                		
																					String setCookieStr = "";
																					try
																					{
																					for(int n=0;n<tmpmp.length;n++)
																					{
																						if(n%2==0)
																						{
																							if(tmpmp[n].startsWith("Set-Cookie"))
																							{
																								setCookieStr =setCookieStr+tmpmp[n+1].trim()+";";
																							}
																						}
																					}
																					String OKrequestData="";
																					String[] requestDatas = requestData.split("\r\n");
																					boolean addCookie = true;
																					for(int n=0;n<requestDatas.length;n++)
																					{		
																						requestDatas[n] = requestDatas[n]+"\r\n";
																						if(requestDatas[n].toLowerCase().trim().startsWith("cookie:"))
																						{
																							String[] tmpCookie = requestDatas[n].toLowerCase().trim().split("cookie:");
																							if(tmpCookie.length>1)
																							{
																								tmpCookie[1] = Method.replaceCookie(tmpCookie[1].trim(),setCookieStr);
																								requestDatas[n] = "Cookie: "+Method.stringsToString(tmpCookie)+"\r\n";
																								addCookie = false;
																							}
																						}
																					}
					                                    	  
																					if(addCookie)
																					{
																						OKrequestData =Method.addHeader(requestData,"Cookie: ",setCookieStr);
																					}
																					else
																					{
																						OKrequestData = Method.stringsToString(requestDatas);
																					}
																					requestData = OKrequestData; 
																					}
																					catch(Exception e5)
																					{}
					                                		
																					  
					                                        
																				}
																				else
																				{
																				}
																		
					                                      	
																				int randomStringTmpLength = lineTxtTmp[6].trim().split(":").length;
																				if (randomStringTmpLength > 1)
																				{
																					String randomStringString = lineTxtTmp[6].trim().split(":")[1].trim();
																					String[] randomStringItemStrings = randomStringString.split(",");
																					int randomStringItemCount = randomStringItemStrings.length;
																					
																					for (int k = 0; k < randomStringItemCount; k++)
																					{
																						String[] randomStringItem = new String[5];
																						randomStringItem[0] = Method.getFromBase64(randomStringItemStrings[k]).split(",")[0];
																						randomStringItem[1] = Method.getFromBase64(randomStringItemStrings[k]).split(",")[1];
																						randomStringItem[2] =Method.getFromBase64(randomStringItemStrings[k]).split(",")[2].substring(0, 1);
																						randomStringItem[4] = Method.getFromBase64(randomStringItemStrings[k]).split(",")[3];
																					
																						if (Integer.valueOf(randomStringItem[1]).intValue() == 3)
																						{
																							randomStringItem[3] = randomStringItem[2].substring(1);
																							randomStringItem[2] = randomStringItem[2].substring(0, 1);
																						}																			
					                                        		
																						String strTmp = "rSI"+randomStringItem[0];
																						strTmp = Method.getBase64(strTmp);
																						strTmp = Method.addSignRegex(strTmp);
																						String randomStrTmp = "";
																						switch(Integer.valueOf(randomStringItem[1]))
																						{
																							case 0:randomStrTmp =new RandomString().getRandomString(Integer.valueOf(randomStringItem[2]),"i");requestData =requestData.replaceAll(strTmp, randomStrTmp);break;
																							case 1:randomStrTmp =new RandomString().getRandomString(Integer.valueOf(randomStringItem[2]),"l");requestData =requestData.replaceAll(strTmp, randomStrTmp);break;
																							case 2:randomStrTmp =new RandomString().getRandomString(Integer.valueOf(randomStringItem[2]),"il");requestData =requestData.replaceAll(strTmp, randomStrTmp);break;
																							case 3:randomStrTmp =new RandomString().getRandomStringFromDefineString(randomStringItem[3],Integer.valueOf(randomStringItem[2]));requestData =requestData.replaceAll(strTmp, randomStrTmp);break;																																																		
																						}
					                                        		
																						randomStringList.add(randomStringItem);
																						
																					}
																					
																				}
					                                      	
																			
																				int captchaTmpLength = lineTxtTmp[7].trim().split(":").length;
																				if (captchaTmpLength > 1)
																				{
																					String captchaString = lineTxtTmp[7].trim().split(":")[1].trim();
																					String[] captchaItemStrings = captchaString.split(",");
																					int captchaItemCount = captchaItemStrings.length;
																					for (int k = 0; k < captchaItemCount; k++)
																					{
																						String[] captchaItem = new String[4];
																						captchaItem = Method.getFromBase64(captchaItemStrings[k]).split(",");
																						captchaList.add(captchaItem);
																						
																						String captchaStrTmp = "sPFC"+captchaItem[0];
																						captchaStrTmp = Method.getBase64(captchaStrTmp);
																						captchaStrTmp = Method.addSignRegex(captchaStrTmp);
					                                            			                                            
																						boolean status = UUAPI.checkAPI();

																						if (!status)
																						{
																							JOptionPane.showMessageDialog(null, "API文件校验失败，无法使用打码服务", "Warning", 2);
																						}
																						else
																						{
																							final String ImgURL = "http://"+host+previousURL+captchaItem[1];  //获取验证码的url  
																					//		System.out.println(ImgURL);                                       
																							final int CaptchaNumbersTextInt = Integer.valueOf(captchaItem[2]);
																							final String saveFileName = "img/tmp.jpg";
																							
																							String saveAbsolutePath = Method.getImgFromURL(ImgURL,saveFileName);
																							if (saveAbsolutePath != null)
																							{
																								if (CaptchaNumbersTextInt != 0)
																								{
																									try 
																									{
																										String[] captchaResult = UUAPI.easyDecaptcha(saveFileName, 1000 + CaptchaNumbersTextInt);
																										if (captchaResult[1].length() > 0)
																										{
																											requestData = requestData.replaceAll(captchaStrTmp, captchaResult[1]);   
																										}
																									} 
																									catch (IOException eee) 
																									{
																										eee.printStackTrace();
																									}	                                                    
																								}		                                      					
																							}																				
																						}		                                            
																					}
																				}
					                                        			                                      
					                                        
																				//useFileForCycleWithPackageLineText
																				String useFileForCycleWithPackageLineStrTmp1 = "File-1";
																				useFileForCycleWithPackageLineStrTmp1 = Method.getBase64(useFileForCycleWithPackageLineStrTmp1);
																				useFileForCycleWithPackageLineStrTmp1 = Method.addSignRegex(useFileForCycleWithPackageLineStrTmp1);
																				requestData = requestData.replaceAll(useFileForCycleWithPackageLineStrTmp1, useFileForCycleWithPackageLineText1);
																																					
																				String useFileForCycleWithPackageLineStrTmp2 = "File-2";
																				useFileForCycleWithPackageLineStrTmp2 = Method.getBase64(useFileForCycleWithPackageLineStrTmp2);
																				useFileForCycleWithPackageLineStrTmp2 = Method.addSignRegex(useFileForCycleWithPackageLineStrTmp2);
																				requestData = requestData.replaceAll(useFileForCycleWithPackageLineStrTmp2, useFileForCycleWithPackageLineText2);
					                                      																
					                                      		
																				// 发送http 数据
																				
																				responseData = Method.bytesToString(Method.getHttPResponseData(host,port,requestData));
																			//	System.out.println(requestData);
																				//System.out.println("--");
																			//	System.out.println(responseData);
																				responseStatus = Method.getHttpResponseHeaderStatus(responseData);
																				
																				
																				int setGoAheadOrStopFromStatusTmpLength = lineTxtTmp[9].trim().split(":").length;
																				if (setGoAheadOrStopFromStatusTmpLength > 1)
																				{
																					String setGoAheadOrStopFromStatusString = lineTxtTmp[9].trim().split(":")[1].trim();
																					String[] setGoAheadOrStopFromStatusItemStrings = setGoAheadOrStopFromStatusString.split(",");
																					int setGoAheadOrStopFromStatusItemCount = setGoAheadOrStopFromStatusItemStrings.length;
																					for (int k = 0; k < setGoAheadOrStopFromStatusItemCount; k++)
																					{
																						String[] setGoAheadOrStopFromStatusItem = new String[2];
																						setGoAheadOrStopFromStatusItem = Method.getFromBase64(setGoAheadOrStopFromStatusItemStrings[k]).split(",");
																						setGoAheadOrStopFromStatusList.add(setGoAheadOrStopFromStatusItem);															
																					}
																				}		                                      	
					                                      		
																				int setGoAheadOrStopFromStatusListCount = setGoAheadOrStopFromStatusList.size();
																				if (setGoAheadOrStopFromStatusListCount > 0)
																				{														
																					for (int j = 0; j < setGoAheadOrStopFromStatusListCount; j++)
																					{
																						String[] setGoAheadOrStopFromStatusListItem = (String[])setGoAheadOrStopFromStatusList.get(j);
																				
																						if (responseStatus == Integer.valueOf(setGoAheadOrStopFromStatusListItem[0]))
																						{
																							String actionForStatus = setGoAheadOrStopFromStatusListItem[1];
					                                            			
																							//                         GO ahead current   stop current 
																							///                                               ||
																							//																		
																							///											Go Exp    Stop Exp
																							///
																						
																							if(actionForStatus.equals("Go Ahead EXP"))
																							{
																								GoAheadExp  = true;
																								whyToStop = "responseStatus; " + responseStatus + " -- Rule; " + setGoAheadOrStopFromStatusListItem[0] + " --Action; "+ setGoAheadOrStopFromStatusListItem[1];
																								break;
																							}
																							else if(actionForStatus.equals("Stop EXP"))
																							{
																								StopExp = true;
																								whyToStop = "responseStatus; " + responseStatus + " -- Rule; " + setGoAheadOrStopFromStatusListItem[0] + " --Action; "+ setGoAheadOrStopFromStatusListItem[1];
																								break;
																							}
																							else if(actionForStatus.equals("Go Ahead Current"))
																							{
																								GoAheadCurrent = true;
																								whyToStop = "responseStatus; " + responseStatus + " -- Rule; " + setGoAheadOrStopFromStatusListItem[0]+ " --Action; " + setGoAheadOrStopFromStatusListItem[1];
																								break;
																							}
																							else if(actionForStatus.equals("Stop Current"))
																							{
																								//StopCurrent = true;
																								//whyToStop = "responseStatus:" + responseStatus + " -- Rule" + setGoAheadOrStopFromStatusListItem[0] + setGoAheadOrStopFromStatusListItem[1];
																								//System.out.println(whyToStop);
																								//break;
																							}                                      			
																						}
																					}
																				}
																				else
																				{
																					//System.out.println("mei you setGoAheadOrStopFromStatusListCount");
																				}

																				if ((StopExp) )
																				{
																					JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,"- -",useFileForCycleWithPackageLineText1+"-|-"+useFileForCycleWithPackageLineText2,whyToStop,"show"});
																					ExpPackageHistoryItem[0] = StepFileNameTmp;
																					ExpPackageHistoryItem[1] = requestData;
																					ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																					previousData = responseData;
																					ExpPackageHistoryItem[3] = responseData;
																					ExpPackageHistoryItem[4] = whyToStop;
																					ExpPackageHistoryList.add(ExpPackageHistoryItem);
																					expHTTPHistoryList.add(ExpPackageHistoryItem);
																					ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																					ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																					ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																					ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																					ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																					ExpPackageHistoryItem = new String[7];
																					responseStatus = 0;
																					responseData = "";
																					whyToStop = "Run Over";
						                                      		
																					break;
																				}
					                                            
																				if(GoAheadCurrent)
																				{
																					JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,"- -",useFileForCycleWithPackageLineText1+"-|-"+useFileForCycleWithPackageLineText2,whyToStop,"show"});
																					ExpPackageHistoryItem[0] = StepFileNameTmp;
																					ExpPackageHistoryItem[1] = requestData;
																					ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																					previousData = responseData;
																					ExpPackageHistoryItem[3] = responseData;
																					ExpPackageHistoryItem[4] = whyToStop;
																					ExpPackageHistoryList.add(ExpPackageHistoryItem);
																					expHTTPHistoryList.add(ExpPackageHistoryItem);
																					ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																					ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																					ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																					ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																					ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																					ExpPackageHistoryItem = new String[7];
																					responseStatus = 0;
																					responseData = "";
																					whyToStop = "Run Over";
																					GoAheadCurrent = false;
																					break;
																				}
					                                            
																				if(GoAheadExp)
																				{
																					JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,"- -",useFileForCycleWithPackageLineText1+"-|-"+useFileForCycleWithPackageLineText2,whyToStop,"show"});
																					ExpPackageHistoryItem[0] = StepFileNameTmp;
																					ExpPackageHistoryItem[1] = requestData;
																					ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																					previousData = responseData;
																					ExpPackageHistoryItem[3] = responseData;
																					ExpPackageHistoryItem[4] = whyToStop;
																					ExpPackageHistoryList.add(ExpPackageHistoryItem);
																					expHTTPHistoryList.add(ExpPackageHistoryItem);
																					ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																					ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																					ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																					ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																					ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																					ExpPackageHistoryItem = new String[7];
																					responseStatus = 0;
																					responseData = "";
																					whyToStop = "Run Over";
																					break;
																				}
															
																				
																				int setGoAheadOrStopFromDataTmpLength = lineTxtTmp[10].trim().split(":").length;
																				if (setGoAheadOrStopFromDataTmpLength > 1)
																				{
																					String setGoAheadOrStopFromDataString = lineTxtTmp[10].trim().split(":")[1].trim();
																					String[] setGoAheadOrStopFromDataItemStrings = setGoAheadOrStopFromDataString.split(",");
																					int setGoAheadOrStopFromDataItemCount = setGoAheadOrStopFromDataItemStrings.length;
																					for (int k = 0; k < setGoAheadOrStopFromDataItemCount; k++)
																					{
																						String[] setGoAheadOrStopFromDataItem = new String[2];
																						setGoAheadOrStopFromDataItem = Method.getFromBase64(setGoAheadOrStopFromDataItemStrings[k]).split(",");
																						setGoAheadOrStopFromDataList.add(setGoAheadOrStopFromDataItem);																
																					}
																				}	                                            
																				
																				int j;
																				int setGoAheadOrStopFromDataListCount = setGoAheadOrStopFromDataList.size();
																				if (setGoAheadOrStopFromDataListCount > 0)
																				{
																					for (j = 0; j < setGoAheadOrStopFromDataListCount; j++)
																					{
																						String[] setGoAheadOrStopFromDataListItem = (String[])setGoAheadOrStopFromDataList.get(j);

																						Pattern p=Pattern.compile(Method.getFromBase64(setGoAheadOrStopFromDataListItem[0]));
																						Matcher m=p.matcher(responseData);
																						//System.out.println(m.find());
																						
																						if (m.find())
																						{
																							String actionForData = setGoAheadOrStopFromDataListItem[1];
																							
																							if(actionForData.equals("Go Ahead EXP"))
																							{
																								GoAheadExp  = true;
																								whyToStop = " -- Rule; " + Method.getFromBase64(setGoAheadOrStopFromDataListItem[0]) +"  --result; "+m.group()+ " --Action; "+  setGoAheadOrStopFromDataListItem[1];
																								break;
																							}
																							else if(actionForData.equals("Stop EXP"))
																							{
																								StopExp = true;
																								whyToStop = " -- Rule; " + Method.getFromBase64(setGoAheadOrStopFromDataListItem[0]) +"  --result; "+m.group()+ " --Action; "+  setGoAheadOrStopFromDataListItem[1];
																								break;
																							}
																							else if(actionForData.equals("Go Ahead Current"))
																							{
																								GoAheadCurrent = true;
																								whyToStop = " -- Rule; " + Method.getFromBase64(setGoAheadOrStopFromDataListItem[0]) +"  --result; "+m.group()+ " --Action; "+  setGoAheadOrStopFromDataListItem[1];
																								break;
																							}
																							else if(actionForData.equals("Stop Current"))
																							{
																								//StopCurrent = true;
																								//whyToStop = "responseStatus:" + responseStatus + " -- Rule" + setGoAheadOrStopFromStatusListItem[0] + setGoAheadOrStopFromStatusListItem[1];
																								//System.out.println(whyToStop);
																								//break;
																							} 
																							
																						}
																					}
																				}
																				else
																				{
																					//System.out.println("meiyou setGoAheadOrStopFromDataList");
																				}

																				if ((StopExp) )
																				{
																					JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,"- -",useFileForCycleWithPackageLineText1+"-|-"+useFileForCycleWithPackageLineText2,whyToStop,"show"});
																					ExpPackageHistoryItem[0] = StepFileNameTmp;
																					ExpPackageHistoryItem[1] = requestData;
																					ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																					previousData = responseData;
																					ExpPackageHistoryItem[3] = responseData;
																					ExpPackageHistoryItem[4] = whyToStop;
																					ExpPackageHistoryList.add(ExpPackageHistoryItem);
																					expHTTPHistoryList.add(ExpPackageHistoryItem);
																					ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																					ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																					ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																					ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																					ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																					ExpPackageHistoryItem = new String[7];
																					responseStatus = 0;
																					responseData = "";
																					whyToStop = "Run Over";
																					break;
																				}
					                                            
																				if(GoAheadCurrent)
																				{
																					JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,"- -",useFileForCycleWithPackageLineText1+"-|-"+useFileForCycleWithPackageLineText2,whyToStop,"show"});
																					ExpPackageHistoryItem[0] = StepFileNameTmp;
																					ExpPackageHistoryItem[1] = requestData;
																					ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																					previousData = responseData;
																					ExpPackageHistoryItem[3] = responseData;
																					ExpPackageHistoryItem[4] = whyToStop;
																					ExpPackageHistoryList.add(ExpPackageHistoryItem);
																					expHTTPHistoryList.add(ExpPackageHistoryItem);
																					ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																					ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																					ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																					ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																					ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																					ExpPackageHistoryItem = new String[7];
																					responseStatus = 0;
																					responseData = "";
																					whyToStop = "Run Over";
																					GoAheadCurrent = false;
																					break;
																				}
					                                            
																				if(GoAheadExp)
																				{
																					JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,"- -",useFileForCycleWithPackageLineText1+"-|-"+useFileForCycleWithPackageLineText2,whyToStop,"show"});
																					ExpPackageHistoryItem[0] = StepFileNameTmp;
																					ExpPackageHistoryItem[1] = requestData;
																					ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																					previousData = responseData;
																					ExpPackageHistoryItem[3] = responseData;
																					ExpPackageHistoryItem[4] = whyToStop;
																					ExpPackageHistoryList.add(ExpPackageHistoryItem);
																					expHTTPHistoryList.add(ExpPackageHistoryItem);
																					ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																					ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																					ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																					ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																					ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																					ExpPackageHistoryItem = new String[7];
																					responseStatus = 0;
																					responseData = "";
																					whyToStop = "Run Over";
																					break;
																				}

																				JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,"- -",useFileForCycleWithPackageLineText1+"-|-"+useFileForCycleWithPackageLineText2,whyToStop,"show"});
																				ExpPackageHistoryItem[0] = StepFileNameTmp;
																				ExpPackageHistoryItem[1] = requestData;
																				ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																				previousData = responseData;
																				ExpPackageHistoryItem[3] = responseData;
																				ExpPackageHistoryItem[4] = whyToStop;
																				ExpPackageHistoryList.add(ExpPackageHistoryItem);
																				expHTTPHistoryList.add(ExpPackageHistoryItem);
																				ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																				ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																				ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																				ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																				ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																				ExpPackageHistoryItem = new String[7];
																				responseStatus = 0;
																				responseData = "";
																				whyToStop = "Run Over";                	                
																				
																			}
																		}
																	}
																}
															}            
														}
														else ///////////////////////////////////////////////////////////
														{
															//System.out.println("meiyou package file cyle");
															
															
															if (lineTxtTmp[1].trim().split(":").length > 1)
															{
																host = Method.getFromBase64(lineTxtTmp[1].trim().split(":")[1]);		
															}
															else
															{
																break;
															}

															
															if (lineTxtTmp[2].trim().split(":").length > 1)
															{
																port = Integer.valueOf(Method.getFromBase64(lineTxtTmp[2].trim().split(":")[1]));
															}
															else
															{
																port=80;
															}
									
															StepFileNameTmp = Method.getFromBase64(lineTxtTmp[3].trim().split(":")[1]);									
															if(StepFileNameTmp.length()>0)
															{
																requestData = Method.bytesToString(Method.HexStringTobytes(Method.stringsToString(Method.readFileToHexStrings(fileDir+"\\"+StepFileNameTmp))));
															}
															
															if(previousURL.length()>0)
															{
																// 添加 phpcms/  到request 的 GET  phpcms/。。。。  http/1.1
																requestData = Method.addPreviousURL(requestData,previousURL);
															}
				                      
															String replaceHostPortString = lineTxtTmp[4].trim().split(":")[1].trim();
															if (replaceHostPortString.equals("true"))
															{
																host = targetHost;
																port = targetPort;
																requestData = Method.replaceHostPort(requestData,host,port);
															}
															else
															{
															}
															
															String useSetCookieFromPreviousString = lineTxtTmp[5].trim().split(":")[1].trim();
															if (useSetCookieFromPreviousString.equals("true"))
															{
																/*
																previousData = "HTTP/1.1 200 OK"+"\r\n"+
																					  "Date: Sun, 30 Nov 2014 09:16:33 GMT"+"\r\n"+
																					  "Server: Apache/2.2.9 (APMServ) PHP/5.2.6+"+"\r\n"+
																					  "X-Powered-By: PHP/5.2.6"+"\r\n"+
																					  "Set-Cookie: SYSZC=d2d4a91a4e41bea2078b09c29cd6a44a"+"\r\n"+
																					  "Set-Cookie: SYSUSER=admin"+"\r\n"+
																					  "Set-Cookie: SYSNAME=%E7%B3%BB%E7%BB%9F%E7%AE%A1%E7%90%86%E5%91%98"+"\r\n"+
																					  "Set-Cookie: SYSUSERID=3"+"\r\n"+
																					  "Set-Cookie: SYSTM=1417338994"+"\r\n"+
																					  "Content-Length: 2"+"\r\n"+
																					  "Content-Type: text/html"+"\r\n"+
																					  "\r\n"+							  
																					  "OK";
				                        						*/
				                        
																String[] tmpmp = Method.getHTTPHeadersParams(previousData);	                		
																String setCookieStr = "";
																try
																{
																	for(int n=0;n<tmpmp.length;n++)
																	{
																		if(n%2==0)
																		{
																			if(tmpmp[n].startsWith("Set-Cookie"))
																			{
																				setCookieStr =setCookieStr+tmpmp[n+1].trim()+";";
																			}
																		}
																	}
																	String OKrequestData="";        		
																	String[] requestDatas = requestData.split("\r\n");
																	boolean addCookie = true;
																	
																	for(int n=0;n<requestDatas.length;n++)
																	{		
																		requestDatas[n] = requestDatas[n]+"\r\n";
																		if(requestDatas[n].toLowerCase().trim().startsWith("cookie:"))
																		{
																			String[] tmpCookie = requestDatas[n].toLowerCase().trim().split("cookie:");
																			if(tmpCookie.length>1)
																			{
																				tmpCookie[1] = Method.replaceCookie(tmpCookie[1].trim(),setCookieStr);
																				requestDatas[n] = "Cookie: "+Method.stringsToString(tmpCookie)+"\r\n";
																				addCookie = false;
																			}
																		}
																	}
					                    	  
																	if(addCookie)
																	{
																		OKrequestData =Method.addHeader(requestData,"Cookie: ",setCookieStr);		
																	}
																	else
																	{
																		OKrequestData = Method.stringsToString(requestDatas);
																	}
																	requestData = OKrequestData; 
																}
																catch(Exception asdf)
																{
																	
																}
				                		
				                		
																  
				                        
															}
															else
															{
															}									
				                      	
															int randomStringTmpLength = lineTxtTmp[6].trim().split(":").length;
															if (randomStringTmpLength > 1)
															{
																String randomStringString = lineTxtTmp[6].trim().split(":")[1].trim();
																String[] randomStringItemStrings = randomStringString.split(",");
																int randomStringItemCount = randomStringItemStrings.length;
																
																for (int k = 0; k < randomStringItemCount; k++)
																{
																	String[] randomStringItem = new String[5];
																	randomStringItem[0] = Method.getFromBase64(randomStringItemStrings[k]).split(",")[0];
																	randomStringItem[1] = Method.getFromBase64(randomStringItemStrings[k]).split(",")[1];
																	randomStringItem[2] =Method.getFromBase64(randomStringItemStrings[k]).split(",")[2].substring(0, 1);
																	randomStringItem[4] = Method.getFromBase64(randomStringItemStrings[k]).split(",")[3];

																	if (Integer.valueOf(randomStringItem[1]).intValue() == 3)
																	{
																		randomStringItem[3] = randomStringItem[2].substring(1);
																		randomStringItem[2] = randomStringItem[2].substring(0, 1);
																	}															
				                        		
																	String strTmp = "rSI"+randomStringItem[0];
																	strTmp = Method.getBase64(strTmp);
																	strTmp = Method.addSignRegex(strTmp);
																	String randomStrTmp = "";
																	switch(Integer.valueOf(randomStringItem[1]))
																	{
																		case 0:randomStrTmp =new RandomString().getRandomString(Integer.valueOf(randomStringItem[2]),"i");requestData =requestData.replaceAll(strTmp, randomStrTmp);break;
																		case 1:randomStrTmp =new RandomString().getRandomString(Integer.valueOf(randomStringItem[2]),"l");requestData =requestData.replaceAll(strTmp, randomStrTmp);break;
																		case 2:randomStrTmp =new RandomString().getRandomString(Integer.valueOf(randomStringItem[2]),"il");requestData =requestData.replaceAll(strTmp, randomStrTmp);break;
																		case 3:randomStrTmp =new RandomString().getRandomStringFromDefineString(randomStringItem[3],Integer.valueOf(randomStringItem[2]));requestData =requestData.replaceAll(strTmp, randomStrTmp);break;																		
																	}
																	randomStringList.add(randomStringItem);											
																}													
															}

															
															int captchaTmpLength = lineTxtTmp[7].trim().split(":").length;
															if (captchaTmpLength > 1)
															{
																String captchaString = lineTxtTmp[7].trim().split(":")[1].trim();
																String[] captchaItemStrings = captchaString.split(",");
																int captchaItemCount = captchaItemStrings.length;
																for (int k = 0; k < captchaItemCount; k++)
																{
																	String[] captchaItem = new String[4];
																	captchaItem = Method.getFromBase64(captchaItemStrings[k]).split(",");
																	captchaList.add(captchaItem);
																	
																	String captchaStrTmp = "sPFC"+captchaItem[0];
																	captchaStrTmp = Method.getBase64(captchaStrTmp);
																	captchaStrTmp = Method.addSignRegex(captchaStrTmp);
				                            
																	boolean status = UUAPI.checkAPI();

																	if (!status)
																	{
																		JOptionPane.showMessageDialog(null, "API文件校验失败，无法使用打码服务", "Warning", 2);
																	}
																	else
																	{
																		final String ImgURL = "http://"+host+previousURL+captchaItem[1];  //获取验证码的url  
																		//System.out.println(ImgURL);                                         
																		final int CaptchaNumbersTextInt = Integer.valueOf(captchaItem[2]);
																		final String saveFileName = "img/tmp.jpg";
																		
																		String saveAbsolutePath = Method.getImgFromURL(ImgURL,saveFileName);
																		if (saveAbsolutePath != null)
																		{
																			if (CaptchaNumbersTextInt != 0)
																			{
																				try 
																				{
																					String[] captchaResult = UUAPI.easyDecaptcha(saveFileName, 1000 + CaptchaNumbersTextInt);
																					if (captchaResult[1].length() > 0)
																					{
																						requestData = requestData.replaceAll(captchaStrTmp, captchaResult[1]);   
																					}
																				} 
																				catch (IOException eee) 
																				{
																					eee.printStackTrace();
																				}	                                                    
																			}
																		}															
																	}		                            
																}
															}
				                        	                      													
															// 发送http 数据
															//requestData = requestData+"\r\n";
															//System.out.println(requestData);
															//System.out.println("--");
															//System.out.println(responseData);
															
															responseData = Method.bytesToString(Method.getHttPResponseData(host,port,requestData));
															
															responseStatus = Method.getHttpResponseHeaderStatus(responseData);
																				
				                      		
															int setGoAheadOrStopFromStatusTmpLength = lineTxtTmp[9].trim().split(":").length;
															if (setGoAheadOrStopFromStatusTmpLength > 1)
															{
																String setGoAheadOrStopFromStatusString = lineTxtTmp[9].trim().split(":")[1].trim();
																String[] setGoAheadOrStopFromStatusItemStrings = setGoAheadOrStopFromStatusString.split(",");
																int setGoAheadOrStopFromStatusItemCount = setGoAheadOrStopFromStatusItemStrings.length;
																for (int k = 0; k < setGoAheadOrStopFromStatusItemCount; k++)
																{
																	String[] setGoAheadOrStopFromStatusItem = new String[2];
																	setGoAheadOrStopFromStatusItem = Method.getFromBase64(setGoAheadOrStopFromStatusItemStrings[k]).split(",");
																	setGoAheadOrStopFromStatusList.add(setGoAheadOrStopFromStatusItem);														
																}
															}
				                      		
				                      		
				                      		
				                      		
															int setGoAheadOrStopFromStatusListCount = setGoAheadOrStopFromStatusList.size();
															if (setGoAheadOrStopFromStatusListCount > 0)
															{
																for (int j = 0; j < setGoAheadOrStopFromStatusListCount; j++)
																{
																	String[] setGoAheadOrStopFromStatusListItem = (String[])setGoAheadOrStopFromStatusList.get(j);
																	
																	if (responseStatus == Integer.valueOf(setGoAheadOrStopFromStatusListItem[0]))
																	{
																		String actionForStatus = setGoAheadOrStopFromStatusListItem[1];
				                            			
																		//                         GO ahead current   stop current 
																		///                                               ||
																		//																		
																		///											Go Exp    Stop Exp
																		///										
																		
																		if(actionForStatus.equals("Go Ahead EXP"))
																		{
																			GoAheadExp  = true;
																			whyToStop = "responseStatus; " + responseStatus + " -- Rule; " + setGoAheadOrStopFromStatusListItem[0] + " --Action; "+ setGoAheadOrStopFromStatusListItem[1];
																			break;
																		}
																		else if(actionForStatus.equals("Stop EXP"))
																		{
																			StopExp = true;	                            				
																			whyToStop = "responseStatus; " + responseStatus + " -- Rule; " + setGoAheadOrStopFromStatusListItem[0] + " --Action; "+ setGoAheadOrStopFromStatusListItem[1];
																			break;
																		}
																		else if(actionForStatus.equals("Go Ahead Current"))
																		{
																			GoAheadCurrent = true;
																			whyToStop = "responseStatus; " + responseStatus + " -- Rule; " + setGoAheadOrStopFromStatusListItem[0]+ " --Action; " + setGoAheadOrStopFromStatusListItem[1];
																			break;
																		}
																		else if(actionForStatus.equals("Stop Current"))
																		{
																			//StopCurrent = true;
																			//whyToStop = "responseStatus:" + responseStatus + " -- Rule" + setGoAheadOrStopFromStatusListItem[0] + setGoAheadOrStopFromStatusListItem[1];
																			//System.out.println(whyToStop);
																			//break;
																		}                                      				
																	}
																}
															}
															else
															{
																//System.out.println("mei you setGoAheadOrStopFromStatusListCount");
															}

															if ((StopExp) )
															{
																JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,"- -","- -",whyToStop,"show"});
																ExpPackageHistoryItem[0] = StepFileNameTmp;
																ExpPackageHistoryItem[1] = requestData;
																ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																previousData = responseData;
																ExpPackageHistoryItem[3] = responseData;
																ExpPackageHistoryItem[4] = whyToStop;
																ExpPackageHistoryList.add(ExpPackageHistoryItem);
																expHTTPHistoryList.add(ExpPackageHistoryItem);
																ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																ExpPackageHistoryItem = new String[7];
																responseStatus = 0;
																responseData = "";
																whyToStop = "Run Over";
																break;
															}
				                            
															if(GoAheadCurrent)
															{
																JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,"- -","- -",whyToStop,"show"});
																ExpPackageHistoryItem[0] = StepFileNameTmp;
																ExpPackageHistoryItem[1] = requestData;
																ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																previousData = responseData;
																ExpPackageHistoryItem[3] = responseData;
																ExpPackageHistoryItem[4] = whyToStop;
																ExpPackageHistoryList.add(ExpPackageHistoryItem);
																expHTTPHistoryList.add(ExpPackageHistoryItem);
																ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																ExpPackageHistoryItem = new String[7];
																responseStatus = 0;
																responseData = "";
																whyToStop = "Run Over";
																GoAheadCurrent = false;
																break;
															}
				                            
															if(GoAheadExp)
															{
																JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,"- -","- -",whyToStop,"show"});
																ExpPackageHistoryItem[0] = StepFileNameTmp;
																ExpPackageHistoryItem[1] = requestData;
																ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																previousData = responseData;
																ExpPackageHistoryItem[3] = responseData;
																ExpPackageHistoryItem[4] = whyToStop;
																ExpPackageHistoryList.add(ExpPackageHistoryItem);
																expHTTPHistoryList.add(ExpPackageHistoryItem);
																ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																ExpPackageHistoryItem = new String[7];
																responseStatus = 0;
																responseData = "";
																whyToStop = "Run Over";
																break;
															}	                            											
															
															int setGoAheadOrStopFromDataTmpLength = lineTxtTmp[10].trim().split(":").length;
															if (setGoAheadOrStopFromDataTmpLength > 1)
															{
																String setGoAheadOrStopFromDataString = lineTxtTmp[10].trim().split(":")[1].trim();
																String[] setGoAheadOrStopFromDataItemStrings = setGoAheadOrStopFromDataString.split(",");
																int setGoAheadOrStopFromDataItemCount = setGoAheadOrStopFromDataItemStrings.length;
																for (int k = 0; k < setGoAheadOrStopFromDataItemCount; k++)
																{
																	String[] setGoAheadOrStopFromDataItem = new String[2];
																	setGoAheadOrStopFromDataItem = Method.getFromBase64(setGoAheadOrStopFromDataItemStrings[k]).split(",");
																	setGoAheadOrStopFromDataList.add(setGoAheadOrStopFromDataItem);												
																}
															}
															                            
															
															int j;
															int setGoAheadOrStopFromDataListCount = setGoAheadOrStopFromDataList.size();
															if (setGoAheadOrStopFromDataListCount > 0)
															{
																for (j = 0; j < setGoAheadOrStopFromDataListCount; j++)
																{
																	String[] setGoAheadOrStopFromDataListItem = (String[])setGoAheadOrStopFromDataList.get(j);

																	
																	Pattern p=Pattern.compile(Method.getFromBase64(setGoAheadOrStopFromDataListItem[0]));
																	Matcher m=p.matcher(responseData);
																	//System.out.println(m.find());
																	
																	if (m.find())
																	{
																		String actionForData = setGoAheadOrStopFromDataListItem[1];
																		
																		if(actionForData.equals("Go Ahead EXP"))
																		{
																			GoAheadExp  = true;
																			whyToStop = " -- Rule; " + Method.getFromBase64(setGoAheadOrStopFromDataListItem[0]) +"  --result; "+m.group()+ " --Action; "+ setGoAheadOrStopFromDataListItem[1];
																			break;
																		}
																		else if(actionForData.equals("Stop EXP"))
																		{
																			StopExp = true;
																			whyToStop = " -- Rule; " + Method.getFromBase64(setGoAheadOrStopFromDataListItem[0]) +"  --result; "+m.group()+ " --Action; "+ setGoAheadOrStopFromDataListItem[1];
																			break;
																		}
																		else if(actionForData.equals("Go Ahead Current"))
																		{
																			GoAheadCurrent = true;
																			whyToStop = " -- Rule; " + Method.getFromBase64(setGoAheadOrStopFromDataListItem[0]) +"  --result; "+m.group()+ " --Action; "+ setGoAheadOrStopFromDataListItem[1];
																			break;
																		}
																		else if(actionForData.equals("Stop Current"))
																		{
																			//StopCurrent = true;
																			//whyToStop = "responseStatus:" + responseStatus + " -- Rule" + setGoAheadOrStopFromStatusListItem[0] + setGoAheadOrStopFromStatusListItem[1];
																			//System.out.println(whyToStop);
																			//break;
																		} 													
																	}
																}
															}
															else
															{
																//System.out.println("meiyou setGoAheadOrStopFromDataList");
															}

															if ((StopExp) )
															{
																JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,"- -","- -",whyToStop,"show"});
																ExpPackageHistoryItem[0] = StepFileNameTmp;
																ExpPackageHistoryItem[1] = requestData;
																ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																previousData = responseData;
																ExpPackageHistoryItem[3] = responseData;
																ExpPackageHistoryItem[4] = whyToStop;
																ExpPackageHistoryList.add(ExpPackageHistoryItem);
																expHTTPHistoryList.add(ExpPackageHistoryItem);
																ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																ExpPackageHistoryItem = new String[7];
																responseStatus = 0;
																responseData = "";
																whyToStop = "Run Over";
																break;
															}
				                            
															if(GoAheadCurrent)
															{
																JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,"- -","- -",whyToStop,"show"});
																ExpPackageHistoryItem[0] = StepFileNameTmp;
																ExpPackageHistoryItem[1] = requestData;
																ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																previousData = responseData;
																ExpPackageHistoryItem[3] = responseData;
																ExpPackageHistoryItem[4] = whyToStop;
																ExpPackageHistoryList.add(ExpPackageHistoryItem);
																expHTTPHistoryList.add(ExpPackageHistoryItem);
																ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																ExpPackageHistoryItem = new String[7];
																responseStatus = 0;
																responseData = "";
																whyToStop = "Run Over";
																GoAheadCurrent = false;
																break;
															}
				                            
															if(GoAheadExp)
															{
																JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,"- -","- -",whyToStop,"show"});
																ExpPackageHistoryItem[0] = StepFileNameTmp;
																ExpPackageHistoryItem[1] = requestData;
																ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																previousData = responseData;
																ExpPackageHistoryItem[3] = responseData;
																ExpPackageHistoryItem[4] = whyToStop;
																ExpPackageHistoryList.add(ExpPackageHistoryItem);
																expHTTPHistoryList.add(ExpPackageHistoryItem);
																ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																ExpPackageHistoryItem = new String[7];
																responseStatus = 0;
																responseData = "";
																whyToStop = "Run Over";
																break;
															}

															JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,"- -","- -",whyToStop,"show"});
															ExpPackageHistoryItem[0] = StepFileNameTmp;
															ExpPackageHistoryItem[1] = requestData;
															ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
															previousData = responseData;
															ExpPackageHistoryItem[3] = responseData;
															ExpPackageHistoryItem[4] = whyToStop;
															ExpPackageHistoryList.add(ExpPackageHistoryItem);
															expHTTPHistoryList.add(ExpPackageHistoryItem);
															ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
															ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
															ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
															ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
															ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
															ExpPackageHistoryItem = new String[7];
															responseStatus = 0;
															responseData = "";
															whyToStop = "Run Over";
				                      														
														}                    
													}
													else
													{
														//System.out.println("lineTxtTmpLength  !===== 131311313");
													}                                   
												}
												else
												{
													//System.out.println("lineTxtTmpLength>>>>>>>>>>>..0");
												}
											} 
											if(GoAheadExp)
											{	
												GoAheadExp  = false;									
											}
										}					
									}
									else
									{
										//System.out.println("找不到指定的文件");
									}
								}
								catch (Exception ee)
								{
									ee.printStackTrace();
								}								
							}
						}							
						JDPDetectionRightPanelTargetGoPanelEditPanelButton.setEnabled(true);
					}
					
				});
					
				
				startThread.start();
				
				
				
			}
		});
		
		JDPDetectionRightPanelTargetGoPanelEditPanel.add(JDPDetectionRightPanelTargetGoPanelEditPanelTargetLabel);
		JDPDetectionRightPanelTargetGoPanelEditPanel.add(JDPDetectionRightPanelTargetGoPanelEditPanelText);
		JDPDetectionRightPanelTargetGoPanelEditPanel.add(JDPDetectionRightPanelTargetGoPanelEditPanelButton);
		
		//Detection  右侧的 request 和 response 面板
		JPanel JDPDetectionRightPanelTargetGoPanelShowPanel = new JPanel();
		JDPDetectionRightPanelTargetGoPanelShowPanel.setPreferredSize(new Dimension(0,100));
		JDPDetectionRightPanelTargetGoPanel.add(JDPDetectionRightPanelTargetGoPanelEditPanel,BorderLayout.NORTH);
		JDPDetectionRightPanelTargetGoPanel.add(JDPDetectionRightPanelTargetGoPanelShowPanel,BorderLayout.CENTER);
		JDPDetectionRightPanelTargetGoPanelShowPanel.setBackground(Color.blue);
		JDPDetectionRightPanelTargetGoPanelShowPanel.setLayout(new BorderLayout());
		
		
		
		
	    JDPDetectionRightPanelTargetGoPanelShowPanel.add(JDPDetectionRightPanelTargetGoPanelShowPanelScollPanel,BorderLayout.CENTER);
	    
	    
	    
	
		JDPDetectionRightPanel.add(JDPDetectionRightPanelConfigPanel,BorderLayout.NORTH);
		JDPDetectionRightPanel.add(JDPDetectionRightPanelTargetGoPanel,BorderLayout.CENTER);						
		//JDPDetection.add(JDPDetectionLeftPanel,BorderLayout.WEST);
		//JDPDetection.add(JDPDetectionRightPanel,BorderLayout.CENTER);
		JSplitPane SplitDetectionPanel = new JSplitPane();// 可以改变大小的 面板
		SplitDetectionPanel.add(JDPDetectionLeftPanel, JSplitPane.LEFT);  
		SplitDetectionPanel.add(JDPDetectionRightPanel, JSplitPane.RIGHT);  
		JDPDetection.add(SplitDetectionPanel,BorderLayout.CENTER);
		JButton test = new JButton("test");
		//JDPDetection.add(test, "Center");

		///////////////////////////////////////////////////////////////////////////////////////////////stop  ///////////////////////
		/*
		test.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				String fileDir = "test/";

				String StepFileNameTmp = "";

				List randomStringList = new ArrayList();
				List captchaList = new ArrayList();
				List setParamsFromDataPackageList = new ArrayList();
				List setGoAheadOrStopFromStatusList = new ArrayList();
				List setGoAheadOrStopFromDataList = new ArrayList();
				List useFileForCycleWithPackageList = new ArrayList();
				String fileForCycleWithEXP = "";
        
				String targetHost = "www.baidu.com";
				int targetPort = 88;
        
				try
				{
					File file = new File(fileDir + "Step.txt");
					if ((file.isFile()) && (file.exists()))
					{
						InputStreamReader read = new InputStreamReader(new FileInputStream(file));
						BufferedReader bufferedReader = new BufferedReader(read);
						String lineTxt = null;
						List tmplist = new ArrayList();

						while ((lineTxt = bufferedReader.readLine()) != null)
						{
							tmplist.add(lineTxt);
						}
						
						read.close();

						int StepNums = tmplist.size();

						if (StepNums > 0)
						{
							String tmpStr = (String)tmplist.get(0);
							fileForCycleWithEXP = "";
							String[] lineTxtTmp = tmpStr.split("-");
							int lineTxtTmpLength = lineTxtTmp.length;
							if (lineTxtTmpLength == 13)
							{
								String[] fileForCycleWithEXPStrings = lineTxtTmp[12].trim().split(":");
								if (fileForCycleWithEXPStrings.length > 1)
								{
									fileForCycleWithEXP = Method.getFromBase64(fileForCycleWithEXPStrings[1].trim());
								}
								System.out.println(fileForCycleWithEXP);
							}

						}

						if (fileForCycleWithEXP.length() > 0)
						{
							System.out.println("you ffileForCycleWithEXP ");

							File fileForCycleWithEXPFile = new File(fileForCycleWithEXP);
							InputStreamReader inputSteamReadTmp = new InputStreamReader(new FileInputStream(fileForCycleWithEXPFile));
							BufferedReader expFileBufferedReader = new BufferedReader(inputSteamReadTmp);
							String expFileLineTxt = null;

							boolean StopExp = false;
							boolean StopCurrent =false;
							boolean GoAheadCurrent = false;
							boolean GoAheadExp = false;
              
              
              
							while((expFileLineTxt = expFileBufferedReader.readLine()) != null&&!StopExp)
							{
            	  
            	  
            	  
								List ExpPackageHistoryList = new ArrayList();
								String[] ExpPackagePreviousItem = new String[5];

								for (int i = 0; i < StepNums; i++)
								{
									String StringForEachStep = (String)tmplist.get(i);
									
									if (StringForEachStep.length() > 0)
									{
										StepFileNameTmp = "";
                      
										String requestData = "";
										String responseData = "";
										int responseStatus = 0;
										String whyToStop = "Run Over";

										String[] ExpPackageHistoryItem = new String[5];
                      
										String host = "";
										int port = 80;
										boolean replaceHostPort = true;
										boolean useSetCookieFromPrevious = true;
										randomStringList = new ArrayList();
										captchaList = new ArrayList();
										setParamsFromDataPackageList = new ArrayList();
										setGoAheadOrStopFromStatusList = new ArrayList();
										setGoAheadOrStopFromDataList = new ArrayList();
										useFileForCycleWithPackageList = new ArrayList();
										fileForCycleWithEXP = "";
										String[] lineTxtTmp = StringForEachStep.split("-");
										int lineTxtTmpLength = lineTxtTmp.length;
                      
                      
										if (lineTxtTmpLength == 13)
										{
                    	  
											System.out.println("useFileForCycleWithPackage");
											int useFileForCycleWithPackageTmpLength = lineTxtTmp[11].trim().split(":").length;
											if (useFileForCycleWithPackageTmpLength > 1)
											{
												String useFileForCycleWithPackageString = lineTxtTmp[11].trim().split(":")[1].trim();
												String[] useFileForCycleWithPackageItemStrings = useFileForCycleWithPackageString.split(",");
												int useFileForCycleWithPackageItemCount = useFileForCycleWithPackageItemStrings.length;
												for (int k = 0; k < useFileForCycleWithPackageItemCount; k++)
												{
													String[] useFileForCycleWithPackageItem = new String[4];
													useFileForCycleWithPackageItem = Method.getFromBase64(useFileForCycleWithPackageItemStrings[k]).split(",");

													if (Integer.valueOf(useFileForCycleWithPackageItem[0]).intValue() == 1)
													{
														useFileForCycleWithPackageItem[1] = Method.getFromBase64(useFileForCycleWithPackageItem[1].trim());
														System.out.println(useFileForCycleWithPackageItem[0] + "," + useFileForCycleWithPackageItem[1]);
													}
													else
													{
														useFileForCycleWithPackageItem[1] = Method.getFromBase64(useFileForCycleWithPackageItem[1].trim());
														useFileForCycleWithPackageItem[2] = Method.getFromBase64(useFileForCycleWithPackageItem[2].trim());
														useFileForCycleWithPackageItem[3] = useFileForCycleWithPackageItem[3].trim();
														System.out.println(useFileForCycleWithPackageItem[0] + "," + useFileForCycleWithPackageItem[1] + "," + useFileForCycleWithPackageItem[2] + "," + useFileForCycleWithPackageItem[3]);
													}
													useFileForCycleWithPackageList.add(useFileForCycleWithPackageItem);
												}
                              
                              
                              
												int useFileForCycleWithPackageListCount = useFileForCycleWithPackageList.size();
												for (int t = 0; t < useFileForCycleWithPackageListCount; t++)
												{
													String[] useFileForCycleWithPackageItem = (String[])useFileForCycleWithPackageList.get(t);
													if (Integer.valueOf(useFileForCycleWithPackageItem[0]) == 1)
													{
														System.out.println("package 一个文件");
														File useFileForCycleWithPackageFile1 = new File(useFileForCycleWithPackageItem[1]);
														InputStreamReader useFileForCycleWithPackageFile1InputSteamReadTmp = new InputStreamReader(new FileInputStream(useFileForCycleWithPackageFile1));
														BufferedReader useFileForCycleWithPackageFile1ExpFileBufferedReader = new BufferedReader(useFileForCycleWithPackageFile1InputSteamReadTmp);
														String useFileForCycleWithPackageLineText = null;
														while ((useFileForCycleWithPackageLineText = useFileForCycleWithPackageFile1ExpFileBufferedReader.readLine()) != null)
														{
															System.out.println(useFileForCycleWithPackageLineText);
                                      
                                      
                                      
															if (lineTxtTmp[1].trim().split(":").length > 1)
															{
																host = Method.getFromBase64(lineTxtTmp[1].trim().split(":")[1]);
																System.out.println(host);
															}
															else
															{
																System.out.println("host is null and next package");
																//continue;
															}

															if (lineTxtTmp[2].trim().split(":").length > 1)
															{
																port = Integer.valueOf(Method.getFromBase64(lineTxtTmp[2].trim().split(":")[1]));
																System.out.println(port);
															}
															else
															{
																System.out.println("port is null and next package");
																//continue;
															}


                                      
                                      
                                      
															System.out.println(lineTxtTmp[3].trim());
															StepFileNameTmp = Method.getFromBase64(lineTxtTmp[3].trim().split(":")[1]);
															System.out.println(StepFileNameTmp);
															if(StepFileNameTmp.length()>0)
															{
																requestData = Method.bytesToString(Method.HexStringTobytes(Method.stringsToString(Method.readFileToHexStrings(fileDir+"\\"+StepFileNameTmp))));
															}
															
                                     
                                      
															String replaceHostPortString = lineTxtTmp[4].trim().split(":")[1].trim();
															if (replaceHostPortString.equals("true"))
															{
																replaceHostPort = true;
																host = targetHost;
																port = targetPort;
																requestData = Method.replaceHostPort(requestData,host,port);
															}
															else
															{
																replaceHostPort = false;
															}
															//System.out.println("replaceHostPortString:" + replaceHostPort);
															//System.out.println(requestData);
															//System.exit(1);
															
                                      
                                      
                                      
															String useSetCookieFromPreviousString = lineTxtTmp[5].trim().split(":")[1].trim();
															if (useSetCookieFromPreviousString.equals("true"))
															{
																useSetCookieFromPrevious = true;
																String previousData = "HTTP/1.1 200 OK"+"\r\n"+
																					  "Date: Sun, 30 Nov 2014 09:16:33 GMT"+"\r\n"+
																					  "Server: Apache/2.2.9 (APMServ) PHP/5.2.6+"+"\r\n"+
																					  "X-Powered-By: PHP/5.2.6"+"\r\n"+
																					  "Set-Cookie: SYSZC=d2d4a91a4e41bea2078b09c29cd6a44a"+"\r\n"+
																					  "Set-Cookie: SYSUSER=admin"+"\r\n"+
																					  "Set-Cookie: SYSNAME=%E7%B3%BB%E7%BB%9F%E7%AE%A1%E7%90%86%E5%91%98"+"\r\n"+
																					  "Set-Cookie: SYSUSERID=3"+"\r\n"+
																					  "Set-Cookie: SYSTM=1417338994"+"\r\n"+
																					  "Content-Length: 2"+"\r\n"+
																					  "Content-Type: text/html"+"\r\n"+
																					  "\r\n"+							  
																					  "OK";
                                        
                                        
																String[] tmpmp = Method.getHTTPHeadersParams(previousData);
                                		
																String setCookieStr = "";
																for(int n=0;n<tmpmp.length;n++)
																{
																	if(n%2==0)
																	{
																		//System.out.println(tmpmp[i]);
																		if(tmpmp[n].startsWith("Set-Cookie"))
																		{
																			setCookieStr =setCookieStr+tmpmp[n+1].trim()+";";
																		}
																	}
																}
                                		
                                		
																String OKrequestData="";
                                		
																String[] requestDatas = requestData.split("\r\n");
																boolean addCookie = true;
																System.out.println(requestDatas.length);
																for(int n=0;n<requestDatas.length;n++)
																{		
																	requestDatas[n] = requestDatas[n]+"\r\n";
																	if(requestDatas[n].toLowerCase().trim().startsWith("cookie:"))
																	{
																		//System.out.println("CXoi");
																		String[] tmpCookie = requestDatas[n].toLowerCase().trim().split("cookie:");
																		if(tmpCookie.length>1)
																		{
																			//System.out.println("-"+tmpCookie[0]+"-");
																			tmpCookie[1] = Method.replaceCookie(tmpCookie[1].trim(),setCookieStr);
																			requestDatas[n] = "Cookie: "+Method.stringsToString(tmpCookie)+"\r\n";
																			addCookie = false;
																		}
																	}
																}
                                    	  
																if(addCookie)
																{
																	OKrequestData =Method.addHeader(requestData,"Cookie: ",setCookieStr);
	                                			
																}
																else
																{
																	OKrequestData = Method.stringsToString(requestDatas);
																}
																requestData = OKrequestData;   
                                        
															}
															else
															{
																useSetCookieFromPrevious = false;
															}
															//System.out.println("useSetCookieFromPreviousString:" + useSetCookieFromPrevious);
															//System.out.println(requestData);
															//System.exit(1);
                                      
                                      
                                      	
                                      	
                                      	
															int randomStringTmpLength = lineTxtTmp[6].trim().split(":").length;
															if (randomStringTmpLength > 1)
															{
																String randomStringString = lineTxtTmp[6].trim().split(":")[1].trim();
																String[] randomStringItemStrings = randomStringString.split(",");
																int randomStringItemCount = randomStringItemStrings.length;
																for (int k = 0; k < randomStringItemCount; k++)
																{
																	String[] randomStringItem = new String[4];
																	randomStringItem[0] = Method.getFromBase64(randomStringItemStrings[k]).split(",")[0];
																	randomStringItem[1] = Method.getFromBase64(randomStringItemStrings[k]).split(",")[1];
																	randomStringItem[2] =Method.getFromBase64(randomStringItemStrings[k]).split(",")[2].substring(0, 1);

																	System.out.print(randomStringItem[0] + "," + randomStringItem[1] + "," + randomStringItem[2]);
																	if (Integer.valueOf(randomStringItem[1]).intValue() == 3)
																	{
																		randomStringItem[3] = randomStringItem[2].substring(1);
																		randomStringItem[2] = randomStringItem[2].substring(0, 1);
																	}

																	System.out.println("---" + randomStringItem[0] + "-" + randomStringItem[1] + "-" + randomStringItem[2] + "-------");
                                        		
																	String strTmp = "rSI"+randomStringItem[0];
																	strTmp = Method.getBase64(strTmp);
																	strTmp = Method.addSignRegex(strTmp);
																	String randomStrTmp = "";
																	switch(Integer.valueOf(randomStringItem[1]))
																	{
																		case 0:randomStrTmp =new RandomString().getRandomString(Integer.valueOf(randomStringItem[2]),"i");System.out.println(randomStrTmp);requestData =requestData.replaceAll(strTmp, randomStrTmp);break;
																		case 1:randomStrTmp =new RandomString().getRandomString(Integer.valueOf(randomStringItem[2]),"l");System.out.println(randomStrTmp);requestData =requestData.replaceAll(strTmp, randomStrTmp);break;
																		case 2:randomStrTmp =new RandomString().getRandomString(Integer.valueOf(randomStringItem[2]),"il");System.out.println(randomStrTmp);requestData =requestData.replaceAll(strTmp, randomStrTmp);break;
																		case 3:break;
																	
																	
																	}
                                        		
																	randomStringList.add(randomStringItem);
																	System.out.println(requestData);
																}
																
															}
                                      	
                                      	
                                      	
															
															int captchaTmpLength = lineTxtTmp[7].trim().split(":").length;
															if (captchaTmpLength > 1)
															{
																String captchaString = lineTxtTmp[7].trim().split(":")[1].trim();
																String[] captchaItemStrings = captchaString.split(",");
																int captchaItemCount = captchaItemStrings.length;
																for (int k = 0; k < captchaItemCount; k++)
																{
																	String[] captchaItem = new String[3];
																	captchaItem = Method.getFromBase64(captchaItemStrings[k]).split(",");
																	captchaList.add(captchaItem);
																	System.out.println(captchaItem[0] + "," + captchaItem[1] + "," + captchaItem[2]);
																	
																	String captchaStrTmp = "sPFC"+captchaItem[0];
																	captchaStrTmp = Method.getBase64(captchaStrTmp);
																	captchaStrTmp = Method.addSignRegex(captchaStrTmp);
                                            
                                            
																	boolean status = UUAPI.checkAPI();

																	if (!status)
																	{
																		System.out.print("API文件校验失败，无法使用打码服务");
																		JOptionPane.showMessageDialog(null, "API文件校验失败，无法使用打码服务", "Warning", 2);
																	}
																	else
																	{
																		final String ImgURL = captchaItem[1];                                             
																		final int CaptchaNumbersTextInt = Integer.valueOf(captchaItem[2]);
																		final String saveFileName = "img/tmp.jpg";
																		
																		String saveAbsolutePath = Method.getImgFromURL(ImgURL,saveFileName);
																		if (saveAbsolutePath != null)
																		{
																			if (CaptchaNumbersTextInt != 0)
																			{
																				try 
																				{
																					String[] captchaResult = UUAPI.easyDecaptcha(saveFileName, 1000 + CaptchaNumbersTextInt);
																					if (captchaResult[1].length() > 0)
																					{
																						requestData = requestData.replaceAll(captchaStrTmp, captchaResult[1]);   
																					}
																				} 
																				catch (IOException eee) 
																				{
																					// TODO Auto-generated catch block
																					eee.printStackTrace();
																				}	                                                    
																			}
                                      					
																		}
																		
																	}
                                            
																}

															}
                                        
                                      	
                                        
															//
															String FileForCycleWithEXPStrTmp = "FileForCycleWithEXP";
															FileForCycleWithEXPStrTmp = Method.getBase64(FileForCycleWithEXPStrTmp);
															FileForCycleWithEXPStrTmp = Method.addSignRegex(FileForCycleWithEXPStrTmp);
															requestData = requestData.replaceAll(FileForCycleWithEXPStrTmp, expFileLineTxt);
                                      		
                                        
                                        
															//useFileForCycleWithPackageLineText
															String useFileForCycleWithPackageLineStrTmp = "File-1";
															useFileForCycleWithPackageLineStrTmp = Method.getBase64(useFileForCycleWithPackageLineStrTmp);
															useFileForCycleWithPackageLineStrTmp = Method.addSignRegex(useFileForCycleWithPackageLineStrTmp);
															requestData = requestData.replaceAll(useFileForCycleWithPackageLineStrTmp, useFileForCycleWithPackageLineText);
                                      		
                                      		
															System.out.println(requestData);
                                      		
                                      	
                                        
															responseStatus = 200;
                                        
                                        
                                      		
                                      		
                                      		
                                      		
															int setGoAheadOrStopFromStatusTmpLength = lineTxtTmp[9].trim().split(":").length;
															if (setGoAheadOrStopFromStatusTmpLength > 1)
															{
																String setGoAheadOrStopFromStatusString = lineTxtTmp[9].trim().split(":")[1].trim();
																String[] setGoAheadOrStopFromStatusItemStrings = setGoAheadOrStopFromStatusString.split(",");
																int setGoAheadOrStopFromStatusItemCount = setGoAheadOrStopFromStatusItemStrings.length;
																for (int k = 0; k < setGoAheadOrStopFromStatusItemCount; k++)
																{
																	String[] setGoAheadOrStopFromStatusItem = new String[2];
																	setGoAheadOrStopFromStatusItem = Method.getFromBase64(setGoAheadOrStopFromStatusItemStrings[k]).split(",");
																	setGoAheadOrStopFromStatusList.add(setGoAheadOrStopFromStatusItem);
																	System.out.println(setGoAheadOrStopFromStatusItem[0] + "," + setGoAheadOrStopFromStatusItem[1]);
																}

															}
                                      		
                                      		
                                      		
                                      		
															int setGoAheadOrStopFromStatusListCount = setGoAheadOrStopFromStatusList.size();
															if (setGoAheadOrStopFromStatusListCount > 0)
															{
																System.out.println("you setGoAheadOrStopFromStatusListCount ");
																for (int j = 0; j < setGoAheadOrStopFromStatusListCount; j++)
																{
																	String[] setGoAheadOrStopFromStatusListItem = (String[])setGoAheadOrStopFromStatusList.get(j);
																	System.out.println("break");
																	
																	if (responseStatus == Integer.valueOf(setGoAheadOrStopFromStatusListItem[0]))
																	{
																		String actionForStatus = setGoAheadOrStopFromStatusListItem[1];
                                            			
                                            			
																		if(actionForStatus.equals("Go Ahead EXP"))
																		{
																			GoAheadExp  = true;
																			break;
																		}
																		else if(actionForStatus.equals("Stop EXP"))
																		{
																			StopExp = true;
                                            				
																			whyToStop = "responseStatus:" + responseStatus + " -- Rule" + setGoAheadOrStopFromStatusListItem[0] + setGoAheadOrStopFromStatusListItem[1];
																			System.out.println(whyToStop);
																			break;
																		}
																		else if(actionForStatus.equals("Go Ahead Current"))
																		{
																			GoAheadCurrent = true;
																			break;
																		}
																		else if(actionForStatus.equals("Stop Current"))
																		{
																			StopCurrent = true;
																			whyToStop = "responseStatus:" + responseStatus + " -- Rule" + setGoAheadOrStopFromStatusListItem[0] + setGoAheadOrStopFromStatusListItem[1];
																			System.out.println(whyToStop);
																			break;
																		}                                      			
																		
																	}
																}
															}
															else
															{
																System.out.println("mei you setGoAheadOrStopFromStatusListCount");
															}

															if ((StopExp) || (StopCurrent))
															{
																
																System.out.println("StopEXP" + StopExp + "StopCurrent:" + StopCurrent);
																StopCurrent = false;
																break;
															}
                                            
															if(GoAheadCurrent)
															{
																GoAheadCurrent = false;
																continue;
															}
                                            
															if(GoAheadExp)
															{
																break;
															}
                                            
                                            
															int j;
															responseData = "/root/passwd/jdnehc";
															int setGoAheadOrStopFromDataListCount = setGoAheadOrStopFromDataList.size();
															if (setGoAheadOrStopFromDataListCount > 0)
															{
																System.out.println("you setGoAheadOrStopFromDataList ");
																for (j = 0; j < setGoAheadOrStopFromDataListCount; j++)
																{
																	String[] setGoAheadOrStopFromDataListItem = (String[])setGoAheadOrStopFromDataList.get(j);

																	boolean result = Pattern.matches(setGoAheadOrStopFromDataListItem[0], responseData);
																	if (result)
																	{
																		String actionForData = setGoAheadOrStopFromDataListItem[1];
																		if (!actionForData.equals("Go Ahead EXP"))
																		{
																			if (actionForData.equals("Stop EXP"))
																			{
																				StopExp = true;
																				whyToStop = "responseStatus:" + responseStatus + " -- Rule" + setGoAheadOrStopFromDataListItem[0] + setGoAheadOrStopFromDataListItem[1];
																			}
																			else if (!actionForData.equals("Go Ahead Current"))
																			{
																				if (actionForData.equals("Stop Current"))
																				{
																					StopCurrent = true;
																					whyToStop = "responseStatus:" + responseStatus + " -- Rule" + setGoAheadOrStopFromDataListItem[0] + setGoAheadOrStopFromDataListItem[1];
																				}
																			}
																		}
																	}
																}
															}
															else
															{
																System.out.println("meiyou setGoAheadOrStopFromDataList");
															}

															if ((StopExp) || (StopCurrent))
															{
																StopCurrent = false;
																System.out.println("StopEXP" + StopExp + "StopCurrent:" + StopCurrent);
																break;
															}

															ExpPackageHistoryItem[0] = StepFileNameTmp;
															ExpPackageHistoryItem[1] = requestData;
															ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
															ExpPackageHistoryItem[3] = responseData;
															ExpPackageHistoryItem[4] = whyToStop;
															ExpPackageHistoryList.add(ExpPackageHistoryItem);
															ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
															ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
															ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
															ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
															ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
															responseStatus = 0;
															responseData = "";
															whyToStop = "";
                                      		
                                        
                                        
															System.out.println(";;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;");
															System.out.println(requestData);
															System.out.println(";;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;");
															System.out.println(responseData);
                                            
															//System.exit(1);
                                      	
														}
													}
													else
													{
														System.out.println("两个文件11111111111111");
														System.out.println(useFileForCycleWithPackageItem.length);
														System.out.println(useFileForCycleWithPackageItem[1]);
														System.out.println(useFileForCycleWithPackageItem[2]);
														System.out.println("两个文件22222222222");
													}
												}            
											}
											else
											{
												System.out.println("meiyou package file cyle");
											}                    
										}
										else
										{
											System.out.println("lineTxtTmpLength  !===== 131311313");
										}                                   
									}
									else
									{
										System.out.println("lineTxtTmpLength>>>>>>>>>>>..0");
									}
								}         	            	  
							}
            
						}
						else
						{
							System.out.println("meiyou fileForCycleWithEXP");
						}					
					}
					else
					{
						System.out.println("找不到指定的文件");
					}
				}
				catch (Exception ee)
				{
					ee.printStackTrace();
				}
			}
		});
		
		*/
		
		//zhu tab Setting Desktop 
		JDesktopPane JDPSettings = new JDesktopPane();
		this.JPtab.add(JDPSettings, "Settings");
		JDPSettings.setLayout(new BorderLayout());
		JLabel tmp = new JLabel("这里要实现 设置 uu打码帐号，  或者 配置  ocr 验证码的本地程序 等");
		//JDPSettings.add(tmp, "Center");

		JPanel JDPSettingsMainPane = new JPanel();
		JDPSettingsMainPane.setLayout(new BorderLayout());
		JDPSettingsMainPane.setBackground(Color.blue);
		JDPSettings.add(JDPSettingsMainPane,BorderLayout.CENTER);
		
		JPanel JDPSettingsMainPaneUUDAMAPanel = new JPanel();
		JDPSettingsMainPaneUUDAMAPanel.setLayout(null);
		JDPSettingsMainPaneUUDAMAPanel.setBackground(Color.white);
		JDPSettingsMainPane.add(JDPSettingsMainPaneUUDAMAPanel,"Center");
		
		JLabel JDPSettingsMainPaneUUDAMAPanelUserNameLabel = new JLabel("UserName: ");
		JDPSettingsMainPaneUUDAMAPanelUserNameLabel.setSize(100,30);
		JDPSettingsMainPaneUUDAMAPanelUserNameLabel.setLocation(10, 10);
		JDPSettingsMainPaneUUDAMAPanel.add(JDPSettingsMainPaneUUDAMAPanelUserNameLabel);
		
		final JTextField JDPSettingsMainPaneUUDAMAPanelUserNameText = new JTextField();
		JDPSettingsMainPaneUUDAMAPanelUserNameText.setSize(300,25);
		JDPSettingsMainPaneUUDAMAPanelUserNameText.setLocation(110, 15);
		JDPSettingsMainPaneUUDAMAPanel.add(JDPSettingsMainPaneUUDAMAPanelUserNameText);
		
		JLabel JDPSettingsMainPaneUUDAMAPanelPasswordLabel = new JLabel("Password: ");
		JDPSettingsMainPaneUUDAMAPanelPasswordLabel.setSize(100,30);
		JDPSettingsMainPaneUUDAMAPanelPasswordLabel.setLocation(10, 40);
		JDPSettingsMainPaneUUDAMAPanel.add(JDPSettingsMainPaneUUDAMAPanelPasswordLabel);
		
		final JTextField JDPSettingsMainPaneUUDAMAPanelPasswordText = new JTextField();
		JDPSettingsMainPaneUUDAMAPanelPasswordText.setSize(300,25);
		JDPSettingsMainPaneUUDAMAPanelPasswordText.setLocation(110, 45);
		JDPSettingsMainPaneUUDAMAPanel.add(JDPSettingsMainPaneUUDAMAPanelPasswordText);
		
		JLabel JDPSettingsMainPaneUUDAMAPanelDllPathLabel = new JLabel("DllPath: ");
		JDPSettingsMainPaneUUDAMAPanelDllPathLabel.setSize(100,30);
		JDPSettingsMainPaneUUDAMAPanelDllPathLabel.setLocation(10, 70);
		JDPSettingsMainPaneUUDAMAPanel.add(JDPSettingsMainPaneUUDAMAPanelDllPathLabel);
		
		final JTextField JDPSettingsMainPaneUUDAMAPanelDllPathText = new JTextField();
		JDPSettingsMainPaneUUDAMAPanelDllPathText.setSize(300,25);
		JDPSettingsMainPaneUUDAMAPanelDllPathText.setLocation(110, 75);
		JDPSettingsMainPaneUUDAMAPanel.add(JDPSettingsMainPaneUUDAMAPanelDllPathText);
		
		JLabel JDPSettingsMainPaneUUDAMAPanelSOFTIDLabel = new JLabel("SOFTID: ");
		JDPSettingsMainPaneUUDAMAPanelSOFTIDLabel.setSize(100,30);
		JDPSettingsMainPaneUUDAMAPanelSOFTIDLabel.setLocation(10, 100);
		JDPSettingsMainPaneUUDAMAPanel.add(JDPSettingsMainPaneUUDAMAPanelSOFTIDLabel);
		
		final JTextField JDPSettingsMainPaneUUDAMAPanelSOFTIDText = new JTextField();
		JDPSettingsMainPaneUUDAMAPanelSOFTIDText.setSize(300,25);
		JDPSettingsMainPaneUUDAMAPanelSOFTIDText.setLocation(110, 105);
		JDPSettingsMainPaneUUDAMAPanel.add(JDPSettingsMainPaneUUDAMAPanelSOFTIDText);
		
		JLabel JDPSettingsMainPaneUUDAMAPanelSOFTKeyLabel = new JLabel("SOFTKey: ");
		JDPSettingsMainPaneUUDAMAPanelSOFTKeyLabel.setSize(100,30);
		JDPSettingsMainPaneUUDAMAPanelSOFTKeyLabel.setLocation(10, 130);
		JDPSettingsMainPaneUUDAMAPanel.add(JDPSettingsMainPaneUUDAMAPanelSOFTKeyLabel);
		
		final JTextField JDPSettingsMainPaneUUDAMAPanelSOFTKeyText = new JTextField();
		JDPSettingsMainPaneUUDAMAPanelSOFTKeyText.setSize(300,25);
		JDPSettingsMainPaneUUDAMAPanelSOFTKeyText.setLocation(110, 135);
		JDPSettingsMainPaneUUDAMAPanel.add(JDPSettingsMainPaneUUDAMAPanelSOFTKeyText);
		
		JLabel JDPSettingsMainPaneUUDAMAPanelDLLVerifyKeyLabel = new JLabel("DLLVerifyKey: ");
		JDPSettingsMainPaneUUDAMAPanelDLLVerifyKeyLabel.setSize(100,30);
		JDPSettingsMainPaneUUDAMAPanelDLLVerifyKeyLabel.setLocation(10, 160);
		JDPSettingsMainPaneUUDAMAPanel.add(JDPSettingsMainPaneUUDAMAPanelDLLVerifyKeyLabel);
		
		final JTextField JDPSettingsMainPaneUUDAMAPanelDLLVerifyKeyText = new JTextField();
		JDPSettingsMainPaneUUDAMAPanelDLLVerifyKeyText.setSize(300,25);
		JDPSettingsMainPaneUUDAMAPanelDLLVerifyKeyText.setLocation(110, 165);
		JDPSettingsMainPaneUUDAMAPanel.add(JDPSettingsMainPaneUUDAMAPanelDLLVerifyKeyText);
		
		JLabel JDPSettingsMainPaneUUDAMAPanelCheckStatusLabel = new JLabel("checkStatus: ");
		JDPSettingsMainPaneUUDAMAPanelCheckStatusLabel.setSize(100,30);
		JDPSettingsMainPaneUUDAMAPanelCheckStatusLabel.setLocation(10, 190);
		JDPSettingsMainPaneUUDAMAPanel.add(JDPSettingsMainPaneUUDAMAPanelCheckStatusLabel);
		
		final JTextField JDPSettingsMainPaneUUDAMAPanelCheckStatusText = new JTextField();
		JDPSettingsMainPaneUUDAMAPanelCheckStatusText.setSize(300,25);
		JDPSettingsMainPaneUUDAMAPanelCheckStatusText.setLocation(110, 195);
		JDPSettingsMainPaneUUDAMAPanel.add(JDPSettingsMainPaneUUDAMAPanelCheckStatusText);
		
		JButton  JDPSettingsMainPaneUUDAMAPanelSaveButton = new JButton("Save");
		JDPSettingsMainPaneUUDAMAPanelSaveButton.setFocusable(false);
		JDPSettingsMainPaneUUDAMAPanelSaveButton.setSize(100,25);
		JDPSettingsMainPaneUUDAMAPanelSaveButton.setLocation(310, 240);
		JDPSettingsMainPaneUUDAMAPanel.add(JDPSettingsMainPaneUUDAMAPanelSaveButton);
		
		
		JDPSettingsMainPaneUUDAMAPanelSaveButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				String USERNAME = "USERNAME:"+JDPSettingsMainPaneUUDAMAPanelUserNameText.getText().trim();
				String PASSWORD = "PASSWORD:"+JDPSettingsMainPaneUUDAMAPanelPasswordText.getText().trim();
				String DLLPATH = "DLLPATH:"+JDPSettingsMainPaneUUDAMAPanelDllPathText.getText().trim();
				String SOFTID = "SOFTID:"+JDPSettingsMainPaneUUDAMAPanelSOFTIDText.getText().trim();
				String SOFTKEY = "SOFTKEY:"+JDPSettingsMainPaneUUDAMAPanelSOFTKeyText.getText().trim();
				String DLLVerifyKey = "DLLVerifyKey:"+JDPSettingsMainPaneUUDAMAPanelDLLVerifyKeyText.getText().trim();
				String CheckStatus = "CheckStatus:"+JDPSettingsMainPaneUUDAMAPanelCheckStatusText.getText().trim();
				
				try
				{
					BufferedWriter writer = new BufferedWriter(new FileWriter(new File("sysconfig.txt")));
					writer.write(USERNAME+"\r\n");
					writer.write(PASSWORD+"\r\n");
					writer.write(DLLPATH+"\r\n");
					writer.write(SOFTID+"\r\n");
					writer.write(SOFTKEY+"\r\n");
					writer.write(DLLVerifyKey+"\r\n");
					writer.write(CheckStatus);
					writer.close();
					
					JOptionPane.showMessageDialog(null, "保存成功", "Warning", 2);
				}
				catch(Exception es)
				{
					es.printStackTrace();
				}
			}
		});
		
		
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
					//System.out.println();
					JDPSettingsMainPaneUUDAMAPanelUserNameText.setText(StringLineTmp.split("USERNAME:")[1]);
				}
				else if(StringLineTmp.startsWith("PASSWORD:"))
				{
				//	System.out.println(StringLineTmp);
					JDPSettingsMainPaneUUDAMAPanelPasswordText.setText(StringLineTmp.split("PASSWORD:")[1]);
				}
				else if(StringLineTmp.startsWith("DLLPATH:"))
				{
					//System.out.println(StringLineTmp);
					JDPSettingsMainPaneUUDAMAPanelDllPathText.setText(StringLineTmp.split("DLLPATH:")[1]);
				}
				else if(StringLineTmp.startsWith("SOFTID:"))
				{
					//System.out.println(StringLineTmp);
					JDPSettingsMainPaneUUDAMAPanelSOFTIDText.setText(StringLineTmp.split("SOFTID:")[1]);
				}
				else if(StringLineTmp.startsWith("SOFTKEY:"))
				{
				//	System.out.println(StringLineTmp);
					JDPSettingsMainPaneUUDAMAPanelSOFTKeyText.setText(StringLineTmp.split("SOFTKEY:")[1]);
				}
				else if(StringLineTmp.startsWith("DLLVerifyKey:"))
				{
				//	System.out.println(StringLineTmp);
					JDPSettingsMainPaneUUDAMAPanelDLLVerifyKeyText.setText(StringLineTmp.split("DLLVerifyKey:")[1]);
				}
				else if(StringLineTmp.startsWith("CheckStatus:"))
				{
				//	System.out.println(StringLineTmp);
					JDPSettingsMainPaneUUDAMAPanelCheckStatusText.setText(StringLineTmp.split("CheckStatus:")[1]);
				}
			}
			tmpFileInputSteamReadTmp.close();
			tmpFileBufferedReader.close();
		}
		catch(Exception e)
		{
			//e.printStackTrace();
			try
			{
				BufferedWriter writer = new BufferedWriter(new FileWriter(new File("sysconfig.txt")));
				writer.write("USERNAME:"+"\r\n");
				writer.write("PASSWORD:"+"\r\n");
				writer.write("DLLPATH:"+"\r\n");
				writer.write("SOFTID:"+"\r\n");
				writer.write("SOFTKEY:"+"\r\n");
				writer.write("DLLVerifyKey:"+"\r\n");
				writer.write("CheckStatus:");
				writer.close();
			}
			catch(Exception es)
			{
				es.printStackTrace();
			}
		}
		
		
		
		
		
		
		
		
		
		
		//zhu tab  Others Desktop
		JDesktopPane JDPOthers = new JDesktopPane();
		//this.JPtab.add(JDPOthers, "Others");
		JDPOthers.setLayout(new BorderLayout());
		JLabel tmp2 = new JLabel("这里可以导入一些 小工具，  木马扫描 ， 键盘记录，正则表达式联系");
		JDPOthers.add(tmp2, "Center");

		//Proxy Intercept 的主面板 
		JPanel InterceptMainPane = new JPanel();
		JDPIntercept.setLayout(new BorderLayout());
		JDPIntercept.add(InterceptMainPane, "Center");
		InterceptMainPane.setLayout(GBL);

		//proxy Intercept 的按钮面板   有 forward  drop  Intercept if on/off
		JPanel InterceptButtonPane = new JPanel();
		InterceptButtonPane.setLayout(null);
		InterceptButtonPane.setPreferredSize(new Dimension(0, 230));
		GridBagConstraints GBCIBP = new GridBagConstraints();
		GBCIBP.fill = 1;
		GBCIBP.insets = new Insets(5, 5, 5, 5);
		GBCIBP.gridx = 0;
		GBCIBP.gridy = 0;
		GBCIBP.gridheight = 4;
		GBCIBP.gridwidth = 1;
		GBCIBP.weightx = 1.0D;
		GBCIBP.weighty = 4.0D;
		InterceptMainPane.add(InterceptButtonPane, GBCIBP);

		//Proxy Intercept Forward  面板
		ForwardButton = new JButton("Forward");
		ForwardButton.setFocusable(false);
		ForwardButton.setSize(120, 28);
		ForwardButton.setLocation(18, 16);
		ForwardButton.setEnabled(false);
		InterceptButtonPane.add(ForwardButton);

		ForwardButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				//分别对应队列的三个参数     是否使用https   http请求的相关数据            http请求的输出流对象
				final Integer queueHTTPSSocketTmpTmp = queueHTTPSSocketTmp;
				final String[] queueproxyHistoryItemTmpTmp = queueproxyHistoryItemTmp;
				final OutputStream queueOutTmpTmp = queueOutTmp;
				
				//获取 Proxy History 的历史 table  用来更新http forward 请求之后的  response status
				final DefaultTableModel tableModel = (DefaultTableModel)ProxyHistoryListTable.getModel();
				
				//获取raw headers hex 存储数据的  数据
				final String StrBufferInterceptDaraRawTextArea = InterceptShowString;
				InterceptShowString = ""; //获取一次则清空
				InterceptDaraRawTextArea.setText(null);//清空 raw 
				
				//通过 队列的 queueHTTPSSocketTmpTmp 来判断当前是否有正常的http请求
				if (queueHTTPSSocketTmpTmp != null)
				{
					Thread InterceptForwardThread = new Thread(new Runnable() //定义一个新的线程来出来forward 
					{
						public void run()
						{
							String host = queueproxyHistoryItemTmpTmp[1]; //获取目标主机地址 
							int port = Integer.valueOf(queueproxyHistoryItemTmpTmp[7]);//获取目标端口
							String StrBuffer = queueproxyHistoryItemTmpTmp[5];//获取 完整的http请求数据报文
							
							if (!StrBuffer.equals(StrBufferInterceptDaraRawTextArea)) //这里是为了 如果有修改原有的数据包  则把修改后的存储到  【8】 
							{
								StrBuffer = StrBufferInterceptDaraRawTextArea;
								queueproxyHistoryItemTmpTmp[8] = StrBufferInterceptDaraRawTextArea;
							}

							boolean HttpsSocketBoolean = false;
							if (queueHTTPSSocketTmpTmp == 1) //1 说明使用 https 
							{
								HttpsSocketBoolean = true;
							}
							
							//获取http 请求的 返回的 status  随后更新table 
							int HttpResponseHeaderStatus = Method.getHttPResponseData(HttpsSocketBoolean, host, port, StrBuffer, queueOutTmpTmp, queueproxyHistoryItemTmpTmp);
							//【0】 位置 是记录当前是table 表格中的第几个 item  把  返回来的status 更新到响应的  item上
							int proxyHistoryNowNum = Integer.valueOf(queueproxyHistoryItemTmpTmp[0]);
							if (HttpResponseHeaderStatus > 0)
							{
								queueproxyHistoryItemTmpTmp[4] = String.valueOf(HttpResponseHeaderStatus);//【4】 位置是存放  response status
								tableModel.setValueAt(Integer.valueOf(HttpResponseHeaderStatus), proxyHistoryNowNum - 1, 4);
								ProxyHistoryListTable.invalidate();
							}
						}
					});
					InterceptForwardThread.start();
          
					//如果队列里 还有  序号 则继续 
					if ((queueHTTPSSocketTmp = (Integer)queueHTTPSSocket.poll()) != null)
					{
						queueproxyHistoryItemTmp = ((String[])queueproxyHistoryItem.poll());
						queueOutTmp = ((OutputStream)queueOut.poll());
						//分别取出队列的三个对象    放入到中心
						
						//取出request 放入  raw header hex 存储中心 
						InterceptShowString = queueproxyHistoryItemTmp[5];
						//proxy Intercept raw 显示 request
						InterceptDaraRawTextArea.setText(InterceptShowString);
						InterceptDaraRawTextArea.setSelectionStart(1);
						InterceptDaraRawTextArea.setSelectionEnd(1);
					}
				}
			}
		});
		DropButton = new JButton("Drop");
		DropButton.setFocusable(false);
		DropButton.setSize(120, 28);
		DropButton.setLocation(156, 16);
		DropButton.setEnabled(false);
		InterceptButtonPane.add(DropButton);
		
		DropButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				queueHTTPSSocketTmp=null;
				queueproxyHistoryItemTmp=null;
				try 
				{
					queueOutTmp.close();
				} 
				catch (IOException e1) 
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				InterceptDaraRawTextArea.setText("");
				
				
				//如果队列里 还有  序号 则继续 
				if ((queueHTTPSSocketTmp = (Integer)queueHTTPSSocket.poll()) != null)
				{
					queueproxyHistoryItemTmp = ((String[])queueproxyHistoryItem.poll());
					queueOutTmp = ((OutputStream)queueOut.poll());
					//分别取出队列的三个对象    放入到中心
					
					//取出request 放入  raw header hex 存储中心 
					InterceptShowString = queueproxyHistoryItemTmp[5];
					//proxy Intercept raw 显示 request
					InterceptDaraRawTextArea.setText(InterceptShowString);
					InterceptDaraRawTextArea.setSelectionStart(1);
					InterceptDaraRawTextArea.setSelectionEnd(1);
				}
				
				
			}
		});
		
		
		//Proxy Intercept Intercept is of/on 按钮
		final JButton InterceptStateButton = new JButton("Intercept is off");
		InterceptStateButton.setFocusable(false);
		InterceptStateButton.setSize(120, 28);
		InterceptStateButton.setLocation(294, 16);
		InterceptButtonPane.add(InterceptStateButton);

		//
		InterceptStateButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (!InterceptState) //原来不揭短  
				{
					InterceptStateButton.setText("Intercept is on");
					InterceptStateButton.setBackground(Color.white);
					InterceptState = true;
				}
				else //原本截断 
				{
					InterceptState = false;
					InterceptStateButton.setText("Intercept is off");
					InterceptStateButton.setBackground(null);
					
					
					
					// 放开就是forward  - ----------------------------------------------------------------
					
					final Integer queueHTTPSSocketTmpTmp = queueHTTPSSocketTmp;
					final String[] queueproxyHistoryItemTmpTmp = queueproxyHistoryItemTmp;
					final OutputStream queueOutTmpTmp = queueOutTmp;
					
					//获取 Proxy History 的历史 table  用来更新http forward 请求之后的  response status
					final DefaultTableModel tableModel = (DefaultTableModel)ProxyHistoryListTable.getModel();
					
					//获取raw headers hex 存储数据的  数据
					final String StrBufferInterceptDaraRawTextArea = InterceptShowString;
					InterceptShowString = ""; //获取一次则清空
					InterceptDaraRawTextArea.setText(null);//清空 raw 
					
					//通过 队列的 queueHTTPSSocketTmpTmp 来判断当前是否有正常的http请求
					if (queueHTTPSSocketTmpTmp != null)
					{
						Thread InterceptForwardThread = new Thread(new Runnable() //定义一个新的线程来出来forward 
						{
							public void run()
							{
								String host = queueproxyHistoryItemTmpTmp[1]; //获取目标主机地址 
								int port = Integer.valueOf(queueproxyHistoryItemTmpTmp[7]);//获取目标端口
								String StrBuffer = queueproxyHistoryItemTmpTmp[5];//获取 完整的http请求数据报文
								
								if (!StrBuffer.equals(StrBufferInterceptDaraRawTextArea)) //这里是为了 如果有修改原有的数据包  则把修改后的存储到  【8】 
								{
									StrBuffer = StrBufferInterceptDaraRawTextArea;
									queueproxyHistoryItemTmpTmp[8] = StrBufferInterceptDaraRawTextArea;
								}

								boolean HttpsSocketBoolean = false;
								if (queueHTTPSSocketTmpTmp == 1) //1 说明使用 https 
								{
									HttpsSocketBoolean = true;
								}
								
								//获取http 请求的 返回的 status  随后更新table 
								int HttpResponseHeaderStatus = Method.getHttPResponseData(HttpsSocketBoolean, host, port, StrBuffer, queueOutTmpTmp, queueproxyHistoryItemTmpTmp);
								//【0】 位置 是记录当前是table 表格中的第几个 item  把  返回来的status 更新到响应的  item上
								int proxyHistoryNowNum = Integer.valueOf(queueproxyHistoryItemTmpTmp[0]);
								if (HttpResponseHeaderStatus > 0)
								{
									queueproxyHistoryItemTmpTmp[4] = String.valueOf(HttpResponseHeaderStatus);//【4】 位置是存放  response status
									tableModel.setValueAt(Integer.valueOf(HttpResponseHeaderStatus), proxyHistoryNowNum - 1, 4);
									ProxyHistoryListTable.invalidate();
								}
							}
						});
						InterceptForwardThread.start();
					
						//如果队列里 还有  序号 则继续   先把  队列里 可以放行的 所有 http请求 先取出来    因为后续如果按到截断  才不会连后面的都给直接发送了
						
						//定义三个临时队列
						
						Queue<Integer> queueHTTPSSocketInterceptIsOff = new LinkedList();
						Queue<OutputStream> queueOutInterceptIsOff = new LinkedList();
						Queue<String[]> queueproxyHistoryItemInterceptIsOff = new LinkedList();
						Integer queueHTTPSSocketInterceptIsOffTmp;
						//System.out.println("start");
						
						
						
						//取出数据
						try
						{
							while((queueHTTPSSocketInterceptIsOffTmp = (Integer)queueHTTPSSocket.poll()) != null)
							{								
								queueHTTPSSocketInterceptIsOff.offer(queueHTTPSSocketInterceptIsOffTmp);
								queueOutInterceptIsOff.offer(queueOut.poll());
								queueproxyHistoryItemInterceptIsOff.offer(queueproxyHistoryItem.poll());
							}
						}
						catch(Exception es)
						{
							es.printStackTrace();
						}
					
						
						Integer forwardHTTPSScoketTmpTmp=0;
						
						//forward 所有数据 
						while((forwardHTTPSScoketTmpTmp = queueHTTPSSocketInterceptIsOff.poll())!=null)
						{
							
							final Integer forwardHTTPSScoketTmp = forwardHTTPSScoketTmpTmp;
							final OutputStream forwardOutTmp  = queueOutInterceptIsOff.poll();
							final  String[] forwardItemTmp = queueproxyHistoryItemInterceptIsOff.poll();
							
							
							final DefaultTableModel ForwardtableModel = (DefaultTableModel)ProxyHistoryListTable.getModel();
							
							if (forwardHTTPSScoketTmp != null)
							{
								Thread InterceptForwardThreadTmp = new Thread(new Runnable() //定义一个新的线程来出来forward 
								{
									public void run()
									{
										String host = forwardItemTmp[1]; //获取目标主机地址 
										int port = Integer.valueOf(forwardItemTmp[7]);//获取目标端口
										String StrBuffer = forwardItemTmp[5];//获取 完整的http请求数据报文
										
										boolean HttpsSocketBoolean = false;
										if (forwardHTTPSScoketTmp == 1) //1 说明使用 https 
										{
											HttpsSocketBoolean = true;
										}
										
										//获取http 请求的 返回的 status  随后更新table 
										int HttpResponseHeaderStatus = Method.getHttPResponseData(HttpsSocketBoolean, host, port, StrBuffer, forwardOutTmp, forwardItemTmp);
										//【0】 位置 是记录当前是table 表格中的第几个 item  把  返回来的status 更新到响应的  item上
										int proxyHistoryNowNum = Integer.valueOf(forwardItemTmp[0]);
										if (HttpResponseHeaderStatus > 0)
										{
											forwardItemTmp[4] = String.valueOf(HttpResponseHeaderStatus);//【4】 位置是存放  response status
											tableModel.setValueAt(Integer.valueOf(HttpResponseHeaderStatus), proxyHistoryNowNum - 1, 4);
											ProxyHistoryListTable.invalidate();
										}
									}
								});
								InterceptForwardThreadTmp.start();
							}					
						}
					}
					
					//--------------------放开就是forward--------------------------------------
					
				}
			}
		});
		
		//Proxy Intercept Raw Headers Hex  Send To Editor  面板
		JPanel InterceptDataPane = new JPanel();
		InterceptDataPane.setLayout(GBL);
		GridBagConstraints GBCIDP = new GridBagConstraints();
		GBCIDP.fill = 1;
		GBCIDP.insets = new Insets(5, 5, 5, 5);
		GBCIDP.gridx = 0;
		GBCIDP.gridy = 4;
		GBCIDP.gridwidth = 1;
		GBCIDP.gridheight = 16;
		GBCIDP.weightx = 1.0D;
		GBCIDP.weighty = 16.0D;
		InterceptMainPane.add(InterceptDataPane, GBCIDP);

		//Proxy Intercept Raw Headers Hex  面板
		JPanel InterceptDataShowPane = new JPanel();
		InterceptDataShowPane.setLayout(new BorderLayout());
		GridBagConstraints GBCIDSP = new GridBagConstraints();
		GBCIDSP.fill = 1;
		GBCIDSP.insets = new Insets(5, 5, 5, 5);
		GBCIDSP.gridx = 0;
		GBCIDSP.gridy = 0;
		GBCIDSP.gridwidth = 20;
		GBCIDSP.gridheight = 1;
		GBCIDSP.weightx = 20.0D;
		GBCIDSP.weighty = 1.0D;
		InterceptDataPane.add(InterceptDataShowPane, GBCIDSP);

		//proxy Intercept send to editor 面板
		JPanel InterceptDataEditPane = new JPanel();
		InterceptDataEditPane.setLayout(GBL);
		GridBagConstraints GBCIDEP = new GridBagConstraints();
		GBCIDEP.fill = 1;
		GBCIDEP.insets = new Insets(5, 5, 5, 5);
		GBCIDEP.gridx = 20;
		GBCIDEP.gridy = 0;
		GBCIDEP.gridwidth = 1;
		GBCIDEP.gridheight = 1;
		GBCIDEP.weightx = 1.0D;
		GBCIDEP.weighty = 1.0D;
	    InterceptDataPane.add(InterceptDataEditPane, GBCIDEP);

	    //为了调整位置 填充的空面板
	    JPanel InterceptDataEditNullPane = new JPanel();
	    GridBagConstraints GBCIDENP = new GridBagConstraints();
	    GBCIDENP.fill = 1;
	    GBCIDENP.insets = new Insets(5, 5, 5, 5);
	    GBCIDENP.gridx = 0;
	    GBCIDENP.gridy = 0;
	    GBCIDENP.gridwidth = 1;
	    GBCIDENP.gridheight = 16;
	    GBCIDENP.weightx = 1.0D;
	    GBCIDENP.weighty = 16.0D;
	    InterceptDataEditPane.add(InterceptDataEditNullPane, GBCIDENP);

	    //Proxy Intercept Send To editor Button 面板
	    JPanel SendToEdtiroButtonPane = new JPanel();
	    GridBagConstraints GBCSTEBP = new GridBagConstraints();
	    GBCSTEBP.fill = 1;
	    GBCSTEBP.insets = new Insets(5, 5, 5, 5);
	    GBCSTEBP.gridx = 0;
	    GBCSTEBP.gridy = 16;
	    GBCSTEBP.gridwidth = 1;
	    GBCSTEBP.gridheight = 1;
	    GBCSTEBP.weightx = 1.0D;
	    GBCSTEBP.weighty = 1.0D;
	    InterceptDataEditPane.add(SendToEdtiroButtonPane, GBCSTEBP);

	    //Proxy Intercept Send To editor Button
	    SendToEditorButton = new JButton("Send To Editor");
	    SendToEditorButton.setFocusable(false);
	    SendToEditorButton.setSize(120, 28);
	    SendToEditorButton.setEnabled(false);
	    SendToEdtiroButtonPane.add(SendToEditorButton);

	    
	    SendToEditorButton.addActionListener(new ActionListener()
	    {
	    	public void actionPerformed(ActionEvent e)
	    	{
	    		//String tmp = "GET /img/bd_logo1.png HTTP/1.1\r\nHost: www.baidu.com\r\nProxy-Connection: keep-alive\r\nCache-Control: max-age=0\r\nAccept: image/webp,*/*;q=0.8\r\nIf-None-Match: \"1ec5-502264e2ae4c0\"\r\nIf-Modified-Since: Wed, 03 Sep 2014 10:00:27 GMT\r\nUser-Agent: Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.131 Safari/537.36\r\nReferer: http://www.baidu.com/\r\n\r\n";

	    		//从raw headers hex 数据中心获取 当前截断的 http请求的数据包
	    		String[] queueproxyHistoryItemTmpTmp = queueproxyHistoryItemTmp;

	    		//添加到 Edtior 面板处    参数 Tab JPtabEditorPanel        、host                       port                                   http request
	    		//没有返回值
	    		AddTabPanel(JPtabEditorPanel, queueproxyHistoryItemTmpTmp[1], Integer.valueOf(queueproxyHistoryItemTmpTmp[7]), queueproxyHistoryItemTmpTmp[5]);
	    		//启动一个线程 修改 主Tab 上  Editor Label 的颜色 ，提醒 已经 send 过去了
	    		Thread setFontThread = new Thread(new Runnable()
	    		{
	    			public void run()
	    			{
	    				JLabel labelTmp = new JLabel("Editor");
	    				labelTmp.setForeground(Color.red);
	    				JPtab.setTabComponentAt(1, labelTmp);
	    				try
	    				{
	    					Thread.sleep(3000);
	    				}
	    				catch (Exception e)
	    				{
	    					e.printStackTrace();
	    				}
	    				labelTmp.setForeground(null);
	    				JPtab.setTabComponentAt(1, labelTmp);
	    			}
	    		});
	    		setFontThread.start();
	    	}
	    });
	    
	    //填充面板
	    JPanel InterceptDataEditNull2Pane = new JPanel();
	    GridBagConstraints GBCIDEN2P = new GridBagConstraints();
	    GBCIDEN2P.fill = 1;
	    GBCIDEN2P.insets = new Insets(5, 5, 5, 5);
	    GBCIDEN2P.gridx = 0;
	    GBCIDEN2P.gridy = 17;
	    GBCIDEN2P.gridwidth = 1;
	    GBCIDEN2P.gridheight = 16;
	    GBCIDEN2P.weightx = 1.0D;
	    GBCIDEN2P.weighty = 16.0D;
	    InterceptDataEditPane.add(InterceptDataEditNull2Pane, GBCIDEN2P);
	    
	    JTabbedPane JPtabInterceptData = new JTabbedPane(1);// 这个tab 用来承载 Proxy Intercept 的 Raw Headers Hex 
	    InterceptDataShowPane.add(JPtabInterceptData, "Center");
	    //这个 raw headers hex 相互切换的时候 触发这个
	    JPtabInterceptData.addChangeListener(new ChangeListener()
	    {
	    	public void stateChanged(ChangeEvent e)
	    	{
	    		//JTabbedPane tabbedPane = (JTabbedPane)e.getSource();
	    		//int selectedIndex = tabbedPane.getSelectedIndex();
	    		//System.out.println(selectedIndex+".............");
	    		//更新函数
	    		InterceptShowRawHeadersHexUpdate();
	    	}
	    });
	    
	    //Proxy Intercept raw 的Desktop 
	    JDesktopPane InterceptDataRawPane = new JDesktopPane();
	    InterceptDataRawPane.setFocusable(false);
	    InterceptDataRawPane.setLayout(new BorderLayout());
	    JPtabInterceptData.add(InterceptDataRawPane, "Raw");

	    //初始化  Proxy Intercept raw 的 TextArea
	    InterceptDaraRawTextArea = new JTextArea();
	    InterceptDaraRawTextArea.setEditable(false);
	    InterceptDaraRawTextArea.setLineWrap(true);
	    JScrollPane InterceptDaraRawScrollPane = new JScrollPane(InterceptDaraRawTextArea);
	    InterceptDataRawPane.add(InterceptDaraRawScrollPane, "Center");

	    //Proxy Intercept raw TextArea  监听
	    InterceptDaraRawTextArea.getDocument().addDocumentListener(new DocumentListener()
	    {
	    	//删除操作触发  
	    	public void removeUpdate(DocumentEvent e)
	    	{
	    		InterceptShowString = InterceptDaraRawTextArea.getText(); // 每次修改都需要把修改后的字符串 上传的 数据中心  InterceptShowString
	    		if (InterceptShowString.length() == 0)
	    		{
	    			ForwardButton.setEnabled(false);
	    			DropButton.setEnabled(false);
	    			SendToEditorButton.setEnabled(false);
	    		}
	    	}

	    	//添加操作触发
	    	public void insertUpdate(DocumentEvent e)
	    	{
	    		ForwardButton.setEnabled(true);
	    		DropButton.setEnabled(true);
	    		SendToEditorButton.setEnabled(true);
	    		InterceptShowString = InterceptDaraRawTextArea.getText();
	    		InterceptShowRawHeadersHexUpdate();
	    		
	    	}

	    	public void changedUpdate(DocumentEvent e)
	    	{
	    	}
	    });
	    
	    //Proxy Intercept Headers 的Desktop
	    JDesktopPane InterceptDataHeaders = new JDesktopPane();
	    InterceptDataHeaders.setFocusable(false);
	    InterceptDataHeaders.setLayout(new BorderLayout());
	    JPtabInterceptData.add(InterceptDataHeaders, "Headers");
	    
	    //Proxy Intercept Headers 的Desktop 上面的  主面板
	    JPanel InterceptDataHeadersDataShowPane = new JPanel();
	    InterceptDataHeadersDataShowPane.setBackground(Color.gray);
	    InterceptDataHeadersDataShowPane.setLayout(new BorderLayout());
	    GridBagConstraints GBCIDHDSP = new GridBagConstraints();
	    GBCIDHDSP.fill = 1;
	    GBCIDHDSP.insets = new Insets(5, 5, 5, 5);
	    GBCIDHDSP.gridx = 0;
	    GBCIDHDSP.gridy = 0;
	    GBCIDHDSP.gridwidth = 30;
	    GBCIDHDSP.gridheight = 1;
	    GBCIDHDSP.weightx = 30.0D;
	    GBCIDHDSP.weighty = 1.0D;
	    InterceptDataHeaders.add(InterceptDataHeadersDataShowPane, "Center");

	    //Proxy Intercept Headers 的 Table 表格
	    String[] JPtabInterceptDataShowHeadersTableHeaders = { "Name", "Value" };
	    Object[][] JPtabInterceptDataShowHeadersTableCellData = null;
	    DefaultTableModel JPtabInterceptDataShowHeadersTableModel = new DefaultTableModel(JPtabInterceptDataShowHeadersTableCellData, JPtabInterceptDataShowHeadersTableHeaders) {
	    	public boolean isCellEditable(int row, int column) 
	    	{
	    		return true;
	    	}
	    };
	    JPtabInterceptDataShowHeadersTable = new JTable(JPtabInterceptDataShowHeadersTableModel);
	    
	    //监听事件
	    JPtabInterceptDataShowHeadersTable.getModel().addTableModelListener(new TableModelListener()
	    {
	    	public void tableChanged(TableModelEvent e)
	    	{
	    		if (e.getType() == 0)
	    		{
	    			TableModel tableModelTmp = JPtabInterceptDataShowHeadersTable.getModel();
	    			int tableRowCount = tableModelTmp.getRowCount();
	    			
	    			String interceptDataStingStmp = "";
	    			if (tableRowCount > 0)
	    			{
	    				interceptDataStingStmp = interceptDataStingStmp + tableModelTmp.getValueAt(0, 0) + " " + tableModelTmp.getValueAt(0, 1) + "\r\n";
	    				for (int i = 1; i < tableRowCount; i++)
	    				{
	    					interceptDataStingStmp = interceptDataStingStmp + tableModelTmp.getValueAt(i, 0) + ": " + tableModelTmp.getValueAt(i, 1) + "\r\n";
	    				}
	    				InterceptShowString = interceptDataStingStmp;// 修改后的数据 更新到数据中心
	    				InterceptDaraRawTextArea.setText(InterceptShowString); //更新raw   而 更新raw 的同时  又会触发更新  InterceptShowRawHeadersHexUpdate 就是raw headers hex 都更新了
	    				InterceptDaraRawTextArea.setSelectionStart(1);
						InterceptDaraRawTextArea.setSelectionEnd(1);
	    				
	    			}
	    		}
	    	}
	    });
	    JScrollPane JPtabInterceptDataShowHeadersTableScollPanel = new JScrollPane(JPtabInterceptDataShowHeadersTable);
	    InterceptDataHeadersDataShowPane.add(JPtabInterceptDataShowHeadersTableScollPanel, "Center");

	  //Proxy Intercept Headers  右侧的按钮面板
	  JPanel InterceptDataHeadersDataButtonPane = new JPanel();
 	  InterceptDataHeadersDataButtonPane.setBackground(Color.blue);
 	  InterceptDataHeadersDataButtonPane.setPreferredSize(new Dimension(120, 0));
 	  InterceptDataHeadersDataButtonPane.setLayout(null);
 	  GridBagConstraints GBCIDHDBP = new GridBagConstraints();
 	  GBCIDHDBP.fill = 1;
 	  GBCIDHDBP.insets = new Insets(5, 5, 5, 5);
 	  GBCIDHDBP.gridx = 30;
 	  GBCIDHDBP.gridy = 0;
 	  GBCIDHDBP.gridwidth = 1;
 	  GBCIDHDBP.gridheight = 1;
 	  GBCIDHDBP.weightx = 1.0D;
 	  GBCIDHDBP.weighty = 1.0D;
 	  //InterceptDataHeaders.add(InterceptDataHeadersDataButtonPane, "East");

 	  //Proxy Intercept Headers Add BUtton
 	  JButton InterceptDataHeadersDataAddButton = new JButton("Add");
 	  GridBagConstraints GBCIDHDAB = new GridBagConstraints();
 	  InterceptDataHeadersDataAddButton.setPreferredSize(new Dimension(130, 28));
 	  InterceptDataHeadersDataAddButton.setLocation(0, 10);
 	  GBCIDHDAB.fill = 1;
 	  GBCIDHDAB.insets = new Insets(5, 5, 5, 5);
 	  GBCIDHDAB.gridx = 0;
 	  GBCIDHDAB.gridy = 0;
 	  GBCIDHDAB.gridwidth = 1;
 	  GBCIDHDAB.gridheight = 1;
 	  GBCIDHDAB.weightx = 1.0D;
 	  GBCIDHDAB.weighty = 1.0D;
 	  InterceptDataHeadersDataAddButton.setFocusable(false);
 	  InterceptDataHeadersDataAddButton.setSize(100, 30);
 	  InterceptDataHeadersDataButtonPane.add(InterceptDataHeadersDataAddButton);

 	  //Proxy Intercept Headers remove BUtton
 	  JButton InterceptDataHeadersDataRemoveButton = new JButton("Remove");
 	  InterceptDataHeadersDataRemoveButton.setPreferredSize(new Dimension(130, 28));
 	  InterceptDataHeadersDataRemoveButton.setLocation(0, 50);
 	  GridBagConstraints GBCIDHDRB = new GridBagConstraints();
 	  GBCIDHDRB.fill = 1;
 	  GBCIDHDRB.insets = new Insets(5, 5, 5, 5);
 	  GBCIDHDRB.gridx = 0;
 	  GBCIDHDRB.gridy = 1;
 	  GBCIDHDRB.gridwidth = 1;
 	  GBCIDHDRB.gridheight = 1;
 	  GBCIDHDRB.weightx = 1.0D;
 	  GBCIDHDRB.weighty = 1.0D;
 	  InterceptDataHeadersDataRemoveButton.setFocusable(false);
 	  InterceptDataHeadersDataRemoveButton.setSize(100, 30);
 	  InterceptDataHeadersDataButtonPane.add(InterceptDataHeadersDataRemoveButton);

 	  //Proxy Intercept Headers Up BUtton
 	  JButton InterceptDataHeadersDataUpButton = new JButton("Up");
 	  InterceptDataHeadersDataUpButton.setPreferredSize(new Dimension(130, 28));
 	  InterceptDataHeadersDataUpButton.setLocation(0, 90);
 	  GridBagConstraints GBCIDHDUB = new GridBagConstraints();
 	  GBCIDHDUB.fill = 1;
 	  GBCIDHDUB.insets = new Insets(5, 5, 5, 5);
 	  GBCIDHDUB.gridx = 0;
 	  GBCIDHDUB.gridy = 2;
 	  GBCIDHDUB.gridwidth = 1;
 	  GBCIDHDUB.gridheight = 1;
 	  GBCIDHDUB.weightx = 1.0D;
 	  GBCIDHDUB.weighty = 1.0D;
 	  InterceptDataHeadersDataUpButton.setFocusable(false);
 	  InterceptDataHeadersDataUpButton.setSize(100, 30);
 	  InterceptDataHeadersDataButtonPane.add(InterceptDataHeadersDataUpButton);

 	  //Proxy Intercept Headers down BUtton
 	  JButton InterceptDataHeadersDataDownButton = new JButton("Down");
 	  InterceptDataHeadersDataDownButton.setPreferredSize(new Dimension(130, 28));
 	  InterceptDataHeadersDataDownButton.setLocation(0, 130);
 	  GridBagConstraints GBCIDHDDB = new GridBagConstraints();
 	  GBCIDHDDB.fill = 1;
 	  GBCIDHDDB.insets = new Insets(5, 5, 5, 5);
 	  GBCIDHDDB.gridx = 0;
 	  GBCIDHDDB.gridy = 3;
 	  GBCIDHDDB.gridwidth = 1;
 	  GBCIDHDDB.gridheight = 1;
 	  GBCIDHDDB.weightx = 1.0D;
 	  GBCIDHDDB.weighty = 1.0D;
 	  InterceptDataHeadersDataDownButton.setFocusable(false);
 	  InterceptDataHeadersDataDownButton.setSize(100, 30);
 	  InterceptDataHeadersDataButtonPane.add(InterceptDataHeadersDataDownButton);

 	  //填充面板
 	  JPanel InterceptDataHeadersDataEditNull = new JPanel();
 	  GridBagConstraints GBCIDHDEN = new GridBagConstraints();
 	  GBCIDHDEN.fill = 1;
 	  GBCIDHDEN.insets = new Insets(5, 5, 5, 5);
 	  GBCIDHDEN.gridx = 0;
 	  GBCIDHDEN.gridy = 4;
 	  GBCIDHDEN.gridwidth = 1;
 	  GBCIDHDEN.gridheight = 10;
 	  GBCIDHDEN.weightx = 1.0D;
 	  GBCIDHDEN.weighty = 10.0D;
 	  InterceptDataHeadersDataButtonPane.add(InterceptDataHeadersDataEditNull, GBCIDHDEN);

 	  //Proxy Intercept Hex 的 Desktop
 	  JDesktopPane InterceptDataHex = new JDesktopPane();
 	  InterceptDataHex.setFocusable(false);
 	  InterceptDataHex.setLayout(new BorderLayout());
 	  JPtabInterceptData.add(InterceptDataHex, "Hex");

 	  //Proxy Intercept Hex 的 table 
 	  String[] JPtabProxyInterceptDataHexTableHeaders = { "Num", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "String" };
 	  Object[][] JPtabProxyInterceptDataHexTableCellData = null;
 	  DefaultTableModel JPtabProxyInterceptDataHexTableModel = new DefaultTableModel(JPtabProxyInterceptDataHexTableCellData, JPtabProxyInterceptDataHexTableHeaders) {
 		  public boolean isCellEditable(int row, int column) {
 			  return true;
 		  }
 	  };
 	  JPtabProxyInterceptDataHexTable = new JTable(JPtabProxyInterceptDataHexTableModel)
 	  {
 		  //设置第一列 和第18列 不能编辑
 		  public boolean isCellEditable(int row, int column)
 		  {
 			  if ((column == 0) || (column == 17))
 				  return false;
 			  return true;
 		  }
 	  };
 	  JPtabProxyInterceptDataHexTable.getTableHeader().setVisible(false);//隐藏table 的表头
 	  JPtabProxyInterceptDataHexTable.setCellSelectionEnabled(false);
 	  //修改后更新 
 	  JPtabProxyInterceptDataHexTable.getModel().addTableModelListener(new TableModelListener()
 	  {
 		  public void tableChanged(TableModelEvent e)
 		  {
 			  if (e.getType() == 0)
 			  {
 				  String newvalue = JPtabProxyInterceptDataHexTable.getValueAt(e.getLastRow(), e.getColumn()).toString();				  
 				  String[] HttpRequestDataHexStrings = Method.bytesToHexStrings(InterceptShowString.getBytes());
 				  HttpRequestDataHexStrings[(e.getLastRow() * 16 + e.getColumn() - 1)] = newvalue;
 				  InterceptShowString =Method.bytesToString(Method.HexStringTobytes(Method.stringsToString(HttpRequestDataHexStrings)));
 				  InterceptShowRawHeadersHexUpdate();
 				  InterceptDaraRawTextArea.setText(InterceptShowString);
 				 InterceptDaraRawTextArea.setSelectionStart(1);
					InterceptDaraRawTextArea.setSelectionEnd(1);
 			  }
 		  }
 	  });
 	  //设置第一行 和第十八行的  行宽
 	  TableColumn JPtabProxyInterceptDataHexTableFirstColumn = JPtabProxyInterceptDataHexTable.getColumnModel().getColumn(0);
 	  JPtabProxyInterceptDataHexTableFirstColumn.setPreferredWidth(120);

 	  TableColumn JPtabProxyInterceptDataHexTableStringColumn = JPtabProxyInterceptDataHexTable.getColumnModel().getColumn(17);
 	  JPtabProxyInterceptDataHexTableStringColumn.setPreferredWidth(200);

 	  JScrollPane JPtabProxyInterceptDataHexScrollPanel = new JScrollPane(JPtabProxyInterceptDataHexTable);
 	  InterceptDataHex.add(JPtabProxyInterceptDataHexScrollPanel, "Center");

 	  //Proxy History 的历史数据面板  用来承载  table 
 	  JPanel ProxyHistoryListPanel = new JPanel();
 	  ProxyHistoryListPanel.setBackground(Color.yellow);
 	  ProxyHistoryListPanel.setPreferredSize(new Dimension(0, 200));
 	  ProxyHistoryListPanel.setLayout(new BorderLayout());

 	  //Proxy History 的历史数据的 详细Item 数据     request response
 	  JPanel ProxyHistoryDetailPanl = new JPanel();
 	  ProxyHistoryDetailPanl.setBackground(Color.gray);
 	  ProxyHistoryDetailPanl.setLayout(new BorderLayout());

 	  JSplitPane SplitHistoryPanel = new JSplitPane(0, true, ProxyHistoryListPanel, ProxyHistoryDetailPanl);// 可以改变大小的 面板
 	  JDPHistory.add(SplitHistoryPanel, "Center");

 	  //Proxy History Table 
 	  String[] headers = { "#", "Host", "Method", "URL", "Status" };
 	  Object[][] cellData = null;
 	  DefaultTableModel model = new DefaultTableModel(cellData, headers) {
 		  public boolean isCellEditable(int row, int column) {
 			  return true;
 		  }
 	  };
 	  ProxyHistoryListTable = new JTable(model);

 	  //设置行宽
 	  TableColumn firsetColumn = ProxyHistoryListTable.getColumnModel().getColumn(0);
 	  firsetColumn.setPreferredWidth(3);

 	  TableColumn secondColumn = ProxyHistoryListTable.getColumnModel().getColumn(1);
 	  secondColumn.setPreferredWidth(220);

 	  TableColumn thirdColumn = ProxyHistoryListTable.getColumnModel().getColumn(2);
 	  thirdColumn.setPreferredWidth(20);

 	  TableColumn fourColumn = ProxyHistoryListTable.getColumnModel().getColumn(3);
 	  fourColumn.setPreferredWidth(700);

 	  TableColumn fiveColumn = ProxyHistoryListTable.getColumnModel().getColumn(4);
 	  
 	  ProxyHistoryListTable.addMouseListener(new MouseListener()
 	  {
 		  public void mouseReleased(MouseEvent e)
 		  {
 		  }

 		  public void mousePressed(MouseEvent e)
 		  {
 		  }

 		  public void mouseExited(MouseEvent e)
 		  {
 		  }

 		  public void mouseEntered(MouseEvent e)
 		  {
 		  }

 		  public void mouseClicked(MouseEvent e)
 		  {
 			  int sr;// 获取选中的行数，如果没有选中 就返回
 			  if ((sr = ProxyHistoryListTable.getSelectedRow()) == -1)
 			  {
 				//  System.out.println("乱来的");
 				  return;
 			  }

 			  //获取选中行的数据
 			  String[] tmp = (String[])proxyHistoryList.get(ProxyHistoryListTable.getSelectedRow());

 			  //第八行 为  如果前面截断的时候 有修改源数据包   则在这边会多一个面板出来 用来显示 修改钱 和修改后的 不用数据
 			  if (tmp[8] == null)
 			//	  System.out.println("tmp[8] kolng");
 			  if ((tmp[8] != null) && (tmp[8].length() > 0))
 			  {
 				  //初始化
 				  JPtabProxyHistoryDetail.remove(JPtabProxyHistoryDetailEditedRequestPanel);
 				  //
 				  JPtabProxyHistoryDetailEditedRequestPanel = new JDesktopPane();
 				  JPtabProxyHistoryDetailEditedRequestPanel.setLayout(new BorderLayout());
 				  JPtabProxyHistoryDetail.add(JPtabProxyHistoryDetailEditedRequestPanel, "Edited Request");

 				  //Proxy History Edited Request Tab 面板
 				  JTabbedPane JPtabProxyHistoryDetailEditedRequest = new JTabbedPane(1);
 				  JPtabProxyHistoryDetailEditedRequest.setFocusable(false);
 				  JPtabProxyHistoryDetailEditedRequest.setForeground(new Color(Integer.decode("#4B4B4B").intValue()));
 				  JPtabProxyHistoryDetailEditedRequestPanel.add(JPtabProxyHistoryDetailEditedRequest, "Center");

 				  //Proxy History Edited Request Tab 上面板的  raw  
 				  JDesktopPane JPtabProxyHistoryDetailEditedRequestRawPanel = new JDesktopPane();
 				  JPtabProxyHistoryDetailEditedRequestRawPanel.setBackground(Color.black);
 				  JTextArea JPtabProxyHistoryDetailEditedRequestRawText = new JTextArea();
 				  JPtabProxyHistoryDetailEditedRequestRawText.setEditable(false);

 				  //显示数据
 				  JPtabProxyHistoryDetailEditedRequestRawText.setText(tmp[8]);
 				  JScrollPane JPtabProxyHistoryDetailEditedRequestRawScollPanel = new JScrollPane(JPtabProxyHistoryDetailEditedRequestRawText);
 				  JPtabProxyHistoryDetailEditedRequestRawPanel.setLayout(new BorderLayout());
 				  JPtabProxyHistoryDetailEditedRequestRawPanel.add(JPtabProxyHistoryDetailEditedRequestRawScollPanel, "Center");

 				  JPtabProxyHistoryDetailEditedRequest.add(JPtabProxyHistoryDetailEditedRequestRawPanel, "Raw");

 				  JDesktopPane JPtabProxyHistoryDetailEditedRequestHeadersPanel = new JDesktopPane();
 				  JPtabProxyHistoryDetailEditedRequestHeadersPanel.setLayout(new BorderLayout());

 				  ///Proxy History Edited Request Tab Headers Table 
 				  String[] JPtabProxyHistoryDetailEditedRequestHeadersTableHeaders = { "Name", "Value" };
 				  Object[][] JPtabProxyHistoryDetailEditedRequestHeadersTableCellData = null;
 				  DefaultTableModel JPtabProxyHistoryDetailEditedRequestHeadersTableModel = new DefaultTableModel(JPtabProxyHistoryDetailEditedRequestHeadersTableCellData, JPtabProxyHistoryDetailEditedRequestHeadersTableHeaders) {
 					  public boolean isCellEditable(int row, int column) {
 						  return true;
 					  }
 				  };
 				  JTable JPtabProxyHistoryDetailEditedRequestHeadersTable = new JTable(JPtabProxyHistoryDetailEditedRequestHeadersTableModel);

 				  DefaultTableModel tableModel = (DefaultTableModel)JPtabProxyHistoryDetailEditedRequestHeadersTable.getModel();
 				  tableModel.setRowCount(0);
 				  String[] headerParams = Method.getHTTPHeadersParams(tmp[8]);
 				  int headerParamLength = headerParams.length / 2;
 				  for (int i = 0; i < headerParamLength; i++)
 				  {
 					  tableModel.addRow(new Object[] { headerParams[(i * 2)], headerParams[(i * 2 + 1)] });
 				  }
 				  JPtabProxyHistoryDetailEditedRequestHeadersTable.invalidate();

 				  JScrollPane JPtabProxyHistoryDetailEditedRequestHeadersTableScollPanel = new JScrollPane(JPtabProxyHistoryDetailEditedRequestHeadersTable);
 				  JPtabProxyHistoryDetailEditedRequestHeadersPanel.add(JPtabProxyHistoryDetailEditedRequestHeadersTableScollPanel, "Center");

 				  JPtabProxyHistoryDetailEditedRequest.add(JPtabProxyHistoryDetailEditedRequestHeadersPanel, "Headers");

 				  JDesktopPane JPtabProxyHistoryDetailEditedRequestHexPanel = new JDesktopPane();
 				  JPtabProxyHistoryDetailEditedRequest.add(JPtabProxyHistoryDetailEditedRequestHexPanel, "Hex");
 				  JPtabProxyHistoryDetailEditedRequestHexPanel.setLayout(new BorderLayout());

 				  String[] JPtabProxyHistoryDetailEditedRequestHexTableHeaders = { "Num", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "String" };
 				  Object[][] JPtabProxyHistoryDetailEditedRequestHexTableCellData = null;
 				  DefaultTableModel JPtabProxyHistoryDetailEditedRequestHexTableModel = new DefaultTableModel(JPtabProxyHistoryDetailEditedRequestHexTableCellData, JPtabProxyHistoryDetailEditedRequestHexTableHeaders) {
 					  public boolean isCellEditable(int row, int column) {
 						  return true;
 					  }
 				  };
 				  JTable JPtabProxyHistoryDetailEditedRequestHexTable = new JTable(JPtabProxyHistoryDetailEditedRequestHexTableModel) { public boolean isCellEditable(int row, int column) { return false; }

 				  };
 				  JPtabProxyHistoryDetailEditedRequestHexTable.getTableHeader().setVisible(false);
 				  JPtabProxyHistoryDetailEditedRequestHexTable.setCellSelectionEnabled(false);

 				  TableColumn JPtabProxyHistoryDetailEditedRequestHexTableFirstColumn = JPtabProxyHistoryDetailEditedRequestHexTable.getColumnModel().getColumn(0);
 				  JPtabProxyHistoryDetailEditedRequestHexTableFirstColumn.setPreferredWidth(120);

 				  TableColumn JPtabProxyHistoryDetailEditedRequestHexTableStringColumn = JPtabProxyHistoryDetailEditedRequestHexTable.getColumnModel().getColumn(17);
 				  JPtabProxyHistoryDetailEditedRequestHexTableStringColumn.setPreferredWidth(200);

 				  JScrollPane JPtabProxyHistoryDetailEditedRequestHexScrollPanel = new JScrollPane(JPtabProxyHistoryDetailEditedRequestHexTable);
 				  JPtabProxyHistoryDetailEditedRequestHexPanel.add(JPtabProxyHistoryDetailEditedRequestHexScrollPanel, "Center");

 				  String[] HttpEditedRequestDataHexStrings = Method.bytesToHexStrings(tmp[8].getBytes());
 				  int HttpEditedRequestDataHexStringsLength = HttpEditedRequestDataHexStrings.length;
 				  int HttpEditedRequestDataHexStringsRowNum = HttpEditedRequestDataHexStringsLength / 16;
 				  int HttpEditedRequestDataHexStringsRealRowNum = 0;
 				  if (HttpEditedRequestDataHexStringsLength % 16 == 0)
 				  {
 					  HttpEditedRequestDataHexStringsRealRowNum = HttpEditedRequestDataHexStringsRowNum;
 				  }
 				  else
 				  {
 					  HttpEditedRequestDataHexStringsRealRowNum = HttpEditedRequestDataHexStringsRowNum + 1;
 				  }

 				  String[] HttpEditedRequestDataHexStringsRealRowStrings = new String[HttpEditedRequestDataHexStringsRealRowNum];
 				  String HttpEditedRequestDataHexStringsRealRowStringTmp = "";
 				  char[] EditedRequestStringchars = tmp[8].toCharArray();
 				  int EditedRequestStringcharsLength = EditedRequestStringchars.length;
 				  for (int j = 0,K=0; (K <= EditedRequestStringcharsLength) && (j < HttpEditedRequestDataHexStringsRealRowNum); K++)
 				  {
 					  HttpEditedRequestDataHexStringsRealRowStringTmp = HttpEditedRequestDataHexStringsRealRowStringTmp + EditedRequestStringchars[(K - 1)];
 					  
 					  if (K % 16 == 0)
 					  {
 						  HttpEditedRequestDataHexStringsRealRowStrings[j] = HttpEditedRequestDataHexStringsRealRowStringTmp;
 						  HttpEditedRequestDataHexStringsRealRowStringTmp = "";
 						  j++;
 					  }

 				  }

 				  HttpEditedRequestDataHexStringsRealRowStrings[(HttpEditedRequestDataHexStringsRealRowNum - 1)] = HttpEditedRequestDataHexStringsRealRowStringTmp;

 				  DefaultTableModel JPtabProxyHistoryDetailEditedRequestHexTableModelAdd = (DefaultTableModel)JPtabProxyHistoryDetailEditedRequestHexTable.getModel();
 				  JPtabProxyHistoryDetailEditedRequestHexTableModelAdd.setRowCount(0);
 				 
 				  for (int k = 0; k < HttpEditedRequestDataHexStringsRowNum; k++)
 				  {
 					  JPtabProxyHistoryDetailEditedRequestHexTableModelAdd.addRow(new Object[] { Integer.valueOf(k + 1), HttpEditedRequestDataHexStrings[(k * 16)], HttpEditedRequestDataHexStrings[(k * 16 + 1)], HttpEditedRequestDataHexStrings[(k * 16 + 2)], HttpEditedRequestDataHexStrings[(k * 16 + 3)], HttpEditedRequestDataHexStrings[(k * 16 + 4)], HttpEditedRequestDataHexStrings[(k * 16 + 5)], HttpEditedRequestDataHexStrings[(k * 16 + 6)], HttpEditedRequestDataHexStrings[(k * 16 + 7)], HttpEditedRequestDataHexStrings[(k * 16 + 8)], HttpEditedRequestDataHexStrings[(k * 16 + 9)], HttpEditedRequestDataHexStrings[(k * 16 + 10)], HttpEditedRequestDataHexStrings[(k * 16 + 11)], HttpEditedRequestDataHexStrings[(k * 16 + 12)], HttpEditedRequestDataHexStrings[(k * 16 + 13)], HttpEditedRequestDataHexStrings[(k * 16 + 14)], HttpEditedRequestDataHexStrings[(k * 16 + 15)], HttpEditedRequestDataHexStringsRealRowStrings[k] });
 				  }

 				  String[] EditedRequestHexStringsTmp = { String.valueOf(HttpEditedRequestDataHexStringsRealRowNum), "--", "--", "--", "--", "--", "--", "--", "--", "--", "--", "--", "--", "--", "--", "--", "--", HttpEditedRequestDataHexStringsRealRowStrings[(HttpEditedRequestDataHexStringsRealRowNum - 1)] };
 				  int HttpEditedRequestDataHexStringsSHENGYUGESHU = HttpEditedRequestDataHexStringsLength % 16;
 				  for (int k = 0; k < HttpEditedRequestDataHexStringsSHENGYUGESHU; k++)
 				  {
 					  EditedRequestHexStringsTmp[(k + 1)] = HttpEditedRequestDataHexStrings[(HttpEditedRequestDataHexStringsRowNum * 16 + k)];
 				  }
 				  
 				  if (HttpEditedRequestDataHexStringsLength % 16 != 0)
 				  {
 					  JPtabProxyHistoryDetailEditedRequestHexTableModelAdd.addRow(EditedRequestHexStringsTmp);
 					  JPtabProxyHistoryDetailEditedRequestHexTable.invalidate();
 				  }

 			  }
 			  else
 			  {
 				  JPtabProxyHistoryDetail.remove(JPtabProxyHistoryDetailEditedRequestPanel);
 			  }

 			  JPtabProxyHistoryDetailRequestRawText.setText(tmp[5]);
 			  JPtabProxyHistoryDetailResponseRawText.setText(tmp[6]);

 			  DefaultTableModel tableModel = (DefaultTableModel)JPtabProxyHistoryDetailRequestHeadersTable.getModel();
 			  tableModel.setRowCount(0);
 			  String[] headerParams = Method.getHTTPHeadersParams(tmp[5]);
 			  int headerParamLength = headerParams.length / 2;
 			  for (int i = 0; i < headerParamLength; i++)
 			  {
 				  tableModel.addRow(new Object[] { headerParams[(i * 2)], headerParams[(i * 2 + 1)] });
 			  }
 			  JPtabProxyHistoryDetailRequestHeadersTable.invalidate();

 			  DefaultTableModel ResponsetableModel = (DefaultTableModel)JPtabProxyHistoryDetailResponseHeadersTable.getModel();
 			  ResponsetableModel.setRowCount(0);
 			  
 			  if ((tmp[6] != null) && (!tmp[6].equals(null)) && (tmp[6].length() != 0))
 			  {
 				  String[] ResponseheaderParams = Method.getHTTPHeadersParams(tmp[6]);

 				  int ResponseheaderParamLength = ResponseheaderParams.length / 2;
 				  for (int i = 0; i < ResponseheaderParamLength; i++)
 				  {
 					  if (ResponseheaderParams[(i * 2)].length() > 0)
 					  {
 						  ResponsetableModel.addRow(new Object[] { ResponseheaderParams[(i * 2)], ResponseheaderParams[(i * 2 + 1)] });
 					  }
 				  }
 				  JPtabProxyHistoryDetailResponseHeadersTable.invalidate();
 			  }

 			  String[] HttpRequestDataHexStrings = Method.bytesToHexStrings(tmp[5].getBytes());
 			  int HttpRequestDataHexStringsLength = HttpRequestDataHexStrings.length;
 			  int HttpRequestDataHexStringsRowNum = HttpRequestDataHexStringsLength / 16;
 			  int HttpRequestDataHexStringsRealRowNum = 0;
 			  if (HttpRequestDataHexStringsLength % 16 == 0)
 			  {
 				  HttpRequestDataHexStringsRealRowNum = HttpRequestDataHexStringsRowNum;
 			  }
 			  else
 			  {
 				  HttpRequestDataHexStringsRealRowNum = HttpRequestDataHexStringsRowNum + 1;
 			  }

 			  String[] HttpRequestDataHexStringsRealRowStrings = new String[HttpRequestDataHexStringsRealRowNum];
 			  String HttpRequestDataHexStringsRealRowStringTmp = "";
 			  char[] RequestStringchars = tmp[5].toCharArray();
 			  int RequestStringcharsLength = RequestStringchars.length;
 			  for (int j = 0,i=1; (i <= RequestStringcharsLength) && (j < HttpRequestDataHexStringsRealRowNum); i++)
 			  {
 				  HttpRequestDataHexStringsRealRowStringTmp = HttpRequestDataHexStringsRealRowStringTmp + RequestStringchars[(i - 1)];

 				  if (i % 16 == 0)
 				  {
 					  HttpRequestDataHexStringsRealRowStrings[j] = HttpRequestDataHexStringsRealRowStringTmp;
 					  HttpRequestDataHexStringsRealRowStringTmp = "";
 					  j++;
 				  }

 			  }

 			  HttpRequestDataHexStringsRealRowStrings[(HttpRequestDataHexStringsRealRowNum - 1)] = HttpRequestDataHexStringsRealRowStringTmp;

 			  DefaultTableModel JPtabProxyHistoryDetailRequestHexTableModel = (DefaultTableModel)JPtabProxyHistoryDetailRequestHexTable.getModel();
 			  JPtabProxyHistoryDetailRequestHexTableModel.setRowCount(0);
 			  
 		
 			  
 			  for (int k = 0; k < HttpRequestDataHexStringsRowNum; k++)
 			  {
 				  JPtabProxyHistoryDetailRequestHexTableModel.addRow(new Object[] { Integer.valueOf(k + 1), HttpRequestDataHexStrings[(k * 16)], HttpRequestDataHexStrings[(k * 16 + 1)], HttpRequestDataHexStrings[(k * 16 + 2)], HttpRequestDataHexStrings[(k * 16 + 3)], HttpRequestDataHexStrings[(k * 16 + 4)], HttpRequestDataHexStrings[(k * 16 + 5)], HttpRequestDataHexStrings[(k * 16 + 6)], HttpRequestDataHexStrings[(k * 16 + 7)], HttpRequestDataHexStrings[(k * 16 + 8)], HttpRequestDataHexStrings[(k * 16 + 9)], HttpRequestDataHexStrings[(k * 16 + 10)], HttpRequestDataHexStrings[(k * 16 + 11)], HttpRequestDataHexStrings[(k * 16 + 12)], HttpRequestDataHexStrings[(k * 16 + 13)], HttpRequestDataHexStrings[(k * 16 + 14)], HttpRequestDataHexStrings[(k * 16 + 15)], HttpRequestDataHexStringsRealRowStrings[k] });
 				
 			  }

        
 			 
 			  String[] RequestHexStringsTmp = { String.valueOf(HttpRequestDataHexStringsRealRowNum), "--", "--", "--", "--", "--", "--", "--", "--", "--", "--", "--", "--", "--", "--", "--", "--", HttpRequestDataHexStringsRealRowStrings[(HttpRequestDataHexStringsRealRowNum - 1)] };
 			  int HttpRequestDataHexStringsSHENGYUGESHU = HttpRequestDataHexStringsLength % 16;
 			  for (int k = 0; k < HttpRequestDataHexStringsSHENGYUGESHU; k++)
 			  {
 				  RequestHexStringsTmp[(k + 1)] = HttpRequestDataHexStrings[(HttpRequestDataHexStringsRowNum * 16 + k)];
 			  }

 			  if (HttpRequestDataHexStringsLength % 16 != 0)
 			  {
 				  JPtabProxyHistoryDetailRequestHexTableModel.addRow(RequestHexStringsTmp);
 				  JPtabProxyHistoryDetailRequestHexTable.invalidate();
 			  }

 			  if ((tmp[6] != null) && (!tmp[6].equals(null)) && (tmp[6].length() != 0))
 			  {
 				  String[] HttpResponseDataHexStrings = Method.bytesToHexStrings(tmp[6].getBytes());
 				  int HttpResponseDataHexStringsLength = HttpResponseDataHexStrings.length;
 				  int HttpResponseDataHexStringsRowNum = HttpResponseDataHexStringsLength / 16;
 				  int HttpResponseDataHexStringsRealRowNum = 0;
 				  if (HttpResponseDataHexStringsLength % 16 == 0)
 				  {
 					  HttpResponseDataHexStringsRealRowNum = HttpResponseDataHexStringsRowNum;
 				  }
 				  else
 				  {
 					  HttpResponseDataHexStringsRealRowNum = HttpResponseDataHexStringsRowNum + 1;
 				  }
 				  
 				  String[] HttpResponseDataHexStringsRealRowStrings = new String[HttpResponseDataHexStringsRealRowNum];
 				  String HttpResponseDataHexStringsRealRowStringTmp = "";
 				  char[] ResponseStringchars = tmp[6].toCharArray();
 				  int ResponseStringcharsLength = ResponseStringchars.length;
 				  int h = 1; 
 				  
 				  for (int j = 0; (h <= ResponseStringcharsLength) && (j < HttpResponseDataHexStringsRealRowNum); h++)
 				  {
 					  HttpResponseDataHexStringsRealRowStringTmp = HttpResponseDataHexStringsRealRowStringTmp + ResponseStringchars[(h - 1)];
 					  if (h % 16 == 0)
 					  {
 						  HttpResponseDataHexStringsRealRowStrings[j] = HttpResponseDataHexStringsRealRowStringTmp;
 						  HttpResponseDataHexStringsRealRowStringTmp = "";
 						  j++;
 					  }
 				  }
 				  
 				  HttpResponseDataHexStringsRealRowStrings[(HttpResponseDataHexStringsRealRowNum - 1)] = HttpResponseDataHexStringsRealRowStringTmp;

 				  DefaultTableModel JPtabProxyHistoryDetailResponseHexTableModel = (DefaultTableModel)JPtabProxyHistoryDetailResponseHexTable.getModel();
 				  JPtabProxyHistoryDetailResponseHexTableModel.setRowCount(0);
 				  
 				  for (int k = 0; k < HttpResponseDataHexStringsRowNum; k++)
 				  {
 					  JPtabProxyHistoryDetailResponseHexTableModel.addRow(new Object[] { Integer.valueOf(k + 1), HttpResponseDataHexStrings[(k * 16)], HttpResponseDataHexStrings[(k * 16 + 1)], HttpResponseDataHexStrings[(k * 16 + 2)], HttpResponseDataHexStrings[(k * 16 + 3)], HttpResponseDataHexStrings[(k * 16 + 4)], HttpResponseDataHexStrings[(k * 16 + 5)], HttpResponseDataHexStrings[(k * 16 + 6)], HttpResponseDataHexStrings[(k * 16 + 7)], HttpResponseDataHexStrings[(k * 16 + 8)], HttpResponseDataHexStrings[(k * 16 + 9)], HttpResponseDataHexStrings[(k * 16 + 10)], HttpResponseDataHexStrings[(k * 16 + 11)], HttpResponseDataHexStrings[(k * 16 + 12)], HttpResponseDataHexStrings[(k * 16 + 13)], HttpResponseDataHexStrings[(k * 16 + 14)], HttpResponseDataHexStrings[(k * 16 + 15)], HttpResponseDataHexStringsRealRowStrings[k] });
 				  }

 				  String[] ResponseHexStringsTmp = { String.valueOf(HttpResponseDataHexStringsRealRowNum), "--", "--", "--", "--", "--", "--", "--", "--", "--", "--", "--", "--", "--", "--", "--", "--", HttpResponseDataHexStringsRealRowStrings[(HttpResponseDataHexStringsRealRowNum - 1)] };
 				  int HttpResponseDataHexStringsSHENGYUGESHU = HttpResponseDataHexStringsLength % 16;
 				  for (int k = 0; k < HttpResponseDataHexStringsSHENGYUGESHU; k++)
 				  {
 					  ResponseHexStringsTmp[(k + 1)] = HttpResponseDataHexStrings[(HttpResponseDataHexStringsRowNum * 16 + k)];
 				  }
 				  if (HttpResponseDataHexStringsLength % 16 != 0)
 				  {
 					  JPtabProxyHistoryDetailResponseHexTableModel.addRow(ResponseHexStringsTmp);
 					  JPtabProxyHistoryDetailResponseHexTable.invalidate();
 				  }
 			  }
 			  
 			  
 			  
 			  
 			  
 			  
 			  
 			  
 			 
 			 if((e.getModifiers()&InputEvent.BUTTON3_MASK)!=0)
 			 {
 				final String[] tmp2 = (String[])proxyHistoryList.get(ProxyHistoryListTable.getSelectedRow());
 				
 				
 				PopupMenu ProxyHistorItemPopupMenu = new PopupMenu();				
 				MenuItem ProxyHistoryItemRightKeyItem = new MenuItem();
 				ProxyHistoryItemRightKeyItem.setLabel("Send To Editor");
 				MenuItem ProxyHistoryItemRightKeyItemClearHistory = new MenuItem();
 				ProxyHistoryItemRightKeyItemClearHistory.setLabel("Clear History");
 				
 				ProxyHistorItemPopupMenu.add(ProxyHistoryItemRightKeyItem);
 				ProxyHistorItemPopupMenu.add(ProxyHistoryItemRightKeyItemClearHistory);
 				ProxyHistoryListTable.add(ProxyHistorItemPopupMenu);				
 				ProxyHistorItemPopupMenu.show(ProxyHistoryListTable, e.getX(), e.getY());
 				
 				ProxyHistoryItemRightKeyItem.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						// TODO Auto-generated method stub
						String[] queueproxyHistoryItemTmpTmp = tmp2;

			    		AddTabPanel(JPtabEditorPanel, queueproxyHistoryItemTmpTmp[1], Integer.valueOf(queueproxyHistoryItemTmpTmp[7]), queueproxyHistoryItemTmpTmp[5]);

			    		Thread setFontThread = new Thread(new Runnable()
			    		{
			    			public void run()
			    			{
			    				JLabel ttt = new JLabel("Editor");
			    				ttt.setForeground(Color.red);
			    				JPtab.setTabComponentAt(1, ttt);
			    				try
			    				{
			    					Thread.sleep(3000L);
			    				}
			    				catch (Exception e)
			    				{
			    					e.printStackTrace();
			    				}
			    				ttt.setForeground(null);
			    				JPtab.setTabComponentAt(1, ttt);
			    			}
			    		});
			    		setFontThread.start();
					}
				});
 				
 				ProxyHistoryItemRightKeyItemClearHistory.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						// TODO Auto-generated method stub
						proxyHistoryList.clear();
						DefaultTableModel tableModel = (DefaultTableModel)ProxyHistoryListTable.getModel();
						tableModel.setRowCount(0);
					}
				});
 				 
 			 }
 			  
 			  
 			  
 			  
 		  }
 	  });
 	  JScrollPane ProxyHistoryListScrollPanel = new JScrollPane(ProxyHistoryListTable);
 	  ProxyHistoryListScrollPanel.setPreferredSize(new Dimension(0, 200));
 	  ProxyHistoryListPanel.add(ProxyHistoryListScrollPanel, "Center");

 	  JPtabProxyHistoryDetail = new JTabbedPane(1);
 	  JPtabProxyHistoryDetail.setFocusable(false);
 	  JPtabProxyHistoryDetail.setForeground(new Color(Integer.decode("#4B4B4B").intValue()));
 	  ProxyHistoryDetailPanl.add(JPtabProxyHistoryDetail, "Center");

 	  JDesktopPane JPtabProxyHistoryDetailRequestPanel = new JDesktopPane();
 	  JPtabProxyHistoryDetailRequestPanel.setLayout(new BorderLayout());
 	  JPtabProxyHistoryDetail.add(JPtabProxyHistoryDetailRequestPanel, "Request");

 	  JDesktopPane JPtabProxyHistoryDetailResponsePanel = new JDesktopPane();
 	  JPtabProxyHistoryDetailResponsePanel.setLayout(new BorderLayout());
 	  JPtabProxyHistoryDetail.add(JPtabProxyHistoryDetailResponsePanel, "Response");

 	  JTabbedPane JPtabProxyHistoryDetailRequest = new JTabbedPane(1);
 	  JPtabProxyHistoryDetailRequest.setFocusable(false);
 	  JPtabProxyHistoryDetailRequest.setForeground(new Color(Integer.decode("#4B4B4B").intValue()));
 	  JPtabProxyHistoryDetailRequestPanel.add(JPtabProxyHistoryDetailRequest, "Center");

 	  JDesktopPane JPtabProxyHistoryDetailRequestRawPanel = new JDesktopPane();
 	  JPtabProxyHistoryDetailRequestRawPanel.setBackground(Color.black);
 	  JPtabProxyHistoryDetailRequestRawText = new JTextArea();
 	  JPtabProxyHistoryDetailRequestRawText.setEditable(false);

 	  JScrollPane JPtabProxyHistoryDetailRequestRawScollPanel = new JScrollPane(JPtabProxyHistoryDetailRequestRawText);
 	  JPtabProxyHistoryDetailRequestRawPanel.setLayout(new BorderLayout());
 	  JPtabProxyHistoryDetailRequestRawPanel.add(JPtabProxyHistoryDetailRequestRawScollPanel, "Center");

 	  JPtabProxyHistoryDetailRequest.add(JPtabProxyHistoryDetailRequestRawPanel, "Raw");

 	  JDesktopPane JPtabProxyHistoryDetailRequestHeadersPanel = new JDesktopPane();
 	  JPtabProxyHistoryDetailRequestHeadersPanel.setLayout(new BorderLayout());

 	  String[] JPtabProxyHistoryDetailRequestHeadersTableHeaders = { "Name", "Value" };
 	  Object[][] JPtabProxyHistoryDetailRequestHeadersTableCellData = null;
 	  DefaultTableModel JPtabProxyHistoryDetailRequestHeadersTableModel = new DefaultTableModel(JPtabProxyHistoryDetailRequestHeadersTableCellData, JPtabProxyHistoryDetailRequestHeadersTableHeaders) {
 		  public boolean isCellEditable(int row, int column) {
 			  return true;
 		  }
 	  };
 	  JPtabProxyHistoryDetailRequestHeadersTable = new JTable(JPtabProxyHistoryDetailRequestHeadersTableModel);

 	  JScrollPane JPtabProxyHistoryDetailRequestHeadersTableScollPanel = new JScrollPane(JPtabProxyHistoryDetailRequestHeadersTable);
 	  JPtabProxyHistoryDetailRequestHeadersPanel.add(JPtabProxyHistoryDetailRequestHeadersTableScollPanel, "Center");

 	  JPtabProxyHistoryDetailRequest.add(JPtabProxyHistoryDetailRequestHeadersPanel, "Headers");

 	  JDesktopPane JPtabProxyHistoryDetailRequestHexPanel = new JDesktopPane();
 	  JPtabProxyHistoryDetailRequest.add(JPtabProxyHistoryDetailRequestHexPanel, "Hex");
 	  JPtabProxyHistoryDetailRequestHexPanel.setLayout(new BorderLayout());

 	  String[] JPtabProxyHistoryDetailRequestHexTableHeaders = { "Num", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "String" };
 	  Object[][] JPtabProxyHistoryDetailRequestHexTableCellData = null;
 	  DefaultTableModel JPtabProxyHistoryDetailRequestHexTableModel = new DefaultTableModel(JPtabProxyHistoryDetailRequestHexTableCellData, JPtabProxyHistoryDetailRequestHexTableHeaders) {
 		  public boolean isCellEditable(int row, int column) {
    	  return true;
 		  }
 	  };
 	  JPtabProxyHistoryDetailRequestHexTable = new JTable(JPtabProxyHistoryDetailRequestHexTableModel) 
 	  { 
 		  public boolean isCellEditable(int row, int column) { return false; }
 	  };
 	  JPtabProxyHistoryDetailRequestHexTable.getTableHeader().setVisible(false);
 	  JPtabProxyHistoryDetailRequestHexTable.setCellSelectionEnabled(false);

 	  TableColumn JPtabProxyHistoryDetailRequestHexTableFirstColumn = JPtabProxyHistoryDetailRequestHexTable.getColumnModel().getColumn(0);
 	  JPtabProxyHistoryDetailRequestHexTableFirstColumn.setPreferredWidth(120);

 	  TableColumn JPtabProxyHistoryDetailRequestHexTableStringColumn = JPtabProxyHistoryDetailRequestHexTable.getColumnModel().getColumn(17);
 	  JPtabProxyHistoryDetailRequestHexTableStringColumn.setPreferredWidth(200);

 	  JScrollPane JPtabProxyHistoryDetailRequestHexScrollPanel = new JScrollPane(JPtabProxyHistoryDetailRequestHexTable);
 	  JPtabProxyHistoryDetailRequestHexPanel.add(JPtabProxyHistoryDetailRequestHexScrollPanel, "Center");

 	  JTabbedPane JPtabProxyHistoryDetailResponse = new JTabbedPane(1);
 	  JPtabProxyHistoryDetailResponse.setFocusable(false);
 	  JPtabProxyHistoryDetailResponse.setForeground(new Color(Integer.decode("#4B4B4B").intValue()));
 	  JPtabProxyHistoryDetailResponsePanel.add(JPtabProxyHistoryDetailResponse, "Center");
    
 	  JDesktopPane JPtabProxyHistoryDetailResponseRawPanel = new JDesktopPane();
 	  JPtabProxyHistoryDetailResponseRawPanel.setBackground(Color.black);
 	  JPtabProxyHistoryDetailResponseRawText = new JTextArea();
 	  JPtabProxyHistoryDetailResponseRawText.setEditable(false);

 	  JScrollPane JPtabProxyHistoryDetailResponseRawScollPanel = new JScrollPane(JPtabProxyHistoryDetailResponseRawText);
 	  JPtabProxyHistoryDetailResponseRawPanel.setLayout(new BorderLayout());
 	  JPtabProxyHistoryDetailResponseRawPanel.add(JPtabProxyHistoryDetailResponseRawScollPanel, "Center");

 	  JPtabProxyHistoryDetailResponse.add(JPtabProxyHistoryDetailResponseRawPanel, "Raw");
    
 	  JDesktopPane JPtabProxyHistoryDetailResponseHeadersPanel = new JDesktopPane();
 	  JPtabProxyHistoryDetailResponseHeadersPanel.setLayout(new BorderLayout());
 	  String[] JPtabProxyHistoryDetailResponseHeadersTableHeaders = { "Name", "Value" };
 	  Object[][] JPtabProxyHistoryDetailResponseHeadersTableCellData = null;
 	  DefaultTableModel JPtabProxyHistoryDetailResponseHeadersTableModel = new DefaultTableModel(JPtabProxyHistoryDetailResponseHeadersTableCellData, JPtabProxyHistoryDetailResponseHeadersTableHeaders) {
 		  public boolean isCellEditable(int row, int column) {
 			  return true;
 		  }
 	  };
 	  JPtabProxyHistoryDetailResponseHeadersTable = new JTable(JPtabProxyHistoryDetailResponseHeadersTableModel);

 	  JScrollPane JPtabProxyHistoryDetailResponseHeadersTableScollPanel = new JScrollPane(JPtabProxyHistoryDetailResponseHeadersTable);
 	  JPtabProxyHistoryDetailResponseHeadersPanel.add(JPtabProxyHistoryDetailResponseHeadersTableScollPanel, "Center");

 	  JPtabProxyHistoryDetailResponse.add(JPtabProxyHistoryDetailResponseHeadersPanel, "Headers");

 	  JDesktopPane JPtabProxyHistoryDetailResponseHexPanel = new JDesktopPane();
 	  JPtabProxyHistoryDetailResponseHexPanel.setLayout(new BorderLayout());
 	  JPtabProxyHistoryDetailResponse.add(JPtabProxyHistoryDetailResponseHexPanel, "Hex");
    
 	  String[] JPtabProxyHistoryDetailResponseHexTableHeaders = { "Num", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "String" };
 	  Object[][] JPtabProxyHistoryDetailResponseHexTableCellData = null;
 	  DefaultTableModel JPtabProxyHistoryDetailResponseHexTableModel = new DefaultTableModel(JPtabProxyHistoryDetailResponseHexTableCellData, JPtabProxyHistoryDetailResponseHexTableHeaders) {
 		  public boolean isCellEditable(int row, int column) {
 			  return true;
 		  }
 	  };
 	  JPtabProxyHistoryDetailResponseHexTable = new JTable(JPtabProxyHistoryDetailResponseHexTableModel) 
 	  { 
 		  public boolean isCellEditable(int row, int column) { return false; }
 	  };
 	  JPtabProxyHistoryDetailResponseHexTable.getTableHeader().setVisible(false);
 	  JPtabProxyHistoryDetailResponseHexTable.setCellSelectionEnabled(false);

 	  TableColumn JPtabProxyHistoryDetailResponseHexTableFirstColumn = JPtabProxyHistoryDetailResponseHexTable.getColumnModel().getColumn(0);
 	  JPtabProxyHistoryDetailResponseHexTableFirstColumn.setPreferredWidth(120);

 	  TableColumn JPtabProxyHistoryDetailResponseHexTableStringColumn = JPtabProxyHistoryDetailResponseHexTable.getColumnModel().getColumn(17);
 	  JPtabProxyHistoryDetailResponseHexTableStringColumn.setPreferredWidth(200);

 	  JScrollPane JPtabProxyHistoryDetailResponseHexScrollPanel = new JScrollPane(JPtabProxyHistoryDetailResponseHexTable);
 	  JPtabProxyHistoryDetailResponseHexPanel.add(JPtabProxyHistoryDetailResponseHexScrollPanel, "Center");

 	  JPanel EditorButtonPane = new JPanel();
 	  //EditorButtonPane.setBackground(Color.yellow);
 	  EditorButtonPane.setLayout(null);

 	  EditorButtonPane.setPreferredSize(new Dimension(1000, 70));
 	  GridBagConstraints GBCEBP = new GridBagConstraints();
 	  GBCEBP.fill = 1;
 	  GBCEBP.insets = new Insets(5, 5, 5, 5);
 	  GBCEBP.gridx = 0;
 	  GBCEBP.gridy = 0;
 	  GBCEBP.gridheight = 4;
 	  GBCEBP.gridwidth = 1;
 	  GBCEBP.weightx = 1.0D;
 	  GBCEBP.weighty = 0.0D;

 	  JDPEditor.add(EditorButtonPane, "North");
    
 	  JButton JPtabEditorPanelAddButton = new JButton("Add Step");
 	  JPtabEditorPanelAddButton.setFocusable(false);
 	  JPtabEditorPanelAddButton.setBounds(60, 15, 100, 28);
 	  EditorButtonPane.add(JPtabEditorPanelAddButton);

 	  JPanel JPtabEditorPanelCreateModulePanel = new JPanel();
 	  JPtabEditorPanelCreateModulePanel.setLayout(null);
 	  //JPtabEditorPanelCreateModulePanel.setBackground(Color.blue);
 	  JPtabEditorPanelCreateModulePanel.setBounds(500, 0, 500, 100);
 	  JPtabEditorPanelCreateModulePanel.setMaximumSize(new Dimension(600, 100));
 	  JPtabEditorPanelCreateModulePanel.setSize(600, 300);
 	  EditorButtonPane.add(JPtabEditorPanelCreateModulePanel);

 	  JButton JPtabEditorPanelCreateButton = new JButton("Create");
 	  JPtabEditorPanelCreateButton.setFocusable(false);
 	  JPtabEditorPanelCreateButton.setBounds(10, 15, 100, 28);
 	  JPtabEditorPanelCreateModulePanel.add(JPtabEditorPanelCreateButton);
 	  
 	  JButton JPtabEditorPanelReSetAllButton = new JButton("ReSetAll");
 	  JPtabEditorPanelReSetAllButton.setFocusable(false);
 	  JPtabEditorPanelReSetAllButton.setBounds(130,15,100,28);
 	  JPtabEditorPanelCreateModulePanel.add(JPtabEditorPanelReSetAllButton);
 	  
 	  JButton JPtabEditorPanelSentToDetection = new JButton("Test Run");
 	  JPtabEditorPanelSentToDetection.setFocusable(false);
 	  JPtabEditorPanelSentToDetection.setBounds(250,15,100,28);
	  JPtabEditorPanelCreateModulePanel.add(JPtabEditorPanelSentToDetection);
	  
	  JLabel JPtabEditorPanelSentToDetectionTargetLabel = new JLabel("Target: ");
	  JPtabEditorPanelSentToDetectionTargetLabel.setBounds(360,15,70,30);
	  JPtabEditorPanelCreateModulePanel.add(JPtabEditorPanelSentToDetectionTargetLabel);
	  
	  JPtabEditorPanelSentToDetectionTargetText = new JTextField();
	  JPtabEditorPanelSentToDetectionTargetText.setBounds(420,20,180,25);
	  JPtabEditorPanelCreateModulePanel.add(JPtabEditorPanelSentToDetectionTargetText);
 	  
	  JPtabEditorPanelSentToDetection.addActionListener(new ActionListener() {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			File ExpTmpFile = new File("ExpTmp");
			if(ExpTmpFile.exists())
			{
				if(ExpTmpFile.isDirectory())
				{
					Method.deleteDirectory("ExpTmp");
				}
				else
				{
					ExpTmpFile.delete();
				}
			}
			
			
			String ExpName = "ExpTmp";	
			  String ExpAboutString = "This is Test Run Tmp File";
			  ExpAboutString = ExpAboutString.replaceAll("\n", "\r\n");
			//  System.out.println(ExpAboutString);
			  
			  
			 if (ExpName == null||ExpName.length()==0)
			  {
				  JOptionPane.showMessageDialog(null, "请输入要保存的Exp名称", "Warning", 2);
			  }
			  else
			  {
				  //fuck
				  int EditorStepCount = JPtabEditorPanel.getTabCount();
				  String StepFileTitle = "#Step-IP/Host-Port-FileName-UserURLToSocketHostAndPort-UserPreviousSet-Cookie";
				  String ItemNama = ExpName;

				  File ItemFileDir = new File(ItemNama);
				  
				  if (ItemFileDir.exists())
				  {
					  JOptionPane.showMessageDialog(null, "当前名称已经存在，请换个名称", "Warning", 2);
				  }
				  else
				  {
					  ItemFileDir.mkdir();
					  try
					  {
						  FileWriter fileWriter = new FileWriter(ItemFileDir + "/Step.txt", true);

						  for (int i = 0; i < EditorStepCount; i++)
						  {
							//  System.out.println("第一个");
							  EditorStep EditorStepTmp = (EditorStep)JPtabEditorPanel.getComponentAt(i);
							  
							  String ExpResourceRequestFileName = "Step"+i+"_Resource.txt";
							  FileWriter ExpResourceRequestFileWriter = new FileWriter(ItemFileDir +"/"+ExpResourceRequestFileName, true);
							  ExpResourceRequestFileWriter.write(EditorStepTmp.resourceStepString);
							  ExpResourceRequestFileWriter.flush();
							  ExpResourceRequestFileWriter.close();
							  

							  String FileName = "Step" + i + ".txt";
							  fileWriter.write(FileName + "-");

							  fileWriter.write("host:" + Method.getBase64(EditorStepTmp.host) + "-");

							  fileWriter.write("port:" + Method.getBase64(String.valueOf(EditorStepTmp.port)) + "-");

							  fileWriter.write("stepName:" + Method.getBase64(FileName) + "-");

							  if (EditorStepTmp.replaceHostPort)
							  {
								  fileWriter.write("replaceHostPort:true-");
							  }
							  else
							  {
								  fileWriter.write("replaceHostPort:false-");
							  }

							//  System.out.println("EditorStepTmpEditorStepTmp::" + EditorStepTmp.useSetCookieFromPrevious);
							  if (EditorStepTmp.useSetCookieFromPrevious)
							  {
								  fileWriter.write("useSetCookieFromPrevious:true-");
							  }
							  else
							  {
								  fileWriter.write("useSetCookieFromPrevious:false-");
							  }

							  fileWriter.write("randomString:");
							  int randomStringListCount = EditorStepTmp.randomStringList.size();
							  for (int q = 0; q < randomStringListCount; q++)
							  {
								  String[] tmp = new String[5];
								  String StrTmp = "";
								  tmp = (String[])EditorStepTmp.randomStringList.get(q);
								  StrTmp = tmp[0] + "," + tmp[1] + "," + tmp[2] + tmp[3]+","+tmp[4];
								  fileWriter.write(Method.getBase64(StrTmp));
								  fileWriter.write(",");
							  }
							  fileWriter.write("-");

							  fileWriter.write("captcha:");
							  int captchaListCount = EditorStepTmp.captchaList.size();
							  for (int q = 0; q < captchaListCount; q++)
							  {
								  String[] tmp = new String[4];
								  String StrTmp = "";
								  tmp = (String[])EditorStepTmp.captchaList.get(q);
								  StrTmp = tmp[0] + "," + tmp[1] + "," + tmp[2]+","+tmp[3];
								  fileWriter.write(Method.getBase64(StrTmp));
								  fileWriter.write(",");
							  }
							  fileWriter.write("-");
							  
							  fileWriter.write("setParamsFromDataPackage:");
							  int setParamsFromDataPackageListCount = EditorStepTmp.setParamsFromDataPackageList.size();
							  for (int q = 0; q < 0; q++)
							  {
								  String[] tmp = new String[4];
								  String StrTmp = "";
								  tmp = (String[])EditorStepTmp.setParamsFromDataPackageList.get(q);
								  StrTmp = tmp[0] + "," + tmp[1] + "," + tmp[2];
								  fileWriter.write(Method.getBase64(StrTmp));
								  fileWriter.write(",");
							  }
							  fileWriter.write("-");

							  fileWriter.write("setGoAheadOrStopFromStatus:");
							  int setGoAheadOrStopFromStatusListCount = EditorStepTmp.setGoAheadOrStopFromStatusList.size();
							  for (int q = 0; q < setGoAheadOrStopFromStatusListCount; q++)
							  {
								  String[] tmp = new String[4];
								  String StrTmp = "";
								  tmp = (String[])EditorStepTmp.setGoAheadOrStopFromStatusList.get(q);
								  StrTmp = tmp[0] + "," + tmp[1];
								  fileWriter.write(Method.getBase64(StrTmp));
								  fileWriter.write(",");
							  }
							  fileWriter.write("-");

							  fileWriter.write("setGoAheadOrStopFromData:");
							  int setGoAheadOrStopFromDataListCount = EditorStepTmp.setGoAheadOrStopFromDataList.size();
							  for (int q = 0; q < setGoAheadOrStopFromDataListCount; q++)
							  {
								  String[] tmp = new String[4];
								  String StrTmp = "";
								  tmp = (String[])EditorStepTmp.setGoAheadOrStopFromDataList.get(q);
								  StrTmp = Method.getBase64(tmp[0]) + "," + tmp[1];
								  fileWriter.write(Method.getBase64(StrTmp));
								  fileWriter.write(",");
							  }
							  fileWriter.write("-");

							  fileWriter.write("useFileForCycleWithPackage:");
							  String[] useFileForCycleWithPackageItemStrings = EditorStepTmp.useFileForCycleWithPackageItem;
							  try
							  {
								  int useFileForCycleWithPackageItemFileNums = Integer.valueOf(useFileForCycleWithPackageItemStrings[0]);
								  if (useFileForCycleWithPackageItemFileNums == 1)
								  {
									  String StrTmp = "";
									  File tmp = new File(useFileForCycleWithPackageItemStrings[1].trim());
									  StrTmp = useFileForCycleWithPackageItemStrings[0] + "," + Method.getBase64(tmp.getName()) + "," + ","+","+Method.getBase64(useFileForCycleWithPackageItemStrings[4])+",";
									  fileWriter.write(Method.getBase64(StrTmp));
									  fileWriter.write(",");
									  

									  Method.copyFile(useFileForCycleWithPackageItemStrings[1].trim(), ItemNama + "/" + tmp.getName());
								  }
								  else if (useFileForCycleWithPackageItemFileNums == 2)
								  {
									  String StrTmp = "";
									  File tmp1 = new File(useFileForCycleWithPackageItemStrings[1].trim());
									  File tmp2 = new File(useFileForCycleWithPackageItemStrings[2].trim());
									 // System.out.println(useFileForCycleWithPackageItemStrings[0]);
									//  System.out.println(useFileForCycleWithPackageItemStrings[1]);
									//  System.out.println(useFileForCycleWithPackageItemStrings[2]);
									//  System.out.println(useFileForCycleWithPackageItemStrings[3]);
									  StrTmp = useFileForCycleWithPackageItemStrings[0] + "," + Method.getBase64(tmp1.getName()) + "," + Method.getBase64(tmp2.getName()) + "," + useFileForCycleWithPackageItemStrings[3]+","+Method.getBase64(useFileForCycleWithPackageItemStrings[4])+","+Method.getBase64(useFileForCycleWithPackageItemStrings[5]);
									//  System.out.println(StrTmp);
									//  System.out.println(Method.getBase64(StrTmp));

									  fileWriter.write(Method.getBase64(StrTmp));
									  fileWriter.write(",");
									 

									//  System.out.println(tmp1.getName());
									 // System.out.println(tmp2.getName());
									  Method.copyFile(useFileForCycleWithPackageItemStrings[1].trim(), ItemNama + "/" + tmp1.getName());
									  Method.copyFile(useFileForCycleWithPackageItemStrings[2].trim(), ItemNama + "/" + tmp2.getName());
								  }
								  
							  }
							  catch (Exception localException1)
							  {
							  }

							  fileWriter.write("-");

							  fileWriter.write("fileForCycleWithEXP:");
							  String fileForCycleWithEXPName = EditorStepTmp.FileForCycleWithEXP;
							  String fileForCycleWithExpInStep = EditorStepTmp.FileForCycleWithEXPStrings[1];
							  String fileForCycleWithExpResource =  EditorStepTmp.FileForCycleWithEXPStrings[2];
							  if (fileForCycleWithEXPName.length() > 0)
							  {
								  File tmp = new File(fileForCycleWithEXPName.trim());
								  fileWriter.write(Method.getBase64(tmp.getName()+","+fileForCycleWithExpInStep+","+fileForCycleWithExpResource));
								  
								  
								  Method.copyFile(fileForCycleWithEXPName.trim(), ItemNama + "/" + tmp.getName());
							  }

							  String requestData = EditorStepTmp.editorStepRequestData;

							  fileWriter.write("\r\n");
							  
							  Method.writeHexStringsToFile(ItemFileDir + "/" + FileName, Method.bytesToHexStrings(requestData.getBytes()));
						  }

						  fileWriter.flush();
						  fileWriter.close();
						  
						  
						  FileWriter AboutfileWriter = new FileWriter(ItemFileDir + "/About.txt", true);
						  AboutfileWriter.write(ExpAboutString);
						  AboutfileWriter.flush();
						  AboutfileWriter.close();
						  
						  
						  
						  
						  
					  }
					  catch (Exception re)
					  {
						  re.printStackTrace();
						  JOptionPane.showMessageDialog(null, "请输入正确的文件名", "Warning", 2);
					  }
				  }
			  }
			
			
			 
			 
			 
			 
			 ////////////////////////////////////
			 
			 
			
			 
			 
			 
			 
			 
			 
			 
			 Thread testRunStartThread = new Thread(new Runnable() {
					public void run() {

						//JDPDetectionRightPanelTargetGoPanelEditPanelButton.setEnabled(false);
						//虚拟的目标主机 
						String targetHost = "www.baidu.com";
						
						//虚拟的目标端口
						int targetPort = 80;
						
						String previousURL="";
						
						String targetTmp = JPtabEditorPanelSentToDetectionTargetText.getText().trim();// 将要检测是字符串   example：www.baidu.com
						targetTmp = targetTmp.replaceAll("\\\\","/");// 替换掉  \ 为  /
						
						//分为是否有  http://
						if(targetTmp.toLowerCase().startsWith("http://"))
						{
							//如果有 http:// 直接去掉 
							targetTmp = targetTmp.substring(7);
							
							//后续操作就和没有http:// 一样
							
							//以 冒号来分割   判断是否存在端口号
							String[] targetStringsTmp = targetTmp.split(":");
							try
							{
								int previousIntTmp=0;
								//这部分是测试 是否存在  /  存在  / 这个就判断为 有 url前缀
								try
								{
									previousIntTmp = targetTmp.indexOf("/");
								}
								catch(Exception es)
								{
									es.printStackTrace();
								}
								
								
								if(targetStringsTmp.length>1)// 有端口
								{
									targetHost = (targetStringsTmp[0]);
									if(previousIntTmp>0) //有url 前缀
									{
										targetPort = Integer.valueOf(targetStringsTmp[1].substring(0,targetStringsTmp[1].indexOf("/")));
										previousURL = targetStringsTmp[1].substring(targetStringsTmp[1].indexOf("/"));
									}
									else //没有url qianzhui 
									{
										targetPort = Integer.valueOf(targetStringsTmp[1]);
										previousURL="";
									}
									
								}
								else //没有端口
								{
									if(previousIntTmp>0)//有url 前缀
									{
										targetHost = targetTmp.substring(0,previousIntTmp);
										previousURL = targetTmp.substring(previousIntTmp);
									}
									else//没有url 前缀
									{
										targetHost = targetTmp;
										previousURL="";
									}
								}
								
								
							}
							catch(Exception eg2)
							{
								targetPort=80;
								eg2.printStackTrace();
								
							}
						}
						else
						{
							String[] targetStringsTmp = targetTmp.split(":");
							try
							{
								int previousIntTmp=0;
								try
								{
									previousIntTmp = targetTmp.indexOf("/");
								}
								catch(Exception es)
								{
									es.printStackTrace();
								}
								
								if(targetStringsTmp.length>1)// 有端口
								{
									targetHost = (targetStringsTmp[0]);
									if(previousIntTmp>0) //有url 前缀
									{
										targetPort = Integer.valueOf(targetStringsTmp[1].substring(0,targetStringsTmp[1].indexOf("/")));
										previousURL = targetStringsTmp[1].substring(targetStringsTmp[1].indexOf("/"));
									}
									else //没有url qianzhui 
									{
										targetPort = Integer.valueOf(targetStringsTmp[1]);
										previousURL="";
									}
									
								}
								else //没有端口
								{
									if(previousIntTmp>0)//有url 前缀
									{
										targetHost = targetTmp.substring(0,previousIntTmp);
										previousURL = targetTmp.substring(previousIntTmp);
									}
									else//没有url 前缀
									{
										targetHost = targetTmp;
										previousURL="";
									}
								}
								
								
							}
							catch(Exception eg2)
							{
								targetPort=80;
								eg2.printStackTrace();
								
							}
						}
						
						/*
						String requestDat2a = "GET /previousURL/scada/Login.aspx HTTP/1.1"+"\r\n"+
											 "Host: takipmobil.com"+"\r\n"+
											 "Proxy-Connection: keep-alive"+"\r\n"+
											 "Cache-Control: max-age=0"+"\r\n"+
											 "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp;q=0.8"+"\r\n"+
											 "User-Agent: Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.131 Safari/537.36"+"\r\n"+
											 "Referer: http://www.gfsoso.com/?q=inurl%3Ascada/login&pn=10"+"\r\n"+
											 "Accept-Encoding: gzip,deflate,sdch"+"\r\n"+
											 "Accept-Language: zh-CN,zh;q=0.8,en;q=0.6"+"\r\n"+
											 "Cookie: ASP.NET_SessionId=4prxqtx5hupq3iimnvzejsll"+"\r\n"+
											 "\r\n";

		 
						
						
						System.out.println(requestDat2a);
						System.out.println(previousURL.length());
						
						if(previousURL.length()>0)
						{
							requestDat2a = Method.addPreviousURL(requestDat2a,previousURL);
						}
						
						//System.exit(1);
						*/
						
						
						
						
						
						//System.out.println(previousURL);
						//System.out.println(targetHost);
						//System.out.println(targetPort);
						
						
						
						
						//主tab  Detection 左侧 table 的行数
						//int ExpsListCount =  JDPDetectionLeftPanelTable.getRowCount();
						//DefaultTableModel  tablemodel = (DefaultTableModel) JDPDetectionLeftPanelTable.getModel();
						//循环获取 table 中被选中的 exp 来执行
						//for(int h=0;h<ExpsListCount;h++)
						//{
							//选中
							//if((boolean)tablemodel.getValueAt(h, 0))
							//{
								//默认的exp 都是存放在 exps 这个目录里的
								String ExpName="ExpTmp";
								String fileDir = ExpName+"/";

								
								//这是每个Exp 里 step 步骤的  文件名   例如   Step1、Step2
								String StepFileNameTmp = "";

								//这个是统计每个 Step 里 随机字符串设置    的 个数
								List randomStringList = new ArrayList();
								//这个是统计每个 Step 里验证码设置    的 个数
								List captchaList = new ArrayList();
								//这个是统计每个 Step 里。。。设置    的 个数
								List setParamsFromDataPackageList = new ArrayList();
								//这个是统计每个 Step 里设置停止前进 匹配    从返回的 状态 的 个数
								List setGoAheadOrStopFromStatusList = new ArrayList();
								//这个是统计每个 Step 里设置停止前进 匹配    从返回的文本 的 个数
								List setGoAheadOrStopFromDataList = new ArrayList();
								//这个是统计每个 Step 里使用 package  的 个数  最大两个
								List useFileForCycleWithPackageList = new ArrayList();
								//这个为每个Exp中  如果存在 expFileCycle  则 这个字符串就是该文件名
								String fileForCycleWithEXP = "";
				        
								
				        
								//开始每个exp 的检测
								try
								{
									//读取  Exp 的 主配置文件  记录每个步骤的详细
									File file = new File(fileDir + "Step.txt");
									if ((file.isFile()) && (file.exists()))
									{
										InputStreamReader read = new InputStreamReader(new FileInputStream(file));
										BufferedReader bufferedReader = new BufferedReader(read);
										String lineTxt = null;
										
										//先把 exp 的所有步骤全部读取出来存放到  tmplist 里面
										List tmplist = new ArrayList();

										while ((lineTxt = bufferedReader.readLine()) != null)
										{
											tmplist.add(lineTxt);
										}							
										read.close();
										
										//Exp 的步骤数     等下循环是根据步骤数来循环的
										int StepNums = tmplist.size();
										// 
										if (StepNums > 0)
										{
											String tmpStr = (String)tmplist.get(0);//获取第一行  、 这个的目的是为了判断  获取 存在的  CycleWithExp 的文件名，  每个步骤都有记录 这个文件名  所以读取一行就够了
											fileForCycleWithEXP = "";//初始化 存储cyclewithexp 的字符串对象
											String[] lineTxtTmp = tmpStr.split("-");//每个步骤中的 每个参数之间用 - 隔开
											int lineTxtTmpLength = lineTxtTmp.length;//
											if (lineTxtTmpLength == 13)//每个步骤的参数都是固定 13个
											{
												String[] fileForCycleWithEXPStrings = lineTxtTmp[12].trim().split(":");// CycleWithExp  固定位置  读取文件名
												if (fileForCycleWithEXPStrings.length > 1)
												{
													fileForCycleWithEXP = Method.getFromBase64(fileForCycleWithEXPStrings[1].trim()).split(",")[0];
												}									
											}
										}

										// 有 CycleWithExp 文件  
										if (fileForCycleWithEXP.length() > 0)
										{
											File fileForCycleWithEXPFile = new File(fileDir+fileForCycleWithEXP);
											InputStreamReader inputSteamReadTmp = new InputStreamReader(new FileInputStream(fileForCycleWithEXPFile));
											BufferedReader expFileBufferedReader = new BufferedReader(inputSteamReadTmp);
											String expFileLineTxt = null;

											boolean StopExp = false; //停止exp 的标志
											boolean StopCurrent =false;//这个暂时没用
											boolean GoAheadCurrent = false;//这个是停止步骤当前循环 继续下一个步骤
											boolean GoAheadExp = false;//这个是停止所有步骤  继续下一个 exp 的循环
											List ExpPackageHistoryList = new ArrayList();//所有 http 的 发送记录
											        
											int expNum=0;//通过这个来排序   
											//StopExp  标志来判断 是否停止
											while((expFileLineTxt = expFileBufferedReader.readLine()) != null&&!StopExp)
											{					
												expNum++;//累计 个数
												String[] ExpPackagePreviousItem = new String[7];//用来记录上一个数据包的数据 Item  

												//根据步骤数来循环  StopExp 来跳出循环
												for (int i = 0; i < StepNums&&!StopExp; i++)
												{
													//循环获取步骤的相关参数 
													String StringForEachStep = (String)tmplist.get(i);
													if (StringForEachStep.length() > 0)
													{
														StepFileNameTmp = "";//记录  步骤中  要发送的数据的 http报文的  保存文件名		                      
														String requestData = "";//记录 要发送的http 报文
														String responseData = "";//记录 返回的 http 报文
														int responseStatus = 0;//记录 返回的http 状态值
														String whyToStop = "Run Over";//记录 Stop 的原因
														String host = "";//记录 目标主机地址
														int port = 80;//记录目标主机端口  默认为80端口
														String[] ExpPackageHistoryItem = new String[7]; //历史 list 的一个item
				                      											
														String previousData=""; //记录上一个步骤中的 返回的response data  即 ExpPackagePreviousItem【3】
														
														//每个步骤的  list 都不一定一样  
														randomStringList = new ArrayList();
														captchaList = new ArrayList();
														setParamsFromDataPackageList = new ArrayList();
														setGoAheadOrStopFromStatusList = new ArrayList();
														setGoAheadOrStopFromDataList = new ArrayList();
														useFileForCycleWithPackageList = new ArrayList();
														
														//fileForCycleWithEXP = "";
														
														
														//每个步骤中的 每个参数之间用 - 隔开
														String[] lineTxtTmp = StringForEachStep.split("-");
														//每个步骤的参数都是固定 13个
														int lineTxtTmpLength = lineTxtTmp.length;	                      
														if (lineTxtTmpLength == 13)
														{		              
															//先读取  CycleWithPackage  如果有  用来循环
															int useFileForCycleWithPackageTmpLength = lineTxtTmp[11].trim().split(":").length;
															
															if (useFileForCycleWithPackageTmpLength > 1)
															{
																// 有 CyclewithPackage 文件 
																
																
																String useFileForCycleWithPackageString = lineTxtTmp[11].trim().split(":")[1].trim();
																String[] useFileForCycleWithPackageItemStrings = useFileForCycleWithPackageString.split(",");
																//其实这边最多只有一个   参数  useFileForCycleWithPackageItemCount  如果有就是 1
																
																//存储格式     ： base64( 文件个数、 file1、file2、组合方式      )
																int useFileForCycleWithPackageItemCount = useFileForCycleWithPackageItemStrings.length;
																for (int k = 0; k < useFileForCycleWithPackageItemCount; k++)
																{
																	String[] useFileForCycleWithPackageItem = new String[6];
																	useFileForCycleWithPackageItem = Method.getFromBase64(useFileForCycleWithPackageItemStrings[k]).split(",");

																	// 1  说明 只有一个文件  
																	if (Integer.valueOf(useFileForCycleWithPackageItem[0]) == 1)
																	{
																		useFileForCycleWithPackageItem[1] = Method.getFromBase64(useFileForCycleWithPackageItem[1].trim());														
																	}
																	//2  两个文件  
																	else if(Integer.valueOf(useFileForCycleWithPackageItem[0]) == 2)
																	{
																		useFileForCycleWithPackageItem[1] = Method.getFromBase64(useFileForCycleWithPackageItem[1].trim());//file1
																		useFileForCycleWithPackageItem[2] = Method.getFromBase64(useFileForCycleWithPackageItem[2].trim());//file2
																		useFileForCycleWithPackageItem[3] = useFileForCycleWithPackageItem[3].trim();//组合方式
																	}
																	//最多两个文件
																	
																	//添加到 list 里面
																	useFileForCycleWithPackageList.add(useFileForCycleWithPackageItem);
																}
				                              
																
				                              
																//获取 list 的个数   其实 最多只有一个
																int useFileForCycleWithPackageListCount = useFileForCycleWithPackageList.size();
																
																for (int t = 0; t < useFileForCycleWithPackageListCount; t++)
																{
																	//获取 cycleWithPackage 参数
																	String[] useFileForCycleWithPackageItem = (String[])useFileForCycleWithPackageList.get(t);
																	// 一个文件 
																	if (Integer.valueOf(useFileForCycleWithPackageItem[0]) == 1)
																	{
																		// 一个文件  声明文件对象
																		File useFileForCycleWithPackageFile1 = new File(fileDir+useFileForCycleWithPackageItem[1]);
																		InputStreamReader useFileForCycleWithPackageFile1InputSteamReadTmp = new InputStreamReader(new FileInputStream(useFileForCycleWithPackageFile1));
																		BufferedReader useFileForCycleWithPackageFile1ExpFileBufferedReader = new BufferedReader(useFileForCycleWithPackageFile1InputSteamReadTmp);
																		String useFileForCycleWithPackageLineText = null;
																		
																		int PackageNum=0;//这个用来统计累计
																		
																		while ((useFileForCycleWithPackageLineText = useFileForCycleWithPackageFile1ExpFileBufferedReader.readLine()) != null)
																		{																
																			PackageNum++;
																			
																			//第一个参数   存储目标主机地址																	
																			if (lineTxtTmp[1].trim().split(":").length > 1)
																			{
																				host = Method.getFromBase64(lineTxtTmp[1].trim().split(":")[1]);																		
																			}
																			else
																			{
																				//没有主机地址的  直接跳过   到下一个步骤
																				break;
																			}

																			//第二个参数  存储目标端口
																			if (lineTxtTmp[2].trim().split(":").length > 1)
																			{
																				port = Integer.valueOf(Method.getFromBase64(lineTxtTmp[2].trim().split(":")[1]));
																			}
																			else
																			{
																				//没有目标端口的  默认为 80端口
																				port =80;
																			}

																			//第三个参数  存储 http请求的文件名
																			StepFileNameTmp = Method.getFromBase64(lineTxtTmp[3].trim().split(":")[1]);									
																			if(StepFileNameTmp.length()>0)
																			{
																				requestData = Method.bytesToString(Method.HexStringTobytes(Method.stringsToString(Method.readFileToHexStrings(fileDir+"\\"+StepFileNameTmp))));
																			}																
																			//如果目标通过映射访问    也就是 www.xxx.com/phpcms/   这个是跟目录的话   对 exp 来说  需要一个  url 前缀 phpcms/
																			if(previousURL.length()>0)
																			{
																				// 添加 phpcms/  到request 的 GET  phpcms/。。。。  http/1.1
																				requestData = Method.addPreviousURL(requestData,previousURL);
																			}
																										
																			//第四个参数  是否替换 host 和 port   默认都是替换 为目标地址 和端口的
																			String replaceHostPortString = lineTxtTmp[4].trim().split(":")[1].trim();
																			if (replaceHostPortString.equals("true"))
																			{
																				host = targetHost;
																				port = targetPort;
																				requestData = Method.replaceHostPort(requestData,host,port);
																			}
																			else
																			{
																				
																			}
																			
																			//第五个参数  是否使用前一个 response 的  set-cookie  登录的时候用    默认是 使用
																			String useSetCookieFromPreviousString = lineTxtTmp[5].trim().split(":")[1].trim();
																			if (useSetCookieFromPreviousString.equals("true"))
																			{
																				/*
																				previousData = "HTTP/1.1 200 OK"+"\r\n"+
																									  "Date: Sun, 30 Nov 2014 09:16:33 GMT"+"\r\n"+
																									  "Server: Apache/2.2.9 (APMServ) PHP/5.2.6+"+"\r\n"+
																									  "X-Powered-By: PHP/5.2.6"+"\r\n"+
																									  "Set-Cookie: SYSZC=d2d4a91a4e41bea2078b09c29cd6a44a"+"\r\n"+
																									  "Set-Cookie: SYSUSER=admin"+"\r\n"+
																									  "Set-Cookie: SYSNAME=%E7%B3%BB%E7%BB%9F%E7%AE%A1%E7%90%86%E5%91%98"+"\r\n"+
																									  "Set-Cookie: SYSUSERID=3"+"\r\n"+
																									  "Set-Cookie: SYSTM=1417338994"+"\r\n"+
																									  "Content-Length: 2"+"\r\n"+
																									  "Content-Type: text/html"+"\r\n"+
																									  "\r\n"+							  
																									  "OK";
				                                        						*/
				                                        
																				//获取 set-cookie的相关参数
																				String[] tmpmp = Method.getHTTPHeadersParams(previousData);	                                		
																				String setCookieStr = "";
																				try
																				{
																				for(int n=0;n<tmpmp.length;n++)
																				{
																					if(n%2==0)
																					{
																						if(tmpmp[n].startsWith("Set-Cookie"))
																						{
																							setCookieStr =setCookieStr+tmpmp[n+1].trim()+";";
																						}
																					}
																				}
																				String OKrequestData="";		                                		
																				String[] requestDatas = requestData.split("\r\n");
																				//cookie 中如果有参数一样的    要替换掉  比如 原本有 name=admin   而 set-cookie 有 name=manange  要替换
																				boolean addCookie = true;																		
																				for(int n=0;n<requestDatas.length;n++)
																				{		
																					requestDatas[n] = requestDatas[n]+"\r\n";
																					if(requestDatas[n].toLowerCase().trim().startsWith("cookie:"))
																					{
																						String[] tmpCookie = requestDatas[n].toLowerCase().trim().split("cookie:");
																						if(tmpCookie.length>1)
																						{
																							tmpCookie[1] = Method.replaceCookie(tmpCookie[1].trim(),setCookieStr);
																							requestDatas[n] = "Cookie: "+Method.stringsToString(tmpCookie)+"\r\n";
																							addCookie = false;
																						}
																					}
																				}
				                                    	  
																				//
																				if(addCookie)
																				{
																					OKrequestData =Method.addHeader(requestData,"Cookie: ",setCookieStr);			                                			
																				}
																				else
																				{
																					OKrequestData = Method.stringsToString(requestDatas);
																				}
																				requestData = OKrequestData;  
																				}
																				catch(Exception e5)
																				{
																					
																				}
				                                		
				                                		
																				 
				                                        
																			}
																			else
																			{
																				
																			}
				                                      
																			//第六个参数  存储随机字符串的相关参数  
																			int randomStringTmpLength = lineTxtTmp[6].trim().split(":").length;
																			if (randomStringTmpLength > 1)
																			{
																				String randomStringString = lineTxtTmp[6].trim().split(":")[1].trim();
																				String[] randomStringItemStrings = randomStringString.split(",");
																				int randomStringItemCount = randomStringItemStrings.length;
																				//根据设置随机字符串的  个数循环替换request
																				for (int k = 0; k < randomStringItemCount; k++)
																				{
																					//先读取出相关参数
																					String[] randomStringItem = new String[5];
																					randomStringItem[0] = Method.getFromBase64(randomStringItemStrings[k]).split(",")[0];
																					randomStringItem[1] = Method.getFromBase64(randomStringItemStrings[k]).split(",")[1];
																					randomStringItem[2] =Method.getFromBase64(randomStringItemStrings[k]).split(",")[2].substring(0, 1);
																					randomStringItem[4] =  Method.getFromBase64(randomStringItemStrings[k]).split(",")[3];
																					
																					
																					
																					//randomStringItem[1] ==3  即 选择的类型是  自定义随机数母本的值          
																					//存储随机数设置的格式                 位置（第几个）、类型 、长度             而当类型为3 的时候   长度是和 自定义的母本放在一样的  如          6qwer123  6为长度  后面的为母本
																					//
																					if (Integer.valueOf(randomStringItem[1])== 3)
																					{
																						randomStringItem[3] = randomStringItem[2].substring(1);
																						randomStringItem[2] = randomStringItem[2].substring(0, 1);
																					}

																					// rSI+Num    为随机字符串的位置  即第几个  
																					String strTmp = "rSI"+randomStringItem[0];
																					strTmp = Method.getBase64(strTmp);
																					strTmp = Method.addSignRegex(strTmp);
																					String randomStrTmp = "";
																					
																					switch(Integer.valueOf(randomStringItem[1]))
																					{
																						//0  数字
																						case 0:randomStrTmp =new RandomString().getRandomString(Integer.valueOf(randomStringItem[2]),"i");requestData =requestData.replaceAll(strTmp, randomStrTmp);break;
																						//字母
																						case 1:randomStrTmp =new RandomString().getRandomString(Integer.valueOf(randomStringItem[2]),"l");requestData =requestData.replaceAll(strTmp, randomStrTmp);break;
																						//数字和字母
																						case 2:randomStrTmp =new RandomString().getRandomString(Integer.valueOf(randomStringItem[2]),"il");requestData =requestData.replaceAll(strTmp, randomStrTmp);break;
																						case 3:randomStrTmp =new RandomString().getRandomStringFromDefineString(randomStringItem[3],Integer.valueOf(randomStringItem[2]));requestData =requestData.replaceAll(strTmp, randomStrTmp);break;																		
																					
																					
																					}
																					//存储到list 里面  其实没有用处  为了可能会用到
																					randomStringList.add(randomStringItem);
																				}
																				
																			}
				                                      		                                      	
																			//第七个参数  存储 验证码的相关参数
																			int captchaTmpLength = lineTxtTmp[7].trim().split(":").length;
																			if (captchaTmpLength > 1)
																			{
																				String captchaString = lineTxtTmp[7].trim().split(":")[1].trim();
																				String[] captchaItemStrings = captchaString.split(",");
																				int captchaItemCount = captchaItemStrings.length;
																				//根据设置 验证码的个数来  循环替换request
																				for (int k = 0; k < captchaItemCount; k++)
																				{
																					String[] captchaItem = new String[4];
																					captchaItem = Method.getFromBase64(captchaItemStrings[k]).split(",");
																					
																					captchaList.add(captchaItem);//这个list 同样也是没有用到  
																					
																					//sPFC+Num  验证码的累计个数
																					String captchaStrTmp = "sPFC"+captchaItem[0];
																					captchaStrTmp = Method.getBase64(captchaStrTmp);
																					captchaStrTmp = Method.addSignRegex(captchaStrTmp);
				                                            
																					//这里使用 uu 的云验证码  虽然速度慢了点  但又不是要批量  单个检测 不影响 效果
																					boolean status = UUAPI.checkAPI();

																					if (!status)//一定要的    
																					{
																						//System.out.print("API文件校验失败，无法使用打码服务");
																						JOptionPane.showMessageDialog(null, "API文件校验失败，无法使用打码服务", "Warning", 2);
																					}
																					else
																					{
																						final String ImgURL = "http://"+host+previousURL+captchaItem[1];  //获取验证码的url  
																					//	System.out.println(ImgURL);
																						final int CaptchaNumbersTextInt = Integer.valueOf(captchaItem[2]);//验证码的位数
																						final String saveFileName = "img/tmp.jpg";//临时图片保存的地址
																						
																						String saveAbsolutePath = Method.getImgFromURL(ImgURL,saveFileName);//获取图片  返回保存的绝对路径
																						if (saveAbsolutePath != null)
																						{
																							if (CaptchaNumbersTextInt != 0)
																							{
																								try 
																								{
																									//调用api   获取识别结果      1000 + CaptchaNumbersTextInt  为他们的一种格式      1000   验证码位数几位+几
																									String[] captchaResult = UUAPI.easyDecaptcha(saveFileName, 1000 + CaptchaNumbersTextInt);
																									if (captchaResult[1].length() > 0)
																									{
																										requestData = requestData.replaceAll(captchaStrTmp, captchaResult[1]);   
																									}
																								} 
																								catch (IOException eee) 
																								{																		
																									eee.printStackTrace();
																								}	                                                    
																							}
																							else if(CaptchaNumbersTextInt==0)
																							{
																								//这里会用  不确定的 验证码位数      
																							//	System.out.println("这里会用  不确定的 验证码位数  ");
																							}		                                      					
																						}																				
																					}		                                            
																				}
																			}
				                                                                             
				                                        
																			//这里替换 cyclewithExp      字符串 默认都是  FileForCycleWithEXP
																			String FileForCycleWithEXPStrTmp = "FileForCycleWithEXP";
																			FileForCycleWithEXPStrTmp = Method.getBase64(FileForCycleWithEXPStrTmp);
																			FileForCycleWithEXPStrTmp = Method.addSignRegex(FileForCycleWithEXPStrTmp);
																			requestData = requestData.replaceAll(FileForCycleWithEXPStrTmp, expFileLineTxt);
				                                      		
				                                        
				                                        
																			//这里替换   cyclewithpackage   字符串规则是  File-1  File-2  useFileForCycleWithPackageLineText
																			String useFileForCycleWithPackageLineStrTmp = "File-1";
																			useFileForCycleWithPackageLineStrTmp = Method.getBase64(useFileForCycleWithPackageLineStrTmp);
																			useFileForCycleWithPackageLineStrTmp = Method.addSignRegex(useFileForCycleWithPackageLineStrTmp);
																			requestData = requestData.replaceAll(useFileForCycleWithPackageLineStrTmp, useFileForCycleWithPackageLineText);
				                                      		
				                                      		
																			//发送http 数据
																			responseData = Method.bytesToString(Method.getHttPResponseData(host,port,requestData));
																		//	System.out.println(requestData);
																		//	System.out.println(responseData);
																			responseStatus = Method.getHttpResponseHeaderStatus(responseData);
																			
																			//第九个参数  存储 设置 go or stop  从 response status
																			int setGoAheadOrStopFromStatusTmpLength = lineTxtTmp[9].trim().split(":").length;
																			if (setGoAheadOrStopFromStatusTmpLength > 1)
																			{
																				String setGoAheadOrStopFromStatusString = lineTxtTmp[9].trim().split(":")[1].trim();
																				String[] setGoAheadOrStopFromStatusItemStrings = setGoAheadOrStopFromStatusString.split(",");
																				int setGoAheadOrStopFromStatusItemCount = setGoAheadOrStopFromStatusItemStrings.length;
																				//根据 设置规则的 个数  先取出来  放到 list 里面
																				for (int k = 0; k < setGoAheadOrStopFromStatusItemCount; k++)
																				{
																					String[] setGoAheadOrStopFromStatusItem = new String[2];
																					setGoAheadOrStopFromStatusItem = Method.getFromBase64(setGoAheadOrStopFromStatusItemStrings[k]).split(",");
																					setGoAheadOrStopFromStatusList.add(setGoAheadOrStopFromStatusItem);
																				}
																			}
																			
																			//根据 list 的个数
																			int setGoAheadOrStopFromStatusListCount = setGoAheadOrStopFromStatusList.size();
																			if (setGoAheadOrStopFromStatusListCount > 0)
																			{
																				//循环
																				for (int j = 0; j < setGoAheadOrStopFromStatusListCount; j++)
																				{
																					String[] setGoAheadOrStopFromStatusListItem = (String[])setGoAheadOrStopFromStatusList.get(j);
																					
																					//这个是根据  response status  来判断   如果相等  就匹配 触发规则
																					if (responseStatus == Integer.valueOf(setGoAheadOrStopFromStatusListItem[0]))
																					{
																						String actionForStatus = setGoAheadOrStopFromStatusListItem[1];	                                            			
				                                            			
																						//									      action
																						//								            ||
																						//
																						//                         GO ahead current   stop current 
																						///                                               ||
																						//																		
																						///											Go Exp    Stop Exp
																					
																						if(actionForStatus.equals("Go Ahead EXP"))
																						{
																							GoAheadExp  = true;
																							whyToStop = "responseStatus:" + responseStatus + " -- Rule; " + setGoAheadOrStopFromStatusListItem[0] + " --Action; "+ setGoAheadOrStopFromStatusListItem[1];
																							break;
																						}
																						else if(actionForStatus.equals("Stop EXP"))
																						{
																							StopExp = true;		                                            				
																							whyToStop = "responseStatus:" + responseStatus + " -- Rule; " + setGoAheadOrStopFromStatusListItem[0] + " --Action; "+ setGoAheadOrStopFromStatusListItem[1];
																							break;
																						}
																						else if(actionForStatus.equals("Go Ahead Current"))
																						{
																							GoAheadCurrent = true;
																							whyToStop = "responseStatus:" + responseStatus + " -- Rule; " + setGoAheadOrStopFromStatusListItem[0] + " --Action; "+ setGoAheadOrStopFromStatusListItem[1];
																							break;
																						}
																						else if(actionForStatus.equals("Stop Current"))//这个暂时没有想到用途
																						{
																							//StopCurrent = true;
																							//whyToStop = "responseStatus:" + responseStatus + " -- Rule" + setGoAheadOrStopFromStatusListItem[0] + setGoAheadOrStopFromStatusListItem[1];
																							//System.out.println(whyToStop);
																							//break;
																						}                                      			
																						
																					}
																				}
																			}
																			else
																			{
																				//没有   setGoAheadOrStopFromStatusListCount
																			}									
																																		
																			
																			if ((StopExp) )
																			{
																				
																				JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,expFileLineTxt,useFileForCycleWithPackageLineText,whyToStop,"show"});
																				ExpPackageHistoryItem[0] = StepFileNameTmp;
																				ExpPackageHistoryItem[1] = requestData;
																				ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																				previousData = responseData;
																				ExpPackageHistoryItem[3] = responseData;
																				ExpPackageHistoryItem[4] = whyToStop;
																				ExpPackageHistoryList.add(ExpPackageHistoryItem);
																				expHTTPHistoryList.add(ExpPackageHistoryItem);
																				ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																				ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																				ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																				ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																				ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																				ExpPackageHistoryItem = new String[7];
																				responseStatus = 0;
																				responseData = "";
																				whyToStop = "Run Over";
																				break;
																			}
				                                            
																			if(GoAheadCurrent)
																			{
																				JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,expFileLineTxt,useFileForCycleWithPackageLineText,whyToStop,"show"});
																				//当是  Go Aheade 的时候 就放弃循环 继续下一个   Step Num
																				ExpPackageHistoryItem[0] = StepFileNameTmp;
																				ExpPackageHistoryItem[1] = requestData;
																				ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																				previousData = responseData;
																				ExpPackageHistoryItem[3] = responseData;
																				ExpPackageHistoryItem[4] = whyToStop;
																				ExpPackageHistoryList.add(ExpPackageHistoryItem);
																				expHTTPHistoryList.add(ExpPackageHistoryItem);
																				ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																				ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																				ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																				ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																				ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																				ExpPackageHistoryItem = new String[7];
																				responseStatus = 0;
																				responseData = "";
																				whyToStop = "Run Over";
																				GoAheadCurrent = false;
																				break;
																			}
				                                            
																			if(GoAheadExp)
																			{
																				JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,expFileLineTxt,useFileForCycleWithPackageLineText,whyToStop,"show"});
																				ExpPackageHistoryItem[0] = StepFileNameTmp;
																				ExpPackageHistoryItem[1] = requestData;
																				ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																				previousData = responseData;
																				ExpPackageHistoryItem[3] = responseData;
																				ExpPackageHistoryItem[4] = whyToStop;
																				ExpPackageHistoryList.add(ExpPackageHistoryItem);
																				expHTTPHistoryList.add(ExpPackageHistoryItem);
																				ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																				ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																				ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																				ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																				ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																				ExpPackageHistoryItem = new String[7];
																				responseStatus = 0;
																				responseData = "";
																				whyToStop = "Run Over";
																				
																				break;
																			}

																	
																			//根据 response data  来判断 是否stop or go
																			int setGoAheadOrStopFromDataTmpLength = lineTxtTmp[10].trim().split(":").length;
																			if (setGoAheadOrStopFromDataTmpLength > 1)
																			{
																				String setGoAheadOrStopFromDataString = lineTxtTmp[10].trim().split(":")[1].trim();
																				String[] setGoAheadOrStopFromDataItemStrings = setGoAheadOrStopFromDataString.split(",");
																				int setGoAheadOrStopFromDataItemCount = setGoAheadOrStopFromDataItemStrings.length;
																				for (int k = 0; k < setGoAheadOrStopFromDataItemCount; k++)
																				{
																					String[] setGoAheadOrStopFromDataItem = new String[2];
																					setGoAheadOrStopFromDataItem = Method.getFromBase64(setGoAheadOrStopFromDataItemStrings[k]).split(",");
																					setGoAheadOrStopFromDataList.add(setGoAheadOrStopFromDataItem);																			
																				}
																			}
																			
																																			
																			int j;
																			int setGoAheadOrStopFromDataListCount = setGoAheadOrStopFromDataList.size();
																			if (setGoAheadOrStopFromDataListCount > 0)
																			{
																				for (j = 0; j < setGoAheadOrStopFromDataListCount; j++)
																				{
																					String[] setGoAheadOrStopFromDataListItem = (String[])setGoAheadOrStopFromDataList.get(j);
																					Pattern p=Pattern.compile(Method.getFromBase64(setGoAheadOrStopFromDataListItem[0]));
																					Matcher m=p.matcher(responseData);
																				//	System.out.println(m.find());
																					
																					if (m.find())
																					{
																						String actionForData = setGoAheadOrStopFromDataListItem[1];
																						
																						if(actionForData.equals("Go Ahead EXP"))
																						{
																							GoAheadExp  = true;
																							whyToStop = " -- Rule; " + Method.getFromBase64(setGoAheadOrStopFromDataListItem[0]) +"  --result; "+m.group()+ " --Action; "+setGoAheadOrStopFromDataListItem[1];
																							break;
																						}
																						else if(actionForData.equals("Stop EXP"))
																						{
																							StopExp = true;
																							whyToStop = " -- Rule; " + Method.getFromBase64(setGoAheadOrStopFromDataListItem[0]) +"  --result; "+m.group()+ " --Action; "+setGoAheadOrStopFromDataListItem[1];
																							break;
																						}
																						else if(actionForData.equals("Go Ahead Current"))
																						{
																							GoAheadCurrent = true;
																							whyToStop = " -- Rule; " + Method.getFromBase64(setGoAheadOrStopFromDataListItem[0]) +"  --result; "+m.group()+ " --Action; "+setGoAheadOrStopFromDataListItem[1];
																							break;
																						}
																						else if(actionForData.equals("Stop Current"))
																						{
																							//StopCurrent = true;
																							//whyToStop = "responseStatus:" + responseStatus + " -- Rule" + setGoAheadOrStopFromStatusListItem[0] + setGoAheadOrStopFromStatusListItem[1];
																							//System.out.println(whyToStop);
																							//break;
																						} 																			
																					}
																				}
																			}
																			else
																			{
																				// 没有  setGoAheadOrStopFromDataList");
																			}

																			if ((StopExp) )
																			{
																				JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,expFileLineTxt,useFileForCycleWithPackageLineText,whyToStop,"show"});
																				ExpPackageHistoryItem[0] = StepFileNameTmp;
																				ExpPackageHistoryItem[1] = requestData;
																				ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																				previousData = responseData;
																				ExpPackageHistoryItem[3] = responseData;
																				ExpPackageHistoryItem[4] = whyToStop;
																				ExpPackageHistoryList.add(ExpPackageHistoryItem);
																				expHTTPHistoryList.add(ExpPackageHistoryItem);
																				ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																				ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																				ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																				ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																				ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																				ExpPackageHistoryItem = new String[7];
																				responseStatus = 0;
																				responseData = "";
																				whyToStop = "Run Over";
																				
																				break;
																			}
				                                            
																			if(GoAheadCurrent)
																			{
																				JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,expFileLineTxt,useFileForCycleWithPackageLineText,whyToStop,"show"});
																				ExpPackageHistoryItem[0] = StepFileNameTmp;
																				ExpPackageHistoryItem[1] = requestData;
																				ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																				previousData = responseData;
																				ExpPackageHistoryItem[3] = responseData;
																				ExpPackageHistoryItem[4] = whyToStop;
																				ExpPackageHistoryList.add(ExpPackageHistoryItem);
																				expHTTPHistoryList.add(ExpPackageHistoryItem);
																				ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																				ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																				ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																				ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																				ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																				ExpPackageHistoryItem = new String[7];
																				responseStatus = 0;
																				responseData = "";
																				whyToStop = "Run Over";
																				GoAheadCurrent = false;
																				break;
																			}
				                                            
																			if(GoAheadExp)
																			{
																				JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,expFileLineTxt,useFileForCycleWithPackageLineText,whyToStop,"show"});
																				ExpPackageHistoryItem[0] = StepFileNameTmp;
																				ExpPackageHistoryItem[1] = requestData;
																				ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																				previousData = responseData;
																				ExpPackageHistoryItem[3] = responseData;
																				ExpPackageHistoryItem[4] = whyToStop;
																				ExpPackageHistoryList.add(ExpPackageHistoryItem);
																				expHTTPHistoryList.add(ExpPackageHistoryItem);
																				ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																				ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																				ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																				ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																				ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																				ExpPackageHistoryItem = new String[7];
																				responseStatus = 0;
																				responseData = "";
																				whyToStop = "Run Over";
																				break;
																			}

																			
																			JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,expFileLineTxt,useFileForCycleWithPackageLineText,whyToStop,"show"});
																			ExpPackageHistoryItem[0] = StepFileNameTmp;
																			ExpPackageHistoryItem[1] = requestData;
																			ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																			previousData = responseData;
																			ExpPackageHistoryItem[3] = responseData;
																			ExpPackageHistoryItem[4] = whyToStop;
																			ExpPackageHistoryList.add(ExpPackageHistoryItem);
																			expHTTPHistoryList.add(ExpPackageHistoryItem);
																			ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																			ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																			ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																			ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																			ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																			ExpPackageHistoryItem = new String[7];
																			responseStatus = 0;
																			responseData = "";
																			whyToStop = "Run Over";
				                                      	
																		}
																	}
																	// 两个 cyclewithpackage  
																	else if(Integer.valueOf(useFileForCycleWithPackageItem[0])==2)
																	{																
																		//组合方式1   一对一
																		if(Integer.valueOf(useFileForCycleWithPackageItem[3])==1)
																		{
																			//
																			File useFileForCycleWithPackageFile1 = new File(fileDir+useFileForCycleWithPackageItem[1]);
																			File useFileForCycleWithPackageFile2 = new File(fileDir+useFileForCycleWithPackageItem[2]);
																			
																			InputStreamReader useFileForCycleWithPackageFile1InputSteamReadTmp = new InputStreamReader(new FileInputStream(useFileForCycleWithPackageFile1));
																			InputStreamReader useFileForCycleWithPackageFile2InputSteamReadTmp = new InputStreamReader(new FileInputStream(useFileForCycleWithPackageFile2));
																			
																			BufferedReader useFileForCycleWithPackageFile1ExpFileBufferedReader = new BufferedReader(useFileForCycleWithPackageFile1InputSteamReadTmp);
																			BufferedReader useFileForCycleWithPackageFile2ExpFileBufferedReader = new BufferedReader(useFileForCycleWithPackageFile2InputSteamReadTmp);
																			
																			String useFileForCycleWithPackageLineText1 = null;
																			String useFileForCycleWithPackageLineText2 = null;
																			
																			int PackageNum=0;
																			
																			//组合方式1   一对一
																			while (((useFileForCycleWithPackageLineText1 = useFileForCycleWithPackageFile1ExpFileBufferedReader.readLine()) != null)&&((useFileForCycleWithPackageLineText2 = useFileForCycleWithPackageFile2ExpFileBufferedReader.readLine()) != null))
																			{								
																				PackageNum++;
																				
																				if (lineTxtTmp[1].trim().split(":").length > 1)
																				{
																					host = Method.getFromBase64(lineTxtTmp[1].trim().split(":")[1]);																			
																				}
																				else
																				{
																					
																					break;
																				}

																				
																				if (lineTxtTmp[2].trim().split(":").length > 1)
																				{
																					port = Integer.valueOf(Method.getFromBase64(lineTxtTmp[2].trim().split(":")[1]));
																					
																				}
																				else
																				{
																					port=80;
																				}

				                                      																	
																				StepFileNameTmp = Method.getFromBase64(lineTxtTmp[3].trim().split(":")[1]);									
																				if(StepFileNameTmp.length()>0)
																				{
																					requestData = Method.bytesToString(Method.HexStringTobytes(Method.stringsToString(Method.readFileToHexStrings(fileDir+"\\"+StepFileNameTmp))));
																				}
																						 
																				if(previousURL.length()>0)
																				{
																					// 添加 phpcms/  到request 的 GET  phpcms/。。。。  http/1.1
																					requestData = Method.addPreviousURL(requestData,previousURL);
																				}

																																						
																				String replaceHostPortString = lineTxtTmp[4].trim().split(":")[1].trim();
																				if (replaceHostPortString.equals("true"))
																				{
																					
																					host = targetHost;
																					port = targetPort;
																					requestData = Method.replaceHostPort(requestData,host,port);
																				}
																				else
																				{
																					
																				}
																				                                    
					                                      
																				String useSetCookieFromPreviousString = lineTxtTmp[5].trim().split(":")[1].trim();
																				if (useSetCookieFromPreviousString.equals("true"))
																				{
																					/*
																					previousData = "HTTP/1.1 200 OK"+"\r\n"+
																										  "Date: Sun, 30 Nov 2014 09:16:33 GMT"+"\r\n"+
																										  "Server: Apache/2.2.9 (APMServ) PHP/5.2.6+"+"\r\n"+
																										  "X-Powered-By: PHP/5.2.6"+"\r\n"+
																										  "Set-Cookie: SYSZC=d2d4a91a4e41bea2078b09c29cd6a44a"+"\r\n"+
																										  "Set-Cookie: SYSUSER=admin"+"\r\n"+
																										  "Set-Cookie: SYSNAME=%E7%B3%BB%E7%BB%9F%E7%AE%A1%E7%90%86%E5%91%98"+"\r\n"+
																										  "Set-Cookie: SYSUSERID=3"+"\r\n"+
																										  "Set-Cookie: SYSTM=1417338994"+"\r\n"+
																										  "Content-Length: 2"+"\r\n"+
																										  "Content-Type: text/html"+"\r\n"+
																										  "\r\n"+							  
																										  "OK";
					                                        
					                                        						*/
					                                        
																					String[] tmpmp = Method.getHTTPHeadersParams(previousData);
					                                		
																					String setCookieStr = "";
																					try
																					{
																					for(int n=0;n<tmpmp.length;n++)
																					{
																						if(n%2==0)
																						{																					
																							if(tmpmp[n].startsWith("Set-Cookie"))
																							{
																								setCookieStr =setCookieStr+tmpmp[n+1].trim()+";";
																							}
																						}
																					}
																					String OKrequestData="";			                                		
																					String[] requestDatas = requestData.split("\r\n");
																					boolean addCookie = true;
																					
																					for(int n=0;n<requestDatas.length;n++)
																					{		
																						requestDatas[n] = requestDatas[n]+"\r\n";
																						if(requestDatas[n].toLowerCase().trim().startsWith("cookie:"))
																						{																					
																							String[] tmpCookie = requestDatas[n].toLowerCase().trim().split("cookie:");
																							if(tmpCookie.length>1)
																							{																			
																								tmpCookie[1] = Method.replaceCookie(tmpCookie[1].trim(),setCookieStr);
																								requestDatas[n] = "Cookie: "+Method.stringsToString(tmpCookie)+"\r\n";
																								addCookie = false;
																							}
																						}
																					}
					                                    	  
																					if(addCookie)
																					{
																						OKrequestData =Method.addHeader(requestData,"Cookie: ",setCookieStr);
						                                			
																					}
																					else
																					{
																						OKrequestData = Method.stringsToString(requestDatas);
																					}
																					requestData = OKrequestData;  
																					}
																					catch(Exception e5)
																					{
																						
																					}
					                                		
																					 
					                                        
																				}
																				else
																				{
																					//useSetCookieFromPrevious = false;
																				}
																				
					                                      	
																				int randomStringTmpLength = lineTxtTmp[6].trim().split(":").length;
																				if (randomStringTmpLength > 1)
																				{
																					String randomStringString = lineTxtTmp[6].trim().split(":")[1].trim();
																					String[] randomStringItemStrings = randomStringString.split(",");
																					int randomStringItemCount = randomStringItemStrings.length;

																					for (int k = 0; k < randomStringItemCount; k++)
																					{
																						String[] randomStringItem = new String[5];
																						randomStringItem[0] = Method.getFromBase64(randomStringItemStrings[k]).split(",")[0];
																						randomStringItem[1] = Method.getFromBase64(randomStringItemStrings[k]).split(",")[1];
																						randomStringItem[2] =Method.getFromBase64(randomStringItemStrings[k]).split(",")[2].substring(0, 1);
																						randomStringItem[4] = Method.getFromBase64(randomStringItemStrings[k]).split(",")[3];
																						if (Integer.valueOf(randomStringItem[1]).intValue() == 3)
																						{
																							randomStringItem[3] = randomStringItem[2].substring(1);
																							randomStringItem[2] = randomStringItem[2].substring(0, 1);
																						}									
					                                        		
																						String strTmp = "rSI"+randomStringItem[0];
																						strTmp = Method.getBase64(strTmp);
																						strTmp = Method.addSignRegex(strTmp);
																						String randomStrTmp = "";
																						switch(Integer.valueOf(randomStringItem[1]))
																						{
																							case 0:randomStrTmp =new RandomString().getRandomString(Integer.valueOf(randomStringItem[2]),"i");requestData =requestData.replaceAll(strTmp, randomStrTmp);break;
																							case 1:randomStrTmp =new RandomString().getRandomString(Integer.valueOf(randomStringItem[2]),"l");requestData =requestData.replaceAll(strTmp, randomStrTmp);break;
																							case 2:randomStrTmp =new RandomString().getRandomString(Integer.valueOf(randomStringItem[2]),"il");requestData =requestData.replaceAll(strTmp, randomStrTmp);break;
																							case 3:randomStrTmp =new RandomString().getRandomStringFromDefineString(randomStringItem[3],Integer.valueOf(randomStringItem[2]));requestData =requestData.replaceAll(strTmp, randomStrTmp);break;																																																								
																						}
					                                        		
																						randomStringList.add(randomStringItem);
																						
																					}
																					
																				}			                                      																			
																				
																				int captchaTmpLength = lineTxtTmp[7].trim().split(":").length;
																				if (captchaTmpLength > 1)
																				{
																					String captchaString = lineTxtTmp[7].trim().split(":")[1].trim();
																					String[] captchaItemStrings = captchaString.split(",");
																					int captchaItemCount = captchaItemStrings.length;
																					for (int k = 0; k < captchaItemCount; k++)
																					{
																						String[] captchaItem = new String[4];
																						captchaItem = Method.getFromBase64(captchaItemStrings[k]).split(",");
																						captchaList.add(captchaItem);																			
																						
																						String captchaStrTmp = "sPFC"+captchaItem[0];
																						captchaStrTmp = Method.getBase64(captchaStrTmp);
																						captchaStrTmp = Method.addSignRegex(captchaStrTmp);
					                                            			                                            
																						boolean status = UUAPI.checkAPI();

																						if (!status)
																						{
																							JOptionPane.showMessageDialog(null, "API文件校验失败，无法使用打码服务", "Warning", 2);
																						}
																						else
																						{
																							final String ImgURL = "http://"+host+previousURL+captchaItem[1];  //获取验证码的url  
																						//	System.out.println(ImgURL);                                           
																							final int CaptchaNumbersTextInt = Integer.valueOf(captchaItem[2]);
																							final String saveFileName = "img/tmp.jpg";
																							
																							String saveAbsolutePath = Method.getImgFromURL(ImgURL,saveFileName);
																							if (saveAbsolutePath != null)
																							{
																								if (CaptchaNumbersTextInt != 0)
																								{
																									try 
																									{
																										String[] captchaResult = UUAPI.easyDecaptcha(saveFileName, 1000 + CaptchaNumbersTextInt);
																										if (captchaResult[1].length() > 0)
																										{
																											requestData = requestData.replaceAll(captchaStrTmp, captchaResult[1]);   
																										}
																									} 
																									catch (IOException eee) 
																									{																								
																										eee.printStackTrace();
																									}	                                                    
																								}			                                      					
																							}																				
																						}		                                            
																					}
																				}
					                                        		                                        
																				//
																				String FileForCycleWithEXPStrTmp = "FileForCycleWithEXP";
																				FileForCycleWithEXPStrTmp = Method.getBase64(FileForCycleWithEXPStrTmp);
																				FileForCycleWithEXPStrTmp = Method.addSignRegex(FileForCycleWithEXPStrTmp);
																				requestData = requestData.replaceAll(FileForCycleWithEXPStrTmp, expFileLineTxt);		                                      					                                        
					                                        
																				//useFileForCycleWithPackageLineText
																				String useFileForCycleWithPackageLineStrTmp1 = "File-1";
																				useFileForCycleWithPackageLineStrTmp1 = Method.getBase64(useFileForCycleWithPackageLineStrTmp1);
																				useFileForCycleWithPackageLineStrTmp1 = Method.addSignRegex(useFileForCycleWithPackageLineStrTmp1);
																				requestData = requestData.replaceAll(useFileForCycleWithPackageLineStrTmp1, useFileForCycleWithPackageLineText1);
																																					
																				String useFileForCycleWithPackageLineStrTmp2 = "File-2";
																				useFileForCycleWithPackageLineStrTmp2 = Method.getBase64(useFileForCycleWithPackageLineStrTmp2);
																				useFileForCycleWithPackageLineStrTmp2 = Method.addSignRegex(useFileForCycleWithPackageLineStrTmp2);
																				requestData = requestData.replaceAll(useFileForCycleWithPackageLineStrTmp2, useFileForCycleWithPackageLineText2);
					                                      		
					                                      		
																				// 发送http 数据
																				
																				responseData = Method.bytesToString(Method.getHttPResponseData(host,port,requestData));
																			//	System.out.println(requestData);
																			//	System.out.println("--");
																			//	System.out.println(responseData);
																				responseStatus = Method.getHttpResponseHeaderStatus(responseData);																
					                                      		
																				int setGoAheadOrStopFromStatusTmpLength = lineTxtTmp[9].trim().split(":").length;
																				if (setGoAheadOrStopFromStatusTmpLength > 1)
																				{
																					String setGoAheadOrStopFromStatusString = lineTxtTmp[9].trim().split(":")[1].trim();
																					String[] setGoAheadOrStopFromStatusItemStrings = setGoAheadOrStopFromStatusString.split(",");
																					int setGoAheadOrStopFromStatusItemCount = setGoAheadOrStopFromStatusItemStrings.length;
																					for (int k = 0; k < setGoAheadOrStopFromStatusItemCount; k++)
																					{
																						String[] setGoAheadOrStopFromStatusItem = new String[2];
																						setGoAheadOrStopFromStatusItem = Method.getFromBase64(setGoAheadOrStopFromStatusItemStrings[k]).split(",");
																						setGoAheadOrStopFromStatusList.add(setGoAheadOrStopFromStatusItem);								
																					}
																				}
					                                      				                                      		
																				int setGoAheadOrStopFromStatusListCount = setGoAheadOrStopFromStatusList.size();
																				if (setGoAheadOrStopFromStatusListCount > 0)
																				{
																					for (int j = 0; j < setGoAheadOrStopFromStatusListCount; j++)
																					{
																						String[] setGoAheadOrStopFromStatusListItem = (String[])setGoAheadOrStopFromStatusList.get(j);																				
																						if (responseStatus == Integer.valueOf(setGoAheadOrStopFromStatusListItem[0]))
																						{
																							String actionForStatus = setGoAheadOrStopFromStatusListItem[1];
					                                            			
					                                            			
																							//                         GO ahead current   stop current 
																							///                                               ||
																							//																		
																							///											Go Exp    Stop Exp
																							
																							
																							if(actionForStatus.equals("Go Ahead EXP"))
																							{
																								GoAheadExp  = true;
																								whyToStop = "responseStatus; " + responseStatus + " -- Rule; " + setGoAheadOrStopFromStatusListItem[0] + " --Action; "+ setGoAheadOrStopFromStatusListItem[1];
																								break;
																							}
																							else if(actionForStatus.equals("Stop EXP"))
																							{
																								StopExp = true;                         				
																								whyToStop = "responseStatus; " + responseStatus + " -- Rule; " + setGoAheadOrStopFromStatusListItem[0] + " --Action; "+ setGoAheadOrStopFromStatusListItem[1];
																								//System.out.println(whyToStop);
																								break;
																							}
																							else if(actionForStatus.equals("Go Ahead Current"))
																							{
																								GoAheadCurrent = true;
																								whyToStop = "responseStatus; " + responseStatus + " -- Rule; " + setGoAheadOrStopFromStatusListItem[0] + " --Action; "+ setGoAheadOrStopFromStatusListItem[1];
																								break;
																							}
																							else if(actionForStatus.equals("Stop Current"))
																							{
																								//StopCurrent = true;
																								//whyToStop = "responseStatus:" + responseStatus + " -- Rule" + setGoAheadOrStopFromStatusListItem[0] + setGoAheadOrStopFromStatusListItem[1];
																								//System.out.println(whyToStop);
																								//break;
																							}                                      																								
																						}
																					}
																				}
																				else
																				{
																				//	System.out.println("mei you setGoAheadOrStopFromStatusListCount");
																				}

																				if ((StopExp) )
																				{
																					JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,expFileLineTxt,useFileForCycleWithPackageLineText1+"-|-"+useFileForCycleWithPackageLineText2,whyToStop,"show"});
																					ExpPackageHistoryItem[0] = StepFileNameTmp;
																					ExpPackageHistoryItem[1] = requestData;
																					ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																					previousData = responseData;
																					ExpPackageHistoryItem[3] = responseData;
																					ExpPackageHistoryItem[4] = whyToStop;
																					ExpPackageHistoryList.add(ExpPackageHistoryItem);
																					expHTTPHistoryList.add(ExpPackageHistoryItem);
																					ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																					ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																					ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																					ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																					ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																					ExpPackageHistoryItem = new String[7];
																					responseStatus = 0;
																					responseData = "";
																					whyToStop = "Run Over";
																					break;
																				}
					                                            
																				if(GoAheadCurrent)
																				{
																					JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,expFileLineTxt,useFileForCycleWithPackageLineText1+"-|-"+useFileForCycleWithPackageLineText2,whyToStop,"show"});
																					ExpPackageHistoryItem[0] = StepFileNameTmp;
																					ExpPackageHistoryItem[1] = requestData;
																					ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																					previousData = responseData;
																					ExpPackageHistoryItem[3] = responseData;
																					ExpPackageHistoryItem[4] = whyToStop;
																					ExpPackageHistoryList.add(ExpPackageHistoryItem);
																					expHTTPHistoryList.add(ExpPackageHistoryItem);
																					ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																					ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																					ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																					ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																					ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																					ExpPackageHistoryItem = new String[7];
																					responseStatus = 0;
																					responseData = "";
																					whyToStop = "Run Over";
																					GoAheadCurrent = false;
																					break;
																				}
					                                            
																				if(GoAheadExp)
																				{
																					JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,expFileLineTxt,useFileForCycleWithPackageLineText1+"-|-"+useFileForCycleWithPackageLineText2,whyToStop,"show"});
																					ExpPackageHistoryItem[0] = StepFileNameTmp;
																					ExpPackageHistoryItem[1] = requestData;
																					ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																					previousData = responseData;
																					ExpPackageHistoryItem[3] = responseData;
																					ExpPackageHistoryItem[4] = whyToStop;
																					ExpPackageHistoryList.add(ExpPackageHistoryItem);
																					expHTTPHistoryList.add(ExpPackageHistoryItem);
																					ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																					ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																					ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																					ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																					ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																					ExpPackageHistoryItem = new String[7];
																					responseStatus = 0;
																					responseData = "";
																					whyToStop = "Run Over";
																					break;
																				}
					                                            
																				int setGoAheadOrStopFromDataTmpLength = lineTxtTmp[10].trim().split(":").length;
																				if (setGoAheadOrStopFromDataTmpLength > 1)
																				{
																					String setGoAheadOrStopFromDataString = lineTxtTmp[10].trim().split(":")[1].trim();
																					String[] setGoAheadOrStopFromDataItemStrings = setGoAheadOrStopFromDataString.split(",");
																					int setGoAheadOrStopFromDataItemCount = setGoAheadOrStopFromDataItemStrings.length;
																					for (int k = 0; k < setGoAheadOrStopFromDataItemCount; k++)
																					{
																						String[] setGoAheadOrStopFromDataItem = new String[2];
																						setGoAheadOrStopFromDataItem = Method.getFromBase64(setGoAheadOrStopFromDataItemStrings[k]).split(",");
																						setGoAheadOrStopFromDataList.add(setGoAheadOrStopFromDataItem);																			
																					}
																				}
																					                                            
																				
																				int j;
																				int setGoAheadOrStopFromDataListCount = setGoAheadOrStopFromDataList.size();
																				if (setGoAheadOrStopFromDataListCount > 0)
																				{
																					for (j = 0; j < setGoAheadOrStopFromDataListCount; j++)
																					{
																						String[] setGoAheadOrStopFromDataListItem = (String[])setGoAheadOrStopFromDataList.get(j);

																						Pattern p=Pattern.compile(Method.getFromBase64(setGoAheadOrStopFromDataListItem[0]));
																						Matcher m=p.matcher(responseData);
																					//	System.out.println(m.find());
																						
																						if (m.find())
																						{
																							String actionForData = setGoAheadOrStopFromDataListItem[1];
																							
																							if(actionForData.equals("Go Ahead EXP"))
																							{
																								GoAheadExp  = true;
																								whyToStop = " -- Rule; " + Method.getFromBase64(setGoAheadOrStopFromDataListItem[0]) +"  --result; "+m.group()+ " --Action; "+ setGoAheadOrStopFromDataListItem[1];
																								break;
																							}
																							else if(actionForData.equals("Stop EXP"))
																							{
																								StopExp = true;
																								whyToStop = " -- Rule; " + Method.getFromBase64(setGoAheadOrStopFromDataListItem[0]) +"  --result; "+m.group()+ " --Action; "+ setGoAheadOrStopFromDataListItem[1];
																								break;
																							}
																							else if(actionForData.equals("Go Ahead Current"))
																							{
																								GoAheadCurrent = true;
																								whyToStop = " -- Rule; " + Method.getFromBase64(setGoAheadOrStopFromDataListItem[0]) +"  --result; "+m.group()+ " --Action; "+ setGoAheadOrStopFromDataListItem[1];
																								break;
																							}
																							else if(actionForData.equals("Stop Current"))
																							{
																								//StopCurrent = true;
																								//whyToStop = "responseStatus:" + responseStatus + " -- Rule" + setGoAheadOrStopFromStatusListItem[0] + setGoAheadOrStopFromStatusListItem[1];
																								//System.out.println(whyToStop);
																								//break;
																							} 								
																						}
																					}
																				}
																				else
																				{
																				//	System.out.println("meiyou setGoAheadOrStopFromDataList");
																				}

																				if ((StopExp) )
																				{
																					JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,expFileLineTxt,useFileForCycleWithPackageLineText1+"-|-"+useFileForCycleWithPackageLineText2,whyToStop,"show"});
																					ExpPackageHistoryItem[0] = StepFileNameTmp;
																					ExpPackageHistoryItem[1] = requestData;
																					ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																					previousData = responseData;
																					ExpPackageHistoryItem[3] = responseData;
																					ExpPackageHistoryItem[4] = whyToStop;
																					ExpPackageHistoryList.add(ExpPackageHistoryItem);
																					expHTTPHistoryList.add(ExpPackageHistoryItem);
																					ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																					ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																					ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																					ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																					ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																					ExpPackageHistoryItem = new String[7];
																					responseStatus = 0;
																					responseData = "";
																					whyToStop = "Run Over";
																					break;
																				}
					                                            
																				if(GoAheadCurrent)
																				{
																					JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,expFileLineTxt,useFileForCycleWithPackageLineText1+"-|-"+useFileForCycleWithPackageLineText2,whyToStop,"show"});
																					ExpPackageHistoryItem[0] = StepFileNameTmp;
																					ExpPackageHistoryItem[1] = requestData;
																					ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																					previousData = responseData;
																					ExpPackageHistoryItem[3] = responseData;
																					ExpPackageHistoryItem[4] = whyToStop;
																					ExpPackageHistoryList.add(ExpPackageHistoryItem);
																					expHTTPHistoryList.add(ExpPackageHistoryItem);
																					ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																					ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																					ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																					ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																					ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																					ExpPackageHistoryItem = new String[7];
																					responseStatus = 0;
																					responseData = "";
																					whyToStop = "Run Over";
																					GoAheadCurrent = false;
																					break;
																				}
					                                            
																				if(GoAheadExp)
																				{
																					JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,expFileLineTxt,useFileForCycleWithPackageLineText1+"-|-"+useFileForCycleWithPackageLineText2,whyToStop,"show"});
																					ExpPackageHistoryItem[0] = StepFileNameTmp;
																					ExpPackageHistoryItem[1] = requestData;
																					ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																					previousData = responseData;
																					ExpPackageHistoryItem[3] = responseData;
																					ExpPackageHistoryItem[4] = whyToStop;
																					ExpPackageHistoryList.add(ExpPackageHistoryItem);
																					expHTTPHistoryList.add(ExpPackageHistoryItem);
																					ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																					ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																					ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																					ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																					ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																					ExpPackageHistoryItem = new String[7];
																					responseStatus = 0;
																					responseData = "";
																					whyToStop = "Run Over";
																					break;
																				}

																				JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,expFileLineTxt,useFileForCycleWithPackageLineText1+"-|-"+useFileForCycleWithPackageLineText2,whyToStop,"show"});
																				ExpPackageHistoryItem[0] = StepFileNameTmp;
																				ExpPackageHistoryItem[1] = requestData;
																				ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																				previousData = responseData;
																				ExpPackageHistoryItem[3] = responseData;
																				ExpPackageHistoryItem[4] = whyToStop;
																				ExpPackageHistoryList.add(ExpPackageHistoryItem);
																				expHTTPHistoryList.add(ExpPackageHistoryItem);
																				ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																				ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																				ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																				ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																				ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																				ExpPackageHistoryItem = new String[7];
																				responseStatus = 0;
																				responseData = "";
																				whyToStop = "Run Over";
																			
																			}
																		}
																		//组合方式二  一对多
																		else if(Integer.valueOf(useFileForCycleWithPackageItem[3])==2)
																		{
																			
																			File useFileForCycleWithPackageFile1 = new File(fileDir+useFileForCycleWithPackageItem[1]);																																
																			InputStreamReader useFileForCycleWithPackageFile1InputSteamReadTmp = new InputStreamReader(new FileInputStream(useFileForCycleWithPackageFile1));																															
																			BufferedReader useFileForCycleWithPackageFile1ExpFileBufferedReader = new BufferedReader(useFileForCycleWithPackageFile1InputSteamReadTmp);																															
																			String useFileForCycleWithPackageLineText1 = null;
														
																			int PackageNum=0;
																			
																			while (((useFileForCycleWithPackageLineText1 = useFileForCycleWithPackageFile1ExpFileBufferedReader.readLine()) != null))
																			{																	
																				File useFileForCycleWithPackageFile2 = new File(fileDir+useFileForCycleWithPackageItem[2]);
																				InputStreamReader useFileForCycleWithPackageFile2InputSteamReadTmp = new InputStreamReader(new FileInputStream(useFileForCycleWithPackageFile2));
																				BufferedReader useFileForCycleWithPackageFile2ExpFileBufferedReader = new BufferedReader(useFileForCycleWithPackageFile2InputSteamReadTmp);
																				String useFileForCycleWithPackageLineText2 = null;
																				while(((useFileForCycleWithPackageLineText2 = useFileForCycleWithPackageFile2ExpFileBufferedReader.readLine()) != null))
																				{																		
																					
																					PackageNum++;
																					if (lineTxtTmp[1].trim().split(":").length > 1)
																					{
																						host = Method.getFromBase64(lineTxtTmp[1].trim().split(":")[1]);																		
																					}
																					else
																					{
																						break;
																					}

																					
																					if (lineTxtTmp[2].trim().split(":").length > 1)
																					{
																						port = Integer.valueOf(Method.getFromBase64(lineTxtTmp[2].trim().split(":")[1]));
																					}
																					else
																					{
																						port= 80;
																					}
			                                      				                                      
																					
																					StepFileNameTmp = Method.getFromBase64(lineTxtTmp[3].trim().split(":")[1]);									
																					if(StepFileNameTmp.length()>0)
																					{
																						requestData = Method.bytesToString(Method.HexStringTobytes(Method.stringsToString(Method.readFileToHexStrings(fileDir+"\\"+StepFileNameTmp))));
																					}
																					
																					
																					if(previousURL.length()>0)
																					{
																						// 添加 phpcms/  到request 的 GET  phpcms/。。。。  http/1.1
																						requestData = Method.addPreviousURL(requestData,previousURL);
																					}
						                                     
						                                      
																					String replaceHostPortString = lineTxtTmp[4].trim().split(":")[1].trim();
																					if (replaceHostPortString.equals("true"))
																					{																				
																						host = targetHost;
																						port = targetPort;
																						requestData = Method.replaceHostPort(requestData,host,port);
																					}
																					else
																					{
																						
																					}
						                                      
						                                      
																					String useSetCookieFromPreviousString = lineTxtTmp[5].trim().split(":")[1].trim();
																					if (useSetCookieFromPreviousString.equals("true"))
																					{
																						/*
																						previousData = "HTTP/1.1 200 OK"+"\r\n"+
																											  "Date: Sun, 30 Nov 2014 09:16:33 GMT"+"\r\n"+
																											  "Server: Apache/2.2.9 (APMServ) PHP/5.2.6+"+"\r\n"+
																											  "X-Powered-By: PHP/5.2.6"+"\r\n"+
																											  "Set-Cookie: SYSZC=d2d4a91a4e41bea2078b09c29cd6a44a"+"\r\n"+
																											  "Set-Cookie: SYSUSER=admin"+"\r\n"+
																											  "Set-Cookie: SYSNAME=%E7%B3%BB%E7%BB%9F%E7%AE%A1%E7%90%86%E5%91%98"+"\r\n"+
																											  "Set-Cookie: SYSUSERID=3"+"\r\n"+
																											  "Set-Cookie: SYSTM=1417338994"+"\r\n"+
																											  "Content-Length: 2"+"\r\n"+
																											  "Content-Type: text/html"+"\r\n"+
																											  "\r\n"+							  
																											  "OK";
						                                        
						                                        						*/
																						String[] tmpmp = Method.getHTTPHeadersParams(previousData);
						                                		
																						String setCookieStr = "";
																						try
																						{
																						for(int n=0;n<tmpmp.length;n++)
																						{
																							if(n%2==0)
																							{
																								if(tmpmp[n].startsWith("Set-Cookie"))
																								{
																									setCookieStr =setCookieStr+tmpmp[n+1].trim()+";";
																								}
																							}
																						}
																						String OKrequestData="";		                                		
																						String[] requestDatas = requestData.split("\r\n");
																						boolean addCookie = true;
																						
																						for(int n=0;n<requestDatas.length;n++)
																						{		
																							requestDatas[n] = requestDatas[n]+"\r\n";
																							if(requestDatas[n].toLowerCase().trim().startsWith("cookie:"))
																							{
																								String[] tmpCookie = requestDatas[n].toLowerCase().trim().split("cookie:");
																								if(tmpCookie.length>1)
																								{
																									tmpCookie[1] = Method.replaceCookie(tmpCookie[1].trim(),setCookieStr);
																									requestDatas[n] = "Cookie: "+Method.stringsToString(tmpCookie)+"\r\n";
																									addCookie = false;
																								}
																							}
																						}
						                                    	  
																						if(addCookie)
																						{
																							OKrequestData =Method.addHeader(requestData,"Cookie: ",setCookieStr);				                                			
																						}
																						else
																						{
																							OKrequestData = Method.stringsToString(requestDatas);
																						}
																						requestData = OKrequestData;   
																						}
																						catch(Exception e5)
																						{
																							
																						}
						                                		
																						
						                                        
																					}
																					else
																					{
																						
																					}
																																							
						                                      	
																					int randomStringTmpLength = lineTxtTmp[6].trim().split(":").length;
																					if (randomStringTmpLength > 1)
																					{
																						String randomStringString = lineTxtTmp[6].trim().split(":")[1].trim();
																						String[] randomStringItemStrings = randomStringString.split(",");
																						int randomStringItemCount = randomStringItemStrings.length;
																						for (int k = 0; k < randomStringItemCount; k++)
																						{
																							String[] randomStringItem = new String[5];
																							randomStringItem[0] = Method.getFromBase64(randomStringItemStrings[k]).split(",")[0];
																							randomStringItem[1] = Method.getFromBase64(randomStringItemStrings[k]).split(",")[1];
																							randomStringItem[2] =Method.getFromBase64(randomStringItemStrings[k]).split(",")[2].substring(0, 1);
																							randomStringItem[4] = Method.getFromBase64(randomStringItemStrings[k]).split(",")[3];
																				
																							if (Integer.valueOf(randomStringItem[1]).intValue() == 3)
																							{
																								randomStringItem[3] = randomStringItem[2].substring(1);
																								randomStringItem[2] = randomStringItem[2].substring(0, 1);
																							}																					
						                                        		
																							String strTmp = "rSI"+randomStringItem[0];
																							strTmp = Method.getBase64(strTmp);
																							strTmp = Method.addSignRegex(strTmp);
																							String randomStrTmp = "";
																							switch(Integer.valueOf(randomStringItem[1]))
																							{
																								case 0:randomStrTmp =new RandomString().getRandomString(Integer.valueOf(randomStringItem[2]),"i");requestData =requestData.replaceAll(strTmp, randomStrTmp);break;
																								case 1:randomStrTmp =new RandomString().getRandomString(Integer.valueOf(randomStringItem[2]),"l");requestData =requestData.replaceAll(strTmp, randomStrTmp);break;
																								case 2:randomStrTmp =new RandomString().getRandomString(Integer.valueOf(randomStringItem[2]),"il");requestData =requestData.replaceAll(strTmp, randomStrTmp);break;
																								case 3:randomStrTmp =new RandomString().getRandomStringFromDefineString(randomStringItem[3],Integer.valueOf(randomStringItem[2]));requestData =requestData.replaceAll(strTmp, randomStrTmp);break;																		
																							
																							
																							}				                                        		
																							randomStringList.add(randomStringItem);
																							
																						}
																						
																					}
						                                      	
																					
																					int captchaTmpLength = lineTxtTmp[7].trim().split(":").length;
																					if (captchaTmpLength > 1)
																					{
																						String captchaString = lineTxtTmp[7].trim().split(":")[1].trim();
																						String[] captchaItemStrings = captchaString.split(",");
																						int captchaItemCount = captchaItemStrings.length;
																						for (int k = 0; k < captchaItemCount; k++)
																						{
																							String[] captchaItem = new String[3];
																							captchaItem = Method.getFromBase64(captchaItemStrings[k]).split(",");
																							captchaList.add(captchaItem);																					
																							
																							String captchaStrTmp = "sPFC"+captchaItem[0];
																							captchaStrTmp = Method.getBase64(captchaStrTmp);
																							captchaStrTmp = Method.addSignRegex(captchaStrTmp);				                                            
						                                            
																							boolean status = UUAPI.checkAPI();

																							if (!status)
																							{
																								JOptionPane.showMessageDialog(null, "API文件校验失败，无法使用打码服务", "Warning", 2);
																							}
																							else
																							{
																								final String ImgURL = "http://"+host+previousURL+captchaItem[1];  //获取验证码的url  
																						//		System.out.println(ImgURL);                                         
																								final int CaptchaNumbersTextInt = Integer.valueOf(captchaItem[2]);
																								final String saveFileName = "img/tmp.jpg";
																								
																								String saveAbsolutePath = Method.getImgFromURL(ImgURL,saveFileName);
																								if (saveAbsolutePath != null)
																								{
																									if (CaptchaNumbersTextInt != 0)
																									{
																										try 
																										{
																											String[] captchaResult = UUAPI.easyDecaptcha(saveFileName, 1000 + CaptchaNumbersTextInt);
																											if (captchaResult[1].length() > 0)
																											{
																												requestData = requestData.replaceAll(captchaStrTmp, captchaResult[1]);   
																											}
																										} 
																										catch (IOException eee) 
																										{																								
																											eee.printStackTrace();
																										}	                                                    
																									}				                                      					
																								}																						
																							}				                                            
																						}
																					}
						                                        
						                                        
																					//
																					String FileForCycleWithEXPStrTmp = "FileForCycleWithEXP";
																					FileForCycleWithEXPStrTmp = Method.getBase64(FileForCycleWithEXPStrTmp);
																					FileForCycleWithEXPStrTmp = Method.addSignRegex(FileForCycleWithEXPStrTmp);
																					requestData = requestData.replaceAll(FileForCycleWithEXPStrTmp, expFileLineTxt);
						                                      		
						                                        
						                                        
																					//useFileForCycleWithPackageLineText
																					String useFileForCycleWithPackageLineStrTmp1 = "File-1";
																					useFileForCycleWithPackageLineStrTmp1 = Method.getBase64(useFileForCycleWithPackageLineStrTmp1);
																					useFileForCycleWithPackageLineStrTmp1 = Method.addSignRegex(useFileForCycleWithPackageLineStrTmp1);
																					requestData = requestData.replaceAll(useFileForCycleWithPackageLineStrTmp1, useFileForCycleWithPackageLineText1);
																					
																					
																					String useFileForCycleWithPackageLineStrTmp2 = "File-2";
																					useFileForCycleWithPackageLineStrTmp2 = Method.getBase64(useFileForCycleWithPackageLineStrTmp2);
																					useFileForCycleWithPackageLineStrTmp2 = Method.addSignRegex(useFileForCycleWithPackageLineStrTmp2);
																					requestData = requestData.replaceAll(useFileForCycleWithPackageLineStrTmp2, useFileForCycleWithPackageLineText2);
						                                      		
						                                      		
																					// 发送http 数据
																					
																					responseData = Method.bytesToString(Method.getHttPResponseData(host,port,requestData));
																				//	System.out.println(requestData);
																				//	System.out.println("--");
																				//	System.out.println(responseData);
																					responseStatus = Method.getHttpResponseHeaderStatus(responseData);
																																							
						                                      		
																					int setGoAheadOrStopFromStatusTmpLength = lineTxtTmp[9].trim().split(":").length;
																					if (setGoAheadOrStopFromStatusTmpLength > 1)
																					{
																						String setGoAheadOrStopFromStatusString = lineTxtTmp[9].trim().split(":")[1].trim();
																						String[] setGoAheadOrStopFromStatusItemStrings = setGoAheadOrStopFromStatusString.split(",");
																						int setGoAheadOrStopFromStatusItemCount = setGoAheadOrStopFromStatusItemStrings.length;
																						for (int k = 0; k < setGoAheadOrStopFromStatusItemCount; k++)
																						{
																							String[] setGoAheadOrStopFromStatusItem = new String[2];
																							setGoAheadOrStopFromStatusItem = Method.getFromBase64(setGoAheadOrStopFromStatusItemStrings[k]).split(",");
																							setGoAheadOrStopFromStatusList.add(setGoAheadOrStopFromStatusItem);										
																						}
																					}
						                                      				                                      		
																					int setGoAheadOrStopFromStatusListCount = setGoAheadOrStopFromStatusList.size();
																					if (setGoAheadOrStopFromStatusListCount > 0)
																					{
																						for (int j = 0; j < setGoAheadOrStopFromStatusListCount; j++)
																						{
																							String[] setGoAheadOrStopFromStatusListItem = (String[])setGoAheadOrStopFromStatusList.get(j);															
																							if (responseStatus == Integer.valueOf(setGoAheadOrStopFromStatusListItem[0]))
																							{
																								String actionForStatus = setGoAheadOrStopFromStatusListItem[1];
						                                            			
						                                            			
																								//                         GO ahead current   stop current 
																								///                                               ||
																								//																		
																								///											Go Exp    Stop Exp
																								///									
																								
																								if(actionForStatus.equals("Go Ahead EXP"))
																								{
																									GoAheadExp  = true;
																									whyToStop = "responseStatus; " + responseStatus + " -- Rule; " + setGoAheadOrStopFromStatusListItem[0] + " --Action; "+ setGoAheadOrStopFromStatusListItem[1];
																									break;
																								}
																								else if(actionForStatus.equals("Stop EXP"))
																								{
																									StopExp = true;                                    				
																									whyToStop = "responseStatus; " + responseStatus + " -- Rule; " + setGoAheadOrStopFromStatusListItem[0] + " --Action; "+ setGoAheadOrStopFromStatusListItem[1];
																									break;
																								}
																								else if(actionForStatus.equals("Go Ahead Current"))
																								{
																									GoAheadCurrent = true;
																									whyToStop = "responseStatus; " + responseStatus + " -- Rule; " + setGoAheadOrStopFromStatusListItem[0] + " --Action; "+ setGoAheadOrStopFromStatusListItem[1];
																									break;
																								}
																								else if(actionForStatus.equals("Stop Current"))
																								{
																									//StopCurrent = true;
																									//whyToStop = "responseStatus:" + responseStatus + " -- Rule" + setGoAheadOrStopFromStatusListItem[0] + setGoAheadOrStopFromStatusListItem[1];
																									//System.out.println(whyToStop);
																									//break;
																								}                                      			
																								
																							}
																						}
																					}
																					else
																					{
																						//System.out.println("mei you setGoAheadOrStopFromStatusListCount");
																					}

																					if ((StopExp) )
																					{
																						JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,expFileLineTxt,useFileForCycleWithPackageLineText1+"-|-"+useFileForCycleWithPackageLineText2,whyToStop,"show"});
																						ExpPackageHistoryItem[0] = StepFileNameTmp;
																						ExpPackageHistoryItem[1] = requestData;
																						ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																						previousData = responseData;
																						ExpPackageHistoryItem[3] = responseData;
																						ExpPackageHistoryItem[4] = whyToStop;
																						ExpPackageHistoryList.add(ExpPackageHistoryItem);
																						expHTTPHistoryList.add(ExpPackageHistoryItem);
																						ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																						ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																						ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																						ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																						ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																						ExpPackageHistoryItem = new String[7];
																						responseStatus = 0;
																						responseData = "";
																						whyToStop = "Run Over";
																						break;
																					}
						                                            
																					if(GoAheadCurrent)
																					{
																						JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,expFileLineTxt,useFileForCycleWithPackageLineText1+"-|-"+useFileForCycleWithPackageLineText2,whyToStop,"show"});
																						ExpPackageHistoryItem[0] = StepFileNameTmp;
																						ExpPackageHistoryItem[1] = requestData;
																						ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																						previousData = responseData;
																						ExpPackageHistoryItem[3] = responseData;
																						ExpPackageHistoryItem[4] = whyToStop;
																						ExpPackageHistoryList.add(ExpPackageHistoryItem);
																						expHTTPHistoryList.add(ExpPackageHistoryItem);
																						ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																						ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																						ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																						ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																						ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																						ExpPackageHistoryItem = new String[7];
																						responseStatus = 0;
																						responseData = "";
																						whyToStop = "Run Over";
																						GoAheadCurrent = false;
																						break;
																					}
						                                            
																					if(GoAheadExp)
																					{
																						JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,expFileLineTxt,useFileForCycleWithPackageLineText1+"-|-"+useFileForCycleWithPackageLineText2,whyToStop,"show"});
																						ExpPackageHistoryItem[0] = StepFileNameTmp;
																						ExpPackageHistoryItem[1] = requestData;
																						ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																						previousData = responseData;
																						ExpPackageHistoryItem[3] = responseData;
																						ExpPackageHistoryItem[4] = whyToStop;
																						ExpPackageHistoryList.add(ExpPackageHistoryItem);
																						expHTTPHistoryList.add(ExpPackageHistoryItem);
																						ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																						ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																						ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																						ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																						ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																						ExpPackageHistoryItem = new String[7];
																						responseStatus = 0;
																						responseData = "";
																						whyToStop = "Run Over";
																						break;
																					}
						                                            																																					
																					
																					int setGoAheadOrStopFromDataTmpLength = lineTxtTmp[10].trim().split(":").length;
																					if (setGoAheadOrStopFromDataTmpLength > 1)
																					{
																						String setGoAheadOrStopFromDataString = lineTxtTmp[10].trim().split(":")[1].trim();
																						String[] setGoAheadOrStopFromDataItemStrings = setGoAheadOrStopFromDataString.split(",");
																						int setGoAheadOrStopFromDataItemCount = setGoAheadOrStopFromDataItemStrings.length;
																						for (int k = 0; k < setGoAheadOrStopFromDataItemCount; k++)
																						{
																							String[] setGoAheadOrStopFromDataItem = new String[2];
																							setGoAheadOrStopFromDataItem = Method.getFromBase64(setGoAheadOrStopFromDataItemStrings[k]).split(",");
																							setGoAheadOrStopFromDataList.add(setGoAheadOrStopFromDataItem);										
																						}
																					}
																					
																		
																					int j;																	
																					int setGoAheadOrStopFromDataListCount = setGoAheadOrStopFromDataList.size();
																					if (setGoAheadOrStopFromDataListCount > 0)
																					{
																						for (j = 0; j < setGoAheadOrStopFromDataListCount; j++)
																						{
																							String[] setGoAheadOrStopFromDataListItem = (String[])setGoAheadOrStopFromDataList.get(j);

																							Pattern p=Pattern.compile(Method.getFromBase64(setGoAheadOrStopFromDataListItem[0]));
																							Matcher m=p.matcher(responseData);
																						//	System.out.println(m.find());
																							
																							if (m.find())
																							{
																								String actionForData = setGoAheadOrStopFromDataListItem[1];
																								
																								if(actionForData.equals("Go Ahead EXP"))
																								{
																									GoAheadExp  = true;
																									whyToStop = " -- Rule; " + Method.getFromBase64(setGoAheadOrStopFromDataListItem[0]) +"  --result; "+m.group()+ " --Action; "+ setGoAheadOrStopFromDataListItem[1];
																									break;
																								}
																								else if(actionForData.equals("Stop EXP"))
																								{
																									StopExp = true;
																									whyToStop = " -- Rule; " + Method.getFromBase64(setGoAheadOrStopFromDataListItem[0]) +"  --result; "+m.group()+ " --Action; "+ setGoAheadOrStopFromDataListItem[1];
																									break;
																								}
																								else if(actionForData.equals("Go Ahead Current"))
																								{
																									GoAheadCurrent = true;
																									whyToStop = " -- Rule; " + Method.getFromBase64(setGoAheadOrStopFromDataListItem[0]) +"  --result; "+m.group()+ " --Action; "+ setGoAheadOrStopFromDataListItem[1];
																									break;
																								}
																								else if(actionForData.equals("Stop Current"))
																								{
																									//StopCurrent = true;
																									//whyToStop = "responseStatus:" + responseStatus + " -- Rule" + setGoAheadOrStopFromStatusListItem[0] + setGoAheadOrStopFromStatusListItem[1];
																									//System.out.println(whyToStop);
																									//break;
																								} 
																								
																							}
																						}
																					}
																					else
																					{
																						//System.out.println("meiyou setGoAheadOrStopFromDataList");
																					}

																					if ((StopExp) )
																					{
																						JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,expFileLineTxt,useFileForCycleWithPackageLineText1+"-|-"+useFileForCycleWithPackageLineText2,whyToStop,"show"});
																						ExpPackageHistoryItem[0] = StepFileNameTmp;
																						ExpPackageHistoryItem[1] = requestData;
																						ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																						previousData = responseData;
																						ExpPackageHistoryItem[3] = responseData;
																						ExpPackageHistoryItem[4] = whyToStop;
																						ExpPackageHistoryList.add(ExpPackageHistoryItem);
																						expHTTPHistoryList.add(ExpPackageHistoryItem);
																						ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																						ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																						ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																						ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																						ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																						ExpPackageHistoryItem = new String[7];
																						responseStatus = 0;
																						responseData = "";
																						whyToStop = "Run Over";
																						break;
																					}
						                                            
																					if(GoAheadCurrent)
																					{
																						JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,expFileLineTxt,useFileForCycleWithPackageLineText1+"-|-"+useFileForCycleWithPackageLineText2,whyToStop,"show"});
																						ExpPackageHistoryItem[0] = StepFileNameTmp;
																						ExpPackageHistoryItem[1] = requestData;
																						ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																						previousData = responseData;
																						ExpPackageHistoryItem[3] = responseData;
																						ExpPackageHistoryItem[4] = whyToStop;
																						ExpPackageHistoryList.add(ExpPackageHistoryItem);
																						expHTTPHistoryList.add(ExpPackageHistoryItem);
																						ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																						ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																						ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																						ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																						ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																						ExpPackageHistoryItem = new String[7];
																						responseStatus = 0;
																						responseData = "";
																						whyToStop = "Run Over";
																						GoAheadCurrent = false;
																						break;
																					}
						                                            
																					if(GoAheadExp)
																					{
																						JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,expFileLineTxt,useFileForCycleWithPackageLineText1+"-|-"+useFileForCycleWithPackageLineText2,whyToStop,"show"});
																						ExpPackageHistoryItem[0] = StepFileNameTmp;
																						ExpPackageHistoryItem[1] = requestData;
																						ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																						previousData = responseData;
																						ExpPackageHistoryItem[3] = responseData;
																						ExpPackageHistoryItem[4] = whyToStop;
																						ExpPackageHistoryList.add(ExpPackageHistoryItem);
																						expHTTPHistoryList.add(ExpPackageHistoryItem);
																						ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																						ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																						ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																						ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																						ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																						ExpPackageHistoryItem = new String[7];
																						responseStatus = 0;
																						responseData = "";
																						whyToStop = "Run Over";
																						break;
																					}
																					JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,expFileLineTxt,useFileForCycleWithPackageLineText1+"-|-"+useFileForCycleWithPackageLineText2,whyToStop,"show"});
																					ExpPackageHistoryItem[0] = StepFileNameTmp;
																					ExpPackageHistoryItem[1] = requestData;
																					ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																					previousData = responseData;
																					ExpPackageHistoryItem[3] = responseData;
																					ExpPackageHistoryItem[4] = whyToStop;
																					ExpPackageHistoryList.add(ExpPackageHistoryItem);
																					expHTTPHistoryList.add(ExpPackageHistoryItem);
																					ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																					ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																					ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																					ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																					ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																					ExpPackageHistoryItem = new String[7];
																					responseStatus = 0;
																					responseData = "";
																					whyToStop = "Run Over";
				                                        																			
																				}
																			}
																		}
																	}
																}            
															}
															else
															{
																//System.out.println("meiyou package file cyle");

																if (lineTxtTmp[1].trim().split(":").length > 1)
																{
																	host = Method.getFromBase64(lineTxtTmp[1].trim().split(":")[1]);
																}
																else
																{
																	break;
																}

																
																if (lineTxtTmp[2].trim().split(":").length > 1)
																{
																	port = Integer.valueOf(Method.getFromBase64(lineTxtTmp[2].trim().split(":")[1]));
																}
																else
																{
																	port=80;
																}
		                    
																
																StepFileNameTmp = Method.getFromBase64(lineTxtTmp[3].trim().split(":")[1]);									
																if(StepFileNameTmp.length()>0)
																{
																	requestData = Method.bytesToString(Method.HexStringTobytes(Method.stringsToString(Method.readFileToHexStrings(fileDir+"\\"+StepFileNameTmp))));
																}
																
																if(previousURL.length()>0)
																{
																	// 添加 phpcms/  到request 的 GET  phpcms/。。。。  http/1.1
																	requestData = Method.addPreviousURL(requestData,previousURL);
																}
			                          
																String replaceHostPortString = lineTxtTmp[4].trim().split(":")[1].trim();
																if (replaceHostPortString.equals("true"))
																{
																	host = targetHost;
																	port = targetPort;
																	requestData = Method.replaceHostPort(requestData,host,port);
																}
																else
																{
																	
																}
																				
				                          
				                          
																String useSetCookieFromPreviousString = lineTxtTmp[5].trim().split(":")[1].trim();
																if (useSetCookieFromPreviousString.equals("true"))
																{
																	/*
																	previousData = "HTTP/1.1 200 OK"+"\r\n"+
																						  "Date: Sun, 30 Nov 2014 09:16:33 GMT"+"\r\n"+
																						  "Server: Apache/2.2.9 (APMServ) PHP/5.2.6+"+"\r\n"+
																						  "X-Powered-By: PHP/5.2.6"+"\r\n"+
																						  "Set-Cookie: SYSZC=d2d4a91a4e41bea2078b09c29cd6a44a"+"\r\n"+
																						  "Set-Cookie: SYSUSER=admin"+"\r\n"+
																						  "Set-Cookie: SYSNAME=%E7%B3%BB%E7%BB%9F%E7%AE%A1%E7%90%86%E5%91%98"+"\r\n"+
																						  "Set-Cookie: SYSUSERID=3"+"\r\n"+
																						  "Set-Cookie: SYSTM=1417338994"+"\r\n"+
																						  "Content-Length: 2"+"\r\n"+
																						  "Content-Type: text/html"+"\r\n"+
																						  "\r\n"+							  
																						  "OK";
				                            
				                            						*/
																	String[] tmpmp = Method.getHTTPHeadersParams(previousData);
				                    		
																	String setCookieStr = "";
																	try
																	{
																	for(int n=0;n<tmpmp.length;n++)
																	{
																		if(n%2==0)
																		{
																			if(tmpmp[n].startsWith("Set-Cookie"))
																			{
																				setCookieStr =setCookieStr+tmpmp[n+1].trim()+";";
																			}
																		}
																	}
																	String OKrequestData="";	                    		
																	String[] requestDatas = requestData.split("\r\n");
																	boolean addCookie = true;
																	
																	for(int n=0;n<requestDatas.length;n++)
																	{		
																		requestDatas[n] = requestDatas[n]+"\r\n";
																		if(requestDatas[n].toLowerCase().trim().startsWith("cookie:"))
																		{
																			String[] tmpCookie = requestDatas[n].toLowerCase().trim().split("cookie:");
																			if(tmpCookie.length>1)
																			{
																				tmpCookie[1] = Method.replaceCookie(tmpCookie[1].trim(),setCookieStr);
																				requestDatas[n] = "Cookie: "+Method.stringsToString(tmpCookie)+"\r\n";
																				addCookie = false;
																			}
																		}
																	}
				                        	  
																	if(addCookie)
																	{
																		OKrequestData =Method.addHeader(requestData,"Cookie: ",setCookieStr);	                        			
																	}
																	else
																	{
																		OKrequestData = Method.stringsToString(requestDatas);
																	}
																	requestData = OKrequestData;  
																	}
																	catch(Exception e5)
																	{
																		
																	}
																	
																	 
				                            
																}
																else
																{
																	
																}
																
				                          	
																int randomStringTmpLength = lineTxtTmp[6].trim().split(":").length;
																if (randomStringTmpLength > 1)
																{
																	String randomStringString = lineTxtTmp[6].trim().split(":")[1].trim();
																	String[] randomStringItemStrings = randomStringString.split(",");
																	int randomStringItemCount = randomStringItemStrings.length;
																	for (int k = 0; k < randomStringItemCount; k++)
																	{
																		String[] randomStringItem = new String[5];
																		randomStringItem[0] = Method.getFromBase64(randomStringItemStrings[k]).split(",")[0];
																		randomStringItem[1] = Method.getFromBase64(randomStringItemStrings[k]).split(",")[1];
																		randomStringItem[2] =Method.getFromBase64(randomStringItemStrings[k]).split(",")[2].substring(0, 1);
																		randomStringItem[4] = Method.getFromBase64(randomStringItemStrings[k]).split(",")[3];

																		if (Integer.valueOf(randomStringItem[1]).intValue() == 3)
																		{
																			randomStringItem[3] = randomStringItem[2].substring(1);
																			randomStringItem[2] = randomStringItem[2].substring(0, 1);
																		}										
				                            		
																		String strTmp = "rSI"+randomStringItem[0];
																		strTmp = Method.getBase64(strTmp);
																		strTmp = Method.addSignRegex(strTmp);
																		String randomStrTmp = "";
																		switch(Integer.valueOf(randomStringItem[1]))
																		{
																			case 0:randomStrTmp =new RandomString().getRandomString(Integer.valueOf(randomStringItem[2]),"i");requestData =requestData.replaceAll(strTmp, randomStrTmp);break;
																			case 1:randomStrTmp =new RandomString().getRandomString(Integer.valueOf(randomStringItem[2]),"l");requestData =requestData.replaceAll(strTmp, randomStrTmp);break;
																			case 2:randomStrTmp =new RandomString().getRandomString(Integer.valueOf(randomStringItem[2]),"il");requestData =requestData.replaceAll(strTmp, randomStrTmp);break;
																			case 3:randomStrTmp =new RandomString().getRandomStringFromDefineString(randomStringItem[3],Integer.valueOf(randomStringItem[2]));requestData =requestData.replaceAll(strTmp, randomStrTmp);break;																																															
																		}                       		
																		randomStringList.add(randomStringItem);
																	}														
																}                      	
																
																int captchaTmpLength = lineTxtTmp[7].trim().split(":").length;
																if (captchaTmpLength > 1)
																{
																	String captchaString = lineTxtTmp[7].trim().split(":")[1].trim();
																	String[] captchaItemStrings = captchaString.split(",");
																	int captchaItemCount = captchaItemStrings.length;
																	for (int k = 0; k < captchaItemCount; k++)
																	{
																		String[] captchaItem = new String[4];
																		captchaItem = Method.getFromBase64(captchaItemStrings[k]).split(",");
																		captchaList.add(captchaItem);
																		
																		String captchaStrTmp = "sPFC"+captchaItem[0];
																		captchaStrTmp = Method.getBase64(captchaStrTmp);
																		captchaStrTmp = Method.addSignRegex(captchaStrTmp);	                                
																		boolean status = UUAPI.checkAPI();

																		if (!status)
																		{
																			JOptionPane.showMessageDialog(null, "API文件校验失败，无法使用打码服务", "Warning", 2);
																		}
																		else
																		{
																			final String ImgURL = "http://"+host+previousURL+captchaItem[1];  //获取验证码的url   
																	//		System.out.println(ImgURL);                                       
																			final int CaptchaNumbersTextInt = Integer.valueOf(captchaItem[2]);
																			final String saveFileName = "img/tmp.jpg";
																			
																			String saveAbsolutePath = Method.getImgFromURL(ImgURL,saveFileName);
																			if (saveAbsolutePath != null)
																			{
																				if (CaptchaNumbersTextInt != 0)
																				{
																					try 
																					{
																						String[] captchaResult = UUAPI.easyDecaptcha(saveFileName, 1000 + CaptchaNumbersTextInt);
																						if (captchaResult[1].length() > 0)
																						{
																							requestData = requestData.replaceAll(captchaStrTmp, captchaResult[1]);   
																						}
																					} 
																					catch (IOException eee) 
																					{
																						eee.printStackTrace();
																					}	                                                    
																				}	                          					
																			}																	
																		}	                                
																	}
																}	                            
																
				                            
																//
																String FileForCycleWithEXPStrTmp = "FileForCycleWithEXP";
																FileForCycleWithEXPStrTmp = Method.getBase64(FileForCycleWithEXPStrTmp);
																FileForCycleWithEXPStrTmp = Method.addSignRegex(FileForCycleWithEXPStrTmp);
																requestData = requestData.replaceAll(FileForCycleWithEXPStrTmp, expFileLineTxt);	                          			                         
				                          		
																// 发送http 数据
																
																responseData = Method.bytesToString(Method.getHttPResponseData(host,port,requestData));
															//	System.out.println(requestData);
															//	System.out.println("--");
																//System.out.println(responseData);
																responseStatus = Method.getHttpResponseHeaderStatus(responseData);
																													
				                          		
																int setGoAheadOrStopFromStatusTmpLength = lineTxtTmp[9].trim().split(":").length;
																if (setGoAheadOrStopFromStatusTmpLength > 1)
																{
																	String setGoAheadOrStopFromStatusString = lineTxtTmp[9].trim().split(":")[1].trim();
																	String[] setGoAheadOrStopFromStatusItemStrings = setGoAheadOrStopFromStatusString.split(",");
																	int setGoAheadOrStopFromStatusItemCount = setGoAheadOrStopFromStatusItemStrings.length;
																	for (int k = 0; k < setGoAheadOrStopFromStatusItemCount; k++)
																	{
																		String[] setGoAheadOrStopFromStatusItem = new String[2];
																		setGoAheadOrStopFromStatusItem = Method.getFromBase64(setGoAheadOrStopFromStatusItemStrings[k]).split(",");
																		setGoAheadOrStopFromStatusList.add(setGoAheadOrStopFromStatusItem);															
																	}

																}      		
				                          		
																int setGoAheadOrStopFromStatusListCount = setGoAheadOrStopFromStatusList.size();
																if (setGoAheadOrStopFromStatusListCount > 0)
																{
																	for (int j = 0; j < setGoAheadOrStopFromStatusListCount; j++)
																	{
																		String[] setGoAheadOrStopFromStatusListItem = (String[])setGoAheadOrStopFromStatusList.get(j);
																		
																		if (responseStatus == Integer.valueOf(setGoAheadOrStopFromStatusListItem[0]))
																		{
																			String actionForStatus = setGoAheadOrStopFromStatusListItem[1];
				                                			
				                                			
																			//                         GO ahead current   stop current 
																			///                                               ||
																			//																		
																			///											Go Exp    Stop Exp
																		
																			
																			if(actionForStatus.equals("Go Ahead EXP"))
																			{
																				GoAheadExp  = true;
																				whyToStop = "responseStatus; " + responseStatus + " -- Rule; " + setGoAheadOrStopFromStatusListItem[0] + " --Action; "+ setGoAheadOrStopFromStatusListItem[1];
																				break;
																			}
																			else if(actionForStatus.equals("Stop EXP"))
																			{
																				StopExp = true; 				
																				whyToStop = "responseStatus; " + responseStatus + " -- Rule; " + setGoAheadOrStopFromStatusListItem[0] + " --Action; "+ setGoAheadOrStopFromStatusListItem[1];
																				break;
																			}
																			else if(actionForStatus.equals("Go Ahead Current"))
																			{
																				GoAheadCurrent = true;
																				whyToStop = "responseStatus; " + responseStatus + " -- Rule; " + setGoAheadOrStopFromStatusListItem[0] + " --Action; "+ setGoAheadOrStopFromStatusListItem[1];
																				break;
																			}
																			else if(actionForStatus.equals("Stop Current"))
																			{
																				//StopCurrent = true;
																				//whyToStop = "responseStatus:" + responseStatus + " -- Rule" + setGoAheadOrStopFromStatusListItem[0] + setGoAheadOrStopFromStatusListItem[1];
																				//System.out.println(whyToStop);
																				//break;
																			}                                      			
																			
																		}
																	}
																}
																else
																{
																	//System.out.println("mei you setGoAheadOrStopFromStatusListCount");
																}

																if ((StopExp) )
																{
																	JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,expFileLineTxt,"- -",whyToStop,"show"});
																	ExpPackageHistoryItem[0] = StepFileNameTmp;
																	ExpPackageHistoryItem[1] = requestData;
																	ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																	previousData = responseData;
																	ExpPackageHistoryItem[3] = responseData;
																	ExpPackageHistoryItem[4] = whyToStop;
																	ExpPackageHistoryList.add(ExpPackageHistoryItem);
																	expHTTPHistoryList.add(ExpPackageHistoryItem);
																	ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																	ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																	ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																	ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																	ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																	ExpPackageHistoryItem = new String[7];
																	responseStatus = 0;
																	responseData = "";
																	whyToStop = "Run Over";
																	break;
																}
				                                
																if(GoAheadCurrent)
																{
																	JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,expFileLineTxt,"- -",whyToStop,"show"});
																	ExpPackageHistoryItem[0] = StepFileNameTmp;
																	ExpPackageHistoryItem[1] = requestData;
																	ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																	previousData = responseData;
																	ExpPackageHistoryItem[3] = responseData;
																	ExpPackageHistoryItem[4] = whyToStop;
																	ExpPackageHistoryList.add(ExpPackageHistoryItem);
																	expHTTPHistoryList.add(ExpPackageHistoryItem);
																	ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																	ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																	ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																	ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																	ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																	ExpPackageHistoryItem = new String[7];
																	responseStatus = 0;
																	responseData = "";
																	whyToStop = "Run Over";
																	GoAheadCurrent = false;
																	break;
																}
				                                
																if(GoAheadExp)
																{
																	JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,expFileLineTxt,"- -",whyToStop,"show"});
																	ExpPackageHistoryItem[0] = StepFileNameTmp;
																	ExpPackageHistoryItem[1] = requestData;
																	ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																	previousData = responseData;
																	ExpPackageHistoryItem[3] = responseData;
																	ExpPackageHistoryItem[4] = whyToStop;
																	ExpPackageHistoryList.add(ExpPackageHistoryItem);
																	expHTTPHistoryList.add(ExpPackageHistoryItem);
																	ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																	ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																	ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																	ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																	ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																	ExpPackageHistoryItem = new String[7];
																	responseStatus = 0;
																	responseData = "";
																	whyToStop = "Run Over";
																	break;
																}
				                               										
																
																int setGoAheadOrStopFromDataTmpLength = lineTxtTmp[10].trim().split(":").length;
																if (setGoAheadOrStopFromDataTmpLength > 1)
																{
																	String setGoAheadOrStopFromDataString = lineTxtTmp[10].trim().split(":")[1].trim();
																	String[] setGoAheadOrStopFromDataItemStrings = setGoAheadOrStopFromDataString.split(",");
																	int setGoAheadOrStopFromDataItemCount = setGoAheadOrStopFromDataItemStrings.length;
																	for (int k = 0; k < setGoAheadOrStopFromDataItemCount; k++)
																	{
																		String[] setGoAheadOrStopFromDataItem = new String[2];
																		setGoAheadOrStopFromDataItem = Method.getFromBase64(setGoAheadOrStopFromDataItemStrings[k]).split(",");
																		setGoAheadOrStopFromDataList.add(setGoAheadOrStopFromDataItem);													
																	}

																}
																						
																
				                                
																
																int j;
																int setGoAheadOrStopFromDataListCount = setGoAheadOrStopFromDataList.size();
																if (setGoAheadOrStopFromDataListCount > 0)
																{
																	for (j = 0; j < setGoAheadOrStopFromDataListCount; j++)
																	{
																		String[] setGoAheadOrStopFromDataListItem = (String[])setGoAheadOrStopFromDataList.get(j);
																		Pattern p=Pattern.compile(Method.getFromBase64(setGoAheadOrStopFromDataListItem[0]));
																		Matcher m=p.matcher(responseData);
																	//	System.out.println(m.find());
																		
																		if (m.find())
																		{
																			String actionForData = setGoAheadOrStopFromDataListItem[1];
																			
																			if(actionForData.equals("Go Ahead EXP"))
																			{
																				GoAheadExp  = true;
																				whyToStop = " -- Rule; " + Method.getFromBase64(setGoAheadOrStopFromDataListItem[0]) +"  --result; "+m.group()+ " --Action; "+ setGoAheadOrStopFromDataListItem[1];
																				break;
																			}
																			else if(actionForData.equals("Stop EXP"))
																			{
																				StopExp = true;
																				whyToStop = " -- Rule; " + Method.getFromBase64(setGoAheadOrStopFromDataListItem[0]) +"  --result; "+m.group()+ " --Action; "+ setGoAheadOrStopFromDataListItem[1];
																				break;
																			}
																			else if(actionForData.equals("Go Ahead Current"))
																			{
																				GoAheadCurrent = true;
																				whyToStop = " -- Rule; " + Method.getFromBase64(setGoAheadOrStopFromDataListItem[0]) +"  --result; "+m.group()+ " --Action; "+ setGoAheadOrStopFromDataListItem[1];
																				break;
																			}
																			else if(actionForData.equals("Stop Current"))
																			{
																				//StopCurrent = true;
																				//whyToStop = "responseStatus:" + responseStatus + " -- Rule" + setGoAheadOrStopFromStatusListItem[0] + setGoAheadOrStopFromStatusListItem[1];
																				//System.out.println(whyToStop);
																				//break;
																			} 
																			
																		}
																	}
																}
																else
																{
																	//System.out.println("meiyou setGoAheadOrStopFromDataList");
																}

																if ((StopExp) )
																{
																	JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,expFileLineTxt,"- -",whyToStop,"show"});
																	ExpPackageHistoryItem[0] = StepFileNameTmp;
																	ExpPackageHistoryItem[1] = requestData;
																	ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																	previousData = responseData;
																	ExpPackageHistoryItem[3] = responseData;
																	ExpPackageHistoryItem[4] = whyToStop;
																	ExpPackageHistoryList.add(ExpPackageHistoryItem);
																	expHTTPHistoryList.add(ExpPackageHistoryItem);
																	ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																	ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																	ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																	ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																	ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																	ExpPackageHistoryItem = new String[7];
																	responseStatus = 0;
																	responseData = "";
																	whyToStop = "Run Over";
																	break;
																}
				                                
																if(GoAheadCurrent)
																{
																	JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,expFileLineTxt,"- -",whyToStop,"show"});
																	ExpPackageHistoryItem[0] = StepFileNameTmp;
																	ExpPackageHistoryItem[1] = requestData;
																	ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																	previousData = responseData;
																	ExpPackageHistoryItem[3] = responseData;
																	ExpPackageHistoryItem[4] = whyToStop;
																	ExpPackageHistoryList.add(ExpPackageHistoryItem);
																	expHTTPHistoryList.add(ExpPackageHistoryItem);
																	ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																	ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																	ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																	ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																	ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																	ExpPackageHistoryItem = new String[7];
																	responseStatus = 0;
																	responseData = "";
																	whyToStop = "Run Over";
																	GoAheadCurrent = false;
																	break;
																}
				                                
																if(GoAheadExp)
																{
																	JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,expFileLineTxt,"- -",whyToStop,"show"});
																	ExpPackageHistoryItem[0] = StepFileNameTmp;
																	ExpPackageHistoryItem[1] = requestData;
																	ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																	previousData = responseData;
																	ExpPackageHistoryItem[3] = responseData;
																	ExpPackageHistoryItem[4] = whyToStop;
																	ExpPackageHistoryList.add(ExpPackageHistoryItem);
																	expHTTPHistoryList.add(ExpPackageHistoryItem);
																	ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																	ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																	ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																	ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																	ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																	ExpPackageHistoryItem = new String[7];
																	responseStatus = 0;
																	responseData = "";
																	whyToStop = "Run Over";
																	break;
																}

																JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,expFileLineTxt,"- -",whyToStop,"show"});
																ExpPackageHistoryItem[0] = StepFileNameTmp;
																ExpPackageHistoryItem[1] = requestData;
																ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																previousData = responseData;
																ExpPackageHistoryItem[3] = responseData;
																ExpPackageHistoryItem[4] = whyToStop;
																ExpPackageHistoryList.add(ExpPackageHistoryItem);
																expHTTPHistoryList.add(ExpPackageHistoryItem);
																ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																ExpPackageHistoryItem = new String[7];
																responseStatus = 0;
																responseData = "";
																whyToStop = "Run Over";
				                          		
																
															}                    
														}
														else
														{
															//System.out.println("lineTxtTmpLength  !===== 131311313");
														}                                   
													}
													else
													{
														//System.out.println("lineTxtTmpLength>>>>>>>>>>>..0");
													}
												} 
												if(GoAheadExp)
												{								
													GoAheadExp  = false;
													continue;
												}
											}
				            
										}
										else
										{
											//System.out.println("meiyou fileForCycleWithEXP");
										
											boolean StopExp = false;
											boolean StopCurrent =false;
											boolean GoAheadCurrent = false;
											boolean GoAheadExp = false;
											List ExpPackageHistoryList = new ArrayList();
											
											String[] ExpPackagePreviousItem = new String[5];
											for (int i = 0; i < StepNums&&!StopExp; i++)
											{
												String StringForEachStep = (String)tmplist.get(i);									
												
												if (StringForEachStep.length() > 0)
												{
													StepFileNameTmp = "";
				                  
													String requestData = "";
													String responseData = "";
													int responseStatus = 0;
													String whyToStop = "Run Over";

													String[] ExpPackageHistoryItem = new String[5];
				                  
													String host = "";
													int port = 80;
													
													String previousData="";
													
													randomStringList = new ArrayList();
													captchaList = new ArrayList();
													setParamsFromDataPackageList = new ArrayList();
													setGoAheadOrStopFromStatusList = new ArrayList();
													setGoAheadOrStopFromDataList = new ArrayList();
													useFileForCycleWithPackageList = new ArrayList();
													
													String[] lineTxtTmp = StringForEachStep.split("-");
													int lineTxtTmpLength = lineTxtTmp.length;								
				                  
													if (lineTxtTmpLength == 13)
													{
				                	  
														//System.out.println("useFileForCycleWithPackage");
														int useFileForCycleWithPackageTmpLength = lineTxtTmp[11].trim().split(":").length;
														if (useFileForCycleWithPackageTmpLength > 1)
														{
															String useFileForCycleWithPackageString = lineTxtTmp[11].trim().split(":")[1].trim();
															String[] useFileForCycleWithPackageItemStrings = useFileForCycleWithPackageString.split(",");
															int useFileForCycleWithPackageItemCount = useFileForCycleWithPackageItemStrings.length;
															for (int k = 0; k < useFileForCycleWithPackageItemCount; k++)
															{
																String[] useFileForCycleWithPackageItem = new String[6];
																useFileForCycleWithPackageItem = Method.getFromBase64(useFileForCycleWithPackageItemStrings[k]).split(",");

																if (Integer.valueOf(useFileForCycleWithPackageItem[0]).intValue() == 1)
																{
																	useFileForCycleWithPackageItem[1] = Method.getFromBase64(useFileForCycleWithPackageItem[1].trim());									
																}
																else
																{
																	useFileForCycleWithPackageItem[1] = Method.getFromBase64(useFileForCycleWithPackageItem[1].trim());
																	useFileForCycleWithPackageItem[2] = Method.getFromBase64(useFileForCycleWithPackageItem[2].trim());
																	useFileForCycleWithPackageItem[3] = useFileForCycleWithPackageItem[3].trim();						
																}
																useFileForCycleWithPackageList.add(useFileForCycleWithPackageItem);
															}										
				                          
															int useFileForCycleWithPackageListCount = useFileForCycleWithPackageList.size();												
															for (int t = 0; t < useFileForCycleWithPackageListCount; t++)
															{
																String[] useFileForCycleWithPackageItem = (String[])useFileForCycleWithPackageList.get(t);
																if (Integer.valueOf(useFileForCycleWithPackageItem[0]) == 1)
																{
																	//System.out.println("package 一个文件");
																	
																	File useFileForCycleWithPackageFile1 = new File(fileDir+useFileForCycleWithPackageItem[1]);
																	InputStreamReader useFileForCycleWithPackageFile1InputSteamReadTmp = new InputStreamReader(new FileInputStream(useFileForCycleWithPackageFile1));
																	BufferedReader useFileForCycleWithPackageFile1ExpFileBufferedReader = new BufferedReader(useFileForCycleWithPackageFile1InputSteamReadTmp);
																	String useFileForCycleWithPackageLineText = null;
																	
																	int PackageNum=0;
																	
																	while ((useFileForCycleWithPackageLineText = useFileForCycleWithPackageFile1ExpFileBufferedReader.readLine()) != null)
																	{
																		
																		PackageNum++;
																		if (lineTxtTmp[1].trim().split(":").length > 1)
																		{
																			host = Method.getFromBase64(lineTxtTmp[1].trim().split(":")[1]);															
																		}
																		else
																		{
																			break;
																		}

																		
																		if (lineTxtTmp[2].trim().split(":").length > 1)
																		{
																			port = Integer.valueOf(Method.getFromBase64(lineTxtTmp[2].trim().split(":")[1]));
																		}
																		else
																		{
																			port=80;
																		}
																
																		StepFileNameTmp = Method.getFromBase64(lineTxtTmp[3].trim().split(":")[1]);									
																		if(StepFileNameTmp.length()>0)
																		{
																			requestData = Method.bytesToString(Method.HexStringTobytes(Method.stringsToString(Method.readFileToHexStrings(fileDir+"\\"+StepFileNameTmp))));
																		}
																		       
																		
																		if(previousURL.length()>0)
																		{
																			// 添加 phpcms/  到request 的 GET  phpcms/。。。。  http/1.1
																			requestData = Method.addPreviousURL(requestData,previousURL);
																		}
				                                  
																		String replaceHostPortString = lineTxtTmp[4].trim().split(":")[1].trim();
																		if (replaceHostPortString.equals("true"))
																		{
																			host = targetHost;
																			port = targetPort;
																			requestData = Method.replaceHostPort(requestData,host,port);
																		}
																		else
																		{
																		}
																		
																		
																		
				                                  
																		
				                                  
				                                  
																		String useSetCookieFromPreviousString = lineTxtTmp[5].trim().split(":")[1].trim();
																		if (useSetCookieFromPreviousString.equals("true"))
																		{
																			/*
																			previousData = "HTTP/1.1 200 OK"+"\r\n"+
																								  "Date: Sun, 30 Nov 2014 09:16:33 GMT"+"\r\n"+
																								  "Server: Apache/2.2.9 (APMServ) PHP/5.2.6+"+"\r\n"+
																								  "X-Powered-By: PHP/5.2.6"+"\r\n"+
																								  "Set-Cookie: SYSZC=d2d4a91a4e41bea2078b09c29cd6a44a"+"\r\n"+
																								  "Set-Cookie: SYSUSER=admin"+"\r\n"+
																								  "Set-Cookie: SYSNAME=%E7%B3%BB%E7%BB%9F%E7%AE%A1%E7%90%86%E5%91%98"+"\r\n"+
																								  "Set-Cookie: SYSUSERID=3"+"\r\n"+
																								  "Set-Cookie: SYSTM=1417338994"+"\r\n"+
																								  "Content-Length: 2"+"\r\n"+
																								  "Content-Type: text/html"+"\r\n"+
																								  "\r\n"+							  
																								  "OK";
				                                    						*/
				                                    
																			String[] tmpmp = Method.getHTTPHeadersParams(previousData);                          		
																			String setCookieStr = "";
																			try
																			{
																			for(int n=0;n<tmpmp.length;n++)
																			{
																				if(n%2==0)
																				{
																					if(tmpmp[n].startsWith("Set-Cookie"))
																					{
																						setCookieStr =setCookieStr+tmpmp[n+1].trim()+";";
																					}
																				}
																			}
																			String OKrequestData="";	                            		
																			String[] requestDatas = requestData.split("\r\n");
																			boolean addCookie = true;
																			
																			for(int n=0;n<requestDatas.length;n++)
																			{		
																				requestDatas[n] = requestDatas[n]+"\r\n";
																				if(requestDatas[n].toLowerCase().trim().startsWith("cookie:"))
																				{
																					String[] tmpCookie = requestDatas[n].toLowerCase().trim().split("cookie:");
																					if(tmpCookie.length>1)
																					{
																						tmpCookie[1] = Method.replaceCookie(tmpCookie[1].trim(),setCookieStr);
																						requestDatas[n] = "Cookie: "+Method.stringsToString(tmpCookie)+"\r\n";
																						addCookie = false;
																					}
																				}
																			}
				                                	  
																			if(addCookie)
																			{
																				OKrequestData =Method.addHeader(requestData,"Cookie: ",setCookieStr);	                                			
																			}
																			else
																			{
																				OKrequestData = Method.stringsToString(requestDatas);
																			}
																			requestData = OKrequestData; 
																			}
																			catch(Exception e5)
																			{}
				                            		
																			  
				                                    
																		}
																		else
																		{
																			//useSetCookieFromPrevious = false;
																		}
																		
				                                  	
																		int randomStringTmpLength = lineTxtTmp[6].trim().split(":").length;
																		if (randomStringTmpLength > 1)
																		{
																			String randomStringString = lineTxtTmp[6].trim().split(":")[1].trim();
																			String[] randomStringItemStrings = randomStringString.split(",");
																			int randomStringItemCount = randomStringItemStrings.length;
																			for (int k = 0; k < randomStringItemCount; k++)
																			{
																				String[] randomStringItem = new String[5];
																				randomStringItem[0] = Method.getFromBase64(randomStringItemStrings[k]).split(",")[0];
																				randomStringItem[1] = Method.getFromBase64(randomStringItemStrings[k]).split(",")[1];
																				randomStringItem[2] =Method.getFromBase64(randomStringItemStrings[k]).split(",")[2].substring(0, 1);
																				randomStringItem[4] = Method.getFromBase64(randomStringItemStrings[k]).split(",")[3];
																		
																				if (Integer.valueOf(randomStringItem[1]).intValue() == 3)
																				{
																					randomStringItem[3] = randomStringItem[2].substring(1);
																					randomStringItem[2] = randomStringItem[2].substring(0, 1);
																				}																	
				                                    		
																				String strTmp = "rSI"+randomStringItem[0];
																				strTmp = Method.getBase64(strTmp);
																				strTmp = Method.addSignRegex(strTmp);
																				String randomStrTmp = "";
																				switch(Integer.valueOf(randomStringItem[1]))
																				{
																					case 0:randomStrTmp =new RandomString().getRandomString(Integer.valueOf(randomStringItem[2]),"i");requestData =requestData.replaceAll(strTmp, randomStrTmp);break;
																					case 1:randomStrTmp =new RandomString().getRandomString(Integer.valueOf(randomStringItem[2]),"l");requestData =requestData.replaceAll(strTmp, randomStrTmp);break;
																					case 2:randomStrTmp =new RandomString().getRandomString(Integer.valueOf(randomStringItem[2]),"il");requestData =requestData.replaceAll(strTmp, randomStrTmp);break;
																					case 3:randomStrTmp =new RandomString().getRandomStringFromDefineString(randomStringItem[3],Integer.valueOf(randomStringItem[2]));requestData =requestData.replaceAll(strTmp, randomStrTmp);break;																																																
																				}		                                    		
																				randomStringList.add(randomStringItem);
																			}																	
																		}                                  		
				                                  	
																		
																		int captchaTmpLength = lineTxtTmp[7].trim().split(":").length;
																		if (captchaTmpLength > 1)
																		{
																			String captchaString = lineTxtTmp[7].trim().split(":")[1].trim();
																			String[] captchaItemStrings = captchaString.split(",");
																			int captchaItemCount = captchaItemStrings.length;
																			for (int k = 0; k < captchaItemCount; k++)
																			{
																				String[] captchaItem = new String[4];
																				captchaItem = Method.getFromBase64(captchaItemStrings[k]).split(",");
																				captchaList.add(captchaItem);
																				
																				String captchaStrTmp = "sPFC"+captchaItem[0];
																				captchaStrTmp = Method.getBase64(captchaStrTmp);
																				captchaStrTmp = Method.addSignRegex(captchaStrTmp);
				                                        
				                                        
																				boolean status = UUAPI.checkAPI();

																				if (!status)
																				{
																					JOptionPane.showMessageDialog(null, "API文件校验失败，无法使用打码服务", "Warning", 2);
																				}
																				else
																				{
																					final String ImgURL = "http://"+host+previousURL+captchaItem[1];  //获取验证码的url  
																				//	System.out.println(ImgURL);                                           
																					final int CaptchaNumbersTextInt = Integer.valueOf(captchaItem[2]);
																					final String saveFileName = "img/tmp.jpg";
																					
																					String saveAbsolutePath = Method.getImgFromURL(ImgURL,saveFileName);
																					if (saveAbsolutePath != null)
																					{
																						if (CaptchaNumbersTextInt != 0)
																						{
																							try 
																							{
																								String[] captchaResult = UUAPI.easyDecaptcha(saveFileName, 1000 + CaptchaNumbersTextInt);
																								if (captchaResult[1].length() > 0)
																								{
																									requestData = requestData.replaceAll(captchaStrTmp, captchaResult[1]);   
																								}
																							} 
																							catch (IOException eee) 
																							{
																								eee.printStackTrace();
																							}	                                                    
																						}		                                  					
																					}																			
																				}		                                        
																			}
																		}
				                                    	                                 
				                                    
																		//useFileForCycleWithPackageLineText
																		String useFileForCycleWithPackageLineStrTmp = "File-1";
																		useFileForCycleWithPackageLineStrTmp = Method.getBase64(useFileForCycleWithPackageLineStrTmp);
																		useFileForCycleWithPackageLineStrTmp = Method.addSignRegex(useFileForCycleWithPackageLineStrTmp);
																		requestData = requestData.replaceAll(useFileForCycleWithPackageLineStrTmp, useFileForCycleWithPackageLineText);
				                                  		
				                                  	
				                                  		
																		// 发送http 数据
																		
																		responseData = Method.bytesToString(Method.getHttPResponseData(host,port,requestData));
																	//	System.out.println(requestData);
																	//	System.out.println("--");
																	//	System.out.println(responseData);
																		responseStatus = Method.getHttpResponseHeaderStatus(responseData);
																		                                  		
																		int setGoAheadOrStopFromStatusTmpLength = lineTxtTmp[9].trim().split(":").length;
																		if (setGoAheadOrStopFromStatusTmpLength > 1)
																		{
																			String setGoAheadOrStopFromStatusString = lineTxtTmp[9].trim().split(":")[1].trim();
																			String[] setGoAheadOrStopFromStatusItemStrings = setGoAheadOrStopFromStatusString.split(",");
																			int setGoAheadOrStopFromStatusItemCount = setGoAheadOrStopFromStatusItemStrings.length;
																			for (int k = 0; k < setGoAheadOrStopFromStatusItemCount; k++)
																			{
																				String[] setGoAheadOrStopFromStatusItem = new String[2];
																				setGoAheadOrStopFromStatusItem = Method.getFromBase64(setGoAheadOrStopFromStatusItemStrings[k]).split(",");
																				setGoAheadOrStopFromStatusList.add(setGoAheadOrStopFromStatusItem);																
																			}
																		}
				                                  		
				                                  		
				                                  		
				                                  		
																		int setGoAheadOrStopFromStatusListCount = setGoAheadOrStopFromStatusList.size();
																		if (setGoAheadOrStopFromStatusListCount > 0)
																		{
																			for (int j = 0; j < setGoAheadOrStopFromStatusListCount; j++)
																			{
																				String[] setGoAheadOrStopFromStatusListItem = (String[])setGoAheadOrStopFromStatusList.get(j);
																				if (responseStatus == Integer.valueOf(setGoAheadOrStopFromStatusListItem[0]))
																				{
																					String actionForStatus = setGoAheadOrStopFromStatusListItem[1];
				                                        				
																					//                         GO ahead current   stop current 
																					///                                               ||
																					//																		
																					///											Go Exp    Stop Exp
																					///
																					//	
																					
																					if(actionForStatus.equals("Go Ahead EXP"))
																					{
																						GoAheadExp  = true;
																						whyToStop = "responseStatus; " + responseStatus + " -- Rule; " + setGoAheadOrStopFromStatusListItem[0] + " --Action; "+ setGoAheadOrStopFromStatusListItem[1];
																						break;
																					}
																					else if(actionForStatus.equals("Stop EXP"))
																					{
																						StopExp = true;				
																						whyToStop = "responseStatus; " + responseStatus + " -- Rule; " + setGoAheadOrStopFromStatusListItem[0] + " --Action; "+ setGoAheadOrStopFromStatusListItem[1];
																						break;
																					}
																					else if(actionForStatus.equals("Go Ahead Current"))
																					{
																						GoAheadCurrent = true;
																						whyToStop = "responseStatus; " + responseStatus + " -- Rule; " + setGoAheadOrStopFromStatusListItem[0] + " --Action; "+ setGoAheadOrStopFromStatusListItem[1];
																						break;
																					}
																					else if(actionForStatus.equals("Stop Current"))
																					{
																						//StopCurrent = true;
																						//whyToStop = "responseStatus:" + responseStatus + " -- Rule" + setGoAheadOrStopFromStatusListItem[0] + setGoAheadOrStopFromStatusListItem[1];
																						//System.out.println(whyToStop);
																						//break;
																					}                                      															
																				}
																			}
																		}
																		else
																		{
																			//System.out.println("mei you setGoAheadOrStopFromStatusListCount");
																		}

																		if ((StopExp) )
																		{
																			JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,"- -",useFileForCycleWithPackageLineText,whyToStop,"show"});
																			ExpPackageHistoryItem[0] = StepFileNameTmp;
																			ExpPackageHistoryItem[1] = requestData;
																			ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																			previousData = responseData;
																			ExpPackageHistoryItem[3] = responseData;
																			ExpPackageHistoryItem[4] = whyToStop;
																			ExpPackageHistoryList.add(ExpPackageHistoryItem);
																			expHTTPHistoryList.add(ExpPackageHistoryItem);
																			ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																			ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																			ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																			ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																			ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																			ExpPackageHistoryItem = new String[7];
																			responseStatus = 0;
																			responseData = "";
																			whyToStop = "Run Over";
																			break;
																		}
				                                        
																		if(GoAheadCurrent)
																		{
																			JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,"- -",useFileForCycleWithPackageLineText,whyToStop,"show"});
																			ExpPackageHistoryItem[0] = StepFileNameTmp;
																			ExpPackageHistoryItem[1] = requestData;
																			ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																			previousData = responseData;
																			ExpPackageHistoryItem[3] = responseData;
																			ExpPackageHistoryItem[4] = whyToStop;
																			ExpPackageHistoryList.add(ExpPackageHistoryItem);
																			expHTTPHistoryList.add(ExpPackageHistoryItem);
																			ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																			ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																			ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																			ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																			ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																			ExpPackageHistoryItem = new String[7];
																			responseStatus = 0;
																			responseData = "";
																			whyToStop = "Run Over";
																			GoAheadCurrent = false;
																			break;
																		}
				                                        
																		if(GoAheadExp)
																		{
																			JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,"- -",useFileForCycleWithPackageLineText,whyToStop,"show"});
																			ExpPackageHistoryItem[0] = StepFileNameTmp;
																			ExpPackageHistoryItem[1] = requestData;
																			ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																			previousData = responseData;
																			ExpPackageHistoryItem[3] = responseData;
																			ExpPackageHistoryItem[4] = whyToStop;
																			ExpPackageHistoryList.add(ExpPackageHistoryItem);
																			expHTTPHistoryList.add(ExpPackageHistoryItem);
																			ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																			ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																			ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																			ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																			ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																			ExpPackageHistoryItem = new String[7];
																			responseStatus = 0;
																			responseData = "";
																			whyToStop = "Run Over";
																			break;
																		}
																		
																		int setGoAheadOrStopFromDataTmpLength = lineTxtTmp[10].trim().split(":").length;
																		if (setGoAheadOrStopFromDataTmpLength > 1)
																		{
																			String setGoAheadOrStopFromDataString = lineTxtTmp[10].trim().split(":")[1].trim();
																			String[] setGoAheadOrStopFromDataItemStrings = setGoAheadOrStopFromDataString.split(",");
																			int setGoAheadOrStopFromDataItemCount = setGoAheadOrStopFromDataItemStrings.length;
																			for (int k = 0; k < setGoAheadOrStopFromDataItemCount; k++)
																			{
																				String[] setGoAheadOrStopFromDataItem = new String[2];
																				setGoAheadOrStopFromDataItem = Method.getFromBase64(setGoAheadOrStopFromDataItemStrings[k]).split(",");
																				setGoAheadOrStopFromDataList.add(setGoAheadOrStopFromDataItem);
																			}
																		}
			                                        
																		
																		int j;
																		int setGoAheadOrStopFromDataListCount = setGoAheadOrStopFromDataList.size();
																		if (setGoAheadOrStopFromDataListCount > 0)
																		{
																			for (j = 0; j < setGoAheadOrStopFromDataListCount; j++)
																			{
																				String[] setGoAheadOrStopFromDataListItem = (String[])setGoAheadOrStopFromDataList.get(j);
																				Pattern p=Pattern.compile(Method.getFromBase64(setGoAheadOrStopFromDataListItem[0]));
																				Matcher m=p.matcher(responseData);
																			//	System.out.println(m.find());
																				
																				if (m.find())
																				{
																					String actionForData = setGoAheadOrStopFromDataListItem[1];
																					
																					if(actionForData.equals("Go Ahead EXP"))
																					{
																						GoAheadExp  = true;
																						whyToStop = " -- Rule; " + Method.getFromBase64(setGoAheadOrStopFromDataListItem[0]) +"  --result; "+m.group()+ " --Action; "+ setGoAheadOrStopFromDataListItem[1];
																						break;
																					}
																					else if(actionForData.equals("Stop EXP"))
																					{
																						StopExp = true;
																						whyToStop = " -- Rule; " + Method.getFromBase64(setGoAheadOrStopFromDataListItem[0]) +"  --result; "+m.group()+ " --Action; "+ setGoAheadOrStopFromDataListItem[1];
																						break;
																					}
																					else if(actionForData.equals("Go Ahead Current"))
																					{
																						GoAheadCurrent = true;
																						whyToStop = " -- Rule; " + Method.getFromBase64(setGoAheadOrStopFromDataListItem[0]) +"  --result; "+m.group()+ " --Action; "+ setGoAheadOrStopFromDataListItem[1];
																						break;
																					}
																					else if(actionForData.equals("Stop Current"))
																					{
																						//StopCurrent = true;
																						//whyToStop = "responseStatus:" + responseStatus + " -- Rule" + setGoAheadOrStopFromStatusListItem[0] + setGoAheadOrStopFromStatusListItem[1];
																						//System.out.println(whyToStop);
																						//break;
																					} 
																					
																				}
																			}
																		}
																		else
																		{
																			//System.out.println("meiyou setGoAheadOrStopFromDataList");
																		}

																		if ((StopExp) )
																		{
																			JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,"- -",useFileForCycleWithPackageLineText,whyToStop,"show"});
																			ExpPackageHistoryItem[0] = StepFileNameTmp;
																			ExpPackageHistoryItem[1] = requestData;
																			ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																			previousData = responseData;
																			ExpPackageHistoryItem[3] = responseData;
																			ExpPackageHistoryItem[4] = whyToStop;
																			ExpPackageHistoryList.add(ExpPackageHistoryItem);
																			expHTTPHistoryList.add(ExpPackageHistoryItem);
																			ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																			ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																			ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																			ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																			ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																			ExpPackageHistoryItem = new String[7];
																			responseStatus = 0;
																			responseData = "";
																			whyToStop = "Run Over";
																			break;
																		}
				                                        
																		if(GoAheadCurrent)
																		{
																			JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,"- -",useFileForCycleWithPackageLineText,whyToStop,"show"});
																			ExpPackageHistoryItem[0] = StepFileNameTmp;
																			ExpPackageHistoryItem[1] = requestData;
																			ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																			previousData = responseData;
																			ExpPackageHistoryItem[3] = responseData;
																			ExpPackageHistoryItem[4] = whyToStop;
																			ExpPackageHistoryList.add(ExpPackageHistoryItem);
																			expHTTPHistoryList.add(ExpPackageHistoryItem);
																			ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																			ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																			ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																			ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																			ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																			ExpPackageHistoryItem = new String[7];
																			responseStatus = 0;
																			responseData = "";
																			whyToStop = "Run Over";
																			GoAheadCurrent = false;
																			break;
																		}
				                                        
																		if(GoAheadExp)
																		{
																			JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,"- -",useFileForCycleWithPackageLineText,whyToStop,"show"});
																			ExpPackageHistoryItem[0] = StepFileNameTmp;
																			ExpPackageHistoryItem[1] = requestData;
																			ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																			previousData = responseData;
																			ExpPackageHistoryItem[3] = responseData;
																			ExpPackageHistoryItem[4] = whyToStop;
																			ExpPackageHistoryList.add(ExpPackageHistoryItem);
																			expHTTPHistoryList.add(ExpPackageHistoryItem);
																			ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																			ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																			ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																			ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																			ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																			ExpPackageHistoryItem = new String[7];
																			responseStatus = 0;
																			responseData = "";
																			whyToStop = "Run Over";
																			break;
																		}

																		JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,"- -",useFileForCycleWithPackageLineText,whyToStop,"show"});
																		ExpPackageHistoryItem[0] = StepFileNameTmp;
																		ExpPackageHistoryItem[1] = requestData;
																		ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																		previousData = responseData;
																		ExpPackageHistoryItem[3] = responseData;
																		ExpPackageHistoryItem[4] = whyToStop;
																		ExpPackageHistoryList.add(ExpPackageHistoryItem);
																		expHTTPHistoryList.add(ExpPackageHistoryItem);
																		ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																		ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																		ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																		ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																		ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																		ExpPackageHistoryItem = new String[7];
																		responseStatus = 0;
																		responseData = "";
																		whyToStop = "Run Over";
				                                  	
				                                  	
																	}
																}
																else if(Integer.valueOf(useFileForCycleWithPackageItem[0])==2)
																{
																	//System.out.println("两个文件");												
																	
																	//组合方式1   一对一
																	if(Integer.valueOf(useFileForCycleWithPackageItem[3])==1)
																	{
																		File useFileForCycleWithPackageFile1 = new File(fileDir+useFileForCycleWithPackageItem[1]);
																		File useFileForCycleWithPackageFile2 = new File(fileDir+useFileForCycleWithPackageItem[2]);
																		
																		InputStreamReader useFileForCycleWithPackageFile1InputSteamReadTmp = new InputStreamReader(new FileInputStream(useFileForCycleWithPackageFile1));
																		InputStreamReader useFileForCycleWithPackageFile2InputSteamReadTmp = new InputStreamReader(new FileInputStream(useFileForCycleWithPackageFile2));
																		
																		BufferedReader useFileForCycleWithPackageFile1ExpFileBufferedReader = new BufferedReader(useFileForCycleWithPackageFile1InputSteamReadTmp);
																		BufferedReader useFileForCycleWithPackageFile2ExpFileBufferedReader = new BufferedReader(useFileForCycleWithPackageFile2InputSteamReadTmp);
																		
																		String useFileForCycleWithPackageLineText1 = null;
																		String useFileForCycleWithPackageLineText2 = null;
																		
																		int PackageNum=0;
																		while (((useFileForCycleWithPackageLineText1 = useFileForCycleWithPackageFile1ExpFileBufferedReader.readLine()) != null)&&((useFileForCycleWithPackageLineText2 = useFileForCycleWithPackageFile2ExpFileBufferedReader.readLine()) != null))
																		{							
																			PackageNum++;
																			if (lineTxtTmp[1].trim().split(":").length > 1)
																			{
																				host = Method.getFromBase64(lineTxtTmp[1].trim().split(":")[1]);																	
																			}
																			else
																			{
																				break;
																			}

																			
																			if (lineTxtTmp[2].trim().split(":").length > 1)
																			{
																				port = Integer.valueOf(Method.getFromBase64(lineTxtTmp[2].trim().split(":")[1]));
																			}
																			else
																			{
																				port=80;
																			}                       
																			
																			StepFileNameTmp = Method.getFromBase64(lineTxtTmp[3].trim().split(":")[1]);									
																			if(StepFileNameTmp.length()>0)
																			{
																				requestData = Method.bytesToString(Method.HexStringTobytes(Method.stringsToString(Method.readFileToHexStrings(fileDir+"\\"+StepFileNameTmp))));
																			}
																			
		                                      
																			if(previousURL.length()>0)
																			{
																				// 添加 phpcms/  到request 的 GET  phpcms/。。。。  http/1.1
																				requestData = Method.addPreviousURL(requestData,previousURL);
																			}
																			
																			String replaceHostPortString = lineTxtTmp[4].trim().split(":")[1].trim();
																			if (replaceHostPortString.equals("true"))
																			{
																				host = targetHost;
																				port = targetPort;
																				requestData = Method.replaceHostPort(requestData,host,port);
																			}
																			else
																			{
																			
																			}

				                                      
																			String useSetCookieFromPreviousString = lineTxtTmp[5].trim().split(":")[1].trim();
																			if (useSetCookieFromPreviousString.equals("true"))
																			{
																				/*
																				previousData = "HTTP/1.1 200 OK"+"\r\n"+
																									  "Date: Sun, 30 Nov 2014 09:16:33 GMT"+"\r\n"+
																									  "Server: Apache/2.2.9 (APMServ) PHP/5.2.6+"+"\r\n"+
																									  "X-Powered-By: PHP/5.2.6"+"\r\n"+
																									  "Set-Cookie: SYSZC=d2d4a91a4e41bea2078b09c29cd6a44a"+"\r\n"+
																									  "Set-Cookie: SYSUSER=admin"+"\r\n"+
																									  "Set-Cookie: SYSNAME=%E7%B3%BB%E7%BB%9F%E7%AE%A1%E7%90%86%E5%91%98"+"\r\n"+
																									  "Set-Cookie: SYSUSERID=3"+"\r\n"+
																									  "Set-Cookie: SYSTM=1417338994"+"\r\n"+
																									  "Content-Length: 2"+"\r\n"+
																									  "Content-Type: text/html"+"\r\n"+
																									  "\r\n"+							  
																									  "OK";
				                                        
				                                        						*/
																				String[] tmpmp = Method.getHTTPHeadersParams(previousData);
				                                		
																				String setCookieStr = "";
																				try
																				{
																				for(int n=0;n<tmpmp.length;n++)
																				{
																					if(n%2==0)
																					{
																						if(tmpmp[n].startsWith("Set-Cookie"))
																						{
																							setCookieStr =setCookieStr+tmpmp[n+1].trim()+";";
																						}
																					}
																				}
																				String OKrequestData="";
																				String[] requestDatas = requestData.split("\r\n");
																				boolean addCookie = true;
																				
																				for(int n=0;n<requestDatas.length;n++)
																				{		
																					requestDatas[n] = requestDatas[n]+"\r\n";
																					if(requestDatas[n].toLowerCase().trim().startsWith("cookie:"))
																					{
																						String[] tmpCookie = requestDatas[n].toLowerCase().trim().split("cookie:");
																						if(tmpCookie.length>1)
																						{
																							tmpCookie[1] = Method.replaceCookie(tmpCookie[1].trim(),setCookieStr);
																							requestDatas[n] = "Cookie: "+Method.stringsToString(tmpCookie)+"\r\n";
																							addCookie = false;
																						}
																					}
																				}
				                                    	  
																				if(addCookie)
																				{
																					OKrequestData =Method.addHeader(requestData,"Cookie: ",setCookieStr);
																				}
																				else
																				{
																					OKrequestData = Method.stringsToString(requestDatas);
																				}
																				requestData = OKrequestData; 
																				}
																				catch(Exception e5)
																				{
																					
																				}
				                                		
																				  
				                                        
																			}
																			else
																			{
																			
																			}
																			
				                                      	
																			int randomStringTmpLength = lineTxtTmp[6].trim().split(":").length;
																			if (randomStringTmpLength > 1)
																			{
																				String randomStringString = lineTxtTmp[6].trim().split(":")[1].trim();
																				String[] randomStringItemStrings = randomStringString.split(",");
																				int randomStringItemCount = randomStringItemStrings.length;
																				for (int k = 0; k < randomStringItemCount; k++)
																				{
																					String[] randomStringItem = new String[5];
																					randomStringItem[0] = Method.getFromBase64(randomStringItemStrings[k]).split(",")[0];
																					randomStringItem[1] = Method.getFromBase64(randomStringItemStrings[k]).split(",")[1];
																					randomStringItem[2] =Method.getFromBase64(randomStringItemStrings[k]).split(",")[2].substring(0, 1);
																					randomStringItem[4] = Method.getFromBase64(randomStringItemStrings[k]).split(",")[3];
												
																					if (Integer.valueOf(randomStringItem[1]).intValue() == 3)
																					{
																						randomStringItem[3] = randomStringItem[2].substring(1);
																						randomStringItem[2] = randomStringItem[2].substring(0, 1);
																					}																	
				                                        		
																					String strTmp = "rSI"+randomStringItem[0];
																					strTmp = Method.getBase64(strTmp);
																					strTmp = Method.addSignRegex(strTmp);
																					String randomStrTmp = "";
																					switch(Integer.valueOf(randomStringItem[1]))
																					{
																						case 0:randomStrTmp =new RandomString().getRandomString(Integer.valueOf(randomStringItem[2]),"i");requestData =requestData.replaceAll(strTmp, randomStrTmp);break;
																						case 1:randomStrTmp =new RandomString().getRandomString(Integer.valueOf(randomStringItem[2]),"l");requestData =requestData.replaceAll(strTmp, randomStrTmp);break;
																						case 2:randomStrTmp =new RandomString().getRandomString(Integer.valueOf(randomStringItem[2]),"il");requestData =requestData.replaceAll(strTmp, randomStrTmp);break;
																						case 3:randomStrTmp =new RandomString().getRandomStringFromDefineString(randomStringItem[3],Integer.valueOf(randomStringItem[2]));requestData =requestData.replaceAll(strTmp, randomStrTmp);break;																		
																					}	                                        		
																					randomStringList.add(randomStringItem);
																				}
																				
																			}
															
																			
																			int captchaTmpLength = lineTxtTmp[7].trim().split(":").length;
																			if (captchaTmpLength > 1)
																			{
																				String captchaString = lineTxtTmp[7].trim().split(":")[1].trim();
																				String[] captchaItemStrings = captchaString.split(",");
																				int captchaItemCount = captchaItemStrings.length;
																				for (int k = 0; k < captchaItemCount; k++)
																				{
																					String[] captchaItem = new String[4];
																					captchaItem = Method.getFromBase64(captchaItemStrings[k]).split(",");
																					captchaList.add(captchaItem);
																					
																					String captchaStrTmp = "sPFC"+captchaItem[0];
																					captchaStrTmp = Method.getBase64(captchaStrTmp);
																					captchaStrTmp = Method.addSignRegex(captchaStrTmp);
				                                            	                                            
																					boolean status = UUAPI.checkAPI();
																					if (!status)
																					{
																						JOptionPane.showMessageDialog(null, "API文件校验失败，无法使用打码服务", "Warning", 2);
																					}
																					else
																					{
																						final String ImgURL = "http://"+host+previousURL+captchaItem[1];  //获取验证码的url  
																					//	System.out.println(ImgURL);                                           
																						final int CaptchaNumbersTextInt = Integer.valueOf(captchaItem[2]);
																						final String saveFileName = "img/tmp.jpg";
																						
																						String saveAbsolutePath = Method.getImgFromURL(ImgURL,saveFileName);
																						if (saveAbsolutePath != null)
																						{
																							if (CaptchaNumbersTextInt != 0)
																							{
																								try 
																								{
																									String[] captchaResult = UUAPI.easyDecaptcha(saveFileName, 1000 + CaptchaNumbersTextInt);
																									if (captchaResult[1].length() > 0)
																									{
																										requestData = requestData.replaceAll(captchaStrTmp, captchaResult[1]);   
																									}
																								} 
																								catch (IOException eee) 
																								{
																									eee.printStackTrace();
																								}	                                                    
																							}		                                      					
																						}																				
																					}		                                            
																				}
																			}
				                                                                             	
				                                        
																			//useFileForCycleWithPackageLineText
																			String useFileForCycleWithPackageLineStrTmp1 = "File-1";
																			useFileForCycleWithPackageLineStrTmp1 = Method.getBase64(useFileForCycleWithPackageLineStrTmp1);
																			useFileForCycleWithPackageLineStrTmp1 = Method.addSignRegex(useFileForCycleWithPackageLineStrTmp1);
																			requestData = requestData.replaceAll(useFileForCycleWithPackageLineStrTmp1, useFileForCycleWithPackageLineText1);
																			
																			
																			String useFileForCycleWithPackageLineStrTmp2 = "File-2";
																			useFileForCycleWithPackageLineStrTmp2 = Method.getBase64(useFileForCycleWithPackageLineStrTmp2);
																			useFileForCycleWithPackageLineStrTmp2 = Method.addSignRegex(useFileForCycleWithPackageLineStrTmp2);
																			requestData = requestData.replaceAll(useFileForCycleWithPackageLineStrTmp2, useFileForCycleWithPackageLineText2);
				                                      		
				                                      		
																			// 发送http 数据
																			
																			responseData = Method.bytesToString(Method.getHttPResponseData(host,port,requestData));
																		//	System.out.println(requestData);
																		//	System.out.println("--");
																		//	System.out.println(responseData);
																			responseStatus = Method.getHttpResponseHeaderStatus(responseData);
																															
				                                      		
																			int setGoAheadOrStopFromStatusTmpLength = lineTxtTmp[9].trim().split(":").length;
																			if (setGoAheadOrStopFromStatusTmpLength > 1)
																			{
																				String setGoAheadOrStopFromStatusString = lineTxtTmp[9].trim().split(":")[1].trim();
																				String[] setGoAheadOrStopFromStatusItemStrings = setGoAheadOrStopFromStatusString.split(",");
																				int setGoAheadOrStopFromStatusItemCount = setGoAheadOrStopFromStatusItemStrings.length;
																				for (int k = 0; k < setGoAheadOrStopFromStatusItemCount; k++)
																				{
																					String[] setGoAheadOrStopFromStatusItem = new String[2];
																					setGoAheadOrStopFromStatusItem = Method.getFromBase64(setGoAheadOrStopFromStatusItemStrings[k]).split(",");
																					setGoAheadOrStopFromStatusList.add(setGoAheadOrStopFromStatusItem);													
																				}
																			}
				                                      		
																			int setGoAheadOrStopFromStatusListCount = setGoAheadOrStopFromStatusList.size();
																			if (setGoAheadOrStopFromStatusListCount > 0)
																			{
																				for (int j = 0; j < setGoAheadOrStopFromStatusListCount; j++)
																				{
																					String[] setGoAheadOrStopFromStatusListItem = (String[])setGoAheadOrStopFromStatusList.get(j);
																					
																					if (responseStatus == Integer.valueOf(setGoAheadOrStopFromStatusListItem[0]))
																					{
																						String actionForStatus = setGoAheadOrStopFromStatusListItem[1];
				                                            					                                            			
																						//                         GO ahead current   stop current 
																						///                                               ||
																						//																		
																						///											Go Exp    Stop Exp
																						///																		
																						
																						
																						if(actionForStatus.equals("Go Ahead EXP"))
																						{
																							GoAheadExp  = true;
																							whyToStop = "responseStatus; " + responseStatus + " -- Rule; " + setGoAheadOrStopFromStatusListItem[0] + " --Action; "+ setGoAheadOrStopFromStatusListItem[1];
																							break;
																						}
																						else if(actionForStatus.equals("Stop EXP"))
																						{
																							StopExp = true;
				                                            				
																							whyToStop = "responseStatus; " + responseStatus + " -- Rule; " + setGoAheadOrStopFromStatusListItem[0] + " --Action; "+ setGoAheadOrStopFromStatusListItem[1];
																							break;
																						}
																						else if(actionForStatus.equals("Go Ahead Current"))
																						{
																							GoAheadCurrent = true;
																							whyToStop = "responseStatus; " + responseStatus + " -- Rule; " + setGoAheadOrStopFromStatusListItem[0]+ " --Action; " + setGoAheadOrStopFromStatusListItem[1];
																							break;
																						}
																						else if(actionForStatus.equals("Stop Current"))
																						{
																							//StopCurrent = true;
																							//whyToStop = "responseStatus:" + responseStatus + " -- Rule" + setGoAheadOrStopFromStatusListItem[0] + setGoAheadOrStopFromStatusListItem[1];
																							//System.out.println(whyToStop);
																							//break;
																						}                                      			
																						
																					}
																				}
																			}
																			else
																			{
																				//System.out.println("mei you setGoAheadOrStopFromStatusListCount");
																			}

																			if ((StopExp) )
																			{
																				JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,"- -",useFileForCycleWithPackageLineText1+"-|-"+useFileForCycleWithPackageLineText2,whyToStop,"show"});
																				ExpPackageHistoryItem[0] = StepFileNameTmp;
																				ExpPackageHistoryItem[1] = requestData;
																				ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																				previousData = responseData;
																				ExpPackageHistoryItem[3] = responseData;
																				ExpPackageHistoryItem[4] = whyToStop;
																				ExpPackageHistoryList.add(ExpPackageHistoryItem);
																				expHTTPHistoryList.add(ExpPackageHistoryItem);
																				ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																				ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																				ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																				ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																				ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																				ExpPackageHistoryItem = new String[7];
																				responseStatus = 0;
																				responseData = "";
																				whyToStop = "Run Over";
																				break;
																			}
				                                            
																			if(GoAheadCurrent)
																			{
																				JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,"- -",useFileForCycleWithPackageLineText1+"-|-"+useFileForCycleWithPackageLineText2,whyToStop,"show"});
																				ExpPackageHistoryItem[0] = StepFileNameTmp;
																				ExpPackageHistoryItem[1] = requestData;
																				ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																				previousData = responseData;
																				ExpPackageHistoryItem[3] = responseData;
																				ExpPackageHistoryItem[4] = whyToStop;
																				ExpPackageHistoryList.add(ExpPackageHistoryItem);
																				expHTTPHistoryList.add(ExpPackageHistoryItem);
																				ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																				ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																				ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																				ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																				ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																				ExpPackageHistoryItem = new String[7];
																				responseStatus = 0;
																				responseData = "";
																				whyToStop = "Run Over";
																				GoAheadCurrent = false;
																				break;
																			}
				                                            
																			if(GoAheadExp)
																			{
																				JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,"- -",useFileForCycleWithPackageLineText1+"-|-"+useFileForCycleWithPackageLineText2,whyToStop,"show"});
																				ExpPackageHistoryItem[0] = StepFileNameTmp;
																				ExpPackageHistoryItem[1] = requestData;
																				ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																				previousData = responseData;
																				ExpPackageHistoryItem[3] = responseData;
																				ExpPackageHistoryItem[4] = whyToStop;
																				ExpPackageHistoryList.add(ExpPackageHistoryItem);
																				expHTTPHistoryList.add(ExpPackageHistoryItem);
																				ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																				ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																				ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																				ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																				ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																				ExpPackageHistoryItem = new String[7];
																				responseStatus = 0;
																				responseData = "";
																				whyToStop = "Run Over";
																				break;
																			}
				                                            
																		
																			
																			int setGoAheadOrStopFromDataTmpLength = lineTxtTmp[10].trim().split(":").length;
																			if (setGoAheadOrStopFromDataTmpLength > 1)
																			{
																				String setGoAheadOrStopFromDataString = lineTxtTmp[10].trim().split(":")[1].trim();
																				String[] setGoAheadOrStopFromDataItemStrings = setGoAheadOrStopFromDataString.split(",");
																				int setGoAheadOrStopFromDataItemCount = setGoAheadOrStopFromDataItemStrings.length;
																				for (int k = 0; k < setGoAheadOrStopFromDataItemCount; k++)
																				{
																					String[] setGoAheadOrStopFromDataItem = new String[2];
																					setGoAheadOrStopFromDataItem = Method.getFromBase64(setGoAheadOrStopFromDataItemStrings[k]).split(",");
																					setGoAheadOrStopFromDataList.add(setGoAheadOrStopFromDataItem);
																				}
																			}
																			                                           
																			
																			int j;
																			int setGoAheadOrStopFromDataListCount = setGoAheadOrStopFromDataList.size();
																			if (setGoAheadOrStopFromDataListCount > 0)
																			{
																				for (j = 0; j < setGoAheadOrStopFromDataListCount; j++)
																				{
																					String[] setGoAheadOrStopFromDataListItem = (String[])setGoAheadOrStopFromDataList.get(j);

																					Pattern p=Pattern.compile(Method.getFromBase64(setGoAheadOrStopFromDataListItem[0]));
																					Matcher m=p.matcher(responseData);
																				//	System.out.println(m.find());
																					
																					if (m.find())
																					{
																						String actionForData = setGoAheadOrStopFromDataListItem[1];
																						
																						if(actionForData.equals("Go Ahead EXP"))
																						{
																							GoAheadExp  = true;
																							whyToStop = " -- Rule; " + Method.getFromBase64(setGoAheadOrStopFromDataListItem[0]) +"  --result; "+m.group()+ " --Action; "+ setGoAheadOrStopFromDataListItem[1];
																							break;
																						}
																						else if(actionForData.equals("Stop EXP"))
																						{
																							StopExp = true;
																							whyToStop = " -- Rule; " + Method.getFromBase64(setGoAheadOrStopFromDataListItem[0]) +"  --result; "+m.group()+ " --Action; "+ setGoAheadOrStopFromDataListItem[1];
																							break;
																						}
																						else if(actionForData.equals("Go Ahead Current"))
																						{
																							GoAheadCurrent = true;
																							whyToStop = " -- Rule; " + Method.getFromBase64(setGoAheadOrStopFromDataListItem[0]) +"  --result; "+m.group()+ " --Action; "+ setGoAheadOrStopFromDataListItem[1];
																							break;
																						}
																						else if(actionForData.equals("Stop Current"))
																						{
																							//StopCurrent = true;
																							//whyToStop = "responseStatus:" + responseStatus + " -- Rule" + setGoAheadOrStopFromStatusListItem[0] + setGoAheadOrStopFromStatusListItem[1];
																							//System.out.println(whyToStop);
																							//break;
																						} 
																						
																					}
																				}
																			}
																			else
																			{
																				//System.out.println("meiyou setGoAheadOrStopFromDataList");
																			}

																			if ((StopExp) )
																			{
																				JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,"- -",useFileForCycleWithPackageLineText1+"-|-"+useFileForCycleWithPackageLineText2,whyToStop,"show"});
																				ExpPackageHistoryItem[0] = StepFileNameTmp;
																				ExpPackageHistoryItem[1] = requestData;
																				ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																				previousData = responseData;
																				ExpPackageHistoryItem[3] = responseData;
																				ExpPackageHistoryItem[4] = whyToStop;
																				ExpPackageHistoryList.add(ExpPackageHistoryItem);
																				expHTTPHistoryList.add(ExpPackageHistoryItem);
																				ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																				ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																				ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																				ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																				ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																				ExpPackageHistoryItem = new String[7];
																				responseStatus = 0;
																				responseData = "";
																				whyToStop = "Run Over";
																				break;
																			}
				                                            
																			if(GoAheadCurrent)
																			{
																				JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,"- -",useFileForCycleWithPackageLineText1+"-|-"+useFileForCycleWithPackageLineText2,whyToStop,"show"});
																				ExpPackageHistoryItem[0] = StepFileNameTmp;
																				ExpPackageHistoryItem[1] = requestData;
																				ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																				previousData = responseData;
																				ExpPackageHistoryItem[3] = responseData;
																				ExpPackageHistoryItem[4] = whyToStop;
																				ExpPackageHistoryList.add(ExpPackageHistoryItem);
																				expHTTPHistoryList.add(ExpPackageHistoryItem);
																				ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																				ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																				ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																				ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																				ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																				ExpPackageHistoryItem = new String[7];
																				responseStatus = 0;
																				responseData = "";
																				whyToStop = "Run Over";
																				GoAheadCurrent = false;
																				break;
																			}
				                                            
																			if(GoAheadExp)
																			{
																				JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,"- -",useFileForCycleWithPackageLineText1+"-|-"+useFileForCycleWithPackageLineText2,whyToStop,"show"});
																				ExpPackageHistoryItem[0] = StepFileNameTmp;
																				ExpPackageHistoryItem[1] = requestData;
																				ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																				previousData = responseData;
																				ExpPackageHistoryItem[3] = responseData;
																				ExpPackageHistoryItem[4] = whyToStop;
																				ExpPackageHistoryList.add(ExpPackageHistoryItem);
																				expHTTPHistoryList.add(ExpPackageHistoryItem);
																				ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																				ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																				ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																				ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																				ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																				ExpPackageHistoryItem = new String[7];
																				responseStatus = 0;
																				responseData = "";
																				whyToStop = "Run Over";
																				break;
																			}

																			JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,"- -",useFileForCycleWithPackageLineText1+"-|-"+useFileForCycleWithPackageLineText2,whyToStop,"show"});
																			ExpPackageHistoryItem[0] = StepFileNameTmp;
																			ExpPackageHistoryItem[1] = requestData;
																			ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																			previousData = responseData;
																			ExpPackageHistoryItem[3] = responseData;
																			ExpPackageHistoryItem[4] = whyToStop;
																			ExpPackageHistoryList.add(ExpPackageHistoryItem);
																			expHTTPHistoryList.add(ExpPackageHistoryItem);
																			ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																			ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																			ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																			ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																			ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																			ExpPackageHistoryItem = new String[7];
																			responseStatus = 0;
																			responseData = "";
																			whyToStop = "Run Over";
				                                      		
																		}
																	}
																	//组合方式二  一对多
																	else if(Integer.valueOf(useFileForCycleWithPackageItem[3])==2)
																	{
																		
																		File useFileForCycleWithPackageFile1 = new File(fileDir+useFileForCycleWithPackageItem[1]);
																		
																		InputStreamReader useFileForCycleWithPackageFile1InputSteamReadTmp = new InputStreamReader(new FileInputStream(useFileForCycleWithPackageFile1));					
																		
																		BufferedReader useFileForCycleWithPackageFile1ExpFileBufferedReader = new BufferedReader(useFileForCycleWithPackageFile1InputSteamReadTmp);
																		
																		String useFileForCycleWithPackageLineText1 = null;
																							
																		int PackageNum=0;
																		while (((useFileForCycleWithPackageLineText1 = useFileForCycleWithPackageFile1ExpFileBufferedReader.readLine()) != null))
																		{
																			File useFileForCycleWithPackageFile2 = new File(fileDir+useFileForCycleWithPackageItem[2]);
																			InputStreamReader useFileForCycleWithPackageFile2InputSteamReadTmp = new InputStreamReader(new FileInputStream(useFileForCycleWithPackageFile2));
																			BufferedReader useFileForCycleWithPackageFile2ExpFileBufferedReader = new BufferedReader(useFileForCycleWithPackageFile2InputSteamReadTmp);
																			String useFileForCycleWithPackageLineText2 = null;
																			while(((useFileForCycleWithPackageLineText2 = useFileForCycleWithPackageFile2ExpFileBufferedReader.readLine()) != null))
																			{
																		
																				PackageNum++;
																				if (lineTxtTmp[1].trim().split(":").length > 1)
																				{
																					host = Method.getFromBase64(lineTxtTmp[1].trim().split(":")[1]);																		
																				}
																				else
																				{
																					break;
																				}

																				
																				if (lineTxtTmp[2].trim().split(":").length > 1)
																				{
																					port = Integer.valueOf(Method.getFromBase64(lineTxtTmp[2].trim().split(":")[1]));
																				}
																				else
																				{
																					port=80;
																				}
																				
																				StepFileNameTmp = Method.getFromBase64(lineTxtTmp[3].trim().split(":")[1]);									
																				if(StepFileNameTmp.length()>0)
																				{
																					requestData = Method.bytesToString(Method.HexStringTobytes(Method.stringsToString(Method.readFileToHexStrings(fileDir+"\\"+StepFileNameTmp))));
																				}																	
					                                      
																				
																				if(previousURL.length()>0)
																				{
																					// 添加 phpcms/  到request 的 GET  phpcms/。。。。  http/1.1
																					requestData = Method.addPreviousURL(requestData,previousURL);
																				}
																				
																				String replaceHostPortString = lineTxtTmp[4].trim().split(":")[1].trim();
																				if (replaceHostPortString.equals("true"))
																				{
																					host = targetHost;
																					port = targetPort;
																					requestData = Method.replaceHostPort(requestData,host,port);
																				}
																				else
																				{
																				}
																				
					                                      
																				String useSetCookieFromPreviousString = lineTxtTmp[5].trim().split(":")[1].trim();
																				if (useSetCookieFromPreviousString.equals("true"))
																				{
																					/*
																					previousData = "HTTP/1.1 200 OK"+"\r\n"+
																										  "Date: Sun, 30 Nov 2014 09:16:33 GMT"+"\r\n"+
																										  "Server: Apache/2.2.9 (APMServ) PHP/5.2.6+"+"\r\n"+
																										  "X-Powered-By: PHP/5.2.6"+"\r\n"+
																										  "Set-Cookie: SYSZC=d2d4a91a4e41bea2078b09c29cd6a44a"+"\r\n"+
																										  "Set-Cookie: SYSUSER=admin"+"\r\n"+
																										  "Set-Cookie: SYSNAME=%E7%B3%BB%E7%BB%9F%E7%AE%A1%E7%90%86%E5%91%98"+"\r\n"+
																										  "Set-Cookie: SYSUSERID=3"+"\r\n"+
																										  "Set-Cookie: SYSTM=1417338994"+"\r\n"+
																										  "Content-Length: 2"+"\r\n"+
																										  "Content-Type: text/html"+"\r\n"+
																										  "\r\n"+							  
																										  "OK";
					                                        
					                                        						*/
																					
																					String[] tmpmp = Method.getHTTPHeadersParams(previousData);                                		
																					String setCookieStr = "";
																					try
																					{
																					for(int n=0;n<tmpmp.length;n++)
																					{
																						if(n%2==0)
																						{
																							if(tmpmp[n].startsWith("Set-Cookie"))
																							{
																								setCookieStr =setCookieStr+tmpmp[n+1].trim()+";";
																							}
																						}
																					}
																					String OKrequestData="";
																					String[] requestDatas = requestData.split("\r\n");
																					boolean addCookie = true;
																					for(int n=0;n<requestDatas.length;n++)
																					{		
																						requestDatas[n] = requestDatas[n]+"\r\n";
																						if(requestDatas[n].toLowerCase().trim().startsWith("cookie:"))
																						{
																							String[] tmpCookie = requestDatas[n].toLowerCase().trim().split("cookie:");
																							if(tmpCookie.length>1)
																							{
																								tmpCookie[1] = Method.replaceCookie(tmpCookie[1].trim(),setCookieStr);
																								requestDatas[n] = "Cookie: "+Method.stringsToString(tmpCookie)+"\r\n";
																								addCookie = false;
																							}
																						}
																					}
					                                    	  
																					if(addCookie)
																					{
																						OKrequestData =Method.addHeader(requestData,"Cookie: ",setCookieStr);
																					}
																					else
																					{
																						OKrequestData = Method.stringsToString(requestDatas);
																					}
																					requestData = OKrequestData; 
																					}
																					catch(Exception e5)
																					{}
					                                		
																					  
					                                        
																				}
																				else
																				{
																				}
																		
					                                      	
																				int randomStringTmpLength = lineTxtTmp[6].trim().split(":").length;
																				if (randomStringTmpLength > 1)
																				{
																					String randomStringString = lineTxtTmp[6].trim().split(":")[1].trim();
																					String[] randomStringItemStrings = randomStringString.split(",");
																					int randomStringItemCount = randomStringItemStrings.length;
																					
																					for (int k = 0; k < randomStringItemCount; k++)
																					{
																						String[] randomStringItem = new String[5];
																						randomStringItem[0] = Method.getFromBase64(randomStringItemStrings[k]).split(",")[0];
																						randomStringItem[1] = Method.getFromBase64(randomStringItemStrings[k]).split(",")[1];
																						randomStringItem[2] =Method.getFromBase64(randomStringItemStrings[k]).split(",")[2].substring(0, 1);
																						randomStringItem[4] = Method.getFromBase64(randomStringItemStrings[k]).split(",")[3];
																					
																						if (Integer.valueOf(randomStringItem[1]).intValue() == 3)
																						{
																							randomStringItem[3] = randomStringItem[2].substring(1);
																							randomStringItem[2] = randomStringItem[2].substring(0, 1);
																						}																			
					                                        		
																						String strTmp = "rSI"+randomStringItem[0];
																						strTmp = Method.getBase64(strTmp);
																						strTmp = Method.addSignRegex(strTmp);
																						String randomStrTmp = "";
																						switch(Integer.valueOf(randomStringItem[1]))
																						{
																							case 0:randomStrTmp =new RandomString().getRandomString(Integer.valueOf(randomStringItem[2]),"i");requestData =requestData.replaceAll(strTmp, randomStrTmp);break;
																							case 1:randomStrTmp =new RandomString().getRandomString(Integer.valueOf(randomStringItem[2]),"l");requestData =requestData.replaceAll(strTmp, randomStrTmp);break;
																							case 2:randomStrTmp =new RandomString().getRandomString(Integer.valueOf(randomStringItem[2]),"il");requestData =requestData.replaceAll(strTmp, randomStrTmp);break;
																							case 3:randomStrTmp =new RandomString().getRandomStringFromDefineString(randomStringItem[3],Integer.valueOf(randomStringItem[2]));requestData =requestData.replaceAll(strTmp, randomStrTmp);break;																																																		
																						}
					                                        		
																						randomStringList.add(randomStringItem);
																						
																					}
																					
																				}
					                                      	
																			
																				int captchaTmpLength = lineTxtTmp[7].trim().split(":").length;
																				if (captchaTmpLength > 1)
																				{
																					String captchaString = lineTxtTmp[7].trim().split(":")[1].trim();
																					String[] captchaItemStrings = captchaString.split(",");
																					int captchaItemCount = captchaItemStrings.length;
																					for (int k = 0; k < captchaItemCount; k++)
																					{
																						String[] captchaItem = new String[4];
																						captchaItem = Method.getFromBase64(captchaItemStrings[k]).split(",");
																						captchaList.add(captchaItem);
																						
																						String captchaStrTmp = "sPFC"+captchaItem[0];
																						captchaStrTmp = Method.getBase64(captchaStrTmp);
																						captchaStrTmp = Method.addSignRegex(captchaStrTmp);
					                                            			                                            
																						boolean status = UUAPI.checkAPI();

																						if (!status)
																						{
																							JOptionPane.showMessageDialog(null, "API文件校验失败，无法使用打码服务", "Warning", 2);
																						}
																						else
																						{
																							final String ImgURL = "http://"+host+previousURL+captchaItem[1];  //获取验证码的url  
																						//	System.out.println(ImgURL);                                       
																							final int CaptchaNumbersTextInt = Integer.valueOf(captchaItem[2]);
																							final String saveFileName = "img/tmp.jpg";
																							
																							String saveAbsolutePath = Method.getImgFromURL(ImgURL,saveFileName);
																							if (saveAbsolutePath != null)
																							{
																								if (CaptchaNumbersTextInt != 0)
																								{
																									try 
																									{
																										String[] captchaResult = UUAPI.easyDecaptcha(saveFileName, 1000 + CaptchaNumbersTextInt);
																										if (captchaResult[1].length() > 0)
																										{
																											requestData = requestData.replaceAll(captchaStrTmp, captchaResult[1]);   
																										}
																									} 
																									catch (IOException eee) 
																									{
																										eee.printStackTrace();
																									}	                                                    
																								}		                                      					
																							}																				
																						}		                                            
																					}
																				}
					                                        			                                      
					                                        
																				//useFileForCycleWithPackageLineText
																				String useFileForCycleWithPackageLineStrTmp1 = "File-1";
																				useFileForCycleWithPackageLineStrTmp1 = Method.getBase64(useFileForCycleWithPackageLineStrTmp1);
																				useFileForCycleWithPackageLineStrTmp1 = Method.addSignRegex(useFileForCycleWithPackageLineStrTmp1);
																				requestData = requestData.replaceAll(useFileForCycleWithPackageLineStrTmp1, useFileForCycleWithPackageLineText1);
																																					
																				String useFileForCycleWithPackageLineStrTmp2 = "File-2";
																				useFileForCycleWithPackageLineStrTmp2 = Method.getBase64(useFileForCycleWithPackageLineStrTmp2);
																				useFileForCycleWithPackageLineStrTmp2 = Method.addSignRegex(useFileForCycleWithPackageLineStrTmp2);
																				requestData = requestData.replaceAll(useFileForCycleWithPackageLineStrTmp2, useFileForCycleWithPackageLineText2);
					                                      																
					                                      		
																				// 发送http 数据
																				
																				responseData = Method.bytesToString(Method.getHttPResponseData(host,port,requestData));
																			//	System.out.println(requestData);
																			//	System.out.println("--");
																			//	System.out.println(responseData);
																				responseStatus = Method.getHttpResponseHeaderStatus(responseData);
																				
																				
																				int setGoAheadOrStopFromStatusTmpLength = lineTxtTmp[9].trim().split(":").length;
																				if (setGoAheadOrStopFromStatusTmpLength > 1)
																				{
																					String setGoAheadOrStopFromStatusString = lineTxtTmp[9].trim().split(":")[1].trim();
																					String[] setGoAheadOrStopFromStatusItemStrings = setGoAheadOrStopFromStatusString.split(",");
																					int setGoAheadOrStopFromStatusItemCount = setGoAheadOrStopFromStatusItemStrings.length;
																					for (int k = 0; k < setGoAheadOrStopFromStatusItemCount; k++)
																					{
																						String[] setGoAheadOrStopFromStatusItem = new String[2];
																						setGoAheadOrStopFromStatusItem = Method.getFromBase64(setGoAheadOrStopFromStatusItemStrings[k]).split(",");
																						setGoAheadOrStopFromStatusList.add(setGoAheadOrStopFromStatusItem);															
																					}
																				}		                                      	
					                                      		
																				int setGoAheadOrStopFromStatusListCount = setGoAheadOrStopFromStatusList.size();
																				if (setGoAheadOrStopFromStatusListCount > 0)
																				{														
																					for (int j = 0; j < setGoAheadOrStopFromStatusListCount; j++)
																					{
																						String[] setGoAheadOrStopFromStatusListItem = (String[])setGoAheadOrStopFromStatusList.get(j);
																				
																						if (responseStatus == Integer.valueOf(setGoAheadOrStopFromStatusListItem[0]))
																						{
																							String actionForStatus = setGoAheadOrStopFromStatusListItem[1];
					                                            			
																							//                         GO ahead current   stop current 
																							///                                               ||
																							//																		
																							///											Go Exp    Stop Exp
																							///
																						
																							if(actionForStatus.equals("Go Ahead EXP"))
																							{
																								GoAheadExp  = true;
																								whyToStop = "responseStatus; " + responseStatus + " -- Rule; " + setGoAheadOrStopFromStatusListItem[0] + " --Action; "+ setGoAheadOrStopFromStatusListItem[1];
																								break;
																							}
																							else if(actionForStatus.equals("Stop EXP"))
																							{
																								StopExp = true;
																								whyToStop = "responseStatus; " + responseStatus + " -- Rule; " + setGoAheadOrStopFromStatusListItem[0] + " --Action; "+ setGoAheadOrStopFromStatusListItem[1];
																								break;
																							}
																							else if(actionForStatus.equals("Go Ahead Current"))
																							{
																								GoAheadCurrent = true;
																								whyToStop = "responseStatus; " + responseStatus + " -- Rule; " + setGoAheadOrStopFromStatusListItem[0]+ " --Action; " + setGoAheadOrStopFromStatusListItem[1];
																								break;
																							}
																							else if(actionForStatus.equals("Stop Current"))
																							{
																								//StopCurrent = true;
																								//whyToStop = "responseStatus:" + responseStatus + " -- Rule" + setGoAheadOrStopFromStatusListItem[0] + setGoAheadOrStopFromStatusListItem[1];
																								//System.out.println(whyToStop);
																								//break;
																							}                                      			
																						}
																					}
																				}
																				else
																				{
																					//System.out.println("mei you setGoAheadOrStopFromStatusListCount");
																				}

																				if ((StopExp) )
																				{
																					JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,"- -",useFileForCycleWithPackageLineText1+"-|-"+useFileForCycleWithPackageLineText2,whyToStop,"show"});
																					ExpPackageHistoryItem[0] = StepFileNameTmp;
																					ExpPackageHistoryItem[1] = requestData;
																					ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																					previousData = responseData;
																					ExpPackageHistoryItem[3] = responseData;
																					ExpPackageHistoryItem[4] = whyToStop;
																					ExpPackageHistoryList.add(ExpPackageHistoryItem);
																					expHTTPHistoryList.add(ExpPackageHistoryItem);
																					ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																					ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																					ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																					ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																					ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																					ExpPackageHistoryItem = new String[7];
																					responseStatus = 0;
																					responseData = "";
																					whyToStop = "Run Over";
						                                      		
																					break;
																				}
					                                            
																				if(GoAheadCurrent)
																				{
																					JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,"- -",useFileForCycleWithPackageLineText1+"-|-"+useFileForCycleWithPackageLineText2,whyToStop,"show"});
																					ExpPackageHistoryItem[0] = StepFileNameTmp;
																					ExpPackageHistoryItem[1] = requestData;
																					ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																					previousData = responseData;
																					ExpPackageHistoryItem[3] = responseData;
																					ExpPackageHistoryItem[4] = whyToStop;
																					ExpPackageHistoryList.add(ExpPackageHistoryItem);
																					expHTTPHistoryList.add(ExpPackageHistoryItem);
																					ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																					ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																					ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																					ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																					ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																					ExpPackageHistoryItem = new String[7];
																					responseStatus = 0;
																					responseData = "";
																					whyToStop = "Run Over";
																					GoAheadCurrent = false;
																					break;
																				}
					                                            
																				if(GoAheadExp)
																				{
																					JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,"- -",useFileForCycleWithPackageLineText1+"-|-"+useFileForCycleWithPackageLineText2,whyToStop,"show"});
																					ExpPackageHistoryItem[0] = StepFileNameTmp;
																					ExpPackageHistoryItem[1] = requestData;
																					ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																					previousData = responseData;
																					ExpPackageHistoryItem[3] = responseData;
																					ExpPackageHistoryItem[4] = whyToStop;
																					ExpPackageHistoryList.add(ExpPackageHistoryItem);
																					expHTTPHistoryList.add(ExpPackageHistoryItem);
																					ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																					ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																					ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																					ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																					ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																					ExpPackageHistoryItem = new String[7];
																					responseStatus = 0;
																					responseData = "";
																					whyToStop = "Run Over";
																					break;
																				}
															
																				
																				int setGoAheadOrStopFromDataTmpLength = lineTxtTmp[10].trim().split(":").length;
																				if (setGoAheadOrStopFromDataTmpLength > 1)
																				{
																					String setGoAheadOrStopFromDataString = lineTxtTmp[10].trim().split(":")[1].trim();
																					String[] setGoAheadOrStopFromDataItemStrings = setGoAheadOrStopFromDataString.split(",");
																					int setGoAheadOrStopFromDataItemCount = setGoAheadOrStopFromDataItemStrings.length;
																					for (int k = 0; k < setGoAheadOrStopFromDataItemCount; k++)
																					{
																						String[] setGoAheadOrStopFromDataItem = new String[2];
																						setGoAheadOrStopFromDataItem = Method.getFromBase64(setGoAheadOrStopFromDataItemStrings[k]).split(",");
																						setGoAheadOrStopFromDataList.add(setGoAheadOrStopFromDataItem);																
																					}
																				}	                                            
																				
																				int j;
																				int setGoAheadOrStopFromDataListCount = setGoAheadOrStopFromDataList.size();
																				if (setGoAheadOrStopFromDataListCount > 0)
																				{
																					for (j = 0; j < setGoAheadOrStopFromDataListCount; j++)
																					{
																						String[] setGoAheadOrStopFromDataListItem = (String[])setGoAheadOrStopFromDataList.get(j);

																						Pattern p=Pattern.compile(Method.getFromBase64(setGoAheadOrStopFromDataListItem[0]));
																						Matcher m=p.matcher(responseData);
																						//System.out.println(m.find());
																						
																						if (m.find())
																						{
																							String actionForData = setGoAheadOrStopFromDataListItem[1];
																							
																							if(actionForData.equals("Go Ahead EXP"))
																							{
																								GoAheadExp  = true;
																								whyToStop = " -- Rule; " + Method.getFromBase64(setGoAheadOrStopFromDataListItem[0]) +"  --result; "+m.group()+ " --Action; "+  setGoAheadOrStopFromDataListItem[1];
																								break;
																							}
																							else if(actionForData.equals("Stop EXP"))
																							{
																								StopExp = true;
																								whyToStop = " -- Rule; " + Method.getFromBase64(setGoAheadOrStopFromDataListItem[0]) +"  --result; "+m.group()+ " --Action; "+  setGoAheadOrStopFromDataListItem[1];
																								break;
																							}
																							else if(actionForData.equals("Go Ahead Current"))
																							{
																								GoAheadCurrent = true;
																								whyToStop = " -- Rule; " + Method.getFromBase64(setGoAheadOrStopFromDataListItem[0]) +"  --result; "+m.group()+ " --Action; "+  setGoAheadOrStopFromDataListItem[1];
																								break;
																							}
																							else if(actionForData.equals("Stop Current"))
																							{
																								//StopCurrent = true;
																								//whyToStop = "responseStatus:" + responseStatus + " -- Rule" + setGoAheadOrStopFromStatusListItem[0] + setGoAheadOrStopFromStatusListItem[1];
																								//System.out.println(whyToStop);
																								//break;
																							} 
																							
																						}
																					}
																				}
																				else
																				{
																					//System.out.println("meiyou setGoAheadOrStopFromDataList");
																				}

																				if ((StopExp) )
																				{
																					JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,"- -",useFileForCycleWithPackageLineText1+"-|-"+useFileForCycleWithPackageLineText2,whyToStop,"show"});
																					ExpPackageHistoryItem[0] = StepFileNameTmp;
																					ExpPackageHistoryItem[1] = requestData;
																					ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																					previousData = responseData;
																					ExpPackageHistoryItem[3] = responseData;
																					ExpPackageHistoryItem[4] = whyToStop;
																					ExpPackageHistoryList.add(ExpPackageHistoryItem);
																					expHTTPHistoryList.add(ExpPackageHistoryItem);
																					ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																					ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																					ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																					ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																					ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																					ExpPackageHistoryItem = new String[7];
																					responseStatus = 0;
																					responseData = "";
																					whyToStop = "Run Over";
																					break;
																				}
					                                            
																				if(GoAheadCurrent)
																				{
																					JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,"- -",useFileForCycleWithPackageLineText1+"-|-"+useFileForCycleWithPackageLineText2,whyToStop,"show"});
																					ExpPackageHistoryItem[0] = StepFileNameTmp;
																					ExpPackageHistoryItem[1] = requestData;
																					ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																					previousData = responseData;
																					ExpPackageHistoryItem[3] = responseData;
																					ExpPackageHistoryItem[4] = whyToStop;
																					ExpPackageHistoryList.add(ExpPackageHistoryItem);
																					expHTTPHistoryList.add(ExpPackageHistoryItem);
																					ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																					ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																					ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																					ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																					ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																					ExpPackageHistoryItem = new String[7];
																					responseStatus = 0;
																					responseData = "";
																					whyToStop = "Run Over";
																					GoAheadCurrent = false;
																					break;
																				}
					                                            
																				if(GoAheadExp)
																				{
																					JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,"- -",useFileForCycleWithPackageLineText1+"-|-"+useFileForCycleWithPackageLineText2,whyToStop,"show"});
																					ExpPackageHistoryItem[0] = StepFileNameTmp;
																					ExpPackageHistoryItem[1] = requestData;
																					ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																					previousData = responseData;
																					ExpPackageHistoryItem[3] = responseData;
																					ExpPackageHistoryItem[4] = whyToStop;
																					ExpPackageHistoryList.add(ExpPackageHistoryItem);
																					expHTTPHistoryList.add(ExpPackageHistoryItem);
																					ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																					ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																					ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																					ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																					ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																					ExpPackageHistoryItem = new String[7];
																					responseStatus = 0;
																					responseData = "";
																					whyToStop = "Run Over";
																					break;
																				}

																				JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,"- -",useFileForCycleWithPackageLineText1+"-|-"+useFileForCycleWithPackageLineText2,whyToStop,"show"});
																				ExpPackageHistoryItem[0] = StepFileNameTmp;
																				ExpPackageHistoryItem[1] = requestData;
																				ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																				previousData = responseData;
																				ExpPackageHistoryItem[3] = responseData;
																				ExpPackageHistoryItem[4] = whyToStop;
																				ExpPackageHistoryList.add(ExpPackageHistoryItem);
																				expHTTPHistoryList.add(ExpPackageHistoryItem);
																				ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																				ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																				ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																				ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																				ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																				ExpPackageHistoryItem = new String[7];
																				responseStatus = 0;
																				responseData = "";
																				whyToStop = "Run Over";                	                
																				
																			}
																		}
																	}
																}
															}            
														}
														else ///////////////////////////////////////////////////////////
														{
															//System.out.println("meiyou package file cyle");
															
															
															if (lineTxtTmp[1].trim().split(":").length > 1)
															{
																host = Method.getFromBase64(lineTxtTmp[1].trim().split(":")[1]);		
															}
															else
															{
																break;
															}

															
															if (lineTxtTmp[2].trim().split(":").length > 1)
															{
																port = Integer.valueOf(Method.getFromBase64(lineTxtTmp[2].trim().split(":")[1]));
															}
															else
															{
																port=80;
															}
									
															StepFileNameTmp = Method.getFromBase64(lineTxtTmp[3].trim().split(":")[1]);									
															if(StepFileNameTmp.length()>0)
															{
																requestData = Method.bytesToString(Method.HexStringTobytes(Method.stringsToString(Method.readFileToHexStrings(fileDir+"\\"+StepFileNameTmp))));
															}
															
															if(previousURL.length()>0)
															{
																// 添加 phpcms/  到request 的 GET  phpcms/。。。。  http/1.1
																requestData = Method.addPreviousURL(requestData,previousURL);
															}
				                      
															String replaceHostPortString = lineTxtTmp[4].trim().split(":")[1].trim();
															if (replaceHostPortString.equals("true"))
															{
																host = targetHost;
																port = targetPort;
																requestData = Method.replaceHostPort(requestData,host,port);
															}
															else
															{
															}
															
															String useSetCookieFromPreviousString = lineTxtTmp[5].trim().split(":")[1].trim();
															if (useSetCookieFromPreviousString.equals("true"))
															{
																/*
																previousData = "HTTP/1.1 200 OK"+"\r\n"+
																					  "Date: Sun, 30 Nov 2014 09:16:33 GMT"+"\r\n"+
																					  "Server: Apache/2.2.9 (APMServ) PHP/5.2.6+"+"\r\n"+
																					  "X-Powered-By: PHP/5.2.6"+"\r\n"+
																					  "Set-Cookie: SYSZC=d2d4a91a4e41bea2078b09c29cd6a44a"+"\r\n"+
																					  "Set-Cookie: SYSUSER=admin"+"\r\n"+
																					  "Set-Cookie: SYSNAME=%E7%B3%BB%E7%BB%9F%E7%AE%A1%E7%90%86%E5%91%98"+"\r\n"+
																					  "Set-Cookie: SYSUSERID=3"+"\r\n"+
																					  "Set-Cookie: SYSTM=1417338994"+"\r\n"+
																					  "Content-Length: 2"+"\r\n"+
																					  "Content-Type: text/html"+"\r\n"+
																					  "\r\n"+							  
																					  "OK";
				                        						*/
				                        
																String[] tmpmp = Method.getHTTPHeadersParams(previousData);	                		
																String setCookieStr = "";
																try
																{
																	for(int n=0;n<tmpmp.length;n++)
																	{
																		if(n%2==0)
																		{
																			if(tmpmp[n].startsWith("Set-Cookie"))
																			{
																				setCookieStr =setCookieStr+tmpmp[n+1].trim()+";";
																			}
																		}
																	}
																	String OKrequestData="";        		
																	String[] requestDatas = requestData.split("\r\n");
																	boolean addCookie = true;
																	
																	for(int n=0;n<requestDatas.length;n++)
																	{		
																		requestDatas[n] = requestDatas[n]+"\r\n";
																		if(requestDatas[n].toLowerCase().trim().startsWith("cookie:"))
																		{
																			String[] tmpCookie = requestDatas[n].toLowerCase().trim().split("cookie:");
																			if(tmpCookie.length>1)
																			{
																				tmpCookie[1] = Method.replaceCookie(tmpCookie[1].trim(),setCookieStr);
																				requestDatas[n] = "Cookie: "+Method.stringsToString(tmpCookie)+"\r\n";
																				addCookie = false;
																			}
																		}
																	}
					                    	  
																	if(addCookie)
																	{
																		OKrequestData =Method.addHeader(requestData,"Cookie: ",setCookieStr);		
																	}
																	else
																	{
																		OKrequestData = Method.stringsToString(requestDatas);
																	}
																	requestData = OKrequestData; 
																}
																catch(Exception asdf)
																{
																	
																}
				                		
				                		
																  
				                        
															}
															else
															{
															}									
				                      	
															int randomStringTmpLength = lineTxtTmp[6].trim().split(":").length;
															if (randomStringTmpLength > 1)
															{
																String randomStringString = lineTxtTmp[6].trim().split(":")[1].trim();
																String[] randomStringItemStrings = randomStringString.split(",");
																int randomStringItemCount = randomStringItemStrings.length;
																
																for (int k = 0; k < randomStringItemCount; k++)
																{
																	String[] randomStringItem = new String[5];
																	randomStringItem[0] = Method.getFromBase64(randomStringItemStrings[k]).split(",")[0];
																	randomStringItem[1] = Method.getFromBase64(randomStringItemStrings[k]).split(",")[1];
																	randomStringItem[2] =Method.getFromBase64(randomStringItemStrings[k]).split(",")[2].substring(0, 1);
																	randomStringItem[4] = Method.getFromBase64(randomStringItemStrings[k]).split(",")[3];

																	if (Integer.valueOf(randomStringItem[1]).intValue() == 3)
																	{
																		randomStringItem[3] = randomStringItem[2].substring(1);
																		randomStringItem[2] = randomStringItem[2].substring(0, 1);
																	}															
				                        		
																	String strTmp = "rSI"+randomStringItem[0];
																	strTmp = Method.getBase64(strTmp);
																	strTmp = Method.addSignRegex(strTmp);
																	String randomStrTmp = "";
																	switch(Integer.valueOf(randomStringItem[1]))
																	{
																		case 0:randomStrTmp =new RandomString().getRandomString(Integer.valueOf(randomStringItem[2]),"i");requestData =requestData.replaceAll(strTmp, randomStrTmp);break;
																		case 1:randomStrTmp =new RandomString().getRandomString(Integer.valueOf(randomStringItem[2]),"l");requestData =requestData.replaceAll(strTmp, randomStrTmp);break;
																		case 2:randomStrTmp =new RandomString().getRandomString(Integer.valueOf(randomStringItem[2]),"il");requestData =requestData.replaceAll(strTmp, randomStrTmp);break;
																		case 3:randomStrTmp =new RandomString().getRandomStringFromDefineString(randomStringItem[3],Integer.valueOf(randomStringItem[2]));requestData =requestData.replaceAll(strTmp, randomStrTmp);break;																		
																	}
																	randomStringList.add(randomStringItem);											
																}													
															}

															
															int captchaTmpLength = lineTxtTmp[7].trim().split(":").length;
															if (captchaTmpLength > 1)
															{
																String captchaString = lineTxtTmp[7].trim().split(":")[1].trim();
																String[] captchaItemStrings = captchaString.split(",");
																int captchaItemCount = captchaItemStrings.length;
																for (int k = 0; k < captchaItemCount; k++)
																{
																	String[] captchaItem = new String[4];
																	captchaItem = Method.getFromBase64(captchaItemStrings[k]).split(",");
																	captchaList.add(captchaItem);
																	
																	String captchaStrTmp = "sPFC"+captchaItem[0];
																	captchaStrTmp = Method.getBase64(captchaStrTmp);
																	captchaStrTmp = Method.addSignRegex(captchaStrTmp);
				                            
																	boolean status = UUAPI.checkAPI();

																	if (!status)
																	{
																		JOptionPane.showMessageDialog(null, "API文件校验失败，无法使用打码服务", "Warning", 2);
																	}
																	else
																	{
																		final String ImgURL = "http://"+host+previousURL+captchaItem[1];  //获取验证码的url  
																		//System.out.println(ImgURL);                                         
																		final int CaptchaNumbersTextInt = Integer.valueOf(captchaItem[2]);
																		final String saveFileName = "img/tmp.jpg";
																		
																		String saveAbsolutePath = Method.getImgFromURL(ImgURL,saveFileName);
																		if (saveAbsolutePath != null)
																		{
																			if (CaptchaNumbersTextInt != 0)
																			{
																				try 
																				{
																					String[] captchaResult = UUAPI.easyDecaptcha(saveFileName, 1000 + CaptchaNumbersTextInt);
																					if (captchaResult[1].length() > 0)
																					{
																						requestData = requestData.replaceAll(captchaStrTmp, captchaResult[1]);   
																					}
																				} 
																				catch (IOException eee) 
																				{
																					eee.printStackTrace();
																				}	                                                    
																			}
																		}															
																	}		                            
																}
															}
				                        	                      													
															// 发送http 数据
															//requestData = requestData+"\r\n";
														//	System.out.println(requestData);
															//System.out.println("--");
															//System.out.println(responseData);
															
															responseData = Method.bytesToString(Method.getHttPResponseData(host,port,requestData));
															
															responseStatus = Method.getHttpResponseHeaderStatus(responseData);
																				
				                      		
															int setGoAheadOrStopFromStatusTmpLength = lineTxtTmp[9].trim().split(":").length;
															if (setGoAheadOrStopFromStatusTmpLength > 1)
															{
																String setGoAheadOrStopFromStatusString = lineTxtTmp[9].trim().split(":")[1].trim();
																String[] setGoAheadOrStopFromStatusItemStrings = setGoAheadOrStopFromStatusString.split(",");
																int setGoAheadOrStopFromStatusItemCount = setGoAheadOrStopFromStatusItemStrings.length;
																for (int k = 0; k < setGoAheadOrStopFromStatusItemCount; k++)
																{
																	String[] setGoAheadOrStopFromStatusItem = new String[2];
																	setGoAheadOrStopFromStatusItem = Method.getFromBase64(setGoAheadOrStopFromStatusItemStrings[k]).split(",");
																	setGoAheadOrStopFromStatusList.add(setGoAheadOrStopFromStatusItem);														
																}
															}
				                      		
				                      		
				                      		
				                      		
															int setGoAheadOrStopFromStatusListCount = setGoAheadOrStopFromStatusList.size();
															if (setGoAheadOrStopFromStatusListCount > 0)
															{
																for (int j = 0; j < setGoAheadOrStopFromStatusListCount; j++)
																{
																	String[] setGoAheadOrStopFromStatusListItem = (String[])setGoAheadOrStopFromStatusList.get(j);
																	
																	if (responseStatus == Integer.valueOf(setGoAheadOrStopFromStatusListItem[0]))
																	{
																		String actionForStatus = setGoAheadOrStopFromStatusListItem[1];
				                            			
																		//                         GO ahead current   stop current 
																		///                                               ||
																		//																		
																		///											Go Exp    Stop Exp
																		///										
																		
																		if(actionForStatus.equals("Go Ahead EXP"))
																		{
																			GoAheadExp  = true;
																			whyToStop = "responseStatus; " + responseStatus + " -- Rule; " + setGoAheadOrStopFromStatusListItem[0] + " --Action; "+ setGoAheadOrStopFromStatusListItem[1];
																			break;
																		}
																		else if(actionForStatus.equals("Stop EXP"))
																		{
																			StopExp = true;	                            				
																			whyToStop = "responseStatus; " + responseStatus + " -- Rule; " + setGoAheadOrStopFromStatusListItem[0] + " --Action; "+ setGoAheadOrStopFromStatusListItem[1];
																			break;
																		}
																		else if(actionForStatus.equals("Go Ahead Current"))
																		{
																			GoAheadCurrent = true;
																			whyToStop = "responseStatus; " + responseStatus + " -- Rule; " + setGoAheadOrStopFromStatusListItem[0]+ " --Action; " + setGoAheadOrStopFromStatusListItem[1];
																			break;
																		}
																		else if(actionForStatus.equals("Stop Current"))
																		{
																			//StopCurrent = true;
																			//whyToStop = "responseStatus:" + responseStatus + " -- Rule" + setGoAheadOrStopFromStatusListItem[0] + setGoAheadOrStopFromStatusListItem[1];
																			//System.out.println(whyToStop);
																			//break;
																		}                                      				
																	}
																}
															}
															else
															{
															//	System.out.println("mei you setGoAheadOrStopFromStatusListCount");
															}

															if ((StopExp) )
															{
																JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,"- -","- -",whyToStop,"show"});
																ExpPackageHistoryItem[0] = StepFileNameTmp;
																ExpPackageHistoryItem[1] = requestData;
																ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																previousData = responseData;
																ExpPackageHistoryItem[3] = responseData;
																ExpPackageHistoryItem[4] = whyToStop;
																ExpPackageHistoryList.add(ExpPackageHistoryItem);
																expHTTPHistoryList.add(ExpPackageHistoryItem);
																ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																ExpPackageHistoryItem = new String[7];
																responseStatus = 0;
																responseData = "";
																whyToStop = "Run Over";
																break;
															}
				                            
															if(GoAheadCurrent)
															{
																JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,"- -","- -",whyToStop,"show"});
																ExpPackageHistoryItem[0] = StepFileNameTmp;
																ExpPackageHistoryItem[1] = requestData;
																ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																previousData = responseData;
																ExpPackageHistoryItem[3] = responseData;
																ExpPackageHistoryItem[4] = whyToStop;
																ExpPackageHistoryList.add(ExpPackageHistoryItem);
																expHTTPHistoryList.add(ExpPackageHistoryItem);
																ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																ExpPackageHistoryItem = new String[7];
																responseStatus = 0;
																responseData = "";
																whyToStop = "Run Over";
																GoAheadCurrent = false;
																break;
															}
				                            
															if(GoAheadExp)
															{
																JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,"- -","- -",whyToStop,"show"});
																ExpPackageHistoryItem[0] = StepFileNameTmp;
																ExpPackageHistoryItem[1] = requestData;
																ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																previousData = responseData;
																ExpPackageHistoryItem[3] = responseData;
																ExpPackageHistoryItem[4] = whyToStop;
																ExpPackageHistoryList.add(ExpPackageHistoryItem);
																expHTTPHistoryList.add(ExpPackageHistoryItem);
																ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																ExpPackageHistoryItem = new String[7];
																responseStatus = 0;
																responseData = "";
																whyToStop = "Run Over";
																break;
															}	                            											
															
															int setGoAheadOrStopFromDataTmpLength = lineTxtTmp[10].trim().split(":").length;
															if (setGoAheadOrStopFromDataTmpLength > 1)
															{
																String setGoAheadOrStopFromDataString = lineTxtTmp[10].trim().split(":")[1].trim();
																String[] setGoAheadOrStopFromDataItemStrings = setGoAheadOrStopFromDataString.split(",");
																int setGoAheadOrStopFromDataItemCount = setGoAheadOrStopFromDataItemStrings.length;
																for (int k = 0; k < setGoAheadOrStopFromDataItemCount; k++)
																{
																	String[] setGoAheadOrStopFromDataItem = new String[2];
																	setGoAheadOrStopFromDataItem = Method.getFromBase64(setGoAheadOrStopFromDataItemStrings[k]).split(",");
																	setGoAheadOrStopFromDataList.add(setGoAheadOrStopFromDataItem);												
																}
															}
															                            
															
															int j;
															int setGoAheadOrStopFromDataListCount = setGoAheadOrStopFromDataList.size();
															if (setGoAheadOrStopFromDataListCount > 0)
															{
																for (j = 0; j < setGoAheadOrStopFromDataListCount; j++)
																{
																	String[] setGoAheadOrStopFromDataListItem = (String[])setGoAheadOrStopFromDataList.get(j);

																	
																	Pattern p=Pattern.compile(Method.getFromBase64(setGoAheadOrStopFromDataListItem[0]));
																	Matcher m=p.matcher(responseData);
																	//System.out.println(m.find());
																	
																	if (m.find())
																	{
																		String actionForData = setGoAheadOrStopFromDataListItem[1];
																		
																		if(actionForData.equals("Go Ahead EXP"))
																		{
																			GoAheadExp  = true;
																			whyToStop = " -- Rule; " + Method.getFromBase64(setGoAheadOrStopFromDataListItem[0]) +"  --result; "+m.group()+ " --Action; "+ setGoAheadOrStopFromDataListItem[1];
																			break;
																		}
																		else if(actionForData.equals("Stop EXP"))
																		{
																			StopExp = true;
																			whyToStop = " -- Rule; " + Method.getFromBase64(setGoAheadOrStopFromDataListItem[0]) +"  --result; "+m.group()+ " --Action; "+ setGoAheadOrStopFromDataListItem[1];
																			break;
																		}
																		else if(actionForData.equals("Go Ahead Current"))
																		{
																			GoAheadCurrent = true;
																			whyToStop = " -- Rule; " + Method.getFromBase64(setGoAheadOrStopFromDataListItem[0]) +"  --result; "+m.group()+ " --Action; "+ setGoAheadOrStopFromDataListItem[1];
																			break;
																		}
																		else if(actionForData.equals("Stop Current"))
																		{
																			//StopCurrent = true;
																			//whyToStop = "responseStatus:" + responseStatus + " -- Rule" + setGoAheadOrStopFromStatusListItem[0] + setGoAheadOrStopFromStatusListItem[1];
																			//System.out.println(whyToStop);
																			//break;
																		} 													
																	}
																}
															}
															else
															{
																//System.out.println("meiyou setGoAheadOrStopFromDataList");
															}

															if ((StopExp) )
															{
																JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,"- -","- -",whyToStop,"show"});
																ExpPackageHistoryItem[0] = StepFileNameTmp;
																ExpPackageHistoryItem[1] = requestData;
																ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																previousData = responseData;
																ExpPackageHistoryItem[3] = responseData;
																ExpPackageHistoryItem[4] = whyToStop;
																ExpPackageHistoryList.add(ExpPackageHistoryItem);
																expHTTPHistoryList.add(ExpPackageHistoryItem);
																ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																ExpPackageHistoryItem = new String[7];
																responseStatus = 0;
																responseData = "";
																whyToStop = "Run Over";
																break;
															}
				                            
															if(GoAheadCurrent)
															{
																JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,"- -","- -",whyToStop,"show"});
																ExpPackageHistoryItem[0] = StepFileNameTmp;
																ExpPackageHistoryItem[1] = requestData;
																ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																previousData = responseData;
																ExpPackageHistoryItem[3] = responseData;
																ExpPackageHistoryItem[4] = whyToStop;
																ExpPackageHistoryList.add(ExpPackageHistoryItem);
																expHTTPHistoryList.add(ExpPackageHistoryItem);
																ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																ExpPackageHistoryItem = new String[7];
																responseStatus = 0;
																responseData = "";
																whyToStop = "Run Over";
																GoAheadCurrent = false;
																break;
															}
				                            
															if(GoAheadExp)
															{
																JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,"- -","- -",whyToStop,"show"});
																ExpPackageHistoryItem[0] = StepFileNameTmp;
																ExpPackageHistoryItem[1] = requestData;
																ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
																previousData = responseData;
																ExpPackageHistoryItem[3] = responseData;
																ExpPackageHistoryItem[4] = whyToStop;
																ExpPackageHistoryList.add(ExpPackageHistoryItem);
																expHTTPHistoryList.add(ExpPackageHistoryItem);
																ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
																ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
																ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
																ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
																ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
																ExpPackageHistoryItem = new String[7];
																responseStatus = 0;
																responseData = "";
																whyToStop = "Run Over";
																break;
															}

															JDPDetectionRightPanelTargetGoPanelShowPanelTableModel.addRow(new Object[]{ExpName,"- -","- -",whyToStop,"show"});
															ExpPackageHistoryItem[0] = StepFileNameTmp;
															ExpPackageHistoryItem[1] = requestData;
															ExpPackageHistoryItem[2] = String.valueOf(responseStatus);
															previousData = responseData;
															ExpPackageHistoryItem[3] = responseData;
															ExpPackageHistoryItem[4] = whyToStop;
															ExpPackageHistoryList.add(ExpPackageHistoryItem);
															expHTTPHistoryList.add(ExpPackageHistoryItem);
															ExpPackagePreviousItem[0] = ExpPackageHistoryItem[0];
															ExpPackagePreviousItem[1] = ExpPackageHistoryItem[1];
															ExpPackagePreviousItem[2] = ExpPackageHistoryItem[2];
															ExpPackagePreviousItem[3] = ExpPackageHistoryItem[3];
															ExpPackagePreviousItem[4] = ExpPackageHistoryItem[4];
															ExpPackageHistoryItem = new String[7];
															responseStatus = 0;
															responseData = "";
															whyToStop = "Run Over";
				                      														
														}                    
													}
													else
													{
														//System.out.println("lineTxtTmpLength  !===== 131311313");
													}                                   
												}
												else
												{
													//System.out.println("lineTxtTmpLength>>>>>>>>>>>..0");
												}
											} 
											if(GoAheadExp)
											{	
												GoAheadExp  = false;									
											}
										}					
									}
									else
									{
									//	System.out.println("找不到指定的文件");
									}
								}
								catch (Exception ee)
								{
									ee.printStackTrace();
								}								
												
						JDPDetectionRightPanelTargetGoPanelEditPanelButton.setEnabled(true);
					}
					
				});
					
				
			 testRunStartThread.start();
			 
			 Thread setFontThread = new Thread(new Runnable()
	    		{
	    			public void run()
	    			{
	    				JLabel labelTmp = new JLabel("Detection");
	    				labelTmp.setForeground(Color.red);
	    				JPtab.setTabComponentAt(2, labelTmp);
	    				try
	    				{
	    					Thread.sleep(3000);
	    				}
	    				catch (Exception e)
	    				{
	    					e.printStackTrace();
	    				}
	    				labelTmp.setForeground(null);
	    				JPtab.setTabComponentAt(2, labelTmp);
	    			}
	    		});
	    		setFontThread.start();
			 
			 
			 
			 
			 
			 
			 
			 
			 
			 
			 
			 
			 
			
		}
	});
	  
	  
	  
 	 JPtabEditorPanelReSetAllButton.addActionListener(new ActionListener() {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			
			
			
			int EditorStepCount = JPtabEditorPanel.getTabCount();
			for (int i = 0; i < EditorStepCount; i++)
			  {
				EditorStep EditorStepTmp = (EditorStep)JPtabEditorPanel.getComponentAt(i);
					EditorStepTmp.host = EditorStepTmp.resourceHost;
					EditorStepTmp.port = EditorStepTmp.resourcePort;
	
					EditorStepTmp.siteURLLabel.setText("Target: " + EditorStepTmp.host+":"+EditorStepTmp.port);
					
					EditorStepTmp.editorStepShowString = EditorStepTmp.resourceStepString;
					EditorStepTmp.EditorRequestDaraRawTextArea.setText(EditorStepTmp.resourceStepString);
					//EditorStepTmp.insertDocument(EditorStepTmp.resourceStepString, Color.black);
					
					EditorStepTmp.EditorStepReplaceHostPort.setLabel("CencleReplaceHostPort");
					EditorStepTmp.replaceHostPort = true;
					
					EditorStepTmp.EditorStepUsepreviousSetCookie.setLabel("CencleUsepreviousSetCookie");
					EditorStepTmp.useSetCookieFromPrevious = true;
					
					EditorStepTmp.randomStringList.clear();
					
					EditorStepTmp.captchaList.clear();
					
					
					
					EditorStepTmp.setGoAheadOrStopFromStatusList.clear();
					
					EditorStepTmp.setGoAheadOrStopFromDataList.clear();
					
					EditorStepTmp.GrobalFileInStep = -1;
					EditorStepTmp.FileForCycleWithEXP = "";
					EditorStepTmp.FileForCycleWithEXPStrings[0] = "";
					EditorStepTmp.FileForCycleWithEXPStrings[1] = "-1";
					EditorStepTmp.EditorStepUseFileForCycleWithExp.setLabel("UseFileForCycleWithExp");
					
					EditorStepTmp.useFileForCycleWithPackageItem = new String[6];
					
					
					
			
					
					
					
					
					
					
					
					
					
					

			  }
			
		}
	});
 	  
 	  final JDialog createExpEditDialog = new JDialog();
 	 createExpEditDialog.setAlwaysOnTop(true);
 	  createExpEditDialog.setSize(400,200);
 	  createExpEditDialog.setLocation(400, 160);
 	  createExpEditDialog.setLayout(null);
 	  
 	  JLabel createExpEditDialogExpLabel = new JLabel("ExpName :");
 	  createExpEditDialogExpLabel.setSize(60,30);
 	  createExpEditDialogExpLabel.setLocation(7, 7);
 	  createExpEditDialog.add(createExpEditDialogExpLabel);
 	  
 	  final JTextField createExpEditDialogExpText = new JTextField();
 	  createExpEditDialogExpText.setSize(200,25);
 	  createExpEditDialogExpText.setLocation(70, 10);
 	  createExpEditDialog.add(createExpEditDialogExpText);
 	  
 	  JButton createExpEditDialogOkButton = new JButton("OK");
 	  createExpEditDialogOkButton.setSize(70,25);
 	  createExpEditDialogOkButton.setLocation(290, 10);
 	 createExpEditDialogOkButton.setFocusable(false);
 	  createExpEditDialog.add(createExpEditDialogOkButton);
 	  
 	  JLabel createExpEditDialogAboutLabel = new JLabel("About :");
 	  createExpEditDialogAboutLabel.setSize(60,30);
 	  createExpEditDialogAboutLabel.setLocation(7, 40);
 	  createExpEditDialog.add(createExpEditDialogAboutLabel);
 	  
 	  final JTextArea createExpEditDialogAboutText = new JTextArea("About Information");
 	  createExpEditDialogAboutText.setLineWrap(true);
 	  JScrollPane createExpEditDialogAboutScroll = new JScrollPane(createExpEditDialogAboutText);
 	  createExpEditDialogAboutScroll.setSize(290,110);
 	  createExpEditDialogAboutScroll.setLocation(70, 40);
 	  createExpEditDialog.add(createExpEditDialogAboutScroll);
 	  
 	  createExpEditDialogOkButton.addActionListener(new ActionListener() {
		
 		  @Override
 		  public void actionPerformed(ActionEvent e) {
 			  // TODO Auto-generated method stub
 			  String ExpName = createExpEditDialogExpText.getText().trim();	
 			  String ExpAboutString = createExpEditDialogAboutText.getText().trim();
 			  ExpAboutString = ExpAboutString.replaceAll("\n", "\r\n");
 			//  System.out.println(ExpAboutString);
 			  
 			  
 			 if (ExpName == null||ExpName.length()==0)
			  {
				  JOptionPane.showMessageDialog(null, "请输入要保存的Exp名称", "Warning", 2);
			  }
			  else
			  {
				  //fuck
				  int EditorStepCount = JPtabEditorPanel.getTabCount();
				  String StepFileTitle = "#Step-IP/Host-Port-FileName-UserURLToSocketHostAndPort-UserPreviousSet-Cookie";
				  String ItemNama = "Exps/"+ExpName;

				  File ItemFileDir = new File(ItemNama);
				  
				  if (ItemFileDir.exists())
				  {
					  JOptionPane.showMessageDialog(null, "当前名称已经存在，请换个名称", "Warning", 2);
				  }
				  else
				  {
					  ItemFileDir.mkdir();
					  try
					  {
						  FileWriter fileWriter = new FileWriter(ItemFileDir + "/Step.txt", true);

						  for (int i = 0; i < EditorStepCount; i++)
						  {
							//  System.out.println("第一个");
							  EditorStep EditorStepTmp = (EditorStep)JPtabEditorPanel.getComponentAt(i);
							  
							  String ExpResourceRequestFileName = "Step"+i+"_Resource.txt";
							  
							  
							  Method.writeHexStringsToFile(ItemFileDir + "/" + ExpResourceRequestFileName, Method.bytesToHexStrings(EditorStepTmp.resourceStepString.getBytes()));
							  
							 
							  

							  String FileName = "Step" + i + ".txt";
							  fileWriter.write(FileName + "-");

							  fileWriter.write("host:" + Method.getBase64(EditorStepTmp.host) + "-");

							  fileWriter.write("port:" + Method.getBase64(String.valueOf(EditorStepTmp.port)) + "-");

							  fileWriter.write("stepName:" + Method.getBase64(FileName) + "-");

							  if (EditorStepTmp.replaceHostPort)
							  {
								  fileWriter.write("replaceHostPort:true-");
							  }
							  else
							  {
								  fileWriter.write("replaceHostPort:false-");
							  }

							 // System.out.println("EditorStepTmpEditorStepTmp::" + EditorStepTmp.useSetCookieFromPrevious);
							  if (EditorStepTmp.useSetCookieFromPrevious)
							  {
								  fileWriter.write("useSetCookieFromPrevious:true-");
							  }
							  else
							  {
								  fileWriter.write("useSetCookieFromPrevious:false-");
							  }

							  fileWriter.write("randomString:");
							  int randomStringListCount = EditorStepTmp.randomStringList.size();
							  for (int q = 0; q < randomStringListCount; q++)
							  {
								  String[] tmp = new String[5];
								  String StrTmp = "";
								  tmp = (String[])EditorStepTmp.randomStringList.get(q);
								  StrTmp = tmp[0] + "," + tmp[1] + "," + tmp[2] + tmp[3]+","+tmp[4];
								  fileWriter.write(Method.getBase64(StrTmp));
								  fileWriter.write(",");
							  }
							  fileWriter.write("-");

							  fileWriter.write("captcha:");
							  int captchaListCount = EditorStepTmp.captchaList.size();
							  for (int q = 0; q < captchaListCount; q++)
							  {
								  String[] tmp = new String[4];
								  String StrTmp = "";
								  tmp = (String[])EditorStepTmp.captchaList.get(q);
								  StrTmp = tmp[0] + "," + tmp[1] + "," + tmp[2]+","+tmp[3];
								  fileWriter.write(Method.getBase64(StrTmp));
								  fileWriter.write(",");
							  }
							  fileWriter.write("-");
							  
							  fileWriter.write("setParamsFromDataPackage:");
							  int setParamsFromDataPackageListCount = EditorStepTmp.setParamsFromDataPackageList.size();
							  for (int q = 0; q < 0; q++)
							  {
								  String[] tmp = new String[4];
								  String StrTmp = "";
								  tmp = (String[])EditorStepTmp.setParamsFromDataPackageList.get(q);
								  StrTmp = tmp[0] + "," + tmp[1] + "," + tmp[2];
								  fileWriter.write(Method.getBase64(StrTmp));
								  fileWriter.write(",");
							  }
							  fileWriter.write("-");

							  fileWriter.write("setGoAheadOrStopFromStatus:");
							  int setGoAheadOrStopFromStatusListCount = EditorStepTmp.setGoAheadOrStopFromStatusList.size();
							  for (int q = 0; q < setGoAheadOrStopFromStatusListCount; q++)
							  {
								  String[] tmp = new String[4];
								  String StrTmp = "";
								  tmp = (String[])EditorStepTmp.setGoAheadOrStopFromStatusList.get(q);
								  StrTmp = tmp[0] + "," + tmp[1];
								  fileWriter.write(Method.getBase64(StrTmp));
								  fileWriter.write(",");
							  }
							  fileWriter.write("-");

							  fileWriter.write("setGoAheadOrStopFromData:");
							  int setGoAheadOrStopFromDataListCount = EditorStepTmp.setGoAheadOrStopFromDataList.size();
							  for (int q = 0; q < setGoAheadOrStopFromDataListCount; q++)
							  {
								  String[] tmp = new String[4];
								  String StrTmp = "";
								  tmp = (String[])EditorStepTmp.setGoAheadOrStopFromDataList.get(q);
								  StrTmp = Method.getBase64(tmp[0]) + "," + tmp[1];
								  fileWriter.write(Method.getBase64(StrTmp));
								  fileWriter.write(",");
							  }
							  fileWriter.write("-");

							  fileWriter.write("useFileForCycleWithPackage:");
							  String[] useFileForCycleWithPackageItemStrings = EditorStepTmp.useFileForCycleWithPackageItem;
							  try
							  {
								  int useFileForCycleWithPackageItemFileNums = Integer.valueOf(useFileForCycleWithPackageItemStrings[0]);
								  if (useFileForCycleWithPackageItemFileNums == 1)
								  {
									  String StrTmp = "";
									  File tmp = new File(useFileForCycleWithPackageItemStrings[1].trim());
									  StrTmp = useFileForCycleWithPackageItemStrings[0] + "," + Method.getBase64(tmp.getName()) + "," + ","+","+Method.getBase64(useFileForCycleWithPackageItemStrings[4])+",";
									  fileWriter.write(Method.getBase64(StrTmp));
									  fileWriter.write(",");
									  

									  Method.copyFile(useFileForCycleWithPackageItemStrings[1].trim(), ItemNama + "/" + tmp.getName());
								  }
								  else if (useFileForCycleWithPackageItemFileNums == 2)
								  {
									  String StrTmp = "";
									  File tmp1 = new File(useFileForCycleWithPackageItemStrings[1].trim());
									  File tmp2 = new File(useFileForCycleWithPackageItemStrings[2].trim());
									//  System.out.println(useFileForCycleWithPackageItemStrings[0]);
									//  System.out.println(useFileForCycleWithPackageItemStrings[1]);
									//  System.out.println(useFileForCycleWithPackageItemStrings[2]);
									//  System.out.println(useFileForCycleWithPackageItemStrings[3]);
									  StrTmp = useFileForCycleWithPackageItemStrings[0] + "," + Method.getBase64(tmp1.getName()) + "," + Method.getBase64(tmp2.getName()) + "," + useFileForCycleWithPackageItemStrings[3]+","+Method.getBase64(useFileForCycleWithPackageItemStrings[4])+","+Method.getBase64(useFileForCycleWithPackageItemStrings[5]);
								//	  System.out.println(StrTmp);
									//  System.out.println(Method.getBase64(StrTmp));

									  fileWriter.write(Method.getBase64(StrTmp));
									  fileWriter.write(",");
									 

									//  System.out.println(tmp1.getName());
									//  System.out.println(tmp2.getName());
									  Method.copyFile(useFileForCycleWithPackageItemStrings[1].trim(), ItemNama + "/" + tmp1.getName());
									  Method.copyFile(useFileForCycleWithPackageItemStrings[2].trim(), ItemNama + "/" + tmp2.getName());
								  }
								  
							  }
							  catch (Exception localException1)
							  {
							  }

							  fileWriter.write("-");

							  fileWriter.write("fileForCycleWithEXP:");
							  String fileForCycleWithEXPName = EditorStepTmp.FileForCycleWithEXP;
							  String fileForCycleWithExpInStep = EditorStepTmp.FileForCycleWithEXPStrings[1];
							  String fileForCycleWithExpResource =  EditorStepTmp.FileForCycleWithEXPStrings[2];
							  if (fileForCycleWithEXPName.length() > 0)
							  {
								  File tmp = new File(fileForCycleWithEXPName.trim());
								  fileWriter.write(Method.getBase64(tmp.getName()+","+fileForCycleWithExpInStep+","+fileForCycleWithExpResource));
								  
								  
								  Method.copyFile(fileForCycleWithEXPName.trim(), ItemNama + "/" + tmp.getName());
							  }

							  String requestData = EditorStepTmp.editorStepRequestData;

							  fileWriter.write("\r\n");
							  
							  Method.writeHexStringsToFile(ItemFileDir + "/" + FileName, Method.bytesToHexStrings(requestData.getBytes()));
						  }

						  fileWriter.flush();
						  fileWriter.close();
						  
						  
						  FileWriter AboutfileWriter = new FileWriter(ItemFileDir + "/About.txt", true);
						  AboutfileWriter.write(ExpAboutString);
						  AboutfileWriter.flush();
						  AboutfileWriter.close();
						  
						  
						  
						  
						  createExpEditDialog.setVisible(false);
					  }
					  catch (Exception re)
					  {
						  re.printStackTrace();
						  JOptionPane.showMessageDialog(null, "请输入正确的文件名", "Warning", 2);
					  }
				  }
			  }
 			 
 			
 			  
 		  }
 	  });
 	  
 	  JPtabEditorPanelCreateButton.addActionListener(new ActionListener()
 	  {
 		  public void actionPerformed(ActionEvent e)
 		  {			  
 			 createExpEditDialogAboutText.setText("");
 			createExpEditDialogExpText.setText("");
 			  createExpEditDialog.setVisible(true);
			  
 		  }
 	  });
 	  JPtabEditorPanel = new JTabbedPane(1);
 	  JPtabEditorPanel.setFocusable(false);

 	  GridBagConstraints GBCJPEP = new GridBagConstraints();
 	  GBCJPEP.fill = 1;
 	  GBCJPEP.insets = new Insets(5, 5, 5, 5);
 	  GBCJPEP.gridx = 0;
 	  GBCJPEP.gridy = 4;
 	  GBCJPEP.gridheight = 1;
 	  GBCJPEP.gridwidth = 16;
 	  GBCJPEP.weightx = 1.0D;
 	  GBCJPEP.weighty = 0.0D;
 	  JPtabEditorPanel.setForeground(new Color(Integer.decode("#4B4B4B").intValue()));

 	  JDPEditor.add(JPtabEditorPanel, "Center");

 	  JPtabEditorPanelAddButton.addActionListener(new ActionListener()
 	  {
 		  public void actionPerformed(ActionEvent e)
 		  {
 			  //String tmp = "GET /img/bd_logo1.png HTTP/1.1\r\nHost: www.baidu.com\r\nProxy-Connection: keep-alive\r\nCache-Control: max-age=0\r\nAccept: image/webp,*/*;q=0.8\r\nIf-None-Match: \"1ec5-502264e2ae4c0\"\r\nIf-Modified-Since: Wed, 03 Sep 2014 10:00:27 GMT\r\nUser-Agent: Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.131 Safari/537.36\r\nReferer: http://www.baidu.com/\r\n\r\n";

 			  AddTabPanel(JPtabEditorPanel);
 		  }
 	  });
	}

	

	public static void InterceptShowRawHeadersHexUpdate()
	{
		//System.out.println("Update");
		String Str = InterceptShowString;

		if ((JPtabInterceptDataShowHeadersTable != null) && (Str.length() != 0))
		{
			DefaultTableModel tableModel = (DefaultTableModel)JPtabInterceptDataShowHeadersTable.getModel();
			tableModel.setRowCount(0);
			String[] headerParams = Method.getHTTPHeadersParams(Str);
			int headerParamLength = headerParams.length / 2;
			for (int i = 0; i < headerParamLength; i++)
			{
				tableModel.addRow(new Object[] { headerParams[(i * 2)].trim(), headerParams[(i * 2 + 1)].trim() });
			}
			JPtabInterceptDataShowHeadersTable.invalidate();
		}

		if ((Str != null) && (!Str.equals(null)) && (Str.length() != 0))
		{
			String[] HttpResponseDataHexStrings = Method.bytesToHexStrings(Str.getBytes());
			int HttpResponseDataHexStringsLength = HttpResponseDataHexStrings.length;
			int HttpResponseDataHexStringsRowNum = HttpResponseDataHexStringsLength / 16;
			int HttpResponseDataHexStringsRealRowNum = 0;
			if (HttpResponseDataHexStringsLength % 16 == 0)
			{
				HttpResponseDataHexStringsRealRowNum = HttpResponseDataHexStringsRowNum;
			}
			else
			{
				HttpResponseDataHexStringsRealRowNum = HttpResponseDataHexStringsRowNum + 1;
			}

			String[] HttpResponseDataHexStringsRealRowStrings = new String[HttpResponseDataHexStringsRealRowNum];
			String HttpResponseDataHexStringsRealRowStringTmp = "";
			char[] ResponseStringchars = Str.toCharArray();
			int ResponseStringcharsLength = ResponseStringchars.length;
			int i = 1; for (int j = 0; (i <= ResponseStringcharsLength) && (j < HttpResponseDataHexStringsRealRowNum); i++)
			{
				HttpResponseDataHexStringsRealRowStringTmp = HttpResponseDataHexStringsRealRowStringTmp + ResponseStringchars[(i - 1)];
				if (i % 16 == 0)
				{
					HttpResponseDataHexStringsRealRowStrings[j] = HttpResponseDataHexStringsRealRowStringTmp;
					HttpResponseDataHexStringsRealRowStringTmp = "";
					j++;
				}
			}

			HttpResponseDataHexStringsRealRowStrings[(HttpResponseDataHexStringsRealRowNum - 1)] = HttpResponseDataHexStringsRealRowStringTmp;

			DefaultTableModel JPtabProxyHistoryDetailResponseHexTableModel = (DefaultTableModel)JPtabProxyInterceptDataHexTable.getModel();
			JPtabProxyHistoryDetailResponseHexTableModel.setRowCount(0);

			for (int k = 0; k < HttpResponseDataHexStringsRowNum; k++)
			{
				JPtabProxyHistoryDetailResponseHexTableModel.addRow(new Object[] { Integer.valueOf(k + 1), HttpResponseDataHexStrings[(k * 16)], HttpResponseDataHexStrings[(k * 16 + 1)], HttpResponseDataHexStrings[(k * 16 + 2)], HttpResponseDataHexStrings[(k * 16 + 3)], HttpResponseDataHexStrings[(k * 16 + 4)], HttpResponseDataHexStrings[(k * 16 + 5)], HttpResponseDataHexStrings[(k * 16 + 6)], HttpResponseDataHexStrings[(k * 16 + 7)], HttpResponseDataHexStrings[(k * 16 + 8)], HttpResponseDataHexStrings[(k * 16 + 9)], HttpResponseDataHexStrings[(k * 16 + 10)], HttpResponseDataHexStrings[(k * 16 + 11)], HttpResponseDataHexStrings[(k * 16 + 12)], HttpResponseDataHexStrings[(k * 16 + 13)], HttpResponseDataHexStrings[(k * 16 + 14)], HttpResponseDataHexStrings[(k * 16 + 15)], HttpResponseDataHexStringsRealRowStrings[k] });
			}

			String[] ResponseHexStringsTmp = { String.valueOf(HttpResponseDataHexStringsRealRowNum), "--", "--", "--", "--", "--", "--", "--", "--", "--", "--", "--", "--", "--", "--", "--", "--", HttpResponseDataHexStringsRealRowStrings[(HttpResponseDataHexStringsRealRowNum - 1)] };
			int HttpResponseDataHexStringsSHENGYUGESHU = HttpResponseDataHexStringsLength % 16;
			for (int k = 0; k < HttpResponseDataHexStringsSHENGYUGESHU; k++)
			{
				ResponseHexStringsTmp[(k + 1)] = HttpResponseDataHexStrings[(HttpResponseDataHexStringsRowNum * 16 + k)];
			}
			if (HttpResponseDataHexStringsLength % 16 != 0)
			{
				JPtabProxyHistoryDetailResponseHexTableModel.addRow(ResponseHexStringsTmp);
				JPtabProxyInterceptDataHexTable.invalidate();
			}
		}
	}

	

	public static String[] printHexString(byte[] b)
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

	

	

	

	
	//增加 Tab Step 步骤   不带任何参数的   Editor Add Step
	private static void AddTabPanel(final JTabbedPane JTabbedPaneTmp)
	{
		
		JPanel TitlePanel = new JPanel();//用来设置标题 和设置带有关闭的 x label
		TitlePanel.setOpaque(false);//设置控件透明
		JLabel stepLabel = new JLabel();//Step 的Title 的标签  为 step0  step1 。。
		final JLabel closeLabel = new JLabel(" x ");//Step 的Title 的 关闭标签
		stepLabel.setHorizontalAlignment(2);//
		closeLabel.setHorizontalAlignment(4);//

		TitlePanel.add(stepLabel);// 添加标签到面板上
		TitlePanel.add(closeLabel);//添加标签到面板上
		final EditorStep EditorStepTmp = new EditorStep(JTabbedPaneTmp);//增加一个 步骤 Step  EditorStep 为 JDesktopPane  传入对象 是为了能控制  各个step 的详细数据
		JTabbedPaneTmp.add(EditorStepTmp, "test");//初始化   test 名字在后面会被替换了

		int RunCount = JTabbedPaneTmp.getTabRunCount();//
		int TotalCount = JTabbedPaneTmp.getTabCount();//
		stepLabel.setText("Step" + (EditorStepNum ));//设置Step 的名称  step0  step1 。。
		EditorStepNum += 1; //用来累计 step 的计数器
		//System.out.println("RUN:" + RunCount + "--" + "Total:" + TotalCount);
		JTabbedPaneTmp.setTabComponentAt(TotalCount - 1, TitlePanel);//设置step 添加之后的位置。  设置添加到最后
		JTabbedPaneTmp.setTitleAt(TotalCount - 1, "Step" + (EditorStepNum + 1));//这步还是修改标题
		JTabbedPaneTmp.setSelectedIndex(TotalCount - 1);//增加之后 就默认选中
	//	System.out.println(JTabbedPaneTmp.indexOfComponent(EditorStepTmp));
		closeLabel.addMouseListener(new MouseListener()//关闭标签的动作
		{
			public void mouseReleased(MouseEvent e)
			{
			}

			public void mousePressed(MouseEvent e)
			{
			}

			public void mouseExited(MouseEvent e)//设置 移进移出的不同状态
			{
				if (JTabbedPaneTmp.indexOfComponent(EditorStepTmp) == JTabbedPaneTmp.getSelectedIndex())//只有在当前选中的tab中操作，才可以触发
					closeLabel.setText(" x ");
			}

			public void mouseEntered(MouseEvent e)
			{
				if (JTabbedPaneTmp.indexOfComponent(EditorStepTmp) == JTabbedPaneTmp.getSelectedIndex())//只有在当前选中的tab中操作，才可以触发
					closeLabel.setText("|x|");
			}

			public void mouseClicked(MouseEvent e)//点击之后的动作
			{
				if (JTabbedPaneTmp.indexOfComponent(EditorStepTmp) == JTabbedPaneTmp.getSelectedIndex())//只有在当前选中的tab中操作，才可以触发
				{
					EditorStepTmp.GrobalFileInStep = -1;//  这个变量是类的静态变量    用来设置 cycleforexp 设定在哪个步骤了
					EditorStepTmp.DataFile1CycleInOnePackage = -1;//这是设置 cycleforpackage
					EditorStepTmp.DataFile2CycleInOnePackage = -1;//同上                            其实这两个可以要可以不要
					EditorStepTmp.FileForCycleWithEXP = "";// 这个变量是类的静态变量    用来设置 cycleforexp 设定在哪个步骤了  设置文件名
					JTabbedPaneTmp.remove(JTabbedPaneTmp.indexOfComponent(EditorStepTmp));/// 移除当前tab
				}
			}
		});
	}
	
	//增加 Tab Step 步骤   带参数的   Editor Step   可以从 send to editor  
	private static void AddTabPanel(final JTabbedPane JTabbedPaneTmp, String host, int port, String StrBuffer)
	{
		JPanel TitlePanel = new JPanel();
		TitlePanel.setOpaque(false);
		JLabel stepLabel = new JLabel();
		final JLabel closeLabel = new JLabel(" x ");
		stepLabel.setHorizontalAlignment(2);
		closeLabel.setHorizontalAlignment(4);

		TitlePanel.add(stepLabel);
		TitlePanel.add(closeLabel);
		int RunCount = JTabbedPaneTmp.getTabRunCount();
		int TotalCount = JTabbedPaneTmp.getTabCount();
		stepLabel.setText("Step" + EditorStepNum);
		EditorStepNum += 1;
		final EditorStep EditorStepTmp = new EditorStep(JTabbedPaneTmp, host, port, StrBuffer);//只有这步不一样，其他的都完全一样  加入host port httpreuest 参数
		JTabbedPaneTmp.add(EditorStepTmp, "Step" + EditorStepNum);

		JTabbedPaneTmp.setTabComponentAt(TotalCount, TitlePanel);
		JTabbedPaneTmp.setSelectedIndex(TotalCount);

		closeLabel.addMouseListener(new MouseListener()
		{
			public void mouseReleased(MouseEvent e)
			{
			}

			public void mousePressed(MouseEvent e)
			{
			}

			public void mouseExited(MouseEvent e)
			{
				if (JTabbedPaneTmp.indexOfComponent(EditorStepTmp) == JTabbedPaneTmp.getSelectedIndex())
					closeLabel.setText(" x ");
			}

			public void mouseEntered(MouseEvent e)
			{
				if (JTabbedPaneTmp.indexOfComponent(EditorStepTmp) == JTabbedPaneTmp.getSelectedIndex())
					closeLabel.setText("|x|");
			}

			public void mouseClicked(MouseEvent e)
			{
				if (JTabbedPaneTmp.indexOfComponent(EditorStepTmp) == JTabbedPaneTmp.getSelectedIndex())
					JTabbedPaneTmp.remove(JTabbedPaneTmp.indexOfComponent(EditorStepTmp));
			}
		});
	}
}

//步骤的 类   step  step0 step1。。

class EditorStep extends JDesktopPane
{
	public String resourceStepString="";//这个用来存储  send to editor 过来的 源 数据包   以便reset
	public String resourceHost = ""; //这个存储  源 ip  以便reset
	public int resourcePort;//这个存储源 port 以便reset
	
	
	public String editorStepShowString = ""; //request 这个就是 raw header hex 的数据中心
	public String host = "";// 当前包的 host
	public int port;//当前包的  port
	public String editorStepRequestData = "";//这个是 raw 的  http request

	public static int DataFile1CycleInOnePackage = -1;//cycleforpackage  1
	public static int GrobalFileInStep = -1;//cycleforexp FileForCycleWithEXPStrings
	public static int DataFile2CycleInOnePackage = -1;//cycleforpackage2

	public boolean replaceHostPort = true;//替换成目标  默认替换
	public boolean useSetCookieFromPrevious = true;//使用前一个数据包返回的 set-cookie  默认使用

 	public List<String[]> randomStringList = new ArrayList();//存储随机字符串的 list
 	public String[] randomStringItem = new String[5];//设置随机字符串的 item
  
 	public List<String[]> captchaList = new ArrayList();//存储验证码的list
 	public String[] captchaItem = new String[4];//设置验证码的item

 	public List<String[]> setParamsFromDataPackageList = new ArrayList();//使用第几个step的数据的list
 	public String[] setParamsFromDataPackageItem = new String[6];//使用第几个step的数据的item

 	public List<String[]> setGoAheadOrStopFromStatusList = new ArrayList();//设置go ahead or stop 的规则的list   通过response status
 	public String[] setGoAheadOrStopFromStatusItem = new String[2];//设置 go ahead or stop 的item

 	public List<String[]> setGoAheadOrStopFromDataList = new ArrayList();//设置go ahead or stop 的规则的list   通过response  data
 	public String[] setGoAheadOrStopFromDataItem = new String[2];//设置 go ahead or stop 的item

 	public List<String[]> useFileForCycleWithPackageList = new ArrayList();// 这个list  可以用可以不用， 因为最多只有一个
 	public String[] useFileForCycleWithPackageItem = new String[6];//
 	
  
 	public static String FileForCycleWithEXP = "";//存储cycleforexp 的文件名
 	public static String[] FileForCycleWithEXPStrings = new String[3];
 	//0、存储文件名
 	//1、存储cycle 设置的字符串在step
 	//2、存储源字符串
 	
 	public JTextArea EditorResponseDaraRawTextArea;//Editor Step Response raw 的 text
 	public JTable JPtabEditorResponseHeadersTable;//Editor Step Response Header table
 	public JTable JPtabEditorResponseHexTable;//Editor Step response hex table
 	public String responseDataString = ""; //这个就是 raw header hex 的数据中心  response      response 的data 不能改变 这个也没什么用途  就是用来显示的
 	public static JTabbedPane JTabbedPaneTmp;//   这个是 增加 step 的 tab 对象
 	
 	public String currentExpName="";
 	
 	//reset
 	public JTextPane EditorRequestDaraRawTextArea;//Editor Step request raw 的 text
 	public MenuItem EditorStepReplaceHostPort;//  editor 编辑框的右键菜单的   replacehostport
 	public MenuItem EditorStepUsepreviousSetCookie;//editor 编辑框的 右键才对那的  replacesetcookie
 	public MenuItem EditorStepUseFileForCycleWithExp;//  editor 编辑框的 右键菜单的  使用 cycleforexp
 	
 	public JTable JPtabEditorSetGoAheadOrStopFromStatusTable;//editor 右键菜单中 设置 setgoaheadorstopfromstatu 的table表格
 	public JTable JPtabEditorSetGoAheadOrStopFromDataTable;//editor 右键菜单中设置 setgoaheadorstopfromdata 的table 表格
 	
 	
 	public JLabel siteURLLabel;

 	public EditorStep(JTabbedPane JTabbedPaneTmp, String host, int port, String tmp)//构造函数， 带参数的  
 	{
 		//存储各个相关参数
 		this.editorStepShowString = tmp; 
 		this.host = host;
 		this.port = port;
 		this.editorStepRequestData = this.editorStepShowString;
 		this.JTabbedPaneTmp = JTabbedPaneTmp;
 		
 		this.resourceStepString = tmp;
 		this.resourceHost = host;
 		this.resourcePort = port;
 		init();
 	}

 	public EditorStep(JTabbedPane JTabbedPaneTmp) 
 	{
 		this.JTabbedPaneTmp = JTabbedPaneTmp;
 		init();
 	}

 	public void init()
 	{
 		setLayout(new BorderLayout());//布局
 		JPanel ButtonPanel = new JPanel();//按钮面板用来承载  go cencle 。。。 detail 按钮

 		ButtonPanel.setPreferredSize(new Dimension(0, 60));//设置大小
 		ButtonPanel.setLayout(null);//布局
 		add(ButtonPanel, "North");

 		final JButton GOButton = new JButton("GO");//editor 的go 按钮
 		GOButton.setEnabled(true);//
 		GOButton.setSize(new Dimension(80, 28));//大小
 		GOButton.setFocusable(false);//去除小方框
 		GOButton.setLocation(27, 7);//位置
 		ButtonPanel.add(GOButton);

 		final JButton CencleButton = new JButton("Cencle");
 		CencleButton.setEnabled(false);
 		CencleButton.setSize(new Dimension(80, 28));
 		CencleButton.setFocusable(false);
 		CencleButton.setLocation(134, 7);
 		ButtonPanel.add(CencleButton);

 		
 		
 		
 		siteURLLabel = new JLabel("Target: " + this.host+":"+this.port);
 		siteURLLabel.setSize(200, 30);
 		siteURLLabel.setLocation(290, 7);
 		
 		JButton siteEditButton = new JButton("...");
 		siteEditButton.setFocusable(false);
 		siteEditButton.setSize(new Dimension(30, 30));
 		siteEditButton.setLocation(240, 7);
 		ButtonPanel.add(siteURLLabel);
 		ButtonPanel.add(siteEditButton);

 		
 		JButton stepDetail = new JButton("Detail");
 		stepDetail.setFocusable(false);
 		stepDetail.setSize(100,30);
 		stepDetail.setLocation(1000,7);
 		ButtonPanel.add(stepDetail);
 		
 		
 		final JDialog stepDetailDialog = new JDialog();
 		stepDetailDialog.setSize(new Dimension(800,400));
 		stepDetailDialog.setLocation(500,200);
 		stepDetailDialog.setLayout(null);
 		stepDetailDialog.setAlwaysOnTop(true);
 		
 		JLabel stepDetailReplaceHostPortLabel = new JLabel("ReplaceHostPort: ");
 		stepDetailReplaceHostPortLabel.setLocation(7, 7);
 		stepDetailReplaceHostPortLabel.setSize(110,30);
 		stepDetailDialog.add(stepDetailReplaceHostPortLabel);
 		
 		final JRadioButton stepDetailReplaceHostPortRadioTrue = new JRadioButton("True");
 		stepDetailReplaceHostPortRadioTrue.setFocusable(false);
 		stepDetailReplaceHostPortRadioTrue.setSize(70,30);
 		stepDetailReplaceHostPortRadioTrue.setLocation(150, 7);
 		stepDetailDialog.add(stepDetailReplaceHostPortRadioTrue);
 		
 		stepDetailReplaceHostPortRadioTrue.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				replaceHostPort = true;
				EditorStepReplaceHostPort.setLabel("CencleReplaceHostPort");
			}
		});
 		
 		final JRadioButton stepDetailReplaceHostPortRadioFalse = new JRadioButton("False");
 		stepDetailReplaceHostPortRadioFalse.setFocusable(false);
 		stepDetailReplaceHostPortRadioFalse.setSize(70,30);
 		stepDetailReplaceHostPortRadioFalse.setLocation(220, 7);
 		stepDetailDialog.add(stepDetailReplaceHostPortRadioFalse);
 		
 		stepDetailReplaceHostPortRadioFalse.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				replaceHostPort = false;
				EditorStepReplaceHostPort.setLabel("ReplaceHostPort");
			}
		});
 		
 		ButtonGroup stepDetailReplaceHostPortRadioGroup = new ButtonGroup();
 		stepDetailReplaceHostPortRadioGroup.add(stepDetailReplaceHostPortRadioFalse);
 		stepDetailReplaceHostPortRadioGroup.add(stepDetailReplaceHostPortRadioTrue);
 		
 		
 		JLabel stepDetailUsePreviousSetCookieLabel = new JLabel("UsePreviousSetCookie: ");
 		stepDetailUsePreviousSetCookieLabel.setSize(150,30);
 		stepDetailUsePreviousSetCookieLabel.setLocation(7, 40);;
 		stepDetailDialog.add(stepDetailUsePreviousSetCookieLabel);
 		
 		
 		final JRadioButton stepDetailUsePreviousSetCookieRadioTrue = new JRadioButton("True");
 		stepDetailUsePreviousSetCookieRadioTrue.setFocusable(false);
 		stepDetailUsePreviousSetCookieRadioTrue.setSize(70,30);
 		stepDetailUsePreviousSetCookieRadioTrue.setLocation(150, 40);
 		stepDetailDialog.add(stepDetailUsePreviousSetCookieRadioTrue);
 		
 		stepDetailUsePreviousSetCookieRadioTrue.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				useSetCookieFromPrevious = true;
				EditorStepUsepreviousSetCookie.setLabel("CencleUsepreviousSetCookie");
			}
		});
 		
 		
 		final JRadioButton stepDetailUsePreviousSetCookieRadioFalse = new JRadioButton("False");
 		stepDetailUsePreviousSetCookieRadioFalse.setFocusable(false);
 		stepDetailUsePreviousSetCookieRadioFalse.setSize(70,30);
 		stepDetailUsePreviousSetCookieRadioFalse.setLocation(220, 40);
 		stepDetailDialog.add(stepDetailUsePreviousSetCookieRadioFalse);
 		
 		stepDetailUsePreviousSetCookieRadioFalse.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				useSetCookieFromPrevious = false;
				EditorStepUsepreviousSetCookie.setLabel("UsepreviousSetCookie");
			}
		});
 		
 		ButtonGroup stepDetailUsePreviousSetCookieRadioGroup = new ButtonGroup();
 		stepDetailUsePreviousSetCookieRadioGroup.add(stepDetailUsePreviousSetCookieRadioFalse);
 		stepDetailUsePreviousSetCookieRadioGroup.add(stepDetailUsePreviousSetCookieRadioTrue);
 		
 		
 		JLabel stepDetailReplaceRandomStringLabel = new JLabel("RandomString: ");
 		stepDetailReplaceRandomStringLabel.setForeground(Color.red);
 		stepDetailReplaceRandomStringLabel.setSize(120,30);
 		stepDetailReplaceRandomStringLabel.setLocation(7,70);
 		stepDetailDialog.add(stepDetailReplaceRandomStringLabel);
 		
 		final JComboBox stepDetailRandomStringComboBox = new JComboBox();
 		stepDetailRandomStringComboBox.setEnabled(false);
 		stepDetailRandomStringComboBox.setSize(50,20);
 		stepDetailRandomStringComboBox.setLocation(160, 75);
 		stepDetailDialog.add(stepDetailRandomStringComboBox);
 		
 		
 		
 		JLabel stepDetailRandomStringItemDigitsLabel = new JLabel("Digits: ");
 		stepDetailRandomStringItemDigitsLabel.setSize(70,30);
 		stepDetailRandomStringItemDigitsLabel.setLocation(220, 70);
 		stepDetailDialog.add(stepDetailRandomStringItemDigitsLabel);
 		
 		final JTextField stepDetailRandomStringItemDigitsText = new JTextField();
 		stepDetailRandomStringItemDigitsText.setEnabled(false);
 		stepDetailRandomStringItemDigitsText.setSize(40,23);
 		stepDetailRandomStringItemDigitsText.setLocation(270,75);
 		stepDetailDialog.add(stepDetailRandomStringItemDigitsText);
 		
 		
 		
 		JLabel stepDetailRandomStringItemtypeLabel = new JLabel("Type: ");
 		stepDetailRandomStringItemtypeLabel.setSize(60,30);
 		stepDetailRandomStringItemtypeLabel.setLocation(320, 70);
 		stepDetailDialog.add(stepDetailRandomStringItemtypeLabel);
 		
 		String[] randomStringType = {"Numbers","Strings","NumStrs","Define"};
 		final JComboBox stepDetailRandomStringItemTypeComboBox = new JComboBox(randomStringType);
 		stepDetailRandomStringItemTypeComboBox.setEnabled(false);
 		stepDetailRandomStringItemTypeComboBox.setSize(100,20);
 		stepDetailRandomStringItemTypeComboBox.setLocation(370, 75);
 		stepDetailDialog.add(stepDetailRandomStringItemTypeComboBox);
 		
 		final JTextField stepDetailRandomStringItemType3Text = new JTextField();
 		stepDetailRandomStringItemType3Text.setVisible(false);
 		stepDetailRandomStringItemType3Text.setSize(100,20);
 		stepDetailRandomStringItemType3Text.setLocation(480, 75);
 		stepDetailDialog.add(stepDetailRandomStringItemType3Text);
 		
 		final JLabel stepDetailRandomStringItemResplaceName = new JLabel("");
 		stepDetailRandomStringItemResplaceName.setSize(70,30);
 		stepDetailRandomStringItemResplaceName.setLocation(600,70);
 		stepDetailDialog.add(stepDetailRandomStringItemResplaceName);
 		
 		final JButton stepDetailRandomStringItemDeleteButton = new JButton("Delete");
 		stepDetailRandomStringItemDeleteButton.setEnabled(false);
 		stepDetailRandomStringItemDeleteButton.setFocusable(false);
 		stepDetailRandomStringItemDeleteButton.setSize(100,25);
 		stepDetailRandomStringItemDeleteButton.setLocation(670, 70);
 		stepDetailDialog.add(stepDetailRandomStringItemDeleteButton);
 		
 		
 		stepDetailRandomStringItemDigitsText.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
			//	System.out.println("1..");
				
				
				if(stepDetailRandomStringComboBox.getSelectedIndex()>=0)
					randomStringList.get(stepDetailRandomStringComboBox.getSelectedIndex())[2]=stepDetailRandomStringItemDigitsText.getText().trim();
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
			//	System.out.println("2..");
				
				
				if(stepDetailRandomStringComboBox.getSelectedIndex()>=0)
					randomStringList.get(stepDetailRandomStringComboBox.getSelectedIndex())[2]=stepDetailRandomStringItemDigitsText.getText().trim();
				
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				//System.out.println("3..");
			}
		});
 		
 		
 		stepDetailRandomStringComboBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				try
				{
			//	System.out.println("--");
			//	System.out.println();
				int index = stepDetailRandomStringComboBox.getSelectedIndex();
				if(index>=0)
				{
					String[] tmp =randomStringList.get(index);
					
					stepDetailRandomStringItemResplaceName.setText(Method.getBase64("rSI"+tmp[0]));
					stepDetailRandomStringItemTypeComboBox.setSelectedIndex(Integer.valueOf(tmp[1]));
					stepDetailRandomStringItemDigitsText.setText(tmp[2]);
				//	System.out.println();
					if(tmp[1].equals("3"))
					{
						//System.out.println(tmp[3]+"//////////////////////////////////////");
						stepDetailRandomStringItemType3Text.setText(tmp[3]);
						stepDetailRandomStringItemType3Text.setVisible(true);
					}
					else
					{
						stepDetailRandomStringItemType3Text.setVisible(false);
					}
					
					String StringTmp = EditorRequestDaraRawTextArea.getText();
				//	System.out.println(StringTmp+"StringTmpStringTmpStringTmpStringTmpStringTmpStringTmpStringTmpStringTmpStringTmp");
					EditorRequestDaraRawTextArea.setText("");
					String[] StringsTmp = StringTmp.split(Method.getBase64("rSI"+tmp[0]));
					int count = StringsTmp.length;
					insertDocument(StringsTmp[0], Color.black);
					for(int i=1;i<count;i++)
					{
						insertDocument(Method.getBase64("rSI"+tmp[0]), Color.red);
						insertDocument(StringsTmp[i], Color.black);
					}
				}
				else
				{
					stepDetailRandomStringItemResplaceName.setText("");
					stepDetailRandomStringItemDigitsText.setText("");
					
				}
				}
				catch(Exception es)
				{
					es.printStackTrace();
				}
				
			}
		});
 		
 		stepDetailRandomStringItemTypeComboBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				if(stepDetailRandomStringComboBox.getSelectedIndex()>=0)
				{
				//	System.out.println(stepDetailRandomStringItemTypeComboBox.getSelectedItem());
					randomStringList.get(stepDetailRandomStringComboBox.getSelectedIndex())[1]=String.valueOf(stepDetailRandomStringItemTypeComboBox.getSelectedIndex());
					if(stepDetailRandomStringItemTypeComboBox.getSelectedIndex()==3)
					{
					//	System.out.println("111.....");
						stepDetailRandomStringItemType3Text.setVisible(true);
					}
					else
					{
						stepDetailRandomStringItemType3Text.setVisible(false);
					}
				}
			}
		});
 		
 		
 		stepDetailRandomStringItemType3Text.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				if(stepDetailRandomStringComboBox.getSelectedIndex()>=0)
					randomStringList.get(stepDetailRandomStringComboBox.getSelectedIndex())[3]=stepDetailRandomStringItemType3Text.getText().trim();
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				if(stepDetailRandomStringComboBox.getSelectedIndex()>=0)
					randomStringList.get(stepDetailRandomStringComboBox.getSelectedIndex())[3]=stepDetailRandomStringItemType3Text.getText().trim();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
 		
 		
 		stepDetailRandomStringItemDeleteButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				if(stepDetailRandomStringComboBox.getSelectedIndex()>=0)
				{
					
					String[] tmp = randomStringList.get(stepDetailRandomStringComboBox.getSelectedIndex());
				//	System.out.println(tmp[4]);
					//EditorRequestDaraRawTextArea.getText().replaceAll("", tmp[4]);
					
					
					
					
					
					String StringTmp = EditorRequestDaraRawTextArea.getText();
				//	System.out.println(StringTmp+"StringTmpStringTmpStringTmpStringTmpStringTmpStringTmpStringTmpStringTmpStringTmp");
					EditorRequestDaraRawTextArea.setText("");
					String[] StringsTmp = StringTmp.split("\\$"+Method.getBase64("rSI"+tmp[0])+"\\$");
				//	System.out.println("\\$"+Method.getBase64("rSI"+tmp[0])+"\\$");
				//	System.out.println(StringsTmp.length);
					int count = StringsTmp.length;
					if(StringsTmp.length>0)
					{
						insertDocument(StringsTmp[0], Color.black);
						for(int i=1;i<count;i++)
						{
							insertDocument(tmp[4], Color.red);
							insertDocument(StringsTmp[i], Color.black);
						}
					}
					
					
					
					randomStringList.remove(stepDetailRandomStringComboBox.getSelectedIndex());
					// shuanxin  
					int randomStringCount = randomStringList.size();
					stepDetailRandomStringComboBox.removeAllItems();
					for(int i=0;i<randomStringCount;i++)
					{
						stepDetailRandomStringComboBox.addItem(i);
						stepDetailRandomStringComboBox.invalidate();
					}
					if(randomStringCount>0)
					{
						stepDetailRandomStringComboBox.setSelectedIndex(0);
						stepDetailRandomStringComboBox.setEnabled(true);
						
				 		
				 		stepDetailRandomStringItemDigitsText.setEnabled(true);
				 		
				 		
				 		stepDetailRandomStringItemTypeComboBox.setEnabled(true);
				 		
				 		stepDetailRandomStringItemDeleteButton.setEnabled(true);
					}
					else
					{
						stepDetailRandomStringComboBox.setEnabled(false);
						
				 		
				 		stepDetailRandomStringItemDigitsText.setEnabled(false);
				 		
				 		
				 		stepDetailRandomStringItemTypeComboBox.setEnabled(false);
				 		
				 		stepDetailRandomStringItemDeleteButton.setEnabled(false);
					}
					//System.out.println(randomStringCount);
				}
			}
		});
 		
 		JLabel stepDetailSetParamsFromCaptchaLabel = new JLabel("SetParamsFromCaptcha: ");
 		stepDetailSetParamsFromCaptchaLabel.setForeground(Color.blue);
 		stepDetailSetParamsFromCaptchaLabel.setSize(150,30);
 		stepDetailSetParamsFromCaptchaLabel.setLocation(7, 100);
 		stepDetailDialog.add(stepDetailSetParamsFromCaptchaLabel);
 		
 		final JComboBox stepDetailSetParamsFromCaptchaComboBox = new JComboBox();
 		stepDetailSetParamsFromCaptchaComboBox.setEnabled(false);
 		stepDetailSetParamsFromCaptchaComboBox.setSize(50,20);
 		stepDetailSetParamsFromCaptchaComboBox.setLocation(160,105);
 		stepDetailDialog.add(stepDetailSetParamsFromCaptchaComboBox);
 		
 		JLabel stepDetailSetParamsFromCaptchaDigitsLabel = new JLabel("Digits: ");
 		stepDetailSetParamsFromCaptchaDigitsLabel.setSize(60,30);
 		stepDetailSetParamsFromCaptchaDigitsLabel.setLocation(220, 100);
 		stepDetailDialog.add(stepDetailSetParamsFromCaptchaDigitsLabel);
 		
 		final JTextField stepDetailSetParamsFromCaptchaDigitsText = new JTextField();
 		stepDetailSetParamsFromCaptchaDigitsText.setEnabled(false);
 		stepDetailSetParamsFromCaptchaDigitsText.setSize(40,23);
 		stepDetailSetParamsFromCaptchaDigitsText.setLocation(270, 105);
 		stepDetailDialog.add(stepDetailSetParamsFromCaptchaDigitsText);
 		
 		JLabel stepDetailSetParamsFromCaptchaBaseURLLabel = new JLabel("BaseURL: ");
 		stepDetailSetParamsFromCaptchaBaseURLLabel.setSize(70,30);
 		stepDetailSetParamsFromCaptchaBaseURLLabel.setLocation(320, 100);
 		stepDetailDialog.add(stepDetailSetParamsFromCaptchaBaseURLLabel);
 		
 		final JTextField stepDetailSetParamsFromCaptchaBaseURLText = new JTextField();
 		stepDetailSetParamsFromCaptchaBaseURLText.setEnabled(false);
 		stepDetailSetParamsFromCaptchaBaseURLText.setSize(250,25);
 		stepDetailSetParamsFromCaptchaBaseURLText.setLocation(390,105);
 		stepDetailDialog.add(stepDetailSetParamsFromCaptchaBaseURLText);
 		
 		final JButton stepDetailSetParamsFromCaptchaDeleteButton = new JButton("Delete");
 		stepDetailSetParamsFromCaptchaDeleteButton.setEnabled(false);
 		stepDetailSetParamsFromCaptchaDeleteButton.setFocusable(false);
 		stepDetailSetParamsFromCaptchaDeleteButton.setSize(100,25);
 		stepDetailSetParamsFromCaptchaDeleteButton.setLocation(670, 105);
 		stepDetailDialog.add(stepDetailSetParamsFromCaptchaDeleteButton);
 		
 		
 		
 		stepDetailSetParamsFromCaptchaDigitsText.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				//System.out.println();
				if(stepDetailSetParamsFromCaptchaComboBox.getSelectedIndex()>=0)
				{
				//	System.out.println(captchaList.get(stepDetailSetParamsFromCaptchaComboBox.getSelectedIndex())[0]);
				//	System.out.println(captchaList.get(stepDetailSetParamsFromCaptchaComboBox.getSelectedIndex())[1]);
				//	System.out.println();
					captchaList.get(stepDetailSetParamsFromCaptchaComboBox.getSelectedIndex())[2] = stepDetailSetParamsFromCaptchaDigitsText.getText().trim();
				}
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				if(stepDetailSetParamsFromCaptchaComboBox.getSelectedIndex()>=0)
				
					captchaList.get(stepDetailSetParamsFromCaptchaComboBox.getSelectedIndex())[2] = stepDetailSetParamsFromCaptchaDigitsText.getText().trim();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
 		
 		
 		stepDetailSetParamsFromCaptchaBaseURLText.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				if(stepDetailSetParamsFromCaptchaComboBox.getSelectedIndex()>=0)
					captchaList.get(stepDetailSetParamsFromCaptchaComboBox.getSelectedIndex())[1] = stepDetailSetParamsFromCaptchaBaseURLText.getText().trim();
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				if(stepDetailSetParamsFromCaptchaComboBox.getSelectedIndex()>=0)
					captchaList.get(stepDetailSetParamsFromCaptchaComboBox.getSelectedIndex())[1] = stepDetailSetParamsFromCaptchaBaseURLText.getText().trim();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
 		
 		
 		stepDetailSetParamsFromCaptchaComboBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				
				int index = stepDetailSetParamsFromCaptchaComboBox.getSelectedIndex();
			//	System.out.println(index);
				if(index>=0)
				{
					try
					{
					String[] tmp = captchaList.get(stepDetailSetParamsFromCaptchaComboBox.getSelectedIndex());
					stepDetailSetParamsFromCaptchaDigitsText.setText(tmp[2]);
					stepDetailSetParamsFromCaptchaBaseURLText.setText(tmp[1]);
				//	System.out.println(tmp[0]);
				//	System.out.println(tmp[1]);
				//	System.out.println(tmp[2]);
					
					
					
					
					
					String StringTmp = EditorRequestDaraRawTextArea.getText();
				//	System.out.println(StringTmp+"StringTmpStringTmpStringTmpStringTmpStringTmpStringTmpStringTmpStringTmpStringTmp");
					EditorRequestDaraRawTextArea.setText("");
					String[] StringsTmp = StringTmp.split(Method.getBase64("sPFC"+tmp[0]));
					int count = StringsTmp.length;
					if(StringsTmp.length>0)
					{
						insertDocument(StringsTmp[0], Color.black);
						for(int i=1;i<count;i++)
						{
							insertDocument(Method.getBase64("sPFC"+tmp[0]), Color.blue);
							insertDocument(StringsTmp[i], Color.black);
						}
					}
					
					}
					catch(Exception es)
					{
						es.printStackTrace();
					}
				}
				else
				{
					stepDetailSetParamsFromCaptchaDigitsText.setText("");
					stepDetailSetParamsFromCaptchaBaseURLText.setText("");
				}
				
				
				
				
				
				
			}
		});
 		
 		
 		stepDetailSetParamsFromCaptchaDeleteButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				
				
				

				String[] tmp = captchaList.get(stepDetailSetParamsFromCaptchaComboBox.getSelectedIndex());
			//	System.out.println(tmp[3]);
					//EditorRequestDaraRawTextArea.getText().replaceAll("", tmp[4]);
				
				
				//System.exit(1);
				
				
				
				
				captchaList.remove(stepDetailSetParamsFromCaptchaComboBox.getSelectedIndex());
				int captchaListCount = captchaList.size();
				stepDetailSetParamsFromCaptchaComboBox.removeAllItems();
				
				
				
				
				String StringTmp = EditorRequestDaraRawTextArea.getText();
			
				EditorRequestDaraRawTextArea.setText("");
				String[] StringsTmp = StringTmp.split("\\$"+Method.getBase64("sPFC"+tmp[0])+"\\$");
				
				int count = StringsTmp.length;
				insertDocument(StringsTmp[0], Color.black);
				for(int i=1;i<count;i++)
				{
					insertDocument(tmp[3], Color.blue);
					insertDocument(StringsTmp[i], Color.black);
				}
				
				
				
				
				
				
				
				
				for(int i=0;i<captchaListCount;i++)
				{
					stepDetailSetParamsFromCaptchaComboBox.addItem(i);
					stepDetailSetParamsFromCaptchaComboBox.invalidate();
				}
				if(captchaListCount>0)
				{
					stepDetailSetParamsFromCaptchaComboBox.setSelectedIndex(0);
					
					
			 		stepDetailSetParamsFromCaptchaComboBox.setEnabled(true);		 		
			 		
			 		stepDetailSetParamsFromCaptchaDigitsText.setEnabled(true);
			 		
			 		stepDetailSetParamsFromCaptchaBaseURLText.setEnabled(true);
			 		
			 		stepDetailSetParamsFromCaptchaDeleteButton.setEnabled(true);
					
				}
				else
				{
					stepDetailSetParamsFromCaptchaComboBox.setEnabled(false);		 		
			 		
			 		stepDetailSetParamsFromCaptchaDigitsText.setEnabled(false);
			 		
			 		stepDetailSetParamsFromCaptchaBaseURLText.setEnabled(false);
			 		
			 		stepDetailSetParamsFromCaptchaDeleteButton.setEnabled(false);
				}
				//System.out.println(captchaListCount);
			}
		});
 		
 		
 		JLabel stepDetailSetGoaheadOrStopFromResponseStatusLabel = new JLabel("SetGoAheadOrStopFromResponseStatus: ");
 		stepDetailSetGoaheadOrStopFromResponseStatusLabel.setSize(250,30);
 		stepDetailSetGoaheadOrStopFromResponseStatusLabel.setLocation(7, 130);
 		stepDetailDialog.add(stepDetailSetGoaheadOrStopFromResponseStatusLabel);
 		
 		final JComboBox stepDetailSetGoaheadOrStopFromResponseStatusNumsComboBox = new JComboBox(); 
 		stepDetailSetGoaheadOrStopFromResponseStatusNumsComboBox.setEnabled(false);
 		stepDetailSetGoaheadOrStopFromResponseStatusNumsComboBox.setSize(50,20);
 		stepDetailSetGoaheadOrStopFromResponseStatusNumsComboBox.setLocation(270, 135);
 		stepDetailDialog.add(stepDetailSetGoaheadOrStopFromResponseStatusNumsComboBox);
 		
 		JLabel stepDetailSetGoaheadOrStopFromResponseStatusStatusLabel = new JLabel("Status: ");
 		stepDetailSetGoaheadOrStopFromResponseStatusStatusLabel.setSize(70,30);
 		stepDetailSetGoaheadOrStopFromResponseStatusStatusLabel.setLocation(330, 130);
 		stepDetailDialog.add(stepDetailSetGoaheadOrStopFromResponseStatusStatusLabel);
 		
 		
 		final JTextField stepDetailSetGoaheadOrStopFromResponseStatusStatusText = new JTextField();
 		stepDetailSetGoaheadOrStopFromResponseStatusStatusText.setEnabled(false);
 		stepDetailSetGoaheadOrStopFromResponseStatusStatusText.setSize(70,25);
 		stepDetailSetGoaheadOrStopFromResponseStatusStatusText.setLocation(380, 135);
 		stepDetailDialog.add(stepDetailSetGoaheadOrStopFromResponseStatusStatusText);
 		
 		JLabel stepDetailSetGoaheadOrStopFromResponseStatusActionLabel = new JLabel("Action: ");
 		stepDetailSetGoaheadOrStopFromResponseStatusActionLabel.setSize(70,30);
 		stepDetailSetGoaheadOrStopFromResponseStatusActionLabel.setLocation(460, 130);
 		stepDetailDialog.add(stepDetailSetGoaheadOrStopFromResponseStatusActionLabel);
 		
 		
 		String[] stepDetailSetGoaheadOrStopFromResponseStatusAction = {"Stop Current","Go Ahead Current","Stop EXP","Go Ahead EXP"};
 		final JComboBox stepDetailSetGoaheadOrStopFromResponseStatusActionComboBox = new JComboBox(stepDetailSetGoaheadOrStopFromResponseStatusAction);
 		stepDetailSetGoaheadOrStopFromResponseStatusActionComboBox.setEnabled(false);
 		stepDetailSetGoaheadOrStopFromResponseStatusActionComboBox.setSize(150,25);
 		stepDetailSetGoaheadOrStopFromResponseStatusActionComboBox.setLocation(510, 135);
 		stepDetailDialog.add(stepDetailSetGoaheadOrStopFromResponseStatusActionComboBox);
 		
 		final JButton stepDetailSetGoaheadOrStopFromResponseStatusDeleteButton =new JButton("Delete");
 		stepDetailSetGoaheadOrStopFromResponseStatusDeleteButton.setEnabled(false);
 		stepDetailSetGoaheadOrStopFromResponseStatusDeleteButton.setFocusable(false);
 		stepDetailSetGoaheadOrStopFromResponseStatusDeleteButton.setSize(100,25);
 		stepDetailSetGoaheadOrStopFromResponseStatusDeleteButton.setLocation(670, 135);
 		stepDetailDialog.add(stepDetailSetGoaheadOrStopFromResponseStatusDeleteButton);
 		
 		
 		stepDetailSetGoaheadOrStopFromResponseStatusNumsComboBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				if(stepDetailSetGoaheadOrStopFromResponseStatusNumsComboBox.getSelectedIndex()>=0)
				{
					//System.out.println("stepDetailSetGoaheadOrStopFromResponseStatusNumsComboBox.getSelectedIndex()"+stepDetailSetGoaheadOrStopFromResponseStatusNumsComboBox.getSelectedIndex());
					String[] tmp = setGoAheadOrStopFromStatusList.get(stepDetailSetGoaheadOrStopFromResponseStatusNumsComboBox.getSelectedIndex());
					stepDetailSetGoaheadOrStopFromResponseStatusStatusText.setText(tmp[0]);
					if(tmp[1].equals("Stop Current"))
					{
						stepDetailSetGoaheadOrStopFromResponseStatusActionComboBox.setSelectedIndex(0);
					}
					else if(tmp[1].equals("Go Ahead Current"))
					{
						stepDetailSetGoaheadOrStopFromResponseStatusActionComboBox.setSelectedIndex(1);
					}
					else if(tmp[1].equals("Stop EXP"))
					{
						stepDetailSetGoaheadOrStopFromResponseStatusActionComboBox.setSelectedIndex(2);
					}
					else if(tmp[1].equals("Go Ahead EXP"))
					{
						stepDetailSetGoaheadOrStopFromResponseStatusActionComboBox.setSelectedIndex(3);
					}
						
					
				//	System.out.println(tmp[1]);
				//	System.out.println();
				}	
				else
				{
					stepDetailSetGoaheadOrStopFromResponseStatusStatusText.setText("");
				}
				
			}
		});
 		
 		
 		stepDetailSetGoaheadOrStopFromResponseStatusStatusText.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				if(stepDetailSetGoaheadOrStopFromResponseStatusNumsComboBox.getSelectedIndex()>=0)
				{
				//	System.out.println();
				//	System.out.println(setGoAheadOrStopFromStatusList.get(stepDetailSetGoaheadOrStopFromResponseStatusNumsComboBox.getSelectedIndex())[1]);
					
					setGoAheadOrStopFromStatusList.get(stepDetailSetGoaheadOrStopFromResponseStatusNumsComboBox.getSelectedIndex())[0] = stepDetailSetGoaheadOrStopFromResponseStatusStatusText.getText().trim();
				}
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				if(stepDetailSetGoaheadOrStopFromResponseStatusNumsComboBox.getSelectedIndex()>=0)
				{
				//	System.out.println();
				//	System.out.println(setGoAheadOrStopFromStatusList.get(stepDetailSetGoaheadOrStopFromResponseStatusNumsComboBox.getSelectedIndex())[1]);
					
					setGoAheadOrStopFromStatusList.get(stepDetailSetGoaheadOrStopFromResponseStatusNumsComboBox.getSelectedIndex())[0] = stepDetailSetGoaheadOrStopFromResponseStatusStatusText.getText().trim();
				}
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
 		
 		stepDetailSetGoaheadOrStopFromResponseStatusActionComboBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				if(stepDetailSetGoaheadOrStopFromResponseStatusNumsComboBox.getSelectedIndex()>=0)
				{
					setGoAheadOrStopFromStatusList.get(stepDetailSetGoaheadOrStopFromResponseStatusNumsComboBox.getSelectedIndex())[1] = (String) stepDetailSetGoaheadOrStopFromResponseStatusActionComboBox.getSelectedItem();
				}
			}
		});
 		
 		
 		stepDetailSetGoaheadOrStopFromResponseStatusDeleteButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				if(stepDetailSetGoaheadOrStopFromResponseStatusNumsComboBox.getSelectedIndex()>=0)
				{
					//System.out.println("setGoAheadOrStopFromStatusList"+setGoAheadOrStopFromStatusList.size());
					//System.out.println("stepDetailSetGoaheadOrStopFromResponseStatusNumsComboBox.getSelectedIndex()"+stepDetailSetGoaheadOrStopFromResponseStatusNumsComboBox.getSelectedIndex());
					setGoAheadOrStopFromStatusList.remove(stepDetailSetGoaheadOrStopFromResponseStatusNumsComboBox.getSelectedIndex());


					int setGoAheadOrStopFromStatusListCount = setGoAheadOrStopFromStatusList.size();
					stepDetailSetGoaheadOrStopFromResponseStatusNumsComboBox.removeAllItems();
					for(int i=0;i<setGoAheadOrStopFromStatusListCount;i++)
					{
						stepDetailSetGoaheadOrStopFromResponseStatusNumsComboBox.addItem(i);
						stepDetailSetGoaheadOrStopFromResponseStatusNumsComboBox.invalidate();
					}
					if(setGoAheadOrStopFromStatusListCount>0)
					{
						stepDetailSetGoaheadOrStopFromResponseStatusNumsComboBox.setSelectedIndex(0);
						
						stepDetailSetGoaheadOrStopFromResponseStatusNumsComboBox.setEnabled(true);
				 		
				 		stepDetailSetGoaheadOrStopFromResponseStatusStatusText.setEnabled(true);
				 		
				 		stepDetailSetGoaheadOrStopFromResponseStatusActionComboBox.setEnabled(true);
				 		
				 		stepDetailSetGoaheadOrStopFromResponseStatusDeleteButton.setEnabled(true);
						
						
					}
					else
					{
						stepDetailSetGoaheadOrStopFromResponseStatusNumsComboBox.setEnabled(false);
				 		
				 		stepDetailSetGoaheadOrStopFromResponseStatusStatusText.setEnabled(false);
				 		
				 		stepDetailSetGoaheadOrStopFromResponseStatusActionComboBox.setEnabled(false);
				 		
				 		stepDetailSetGoaheadOrStopFromResponseStatusDeleteButton.setEnabled(false);
					}
					//System.out.println(setGoAheadOrStopFromStatusListCount);
					
				}
			}
		});
 		
 		JLabel stepDetailSetGoaheadOrStopFromResponseDataLabel = new JLabel("SetGoAheadOrStopFromResponseData: ");
 		stepDetailSetGoaheadOrStopFromResponseDataLabel.setSize(250,30);
 		stepDetailSetGoaheadOrStopFromResponseDataLabel.setLocation(7, 160);
 		stepDetailDialog.add(stepDetailSetGoaheadOrStopFromResponseDataLabel);
 		
 		final JComboBox stepDetailSetGoaheadOrStopFromResponseDataNumsComboBox = new JComboBox(); 
 		stepDetailSetGoaheadOrStopFromResponseDataNumsComboBox.setEnabled(false);
 		stepDetailSetGoaheadOrStopFromResponseDataNumsComboBox.setSize(50,20);
 		stepDetailSetGoaheadOrStopFromResponseDataNumsComboBox.setLocation(270, 165);
 		stepDetailDialog.add(stepDetailSetGoaheadOrStopFromResponseDataNumsComboBox);
 		
 		JLabel stepDetailSetGoaheadOrStopFromResponseDataDataLabel = new JLabel("Data: ");
 		stepDetailSetGoaheadOrStopFromResponseDataDataLabel.setSize(70,30);
 		stepDetailSetGoaheadOrStopFromResponseDataDataLabel.setLocation(330, 160);
 		stepDetailDialog.add(stepDetailSetGoaheadOrStopFromResponseDataDataLabel);
 		
 		
 		final JTextField stepDetailSetGoaheadOrStopFromResponseDataDataText = new JTextField();
 		stepDetailSetGoaheadOrStopFromResponseDataDataText.setEnabled(false);
 		stepDetailSetGoaheadOrStopFromResponseDataDataText.setSize(70,25);
 		stepDetailSetGoaheadOrStopFromResponseDataDataText.setLocation(380, 165);
 		stepDetailDialog.add(stepDetailSetGoaheadOrStopFromResponseDataDataText);
 		
 		
 		JLabel stepDetailSetGoaheadOrStopFromResponseDataActionLabel = new JLabel("Action: ");
 		stepDetailSetGoaheadOrStopFromResponseDataActionLabel.setSize(70,30);
 		stepDetailSetGoaheadOrStopFromResponseDataActionLabel.setLocation(460, 160);
 		stepDetailDialog.add(stepDetailSetGoaheadOrStopFromResponseDataActionLabel);
 		
 		String[] stepDetailSetGoaheadOrStopFromResponseDataAction = {"Stop Current","Go Ahead Current","Stop EXP","Go Ahead EXP"};
 		final JComboBox stepDetailSetGoaheadOrStopFromResponseDataActionComboBox = new JComboBox(stepDetailSetGoaheadOrStopFromResponseDataAction);
 		stepDetailSetGoaheadOrStopFromResponseDataActionComboBox.setEnabled(false);
 		stepDetailSetGoaheadOrStopFromResponseDataActionComboBox.setSize(150,25);
 		stepDetailSetGoaheadOrStopFromResponseDataActionComboBox.setLocation(510, 165);
 		stepDetailDialog.add(stepDetailSetGoaheadOrStopFromResponseDataActionComboBox);
 		
 		final JButton stepDetailSetGoaheadOrStopFromResponseDataDeleteButton =new JButton("Delete");
 		stepDetailSetGoaheadOrStopFromResponseDataDeleteButton.setEnabled(false);
 		stepDetailSetGoaheadOrStopFromResponseDataDeleteButton.setFocusable(false);
 		stepDetailSetGoaheadOrStopFromResponseDataDeleteButton.setSize(100,25);
 		stepDetailSetGoaheadOrStopFromResponseDataDeleteButton.setLocation(670, 165);
 		stepDetailDialog.add(stepDetailSetGoaheadOrStopFromResponseDataDeleteButton);
 		
 		
 		stepDetailSetGoaheadOrStopFromResponseDataNumsComboBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				int index  = stepDetailSetGoaheadOrStopFromResponseDataNumsComboBox.getSelectedIndex();
				if(index>=0)
				{
					String[] tmp = setGoAheadOrStopFromDataList.get(index);
					stepDetailSetGoaheadOrStopFromResponseDataDataText.setText(tmp[0]);
				//	System.out.println(tmp[0]);
				//	System.out.println(tmp[1]);
					if(tmp[1].equals("Stop Current"))
					{
						stepDetailSetGoaheadOrStopFromResponseDataActionComboBox.setSelectedIndex(0);
					}
					else if(tmp[1].equals("Go Ahead Current"))
					{
						stepDetailSetGoaheadOrStopFromResponseDataActionComboBox.setSelectedIndex(1);
					}
					else if(tmp[1].equals("Stop EXP"))
					{
						stepDetailSetGoaheadOrStopFromResponseDataActionComboBox.setSelectedIndex(2);
					}
					else if(tmp[1].equals("Go Ahead EXP"))
					{
						stepDetailSetGoaheadOrStopFromResponseDataActionComboBox.setSelectedIndex(3);
					}
				}
				else
				{
					stepDetailSetGoaheadOrStopFromResponseDataDataText.setText("");
				}
			}
		});
 		
 		stepDetailSetGoaheadOrStopFromResponseDataDataText.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				if(stepDetailSetGoaheadOrStopFromResponseDataNumsComboBox.getSelectedIndex()>=0)
				{
					//System.out.println();
					//System.out.println(setGoAheadOrStopFromDataList.get(stepDetailSetGoaheadOrStopFromResponseDataNumsComboBox.getSelectedIndex())[1]);
					
					setGoAheadOrStopFromDataList.get(stepDetailSetGoaheadOrStopFromResponseDataNumsComboBox.getSelectedIndex())[0] = stepDetailSetGoaheadOrStopFromResponseDataDataText.getText().trim();
				}
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				if(stepDetailSetGoaheadOrStopFromResponseDataNumsComboBox.getSelectedIndex()>=0)
				{
					//System.out.println();
				//	System.out.println(setGoAheadOrStopFromDataList.get(stepDetailSetGoaheadOrStopFromResponseDataNumsComboBox.getSelectedIndex())[1]);
					
					setGoAheadOrStopFromDataList.get(stepDetailSetGoaheadOrStopFromResponseDataNumsComboBox.getSelectedIndex())[0] = stepDetailSetGoaheadOrStopFromResponseDataDataText.getText().trim();
				}
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
 		
 		
 		stepDetailSetGoaheadOrStopFromResponseDataActionComboBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				if(stepDetailSetGoaheadOrStopFromResponseDataNumsComboBox.getSelectedIndex()>=0)
				{
					setGoAheadOrStopFromDataList.get(stepDetailSetGoaheadOrStopFromResponseDataNumsComboBox.getSelectedIndex())[1] = (String) stepDetailSetGoaheadOrStopFromResponseDataActionComboBox.getSelectedItem();
				}
			}
		});
 		
 		
 		
 		stepDetailSetGoaheadOrStopFromResponseDataDeleteButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				if(stepDetailSetGoaheadOrStopFromResponseDataNumsComboBox.getSelectedIndex()>=0)
				{
					//System.out.println("setGoAheadOrStopFromDataList"+setGoAheadOrStopFromDataList.size());
					//System.out.println("stepDetailSetGoaheadOrStopFromResponseDataNumsComboBox.getSelectedIndex()"+stepDetailSetGoaheadOrStopFromResponseDataNumsComboBox.getSelectedIndex());
					setGoAheadOrStopFromDataList.remove(stepDetailSetGoaheadOrStopFromResponseDataNumsComboBox.getSelectedIndex());


					int setGoAheadOrStopFromDataListCount = setGoAheadOrStopFromDataList.size();
					stepDetailSetGoaheadOrStopFromResponseDataNumsComboBox.removeAllItems();
					for(int i=0;i<setGoAheadOrStopFromDataListCount;i++)
					{
						stepDetailSetGoaheadOrStopFromResponseDataNumsComboBox.addItem(i);
						stepDetailSetGoaheadOrStopFromResponseDataNumsComboBox.invalidate();
					}
					if(setGoAheadOrStopFromDataListCount>0)
					{
						stepDetailSetGoaheadOrStopFromResponseDataNumsComboBox.setSelectedIndex(0);
						
						stepDetailSetGoaheadOrStopFromResponseDataNumsComboBox.setEnabled(true);
				 		
				 		stepDetailSetGoaheadOrStopFromResponseDataDataText.setEnabled(true);
				 		
				 		stepDetailSetGoaheadOrStopFromResponseDataActionComboBox.setEnabled(true);
				 		
				 		stepDetailSetGoaheadOrStopFromResponseDataDeleteButton.setEnabled(true);
						
						
					}
					else
					{
						stepDetailSetGoaheadOrStopFromResponseDataNumsComboBox.setEnabled(false);
				 		
				 		stepDetailSetGoaheadOrStopFromResponseDataDataText.setEnabled(false);
				 		
				 		stepDetailSetGoaheadOrStopFromResponseDataActionComboBox.setEnabled(false);
				 		
				 		stepDetailSetGoaheadOrStopFromResponseDataDeleteButton.setEnabled(false);
					}
					//System.out.println(setGoAheadOrStopFromDataListCount);
					
				}
			}
		});
 		
 		
 		JLabel stepDetailUseFileForCycleWithExpLabel = new JLabel("UseFileForCycleWithExp: ");
 		stepDetailUseFileForCycleWithExpLabel.setForeground(Color.green);
 		stepDetailUseFileForCycleWithExpLabel.setSize(200,30);
 		stepDetailUseFileForCycleWithExpLabel.setLocation(7, 190);
 		stepDetailDialog.add(stepDetailUseFileForCycleWithExpLabel);
 		
 		final JTextField stepDetailUseFileForCycleWithExpText = new JTextField();
 		stepDetailUseFileForCycleWithExpText.setEnabled(false);
 		stepDetailUseFileForCycleWithExpText.setSize(400,25);
 		stepDetailUseFileForCycleWithExpText.setLocation(180, 195);
 		stepDetailDialog.add(stepDetailUseFileForCycleWithExpText);
 		
 		final JLabel stepDetailUseFileForCycleWithExpInStepLabel = new JLabel("In Step");
 		stepDetailUseFileForCycleWithExpInStepLabel.setSize(80,30);
 		stepDetailUseFileForCycleWithExpInStepLabel.setLocation(590, 190);
 		stepDetailDialog.add(stepDetailUseFileForCycleWithExpInStepLabel);
 		
 		final JButton stepDetailUseFileForCycleWithExpDeleteButton = new JButton("Delete");
 		stepDetailUseFileForCycleWithExpDeleteButton.setEnabled(false);
 		stepDetailUseFileForCycleWithExpDeleteButton.setFocusable(false);
 		stepDetailUseFileForCycleWithExpDeleteButton.setSize(100,25);
 		stepDetailUseFileForCycleWithExpDeleteButton.setLocation(670, 195);
 		stepDetailDialog.add(stepDetailUseFileForCycleWithExpDeleteButton);
 		
 		
 		stepDetailUseFileForCycleWithExpText.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				//System.out.println("xianshi");
				
				String StringTmp = EditorRequestDaraRawTextArea.getText();
				//System.out.println(StringTmp+"StringTmpStringTmpStringTmpStringTmpStringTmpStringTmpStringTmpStringTmpStringTmp");
				EditorRequestDaraRawTextArea.setText("");
				String[] StringsTmp = StringTmp.split(Method.getBase64("FileForCycleWithEXP"));
				int count = StringsTmp.length;
				insertDocument(StringsTmp[0], Color.black);
				for(int i=1;i<count;i++)
				{
					insertDocument(Method.getBase64("FileForCycleWithEXP"), Color.green);
					insertDocument(StringsTmp[i], Color.black);
				}
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
 		
 		
 		stepDetailUseFileForCycleWithExpText.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				FileForCycleWithEXP = stepDetailUseFileForCycleWithExpText.getText().trim();
				FileForCycleWithEXPStrings[0] = FileForCycleWithEXP;
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				FileForCycleWithEXP = stepDetailUseFileForCycleWithExpText.getText().trim();
				FileForCycleWithEXPStrings[0] = FileForCycleWithEXP;
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
 		
 		stepDetailUseFileForCycleWithExpDeleteButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				FileForCycleWithEXP="";
				GrobalFileInStep=-1;
				
				FileForCycleWithEXPStrings[0] = "";
				FileForCycleWithEXPStrings[1] = "-1";
				
				
				
				
				
				
				String StringTmp = EditorRequestDaraRawTextArea.getText();
				
				EditorRequestDaraRawTextArea.setText("");
				String[] StringsTmp = StringTmp.split("\\$"+Method.getBase64("FileForCycleWithEXP")+"\\$");
				int count = StringsTmp.length;
				if(count>0)
				{
					insertDocument(StringsTmp[0], Color.black);
					for(int i=1;i<count;i++)
					{
						insertDocument(FileForCycleWithEXPStrings[3], Color.red);
						insertDocument(StringsTmp[i], Color.black);
					}
				}

				
				
				
				
				
				
				
			//	System.out.println("______________-");
				if(FileForCycleWithEXP.length()>0)
				{
					stepDetailUseFileForCycleWithExpText.setText(FileForCycleWithEXP);
					//System.out.println("Step in "+GrobalFileInStep);
					stepDetailUseFileForCycleWithExpInStepLabel.setText("In Step"+(GrobalFileInStep-1));
					
					
					stepDetailUseFileForCycleWithExpText.setEnabled(true);
			 		
			 		stepDetailUseFileForCycleWithExpDeleteButton.setEnabled(true);
					
					
				}
				else
				{
					stepDetailUseFileForCycleWithExpText.setText("");
					stepDetailUseFileForCycleWithExpInStepLabel.setText("In Step");
					
					stepDetailUseFileForCycleWithExpText.setEnabled(false);
			 		
			 		stepDetailUseFileForCycleWithExpDeleteButton.setEnabled(false);
					
				}
			//	System.out.println(FileForCycleWithEXP+"__________"+GrobalFileInStep);
				
				
				
			}
		});
 		
 		JLabel stepDetailUseFileForCycleWithPackageLabel = new JLabel("UseFileForCycleWithPackage: ");
 		stepDetailUseFileForCycleWithPackageLabel.setForeground(Color.PINK);
 		stepDetailUseFileForCycleWithPackageLabel.setSize(200,30);
 		stepDetailUseFileForCycleWithPackageLabel.setLocation(7, 220);
 		stepDetailDialog.add(stepDetailUseFileForCycleWithPackageLabel);
 		
 		final JTextField stepDetailUseFileForCycleWithPackageFile1Text = new JTextField();
 		stepDetailUseFileForCycleWithPackageFile1Text.setEnabled(false);
 		stepDetailUseFileForCycleWithPackageFile1Text.setSize(400,25);
 		stepDetailUseFileForCycleWithPackageFile1Text.setLocation(180, 225);
 		stepDetailDialog.add(stepDetailUseFileForCycleWithPackageFile1Text);
 		
 		final JButton stepDetailUseFileForCycleWithPackageFile1DeleteButton = new JButton("Delete");
 		stepDetailUseFileForCycleWithPackageFile1DeleteButton.setEnabled(false);
 		stepDetailUseFileForCycleWithPackageFile1DeleteButton.setSize(100,25);
 		stepDetailUseFileForCycleWithPackageFile1DeleteButton.setLocation(670, 225);
 		stepDetailDialog.add(stepDetailUseFileForCycleWithPackageFile1DeleteButton);
 		stepDetailUseFileForCycleWithPackageFile1DeleteButton.setFocusable(false);
 		
 		JLabel stepDetailUseFileForCycleWithPackageMethodLabel = new JLabel("组合方式: ");
 		stepDetailUseFileForCycleWithPackageMethodLabel.setSize(70,30);
 		stepDetailUseFileForCycleWithPackageMethodLabel.setLocation(300, 250);
 		stepDetailDialog.add(stepDetailUseFileForCycleWithPackageMethodLabel);
 		
 		final JRadioButton stepDetailUseFileForCycleWithPackageMethodRadio1 = new JRadioButton("一对一");
 		stepDetailUseFileForCycleWithPackageMethodRadio1.setEnabled(false);
 		stepDetailUseFileForCycleWithPackageMethodRadio1.setSize(70,25);
 		stepDetailUseFileForCycleWithPackageMethodRadio1.setLocation(370, 255);
 		stepDetailDialog.add(stepDetailUseFileForCycleWithPackageMethodRadio1);
 		
 		final JRadioButton stepDetailUseFileForCycleWithPackageMethodRadio2 = new JRadioButton("一对duo");
 		stepDetailUseFileForCycleWithPackageMethodRadio2.setEnabled(false);
 		stepDetailUseFileForCycleWithPackageMethodRadio2.setSize(70,25);
 		stepDetailUseFileForCycleWithPackageMethodRadio2.setLocation(450, 255);
 		stepDetailDialog.add(stepDetailUseFileForCycleWithPackageMethodRadio2);
 		
 		ButtonGroup stepDetailUseFileForCycleWithPackageMethodRadioButtonGroup = new ButtonGroup();
 		stepDetailUseFileForCycleWithPackageMethodRadioButtonGroup.add(stepDetailUseFileForCycleWithPackageMethodRadio1);
 		stepDetailUseFileForCycleWithPackageMethodRadioButtonGroup.add(stepDetailUseFileForCycleWithPackageMethodRadio2);
 		
 		
 		final JTextField stepDetailUseFileForCycleWithPackageFile2Text = new JTextField();
 		stepDetailUseFileForCycleWithPackageFile2Text.setEnabled(false);
 		stepDetailUseFileForCycleWithPackageFile2Text.setSize(400,25);
 		stepDetailUseFileForCycleWithPackageFile2Text.setLocation(180, 285);
 		stepDetailDialog.add(stepDetailUseFileForCycleWithPackageFile2Text);
 		
 		final JButton stepDetailUseFileForCycleWithPackageFile2DeleteButton = new JButton("Delete");
 		stepDetailUseFileForCycleWithPackageFile2DeleteButton.setEnabled(false);
 		stepDetailUseFileForCycleWithPackageFile2DeleteButton.setSize(100,25);
 		stepDetailUseFileForCycleWithPackageFile2DeleteButton.setLocation(670, 285);
 		stepDetailDialog.add(stepDetailUseFileForCycleWithPackageFile2DeleteButton);
 		stepDetailUseFileForCycleWithPackageFile2DeleteButton.setFocusable(false);
 		
 		
 		stepDetailUseFileForCycleWithPackageFile1Text.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				useFileForCycleWithPackageItem[1] = stepDetailUseFileForCycleWithPackageFile1Text.getText().trim();
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				useFileForCycleWithPackageItem[1] = stepDetailUseFileForCycleWithPackageFile1Text.getText().trim();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
 		
 		
 		stepDetailUseFileForCycleWithPackageFile1Text.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				String StringTmp = EditorRequestDaraRawTextArea.getText();
				//System.out.println(StringTmp+"StringTmpStringTmpStringTmpStringTmpStringTmpStringTmpStringTmpStringTmpStringTmp");
				EditorRequestDaraRawTextArea.setText("");
				String[] StringsTmp = StringTmp.split(Method.getBase64("File-1"));
				int count = StringsTmp.length;
				insertDocument(StringsTmp[0], Color.black);
				for(int i=1;i<count;i++)
				{
					insertDocument(Method.getBase64("File-1"), Color.PINK);
					insertDocument(StringsTmp[i], Color.black);
				}
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
 		
 		
 		
 		stepDetailUseFileForCycleWithPackageFile2Text.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				useFileForCycleWithPackageItem[2] = stepDetailUseFileForCycleWithPackageFile2Text.getText().trim();
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				useFileForCycleWithPackageItem[2] = stepDetailUseFileForCycleWithPackageFile2Text.getText().trim();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
 		
 		
 		stepDetailUseFileForCycleWithPackageFile2Text.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				String StringTmp = EditorRequestDaraRawTextArea.getText();
				//System.out.println(StringTmp+"StringTmpStringTmpStringTmpStringTmpStringTmpStringTmpStringTmpStringTmpStringTmp");
				EditorRequestDaraRawTextArea.setText("");
				String[] StringsTmp = StringTmp.split(Method.getBase64("File-2"));
				int count = StringsTmp.length;
				insertDocument(StringsTmp[0], Color.black);
				for(int i=1;i<count;i++)
				{
					insertDocument(Method.getBase64("File-2"), Color.PINK);
					insertDocument(StringsTmp[i], Color.black);
				}
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
 		
 		stepDetailUseFileForCycleWithPackageMethodRadio1.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				//System.out.println("111");
				useFileForCycleWithPackageItem[3] = "1";
			}
		});
 		
 		
 		stepDetailUseFileForCycleWithPackageMethodRadio2.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				useFileForCycleWithPackageItem[3] = "2";
			}
		});
 		
 		
 		stepDetailUseFileForCycleWithPackageFile1DeleteButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				if(useFileForCycleWithPackageItem[0].equals("1"))
				{
					useFileForCycleWithPackageItem[0] = "0";
					useFileForCycleWithPackageItem[1] = "";
					//useFileForCycleWithPackageItem[4] = "";
					
					String StringTmp = EditorRequestDaraRawTextArea.getText();
				//	System.out.println("999999999999999999999999999999999999999999"+StringTmp);
					
					EditorRequestDaraRawTextArea.setText("");
					String[] StringsTmp = StringTmp.split("\\$"+Method.getBase64("File-1")+"\\$");
					
					
					int count = StringsTmp.length;
					insertDocument(StringsTmp[0], Color.black);
					for(int i=1;i<count;i++)
					{
						insertDocument(useFileForCycleWithPackageItem[4], Color.red);
						insertDocument(StringsTmp[i], Color.black);
					}
					
					
					useFileForCycleWithPackageItem[4] = "";
					
					stepDetailUseFileForCycleWithPackageFile1Text.setText("");
					
					
				}
				else if(useFileForCycleWithPackageItem[0].equals("2"))
				{
					useFileForCycleWithPackageItem[0] = "1";
					useFileForCycleWithPackageItem[1] = useFileForCycleWithPackageItem[2];
					useFileForCycleWithPackageItem[2] = "";
					useFileForCycleWithPackageItem[3] = "0";
					
					
					
					
					
					String StringTmp = EditorRequestDaraRawTextArea.getText();
					
					
				
					
					//System.out.println("StringTmpStringTmpStringTmpStringTmpStringTmpStringTmpStringTmpStringTmpStringTmpStringTmp"+StringTmp);
					
					EditorRequestDaraRawTextArea.setText("");
					String[] StringsTmp = StringTmp.split("\\$"+Method.getBase64("File-1")+"\\$");
					
					int count = StringsTmp.length;
					insertDocument(StringsTmp[0], Color.black);
					for(int i=1;i<count;i++)
					{
						insertDocument(useFileForCycleWithPackageItem[4], Color.red);
						insertDocument(StringsTmp[i], Color.black);
					}
					
					
					useFileForCycleWithPackageItem[4] =useFileForCycleWithPackageItem[5];
					useFileForCycleWithPackageItem[5] = "";
					
					//System.out.println(useFileForCycleWithPackageItem[4]);
					//System.exit(1);
					
					
					StringTmp = EditorRequestDaraRawTextArea.getText();
					StringTmp = StringTmp.replaceAll(Method.addSignRegex(Method.getBase64("File-2")), Method.addSignRegex(Method.getBase64("File-1")));
					EditorRequestDaraRawTextArea.setText(StringTmp);
					
					//String StringTmp2 = EditorRequestDaraRawTextArea.getText();
					
					//System.out.println("StringTmp2StringTmp2StringTmp2StringTmp2"+Method.addSign(Method.getBase64("File-2")));
					//StringTmp2 = StringTmp2.replaceAll(Method.addSign(Method.getBase64("File-2")), Method.addSign(Method.getBase64("File-1")));
					
					
					stepDetailUseFileForCycleWithPackageFile2Text.setText("");
				}
				
				
				
				
				
				
				int useFileForCycleWithPackageListCount = useFileForCycleWithPackageList.size();
				String[] useFileForCycleWithPackageTmp = useFileForCycleWithPackageItem;
		//		System.out.println(useFileForCycleWithPackageTmp[0]+"useFileForCycleWithPackageTmp[0]");
				if(useFileForCycleWithPackageTmp[0]!=null&&!useFileForCycleWithPackageTmp[0].equals(null)&&useFileForCycleWithPackageTmp.length>1)
				{
					//System.out.println();
					if(useFileForCycleWithPackageTmp[0].equals("1"))
					{
						//System.out.println(useFileForCycleWithPackageTmp[0]);
						//System.out.println();
						stepDetailUseFileForCycleWithPackageFile1Text.setText(useFileForCycleWithPackageTmp[1]);
						
						stepDetailUseFileForCycleWithPackageFile1Text.setEnabled(true);
						stepDetailUseFileForCycleWithPackageFile1DeleteButton.setEnabled(true);
						stepDetailUseFileForCycleWithPackageMethodRadio1.setEnabled(false);
						stepDetailUseFileForCycleWithPackageMethodRadio2.setEnabled(false);
						stepDetailUseFileForCycleWithPackageFile2Text.setEnabled(false);
						stepDetailUseFileForCycleWithPackageFile2DeleteButton.setEnabled(false);
						
						
					}
					else if(useFileForCycleWithPackageTmp[0].equals("2"))
					{
					//	System.out.println(useFileForCycleWithPackageTmp[0]);
						stepDetailUseFileForCycleWithPackageFile1Text.setText(useFileForCycleWithPackageTmp[1]);
					//	System.out.println(useFileForCycleWithPackageTmp[1]);
						stepDetailUseFileForCycleWithPackageFile2Text.setText(useFileForCycleWithPackageTmp[2]);
					//	System.out.println(useFileForCycleWithPackageTmp[2]);
						
						stepDetailUseFileForCycleWithPackageFile1Text.setEnabled(true);
						stepDetailUseFileForCycleWithPackageFile1DeleteButton.setEnabled(true);
						stepDetailUseFileForCycleWithPackageMethodRadio1.setEnabled(true);
						stepDetailUseFileForCycleWithPackageMethodRadio2.setEnabled(true);
						stepDetailUseFileForCycleWithPackageFile2Text.setEnabled(true);
						stepDetailUseFileForCycleWithPackageFile2DeleteButton.setEnabled(true);
						
						
					//	System.out.println("stepDetailUseFileForCycleWithPackageMethodRadio1");
						if(useFileForCycleWithPackageTmp[3].equals("1"))
						{
							stepDetailUseFileForCycleWithPackageMethodRadio1.setSelected(true);
						}
						else if(useFileForCycleWithPackageTmp[3].equals("2"))
						{
							stepDetailUseFileForCycleWithPackageMethodRadio2.setSelected(true);
						}
						
						
						
					}
					else if(useFileForCycleWithPackageTmp[0].equals("0"))
					{
						stepDetailUseFileForCycleWithPackageFile1Text.setEnabled(false);
						stepDetailUseFileForCycleWithPackageFile1DeleteButton.setEnabled(false);
						stepDetailUseFileForCycleWithPackageMethodRadio1.setEnabled(false);
						stepDetailUseFileForCycleWithPackageMethodRadio2.setEnabled(false);
						stepDetailUseFileForCycleWithPackageFile2Text.setEnabled(false);
						stepDetailUseFileForCycleWithPackageFile2DeleteButton.setEnabled(false);
					}
				}
				else
				{
					stepDetailUseFileForCycleWithPackageFile1Text.setEnabled(false);
					stepDetailUseFileForCycleWithPackageFile1DeleteButton.setEnabled(false);
					stepDetailUseFileForCycleWithPackageMethodRadio1.setEnabled(false);
					stepDetailUseFileForCycleWithPackageMethodRadio2.setEnabled(false);
					stepDetailUseFileForCycleWithPackageFile2Text.setEnabled(false);
					stepDetailUseFileForCycleWithPackageFile2DeleteButton.setEnabled(false);
				}
			//	System.out.println(useFileForCycleWithPackageListCount);
				
				
				
				
					
			}
		});
 		
 		stepDetailUseFileForCycleWithPackageFile2DeleteButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				useFileForCycleWithPackageItem[0] = "1";
				useFileForCycleWithPackageItem[2] = "";
				useFileForCycleWithPackageItem[3] = "0";
				
				
				stepDetailUseFileForCycleWithPackageFile2Text.setText("");
				
				
				
				
				int useFileForCycleWithPackageListCount = useFileForCycleWithPackageList.size();
				String[] useFileForCycleWithPackageTmp = useFileForCycleWithPackageItem;
			//	System.out.println(useFileForCycleWithPackageTmp[0]+"useFileForCycleWithPackageTmp[0]");
				if(useFileForCycleWithPackageTmp[0]!=null&&!useFileForCycleWithPackageTmp[0].equals(null)&&useFileForCycleWithPackageTmp.length>1)
				{
					//System.out.println();
					if(useFileForCycleWithPackageTmp[0].equals("1"))
					{
					//	System.out.println(useFileForCycleWithPackageTmp[0]);
						//System.out.println();
						stepDetailUseFileForCycleWithPackageFile1Text.setText(useFileForCycleWithPackageTmp[1]);
						
						stepDetailUseFileForCycleWithPackageFile1Text.setEnabled(true);
						stepDetailUseFileForCycleWithPackageFile1DeleteButton.setEnabled(true);
						stepDetailUseFileForCycleWithPackageMethodRadio1.setEnabled(false);
						stepDetailUseFileForCycleWithPackageMethodRadio2.setEnabled(false);
						stepDetailUseFileForCycleWithPackageFile2Text.setEnabled(false);
						stepDetailUseFileForCycleWithPackageFile2DeleteButton.setEnabled(false);
						
						
					}
					else if(useFileForCycleWithPackageTmp[0].equals("2"))
					{
					//	System.out.println(useFileForCycleWithPackageTmp[0]);
						stepDetailUseFileForCycleWithPackageFile1Text.setText(useFileForCycleWithPackageTmp[1]);
						//System.out.println(useFileForCycleWithPackageTmp[1]);
						stepDetailUseFileForCycleWithPackageFile2Text.setText(useFileForCycleWithPackageTmp[2]);
					//	System.out.println(useFileForCycleWithPackageTmp[2]);
						
						stepDetailUseFileForCycleWithPackageFile1Text.setEnabled(true);
						stepDetailUseFileForCycleWithPackageFile1DeleteButton.setEnabled(true);
						stepDetailUseFileForCycleWithPackageMethodRadio1.setEnabled(true);
						stepDetailUseFileForCycleWithPackageMethodRadio2.setEnabled(true);
						stepDetailUseFileForCycleWithPackageFile2Text.setEnabled(true);
						stepDetailUseFileForCycleWithPackageFile2DeleteButton.setEnabled(true);
						
						
						//System.out.println("stepDetailUseFileForCycleWithPackageMethodRadio1");
						if(useFileForCycleWithPackageTmp[3].equals("1"))
						{
							stepDetailUseFileForCycleWithPackageMethodRadio1.setSelected(true);
						}
						else if(useFileForCycleWithPackageTmp[3].equals("2"))
						{
							stepDetailUseFileForCycleWithPackageMethodRadio2.setSelected(true);
						}
						
						
						
					}
					else if(useFileForCycleWithPackageTmp[0].equals("0"))
					{
						stepDetailUseFileForCycleWithPackageFile1Text.setEnabled(false);
						stepDetailUseFileForCycleWithPackageFile1DeleteButton.setEnabled(false);
						stepDetailUseFileForCycleWithPackageMethodRadio1.setEnabled(false);
						stepDetailUseFileForCycleWithPackageMethodRadio2.setEnabled(false);
						stepDetailUseFileForCycleWithPackageFile2Text.setEnabled(false);
						stepDetailUseFileForCycleWithPackageFile2DeleteButton.setEnabled(false);
					}
				}
				else
				{
					stepDetailUseFileForCycleWithPackageFile1Text.setEnabled(false);
					stepDetailUseFileForCycleWithPackageFile1DeleteButton.setEnabled(false);
					stepDetailUseFileForCycleWithPackageMethodRadio1.setEnabled(false);
					stepDetailUseFileForCycleWithPackageMethodRadio2.setEnabled(false);
					stepDetailUseFileForCycleWithPackageFile2Text.setEnabled(false);
					stepDetailUseFileForCycleWithPackageFile2DeleteButton.setEnabled(false);
				}
			//	System.out.println(useFileForCycleWithPackageListCount);
				
				
				
				
				
			}
		});
 		
 		
 		JButton stepDetailReSetButton = new JButton("ReSet");
 		stepDetailReSetButton.setFocusable(false);
 		stepDetailReSetButton.setSize(100,25);
 		stepDetailReSetButton.setLocation(670, 330);
 		stepDetailDialog.add(stepDetailReSetButton);
 		
 		
 		stepDetailReSetButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				//System.out.println("resourceStepStringresourceStepStringresourceStepString"+resourceStepString);
				
				host = resourceHost;
				port = resourcePort;
				siteURLLabel.setText("Target: " + host+":"+port);
				
				editorStepShowString = resourceStepString;
				EditorRequestDaraRawTextArea.setText(resourceStepString);
				//insertDocument(resourceStepString, Color.black);
				
				EditorStepReplaceHostPort.setLabel("CencleReplaceHostPort");
				replaceHostPort = true;
				
				EditorStepUsepreviousSetCookie.setLabel("CencleUsepreviousSetCookie");
				useSetCookieFromPrevious = true;
				
				randomStringList.clear();
				
				captchaList.clear();
				
				
				
				setGoAheadOrStopFromStatusList.clear();
				
				setGoAheadOrStopFromDataList.clear();
				
				GrobalFileInStep = -1;
				FileForCycleWithEXP = "";
				FileForCycleWithEXPStrings[0] = "";
				FileForCycleWithEXPStrings[1] = "-1";
				
				EditorStepUseFileForCycleWithExp.setLabel("UseFileForCycleWithExp");
				
				useFileForCycleWithPackageItem = new String[6];
				
				
				
				
				
				
				
				////////
				//System.out.println(replaceHostPort);
				if(replaceHostPort)
				{
					stepDetailReplaceHostPortRadioTrue.setSelected(true);
				}
				else
				{
					stepDetailReplaceHostPortRadioFalse.setSelected(true);
				}
			
			//	System.out.println(useSetCookieFromPrevious);
				if(useSetCookieFromPrevious)
				{
					stepDetailUsePreviousSetCookieRadioTrue.setSelected(true);
				}
				else
				{
					stepDetailUsePreviousSetCookieRadioFalse.setSelected(true);
				}
			
				int randomStringCount = randomStringList.size();
				stepDetailRandomStringComboBox.removeAllItems();
				for(int i=0;i<randomStringCount;i++)
				{
					stepDetailRandomStringComboBox.addItem(i);
					stepDetailRandomStringComboBox.invalidate();
				}
				if(randomStringCount>0)
				{
					stepDetailRandomStringComboBox.setSelectedIndex(0);
					stepDetailRandomStringComboBox.setEnabled(true);
					
			 		
			 		stepDetailRandomStringItemDigitsText.setEnabled(true);
			 		
			 		
			 		stepDetailRandomStringItemTypeComboBox.setEnabled(true);
			 		
			 		stepDetailRandomStringItemDeleteButton.setEnabled(true);
				}
				else
				{
					stepDetailRandomStringComboBox.setEnabled(false);
					
			 		
			 		stepDetailRandomStringItemDigitsText.setEnabled(false);
			 		
			 		
			 		stepDetailRandomStringItemTypeComboBox.setEnabled(false);
			 		
			 		stepDetailRandomStringItemDeleteButton.setEnabled(false);
				}
			//	System.out.println(randomStringCount);
			
				int captchaListCount = captchaList.size();
				stepDetailSetParamsFromCaptchaComboBox.removeAllItems();
				for(int i=0;i<captchaListCount;i++)
				{
					stepDetailSetParamsFromCaptchaComboBox.addItem(i);
					stepDetailSetParamsFromCaptchaComboBox.invalidate();
				}
				if(captchaListCount>0)
				{
					stepDetailSetParamsFromCaptchaComboBox.setSelectedIndex(0);
					
					
			 		stepDetailSetParamsFromCaptchaComboBox.setEnabled(true);		 		
			 		
			 		stepDetailSetParamsFromCaptchaDigitsText.setEnabled(true);
			 		
			 		stepDetailSetParamsFromCaptchaBaseURLText.setEnabled(true);
			 		
			 		stepDetailSetParamsFromCaptchaDeleteButton.setEnabled(true);
					
				}
				else
				{
					stepDetailSetParamsFromCaptchaComboBox.setEnabled(false);		 		
			 		
			 		stepDetailSetParamsFromCaptchaDigitsText.setEnabled(false);
			 		
			 		stepDetailSetParamsFromCaptchaBaseURLText.setEnabled(false);
			 		
			 		stepDetailSetParamsFromCaptchaDeleteButton.setEnabled(false);
				}
				//System.out.println(captchaListCount);
				

		  
				int setParamsFromDataPackageListCount = setParamsFromDataPackageList.size();
				
				//System.out.println(setParamsFromDataPackageListCount);

				
				
				
				int setGoAheadOrStopFromStatusListCount = setGoAheadOrStopFromStatusList.size();
				stepDetailSetGoaheadOrStopFromResponseStatusNumsComboBox.removeAllItems();
				for(int i=0;i<setGoAheadOrStopFromStatusListCount;i++)
				{
					stepDetailSetGoaheadOrStopFromResponseStatusNumsComboBox.addItem(i);
					stepDetailSetGoaheadOrStopFromResponseStatusNumsComboBox.invalidate();
				}
				if(setGoAheadOrStopFromStatusListCount>0)
				{
					stepDetailSetGoaheadOrStopFromResponseStatusNumsComboBox.setSelectedIndex(0);
					
					stepDetailSetGoaheadOrStopFromResponseStatusNumsComboBox.setEnabled(true);
			 		
			 		stepDetailSetGoaheadOrStopFromResponseStatusStatusText.setEnabled(true);
			 		
			 		stepDetailSetGoaheadOrStopFromResponseStatusActionComboBox.setEnabled(true);
			 		
			 		stepDetailSetGoaheadOrStopFromResponseStatusDeleteButton.setEnabled(true);
					
					
				}
				else
				{
					stepDetailSetGoaheadOrStopFromResponseStatusNumsComboBox.setEnabled(false);
			 		
			 		stepDetailSetGoaheadOrStopFromResponseStatusStatusText.setEnabled(false);
			 		
			 		stepDetailSetGoaheadOrStopFromResponseStatusActionComboBox.setEnabled(false);
			 		
			 		stepDetailSetGoaheadOrStopFromResponseStatusDeleteButton.setEnabled(false);
				}
				//System.out.println(setGoAheadOrStopFromStatusListCount);
			
				
				
				int setGoAheadOrStopFromDataListCount = setGoAheadOrStopFromDataList.size();
				stepDetailSetGoaheadOrStopFromResponseDataNumsComboBox.removeAllItems();
				for(int i=0;i<setGoAheadOrStopFromDataListCount;i++)
				{
					stepDetailSetGoaheadOrStopFromResponseDataNumsComboBox.addItem(i);
					stepDetailSetGoaheadOrStopFromResponseDataNumsComboBox.invalidate();
				}
				if(setGoAheadOrStopFromDataListCount>0)
				{
					stepDetailSetGoaheadOrStopFromResponseDataNumsComboBox.setSelectedIndex(0);
					
					stepDetailSetGoaheadOrStopFromResponseDataNumsComboBox.setEnabled(true);
			 		
			 		stepDetailSetGoaheadOrStopFromResponseDataDataText.setEnabled(true);
			 		
			 		stepDetailSetGoaheadOrStopFromResponseDataActionComboBox.setEnabled(true);
			 		
			 		stepDetailSetGoaheadOrStopFromResponseDataDeleteButton.setEnabled(true);
					
				}
				else
				{
					
					
					stepDetailSetGoaheadOrStopFromResponseDataNumsComboBox.setEnabled(false);
			 		
			 		stepDetailSetGoaheadOrStopFromResponseDataDataText.setEnabled(false);
			 		
			 		stepDetailSetGoaheadOrStopFromResponseDataActionComboBox.setEnabled(false);
			 		
			 		stepDetailSetGoaheadOrStopFromResponseDataDeleteButton.setEnabled(false);
				}
			//	System.out.println(setGoAheadOrStopFromDataListCount);

				int useFileForCycleWithPackageListCount = useFileForCycleWithPackageList.size();
				String[] useFileForCycleWithPackageTmp = useFileForCycleWithPackageItem;
			//	System.out.println(useFileForCycleWithPackageTmp[0]+"useFileForCycleWithPackageTmp[0]");
				if(useFileForCycleWithPackageTmp[0]!=null&&!useFileForCycleWithPackageTmp[0].equals(null)&&useFileForCycleWithPackageTmp.length>1)
				{
					//System.out.println();
					if(useFileForCycleWithPackageTmp[0].equals("1"))
					{
						System.out.println(useFileForCycleWithPackageTmp[0]);
						//System.out.println();
						stepDetailUseFileForCycleWithPackageFile1Text.setText(useFileForCycleWithPackageTmp[1]);
						
						stepDetailUseFileForCycleWithPackageFile1Text.setEnabled(true);
						stepDetailUseFileForCycleWithPackageFile1DeleteButton.setEnabled(true);
						stepDetailUseFileForCycleWithPackageMethodRadio1.setEnabled(false);
						stepDetailUseFileForCycleWithPackageMethodRadio2.setEnabled(false);
						stepDetailUseFileForCycleWithPackageFile2Text.setEnabled(false);
						stepDetailUseFileForCycleWithPackageFile2DeleteButton.setEnabled(false);
						
						
					}
					else if(useFileForCycleWithPackageTmp[0].equals("2"))
					{
						//System.out.println(useFileForCycleWithPackageTmp[0]);
						stepDetailUseFileForCycleWithPackageFile1Text.setText(useFileForCycleWithPackageTmp[1]);
						//System.out.println(useFileForCycleWithPackageTmp[1]);
						stepDetailUseFileForCycleWithPackageFile2Text.setText(useFileForCycleWithPackageTmp[2]);
						//System.out.println(useFileForCycleWithPackageTmp[2]);
						
						stepDetailUseFileForCycleWithPackageFile1Text.setEnabled(true);
						stepDetailUseFileForCycleWithPackageFile1DeleteButton.setEnabled(true);
						stepDetailUseFileForCycleWithPackageMethodRadio1.setEnabled(true);
						stepDetailUseFileForCycleWithPackageMethodRadio2.setEnabled(true);
						stepDetailUseFileForCycleWithPackageFile2Text.setEnabled(true);
						stepDetailUseFileForCycleWithPackageFile2DeleteButton.setEnabled(true);
						
						
						//System.out.println("stepDetailUseFileForCycleWithPackageMethodRadio1");
						if(useFileForCycleWithPackageTmp[3].equals("1"))
						{
							stepDetailUseFileForCycleWithPackageMethodRadio1.setSelected(true);
						}
						else if(useFileForCycleWithPackageTmp[3].equals("2"))
						{
							stepDetailUseFileForCycleWithPackageMethodRadio2.setSelected(true);
						}
						
						
						
					}
					else if(useFileForCycleWithPackageTmp[0].equals("0"))
					{
						stepDetailUseFileForCycleWithPackageFile1Text.setEnabled(false);
						stepDetailUseFileForCycleWithPackageFile1DeleteButton.setEnabled(false);
						stepDetailUseFileForCycleWithPackageMethodRadio1.setEnabled(false);
						stepDetailUseFileForCycleWithPackageMethodRadio2.setEnabled(false);
						stepDetailUseFileForCycleWithPackageFile2Text.setEnabled(false);
						stepDetailUseFileForCycleWithPackageFile2DeleteButton.setEnabled(false);
					}
				}
				else
				{
					stepDetailUseFileForCycleWithPackageFile1Text.setEnabled(false);
					stepDetailUseFileForCycleWithPackageFile1DeleteButton.setEnabled(false);
					stepDetailUseFileForCycleWithPackageMethodRadio1.setEnabled(false);
					stepDetailUseFileForCycleWithPackageMethodRadio2.setEnabled(false);
					stepDetailUseFileForCycleWithPackageFile2Text.setEnabled(false);
					stepDetailUseFileForCycleWithPackageFile2DeleteButton.setEnabled(false);
				}
				//System.out.println(useFileForCycleWithPackageListCount);

				
				//System.out.println("______________-");
				if(FileForCycleWithEXP.length()>0)
				{
					stepDetailUseFileForCycleWithExpText.setText(FileForCycleWithEXP);
				//	System.out.println("Step in "+GrobalFileInStep);
					stepDetailUseFileForCycleWithExpInStepLabel.setText("In Step"+(GrobalFileInStep-1));
					
					
					stepDetailUseFileForCycleWithExpText.setEnabled(true);
			 		
			 		stepDetailUseFileForCycleWithExpDeleteButton.setEnabled(true);
					
					
				}
				else
				{
					stepDetailUseFileForCycleWithExpText.setText("");
					stepDetailUseFileForCycleWithExpInStepLabel.setText("In Step");
					
					stepDetailUseFileForCycleWithExpText.setEnabled(false);
			 		
			 		stepDetailUseFileForCycleWithExpDeleteButton.setEnabled(false);
					
				}
				//System.out.println(FileForCycleWithEXP+"__________"+GrobalFileInStep);
		  
				
				stepDetailDialog.setVisible(true);
				
				
				
				
				
				
				
				
				
				
			}
		});
 		
 		stepDetail.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				if(!stepDetailDialog.isVisible())
				{
					
				//	System.out.println(replaceHostPort);
					if(replaceHostPort)
					{
						stepDetailReplaceHostPortRadioTrue.setSelected(true);
					}
					else
					{
						stepDetailReplaceHostPortRadioFalse.setSelected(true);
					}
				
					//System.out.println(useSetCookieFromPrevious);
					if(useSetCookieFromPrevious)
					{
						stepDetailUsePreviousSetCookieRadioTrue.setSelected(true);
					}
					else
					{
						stepDetailUsePreviousSetCookieRadioFalse.setSelected(true);
					}
				
					int randomStringCount = randomStringList.size();
					stepDetailRandomStringComboBox.removeAllItems();
					for(int i=0;i<randomStringCount;i++)
					{
						stepDetailRandomStringComboBox.addItem(i);
						stepDetailRandomStringComboBox.invalidate();
					}
					if(randomStringCount>0)
					{
						stepDetailRandomStringComboBox.setSelectedIndex(0);
						stepDetailRandomStringComboBox.setEnabled(true);
						
				 		
				 		stepDetailRandomStringItemDigitsText.setEnabled(true);
				 		
				 		
				 		stepDetailRandomStringItemTypeComboBox.setEnabled(true);
				 		
				 		stepDetailRandomStringItemDeleteButton.setEnabled(true);
				 		
				 		
				 		
					}
					else
					{
						stepDetailRandomStringComboBox.setEnabled(false);
						
				 		
				 		stepDetailRandomStringItemDigitsText.setEnabled(false);
				 		
				 		
				 		stepDetailRandomStringItemTypeComboBox.setEnabled(false);
				 		
				 		stepDetailRandomStringItemDeleteButton.setEnabled(false);
					}
					//System.out.println(randomStringCount);
				
					int captchaListCount = captchaList.size();
					stepDetailSetParamsFromCaptchaComboBox.removeAllItems();
					for(int i=0;i<captchaListCount;i++)
					{
						stepDetailSetParamsFromCaptchaComboBox.addItem(i);
						stepDetailSetParamsFromCaptchaComboBox.invalidate();
					}
					if(captchaListCount>0)
					{
						stepDetailSetParamsFromCaptchaComboBox.setSelectedIndex(0);
						
						
				 		stepDetailSetParamsFromCaptchaComboBox.setEnabled(true);		 		
				 		
				 		stepDetailSetParamsFromCaptchaDigitsText.setEnabled(true);
				 		
				 		stepDetailSetParamsFromCaptchaBaseURLText.setEnabled(true);
				 		
				 		stepDetailSetParamsFromCaptchaDeleteButton.setEnabled(true);
						
					}
					else
					{
						stepDetailSetParamsFromCaptchaComboBox.setEnabled(false);		 		
				 		
				 		stepDetailSetParamsFromCaptchaDigitsText.setEnabled(false);
				 		
				 		stepDetailSetParamsFromCaptchaBaseURLText.setEnabled(false);
				 		
				 		stepDetailSetParamsFromCaptchaDeleteButton.setEnabled(false);
					}
					//System.out.println(captchaListCount);
					

			  
					int setParamsFromDataPackageListCount = setParamsFromDataPackageList.size();
					
				//	System.out.println(setParamsFromDataPackageListCount);

					
					
					
					int setGoAheadOrStopFromStatusListCount = setGoAheadOrStopFromStatusList.size();
					stepDetailSetGoaheadOrStopFromResponseStatusNumsComboBox.removeAllItems();
					for(int i=0;i<setGoAheadOrStopFromStatusListCount;i++)
					{
						stepDetailSetGoaheadOrStopFromResponseStatusNumsComboBox.addItem(i);
						stepDetailSetGoaheadOrStopFromResponseStatusNumsComboBox.invalidate();
					}
					if(setGoAheadOrStopFromStatusListCount>0)
					{
						stepDetailSetGoaheadOrStopFromResponseStatusNumsComboBox.setSelectedIndex(0);
						
						stepDetailSetGoaheadOrStopFromResponseStatusNumsComboBox.setEnabled(true);
				 		
				 		stepDetailSetGoaheadOrStopFromResponseStatusStatusText.setEnabled(true);
				 		
				 		stepDetailSetGoaheadOrStopFromResponseStatusActionComboBox.setEnabled(true);
				 		
				 		stepDetailSetGoaheadOrStopFromResponseStatusDeleteButton.setEnabled(true);
						
						
					}
					else
					{
						stepDetailSetGoaheadOrStopFromResponseStatusNumsComboBox.setEnabled(false);
				 		
				 		stepDetailSetGoaheadOrStopFromResponseStatusStatusText.setEnabled(false);
				 		
				 		stepDetailSetGoaheadOrStopFromResponseStatusActionComboBox.setEnabled(false);
				 		
				 		stepDetailSetGoaheadOrStopFromResponseStatusDeleteButton.setEnabled(false);
					}
					//System.out.println(setGoAheadOrStopFromStatusListCount);
				
					
					
					int setGoAheadOrStopFromDataListCount = setGoAheadOrStopFromDataList.size();
					stepDetailSetGoaheadOrStopFromResponseDataNumsComboBox.removeAllItems();
					for(int i=0;i<setGoAheadOrStopFromDataListCount;i++)
					{
						stepDetailSetGoaheadOrStopFromResponseDataNumsComboBox.addItem(i);
						stepDetailSetGoaheadOrStopFromResponseDataNumsComboBox.invalidate();
					}
					if(setGoAheadOrStopFromDataListCount>0)
					{
						stepDetailSetGoaheadOrStopFromResponseDataNumsComboBox.setSelectedIndex(0);
						
						stepDetailSetGoaheadOrStopFromResponseDataNumsComboBox.setEnabled(true);
				 		
				 		stepDetailSetGoaheadOrStopFromResponseDataDataText.setEnabled(true);
				 		
				 		stepDetailSetGoaheadOrStopFromResponseDataActionComboBox.setEnabled(true);
				 		
				 		stepDetailSetGoaheadOrStopFromResponseDataDeleteButton.setEnabled(true);
						
					}
					else
					{
						
						
						stepDetailSetGoaheadOrStopFromResponseDataNumsComboBox.setEnabled(false);
				 		
				 		stepDetailSetGoaheadOrStopFromResponseDataDataText.setEnabled(false);
				 		
				 		stepDetailSetGoaheadOrStopFromResponseDataActionComboBox.setEnabled(false);
				 		
				 		stepDetailSetGoaheadOrStopFromResponseDataDeleteButton.setEnabled(false);
					}
					//System.out.println(setGoAheadOrStopFromDataListCount);

					int useFileForCycleWithPackageListCount = useFileForCycleWithPackageList.size();
					String[] useFileForCycleWithPackageTmp = useFileForCycleWithPackageItem;
				//	System.out.println(useFileForCycleWithPackageTmp[0]+"useFileForCycleWithPackageTmp[0]");
					if(useFileForCycleWithPackageTmp[0]!=null&&!useFileForCycleWithPackageTmp[0].equals(null)&&useFileForCycleWithPackageTmp.length>1)
					{
						//System.out.println();
						if(useFileForCycleWithPackageTmp[0].equals("1"))
						{
							System.out.println(useFileForCycleWithPackageTmp[0]);
							//System.out.println();
							stepDetailUseFileForCycleWithPackageFile1Text.setText(useFileForCycleWithPackageTmp[1]);
							
							stepDetailUseFileForCycleWithPackageFile1Text.setEnabled(true);
							stepDetailUseFileForCycleWithPackageFile1DeleteButton.setEnabled(true);
							stepDetailUseFileForCycleWithPackageMethodRadio1.setEnabled(false);
							stepDetailUseFileForCycleWithPackageMethodRadio2.setEnabled(false);
							stepDetailUseFileForCycleWithPackageFile2Text.setEnabled(false);
							stepDetailUseFileForCycleWithPackageFile2DeleteButton.setEnabled(false);
							
							
						}
						else if(useFileForCycleWithPackageTmp[0].equals("2"))
						{
							//System.out.println(useFileForCycleWithPackageTmp[0]);
							stepDetailUseFileForCycleWithPackageFile1Text.setText(useFileForCycleWithPackageTmp[1]);
							//System.out.println(useFileForCycleWithPackageTmp[1]);
							stepDetailUseFileForCycleWithPackageFile2Text.setText(useFileForCycleWithPackageTmp[2]);
							//System.out.println(useFileForCycleWithPackageTmp[2]);
							
							stepDetailUseFileForCycleWithPackageFile1Text.setEnabled(true);
							stepDetailUseFileForCycleWithPackageFile1DeleteButton.setEnabled(true);
							stepDetailUseFileForCycleWithPackageMethodRadio1.setEnabled(true);
							stepDetailUseFileForCycleWithPackageMethodRadio2.setEnabled(true);
							stepDetailUseFileForCycleWithPackageFile2Text.setEnabled(true);
							stepDetailUseFileForCycleWithPackageFile2DeleteButton.setEnabled(true);
							
							
							//System.out.println("stepDetailUseFileForCycleWithPackageMethodRadio1");
							if(useFileForCycleWithPackageTmp[3].equals("1"))
							{
								stepDetailUseFileForCycleWithPackageMethodRadio1.setSelected(true);
							}
							else if(useFileForCycleWithPackageTmp[3].equals("2"))
							{
								stepDetailUseFileForCycleWithPackageMethodRadio2.setSelected(true);
							}
							
							
							
						}
						else if(useFileForCycleWithPackageTmp[0].equals("0"))
						{
							stepDetailUseFileForCycleWithPackageFile1Text.setEnabled(false);
							stepDetailUseFileForCycleWithPackageFile1DeleteButton.setEnabled(false);
							stepDetailUseFileForCycleWithPackageMethodRadio1.setEnabled(false);
							stepDetailUseFileForCycleWithPackageMethodRadio2.setEnabled(false);
							stepDetailUseFileForCycleWithPackageFile2Text.setEnabled(false);
							stepDetailUseFileForCycleWithPackageFile2DeleteButton.setEnabled(false);
						}
					}
					else
					{
						stepDetailUseFileForCycleWithPackageFile1Text.setEnabled(false);
						stepDetailUseFileForCycleWithPackageFile1DeleteButton.setEnabled(false);
						stepDetailUseFileForCycleWithPackageMethodRadio1.setEnabled(false);
						stepDetailUseFileForCycleWithPackageMethodRadio2.setEnabled(false);
						stepDetailUseFileForCycleWithPackageFile2Text.setEnabled(false);
						stepDetailUseFileForCycleWithPackageFile2DeleteButton.setEnabled(false);
					}
					//System.out.println(useFileForCycleWithPackageListCount);

					
					//System.out.println("______________-");
					if(FileForCycleWithEXP.length()>0)
					{
						stepDetailUseFileForCycleWithExpText.setText(FileForCycleWithEXP);
						//System.out.println("Step in "+GrobalFileInStep);
						stepDetailUseFileForCycleWithExpInStepLabel.setText("In Step"+(GrobalFileInStep-1));
						
						
						stepDetailUseFileForCycleWithExpText.setEnabled(true);
				 		
				 		stepDetailUseFileForCycleWithExpDeleteButton.setEnabled(true);
						
						
					}
					else
					{
						stepDetailUseFileForCycleWithExpText.setText("");
						stepDetailUseFileForCycleWithExpInStepLabel.setText("In Step");
						
						stepDetailUseFileForCycleWithExpText.setEnabled(false);
				 		
				 		stepDetailUseFileForCycleWithExpDeleteButton.setEnabled(false);
						
					}
					//System.out.println(FileForCycleWithEXP+"__________"+GrobalFileInStep);
					stepDetailDialog.setTitle(currentExpName);
					stepDetailDialog.setVisible(true);
					
				}
			}
		});

 		final JDialog siteEditDialog = new JDialog();//这个是编辑 host 和port 的 dialog
 		siteEditDialog.setAlwaysOnTop(true);
 		siteEditDialog.setLayout(null);
		siteEditDialog.setLocation(100, 200);
		siteEditDialog.setSize(300, 180);
			
		JLabel hostLabel = new JLabel("Host :");
		hostLabel.setLocation(7, 7);
		hostLabel.setSize(50, 30);
		final JTextField hostText = new JTextField(host);
		hostText.setLocation(50, 12);
		hostText.setSize(200, 25);

		JLabel portLabel = new JLabel("Port :");
		portLabel.setLocation(7, 50);
		portLabel.setSize(50, 30);
		final JTextField portText = new JTextField(String.valueOf(port));
		portText.setSize(50, 25);
		portText.setLocation(50, 50);

		JButton OKButton = new JButton("OK");
		OKButton.setSize(80, 25);
		OKButton.setLocation(150, 90);

		OKButton.addActionListener(new ActionListener() //编辑host 和 port 的ok 按钮
		{
			public void actionPerformed(ActionEvent e)
			{
				String hostStr = hostText.getText();
				try
				{
					int portInt = Integer.valueOf(portText.getText()).intValue();
					//System.out.println(hostStr + ":" + portInt);
					if ((hostStr.length() > 0) && (portInt > 0))
					{
						host = hostStr;
						port = portInt;
						siteURLLabel.setText("Target: " + host+":"+port);
						siteEditDialog.setVisible(false);
					}
					else
					{
						JOptionPane.showMessageDialog(null, "请输入正确的主机和端口", "Warning", 2);
					}
				}
				catch (Exception eg)
				{
					JOptionPane.showMessageDialog(null, "请输入正确的主机和端口", "Warning", 2);
				}
			}
		});
				
				
				
		siteEditDialog.add(hostText);
		siteEditDialog.add(hostLabel);
		siteEditDialog.add(portText);
		siteEditDialog.add(portLabel);
		siteEditDialog.add(OKButton);	
 		siteEditButton.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent e)
 			{
 				//System.out.println("应该跳出host：port 编辑框"+host+":"+port);
 				
 				siteEditDialog.setVisible(true);

 				
 			}
 		});
 		
 		//Editor Step 的go 按钮
 		GOButton.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent e)
 			{
 				if ((host != null) && (!host.equals(null)) && (host.length() > 0) && (port > 0)) //首先判断 host 和port 是否存在
 				{
 					Thread ThreadGetResponseData = new Thread(new Runnable()//请求的 线程
 					{
 						public void run()
 						{
 							GOButton.setEnabled(false);
 							CencleButton.setEnabled(true);
 							EditorResponseDaraRawTextArea.setText("");//清空 response的数据

 							responseDataString = Method.bytesToString(Method.getHttPResponseData(host, port, editorStepShowString));// 请求 获取 responsedata
 							EditorResponseDaraRawTextArea.setText(responseDataString);//显示 responsedata

 							DefaultTableModel ResponsetableModel = (DefaultTableModel)JPtabEditorResponseHeadersTable.getModel();//editor response header
 							ResponsetableModel.setRowCount(0); //清空

 							DefaultTableModel JPtabEditorResponseHexTableModel = (DefaultTableModel)JPtabEditorResponseHexTable.getModel();//editor response hex
 							JPtabEditorResponseHexTableModel.setRowCount(0);//清空

 							if ((responseDataString != null) && (!responseDataString.equals(null)) && (responseDataString.length() != 0))//response data 存在 才进行分解  获取各个header 参数
 							{
 								//获取参数列表  head
 								String[] ResponseheaderParams = Method.getHTTPHeadersParams(responseDataString);
 								//参数分为参数名和参数值   长度为1/2
 								int ResponseheaderParamLength = ResponseheaderParams.length / 2;
 								//添加到 table 上
 								for (int i = 0; i < ResponseheaderParamLength; i++)
 								{
 									if (ResponseheaderParams[(i * 2)].length() > 0)
 									{
 										ResponsetableModel.addRow(new Object[] { ResponseheaderParams[(i * 2)], ResponseheaderParams[(i * 2 + 1)] });
 									}
 								}
 								JPtabEditorResponseHeadersTable.invalidate();//更新

 								String[] EditorResponseDataHexStrings = Method.bytesToHexStrings(responseDataString.getBytes());//把responsedata 转化为 hex 数据
 								int EditorResponseDataHexStringsLength = EditorResponseDataHexStrings.length;//获取长度
 								int EditorResponseDataHexStringsRowNum = EditorResponseDataHexStringsLength / 16;//分割为16个字符一行
 								int EditorResponseDataHexStringsRealRowNum = 0;//上面那个没有判断是不是整除， 整除比不整除少一行
 								if (EditorResponseDataHexStringsLength % 16 == 0)
 								{
 									EditorResponseDataHexStringsRealRowNum = EditorResponseDataHexStringsRowNum;
 								}
 								else
 								{
 									EditorResponseDataHexStringsRealRowNum = EditorResponseDataHexStringsRowNum + 1;
 								}
 								//每一行数据放在 一个【】 里面
 								String[] EditorResponseDataHexStringsRealRowStrings = new String[EditorResponseDataHexStringsRealRowNum];
 								String EditorResponseDataHexStringsRealRowStringTmp = "";//每16个16进制数据为一行，旁边还有一个显示 相对应的字符串
 								char[] EditorResponseStringchars = responseDataString.toCharArray();
 								int EditorResponseStringcharsLength = EditorResponseStringchars.length;
 								int i = 1; for (int j = 0; (i <= EditorResponseStringcharsLength) && (j < EditorResponseDataHexStringsRealRowNum); i++)
 								{
 									EditorResponseDataHexStringsRealRowStringTmp = EditorResponseDataHexStringsRealRowStringTmp + EditorResponseStringchars[(i - 1)];
 									if (i % 16 == 0)
 									{
 										EditorResponseDataHexStringsRealRowStrings[j] = EditorResponseDataHexStringsRealRowStringTmp;
 										EditorResponseDataHexStringsRealRowStringTmp = "";
 										j++;
 									}
 								}
 								EditorResponseDataHexStringsRealRowStrings[(EditorResponseDataHexStringsRealRowNum - 1)] = EditorResponseDataHexStringsRealRowStringTmp;
 								
 								for (int k = 0; k < EditorResponseDataHexStringsRowNum; k++)
 								{
 									JPtabEditorResponseHexTableModel.addRow(new Object[] { Integer.valueOf(k + 1), EditorResponseDataHexStrings[(k * 16)], EditorResponseDataHexStrings[(k * 16 + 1)], EditorResponseDataHexStrings[(k * 16 + 2)], EditorResponseDataHexStrings[(k * 16 + 3)], EditorResponseDataHexStrings[(k * 16 + 4)], EditorResponseDataHexStrings[(k * 16 + 5)], EditorResponseDataHexStrings[(k * 16 + 6)], EditorResponseDataHexStrings[(k * 16 + 7)], EditorResponseDataHexStrings[(k * 16 + 8)], EditorResponseDataHexStrings[(k * 16 + 9)], EditorResponseDataHexStrings[(k * 16 + 10)], EditorResponseDataHexStrings[(k * 16 + 11)], EditorResponseDataHexStrings[(k * 16 + 12)], EditorResponseDataHexStrings[(k * 16 + 13)], EditorResponseDataHexStrings[(k * 16 + 14)], EditorResponseDataHexStrings[(k * 16 + 15)], EditorResponseDataHexStringsRealRowStrings[k] });
 								}

 								String[] EditorResponseHexStringsTmp = { String.valueOf(EditorResponseDataHexStringsRealRowNum), "--", "--", "--", "--", "--", "--", "--", "--", "--", "--", "--", "--", "--", "--", "--", "--", EditorResponseDataHexStringsRealRowStrings[(EditorResponseDataHexStringsRealRowNum - 1)] };
 								int EditorResponseDataHexStringsSHENGYUGESHU = EditorResponseDataHexStringsLength % 16;
 								for (int k = 0; k < EditorResponseDataHexStringsSHENGYUGESHU; k++)
 								{
 									EditorResponseHexStringsTmp[(k + 1)] = EditorResponseDataHexStrings[(EditorResponseDataHexStringsRowNum * 16 + k)];
 								}
 								if (EditorResponseDataHexStringsLength % 16 != 0)
 								{
 									JPtabEditorResponseHexTableModel.addRow(EditorResponseHexStringsTmp);
 									JPtabEditorResponseHexTable.invalidate();
 								}
 							}
 							GOButton.setEnabled(true);
 							CencleButton.setEnabled(false);
 						}
 					});
 					ThreadGetResponseData.start();
 				}
 				else
 				{
 					siteEditDialog.setVisible(true);
 				}
 			}
 		});
 		
 		JLabel RequestLabel = new JLabel("Request ");
 		RequestLabel.setSize(new Dimension(100, 20));
 		RequestLabel.setLocation(10, 40);
 		ButtonPanel.add(RequestLabel);

 		JPanel RequestResponseDataPanel = new JPanel();

 		RequestResponseDataPanel.setLayout(new BorderLayout());
 		add(RequestResponseDataPanel, "Center");
 		
 		JTabbedPane RequestTabPanel = new JTabbedPane(1);
 		RequestTabPanel.setPreferredSize(new Dimension(0, 250));

 		JDesktopPane EditorRequestDataRawPane = new JDesktopPane();
 		EditorRequestDataRawPane.setFocusable(false);
 		EditorRequestDataRawPane.setLayout(new BorderLayout());
 		RequestTabPanel.add(EditorRequestDataRawPane, "Raw");

 		EditorRequestDaraRawTextArea = new JTextPane();
 		//EditorRequestDaraRawTextArea.setLineWrap(true);

 		JScrollPane EditorRequestDaraRawScrollPane = new JScrollPane(EditorRequestDaraRawTextArea);
 		EditorRequestDataRawPane.add(EditorRequestDaraRawScrollPane, "Center");
    
 		EditorStepReplaceHostPort = new MenuItem();
 		EditorStepReplaceHostPort.setLabel("CencleReplaceHostPort");

 		EditorStepUsepreviousSetCookie = new MenuItem();
 		EditorStepUsepreviousSetCookie.setLabel("CencleUsepreviousSetCookie");

 		final PopupMenu EditorStepPopupMenu = new PopupMenu();
 		MenuItem EditorStepRandomStringmenuItem = new MenuItem();
 		EditorStepRandomStringmenuItem.setLabel("ReplaceRandomStrings");

 		EditorStepUseFileForCycleWithExp = new MenuItem();
 		EditorStepUseFileForCycleWithExp.setLabel("UseFileForCycleWithExp");

 		MenuItem EditorStepSetGoaheadOrStopFromResponseStatus = new MenuItem();
 		EditorStepSetGoaheadOrStopFromResponseStatus.setLabel("SetGoaheadOrStopFromResponseStatus");

 		MenuItem EditorStepSetGoaheadOrStopFromResponseData = new MenuItem();
 		EditorStepSetGoaheadOrStopFromResponseData.setLabel("SetGoaheadOrStopFromResponseData");

 		MenuItem EditorStepSetParamsFromDataPackage = new MenuItem();
 		EditorStepSetParamsFromDataPackage.setLabel("SetParamsFromDataPackage");

 		MenuItem EditorStepSetParamsFromCaptcha = new MenuItem();
 		EditorStepSetParamsFromCaptcha.setLabel("SetParamsFromCaptcha");

 		MenuItem EditorStepUseFileForCycleWithPackage = new MenuItem();
 		EditorStepUseFileForCycleWithPackage.setLabel("UseFileForCycleWithPackage");

 		final MenuItem EditorStepUseFileForCycleWithPackage1 = new MenuItem();
 		EditorStepUseFileForCycleWithPackage1.setLabel("UseFile1ForCycleWithPackage");

 		final MenuItem EditorStepUseFileForCycleWithPackage2 = new MenuItem();
 		EditorStepUseFileForCycleWithPackage2.setLabel("UseFile2ForCycleWithPackage");
 		
 		MenuItem EditorStepReSetItem = new MenuItem();
 		EditorStepReSetItem.setLabel("ReSet");
 		
 		EditorStepReSetItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				//System.out.println("resourceStepStringresourceStepStringresourceStepString"+resourceStepString);
				
				host = resourceHost;
				port = resourcePort;
				siteURLLabel.setText("Target: " + host+":"+port);
				
				editorStepShowString = resourceStepString;
				EditorRequestDaraRawTextArea.setText(resourceStepString);
				//insertDocument(resourceStepString, Color.black);
				
				EditorStepReplaceHostPort.setLabel("CencleReplaceHostPort");
				replaceHostPort = true;
				
				EditorStepUsepreviousSetCookie.setLabel("CencleUsepreviousSetCookie");
				useSetCookieFromPrevious = true;
				
				randomStringList.clear();
				
				captchaList.clear();
				
				
				
				setGoAheadOrStopFromStatusList.clear();
				
				setGoAheadOrStopFromDataList.clear();
				
				GrobalFileInStep = -1;
				FileForCycleWithEXP = "";
				FileForCycleWithEXPStrings[0] = "";
				FileForCycleWithEXPStrings[1] = "-1";
				
				EditorStepUseFileForCycleWithExp.setLabel("UseFileForCycleWithExp");
				
				useFileForCycleWithPackageItem = new String[6];
				
				
				
			}
		});

 		final JDialog EditorStepRandomStringmenuItemDialog = new JDialog();

 		EditorStepRandomStringmenuItem.addActionListener(new ActionListener() {
 			int RandomStringType = 2;

 			public void actionPerformed(ActionEvent e)
 			{
 				EditorStepRandomStringmenuItemDialog.setLayout(null);
 				EditorStepRandomStringmenuItemDialog.setSize(new Dimension(500, 120));
 				EditorStepRandomStringmenuItemDialog.setVisible(true);
 				EditorStepRandomStringmenuItemDialog.setLocation(100, 200);
 				EditorStepRandomStringmenuItemDialog.setAlwaysOnTop(true);

 				JRadioButton EditorStepRandomStringmenuItemNumbers = new JRadioButton("Numbers");
 				EditorStepRandomStringmenuItemNumbers.setLocation(7, 7);
 				EditorStepRandomStringmenuItemNumbers.setSize(80, 30);
 				EditorStepRandomStringmenuItemNumbers.setFocusable(false);

 				JRadioButton EditorStepRandomStringmenuItemStrings = new JRadioButton("Strings");
 				EditorStepRandomStringmenuItemStrings.setLocation(90, 7);
 				EditorStepRandomStringmenuItemStrings.setSize(75, 30);
 				EditorStepRandomStringmenuItemStrings.setFocusable(false);

 				JRadioButton EditorStepRandomStringmenuItemNumbersAndStrings = new JRadioButton("NumStrs");
 				EditorStepRandomStringmenuItemNumbersAndStrings.setLocation(160, 7);
 				EditorStepRandomStringmenuItemNumbersAndStrings.setSize(80, 30);
 				EditorStepRandomStringmenuItemNumbersAndStrings.setSelected(true);
 				EditorStepRandomStringmenuItemNumbersAndStrings.setFocusable(false);

 				JRadioButton EditorStepRandomStringmenuItemSelfDefine = new JRadioButton("Define:");
 				EditorStepRandomStringmenuItemSelfDefine.setFocusable(false);
 				EditorStepRandomStringmenuItemSelfDefine.setLocation(245, 7);
 				EditorStepRandomStringmenuItemSelfDefine.setSize(70, 30);

 				final JTextField EditorStepRandomStringmenuItemSelfDefineString = new JTextField("cqwer1234");
 				EditorStepRandomStringmenuItemSelfDefineString.setEditable(false);
 				EditorStepRandomStringmenuItemSelfDefineString.setLocation(315, 7);
 				EditorStepRandomStringmenuItemSelfDefineString.setSize(100, 30);

 				EditorStepRandomStringmenuItemNumbers.addActionListener(new ActionListener()
 				{
 					public void actionPerformed(ActionEvent e)
 					{
 						EditorStepRandomStringmenuItemSelfDefineString.setEditable(false);
 						RandomStringType = 0;
 					}
 				});
 				EditorStepRandomStringmenuItemStrings.addActionListener(new ActionListener()
 				{
 					public void actionPerformed(ActionEvent e)
 					{
 						EditorStepRandomStringmenuItemSelfDefineString.setEditable(false);
 						RandomStringType = 1;
 					}
 				});
 				EditorStepRandomStringmenuItemNumbersAndStrings.addActionListener(new ActionListener()
 				{
 					public void actionPerformed(ActionEvent e)
 					{
 						EditorStepRandomStringmenuItemSelfDefineString.setEditable(false);
 						RandomStringType = 2;
 					}
 				});
 				EditorStepRandomStringmenuItemSelfDefine.addActionListener(new ActionListener()
 				{
 					public void actionPerformed(ActionEvent e)
 					{
 						EditorStepRandomStringmenuItemSelfDefineString.setEditable(true);
 						RandomStringType = 3;
 					}
 				});
 				ButtonGroup RadioButtonGroup = new ButtonGroup();
 				RadioButtonGroup.add(EditorStepRandomStringmenuItemNumbers);
 				RadioButtonGroup.add(EditorStepRandomStringmenuItemStrings);
 				RadioButtonGroup.add(EditorStepRandomStringmenuItemNumbersAndStrings);
 				RadioButtonGroup.add(EditorStepRandomStringmenuItemSelfDefine);

 				JLabel DigitsLebel = new JLabel("Digits:");
 				DigitsLebel.setLocation(10, 40);
 				DigitsLebel.setSize(new Dimension(50, 30));

 				final JTextField DigitsText = new JTextField("6");
 				DigitsText.setLocation(50, 40);
 				DigitsText.setSize(50, 30);

 				JButton EditorStepRandomStringOkButton = new JButton("OK");
 				EditorStepRandomStringOkButton.setLocation(110, 40);
 				EditorStepRandomStringOkButton.setSize(new Dimension(60, 30));
 				EditorStepRandomStringOkButton.setFocusable(false);
 				
 				
 				
 				
 				
 				
 				EditorStepRandomStringOkButton.addActionListener(new ActionListener()
 				{
 					public void actionPerformed(ActionEvent e)
 					{
 						randomStringItem = new String[5];
 						String tmp = "";
 						try
 						{
 							int digitsint = Integer.valueOf(DigitsText.getText().trim()).intValue();
 							int randomListCount = randomStringList.size();
 							//System.out.println(randomListCount);
 							if (digitsint > 0)
 							{
 								randomStringItem[0] = String.valueOf(randomListCount);
 								randomStringItem[1] = String.valueOf(RandomStringType);
 								randomStringItem[2] = String.valueOf(digitsint);
 								if (RandomStringType == 3)
 								{
 									randomStringItem[3] = EditorStepRandomStringmenuItemSelfDefineString.getText().trim();
 								}
 								randomStringItem[4] = EditorRequestDaraRawTextArea.getSelectedText();
 								randomStringList.add(randomStringItem);
 								tmp = "rSI" + randomListCount;
 								String returnStr = Method.getBase64(tmp);
 								returnStr = Method.addSign(returnStr);
 								EditorRequestDaraRawTextArea.replaceSelection(returnStr);
 								EditorStepRandomStringmenuItemDialog.setVisible(false);
 								//System.out.println(tmp);
 								//System.out.println(randomStringList.size());
 							}
 							else
 							{
 								JOptionPane.showMessageDialog(null, "请输入一个正确的数字", "Warning", 2);
 							}
 						}
 						catch (Exception et)
 						{
 							JOptionPane.showMessageDialog(null, "请输入一个正确的数字", "Warning", 2);
 						}
 					}
 				});
 				EditorStepRandomStringmenuItemDialog.add(EditorStepRandomStringmenuItemNumbers);
 				EditorStepRandomStringmenuItemDialog.add(EditorStepRandomStringmenuItemStrings);
 				EditorStepRandomStringmenuItemDialog.add(EditorStepRandomStringmenuItemNumbersAndStrings);
 				EditorStepRandomStringmenuItemDialog.add(DigitsLebel);
 				EditorStepRandomStringmenuItemDialog.add(DigitsText);
 				EditorStepRandomStringmenuItemDialog.add(EditorStepRandomStringOkButton);
 				EditorStepRandomStringmenuItemDialog.add(EditorStepRandomStringmenuItemSelfDefine);
 				EditorStepRandomStringmenuItemDialog.add(EditorStepRandomStringmenuItemSelfDefineString);
 			}
 		});
 		EditorStepReplaceHostPort.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent e)
 			{
 				//System.out.println(EditorStepReplaceHostPort.getLabel());
 				if (EditorStepReplaceHostPort.getLabel().equals("ReplaceHostPort"))
 				{
 					EditorStepReplaceHostPort.setLabel("CencleReplaceHostPort");
 					replaceHostPort = true;
 				}
 				else
 				{
 					EditorStepReplaceHostPort.setLabel("ReplaceHostPort");
 					replaceHostPort = false;
 				}
     	}
 		});
 		EditorStepUsepreviousSetCookie.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent e)
 			{
 				if (EditorStepUsepreviousSetCookie.getLabel().equals("UsepreviousSetCookie"))
 				{
 					EditorStepUsepreviousSetCookie.setLabel("CencleUsepreviousSetCookie");
 					useSetCookieFromPrevious = true;
 					//System.out.println(useSetCookieFromPrevious);
 				}
 				else
 				{
 					EditorStepUsepreviousSetCookie.setLabel("UsepreviousSetCookie");
 					useSetCookieFromPrevious = false;
 					//System.out.println(useSetCookieFromPrevious);
 				}
 			}
 		});
 		EditorStepUseFileForCycleWithExp.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent e)
 			{
 				//System.out.println(JTabbedPaneTmp.getSelectedIndex());
 				
 				//System.out.println(JTabbedPaneTmp.getTitleAt(JTabbedPaneTmp.getSelectedIndex()).split("Step").length);
 				//System.out.println(JTabbedPaneTmp.getTitleAt(JTabbedPaneTmp.getSelectedIndex()).split("Step")[0]);
 				int tmp = Integer.valueOf(JTabbedPaneTmp.getTitleAt(JTabbedPaneTmp.getSelectedIndex()).split("Step")[1]) - 1;
 				//System.out.println(DataFile1CycleInOnePackage);
 				if (GrobalFileInStep == -1)
 				{
 					//System.out.println(useSetCookieFromPrevious);

 					JFileChooser jfc = new JFileChooser();
 					jfc.setFileSelectionMode(2);
 					jfc.showDialog(new JLabel(), "选择");
 					File file = jfc.getSelectedFile();
 					if ((file == null) || (file.equals(null)) || (file.isDirectory()))
 					{
 						JOptionPane.showMessageDialog(null, "请选择一个文件", "Warning", 2);
 					}
 					else if (file.isFile())
 					{
 						FileForCycleWithEXP = file.getAbsolutePath();
 						GrobalFileInStep = tmp;
 						FileForCycleWithEXPStrings[0] = FileForCycleWithEXP;
 						FileForCycleWithEXPStrings[1] = String.valueOf(tmp);
 						FileForCycleWithEXPStrings[2] = EditorRequestDaraRawTextArea.getSelectedText();
 						
 						EditorStepUseFileForCycleWithExp.setLabel("CencleUseFileForCycleWithExp");

 						String returnStr = Method.getBase64("FileForCycleWithEXP");
 						returnStr = Method.addSign(returnStr);
 						EditorRequestDaraRawTextArea.replaceSelection(returnStr);
 					}
 					
 				}
 				else if ((GrobalFileInStep == tmp) && (EditorStepUseFileForCycleWithExp.getLabel().equals("CencleUseFileForCycleWithExp")))
 				{
 					GrobalFileInStep = -1;
 					FileForCycleWithEXP = "";
 					FileForCycleWithEXPStrings[0] = "";
 					FileForCycleWithEXPStrings[1] = "-1";
 					FileForCycleWithEXPStrings[2] = "";
 					EditorStepUseFileForCycleWithExp.setLabel("UseFileForCycleWithExp");
 				}
 				else
 				{
 					JOptionPane.showMessageDialog(null, "UseFileForCycleWithExp Only In One DataPackage,Now in Step" + (GrobalFileInStep-1), "Warning", 2);
 				}
 			}
 		});
 		final JDialog EditorStepUseFileForCycleWithPackageDialog = new JDialog();
 		EditorStepUseFileForCycleWithPackageDialog.setLayout(null);
		EditorStepUseFileForCycleWithPackageDialog.setSize(new Dimension(400, 300));
		EditorStepUseFileForCycleWithPackageDialog.setLocation(100, 200);
			
		JLabel EditorStepUseFileForCycleWithPackageFileLabel = new JLabel("File:");
		EditorStepUseFileForCycleWithPackageFileLabel.setLocation(7, 7);
		EditorStepUseFileForCycleWithPackageFileLabel.setSize(70, 30);
			final JTextField EditorStepUseFileForCycleWithPackageFileText = new JTextField("");
			EditorStepUseFileForCycleWithPackageFileText.setLocation(40, 12);
			EditorStepUseFileForCycleWithPackageFileText.setSize(300, 25);
			
			final JRadioButton EditorStepUseFileForCycleWithPackageyiduiyiButton = new JRadioButton("一对一: ");
			EditorStepUseFileForCycleWithPackageyiduiyiButton.setFocusable(false);
			EditorStepUseFileForCycleWithPackageyiduiyiButton.setLocation(10, 45);
			EditorStepUseFileForCycleWithPackageyiduiyiButton.setSize(100, 30);
			EditorStepUseFileForCycleWithPackageyiduiyiButton.setSelected(true);
			JRadioButton EditorStepUseFileForCycleWithPackageyiduiduoButton = new JRadioButton("一对多: ");
			EditorStepUseFileForCycleWithPackageyiduiduoButton.setFocusable(false);
			EditorStepUseFileForCycleWithPackageyiduiduoButton.setLocation(120, 45);
			EditorStepUseFileForCycleWithPackageyiduiduoButton.setSize(100, 30);

			ButtonGroup EditorStepUseFileForCycleWithPackagebuttongroup = new ButtonGroup();
			EditorStepUseFileForCycleWithPackagebuttongroup.add(EditorStepUseFileForCycleWithPackageyiduiyiButton);
			EditorStepUseFileForCycleWithPackagebuttongroup.add(EditorStepUseFileForCycleWithPackageyiduiduoButton);
			JButton EditorStepUseFileForCycleWithPackageAddButton = new JButton("Add");
			EditorStepUseFileForCycleWithPackageAddButton.setFocusable(false);
			EditorStepUseFileForCycleWithPackageAddButton.setLocation(25, 85);
			EditorStepUseFileForCycleWithPackageAddButton.setSize(70, 25);

			JButton EditorStepUseFileForCycleWithPackageDelButton = new JButton("Del");
			EditorStepUseFileForCycleWithPackageDelButton.setFocusable(false);
			EditorStepUseFileForCycleWithPackageDelButton.setLocation(110, 85);
			EditorStepUseFileForCycleWithPackageDelButton.setSize(70, 25);

			JButton EditorStepUseFileForCycleWithPackageOKButton = new JButton("OK");
			EditorStepUseFileForCycleWithPackageOKButton.setFocusable(false);
			EditorStepUseFileForCycleWithPackageOKButton.setLocation(195, 85);
			EditorStepUseFileForCycleWithPackageOKButton.setSize(70, 25);

			String[] EditorStepUseFileForCycleWithPackageTableHeaders = { "FileIndex", "FileName", "匹配方式" };
			Object[][] EditorStepUseFileForCycleWithPackageTableCellData = null;
			DefaultTableModel EditorStepUseFileForCycleWithPackageTableModel = new DefaultTableModel(EditorStepUseFileForCycleWithPackageTableCellData, EditorStepUseFileForCycleWithPackageTableHeaders);

			final JTable EditorStepUseFileForCycleWithPackageTable = new JTable(EditorStepUseFileForCycleWithPackageTableModel);

			JScrollPane EditorStepUseFileForCycleWithPackageShowScroll = new JScrollPane(EditorStepUseFileForCycleWithPackageTable);
			EditorStepUseFileForCycleWithPackageShowScroll.setLocation(10, 125);
			EditorStepUseFileForCycleWithPackageShowScroll.setSize(300, 120);

			EditorStepUseFileForCycleWithPackageAddButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					String fileName = EditorStepUseFileForCycleWithPackageFileText.getText().trim();
					int FileCount = EditorStepUseFileForCycleWithPackageTable.getRowCount();
					DefaultTableModel Model = (DefaultTableModel)EditorStepUseFileForCycleWithPackageTable.getModel();
					//useFileForCycleWithPackageItem = new String[4];
					String tmp = "";
					int tmpInt;
					if (EditorStepUseFileForCycleWithPackageyiduiyiButton.isSelected())
					{
						tmp = "yiduiyi";
						tmpInt = 1;
					}
					else
					{
						tmp = "yiduiduo";
						tmpInt = 2;
					}
					if (fileName.length() > 0)
					{
						if (FileCount == 0)
						{
							Model.addRow(new Object[] { "File-1", fileName });
							
							useFileForCycleWithPackageItem[0] = "1";
							useFileForCycleWithPackageItem[1] = fileName;
							useFileForCycleWithPackageItem[4] = EditorRequestDaraRawTextArea.getSelectedText();
							String returnStr = Method.getBase64("File-1");
							returnStr = Method.addSign(returnStr);
							EditorRequestDaraRawTextArea.replaceSelection(returnStr);
						}
						else if (FileCount == 1)
						{
							Model.addRow(new Object[] { "File-2", fileName, tmp });
							useFileForCycleWithPackageItem[0] = "2";
							useFileForCycleWithPackageItem[1] = Model.getValueAt(0, 1).toString();
							useFileForCycleWithPackageItem[2] = fileName;
							useFileForCycleWithPackageItem[3] = String.valueOf(tmpInt);
							useFileForCycleWithPackageItem[5] = EditorRequestDaraRawTextArea.getSelectedText();
							String returnStr = Method.getBase64("File-2");
							returnStr = Method.addSign(returnStr);
							EditorRequestDaraRawTextArea.replaceSelection(returnStr);
						}
						else if(FileCount==2)
						{
							JOptionPane.showMessageDialog(null, "最多选择两个文件！", "Warning", 2);
						}

					}

					//System.out.println(FileCount);
				}
			});
			EditorStepUseFileForCycleWithPackageDelButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					DefaultTableModel Model = (DefaultTableModel)EditorStepUseFileForCycleWithPackageTable.getModel();
					int selectindex = EditorStepUseFileForCycleWithPackageTable.getSelectedRow();
					if (selectindex == 0)
					{
						Model.removeRow(selectindex);
						if (EditorStepUseFileForCycleWithPackageTable.getSelectedRow() == 1)
						{
							Model.setValueAt("File-1", 0, 0);
							Model.setValueAt("", 0, 2);
						}
						useFileForCycleWithPackageItem[0] = String.valueOf(1);
						useFileForCycleWithPackageItem[1] = useFileForCycleWithPackageItem[2];
						useFileForCycleWithPackageItem[2] = "";
						useFileForCycleWithPackageItem[3] = "";
						useFileForCycleWithPackageItem[4] = useFileForCycleWithPackageItem[5];
						useFileForCycleWithPackageItem[5]= "";
					}
					else if (selectindex == 1)
					{
						Model.removeRow(selectindex);
						useFileForCycleWithPackageItem[0] = String.valueOf(1);
						useFileForCycleWithPackageItem[2] = "";
						useFileForCycleWithPackageItem[3] = "";
						useFileForCycleWithPackageItem[5] = "";
					}
				}
			});
			EditorStepUseFileForCycleWithPackageOKButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					EditorStepUseFileForCycleWithPackageDialog.setVisible(false);
				}
			});
			EditorStepUseFileForCycleWithPackageFileText.addMouseListener(new MouseListener()
			{
				public void mouseReleased(MouseEvent e)
				{
				}

				public void mousePressed(MouseEvent e)
				{
				}

				public void mouseExited(MouseEvent e)
				{
				}

				public void mouseEntered(MouseEvent e)
				{
				}

				public void mouseClicked(MouseEvent e)
				{
					JFileChooser jfc = new JFileChooser("");
					jfc.setFileSelectionMode(2);
					jfc.showDialog(new JLabel(), "选择");
					File file = jfc.getSelectedFile();
					if ((file == null) || (file.equals(null)) || (file.isDirectory()))
					{
						JOptionPane.showMessageDialog(null, "请选择一个文件", "Warning", 2);
					}
					else if (file.isFile())
					{
						EditorStepUseFileForCycleWithPackageFileText.setText(file.getAbsolutePath());
					}
				}
			});
			EditorStepUseFileForCycleWithPackageDialog.add(EditorStepUseFileForCycleWithPackageFileLabel);
			EditorStepUseFileForCycleWithPackageDialog.add(EditorStepUseFileForCycleWithPackageFileText);
			EditorStepUseFileForCycleWithPackageDialog.add(EditorStepUseFileForCycleWithPackageyiduiyiButton);
			EditorStepUseFileForCycleWithPackageDialog.add(EditorStepUseFileForCycleWithPackageyiduiduoButton);
			EditorStepUseFileForCycleWithPackageDialog.add(EditorStepUseFileForCycleWithPackageAddButton);
			EditorStepUseFileForCycleWithPackageDialog.add(EditorStepUseFileForCycleWithPackageDelButton);
			EditorStepUseFileForCycleWithPackageDialog.add(EditorStepUseFileForCycleWithPackageOKButton);
			EditorStepUseFileForCycleWithPackageDialog.add(EditorStepUseFileForCycleWithPackageShowScroll);	
			
		
			
 		EditorStepUseFileForCycleWithPackage.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent e)
 			{
 				
 				String[] tmp = useFileForCycleWithPackageItem;
 				DefaultTableModel Model = (DefaultTableModel)EditorStepUseFileForCycleWithPackageTable.getModel();
 				Model.setRowCount(0);
 				if(tmp!=null&&!tmp.equals(null)&&tmp[0]!=null)
 				{
 					if(tmp[0].equals("1"))
 					{
 						Model.addRow(new Object[] { "File-1", tmp[1] });
 					}
 					else if(tmp[0].equals("2"))
 					{
 						Model.addRow(new Object[] { "File-1", tmp[1] });
 						
 						if(tmp[3].equals("1"))
 						{
 							Model.addRow(new Object[] { "File-2", tmp[2],"一对yi" });
 						}
 						else if(tmp[3].equals("2"))
 						{
 							Model.addRow(new Object[] { "File-2", tmp[2],"一对多" });
 						}
 					}
 				}
 				
 				EditorStepUseFileForCycleWithPackageDialog.setVisible(true);

 				
 			}
 		});
 		EditorStepUseFileForCycleWithPackage1.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent e)
 			{
 				int tmp = Integer.valueOf(JTabbedPaneTmp.getTitleAt(JTabbedPaneTmp.getSelectedIndex()).split("Step")[1]).intValue() - 1;
 				//System.out.println(DataFile1CycleInOnePackage);
 				if (DataFile1CycleInOnePackage == -1)
 				{
 					//System.out.println(tmp);
 					DataFile1CycleInOnePackage = tmp;
 					//System.out.println(DataFile1CycleInOnePackage);
 					EditorStepUseFileForCycleWithPackage1.setLabel("CencleUseFile1ForCycleWithPackage");
 					EditorStepPopupMenu.add(EditorStepUseFileForCycleWithPackage2);
 					//System.out.println("gg");
 				}
 				else if ((DataFile1CycleInOnePackage == tmp) && (EditorStepUseFileForCycleWithPackage1.getLabel().equals("CencleUseFile1ForCycleWithPackage")))
 				{
 					DataFile1CycleInOnePackage = -1;
 					EditorStepUseFileForCycleWithPackage1.setLabel("UseFile1ForCycleWithPackage");
 					EditorStepPopupMenu.remove(EditorStepUseFileForCycleWithPackage2);
 				}
 				else
 				{
 					JOptionPane.showMessageDialog(null, "UseFile1ForCycleWithPackage Only In One DataPackage,Now in Step" + DataFile1CycleInOnePackage, "Warning", 2);
 				}
 			}
 		});
 		EditorStepUseFileForCycleWithPackage2.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent e)
 			{
 				int tmp = Integer.valueOf(JTabbedPaneTmp.getTitleAt(JTabbedPaneTmp.getSelectedIndex()).split("Step")[1]).intValue() - 1;
 				if (DataFile2CycleInOnePackage == -1)
 				{
 					//System.out.println(tmp);
 					DataFile2CycleInOnePackage = tmp;
 					//System.out.println(DataFile2CycleInOnePackage);
 					EditorStepUseFileForCycleWithPackage2.setLabel("CencleUseFile2ForCycleWithPackage");
 					EditorStepPopupMenu.remove(EditorStepUseFileForCycleWithPackage1);
 					//System.out.println("gg");
 				}
 				else if ((DataFile2CycleInOnePackage == tmp) && (EditorStepUseFileForCycleWithPackage2.getLabel().equals("CencleUseFile2ForCycleWithPackage")))
 				{
 					DataFile2CycleInOnePackage = -1;
 					EditorStepUseFileForCycleWithPackage2.setLabel("UseFile2ForCycleWithPackage");
 					EditorStepPopupMenu.add(EditorStepUseFileForCycleWithPackage1);
 				}
 				else
 				{
 					JOptionPane.showMessageDialog(null, "UseFile1ForCycleWithPackage Only In One DataPackage,Now in Step" + DataFile1CycleInOnePackage, "Warning", 2);
 				}
 			}
 		});
 		final JDialog EditorStepSetGoaheadOrStopFromResponseStatusDialog = new JDialog();
 		EditorStepSetGoaheadOrStopFromResponseStatusDialog.setLayout(null);
			EditorStepSetGoaheadOrStopFromResponseStatusDialog.setSize(500, 400);
			EditorStepSetGoaheadOrStopFromResponseStatusDialog.setLocation(100, 200);
			EditorStepSetGoaheadOrStopFromResponseStatusDialog.setAlwaysOnTop(true);
 		///////////////////////////////////////////////////////////////////////////////////
 		final JRadioButton StopRadioButton = new JRadioButton("Stop Current");
			StopRadioButton.setLocation(7, 7);
			StopRadioButton.setSize(new Dimension(100, 30));
			StopRadioButton.setFocusable(false);
			final JRadioButton StartRadioButton = new JRadioButton("Go Ahead Current");
			StartRadioButton.setLocation(120, 7);
			StartRadioButton.setSize(new Dimension(130, 30));
			StartRadioButton.setFocusable(false);
			StartRadioButton.setSelected(true);

			final JRadioButton StopExpRadioButton = new JRadioButton("Stop EXP");
			StopExpRadioButton.setLocation(260, 7);
			StopExpRadioButton.setSize(new Dimension(100, 30));
			StopExpRadioButton.setFocusable(false);

			final JRadioButton StartExpRadioButton = new JRadioButton("Go Ahead EXP");
			StartExpRadioButton.setLocation(360, 7);
			StartExpRadioButton.setSize(new Dimension(120, 30));
			StartExpRadioButton.setFocusable(false);

			JLabel StatusLabel = new JLabel("Status :");
			StatusLabel.setLocation(15, 40);
			StatusLabel.setSize(new Dimension(70, 30));
			final JTextField StatusText = new JTextField("0");
			StatusText.setLocation(60, 45);
			StatusText.setSize(new Dimension(40, 20));

			JButton AddButton = new JButton("Add");
			AddButton.setLocation(130, 40);
			AddButton.setSize(new Dimension(60, 25));
			AddButton.setFocusable(false);

			JButton DelButton = new JButton("Del");
			DelButton.setLocation(210, 40);
			DelButton.setSize(new Dimension(60, 25));
			DelButton.setFocusable(false);

			JButton OkButton = new JButton("OK");
			OkButton.setLocation(320, 40);
			OkButton.setSize(new Dimension(60, 25));
			OkButton.setFocusable(false);

			OkButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					EditorStepSetGoaheadOrStopFromResponseStatusDialog.setVisible(false);
				}
			});

			String[] JPtabEditorSetGoAheadOrStopFromStatusTableHeaders = { "Name", "Value" };
			Object[][] JPtabEditorSetGoAheadOrStopFromStatusTableCellData = null;
			DefaultTableModel JPtabEditorSetGoAheadOrStopFromStatusTableModel = new DefaultTableModel(JPtabEditorSetGoAheadOrStopFromStatusTableCellData, JPtabEditorSetGoAheadOrStopFromStatusTableHeaders) {
				public boolean isCellEditable(int row, int column) {
					return true;
				}
			};
			JPtabEditorSetGoAheadOrStopFromStatusTable = new JTable(JPtabEditorSetGoAheadOrStopFromStatusTableModel)
			{
				public boolean isCellEditable(int row, int column)
				{
					return false;
				}
			};
			DelButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					DefaultTableModel tableModel = (DefaultTableModel)JPtabEditorSetGoAheadOrStopFromStatusTable.getModel();
					int selectrow = JPtabEditorSetGoAheadOrStopFromStatusTable.getSelectedRow();
					if (selectrow >= 0)
					{
						tableModel.removeRow(JPtabEditorSetGoAheadOrStopFromStatusTable.getSelectedRow());
						String[] tmp = (String[])setGoAheadOrStopFromStatusList.get(selectrow);
						setGoAheadOrStopFromStatusList.remove(selectrow);
						for (int i = 0; i < setGoAheadOrStopFromStatusList.size(); i++)
						{
							String[] ttt = (String[])setGoAheadOrStopFromStatusList.get(i);
							//System.out.println(ttt[0] + ":" + ttt[1]);
						}
					}
				}
			});
			JScrollPane ShowScrollPanel = new JScrollPane(JPtabEditorSetGoAheadOrStopFromStatusTable);
			AddButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					try
					{
						int status = Integer.valueOf(StatusText.getText()).intValue();
						String actionString = "";
						if (StopRadioButton.isSelected())
						{
							actionString = StopRadioButton.getText();
							//System.out.println(actionString);
						}
						else if (StartRadioButton.isSelected())
						{
							actionString = StartRadioButton.getText();
							//System.out.println(actionString);
						}
						else if (StopExpRadioButton.isSelected())
						{
							actionString = StopExpRadioButton.getText();
							//System.out.println(actionString);
						}
						else if (StartExpRadioButton.isSelected())
						{
							actionString = StartExpRadioButton.getText();
							//System.out.println(actionString);
						}

						if ((status >= 0) && (status < 1000))
						{
							DefaultTableModel tableModel = (DefaultTableModel)JPtabEditorSetGoAheadOrStopFromStatusTable.getModel();
							tableModel.addRow(new Object[] { Integer.valueOf(status), actionString });
							JPtabEditorSetGoAheadOrStopFromStatusTable.invalidate();
							setGoAheadOrStopFromStatusItem = new String[2];
							setGoAheadOrStopFromStatusItem[0] = String.valueOf(status);
							setGoAheadOrStopFromStatusItem[1] = actionString;
							setGoAheadOrStopFromStatusList.add(setGoAheadOrStopFromStatusItem);
						}
						else
						{
							JOptionPane.showMessageDialog(null, "请输入正确的状态号", "Warning", 2);
						}
						//System.out.println(setGoAheadOrStopFromStatusList.size());
					}
					catch (Exception ee)
					{
						JOptionPane.showMessageDialog(null, "请输入正确的状态号", "Warning", 2);
					}
				}
			});
			ButtonGroup RadioButton = new ButtonGroup();
			RadioButton.add(StartRadioButton);
			RadioButton.add(StopRadioButton);
			RadioButton.add(StopExpRadioButton);
			RadioButton.add(StartExpRadioButton);
			
			ShowScrollPanel.setLocation(10, 80);
			ShowScrollPanel.setSize(new Dimension(400, 250));
			ShowScrollPanel.setBackground(Color.white);

			EditorStepSetGoaheadOrStopFromResponseStatusDialog.add(StartRadioButton);
			EditorStepSetGoaheadOrStopFromResponseStatusDialog.add(StopRadioButton);
			EditorStepSetGoaheadOrStopFromResponseStatusDialog.add(StopExpRadioButton);
			EditorStepSetGoaheadOrStopFromResponseStatusDialog.add(StartExpRadioButton);
			EditorStepSetGoaheadOrStopFromResponseStatusDialog.add(StatusText);
			EditorStepSetGoaheadOrStopFromResponseStatusDialog.add(StatusLabel);
			EditorStepSetGoaheadOrStopFromResponseStatusDialog.add(AddButton);
			EditorStepSetGoaheadOrStopFromResponseStatusDialog.add(DelButton);
			EditorStepSetGoaheadOrStopFromResponseStatusDialog.add(OkButton);
			EditorStepSetGoaheadOrStopFromResponseStatusDialog.add(ShowScrollPanel);
 		
 		/////////////////////////////////////////////////////////////////////////////////////
 		
 		
 		EditorStepSetGoaheadOrStopFromResponseStatus.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent e)
 			{
 				
 				EditorStepSetGoaheadOrStopFromResponseStatusDialog.setVisible(true);

 				
 			}
 		});
 		final JDialog SetGoaheadOrStopFromResponseDataDialog = new JDialog();
 		
 		
 		
 		
 		////////////////////////////////////
 		String[] JPtabEditorSetGoAheadOrStopFromDataTableHeaders = { "Name", "Value" };
			Object[][] JPtabEditorSetGoAheadOrStopFromDataTableCellData = null;
			DefaultTableModel JPtabEditorSetGoAheadOrStopFromDataTableModel = new DefaultTableModel(JPtabEditorSetGoAheadOrStopFromDataTableCellData, JPtabEditorSetGoAheadOrStopFromDataTableHeaders) {
				public boolean isCellEditable(int row, int column) {
					return true;
				}
			};
			JPtabEditorSetGoAheadOrStopFromDataTable = new JTable(JPtabEditorSetGoAheadOrStopFromDataTableModel)
			{
				public boolean isCellEditable(int row, int column) {
					return false;
				}
			};
 		////////////////////////////////////
 		
 		
 		
 		
 		
 		
 		
 		
 		
 		
 		EditorStepSetGoaheadOrStopFromResponseData.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent e)
 			{
 				SetGoaheadOrStopFromResponseDataDialog.setLayout(null);
 				SetGoaheadOrStopFromResponseDataDialog.setSize(500, 400);
 				SetGoaheadOrStopFromResponseDataDialog.setLocation(100, 200);
 				SetGoaheadOrStopFromResponseDataDialog.setVisible(true);
 				SetGoaheadOrStopFromResponseDataDialog.setAlwaysOnTop(true);

 				final JRadioButton StopRadio = new JRadioButton("Stop Current");

 				StopRadio.setFocusable(false);
 				StopRadio.setLocation(7, 7);
 				StopRadio.setSize(100, 30);
 				final JRadioButton GOAheadRadio = new JRadioButton("Go Ahead Current");
 				GOAheadRadio.setFocusable(false);
 				GOAheadRadio.setLocation(120, 7);
 				GOAheadRadio.setSize(130, 30);
 				GOAheadRadio.setSelected(true);

 				final JRadioButton StopExpRadioButton = new JRadioButton("Stop EXP");
 				StopExpRadioButton.setLocation(260, 7);
 				StopExpRadioButton.setSize(new Dimension(100, 30));
 				StopExpRadioButton.setFocusable(false);

 				final JRadioButton StartExpRadioButton = new JRadioButton("Go Ahead EXP");
 				StartExpRadioButton.setLocation(360, 7);
 				StartExpRadioButton.setSize(new Dimension(120, 30));
 				StartExpRadioButton.setFocusable(false);

 				JLabel regexLabel = new JLabel("Regex :");
 				regexLabel.setLocation(20, 40);
 				regexLabel.setSize(60, 30);

 				final JTextField regexText = new JTextField(".*");
 				regexText.setSize(150, 20);
 				regexText.setLocation(70, 45);

 				JButton AddButton = new JButton("Add");
 				AddButton.setFocusable(false);
 				AddButton.setSize(60, 25);
 				AddButton.setLocation(230, 40);

 				JButton DelButton = new JButton("Del");
 				DelButton.setFocusable(false);
 				DelButton.setSize(60, 25);
 				DelButton.setLocation(300, 40);

 				JButton OkButton = new JButton("OK");
 				OkButton.setFocusable(false);
 				OkButton.setSize(60, 25);
 				OkButton.setLocation(370, 40);

 				
 				OkButton.addActionListener(new ActionListener()
 				{
 					public void actionPerformed(ActionEvent e)
 					{
 						SetGoaheadOrStopFromResponseDataDialog.setVisible(false);
 					}
 				});
 				AddButton.addActionListener(new ActionListener()
 				{
 					public void actionPerformed(ActionEvent e)
 					{
 						String regex = regexText.getText();
 						String actionString = "";
 						if (GOAheadRadio.isSelected())
 						{
 							actionString = GOAheadRadio.getText();
 							//System.out.println(actionString);
 						}
 						else if (StopRadio.isSelected())
 						{
 							actionString = StopRadio.getText();
 							//System.out.println(actionString);
 						}
 						else if (StopExpRadioButton.isSelected())
 						{
 							actionString = StopExpRadioButton.getText();
 							//System.out.println(actionString);
 						}
 						else if (StartExpRadioButton.isSelected())
 						{
 							actionString = StartExpRadioButton.getText();
 							//System.out.println(actionString);
 						}
 						DefaultTableModel tableModel = (DefaultTableModel)JPtabEditorSetGoAheadOrStopFromDataTable.getModel();
 						tableModel.addRow(new Object[] { regex, actionString });
 						JPtabEditorSetGoAheadOrStopFromDataTable.invalidate();
 						setGoAheadOrStopFromDataItem = new String[2];
 						setGoAheadOrStopFromDataItem[0] = regex;
 						setGoAheadOrStopFromDataItem[1] = actionString;
 						setGoAheadOrStopFromDataList.add(setGoAheadOrStopFromDataItem);
 					}
 				});
 				DelButton.addActionListener(new ActionListener()
 				{
 					public void actionPerformed(ActionEvent e)
 					{
 						DefaultTableModel tableModel = (DefaultTableModel)JPtabEditorSetGoAheadOrStopFromDataTable.getModel();
 						int selectrow = JPtabEditorSetGoAheadOrStopFromDataTable.getSelectedRow();
 						if (selectrow >= 0)
 						{
 							tableModel.removeRow(JPtabEditorSetGoAheadOrStopFromDataTable.getSelectedRow());
 							setGoAheadOrStopFromDataList.remove(selectrow);
 						}
 					}
 				});
 				JScrollPane ShowScrollPanel = new JScrollPane(JPtabEditorSetGoAheadOrStopFromDataTable);
 				ShowScrollPanel.setLocation(10, 80);
 				ShowScrollPanel.setSize(new Dimension(400, 250));
 				ShowScrollPanel.setBackground(Color.white);

 				ButtonGroup buttonGroup = new ButtonGroup();
 				buttonGroup.add(GOAheadRadio);
 				buttonGroup.add(StopRadio);
 				buttonGroup.add(StartExpRadioButton);
 				buttonGroup.add(StopExpRadioButton);

 				SetGoaheadOrStopFromResponseDataDialog.add(GOAheadRadio);
 				SetGoaheadOrStopFromResponseDataDialog.add(StopRadio);
 				SetGoaheadOrStopFromResponseDataDialog.add(StartExpRadioButton);
 				SetGoaheadOrStopFromResponseDataDialog.add(StopExpRadioButton);
 				SetGoaheadOrStopFromResponseDataDialog.add(regexLabel);
 				SetGoaheadOrStopFromResponseDataDialog.add(regexText);
 				SetGoaheadOrStopFromResponseDataDialog.add(AddButton);
 				SetGoaheadOrStopFromResponseDataDialog.add(DelButton);
 				SetGoaheadOrStopFromResponseDataDialog.add(OkButton);
 				SetGoaheadOrStopFromResponseDataDialog.add(ShowScrollPanel);
 			}
 		});
 		final JDialog SetParamsFromDataPackageDialog = new JDialog();
 		EditorStepSetParamsFromDataPackage.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent e)
 			{
 				JOptionPane.showMessageDialog(null, "暂时未实现", "Warning", 2);

 				SetParamsFromDataPackageDialog.setLayout(new BorderLayout());
 				SetParamsFromDataPackageDialog.setSize(700, 600);
 				SetParamsFromDataPackageDialog.setLocation(100, 100);
 				SetParamsFromDataPackageDialog.setVisible(true);
 				SetParamsFromDataPackageDialog.setAlwaysOnTop(true);

 				JPanel buttonPanel = new JPanel();
 				buttonPanel.setLayout(null);
 				buttonPanel.setPreferredSize(new Dimension(0, 70));
 				buttonPanel.setSize(new Dimension(0, 150));
 				final JRadioButton previousDataPackage = new JRadioButton("From Previous Data Package");
 				previousDataPackage.setFocusable(false);
 				previousDataPackage.setLocation(7, 7);
 				previousDataPackage.setSize(190, 30);
 				final JRadioButton currentDataPackage = new JRadioButton("From Current Data Package");
 				currentDataPackage.setSelected(true);
 				currentDataPackage.setLocation(210, 7);
 				currentDataPackage.setSize(190, 30);
 				currentDataPackage.setFocusable(false);
 				final JRadioButton otherDataPackage = new JRadioButton("From Other Data Package");
 				otherDataPackage.setLocation(410, 7);
 				otherDataPackage.setSize(190, 30);
 				otherDataPackage.setFocusable(false);
 				
 				ButtonGroup buttonGroup = new ButtonGroup();
 				buttonGroup.add(previousDataPackage);
 				buttonGroup.add(currentDataPackage);
 				buttonGroup.add(otherDataPackage);

 				buttonPanel.add(previousDataPackage);
 				buttonPanel.add(currentDataPackage);
 				buttonPanel.add(otherDataPackage);

 				final JTabbedPane JTabbedPaneTTT = new JTabbedPane(1);
 				JTabbedPaneTTT.setPreferredSize(new Dimension(0, 700));

 				final JDesktopPane PreviousDesktop = new JDesktopPane();
 				final JDesktopPane CurrentDesktop = new JDesktopPane();
 				final JDesktopPane OtherDesktop = new JDesktopPane();

 				JTabbedPaneTTT.add(PreviousDesktop, "Previous");
 				JTabbedPaneTTT.add(CurrentDesktop, "Current");
 				JTabbedPaneTTT.add(OtherDesktop, "Other");
 				JTabbedPaneTTT.setSelectedComponent(CurrentDesktop);

 				JTabbedPaneTTT.addChangeListener(new ChangeListener()
 				{
 					public void stateChanged(ChangeEvent e)
 					{
 						int tmp = JTabbedPaneTTT.getSelectedIndex();
 						if (tmp == 0)
 						{
 							previousDataPackage.setSelected(true);
 						}
 						else if (tmp == 1)
 						{
 							currentDataPackage.setSelected(true);
 						}
 						else if (tmp == 2)
 						{
 							otherDataPackage.setSelected(true);
 						}
 					}
 				});
 				previousDataPackage.addActionListener(new ActionListener()
 				{
 					public void actionPerformed(ActionEvent e)
 					{
 						JTabbedPaneTTT.setSelectedComponent(PreviousDesktop);
 					}
 				});
 				currentDataPackage.addActionListener(new ActionListener()
 				{
 					public void actionPerformed(ActionEvent e)
 					{
 						JTabbedPaneTTT.setSelectedComponent(CurrentDesktop);
 					}
 				});
 				otherDataPackage.addActionListener(new ActionListener()
 				{
 					public void actionPerformed(ActionEvent e)
 					{
 						JTabbedPaneTTT.setSelectedComponent(OtherDesktop);
 					}
 				});
 				SetParamsFromDataPackageDialog.add(buttonPanel, "North");
 				SetParamsFromDataPackageDialog.add(JTabbedPaneTTT, "Center");
 			}
 		});
 		final JDialog SetParamsFromCaptchaDialog = new JDialog();
 		EditorStepSetParamsFromCaptcha.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent e)
 			{
 				SetParamsFromCaptchaDialog.setLayout(new BorderLayout());
 				SetParamsFromCaptchaDialog.setLocation(100, 200);
 				SetParamsFromCaptchaDialog.setSize(new Dimension(500, 300));
 				SetParamsFromCaptchaDialog.setVisible(true);
 				SetParamsFromCaptchaDialog.setAlwaysOnTop(true);

        

 				JLabel URLLabel = new JLabel("URL :");
 				URLLabel.setLocation(7, 7);
 				URLLabel.setSize(new Dimension(50, 30));

 				final JTextField URLText = new JTextField();
 				URLText.setLocation(40, 12);
 				URLText.setSize(new Dimension(430, 25));
        
 				JLabel CaptchaNumbersLabel = new JLabel("Captchas Nums:");
 				CaptchaNumbersLabel.setLocation(7, 40);
 				CaptchaNumbersLabel.setSize(new Dimension(95, 30));
 				final JTextField CaptchaNumbersText = new JTextField("4");
 				CaptchaNumbersText.setLocation(110, 45);
 				CaptchaNumbersText.setSize(40, 25);
        
 				/*
        		SetParamsFromCaptchaDialog.addWindowListener(new WindowListener() {
			
				@Override
				public void windowOpened(WindowEvent e) {
				// TODO Auto-generated method stub
				//URLText.requestFocus(false);
				CaptchaNumbersText.requestFocus();
				}
			
				@Override
				public void windowIconified(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowDeiconified(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowDeactivated(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowClosing(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowClosed(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowActivated(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
	*/
        
        

 				JLabel TestTimesLabel = new JLabel("Test Tmies :");
 				TestTimesLabel.setLocation(170, 40);
 				TestTimesLabel.setSize(80, 30);
 				final JTextField TestTimesText = new JTextField("10");
 				TestTimesText.setLocation(250, 45);
 				TestTimesText.setSize(40, 25);
 				
 				JButton OKButton = new JButton("OK");
 				OKButton.setFocusable(false);
 				OKButton.setLocation(310, 45);
 				OKButton.setSize(new Dimension(70, 25));

 				final JButton TestButton = new JButton("Test");
 				TestButton.setFocusable(false);
 				TestButton.setLocation(400, 45);
 				TestButton.setSize(new Dimension(70, 25));

 				JPanel EditPanel = new JPanel();
 				EditPanel.setBackground(Color.white);
 				EditPanel.setSize(new Dimension(0, 70));
 				EditPanel.setPreferredSize(new Dimension(0, 70));
 				EditPanel.setLayout(null);

 				JPanel ShowPanel = new JPanel();
 				ShowPanel.setLayout(null);
 				ShowPanel.setPreferredSize(new Dimension(0, 700));
 				ShowPanel.setSize(new Dimension(0, 700));
 				ShowPanel.setBackground(Color.white);

 				final JEditorPane imageShowPane = new JEditorPane();
 				imageShowPane.setEditable(false);

 				imageShowPane.setSize(400, 40);
 				imageShowPane.setLocation(7, 7);
 				ShowPanel.add(imageShowPane);

 				final JButton openHTMLButton = new JButton("open");
 				openHTMLButton.setSize(70, 25);
 				openHTMLButton.setLocation(190, 50);
 				ShowPanel.add(openHTMLButton);

 				JLabel captchaBaseURLLabel = new JLabel("Base URL");
 				captchaBaseURLLabel.setSize(60,30);
 				captchaBaseURLLabel.setLocation(7, 90);
 				ShowPanel.add(captchaBaseURLLabel);
        
 				final JTextField captchaBaseURL = new  JTextField();
 				captchaBaseURL.setSize(300,25);
 				captchaBaseURL.setLocation(70, 90);
 				captchaBaseURL.setEditable(false);
 				ShowPanel.add(captchaBaseURL);
        
        
 				URLText.addKeyListener(new KeyListener() {
			
 					@Override
 					public void keyTyped(KeyEvent e) {
 						// TODO Auto-generated method stub
				
 					}
			
 					@Override
 					public void keyReleased(KeyEvent e) {
 						// TODO Auto-generated method stub
 						//System.out.println(URLText.getText());
 						String previousURL="";
				
 						String targetTmp = URLText.getText().trim();// 将要检测是字符串   example：www.baidu.com
 						targetTmp = targetTmp.replaceAll("\\\\","/");// 替换掉  \ 为  /
				
 						//分为是否有  http://
 						if(targetTmp.toLowerCase().startsWith("http://"))
 						{
 							//如果有 http:// 直接去掉 
 							targetTmp = targetTmp.substring(7);
 							
 							//后续操作就和没有http:// 一样
 							
 							//以 冒号来分割   判断是否存在端口号
 							String[] targetStringsTmp = targetTmp.split(":");
 							try
 							{
 								int previousIntTmp=0;
 								//这部分是测试 是否存在  /  存在  / 这个就判断为 有 url前缀
 								try
 								{
 									previousIntTmp = targetTmp.indexOf("/");
 								}
 								catch(Exception es)
 								{
 									es.printStackTrace();
 								}
						
						
 								if(targetStringsTmp.length>1)// 有端口
 								{
 									
 									if(previousIntTmp>0) //有url 前缀
 									{
								
 										previousURL = targetStringsTmp[1].substring(targetStringsTmp[1].indexOf("/"));
 									}
 									else //没有url qianzhui 
 									{
								
 										previousURL="";
 									}
							
 								}
 								else //没有端口
 								{
 									if(previousIntTmp>0)//有url 前缀
 									{
 										
 										previousURL = targetTmp.substring(previousIntTmp);
 									}
 									else//没有url 前缀
 									{
 										
 										previousURL="";
 									}
 								}
						
						
 							}
 							catch(Exception eg2)
 							{
 								
 								eg2.printStackTrace();
						
 							}
 						}
 						else
 						{
 							String[] targetStringsTmp = targetTmp.split(":");
 							try
 							{
 								int previousIntTmp=0;
 								try
 								{
 									previousIntTmp = targetTmp.indexOf("/");
 								}
 								catch(Exception es)
 								{
 									es.printStackTrace();
 								}
						
 								if(targetStringsTmp.length>1)// 有端口
 								{
 									
 									if(previousIntTmp>0) //有url 前缀
 									{
 										
 										previousURL = targetStringsTmp[1].substring(targetStringsTmp[1].indexOf("/"));
 									}
 									else //没有url qianzhui 
 									{
								
 										previousURL="";
 									}
 									
 								}
 								else //没有端口
 								{
 									if(previousIntTmp>0)//有url 前缀
 									{
 										
 										previousURL = targetTmp.substring(previousIntTmp);
 									}
 									else//没有url 前缀
 									{
								
 										previousURL="";
 									}
 								}
						
						
 							}
 							catch(Exception eg2)
 							{
				
 								eg2.printStackTrace();
						
 							}
 						}
				
				
 						//System.out.println(previousURL);
 						captchaBaseURL.setText(previousURL);
 						captchaBaseURL.setEditable(true);
 					}
			
 					@Override
 					public void keyPressed(KeyEvent e) {
 						// TODO Auto-generated method stub			
 						
 					}
 				});
        
        
        
        
        
 				if (imageShowPane.getText().indexOf("img") > 0)
 				{
 					//System.out.println(imageShowPane.getText());
 					openHTMLButton.setEnabled(true);
 				}
 				else
 				{
 					openHTMLButton.setEnabled(false);
 				}

 				EditPanel.add(URLLabel);
 				EditPanel.add(URLText);
 				EditPanel.add(CaptchaNumbersLabel);
 				EditPanel.add(CaptchaNumbersText);
 				EditPanel.add(TestTimesLabel);
 				EditPanel.add(TestTimesText);
 				EditPanel.add(OKButton);
 				EditPanel.add(TestButton);

 				openHTMLButton.addActionListener(new ActionListener()
 				{
 					public void actionPerformed(ActionEvent e)
 					{
 						try
 						{
 							Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + imageShowPane.getText().trim());
 						}
 						catch (Exception err)
 						{
 							err.printStackTrace();
 							JOptionPane.showMessageDialog(null, "请手动打开" + imageShowPane.getText().trim(), "Warning", 2);
 						}
 					}
 				});
 				OKButton.addActionListener(new ActionListener()
 				{
 					public void actionPerformed(ActionEvent e)
 					{
 						//String urlTmp = URLText.getText().trim();
 						String urlTmp = captchaBaseURL.getText().trim();

 						String tmp = "";
 						captchaItem = new String[4];
 						int captchaItemCount = captchaList.size();
 						try
 						{
 							int captchaNums = Integer.valueOf(CaptchaNumbersText.getText().trim()).intValue();
 							if (urlTmp.length() > 1)
 							{
 								captchaItem[0] = String.valueOf(captchaItemCount);
 								captchaItem[1] = urlTmp;
 								captchaItem[2] = String.valueOf(captchaNums);
 								captchaItem[3] = EditorRequestDaraRawTextArea.getSelectedText();
 								captchaList.add(captchaItem);

 								tmp = "sPFC" + captchaItemCount;
 								String returnStr = Method.getBase64(tmp);
 								returnStr = Method.addSign(returnStr);
 								EditorRequestDaraRawTextArea.replaceSelection(returnStr);
 								SetParamsFromCaptchaDialog.setVisible(false);
 								//System.out.println(tmp);
 								//System.out.println(randomStringList.size());
 							}
 							else
 							{
 								JOptionPane.showMessageDialog(null, "请输入一个正确的URL地址", "Warning", 2);
 							}
 						}
 						catch (Exception ee)
 						{
 							JOptionPane.showMessageDialog(null, "请输入一个正确的数字", "Warning", 2);
 						}
 					}
 				});
 				TestButton.addActionListener(new ActionListener()
 				{
 					public void actionPerformed(ActionEvent e)
 					{
 						Thread testThread = new Thread(new Runnable()
 						{
 							public void run()
 							{
 								imageShowPane.setText("");
 								TestButton.setEnabled(false);
 								try
 								{
 									boolean status = UUAPI.checkAPI();

 									if (!status)
 									{
 										//System.out.print("API文件校验失败，无法使用打码服务");
 										JOptionPane.showMessageDialog(null, "API文件校验失败，无法使用打码服务", "Warning", 2);
 									}
 									else
 									{
 										//System.out.println("g");
 										String ImgURL = URLText.getText();
 										int TestTimesTextCount = Integer.valueOf(TestTimesText.getText()).intValue();
 										int CaptchaNumbersTextInt = Integer.valueOf(CaptchaNumbersText.getText()).intValue();
 										File tmpHtml = new File("imageHTML.html");
 										FileWriter fileWriter = new FileWriter(tmpHtml, false);
 										fileWriter.write("<html><head><title>Captcha Test</title></head><body></br></br>");
 										for (int i = 0; i < TestTimesTextCount; i++)
 										{
 											String saveFileName = "img/test" + i + ".jpg";
 											String saveAbsolutePath = Method.getImgFromURL(ImgURL, saveFileName);
 											if (saveAbsolutePath != null)
 											{
 												if (CaptchaNumbersTextInt != 0)
 												{
 													String[] result = UUAPI.easyDecaptcha(saveFileName, 1000 + CaptchaNumbersTextInt);
 													if (result[1].length() > 0)
 													{
 														String tmpStr = "-->:<img src='" + saveFileName + "' /> ------ >" + result[1] + "</br>";
 														fileWriter.write(tmpStr);
 													}
 												}
 											}
 										}
 										imageShowPane.setText(tmpHtml.getAbsolutePath());
 										//System.out.println(tmpHtml.getAbsolutePath());
 										fileWriter.write("</body></html>");
 										fileWriter.flush();
 										fileWriter.close();
 										TestButton.setEnabled(true);
 										openHTMLButton.setEnabled(true);
 									}

 								}
 								catch (Exception eee)
 								{
 									eee.printStackTrace();
 								}
 							}
 						});
 						testThread.start();
 					}
 				});
 				SetParamsFromCaptchaDialog.add(EditPanel, "North");
 				SetParamsFromCaptchaDialog.add(ShowPanel, "Center");
 			}
 		});
 		EditorStepPopupMenu.add(EditorStepReplaceHostPort);
 		EditorStepPopupMenu.add(EditorStepUsepreviousSetCookie);
 		EditorStepPopupMenu.add(EditorStepRandomStringmenuItem);
 		EditorStepPopupMenu.add(EditorStepSetParamsFromCaptcha);
 		//EditorStepPopupMenu.add(EditorStepSetParamsFromDataPackage);
 		EditorStepPopupMenu.add(EditorStepSetGoaheadOrStopFromResponseStatus);
 		EditorStepPopupMenu.add(EditorStepSetGoaheadOrStopFromResponseData);
 		EditorStepPopupMenu.add(EditorStepUseFileForCycleWithExp);
 		EditorStepPopupMenu.add(EditorStepUseFileForCycleWithPackage);
 		EditorStepPopupMenu.add(EditorStepReSetItem);

 		EditorRequestDaraRawTextArea.add(EditorStepPopupMenu);

 		EditorRequestDaraRawTextArea.addMouseListener(new MouseListener()
 		{
 			public void mouseReleased(MouseEvent e)
 			{
 			}
 			
 			public void mousePressed(MouseEvent e)
 			{
 			}

 			public void mouseExited(MouseEvent e)
 			{
 			}

 			public void mouseEntered(MouseEvent e)
 			{
 			}

 			public void mouseClicked(MouseEvent e)
 			{
 				int mods = e.getModifiers();
 				if ((mods & 0x4) != 0)
 				{
 					EditorStepPopupMenu.show(EditorRequestDaraRawTextArea, e.getX(), e.getY());
 				}
 			}
 		});
 		JDesktopPane EditorRequestDataHeaders = new JDesktopPane();
 		EditorRequestDataHeaders.setFocusable(false);
 		EditorRequestDataHeaders.setLayout(new BorderLayout());
 		RequestTabPanel.add(EditorRequestDataHeaders, "Headers");

 		JPanel EditorRequestDataHeadersDataShowPane = new JPanel();

 		EditorRequestDataHeadersDataShowPane.setLayout(new BorderLayout());
 		EditorRequestDataHeaders.add(EditorRequestDataHeadersDataShowPane, "Center");

 		JPanel EditorRequestDataHeadersDataButtonPane = new JPanel();

 		EditorRequestDataHeadersDataButtonPane.setPreferredSize(new Dimension(120, 0));
 		EditorRequestDataHeadersDataButtonPane.setLayout(null);
 		//EditorRequestDataHeaders.add(EditorRequestDataHeadersDataButtonPane, "East");

 		JButton EditorRequestDataHeadersDataAddButton = new JButton("Add");
 		EditorRequestDataHeadersDataAddButton.setPreferredSize(new Dimension(130, 28));
 		EditorRequestDataHeadersDataAddButton.setLocation(0, 10);
 		EditorRequestDataHeadersDataAddButton.setFocusable(false);
 		EditorRequestDataHeadersDataAddButton.setSize(100, 30);
 		EditorRequestDataHeadersDataButtonPane.add(EditorRequestDataHeadersDataAddButton);

 		JButton EditorRequestDataHeadersDataRemoveButton = new JButton("Remove");
 		EditorRequestDataHeadersDataRemoveButton.setPreferredSize(new Dimension(130, 28));
 		EditorRequestDataHeadersDataRemoveButton.setLocation(0, 50);
 		EditorRequestDataHeadersDataRemoveButton.setFocusable(false);
 		EditorRequestDataHeadersDataRemoveButton.setSize(100, 30);
 		EditorRequestDataHeadersDataButtonPane.add(EditorRequestDataHeadersDataRemoveButton);

 		JButton EditorRequestDataHeadersDataUpButton = new JButton("Up");
 		EditorRequestDataHeadersDataUpButton.setPreferredSize(new Dimension(130, 28));
 		EditorRequestDataHeadersDataUpButton.setLocation(0, 90);
 		EditorRequestDataHeadersDataUpButton.setFocusable(false);
 		EditorRequestDataHeadersDataUpButton.setSize(100, 30);
 		EditorRequestDataHeadersDataButtonPane.add(EditorRequestDataHeadersDataUpButton);

 		JButton EditorRequestDataHeadersDataDownButton = new JButton("Down");
 		EditorRequestDataHeadersDataDownButton.setPreferredSize(new Dimension(130, 28));
 		EditorRequestDataHeadersDataDownButton.setLocation(0, 130);
 		EditorRequestDataHeadersDataDownButton.setFocusable(false);
 		EditorRequestDataHeadersDataDownButton.setSize(100, 30);
 		EditorRequestDataHeadersDataButtonPane.add(EditorRequestDataHeadersDataDownButton);

 		JDesktopPane EditorRequestDataHex = new JDesktopPane();
 		EditorRequestDataHex.setFocusable(false);
 		EditorRequestDataHex.setLayout(new BorderLayout());
 		RequestTabPanel.add(EditorRequestDataHex, "Hex");

 		String[] JPtabEditorRequestDataShowHeadersTableHeaders = { "Name", "Value" };
 		Object[][] JPtabEditorRequestDataShowHeadersTableCellData = null;
 		DefaultTableModel JPtabEditorRequestDataShowHeadersTableModel = new DefaultTableModel(JPtabEditorRequestDataShowHeadersTableCellData, JPtabEditorRequestDataShowHeadersTableHeaders) {
 			public boolean isCellEditable(int row, int column) {
 				return true;
 			}
 		};
 		final JTable JPtabEditorRequestDataShowHeadersTable = new JTable(JPtabEditorRequestDataShowHeadersTableModel);
 		JPtabEditorRequestDataShowHeadersTable.getModel().addTableModelListener(new TableModelListener()
 		{
 			public void tableChanged(TableModelEvent e)
 			{
 				if (e.getType() == 0)
 				{
 					TableModel tableModelTmp = JPtabEditorRequestDataShowHeadersTable.getModel();
 					int tableRowCount = tableModelTmp.getRowCount();
 					//System.out.println(tableRowCount);
 					String editorRequestDataStingStmp = "";
 					if (tableRowCount > 0)
 					{
 						editorRequestDataStingStmp = editorRequestDataStingStmp + tableModelTmp.getValueAt(0, 0) + " " + tableModelTmp.getValueAt(0, 1) + "\r\n";
 						for (int i = 1; i < tableRowCount; i++)
 						{
 							editorRequestDataStingStmp = editorRequestDataStingStmp + tableModelTmp.getValueAt(i, 0) + ": " + tableModelTmp.getValueAt(i, 1) + "\r\n";
 						}
 						editorStepShowString = editorRequestDataStingStmp;
 						editorStepRequestData = editorStepShowString;
 						//EditorRequestDaraRawTextArea.setText(editorStepShowString);
 						insertDocument(editorStepShowString, Color.black);
 					}
 				}
 			}
 		});
 		JScrollPane JPtabEditorRequestDataShowHeadersTableScollPanel = new JScrollPane(JPtabEditorRequestDataShowHeadersTable);
 		EditorRequestDataHeadersDataShowPane.add(JPtabEditorRequestDataShowHeadersTableScollPanel, "Center");

 		String[] JPtabProxyEditorRequestDataHexTableHeaders = { "Num", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "String" };
 		Object[][] JPtabProxyEditorRequestDataHexTableCellData = null;
 		DefaultTableModel JPtabProxyEditorRequestDataHexTableModel = new DefaultTableModel(JPtabProxyEditorRequestDataHexTableCellData, JPtabProxyEditorRequestDataHexTableHeaders) {
 			public boolean isCellEditable(int row, int column) {
 				return true;
 			}
 		};
 		final JTable JPtabEditorRequestDataHexTable = new JTable(JPtabProxyEditorRequestDataHexTableModel)
 		{
 			public boolean isCellEditable(int row, int column)
 			{
 				if ((column == 0) || (column == 17))
 					return false;
 				return true;
 			}
 		};
 		JPtabEditorRequestDataHexTable.getTableHeader().setVisible(false);
 		JPtabEditorRequestDataHexTable.setCellSelectionEnabled(false);
 		JPtabEditorRequestDataHexTable.getModel().addTableModelListener(new TableModelListener()
 		{
 			public void tableChanged(TableModelEvent e)
 			{
 				if (e.getType() == 0)
 				{
 					String newvalue = JPtabEditorRequestDataHexTable.getValueAt(e.getLastRow(), e.getColumn()).toString();
 					String[] HttpEditorRequestDataHexStrings = Method.bytesToHexStrings(editorStepShowString.getBytes());
 					HttpEditorRequestDataHexStrings[(e.getLastRow() * 16 + e.getColumn() - 1)] = newvalue;
 					editorStepShowString = Method.bytesToString(Method.HexStringTobytes(Method.stringsToString(HttpEditorRequestDataHexStrings)));
 					editorStepRequestData = editorStepShowString;

 					String Str = editorStepShowString;

 					if ((JPtabEditorRequestDataShowHeadersTable != null) && (Str.length() != 0))
 					{
 						DefaultTableModel tableModel = (DefaultTableModel)JPtabEditorRequestDataShowHeadersTable.getModel();
 						tableModel.setRowCount(0);
 						String[] headerParams = Method.getHTTPHeadersParams(Str);
 						int headerParamLength = headerParams.length / 2;
 						for (int i = 0; i < headerParamLength; i++)
 						{
 							tableModel.addRow(new Object[] { headerParams[(i * 2)].trim(), headerParams[(i * 2 + 1)].trim() });
 						}
 						JPtabEditorRequestDataShowHeadersTable.invalidate();
 					}

 					if ((Str != null) && (!Str.equals(null)) && (Str.length() != 0))
 					{
 						String[] HttpResponseDataHexStrings = Method.bytesToHexStrings(Str.getBytes());
 						int HttpResponseDataHexStringsLength = HttpResponseDataHexStrings.length;
 						int HttpResponseDataHexStringsRowNum = HttpResponseDataHexStringsLength / 16;
 						int HttpResponseDataHexStringsRealRowNum = 0;
 						if (HttpResponseDataHexStringsLength % 16 == 0)
 						{
 							HttpResponseDataHexStringsRealRowNum = HttpResponseDataHexStringsRowNum;
 						}
 						else
 						{
 							HttpResponseDataHexStringsRealRowNum = HttpResponseDataHexStringsRowNum + 1;
 						}

 						String[] HttpResponseDataHexStringsRealRowStrings = new String[HttpResponseDataHexStringsRealRowNum];
 						String HttpResponseDataHexStringsRealRowStringTmp = "";
 						char[] ResponseStringchars = Str.toCharArray();
 						int ResponseStringcharsLength = ResponseStringchars.length;
 						int i = 1; for (int j = 0; (i <= ResponseStringcharsLength) && (j < HttpResponseDataHexStringsRealRowNum); i++)
 						{
 							HttpResponseDataHexStringsRealRowStringTmp = HttpResponseDataHexStringsRealRowStringTmp + ResponseStringchars[(i - 1)];
 							if (i % 16 == 0)
 							{
 								HttpResponseDataHexStringsRealRowStrings[j] = HttpResponseDataHexStringsRealRowStringTmp;
 								HttpResponseDataHexStringsRealRowStringTmp = "";
 								j++;
 							}
 						}
 						HttpResponseDataHexStringsRealRowStrings[(HttpResponseDataHexStringsRealRowNum - 1)] = HttpResponseDataHexStringsRealRowStringTmp;

 						DefaultTableModel JPtabProxyHistoryDetailResponseHexTableModel = (DefaultTableModel)JPtabEditorRequestDataHexTable.getModel();
 						JPtabProxyHistoryDetailResponseHexTableModel.setRowCount(0);

 						for (int k = 0; k < HttpResponseDataHexStringsRowNum; k++)
 						{
 							JPtabProxyHistoryDetailResponseHexTableModel.addRow(new Object[] { Integer.valueOf(k + 1), HttpResponseDataHexStrings[(k * 16)], HttpResponseDataHexStrings[(k * 16 + 1)], HttpResponseDataHexStrings[(k * 16 + 2)], HttpResponseDataHexStrings[(k * 16 + 3)], HttpResponseDataHexStrings[(k * 16 + 4)], HttpResponseDataHexStrings[(k * 16 + 5)], HttpResponseDataHexStrings[(k * 16 + 6)], HttpResponseDataHexStrings[(k * 16 + 7)], HttpResponseDataHexStrings[(k * 16 + 8)], HttpResponseDataHexStrings[(k * 16 + 9)], HttpResponseDataHexStrings[(k * 16 + 10)], HttpResponseDataHexStrings[(k * 16 + 11)], HttpResponseDataHexStrings[(k * 16 + 12)], HttpResponseDataHexStrings[(k * 16 + 13)], HttpResponseDataHexStrings[(k * 16 + 14)], HttpResponseDataHexStrings[(k * 16 + 15)], HttpResponseDataHexStringsRealRowStrings[k] });
 						}
 						String[] ResponseHexStringsTmp = { String.valueOf(HttpResponseDataHexStringsRealRowNum), "--", "--", "--", "--", "--", "--", "--", "--", "--", "--", "--", "--", "--", "--", "--", "--", HttpResponseDataHexStringsRealRowStrings[(HttpResponseDataHexStringsRealRowNum - 1)] };
 						int HttpResponseDataHexStringsSHENGYUGESHU = HttpResponseDataHexStringsLength % 16;
 						for (int k = 0; k < HttpResponseDataHexStringsSHENGYUGESHU; k++)
 						{
 							ResponseHexStringsTmp[(k + 1)] = HttpResponseDataHexStrings[(HttpResponseDataHexStringsRowNum * 16 + k)];
 						}
 						if (HttpResponseDataHexStringsLength % 16 != 0)
 						{
 							JPtabProxyHistoryDetailResponseHexTableModel.addRow(ResponseHexStringsTmp);
 							JPtabEditorRequestDataHexTable.invalidate();
 						}
 					}
 					//EditorRequestDaraRawTextArea.setText(editorStepShowString);
 					insertDocument(editorStepShowString, Color.black);
 				}
 			}
 		});
 		EditorRequestDaraRawTextArea.getDocument().addDocumentListener(new DocumentListener()
 		{
 			public void removeUpdate(DocumentEvent e)
 			{
 				editorStepShowString = EditorRequestDaraRawTextArea.getText();
 				editorStepShowString = Method.replace10To1310(editorStepShowString);
 				editorStepRequestData = editorStepShowString;
 				if (editorStepShowString.length() == 0)
 				{
 					GOButton.setEnabled(false);
 					CencleButton.setEnabled(false);
 				}
 			}

 			public void insertUpdate(DocumentEvent e)
 			{
 				GOButton.setEnabled(true);
 				CencleButton.setEnabled(true);
 				editorStepShowString = EditorRequestDaraRawTextArea.getText();
 				editorStepShowString = Method.replace10To1310(editorStepShowString);
 				editorStepRequestData = editorStepShowString;

 				String Str = editorStepShowString;
 				
 				if ((JPtabEditorRequestDataShowHeadersTable != null) && (Str.length() != 0))
 				{
 					DefaultTableModel tableModel = (DefaultTableModel)JPtabEditorRequestDataShowHeadersTable.getModel();
 					tableModel.setRowCount(0);
 					String[] headerParams = Method.getHTTPHeadersParams(Str);
 					if ((headerParams != null) && (!headerParams.equals(null)) && (headerParams.length > 0))
 					{
 						int headerParamLength = headerParams.length / 2;
 						//System.out.println(headerParams.length+"headerParams");
 						for (int i = 0; i < headerParamLength; i++)
 						{
 							//System.out.println(i);
 							//System.out.println(headerParams[(i * 2)]);
 							//System.out.println(headerParams[(i * 2+1)]);
 							tableModel.addRow(new Object[] { headerParams[(i * 2)].trim(), headerParams[(i * 2 + 1)].trim() });
 						}
 						JPtabEditorRequestDataShowHeadersTable.invalidate();
 					}

 				}

 				if ((Str != null) && (!Str.equals(null)) && (Str.length() != 0))
 				{
 					String[] HttpResponseDataHexStrings = Method.bytesToHexStrings(Str.getBytes());
 					int HttpResponseDataHexStringsLength = HttpResponseDataHexStrings.length;
 					int HttpResponseDataHexStringsRowNum = HttpResponseDataHexStringsLength / 16;
 					int HttpResponseDataHexStringsRealRowNum = 0;
 					if (HttpResponseDataHexStringsLength % 16 == 0)
 					{
 						HttpResponseDataHexStringsRealRowNum = HttpResponseDataHexStringsRowNum;
 					}
 					else
 					{
 						HttpResponseDataHexStringsRealRowNum = HttpResponseDataHexStringsRowNum + 1;
 					}

 					String[] HttpResponseDataHexStringsRealRowStrings = new String[HttpResponseDataHexStringsRealRowNum];
 					String HttpResponseDataHexStringsRealRowStringTmp = "";
 					char[] ResponseStringchars = Str.toCharArray();
 					int ResponseStringcharsLength = ResponseStringchars.length;
 					int i = 1; for (int j = 0; (i <= ResponseStringcharsLength) && (j < HttpResponseDataHexStringsRealRowNum); i++)
 					{
 						HttpResponseDataHexStringsRealRowStringTmp = HttpResponseDataHexStringsRealRowStringTmp + ResponseStringchars[(i - 1)];
 						if (i % 16 == 0)
 						{
 							HttpResponseDataHexStringsRealRowStrings[j] = HttpResponseDataHexStringsRealRowStringTmp;
 							HttpResponseDataHexStringsRealRowStringTmp = "";
 							j++;
 						}
 					}
 					HttpResponseDataHexStringsRealRowStrings[(HttpResponseDataHexStringsRealRowNum - 1)] = HttpResponseDataHexStringsRealRowStringTmp;

 					DefaultTableModel JPtabProxyHistoryDetailResponseHexTableModel = (DefaultTableModel)JPtabEditorRequestDataHexTable.getModel();
 					JPtabProxyHistoryDetailResponseHexTableModel.setRowCount(0);
 					
 					for (int k = 0; k < HttpResponseDataHexStringsRowNum; k++)
 					{
 						JPtabProxyHistoryDetailResponseHexTableModel.addRow(new Object[] { Integer.valueOf(k + 1), HttpResponseDataHexStrings[(k * 16)], HttpResponseDataHexStrings[(k * 16 + 1)], HttpResponseDataHexStrings[(k * 16 + 2)], HttpResponseDataHexStrings[(k * 16 + 3)], HttpResponseDataHexStrings[(k * 16 + 4)], HttpResponseDataHexStrings[(k * 16 + 5)], HttpResponseDataHexStrings[(k * 16 + 6)], HttpResponseDataHexStrings[(k * 16 + 7)], HttpResponseDataHexStrings[(k * 16 + 8)], HttpResponseDataHexStrings[(k * 16 + 9)], HttpResponseDataHexStrings[(k * 16 + 10)], HttpResponseDataHexStrings[(k * 16 + 11)], HttpResponseDataHexStrings[(k * 16 + 12)], HttpResponseDataHexStrings[(k * 16 + 13)], HttpResponseDataHexStrings[(k * 16 + 14)], HttpResponseDataHexStrings[(k * 16 + 15)], HttpResponseDataHexStringsRealRowStrings[k] });
 					}
 					String[] ResponseHexStringsTmp = { String.valueOf(HttpResponseDataHexStringsRealRowNum), "--", "--", "--", "--", "--", "--", "--", "--", "--", "--", "--", "--", "--", "--", "--", "--", HttpResponseDataHexStringsRealRowStrings[(HttpResponseDataHexStringsRealRowNum - 1)] };
 					int HttpResponseDataHexStringsSHENGYUGESHU = HttpResponseDataHexStringsLength % 16;
 					for (int k = 0; k < HttpResponseDataHexStringsSHENGYUGESHU; k++)
 					{
 						ResponseHexStringsTmp[(k + 1)] = HttpResponseDataHexStrings[(HttpResponseDataHexStringsRowNum * 16 + k)];
 					}
 					if (HttpResponseDataHexStringsLength % 16 != 0)
 					{
 						JPtabProxyHistoryDetailResponseHexTableModel.addRow(ResponseHexStringsTmp);
 						JPtabEditorRequestDataHexTable.invalidate();
 					}
 				}
 			}

 			public void changedUpdate(DocumentEvent e)
 			{
 			}
 		});
 		TableColumn JPtabProxyEditorRequestDataHexTableFirstColumn = JPtabEditorRequestDataHexTable.getColumnModel().getColumn(0);
 		JPtabProxyEditorRequestDataHexTableFirstColumn.setPreferredWidth(120);

 		TableColumn JPtabProxyEditorRequestDataHexTableStringColumn = JPtabEditorRequestDataHexTable.getColumnModel().getColumn(17);
 		JPtabProxyEditorRequestDataHexTableStringColumn.setPreferredWidth(200);

 		JScrollPane JPtabProxyEditorRequestDataHexScrollPanel = new JScrollPane(JPtabEditorRequestDataHexTable);
 		EditorRequestDataHex.add(JPtabProxyEditorRequestDataHexScrollPanel, "Center");

 		JPanel ResponsePanel = new JPanel();

 		ResponsePanel.setLayout(new BorderLayout());

 		JPanel ResponseLabelPanel = new JPanel();
 		ResponseLabelPanel.setLayout(null);
 		ResponseLabelPanel.setPreferredSize(new Dimension(0, 20));

 		ResponsePanel.add(ResponseLabelPanel, "North");

 		JLabel ResponseLabel = new JLabel("Response ");
 		ResponseLabel.setSize(new Dimension(100, 20));
 		ResponseLabel.setLocation(10, 0);
 		ResponseLabelPanel.add(ResponseLabel);

 		JTabbedPane ResponseTabPanel = new JTabbedPane(1);

 		ResponsePanel.add(ResponseTabPanel, "Center");

 		if (editorStepShowString.length() > 0)
 		{
 			//EditorRequestDaraRawTextArea.setText(editorStepShowString);
 			insertDocument(editorStepShowString, Color.black);

 			String Str = editorStepShowString;

 			if ((JPtabEditorRequestDataShowHeadersTable != null) && (Str.length() != 0))
 			{
 				DefaultTableModel tableModel = (DefaultTableModel)JPtabEditorRequestDataShowHeadersTable.getModel();
 				tableModel.setRowCount(0);
 				String[] headerParams = Method.getHTTPHeadersParams(Str);
 				if(headerParams!=null&&!headerParams.equals(null))
 				{
 					int headerParamLength = headerParams.length / 2;
 					for (int i = 0; i < headerParamLength; i++)
 					{
 						tableModel.addRow(new Object[] { headerParams[(i * 2)].trim(), headerParams[(i * 2 + 1)].trim() });
 					}
 					JPtabEditorRequestDataShowHeadersTable.invalidate();
 			
 				}
 			}
 			if ((Str != null) && (!Str.equals(null)) && (Str.length() != 0))
 			{
 				String[] HttpResponseDataHexStrings = Method.bytesToHexStrings(Str.getBytes());
 				int HttpResponseDataHexStringsLength = HttpResponseDataHexStrings.length;
 				int HttpResponseDataHexStringsRowNum = HttpResponseDataHexStringsLength / 16;
 				int HttpResponseDataHexStringsRealRowNum = 0;
 				if (HttpResponseDataHexStringsLength % 16 == 0)
 				{
 					HttpResponseDataHexStringsRealRowNum = HttpResponseDataHexStringsRowNum;
 				}
 				else
 				{
 					HttpResponseDataHexStringsRealRowNum = HttpResponseDataHexStringsRowNum + 1;
 				}

 				String[] HttpResponseDataHexStringsRealRowStrings = new String[HttpResponseDataHexStringsRealRowNum];
 				String HttpResponseDataHexStringsRealRowStringTmp = "";
 				char[] ResponseStringchars = Str.toCharArray();
 				int ResponseStringcharsLength = ResponseStringchars.length;
 				int i = 1; for (int j = 0; (i <= ResponseStringcharsLength) && (j < HttpResponseDataHexStringsRealRowNum); i++)
 				{
 					HttpResponseDataHexStringsRealRowStringTmp = HttpResponseDataHexStringsRealRowStringTmp + ResponseStringchars[(i - 1)];
 					if (i % 16 == 0)
 					{
 						HttpResponseDataHexStringsRealRowStrings[j] = HttpResponseDataHexStringsRealRowStringTmp;
 						HttpResponseDataHexStringsRealRowStringTmp = "";
 						j++;
 					}
 				}
 				HttpResponseDataHexStringsRealRowStrings[(HttpResponseDataHexStringsRealRowNum - 1)] = HttpResponseDataHexStringsRealRowStringTmp;

 				DefaultTableModel JPtabProxyHistoryDetailResponseHexTableModel = (DefaultTableModel)JPtabEditorRequestDataHexTable.getModel();
 				JPtabProxyHistoryDetailResponseHexTableModel.setRowCount(0);

 				for (int k = 0; k < HttpResponseDataHexStringsRowNum; k++)
 				{
 					JPtabProxyHistoryDetailResponseHexTableModel.addRow(new Object[] { Integer.valueOf(k + 1), HttpResponseDataHexStrings[(k * 16)], HttpResponseDataHexStrings[(k * 16 + 1)], HttpResponseDataHexStrings[(k * 16 + 2)], HttpResponseDataHexStrings[(k * 16 + 3)], HttpResponseDataHexStrings[(k * 16 + 4)], HttpResponseDataHexStrings[(k * 16 + 5)], HttpResponseDataHexStrings[(k * 16 + 6)], HttpResponseDataHexStrings[(k * 16 + 7)], HttpResponseDataHexStrings[(k * 16 + 8)], HttpResponseDataHexStrings[(k * 16 + 9)], HttpResponseDataHexStrings[(k * 16 + 10)], HttpResponseDataHexStrings[(k * 16 + 11)], HttpResponseDataHexStrings[(k * 16 + 12)], HttpResponseDataHexStrings[(k * 16 + 13)], HttpResponseDataHexStrings[(k * 16 + 14)], HttpResponseDataHexStrings[(k * 16 + 15)], HttpResponseDataHexStringsRealRowStrings[k] });
 				}
 				String[] ResponseHexStringsTmp = { String.valueOf(HttpResponseDataHexStringsRealRowNum), "--", "--", "--", "--", "--", "--", "--", "--", "--", "--", "--", "--", "--", "--", "--", "--", HttpResponseDataHexStringsRealRowStrings[(HttpResponseDataHexStringsRealRowNum - 1)] };
 				int HttpResponseDataHexStringsSHENGYUGESHU = HttpResponseDataHexStringsLength % 16;
 				for (int k = 0; k < HttpResponseDataHexStringsSHENGYUGESHU; k++)
 				{
 					ResponseHexStringsTmp[(k + 1)] = HttpResponseDataHexStrings[(HttpResponseDataHexStringsRowNum * 16 + k)];
 				}
 				if (HttpResponseDataHexStringsLength % 16 != 0)
 				{
 					JPtabProxyHistoryDetailResponseHexTableModel.addRow(ResponseHexStringsTmp);
 					JPtabEditorRequestDataHexTable.invalidate();
 				}

 			}

 		}

 		JDesktopPane EditorResponseDataRawPane = new JDesktopPane();
 		EditorResponseDataRawPane.setFocusable(false);
 		EditorResponseDataRawPane.setLayout(new BorderLayout());
 		ResponseTabPanel.add(EditorResponseDataRawPane, "Raw");

 		EditorResponseDaraRawTextArea = new JTextArea();
 		EditorResponseDaraRawTextArea.setLineWrap(true);
 		EditorResponseDaraRawTextArea.setEditable(false);
 		JScrollPane EditorResponseDaraRawScrollPane = new JScrollPane(EditorResponseDaraRawTextArea);
 		EditorResponseDataRawPane.add(EditorResponseDaraRawScrollPane, "Center");

 		JDesktopPane EditorResponseDataHeaders = new JDesktopPane();
 		EditorResponseDataHeaders.setFocusable(false);
 		EditorResponseDataHeaders.setLayout(new BorderLayout());
 		ResponseTabPanel.add(EditorResponseDataHeaders, "Headers");

 		String[] JPtabEditorResponseHeadersTableHeaders = { "Name", "Value" };
 		Object[][] JPtabEditorResponseHeadersTableCellData = null;
 		DefaultTableModel JPtabEditorResponseHeadersTableModel = new DefaultTableModel(JPtabEditorResponseHeadersTableCellData, JPtabEditorResponseHeadersTableHeaders) {
 			public boolean isCellEditable(int row, int column) {
 				return true;
 			}
 		};
 		JPtabEditorResponseHeadersTable = new JTable(JPtabEditorResponseHeadersTableModel);
 		JScrollPane JPtabProxyHistoryDetailRequestHeadersTableScollPanel = new JScrollPane(JPtabEditorResponseHeadersTable);
 		EditorResponseDataHeaders.add(JPtabProxyHistoryDetailRequestHeadersTableScollPanel, "Center");

 		JDesktopPane EditorResponseDataHex = new JDesktopPane();
 		EditorResponseDataHex.setLayout(new BorderLayout());
 		EditorResponseDataHex.setFocusable(false);
 		ResponseTabPanel.add(EditorResponseDataHex, "Hex");

 		String[] JPtabEditorResponseHexTableHeaders = { "Num", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "String" };
 		Object[][] JPtabEditorResponseHexTableCellData = null;
 		DefaultTableModel JPtabEditorResponsetHexTableModel = new DefaultTableModel(JPtabEditorResponseHexTableCellData, JPtabEditorResponseHexTableHeaders) {
 			public boolean isCellEditable(int row, int column) {
 				return true;
 			}
 		};
 		JPtabEditorResponseHexTable = new JTable(JPtabEditorResponsetHexTableModel) 
 		{ 
 			public boolean isCellEditable(int row, int column) { return false; }

 		};
 		JPtabEditorResponseHexTable.getTableHeader().setVisible(false);
 		JPtabEditorResponseHexTable.setCellSelectionEnabled(false);

 		TableColumn JPtabEditorResponseHexTableFirstColumn = JPtabEditorResponseHexTable.getColumnModel().getColumn(0);
 		JPtabEditorResponseHexTableFirstColumn.setPreferredWidth(120);

 		TableColumn JPtabEditorResponseHexTableStringColumn = JPtabEditorResponseHexTable.getColumnModel().getColumn(17);
 		JPtabEditorResponseHexTableStringColumn.setPreferredWidth(200);

 		JScrollPane JPtabEditorResponseHexScrollPanel = new JScrollPane(JPtabEditorResponseHexTable);
 		EditorResponseDataHex.add(JPtabEditorResponseHexScrollPanel, "Center");

 		JSplitPane SplitHistoryPanel = new JSplitPane(0, true, RequestTabPanel, ResponsePanel);
 		RequestResponseDataPanel.add(SplitHistoryPanel, "Center");
 	}

 	public static void EditorStepShowRawHeadersHexUpdate()
 	{
 	}
 	
 	
 	
 	public void insertDocument(String text , Color textColor)//根据传入的颜色及文字，将文字插入文本域
 	 {
 		 SimpleAttributeSet set = new SimpleAttributeSet();
 		 StyleConstants.setForeground(set, textColor);//设置文字颜色
 		 StyleConstants.setFontSize(set, 12);//设置字体大小
 		 Document doc = EditorRequestDaraRawTextArea.getStyledDocument();
 		 try
 		 {
 			 doc.insertString(doc.getLength(), text, set);//插入文字
 	   
 		 }
 		 catch (BadLocationException e)
 		 {
 		 }
 	 }
 	
 	
 	
}







 class RolloverRenderer extends DefaultTableCellRenderer implements MouseInputListener {
	 
	 int row = -1;
	 int col = -1;
	 JTable table = null;
	 DefaultTableModel tableModle;
	 JDialog siteEditDialog;
	 JTextArea aboutStrings;
	 JFrame frame;
	 public RolloverRenderer(TableModel tableModle,JFrame frame)
	 {
		 this.tableModle = (DefaultTableModel)tableModle;
		 this.frame = frame;
		 
		 this.siteEditDialog = new JDialog();
		 siteEditDialog.setLayout(new BorderLayout());	
		 siteEditDialog.setFocusable(false);
		 siteEditDialog.setSize(300, 180);
		 siteEditDialog.setAlwaysOnTop(true);
		 aboutStrings = new JTextArea();
	 	 aboutStrings.setLineWrap(true);
	 	 siteEditDialog.add(aboutStrings,BorderLayout.CENTER);
	 }
	 
	 			
	 
	
	 
	 
	 
	 
	 
	 
	 public Component getTableCellRendererComponent(JTable table,Object value, boolean isSelected, boolean hasFocus, int row,int column) 
	 {
		 this.table = table;
		 super.getTableCellRendererComponent(table, value, isSelected,hasFocus, row, column);
		 
		 int absoluteX = frame.getX();
		 int absoluteY = frame.getY();
		 
		 
		 if (row == this.row && column == this.col) 
		 {

			 
			 String tmp = (String) tableModle.getValueAt(row, column);
			 
			 String aboutFileString = "Exps/"+tmp+"/about.txt";

			// System.out.println(aboutFileString);
			 /*
			File aboutFile = new File(aboutFileString);
			String aboutFileBufferString="";
			String readLineTmp="";
			try
			{
				InputStreamReader aboutFileInputSteamReadTmp = new InputStreamReader(new FileInputStream(aboutFile));
				BufferedReader aboutFileBufferedReader = new BufferedReader(aboutFileInputSteamReadTmp);
				while((readLineTmp=aboutFileBufferedReader.readLine())!=null)
				{
					aboutFileBufferString = aboutFileBufferString+readLineTmp+"\n";
				}

				aboutStrings.setText("");
				siteEditDialog.setLocation(absoluteX+160, absoluteY+75+row*16);
				//siteEditDialog.setAlwaysOnTop(true);
				frame.requestFocus();
				siteEditDialog.setTitle(tmp);
			 	siteEditDialog.setVisible(true);		 		
			 	aboutStrings.setText(aboutFileBufferString);
			 		
			}
			catch(Exception es)
			{
				JOptionPane.showMessageDialog(null, "找不到about.txt文件", "Warning", 2);
			}
			 
			 */
			 
			 
			 
		 }
		 else if (isSelected) 
		 {
			 setForeground(table.getSelectionForeground());
			 setBackground(table.getSelectionBackground());
			
		 } 
		 else 
		 {
			 setBackground(Color.white);
			 siteEditDialog.setVisible(false);	
		 }
		 return this;
	 }
	 public void mouseExited(MouseEvent e) 
	 {
		 if (table != null)
		 {
			 int oldRow = row;
			 int oldCol = col;
			 row = -1;
			 col = -1;
			 if (oldRow != -1 && oldCol != -1)
			 {
				 Rectangle rect = table.getCellRect(oldRow, oldCol, false);
				 table.repaint(rect);
			 }
		 }
	 }
	 
	 public void mouseDragged(MouseEvent e) 
	 {
		 mouseMoved(e);
	 }
	 
	 public void mouseMoved(MouseEvent e) 
	 {
		 if (table != null) 
		 {
			 Point p = e.getPoint();
			 int oldRow = row;
			 int oldCol = col;
			 row = table.rowAtPoint(p);
			 col = table.columnAtPoint(p);
			 if (oldRow != -1 && oldCol != -1)
			 {
				 Rectangle rect = table.getCellRect(oldRow, oldCol, false);
				 table.repaint(rect);
			 }
			 if (row != -1 && col != -1) 
			 {
				 Rectangle rect = table.getCellRect(row, col, false);
				 table.repaint(rect);
			 }
		 }
	 }

	 public void mouseClicked(MouseEvent e) 
	 {
	 }
	 public void mousePressed(MouseEvent e) 
	 {
	 }
	 public void mouseReleased(MouseEvent e)
	 {
	 }
	 public void mouseEntered(MouseEvent e) 
	 {
	 }
 }