package com.gepardec.mega.zep;

import com.gepardec.mega.application.configuration.ZepConfig;
import com.sun.xml.ws.developer.JAXWSProperties;
import de.provantis.zep.RequestHeaderType;
import de.provantis.zep.ZepSoap;
import de.provantis.zep.ZepSoapPortType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.xml.ws.BindingProvider;

import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class ZepSoapProvider {

    @Inject
    ZepConfig zepConfig;

    @Produces
    @Dependent
    ZepSoapPortType createZepSoapPortType() {
        final ZepSoap zs = new ZepSoap(Thread.currentThread()
                .getContextClassLoader()
                .getResource("wsdl/zep_v7.wsdl"));
        final ZepSoapPortType port = zs.getZepSOAP();
        configureWebserviceClient((BindingProvider) port, zepConfig.getUrlAsString(), 30L, 60L, TimeUnit.SECONDS);
        return port;
    }

    public RequestHeaderType createRequestHeaderType() {
        RequestHeaderType requestHeaderType = new RequestHeaderType();
        requestHeaderType.setAuthorizationToken(zepConfig.getAuthorizationToken());
        return requestHeaderType;
    }

    private void configureWebserviceClient(final BindingProvider bindingProvider, final String endpoint, final Long connectionTimeout,
                                           final Long requestTimeout, final TimeUnit timeoutUnit) {
        bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpoint);
        final int connectionTimeoutMillis = (int) TimeUnit.MILLISECONDS.convert(connectionTimeout, timeoutUnit);
        final int requestTimeoutMillis = (int) TimeUnit.MILLISECONDS.convert(requestTimeout, timeoutUnit);
        bindingProvider.getRequestContext().put(JAXWSProperties.CONNECT_TIMEOUT, connectionTimeoutMillis);
        bindingProvider.getRequestContext().put(JAXWSProperties.REQUEST_TIMEOUT, requestTimeoutMillis);
    }
}
