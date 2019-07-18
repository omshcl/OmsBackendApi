# uses archlinux for prebuilt pandas binaries
FROM archlinux/base
RUN pacman -Syyu --noconfirm && pacman -S --noconfirm python python-pip python-pandas jdk8-openjdk maven python-requests  && pip install cassandra-driver
#WORKDIR "/root"
#RUN ls
#RUN export JAVA_HOME=/usr/lib/jvm/default && mvn tomcat7:run
# install cassandra and java runtime

#COPY . /root/
ENTRYPOINT ["/bin/bash"]
