# Hiring Procedure Interfaces

## `/api/hiring-procedure/apply?courseID=courseID`

Endpoint for sending out applications. The body of the request should contain the JWT of the user's session. 
Only users with `student` permissions have access. 
The `courseID` parameter specifies the course for which the user wishes to apply.

If the user that has made this request is not a `student`, the response will be a `403 Forbidden` response. <br>
If the `courseID` is not valid, the response will be a `404 Not Found` response.<br>
If the time that this request has been made is closer than 3 weeks to the start of the course, 
the response will be a `405 Method Not Allowed` response.<br>
If the user that has made this request has already applied for that course, 
the response will be a `405 Method Not Allowed` response.

## `/api/hiring-procedure/withdraw?courseID=courseID`

Endpoint for withdrawing applications. The body of the request should contain the JWT of the user's session.
Only users with `student` permissions have access.
The `courseID` parameter specifies the course for which the user wishes to apply.

If the user that has made this request is not a `student`, `admin`, or `TA`, the response will be a `403 Forbidden` response. <br>
If the user that has made this request has not applied for that course, the response will be a `405 Method Not Allowed` response.
If the application has already been processed, the response will be a `405 Method Not Allowed` response.

## `/api/hiring-procedure/reject?applicationId=applicationId`

Endpoint for rejecting applications. The body of the request should contain the JWT of the user's session.
Only users with `lecturer` permissions have access.
The `applicationId` parameter specifies the application that must be rejected.

If the user that has made this request is not a `lecturer`, or `admin`, the response will be a `403 Forbidden` response. <br>
If application does not exist, the response will be a `404 Not Found`. <br>
If the application has already been processed, the response will be a `405 Method Not Allowed` response.

## `/api/hiring-procedure/hire-TA?userID=userID&courseID=courseID`

Endpoint for hiring a candidate TA for a specific course. The body of the request should contain the JWT of the user's 
session. Only users with the `lecturer` permission have access. The `courseID` parameter specifies the course for which
the TA is selected. The `userID` parameter specifies the userID of the TA to be hired.
The `userID` has to be the userID of a candidate TA for that specific course.

If the user that has made this request is not a `lecturer`, the response will be a `403 Forbidden` response. <br>
If the `courseID` is not valid, the response will be a `404 Not Found` response.<br>
If the `userID` corresponds to a user that is not a candidate TA for that course, 
the response will be a `405 Method Not Allowed` response.<br>
If the `userID` corresponds to a user that has already been hired to that course, 
the response will be a `405 Method Not Allowed` response.<br>
If the `userID` corresponds to a non-existing user, the response will be a `404 Not Found` response.

## `/api/hiring-procedure/get-all-applications?courseID=courseID`

Endpoint for retrieving all candidate TAs for a specific course. The body of the request should contain the JWT of
the user's session. Only users with the `lecturer` permission have access.
The `courseID` parameter specifies the course for which the applications should be retrieved.

If the user that has made this request is not a `lecturer`, the response will be a `403 Forbidden` response. <br>
If the `courseID` is not valid, the response will be a `404 Not Found` response.

## `/api/hiring-procedure/get-applications?userID=userID`

Endpoint for retrieving all applications of one specific user. The body of the request should contain the JWT of
the user's session. Only users with the `lecturer` permission or the user associated with the `userID` have access.
The `userID` parameter specifies the userID for which the applications should be retrieved.

If the user that has made this request is not a `lecturer` or the user associated with the userID, 
the response will be a `403 Forbidden` response. <br>
If the `userID` corresponds to a non-existing user, the response will be a `404 Not Found` response.

## `/api/hiring-procedure/get-contract?courseID=courseID`

Endpoint for requesting a template contract. The body of the request should contain the JWT of the user's session.
Only users with the `candidate TA` permission have access.
The `courseID` parameter specifies the course for which the contract should be retrieved.

If the user that has made this request is not a `candidate TA`, the response will be a `403 Forbidden` response. <br>
If the `courseID` is not valid, the response will be a `404 Not Found` response.<br>
If the user that has made this request is not a candidate TA for that course, 
the response will be a `403 Forbidden` response.