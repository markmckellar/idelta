# smap
a simple light weight server to simplify service mappings

to build :
* gradle

to make war :
* gradle war

A test WebApp folder can be found at : /smap/src/org/smaptest/server/web

Once ran it can be accessed at : http://127.0.0.1:8080/smap/html/test.html


The entry point in the example can be viewed at : https://github.com/markmckellar/smap/blob/master/src/main/java/org/smaptest/server/service/SMapTest.java


A docker command to test : sudo docker run -v <path-to-war-file>/smap_work.war:/usr/local/tomcat/webapps/smap.war -it -p 8080:8080 tomcat
"# idelta" 
"# idelta" 
