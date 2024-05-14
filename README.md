# Notes

This is the backend for an application allowing users to create, read, update and delete notes.

Such notes MUST have a title and a text body, and MAY be tagged as `BUSINESS`, `PERSONAL` and `IMPORTANT`

The endpoint root is `/api/v1/notes`

## Notes creation

to create a note, send a `POST` request to the endpoint, with the `content-type` header set as `application/json` and the following json body:

* `title` string, required
* `body`  string  required
* `tags`  string array, optional, each item must be one of `BUSINESS`, `PERSONAL` or `IMPORTANT`

upon successful completion of the request, the endpoint will return the `HTTP_CREATED` (201) status code and a json response object with the same elements plus:

* `id` string, the UUIDv4 associated to this note
* `created_date` the UTC ISO 8501 timestamp of when the note was created

### Status codes

* `CREATED (201)` if the note is successfully created
* `BAD REQUEST` if title or body are either missing, empty or blank

### Example

if the body of the POST request is
```json
{
  "title": "My first note", 
  "body": "This is my first note", 
  "tags": ["PERSONAL"]
}
```

the successful response will be 
```json
{
  "id": "fa15260f-505d-4695-a786-e57ef3ee5987",
  "title": "My first note",
  "body": "This is my first note",
  "tags": ["PERSONAL"],
  "created": "2024-05-14T07:37:46.342518+00:00"
}
```