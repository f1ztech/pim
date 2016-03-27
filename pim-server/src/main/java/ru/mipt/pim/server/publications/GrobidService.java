package ru.mipt.pim.server.publications;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class GrobidService {

	@Resource(name = "appProperties")
	private Properties properties;

	private String serviceUrl;

	private final Log logger = LogFactory.getLog(getClass());

	@PostConstruct
	private void init() {
		serviceUrl = properties.getProperty("grobid.service-url");
	}

	public RestTemplate prepareTemplate() {
		RestTemplate template = new RestTemplate();
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setBufferRequestBody(false);
		template.setRequestFactory(requestFactory);

		// change response string encoding
		int ind = 0;
		for (HttpMessageConverter<?> converter : template.getMessageConverters()) {
			if (converter instanceof StringHttpMessageConverter) {
				break;
			}
			ind++;
		}
		template.getMessageConverters().add(ind, new org.springframework.http.converter.StringHttpMessageConverter(Charset.forName("UTF-8")));

		// log errors
		template.setErrorHandler(new ResponseErrorHandler() {
		    private ResponseErrorHandler errorHandler = new DefaultResponseErrorHandler();

		    public void handleError(ClientHttpResponse response) throws IOException {

		    	String message = IOUtils.toString(response.getBody());
		    	logger.error(message);
		        try {
		            errorHandler.handleError(response);
		        } catch (RestClientException scx) {
		        }
		    }

			@Override
			public boolean hasError(ClientHttpResponse response) throws IOException {
				return errorHandler.hasError(response);
			}
		});
		return template;
	}

	private HttpEntity<MultiValueMap<String, Object>> preparePostEntity(Object... params) {
		boolean isMultipart = false;
		MultiValueMap<String, Object> parameters = new LinkedMultiValueMap<String, Object>();
		for (int i = 0; i < params.length - 1; i = i + 2) {
			parameters.add((String) params[i], params[i + 1]);
			isMultipart |= !(params[i + 1] instanceof String);
		}

		HttpHeaders headers = makeHeaders();
		if (isMultipart) {
			headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		}

		HttpEntity<MultiValueMap<String, Object>> postEntity = new HttpEntity<MultiValueMap<String, Object>>(parameters, headers);

		return postEntity;
	}

	public String processHeader(File file) {
		HttpEntity<MultiValueMap<String, Object>> postEntity = preparePostEntity("input", new FileSystemResource(file), "consolidate", false);
		for (int i = 0; i < 5; i++) { // try 5 times in case of errors
			try {
				return prepareTemplate().exchange(serviceUrl + "/processHeaderDocument", HttpMethod.POST, postEntity, String.class).getBody();
			} catch (Exception e) {
				if (i == 4) {
					throw e;
				}
			}
		}
		return null;
	}

	public String processReferences(File file) {
		HttpEntity<MultiValueMap<String, Object>> postEntity = preparePostEntity("input", new FileSystemResource(file), "consolidate", false);
		for (int i = 0; i < 5; i++) { // try 5 times in case of errors
			try {
				return prepareTemplate().exchange(serviceUrl + "/processReferences", HttpMethod.POST, postEntity, String.class).getBody();
			} catch (Exception e) {
				if (i == 4) {
					throw e;
				}
			}
		}
		return null;
	}

	private HttpHeaders makeHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(new MediaType("application", "x-www-form-urlencoded", Charset.forName("UTF-8")));
		return headers;
	}

}
