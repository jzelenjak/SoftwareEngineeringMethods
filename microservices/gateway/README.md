# Gateway Microservice

## Purpose

Gateway is the microservice that facilitates communication between the client and the system as well as the communication 
between microservices.

As the microservices are independent entities, their precise locations (port/path) aren't necessarily known by either 
the clients or the other microservices. Gateway helps to remedy this problem by acting as a middle-man that stores 
information about where *active* microservices reside and then reroutes requests to their intended locations. To better separate these concerns, the gateway has been internally split into two components: `Discovery Server` and 
`Router`.

### Discovery Server

Discovery server retains path information about *active* microservices. The way a microservice is to be declared 
is through a heartbeat system. The discovery server requires heartbeat information from a microservice that 
contains its location in configurable set intervals.

Whenever a heartbeat arrives the heartbeat is put into a cache with a set interval amount, preferably something 
a little longer than the heartbeat interval.

### Router

The router acts as a proxy and asynchronously re-routes any request coming to the gateway to its intended recipient 
microservice. It passes along an exact copy, with all header information and body, of both requests and responses. 
This allows clients and the system microservices to function fully by only knowing one location, the location of 
the gateway.

## API

A description of the API that is exposed by the gateway microservice can be found in the [`docs/API-document`](docs/API-document.md) document.

## Testing

We have properly tested this microservice using a variety of test frameworks and testing techniques. A document related to this topic can be found in the [`docs/Testing-documentation.md`](docs/Testing-documentation.md) document. This document contains a description of certain test conventions as well as manual/functional tests made with Postman.