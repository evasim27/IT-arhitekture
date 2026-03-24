FROM node:20-alpine

WORKDIR /usr/src/app

COPY package*.json ./
RUN npm install --omit=dev

COPY proto ./proto
COPY src ./src

EXPOSE 50051

ENV GRPC_HOST=0.0.0.0
ENV GRPC_PORT=50051
ENV LOG_LEVEL=info

CMD ["npm", "start"]