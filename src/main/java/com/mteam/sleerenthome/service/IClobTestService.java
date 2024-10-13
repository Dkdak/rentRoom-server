package com.mteam.sleerenthome.service;

import com.fasterxml.jackson.databind.JsonNode;

public interface IClobTestService {
    JsonNode getClob1Data(int id) throws Exception;
    String getClob2Html(int id);
    void saveClobData(int id, String jsonData, String htmlData);

}
