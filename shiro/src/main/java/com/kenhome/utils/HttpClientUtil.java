/**   
 * Copyright © 2018  
 * @Package: com.chenmeikai.utils.http   
 * @date: 2018年3月14日 上午8:55:32 
 */
package com.kenhome.utils;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.AbstractHttpMessage;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * 
 * @Description:httpClient
 * @author: cmk 
 * @date:   2018年3月18日 上午10:55:49
 */
public class HttpClientUtil {

	private final static Logger log = LoggerFactory.getLogger(HttpClientUtil.class);
	
	/*
	 * get方法
	 */
	public static String doGet(String url) {
		return baseGet(url, null, 3000, 5000);
	};
	public static String doGet(String url, int connectTimeout, int socketTimeout) {
		return baseGet(url, null, connectTimeout, socketTimeout);
	};
	public static String doGet(String urlString, Map<String, String> params) {
		String url = urlString + "?" + tokeyValue(params);
		return baseGet(url, null, 3000, 5000);
	};
	public static String doGet(String urlString, Map<String, String> params, Map<String, String> headers) {
		String url = urlString + "?" + tokeyValue(params);
		return baseGet(url, headers, 3000, 5000);
	};
	public static String doGet(String urlString, Map<String, String> params, int connectTimeout, int socketTimeout) {
		String url = urlString + "?" + tokeyValue(params);
		return baseGet(url, null, connectTimeout, socketTimeout);
	};
	public static String doGet(String urlString, Map<String, String> params, Map<String, String> headers,
			int connectTimeout, int socketTimeout) {
		String url = urlString + "?" + tokeyValue(params);
		return baseGet(url, headers, connectTimeout, socketTimeout);
	};
	
	
	/*
	 * post方法
	 */
	public static String doPost(String url, Map<String, String> params) {
		return basePost(url,params, null, 3000, 5000);
	};
	public static String doPost(String url, Map<String, String> params, Map<String, String> headers) {
		return basePost(url,params, headers, 3000, 5000);
	};

	public static String doPost(String url, Map<String, String> params, int connectTimeout, int socketTimeout) {
		return basePost(url,params, null, connectTimeout, socketTimeout);
	};
	public static String doPost(String url, Map<String, String> params, Map<String, String> headers,
			int connectTimeout, int socketTimeout) {
		return basePost(url,params, headers, connectTimeout, socketTimeout);
	};
	
	//json
	public static String doJsonPost(String url, Map<String, String> params,
			int connectTimeout, int socketTimeout) {
		Map<String,String> headers =new HashMap<>();
		headers.put("Content-Type", "application/json;charset=utf-8");
		return basePost(url,params, headers, connectTimeout, socketTimeout);
	};

	/**
	 * get方法基类
	 * @param urlString
	 * @param headers
	 * @param connectTimeout
	 * @param socketTimeout
	 * @return
	 */
	public static String baseGet(String urlString, Map<String, String> headers, int connectTimeout, int socketTimeout) {
		String result = "";
		// 获取httpclient连接
		CloseableHttpClient httpclient = HttpClients.createDefault();;
		// 定义请求设置 ConnectTimeout连接超时，SocketTimeout读取数据超时
		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(connectTimeout)
				.setSocketTimeout(socketTimeout).build();
		// 执行get请求.
		CloseableHttpResponse response;
		HttpGet httpGet = null;
		urlString = urlString.trim();
		if (null == urlString || urlString.isEmpty() || !urlString.startsWith("http")) {
			return result;
		}
		// 转化String url为URI,解决url中包含特殊字符的情况
		try {
			URL url = new URL(urlString);
			URI uri = url.toURI();
			httpGet = new HttpGet(uri);
			// 超时设置
			httpGet.setConfig(requestConfig);
			// 默认请求头
			setCommonHeaders(httpGet);
			// 请求头修改
			if (headers != null && headers.size() > 0) {
				for (Map.Entry<String, String> header : headers.entrySet()) {
					if (httpGet.containsHeader(header.getKey())) {
						httpGet.setHeader(header.getKey(), header.getValue());
					} else {
						httpGet.addHeader(header.getKey(), header.getValue());
					}
				}
			}
			response = httpclient.execute(httpGet);
			// 响应状态码
			int stateCode = response.getStatusLine().getStatusCode();
			if (stateCode == 200) {
				// 获取响应实体
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					result = EntityUtils.toString(entity);
					return result;
				}
			} else {
				log.warn(urlString + "-请求返回的状态码" + stateCode);
				result = "{\"code\":" + stateCode + "}";
				return result;
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} finally {
			httpGet.abort();
			try {
				httpclient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * post请求基类
	 */
	public static String basePost(String urlString, Map<String, String> params, Map<String, String> headers,
			int connectTimeout, int socketTimeout) {
		
		String result = "";
		// 获取httpclient连接
		CloseableHttpClient httpclient = HttpClients.createDefault();
		// 定义请求设置 ConnectTimeout连接超时，SocketTimeout读取数据超时
		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(connectTimeout)
				.setSocketTimeout(socketTimeout).build();
		// 执行post请求.
		CloseableHttpResponse response;
		HttpPost httpPost = null;
		urlString = urlString.trim();
		if (null == urlString || urlString.isEmpty() || !urlString.startsWith("http")) {
			return result;
		}
		try {
			httpPost = new HttpPost(urlString);
			// 超时设置
			httpPost.setConfig(requestConfig);
			//默认头部
			setCommonHeaders(httpPost);
			// 如果有自定义的header头部则添加修改
			if (headers != null && headers.size() > 0) {
				for (Map.Entry<String, String> header : headers.entrySet()) {
					if (httpPost.containsHeader(header.getKey())) {
						httpPost.setHeader(header.getKey(), header.getValue());
					} else {
						httpPost.addHeader(header.getKey(), header.getValue());
					}
				}
			}
			if (params != null && params.size() > 0) {
				List<BasicNameValuePair> nvps = new ArrayList<>();
				for (Map.Entry<String, String> entry : params.entrySet()) {
					nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
				}
				// 这里设置的是返回结果编码,大多数都是UTF-8,如果乱码,可以查看网页的meta中的content的编码.如果是GBK,这里改为GBK即可.
				// 这里entity只能读取一次,想要读取多次,百度一下.
				httpPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
			}
			response = httpclient.execute(httpPost);
			// 响应状态码
			int stateCode = response.getStatusLine().getStatusCode();
			if (stateCode == 200) {
				// 获取响应实体
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					// 打印响应内容
					result = EntityUtils.toString(entity);
					return result;
				}
			} else {
				log.warn(urlString + "-请求返回的状态码" + stateCode);
				result = "{\"code\":" + stateCode + "}";
				return result;
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			httpPost.abort();
			try {
				httpclient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	// 默认请求头
	private static void setCommonHeaders(AbstractHttpMessage request) {
		request.addHeader("Accept",
				"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
		// request.addHeader("Connection", "keep-alive");
		request.addHeader("Accept-Language", "zh-CN,zh;q=0.8");
		request.addHeader("Accept-Encoding", "gzip, deflate, br");
		// User-Agent最好随机换
		request.addHeader("User-Agent",
				"Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.94 Safari/537.36");
	}

	/**
	 * 将map集合转化为key-value的键值对String
	 * 
	 * @param params
	 * @return
	 */
	public static String tokeyValue(Map<String, String> params) {
		List<String> keys = new ArrayList<String>(params.keySet());
		String prestr = "";
		for (int i = 0; i < keys.size(); i++) {
			String key = keys.get(i);
			String value = (String) params.get(key);

			if (i == keys.size() - 1) {// 拼接时，不包括最后一个&字符
				prestr = prestr + key + "=" + value;
			} else {
				prestr = prestr + key + "=" + value + "&";
			}
		}
		return prestr;
	}
	
	/**
     * 获取客户端ip地址
     * @param request
     * @return
     */
    public static String getCliectIp(HttpServletRequest request)
    {
    	 String ipAddress = request.getHeader("x-forwarded-for");  
         if(ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {  
             ipAddress = request.getHeader("Proxy-Client-IP");  
         }  
         if(ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {  
             ipAddress = request.getHeader("WL-Proxy-Client-IP");  
         }  
         if(ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {  
             ipAddress = request.getRemoteAddr();  
             if(ipAddress.equals("127.0.0.1") || ipAddress.equals("0:0:0:0:0:0:0:1")){  
                 //根据网卡取本机配置的IP  
                 InetAddress inet=null;  
                 try {  
                     inet = InetAddress.getLocalHost();  
                 } catch (UnknownHostException e) {  
                     e.printStackTrace();  
                 }  
                 ipAddress= inet.getHostAddress();  
             }  
         }  
         //对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割  
         if(ipAddress!=null && ipAddress.length()>15){ //"***.***.***.***".length() = 15  
             if(ipAddress.indexOf(",")>0){  
                 ipAddress = ipAddress.substring(0,ipAddress.indexOf(","));  
             }  
         }  
         return ipAddress;   
    }
    
    /**
     * 获得请求类型 传统还是ajax  
     * @param request
     * @return
     */
    public static String getRequestType(HttpServletRequest request) {
    	String type =request.getHeader("X-Requested-With");
    	if("XMLHttpRequest".equalsIgnoreCase(type)) {
    		return "ajax";
    	}
        return "normal";
    }

}