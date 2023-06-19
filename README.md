# veridion

This is an application written in Java 17 and Spring Boot 3.1.0.

It is supposed to be encapsulated in a docker container and then deployed to a kubernetes cluster (see the `Dockerfile`
and `deployment.yaml` files).

It listens for messages on a kafka topic and when a message is consumed, it starts extracting companies data
(phoneNumbers, socialMediaLinks, addresses) from a predefined list of websites html pages. Then calls a PUT endpoint
that is exposed by a different application (`elasticsearch-interactive-service`) and updates the companies data which is
saved in an elasticsearch cluster.

### How it works (in my case, on a Windows machine):

*Disclaimer: Kubernetes and Elasticsearch are completely new technologies to me, therefore some details that I present
here might be obvious and unnecessary*

Prerequisites: Installed Docker Desktop application and enabled Kubernetes

1. Set up kafka
    - there are multiple steps that need to be followed, see: https://www.youtube.com/watch?v=BwYFuhVhshI
    - the main point is that Kafka needs Zookeeper in order to be able to run
    - that's why we can see these 2 manifest files in this project: `kafka-deployment.yaml`
      and `zookeeper-deployment.yaml`
    - each of them has 2 resources defined, one `Deployment` and one `Service` which is used to expose the
      kafka/zookeeper to other services or clients within the Kubernetes cluster
    - kafka will communicate with zookeeper through port `2181` while the clients that want to consume or produce
      messages on the kafka topics will communicate through port `9092`
    - to deploy to k8s we need to run:
    ```bash
    kubectl apply -f zookeeper-deployment.yaml
    kubectl apply -f kafka-deployment.yaml
    ```
    - next step is to create a kafka topic (this is not mandatory because a topic can be created automatically when a
      message is produced, but most of the time the topic is created manually)
    ```bash
    # find the name for the kafka pod
    kubectl get pods
    # enter the kafka pod
    kubectl exec -it kafka-deployment-6b7b57cbd9-gblxt -- /bin/sh
    # use the kafka-topics utility to create the topic named 'my-topic'
    kafka-topics --create --bootstrap-server localhost:29092 --replication-factor 1 --partitions 1 --topic my-topic
    ```
2. Create the `Dockerfile` and `deployment.yaml` manifest file for this application
    - for the docker image that we are building we need jdk 17 and decided to use `openjdk:17-jdk-slim`
    - related to the manifest file: again two resources, one `Deployment` and one `Service` that will expose this
      application to the k8s cluster
    - build the image:
      ```bash
      docker build -t veridion-image .
      ```
    - deploy in k8s:
      ```bash
      kubectl apply -f deployment.yaml
      ```
3. Once the application is up, we can produce a message on the kafka topic so that the websites processing starts
    - one way to do this is:
      ```bash
      # find the name for the kafka pod
      kubectl get pods
      # enter the kafka pod
      kubectl exec -it kafka-deployment-6b7b57cbd9-gblxt -- /bin/sh
      # use the kafka-console-producer utility to open a console and produce messages on the topic named 'my-topic'
      kafka-console-producer --bootstrap-server localhost:29092 --topic my-topic
      # a console is opened an you can type any message
      ```
4. The java code:
    - the entrypoint to this code is the `KafkaConsumer::listen` method. We can see that the `hostname` is used a lot
      in `KafkaConsumer.java`. What is the reason?
    - inside `sample-websites.json` we have around 1000 different domains that need to be processed (fetch the html
      page and extract companies data such as phoneNumbers, socialMediaLinks, addresses). The first time I tried to run
      the application, it took around 32 minutes to finish the processing. Then I realized that something needs to be
      changed in order to be able to `to crawl the entire list in no more than 10 minutes` as asked in the assignment.
    - I decided to deploy multiple instances of this application in the k8s cluster (see `replicas` property
      in `deployment.yaml`). Deploying multiple instances is easy, but how can I start them all at the same time?
    - realized that I need some kind of start signal that can be sent to all instances, then decided to use kafka.
      Managed to set up kafka, create a topic and produce messages (as mentioned earlier) but there was still a problem,
      only one app instance was consuming the messages from the topic.
    - learned that Kafka ensures that each message is consumed by only one consumer within a consumer group, so had to
      put each instance in a different group. To do this I needed a variable that would be different for each instance.
      Then I realized that I also want to use this variable to properly split the websites between the app instances, so
      that each website is processed once and only once
    - learned about the `${HOSTNAME}` variable which is exactly the pod name, like: `veridion-app-88bb4bb74-4xvwb`. This
      would fix the consumer group issue, but wouldn't help me at all with the second issue. I needed a counter and I
      learned that this is possible with a `StatefulSet` resource: each pod in a StatefulSet gets a unique, stable
      hostname and ordinal index. This allows applications to have consistent network identities and is exactly what I
      needed. Therefore, replaced the initial `Deployment` resource from `deployment.yaml` with a `StatefulSet`
      resource and now the `${HOSTNAME}` values are: `veridion-app-0`, `veridion-app-1`, ... `veridion-app-9`.
    - now going back to the code, the `KafkaConsumer` first calls `DataExtractorService::extractData`. This method will
      first decide which 100 websites should be processed, based on the app instances corresponding pod number (0,
      1, ... 9) and then use the `jsoup` library to connect to each given website and fetch the html document.
    - then the html document is analyzed by looking at the text in each html element and also at the css classes. There
      are probably much better ways of identifying the companies' data (one would be using machine learning and AI
      algorithms), but fow now I just wanted to make it work (as some would say, Make It Work Make It Right Make It
      Fast)
    - after the documents are processed, the `HttpService::updateCompanyData` method is called. This sends a PUT request
      to the other application that was created for this assignment, `elasticsearch-interactive-service`. The extracted
      data is then saved in an elasticsearch cluster which can eventually be queried.

## More details to be added...