package com.titu.core.repository;

import com.titu.core.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    // O Spring cria o SQL sozinho baseado nesses nomes:
    boolean existsByEmail(String email);
    boolean existsByCnpj(String cnpj);

    Optional<Cliente> findByEmail(String email);
}