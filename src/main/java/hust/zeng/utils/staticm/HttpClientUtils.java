package hust.zeng.utils.staticm;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;

/**
 * Http工具类
 * @title HttpClientUtils
 * @author zengzhihua
 */
public class HttpClientUtils {

	private static PoolingHttpClientConnectionManager connectionManager = null;
	private static HttpClientBuilder httpBulder = null;
	private static RequestConfig requestConfig = null;

	private static int MAXCONNECTION = 500;

	private static int DEFAULTMAXCONNECTION = 100;

	static {
		// 设置http的状态参数
		requestConfig = RequestConfig.custom().setSocketTimeout(5000).setConnectTimeout(5000)
				.setConnectionRequestTimeout(5000).build();

		// HttpHost target = new HttpHost(IP, PORT);
		connectionManager = new PoolingHttpClientConnectionManager();
		connectionManager.setMaxTotal(MAXCONNECTION);
		connectionManager.setDefaultMaxPerRoute(DEFAULTMAXCONNECTION);
		// connectionManager.setMaxPerRoute(new HttpRoute(target), 20);
		httpBulder = HttpClients.custom();
		httpBulder.setConnectionManager(connectionManager);
	}

	private static CloseableHttpClient getConnection() {
		CloseableHttpClient httpClient = httpBulder.build();
		httpClient = httpBulder.build();
		return httpClient;
	}

	private static HttpUriRequest getRequestMethod(Map<String, String> map, String url, String method) {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		Set<Map.Entry<String, String>> entrySet = map.entrySet();
		for (Map.Entry<String, String> e : entrySet) {
			String name = e.getKey();
			String value = e.getValue();
			NameValuePair pair = new BasicNameValuePair(name, value);
			params.add(pair);
		}
		HttpUriRequest reqMethod = null;
		if ("post".equals(method)) {
			reqMethod = RequestBuilder.post().setUri(url)
					.addParameters(params.toArray(new BasicNameValuePair[params.size()])).setConfig(requestConfig)
					.build();
		} else if ("get".equals(method)) {
			reqMethod = RequestBuilder.get().setUri(url)
					.addParameters(params.toArray(new BasicNameValuePair[params.size()])).setConfig(requestConfig)
					.build();
		} else if ("delete".equals(method)) {
			reqMethod = RequestBuilder.delete().setUri(url)
					.addParameters(params.toArray(new BasicNameValuePair[params.size()])).setConfig(requestConfig)
					.build();
		}
		return reqMethod;
	}

	private static HttpUriRequest getRequestMethod(String json, String url, String method) {
		HttpUriRequest reqMethod = null;
		if ("postjson".equals(method)) {
			InputStreamEntity reqEntity = new InputStreamEntity(new ByteArrayInputStream(json.getBytes()));
			reqEntity.setContentType("application/json");
			reqEntity.setContentEncoding("utf-8");
			reqEntity.setChunked(true);
			reqMethod = RequestBuilder.post().setUri(url).setEntity(reqEntity).setConfig(requestConfig).build();
		}
		if ("putjson".equals(method)) {
			InputStreamEntity reqEntity = new InputStreamEntity(new ByteArrayInputStream(json.getBytes()));
			reqEntity.setContentType("application/json");
			reqEntity.setContentEncoding("utf-8");
			reqEntity.setChunked(true);
			reqMethod = RequestBuilder.put().setUri(url).setEntity(reqEntity).setConfig(requestConfig).build();
		}
		return reqMethod;
	}

	public static HttpResponse httpget(Map<String, String> map, String url)
			throws ClientProtocolException, IOException {
		HttpClient client = getConnection();
		HttpUriRequest get = getRequestMethod(map, url, "get");
		HttpResponse response = client.execute(get);
		return response;
	}

	public static HttpResponse httppost(Map<String, String> map, String url)
			throws ClientProtocolException, IOException {
		HttpClient client = getConnection();
		HttpUriRequest post = getRequestMethod(map, url, "post");
		HttpResponse response = client.execute(post);
		return response;
	}

	public static HttpResponse httpdelete(Map<String, String> map, String url)
			throws ClientProtocolException, IOException {
		HttpClient client = getConnection();
		HttpUriRequest delete = getRequestMethod(map, url, "delete");
		HttpResponse response = client.execute(delete);
		return response;
	}

	public static HttpResponse jsonpost(String json, String url) throws ClientProtocolException, IOException {
		HttpClient client = getConnection();
		HttpUriRequest post = getRequestMethod(json, url, "postjson");
		HttpResponse response = client.execute(post);
		return response;
	}

	public static HttpResponse jsonput(String json, String url) throws ClientProtocolException, IOException {
		HttpClient client = getConnection();
		HttpUriRequest put = getRequestMethod(json, url, "putjson");
		HttpResponse response = client.execute(put);
		return response;
	}
}
