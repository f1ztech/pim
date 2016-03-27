package ru.mipt.pim.util;

import java.util.function.Consumer;
import java.util.function.Function;

public class Exceptions {

	@FunctionalInterface
	public static interface FunctionWithExceptions<T, R> {
		public R apply(T param) throws Exception;
	}

	@FunctionalInterface
	public static interface ConsumerWithExceptions<T> {
		public void accept(T param) throws Exception;
	}

	@FunctionalInterface
	public static interface RunnableWithExceptions {
		public void run() throws Exception;
	}

	public static void ignore(Exceptions.RunnableWithExceptions runnable) {
		try {
			runnable.run();
		} catch (Exception ignore) { }
	}

	public static void ignoreAndLog(Exceptions.RunnableWithExceptions runnable, Consumer<Exception> logFunction) {
		try {
			runnable.run();
		} catch (Exception e) {
			logFunction.accept(e);
		}
	}


	public static void wrap(Exceptions.RunnableWithExceptions runnable) {
		try {
			runnable.run();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static <T, R> Function<T, R> wrap(Exceptions.FunctionWithExceptions<T, R> runnable) {
		return t -> {
			try {
				return runnable.apply(t);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};
	}

	public static <T> Consumer<T> wrap(Exceptions.ConsumerWithExceptions<T> runnable) {
		return t -> {
			try {
				runnable.accept(t);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};
	}

	public static <T> Consumer<T> wrap(Exceptions.ConsumerWithExceptions<T> runnable, Consumer<Exception> logFunction) {
		return t -> {
			try {
				runnable.accept(t);
			} catch (Exception e) {
				logFunction.accept(e);
			}
		};
	}

}
