# Gateway - API Document

## Register microservices

```
POST /discovery/register/{serviceName}
```

Endpoint for registering new microservices to the registry server. The request body should look similar to the JSON object below. The name of the microservice is part of the path (`serviceName`).

This endpoint needs to be called periodically to keep the microservice _alive_. If this does not happen, the registry server will automatically invalidate the registration after **1 minute** has passed.

```json
{
  "host": "<hostname>",
  "port": 1234
}
```

| Response code   | Reason                           |
| --------------- | -------------------------------- |
| 400 BAD REQUEST | Invalid request body encountered |

---

```
GET  /discovery/{serviceName}
```

Endpoint for retrieving a single registration that is stored in the registry server. An example of a JSON response body can be found below. The `serviceName` path variable is used to indicate the type of microservice that a registration is requested of.

```json
{
  "host": "<hostname>",
  "port": 1234
}
```

| Response code | Reason                                         |
| ------------- | ---------------------------------------------- |
| 404 NOT FOUND | No registration found for the specified target |

---

## Forwarding requests

```
POST /api/{serviceName}/**
```

Endpoint for forwarding requests and responses. The central place of the gateway used by applications and other microservices to interact with each other. The call is forwarded if a valid registration exists, using a round-robin scheduling mechanism. All request attributes (e.g., headers, body, query parameters) are forwarded to the destination and/or caller.

| Response code | Reason                                         |
| ------------- | ---------------------------------------------- |
| 404 NOT FOUND | No registration found for the specified target |

**Note:** the response code above is the only invalid response that comes from the gateway. Because the gateway forwards response codes as well, the result will depend on the API of the targeted microservice.
