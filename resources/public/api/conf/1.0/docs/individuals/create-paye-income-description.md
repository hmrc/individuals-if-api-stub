<p>Use this endpoint to create PAYE income test data for an individual.</p>
<p>This resource acts as a stub for the Integration Framework (IF). It accepts JSON payloads.</p>
<p>The JSON payloads must match the IF schema. Example IF payloads are provided.</p>
<p>As data is scoped, the JSON payload that you send should only contain the data items specific to the use case.</p>
<p>The paymentDate value provided in the POST body will be used to determine if the PAYE data sits within the date range 
provided in the query parameters, if they do not then the PAYE data will not be returned.</p>