Goal:
A web app for manipulating pdf files: includes the ability to merge png, jpg, pdf pages at once. Also to delete pages in pdf 

Docker

Languages:
Python (flask): Web API
JavaScript (React): Web Application
Java: Utility library for modifying pdfs

Communication methods:
Python -> Java: calling Java library methods from Python using Jpype, which I believe uses the JNI for communcating between Python and Java
JavaScript -> Python: making REST requests to Python server from JavaScript client code

Steps to start project:
- Make sure there are no existing containers named flask or react
- docker-compose build
- docker-compose up

Features:
- custom java library (PDFTool) for manipulating pdfs

- react app:
- convert jpg, png files to pdf
- combine jpg, png, pdf files, then hit merge to convert, then download the merged pdf
- upload a new file, and merge again to append to the previously merged pdf
- click red x on thumbnail to remove file or pdf page
