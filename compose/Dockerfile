FROM node:22-alpine AS web-build

WORKDIR /app/web

COPY web/package*.json ./
RUN npm ci

COPY web/ ./
RUN npm run build

FROM nginx:1.28-alpine

COPY --from=web-build /app/web/dist /usr/share/nginx/html

EXPOSE 80
