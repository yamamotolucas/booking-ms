<div align="center">

# 📅 Booking Microservice (`booking-ms`)

<p align="center">
  <b>Coração de agendamentos e regras de negócio da plataforma, integrando comunicação síncrona tolerante a falhas e mensageria assíncrona.</b>
</p>

<!-- Badges -->
![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0.5-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Spring Cloud](https://img.shields.io/badge/Spring_Cloud-2025.1.1-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![OpenFeign](https://img.shields.io/badge/OpenFeign-HTTP_Client-blue?style=for-the-badge&logo=spring&logoColor=white)
![Resilience4j](https://img.shields.io/badge/Resilience4j-Circuit_Breaker-black?style=for-the-badge)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-AMQP-FF6600?style=for-the-badge&logo=rabbitmq&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Flyway](https://img.shields.io/badge/Flyway-CC0202?style=for-the-badge&logo=flyway&logoColor=white)
![Netflix Eureka](https://img.shields.io/badge/Eureka-Discovery_Client-brightgreen?style=for-the-badge&logo=spring&logoColor=white)
![Swagger / OpenAPI](https://img.shields.io/badge/Swagger-OpenAPI_3-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)

</div>

---

## 📌 Visão Geral

O **`booking-ms`** é o microsserviço responsável pelo ciclo de vida completo dos agendamentos na barbearia. Ele consolida validações complexas de horários, disponibilidade de barbeiros, limites diários de agendamento por cliente e comunicação distribuída com outros serviços via **OpenFeign com Resilience4j** (síncrono) e **RabbitMQ** (assíncrono).

### ✨ Principais Funcionalidades

- 📆 **Gestão de Agendamentos:** Criação, cancelamento, alteração de status e listagem de reservas por período e cliente.
- ⚡ **Comunicação Inter-serviços com OpenFeign:** Consumo do `identity-ms` para validação de perfis e detalhes de clientes/barbeiros sem acoplamento direto.
- 🛡️ **Tolerância a Falhas com Resilience4j:** Circuit Breaker implementado nas integrações HTTP externas para prevenir falhas em cascata.
- 📬 **Mensageria Assíncrona com RabbitMQ:** Publicação para envio assíncrono de notificações.
- 🔍 **Validações de Domínio Customizadas:** Verificação de conflito de agendas, limite diário de marcações por usuário e regras de antecedência mínima.
- 🗄️ **Versionamento de Banco com Flyway:** Migrations estruturadas para tabelas de serviços, agendamentos e vínculos relacionais no MySQL.
- 📖 **Documentação Swagger/OpenAPI:** Catálogo interativo de contratos de API e schemas JSON.

---

## 🏗️ Arquitetura & Estrutura de Pastas

O microsserviço adota uma arquitetura em camadas estruturada para suportar regras de negócio ricas e clientes externos:

```text
booking-ms/
├── src/
│   ├── main/
│   │   ├── java/br/com/booking_ms/
│   │   │   ├── amqp/              # Publicadores de eventos no RabbitMQ
│   │   │   ├── client/            # Clientes OpenFeign
│   │   │   ├── config/            # SecurityConfig
│   │   │   ├── controller/        # REST Controllers (BookingController, ServiceCatalogController)
│   │   │   ├── dto/               # Records de entrada/saída (BookingRequest, BookingResponse)
│   │   │   ├── exception/         # Handlers de exceção e erros de validação (@RestControllerAdvice)
│   │   │   ├── jobs/              # Auto-update de status de agendamentos
│   │   │   ├── model/             # Entidades
│   │   │   ├── repository/        # Spring Data JPA Repositories com queries de conflito de horários
│   │   │   ├── service/           # Regras de negócio, agendamento e orquestração
│   │   │   └── validation/        # Validadores desacoplados (ex: BookingDailyLimitValidator)
│   │   └── resources/
│   │       ├── db/migration/             # Scripts SQL versionados do Flyway (V1__create_tables.sql...)
│   │       └── application.properties    # Configurações do Spring, Eureka, RabbitMQ e Feign
│   └── test/
│       └── java/br/com/booking_ms/ # Testes unitários (Mockito) e de validação
│      
├── pom.xml
└── Dockerfile
```

---

## 🔌 Endpoints da API

> 💡 A documentação interativa com Swagger UI pode ser acessada em:  
> `http://localhost:8080/webjars/swagger-ui/index.html` (via API Gateway)

---

## ⚙️ Variáveis de Ambiente Esperadas

As configurações principais gerenciadas no `application.properties` aceitam as seguintes variáveis de ambiente:

| Variável | Descrição | Valor Padrão / Exemplo |
| :--- | :--- | :--- |
| `SERVER_PORT` | Porta de execução do microsserviço | `8083` |
| `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` | URL do servidor de Service Discovery (Eureka) | `http://localhost:8081/eureka` |
| `SPRING_DATASOURCE_URL` | URL de conexão JDBC com o MySQL | `jdbc:mysql://localhost:3306/booking_ms?createDatabaseIfNotExist=true` |
| `SPRING_DATASOURCE_USERNAME` | Usuário do banco de dados | `root` |
| `SPRING_DATASOURCE_PASSWORD` | Senha de acesso ao banco | `root123` |
| `JWT_SECRET` | Chave secreta HMAC-SHA256 para validação dos tokens | `SUA_CHAVE_JWT_SECRETA_LOCAL_COM_NO_MINIMO_32_CARACTERES` |
| `SPRING_RABBITMQ_HOST` | Host da instância do RabbitMQ | `localhost` |
| `SPRING_RABBITMQ_PORT` | Porta de comunicação AMQP do RabbitMQ | `5672` |
| `SPRING_RABBITMQ_USERNAME` | Usuário de autenticação no RabbitMQ | `guest` |
| `SPRING_RABBITMQ_PASSWORD` | Senha de autenticação no RabbitMQ | `guest` |

---

## 🧪 Testes Automatizados

O microsserviço possui testes unitários focados em manter a consistência das regras de negócio:

- **Testes de unidade:** Testes automatizados das principais funcionalidades e validações de regras.
- **Validators Test:** Verificação isolada de regras de domínio como limite diário de reservas e intervalos de agenda.

```bash
# Execução dos testes
./mvnw test
```

---
## ⚠️ Observações de Execução no Ecossistema

> **Atenção:** Este microsserviço integra uma arquitetura distribuída. Para o seu funcionamento pleno (comunicação entre serviços, descoberta de rotas e persistência de dados), é indispensável que o **`eureka-server`**, uma instância ativa do **MySQL** e os demais microsserviços dependentes estejam em execução.
>
> Para executar ou clonar o ecossistema completo com todas as dependências e submódulos configurados, acesse o repositório central:  
> 🔗 [**barbearia-network-main**](https://github.com/yamamotolucas/barbearia-network-main)
