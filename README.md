2015-1-4 update

增加了Struts 利用Exp和drupal 注入利用Exp

1、任何一个HTTP请求中，$host$  都会被替换成目标地址


2、Example: 
	
	Target:www.xxx.com/aaa/123.jsp

  	GET /www/index.html HTTP/1.1  >>  GET /aaa/123.jsp/www/index.html

	GET $1$/www/index.html HTTP/1.1  >>  GET /aaa/www/index.html
			
