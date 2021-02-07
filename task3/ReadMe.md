#  Выполнение задания Task3 (Ansible)  #  
  
 Развернуты 2 виртуальные машины с Ubuntu 20.04LTS 
 1. Сервер 192.168.0.1  
 2. Клиент 192.168.0.10  user: ansclient
 3. Для проверки пинга также используется  ранее установленная машина с Debian8: 192.168.0.112  
 
##  Установка  Ansible  ## 

sudo apt install ansible - последняя  

Для установки версии 2.8.10 исползуем pip

>	sudo apt update
>	sudo apt install python3-pip
>	sudo -H pip3 install ansible==2.8.10  
или
>	pip3 install --user ansible==2.8.10

##  Настройка  Ansible  ## 

1. Подключение по ssh
	На клиенте разрешаем подключение по ssh . При необходимости установим 
    >   openssh-server  

    Проверим:  
    >	sudo systemctl status ssh  

    Генерируем ssh ключи на сервере управления  Ansible
    >	ssh-keygen  

	Копируем отклытый ключ на клиента:
	>	cat ~/.ssh/id_rsa.pub  
	
	Подключаемся по ssh к клиенту м вставляем 
	>	nano ~/.ssh/authorized_keys  
	
	меняем permissions
	>	chmod 600 authorized_keys  
	
	или проще 
	>	ssh-copy-id ansclient@192.168.0.10
	
2. На сервере управления Ansible настраиваем hosts:  
	>	sudo nano /etc/ansible/hosts  
	
	# Так как в Ubuntu python3, а в Debian - python2 разделим по группам:
	[ubuntu]
        client1 ansible_host=ansclient@192.168.0.10
	[ubuntu:vars]
        ansible_python_interpreter=/usr/bin/python3
	[debian]
        server2 ansible_host=root@192.168.0.112
        #host3 ansible_host=92.168.0.113
	[debian:vars]
        ansible_python_interpreter=/usr/bin/python 
		
	Переменные рекомендуют хранить в отдельных файлах {host,group}_vars, но для выполнения задания не буду усложнять структуру  

	Проверяем hosts:
	
	>	ansible-inventory --list -y  
	
	Проверяем подключение: 
	
	>	ansible all -m ping   # здесь не указывается пользователь, т.к. я его указал явно в hosts
	
	
	
	![Результат выполнения:](ping-pong.jpg)