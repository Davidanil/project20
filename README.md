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

<table>
	<tr>
	<th>Kevin</th>
	<th>David</th>
	<th>Guilherme</th>
	</tr>
  <tr>
	  <td><s>Last Location</s></td>
    <td>Tracking</td>
	<td>Secure channels (1)</td>
  </tr>
  <tr>
    <td colspan="2">Nonces (register/add devices)</td>
	<td><s>Message Hashes</s></td>
  </tr>
  <tr>
    <td colspan="3">2factor authentication / reauthentication</td>
  </tr>
  <tr>
    <td colspan="3">certificates</td>
  </tr>
</table>
(1) - server vai ter um Map<phoneNumber,SymetricKey> e comunicacao inicial entre client e server e feita com cifra assimetrica


* A cada tentativa falhada, incrementar "attempts" na DB, com login bem sucedido meter a 0.
* Tratar do verified (como simular isto?)
