-- MySQL Script generated by MySQL Workbench
-- Wed Dec  6 14:37:29 2017
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
  `connectid` INT(10) NOT NULL AUTO_INCREMENT,
  `followeePhone` INT(16) NOT NULL,
  `followerPhone` INT(16) NOT NULL,
  `connected` TINYINT(1) NULL DEFAULT '0',
  `nonce` VARCHAR(16) NULL DEFAULT NULL,
  `timestamp` TIMESTAMP NULL DEFAULT now(),
  PRIMARY KEY (`connectid`),
  UNIQUE INDEX `connectid_UNIQUE` (`connectid` ASC),
  UNIQUE INDEX `nonce_UNIQUE` (`nonce` ASC))
ENGINE = InnoDB
AUTO_INCREMENT = 2
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
  `lastlogin` TIMESTAMP NULL DEFAULT now(),
  `loginregistercode` VARCHAR (15) DEFAULT NULL, 
  PRIMARY KEY (`phone`),
  UNIQUE INDEX `username_UNIQUE` (`phone` ASC),
  UNIQUE INDEX `salt_UNIQUE` (`salt` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `childdb`.`position`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `childdb`.`position` (
  `phone` INT(16) NOT NULL,
  `latitude` DOUBLE NULL DEFAULT 0,
  `longitude` DOUBLE NULL DEFAULT 0,
  `timestamp` TIMESTAMP NULL DEFAULT now(),
  PRIMARY KEY (`phone`))
ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
