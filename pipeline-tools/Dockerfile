FROM registry.access.redhat.com/ubi8/ubi

ARG THE_USER=putoamo
ARG USER_HOME=/home/${THE_USER}
ARG ASDF_BASE_FOLDER=${USER_HOME}/.asdf
ENV ASDF_SH=${ASDF_BASE_FOLDER}/asdf.sh
ENV ASDF_DATA_DIR="${USER_HOME}/.asdf-data"
ARG ASDF_BRANCH=v0.8.1
ENV PATH="${PATH}:${ASDF_DATA_DIR}/shims:${ASDF_DATA_DIR}/bin"

RUN dnf -y module enable container-tools:rhel8; dnf -y update; rpm --restore --quiet shadow-utils; \
    dnf -y install crun podman unzip curl git tar jq findutils fuse-overlayfs /etc/containers/storage.conf --exclude container-selinux; \
    rm -rf /var/cache /var/log/dnf* /var/log/yum.*

RUN useradd -m -s /usr/bin/bash -g root -u 1000 ${THE_USER}; \
    echo ${THE_USER}:10000:5000 > /etc/subuid; \
    echo ${THE_USER}:10000:5000 > /etc/subgid;

VOLUME /var/lib/containers
RUN mkdir -p /home/${THE_USER}/.local/share/containers
RUN chown ${THE_USER}:root -R /home/${THE_USER}
VOLUME /home/${THE_USER}/.local/share/containers

ADD .tool-plugins /home/${THE_USER}/.tool-plugins
ADD .tool-versions /home/${THE_USER}/.tool-versions
ADD asdf-install-toolset.sh /usr/local/bin/asdf-install-toolset
ADD containers.conf /etc/containers/containers.conf
ADD podman-containers.conf /home/${THE_USER}/.config/containers/containers.conf

# chmod containers.conf and adjust storage.conf to enable Fuse storage.
RUN chmod 644 /etc/containers/containers.conf; sed -i -e 's|^#mount_program|mount_program|g' -e '/additionalimage.*/a "/var/lib/shared",' -e 's|^mountopt[[:space:]]*=.*$|mountopt = "nodev,fsync=0"|g' /etc/containers/storage.conf
RUN mkdir -p /var/lib/shared/overlay-images /var/lib/shared/overlay-layers /var/lib/shared/vfs-images /var/lib/shared/vfs-layers; touch /var/lib/shared/overlay-images/images.lock; touch /var/lib/shared/overlay-layers/layers.lock; touch /var/lib/shared/vfs-images/images.lock; touch /var/lib/shared/vfs-layers/layers.lock

ENV _CONTAINERS_USERNS_CONFIGURED=""

WORKDIR $USER_HOME
USER $THE_USER

RUN  \
     # ---- Install asdf -------------------------------------------------------
     git clone --depth 1 https://github.com/asdf-vm/asdf.git ${ASDF_BASE_FOLDER} --branch ${ASDF_BRANCH} \
     # ---- Add docker alias and call asdf.sh in  ~/.bashrc --------------------
     && echo alias docker='podman' >> ${USER_HOME}/.bashrc \
     && echo -e '\n. ${ASDF_SH}' >> ~/.bashrc \
     && echo -e '\n. ${ASDF_SH}' >> ~/.profile \
     && source ${USER_HOME}/.bashrc \
     && mkdir -p "${ASDF_DATA_DIR}"

