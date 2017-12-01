-- MySQL Script generated by MySQL Workbench
-- Thu Nov 30 13:29:40 2017
-- Model: New Model    Version: 1.0
-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

-- -----------------------------------------------------
-- Schema mydb
-- -----------------------------------------------------
-- -----------------------------------------------------
-- Schema childdb
-- -----------------------------------------------------
DROP SCHEMA IF EXISTS `childdb` ;

-- -----------------------------------------------------
-- Schema childdb
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `childdb` DEFAULT CHARACTER SET utf8 ;
USE `childdb` ;

-- -----------------------------------------------------
-- Table `childdb`.`connected`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `childdb`.`connected` (
  `connectid` INT(11) NOT NULL AUTO_INCREMENT,
  `phone` INT(11) NOT NULL,
  `phone2` INT(11) NOT NULL,
  `connected` TINYINT(1) NULL DEFAULT '0',
  `nonce` INT(11) NULL DEFAULT NULL,
  `timestamp` TIMESTAMP NULL DEFAULT NULL,
  PRIMARY KEY (`connectid`),
  UNIQUE INDEX `connectid_UNIQUE` (`connectid` ASC),
  UNIQUE INDEX `nonce_UNIQUE` (`nonce` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `childdb`.`login`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `childdb`.`login` (
  `phone` INT(16) NOT NULL,
  `email` VARCHAR(254) NOT NULL,
  `salt` VARCHAR(64) NOT NULL,
  `password` VARCHAR(64) NOT NULL,
  `attempts` TINYINT(16) NULL DEFAULT '0',
  `verified` TINYINT(1) NULL DEFAULT '0',
  PRIMARY KEY (`phone`),
  UNIQUE INDEX `username_UNIQUE` (`phone` ASC),
  UNIQUE INDEX `salt_UNIQUE` (`salt` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
