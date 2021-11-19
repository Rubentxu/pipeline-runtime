FROM registry.access.redhat.com/ubi8/ubi

ARG THE_USER=podman
ARG USER_HOME=/home/${THE_USER}
ARG ASDF_BASE_FOLDER=${USER_HOME}/.asdf
ARG ASDF_SH=${ASDF_BASE_FOLDER}/asdf.sh
ARG ASDF_BRANCH=v0.8.1
ARG JAVA_VERSION=openjdk-11
ARG MAVEN_VERSION=3.6.1
ARG GRADLE_VERSION=6.7.1
ARG NODEJS_VERSION=12.16.3
ARG HELM_VERSION=2.16.3
ENV JAVA_HOME="/home/${THE_USER}/.asdf/installs/java/${JAVA_VERSION}"
ENV PATH="${PATH}:/asdf/.asdf/shims:/asdf/.asdf/bin:${JAVA_HOME}/bin"
ENV _CONTAINERS_USERNS_CONFIGURED=""

RUN dnf -y update; rpm --restore shadow-utils 2>/dev/null; \
    yum -y install crun podman fuse-overlayfs unzip curl git tar findutils --exclude container-selinux; \
    rm -rf /var/cache /var/log/dnf* /var/log/yum.* \
    && useradd ${THE_USER} \
    && echo ${THE_USER}:10000:5000 > /etc/subuid \
    && echo ${THE_USER}:10000:5000 > /etc/subgid;

VOLUME /var/lib/containers
VOLUME /home/${THE_USER}/.local/share/containers
RUN mkdir -p /home/${THE_USER}/.local/share/containers \
    && chown ${THE_USER}:${THE_USER} -R /home/${THE_USER} \
    && chmod 644 /etc/containers/containers.conf; sed -i -e 's|^#mount_program|mount_program|g' -e '/additionalimage.*/a "/var/lib/shared",' -e 's|^mountopt[[:space:]]*=.*$|mountopt = "nodev,fsync=0"|g' /etc/containers/storage.conf \
    && mkdir -p /var/lib/shared/overlay-images /var/lib/shared/overlay-layers /var/lib/shared/vfs-images /var/lib/shared/vfs-layers; touch /var/lib/shared/overlay-images/images.lock; touch /var/lib/shared/overlay-layers/layers.lock; touch /var/lib/shared/vfs-images/images.lock; touch /var/lib/shared/vfs-layers/layers.lock


ADD containers.conf /etc/containers/containers.conf
ADD podman-containers.conf /home/${THE_USER}/.config/containers/containers.conf

WORKDIR $USER_HOME
USER $THE_USER

RUN  \
     # ---- Install asdf -------------------------------------------------------
     git clone --depth 1 https://github.com/asdf-vm/asdf.git $ASDF_BASE_FOLDER --branch $ASDF_BRANCH \
     && rm -fr $ASDF_BASE_FOLDER/.git $ASDF_BASE_FOLDER/SECURITY.md $ASDF_BASE_FOLDER/Vagrantfile $ASDF_BASE_FOLDER/VERSION \
     $ASDF_BASE_FOLDER/LICENSE $ASDF_BASE_FOLDER/help.txt $ASDF_BASE_FOLDER/.gitignore $ASDF_BASE_FOLDER/.github/ \
     $ASDF_BASE_FOLDER/.editorconfig $ASDF_BASE_FOLDER/CONTRIBUTING.md $ASDF_BASE_FOLDER/ballad-of-asdf.md $ASDF_BASE_FOLDER/docs $ASDF_BASE_FOLDER/test/ \
     # ---- Add docker alias and call asdf.sh in  ~/.bashrc --------------------
     && echo alias docker='podman' >> $USER_HOME/.bashrc \
     && echo . $ASDF_SH >> $USER_HOME/.bashrc \
     # ---- Source ~/.bashrc ---------------------------------------------------
     && . $USER_HOME/.bashrc \
     # ---- Install Nodejs -----------------------------------------------------
      && asdf plugin add nodejs \
      && asdf install nodejs $NODEJS_VERSION \
      && asdf global nodejs $NODEJS_VERSION \
      # ---- Install Java -----------------------------------------------------
      && asdf plugin add java \
      && asdf install java $JAVA_VERSION \
      && asdf global java $JAVA_VERSION \
      # ---- Install Maven -----------------------------------------------------
      && asdf plugin add maven \
      && asdf install maven $MAVEN_VERSION \
      && asdf global maven $MAVEN_VERSION \
      # ---- Install Gradle -----------------------------------------------------
      && asdf plugin add gradle \
      && asdf install gradle $GRADLE_VERSION \
      && asdf global gradle $GRADLE_VERSION \
      # ---- Install Helm -----------------------------------------------------
      && asdf plugin add helm \
      && asdf install helm $HELM_VERSION \
      && asdf global  helm $HELM_VERSION
#      && asdf reshim \
#      && node -v \
#      && mvn -version \
#      && asdf current
