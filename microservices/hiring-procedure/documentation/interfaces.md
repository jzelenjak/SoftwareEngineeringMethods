# Hiring Procedure Interfaces

## `/api/hiring-procedure/apply?courseID=courseID`

Endpoint for sending out applications. The body of the request should contain the JWT of the user.

If the user that has made this request is not a `lecturer`, the response will be a `403 Forbidden` response. <br>
If the `courseID` is not valid, the response will be a `405 Method Not Allowed` response.<br>
If the user that has made this request has already applied for that course, the response will be a `405 Method Not Allowed` response.

## `/api/hiring-procedure/hire-TA?selectedID=netID&courseID=courseID`

Endpoint for hiring a candidate TA for a specific course. The body of the request should contain the JWT of the user.
Only users with the `lecturer` permission have access. `courseID` parameter specifies the course for which
the TA is selected. `selectedID` parameter specifies the netID of the TA to be hired.
`selectedID` has to be the netID of a candidate TA for that specific course.

If the user that has made this request is not a `lecturer`, the response will be a `403 Forbidden` response. <br>
If the `courseID` is not valid, the response will be a `405 Method Not Allowed` response.<br>
If the `selectedID` corresponds to a user that is not a candidate TA for that course, 
the response will be a `405 Method Not Allowed` response.

## `/api/hiring-procedure/get-applications?courseID=courseID`

Endpoint for retrieving all candidate TAs for a specific course. The body of the request should contain the JWT of
the user. Only users with the `lecturer` permission have access.

If the user that has made this request is not a `lecturer`, the response will be a `403 Forbidden` response. <br>
If the `courseID` is not valid, the response will be a `405 Method Not Allowed` response.

## `/api/hiring-procedure/get-contract?courseID=courseID`

Endpoint for requesting a template contract. The body of the request should contain the JWT of the user.
Only users with the `candidate TA` permission have access.

If the user that has made this request is not a `candidate TA`, the response will be a `403 Forbidden` response. <br>
If the `courseID` is not valid, the response will be a `405 Method Not Allowed` response.<br>
If the user that has made this request is not a candidate TA for that course, 
the response will be a `403 Forbidden`response.