-- phpMyAdmin SQL Dump
-- version 4.8.3
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1:3306
-- Generation Time: Nov 23, 2018 at 03:11 AM
-- Server version: 5.7.23
-- PHP Version: 7.2.10

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET AUTOCOMMIT = 0;
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `floor_collection`
--

-- --------------------------------------------------------

--
-- Table structure for table `door_collection_0`
--

DROP TABLE IF EXISTS `door_collection_0`;
CREATE TABLE IF NOT EXISTS `door_collection_0` (
  `iddr` int(11) NOT NULL AUTO_INCREMENT,
  `nm_door` varchar(45) NOT NULL,
  `iddoor` int(11) NOT NULL,
  `MarginLeft` decimal(5,2) NOT NULL,
  `MarginTop` decimal(5,2) NOT NULL,
  `MarginRight` decimal(5,2) NOT NULL,
  `MarginBot` decimal(5,2) NOT NULL,
  `IP_door` varchar(45) NOT NULL,
  PRIMARY KEY (`iddr`)
) ENGINE=MyISAM AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `door_collection_0`
--

INSERT INTO `door_collection_0` (`iddr`, `nm_door`, `iddoor`, `MarginLeft`, `MarginTop`, `MarginRight`, `MarginBot`, `IP_door`) VALUES
(1, 'firstdoor', 0, '50.00', '425.00', '0.00', '0.00', '192.0'),
(2, 'seconddoor', 1, '180.00', '400.00', '0.00', '0.00', '192.1'),
(3, 'thrid', 1, '50.00', '425.00', '0.00', '0.00', '192.2');

-- --------------------------------------------------------

--
-- Table structure for table `floor_collection_0`
--

DROP TABLE IF EXISTS `floor_collection_0`;
CREATE TABLE IF NOT EXISTS `floor_collection_0` (
  `idfloor` int(11) NOT NULL AUTO_INCREMENT,
  `nm_floor` varchar(45) NOT NULL,
  `or_floor` int(3) NOT NULL,
  `img_path` varchar(255) NOT NULL,
  `iddoor` int(3) NOT NULL,
  PRIMARY KEY (`idfloor`),
  UNIQUE KEY `idfloor_UNIQUE` (`idfloor`)
) ENGINE=MyISAM AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `floor_collection_0`
--

INSERT INTO `floor_collection_0` (`idfloor`, `nm_floor`, `or_floor`, `img_path`, `iddoor`) VALUES
(1, '1_main_flr', 1, 'Floor_replace.jpg', 0),
(2, '0_flr_rep', 0, 'Floor_replace.jpg', 1),
(3, '0_flr_main', 0, 'main_floor.jpg', 2);

-- --------------------------------------------------------

--
-- Table structure for table `img_collection`
--

DROP TABLE IF EXISTS `img_collection`;
CREATE TABLE IF NOT EXISTS `img_collection` (
  `idimg` int(11) NOT NULL AUTO_INCREMENT,
  `nm_img` varchar(45) NOT NULL,
  `path_img` varchar(45) NOT NULL,
  PRIMARY KEY (`idimg`)
) ENGINE=MyISAM AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;

--
-- Dumping data for table `img_collection`
--

INSERT INTO `img_collection` (`idimg`, `nm_img`, `path_img`) VALUES
(1, 'closed_door', 'closed_door.png'),
(2, 'open_door', 'open_door.png');
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
