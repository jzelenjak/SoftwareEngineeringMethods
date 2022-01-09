# GROUP 13a - Sprint Retrospective - 4 (16-12-2021 - 24-12-2021)

| User Story #                                                                                     | Task #                                                                                         | Task assigned to | Estimated Effort per task (in hours) | Actual Effort (in hours) | Done (yes/no) | Notes |
|--------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------|------------------|--------------------------------------|--------------------------|---------------|-------|
| Students should only be able to declare hours up to a maximum value                              | Endpoint for retrieving the maximum hours per contrat                                          | Mihnea Toader    | 4                                    | 5                        | Yes           |       |
| Lecturers should be able to rate their experience with a TA                                      | Endpoint for rating TAs                                                                        | Mihnea Toader    | 4                                    | 5                        | Yes           |       |
| Students and lecturers should receive contracts to fill out and sign                             | Endpoint for sending out contracts                                                             | Mihnea Toader    | 3                                    | 3                        | Yes           |       |
| As a student who applied for a TA job, I want to get notified about the result of my submission. | Modifying endpoints approving/rejecting submission.                                            | Andy             | 4                                    | 6                        | Yes           |       |
| N/A                                                                                              | API for authentication                                                                         | Andy             | 2                                    | 2                        | Yes           |       |
| N/A                                                                                              | Manual testing with Postman                                                                    | Andy             | 7                                    | 7                        | Yes           |       |
| N/A                                                                                              | Removal of TA role in authentication branch                                                    | Andy             | 2                                    | 1.5                      | Yes           |       |
| The admin can give input to the system                                                           | Added Lecturer entity                                                                          | Pranav           | 1                                    | 1                        | yes           |       |
| Responsible Lecturers can select TA candidates                                                   | Added Endpoint for fetching the courses (ids) which the lecturer teaches                       | Pranav           | 1                                    | 2                        | yes           |       |
| Responsible lecturer can approve or reject the declared hours of a TA                            | Added Endpoint for checking whether lecturer teaches courses                                   | Pranav           | 1                                    | 2                        | yes           |       |
| The admin can give input to the system                                                           | Added Endpoint for assigning lecturer to the course                                            | Pranav           | 1                                    | 2                        | yes           |       |
| N/A                                                                                              | API for Users and Authentication (excluding notifications)                                     | Jegor            | 2                                    | 3                        | Yes           |       |
| N/A                                                                                              | Remove TA role from Users and add validation for get endpoints                                 | Jegor            | 4                                    | 5                        | Yes           |       |
| N/A                                                                                              | README.md for Users                                                                            | Jegor            | 1                                    | 1                        | Yes           |       |
| N/A                                                                                              | Remove TA role from Users and change the permissions for change role                           | Jegor            | 3                                    | 5                        | Yes           |       |
| N/A                                                                                              | Refactor API document lay-out                                                                  | Jeroen           | 3                                    | 3                        | Yes           |       |
| N/A                                                                                              | Refactor hour-management GET endpoints (remove body)                                           | Mehmet & Jeroen  | 2                                    | 2                        | Yes           |       |
| N/A                                                                                              | Users should be able to view their own declarations                                            | Mehmet & Jeroen  | 2                                    | 2                        | Yes           |       |
| N/A                                                                                              | Create endpoint for retrieving the amount of hours TAd by several students for several courses | Mehmet & Jeroen  | 2                                    | 3                        | Yes           |       |
| As a user of the system, I could be able to get help or consult the documentation                | Add documentation for gateway microservice                                                     | Mehmet & Jeroen  | 2                                    | 2                        | Yes           |       |
| N/A                                                                                              | Modify response codes of reject/approve endpoints                                              | Mehmet & Jeroen  | 1                                    | 1                        | Yes           |       |
| As a user of the system, I could be able to get help or consult the documentation                | Add documentation for hour-management microservice                                             | Mehmet & Jeroen  | 2                                    | 2                        | Yes           |       |
| N/A                                                                                              | Add the hiring / course time validators to chains at respective endpoints                      | Mehmet & Jeroen  | 1

**General notes:** combined branch coverage of 99% achieved during this sprint. Only certain parts of the course, and hiring-procedure microservices are not covered by the test suite yet.

# Main problems encountered
### Problem 1

Description: Document format mismatch. Some docs for API or READMEs did not have explicit formats put in place from the beginning.

Reaction: Use formats for the documents that everyone agrees upon. Documents must be reformatted.

# Adjustments for the next Sprint (plan)

Improve on the following aspects:

- Attempt to provide more extensive feedback for the merge requests. This is not something that always happens, hence improvement is required.

**Note:** since sprint 5 is _extended_ and takes place during the Christmas holiday, it will be less-strictly enforced during the first week of the sprint.


