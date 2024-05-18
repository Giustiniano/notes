# Notes

This is the backend for an application allowing users to create, read, update and delete notes.

Such notes MUST have a title and a text body, MAY be tagged as `BUSINESS`, `PERSONAL` and `IMPORTANT`
and MAY have a little endian creation date (dd/mm/yyyy)

The endpoint root is `/api/v1/notes`

## Notes creation

to create a note, send a `POST` request to the endpoint, with the `content-type` header set as `application/json` and the following json body:

* `title` string, required
* `body`  string  required
* `tags`  string array, optional, each item must be one of `BUSINESS`, `PERSONAL` or `IMPORTANT`
* `created` date string, optional, must be formatted as dd/mm/yyyy, e.g. `15/05/2024`

upon successful completion of the request, the endpoint will return the `HTTP_CREATED` (201) status code and a json response object with the same elements plus:

* `id` string, the UUIDv4 associated to this note

### Status codes

* `CREATED` if the note is successfully created
* `BAD REQUEST` if title or body are either missing, empty or blank, if the tags are illegal or the date is badly 
formatted

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
}
```
## Note update
to update a note, send a `PUT` request to the endpoint root with the note id that you want to update as a path variable
e.g. /api/v1/note/fa15260f-505d-4695-a786-e57ef3ee5987

The format for the request body and its response are the same as for the `POST` endpoint, with the following additions:
* the path variable must be a valid UUIDv4

### Status codes
`OK` if the note update was successful
`BAD REQUEST` see `POST` endpoint
`NOT FOUND` if there's no note with the supplied id

## List notes
To get the list of available notes, send a `GET` request to the endpoint root.
Pagination is supported: the page number (starting at 0) can be passed as the `page` query parameter,
the page size can be passed as the `size` query parameter.

The response will contain the title and the creation date of each note (if present), plus the pagination links and
information about pagination, such as:
* the current page
* how many pages are present
* how many elements are present

e.g.
```json
{
  "_embedded" : {
    "notes" : [ {
      "id" : "818b6f5e-7db9-4834-99ab-ef5e6fd5b12d",
      "title" : "Tagged note",
      "created" : "15/05/2024"
    } ]
  },
  "_links" : {
    "first" : {
      "href" : "http://localhost:53561/api/v1/note?page=0&size=3"
    },
    "prev" : {
      "href" : "http://localhost:53561/api/v1/note?page=0&size=3"
    },
    "self" : {
      "href" : "http://localhost:53561/api/v1/note?page=1&size=3"
    },
    "last" : {
      "href" : "http://localhost:53561/api/v1/note?page=0&size=3"
    }
  },
  "page" : {
    "size" : 3,
    "totalElements" : 3,
    "totalPages" : 1,
    "number" : 1
  }
}
```
### Status codes

`OK` if the request is successful

## Get a note body
To get a note body (the text of the note). send a `GET` request to `api/v1/notes/body/<UUID of the note>`

The response is a single JSON object, with a single `body` key containing the note body.
e.g.
```json
{"body":"Hello hello world!"}
```