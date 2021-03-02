pipeline { 
    agent any

stages {
    stage('Display') {
        environment {
        newUrl = 'apiUrl: "http://localhost:5000", '
        apiUrl = newUrl.replace("/", "\\/")
    }
        steps {
            
            //sh 'mkdir exadelBonus'
            //sh 'cd exadelBonus'
            git branch: 'develop', url: 'https://github.com/umilanovich/exadelBonus'
            //sh 'cd ..'
            //sh 'cp -f /home/ansclient/frontend/Dockerfile . '
            //sh 'cp -f /home/ansclient/frontend/.dockerignore . '
            //sh 'cp -f /home/ansclient/frontend/nginx.conf . '
            //sh 'mkdir frontend'
            //sh 'docker stop front'
            //sh 'docker rm front'
            //sh 'docker build -t frontend .'
            sh 'cat src/environments/environment.ts'
            
            sh "sed -i -e 's/^.*apiUrl.*/${apiUrl}/g' src/environments/environment.ts"
			sh "sed -i -e 's/^.*apiUrl.*/${apiUrl}/g' src/environments/environment.prod.ts"
            sh 'cat src/environments/environment.ts'
            sh 'ls'
         }
    }
}
}