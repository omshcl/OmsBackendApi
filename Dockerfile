# uses archlinux for prebuilt pandas binaries
FROM archlinux/base
RUN pacman -Syyu --noconfirm && pacman -S --noconfirm python python-pip python-pandas jdk8-openjdk maven python-requests  && pip install cassandra-driver
COPY . /root/
ENTRYPOINT ["/root/setup.sh"]
