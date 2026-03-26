# 💸 Titu - Sistema de Gestão e Conciliação Financeira

## 🎯 O Projeto
O Titu é uma plataforma backend desenvolvida para automatizar a gestão de cobranças, e controle de inadimplência, com gráficos para facilitar a visualização. Criado com foco na resiliência da regra de negócio, o sistema calcula saldos devedores, gerencia múltiplos pagadores e transações pendentes.

## 🚀 Tecnologias e Arquitetura
Este projeto foi construído aplicando princípios de **SOLID** e **Clean Code**, separando claramente as camadas de acesso a dados, regras de negócio e rotas da API.
Projeto ainda em desenvolvimento, esta descrição portanto é provisória, em breve terá as telas de funcionamento e o sistema hospedado na web para testes e conhece-lo mais de perto!

* **Linguagem:** Java 17+
* **Framework:** Spring Boot (Web, Data JPA, Security)
* **Banco de Dados:** PostgreSQL
* **Testes:** JUnit 5 e Mockito (Testes Unitários e Isolamento de Services)
* **Padrões de Projeto:** Builder, Singleton, Injeção de Dependências

## 🧠 Desafios Técnicos Resolvidos
* **Refatoração para RESTful:** Transição de uma arquitetura híbrida para uma API REST pura, servindo dados em formato JSON para desacoplamento total do front-end.
* **Prevenção do N+1:** Utilização avançada de JPQL com `JOIN FETCH` para otimização de consultas e economia de processamento do banco.
* **Segurança e Isolamento:** Regras de negócio blindadas no nível de serviço, garantindo que operações financeiras não sofram mutações indevidas.

## 🛠️ Como Executar
1. Clone este repositório.
2. Configure as variáveis de ambiente no `application.properties` (credenciais do banco).
3. Execute a classe `TituApplication`.
4. A API estará escutando na porta `8080`.
