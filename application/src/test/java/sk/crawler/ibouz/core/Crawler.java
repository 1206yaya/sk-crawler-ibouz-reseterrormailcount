package sk.crawler.ibouz.core;

import static com.codeborne.selenide.Selenide.open;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;

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

	@Autowired WebDriverUtil webDriverUtil;
	@Autowired FileService fileService;
	
	private static Path settingFile;
	LocalDate today;
	LocalDate tomorrow;
	public void setUp() throws IOException {
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
		System.out.println("=========================");
		System.out.println("24時まで、0分と30分に実行します。");
		System.out.println("0分と30分以外は待機状態になります。");
		System.out.println("=========================");
		setUp();
		boolean isFirst = true;
		List<Site> sites = fileService.getSites(settingFile);
		
		while (true) {
			LocalDateTime now = LocalDateTime.now();
			if (now.isAfter(LocalDateTime.of(tomorrow, LocalTime.of(00, 00)))) {
				break;
			}
			
			// 30分ごとに実行したいので、0分と30分に実行するようにする
			if (isFirst || now.getMinute() == 30 || now.getMinute() == 0) {
				for (Site site : sites) {
					ibouz = IbouzBuilder.createIbouz(site);
					ibouz.loginByPlaneText();
					UserSearchPage userSearchPage = open(ibouz.getUserSearchURL(), UserSearchPage.class);
					// メインメールエラー
					userSearchPage.setMailErrorNum(1);
					// // 最終送信
					// userSearchPage.setLastSendTime(lastSendTimeSince, lastSendTimeUntil);
					//  累計送信数
					userSearchPage.setTotalSendingCount(1);
					
					UserSearchResultPage userSearchResultPage = userSearchPage.search();
					userSearchResultPage.clickResetMallErrorCount();
					System.out.println("=========================");
					System.out.println(site.getName() + " " + now + " DONE");
					System.out.println("=========================");
					WebDriverRunner.getWebDriver().quit();
					isFirst = false;
				}
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
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
