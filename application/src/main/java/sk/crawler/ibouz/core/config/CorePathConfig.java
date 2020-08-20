package sk.crawler.ibouz.core.config;

import java.nio.file.Path;
import java.nio.file.Paths;

public class CorePathConfig {
	public final static String PROJ_DIR = Paths.get(System.getProperty("user.dir")).toString();
	public static  final Path SETTINGS_CSV = Paths.get(PROJ_DIR, "SETTINGS.csv");
	public static  final Path DEV_SETTINGS_CSV = Paths.get(PROJ_DIR, "DEV_SETTINGS.csv");
}
