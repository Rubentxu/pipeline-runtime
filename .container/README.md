
```bash
# Construir e Iniciar Contenedor.
# Parametro obligatorio directorio del Proyecto de Shared libraries a exponer en volumen
# La opcion --no-cache para construir todo de cero
$ earthly +start --sharedlibs_dir=$(pwd)/.. --secrets_file=$(pwd)/share/.env
# Parametro opcional exclude_modules para excluir los modulos que no son librerias
$ earthly +start --sharedlibs_dir=$(pwd)/.. --secrets_file=$(pwd)/share/.env


# Reiniciar Contenedor
# Parametro obligatorio directorio del Proyecto de Shared libraries a exponer en volumen
# La opcion --no-cache para construir todo de cero
$ earthly +restart --sharedlibs_dir=$(pwd)/../../modules --secrets_file=$(pwd)/share/.env
# Parametro opcional exclude_modules para excluir los modulos que no son librerias
$ earthly +restart --sharedlibs_dir=$(pwd)/../../modules --exclude_modules=integration-tests,unit-tests-fixtures --secrets_file=$(pwd)/share/.env


# Parar Contenedor de jenkins
$ earthly +stop

# Parar Contenedor, borra imagen y elimina los volumenes. Utilizar en caso de emergencia.
$ earthly +cleanup

# Podemos ver los logs del contenedor
$ earthly +logs


# Para borrar todos los volumenes de docker
$ earthly +delete-all-volumes

# Para parar todos las instancias de docker
$ earthly +stop-all-container

# Entrar en el contenedor en funcionamiento.
$ docker exec  -it prunner bash

$ docker run --name prunner --rm --privileged=true -it -v container-data:/home/prunner -v cache-data:/opt/cache -v $(pwd)/build:/opt/workspace:shared -v $(pwd)/..:/opt/libs:shared --mount type=bind,source=$(pwd)/share/.env,target=/home/prunner/.env --network host prunner bash

$ docker run --name prunner --rm --cap-add SYS_ADMIN --cap-add MKNOD --device /dev/fuse --privileged=true -it -v container-data:/home/prunner -v cache-data:/opt/cache -v $(pwd)/build:/opt/workspace -v $(pwd)/..:/opt/libs --mount type=bind,source=$(pwd)/share/.env,target=/home/prunner/.env,bind-propagation=rshared --network host prunner bash
$ java -jar /opt/libs/build/libs/pipeline-runner-0.1.0-all.jar -p /opt/libs/src/test/resources/pipelines/Jenkinsfile -c /opt/libs/src/test/resources/pipelines/config-test.yaml

$ asdf plugin add java 
$ asdf install java adoptopenjdk-11.0.13+8
$ asdf global java adoptopenjdk-11.0.13+8

echo | openssl s_client -servername $SERVER -connect $SERVER:443 |\
  sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > server-certificate.crt
```
