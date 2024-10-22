package com.mteam.sleerenthome;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

@SpringBootApplication
public class TestClobIBsheetMyApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestClobIBsheetMyApplication.class, args);

        // DataController 인스턴스 생성 및 getDataAsJson 호출
        DataController controller = new DataController();
        try {
            Map<String, Object> jsonData = controller.getDataAsJson();
            // ObjectMapper를 사용하여 JSON으로 출력
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonOutput = objectMapper.writeValueAsString(jsonData);
            System.out.println(jsonOutput);  // 콘솔에 JSON 출력
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

@RestController
class DataController {

    @GetMapping("/data")
    public Map<String, Object> getDataAsJson() {
        // 헤더 정보 생성
        List<String> sheetHeaders = Arrays.asList("Header Name 1", "Header Name 2", "Header Name 3");
        List<String> sheetHeaderId = Arrays.asList("header1", "header2", "header3");
        List<String> links = Arrays.asList("http://example.com/link1", "http://example.com/link2", "http://example.com/link3");

        // 데이터 리스트 생성
        List<Map<String, String>> dataList = new ArrayList<>();
        Map<String, String> row1 = new HashMap<>();
        row1.put("header1", "data1");
        row1.put("header2", "data2");
        row1.put("header3", "data3");
        dataList.add(row1);

        Map<String, Object> result = new HashMap<>();
        result.put("sheetHeaders", sheetHeaders);  // UI에 표시될 헤더 정보
        result.put("sheetHeaderId", sheetHeaderId);  // 데이터 매핑에 사용할 ID
        result.put("links", links);  // 링크 정보
        result.put("data", dataList); // 데이터 리스트

        return result;  // JSON 형태로 클라이언트에 전달
    }
}
