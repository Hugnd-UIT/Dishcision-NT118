import Redis from 'ioredis';

export const redis = new Redis({
  host: process.env.REDIS_HOST || 'localhost',
  port: Number(process.env.REDIS_PORT) || 6379,
  password: process.env.REDIS_PASSWORD || undefined,
  maxRetriesPerRequest: 3,
  lazyConnect: true,
});

redis.on('error', (err) => {
  console.error('Redis error:', err);
});

// ── Cache helpers ─────────────────────────

export const CACHE_TTL = {
  NEWSFEED: 60,         // 1 minute
  USER_PROFILE: 300,    // 5 minutes
  PREFERENCES: 600,     // 10 minutes
  TRENDING: 300,        // 5 minutes
  POST_DETAIL: 120,     // 2 minutes
};

export async function getCache<T>(key: string): Promise<T | null> {
  const data = await redis.get(key);
  return data ? (JSON.parse(data) as T) : null;
}

export async function setCache(key: string, value: unknown, ttl: number): Promise<void> {
  await redis.set(key, JSON.stringify(value), 'EX', ttl);
}

export async function delCache(key: string): Promise<void> {
  await redis.del(key);
}

export async function delCacheByPattern(pattern: string): Promise<void> {
  const keys = await redis.keys(pattern);
  if (keys.length > 0) {
    await redis.del(...keys);
  }
}
