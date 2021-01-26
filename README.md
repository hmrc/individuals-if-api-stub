
# individuals-if-api-stub

Usage 

 - This stub should be switched out with the IF Stub in the external test environment. 
 - This stub has a post endpoint for each IF endpoint that will allow the consumer to post the 
   payload they wish to get back from IF
 - On hitting the GET endpoints for a scoped request the stub should serve back the correct payload
 - When the consumer posts the JSON payload they will need to provide the `use case`
 - Use cases can vary per API; for example:
 
     - Individuals Details API
     
         - LAA-C3-residences
         - LAA-C4-residences
         - HMCTS-C3-residences
         - HMCTS-C4-residences
         - LSANI-C1-residences
         - LSANI-C4-residences
         - NICTSEJO-C4-residences
         - LAA-C4-contact-details
         - HMCTS-C4-contact-details
         
     - Individuals Benefits and Credits API
     
         - LAA-C1-working-tax-credit
         - LAA-C2-working-tax-credit
         - LAA-C3-working-tax-credit
         - LAA-C1-child-tax-credit
         - LAA-C2-child-tax-credit
         - LAA-C3-child-tax-credit
         - HMCTS-C2-working-tax-credit
         - HMCTS-C3-working-tax-credit
         - LSANI-C1-working-tax-credit
         - LSANI-C3-working-tax-credit
         
     - Individuals Employments API
     
         - LAA-C1
         - LAA-C2
         - LAA-C3
         - LAA-C4
         - LSANI-C1
         - LSANI-C3
         - HMCTS-C2
         - HMCTS-C3
         - HMCTS-C4
         - NICTSEJO-C4
         
     - Individuals Income API (PAYE)
     
         - LAA-C1
         - LAA-C2
         - LAA-C3  
         - LAA-C4  
         - HMCTS-C2
         - HMCTS-C3
         - HMCTS-C4
         - LSANI-C1
         - LSANI-C3
         - NICTSEJO-C4
         
     - Individuals Income API (SA)
     
         - LAA-C1
         - LAA-C2
         - LAA-C3  
         - LAA-C4  
         - HMCTS-C2
         - HMCTS-C3
         - HMCTS-C4
         - LSANI-C1
         - LSANI-C3
         - NICTSEJO-C4
         
         
Example API POSTs

 - Individual Details API
 
     - individuals/contact/details/nino/CS700100A?startDate=2019-01-01&endDate=2020-03-01&useCase=LAA-C4-residences
     - individuals/contact/details/nino/CS700100A?startDate=2019-01-01&endDate=2020-03-01&useCase=LAA-C4-contact-details
     
  - Individual Benefits and Credits API
  
      - individuals/tax-credits/nino/CS700100A?startDate=2019-01-01&endDate=2020-03-01&useCase=LAA-C1-working-tax-credit
      - individuals/tax-credits/nino/CS700100A?startDate=2019-01-01&endDate=2020-03-01&useCase=LAA-C1-child-tax-credit
 
 - Individual Employments API
 
     - individuals/employments/nino/CS700100A?startDate=2019-01-01&endDate=2020-03-01&useCase=LAA-C1
     
  - Individual Income API (PAYE)
  
      - individuals/income/paye/nino/CS700100A?startDate=2019-01-01&endDate=2020-03-01&useCase=LAA-C1
     
  - Individual Income API (SA)
  
      - individuals/income/sa/nino/CS700100A?startYear=2019&endYear=2020&useCase=LAA-C1  
      
                      
### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
