package org.smart4j.plugin.rest;

import java.util.ArrayList;
import java.util.List;

import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;
import org.apache.cxf.jaxrs.provider.jsonp.JsonpInInterceptor;
import org.apache.cxf.jaxrs.provider.jsonp.JsonpPostStreamInterceptor;
import org.apache.cxf.jaxrs.provider.jsonp.JsonpPreStreamInterceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.rs.security.cors.CrossOriginResourceSharingFilter;
import org.smart4j.framework.helper.BeanHelper;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

public final class RestHelper {

	private static final List<Object> providerList = new ArrayList<Object>();
	private static final List<Interceptor<? extends Message>> inInterceptorList = new ArrayList<Interceptor<? extends Message>>();
	private static final List<Interceptor<? extends Message>> outInterceptorList = new ArrayList<Interceptor<? extends Message>>();

	static {

		JacksonJsonProvider jsonProvider = new JacksonJsonProvider();
		providerList.add(jsonProvider);

		if (RestConfig.isLog()) {
			inInterceptorList.add(new LoggingInInterceptor());
			outInterceptorList.add(new LoggingOutInterceptor());
		}

		if (RestConfig.isJsonp()) {
			JsonpInInterceptor jsonpInInterceptor = new JsonpInInterceptor();
			jsonpInInterceptor.setCallbackParam(RestConfig.getJsonpFunction());
			inInterceptorList.add(jsonpInInterceptor);

			JsonpPreStreamInterceptor jsonpPreStreamInterceptor = new JsonpPreStreamInterceptor();
			outInterceptorList.add(jsonpPreStreamInterceptor);

			JsonpPostStreamInterceptor jsonpPostStreamInterceptor = new JsonpPostStreamInterceptor();
			outInterceptorList.add(jsonpPostStreamInterceptor);
		}

		if (RestConfig.isCors()) {
			CrossOriginResourceSharingFilter corsProvider = new CrossOriginResourceSharingFilter();
			corsProvider.setAllowOrigins(RestConfig.getCorsOriginList());
			providerList.add(corsProvider);
		}
	}

	public static void publishService(String wadl, Class<?> resourceClass) {
		JAXRSServerFactoryBean factory = new JAXRSServerFactoryBean();
		factory.setAddress(wadl);
		factory.setResourceClasses(resourceClass);
		factory.setResourceProvider(resourceClass, new SingletonResourceProvider(BeanHelper.getBean(resourceClass)));
		factory.setProviders(providerList);
		factory.setInInterceptors(inInterceptorList);
		factory.setOutInterceptors(outInterceptorList);
		factory.create();
	}

	public static <T> T createClient(String wadl, Class<? extends T> resourceClass) {
		return JAXRSClientFactory.create(wadl, resourceClass, providerList);
	}
}
