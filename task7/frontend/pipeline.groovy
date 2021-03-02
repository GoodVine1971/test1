pipeline { 
    agent any

stages {
    stage('Prepare') {
        steps {
            
            //sh 'mkdir exadelBonus'
            //sh 'cd exadelBonus'
            git branch: 'develop', url: 'https://github.com/umilanovich/exadelBonus'
            //sh 'cd ..'
            
            //sh 'cp -f /home/ansclient/frontend/nginx.conf . '
            //sh 'mkdir frontend'
            //sh 'docker stop front'
            //sh 'docker rm front'
            
            //sh 'ls'
         }
    }
    stage('Get Dockerfile and change url to backend') {
        environment {
        newUrl = 'apiUrl: "http://localhost:5000", '
        apiUrl = newUrl.replace("/", "\\/")
        }
        steps {
            //sh 'cp -f /home/ansclient/frontend/Dockerfile . '
            //sh 'cp -f /home/ansclient/frontend/.dockerignore . '
            sh 'svn export --force https://github.com/GoodVine1971/test1/trunk/task7/frontend'
            sh 'mv -f frontend/* frontend/.[^.]* . && rmdir frontend/'
            sh "sed -i -e 's/^.*apiUrl.*/${apiUrl}/g' src/environments/environment.ts"
            sh "sed -i -e 's/^.*apiUrl.*/${apiUrl}/g' src/environments/environment.prod.ts"
            
        }    
        
    }
    stage('Build') {
        steps {
        
        sh 'docker build -t frontend .'
        }
    }
    
 }
}