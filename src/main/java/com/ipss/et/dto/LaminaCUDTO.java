package com.ipss.et.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LaminaCUDTO {
    private Integer numero;

    @JsonProperty("tipo de lamina")
    private java.util.Map<String, Long> tipo; // {"id": 1}

    private java.util.Map<String, Long> album; // {"id": 123}
}
