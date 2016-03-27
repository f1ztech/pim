package ru.mipt.pim.adapters.fs;

import java.io.File;
import java.io.PrintStream;
import java.util.Scanner;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
public class FSAdapterRunner implements CommandLineRunner {

	@Resource
	private ConfigsService configsService;

	@Resource
	private DirectoryWatcherService directoriesWatcher;

	@Resource
	private Utils utils;

	private PrintStream out = System.out;

	private Scanner scanner;

	public static void main(String[] args) {
		SpringApplication.run(FSAdapterRunner.class, args);
	}

	@Override
	public void run(String... arg0) throws Exception {
		if (!directoriesWatcher.getWatchedDirectories().isEmpty()) {
			printCurrentDirectories();
		}

		scanner = new Scanner(System.in);
		if (configsService.getLogin() == null || configsService.getPassword() == null) {
			readLoginAndPassword();
		}

		directoriesWatcher.startWatch();

		String line = null;
		try {
			while (true) {
				utils.logToAll("Введите: \n"
						+ " путь до папки содержащей файлы \n"
						+ "   или 'p' для смены логин/пароля \n"
						+ "   или 'q' для выхода \n"
						+ "   или 'r <название_папки>' чтобы прекратить синхронизацию папки \n"
						+ "   или 'clear' чтобы очистить синхронизуемые папки \n"
						+ "   или 'list' чтобы просмотреть список синхронизованных папок:");
				line = scanner.nextLine();
				switch (line) {
				case "q":
					utils.logToAll("Выход...");
					return;
				case "clear":
					directoriesWatcher.clearWatchedDirectories();
					break;
				case "list":
					printCurrentDirectories();
					break;
				case "p":
					readLoginAndPassword();
					break;
				default:
					File newDirectory = new File(line.replaceAll("\\|/", File.separator));
					if (!newDirectory.exists()) {
						utils.logToAll("Папка не найдена!");
					} else if (!newDirectory.isDirectory()) {
						utils.logToAll("Указанный путь не является папкой!");
					} else {
						String directoryName = extractDirectoryName(newDirectory);
						if (directoriesWatcher.getWatchedDirectories().stream().anyMatch(file -> extractDirectoryName(file).equals(directoryName))) {
							utils.logToAll("Папка c названием " + directoryName + " уже синхронизирована! Синхронизация нескольких папок с одинаковым наименованием не поддерживается.");
						}
						directoriesWatcher.addWatchedDirectory(newDirectory.getAbsolutePath());
						utils.logToAll("Папка добавлена!");
					}
					break;
				}
			}
		} finally {
			scanner.close();
		}
	}

	private String extractDirectoryName(File newDirectory) {
		return StringUtils.substringAfterLast(newDirectory.getName(), File.separator);
	}

	private void readLoginAndPassword() {
		utils.logToAll("Введите логин:");
		configsService.setLogin(scanner.nextLine());

		utils.logToAll("Введите пароль:");
		configsService.setPassword(scanner.nextLine());
	}

	private void printCurrentDirectories() {
		utils.logToAll("Текущие синхронизируемые папки: ");
		for (File file : directoriesWatcher.getWatchedDirectories()) {
			utils.logToAll(file.getAbsolutePath());
		}
	}

}
