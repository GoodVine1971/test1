# Выполнение задания Task7 (Cloud)

Что сделано:

1. Регистрация в Azure
2. Ресурсы:
   - Виртуальная машина
   - Учетная запись Azure Cosmos DB (для Mongodb)
   - Реестр контейнеров
3. Установлен Docker на ВМ
4. Установлен Jenkins  (с помощью Ansible)
5. Настроен pipeline Frontend (build, deploy в Azure Container Registry)
6. Настроен pipeline Backend (build, deploy в Azure Container Registry)
7. Настроен Frestyle, зависящий от Frontend и Backend, для запуска reverse proxy с latest контейнерами
8. Архивация и восстановление базы данных средствами Azure Cosmos DB
9. Опрос Git раз в 3 часа. Сначала было сделано по WebHook, но из-за частого commit изменено 

Что не удалось:

1. Из-за особенностей кеширования поздно выяснилось, что при смене адресов на свои, продолжала использоваться база разработчиков.
Требование обязательного ssl для коннекта с базой данных в azure выполнить не удалось, т.к. требует дополнительно настройки со стороны разработчиков приложения .Net
Пришлось запустить Mongodb в контейнере.
2. Ssl сертификация. Если для frontend доменного имени задать letsencrypt сертификат автоматически удалось с помощью reverse proxy и контейнера letsencrypt-nginx-proxy-companion, то для внутреннего контейнера backend этот способ не подходит. А использование http для обращения к backend из https вызывает блокировку в браузере (смена протокола). 
3. Обращение к backend по имени контейнера (или виртуального хоста). Используется <имя или ip>:port, где port , на котором работает backend (открыт наружу) 
4. Сделать безошибочную проверку кода  (SonaQube + Jankins).


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
очистить кэш docker  
> docker system prune -a   


### Backend   .Net 

Настраиваем Dockerfile и запускаем:
```sh
docker build -t backend .
docker run  -it --rm  --name back -p 5000:80  backend
```

#  ASURE

После регистрации создаем resourse group
Далее идет установка ВМ, Virtual Network

Соединение с ВМ беспарольное, с помощью сертификата Ubuntu-1.pem
(Для использования в putty преобразовать pem ключ в ppk !)

Ставимм docker и docker-compose:

	sudo apt-get update 
	sudo apt-get install -y apt-transport-https ca-certificates \
	curl gnupg-agent software-properties-common 

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

Установка контейнера с jenkins через ansible, установленном на локальной машине:

В папке /etc/ansible/  ректируем hosts
[azure]
        ubuntu-1 ansible_host=GoodVvine@23.97.196.147
#[ubuntu:vars]
[azure:vars]
        ansible_user=GoodVine
        ansible_ssh_private_key_file=/etc/ansible/.ssh/azure-ubuntu-1.pem
        ansible_python_interpreter=/usr/bin/python3

Выполняем плейбук из task7\ansible\dock-jenk.yml

 ansible-playbook dock-jenk.yml 

На Ubuntu-1 :
sudo apt install openjdk-8-jre-headless
Если  в папке jenkins   chown -R 1000:1000 .


Откроем порт 
iptables -A TCP -p tcp --dport 8080 -j ACCEPT
iptables -I INPUT -p tcp --dport 8080 -j ACCEPT
ufw allow 8080



Настройки pipeline

Для скачивания только папки из git установим пакет
sudo apt install subversion -y

svn ls https://github.com/GoodVine1971/test1/trunk/task7/frontend       проверить, а потом
svn checkout https://github.com/GoodVine1971/test1/trunk/task7/frontend   или лучше (так только папка)
svn export --force https://github.com/GoodVine1971/test1/trunk/task7/frontend
А затем перносим содержимое frontend в корень
mv -f frontend/* frontend/.[^.]* . && rmdir frontend/
pipeline:
sh 'svn export --force https://github.com/GoodVine1971/test1/trunk/task7/frontend'
sh 'mv -f frontend/* frontend/.[^.]* . && rmdir frontend/'




Настраиваем webhook в github и в настройках pipeline отмечаем GitHub hook trigger for GITScm polling

http://23.97.196.147:8080/github-webhook/ .
Не сработало
Попробуем использовать Trigger builds remotely
Добавили TOKEN: triggerFrontBuild
и изменили  webhook на git 
http://23.97.196.147:8080/job/frontend/build?token=triggerFrontBuild

Создадми пользователя gituser в jenkins

http://username:password@jenkins.domain.tld/github-webhook/
http://gituser:1@23.97.196.147:8080/job/frontend/build?token=triggerFrontBuild
 Через пароль не работает . Добавляем API token в настройках пользователя gituser
117f7baccf16e2f8c8244376949b3eb322
http://gituser:117f7baccf16e2f8c8244376949b3eb322@23.97.196.147:8080/job/frontend/build?token=triggerFrontBuild
 Ура пошло!
 
 Включить запрещение Do not allow concurrent builds
 или в pipeline 
 options {
disableConcurrentBuilds()
}
 
 Если происходит много commit, то  может возникнуть большая нагрузка, для этого вместо webhook использовать poll SCM
 Расписание:  H */3 * * *   (каждые 3 часа, Р вместо 0, чтобы не запускалось в 3:00, 6:00, а произвольное время, н-р 3:21
 
 
++++++++++++++++++++++++++++++++++


# Container Registry
https://docs.microsoft.com/ru-ru/azure/container-registry/container-registry-tutorial-prepare-registry

Сначала нужно установить Azure CLI : https://docs.microsoft.com/ru-ru/cli/azure/install-azure-cli


Открываем https://microsoft.com/devicelogin   или https://aka.ms/devicelogin вводим полученный код: C4WVU56WC 

Создаем Реестр GoodVine  контейнеров Azure  нашей Resource Group.  (GoodVone_RG)

Перейдите в новый реестр контейнеров на портале Azure и в разделе Параметры выберите Ключи доступа. В разделе Пользователь-администратор выберите Включить

Запуск контейнера с Azure CLI 

docker run --rm -it  -v /var/run/docker.sock:/var/run/docker.sock  mcr.microsoft.com/azure-cli   здесь --rm, чтобы Docker автоматически очищал контейнер и удалял файловую систему при выходе из контейнера

Затем? чтобы можно было работать с image на хосте:
apk --no-cache add docker

Теперь пользоваться docker из контейнера (н-р docker image - все образы на хосте)

Входим: az login
Открываем https://microsoft.com/devicelogin   или https://aka.ms/devicelogin вводим полученный код: C4WVU56WC 

Входим в реестр:
az acr login --name goodvine   
_______________________________


Можно подключиться без CLI 
> docker login goodvine.azurecr.io -u goodvine -p password  , где password из раздела ключи в настройке Registry

Присвоим тэг:

docker tag frontend goodvine.azurecr.io/frontend:v1

Отправим в registry :

docker push goodvine.azurecr.io/frontend:v1 

Просмотреть registry :

az acr repository list --name goodvine --output table

удалить образ

az acr repository delete --name goodvine --image frontend:v1

Создать в registry образ из существующего с другим тэгом:

az acr import -n goodvine --force --source goodvine.azurecr.io/myimage:latest -t myimage:retagged 
az acr import -n goodvine --force --source goodvine.azurecr.io/frontend:v12 -t frontend:latest

Удалить тэг:
az acr repository untag -n goodvine --image frontend:v12
удалить untagged images:

PURGE_CMD="acr purge --filter 'frontend:.*' \
  --untagged --ago 1d"

az acr run \
  --cmd "$PURGE_CMD" \
  --registry goodvine \
  /dev/null
  
  
az acr run --registry goodvine --cmd "purge --filter 'frontend:.*'  --untagged" 
Удалить манифест и все тэги  для image frontend
az acr repository delete -n goodvine --image frontend:v12

_______________________________

Задать credentials для Jenkins 

Create a service principal using Az CLI:

    az ad sp create-for-rbac
	
	Получим
  "appId": "999999999-c740-48a8-90aa-0000000",
  "displayName": "azure-cli-2021-03-03-17-35-27",
  "name": "http://azure-cli-2021-03-03-17-35-27",
  "password": "555555mBQpyxKjXC.5X.00000000000000",
  "tenant": "9999999-5a22-440f-a0a9-0000000000"
	
Добавим credential  Username with password и введем следующее :

Username - The appId of the service principal created.
Password - The password of the service principal created.
ID - Credential identifier such as AzureServicePrincipal
	
_______________________________

##Proxy

Существует проблема запуска на одном хосте frontend и backend:
Cross-Origin Request Blocked: The Same Origin Policy disallows reading the remote resource 

Добавление обратного proxy сервера:

Добавим новую сеть:
docker network create docknet

И в hosts сайты front.loc и back.loc
sudo sed -i "2i127.0.0.1\tfront.loc" /etc/hosts
sudo sed -i "2i127.0.0.1\tback.loc" /etc/hosts

docker network connect docknet front
docker network connect docknet back

проверим:
docker network inspect docknet
Тест пинга:
docker exec -ti front ping back

## SonarQube

Установим в docker:

С помощью docker-compose.yml (подключаем все к сети jenkins

docker network connect sonarqube_jenkins jenkins-dock



Добавим правило в сетевой интерфейс (порт 9000)

Входим ip:9000
admin admin

Administartion->Security->User-> admin 
Генерируем токен: 3c4cf8f1f2064d7b0716746e5a665ac0038e05d5

Создаем новый проект: backend

Устанавливаем плагин SonarQube в Jenkins ? а также NodeJS 

Manage Jenkins > Configure System > Add SonarQube
Добавляем Credential как Secret Text', вносим полученный выше token

В Manage Jenkins > Global Tool Configuration > Add SonarScanner for MSBuild

Новый item - freestyle, укажем Git backend

docker exec -it jenkins-dock bash 
> cd /var/jenkins_home 
> wget https://binaries.sonarsource.com/Distribution/sonar-scanner-cli/sonar-scanner-cli-4.2.0.1873-linux.zip 
> unzip sonar-scanner-cli-4.2.0.1873-linux.zip 

###########################################################

ExadelBonusDb

db.createUser({user: "admin", pwd: "pass", roles:[{role: "readWrite" , db:"ExadelBonusDb"}]});
