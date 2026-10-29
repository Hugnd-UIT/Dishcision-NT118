import express from 'express';
import cors from 'cors';
import helmet from 'helmet';
import compression from 'compression';
import morgan from 'morgan';
import { rateLimit } from 'express-rate-limit';

import authRoutes from './routes/auth.routes';
import userRoutes from './routes/user.routes';
import preferenceRoutes from './routes/preference.routes';
import postRoutes from './routes/post.routes';
import commentRoutes from './routes/comment.routes';
import reviewRoutes from './routes/review.routes';
import foodDecisionRoutes from './routes/food-decision.routes';
import aiRoutes from './routes/ai.routes';
import socialRoutes from './routes/social.routes';
import searchRoutes from './routes/search.routes';
import notificationRoutes from './routes/notification.routes';
import reportRoutes from './routes/report.routes';
import adminRoutes from './routes/admin.routes';
import helpRoutes from './routes/help.routes';
import extraRoutes from './routes/extra.routes';

import {
  getBlockedAccounts,
  unblockAccount,
  getRestrictedAccounts,
  unrestrictAccount,
  getPrivacySettings,
  updatePrivacySettings,
} from './controllers/user.controller';
import {
  getSessions,
  revokeSession,
  get2FAStatus,
  disable2FA,
  verify2FA,
} from './controllers/auth.controller';
import { createReport } from './controllers/report.controller';
import { getTicketDetail, createTicket } from './controllers/help.controller';
import { optionalAuth } from './middlewares/auth.middleware';
import { errorHandler } from './middlewares/error.middleware';
import { notFound } from './middlewares/not-found.middleware';

const app = express();

// ── Security & Performance ───────────────
app.use(helmet());
app.use(compression());
app.use(cors({
  origin: process.env.CORS_ORIGIN?.split(',') || '*',
  credentials: true,
}));

// ── Rate Limiting ─────────────────────────
app.use(rateLimit({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: 500,
  standardHeaders: true,
  legacyHeaders: false,
}));

// ── Body Parsing ──────────────────────────
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true }));

// ── Logging ───────────────────────────────
if (process.env.NODE_ENV !== 'test') {
  app.use(morgan('combined'));
}

// ── Health Check ──────────────────────────
app.get('/health', (_req, res) => {
  res.json({ status: 'ok', timestamp: new Date().toISOString() });
});

// ── API Routes ────────────────────────────
const API_PREFIX = '/api/v1';

app.use(`${API_PREFIX}/auth`, authRoutes);
app.use(`${API_PREFIX}/users`, userRoutes);
app.use(`${API_PREFIX}/preferences`, preferenceRoutes);
app.use(`${API_PREFIX}/posts`, postRoutes);
app.use(`${API_PREFIX}/comments`, commentRoutes);
app.use(`${API_PREFIX}/reviews`, reviewRoutes);
app.use(`${API_PREFIX}/food-decision`, foodDecisionRoutes);
app.use(`${API_PREFIX}/ai`, aiRoutes);
app.use(`${API_PREFIX}`, socialRoutes);
app.use(`${API_PREFIX}/search`, searchRoutes);
app.use(`${API_PREFIX}/notifications`, notificationRoutes);
app.use(`${API_PREFIX}/reports`, reportRoutes);
app.use(`${API_PREFIX}/admin`, adminRoutes);
app.use(`${API_PREFIX}/help`, helpRoutes);
app.use(`${API_PREFIX}`, extraRoutes);

// ── Mobile App Compatibility Aliases ─────
// (Cho phép mobile-app gọi các đường dẫn ngắn mà không bị 404)
app.get(`${API_PREFIX}/accounts/blocked`, optionalAuth, getBlockedAccounts);
app.post(`${API_PREFIX}/accounts/unblock`, optionalAuth, unblockAccount);
app.get(`${API_PREFIX}/accounts/restricted`, optionalAuth, getRestrictedAccounts);
app.post(`${API_PREFIX}/accounts/unrestrict`, optionalAuth, unrestrictAccount);

app.get(`${API_PREFIX}/privacy`, optionalAuth, getPrivacySettings);
app.post(`${API_PREFIX}/privacy/update`, optionalAuth, updatePrivacySettings);

app.get(`${API_PREFIX}/sessions`, optionalAuth, getSessions);
app.post(`${API_PREFIX}/sessions/revoke`, optionalAuth, revokeSession);

app.get(`${API_PREFIX}/2fa/status`, optionalAuth, get2FAStatus);
app.post(`${API_PREFIX}/2fa/verify`, optionalAuth, verify2FA);
app.post(`${API_PREFIX}/2fa/disable`, optionalAuth, disable2FA);

app.post(`${API_PREFIX}/report`, optionalAuth, (req, res) => {
  if (!req.body.reason) req.body.reason = 'Other';
  createReport(req, res);
});

app.get(`${API_PREFIX}/help/ticket`, optionalAuth, (req, res) => {
  req.params.ticketId = (req.query.ticketId as string) || '1';
  getTicketDetail(req, res);
});
app.post(`${API_PREFIX}/help/ticket`, optionalAuth, createTicket);

// ── Error Handling ────────────────────────
app.use(notFound);
app.use(errorHandler);

export default app;
