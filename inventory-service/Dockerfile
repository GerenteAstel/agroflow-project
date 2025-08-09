FROM node:18-alpine  
WORKDIR /app         
COPY package*.json ./ 
RUN npm install      # Instala dependencias
COPY . .             
EXPOSE 3000          
CMD ["node", "server.js"]  