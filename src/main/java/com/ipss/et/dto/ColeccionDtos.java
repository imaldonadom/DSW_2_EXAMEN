package com.ipss.et.dto;

public class ColeccionDtos {

    public static class AltaUnitariaReq {
        public Long coleccionistaId;
        public Long albumId;
        public Integer laminaNumero;
        public Integer cantidad;
    }

    /** <<< NUEVO: respuesta liviana para el unitario >>> */
    public static class AltaUnitariaResp {
        public Long id;
        public Integer numero;
        public Integer cantidad;

        public AltaUnitariaResp(Long id, Integer numero, Integer cantidad) {
            this.id = id;
            this.numero = numero;
            this.cantidad = cantidad;
        }
    }

    public static class TotalesResp {
        public int totalAlbum;
        public int coleccionadas;
        public int faltan;
        public int duplicadas;
        public TotalesResp(int t, int c, int f, int d) {
            totalAlbum = t; coleccionadas = c; faltan = f; duplicadas = d;
        }
    }
}
