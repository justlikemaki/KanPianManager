package com.meteor.kit.http;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;
import com.meteor.kit.DateKit;
import com.meteor.kit.JsonKit;

/**
 * httpclient 帮助类
 */
public class HttpClientHelp {
	private static final Logger logger = LoggerFactory.getLogger(HttpClientHelp.class);
	/**
	 * The constant TEXT_MIME.
	 */
	public static final String TEXT_MIME = "text/plain";
	/**
	 * The constant HTML_MIME.
	 */
	public static final String HTML_MIME = "text/html";
	/**
	 * The constant XML_MIME.
	 */
	public static final String XML_MIME = "application/xml";
	/**
	 * The constant JSON_MIME.
	 */
	public static final String JSON_MIME = "application/json";
	/**
	 * The constant JSON_FORM.
	 */
	public static final String JSON_FORM = "application/x-www-form-urlencoded";

	private static final int DEFAULT_CONN_TIMEOUT_MILLISECONDS = 30 * 1000;
	private static final int DEFAULT_READ_TIMEOUT_MILLISECONDS = 30 * 1000;

	private static final int CLOSE_INACTIVE_CONNECTIONS_SECONDS = 30;

	private static final int DEFAULT_MAX_TOTAL_CONNECTIONS = 100;
	private static final int DEFAULT_MAX_CONNECTIONS_PER_ROUTE = 25;

	public static String HTTP_ENCODING = "UTF-8";
	// 文件下载根目录
	private static final String fileroot = System.getProperty("catalina.home") + "/temp/download/";

	private static PoolingHttpClientConnectionManager connectionManager;
	private static CloseableHttpClient httpClient = null;

	private static PoolingHttpClientConnectionManager other_connectionManager;
	private static CloseableHttpClient other_httpClient = null;

	private static PoolingHttpClientConnectionManager tmp_connectionManager;
	private static CloseableHttpClient tmp_httpClient = null;
	
	static {

		try {
			// 需要通过以下代码声明对https连接支持//忽略ssl证书
			KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			SSLContext sslcontext = SSLContexts.custom().loadTrustMaterial(trustStore, new HttpClientHelpTrustStrategy()).build();
			HostnameVerifier hostnameVerifier = new TrustAnyHostnameVerifier();
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, hostnameVerifier);
			Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create().register("http", PlainConnectionSocketFactory.getSocketFactory()).register("https", sslsf).build();
			connectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
			// 将最大连接数增加到100
			connectionManager.setMaxTotal(DEFAULT_MAX_TOTAL_CONNECTIONS);
			// 将每个路由基础的连接增加到25
			connectionManager.setDefaultMaxPerRoute(DEFAULT_MAX_CONNECTIONS_PER_ROUTE);
			
			other_connectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
			// 将最大连接数增加到100
			other_connectionManager.setMaxTotal(DEFAULT_MAX_TOTAL_CONNECTIONS);
			// 将每个路由基础的连接增加到25
			other_connectionManager.setDefaultMaxPerRoute(DEFAULT_MAX_CONNECTIONS_PER_ROUTE);
			
			tmp_connectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
			// 将最大连接数增加到100
			tmp_connectionManager.setMaxTotal(DEFAULT_MAX_TOTAL_CONNECTIONS);
			// 将每个路由基础的连接增加到25
			tmp_connectionManager.setDefaultMaxPerRoute(DEFAULT_MAX_CONNECTIONS_PER_ROUTE);
		} catch (Exception e) {
			logger.error("初始化cm线程池异常", e);
		}

	}

	public static String getFileroot() {
		return fileroot;
	}

	public static String doPost(String url) throws Exception {
		return doPost(url, Boolean.FALSE);
	}

	public static String doPost(String url, boolean keepAlive) throws Exception {
		return doPost(url, null, null, null, null, HTTP_ENCODING, HTTP_ENCODING, Boolean.FALSE, keepAlive);
	}

	public static String doPost(String url, final String entities, String mimeType, boolean keepAlive) throws Exception {
		return doPost(url, entities, mimeType, HTTP_ENCODING, keepAlive);
	}

	public static String doPost(String url, final String entities, String mimeType, String encoding, boolean keepAlive) throws Exception {
		return doPost(url, entities, mimeType, encoding, Boolean.FALSE, keepAlive);
	}

	public static String doPost(String url, final String entities, String mimeType, String encoding, boolean needCookie, boolean keepAlive) throws Exception {
		return doPost(url, entities, mimeType, null, encoding, needCookie, keepAlive);
	}

	public static String doPost(String url, final String entities, String mimeType, Map<String, String> headers, String encoding, boolean needCookie, boolean keepAlive) throws Exception {
		return doPost(url, entities, mimeType, null, headers, encoding, encoding, needCookie, keepAlive);
	}

	public static String doPost(String url, Map<String, String> params, boolean keepAlive) throws Exception {
		return doPost(url, params, HTTP_ENCODING, keepAlive);
	}

	public static String doPost(String url, Map<String, String> params, String encoding, boolean keepAlive) throws Exception {
		return doPost(url, params, encoding, Boolean.FALSE, keepAlive);
	}

	public static String doPost(String url, Map<String, String> params, String encoding, boolean needCookie, boolean keepAlive) throws Exception {
		return doPost(url, params, null, encoding, needCookie, keepAlive);
	}

	public static String doPost(String url, Map<String, String> params, Map<String, String> headers, String encoding, boolean needCookie, boolean keepAlive) throws Exception {
		return doPost(url, null, null, params, headers, encoding, encoding, needCookie, keepAlive);
	}

	public static String doPost(String url, Map<String, String> headers) throws Exception {
		return doPost(url, null, headers);
	}

	public static String doPost(String url, Map<String, String> params, Map<String, String> headers) throws Exception {
		return doPost(url, null, null, params, headers, HTTP_ENCODING, HTTP_ENCODING, Boolean.FALSE, Boolean.FALSE);
	}

	public static String doGet(String url) throws Exception {
		return doGet(url, Boolean.FALSE);
	}

	public static String doGet(String url, boolean keepAlive) throws Exception {
		return doGet(url, null, null, HTTP_ENCODING, HTTP_ENCODING, Boolean.FALSE, keepAlive);
	}

	public static String doGet(String url, Map<String, String> params, boolean keepAlive) throws Exception {
		return doGet(url, params, HTTP_ENCODING, keepAlive);
	}

	public static String doGet(String url, Map<String, String> params, String encoding, boolean keepAlive) throws Exception {
		return doGet(url, params, encoding, Boolean.FALSE, keepAlive);
	}

	public static String doGet(String url, Map<String, String> params, String encoding, boolean needCookie, boolean keepAlive) throws Exception {
		return doGet(url, params, null, encoding, encoding, needCookie, keepAlive);
	}

	public static String doGet(String url, Map<String, String> headers) throws Exception {
		return doGet(url, null, headers);
	}

	public static String doGet(String url, Map<String, String> params, Map<String, String> headers) throws Exception {
		return doGet(url, params, headers, HTTP_ENCODING, HTTP_ENCODING, Boolean.FALSE, Boolean.FALSE);
	}
	
	public static Map<String, String> getDefaultHeader(){
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Accept-Language", "zh-CN,zh;q=0.8");
		headers.put("Connection", "Keep-Alive");
		headers.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.63 Safari/537.36 QIHU 360SE");
		headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		headers.put("Accept-Encoding", "gzip,deflate,sdch");
		headers.put("Cache-Control", "no-cache");
		return headers;
	}

	private static void logUrl(HttpRequestBase requestBase) throws MalformedURLException {
		logger.info(String.format("发送请求:%s:%s | %s ", requestBase.getURI().getHost(), requestBase.getURI().getPort(), requestBase.getURI().toURL()));
	}

	private static void validateResponse(CloseableHttpResponse response, HttpRequestBase requestBase, Long timeSpan) throws Exception {
		StatusLine status = response.getStatusLine();
		if (response.getStatusLine().getStatusCode() >= 400) {
			response.close();
			throw new IOException("Got bad response, error code = " + response.getStatusLine().getStatusCode());
//			logger.error(String.format(" %s:%s | %s | %s | %s", requestBase.getURI().getHost(), requestBase.getURI().getPort(), status.getStatusCode(), timeSpan, requestBase.getURI().toURL()));
		}else{
			logger.info(String.format(" %s:%s | %s | %s | %s", requestBase.getURI().getHost(), requestBase.getURI().getPort(), status.getStatusCode(), timeSpan, requestBase.getURI().toURL()));
		}
	}

	private static String getResult(String url, String encoding, CloseableHttpResponse response) throws Exception {
		String responseContent = "";
		HttpEntity entity = response.getEntity();
		if (entity == null) {
			logger.warn(String.format("Http entity is null! 请求地址 {%s},response status is {%s}", url, response.getStatusLine()));
		} else {
			responseContent = EntityUtils.toString(entity, encoding);
			EntityUtils.consume(entity);
		}
		return responseContent;
	}
	
	public static CloseableHttpClient getTmpHttpClient() {
		tmp_connectionManager.closeExpiredConnections();
		// 可选的, 关闭自定义秒内不活动的连接
		tmp_connectionManager.closeIdleConnections(CLOSE_INACTIVE_CONNECTIONS_SECONDS, TimeUnit.SECONDS);

		if (tmp_httpClient == null) {
			tmp_httpClient = HttpClients.custom().setConnectionManager(other_connectionManager).build();
		}
		return tmp_httpClient;
	}

	public static CloseableHttpClient getOtherHttpClient() {
		other_connectionManager.closeExpiredConnections();
		// 可选的, 关闭自定义秒内不活动的连接
		other_connectionManager.closeIdleConnections(CLOSE_INACTIVE_CONNECTIONS_SECONDS, TimeUnit.SECONDS);

		if (other_httpClient == null) {
			if (PropKit.get("isproxy_other").equals("1")) {
				String host = PropKit.get("host");
				int port = PropKit.getInt("port");
				other_httpClient = HttpClients.custom().setProxy(new HttpHost(host, port)).setConnectionManager(connectionManager).build();
			} else {
				other_httpClient = HttpClients.custom().setConnectionManager(other_connectionManager).build();
			}
		}
		return other_httpClient;
	}

	private static CloseableHttpClient getHttpClient() {
		connectionManager.closeExpiredConnections();
		// 可选的, 关闭自定义秒内不活动的连接
		connectionManager.closeIdleConnections(CLOSE_INACTIVE_CONNECTIONS_SECONDS, TimeUnit.SECONDS);

		if (httpClient == null) {
			if (PropKit.get("isproxy").equals("1")) {
				String host = PropKit.get("host");
				int port = PropKit.getInt("port");
				httpClient = HttpClients.custom().setProxy(new HttpHost(host, port)).setConnectionManager(connectionManager).build();
			} else {
				httpClient = HttpClients.custom().setConnectionManager(connectionManager).build();
			}
		}
		return httpClient;
	}

	public static RequestConfig getRequestConfig(boolean needCookie) {
		Builder unbRequestConfig = RequestConfig.custom().setMaxRedirects(3)// 设置最大跳转数
				.setSocketTimeout(DEFAULT_READ_TIMEOUT_MILLISECONDS).setConnectTimeout(DEFAULT_CONN_TIMEOUT_MILLISECONDS).setConnectionRequestTimeout(DEFAULT_CONN_TIMEOUT_MILLISECONDS);
		if (needCookie) {
			unbRequestConfig.setCookieSpec(CookieSpecs.STANDARD_STRICT);
		} else {
			unbRequestConfig.setCookieSpec(CookieSpecs.IGNORE_COOKIES);
		}
		return unbRequestConfig.build();
	}

	private static String doRequest(String url, HttpRequestBase request, String resultEncoding, boolean keepAlive) throws Exception {
		CloseableHttpResponse response = null;
		try {
			logUrl(request);
			if (!keepAlive) {
				request.setHeader("Connection", "close");
			}
			StopWatch watch = new StopWatch();
			watch.start();
			httpClient = getHttpClient();
			response = httpClient.execute(request);
			watch.stop();
			validateResponse(response, request, watch.getTime());
			return getResult(url, resultEncoding, response);
		} catch (ConnectTimeoutException e) {
			logger.error(String.format("%s %s:%s | %s | %s | %s", e.getMessage(), request.getURI().getHost(), request.getURI().getPort(), 408.0, DEFAULT_CONN_TIMEOUT_MILLISECONDS, request.getURI().toURL()));
			throw e;
		} catch (SocketTimeoutException e) {
			logger.error(String.format("%s %s:%s | %s | %s | %s", e.getMessage(), request.getURI().getHost(), request.getURI().getPort(), 408.1, DEFAULT_READ_TIMEOUT_MILLISECONDS, request.getURI().toURL()));
			throw e;
		} finally {
			if (response != null)
				response.close();
			request.abort();
			request.releaseConnection();
		}
	}

	/**
	 * 
	 * @Title doPost
	 * @param url
	 * @param entities
	 * @param mimeType
	 * @param params
	 * @param headers
	 * @param encoding
	 * @param resultEncoding
	 * @param needCookie
	 * @param keepAlive
	 * @return
	 * @throws Exception
	 *             String 返回类型
	 * @category post方法请求url，可传流(或传参数)，header，编码方式(请求和响应)，是否签名，是否保持长连接
	 */
	public static String doPost(String url, final String entities, String mimeType, Map<String, String> params, Map<String, String> headers, String encoding, String resultEncoding, boolean needCookie, boolean keepAlive) throws Exception {
		HttpPost httpPost = new HttpPost(url);
		// 要么传字符串，要么传参数
		if (StringUtils.isNotBlank(entities) && StringUtils.isNotBlank(mimeType)) {
			StringEntity entity = new StringEntity(entities, ContentType.create(mimeType, encoding));
			httpPost.setEntity(entity);
		} else if (params != null && !params.isEmpty()) {
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			for (Map.Entry<String, String> entry : params.entrySet()) {
				nameValuePairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
			}
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, encoding));
		}
		// 重设或新增header
		if (headers != null && !headers.isEmpty()) {
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				httpPost.setHeader(entry.getKey(), entry.getValue());
			}
		}
		// 保持长连接必须设置发送内容长度
		if (keepAlive) {
			httpPost.setHeader("Content-Length", String.valueOf(httpPost.getEntity().getContentLength()));
		}
		httpPost.setConfig(getRequestConfig(needCookie));
		return doRequest(url, httpPost, resultEncoding, keepAlive);
	}

	/**
	 * 
	 * @Title doGet
	 * @param url
	 * @param params
	 * @param headers
	 * @param encoding
	 * @param resultEncoding
	 * @param needCookie
	 * @param keepAlive
	 * @return
	 * @throws Exception
	 *             String 返回类型
	 * @category get方法请求url，可传参数，header，编码方式(请求和响应)，是否签名，是否保持长连接
	 */
	public static String doGet(String url, Map<String, String> params, Map<String, String> headers, String encoding, String resultEncoding, boolean needCookie, boolean keepAlive) throws Exception {
		// 增加参数
		URIBuilder builder = new URIBuilder(url);
		if (params != null && !params.isEmpty()) {
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			for (Map.Entry<String, String> entry : params.entrySet()) {
				nameValuePairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
			}
			builder.setCustomQuery(URLEncodedUtils.format(nameValuePairs, encoding));
		}
		HttpGet httpget = new HttpGet(builder.build());
		// 重设或新增header
		if (headers != null && !headers.isEmpty()) {
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				httpget.setHeader(entry.getKey(), entry.getValue());
			}
		}
		httpget.setConfig(getRequestConfig(needCookie));
		return doRequest(url, httpget, resultEncoding, keepAlive);
	}

	// 手动释放请求链接
	public static void releaseMethod(HttpRequestBase hr) {
		if (hr != null) {
			hr.abort();
			hr.releaseConnection();
		}
	}

	// 手动关闭http客户端
	public static boolean closeHttpClient(CloseableHttpClient httpClient) {
		try {
			if (httpClient != null) {
				httpClient.close();
				httpClient = null;
			}
			return true;
		} catch (Exception e) {
			logger.error("关闭httpclient异常", e);
			return false;
		}
	}
	
	public static String getByTmpClient(String url, Map<String, String> params, Map<String, String> headers) throws Exception {
		HttpGet httpget = new HttpGet();
		CloseableHttpResponse response = null;
		try {
			// 增加参数
			URIBuilder builder = new URIBuilder(url);
			if (params != null && !params.isEmpty()) {
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				for (Map.Entry<String, String> entry : params.entrySet()) {
					nameValuePairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
				}
				builder.setCustomQuery(URLEncodedUtils.format(nameValuePairs, HTTP_ENCODING));
			}
			httpget = new HttpGet(builder.build());
			// 重设或新增header
			if (headers != null && !headers.isEmpty()) {
				for (Map.Entry<String, String> entry : headers.entrySet()) {
					httpget.setHeader(entry.getKey(), entry.getValue());
				}
			}
			httpget.setConfig(getRequestConfig(Boolean.FALSE));
			tmp_httpClient = getTmpHttpClient();
			response = tmp_httpClient.execute(httpget);
			
			return getResult(url, HTTP_ENCODING, response);
		} finally {
			if (response != null)
				response.close();
			httpget.abort();
			httpget.releaseConnection();
			closeHttpClient(tmp_httpClient);
		}
	}

	public static String getCookie(String url, Map<String, String> params, Map<String, String> headers) throws Exception {
		HttpGet httpget = new HttpGet();
		CloseableHttpResponse response = null;
		try {
			// 增加参数
			URIBuilder builder = new URIBuilder(url);
			if (params != null && !params.isEmpty()) {
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				for (Map.Entry<String, String> entry : params.entrySet()) {
					nameValuePairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
				}
				builder.setCustomQuery(URLEncodedUtils.format(nameValuePairs, HTTP_ENCODING));
			}
			httpget = new HttpGet(builder.build());
			// 重设或新增header
			if (headers != null && !headers.isEmpty()) {
				for (Map.Entry<String, String> entry : headers.entrySet()) {
					httpget.setHeader(entry.getKey(), entry.getValue());
				}
			}
			httpget.setConfig(getRequestConfig(Boolean.FALSE));
			httpClient = getHttpClient();
			response = httpClient.execute(httpget);
			StringBuffer sb = new StringBuffer();
			Header[] setCookie = response.getHeaders("Set-Cookie");
			for (Header ck : setCookie) {
				String cook = ck.getValue();
				String keyval = cook.split(";")[0];
				String key = keyval.split("=")[0];
				String val = keyval.split("=")[1];
				sb.append(key + "=" + val + ";");
			}
			return sb.toString();
		} finally {
			if (response != null)
				response.close();
			httpget.abort();
			httpget.releaseConnection();
		}
	}

	public static String getReHost(String url, Map<String, String> headers) throws Exception {
		HttpGet httpget = new HttpGet(url);
		CloseableHttpResponse response = null;
		try {
			// 重设或新增header
			if (headers != null && !headers.isEmpty()) {
				for (Map.Entry<String, String> entry : headers.entrySet()) {
					httpget.setHeader(entry.getKey(), entry.getValue());
				}
			}
			httpget.setConfig(getRequestConfig(Boolean.FALSE));
			HttpContext httpContext = new BasicHttpContext();
			httpClient = getHttpClient();
			response = httpClient.execute(httpget, httpContext);
			HttpHost targetHost = (HttpHost) httpContext.getAttribute(HttpCoreContext.HTTP_TARGET_HOST);
			response.close();
			return targetHost.getHostName();
		} finally {
			if (response != null)
				response.close();
			httpget.abort();
			httpget.releaseConnection();
		}
	}

	public static Map<String, Object> getLengthAngName(String url) throws Exception {
		HttpGet httpget = new HttpGet(url);
		CloseableHttpResponse response = null;
		try {
			httpget.setConfig(getRequestConfig(Boolean.FALSE));
			HttpContext httpContext = new BasicHttpContext();
			httpClient = getHttpClient();
			response = httpClient.execute(httpget, httpContext);
			HttpEntity entity = response.getEntity();
			String ctype = entity.getContentType().getValue();
			String filename = getFileName(response, entity);

			// Content-Length
			long contentLength = entity.getContentLength();
			Map<String, Object> mp = new HashMap<String, Object>();
			mp.put("contentLength", contentLength);
			mp.put("filename", filename);
			mp.put("ctype", ctype);
			mp.put("httpstatus", response.getStatusLine().getStatusCode());
			return mp;
		} finally {
			if (response != null)
				response.close();
			httpget.abort();
			httpget.releaseConnection();
		}
	}

	protected static String getFileName(CloseableHttpResponse response, HttpEntity entity) {
		String filename = null;
		String ctype = entity.getContentType().getValue();
		if (!ctype.contains("text/html")) {
			Header hd = response.getFirstHeader("Content-Disposition");
			if (hd != null) {
				filename = hd.getValue(); // hd.toString().split(";")[1];
				filename = filename.substring(filename.indexOf("\"") + 1, filename.lastIndexOf("\""));
				filename = DateKit.getStringTodayB() + "_" + filename;
			} else {
				filename = DateKit.getStringTodayB() + "_" + DateKit.buildRandom(5);
				String filetype = ".*";
				try {
					Prop p = PropKit.getProp("contenttype.properties");
					if(p.get(ctype)!=null){
						filetype = p.get(ctype);
					}
				} catch (Exception e) {
					
				}
				filename = filename + filetype;
			}
		}
		return filename;
	}

	private static boolean checkFileAllDownload(CloseableHttpResponse response, File f) throws Exception {
		Header hd = response.getFirstHeader("Content-Length");
		if (hd != null && f.exists()) {
			int length = Integer.valueOf(hd.getValue());
			int filelength = FileUtils.readFileToByteArray(f).length;
			if (length != filelength) {
				logger.error("资源下载不完整，需重新下载");
				return true;
			}
		}
		return false;
	}

	private static String FileDownload(CloseableHttpResponse response, String filedest, String filename, int isdir) {
		HttpEntity entity = response.getEntity();
		Map res = new HashMap();
		String fullpath = null;
		if (isdir == 1) {
			fullpath = filedest + "/" + filename;
		} else {
			fullpath = filedest;
		}
		try {
			File f = new File(fullpath);
			boolean needNew = checkFileAllDownload(response, f);
			if (!f.exists() || needNew) {
				InputStream is = null;
				is = entity.getContent();
				FileUtils.copyInputStreamToFile(is, f);	
			}
			res.put("status", 0);
			res.put("filepath", f.toString());
		} catch (Exception e) {
			logger.error(e.toString());
			res.put("status", -1);
			res.put("errmsg", e.toString());
		}
		return JsonKit.map2JSON(res);
	}

	public static String getFileDownByPath(String url, String filedest, int isdir, Map<String, String> headers) {
		Map resp = new HashMap();
		String res = "";
		HttpGet httpget = new HttpGet(url);
		CloseableHttpResponse response = null;
		try {
			// 重设或新增header
			if (headers != null && !headers.isEmpty()) {
				for (Map.Entry<String, String> entry : headers.entrySet()) {
					httpget.setHeader(entry.getKey(), entry.getValue());
				}
			}
			httpget.setConfig(getRequestConfig(Boolean.FALSE));
			other_httpClient = getOtherHttpClient();
			response = other_httpClient.execute(httpget);
			HttpEntity entity = response.getEntity();
			String filename = getFileName(response, entity);
			if (StringUtils.isNotBlank(filename)) {
				res = FileDownload(response, filedest, filename, isdir);
			} else {
				String ctype = entity.getContentType().getValue();
				resp.put("status", -2);
				resp.put("errmsg", ctype + "_" + "不是可下载文件类型");
				res = JsonKit.map2JSON(resp);
			}
		} catch (Exception e) {
			if (!e.toString().contains("404")) {
				logger.error("下载文件" + e.toString());
			}
			resp.put("status", -1);
			resp.put("errmsg", e.toString());
			res = JsonKit.map2JSON(resp);
		} catch (Throwable t) {
			if (!t.toString().contains("404")) {
				logger.error("下载文件" + t.toString());
			}
			resp.put("status", -1);
			resp.put("errmsg", t.toString());
			res = JsonKit.map2JSON(resp);
		} finally {
			if (response != null){
				try {
					response.close();
				} catch (IOException e) {
					logger.error("关闭responses异常",e);
				}
			}
			httpget.abort();
			httpget.releaseConnection();
		}
		return res;
	}

	public static String getFileDownByPath(String url, String filedest, int isdir) {
		return getFileDownByPath(url, filedest, isdir, null);
	}

	public static String getFileDownByPathDir(String url, String filedest) {
		return getFileDownByPath(url, filedest, 1, null);
	}

	public static String getFileDownByPathFull(String url, String filedest) {
		return getFileDownByPath(url, filedest, 0, null);
	}
}

class HttpClientHelpTrustStrategy implements TrustStrategy {
	@Override
	public boolean isTrusted(X509Certificate[] paramArrayOfX509Certificate, String paramString) throws CertificateException {
		return true;
	}
}

class TrustAnyHostnameVerifier implements HostnameVerifier {
	public boolean verify(String hostname, SSLSession session) {
		return true;
	}
}