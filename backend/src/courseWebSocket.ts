import WebSocket, { type RawData } from 'ws';

const COURSE_WEBSOCKET_URL = 'wss://8.229.22.124';

export function connectToCourseWebSocket(
  onPixelReceived: (data: RawData, isBinary: boolean) => void
): WebSocket {
  const socket = new WebSocket(COURSE_WEBSOCKET_URL);

  socket.on('open', () => {
    console.log('Connected to CPEN 321 WebSocket');
  });

  socket.on('message', (data, isBinary) => {
    console.log('Pixel received:', data.toString());

    // Immediately pass the original message to our relay
    onPixelReceived(data, isBinary);
  });

  socket.on('error', (error) => {
    console.error('Course WebSocket error:', error);
  });

  socket.on('close', (code, reason) => {
    console.log(
      `Course WebSocket closed: code=${code}, reason=${reason.toString()}`
    );
  });

  return socket;
}