# GROUP 13a - Sprint Retrospective - 5 (25-12-2021 - 09-01-2022)

**Note:** Sprint 5 was extended due to the Christmas holiday.

| User Story #                                                                                           | Task #                                                                         | Task assigned to | Estimated Effort per task (in hours) | Actual Effort (in hours)            | Done (yes/no) | Notes |
|--------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------|------------------|--------------------------------------|-------------------------------------|---------------|-------|
| As an admin I want to get all the users with a specified first name.                                   | Endpoint for getting first name                                                | Andy             | 1                                    | 1                                   | Yes           |       |
| As an admin I want to get all the users with a specified last name.                                    | Endpoint for getting last name                                                 | Andy             | 1                                    | 1                                   | Yes           |       |
| As an admin I want to change the first name of a specific user                                         | Endpoint for changing first name                                               | Andy             | 1                                    | 1                                   | Yes           |       |
| As an admin I want to change the last name of a specific user                                          | Endpoint for changing last name                                                | Andy             | 1                                    | 1                                   | Yes           |       |
| N/A                                                                                                    | Removal and replacing the switch statements in authentication.                 | Andy             | 1                                    | 1                                   | Yes           |       |
| As a responsible lecturer, I could be able to ask the system to recommend the candidate TAs to hire based on their past grades and previous experience in TAing.                        | Endpoint for getting all courses for a course id which has the same course code| Pranav             | 1                                    | 2                                   | Yes           |       |
| As a responsible lecturer, I could be able to ask the system to recommend the candidate TAs to hire based on their past grades and previous experience in TAing.                        | Endpoint for getting all JSON which contains user ID's and grades               | Pranav             | 1                                    | 2                                   | Yes           |       |
| As a user of the system, I could be able to get help or consult the documentation | Add all the documentation for making use of the microservice| Pranav | 3 | 3 | N/A |  |
| As a user of the system, I could be able to get help or consult the documentation | Add all documentation for testing | Pranav | 3 | 3 | N/A | |
| N/A                                                                                                    | "Application" to "Submission" refactor                                         | Mihnea Toader    | 2                                    | 4                                   | Yes           |       |
| Student and lecturers should receive contracts to fill out and sign                                    | Customized contracts                                                           | Mihnea Toader    | 5                                    | 5                                   | Yes           |       |
| N/A                                                                                                    | Add check to hiring procedure that lecturer is associated to course            | Mihnea & Jeroen  | 2                                    | 2                                   | Yes           |       |
| N/A                                                                                                    | Add manual testing document users and add missing endpoints to the API document| Jegor            | 3                                    | 8                                   | Yes           |       |
| N/A                                                                                                    | Add a document for recommendation                                              | Jegor            | 1                                    | 1                                   | Yes           |       |
| As a student, I should not be able to candidate myself for more than three courses in the same quarter | Limit student candidacy to three courses                                       | Jeroen           | 2                                    | 5                                   | Yes           |       |
| As a user of the system, I could be able to get help or consult the documentation.                     | Refactor repository README                                                     | Jeroen           | 1                                    | 1                                   | Yes           |       |
| As a user of the system, I could be able to get help or consult the documentation.                     | Add manual testing document for the gateway                                    | Jeroen           | 1                                    | 1.5                                 | Yes           |       |
| N/A                                                                                                    | Refactoring of the hour-management service and additional statistics           | Mehmet           | 3                                    | 3.5                                 | Yes           |       |
| As a user of the system, I could be able to get help or consult the documentation.                     | Add manual testing document hour-management and resolve endpoint consistencies | Mehmet & Jeroen  | 3                                    | 6.5                                 | Yes           |       |
| N/A                                                                                                    | Add lecturer validator                                                         | Mehmet & Jeroen  | 2                                    | 2                                   | Yes           |       |
| N/A                                                                                                    | Enable pitest for gateway and hour-management                                  | Mehmet & Jeroen  | 2                                    | 2                                   | Yes           |       |
| N/A                                                                                                    | Minor improvements 'General Package' (hour-management + gateway)               | Mehmet & Jeroen  | 3                                    | 3                                   | Yes           |       |


# Main problems encountered
### Problem 1
**Description:** Due to our system having a lot of constraint checks, it took more effort than what we estimated to set up an artificial environment for manual testing. 

To give an example for constraint: a student can apply for being a TA only three weeks before the course starts. However, in order to declare hours, that students needed to be a TA for the course. As we couldn't create a course entity that started three weeks ago (system automatically timestamps the creation date), we had no way of declaring hours during manual testing.

**Reaction:** We manually inserted a course entity with a start date of at least three weeks in the future to ensure that we could have a student entity apply to become a TA. After accepting that student to be a TA, we modified the course entities start date so that the course was treated as if it was started. This allowed us to ensure that we tested all possible functionalities offered by our system.

### Problem 2
**Description:** We also had some API mismatches between the microservice that needed to be addressed for the system as a whole to function as intended.

**Reaction:** We consulted with each other as a team, and fixed most of the mismatches in a branch. Especially, the hour-declaration microservice communicates with all of other microservices. While testing it, a lot of mismatches were resolved.

# Adjustments for the next Sprint plan

Improve on the following aspects:

- Better time keeping (use GitLab's time tracking feature). This allows us to better reflect on the issues we worked on.

**Note:** the deadline for the project is 10-01-2022 at 09:00. The assignments related to refactoring and mutation testing, which are performed after the project deadline do not require an extensive Sprint planning.

