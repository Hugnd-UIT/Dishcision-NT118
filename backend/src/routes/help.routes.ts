import { Router } from 'express';
import { getInbox, getTicketDetail, createTicket } from '../controllers/help.controller';
import { authenticate } from '../middlewares/auth.middleware';

const router = Router();

router.get('/inbox', authenticate, getInbox);
router.get('/tickets/:ticketId', authenticate, getTicketDetail);
router.post('/tickets', authenticate, createTicket);

export default router;
