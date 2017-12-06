/*LOGIN
both passwords in plaintext: teste123
*/
INSERT INTO `login` (`phone`,`email`,`salt`,`password`,`lastlogin`) VALUES (910000000,'david@hotmail.com','6ec7af7a91ac404e56ba5e25ca6902aa0ddecd6953ddb0b3c54db280d9dbbe74','3a18f59cc7a0b594e2fedac8ae233a4ad5b38ab714414670e8434ed89d54dd62', now());
INSERT INTO `login` (`phone`,`email`,`salt`,`password`,`lastlogin`) VALUES (910000001,'kevin@hotmail.com','54184ee693325f5fd25b6c221b679a02ef64d3d6048dfa07070f22876c65e828','3b07e617384f70a987bdc6116f001d83d74b9c3a2c66f6677bc781feb5a4d9e7', now());
INSERT INTO `login` (`phone`,`email`,`salt`,`password`,`lastlogin`) VALUES (910000002,'gui@hotmail.com','c002d9a53697c1cfcfe1453b5a8a79cb23ed4385573350cb32dbc972a524e98b','34321d2ff125ea0949d8da9ed1ae5b7eb66b1939e853820eddc97187605871aa', now());


/*CONNECTED*/
INSERT INTO `childdb`.`connected` (`followeePhone`, `followerPhone`, `nonce`) VALUES ('910000000','910000001','6D68A4E0BC24');

/*POSITION*/
INSERT INTO `childdb`.`position` (`phone`, `latitude`, `longitude`) VALUES ('910000000','39.8628316','-4.0273231');
