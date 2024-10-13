package com.mteam.sleerenthome.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "my_table")
public class MyEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Lob
    @Column(name = "clob1", columnDefinition = "CLOB")
    private String clob1;  // JSON 데이터를 저장할 필드

    @Lob
    @Column(name = "clob2", columnDefinition = "CLOB")
    private String clob2;  // HTML 데이터를 저장할 필드

}
