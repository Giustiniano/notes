package com.example.notes.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Objects;


public class HateoasResponse {
    @JsonProperty
    private EmbeddedNotes _embedded;
    @JsonProperty
    private Links _links;
    @JsonProperty
    private Page page;


    public EmbeddedNotes get_embedded() {
        return _embedded;
    }

    public HateoasResponse set_embedded(EmbeddedNotes _embedded) {
        this._embedded = _embedded;
        return this;
    }

    public Links get_links() {
        return _links;
    }

    public HateoasResponse set_links(Links _links) {
        this._links = _links;
        return this;
    }

    public Page getPage() {
        return page;
    }

    public HateoasResponse setPage(Page page) {
        this.page = page;
        return this;
    }

    public static class Note {
        @JsonProperty
        private String id;
        @JsonProperty
        private String title;
        @JsonProperty
        private String created;
        @JsonProperty
        private String body;

        public String getId() {
            return id;
        }

        public Note setId(String id) {
            this.id = id;
            return this;
        }

        public String getTitle() {
            return title;
        }

        public Note setTitle(String title) {
            this.title = title;
            return this;
        }

        public String getCreated() {
            return created;
        }

        public Note setCreated(String created) {
            this.created = created;
            return this;
        }

        public String getBody() {
            return body;
        }

        public Note setBody(String body) {
            this.body = body;
            return this;
        }
    }

    public static class EmbeddedNotes {
        @JsonProperty
        private List<Note> notes;

        public List<Note> getNotes() {
            return notes;
        }

        public EmbeddedNotes setNotes(List<Note> notes) {
            this.notes = notes;
            return this;
        }
    }

    public static class Links {
        @JsonProperty
        private Link first;
        @JsonProperty
        private Link prev;
        @JsonProperty
        private Link self;
        @JsonProperty
        private Link next;
        @JsonProperty
        private Link last;

        public Link getFirst() {
            return first;
        }

        public Links setFirst(Link first) {
            this.first = first;
            return this;
        }

        public Link getPrev() {
            return prev;
        }

        public Links setPrev(Link prev) {
            this.prev = prev;
            return this;
        }

        public Link getSelf() {
            return self;
        }

        public Links setSelf(Link self) {
            this.self = self;
            return this;
        }

        public Link getNext() {
            return next;
        }

        public Links setNext(Link next) {
            this.next = next;
            return this;
        }

        public Link getLast() {
            return last;
        }

        public Links setLast(Link last) {
            this.last = last;
            return this;
        }

        public class Link {
            public String getHref() {
                return href;
            }

            public Link setHref(String href) {
                this.href = href;
                return this;
            }

            @JsonProperty
            private String href;
        }
    }
    public static class Page {
        private int size;
        private int totalElements;
        private int totalPages;
        private int number;

        public Page(){}

        public Page(int size, int totalElement, int totalPages, int number){
            this.size = size;
            this.totalElements = totalElement;
            this.totalPages = totalPages;
            this.number = number;
        }

        public int getSize() {
            return size;
        }

        public Page setSize(int size) {
            this.size = size;
            return this;
        }

        public int getTotalElements() {
            return totalElements;
        }

        public Page setTotalElements(int totalElements) {
            this.totalElements = totalElements;
            return this;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public Page setTotalPages(int totalPages) {
            this.totalPages = totalPages;
            return this;
        }

        public int getNumber() {
            return number;
        }

        public Page setNumber(int number) {
            this.number = number;
            return this;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Page page = (Page) o;
            return size == page.size && totalElements == page.totalElements && totalPages == page.totalPages && number == page.number;
        }

        @Override
        public int hashCode() {
            return Objects.hash(size, totalElements, totalPages, number);
        }
    }
}
