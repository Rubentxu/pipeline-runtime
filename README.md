## Ejecutar pipeline de ejemplo con:
```bash
$ gradle examples:runScript
```


### Construcción y ejecucion de imagen docker

```bash
$ docker build -t jenkins-tools:1.0.5 .
$ podman build -t jenkins-tools:1.0.9 .
$ docker run -it  jenkins-tools:1.0.5 bash

# Con volumen
$ docker run -it  -v .:/project redis jenkins-tools:1.0.9 bash

$ docker run --user podman -it --privileged --network host \
  -v ~/Proyectos/Mercadona/Pipelines/jenkins-core-lib:/home/podman/project \
  -v ~/Proyectos/Mercadona/Pipelines/jenkins-core-lib/pipeline-runtime/src/container/config-tools/.m2/:/home/podman/.m2 \
  -v ~/Proyectos/Mercadona/Pipelines/jenkins-core-lib/pipeline-runtime/src/container/config-tools/.gradle/:/home/podman/.gradle \
  -v ~/Proyectos/Mercadona/Pipelines/jenkins-core-lib/pipeline-runtime/src/container/config-tools/npm/:/usr/local/etc/npmrc \
  --workdir /home/podman/project \
  -v /tmp/mycontainers:/var/lib/containers \
  jenkins-tools-ex:1.0.8 \
  bash

 $ docker run --user podman  --network host -it --privileged jenkins-tools:1.1.4 podman run ubi8 echo hello
 $ docker run --user podman  -it --privileged -v /tmp/mycontainers:/var/lib/containers jenkins-tools:1.1.4 podman run ubi8 echo hell
 $ podman run -v /tmp/mystorage:/home/podman/.local/share/containers:rw --security-opt label=disable --user podman quay.io/podman/stable \
      podman run --rm docker.io/library/alpine ls

  # Atención se ha metido runc como crun(soporta CgroupsV2, mas rapido) para la ejecuciones con podman
 $ podman --runtime /use/bin/runc run --rm --pids-limit 5 fedora echo it works
    Error: container create failed (no logs from conmon): EOF
 $ podman --runtime /usr/bin/crun run --rm --pids-limit 1 fedora echo it works
    it works
```

