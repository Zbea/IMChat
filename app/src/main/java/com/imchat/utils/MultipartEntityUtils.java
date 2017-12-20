package com.imchat.utils;

import com.android.volley.toolbox.HttpClientStack;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyStore;
import java.util.List;
import java.util.Map;

/**
 * 基于Apache HttpClient的封装，支持HTTP GET、POST请求，支持多文件上传
 */
public class MultipartEntityUtils
{
	//IdentityHashMap

	// 设置URLConnection的连接超时
	private final static int CONNET_TIMEOUT = 30 * 1000;
	// 设置URLConnection的读取超时
	private final static int READ_TIMEOUT = 30 * 1000;


    public interface OnResponseListener
    {
        void onResponse(String result);
    }


    /**
     * HTTP POST请求上传文件，支持多文件上传
     *
     * @param urlStr
     *            请求链接
     * @param headers
     *             请求头
     * @param params
     *            HTTP POST请求文本参数map集合
	  * @param filesName
	 *            HTTP POST请求文件参数名
     * @param files
     *            HTTP POST请求文件参数map集合
     * @return HTTP POST请求结果
     * @throws IOException
     */
    public static void doMultipleFiles(final String urlStr, final Map<String, String> headers, final Map<String, String> params,
									   final String filesName, final List<String> files, final OnResponseListener onResponseListener)
    {
        new Thread()
        {
            public void run()
            {
                try
                {
                    String result = post(urlStr, headers,params,filesName,files);
                    if (onResponseListener != null)
                    {
                        onResponseListener.onResponse(result);
                    }
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            ;
        }.start();
    }


    /**
     * 上传图片
     *
     * @param urlStr
     *            请求链接
     * @param headers
     *             请求头
     * @param pathStr
     *            HTTP 文件地址
     * @throws IOException
     */
    public static void doMultipleFile(final String urlStr, final Map<String, String> headers, final String pathStr, final OnResponseListener onResponseListener)
    {
        new Thread()
        {
            public void run()
            {
                try
                {
                    String result = post(urlStr, headers,pathStr);
                    if (onResponseListener != null)
                    {
                        onResponseListener.onResponse(result);
                    }
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            ;
        }.start();
    }



	private static String post(String url, Map<String, String> headers, Map<String, String> params,
							   String filesName, final List<String> files) throws IOException {
		MultipartEntity multipartEntityBuilder = new MultipartEntity();
//		if (params != null && !params.isEmpty()) {
//			for (Map.Entry<String, String> entry : params.entrySet()) {
//				multipartEntityBuilder.addPart(entry.getKey(),  new ContentBody(entry.getValue()));
//			}
//		}

		if (files != null && !files.isEmpty()) {
			for (String entry : files) {
				File file = new File(entry);
				multipartEntityBuilder.addPart(filesName, new FileBody(file));
			}
		}

		HttpClient client = getNewHttpClient();
		HttpPost post = new HttpPost(url);
        if (headers != null)//设置请求头
        {
            for (String key : headers.keySet())
            {
                post.addHeader(key, headers.get(key));
            }
        }
		post.setEntity(multipartEntityBuilder);
		HttpResponse response = client.execute(post);

		StringBuffer sb = new StringBuffer();
		if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			HttpEntity result = response.getEntity();
			if (result != null) {
				InputStream is = result.getContent();
				BufferedReader br = new BufferedReader(
						new InputStreamReader(is));
				String tempLine;
				while ((tempLine = br.readLine()) != null) {
					sb.append(tempLine);
				}
			}
		}
		post.abort();
		return sb.toString();
	}

    private static String post(String url, Map<String, String> headers, String pathStr) {
        File file=new File(pathStr);
		HttpClient client = getNewHttpClient();
		HttpClientStack.HttpPatch post = new HttpClientStack.HttpPatch(url);
        if (headers != null)//设置请求头
        {
            for (String key : headers.keySet())
            {
                post.addHeader(key, headers.get(key));
            }
        }
		MultipartEntity multipartEntityBuilder = new MultipartEntity();
		multipartEntityBuilder.addPart("users[avater]", new FileBody(file,"image/*"));

		post.setEntity(multipartEntityBuilder);

        HttpResponse response = null;
        try
        {
            response = client.execute(post);

        StringBuffer sb = new StringBuffer();
		if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			HttpEntity result = response.getEntity();
			if (result != null) {
				InputStream is = result.getContent();
				BufferedReader br = new BufferedReader(
						new InputStreamReader(is));
				String tempLine;
				while ((tempLine = br.readLine()) != null) {
					sb.append(tempLine);
				}
			}
		}
		post.abort();
		return sb.toString();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
	}

	/**
	 * 获取HttpClient
	 *
	 * @return
	 */
	public static HttpClient getHttpClient() {
		BasicHttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, CONNET_TIMEOUT);
		HttpConnectionParams.setSoTimeout(httpParams, READ_TIMEOUT);
		HttpClient httpClient = new DefaultHttpClient(httpParams);
		return httpClient;
	}

	/**
	 * 获取HttpClient
	 *
	 * @return
	 */
	private static HttpClient getNewHttpClient() {
		try {
			KeyStore trustStore = KeyStore.getInstance(KeyStore
					.getDefaultType());
			trustStore.load(null, null);
			SSLSocketFactory sf = new SSLSocketFactory(trustStore);
			sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

			HttpParams params = new BasicHttpParams();
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
			HttpConnectionParams.setConnectionTimeout(params, CONNET_TIMEOUT);
			HttpConnectionParams.setSoTimeout(params, READ_TIMEOUT);

			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory
					.getSocketFactory(), 80));
			registry.register(new Scheme("https", sf, 443));
			ClientConnectionManager ccm = new ThreadSafeClientConnManager(
					params, registry);
			return new DefaultHttpClient(ccm, params);
		} catch (Exception e) {
			return new DefaultHttpClient();
		}
	}



}
