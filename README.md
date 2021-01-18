
# individuals-if-api-stub

This is a placeholder README.md for a new repository

How does this work? 

 - This stub should be switched out with the IF Stub in the external test environment. 
 - This stub has a post endpoint for each data type that will allow the consumer to post the 
   payload they wish to get back from IF
 - On hitting the GET endpoints for a scoped request the stub should serve back the correct payload

TODO - What's missing from this stub? 

 - Endpoints store and fetch only for a single key. I.e. Nino. They do not have a date range, fields or filter.
 - A means of storing key'd on more than just a nino
 - This stub needs to behave exactly the IF-Stub however; pulls filed from mongo rather than from a directory 
 - We can't expect the consumer to pass in a fields listing... Could they pass in a date range and who they are?
 - I.e store with key: nino_<thenino>_from_to_use-case -> nino_CS700100A_2019-01-01_2019-03-01_LAA-C1
 - If they can provide us with this info and a Json payload we can store with the above key and fetch in the 
   exact way IF stub does. I.e. mapping the fields param to a use case!
 - I do not think we need to think about the filter param (details) as the consumer should post a cut down version of the json  

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
