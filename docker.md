# Docker Documentation

The oms docker supports the running of the full backend. It contains an apache cassandra database that will be filled with order and item data. When the docker container is built it copies the OmsBackendApi java code into it, this ensures that it is always running the latest version of the backend api. 

# Usage
Building the container in the OmsBackendApi directory

    docker build -t oms . 
Running the container

    docker run -it 
    

# Files
- Dockerfile     The file that specifies how the container should be built
- setup.sh       A script that starts the Cassandra Database and the python populate service
- populate.py    A python script that populates the database tables and launches the tomcat server
- setup.txt      File that specifies initial table schema 