#  Выполнение задания Task3 (Ansible)  #  
  
##  Установка  Ansible  ## 

sudo apt install ansible - последняя  

Для установки версии 2.8.10 исползуем pip

>sudo apt update
>sudo apt install python3-pip
>sudo -H pip3 install ansible==2.8.10
или pip3 install --user ansible==2.8.10
_______________________________________________________


На клиенте разрешаем подключение по ssh 
	Устанавливаем пакет
	sudo apt install openssh-server
	
	Проверим статус 
	sudo systemctl status ssh  #  q - выход 
	 
	Firewall default не включен, иначе :
	sudo ufw allow ssh  # добавляем порт ssh 
	
	Подключение к SSH-серверу
	ssh ansclient@192.168.0.10
		сервере Ansible копируем ключ
		cat ~/.ssh/id_rsa.pub
	
		На клиенте вставляем 
		nano ~/.ssh/authorized_keys
		и меняем permissions
		chmod 600 authorized_keys
		
		Или просто
		ssh-copy-id ansclient@192.168.0.10
		
	По умолчанию в Ubuntu python3, если нет, то установить 
	sudo apt install python (python3)
	
	Отключение/вкл SSH в Ubuntu
	sudo systemctl disable/enable --now ssh
	
На сервере управления Ansible настраиваем hosts: sudo nano /etc/ansible/hosts
	
	[group_name] alias ansible_ssh_host=x.x.x.x
	#Тег group_name позволяет ссылаться сразу на несколько серверов; alias задаёт имя сервера :
	
[ubuntu]
        client1 ansible_host=ansclient@192.168.0.10
[ubuntu:vars]
        ansible_python_interpreter=/usr/bin/python3
[debian]
        server2 ansible_host=root@192.168.0.112
        #host3 ansible_host=92.168.0.113
[debian:vars]
        ansible_python_interpreter=/usr/bin/python

	
Переменные лучше  хранить в отдельных файлах /etc/ansible/{host,group}_vars
/etc/ansible/{host,group}_vars

	
	
	[all:vars]
	ansible_python_interpreter=/usr/bin/python3
	
Проверим hosts: ansible-inventory --list -y

Проверим подключение

ansible all -m ping -u ansclient  # пинг на клиента
ansible all -m ping  # я прописал пользователя в hosts

Ping pong.jpg - результат выполнения ansible all -m ping
Можно указать несколько хостов, разделив их имена двоеточиями:
ansible server1:server2 -m ping -u root  # root, если не известен/незаведен пользователь
ansible all -a "df -h" -u ansclient  # использование диска

=======================================

Создаем playbook:  /etc/ansible/first-nginx.yml

---
- hosts: client1
  become:
    true
  become_method:
    sudo или su
  become_user:
    root
  remote_user:
    ansible
  roles:
   - epel
   - nginx
   
    где:

--- — начало файла YAML. Данный формат имеет строгую структуру  — важен каждый пробел; 
hosts — группа хостов, к которым будут применяться правила плейбука (если мы хотим, чтобы правила применялись ко всем хостам, указываем hosts: all); 
become — указывает на необходимость эскалации привилегий; 
become_method — метод эскалации привилегий; 
become_user — пользователь под которым мы заходим с помощью become_method; 
remote_user — пользователь, под которым будем подключаться к удаленным серверам; 
roles — список ролей, которые будут применяться для плейбука.
* В данном случае мы задействуем нашу группы хостов, которые создали в самом начале; повышаем привилегии методом su под пользователем root (su - root) для группы redhat-servers и методом sudo для debian-servers; подключение к серверам выполняется от пользователя ansible; используем созданную нами роль nginx (саму роль мы создадим позже).


---
- hosts: client1
  become:
    true
  become_method:
    sudo
  tasks:
    - name: Installs nginx
      apt: pkg=nginx

	
	
      apt: pkg=apache2 state=installed update_cache=true	
	
	Запуск playbook с запросом пароля
ansible-playbook first-nginx.yml	--ask-become-pass
или без интерактивного ввода пароля
ansible-playbook first-nginx.yml --extra-vars "ansible_sudo_pass=pass_ansclient"

ansible ubuntu -a 'systemctl status nginx.service'



	
  roles:
   - nginx

mkdir -p /etc/ansible/roles/nginx/tasks








+++++++++++++++++++++++++++++++++++++++++++++++
ssh-keygen

Generating public/private rsa key pair.
Enter file in which to save the key (/home/goodvine/.ssh/id_rsa): 
Created directory '/home/goodvine/.ssh'.
Enter passphrase (empty for no passphrase): Sergey

 
 Your identification has been saved in /home/goodvine/.ssh/id_rsa
Your public key has been saved in /home/goodvine/.ssh/id_rsa.pub
The key fingerprint is:
SHA256:E2oP9OJ32uUgubGI8K+x/WeTQCXMfLYC8t7xjOZlgvI goodvine@Ubuntu-test
The key's randomart image is:
+---[RSA 3072]----+
|       +         |
|    . . = +      |
|     o...= .     |
|     ..o+..      |
|     .=+S*       |
|    .oo+*o=      |
|  . .o.o*=o..    |
|   o =Eo.O=+     |
|    =o+.=o...    |
+----[SHA256]-----+

Если при первом соединении по ssh ключу требует unlock - ввести passphrase
