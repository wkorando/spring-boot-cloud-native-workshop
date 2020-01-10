# Spring Boot Cloud Native Workshop

The Spring Boot Cloud Native Workshop is designed to introduce Spring Developers to the Kubernetes platform and Cloud Native development. If your organization is thinking about, or in the process of, making the move to a Cloud Platform built on Kubernetes, this workshop will give you hands on experience of working with of the most important and fundamental Kubernetes features and concepts.

For additional reading on on Cloud Native development, be sure to check out the [Living on the Cloud](https://developer.ibm.com/series/living-on-the-cloud/) blog series on IBM Developer.  

## What You Will Learn

In this workshop you will learn about the following concepts: 

* Working with Kubernetes
* Building Delivery Pipelines
* Writing Reliable Automated Tests
* Connecting to Cloud Services

## Table of Contents

### 1. [Setup](#setup)
### 2. [Deploying a Spring Boot Application to a Kubernetes Cluster](#deploying-spring-boot-to-a-kubernetes-cluster)
### 3. [Connecting a Spring Boot Application Spring Boot to a Cloud Hosted Database](#connecting-spring-boot-to-a-cloud-hosted-database)
### 4. [Cloud Native Integration Testing](#cloud-native-integration-testing)

## Setup 

As this workshop will be completed using your own system and will involve building and creating several artifacts, some configuration is required in order to successfully complete this workshop. This section will walk you through these steps. 

### Installation Prerequisites 

This workshop requires several tools to be available on your system before beginning:

* [Java 8+](https://adoptopenjdk.net/)
* [Docker](https://www.docker.com/get-started)
* You will need an IDE or an advanced text editor like [Notepad++](https://notepad-plus-plus.org/) or [TextMate](https://macromates.com/)
* [git](https://git-scm.com/downloads)
* [kubectl](https://kubernetes.io/docs/tasks/tools/install-kubectl/)

### Windows Users

<details>
<summary>Click to Expand</summary>
This workshop makes heavy use of terminal commands. The terminal command examples in this workshop are use *nix idioms. For that reason it is highly encouraged to either use [Cygwin](https://www.cygwin.com/) or [install/enable the Linux Bash shell](https://www.windowscentral.com/how-install-bash-shell-command-line-windows-10) that was added to Windows 10.  
</details>

### IBM Cloud Account Setup

During this workshop we will deploy a live application to IBM Cloud and will be making use of services and tools hosted on IBM Cloud. This workshop also requires an "upgraded" IBM Cloud account. This section will walk you through creating an IBM Cloud account and upgrading it:

1. [Create an IBM Cloud account by filling out this form](https://ibm.biz/BdzJmC)
2. Once you have completed the sign up process and are signed into your IBM Cloud account on https://cloud.ibm.com expand the **Manage** menu and select **Account**

	![](images/promo-code-1.png)
3. Once on the Account page select **Account settings** on the left hand of the page, and then click the **Apply Code** button: 	

	![](images/promo-code-2.png)
	
4. Use the provided url to enter a promo code for you account. 	

### Configure IBM Cloud CLI

IBM provides the powerful IBM Cloud Command Line Interface (CLI) for interaction with IBM Cloud. In this workshop we will be making heavy use out of the IBM Cloud CLI to carry out commands. We will need to walk through a few steps however to make sure the CLI is configured correctly for this workshop.

1. Download the [IBM Cloud CLI](https://github.com/IBM-Cloud/ibm-cloud-cli-release/releases/)
2. Once the installation process has completed, open a bash friendly terminal window
	
	The IBM Cloud CLI uses a modular design. Functionality for handling many IBM Cloud services is located within these plugins. We will need to install a two plugins for this workshop.
	
3. Install the **Container Registry** plugin:

   ```
   ibmcloud plugin install container-registry
   ```
4. Install the **Container Service** plugin:
	
	```
	ibmcloud plugin install container-service
	```

5. Login into IBM Cloud CLI with the following:

	```
	ibmcloud login 
	```
   **Note:** If you are using a federated IBM Cloud account [follow these steps](https://cloud.ibm.com/docs/iam?topic=iam-federated_id#federated_id).

5. Run this command to set org and space
	
	```
	ibmcloud target --cf
	```

6. Run this command to set group
	
	```
	ibmcloud target -g Default
	```

5. View the current target region with the following:

	```
	ibmcloud target
	```
	You should get output that looks something like this:
	
	```
	API endpoint:      https://cloud.ibm.com   
	Region:            us-south   
	User:              myemail@mail.com   
	Account:           My Account Name   
	Resource group:    Default   
	CF API endpoint:   https://api.ng.bluemix.net (API version: 2.141.0)   
	Org:               myemail@mail.com   
	Space:             dev   
	```
	If the region in your output is `us-south` and `org` and `space` have provided values, move on to [Initializing a Kubernetes Cluster](#initializing-a-kubernetes-cluster). If you are in a different region or `org` and `space` are blank expand one of the below sections.
	
	
	### Wrong Region
	<details>
	<summary>Expand here to view change region instructions</summary>
	To aid in the following of this, it is best to set your region to `us-south` (Dallas). The free-tier of some services are not available in every region and by using `us-south` the the settings and console output will more closely match the examples given in the guide. The below steps will walk you through this process:
	
	1. Change target region to `us-south`:
	
	```
	ibmcloud target -r us-south
	```
	
	2. IBM Cloud has the "organizations" concept which provides a means for collaboration, logical grouping of applications, and is a security region. From the output of `ibmcloud target` replace the values `YOUR_ORG` and `INITIAL_REGION` with the values of `Org:` and `Region:` respectively.

	```
	ibmcloud account org-replicate YOUR_ORG us-south -r INITIAL_REGION
	```
	
	3. IBM Cloud also has the concept of "spaces" which are sub-group to "organizations", which like "organization" can be used for collaboration, logical group, and act as a security region. We will want to create the `dev` region in our newly replicated `org` with the following command:

	```
	ibmcloud account space-create dev -o YOUR_ORG
	```
	
	4. Finally we will want IBM Cloud CLI to target the org and space we just created as we are going through this workshop this can be done with this command:

	```
	ibmcloud target -o YOUR_ORG -s dev
	```
	
	You should get output that looks like this: 
	
	```
	API endpoint:      https://cloud.ibm.com   
	Region:            us-south   
	User:              myemail@mail.com   
	Account:           My Account Name   
	Resource group:    Default   
	CF API endpoint:   https://api.ng.bluemix.net (API version: 2.141.0)    
	Org:               YOUR_ORG   
	Space:             dev   
	```
	
	If the region is `us-south` and org and space are populated you can move on to [Initializing a Kubernetes Cluster](#initializing-a-kubernetes-cluster). If the region still isn't right or space or org are not populated, verify you run all the above steps or get the attention of someone helping run the workshop.
	</details>
	
	### Blank Org and Space
	<details>
	<summary>Expand here to view blank org and target instructions</summary>
	
	The 'Org' and 'Space' fields not being populated can happen because the region IBM Cloud chose when you created your IBM Cloud account is different from the region IBM Cloud CLI chose when you signed in. Trying changing the target region to a geographically nearby region with the following command:
	
	```
	ibmcloud target -r REGION
	```
	Here is a list of all IBM Cloud regions: 
	
	```
	Name       Display name   
	au-syd     Sydney   
	jp-tok     Tokyo   
	eu-de      Frankfurt   
	eu-gb      London   
	us-south   Dallas   
	us-east    Washington DC 
	``` 
	
	If in the output `Org:` and `Space:` are not populated, trying switching to another region. If `Org:` and `Space:` are populated, but not in `us-south` [check out these instructions](#change-region). If you are still running into issues be sure to ask for help from someone helping run the workshop. 
	</details>

### Initializing a Kubernetes Cluster

We will be deploying and modifying a Spring Boot application on a Kubernetes cluster. A free-tier cluster is available on IBM Cloud to give developers an opportunity to get familiar with working with Kuberentes on a cloud platform. Initializing a Kubernetes cluster takes about ~30 minutes, so let's start the process now and let it run in the background as we work through the next steps. To initialize a free cluster run the following command:  

```
ibmcloud ks cluster create --name spring-boot-workshop --zone dal10
```

## Deploying Spring Boot to a Kubernetes Cluster

In this section we will walk through the steps of configuring our Cloud Platform, building a pipeline, and deploying a Spring Boot application to the Kubernetes cluster we have just created.

### Create a Container Registry

In Kubernetes every application is running in a container. In this workshop we are using Docker as our container implementation, but Kubernetes supports other container types. Kubernetes needs a container registy to pull from that contains the container images we will be telling it to run (more on this in a moment).

IBM Cloud provides a container registry service, we will use it to store the Docker images we will be building in this workshop. Let's configure the container registry now: 

1. Run the following command to ensure the `container-registry` plugin has been installed successsfully: 

   ```
   ibmcloud cr info
   ```

   You should get output that looks like this:

   ```
   Container Registry                us.icr.io
   Container Registry API endpoint   https://us.icr.io/api
   IBM Cloud API endpoint            https://cloud.ibm.com
   IBM Cloud account details         <account details>
   IBM Cloud organization details    <organization details>
   ```

   **Note:** If you had previously installed the container registry and you have container registry URLs that include the word "bluemix," learn how to [update your API endpoints](https://cloud.ibm.com/docs/services/Registry?topic=registry-registry_overview#registry_regions_local).


1. Create a namespace for your container registry, the name doesn't matter, but use something unique and memborable as we will be using that value later. 

   To create a namespace run the following command:

   ```
   ibmcloud cr namespace-add [your namespace]
   ```

   You should get the following response:

   ```
   Adding namespace '[your namespace]'...

   Successfully added namespace '[your namespace]'
   ```

### Setting Up a Deployment Pipeline

Automating the process of building and deploying an application is becoming the norm in the software industry, and is an essential element of Cloud Native development. An automated deployment pipeline reduces friction, the amount of work, and makes the deployment process much more consistent and auditable. In this section we will create a deployment pipeline for the Spring Boot APplication we will be building in this workshop. 

1. Right-click and open a new tab the **Deploy to IBM Cloud** button below:

	[![Deploy to IBM Cloud](https://cloud.ibm.com/devops/setup/deploy/button.png)](https://cloud.ibm.com/devops/setup/deploy?repository=https://github.com/wkorando/spring-boot-cloud-native-workshop&branch=master&env_id=ibm:yp:us-south)
	
	---
	**Tip:** This deployment pipeline is being configured based on the template defined in the files under [.bluemix](/.bluemix). Because of time constraints, we won't cover the details of how these files work, but if you are interested you can [read more here](https://github.com/open-toolchain/).
		
	---
	
2. For the Lite account users, make sure the region (1) is set to Dallas (us-south). Next, click (2) to configure the Delivery Pipeline.

	![](./images/pipeline-1.png)

3. Next create an API Key for this repo by clicking **Delivery Pipeline** and then the **Create** button. The default values that are generated should be fine.

	![](images/create_api_key.png)

4. Cick the **Create** button in the top right corner of the page.

	IBM Cloud will now take a few moments to initialize this toolchain. 
	
	
### Configuring Git

As a part of the process of creating the toolchain IBM Cloud has automatically cloned this workshop repository into a private repository hosted on IBM Cloud. You can configure a toolchain to pull from an existing repository, but this method works best for this workshop. Let's walk through the steps to configure git so we can read and write to this repository.  
	
1. After the toolchain has finished initializing you should have a page that looks like this. Click on the Git icon:

	![](images/open-git.png)
	
2. From the main git repo page click the avatar in the top right hand corner and from the drop down menu select settings.

	![](images/git-token-1.png)	
	
3. On the settings page click on "Access Tokens", on that page give a meaningful name in the name dialog, select some future date for the token to expire, and be ssure to check all the boxes. Once down click "create personal access token"

	![](images/git-token-2.png)	

4. Click projects in the top left hand corner of the page and search for `spring-boot`. CTRL + click on the returned email.

	![](images/git-token-4.png)	

	
5. On the main repo page in the top right hand corner click the blue **Clone** button and copy the **https git repo url**
6. In your terminal window, navigate to a folder where you'd like to have this repo located. Once there run the following command:

	```
	git clone GIT_REPO_URL
	```
	
	You will be prompted for login information. The **username** is you IBM Cloud account and the **password** will be the the token we just created. 
	
	![](images/git-token-3.png)	

### Configuration as Code

Configuration as Code is a practice of co-locating configuration information about how an application should be executed in the same location where the code for the application is stored. This practices helps developers understand an application better by keeping all the important information about an application in a single area. This practice is also essential for building deployment pipelines as the pipelines will read this configuration information when deploying the application. Let's look at the configuration information in this project.

#### Dockerfile

As mentioned earlier every application running in Kubernetes is running in a container. Docker users Dockerfiles to describe how a container image should be built. The Dockerfile in this project looks like this:

```
FROM adoptopenjdk/openjdk8-openj9:alpine-slim

COPY target/storm-tracker.jar /

ENTRYPOINT ["java", "-jar", "storm-tracker.jar" ]
```

Let's step through what all these elements mean.

`FROM adoptopenjdk/openjdk8-openj9:alpine-slim`: This is describing which image to build this docker image off of. In this example we are building off a image provided by adoptopenjdk that contains the OpenJ9 JVM running Java 8. `alpine-slim` is a minimal Linux distro. 

`COPY target/storm-tracker.jar /`: This is copying the Java artifact we will be building into the docker image.

`ENTRYPOINT ["java", "-jar", "storm-tracker.jar" ]`: This is the command the image will run when it is started up. 

#### Deployment.yml

`deployment.yml` files are used by Kubernetes to describe how an application should be deployed on a Kubernetes cluster. A deployment file is already present, below is the fully contents, let's take a closer look at its two major components.

```
apiVersion: extensions/v1beta1
kind: Deployment
metadata:
  labels:
    run: storm-tracker
  name: storm-tracker
  namespace: default
spec:
  replicas: 1
  revisionHistoryLimit: 3
  selector:
    matchLabels:
      run: storm-tracker
  template:
    metadata:
      labels:
        run: storm-tracker
    spec:
      containers:
      - image: ${REGISTRY_URL}/${REGISTRY_NAMESPACE}/${IMAGE_NAME}:${IMAGE_TAG}
        name: storm-tracker
      terminationGracePeriodSeconds: 30
---
kind: Service
apiVersion: v1
metadata:
  labels:
    run: storm-tracker
  name: storm-tracker-port
  namespace: default
spec:
  selector:
    run: storm-tracker
  ports:
    - port: 8080
      name: http
  type: NodePort
```

##### Deploying an Application

```
apiVersion: extensions/v1beta1
kind: Deployment
metadata:
  labels:
    run: storm-tracker
  name: storm-tracker
  namespace: default
spec:
  replicas: 1
  revisionHistoryLimit: 3
  selector:
    matchLabels:
      run: storm-tracker
  template:
    metadata:
      labels:
        run: storm-tracker
    spec:
      containers:
      - image: ${REGISTRY_URL}/${REGISTRY_NAMESPACE}/${IMAGE_NAME}:${IMAGE_TAG}
        name: storm-tracker
      terminationGracePeriodSeconds: 30
```

This section describes how an application should be deployed on the Kubernetes cluster. A [Deployment](https://kubernetes.io/docs/concepts/workloads/controllers/deployment/) describes how a [Pod](https://kubernetes.io/docs/concepts/workloads/pods/pod/) should exist.

```
metadata:
  labels:
    run: storm-tracker
  name: storm-tracker
  namespace: default
```

This section is describing where a the deployment should exist and how it should be presented within the Kubernetes cluster (e.g. how it might be looked up by other Pods and Services within Kubernetes)

```
spec:
  replicas: 1
  revisionHistoryLimit: 3
  selector:
    matchLabels:
      run: storm-tracker
```

This section defines how many instances of this pod Kubernetes should maintain by default as well as how many previous revisions of a deployment Kubernetes should hold onto for rollback purposes. For scaling and reliability this is a very important area to understand for Kubernetes.

```
  template:
    metadata:
      labels:
        run: storm-tracker
    spec:
      containers:
      - image: ${REGISTRY_URL}/${REGISTRY_NAMESPACE}/${IMAGE_NAME}:${IMAGE_TAG}
        name: storm-tracker
      terminationGracePeriodSeconds: 30
```

Here we are defining the actual application we will be running. 

* `image` providing the coordinates in a container registry as to which image to run. 
* `terminationGracePeriodSeconds:` tells Kubernetes how long to wait before terminating a Pod that hasn't reached a healthy state.

##### Exposing an Application

```
kind: Service
apiVersion: v1
metadata:
  labels:
    run: storm-tracker
  name: storm-tracker-port
  namespace: default
spec:
  selector:
    run: storm-tracker
  ports:
    - port: 8080
      name: http
  type: NodePort
```

Kubernetes uses internal networking to allow pods, services, loadbalancers, etc. to communicate with  each other securely. However to have business value something needs to be exposed to the outside world. In the above section of YAML we are creating a [Service](https://kubernetes.io/docs/concepts/services-networking/service/), in this case a [NodePort](https://kubernetes.io/docs/concepts/services-networking/service/#nodeport) to expose the `storm-tracker` application. This association is made with the `selector` text:

```
spec:
  selector:
    run: storm-tracker
```

By default Spring Boot runs on port `8080`, here we are telling the NodePort to direct external traffic through that port. We are also setting the protocol to use `http`. 

```
  ports:
    - port: 8080
      name: http
  type: NodePort
```  

This is an extremely simplified example of how to deploy an application on Kubernetes. We will add more to this deployment file in the workshop, but other concerns not shown in this example would be how to handle rollouts of new version, how to test the health of a pod, security configuration, and many other areas!

### Small But Bootiful

An initial Spring Boot application is already located within the repo we just cloned down. We will be making change to this application through out this workshop, in thiss section we will do the first step every programmer has done, create a "Hello World" application. 

1. Under `src/main/java/com/ibm/developer/stormtracker/` create a new file `StormTrackerController.java` and copy the following into that file: 

	```
	package com.ibm.developer.stormtracker;

	import org.springframework.http.ResponseEntity;
	import org.springframework.web.bind.annotation.GetMapping;
	import org.springframework.web.bind.annotation.RequestMapping;
	import org.springframework.web.bind.annotation.RestController;
	
	@RestController
	@RequestMapping("/api/v1/storms")
	public class StormTrackerController {
	
		@GetMapping
		public ResponseEntity<String> helloWorld(){
			return ResponseEntity.ok("Hello World");
		}
	}
	```

2. Save the file, but **DO NOT** push these changes yet to your repo!

### Deploy to Kubernetes

With some initial knowledge of how Kubernetes works gained, let's see it in action!

1. The Kubernetes cluster should hopefully be initialized by now. To verify this run the following command:

	```
	ibmcloud ks cluster ls
	```
	In the output, under State if it shows as `normal`, you are good to continue the next step in the workshop. If `pending` is still showing wait a few moments and run the command again. If a different state is showing, then seek assistance from someone running the workshop.
	
	```
	OK
Name                   ID                     State     Created          Workers   Location   Version       Resource Group Name   Provider   
spring-boot-workshop   bn41o1pd0eec78gq93rg   running   11 minutes ago   1         hou02      1.14.8_1536   Default               classic  
	```
	
2. Commit and push your changes to your repo:

	```
	git add .
	git commit -m "Added hello world url"
	git push
	```

3. Committing changes to the repo will starts the deployment pipeline process, to view its progress [click this link](https://cloud.ibm.com/devops/toolchains?env_id=ibm%3Ayp%3Aus-south) and clicking on the Delivery Pipeline icon:

	![](images/view-pipeline-1.png)

4. Once every step in the build pipeline is complete continue on to the next section (this may take a couple of minutes)

	![](images/view-pipeline-2.png)

#### Configure Kubectl

While the pipeline process runs, let's configure kubectl so that we can admin the Kubernetes cluster. 

1. Set the region for the ks plugin to `us-south` with the following command:

   ```
   ibmcloud ks region set us-south
   ```

1. Download the configuration information for your cluster:

   ```
   ibmcloud ks cluster config spring-boot-workshop
   ```	

   The output response from this command should look something like this:

   ```
   export KUBECONFIG=/Users/<username>/.bluemix/plugins/container-service/clusters/living-on-the-cloud/kube-config-hou02-living-on-the-cloud.yml
   ```

   Copy and paste the export line returned in your terminal to set kubectl's target.

1. To verify that `kubectl` is able to connect to your cluster, run the following:

   ```
   kubectl get nodes
   ```

   The returned output should look something like this:

   ```
   NAME            STATUS    ROLES     AGE       VERSION
   10.77.223.210   Ready     <none>    8h        v1.11.7+IKS
   ```

### Test the Application

Once the deployment pipeline has completed, we can finally test our application to make sure it deployed successfully. To do that complete the following steps.

1. To find the port that has been publicly exposed, you can ask Kubernetes to provide a description of the `NodePort` you just created:

   ```
   kubectl describe service storm-tracker-port
   ```
   
   This should provide the following output:

   ```
   Name:                     storm-tracker-port
   Namespace:                default
   Labels:                   run=storm-tracker
   Annotations:              <none>
   Selector:                 run=storm-tracker
   Type:                     NodePort
   IP:                       XXX.XXX.XXX.XXX
   Port:                     <unset>  8080/TCP
   TargetPort:               8080/TCP
   NodePort:                 <unset>  30299/TCP
   Endpoints:                XXX.XXX.XXX.XXX:8080
   Session Affinity:         None
   External Traffic Policy:  Cluster
   Events:                   <none>
   ```

   In this example output, the exposed port is `30299`.

1. You will also need to get the public IP of your Kubernetes cluster:

   ```
   ibmcloud ks workers --cluster spring-boot-workshop
   ```

   The output should look similar to this:

   ```
   ID                                                 Public IP         Private IP        Machine Type   State    Status   Zone    Version
   kube-hou02-paeb33817993d9417f9ad8cfad7fcf270e-w1   184.172.XXX.XXX   XXX.XXX.XXX.XXX   free           normal   Ready    hou02   1.11.7_1544
   ```

1. Using the public IP and port from these outputs, you should be able to call your Spring Boot application at `<public IP>:<exposed port>/api/v1/storms`.

## Connecting Spring Boot to a Cloud Hosted Database

IBM Cloud has a catalog of services available to handle many of the needs of an enterprise. One of the most common requirements of enterprises is the longterm persistence of business valuable information in a database. In this section we will walk through connecting our Spring Boot application to a DB2 instance.

### Create a DB2 Database

With the IBM Cloud CLI, you can quick create new service instances with a single command. TO create a new database instance, in this case using DB2, run the following command:

```
ibmcloud resource service-instance-create spring-boot-db2 dashdb-for-transactions free us-south
```

### Handling the CRUD with Spring Data 

Spring Data is a popular library within the Spring Framework ecosystem. Using the Repostiroy pattern within Spring Data you can easily configure an application so that it is communicating with a database in mere minutes. We will be using Spring Data in this section to handle the database with the database we just created.  

1. Open your **pom.xml** update it adding these dependencies:

	```xml
	<dependency>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-data-jpa</artifactId>
	</dependency>
	<dependency>
	<groupId>com.ibm.db2.jcc</groupId>
		<artifactId>db2jcc</artifactId>
		<version>db2jcc4</version>
	</dependency>
	```

4. Create a class called `Storm` under `com.ibm.developer.stormtracker`: 

	```java
	package com.ibm.developer.stormtracker;

	import javax.persistence.Entity;
	import javax.persistence.GeneratedValue;
	import javax.persistence.Id;
	import javax.persistence.SequenceGenerator;
	import javax.persistence.Table;

	@Entity
	@Table(name = "storms")
	public class Storm {
	
		@Id
		@GeneratedValue(generator = "storms_id_generator")
		@SequenceGenerator(name = "storms_id_generator", allocationSize = 1, initialValue = 10)
		private long id;
		private String startDate;
		private String endDate;
		private String startLocation;
		private String endLocation;
		private String type;
		private int intensity;
	
		Storm() {
		}
	
		public Storm(String startDate, String endDate, String startLocation, String endLocation, String type,
				int intensity) {
			this.startDate = startDate;
			this.endDate = endDate;
			this.startLocation = startLocation;
			this.endLocation = endLocation;
			this.type = type;
			this.intensity = intensity;
		}
	
		public long getId() {
			return id;
		}
	
		public String getStartDate() {
			return startDate;
		}
	
		public String getEndDate() {
			return endDate;
		}
	
		public String getStartLocation() {
			return startLocation;
		}
	
		public String getEndLocation() {
			return endLocation;
		}
	
		public String getType() {
			return type;
		}
	
		public int getIntensity() {
			return intensity;
		}
	
	}
	```
5. Create a repository interface `StormRepo` under `com.ibm.developer.stormtracker`: 

	```java
	package com.ibm.developer.stormtracker;

	import org.springframework.data.repository.CrudRepository;

	public interface StormRepo extends CrudRepository<Storm, Long> {
		public Iterable<Storm> findByStartLocation(String type);
	}
	```

6. Update `StormTrackerController`: 

	```java
	package com.ibm.developer.stormtracker;

	import java.net.URI;

	import org.springframework.http.ResponseEntity;
	import org.springframework.web.bind.annotation.GetMapping;
	import org.springframework.web.bind.annotation.PathVariable;
	import org.springframework.web.bind.annotation.PostMapping;
	import org.springframework.web.bind.annotation.RequestBody;
	import org.springframework.web.bind.annotation.RequestMapping;
	import org.springframework.web.bind.annotation.RequestParam;
	import org.springframework.web.bind.annotation.RestController;
	
	
	@RestController
	@RequestMapping("/api/v1/storms")
	public class StormTrackerController {
	
		private StormRepo repo;
	
		public StormTrackerController(StormRepo repo) {
			this.repo = repo;
		}
	
		@GetMapping
		public ResponseEntity<Iterable<Storm>> findAllStorms() {
			return ResponseEntity.ok(repo.findAll());
		}
		
		@GetMapping("/{stormId}")
		public ResponseEntity<Storm> findById(@PathVariable long stormId) {
			return ResponseEntity.ok(repo.findById(stormId).get());
		}
	
		@PostMapping
		public ResponseEntity<?> addNewStorm(@RequestBody Storm storm) {
			storm = repo.save(storm);
			return ResponseEntity.created(URI.create("/api/v1/storms/" + storm.getId())).build();
		}
	}
	```

### Using Kubernetes Secrets to Connect to a Service

To connect to services our Spring Boot application will need connection information like username and password. Storing this information directly in `application.properties` files iss insecure and inconvenient. Kubernetes can securely store sensitive connection information like usernames and passwords in Kubernetes Secrets. You can then configure Kubernetes to supply these values when an application needs them. Let's configure Kubernetes to store the username and password to our new database in a secret: 


1. Open your terminal and run the following commandto create service credentials for connecting to the database we created at the start of this section:

	```
	ibmcloud resource service-key-create creds_Db2 Manager --instance-name spring-boot-db2
	```
	You will get output back that looks something like this:
	
	```	
	Name:          creds_Db2   
	ID:            crn:v1:bluemix:public:dashdb-for-transactions:us-south:a/4b4c36db94004c51b937b0343f8960f0:ec2f281c-5d4f-412e-a3ba-978882506e73:resource-key:1062ffab-c555-42c4-9c2d-c109520425b1   
	Created At:    Mon Nov  4 12:12:49 UTC 2019   
	State:         active   
	Credentials:                       
		       db:           BLUDB      
		       dsn:          DATABASE=BLUDB;HOSTNAME=dashdb-txn-sbox-yp-dal09-04.services.dal.bluemix.net;PORT=50000;PROTOCOL=TCPIP;UID=trv96241;PWD=b23lk8r-qnxwfbtm;      
		       host:         <host>      
		       hostname:     <host name>      
		       https_url:    https://dashdb-txn-sbox-yp-dal09-04.services.dal.bluemix.net      
		       jdbcurl:      jdbc:db2://dashdb-txn-sbox-yp-dal09-04.services.dal.bluemix.net:50000/BLUDB      
		       parameters:         
		       password:     <password>     
		       port:         50000      
		       ssldsn:       DATABASE=BLUDB;HOSTNAME=dashdb-txn-sbox-yp-dal09-04.services.dal.bluemix.net;PORT=50001;PROTOCOL=TCPIP;UID=trv96241;PWD=b23lk8r-qnxwfbtm;Security=SSL;      
		       ssljdbcurl:   jdbc:db2://dashdb-txn-sbox-yp-dal09-04.services.dal.bluemix.net:50001/BLUDB:sslConnection=true;      
		       uri:          db2://trv96241:b23lk8r-qnxwfbtm@dashdb-txn-sbox-yp-dal09-04.services.dal.bluemix.net:50000/BLUDB      
		       username:     <username>  
   	```
   
1. Create a file named **secret.yaml** and copy in the following: 

	```
	apiVersion: v1
	kind: Secret
	metadata:
	  name: db2-connection-info
	type: Opaque
	data:
	  username: <base64 username>
	  password: <base64 password>
	  jdbcurl: <base64 jdbcurl>
	```
1. Replace `<base64 *>` with their appropraite base64 encoded values:
	
	```
	echo -n 'VALUE' | base64
	```

1. In a terminal window in the same directory where you created **secret.yaml** run the following:

	```
	kubectl apply -f secret.yaml
	```

### Adding Test Data

1. Back on [IBM Cloud](https://cloud.ibm.com/), search for the DB2 instance you created earlier.

2. In the DB2 dashboard, click the **Open Console** button near the center of the page.

3. Click the "hamburger" button in the top left hand corner of the page (it's the three small green dashes)

4. Click **Run SQL** 

5. In the query box copy and execute the following: 

	```sql
	create sequence storms_id_generator start with 10 increment by 1 no maxvalue no cycle cache 24;
	
	create table storms (id int8 not null, end_date varchar(255), end_location varchar(255), intensity int4 not null, start_date varchar(255), start_location varchar(255), type varchar(255), primary key (id));
	
	insert into storms (id, start_date, end_date, start_location, end_location, type, intensity) values (storms_id_generator.NEXTVAL, '10-10-2018', '10-13-2018', 'Denver, Colorado', 'Kansas City, Missouri', 'Thunderstorm', 2);
	
	insert into storms (id, start_date, end_date, start_location, end_location, type, intensity) values (storms_id_generator.NEXTVAL, '01-15-2019', '01-17-2019', 'Atlantic Ocean', 'New York City, New York', 'Blizzard', 4);
	```

### Configuring the Application

1. Open **application.properties** in your project under `src/main/resources` and add the following:

	```
	spring.datasource.driver-class-name=com.ibm.db2.jcc.DB2Driver
	spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.DB2Dialect
	spring.jpa.properties.hibernate.jdbc.lob.non_contextual_creation=true
	#^^^prevents a warning exception from being thrown. See: https://github.com/spring-projects/spring-boot/issues/12007
	spring.jpa.open-in-view=false
	#^^^suppresses warning exception related to OSIV https://vladmihalcea.com/the-open-session-in-view-anti-pattern/
	```

2. Update the deployment.yml file with the following under `containers`

	```
	        args: ["--spring.datasource.username=$(db-username)","--spring.datasource.password=$(db-password)","--spring.datasource.url=$(db-jdbcurl)"]
	        env:
	        - name: db-username
	          valueFrom:
	            secretKeyRef:
	              name: db2-connection-info
	              key: username
	        - name: db-password
	          valueFrom:
	            secretKeyRef:
	              name: db2-connection-info
	              key: password
	        - name: db-jdbcurl
	          valueFrom:
	            secretKeyRef:
	              name: db2-connection-info
	              key: jdbcurl
	```  

---
**Tip:** See [here](deploymentFiles/2-deployment.yml) for a completed deployment.yml file with the above added.

---

2. Commit and push your changes to your repo:

	```
	git add .
	git commit -m "Added hello world url"
	git push
	```
3. Once the pipeline has completed run a **curl** command, or go by browser to the your application: `curl http://\<node-ip\>:\<node-port\>/api/v1/storms`. Instructions on how to look up your node-ip and node-port can be found in the [previous section](#test-the-application). You should get a JSON return that looks like this:
	
	```json
	[  
	   {  
	      "id":10,
	      "startDate":"10-10-2018",
	      "endDate":"10-13-2018",
	      "startLocation":"Denver, Colorado",
	      "endLocation":"Kansas City, Missouri",
	      "type":"Thunderstorm",
	      "intensity":2
	   },
	   {  
	      "id":11,
	      "startDate":"01-15-2019",
	      "endDate":"01-17-2019",
	      "startLocation":"Atlantic Ocean",
	      "endLocation":"New York City, New York",
	      "type":"Blizzard",
	      "intensity":4
	   }
	]
	```

## Cloud-Native Integration Testing

With the move to the cloud, comes a change in how to best approach automated testing. In this first section we created a deployment pipeline. When we commit a change to a repo we want this to start a process that will lead to a new version of an application be deployed to production. However for this to work we have to have a reliable suite of automated tests running to give us confidence we are not going to be pushing a bug to production. 

An area within automated testing that has always been difficult to make reliable and portable was integration testing. Writing tests that depend on remote systems can run into issues of flakiness. A test could fail because the remote system was down at the time the test was run, test data had gone missing, or the remote system could be under a lot of load which either slows down a tests execution or leads to a timeout. These issues lead automated tests not being taken seriously. Tests failures might be ignored or might be turned off altogether because they take too long to execute or frequently give false negatives. 

In this section we will look at a couple of tools; [TestContainers](https://www.testcontainers.org/) and [Spring Cloud Contract](https://spring.io/projects/spring-cloud-contract) that can help make integration tests more reliable and portable. 

### Services Integration Testing with TestContainers

1. Open your **pom.xml** and add the following:

	```
	<dependency>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-test</artifactId>
	</dependency>
	<dependency>
		<groupId>org.testcontainers</groupId>
    	<artifactId>db2</artifactId>
   		<version>1.12.3</version>
	</dependency>
	<dependency>
		<groupId>org.junit.jupiter</groupId>
		<artifactId>junit-jupiter</artifactId>
	</dependency>	
	```
	```
	<dependencyManagement>
		<dependencies>
			<dependency>
				<groupId>org.junit</groupId>
				<artifactId>junit-bom</artifactId>
				<version>5.5.0</version>
				<type>pom</type>
				<scope>import</scope>
			</dependency>
		</dependencies>
	</dependencyManagement>
	```
2. Create the source folder **/src/main/test**
3. Create the file **StormRepoTest** under `com.ibm.developer.stormtracker`
4. In **StormRepoTest** add the following:

	```
	@SpringJUnitConfig
	@ContextConfiguration(classes = { StormTrackerApplication.class }, initializers = StormRepoTest.Initializer.class)
	@TestMethodOrder(OrderAnnotation.class)
	public class StormRepoTest {
		private static Db2Container <?> db2 = new Db2Container <>();
	
		static {
	        postgres.start();
	    }
		
		public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
			@Override
			public void initialize(ConfigurableApplicationContext applicationContext) {
				TestPropertyValues.of("spring.datasource.url=" + db2.getJdbcUrl(), //
						"spring.datasource.username=" + db2.getUsername(), //
						"spring.datasource.password=" + db2.getPassword(),
						"spring.datasource.initialization-mode=ALWAYS",
						"spring.datasource.data=classpath:data.sql",
						"spring.datasource.schema=classpath:schema.sql",
						"spring.jpa.database-platform=com.ibm.db2.jcc.DB2Driver") //
						.applyTo(applicationContext);
			}
		}
	
		@Autowired
		private StormRepo repo;
	
		@Test
		@Order(1)
		public void testCountNumberOfStormInDB() {
			assertEquals(2, repo.count());
		}
	
		@Test
		public void testRetrieveStormFromDatabase() {
	
			Storm storm = repo.findAll().iterator().next();
	
			assertEquals(10, storm.getId());
		}
	
		@Test
		public void testStormToDB() throws ParseException {
			Storm storm = new Storm("25-08-2019", "05-09-2019", "Coast of Africa", "North Carolina", "Hurricane", 5);
	
			repo.save(storm);
	
			assertEquals(3, repo.count());
		}
	}
	```
1. Execute the test from the command line by running `mvn test`

In the above test class the tool [TestContainers](https://www.testcontainers.org) is setting up and tearing down a DB2 docker container. By using a local database issues that could cause tests to fail like data missing or network connectivity are reduced. By using a Docker container developers don't have to locally install, configure, and administer a database, making these tests portable.

### API Testing With Spring Cloud Contract

1. Open your **pom.xml** and add the following:

	```
	<dependency>
		<groupId>io.rest-assured</groupId>
		<artifactId>spring-mock-mvc</artifactId>
	</dependency>
	<dependency>
		<groupId>org.springframework.cloud</groupId>
		<artifactId>spring-cloud-starter-contract-verifier</artifactId>
	</dependency>
	```
	```
	<dependencyManagement>
		<dependencies>
			<dependency>
				<groupId>org.springframework.cloud</groupId>
				<artifactId>spring-cloud-dependencies</artifactId>
				<version>Greenwich.SR1</version>
				<type>pom</type>
				<scope>import</scope>
			</dependency>
		</dependencies>
	</dependencyManagement>
	```
	Under **build** add:
	```
	<plugin>
		<groupId>org.springframework.cloud</groupId>
		<artifactId>spring-cloud-contract-maven-plugin</artifactId>
		<version>2.1.1.RELEASE</version>
		<extensions>true</extensions>
		<configuration>
			<baseClassForTests>com.ibm.developer.stormtracker.ContractsBaseClass</baseClassForTests>
		</configuration>
	</plugin>
	```
	
1. Create a new fodler **contracts**  `src/test/resources`
Spring Cloud Contract makes the test of APIs for both producers and consumers super fast and reliable. In the above example we are writing a contract and verifying that our producer service, **storm-tracker** is meeting the requirements of the contract. 

Spring Cloud Contract produces a stubs artifact that consumers (clients) can use to test their service against to see if it can properly consume the produce service. Like with TestContainers above this allows fast and reliable integration testing that avoids issues of data going missing or problems with network connectivity. 

### Generating Documentation From Tests 

1. Open your **pom.xml** and add the following:

	```
	<dependency>
		<groupId>org.springframework.restdocs</groupId>
		<artifactId>spring-restdocs-mockmvc</artifactId>
	</dependency>
	```
	
	Under **build** add:
	
	```
	<plugin>
		<groupId>org.asciidoctor</groupId>
		<artifactId>asciidoctor-maven-plugin</artifactId>
		<version>1.5.3</version>
		<executions>
			<execution>
				<id>generate-docs</id>
				<phase>prepare-package</phase>
				<goals>
					<goal>process-asciidoc</goal>
				</goals>
				<configuration>
					<backend>html</backend>
					<doctype>book</doctype>
				</configuration>
			</execution>
		</executions>
		<dependencies>
			<dependency>
				<groupId>org.springframework.restdocs</groupId>
				<artifactId>spring-restdocs-asciidoctor</artifactId>
				<version>${spring-restdocs.version}</version>
			</dependency>
		</dependencies>
	</plugin>
	```
2. Create the source folder **/src/main/asciidoc**
3. Create the file **index.adoc** and add the following: 
	
	```
	= Storm Tracker Service API Guide
	<your name>;
	:doctype: book
	:icons: font
	:source-highlighter: highlightjs
	:toc: left
	:toclevels: 4
	:sectlinks:
	:operation-curl-request-title: Example request
	:operation-http-response-title: Example response
	
	[[overview]]
	= Overview
	
	API of Storm Tracker Service. Example of using contracts to both test an API to make sure it meets contract requirements and then generate API documentation from supplied contracts. 
	
	Form More Information on Contract Testing checkout:  https://github.com/wkorando/collaborative-contract-testing[Collaborative Contract Testing]
	
	[[resources-tag-retrieve]]
	== Find all storms
	
	A `GET` request to retrieve all produce items
	
	operation::ContractsTest_validate_find_all_storms[snippets='curl-request,response-body,http-request,http-response']
	```
4. Run `mvn install` from your command line
5. Open the **index.html** under `/target/generated/docs`
	
Along with testing your API to ensure it is meeting what is specified in your contracts. Spring Cloud Contract can also generate documentation from your contracts. This makes producing documentation for your services not only easier, but because the documentation is being produced as a part of the test and build process, the doocumentation shuld always be update to date with the current state of the service. 
