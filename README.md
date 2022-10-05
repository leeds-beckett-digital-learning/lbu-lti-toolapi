# lbu-lti-toolapi

This package builds on the lbu-lti library to provide developers with a 
simple framework for the creation of sets of LTI tools.

## Typical Tool

A typical tool provides a single JSP page which acts as the entrypoint for
users. This is backed by a supporting Java class which helps to initialise
that page. The tool also implements a WebSocket endpoint which the JSP page
connects to. The interactive functionality of the tool is implemented by
having the JSP page communicate with the WebSocket endpoint using messages
passing in both directions.

The storage of data relating to the LTI resource is typically implemented
using a JavaCache backed by ultimate storage as JSON files. The application
must either include its own JavaCache implementation or must depend on the
web application container including one.

