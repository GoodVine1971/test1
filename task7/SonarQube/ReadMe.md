For SonarQube, you need to set the recommended values as a root user on the host machine:

На хост машине желательно установить следующие рекомендованные значения:
```sh
sudo sysctl -w vm.max_map_count=262144 
sudo sysctl -w fs.file-max=65536 
или 
sudo echo "vm.max_map_count=262144" >> /etc/sysctl.conf
```
Установим в docker:

mkdir -p /data/sonarqube/{conf,logs,temp,data,extensions,bundled_plugins,postgresql,postgresql_data}

Пользователь sonar имеет id 999:
```sh
sudo chown -R 1000:1000 /data/jenkins/jenkins_home
sudo chown -R 999:999 /data/sonar/sonarqube_conf
sudo chown -R 999:999 /data/sonar/sonarqube_data
sudo chown -R 999:999 /data/sonar/sonarqube_extensions
sudo chown -R 999:999 /data/sonar/sonarqube_bundled-plugins
chown -R 999:999 /data/sonar/
```

С помощью docker-compose.yml (подключаем все к одной сети, н-р jenkins)
```sh
docker network connect sonarqube_jenkins jenkins-dock
```

Добавим правило в сетевой интерфейс (порт 9000)

Входим ip:9000
admin admin

Administartion->Security->User-> admin 
Генерируем токен: 3c4cf8f1f2064d7b0716746e5a665ac0038e05d5

Создаем новый проект: backend

Устанавливаем плагин SonarQube в Jenkins. а также NodeJS 

Manage Jenkins > Configure System > Add SonarQube
Добавляем Credential как Secret Text', вносим полученный выше token

В Manage Jenkins > Global Tool Configuration > Add SonarScanner for MSBuild

Новый item - freestyle, укажем Git backend

docker exec -it jenkins-dock bash 
> cd /var/jenkins_home 
> wget https://binaries.sonarsource.com/Distribution/sonar-scanner-cli/sonar-scanner-cli-4.2.0.1873-linux.zip 
> unzip sonar-scanner-cli-4.2.0.1873-linux.zip 

