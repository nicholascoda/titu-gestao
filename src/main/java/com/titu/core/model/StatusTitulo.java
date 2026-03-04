package com.titu.core.model;

public enum StatusTitulo {
    //Isso blinda seu sistema. Ninguém vai poder escrever "Pago" ou "pagou" errado.
    // Só aceita os valores que definirmos.
    PENDENTE,
    VENCIDO,
    PAGO,
    CANCELADO
}
