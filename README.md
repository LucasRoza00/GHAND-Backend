# GHAND
## GHand é um projeto de controle de fluxo de informações de fornecedores.
### Temos como missão e objetivo centralizar cada informação referente a fornecedores podendo oferecer uma eficacia nas 
### atividades diárias como recepção de produtos, contato com fornecedores, controle de entrega e controle de datas para pagamentos.

## Tecnologias utilizadas:
- Java 19
- Spring
- MongoDB Atlas

## Pré requisitos:
### JAVA 19
### DOCKER

## Instruções de como rodar o projeto:

```bash
git clone https://github.com/starktk/GH_ApiSupply
```
```bash
cd GH_ApiSupply
```
```bash
docker --Version
```
## Caso não esteja com o docker instalado.
### Windows -> https://docs.docker.com/desktop/setup/install/windows-install/
### Linux -> https://docs.docker.com/engine/install/ubuntu/

```bash
docker build -t gh-api-supply
```
```bash
docker run -p 8084:8084 gh-api-supply
```
## Para acessar EndPoints da API utilizar as seguinte urls após inicializar o projeto:
 - http://localhost:8084
 - https://ghand-backend-production.up.railway.app

## Desenvolvido por: Raul Rodrigues
