FROM nginx:1.29-alpine
RUN mkdir -p /var/cache/nginx/fiscal_static
COPY infra/nginx/nginx.conf /etc/nginx/nginx.conf
EXPOSE 8080

