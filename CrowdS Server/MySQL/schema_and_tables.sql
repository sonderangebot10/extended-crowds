-- MySQL dump 10.13  Distrib 5.7.22, for Linux (x86_64)
--
-- Host: localhost    Database: cs
-- ------------------------------------------------------
-- Server version	5.7.22-0ubuntu0.16.04.1

SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT;
SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS;
SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION;
SET NAMES utf8;
SET @OLD_TIME_ZONE=@@TIME_ZONE;
SET TIME_ZONE='+00:00';
SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO';
SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0;

CREATE DATABASE cs CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE cs;

--
-- Table structure for table `human_task`
--

DROP TABLE IF EXISTS `human_task`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `human_task` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `task_type` varchar(45) COLLATE utf8mb4_unicode_ci NOT NULL,
  `task_giver` varchar(225) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(45) COLLATE utf8mb4_unicode_ci NOT NULL,
  `latitude` float(10,6) NOT NULL,
  `longitude` float(10,6) NOT NULL,
  `question` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `answer_choices` varchar(3000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `results` varchar(3000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `completed_time` timestamp NULL DEFAULT NULL,
  `distributed` int(11) DEFAULT NULL,
  `participants` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `answer` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT 'no answer',
  `members` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  `cost` int(11) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1121 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `quality`
--

DROP TABLE IF EXISTS `quality`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `quality` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `email` varchar(225) COLLATE utf8mb4_unicode_ci NOT NULL,
  `single` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT '0.5:',
  `multiple` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT '0.5:',
  `numeric` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT '0.5:',
  `ambient_temperature` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT '0.5:',
  `light` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT '0.5:',
  `accelerometer` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT '0.5:',
  `pressure` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT '0.5:',
  PRIMARY KEY (`id`),
  UNIQUE KEY `email_UNIQUE` (`email`),
  CONSTRAINT `fk_email` FOREIGN KEY (`email`) REFERENCES `user` (`email`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=78 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sensor`
--

DROP TABLE IF EXISTS `sensor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sensor` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `email` varchar(225) COLLATE utf8mb4_unicode_ci NOT NULL,
  `accelerometer` tinyint(1) NOT NULL DEFAULT '0',
  `ambient_temperature` tinyint(1) NOT NULL DEFAULT '0',
  `game_rotation_vector` tinyint(1) NOT NULL DEFAULT '0',
  `geomagnetic_rotation_vector` tinyint(1) NOT NULL DEFAULT '0',
  `gravity` tinyint(1) NOT NULL DEFAULT '0',
  `gyroscope` tinyint(1) NOT NULL DEFAULT '0',
  `gyroscope_uncalibrated` tinyint(1) NOT NULL DEFAULT '0',
  `light` tinyint(1) NOT NULL DEFAULT '0',
  `linear_acceleration` tinyint(1) NOT NULL DEFAULT '0',
  `magnetic_field` tinyint(1) NOT NULL DEFAULT '0',
  `magnetic_field_uncalibrated` tinyint(1) NOT NULL DEFAULT '0',
  `pressure` tinyint(1) NOT NULL DEFAULT '0',
  `proximity` tinyint(1) NOT NULL DEFAULT '0',
  `relative_humidity` tinyint(1) NOT NULL DEFAULT '0',
  `rotation_vector` tinyint(1) NOT NULL DEFAULT '0',
  `significant_motion` tinyint(1) NOT NULL DEFAULT '0',
  `step_counter` tinyint(1) NOT NULL DEFAULT '0',
  `step_detector` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `email_UNIQUE` (`email`),
  CONSTRAINT `fk_sensor_1` FOREIGN KEY (`email`) REFERENCES `user` (`email`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=123 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sensor_task`
--

DROP TABLE IF EXISTS `sensor_task`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sensor_task` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `task_giver` varchar(225) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(45) COLLATE utf8mb4_unicode_ci NOT NULL,
  `latitude` float(10,6) NOT NULL,
  `longitude` float(10,6) NOT NULL,
  `sensor` varchar(45) COLLATE utf8mb4_unicode_ci NOT NULL,
  `duration` varchar(45) COLLATE utf8mb4_unicode_ci NOT NULL,
  `readings` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sensor_data` varchar(225) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `result` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT 'no answer',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `completed_time` timestamp NULL DEFAULT NULL,
  `distributed` int(11) DEFAULT NULL,
  `participants` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `members` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  `cost` int(11) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=764 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user` (
  `email` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `admin` tinyint(1) NOT NULL DEFAULT '0',
  `username` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `os` varchar(45) COLLATE utf8mb4_unicode_ci NOT NULL,
  `model` varchar(45) COLLATE utf8mb4_unicode_ci NOT NULL,
  `latitude` float(10,6) DEFAULT NULL,
  `longitude` float(10,6) DEFAULT NULL,
  `firebase` varchar(152) COLLATE utf8mb4_unicode_ci NOT NULL,
  `login_time` timestamp NULL DEFAULT NULL,
  `logout_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `hits_created` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sensing_created` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `reputation` varchar(1000) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '0.5:',
  `sensor_points` int(11) NOT NULL DEFAULT '0',
  `hit_points` varchar(45) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '0',
  `heartbeat` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT ':',
  `active` tinyint(1) NOT NULL DEFAULT '0',
  `bid` int(11) DEFAULT '0',
  `pay` int(11) DEFAULT '0',
  PRIMARY KEY (`email`),
  UNIQUE KEY `email_UNIQUE` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2018-05-21 14:40:21
