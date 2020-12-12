FROM node:14-buster

#RUN npm install --build-from-source zeromq@6.0.0-beta.5 

# set working directory
WORKDIR /pdf-tool-app

# add `/pdf-tool-app/node_modules/.bin` to $PATH
ENV PATH /pdf-tool-app/node_modules/.bin:$PATH

# install app dependencies
COPY /pdf-tool-app/package.json ./
COPY /pdf-tool-app/package-lock.json ./
RUN npm install

# add app
COPY /pdf-tool-app ./

# start app
CMD ["npm", "start"]    
