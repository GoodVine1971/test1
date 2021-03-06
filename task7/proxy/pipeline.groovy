pipeline { 
    agent any
   
stages {
    stage('Prepare Proxy') {
        steps {
            sh 'svn export --force https://github.com/GoodVine1971/test1/trunk/task7/proxy'
			sh 'mv -f proxy/* proxy/.[^.]* . && rmdir proxy/'
              }
    }
   
    stage('Stop running conteiners') {
        steps {
		sh 'sh docker rm -f newproxy'	
        sh 'sh docker rm -f front'
		sh 'sh docker rm -f back'
		// очистим все подвешенные images
		sh 'docker rmi $(docker images -f dangling=true -q)'
		}
    }
	stage('Run proxy') {
        steps {
        // Запускаем обратный proxy
        sh 'docker-compose up -d'
		
        }
    }

 }
}

