FROM node:12.22 as node

ENV REACT_APP_API_URL=/api

RUN git clone https://github.com/serp-ya/card-transfer.git card-transfer

WORKDIR ./card-transfer

RUN npm install

RUN npm run build

FROM nginx

COPY --from=node /card-transfer/build /usr/share/nginx/html

COPY nginx-default.conf /etc/nginx/conf.d/default.conf
