CREATE USER 'docaid'@'%' IDENTIFIED BY 'docaid';

GRANT ALL PRIVILEGES ON docaid.* TO 'docaid'@'%' IDENTIFIED BY 'docaid';

INSERT INTO `app_settings` (`name`,`value`) VALUES ('diag_populateFromLast','false');
INSERT INTO `app_settings` (`name`,`value`) VALUES ('diag_skipMedStage','false');
INSERT INTO `app_settings` (`name`,`value`) VALUES ('queueing_next','1');
INSERT INTO `app_settings` (`name`,`value`) VALUES ('services_default','diag');
INSERT INTO `app_settings` (`name`,`value`) VALUES ('services_id_diag_name','Khám bệnh');


#2016-04-13
ALTER TABLE `docaid`.`patient` ADD COLUMN `weight` FLOAT NULL AFTER `age`;


