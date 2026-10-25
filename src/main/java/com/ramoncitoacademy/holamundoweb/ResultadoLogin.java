package com.ramoncitoacademy.holamundoweb;

public class ResultadoLogin {

    public enum Estado { OK, CREDENCIALES_INVALIDAS, BLOQUEADO }

    private final Estado estado;
    private final int intentosRestantes;

    private ResultadoLogin(Estado estado, int intentosRestantes) {
        this.estado = estado;
        this.intentosRestantes = intentosRestantes;
    }

    public static ResultadoLogin ok() {
        return new ResultadoLogin(Estado.OK, -1);
    }

    public static ResultadoLogin bloqueado() {
        return new ResultadoLogin(Estado.BLOQUEADO, 0);
    }

    // intentosRestantes = -1 cuando no se conoce (usuario inexistente)
    public static ResultadoLogin credencialesInvalidas(int intentosRestantes) {
        return new ResultadoLogin(Estado.CREDENCIALES_INVALIDAS, intentosRestantes);
    }

    public Estado getEstado() {
        return estado;
    }

    public int getIntentosRestantes() {
        return intentosRestantes;
    }
}
