import { Router } from 'express';
import { getInbox, getTicketDetail, createTicket } from '../controllers/help.controller';
import { authenticate } from '../middlewares/auth.middleware';
import { asyncHandler } from '../utils/http';

const router = Router();

router.get('/inbox', authenticate, asyncHandler(getInbox));
router.get('/tickets/:ticketId', authenticate, asyncHandler(getTicketDetail));
router.post('/tickets', authenticate, asyncHandler(createTicket));

export default router;
