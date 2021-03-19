openssl req -x509 -sha256 -nodes -newkey rsa:2048 -days 365 -keyout localhost.key -out localhost.crt -subj '/CN=localhost'
openssl pkcs12 -export -out localhost.pfx -inkey localhost.key -in localhost.crt


 docker run -p 445:443 -p 8082:80 --name back -e ASPNETCORE_URLS="https://+:443;http://+:80" -e ASPNETCORE_HTTPS_PORT=443 -e ASPNETCORE_Kestrel__Certificates__Default__Password="CHANGETHISSECRETKEY" -e ASPNETCORE_Kestrel__Certificates__Default__Path=/https/localhost.pfx -v ~/proxy/https:/https/ -d backend
 