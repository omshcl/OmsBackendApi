# db2 connection
In db2 command line processor
 
    CREATE DATABASE OMDB
    CONNECT TO OMDB

    CREATE BUFFERPOOL ICMLBUFFER32 PAGESIZE 32k  
    CREATE BUFFERPOOL ICMLBUFFER16 PAGESIZE 16k  
    CREATE BUFFERPOOL ICMLBUFFER8 PAGESIZE 8k  
    CREATE BUFFERPOOL ICMLBUFFER4 PAGESIZE 4k  

    CREATE SYSTEM TEMPORARY TABLESPACE ICMLSSYSTSPACE32 PAGESIZE 32k BUFFERPOOL ICMLBUFFER32  
    CREATE SYSTEM TEMPORARY TABLESPACE ICMLSSYSTSPACE16 PAGESIZE 16k BUFFERPOOL ICMLBUFFER16  
    CREATE SYSTEM TEMPORARY TABLESPACE ICMLSSYSTSPACE8  PAGESIZE  8k BUFFERPOOL ICMLBUFFER8  
    CREATE SYSTEM TEMPORARY TABLESPACE ICMLSSYSTSPACE4  PAGESIZE  4k BUFFERPOOL ICMLBUFFER4  

# installation arguments

Database Vendor

    DB2
Database user name

    alice

Database password
 
    acoount password


Database Catalog name

    OMDB


Hostname

    localhost

port number

    50000

schema name

    ALICE

jar

    add the jdbc db2 driver jar

Additional ant arguments
 
    -XX:MaxPermSize=768m
    
    -J-Xms1408m -J-Xmx1752m

# Web Sphere

C:\Users\alice\IBM\WebSphere\AppServer\bin\wsadimin.bat

Change

    set PERFJAVAOPTION=-Xms256m -Xmx356m -Xquickstart
to

    set PERFJAVAOPTION=-Xms1024m -Xmx2048m -Xquickstart