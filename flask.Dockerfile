### 1. Get Linux
FROM openjdk:8

### 3. Get Python, PIP

RUN apt-get update && apt-get install -y python3.5 python3-dev python3-pip \
&& pip3 install --upgrade pip setuptools 


WORKDIR ./PDFToolServer
ENV FLASK_APP=/PDFToolServer/app.py
ENV FLASK_RUN_HOST=0.0.0.0
RUN apt-get install -y gcc musl-dev g++
COPY /PDFToolServer/requirements.txt requirements.txt
RUN pip3 install -r requirements.txt
RUN pip3 install JPype1
RUN pip3 install flask-cors
RUN export FLASK_ENV=development
EXPOSE 5000
COPY ./PDFToolServer/ .
CMD ["flask", "run", "--host=0.0.0.0"]
