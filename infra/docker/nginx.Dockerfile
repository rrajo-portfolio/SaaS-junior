FROM nginx:1.27-alpine
RUN apk add --no-cache apache2-utils \
  && mkdir -p /var/cache/nginx/fiscal_static \
  && touch /etc/nginx/conf.d/public-auth.conf
COPY infra/nginx/nginx.conf /etc/nginx/nginx.conf
EXPOSE 8080
