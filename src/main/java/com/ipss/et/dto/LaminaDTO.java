package com.ipss.et.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ipss.et.model.Lamina;
import lombok.Builder;
import lombok.Value;

import java.util.Map;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LaminaDTO {
    Long id;
    Integer numero;

    @JsonProperty("tipo de lamina")
    Map<String, Object> tipo;      // {id, nombre}

    Map<String, Long> album;       // {"id": <albumId>}

    public static LaminaDTO of(Lamina l) {
        Map<String,Object> tipoMap = (l.getTipoLamina() == null)
                ? null
                : Map.<String,Object>of(
                    "id", l.getTipoLamina().getId(),
                    "nombre", l.getTipoLamina().getNombre()
                  );

        return LaminaDTO.builder()
                .id(l.getId())
                .numero(l.getNumero())
                .tipo(tipoMap)
                .album(Map.of("id", l.getAlbum().getId()))
                .build();
    }
}
