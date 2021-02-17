#  Выполнение задания Task6 (Logging&Monitoring)  #  

##   ZABBIX   ##

###  1.1 Установить на сервер - сконфигурировать веб и базу ###

Для установки на хост переходим на страницу загрузки https://www.zabbix.com/download и выбераем репозиторий, соответствующий дистрибутиву Linux. 

Для Ubuntu20.04 получаем команды установки репозитория:

```sh
 wget https://repo.zabbix.com/zabbix/5.2/ubuntu/pool/main/z/zabbix-release/zabbix-release_5.2-1+ubuntu20.04_all.deb
 dpkg -i zabbix-release_5.2-1+ubuntu20.04_all.deb
 apt update
``` 

Установка Zabbix server, frontend, agent
Почему-то в зависимостях нет mysql сервера и zabbix-server-mysql не поднимает сервер , поэтому добавим в строку установки mariadb-server
```sh
apt install mariadb-server zabbix-server-mysql zabbix-frontend-php zabbix-nginx-conf zabbix-agent
````

Но сделаем установку в контейнере docker.

На https://www.zabbix.com/container_images можно выбрать нужный образ.

Добавим docker сеть для zabbix:
```sh
docker network create zabbix
docker network inspect zabbix 
```

Создаем в /opt рабочую диресторию zabbix, папку mysql для проброса базы и и docker-compose.yaml:

```sh
mkdir -pv zabbix/mysql
sudo nano docker-compose.yaml
```
		на хосте sudo apt  install nmap
		в контейнере mysql поставим apt-get install iputils-ping


Установка в docker сопровождалась сложностями с настройкой сети и портов, т.к. в образах внутренние порты изменены.
По-умолчанию 
Логин: Admin  (именно сбольшой буквы)
Пароль zabbix

Смотрим ip агента:
docker inspect zabbix-agent | grep "IPAddress\": "

172.26.0.5
вставляем вместо 127.0.0.1 в настойках агента дефолтного хоста Zabbix server

![Результат выполнения:](show_base.jpg)  

Установим агента на виртуальную машину при помощи ansible

ansible-agent.yaml
Выполняем 
 ansible-playbook zabbix-agent.yaml --ask-become-pass






Установим агента для mysql57 из task5 он находится на том же хосте, что и zabbix? но в сети bridge:

В сети bridge не работает link

docker network create mysql_repl

docker network disconnect bridge mysql57
docker network disconnect bridge mysql_slave
docker network connect mysql_repl mysql57
docker network connect mysql_repl mysql_slave

Создаем контейнер с агентом для mysql57

docker run --name zabbix-agent2 -v /opt/zabbix/agent:/etc/zabbix/zabbix_agentd.d --network mysql_repl  --link mysql57:mysql57 -e ZBX_HOSTNAME="mysql57" -e ZBX_SERVER_HOST="192.168.0.1" -d zabbix/zabbix-agent

создаем файл template_db_mysql.conf в /opt/zabbix/agent:

UserParameter=mysql.ping[*], mysqladmin -h"$1" -P"$2" ping
UserParameter=mysql.get_status_variables[*], mysql -h"$1" -P"$2" -sNX -e "show global status"
UserParameter=mysql.version[*], mysqladmin -s -h"$1" -P"$2" version
UserParameter=mysql.db.discovery[*], mysql -h"$1" -P"$2" -sN -e "show databases"
UserParameter=mysql.dbsize[*], mysql -h"$1" -P"$2" -sN -e "SELECT COALESCE(SUM(DATA_LENGTH + INDEX_LENGTH),0) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='$3'"
UserParameter=mysql.replication.discovery[*], mysql -h"$1" -P"$2" -sNX -e "show slave status"
UserParameter=mysql.slave_status[*], mysql -h"$1" -P"$2" -sNX -e "show slave status"

В  /opt/zabbix/conf создаем:
# cat /var/lib/zabbix/.my.cnf
[client]
user = zabbix
password = zabbix

В контейнере mysql добавляем нового пользователя:

mysql -uroot -p
> CREATE USER 'zabbix'@'%' IDENTIFIED BY 'zabbix';
> GRANT USAGE,REPLICATION CLIENT,PROCESS,SHOW DATABASES,SHOW VIEW ON *.* TO 'zabbix'@'%';


перезапускае контейнер zabbix-agent2

В контейнере mysql 



docker run --name zabbix-agent2- -v /opt/zabbix/conf:/etc/zabbix/zabbix_agentd.d --link mysql-server:mysql-server --link zabbix-server:zabbix-server -e ZBX_HOSTNAME="Zabbix server" -e ZBX_SERVER_HOST="zabbix-server" -d zabbix/zabbix-agent


docker run --name some-zabbix-agent -e ZBX_HOSTNAME="some-hostname" -e ZBX_SERVER_HOST="some-zabbix-server" -d zabbix/zabbix-agent:latest
Получаем:
docker run --name zabbix-agent -e ZBX_HOSTNAME="ZabbixServ" -e ZBX_SERVER_HOST="192.168.0.1" -d zabbix/zabbix-agent:latest



docker run --name zabbix-server --link zabbix-agent:zabbix-agent2 -d zabbix/zabbix-server:latest


docker run --name zabbix-server --link zabbix-agent:zabbix-agent -d zabbix/zabbix-server:latest


![Результат выполнения:](show_base.jpg)  

