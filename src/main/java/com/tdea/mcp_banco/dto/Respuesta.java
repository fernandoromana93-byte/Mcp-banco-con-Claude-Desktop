package com.tdea.mcp_banco.dto;

/** Envoltorio común: exito=true trae datos, exito=false trae el mensaje de error. */
public record Respuesta(boolean exito, String error, Object datos) {

    public static Respuesta ok(Object datos) {
        return new Respuesta(true, null, datos);
    }

    public static Respuesta error(String mensaje) {
        return new Respuesta(false, mensaje, null);
    }
}