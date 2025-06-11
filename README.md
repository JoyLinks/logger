# JOYZL logger

轻量化高性能（部分）日志组件，没有任何其它依赖。
日志字符编码固定为UTF-8。

### Logger

常规日志，用于热点代码之外同步输出运行日志。
* 支持 ERROR INFO DEBUG 三级日志；
* 支持控制台同步输出；
* 支持网络端口同步发送；
* 自动按日期切分日志文件；
* 支持清理过期的日志文件；
* 全局静态无须实例化。

```log
12:41:53.883	main	INFOM	TEST
```

从左至右含义为：时间 线程名 消息类型 内容文本，前三个字段固定。

### AccessLogger

访问日志，采用异步模式尽可能减少对业务性能的影响；
与 Apache Common Log Format 类似但不完全相同，采用固定的格式输出，每行首添加时间戳以便于检索，不支持格式配置。
* 自动按日期切分日志文件；
* 支持按时间段从生成的日志文件检索日志；
* 支持清理过期的日志文件；
* 支持按需多实例。

日志输出格式样例：

```log
1749550882032 18:21:22.32 80 www.joyzl.com 192.168.0.1 GET /web HTTP/1.1 0 WEBDAV 0 200 0
```

从左至右含义为：请求时间戳(UTC) 时间 服务器端口 客户端地址 请求方法 请求路径 协议版本 请求体字节数 服务程序 处理用时（毫秒） 响应状态 响应体字节数

```java
final AccessLogger logger = new AccessLogger("access\\acs.log");
logger.record(new AccessRecord(){
	// 字段值
	public int getServerPort(){
		return 80;
	}
	...
});

```

### CommonLogger

符合 RFC6872, RFC6873 规范的CLF(Common Log Format)日志，采用同步输出以确保日志被如实记录；
对业务性能有影响，适用于强日志场景。
* 自动按日期切分日志文件；
* 支持按时间段从生成的日志文件检索日志；
* 支持清理过期的日志文件；
* 支持按需多实例。

日志输出格式样例：

```log
A000493,0053005C005E006D007D008F009E00A000BA00C700EB00F70100
1328821153.010	RORUU	1 INVITE	-	sip:192.0.2.10	192.0.2.10:5060	192.0.2.200:56485	sip:192.0.2.10	-	sip:1001@example.com:5060	DL88360fa5fc	DL70dff590c1-1079051554@example.com	S1781761-88	C67651-11	00@00000000,001C,00,Contact: <sip:bob@192.0.2.4>	00@00000000,0016,00,Reason-Phrase: Ringing	01@00000000,00A9,00,application/sdp v=0%0D%0Ao=alice 2890844526 2890844526 IN IP4 host.example.com%0D%0As=-%0D%0Ac=IN IP4 host.example.com%0D%0At=0 0%0D%0Am=audio 49170 RTP/AVP 0 8 97%0D%0A	01@00000000,0216,01,multipart/mixed;boundary=7a9cbec02ceef655 MIIBUgYJKoZIhvcNAQcCoIIBQzCCAT8CAQExCTAHBgUrDgMCGjALBgkqhkiG9w0BBwExggEgMIIB%0D%0AHAIBATB8MHAxCzAJBgNVBAYTAlVTMRMwEQYDVQQIEwpDYWxpZm9ybmlhMREwDwYDVQQHEwhTYW4g%0D%0ASm9zZTEOMAwGA1UEChMFc2lwaXQxKTAnBgNVBAsTIFNpcGl0IFRlc3QgQ2VydGlmaWNhdGUgQXV0%0D%0AaG9yaXR5AggBlQBxAjMBEzAHBgUrDgMCGjANBgkqhkiG9w0BAQEFAASBgI70ZvlI8FIt0uWXjp2V%0D%0Aquny/hWgZllxYpLo2iqo2DUKaM7/rjy9K/8Wdd3VZI5ZPdZHKPJiIPfpQXSeMw2aFe2r25PRDEIQ%0D%0ALntyidKcwMmuLvvHwM/5Fy87An5PwCfhVG3ktqo6uz5mzMtd1sZLg4MUnLjm/xgtlE/le2W8mdAF%0D%0A	03@00032473,0014,00,a=rtpmap:0 PCMU/8000	07@00032473,0010,00,1877 example.com
```


```java
final CommonLogger logger = new CommonLogger("common\\clf.log");
logger.record(new CommonRecord(){
	// 字段值
	public char getDirection(){
		return CommonCodes.RECEIVED;
	}
	...
});

```


### 日志文件过期删除

LoggerService 类提供日志过期删除功能，默认保留30天的日志文件，以防止过多的日志文件导致存储空间紧张。
日志组件没有自动建立守护进程以自动运行清理功能，需要此功能时应在守护进程定时调用清理方法 LoggerService.clean()，
此方法将返回 LoggerCleaner 对象实例，可获取删除的文件数量和释放的存储空间。

日志有效期为全局参数，将同时清理 Logger 、AccessLogger 和 CommonLogger 当前产生的日志；
注意：仅能清理运行时日志实例关联输出的目录中的日志文件；
不要将其它文件放入日志目录中，清理程序根据文件扩展名判断日志文件，以防你的文件被意外删除。

```log
LoggerCleaner c = LoggerService.clean();
System.out.println(c);
```

### 日志配置

没有默认配置文件，如果需要可以自定义配置文件，将其读取后通过日志对象设置；
参数支持运行时设置，设置的参数将立即生效并影响之后的日志输出。

```java
// 设置日志级别 ERROR=1,INFO=2,DEBUG=3
// 设置级别为0将不会输出任何日志，大于3的值等同于3
Logger.setLevel(3);

// 设置输出异常时的缩进
Logger.setIndent("------------");

// 设置分隔符，仅用于固定字段
Logger.setTab('\t');

// 设置换行符
Logger.setLine('\n');

// 设置控制台输出
Logger.setConsole(true);

// 设置日志目录和文件名
// 生成日志文件：当前程序目录\log\joyzl-20250611.log
Logger.setFile("log", "joyzl", ".log");

// 生成日志文件：当前程序目录\20250611.log
Logger.setFile("", null, ".log");

// 生成日志文件：当前程序目录\20250611
Logger.setFile("", null, null);

// 如果目录为null将关闭文件输出
Logger.setFile(null, null, null);

// 设置日志输出的网络目标(UDP)
Logger.setUDP("192.168.0.2", 8210);

// 如果主机为null或端口为0将关闭网络输出
Logger.setUDP(null, 0);

// 输出日志
Logger.info("test");
Logger.debug("text1","text2");
Logger.error("text1");
Logger.error(new Exception("TEST"));


// 设置日志过期天数
LoggerService.setExpires(30);
// 清理过期日志
// 如果有 AccessLogger 和 CommonLogger 实例，其输出的日志也将被清理。
LoggerService.clean();


```

### 

[www.joyzl.com](http://www.joyzl.com)
