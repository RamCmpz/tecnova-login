package com.ramoncitoacademy.holamundoweb;

/**
 * Patrón Strategy: cada tipo de comprobante (boleta, factura) define sus
 * propias reglas y su serie. VentaRepository trabaja con esta interfaz y no
 * necesita saber qué tipo concreto está emitiendo.
 */
public interface ComprobanteStrategy {

    String getTipo();

    String getSerie();

    /** true si el comprobante debe guardar el RUC y la razón social del cliente. */
    boolean requiereRuc();

    /** Mensaje de error si el cliente no puede recibir este comprobante; null si está bien. */
    String validar(Cliente cliente);

    /** Devuelve la estrategia del tipo indicado, o null si el tipo no existe. */
    static ComprobanteStrategy de(String tipo) {
        if ("Boleta".equals(tipo)) {
            return new BoletaStrategy();
        }
        if ("Factura".equals(tipo)) {
            return new FacturaStrategy();
        }
        return null;
    }
}
