import { createApp } from './app';
import { env } from './config/env';
import { connectToCourseWebSocket } from './courseWebSocket';
import WebSocket, { WebSocketServer } from 'ws';

const app = createApp();

const server = app.listen(env.port, () => {
  console.log(`Server listening on port ${env.port}`);
});

// Our own WebSocket server for Android clients
const webSocketServer = new WebSocketServer({
  server,
  path: '/live',
});

webSocketServer.on('connection', (socket) => {
  console.log('Frontend connected to our WebSocket');

  socket.on('close', () => {
    console.log('Frontend disconnected from our WebSocket');
  });
});

// Connect to the course WebSocket
const courseSocket = connectToCourseWebSocket((data, isBinary) => {

  // Relay every received pixel to all connected frontend clients
  for (const client of webSocketServer.clients) {
    if (client.readyState === WebSocket.OPEN) {
      client.send(data, { binary: isBinary });
    }
  }
});

for (const signal of ['SIGINT', 'SIGTERM'] as const) {
  process.on(signal, () => {
    courseSocket.close();
    webSocketServer.close();

    server.close(() => {
      process.exit(0);
    });
  });
}