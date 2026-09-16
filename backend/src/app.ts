import express, { type Express } from 'express';

export function createApp(): Express {
  const app = express();

  // Existing health-check API
  app.get('/health', (_req, res) => {
    res.json({ status: 'ok' });
  });

  // M1: Return the server public IP address
  app.get('/server-ip', (_req, res) => {
    const serverIp = process.env.SERVER_PUBLIC_IP ?? 'Not configured yet';

    res.json({
      ip: serverIp,
    });
  });

  // M1: Return the server local time
  app.get('/server-time', (_req, res) => {
    const now = new Date();

    const hours = String(now.getHours()).padStart(2, '0');
    const minutes = String(now.getMinutes()).padStart(2, '0');
    const seconds = String(now.getSeconds()).padStart(2, '0');

    const offsetMinutes = -now.getTimezoneOffset();
    const offsetSign = offsetMinutes >= 0 ? '+' : '-';
    const absoluteOffset = Math.abs(offsetMinutes);

    const offsetHours = String(
      Math.floor(absoluteOffset / 60)
    ).padStart(2, '0');

    const offsetRemainingMinutes = String(
      absoluteOffset % 60
    ).padStart(2, '0');

    const formattedTime =
      `${hours}:${minutes}:${seconds} ` +
      `GMT${offsetSign}${offsetHours}:${offsetRemainingMinutes}`;

    res.json({
      time: formattedTime,
    });
  });

  // M1: Return your first and last name
  app.get('/name', (_req, res) => {
    res.json({
      firstName: 'Terry',
      lastName: 'Teh',
    });
  });

  // Keep this LAST: anything not matched above returns 404
  app.use((_req, res) => {
    res.status(404).json({ error: 'Not Found' });
  });

  return app;
}