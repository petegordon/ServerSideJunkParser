
This uses jsoup to analyze server side code (JSP, INC, HTML) to find where text is used.  Say you wanted to find all the text so you could create internationalized dictionaries in an existing application.  That's what I needed.  <smile>

Build the application:
mvn clean install


Run the application:
mvn exec:java -Dexec.mainClass="ServerSideJunkParser.App" -Dexec.args="[THE PATH TO YOUR DIRECTORY WITH JSP, INC, HTML FILES TO SEARCH AND REPORT ON]"


The output:
A tab deliminated file of the Files, HTML elements, HTML element classes, HTML element id, and HTML element "own text", and the entire HTML element.

