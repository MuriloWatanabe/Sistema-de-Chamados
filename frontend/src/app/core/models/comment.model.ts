export interface Comment {
  id: number;
  ticketId: number;
  user: { id: number; name: string; role: number };
  comment: string;
  createdAt: string;
}
