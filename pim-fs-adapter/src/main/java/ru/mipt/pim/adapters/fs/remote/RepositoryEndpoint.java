package ru.mipt.pim.adapters.fs.remote;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.nio.charset.Charset;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.HttpMessageConverterExtractor;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import ru.mipt.pim.adapters.fs.ConfigsService;
import ru.mipt.pim.adapters.fs.Utils;
import ru.mipt.pim.adapters.fs.common.ClientFileTree;
import ru.mipt.pim.adapters.fs.common.JsonRequestResults.AuthenticateResult;
import ru.mipt.pim.adapters.fs.common.JsonRequestResults.CommonRequestResult;
import ru.mipt.pim.adapters.fs.common.JsonRequestResults.ExistsResult;
import ru.mipt.pim.adapters.fs.common.JsonRequestResults.FileTreesResult;
import ru.mipt.pim.adapters.fs.common.JsonRequestResults.RemoveResult;
import ru.mipt.pim.adapters.fs.common.JsonRequestResults.SaveResult;
import ru.mipt.pim.adapters.fs.common.JsonRequestResults.UploadResult;

@Component
public class RepositoryEndpoint {

	@Resource
	private ConfigsService configsService;
	
	@Resource
	private FileSender fileSender;
	
	@Resource
	private Utils utils;

	private String serverUrl;

	private String token;
	
	private long lastRequestTime = 0;
	
	private int connectionErrors = 0;
	
	private RestTemplate template;
	
	private HttpEntity<String> requestEntity;

	private HttpEntity<MultiValueMap<String, Object>> postEntity;
	
	class FileUploadRequestCallback implements RequestCallback {
		
		private InputStream fileStream;

		public FileUploadRequestCallback(InputStream fileStream) {
			this.fileStream = fileStream;
		}
		
	    @Override
	    public void doWithRequest(final ClientHttpRequest request) throws IOException {
	    	request.getHeaders().add("X-Auth-Token", token);
//	        request.getHeaders().add("Content-type", "application/octet-stream");
	        IOUtils.copy(fileStream, request.getBody());
	        IOUtils.closeQuietly(fileStream);
	    }
	};
	
	@PostConstruct
	private void init() {
		serverUrl = configsService.getPimServerUrl();
		template = new RestTemplate();
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setBufferRequestBody(false);     
		template.setRequestFactory(requestFactory); 
		template.setErrorHandler(new ResponseErrorHandler() {
			
		    private ResponseErrorHandler errorHandler = new DefaultResponseErrorHandler();

		    public void handleError(ClientHttpResponse response) throws IOException {

		    	String message = IOUtils.toString(response.getBody());
				utils.logToFile(message);
		    	utils.logToAll(message);
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
	}
	
	private void prepareTemplate() throws InterruptedException {
		if (System.currentTimeMillis() - lastRequestTime > 50 * 60 * 1000) {
			utils.logToAll("Авторизация...");
			AuthenticateResult authenticateResult = template.getForObject(serverUrl + "/rest/authenticate?login={login}&password={password}", 
																				AuthenticateResult.class, configsService.getLogin(), configsService.getPassword());
			
			logResult(authenticateResult);
			
			if (authenticateResult.isSuccess()) {
				token = authenticateResult.getToken();
				requestEntity = new HttpEntity<String>(makeHeaders());
			} else {
				utils.logToAll("Неверный логин или пароль");
				fileSender.sleep(10 * 60 * 1000);
			}
		}
		lastRequestTime = System.currentTimeMillis();
	}

	private HttpHeaders makeHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.add("X-Auth-Token", token);
		headers.setContentType(new MediaType("application", "x-www-form-urlencoded", Charset.forName("UTF-8")));
		return headers;
	}

	public Map<String, ClientFileTree> getFileTrees() throws RepositoryException {
		try {
			prepareTemplate();
			
			FileTreesResult removeResult = template.exchange(serverUrl + "/rest/files/getFileTrees", HttpMethod.GET, requestEntity, FileTreesResult.class).getBody();
			return removeResult.getFileTrees();
		} catch (Exception e) {
			handleRequestError(e);
			throw new RepositoryException("Add file error!", e);
		}
	}
	
	public boolean addFile(File file, String path) throws RepositoryException {
		try {
			prepareTemplate();

			String hash = DigestUtils.sha1Hex(new FileInputStream(file));
			utils.logToFile("Проверка файла " + file.getName() + "...");
			preparePostEntity("fileName", file.getName(), "path", path, "hash", hash);
			ExistsResult existsResult = template.exchange(serverUrl + "/rest/files/isExists", HttpMethod.POST, postEntity, ExistsResult.class).getBody();
			utils.logToFile("Файл " + (existsResult.isFileExists() ? "найден" : "не найден"));
			
			if (!existsResult.isFileExists()) {
				utils.logToAll("Загрузка файла " + file.getName() + "...");
				FileUploadRequestCallback requestCallback = new FileUploadRequestCallback(new FileInputStream(file));
				UploadResult uploadResult = template.execute(serverUrl + "/rest/files/upload", HttpMethod.POST, requestCallback, 
							new HttpMessageConverterExtractor<UploadResult>(UploadResult.class, template.getMessageConverters()));
				logResult(uploadResult);

				utils.logToAll("Сохранение файла " + file.getName() + "...");
				preparePostEntity("fileId", uploadResult.getFileId(),  "fileName", file.getName(), "path", path, "hash", hash);
				SaveResult saveResult = template.exchange(serverUrl + "/rest/files/save", HttpMethod.POST, postEntity, SaveResult.class).getBody();
				logResult(saveResult);
				
				return saveResult.isSuccess();
			}
			return true;
		} catch (Exception e) {
			handleRequestError(e);
			throw new RepositoryException("Add file error!", e);
		}
	}

	private void preparePostEntity(Object... params) {
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
		
		postEntity = new HttpEntity<MultiValueMap<String, Object>>(parameters, headers);
	}

	public boolean removeFile(File file, String path) throws RepositoryException {
		try {
			prepareTemplate();
			utils.logToAll("Удаление файла " + file.getName() + "...");
			preparePostEntity("fileName", file.getName(), "path", path);
			RemoveResult removeResult = template.exchange(serverUrl + "/rest/files/remove", HttpMethod.POST, postEntity, RemoveResult.class).getBody();
			logResult(removeResult);
			return removeResult.isSuccess();
		} catch (Exception e) {
			handleRequestError(e);
			throw new RepositoryException("Remove file error!", e);
		}
	}

	public boolean addFolder(String path) throws RepositoryException {
		try {
			prepareTemplate();
			utils.logToAll("Добавление папки " + path + "...");
			preparePostEntity("path", path);
			UploadResult addResult = template.exchange(serverUrl + "/rest/files/addFolder", HttpMethod.POST, postEntity, UploadResult.class).getBody();
			logResult(addResult);
			return addResult.isSuccess();
		} catch (Exception e) {
			handleRequestError(e);
			throw new RepositoryException("Add folder error!", e);
		}
	}
	
	public boolean removeFolder(String path) throws RepositoryException {
		try {
			prepareTemplate();
			utils.logToAll("Удаление папки " + path + "...");
			preparePostEntity("path", path);
			RemoveResult removeResult = template.exchange(serverUrl + "/rest/files/removeFolder", HttpMethod.POST, postEntity, RemoveResult.class).getBody();
			logResult(removeResult);
			return removeResult.isSuccess();
		} catch (Exception e) {
			handleRequestError(e);
			throw new RepositoryException("Remove folder error!", e);
		}
	}
	
	private void handleRequestError(Exception e) throws RepositoryException {
		if (e.getCause() instanceof ConnectException) {
			utils.logToAll("Сервер недоступен");
			connectionErrors++;
			
			if (connectionErrors >= 5) {
				utils.logToAll("Получено 5 ошибок соединения подряд. Повторная попытка через 10 минут.");
				try {
					fileSender.sleep(10 * 60 * 1000);
					connectionErrors = 0;
				} catch (InterruptedException e1) {
					throw new RepositoryException("Sleep interrupted", e1);
				}
			}
		} else {
			utils.logToAll("Ошибка: " + ExceptionUtils.getFullStackTrace(e));
		}
		utils.logToFile(e);
	}
	
	private void logResult(CommonRequestResult result) {
		if (result.isSuccess()) {
			utils.logToAll("Успешно.");
		} else {
			utils.logToAll("Ошибка.");
			utils.logToFile(result.getError());
		}
	}

}
