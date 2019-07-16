# uses alpine linux to build a lightweight container
FROM archlinux/base
RUN pacman -Syyu --noconfirm && pacman -S --noconfirm python python-pip python-pandas jdk8-openjdk maven python-requests  && pip install cassandra-driver
# install cassandra and java runtime
COPY . /root/
ENTRYPOINT ["/root/setup.sh"]
