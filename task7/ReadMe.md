# Выполнение задания Task7 (Cloud)

![Блок-схема:](./images/task7.jpg) 


Что сделано:

1. Регистрация в Azure
2. Ресурсы:
   - Виртуальная машина
   - Учетная запись Azure Cosmos DB (для Mongodb)
   - Реестр контейнеров
   - Экземпляры контейнеров
3. Установлен Docker на ВМ
4. Установлен Jenkins  (с помощью Ansible)
5. Настроен pipeline Frontend - NodeJS (build, deploy в Azure Container Registry)
6. Настроен pipeline Backend - .Net (build, deploy в Azure Container Registry)
7. Настроен Frestyle, зависящий от Frontend и Backend, для запуска reverse proxy с latest контейнерами
8. Архивация и восстановление базы данных средствами Azure Cosmos DB
9. Опрос Git раз в 3 часа. Сначала было сделано по WebHook, но из-за частого commit изменено 
10. Логировани, мониторинг Grafana + Prometeus. Используем проект https://github.com/stefanprodan/dockprom

Что не удалось:

1. Из-за кеширования поздно выяснилось, что при смене адресов на свои, продолжала использоваться база разработчиков.
Требование обязательного ssl для коннекта с базой данных в azure выполнить не удалось, т.к. требует дополнительно настройки со стороны разработчиков приложения .Net
Пришлось запустить Mongodb в контейнере.
2. Сделать безошибочную проверку кода  (SonaQube + Jenkins). Сделано локально.


## Локальное тестирование

### FRONTEND NodeJS

Ставим docker и git: 
В папке frontend добавлены Dockerfile, .dockerignore, nginx.conf:

```sh
git clone https://github.com/umilanovich/exadelBonus
```
Тестовый pipeline:

	pipeline { 
		agent any

	stages {
		stage('Display') {
			steps {
				git branch: 'develop', url: 'https://github.com/umilanovich/exadelBonus'
				sh 'cp -f /home/ansclient/frontend/Dockerfile . '
				sh 'cp -f /home/ansclient/frontend/.dockerignore . '
				sh 'cp -f /home/ansclient/frontend/nginx.conf . '
				//sh 'mkdir frontend'
				//sh 'docker stop front'
				//sh 'docker rm front'
				sh 'docker build -t frontend .'
			}
		}
	}


после создания образа frontend поднимаем контейнеры
```sh
docker build -t frontend .  
docker run --name front -d -p 80:80 frontend
```
При запуске pipeline (и dockerfile) появляется много подвешенных контейнеров. Работаем с ними:  

>  
Остановить все контейнеры  
> docker stop $(docker ps -a -q)  
Удаление подвешенных образов  
> docker rmi $(docker images -f dangling=true -q)  
очистить кэш docker (удалит все)
> docker system prune -a   


### Backend   .Net 

Настраиваем Dockerfile и запускаем:
```sh
docker build -t backend .
docker run  -it --rm  --name back -p 8082:80  backend
```

#  ASURE

После регистрации создаем resourse group
Далее идет установка ВМ, Virtual Network

Соединение с ВМ беспарольное, с помощью сертификата Ubuntu-1.pem
(Для использования в putty преобразовать pem ключ в ppk !)

##### Ставимм docker и docker-compose:

	sudo apt-get update 
	sudo apt-get install -y apt-transport-https ca-certificates \
	curl gnupg-agent software-properties-common subversion

	sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add - 
	sudo add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" 
	sudo apt-get update 
	sudo apt-get install docker-ce docker-ce-cli containerd.io -y
	sudo curl -L "https://github.com/docker/compose/releases/download/1.28.2/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose 
	sudo chmod +x /usr/local/bin/docker-compose   
	
Добавим пользователя jenkins и вводим в группы docker и sudo:  

	sudo useradd -m -s /bin/bash jenkins	
	sudo passwd jenkins
	sudo usermod -aG docker $USER
	sudo usermod -aG sudo jenkins
	sudo usermod -aG docker jenkins	

##### Установка контейнера с jenkins через ansible, установленном на локальной машине:

В папке /etc/ansible/  ректируем hosts
[azure]
        ubuntu-1 ansible_host=GoodVvine@23.97.196.147
#[ubuntu:vars]
[azure:vars]
        ansible_user=GoodVine
        ansible_ssh_private_key_file=/etc/ansible/.ssh/azure-ubuntu-1.pem
        ansible_python_interpreter=/usr/bin/python3

Выполняем плейбук из task7\ansible\dock-jenk.yml
```sh
 ansible-playbook dock-jenk.yml 
```
(Позже добавлены роли для установки Docker и необходимых пакетов, чтобы автоматизировать установку необходмого.)

На ВМ Ubuntu-1 :
```sh
sudo apt install openjdk-8-jre-headless
```
При необходимости открываем  порт 
>
iptables -A TCP -p tcp --dport 8080 -j ACCEPT  
iptables -I INPUT -p tcp --dport 8080 -j ACCEPT  
ufw allow 8080  

Если собираемся использовать user jenkins в билд-агенте, то в папке jenkins : sudo chown -R 1000:1000 .  
Но можно использовать основного user'а (указываем домашнюю папку этого user в билд-агенте и настраиваем ssh + Credentials в Jenkins - PrivatKey из pem)

##### Настройки pipeline

Поскольку у разработчиков свой Git, а нам нужны дополнительные файлы для build, их расположим в собственном репозитории по папкам (frontend, backend и.т.д.)
Для скачивания нужной папки из git, чтобы не тащить весь репозиторий установим пакет
```sh
sudo apt install subversion -y
```
Проверим наличие нужной папки:
```sh
svn ls https://github.com/GoodVine1971/test1/trunk/task7/frontend     ###### все файлы
svn checkout https://github.com/GoodVine1971/test1/trunk/task7/frontend   ###### или так (только папка)
```
С помощью команды:
```sh
svn export --force https://github.com/GoodVine1971/test1/trunk/task7/frontend
```
скачиваем необходимую папку, а затем перносим содержимое frontend в корень
```sh
mv -f frontend/* frontend/.[^.]* . && rmdir frontend/
```  
Так как сборка NodeJS оказывает большую нагрузку желательно включить запрещение: Do not allow concurrent builds
 или в pipeline:
 
	options {
	disableConcurrentBuilds()
	}
 
##### Настраиваем webhook в github

В настройках pipeline отмечаем: GitHub hook trigger for GITScm polling
В webhook вносим 
http://23.97.196.147:8080/github-webhook/ .
Не сработало. Попробуем использовать Trigger builds remotely  
Добавили Authentication Token: triggerFrontBuild
и изменили  webhook на git 
http://23.97.196.147:8080/job/frontend/build?token=triggerFrontBuild

Создадми пользователя gituser в jenkins  

В webhook вносим: 

http://gituser:password@23.97.196.147:8080/job/frontend/build?token=triggerFrontBuild  

Через пароль не работает . Добавляем API token в настройках пользователя gituser
 
![Токен gituser:](./images/gituser.jpg)

http://гитюзер:117f7baccf16e2f8c82443XXXXXXXXX@23.97.196.147:8080/job/frontend/build?token=triggerFrontBuild

Так работает.
 
![webhook:](./images/webhook.jpg) 
 
 Если происходит много commit, то  может возникнуть большая нагрузка, для этого вместо webhook использовать poll SCM  
 Расписание:  H */3 * * *   (каждые 3 часа, H вместо 0, чтобы не запускалось в 3:00, 6:00, а произвольное время, н-р 3:21
 
 
++++++++++++++++++++++++++++++++++


### Container Registry

Для отправки готовых образов контейнеров в registry воспользуемся руководством: 
https://docs.microsoft.com/ru-ru/azure/container-registry/container-registry-tutorial-prepare-registry  
  
Сначала нужно установить Azure CLI как указано здесь: https://docs.microsoft.com/ru-ru/cli/azure/install-azure-cli  

Воспользуемся установкой  Azure CLI в docker контейнере  

Создаем Реестр GoodVine  контейнеров Azure  нашей Resource Group.  (GoodVone_RG)  

Перейдем в новый реестр контейнеров на портале Azure и в разделе Параметры выберем Ключи доступа. В разделе Пользователь-администратор выберем Включить.  

#### Запуск контейнера с Azure CLI

```sh
docker run --rm -it  -v /var/run/docker.sock:/var/run/docker.sock  mcr.microsoft.com/azure-cli  /bin/bash
```

здесь --rm, чтобы Docker автоматически очищал контейнер и удалял файловую систему при выходе из контейнера

Затем, чтобы можно было работать с image на хосте добавим в терминале нашего контейнера:
```sh
apk --no-cache add docker
```

Теперь можно пользоваться docker'ом из контейнера (н-р docker image - все образы на хосте)

В терминале вводим
```sh
az login  
``` 
Открываем в браузере https://microsoft.com/devicelogin   или https://aka.ms/devicelogin вводим полученный код: C4WVU56WC 

Входим в реестр:
```sh
az acr login --name goodvine   
```

Можно подключиться без CLI непосредственно командой docker:
```sh
 docker login goodvine.azurecr.io -u goodvine -p password  , где password из раздела ключи в настройке Registry
```
Присвоим тэг:
```sh
docker tag frontend goodvine.azurecr.io/frontend:v1
```
Отправим в registry :
```sh
docker push goodvine.azurecr.io/frontend:v1 
```
Просмотреть registry :
```sh
az acr repository list --name goodvine --output table
```
удалить образ
```sh
az acr repository delete --name goodvine --image frontend:v1
```
Создать в registry образ из существующего с другим тэгом:
```sh
az acr import -n goodvine --force --source goodvine.azurecr.io/myimage:latest -t myimage:retagged 
az acr import -n goodvine --force --source goodvine.azurecr.io/frontend:v12 -t frontend:latest
```
Удалить тэг:
```sh
az acr repository untag -n goodvine --image frontend:v12
```
удалить untagged images:
```sh  
az acr run --registry goodvine --cmd "purge --filter 'frontend:.*'  --untagged" 
```
Удалить манифест и все тэги  для image frontend
```sh
az acr repository delete -n goodvine --image frontend:v12
```



#### Задать credentials для Jenkins 

Для автоматического входа в azure registry создадим службу:  service principal using Az CLI:
```sh
az ad sp create-for-rbac
```	
	Получим что-то похожее:
  "appId": "999999999-c740-48a8-90aa-0000000",
  "displayName": "azure-cli-2021-03-03-17-35-27",
  "name": "http://azure-cli-2021-03-03-17-35-27",
  "password": "555555mBQpyxKjXC.5X.00000000000000",
  "tenant": "9999999-5a22-440f-a0a9-0000000000"
	
Создадим в Jenkins credential:  Username with password и введем следующее :

Username - The appId of the service principal created.
Password - The password of the service principal created.
ID - Credential identifier, например: Principal

В pipeline создаем environment и создаем agent docker с образом azure-cli и step: withCredentials([usernamePassword(credentialsId: 'Principal', passwordVariable: 'AZURE_CLIENT_SECRET', usernameVariable: 'AZURE_CLIENT_ID')]) 

_______________________________

##Proxy

Существует проблема запуска на одном хосте frontend и backend:
Cross-Origin Request Blocked: The Same Origin Policy disallows reading the remote resource 

Добавление обратного proxy сервера:

Скачиваем подготовленный docker-compose.yml со своего репозитория: svn export --force https://github.com/GoodVine1971/test1/trunk/task7/proxy 
В нем запускаются frontend, backend, reverse proxy и mongodb. В nginx-proxy.conf указываются необходимые server_name (доменное имя, ip и localhost), в папке vhost - соответствующие по именам файлы, в которых иде proxyPass на наконтейнер back для адресов, содержащих /app. Таким образом из браузера вызывается backend с тем же доменным именем, что и frontend.

### База данных в docker

Для примера user: admin, пароль: pass

use admin
switched to db admin
> db.auth('admin', 'pass');
use ExadelBonusDb

db.createUser({user: "admin", pwd: "pass", roles:[{role: "readWrite" , db:"ExadelBonusDb"}]});

Тогда ConneсtString:
mongodb://admin:pass@mongo:27017


## SonarQube

см здесь: https://github.com/GoodVine1971/test1/tree/master/task7/SonarQube

## Логирование

Используем https://github.com/stefanprodan/dockprom Grafana + Prometeus
