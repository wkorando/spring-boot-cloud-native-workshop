# Connecting Spring Boot to a Cloud Hosted Database

## Create Cloud Database

1. Go to [https://cloud.ibm.com/](https://cloud.ibm.com/) and in the top center search for **ElephantSQL**
2. Give a memorable name for the service
3. Select the **Lite** plan 
4. Click **Create**

## Handling the CRUD with Spring Data 

1. Open your **pom.xml**
2. Update the version:

	```
	<version>0.0.2-SNAPSHOT</version>
	```

3. Update the **pom.xml** to adding these dependencies:

	```xml
	<dependency>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-data-jpa</artifactId>
	</dependency>
	<dependency>
		<groupId>org.postgresql</groupId>
		<artifactId>postgresql</artifactId>
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

## Configuring the Database and Creating Kubernetes Secrets

1. Back on [https://cloud.ibm.com/](https://cloud.ibm.com/) and in the top center search for service name of the database you just created.

1. Create a file named **secret.yaml** and add the below contents to it: 

	```
	apiVersion: v1
	kind: Secret
	metadata:
	  name: postgres-connection-info
	type: Opaque
	data:
	  username: <base64 username>
	  password: <base64 password>
	```
1. Replace `<base64 username>` & `<base64 password>` with the base64 encoded username\password 

	**Note:** Using to base64 a value on a nix system use this command: `echo -n 'value' | base64`

1. In a terminal window in the same directory where you have **secret.yaml** located run the following:

	```
	kubectl apply -f secret.yaml
	```
	
1. Back in your web browser, on the left side of the page select **BROWSER**

1. In the query box copy and execute the following: 

	```sql
	create sequence storms_id_generator start 10 increment 1;
	
	create table storms (id int8 not null, end_date varchar(255), end_location varchar(255), intensity int4 not null, start_date varchar(255), start_location varchar(255), type varchar(255), primary key (id));
	
	insert into storms (id, start_date, end_date, start_location, end_location, type, intensity) values (nextval('storms_id_generator'), '10-10-2018', '10-13-2018', 'Denver, Colorado', 'Kansas City, Missouri', 'Thunderstorm', 2);
	
	insert into storms (id, start_date, end_date, start_location, end_location, type, intensity) values (nextval('storms_id_generator'), '01-15-2019', '01-17-2019', 'Atlantic Ocean', 'New York City, New York', 'Blizzard', 4);
	```

## Update the Kubernetes Deployment and Spring Boot Application


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
	              name: postgres-connection-info
	              key: username
	        - name: db-password
	          valueFrom:
	            secretKeyRef:
	              name: postgres-connection-info
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
	spring.datasource.driver-class-name=org.postgresql.Driver
	spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQL95Dialect
	spring.jpa.properties.hibernate.jdbc.lob.non_contextual_creation=true
	#^^^prevents a warning exception from being thrown. See: https://github.com/spring-projects/spring-boot/issues/12007
	spring.jpa.open-in-view=false
	#^^^suppresses warning exception related to OSIV https://vladmihalcea.com/the-open-session-in-view-anti-pattern/
	```

## Redeploy the Application to Kubernetes

1. In a terminal window from the 

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