package com.mteam.sleerenthome.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mteam.sleerenthome.model.MyEntity;
import com.mteam.sleerenthome.model.SaveRequest;
import com.mteam.sleerenthome.repository.MyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClobTestService implements IClobTestService{


    @Autowired
    private MyRepository repository;


    private final ObjectMapper objectMapper = new ObjectMapper();

    public JsonNode getClob1Data(int id) throws Exception {
        Optional<MyEntity> entityOptional = repository.findById(id);
        if (entityOptional.isPresent()) {
            return objectMapper.readTree(entityOptional.get().getClob1());
        }
        return null;
    }

    public String getClob2Html(int id) {
        return repository.findById(id).map(MyEntity::getClob2).orElse(null);
    }

    public void saveClobData(int id, String jsonData, String htmlData) {
        MyEntity entity = repository.findById(id).orElse(new MyEntity());
        entity.setId(id);
        entity.setClob1(jsonData);
        entity.setClob2(htmlData);
        repository.save(entity);
    }

}
