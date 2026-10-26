package com.ramoncitoacademy.holamundoweb;

public class FacturaStrategy implements ComprobanteStrategy {

    @Override
    public String getTipo() { return "Factura"; }

    @Override
    public String getSerie() { return "F001"; }

    @Override
    public boolean requiereRuc() { return true; }

    @Override
    public String validar(Cliente cliente) {
        if (!cliente.isTieneRuc() || cliente.getRazonSocial() == null) {
            return "El cliente no tiene RUC registrado. Edítalo en Clientes para poder emitir factura.";
        }
        return null;
    }
}
