CREATE DATABASE `docaid` /*!40100 DEFAULT CHARACTER SET utf8 */;
CREATE TABLE `app_settings` (
  `name` varchar(256) NOT NULL,
  `value` varchar(1024) DEFAULT NULL,
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `autotext` (
  `field_id` varchar(32) NOT NULL,
  `content` varchar(512) NOT NULL,
  `keywords` varchar(1024) DEFAULT NULL,
  PRIMARY KEY (`field_id`,`content`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `diagnosis` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `patient_id` int(11) DEFAULT NULL,
  `diag_date` datetime DEFAULT NULL,
  `age` varchar(45) DEFAULT NULL,
  `extras` varchar(2048) DEFAULT NULL,
  `symptoms` varchar(2048) DEFAULT NULL,
  `diag_brief` varchar(2048) DEFAULT NULL,
  `treatments` varchar(2048) DEFAULT NULL,
  `treatment_days` int(11) DEFAULT NULL,
  `revisit_days` int(11) DEFAULT NULL,
  `norevisit_ifgood` tinyint(4) DEFAULT NULL,
  `cost` float DEFAULT NULL,
  `inserted_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `prescription` text,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `medicine` (
  `name` varchar(256) NOT NULL,
  `unit` varchar(45) NOT NULL,
  `keywords` varchar(256) DEFAULT NULL,
  `package_unit` varchar(45) DEFAULT NULL,
  `package_size` float DEFAULT NULL,
  `package_breakable` tinyint(4) DEFAULT NULL,
  `unit_price` int(11) DEFAULT NULL,
  `package_price` int(11) DEFAULT NULL,
  PRIMARY KEY (`name`,`unit`),
  FULLTEXT KEY `index2` (`name`,`keywords`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `patient` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(256) NOT NULL,
  `address` varchar(256) DEFAULT NULL,
  `dob` date DEFAULT NULL,
  `phone` varchar(45) DEFAULT NULL,
  `extras` varchar(2048) DEFAULT NULL,
  `health_note` varchar(2048) DEFAULT NULL,
  `family` varchar(2048) DEFAULT NULL,
  `last_visit` datetime DEFAULT NULL,
  `last_appointment` date DEFAULT NULL,
  `inserted_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `old_id` int(11) DEFAULT NULL,
  `age` varchar(32) DEFAULT NULL,
  `last_symptoms` varchar(2048) DEFAULT NULL,
  `last_diagbrief` varchar(2048) DEFAULT NULL,
  `last_treatments` varchar(2048) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `name_ordering` (`name`),
  KEY `lastvisit` (`last_visit`),
  KEY `lastupdate` (`updated_time`),
  FULLTEXT KEY `name_ft` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `pres_items` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `diag_id` int(11) NOT NULL,
  `med_name` varchar(256) NOT NULL,
  `unit` varchar(45) DEFAULT NULL,
  `unit_package` varchar(45) DEFAULT NULL,
  `volumn` float DEFAULT NULL,
  `volumn_package` float DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `service` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `diag_id` int(11) NOT NULL,
  `name` varchar(45) NOT NULL,
  `price` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `waiting_queue` (
  `patient_id` int(11) NOT NULL,
  `ord_number` int(11) DEFAULT NULL,
  `queue_stage` varchar(45) DEFAULT NULL,
  `name` varchar(256) DEFAULT NULL,
  `dob` date DEFAULT NULL,
  `age` varchar(45) DEFAULT NULL,
  `address` varchar(256) DEFAULT NULL,
  `phone` varchar(45) DEFAULT NULL,
  `extras` varchar(2048) DEFAULT NULL,
  `health_note` varchar(2048) DEFAULT NULL,
  `family` varchar(2048) DEFAULT NULL,
  `last_visit` datetime DEFAULT NULL,
  `last_appointment` date DEFAULT NULL,
  `last_symptoms` varchar(2048) DEFAULT NULL,
  `last_diagbrief` varchar(2048) DEFAULT NULL,
  `last_treatments` varchar(2048) DEFAULT NULL,
  `diag_id` int(11) DEFAULT NULL,
  `diag_date` datetime DEFAULT NULL,
  `revisit_days` int(11) DEFAULT NULL,
  `norevisit_ifgood` tinyint(4) DEFAULT NULL,
  `treatment_days` int(11) DEFAULT NULL,
  `symptoms` varchar(2048) DEFAULT NULL,
  `diag_brief` varchar(2048) DEFAULT NULL,
  `treatments` varchar(2048) DEFAULT NULL,
  `prescription` text,
  `inserted_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime DEFAULT NULL,
  PRIMARY KEY (`patient_id`),
  KEY `queue_order` (`ord_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
