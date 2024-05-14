This is the backend for an application allowing users to create, read, update and delete notes.

Such notes MUST have a title and a text body, and MAY be tagged as `BUSINESS`, `PERSONAL` and `IMPORTANT`

The endpoint root is `/api/v1/notes`

** nodes creation **

to create a note, send a POST request to the endpoint, with the `content-type` header set as `application/json` and the following json body:

`title` string, required
`body`  string  required
`tags`  string[] optional, each item must be one of `BUSINESS`, `PERSONAL` or `IMPORTANT`

