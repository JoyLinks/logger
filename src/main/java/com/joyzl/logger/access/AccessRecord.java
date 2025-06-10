package com.joyzl.logger.access;

import java.net.InetSocketAddress;

/**
 * Apache Common Log Format
 * 
 * @author ZhangXi 2024年12月12日
 */
public interface AccessRecord {

	/*-
	 * Apache 定义的日志内容参考
	 * 
	 * %a	请求的客户端IP地址(Remote IP-address)
	 * %A	本地IP地址(Local IP-address)
	 * %B	响应的大小（以字节为单位），不包括 HTTP 标头
	 * %b	请求的大小（以字节为单位），不包括 HTTP 标头
	 * %D	处理请求所需的时间（以微秒为单位）
	 * %f	Filename.
	 * %h	远程主机名
	 * %H	请求协议
	 * %m	请求方法
	 * %p	处理请求的服务器的规范端口
	 * %q	查询字符串
	 * %r	请求的第一行
	 * %R	生成响应的处理程序（如果有）
	 * %s	响应状态
	 * %t	收到请求的时间
	 * %T	处理请求所需的时间（毫秒）
	 * %u	如果请求已经过身份验证，则为远程用户
	 * %U	请求的 URL 路径，不包括任何查询字符串
	 * %X	响应完成时的连接状态
	 * 
	 * 例：
	 * %h %l %u %t \"%r\" %>s %b
	 * 127.0.0.1 user-identifier frank [10/Oct/2000:13:55:36 -0700] "GET /apache_pb.gif HTTP/1.0" 200 2326
	 * 
	 * 修正后的日志格式
	 * 请求响应为单行日志，由时间戳开始便于检索
	 * 主要考虑性能减少日志对服务性能的影响
	 * 不记录任何用户信息
	 * 
	 * %t	收到请求的时间
	 * %A	服务端口
	 * 
	 * %a	请求的客户端IP地址(Remote IP-address)
	 * %r	请求的第一行
	 * %b	请求的大小（以字节为单位），不包括 HTTP 标头
	 * 
	 * %R	生成响应的处理程序(Servlet)
	 * %T	处理请求所需的时间（毫秒）
	 * %s	响应状态
	 * %B	响应的大小（以字节为单位），不包括 HTTP 标头
	 */

	/** 服务端口 */
	int serverPort();

	/** 请求的客户端地址 */
	InetSocketAddress remoteAddress();

	/** 请求接收时间戳 */
	long requestTimestamp();

	/** 请求的方法 */
	String requestMethod();

	/** 请求的资源路径 */
	String requestURI();

	/** 请求的版本 */
	String requestVersion();

	/** 请求体大小 */
	int requestBodySize();

	/** 处理程序 */
	String servletName();

	/** 处理用时(毫秒) */
	int servletSpend();

	/** 响应状态 */
	int responseStatus();

	/** 响应体大小 */
	int responseBodySize();
}