import 'dotenv/config';
import app from './app';
import { prisma } from './config/prisma';
import { redis } from './config/redis';

const PORT = process.env.PORT || 3000;

async function bootstrap() {
  try {
    // Test database connection
    await prisma.$connect();
    console.log('PostgreSQL connected');

    // Test Redis connection
    await redis.ping();
    console.log('Redis connected');

    app.listen(PORT, () => {
      console.log(`Server running on port ${PORT}`);
      console.log(`Environment: ${process.env.NODE_ENV || 'development'}`);
    });
  } catch (error) {
    console.error('Failed to start server:', error);
    process.exit(1);
  }
}

// Graceful shutdown
process.on('SIGTERM', async () => {
  console.log('Server shutting down...');
  await prisma.$disconnect();
  redis.disconnect();
  process.exit(0);
});

bootstrap();
