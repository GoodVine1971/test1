pipeline { 
    agent any
    environment {
        AZURE_SUBSCRIPTION_ID='286082ee-9003-4877-933d-038fa9e53f0c'
        AZURE_TENANT_ID='db4a653e-5a22-440f-a0a9-3e34b33a4052'
        CONTAINER_REGISTRY='goodvine'
        RESOURCE_GROUP='GoodVine_RG'
        REPO="frontend"
        IMAGE_NAME="image name"
        TAG="${currentBuild.number}"
    }
stages {
    stage('Prepare') {
        steps {
            
            //sh 'mkdir exadelBonus'
            //sh 'cd exadelBonus'
            git branch: 'develop', url: 'https://github.com/umilanovich/exadelBonus'
           
            //sh 'cp -f /home/ansclient/frontend/nginx.conf . '
            //sh 'mkdir frontend'
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
            // Используем trunk для получения папки, затем svn
            sh 'svn export --force https://github.com/GoodVine1971/test1/trunk/task7/frontend'
            // переносим содержимое frontend на шаг выше и удаляем папку
            sh 'mv -f frontend/* frontend/.[^.]* . && rmdir frontend/'
            sh "sed -i -e 's/^.*apiUrl.*/${apiUrl}/g' src/environments/environment.ts"
            sh "sed -i -e 's/^.*apiUrl.*/${apiUrl}/g' src/environments/environment.prod.ts"
        }    
    }
    stage('Build') {
        steps {
        
        sh 'docker build -t frontend .'
      sh 'ls'
        //sh 'docker stop front'
        //sh 'docker rm front'
		// Можго сделать push образа без Azure CLI				
		// sh 'docker login goodvine.azurecr.io -u goodvine -p passwor_from_keys'
       // sh 'docker push goodvine.azurecr.io/frontend:v1'
        }
    }
	stage('Run image') {
        steps {
        // Останавливаем и удаляем контейнер front
        sh 'sh docker rm -f front'
		// запускаем новый
		sh 'docker run --name front -d -p 80:80 frontend'
        //sh 'docker stop front'
        //sh 'docker rm front'
		// Можго сделать push образа без Azure CLI				
		// sh 'docker login goodvine.azurecr.io -u goodvine -p passwor_from_keys'
       // sh 'docker push goodvine.azurecr.io/frontend:v1'
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
				//--user root --privileged необходим, чтобы выполнять внутри контейнера команды
                //args '-v ${HOME}:/home/az -e HOME=/home/az'  возможно можно будет выполнять az из хоста ???
                reuseNode true
		}
    }
        steps {
         withCredentials([usernamePassword(credentialsId: 'Principal', passwordVariable: 'AZURE_CLIENT_SECRET', usernameVariable: 'AZURE_CLIENT_ID')]) {
                     sh 'apk --no-cache add docker'
                            sh 'az login --service-principal -u $AZURE_CLIENT_ID -p $AZURE_CLIENT_SECRET -t $AZURE_TENANT_ID'
                            sh 'az account set -s $AZURE_SUBSCRIPTION_ID'
                            sh 'az acr login --name $CONTAINER_REGISTRY'
						//	sh 'az acr repository list --name $CONTAINER_REGISTRY --output table'
						//	sh 'az acr import -n $CONTAINER_REGISTRY --source goodvine.azurecr.io/frontend:v1 -t frontend:retagged'
						//	sh 'az acr repository delete -n $CONTAINER_REGISTRY --repository $REPO --image frontend:v1'
						//	sh 'az acr repository delete --name $CONTAINER_REGISTRY --image frontend:v1 -y'
                            //sh 'az acr login --name $CONTAINER_REGISTRY --resource-group $RESOURCE_GROUP'
                            //sh 'az acr build --image $REPO/$IMAGE_NAME:$TAG --registry $CONTAINER_REGISTRY --file Dockerfile . '
                           // sh 'docker push goodvine.azurecr.io/frontend:v$TAG'
                          //  sh 'docker login  goodvine.azurecr.io -u goodvine'
                          sh 'docker tag frontend goodvine.azurecr.io/frontend:v$TAG'
                          sh 'docker push goodvine.azurecr.io/frontend:v$TAG'
                        }    
     //   sh 'which az'
    //    sh 'whoami'
	//	sh 'docker tag frontend:latest frontend:${num}' 
    //  sh 'docker rmi frontend:latest' 
    //    sh 'apk --no-cache add docker'
	//	sh 'docker -v'
	//	sh 'docker ps'
	//	sh 'docker images'
	//	sh 'echo $BUILD_NUMBER -1'
		//sh 'echo ${num}'
	//	sh 'az acr repository delete --name goodvine --image frontend:v1'
		sh 'sleep 10'
		
        }
    }
    
 }
}

