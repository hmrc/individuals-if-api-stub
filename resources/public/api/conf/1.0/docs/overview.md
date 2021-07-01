The Integration Framework (IF) Test Support API allows you to use custom data to test the following APIs:

- Individuals Employments (V2)
- Individuals Income (V2)
- Individuals Benefits and Credits (V1)
- Individuals Details (V1)
- Organisations Matching (V1)
- Organisations Details (V1)

External Test is a non-integrated environment. This API allows you to stub IF JSON response objects.

The endpoints in the IF Test Support API mirror the endpoints in IF that the Individuals and Organisations APIs get their data from.

Before calling the endpoints, you will need to create a test user using the [Create Test User API](https://developer.service.hmrc.gov.uk/api-documentation/docs/api/service/api-platform-test-user/1.0).

The test user will either be an individual or an organisation. You will need to create the relevant type of test user for the endpoint you want to post data to.

You can call each endpoint once with the appropriate type of test user. For example, you can call one or more of the Individuals endpoints with the same user. Yet, you will not be able to call the same endpoint twice. The data cannot be updated so you will need to create another test user to post data to the endpoint again. 
