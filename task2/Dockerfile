FROM nginx
MAINTAINER Gurin Sergey <GoodVine1971@gmail.com>
#ENV TZ=Europe/Minsk
RUN apt-get -y update
RUN apt-get install nano mc -y
COPY ./html/ /usr/share/nginx/html/
CMD ["nginx", "-g", "daemon off;"]
ENV DEVOPS=GoodVine
EXPOSE 80
RUN service nginx start
#RUN service nginx-debug start
#RUN service nginx restart
