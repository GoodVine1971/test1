pipeline { 
    agent any
    environment {
        AZURE_SUBSCRIPTION_ID='286082ee-9003-4877-933d-038fa9e53f0c'
        AZURE_TENANT_ID='db4a653e-5a22-440f-a0a9-3e34b33a4052'
        CONTAINER_REGISTRY='goodvine'
        RESOURCE_GROUP='GoodVine_RG'
        REPO="backend"
        IMAGE_NAME="image name"
        TAG="${currentBuild.number}"
    }
stages {
    stage('Prepare') {
        steps {
           
            git branch: 'master', url: 'https://github.com/myuk96/exadel-bonus-plus'
           
         }
    }
    stage('Get Dockerfile') {
        environment {
        newUrl = '"ConnectionString": "mongodb://goodvine:hdfQbf6cHMqAkBhsEVkFbBfD676kCMQm4pmLBYqI0NRZeWtda10gHe3zBBcRhQsgt4Z8QAN5izGfjL9rbmOhdw==@goodvine.mongo.cosmos.azure.com:10255/?ssl=true&retrywrites=false&replicaSet=globaldb&maxIdleTimeMS=120000&appName=@goodvine@", '
		
        ConnectionString = newUrl.replace("/", "\\/")
        }
        steps {
            //sh 'cp -f /home/ansclient/backend/Dockerfile . '
            //sh 'cp -f /home/ansclient/backend/.dockerignore . '
            sh 'svn export --force https://github.com/GoodVine1971/test1/trunk/task7/backend'
            sh 'mv -f backend/* backend/.[^.]* . && rmdir backend/'
            sh "sed -i -e 's/^.*ConnectionString.*/${ConnectionString}/g' ExadelBonusPlus.WebApi/appsettings.json"
            sh "sed -i -e 's/^.*ConnectionString.*/${ConnectionString}/g' ExadelBonusPlus.WebApi/appsettings.json"           
        }    
        
    }
    stage('Build') {
        steps {
        
        sh 'docker build -t backend .'
        }
    }
     stage('Deploy Image') {
		environment {
        int prev_tag = "${currentBuild.previousBuild.getNumber()}"
        
        }
		agent {
        docker { image 'mcr.microsoft.com/azure-cli'
                //args '-u 0:0'
                args '--user root --privileged -v /var/run/docker.sock:/var/run/docker.sock:rw,z'
				reuseNode true
		}
    }
        steps {
         withCredentials([usernamePassword(credentialsId: 'Principal', passwordVariable: 'AZURE_CLIENT_SECRET', usernameVariable: 'AZURE_CLIENT_ID')]) {
                     sh 'apk --no-cache add docker'
                     sh 'az login --service-principal -u $AZURE_CLIENT_ID -p $AZURE_CLIENT_SECRET -t $AZURE_TENANT_ID'
                     sh 'az account set -s $AZURE_SUBSCRIPTION_ID'
                     sh 'az acr login --name $CONTAINER_REGISTRY'
					 sh 'docker tag backend goodvine.azurecr.io/backend:v$TAG'
                     sh 'docker push goodvine.azurecr.io/backend:v$TAG'
                }    
     	
        }
    }
 }
}

