package com.mteam.sleerenthome.utils;

import java.util.*;

public class DynamicClobParser {
    public static void main(String[] args) {
        // 예시 CLOB 데이터 (가변적인 헤더와 데이터, 결과 컬럼 개수)
        String clobData = "header1;header2;header3;header4;data1;data2;data3;data4;result1;result2;"
                + "data21;data22;data23;data24;result21;result22;"
                + "data31;data32;data33;data34;result31;result32;"
                // 더 많은 데이터가 있다고 가정
                + "data10000;data10001;data10002;data10003;result10000;result10001;";

        // 데이터를 세미콜론(;)으로 분리
        String[] splitData = clobData.split(";");

        // 동적 헤더 수 파악 (헤더의 시작은 데이터 시작 이전까지임)
        int headerCount = 0;
        for (int i = 0; i < splitData.length; i++) {
            if (splitData[i].startsWith("data")) {
                headerCount = i; // 헤더 개수 파악
                break;
            }
        }

        // 각 row의 데이터가 포함된 결과 컬럼 개수 파악
        int resultCount = 0;
        for (int i = headerCount; i < splitData.length; i++) {
            if (splitData[i].startsWith("result")) {
                resultCount++;
            } else {
                break;
            }
        }

        int rowDataCount = headerCount + resultCount;  // 한 row에 포함된 전체 데이터 수

        // 데이터를 저장할 리스트
        List<Map<String, String>> dataList = new ArrayList<>();

        // 동적으로 행(row) 데이터를 생성
        for (int i = headerCount; i < splitData.length; i += rowDataCount) {
            if (i + rowDataCount > splitData.length) break; // 데이터 부족 시 중단

            Map<String, String> rowMap = new HashMap<>();

            // 동적으로 헤더에 대응하는 데이터 추가
            for (int j = 0; j < headerCount; j++) {
                rowMap.put(splitData[j], splitData[i + j - headerCount]);  // 헤더-데이터 매핑
            }

            // 동적으로 결과 컬럼 추가
            for (int k = 0; k < resultCount; k++) {
                rowMap.put("result" + (k + 1), splitData[i + headerCount + k]);
            }

            dataList.add(rowMap);
        }

        // 결과 확인 (예: 첫 3개의 행만 출력)
        for (int j = 0; j < 3; j++) {
            System.out.println(dataList.get(j));
        }

    }

}



