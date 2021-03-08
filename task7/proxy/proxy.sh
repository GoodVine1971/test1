svn export --force https://github.com/GoodVine1971/test1/trunk/task7/proxy
mv -f proxy/* . && rmdir proxy/
 # остановим и удалим запущенные ранее контейнеры
docker rm -f newproxy
docker rm -f front
docker rm -f back
	#очистим все подвешенные images
docker rmi $(docker images -f dangling=true -q)
docker-compose up -d