sudo update-ca-certificates
sudo chown -R prunner:prunner .config/
sudo mount --make-rshared /
git config --global http.sslVerify false
asdf plugin add java || true
asdf install java adoptopenjdk-11.0.13+8 || true
asdf global java adoptopenjdk-11.0.13+8 || true