#GROUP 13a - Sprint Retrospective #2
| User Story #                                                         | Task #                                   | Task assigned to | Estimated Effort per task (in hours) | Actual Effort (in hours) | Done (yes/no) | Notes                    |
|----------------------------------------------------------------------|------------------------------------------|------------------|--------------------------------------|--------------------------|---------------|--------------------------|
| TA can declare the worked hours per course                           | Limit on hour declaration for TAs        | Mehmet & Jeroen  | 5                                    | 6                        | Yes           |                          |
| As an admin I can change the password for every user.                | Authentication change password endpoint  | Andy             | 3                                    | 3                        | Yes           |  Changed the way change password works, since this did not actually work when the user was previously logged in. |
| As an admin I can change a user their role.                          | Authentication change role endpoint      | Andy             | 4                                    | 4                        | Yes           | Tested as well. Achieves 100% branch coverage.     |
| As an admin I can delete a user specified by username.               | Authentication delete user endpoint      | Andy             | 2                                    | 1                        | Yes           | Tested as well. Achieves 100% branch coverage.               |
| As a new user I can register myself.                                 | User registration endpoint               | Jegor            | 2                                    | 1                        | Yes           | Tested as well. Achieves 100% branch coverage.               |
| As an admin I can delete a user by userId.                           | User deletion endpoint                   | Jegor            | 2                                    | 1                        | Yes           | Tested as well. Achieves 100% branch coverage.               |
| TA permissions only for the TAed course                              | Authentication and Authorization support for hour management | Mehmet & Jeroen  | 5                                  | 4                        | Yes            |     |
| Sending out contracts                                                | Endpoint for sending out contracts       | Mihnea Toader    | 5                                    | 0                        | No            | ...              |
| Reject applicant                                                     | Endpoint for rejecting                   | Mihnea Toader    | 3                                    | 0                        | No            | ...              |
| Send notification to candidate TA                                    | Send notification to auth microservice   | Mihnea Toader    | 4                                    | 0                        | No            | ...              |
| Reject students that have failed the course                          | Automatically reject if course is failed | Mihnea Toader    | 3                                    | 0                        | No            | ...              |
| Student cannot candidate for more than three courses                 | Change apply endpoint to support this    | Mihnea Toader    | 2                                    | 0                        | No            | ...              |
| Finish base endpoints for Hiring Procedure                           | Testing for endpoints                    | Mihnea Toader    | 7                                    | 12                       | Yes           | ...              |
| As an admin, I can create new courses                                | Creating the endpoint for making new courses | Pranav Parankusam| 4                                    | 10                       | No            | Needs to add authentication to the endpoint |

# Main problems encountered
### Problem 1

Description: Too many issues during a sprint.

Reaction: Split up the work more efficiently. Furthermore, all issues should have weights assigned to them.

### Problem 2

Description: Some issues are created during the sprint, instead of during the start of the sprint.

Reaction: Create *all* issues during the start of the sprint.

### Problem 3

Description: Perform the dev to main merge earlier.

Reaction: In order to help the TA with his review process, we should merge the dev branch into main on wednesday **before** 17:00.

# Adjustments for the next Sprint plan

Improve on the following aspects:

- Better backlog for the current sprint, list only the issues that can be completed **during** the same sprint.
- Create the new issues during the start of the sprint.
- Ensure that all contents of the dev branch are merged into the main branch before 17:00 on wednesdays.
