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

Для скачивания только папки из git 
sudo apt install subversion

svn ls https://github.com/GoodVine1971/test1/trunk/task7/frontend       проверить, а потом
svn checkout https://github.com/GoodVine1971/test1/trunk/task7/frontend   или лучше (так только папка)
svn export --force https://github.com/GoodVine1971/test1/trunk/task7/frontend
А затем перносим содержимое frontend в корень
mv -f frontend/* frontend/.[^.]* . && rmdir frontend/
pipeline:
sh 'svn export --force https://github.com/GoodVine1971/test1/trunk/task7/frontend'
sh 'mv -f frontend/* frontend/.[^.]* . && rmdir frontend/'

Настраиваем webhook в github и в настройках pipeline отмечаем GitHub hook trigger for GITScm polling
Не сработало
Попробуем использовать Trigger builds remotely
Добавили TOKEN: triggerFrontBuild
и изменили  webhook на git 
http://23.97.196.147:8080/job/frontend/build?token=triggerFrontBuild
