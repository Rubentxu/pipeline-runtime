FROM python:3.10-bullseye

ARG THE_USER=prunner
ARG THE_USER_PASS=patata
ARG USER_HOME=/home/${THE_USER}
ENV ASDF_DIR=${USER_HOME}/.asdf
ENV ASDF_DATA_DIR="/opt/cache/jenkinsTools/.asdf-data"
ENV PATH="${PATH}:${ASDF_DATA_DIR}/shims:${ASDF_DIR}/bin"
VOLUME /opt/libs

RUN wget http://downloadcontent.opensuse.org/repositories/home:/alvistack/Debian_11/Release.key -O alvistack_key \
    && cat alvistack_key | gpg --dearmor | tee /etc/apt/trusted.gpg.d/alvistack.gpg  >/dev/null \
    && echo "deb http://downloadcontent.opensuse.org/repositories/home:/alvistack/Debian_11/ /" | tee  /etc/apt/sources.list.d/alvistack.list \
    && apt update \
    && apt install -y --no-install-recommends crun curl findutils fuse-overlayfs podman \
        python3-podman-compose vim fuse3 git runc tar uidmap unzip xz-utils sudo  \
    && apt clean cache  \
    && rm -fr /var/cache/apt /var/log/apt /var/lib/apt/lists/*

RUN groupadd -g 1000 ${THE_USER} \
    && useradd -m -s /bin/bash -g ${THE_USER} -G ${THE_USER} -u 1000 ${THE_USER} \
    && echo ${THE_USER}:10000:1001090000 | tee /etc/subuid /etc/subgid  \
    && echo java:100000:1001090000 | tee -a /etc/subuid /etc/subgid  \
    && echo "${THE_USER}:${THE_USER_PASS}" | chpasswd && adduser ${THE_USER} sudo

# Install httpie as binary
RUN curl https://packages.httpie.io/binaries/linux/http-latest -o http; \
    install http /usr/local/bin/http; \
    ln -s /usr/local/bin/http /usr/local/bin/https; \
    rm -f http \
    && mkdir -m777 -p /opt/repositories && mkdir -m777 -p /opt/cache && mkdir -m777 -p /home/prunner && ls -latr /opt \
    && echo  '[ ! -f /home/prunner/.env ] || export $(grep -v '^#' /home/prunner/.env | xargs)' >> /root/.bashrc \
    && echo  '[ ! -f /home/prunner/.env ] || export $(grep -v '^#' /home/prunner/.env | xargs)' >> /home/prunner/.bashrc \
    && echo "none  /tmp  tmpfs  defaults  0 0" | tee -a /etc/fstab

COPY containers.conf /etc/containers/containers.conf
COPY storage.conf /etc/containers/storage.conf
COPY settings.xml ${USER_HOME}/.m2/
COPY maven.config ${USER_HOME}/.mavenrc
COPY --chmod=0755 init.sh ${USER_HOME}/scripts/
COPY --chmod=0666 containers.conf ${USER_HOME}/.config/containers/containers.conf

RUN mkdir -p ${ASDF_DATA_DIR} ${USER_HOME}/.local/share/containers ${USER_HOME}/.config \
    && chown ${THE_USER}:${THE_USER} -R ${ASDF_DATA_DIR} ${USER_HOME} ${USER_HOME}/.config \
    && chmod 644 /etc/containers/containers.conf  ${USER_HOME}/.config/containers/containers.conf;  \
    sed -i -e 's|^#mount_program|mount_program|g' -e '/additionalimage.*/a "/var/lib/shared",' -e 's|^mountopt[[:space:]]*=.*$|mountopt = "nodev,fsync=0"|g' /etc/containers/storage.conf

WORKDIR $USER_HOME
USER $THE_USER



RUN  git clone --depth 1 https://github.com/asdf-vm/asdf.git ${ASDF_DIR} --branch v0.10.2 ;\
     echo alias docker='podman' >> ${USER_HOME}/.bashrc ;\
     echo ". ${USER_HOME}/.asdf/asdf.sh" >> ~/.bashrc ;\
     echo "export PATH=\"\$PATH:${ASDF_DIR}/bin/\"" >> ~/.bashrc