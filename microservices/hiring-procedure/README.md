# Hiring Procedure Microservice

## Purpose
Hiring Procedure is the microservice that allows users to perform any and all actions related to applying for a TA position. In short:
1. A student applies for a TA position in a course they have previously completed.
2. The system runs some eligibility check.
3. Remaining applications can be approved or rejected by the responsible lecturers.
4. To complete the hiring process, the hired students may also retrieve their contracts to sign.
5. A notification is then sent to the student upon login informing them of the decision.


## Other functionalities
Apart from the basic pipeline described above, the microservice offers a variety of helpful functionalities, especially to lecturers.
Lecturers may retrieve recommendations from the server, based on some specified parameters. Lecturers may also see the students' previous TA experience to help them make more meaningful hiring choices.

## Some technical details
Every endpoint depends on a custom `Chain of Responsibility`, called the `Validation Chain`, to check for authentication tokens, permissions etc.
Many of the operation within this microservice operate asynchronously, through the use of `Mono`s.
Recommendations are generated using a `Strategy` design pattern, letting the client choose their preferred filtering method.

### Validation Chain
The validation chain is an integral part of the microservice as it allows easy and extensible validation of users and requests. Currently, each component in the chain explicitly performs a single check, and should the chain be extended in the future, it is recommended to continue keeping the components granular. Each component operations within Mono's and either returns an error or passes along the request to the next component within the chain. If none of the components throw an exception a successful operation flag (boolean True) is returned from the chain.

For the sake of making it easy to construct the chain, a builder has been created as well. This allows each endpoint to easily construct **its own chain**.

### Recommendation Strategy
To help ease the workload of lecturers, the server is able to provide recommendations based on some parameters. However, we understand that the decisions taken by a lecturer might not conform to the decision taken by another. Therefore, we give users flexibility in choosing the recommendation method through the use of a `Strategy` design pattern.


## API

A description of the API that is exposed by the hiring procedure microservice can be found in the [`docs/API-document`](docs/API-document.md) document.

## Testing

We have properly tested this microservice using a variety of test frameworks and testing techniques. A document related to this topic can be found in the [`docs/Testing-documentation.md`](docs/Testing-documentation.md) document. This document contains a description of certain test conventions as well as manual/functional tests made with Postman.
