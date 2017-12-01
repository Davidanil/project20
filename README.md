# Projecto Grupo 20

### Correr o projecto:
* mvn clean install -DskipTests exec:java


Caso dê algum problema de binding por repetir endereços, vejam as poms do server e cliente e mudem a porta (ou então matem o processo actual)

Usefull links:
  https://crackstation.net/hashing-security.htm
  

  
Edited on 30/11/2017:
* ConnectMySql.java - funçoes para validar o registo e conectar a base de dados. (ler os TODO)
* mysql-connector-java-5.1.44-bin - external library pode ser precisa para ^
* childDB.sql - Cria DB
* login.sql - insert 2 users (ler os comments)
* connected.sql - insert 1 conexão
	

### ToDo:
* Secure channels
* ~Handlers with timestamps -> freshness Add TimeHandler to Client~
* ~Message Hashes~
* Last location of child (saved locally)
* Tracking
* Register/Add device (nonces )
* 2factor authentication / reauthentication
* certificates
