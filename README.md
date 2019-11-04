# Deploying Spring Boot to a Kubernetes Cluster

In this step we will walkthrough the minimal steps of deploying a Spring Boot application on a Kubernetes cluster. 

For more in-depth instruction and explanation of the steps you can read the Living on the Cloud article [here](https://developer.ibm.com/tutorials/living-on-the-cloud-1/).

## Create a Container Registry

In Kubernetes everything is running in a container. In this workshop in this workshop we will be using Docker as our container implementation, but Kubernetes supports other container types. Kubernetes needs a container registy to pull from the container images it will be running. IBM Cloud provides a container registry service, over these next few steps we will be creating and configuring our Kubernetes to communicate with a container registry. 

1. In the top center of the [IBM Cloud Dashboard](https://cloud.ibm.com/) search for **Container Registry** and select it.

1. Click **Create**, and you should be brought to the **Registry** home page.

## Configure IBM Cloud CLI

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
   
## Create Demo Application

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
   
## Containerizing the Application

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
	**Note:** If you are getting an issue where adoptopenjdk/openjdk8-openj9:alpine-slim is not being pulled run `docker pull adoptopenjdk/openjdk8-openj9:alpine-slim` first before the above command. 
	**Note:** You will use the api key in the file we created at the end of the [Configure IBM Cloud](#configure-ibm-cloud). Be sure to use the value **iampikey** for the username.

   You should see output that looks like the following near the end of the build execution:

   ```
   [INFO] DOCKER> Pushed us.icr.io/living-on-the-cloud/storm-tracker in 14 seconds
   ```

## Deploy to Kubernetes

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

## Connecting to a Cloud Hosted Database

In the next exercise we will ook at how to connect the Spring Boot application we built in this exercise to a cloud hosted database. Continue on with the exercise [here](https://github.com/wkorando/spring-boot-cloud-native-workshop/tree/2-connecting-to-a-database). 
