# NetKernel PetClinic
https://github.com/1060NetKernel/netkernel-petclinic

A re-implementation of spring-petclinic using NetKernel.

# Project management
https://trello.com/b/eAEPEJrw/netkernel-petclinic

# Collaboration room
https://appear.in/netkernel-petclinic

# Discussion forum
http://www.1060.org/nk4um/forum/16/

# Setup local development environment
See Trello card https://trello.com/c/YMHuM4sK

# Prerequisites
  - JDK 1.8 or later
  - maven 3.x or later
  - docker
  - DB client tool, DbVisualizer is a good choice
  - NetKernel SE

# Prepare local environment for development
Quick summary - see card https://trello.com/c/YMHuM4sK

NOTE: To stay organized, we recommend to create a top level folder nk-workspace and create project folders underneath.

# Install NetKernel
	- http://download.netkernel.org/nkse/
	- download NK 6.2.1
	- install NKSE
	  - path/to/nkse> java -jar 1060-NetKernel-SE-6.2.1.jar
	  - go to http://localhost:1060/installer/ to install NKSE to disk
	  - accept terms (TAC)
	  - enter full path to install location, path/to/nkse aforementioned
	  - keep Expand JAR files checked
	  - click Install
	  - when you see "NetKernel was successfully installed onto your filesystem at C:\tools\nkse." proceed with on-screen next steps, like shutdown...
	  - go back to your terminal where command prompt waits for you
	  - do "ls -la" to see the NKSE folder structure
	  - on Windows, run  bin\netkernel.bat
	  - on Unix-like OS, run bin/netkernel.sh
	  - wait for NK to boot, till you see "NetKernel Ready, accepting requests..."
	  - now you can interact with NK:
		- on backend fulcrum - http://localhost:1060
		- on frontend fulcrum - http://localhost:8080

# Housekeeping
Delete your local NK PetClinic repo
  - delete the entire folder netkernel-petclinic folder

Clone repo fork again
  - git clone https://github.com/mcocosila/netkernel-petclinic.git

# Create IntelliJ IDEA project for NetKernel PetClinic
  - Create New Project > Empty Project > Next
  - configure SDK at project level, modules will inherit from that
  - in Project location, select C:\data\nk-workspace\netkernel-petclinic from 
    - Project name is auto populated with netkernel-petclinic, keep it like that
    - Project format: .idea
	- OK and Project Structure dialog opens in the Modules section
	- cancel Project Structure dialog
  - for each module in the project, do:
    - File > New > Module... > Java ... > Finish

# Run dockerized PostgreSQL
  - docker run --name postgres-petclinic -e POSTGRES_PASSWORD=petclinic -e POSTGRES_DB=petclinic -p 5432:5432 -d postgres:9.6.0
	Helper docker commands:
	- docker image ls -a
	- docker container ls -a
	- docker ps
	- docker rm /postgres-petclinic
	- docker stop /postgres-petclinic
	- docker start /postgres-petclinic

Database URL: jdbc:postgresql://192.168.99.100:5432/petclinic
NB: IP in the link above is the result of running command "docker-machine ip"
For more details, see screenshots in card https://trello.com/c/SlGrYQQI

# Running Spring PetClinic REST API locally
  - cd path/to/nk-workspace
  - git clone https://github.com/spring-petclinic/spring-petclinic-rest.git
  - cd spring-petclinic-rest
  - ./mvnw spring-boot:run
  
# Swagger documentation
  - http://localhost:9966/petclinic/swagger-ui.html
  
# Connect Angular Ui
  - cd path/to/nk-workspace
  - git clone https://github.com/spring-petclinic/spring-petclinic-angular.git
  - cd spring-petclinic-angular
  - ng serve
  NOTE: If errors:
		- try:
		spring-petclinic-angular>ng update @angular/cli --migrate-only --from=1.7.4
		- for more details, see card https://trello.com/c/JnAiaDX8
  - open browser on http://localhost:4200

# Technical architecture
  - See card https://trello.com/c/Lqbnv6K5
  
