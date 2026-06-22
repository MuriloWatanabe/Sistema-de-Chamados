export interface DashboardStats {
  total: number;
  open: number;
  inProgress: number;
  resolved: number;
  closed: number;
  urgent: number;
}

export interface TicketCreateRequest {
  title: string;
  description: string;
  priority: number;
  requesterId?: number | null;
  assignedToId?: number | null;
}

export interface TicketUpdateRequest {
  status?: number | null;
  priority?: number | null;
  assignedToId?: number | null;
}

export interface CommentCreateRequest {
  comment: string;
}

export interface UserUpsertRequest {
  name: string;
  email: string;
  password?: string | null;
  role: number;
  active: boolean;
}
