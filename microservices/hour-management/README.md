# Hour Management Microservice

## Purpose
Hour Management is the microservice that offers service targeting declaration of work hours by TAs, an integral component to track and appropriately compensate working TAs. Its main workflow is as follows:
1. A TA declares their hours.
2. System does a few checks on these hours:
    - Whether TA belongs to the course.
    - Whether they have declared an appropriate amount of hours.
3. If the declaration is deemed appropriate, it is added to the system and it will wait for approval from a responsible lecturer.
4. A lecturer fetched all of the (unapproved) declarations and reviews them.
5. They either accept or reject this declaration:
    - If accepted, the status of declaration is turned to an accepted one and it resides permanently in the system.
    - If deleted, the declaration is deleted from the system.
6. A notification is sent to the TA informing them whether their declaration was accepted or rejected.

## Other functionalities
Apart from its main workflow, the hour management microservice offers various options for fetching declarations including but not limited to: fetching all declarations by the student and fetching declarations that belong to a set of courses.

It also offers a way of getting aggregation statistics, which is only limited to getting total hours spent by a TA per course at the moment. However, the additional statistics such as median, mode, the standard deviation can easily be integrated into the `/statistics` submodule.

## Some technical details
Many of the operations inside the microservice operate within Monos, Java's version of handling asynchronous futures. Both errors and successes are propagated within Monos in communication endpoints as well as the `validation chain` which has been implemented as a chain of responsibility. This allows the microservices to handle multiple users in parallel and reduces the chances of bottlenecking the system at crucial periods such as the last day of possible declarations.

### Validation Chain
The validation chain is an integral part of the microservice as it allows easy and extensible validation of users and requests. Currently, each component in the chain explicitly performs a single check, and should the chain be extended in the future, it is recommended to continue keeping the components granular. Each component operations within Mono's and either returns an error or passes along the request to the next component within the chain. If none of the components throw an exception a successful operation flag (boolean True) is returned from the chain.

For the sake of making it easy to construct the chain, a builder has been created as well. This allows each endpoint to easily construct **its own chain**.