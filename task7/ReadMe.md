Локальное тестирование

Ставим docker и git: apt-get install git

# FRONTEND

mkdir frontend
cd frontend
git clone https://github.com/umilanovich/exadelBonus

pipeline { 
    agent any

stages {
    stage('Display') {
        steps {
            
            //sh 'mkdir exadelBonus'
            //sh 'cd exadelBonus'
            git branch: 'develop', url: 'https://github.com/umilanovich/exadelBonus'
            //sh 'cd ..'
            sh 'cp -f /home/ansclient/frontend/Dockerfile . '
            sh 'cp -f /home/ansclient/frontend/.dockerignore . '
            //sh 'cp -f /home/ansclient/frontend/nginx.conf . '
            //sh 'mkdir frontend'
            //sh 'docker stop front'
            //sh 'docker rm front'
            sh 'docker build -t frontend .'
            //sh 'ls'
         }
    }
}
}

после создания образа frontend поднимаем контейнеры

docker build -t frontend .
docker run --name front -d -p 80:80 frontend

При запуске pipeline (и dockerfile) появляется много подвешенных контейнеров. Работаем с ними:

Остановить все контейнеры
> docker stop $(docker ps -a -q)
Удаление подвешенных образов
> docker rmi $(docker images -f dangling=true -q)
очистить кэш docker
> docker system prune -a  


# Backend   .Net 

Настраиваем Dockerfile и запускаем:

docker build -t backend .
docker run  -it --rm  --name back -p 5000:80  backend



#  ASURE


Получить pem ключ, преобразовать в ppk

Подключение в putty:
	ssh -i d:\Apache\keys\Ubuntu-1.pem GoodVine@23.97.196.147


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
	
Добавим пользователя jenkins и вводим в группы docker и sudo

	sudo useradd -m -s /bin/bash jenkins	
	sudo passwd jenkins
	sudo usermod -aG docker $USER
	sudo usermod -aG sudo jenkins
	sudo usermod -aG docker jenkins	

Установка контейнера с jenkins через ansible:
sudo apt install openjdk-8-jre-headless
в папке jenkins   chown -R 1000:1000 .

docker-compose -f /opt/jenkins/docker-compose.yml up -d

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
	

