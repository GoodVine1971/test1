pipeline { 
    agent any
   
stages {
    stage('Prepare Proxy') {
        steps {
            sh 'svn export --force https://github.com/GoodVine1971/test1/trunk/task7/proxy'
			mv -f proxy/* . && rmdir proxy/
              }
    }
   
    stage('Stop running conteiners') {
        steps {
		#sh 'sh docker rm -f newproxy'	
        #sh 'sh docker rm -f front'
		#sh 'sh docker rm -f back'
		// очистим все подвешенные images
		#sh 'docker rmi $(docker images -f dangling=true -q)'
		sh 'docker-compose stop && docker-compose rm -f'
		}
    }
	stage('Run proxy') {
        steps {
        // Запускаем обратный proxy
        sh 'docker-compose up --build -d'
		
        }
    }

 }
}

# Freestyle
svn export --force https://github.com/GoodVine1971/test1/trunk/task7/proxy
mv -f proxy/* . && rmdir proxy/
 # остановим и удалим запущенные ранее контейнеры
#docker rm -f newproxy 2>/dev/null
#docker rm -f front 2>/dev/null
#docker rm -f back 2>/dev/null
	#очистим все подвешенные images
#docker rmi -f $(docker images -f dangling=true -q) 2>/dev/null
docker-compose stop && docker-compose rm -f
docker-compose up --build -d 