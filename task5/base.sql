# base Exadel

DROP TABLE IF EXISTS `Students`;

CREATE TABLE `Students ` (
  `ID` tinyint unsigned NOT NULL AUTO_INCREMENT,
  `Student` char(40) NOT NULL,
   PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `Result`;

CREATE TABLE `Result` (
  `ID` tinyint  unsigned NOT NULL AUTO_INCREMENT,
  `StudentId` tinyint unsigned NOT NULL,
  `Task1` char(15) NOT NULL,
  `Task2` char(15) NOT NULL,
  `Task3` char(15) NOT NULL,
  `Task4` char(15) NOT NULL,
   PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

