ARG IMAGE_BASE=quay.io/podman/stable
ARG IMAGE_BASE_TAG=latest


#FROM docker.artifactory.gcp.mercadona.com/${IMAGE_BASE}:${IMAGE_BASE_TAG} AS stage1
FROM ${IMAGE_BASE}:${IMAGE_BASE_TAG} AS stage1

ARG THE_USER=podman
ARG USER_HOME=/home/$THE_USER
ARG ASDF_BASE_FOLDER=$USER_HOME/.asdf
ARG ASDF_SH=$ASDF_BASE_FOLDER/asdf.sh
ARG ASDF_BRANCH=v0.8.1
ARG JAVA_VERSION=openjdk-11.0.2
ARG MAVEN_VERSION=3.6.1
ARG GRADLE_VERSION=6.7.1
ARG NODEJS_VERSION=12.16.3
ARG HELM_VERSION=2.16.3
ENV JAVA_HOME="/home/$THE_USER/.asdf/installs/java/$JAVA_VERSION"
ENV PATH="${PATH}:/asdf/.asdf/shims:/asdf/.asdf/bin:${JAVA_HOME}/bin"

RUN dnf install -y unzip curl git tar findutils
USER $THE_USER

RUN \
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
      && node -v \
      # ---- Install Java -----------------------------------------------------
      && asdf plugin add java \
      && asdf install java $JAVA_VERSION \
      && asdf global java $JAVA_VERSION \
      && asdf current \
      # ---- Install Maven -----------------------------------------------------
      && asdf plugin add maven \
      && asdf install maven $MAVEN_VERSION \
      && asdf global maven $MAVEN_VERSION \
      && mvn -version \
      # ---- Install Gradle -----------------------------------------------------
      && asdf plugin add gradle \
      && asdf install gradle $GRADLE_VERSION \
      && asdf global gradle $GRADLE_VERSION \
      && gradle -version \
      # ---- Install Helm -----------------------------------------------------
      && asdf plugin add helm \
      && asdf install helm $HELM_VERSION \
      && asdf global  helm $HELM_VERSION \

#      && asdf reshim \
      && asdf current



#FROM docker.artifactory.gcp.mercadona.com/${IMAGE_BASE}:${IMAGE_BASE_TAG}
FROM ${IMAGE_BASE}:${IMAGE_BASE_TAG}

ARG THE_USER=podman
ARG USER_HOME=/home/$THE_USER
ARG ASDF_BASE_FOLDER=$USER_HOME/.asdf
ARG ASDF_SH=$ASDF_BASE_FOLDER/asdf.sh
ARG ASDF_BRANCH=v0.8.1
ARG JAVA_VERSION=openjdk-11.0.2
ARG MAVEN_VERSION=3.6.1
ARG GRADLE_VERSION=6.7.1
ARG NODEJS_VERSION=12.16.3
ARG HELM_VERSION=2.16.3
ENV JAVA_HOME="/home/$THE_USER/.asdf/installs/java/$JAVA_VERSION"
ENV PATH="${PATH}:/asdf/.asdf/shims:/asdf/.asdf/bin:${JAVA_HOME}/bin"

RUN dnf install -y unzip curl git findutils

COPY --from=stage1 --chown=podman:root $ASDF_BASE_FOLDER  $ASDF_BASE_FOLDER
COPY --from=stage1 --chown=podman:root $USER_HOME/.bashrc $USER_HOME/.tool-versions $USER_HOME/


WORKDIR $USER_HOME
#RUN update-ca-trust force-enable && update-ca-trust extract

USER $THE_USER
