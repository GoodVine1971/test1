For SonarQube, you need to set the recommended values as a root user on the host machine:

sudo sysctl -w vm.max_map_count=262144 
sudo sysctl -w fs.file-max=65536 
или 
sudo echo "vm.max_map_count=262144" >> /etc/sysctl.conf



mkdir -p /data/sonarqube/{conf,logs,temp,data,extensions,bundled_plugins,postgresql,postgresql_data}


Поднимаем 
docker-compose.yml с jenkins, sonarqube, postgres

sudo chown -R 1000:1000 /data/jenkins/jenkins_home
sudo chown -R 999:999 /data/sonar/sonarqube_conf
sudo chown -R 999:999 /data/sonar/sonarqube_data
sudo chown -R 999:999 /data/sonar/sonarqube_extensions
sudo chown -R 999:999 /data/sonar/sonarqube_bundled-plugins
chown -R 999:999 /data/sonar/


$ docker exec -it <your-jenkins-container-id> bash $ cd /var/jenkins_home $ wget https://binaries.sonarsource.com/Distribution/sonar-scanner-cli/sonar-scanner-cli-4.2.0.1873-linux.zip $ unzip sonar-scanner-cli-4.2.0.1873-linux.zip 