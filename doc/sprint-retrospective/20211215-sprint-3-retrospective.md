#GROUP 13a - Sprint Retrospective - 3 (09-12-2021 - 15-12-2021)

| User Story #                                                                            | Task #                                                        | Task assigned to | Estimated Effort per task (in hours) | Actual Effort (in hours) | Done (yes/no) | Notes |
|-----------------------------------------------------------------------------------------|---------------------------------------------------------------|------------------|--------------------------------------|--------------------------|---------------|-------|
| As an admin I want to add a new notification for a specific user.                       | Endpoint for creating a new notification                      | Andy             | 3                                    | 3                        | Yes           |       |
| As a lecturer I want to add a new notification for a specific user.                     | Endpoint for creating a new notification                      | Andy             | 2                                    | 2                        | Yes           |       |
| As an admin I want to get all the notifications for a specific user.                    | Endpoint for getting all notifications from a user            | Andy             | 2                                    | 2                        | Yes           |       |
| As an admin I want to change the user for an existing notification.                     | Endpoint for changing an existing notification                | Andy             | 2                                    | 2                        | Yes           |       |
| As an admin I want to change the message for an existing notification.                  | Endpoint for changing an existing notification                | Andy             | 2                                    | 1                        | Yes           |       |
| As an admin I want to delete existing notification(s) from a certain user.              | Endpoint for deleting an existing notification                | Andy             | 2                                    | 2                        | Yes           |       |
| As an admin I want to delete an existing notification specified by the notification id. | Endpoint for deleting an existing notification                | Andy             | 2                                    | 2                        | Yes           |       |
| As a user I want to see the notifications I may have upon login.			              | Fetching list of notifications upon login	                  | Andy             | 1                                    | 1                        | Yes           |       |
| Responsible lecturer can approve or reject the declared worked hours                    | Modify declaration endpoints to include specific courses      | Mehmet & Jeroen  | 4                                    | 3                        | Yes           |       |
| TAs can declare the worked hours per course                                             | Add component for checking time of a declaration              | Mehmet & Jeroen  | 3                                    | 3                        | Yes           |       |
| Responsible lecturer can approve or reject the declared worked hours                    | Allow hour declaration to send notifications                  | Mehmet & Jeroen  | 3                                    | 3                        | Yes           |       |
| Indicating actual spent hours                                                           | Aggregation statistics for declared hours per TA              | Mehmet & Jeroen  | 3                                    | 3                        | Yes           |       |
| Candidate TAs can withdraw their candidacy before the selection starts                  | Withdrawing candidacy                                         | Jeroen           | 3                                    | 3                        | Yes           |       |
| Responsible lecturer can select candidate TAs                                           | Rejecting applicants                                          | Jeroen           | 3                                    | 3                        | Yes           |       |
| Students cannot candidate for courses they failed                                       | Reject students that have failed the course                   | Jeroen           | 3                                    | 2                        | Yes           |       |
| N/A                                                                                     | Add API endpoint documentation (hour-management microservice) | Mehmet & Jeroen  | 3                                    | 3                        | Yes           |       |
| As an admin I want to add input to the system                                           | Endpoint for creating new grade for user                      | Pranav           | 1                                    | 3                        | Yes           |       |   
| As an admin I want to add input to the system                                           | Endpoint for deleting grade for user                          | Pranav           | 1                                    | 3                        | Yes           |       |   
| As an admin I want to add input to the system                                           | Endpoint for deleting course                                  | Pranav           | 1                                    | 3                        | Yes           |       |
| As a student I want to be able to candidate for a TA position                           | Endpoint for getting courses by code                          | Pranav           | 1                                    | 3                        | Yes           |       |
| As a student I want to be able to candidate for a TA position                           | Endpoint for getting courses by code                          | Pranav           | 1                                    | 3                        | Yes           |       |
| As a user, I want Users and Authentication microservices communicate with each other    | Users-Auth communication                                      | Jegor            | 10                                   | 25                       | Yes           |       |
| As a user, I want the system to have a root user                                        | Add a root user                                               | Jegor            | 2                                    | 5                        | Yes           |       |
| As a student/lecturer I want to be able to receive a contract to fill out and sign.     | Implement get-contract endpoint.                              | Mihnea           | 3                                    | 3                        | Yes           |       |
| N/A                                                                                     | Refactor hiring procedure to be fully asynchronous.           | Mihnea           | 3                                    | 8                        | Yes           |       |

**General notes:** combined branch coverage of 97% achieved during this sprint. Only certain parts of the course, and hiring-procedure microservices are not covered by the test suite yet.  

# Main problems encountered
### Problem 1

Description: Documented functionalities and API documentation of the individual microservices is lacking, or inconsistent. This can have a negative impact on other team members as they (heavily) rely on the functionality of other microservices.

Reaction: Document this type of information in a local README file **per** microservice. Ensure that the documentation format is consistent and well-explained.

### Problem 2

Description: The weights associated to the current MoSCoW issues are too high. The weights distort the burn-down chart of the active milestone (every Sprint has its own milestone).

Reaction: Either remove the weights from the MoSCoW issues (functional), or lower them to a more reasonable value.

# Adjustments for the next Sprint plan

Improve on the following aspects:

- Lower, or remove the weights from the MoSCoW issues (functional).
