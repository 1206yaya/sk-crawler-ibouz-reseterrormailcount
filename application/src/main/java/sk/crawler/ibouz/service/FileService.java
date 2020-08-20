package sk.crawler.ibouz.service;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

import sk.crawler.ibouz.library.domain.site.Site;
@Service
public class FileService {
	public List<Site> getSites(Path companyiesCSV) {
		List<Site> allSet = new ArrayList<>();
		try {

			FileInputStream fis = new FileInputStream(companyiesCSV.toString());
			InputStreamReader isr = new InputStreamReader(fis, "shift-jis");
			Reader reader = new BufferedReader(isr);

			String[] HEADER = new String[] { "name", "ip", "suburl", "loginid", "loginpw", "is_alive" };
			List<CSVRecord> records = CSVFormat.EXCEL.withHeader(HEADER).withFirstRecordAsHeader().parse(reader).getRecords();
			for (int i = 0; i < records.size(); i++) {
				CSVRecord record = records.get(i);
				String cell = record.get("is_alive");
				if (cell.equals("0")) {
					continue;
				}
				Site site = new Site();
				
				cell = record.get("name");
				site.setName(cell);
				
				cell = record.get("ip");
				site.setIp(cell);

				cell = record.get("suburl");
				site.setSuburl(cell);

				cell = record.get("loginid");
				site.setLoginid(cell);

				cell = record.get("loginpw");
				site.setLoginpw(cell);
				
				allSet.add(site);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return allSet;
	}
}
