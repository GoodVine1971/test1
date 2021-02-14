# base Exadel
CREATE DATABASE IF NOT EXISTS `exadel`;
USE `exadel`;

CREATE TABLE IF NOT EXISTS `Students` (
  `ID` tinyint unsigned NOT NULL AUTO_INCREMENT,
  `Student` char(40) NOT NULL,
   PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
INSERT IGNORE INTO `Students` (`ID`, `Student`) VALUES
	(1, 'Ражабов Дамир Шарофович'),
	(2, 'Сивожелезов Сергей Владимирович'),
	(3, 'Чеснюк Дмитрий Александрович'),
	(4, 'Мацкевич Андрей Анатольевич'),
	(5, 'Шеронов Иван Петрович'),
	(6, 'Гавриш Олег Николаевич'),
	(7, 'Демьянков Владислав Витальевич'),
	(8, 'Пехоцкий Юрий Васильевич'),
	(9, 'Шерстюк Вадим Александрович'),
	(10, 'Казимиров Сергей Анатольевич'),
	(11, 'Назаров Павел Андреевич'),
	(12, 'Шатров Игорь Олегович'),
	(13, 'Никольский Валерий Александрович'),
	(14, 'Гурин Сергей Владимирович'),
	(16, 'Кузин Дмитрий Алексеевич'),
	(17, 'Наливайко Алексей Викторович'),
	(18, 'Чех Александр Борисович'),
	(19, 'Есьман Михаил Михайлович');
	


CREATE TABLE IF NOT EXISTS `Result` (
  `ID` tinyint  unsigned NOT NULL AUTO_INCREMENT,
  `StudentId` tinyint unsigned NOT NULL,
  `Task1` char(15) NOT NULL,
  `Task2` char(15) NOT NULL,
  `Task3` char(15) NOT NULL,
  `Task4` char(15) NOT NULL,
   PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


INSERT IGNORE INTO `Result` (`ID`, `StudentId`, `Task1`, `Task2`, `Task3`, `Task4`) VALUES
	(1, 1, 'pass', 'pass', 'pass', 'pass'),
	(2, 2, 'pass', 'pass', 'pass', ' '),
	(3, 3, 'pass', 'pass', 'pass', 'pass'),
	(4, 4, 'pass', 'pass', 'not passed', ' '),
	(5, 5, 'pass', 'pass', 'not passed', ' '),
	(6, 6, 'pass', 'pass', 'not passed', ' '),
	(7, 7, 'pass', 'not passed', 'not passed', ' '),
	(8, 8, 'pass', 'pass', 'pass', 'pass'),
	(9, 9, 'pass', 'not passed', 'not passed', ' '),
	(10, 10, 'pass', 'pass', 'pass', 'pass'),
	(11, 11, 'pass', 'pass', 'pass', 'pass'),
	(12, 12, 'pass', 'pass', 'pass', 'pass'),
	(13, 13, 'pass', 'not passed', 'not passed', ' '),
	(14, 14, 'pass', 'pass', 'pass', 'pass'),
	(16, 16, 'pass', 'not passed', 'not passed', ' '),
	(17, 17, 'pass', 'pass', 'pass', ' '),
	(18, 18, 'pass', 'pass', 'pass', 'pass'),
	(19, 19, 'pass', 'pass', 'pass', 'pass');
