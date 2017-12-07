# Login to mysql with user root and password toor
mysql -u root -p
# Execute the childDB.sql script to construct the database and tables
# Execute the populateDB.sql script to put some default info in the tables

# Build handlers JAR:
cd handlers/ && mvn clean install

# To execute server:
cd child_locator_ws/ && mvn clean compile install -DskipTests exec:java

# To execute a client:
cd child_locator_ws_cli/ && mvn clean compile install -DskipTests exec:java


# Info for the client:
# PIN = 12345
# Phonenumber = 910000000
# Email = david@hotmail.com
# Password = teste123
# If you wish to start over, just run childDB.sql script alone.
# You also have to delete pin and phoneNumber files in src/main/resources of child_locator_ws_cli/



# Instructions used to generate Certificates and keystores:

# Generate CA certificate
openssl req -new -x509 -keyout $CA_KEY_FILE -out $CA_PEM_FILE -days $KEYS_VALIDITY -passout pass:$CA_CERTIFICATE_PASS


# For each entity you want to create keystore and certificate just run every instruction bellow:
# Generate keypair
keytool -keystore $server_keystore_file -genkey -alias $server_name -keyalg RSA -keysize 2048 -keypass $KEY_PASS -validity $KEYS_VALIDITY -storepass $STORE_PASS
# Generate the Certificate Signing Request
keytool -keystore $server_keystore_file -certreq -alias $server_name -keyalg rsa -file $csr_file -storepass $STORE_PASS -keypass $KEY_PASS
# Generate signed certificate
openssl  x509  -req  -CA $CA_PEM_FILE -CAkey $CA_KEY_FILE -passin pass:$CA_CERTIFICATE_PASS -in $csr_file -out "$server_folder/$server_name.cer"  -days $KEYS_VALIDITY -CAcreateserial
# Import CA certificate to keystore
keytool -import -keystore $server_keystore_file -file $CA_PEM_FILE  -alias $CA_ALIAS -keypass $KEY_PASS -storepass $STORE_PASS -noprompt
# Import signed certificate to its keystore
keytool -import -keystore $server_keystore_file -file "$server_folder/$server_name.cer" -alias $server_name -storepass $STORE_PASS -keypass $KEY_PASS