package com.ramoncitoacademy.holamundoweb;

public class BoletaStrategy implements ComprobanteStrategy {

    @Override
    public String getTipo() { return "Boleta"; }

    @Override
    public String getSerie() { return "B001"; }

    @Override
    public boolean requiereRuc() { return false; }

    @Override
    public String validar(Cliente cliente) {
        return null; // cualquier cliente activo puede recibir boleta
    }
}
