package com.mteam.sleerenthome.repository;

import com.mteam.sleerenthome.model.MyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MyRepository extends JpaRepository<MyEntity, Integer> {

}
