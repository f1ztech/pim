package ru.mipt.pim.adapters.fs;

import javax.annotation.Resource;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class Utils {
	
	@Resource
	private ConfigsService configService;

	@FunctionalInterface
	public static interface RunnableWithExceptions {
		public void run() throws Exception;
	}
	
	public static void ignoreExceptions(RunnableWithExceptions runnable) {
		try {
			runnable.run();
		} catch (Exception ignore) { }
	}
	
	public void debug(String message) {
		if (configService.isDebug()) {
			Logger.getLogger("console").info(message);		
		}
	}
	
	public void logToAll(String error) {
		Logger.getLogger("console").info(error);
		Logger.getLogger("file").info(error);
	}
	
	public void logToAll(String error, Exception e) {
		Logger.getLogger("console").info(error + " " + e.toString());
		Logger.getLogger("file").info(error, e);
		if (configService.isDebug()) {
			Logger.getLogger("console").info(ExceptionUtils.getFullStackTrace(e));		
		}
	}

	
	public void logToFile(String message) {
		logToFile(message, null);
	}
	
	public void logToFile(Exception e) {
		logToFile(null, e);
	}
	
	public void logToFile(String message, Exception e) {
		if (message != null) {
			Logger.getLogger("file").error(message);
			if (configService.isDebug()) {
				Logger.getLogger("console").info(message);		
			}
		}
		if (e != null) {
			Logger.getLogger("file").error(e);
			if (configService.isDebug()) {
				Logger.getLogger("console").info(ExceptionUtils.getFullStackTrace(e));		
			}
		}
	}
}
