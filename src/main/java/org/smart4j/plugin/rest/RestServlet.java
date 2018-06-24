package org.smart4j.plugin.rest;

import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.annotation.WebServlet;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.transport.servlet.CXFNonSpringServlet;
import org.smart4j.framework.helper.ClassHelper;
import org.smart4j.framework.util.CollectionUtil;
import org.smart4j.framework.util.StringUtil;

@WebServlet(urlPatterns = RestConstant.SERVLET_URL, loadOnStartup = 0)
public class RestServlet extends CXFNonSpringServlet {

	@Override
	protected void loadBus(ServletConfig sc) {
		super.loadBus(sc);
		Bus bus = getBus();
		BusFactory.setDefaultBus(bus);
		publishRestService();
	}

	private void publishRestService() {
		Set<Class<?>> restClassSet = ClassHelper.getClassSetByAnnotation(Rest.class);
		if (CollectionUtil.isNotEmpty(restClassSet)) {
			for (Class<?> restClass : restClassSet) {
				String address = getAddress(restClass);
				RestHelper.publishService(address, restClass);
			}
		}
	}

	private String getAddress(Class<?> restClass) {
		String address;
		String restValue = restClass.getAnnotation(Rest.class).value();
		if (StringUtil.isNotEmpty(restValue)) {
			address = restValue;
		} else {
			address = restClass.getSimpleName();
		}

		if (!address.startsWith("/")) {
			address = "/" + address;
		}
		address = address.replaceAll("\\/+", "/");
		return address;
	}

}
