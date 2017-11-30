/*
-- Query: select * from login
LIMIT 0, 1000

-- Date: 2017-11-30 13:22
*/
/*both passwords in plaintext: teste    password=passwordPlainText+Salt
David's password=hash256(testeQxLUF1bgIAdeQX)
Kevin's password=hash256(testebv5PehSMfV11Cd)
*/
INSERT INTO `login` (`phone`,`email`,`salt`,`password`,`attempts`,`verified`) VALUES (910000000,'david@hotmail.com','QxLUF1bgIAdeQX','2d09b8f11b247216a00c69c446d241f1fadcb42994c6e21d2d7999b196f7c4d8',0,1);
INSERT INTO `login` (`phone`,`email`,`salt`,`password`,`attempts`,`verified`) VALUES (910000001,'kevin@hotmail.com','bv5PehSMfV11Cd','1312ed1fdee7fd80b41048509147c5717fd9c6a0358d89841a6c1409e77d192d',0,1);
