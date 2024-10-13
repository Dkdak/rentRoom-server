package com.mteam.sleerenthome.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SaveRequest {
    private String dataJson;  // JSON 데이터를 담기 위한 필드
    private String html;      // HTML 데이터를 담기 위한 필드
}
