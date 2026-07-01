# use the official Tomcat image with Java 17
FROM tomcat:10.1-jdk21

# delete the default Tomcat dummy apps to keep it clean
RUN rm -rf /usr/local/tomcat/webapps/*

# create the directories for the resources and uploads
RUN mkdir -p /usr/local/tomcat/app-data/resources
RUN mkdir -p /usr/local/tomcat/app-data/uploads

# copy the compiled WAR file into the Tomcat webapps directory
# (make sure the ROOT.war is in the same folder as this Dockerfile)
COPY ROOT.war /usr/local/tomcat/webapps/

# copy the initial CSV resource files into the container so the InitDB listener can find them
COPY ./resources/ /usr/local/tomcat/app-data/resources/

# expose port 8080
EXPOSE 8080

# start Tomcat
CMD ["catalina.sh", "run"]