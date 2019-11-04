# Spring Boot Cloud Native Workshop

This workshop is designed to help spring developers get familiar with the important concepts invovled with building, deploying, testing Spring Boot applications on a cloud platform, in this case IBM Cloud, using Kubernetes. This workshop closely follows the content of the article series [Living on the Cloud]((https://developer.ibm.com/series/living-on-the-cloud/)). 

Each exercise in this project is its own branch.

## Prerequisties 

This workshop requires the followign for completion: 

* [Java 8+](https://adoptopenjdk.net/)
* [Docker](https://www.docker.com/)
* [An IBM Cloud account](https://ibm.biz/BdzCAu)
* [IBM Cloud CLI](https://github.com/IBM-Cloud/ibm-cloud-cli-release/releases/)
* An IDE of your choice
* [git](https://git-scm.com/)
* Clone this repo:

	```
	https://github.com/wkorando/spring-boot-cloud-native-workshop.git
	```

### Initialize a Kubernetes Cluster

Initializing a Kubernetes cluster takes ~30 minutes. It is highly encouraged to start this process as early as possible: 

1. Log in to your [IBM Cloud account](http://cloud.ibm.com/). 

1. In the top center of the page search for **Kubernetes Cluster** and select it.

1. Create a **Free Kubernetes cluster**. To initialize a Lite cluster, you will need to upgrade your IBM Cloud account, if you have not already.

## Deploy Spring Boot to Kubernetes 

In the first exercise we will quickly deploy a Spring Boot application to a Kubernetes Cluster: [Code](https://github.com/wkorando/spring-boot-cloud-native-workshop/tree/1-deploying-spring-boot)

## Connecting to a Database

In the second exericse we will connect the Spring Boot application we built in the first exercise to a database: [Code](https://github.com/wkorando/spring-boot-cloud-native-workshop/tree/2-connecting-to-a-database)

## Cloud Native Integration Teting

In the second exericse we will use TestContainers and Spring Cloud Contract to handle test the Integration Testing of our Spring Boot application so that it is reliable and portable: [Code](https://github.com/wkorando/spring-boot-cloud-native-workshop/tree/3-cloud-native-integration-testing)