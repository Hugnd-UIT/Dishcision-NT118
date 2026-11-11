import { Router } from 'express';
import { createReport } from '../controllers/report.controller';
import { authenticate } from '../middlewares/auth.middleware';
import { asyncHandler } from '../utils/http';
import { validate, reportSchema } from '../middlewares/validate.middleware';

const router = Router();

router.post('/', authenticate, validate(reportSchema), asyncHandler(createReport));

export default router;
