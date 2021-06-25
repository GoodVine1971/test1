Ссылка на docker hub: https://hub.docker.com/repository/docker/goodvine/test

task2/docker-compose.yaml использовался для п.2
index.html  для вывода <Username> Exadel Sandbox 2021



task2/5/docker-compose.yaml для п.5 
hello.java - для него сделан Dockerfile в этой же папке и, соответственно image
при deploy replicas : 5  ругается, что сервис на том же порту открыт... как победить?
==================================================================================================

Docker: базовый набор команд

Установка в Ubuntu
1.  + зависимости
$ sudo apt-get update

$ sudo apt-get install \
    apt-transport-https \
    ca-certificates \
    curl \
    gnupg-agent \
    software-properties-common
	
2. Add Docker’s official GPG key:

$ curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -	
	
Проверяем наличие ключа 9DC8 5822 9FC7 DD38 854A  E2D8 8D81 803C 0EBF CD88 по последним ...
 
sudo apt-key fingerprint 0EBFCD88	

3. Подключаем repo 

sudo add-apt-repository \
   "deb [arch=amd64] https://download.docker.com/linux/ubuntu \
   $(lsb_release -cs) \
   stable"
   
4. Устанавливаем Docker

sudo apt-get update
sudo apt-get install docker-ce docker-ce-cli containerd.io

Устанавливаем Docker-compose
sudo curl -L "https://github.com/docker/compose/releases/download/1.28.2/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
После этого мы настроим разрешения:

sudo chmod +x /usr/local/bin/docker-compose
проверки версии:

docker-compose --version

5. Проверяем
sudo docker run hello-world

Автоматически скачается hello-world" image from the Docker Hub
_________________________

Смотрим какие контейнеры:

docker ps -a  // в том числе exited

После регистрации в hub.docker

Login в репозиторий

docker login --username  goodvine

отправить в hub

docker tag goodvine/websrv:v1 goodvine/test:nginx_app
docker push goodvine/test:nginx_app

Поиск образа

docker search nginx

Pull (выгрузка из реестра) образа

docker pull nginx

запуск с пробросом порта
docker run --name Nginx -p 8080:80 -d nginx

запуск с входом в bash
sudo docker run -it --name Ubuntu ubuntu /bin/bash

sudo docker  stop Nginx  - присвоенное имя
sudo docker  start Nginx  - старт остановленного контейнера
docker attach Nginx - подключение
sudo docker  exec -it  c9f6a06f5690 bash  запускаем bash в контейнере

==========================

docker-compose

создаем папку , в ней docker-compose.yaml  :
version: '1.0'
services:
nginx-docker:
  image: nginx
  ports:
    - '8080:80'
 volumes:
    - ./:/usr/share/nginx/html/ 

и index.html/

!DOCTYPE html>
<html>
<head>
<title>Welcome to nginx!</title>
<style>
    body {
        width: 305em;
        margin: 0 auto;
padding-left: 20px;
        font-family: Tahoma, Verdana, Arial, sans-serif;
    }
</style>
</head>
<body>
<h1>GoodVine1971</h1>
<h2 style="color:red">Exadel Sandbox 2021</h><br>
</body>
</html>

docker-compose up

_________________________


Созаем  Dockerfile

FROM ubuntu:18.04
MAINTAINER Gurin Sergey <GoodVine1971@gmail.com>
ENV TZ=Europe/Minsk
ARG DEBIAN_FRONTEND=noninteractive
RUN apt-get update
RUN apt-get install dialog apt-utils -y
RUN apt-get install nginx php php-fpm -y
RUN echo "daemon off;" >> /etc/nginx/nginx.conf
RUN mkdir /run/php-fpm
COPY ./html/ /usr/share/nginx/html/
# ADD ./nginx.conf /etc/nginx/
CMD ["nginx", "-g", "daemon off;"]
CMD php-fpm
CMD nginx
EXPOSE 80
------------------
FROM ubuntu:18.04
MAINTAINER Gurin Sergey <GoodVine1971@gmail.com>
ENV TZ=Europe/Minsk
ARG DEBIAN_FRONTEND=noninteractive
RUN apt-get -y update
RUN apt-get -y install dialog apt-utils nmap apache2 nano
COPY ./html/  /var/www/html
EXPOSE 80
CMD apache2ctl -D FOREGROUND
---------------------------
FROM nginx:latest
MAINTAINER Gurin Sergey <GoodVine1971@gmail.com>
ENV TZ=Europe/Minsk
RUN apt-get -y update
RUN apt-get install nano mc -y
COPY ./html/ /usr/share/nginx/html/
EXPOSE 80

======================
FROM java:7
# ./myapp - директория на локальной машине с приложением
COPY ./myapp  /usr/src/myapp
WORKDIR /usr/src/myapp
RUN javac hello.java
CMD ["java", "hello"]

================================

sudo apt-get install apt-utils

docker build -t goodvine/websrv:v1 .

запуск

sudo docker run -it -d -p 8080:80 --name websrv goodvine/websrv:v1 bash
sudo docker run -it --name websrv  -p 8080:80  -it goodvine/websrv:v1 /bin/bash -c "service nginx start"

docker run -dit -p 80:80 cutom_image
++++++++++++++++++++


удаление  контейнеров

sudo docker rm f7bc71cbaeb1

docker rm $(docker ps -a -f status=exited -q)



запуск с переменной

docker run -d -p --name -e DEVOPS=" "