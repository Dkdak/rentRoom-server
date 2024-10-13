package com.mteam.sleerenthome.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.mteam.sleerenthome.model.SaveRequest;
import com.mteam.sleerenthome.service.IClobTestService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class ClobTestController {

    private static final Logger logger = LogManager.getLogger(RoomController.class);

    @Autowired
    private IClobTestService dataService;

    @GetMapping("/clob1/{id}")
    public ResponseEntity<JsonNode> getClob1Data(@PathVariable int id) {
        try {
            JsonNode clob1Data = dataService.getClob1Data(id);
            return ResponseEntity.ok(clob1Data);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/clob2/{id}")
    public ResponseEntity<String> getClob2Html(@PathVariable int id) {
        String clob2Html = dataService.getClob2Html(id);
        return ResponseEntity.ok(clob2Html);
    }

    @PostMapping("/save/{id}")
    public ResponseEntity<Void> saveClobData(@PathVariable int id, @RequestBody SaveRequest request) {
        dataService.saveClobData(id, request.getDataJson(), request.getHtml());
        return ResponseEntity.ok().build();
    }
}


//{
//        "headers": [
//        { "id": "column1", "name": "컬럼명1" },
//        { "id": "column2", "name": "컬럼명2" },
//        { "id": "column3", "name": "컬럼명3" },
//        { "id": "column4", "name": "컬럼명4" },
//        { "id": "column5", "name": "컬럼명5" },
//        { "id": "resultFace1", "name": "결과면1" }  // 새로운 컬럼 추가
//        ],
//        "data": [
//        { "column1": "값1-1", "column2": "값1-2", "column3": "값1-3", "column4": "값1-4", "column5": "값1-5", "resultFace1": "결과값1-1" },
//        { "column1": "값2-1", "column2": "값2-2", "column3": "값2-3", "column4": "값2-4", "column5": "값2-5", "resultFace1": "결과값2-1" },
//        { "column1": "값3-1", "column2": "값3-2", "column3": "값3-3", "column4": "값3-4", "column5": "값3-5", "resultFace1": "결과값3-1" },
//        { "column1": "값4-1", "column2": "값4-2", "column3": "값4-3", "column4": "값4-4", "column5": "값4-5", "resultFace1": "결과값4-1" },
//        { "column1": "값5-1", "column2": "값5-2", "column3": "값5-3", "column4": "값5-4", "column5": "값5-5", "resultFace1": "결과값5-1" }
//        ]
//}
//headers 배열에 { "id": "resultFace1", "name": "결과면1" }을 추가하여 새 컬럼을 정의합니다.
//data 배열에서도 각 row에 새로운 값 "resultFace1": "결과값1-1"과 같은 형태로 데이터를 추가합니다.