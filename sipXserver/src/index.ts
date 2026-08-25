import express from 'express';

const app = express();
const port = Number(process.env.PORT ?? 3000);

app.use(express.json());

app.get('/health', (_req, res) => {
  res.json({ ok: true, service: 'sipxserver' });
});

app.get('/dao', (_req, res) => {
  res.json({
    message: 'DAO API ready',
  });
});

app.listen(port, () => {
  console.log(`sipXserver listening on http://localhost:${port}`);
});
