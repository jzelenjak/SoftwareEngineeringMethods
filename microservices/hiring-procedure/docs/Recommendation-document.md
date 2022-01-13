# Recommendation


## Introduction

We wanted to include Strategy design pattern as part of the assignment 1 by making recommendation use it. However, due to the lack of time and due to the fact that we already had two design patterns, we agreed not to include Strategy into assignment 1. Nevertheless, we still have decided that we will finish it, even though it will not be part of the assignment.


## Why

The Strategy design pattern enables users to select the behaviour of the program at runtime. This pattern is especially useful in situations where the algorithm should independently vary depending on the client’s request.


## How

In our case, we use the Strategy pattern to send out TA recommendations based on the lecturer’s preference. To do so, we provide a `RecommendationStrategy` interface upon which strategies are implemented. The following four recommendation methods are implemented:

`TotalTimesSelectedStrategy`: prioritize the candidate TA’s that _have been selected the most for a TA position **for any course**_.

`TimesSelectedStrategy`: prioritize the candidate TA’s that _have been selected the most for a TA position **for a certain course** (with a certain course code)_.

`GradeStrategy`: prioritize the candidate TA’s that _have the highest grade for **for a certain course** (with a certain course code)_.

`HoursSpentStrategy`: prioritize the candidate TA’s that _have the most logged hours as a TA **in the previous editions of a certain course**_.


## StrategyFactory class

We have created a class `StrategyFactory` that creates an object of type `RecommendationStrategy` based on the desired type of strategy.


## Asynchronous communication with other microservices

`TotalTimesSelectedStrategy` is completely local and has no communication with other microservices.

`TimesSelectedStrategy` sends a request to Course microservice to resolve course ID into course codes for all the editions of that course.

`GradeStrategy` sends a request to Course microservice to get the recommendations, since the data about grades is stored in Course microservice.

`HoursSpentStrategy` sends a request to Course microservice to resolve course ID into course codes for all the editions of that course, and also to Hour Management microservice get the recommendations, since the data about hours is stored in Hour Management microservice.


## API

`RecommendationController` that handles HTTP requests has only one endpoint for recommendation:

```
POST   /api/hiring-procedure/recommendations/recommend
```

This endpoint requires **lecturer** or **admin** privileges.

The information with respect to the recommendation is placed in the body of the request. This request body should look similar to the JSON object below.

```json
{
	"courseId" : 23453,
	"amount" : 10,
	"minValue" : 100.0,
	"strategy" : "HOURS"
}
```

Note that the `strategy` field must be one of the following: _"TOTAL_TIMES_SELECTED", "TIMES_SELECTED", "GRADE", "HOURS"_. `amount` field is the maximum number of returned recommendations. `minValue` field is the metric for the recommendation (e.g number of times selected, number of hours, grade). `courseId` field is the unique ID for a course of interest.

Upon calling this endpoint, a list of recommendations is returned in a JSON format. See the example below. 

```json
[
  {
	"userId" : 5252112,
	"metric" : 9.0
  },
  {
    "..." : "..."
  }
]
```

| Response code    | Reason                                                            |
|------------------|-------------------------------------------------------------------|
| 200 OK           | Successful completion                                             |
| 400 BAD REQUEST  | Invalid request format                                            |
| 401 UNAUTHORIZED | JWT token is missing or does not start with 'Bearer '             |
| 403 FORBIDDEN    | The operation is forbidden for the requester                      |
| 404 NOT FOUND    | No applicants have been found or no recommendations could be made |
| 4xx              | Error while communicating with other microservices                |


## Further Notes
This is open to further extension if desired by the developers, by implementing new strategy classes. With the HTTP request, the lecturer must also specify the selected strategy. Therefore, new strategies must be communicated to the client. 