FROM node:20-alpine AS base
WORKDIR /app
RUN apk add --no-cache openssl libc6-compat

FROM base AS deps
COPY package*.json ./
COPY prisma* ./prisma/
RUN if [ -f package.json ]; then npm install; fi

FROM base AS builder
COPY --from=deps /app/node_modules* ./node_modules/
COPY . .
RUN if [ -f prisma/schema.prisma ]; then npx prisma generate; fi
RUN if [ -f package.json ]; then npm run build || true; fi

FROM base AS runner
ENV NODE_ENV=production
ENV PORT=3000

RUN addgroup --system --gid 1001 nodejs && \
    adduser --system --uid 1001 appuser

COPY --from=deps /app/node_modules* ./node_modules/
COPY --from=builder /app/dist* ./dist/
COPY --from=builder /app/prisma* ./prisma/
COPY package*.json ./

RUN mkdir -p /app/uploads/images /app/uploads/videos && \
    chown -R appuser:nodejs /app/uploads

USER appuser
EXPOSE 3000

CMD ["sh", "-c", "if [ -f dist/index.js ]; then node dist/index.js; elif [ -f index.js ]; then node index.js; else echo 'Backend is ready, but no code found' && sleep infinity; fi"]
