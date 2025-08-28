
# individuals-if-api-stub

Usage 

 - This stub should be switched out with the IF Stub in the external test environment 
 
 - This stub has a post endpoint for each IF endpoint that will allow the consumer to post the 
   payload they wish to get back from IF
   
 - On hitting the GET endpoints for a scoped request the stub should serve back the correct payload
 
 - When the consumer posts the JSON payload they will need to provide the `use case`
 
 - Use cases can vary per API. Please see the API documentation for usage.

### Running tests

All the  tests with coverage report can be run with the following:

     sbt clean compile coverage test it/test coverageReport
                      
### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
