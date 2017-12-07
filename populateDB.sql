/*LOGIN
both passwords in plaintext: teste123
*/
INSERT INTO `login` (`phone`,`email`,`salt`,`password`,`lastlogin`, `verified`) VALUES (910000000,'david@hotmail.com','b5194318f10c96d4e497c10eccebc5ce0ebd71a764b1ba0a5ca4ecd3f7ac2d46','b0588d7ebfd2f01024545e79263d5d08863ff1a83bed9721e96dbf1ab28b6afc', now(), 1);
INSERT INTO `login` (`phone`,`email`,`salt`,`password`,`lastlogin`, `verified`) VALUES (910000001,'kevin@hotmail.com','641fca67c9421a02de43bcbc36970111c20aaf026bea84452a77f577d8b2116c','eb50fdd6ff92270dabd7de8b8af2cf18c0871514ce996946f34d6a35a75de343', now(), 1);
INSERT INTO `login` (`phone`,`email`,`salt`,`password`,`lastlogin`, `verified`) VALUES (910000002,'gui@hotmail.com','c91fb4f537929070e00e4bcd718000b9c4e7332d47a943acf4a51a4c85dac676','fb95a1777f61297ac0ea33cdf9c2397e8afadc0abde776a5630ddbd715771e1e', now(), 1);


/*CONNECTED*/
INSERT INTO `childdb`.`connected` (`followeePhone`, `followerPhone`, `nonce`) VALUES ('910000000','910000001','6D68A4E0BC24');

/*POSITION*/
INSERT INTO `childdb`.`position` (`phone`, `latitude`, `longitude`) VALUES ('910000000','39.8628316','-4.0273231');
