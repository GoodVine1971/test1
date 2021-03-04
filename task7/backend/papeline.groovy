pipeline { 
    agent any
#	agent {
#    label 'client'
#    }
stages {
    stage('Prepare') {
        steps {
           
            git branch: 'master', url: 'https://github.com/myuk96/exadel-bonus-plus'
           
         }
    }
    stage('Get Dockerfile') {
        
        steps {
            //sh 'cp -f /home/ansclient/backend/Dockerfile . '
            //sh 'cp -f /home/ansclient/backend/.dockerignore . '
            sh 'svn export --force https://github.com/GoodVine1971/test1/trunk/task7/backend'
            sh 'mv -f backend/* backend/.[^.]* . && rmdir backend/'
                        
        }    
        
    }
    stage('Build') {
        steps {
        
        sh 'docker build -t backend .'
        }
    }
    
 }
}

