# Krok 1: Budowanie aplikacji Vite
FROM node:20-alpine AS build
WORKDIR /app

COPY package*.json ./
RUN npm install

COPY . .
RUN npm run build

COPY nginx.conf /etc/nginx/conf.d/default.conf
# Krok 2: Serwowanie plików z folderu /dist przez Nginx
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]