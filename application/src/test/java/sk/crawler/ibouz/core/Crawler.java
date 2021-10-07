package sk.crawler.ibouz.core;

import static com.codeborne.selenide.Selenide.open;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import com.codeborne.selenide.WebDriverRunner;
import com.codeborne.selenide.junit5.ScreenShooterExtension;

import sk.crawler.ibouz.core.config.CorePathConfig;
import sk.crawler.ibouz.library.domain.Ibouz;
import sk.crawler.ibouz.library.domain.IbouzBuilder;
import sk.crawler.ibouz.library.domain.site.Site;
import sk.crawler.ibouz.library.pages.LoginPage;
import sk.crawler.ibouz.library.pages.UserSearchPage;
import sk.crawler.ibouz.library.pages.UserSearchResultPage;
import sk.crawler.ibouz.library.util.CrawlerEnv;
import sk.crawler.ibouz.library.util.WebDriverUtil;
import sk.crawler.ibouz.service.FileService;

@SpringBootTest
class Crawler {
	static Ibouz ibouz;
	static LocalDate lastSendTimeSince;
	static LocalDate lastSendTimeUntil;

	@Value("#{systemProperties['env']}")
	CrawlerEnv env;

	@Autowired
	WebDriverUtil webDriverUtil;
	@Autowired
	FileService fileService;

	private static Path settingFile;
	LocalDate today;
	LocalDate tomorrow;

	public void setUp() throws IOException {
		System.out.println("### START ");
		settingFile = CorePathConfig.SETTINGS_CSV;
		if (env == null || env.equals(CrawlerEnv.IS_DEV)) {
			env = CrawlerEnv.IS_DEV;
			settingFile = CorePathConfig.DEV_SETTINGS_CSV;
		}
		webDriverUtil.setUp(env);
		today = LocalDate.now();
		lastSendTimeSince = today.minusDays(1);
		lastSendTimeUntil = today;
		tomorrow = today.plusDays(1);
	}
	@Test
	void contextLoads() throws IOException {
		setUp();
		List<Site> sites = fileService.getSites(settingFile);
		
		while (true) {
			LocalDateTime now = LocalDateTime.now();
			if (now.isAfter(LocalDateTime.of(tomorrow, LocalTime.of(00, 00)))) {
				break;
			}

			for (Site site : sites) {
				ibouz = IbouzBuilder.createIbouz(site);
				ibouz.loginByPlaneText();
				UserSearchPage userSearchPage = open(ibouz.getUserSearchURL(), UserSearchPage.class);
				// メインメールエラー
				userSearchPage.setMailErrorNum(1);
				// // 最終送信
				// userSearchPage.setLastSendTime(lastSendTimeSince, lastSendTimeUntil);
				// 累計送信数
				userSearchPage.setTotalSendingCount(1);
				UserSearchResultPage userSearchResultPage = userSearchPage.search();
				int totalNum = userSearchResultPage.getResultCount();
				userSearchResultPage.clickResetMallErrorCount();
				System.out.println("=========================");
				System.out.println(
						site.getSitename() + " " + now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")) + " "
								+ totalNum + " Ids DELETE");
				System.out.println("=========================");
				WebDriverRunner.getWebDriver().quit();
			}
			sleep();
		}
	}

	private void login() {
		LoginPage loginPage = open(ibouz.getLogintURL(), LoginPage.class);
		loginPage.login(ibouz.getLoginid(), ibouz.getLoginpw());
	}

	private void sleep() {
		
		try {
			TimeUnit.MINUTES.sleep(5);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
