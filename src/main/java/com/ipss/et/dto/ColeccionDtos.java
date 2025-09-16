package com.ipss.et.dto;

public class ColeccionDtos {

    public static class AltaUnitariaReq {
        public Long coleccionistaId; // si no gestionas usuarios, manda 1
        public Long albumId;
        public Integer laminaNumero; // ej: 7
        public Integer cantidad;     // ej: 1
    }

    public static class TotalesResp {
        public int totalAlbum;      // total de láminas del álbum
        public int coleccionadas;   // láminas distintas que tiene
        public int faltan;          // totalAlbum - coleccionadas
        public int duplicadas;      // sum(cantidades extra) = sum(max(cant-1,0))
        public TotalesResp(int totalAlbum, int coleccionadas, int faltan, int duplicadas) {
            this.totalAlbum = totalAlbum; this.coleccionadas = coleccionadas;
            this.faltan = faltan; this.duplicadas = duplicadas;
        }
    }
}