package com.chuncongcong.test.config;

import javax.net.ssl.SSLContext;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * @author HU
 * @date 2020/4/26 14:57
 */


@Configuration
public class RestTemplateConfig {


	//最好是用不注释的方法，在注入的同时设置连接时间，这种注释的也可以，但是没有设置超时时间
    /*@Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder){
        return builder.build();
    }*/

	@Bean
	public RestTemplate restTemplate(){
		return new RestTemplate(generateHttpsRequestFactory());
	}

	@Bean
	public HttpComponentsClientHttpRequestFactory generateHttpsRequestFactory() {
		try {
			TrustStrategy acceptingTrustStrategy = (x509Certificates, authType) -> true;
			SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
			SSLConnectionSocketFactory connectionSocketFactory =
					new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());

			HttpClientBuilder httpClientBuilder = HttpClients.custom();
			httpClientBuilder.setSSLSocketFactory(connectionSocketFactory);
			CloseableHttpClient httpClient = httpClientBuilder.build();
			HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
			factory.setHttpClient(httpClient);
			factory.setConnectTimeout(10 * 1000);
			factory.setReadTimeout(30 * 1000);
			return factory;
		} catch (Exception e) {
			throw new RuntimeException("创建HttpsRestTemplate失败", e);
		}

	}
}
