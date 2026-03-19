package com.titu.core.repository;

import com.titu.core.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    // o Spring cria o SQL sozinho
    boolean existsByEmail(String email);
    boolean existsByCnpj(String cnpj);

    java.util.Optional<Cliente> findByEmail(String email);
    java.util.Optional<Cliente> findByCnpj(String cnpj);
}