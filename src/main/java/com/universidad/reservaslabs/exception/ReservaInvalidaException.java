package com.universidad.reservaslabs.exception;

// Horario o duración fuera de lo permitido: el error está en los datos
// enviados, por eso se responde 400 y no 409.
public class ReservaInvalidaException extends RuntimeException {
    public ReservaInvalidaException(String mensaje) { super(mensaje); }
}
