# pipeline-tools

Contenedor con el que se gestionan los binarios necesarios en los pipelines de CI.

```bash
$ docker build --network host -t jenkins-tools-ex:1.0.0 .
$ docker run --user podman -it --privileged --network host -v /tmp/storage:/home/podman/.local/share/containers -v "$(pwd)"/tools:/home/podman/tools jenkins-tools-ex:1.0.0 bash
$ asdf-install-toolset -g tools/
```
