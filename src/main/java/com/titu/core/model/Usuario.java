package com.titu.core.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email; // Vamos usar o e-mail para fazer o login!

    @Column(nullable = false)
    private String senha;

    // Regra de acesso (Ex: "ROLE_ADMIN" ou "ROLE_USER")
    @Column(nullable = false)
    private String role;
}