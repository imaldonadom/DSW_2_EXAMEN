package com.ipss.et.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;
import java.util.Map;

public class ColeccionPayloads {

    @Value @Builder @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ColeccionistaAlbumes {
        Map<String,Object> coleccionista; // {id,nombre}
        List<AlbumDeColeccion> album;

        @Value @Builder
        public static class AlbumDeColeccion {
            Long id;
            String nombre;

            @JsonProperty("fecha de lanzamiento") String fechaDeLanzamiento;
            @JsonProperty("fecha sorteo") String fechaSorteo;

            Map<String,Object> categoria;
            List<String> tags;

            @JsonProperty("cantidad de láminas") Integer cantidadDeLaminas;

            List<LaminaCantidad> laminas;

            Map<String,Object> estado; // {id,nombre}
            Boolean activo;
        }

        @Value @Builder
        public static class LaminaCantidad {
            Long id;
            Integer numero;
            @JsonProperty("tipo de lamina") Map<String,Object> tipo;
            Integer cantidad;
        }
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UpsertColeccion {
        private List<AlbumIn> album;

        @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
        public static class AlbumIn {
            private Long id;
            private List<LaminaIn> laminas;
            private Map<String, Long> estado; // {"id":1}
            private Boolean activo;
        }

        @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
        public static class LaminaIn {
            private Long id;
            private Integer cantidad;
        }
    }
}
