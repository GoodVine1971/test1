#  Выполнение задания Task5 (Databases)  #  

##   1   ##

Модели баз данных:
- иерархическая (один ко многим);
- сетевая (многие ко многим);
- реляционная (задаются отношения).
- объектно-ориентированные
- документоориентированные

Реляционные базы данных используют структурированный язык запросов (Structured Query Language, SQL) и имеют форму таблиц.
В нереляционной NoSQL БД данные представляются в виде документов, пар «ключ-значение», графов или хранилищ wide-column.
Здесь можно создавать документы, не задавая их структуру заранее; каждый документ может обладать собственной структурой; у каждой базы данных может быть собственный синтаксис; можно добавлять поля прямо во время работы с данными.
Примерами SQL БД являются ySQL, Oracle, PostgreSQL и Microsoft SQL Server, а NoSQL БД - MongoDB, BigTable, Redis, RavenDB Cassandra, HBase, Neo4j и CouchDB

Кратко и доступно здесь: https://tproger.ru/translations/sql-vs-nosql/


##   2 Развернуть в контейнере базу данных MySQL  ##

Подготавливаем папку для создания контейнера и хранения базы.
 
```sh
sudo mkdir /opt/mysql
sudo cd /opt/mysql
sudo mkdir mysql_db
sudo nano docker-compose.yaml
``` 
Создаем docker-compose файл:

	version: '3.5'
	services:
	  db:
	#	build: 
	#	  context: .
	#	  dockerfile: Dockerfile
	    image: mysql:5.7
		container_name: "mysql_5.7"
	    restart: always
		environment:
		  MYSQL_ROOT_PASSWORD: rootpass
		  MYSQL_DATABASE: exadel
		  MYSQL_USER: stud
		  MYSQL_PASSWORD: studpass
		ports:
		  - "3306:3306"
		volumes:
		  - /opt/mysql/mysql_db:/var/lib/mysql
		  - /opt/mysql/files:/var/lib/mysql-files
	#	  - /opt/mysql/conf:/etc/mysql/mysql.conf.d
	  

Как оказалось MySQL server is running with the --secure-file-priv option  и файлы можно класть только сюда:
 /var/lib/mysql-files  ,  проверяется командой SHOW VARIABLES LIKE "secure_file_priv";
 Можно, конечно, изменить  секцию [mysqld] и добавить строку:  ```secure-file-priv = "" ```,
 но оставим по-умолчанию, а на будущее оставил закоментированной строку с конфигом mysql. 
	Прим.: если на хосте (не в докере) работает mysql на 3306, то будет ошибка, так как порт 3306 уже занят, тогда нужно выбрать другой порт 3307:3306.
	
В подпапке /files  лежат stud.csv, res.csv, base.sql  для импорта базы данных из csv и готовый mysql-скрипт.
 
Запускаем docker-compose и создаем контейнер:	 

```sh
docker-compose up -d
```
Заходим внутрь контейнера и запускаем консоль mysql с кредами, указанными в environment:
```sh
docker exec -it mysql_5.7  bash
mysql -uroot -prootpass
```
> SHOW DATABASES;  
 
![Результат выполнения:](show_base.jpg)  

Здесь видна уже созданная база exadel

##   3 Заполнить базу данных.  ##

Как указано выше для заполнения базы данных созданы 3 файла stud.csv, res.csv, base.sql

1. Создание базы из консоли и импортирование данных из csv.

	Подключаемся к базе exadel с пользователем stud:
	```sh
	mysql -u stud -p exadel
	```
	> use exadel 
	
	Создаем структуру базы:
	
	CREATE TABLE `Students` (
	  `ID` tinyint unsigned NOT NULL AUTO_INCREMENT,
	  `Student` char(40) NOT NULL,
	   PRIMARY KEY (`ID`)
	) ENGINE=InnoDB DEFAULT CHARSET=utf8;

	CREATE TABLE `Result` (
	  `ID` tinyint  unsigned NOT NULL AUTO_INCREMENT,
	  `StudentId` tinyint unsigned NOT NULL,
	  `Task1` char(15) NOT NULL,
	  `Task2` char(15) NOT NULL,
	  `Task3` char(15) NOT NULL,
	  `Task4` char(15) NOT NULL,
	   PRIMARY KEY (`ID`)
	) ENGINE=InnoDB DEFAULT CHARSET=utf8;
	

	Импортируем из csv файлов  (должны быть в utf8)
	
	LOAD DATA INFILE '/var/lib/mysql-files/stud.csv'
	INTO TABLE Students
	FIELDS TERMINATED BY ';'
	ENCLOSED BY '"'
	LINES TERMINATED BY '\n'
	IGNORE 1 ROWS;	

	LOAD DATA INFILE '/var/lib/mysql-files/res.csv'
	INTO TABLE Result
	FIELDS TERMINATED BY ';'
	ENCLOSED BY '"'
	LINES TERMINATED BY '\n'
	IGNORE 1 ROWS;

	![Результат выполнения:](insert_from_csv.jpg)  

	Подключаемся с хоста:
	Прокинул порт с виртуалки на стационарный компьютер, подключился к база на Доккере через HeidiSQL:
	
	Таблица Students:
	
	![Просмотр таблицы Students:](students.jpg)  

	Таблица Result:
	
	![Просмотр таблицы Result:](result.jpg)  
	

2.    3*  SQL скрипт.  

	Удаляем таблицы из базы 
	> DROP TABLE Students;
	> DROP TABLE Result;
	
	Импорт из sql файла bash:
	```sh
	mysql -uroot -p exadel < /var/lib/mysql-files/base.sql
	```
	![Выполнение :](insert_from_sql.jpg)  
	
	
##  4  Написать запрос который по вашей фамилии будет находить информацию   ##

В консоли mysql не вводятся русские буквы

Посмотрел character_set, заменил на utf8. 

> SHOW VARIABLES LIKE 'char%';

```sh
cd /etc/mysql/mysql.conf.d
nano mysqld.cnf
```

	[client]
	default-character-set = utf8
	[mysqld]
	character-set-server=utf8
	collation-server=utf8_general_ci
	init-connect="SET NAMES utf8"
	skip-character-set-client-handshake
	[mysql]
	default-character-set = utf8
	[mysqldump]
	default-character-set = utf8

Выводить русские символы стало правильно, но ввод не появился.

Поэтому выполнил в менеджере MySQL

SELECT Students.Student, Result.Task1, Result.Task2, Result.Task3, Result.Task4  FROM Students  
INNER JOIN Result ON Students.ID = Result.StudentId 
WHERE Students.Student LIKE '%Гурин%' ;

![Результат выполнения:](select_stud.jpg)  


##  5  Настроить репликацию SQL базы данных (Master->Slave)  ##


```sh
sudo mkdir /opt/mysql-slave
```
Поднимаем второй контейнер через  
 в папке /opt/mysql-slave , изменив строки

container_name: "mysql_slave"
ports:
      - "3307:3306"
 volumes:
	  - /opt/mysql-slave/mysql_db:/var/lib/mysql
	  
По умолчанию, если не указана сеть в docker файле - контейнеры изолированы.

Поэтому добавляем строку:  network_mode: "bridge"
	  
	На главном сервере отредактируем файл файл my.cnf, в секцию [mysqld] добавить строки:
	# выбираем ID сервера, произвольное число, лучше начинать с 1
	server-id = 1

	# путь к бинарному логу
	log_bin = /var/log/mysql/mysql-bin.log

	# название реплицируемой базы данных
	binlog_do_db = exadel

перезапускаем: service mysql restart

Создаем пользователя replicator:

> GRANT REPLICATION SLAVE ON *.* TO 'replicator'@'%' IDENTIFIED BY 'pass';
> FLUSH PRIVILEGES;

Проверяем статус Мастер-сервера:

> SHOW MASTER STATUS;

Ок, работает

На Slave сервере отредактируем файл файл my.cnf, в секцию [mysqld] добавить строки:

# ID Слейва, удобно выбирать следующим числом после Мастера
server-id = 2

# Путь к relay логу
relay-log = /var/log/mysql/mysql-relay-bin.log

# Путь к bin логу на Мастере
log_bin = /var/log/mysql/mysql-bin.log

# База данных для репликации
binlog_do_db = exadel

После перезагрузки в консоли mysql выполняем:

> CHANGE MASTER TO MASTER_HOST='mysql_5.7', MASTER_PORT=3306, MASTER_USER='replicator', MASTER_PASSWORD='pass';  
> SHOW SLAVE STATUS\G;

![Статус репликации:](replicat.jpg)  

Возможны ошибки соединенния: проверяем удаленное соединение mysql от slave к master
Можно посмотреть log подключения в docker:

```sh
docker logs -f mysql_slave
```
Или ошибка репликации из-зп одинакового  UUID, тогда удаляем auto.cnf

mysql> STOP SLAVE;  

```sh
service mysql stop
mv /var/lib/mysql/auto.cnf /var/lib/mysql/auto.cnf.bak
service mysql start
```
mysql> START SLAVE;

В Mysql менеджере  на мастере добавляем нового студента, смотрим slave - там тоже добавился.

![Результат репликации:](replica-mysql.jpg)  


##  6,  7 добавить переменную USERNAME, выполнить поиск  ##

Для переменной USERNAME добавляем в environment строку:
     
>	USERNAME: 14

Использовал ID , т.к. в консоли mysql не передается русский

Выполняем из bash, т.к. переменной в консоли mysql нет:

```sh
mysql -uroot -p${MYSQL_ROOT_PASSWORD}  -e  "use exadel; SELECT Student FROM Students WHERE ID=${USERNAME};"
```
Или так, если строка длинная.

```sh
mysql -uroot -p${MYSQL_ROOT_PASSWORD} <<EOF
use exadel
SELECT Student FROM Students WHERE ID=${USERNAME}
EOF
```
Добавляем вывод результатов тестов:

```sh
mysql -uroot -p${MYSQL_ROOT_PASSWORD}  -e  "use exadel;
SELECT Students.Student, Result.Task1, Result.Task2, Result.Task3, Result.Task4  FROM Students  
INNER JOIN Result ON Students.ID = Result.StudentId 
WHERE Students.ID = ${USERNAME} ;"
```
![Результат выполнения:](username.jpg) 

##  8  NoSQL (Mongodb)   ##

Ищем в репозитории:
```sh
docker search mongodb
```
Создаем в /opt директории и docker-compose.yaml:
```sh
mkdir -pv mongodb/database
sudo nano docker-compose.yaml
```
	version: '3.5'
	services:
	  mongodb:
		image: mongo
		container_name: mongodb
		restart: unless-stopped
		environment:
		  MONGO_INITDB_ROOT_USERNAME: root
		  MONGO_INITDB_ROOT_PASSWORD: pass
		ports:
		  - 27017:27017
		network_mode: "bridge"
		volumes:
		  - /opt/mongodb/database:/data/db
	
Запускаем контейнер и заходим в него:
```sh 	
docker-compose up -d
docker exec -it mongodb bash
```

Входим в консоль: mongo

Авторизация:
> db.auth('root', 'pass');  

Создаем базу:
> use exadel  

Создаем коллекцию:
>db.createCollection("Students")  


Для репликации создадим сеть mongo
```sh 	
docker network create mongo
```
Поднимаем 3 контейнера (см mongodb/docker-compose.yaml)

Смотрим ip:
```sh 	
docker network inspect mongo
```
Прописываем соответственно именам контейнеров в hosts для обращения по имени контейнера:

	172.24.0.2      mongo1
	172.24.0.3      mongo2
	172.24.0.4      mongo3

Заходим в первый контейнер:
```sh 
docker exec -it mongo1 bash
mongo
```
Инициируем репликасет:

> rs.initiate()
> db.isMaster()

Видим называется:  rs0:PRIMARY

Добавляем в репликасет mongo2
> rs.add("mongo2:27017")

Mongo3 будет арбитром
> rs.add("mongo3:27017", {arbiterOnly: true}) 

Проверка статуса:
> rs.status()

Добавим обять базу и коллекцию:

> use exadel
> db.createCollection("Students")  


Подключаемся к базам. Репликация работает:

![Результат репликации:](mongo-repl.jpg) 


Для переменной добавим 

Для переменной USERNAME добавляем в в docker-compose.yaml строку в environment :
     
>	USERNAME: Гурин  

Проверяем в bash

```sh
echo $USERNAME - выводим в bash
```