# Processador de Vídeo Distribuído (O "Herói")

Este projeto consiste na re-implementação do sistema de processamento de vídeos utilizando uma arquitetura de microsserviços orientada a eventos. Ele foi desenvolvido como a contraparte ("O Herói") ao sistema monolítico, com o objetivo de demonstrar ganhos em escalabilidade, resiliência e processamento paralelo assíncrono.

O sistema utiliza Apache Kafka para orquestração de eventos e Docker para isolamento dos serviços, permitindo o processamento simultâneo de múltiplos vídeos através de workers distribuídos.

## Desenvolvedores

* Caio Fernando Dias
* Lucas Dogo de Souza Pezzuto
* Matheus Malvão Barbosa

---

## Descrição do Projeto

O sistema é uma plataforma de processamento de vídeo distribuída que desacopla as etapas de upload, processamento e notificação. Diferente da versão monolítica, onde o usuário aguardava o término de todo o lote, nesta versão o upload é confirmado imediatamente, e o processamento ocorre em segundo plano (background) através de uma cadeia de eventos (pipeline).

A arquitetura resolve os gargalos de bloqueio de I/O e CPU, permitindo que múltiplos vídeos sejam redimensionados, marcados e transcodificados simultaneamente por diferentes instâncias de serviço.

---

## Funcionalidades

1.  **Upload Assíncrono:** Recebimento de vídeos em lote sem bloqueio do cliente (resposta imediata com UUIDs de rastreamento).
2.  **Pipeline de Processamento:**
    * **Resizer Service:** Redimensiona o vídeo para 480p.
    * **Watermarker Service:** Adiciona marca d'água textual sobre o vídeo.
    * **Transcoder Service:** Converte o vídeo final para o codec H.264/AAC.
3.  **Escalabilidade Horizontal:** Capacidade de aumentar o número de instâncias (workers) de processamento sob demanda para lidar com alta carga.
4.  **Service Discovery:** Registro e localização dinâmica de serviços via Netflix Eureka.
5.  **API Gateway:** Ponto único de entrada para o cliente, realizando o roteamento para os microsserviços.
6.  **Notificação:** Envio de e-mail automático ao término do processamento de cada vídeo.

---

## Ferramentas Necessárias

Para compilar e executar o projeto, são necessárias as seguintes ferramentas instaladas no ambiente:

* **Docker** (v20.10 ou superior)
* **Docker Compose** (v2.0 ou superior)
* **Git** (para clonagem do repositório)
* **(Opcional)** Postman ou Insomnia para realizar as requisições de teste.
* **(Opcional)** Terminal Bash (Linux/Mac/WSL) para execução do script de automação.

---

## Requisitos Mínimos de Hardware

Devido à natureza distribuída e ao uso de processamento de vídeo (FFmpeg) em múltiplos containers, recomenda-se:

* **Processador:** Mínimo 4 núcleos (para suportar múltiplos containers Java e Kafka simultaneamente).
* **Memória RAM:** Mínimo 8GB (ideal 16GB), pois o ambiente executa Kafka, Zookeeper, Postgres, Eureka e 4 microsserviços Java simultaneamente.
* **Espaço em Disco:** Mínimo 10GB livres para imagens Docker e volumes de processamento de vídeo.

---

## Instruções para Compilar e Executar

### 1. Configuração de Credenciais (Obrigatório)

Antes de iniciar, é necessário configurar as credenciais para o envio de e-mail.

1.  Na raiz do projeto, crie um arquivo chamado `.env`.
2.  Adicione o seguinte conteúdo, substituindo pelos seus dados reais (para Gmail, utilize uma Senha de App):

```properties
MAIL_USERNAME=seu-email@gmail.com
MAIL_PASSWORD=sua-senha-de-app
````

### 2\. Execução Automatizada (Linux / Mac / WSL)

Se estiver utilizando um ambiente Unix, execute o script de automação que realiza a limpeza, build, criação de tópicos e escalonamento automaticamente:

```bash
./start.sh
```

### 3\. Execução Manual (Windows / PowerShell)

Caso não consiga executar o script `.sh`, siga os passos abaixo manualmente no terminal:

**Passo A: Preparar Diretório e Limpar**
Certifique-se de criar a pasta para visualização dos vídeos e remover containers antigos.

```powershell
mkdir videos_processed
docker compose down --volumes --remove-orphans
```

**Passo B: Iniciar Infraestrutura**
Compile e inicie os serviços base (1 instância de cada).

```powershell
docker compose up -d --build
```

*Aguarde cerca de 30 a 60 segundos para que o Kafka e o Eureka inicializem completamente.*

**Passo C: Configurar Kafka (Criação de Tópicos)**
Execute o comando abaixo para garantir que os tópicos tenham 4 partições, permitindo o paralelismo.

```powershell
docker exec micro_kafka kafka-topics --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 4 --topic video-received-topic --if-not-exists
docker exec micro_kafka kafka-topics --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 4 --topic video-resized-topic --if-not-exists
docker exec micro_kafka kafka-topics --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 4 --topic video-watermarked-topic --if-not-exists
docker exec micro_kafka kafka-topics --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 4 --topic video-completed-topic --if-not-exists
```

**Passo D: Escalar Workers**
Suba instâncias adicionais dos serviços de processamento para demonstrar o paralelismo.

```powershell
docker compose up -d --scale resizer=3 --scale watermarker=3 --scale transcoder=3
```

**Passo E: Visualizar Logs**
Acompanhe o processamento distribuído em tempo real.

```powershell
docker compose logs -f resizer watermarker transcoder
```

### 4\. Como Parar

Para encerrar a execução e limpar os recursos consumidos:

**Linux/Mac:**

```bash
./stop.sh
```

**Windows:**

```powershell
docker compose down --volumes --remove-orphans
```

-----

## Como Testar

Com o sistema em execução, utilize uma ferramenta de API para enviar os vídeos.

**Endpoint:** `POST http://localhost:8080/api/v1/videos/upload`

**Configuração da Requisição:**

* **Método:** POST
* **Tipo de Corpo:** `form-data`
* **Chave:** `files` (Tipo: File) -\> Selecione múltiplos arquivos de vídeo (recomendado: 4 arquivos).

**Verificação dos Resultados:**

1.  **Resposta HTTP:** O sistema retornará código `200 OK` imediatamente, confirmando o recebimento.
2.  **Logs:** No terminal, será possível visualizar logs de diferentes instâncias (ex: `resizer-1`, `resizer-2`) processando os vídeos em paralelo.
3.  **Arquivos Gerados:** Os vídeos processados aparecerão automaticamente na pasta local `videos_processed`, criada na raiz do projeto.
4.  **E-mail:** Um e-mail de notificação será enviado para o endereço configurado ao final do processamento de cada vídeo.

<!-- end list -->