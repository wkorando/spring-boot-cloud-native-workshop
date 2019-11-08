# Spring Boot Cloud Native Workshop

This workshop is designed with Spring Developers in mind. If your organization is thinking about or in the process of making the move to a Cloud Platform built on Kubernetes, this workshop will help give you hands on experience of working with Kubernetes.

For additional reading, be sure to check out the [Living on the Cloud](https://developer.ibm.com/series/living-on-the-cloud/) blog series on IBM Developer.  

## What You Will Learn

In this workshop you will learn about the following concepts: 

* Working with Kubernetes
* Building Delivery Pipelines
* Writing Reliable Automated Tests

## Table of Contents

1. [Prerequisties](#prerequisties)
2. [Deploying Spring Boot to a Kubernetes Cluster](#deploying-spring-boot-to-a-kubernetes-luster)
3. Connecting Spring Boot to a Cloud Hosted Database
4. Cloud Native Integration Testing

## Prerequisties 

This workshop requires several tools to be installed on your system before beginning:

* You will need [Java 8+](https://adoptopenjdk.net/)
* Docker will be required as we will be creating Docker images [Docker](https://www.docker.com/)
* [An IBM Cloud account](https://ibm.biz/BdzCAu)
* [IBM Cloud CLI](https://github.com/IBM-Cloud/ibm-cloud-cli-release/releases/)
* You will need an IDE or an advanced text editor like [Notepad++](https://notepad-plus-plus.org/) or [TextMate](https://macromates.com/)
* Clone or download this repo (green button, in upper right hand of page)

### Windows Users

<details>
<summary>Click to Expand</summary>
This workshop makes heavy use of terminal commands. The terminal command examples in this workshop are use *nix idioms. For that reason it is highly encouraged to either use [Cygwin](https://www.cygwin.com/) or [install/enable the Linux Bash shell](https://www.windowscentral.com/how-install-bash-shell-command-line-windows-10) that was added to Windows 10.  
</details>

### Initialize a Kubernetes Cluster

Initializing a Kubernetes cluster takes ~30 minutes. It is highly encouraged to start this process as early as possible: 

1. Log in to your [IBM Cloud account](http://cloud.ibm.com/). 

1. In the top center of the page search for **Kubernetes Cluster** and select it.

1. Create a **Free Kubernetes cluster**. To initialize a Lite cluster, you will need to upgrade your IBM Cloud account, if you have not already.

## Deploying Spring Boot to a Kubernetes Cluster

In this step we will walkthrough the minimal steps of deploying a Spring Boot application on a Kubernetes cluster. 

For more in-depth instruction and explanation of the steps you can read the Living on the Cloud article [here](https://developer.ibm.com/tutorials/living-on-the-cloud-1/).

### Switch to Current Branch

From the root of the project directory make sure you switch to the current branch with the following command: 

```
git checkout 1-deploying-spring-boot
```

### Create a Container Registry

In Kubernetes everything is running in a container. In this workshop in this workshop we will be using Docker as our container implementation, but Kubernetes supports other container types. Kubernetes needs a container registy to pull from the container images it will be running. IBM Cloud provides a container registry service, over these next few steps we will be creating and configuring our Kubernetes to communicate with a container registry. 

1. In the top center of the [IBM Cloud Dashboard](https://cloud.ibm.com/) search for **Container Registry** and select it.

1. Click **Create**, and you should be brought to the **Registry** home page.

### Configure IBM Cloud CLI

IBM Cloud has a powerful Command Line Interface (CLI). Most actions can be handled directly through the IBM CLoub CLI. This is is often quicker and easier than going the the Web UI. The IBM Cloud CLI makes uses of plugins to handle various capabilities. We will be installing a couple of plugins in this section. 

1. Run the following command to install the container-registry plugin:

   ```
   ibmcloud plugin install container-registry
   ```

1. Once the install script has completed, run the following command to ensure it was successful:

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

1. Log in to your IBM Cloud account through the IBM Cloud CLI using the following command:

   ```
   ibmcloud login -a https://cloud.ibm.com
   ```

   You will be prompted to enter the email and password associated with your IBM Cloud account.

   **Note:** If you are using a federated IBM Cloud account [follow these steps](https://cloud.ibm.com/docs/iam?topic=iam-federated_id#federated_id).

1. Once logged in, create a namespace for your container registry, the name doesn't matter, but use something memborable as we will be using that value later. 

   To create a namespace run the following command:

   ```
   ibmcloud cr namespace-add [your namespace]
   ```

   You should get the following response:

   ```
   Adding namespace '[your namespace]'...

   Successfully added namespace '[your namespace]'
   ```

1. Create an API token that you we will use for pushing images to the container registry:

   ```
   ibmcloud iam api-key-create [API-token-name] -d "[token description]" --file [key_file_name]
   ```

   When you open the file we just created with the previous command, you the contents should look something like this: 
   
   ```
   {
		"name": "living-on-the-cloud-cr",
		"description": "living-on-the-cloud-cr-key",
		"apikey": "n6P30qr7efUhsHzoJFmwVAKGkT4o4wLCNrXzSBfDQPBZ",
		"createdAt": "2019-08-01T15:49+0000",
		"locked": false,
		"uuid": "ApiKey-004a8490-5275-4fb3-b8c8-e0fd495214ed"
	}
   ```
   **Note:** This file contains sensitive information so you will want to keep it secure and memborable location.
   
### Create Demo Application

We will be creating a simple Spring Boot application during this workshop. If you don't want to go through the process of writing the demo application and/or run into problems, under the `/finish/storm-tracker` a *near complete* application is available.  

1. Create a project on [start.spring.io](https://start.spring.io/).
	
	* Name the project **storm-tracker**
	* Include the **Web** dependency 

1. Open the project in your IDE.

1. Create a new class called `StormTrackerController` and add to it a `@GetMapping` that returns `"Hello World"`` as the response. 

   Here is what the completed class should look like:

   ```java
   @RestController
   @RequestMapping("/api/v1/storms")
   public class StormTrackerController {

     @GetMapping
     public ResponseEntity<String> helloWorld(){
      return ResponseEntity.ok("Hello World");
     }
   }
   ```

   Verify the application is working: [http://localhost:8080/api/v1/storms](http://localhost:8080/api/v1/storms).
   
### Containerizing the Application

Containerization wraps all an applications dependencies in a lightweight runtime. This helps increase portability as someone only needs the container agent, in this case Docker, to run an application. In Docker containers are built off images, which functions similar to a `.jar` or `.war`. A **Dockerfile** is used to describe how an image should be built. We will be configuring our project to create an Docker image of the Spring Boot project we are building in this section. 

**Note:** If you are using the project under `/finish` you should still read through these instructions as you will need to update the `pom.xml` with values specific to your account.

1. Create a file called **Dockerfile** in the root of the project directory and add the following:  

```
FROM adoptopenjdk/openjdk8-openj9:alpine-slim

COPY target/storm-tracker.jar /

ENTRYPOINT ["java", "-jar", "storm-tracker.jar" ]
```

1. In the **pom.xml**, under **\<build\>** in pom.xml, add the following:

* The value of `container registry` will be the first line returned from calling `ibmcloud cr info`
* The value of `namespace` will be the container namespace created a few steps earlier

   ```
   	<build>
		<finalName>storm-tracker</finalName>
		<plugins>
			...
			<plugin>
				<groupId>io.fabric8</groupId>
				<artifactId>docker-maven-plugin</artifactId>
				<extensions>true</extensions>
				<configuration>
					<images>
						<image>
							<name>[container registry]/[namespace]/${project.name}</name>
							<build>
								<dockerFile>Dockerfile</dockerFile>
								<dockerFileDir>${project.basedir}</dockerFileDir>
								<tags>
									<tag>latest</tag>
									<tag>${project.version}</tag>
								</tags>
							</build>
						</image>
					</images>
				</configuration>
			</plugin>
		</plugins>
	</build>
   ```
   
   **Note:** Be sure to add `<finalName>storm-tracker</finalName>` as well

1. Build the project and push an image to the container registry with the following command:

   ```
	./mvnw package docker:build -Ddocker.username=iamapikey -Ddocker.password=<your api-key> docker:push 
   ```
   
	**Note:** If you are getting an issue where adoptopenjdk/openjdk8-openj9:alpine-slim is not being pulled run:
    ```bash
    docker pull adoptopenjdk/openjdk8-openj9:alpine-slim
    ```
    first before the above command. 
	**Note:** You will use the api key in the file we created at the end of the [Configure IBM Cloud](#configure-ibm-cloud). Be sure to use the value **iampikey** for the username.
   You should see output that looks like the following near the end of the build execution:

   ```
   [INFO] DOCKER> Pushed us.icr.io/living-on-the-cloud/storm-tracker in 14 seconds
   ```

### Deploy to Kubernetes

By now, your Kubernetes cluster has hopefully finished initializing. To verify that it has, go back to the Dashboard page and see if the status is **Normal** for the cluster you just created.

1. Add the container-service plugin to the IBM Cloud CLI, which will also download and install **kubectl**:

   ```
   ibmcloud plugin install container-service
   ```

1. Set the IBM Cloud CLI to the region your cluster is located in. To view all regions run `ibmcloud regions`.

   To set the region run:

   ```
   ibmcloud ks region set <your region>
   ```

1. Download the configuration information for your cluster:

   ```
   ibmcloud ks cluster config <your cluster name>
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

1. Deploy and run the image you created earlier on your cluster (like above you will need to fill in the correct values of `container registry` and `namespace`:

   ```
   kubectl run storm-tracker --image=[container registry]/[namespace]/storm-tracker
   ```

1. To make your service accessible from an external IP, you will need to expose it, like this:

   ```
   kubectl expose deployment storm-tracker --port=8080 --target-port=8080 --name=storm-tracker-service --type=NodePort
   ```

1. To find the port that has been publicly exposed, you can ask Kubernetes to provide a description of the `NodePort` you just created:

   ```
   kubectl describe service storm-tracker-service
   ```

   This should provide the following output:

   ```
   Name:                     storm-tracker-service
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
   ibmcloud ks workers --cluster [cluster name]
   ```

   The output should look similar to this:

   ```
   ID                                                 Public IP         Private IP        Machine Type   State    Status   Zone    Version
   kube-hou02-paeb33817993d9417f9ad8cfad7fcf270e-w1   184.172.XXX.XXX   XXX.XXX.XXX.XXX   free           normal   Ready    hou02   1.11.7_1544
   ```

1. Using the public IP and port from these outputs, you should be able to call your Spring Boot application at `<public IP>:<exposed port>/api/v1/storms`.

## Connecting Spring Boot to a Cloud Hosted Database

IBM Cloud has a catalog of services available to handle many of the needs of an enterprise. One of the most common requirements of enterprises is the longterm persistence of business valuable information in a database. In this section we will walk through connecting our Spring Boot application to a DB2 instance.

### Create Cloud Database

1. Go to [https://cloud.ibm.com/](https://cloud.ibm.com/) and in the top center search for **DB2**
2. Give a memorable name for you DB2 instance, we will be needing it later
3. Select the **Lite** plan 
4. Select the current region you are in
4. Click **Create**

### Handling the CRUD with Spring Data 

Spring Data is a popular library within the Spring Framework ecosystem. Using the Repostiroy pattern within Spring Data you can easily configure an application so that it is communicating with a database in mere minutes. We will be using Spring Data in this section to handle the database with the database we just created.  

1. Open your **pom.xml**
2. We will want to update the version of the application. The build process is using this to tag the image as well and Kubernetes, by default, won't re-deploy an image if it has the same tag as the current version on an application:

	```
	<version>0.0.2-SNAPSHOT</version>
	```

3. Update the **pom.xml** to adding these dependencies:

	```xml
	<dependency>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-data-jpa</artifactId>
	</dependency>
	<groupId>com.ibm.db2.jcc</groupId>
		<artifactId>db2jcc</artifactId>
		<version>db2jcc4</version>
	</dependency>
	```

4. Create a class called `Storm` under `com.ibm.developer.stormtracker`: 

	```java
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
	public interface StormRepo extends CrudRepository<Storm, Long> {
		public Iterable<Storm> findByStartLocation(String type);
	}
	```

6. Update `StormTrackerController`: 

	```java
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
	ibmcloud resource service-key-create creds_Db2 Manager --instance-name <instance name>
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
	```
1. Replace `<base64 username>` & `<base64 password>` with the base64 encoded username\password 

	**Note:** Using to base64 a value on a nix system use this command: `echo -n 'value' | base64`

1. In a terminal window in the same directory where you created **secret.yaml** run the following:

	```
	kubectl apply -f secret.yaml
	```
	
1. Back on [IBM Cloud](https://cloud.ibm.com/), search for the DB2 instance you created earlier.

2. In the DB2 dashboard, click the **Open Console** button near the center of the page.

3. Click the "hamburger" button in the top left hand corner of the page (it's the three small green dashes)

4. Click **Run SQL** 

1. In the query box copy and execute the following: 

	```sql
	create sequence storms_id_generator start with 10 increment by 1 no maxvalue no cycle cache 24;
	
	create table storms (id int8 not null, end_date varchar(255), end_location varchar(255), intensity int4 not null, start_date varchar(255), start_location varchar(255), type varchar(255), primary key (id));
	
	insert into storms (id, start_date, end_date, start_location, end_location, type, intensity) values (storms_id_generator.NEXTVAL, '10-10-2018', '10-13-2018', 'Denver, Colorado', 'Kansas City, Missouri', 'Thunderstorm', 2);
	
	insert into storms (id, start_date, end_date, start_location, end_location, type, intensity) values (storms_id_generator.NEXTVAL, '01-15-2019', '01-17-2019', 'Atlantic Ocean', 'New York City, New York', 'Blizzard', 4);
	```

### Update the Kubernetes Deployment and Spring Boot Application

In the previous section we simply deployed the Spring Boot application from the command line, largely leading the specifics of how the application should be deployed up to the Kubernetes cluster. This isn't a good longterm solution, in this section we will pull down the `deployment.yml` Kubernetes created when we deployed the application initially and update it to fit the needs of our application. 

1. Let's bring down the the YAML file for **storm-tracker** applications we deployed in the previous exercise:
	```
	kubectl get deployments storm-tracker --namespace=default -o yaml > deployment.yaml
	```

	Open **deployment.yaml** in a text editor, it should look something like this: 

	```yaml
	apiVersion: extensions/v1beta1
	kind: Deployment
	metadata:
	  annotations:
	    deployment.kubernetes.io/revision: "12"
	    kubectl.kubernetes.io/last-applied-configuration: |
	      {"apiVersion":"extensions/v1beta1","kind":"Deployment","metadata":{"annotations":{},"labels":{"run":"storm-tracker"},"name":"storm-tracker","namespace":"default","selfLink":"/apis/extensions/v1beta1/namespaces/default/deployments/storm-tracker"},"spec":{"progressDeadlineSeconds":600,"replicas":1,"revisionHistoryLimit":10,"selector":{"matchLabels":{"run":"storm-tracker"}},"strategy":{"rollingUpdate":{"maxSurge":"25%","maxUnavailable":"25%"},"type":"RollingUpdate"},"template":{"metadata":{"creationTimestamp":null,"labels":{"run":"storm-tracker"}},"spec":{"containers":[{"args":["--spring.application.json=$(BINDING)"],"env":[{"name":"BINDING","valueFrom":{"secretKeyRef":{"key":"binding","name":"binding-living-on-the-cloud"}}}],"image":"us.icr.io/openj9-demo/storm-tracker:0.0.2-SNAPSHOT","imagePullPolicy":"Always","name":"storm-tracker","resources":{},"terminationMessagePath":"/dev/termination-log","terminationMessagePolicy":"File"}],"dnsPolicy":"ClusterFirst","restartPolicy":"Always","schedulerName":"default-scheduler","securityContext":{},"terminationGracePeriodSeconds":30}}}}
	  creationTimestamp: "2019-08-01T22:48:32Z"
	  generation: 25
	  labels:
	    run: storm-tracker
	  name: storm-tracker
	  namespace: default
	  resourceVersion: "1132314"
	  selfLink: /apis/extensions/v1beta1/namespaces/default/deployments/storm-tracker
	  uid: 7cb6f3c5-b4ae-11e9-9a9f-461265fcff59
	spec:
	  progressDeadlineSeconds: 600
	  replicas: 1
	  revisionHistoryLimit: 10
	  selector:
	    matchLabels:
	      run: storm-tracker
	  strategy:
	    rollingUpdate:
	      maxSurge: 25%
	      maxUnavailable: 25%
	    type: RollingUpdate
	  template:
	    metadata:
	      creationTimestamp: null
	      labels:
	        run: storm-tracker
	    spec:
	      containers:
	        image: us.icr.io/openj9-demo/storm-tracker:0.0.1-SNAPSHOT
	        imagePullPolicy: Always
	        name: storm-tracker
	        resources: {}
	        terminationMessagePath: /dev/termination-log
	        terminationMessagePolicy: File
	      dnsPolicy: ClusterFirst
	      restartPolicy: Always
	      schedulerName: default-scheduler
	      securityContext: {}
	      terminationGracePeriodSeconds: 30
	status:
	  availableReplicas: 1
	  conditions:
	  - lastTransitionTime: "2019-08-09T16:10:37Z"
	    lastUpdateTime: "2019-08-09T16:10:37Z"
	    message: Deployment has minimum availability.
	    reason: MinimumReplicasAvailable
	    status: "True"
	    type: Available
	  - lastTransitionTime: "2019-08-09T16:10:32Z"
	    lastUpdateTime: "2019-08-09T16:10:37Z"
	    message: ReplicaSet "storm-tracker-5c99cd9c5f" has successfully progressed.
	    reason: NewReplicaSetAvailable
	    status: "True"
	    type: Progressing
	  observedGeneration: 25
	  readyReplicas: 1
	  replicas: 1
	  updatedReplicas: 1
	```

	There is a lot of instance specific information we will want to remove from this **deployment.yaml**. We want is a template for telling Kubernetes how to deploy the **storm-tracker**, in the future. 

	Here is what a **deployment.yaml** should look like this:

	```yaml
	apiVersion: extensions/v1beta1
	kind: Deployment
	metadata:
	  labels:
	    run: storm-tracker
	  name: storm-tracker
	  namespace: default
	  selfLink: /apis/extensions/v1beta1/namespaces/default/deployments/storm-tracker
	spec:
	  progressDeadlineSeconds: 600
	  replicas: 1
	  revisionHistoryLimit: 10
	  selector:
	    matchLabels:
	      run: storm-tracker
	  strategy:
	    rollingUpdate:
	      maxSurge: 25%
	      maxUnavailable: 25%
	    type: RollingUpdate
	  template:
	    metadata:
	      creationTimestamp: null
	      labels:
	        run: storm-tracker
	    spec:
	      containers:
	      - image: <container registry>/<namespace>/storm-tracker:0.0.2-SNAPSHOT
	        imagePullPolicy: Always
	        name: storm-tracker
	        resources: {}
	        terminationMessagePath: /dev/termination-log
	        terminationMessagePolicy: File
	        args: ["--spring.datasource.username=$(db-username)","--spring.datasource.password=$(db-password)"]
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
	      dnsPolicy: ClusterFirst
	      restartPolicy: Always
	      schedulerName: default-scheduler
	      securityContext: {}
	      terminationGracePeriodSeconds: 30
	```

	Remember to update the image line, including updating the version to `0.0.2-SNAPSHOT`:
	
	```
	      - image: us.icr.io/openj9-demo/storm-tracker:0.0.2-SNAPSHOT
	```


1. Open **application.properties** in your project under `src/main/resources` and add the following:

	```
	spring.datasource.url=jdbc:postgresql://raja.db.elephantsql.com:5432/hyvrmshj
	spring.datasource.driver-class-name=com.ibm.db2.jcc.DB2Driver
	spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.DB2Dialect
	spring.jpa.properties.hibernate.jdbc.lob.non_contextual_creation=true
	#^^^prevents a warning exception from being thrown. See: https://github.com/spring-projects/spring-boot/issues/12007
	spring.jpa.open-in-view=false
	#^^^suppresses warning exception related to OSIV https://vladmihalcea.com/the-open-session-in-view-anti-pattern/
	```
	
1. Lastly run the build again to send the updated docker image to the container registry:

```
./mvnw package docker:build -Ddocker.username=iamapikey -Ddocker.password=<your api-key> docker:push 
```

### Redeploy the Application to Kubernetes

1. We will now want to update the deployment description on our Kubernetes cluster with the `deployment.yaml` with the deployment file we just created. 

	```
	kubectl apply -f deployment.yaml
	```
	
	You should get a response back that looks something like this:
	
	```
	deployment.extensions/storm-tracker configured
	```
	
	Wait about 30 seconds while the application starts up and connects to the database. After waiting run a **curl** command, or go by browser to the your application: curl **http://\<node-ip\>:\<node-port\>/api/v1/storms**. Instructions on how to look up your node-ip and node-port can be found in the [previous exercise](https://github.com/wkorando/spring-boot-cloud-native-workshop/tree/1-deploying-spring-boot#deploy-to-kubernetes). You should get a JSON return that looks like this:
	
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
## Cloud native Integration Testing

In this section we will look at some new tools for handling the intergration testing needs of Cloud Native applications. A key to going fast in the modern world is having fast, portable, reliable integration tests. 

### Services Integration Testing with TestContainers

1. Open your **pom.xml** and add the following:

	```
	<dependency>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-test</artifactId>
	</dependency>
	<dependency>
		<groupId>org.testcontainers</groupId>
		<artifactId>postgresql</artifactId>
		<version>1.10.6</version>
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
		private static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>();
	
		static {
	        postgres.start();
	    }
		
		public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
			@Override
			public void initialize(ConfigurableApplicationContext applicationContext) {
				TestPropertyValues.of("spring.datasource.url=" + postgres.getJdbcUrl(), //
						"spring.datasource.username=" + postgres.getUsername(), //
						"spring.datasource.password=" + postgres.getPassword(),
						"spring.datasource.initialization-mode=ALWAYS",
						"spring.datasource.data=classpath:data.sql",
						"spring.datasource.schema=classpath:schema.sql",
						"spring.jpa.database-platform=org.hibernate.dialect.PostgreSQL95Dialect") //
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

In the above test class the tool [TestContainers](https://www.testcontainers.org) is setting up and tearing down a PostgreSQL docker container. By using a local database issues that could cause tests to fail like data missing or network connectivity are reduced. By using a Docker container developers don't have to locally install, configure, and administer a database, making these tests portable.

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