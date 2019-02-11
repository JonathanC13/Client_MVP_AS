-- MySQL dump 10.13  Distrib 8.0.12, for Win64 (x86_64)
--
-- Host: localhost    Database: floor_collection
-- ------------------------------------------------------
-- Server version	8.0.12

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
 SET NAMES utf8 ;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `door_collection_0`
--

DROP TABLE IF EXISTS `door_collection_0`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `door_collection_0` (
  `iddr` int(11) NOT NULL AUTO_INCREMENT,
  `nm_door` varchar(45) NOT NULL,
  `iddoor` int(11) NOT NULL,
  `MarginLeft` decimal(5,2) NOT NULL,
  `MarginTop` decimal(5,2) NOT NULL,
  `MarginRight` decimal(5,2) NOT NULL,
  `MarginBot` decimal(5,2) NOT NULL,
  `IP_door` varchar(45) NOT NULL,
  `dev_port` int(11) NOT NULL,
  `dev_remName` varchar(45) NOT NULL,
  `dev_MAC` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`iddr`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `door_collection_0`
--

LOCK TABLES `door_collection_0` WRITE;
/*!40000 ALTER TABLE `door_collection_0` DISABLE KEYS */;
INSERT INTO `door_collection_0` VALUES (1,'firstdoor',0,50.00,425.00,0.00,0.00,'192.168.2.21',65000,'RDR5, C6:I0:R2','placeholder'),(2,'seconddoor',1,180.00,400.00,0.00,0.00,'192.168.2.21',65000,'RDR5, C6:I0:R2','PLACEholder'),(3,'thirddor',1,50.00,425.00,0.00,0.00,'192.168.2.21',65000,'RDR2, C6:I0:R2','placeholder');
/*!40000 ALTER TABLE `door_collection_0` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2019-02-10 19:55:34
