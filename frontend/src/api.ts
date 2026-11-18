import type { AuthResponse, Equipment, Reservation, AvailabilityResponse, User } from './types';

const envUrl = (import.meta.env.VITE_API_URL || '').trim().replace(/\/+$/, '');
const API_BASE = envUrl
  ? (envUrl.endsWith('/api') ? envUrl : `${envUrl}/api`)
  : 'http://localhost:8080/api';

export function getToken(): string | null {
  return localStorage.getItem('token');
}

export function setAuth(data: AuthResponse) {
  localStorage.setItem('token', data.token);
  localStorage.setItem('user', JSON.stringify(data.user));
}

export function clearAuth() {
  localStorage.removeItem('token');
  localStorage.removeItem('user');
}

export function getStoredUser() {
  const user = localStorage.getItem('user');
  return user ? JSON.parse(user) : null;
}

async function request<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
  const token = getToken();
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(options.headers as Record<string, string>),
  };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const response = await fetch(`${API_BASE}${endpoint}`, {
    ...options,
    headers,
  });

  if (!response.ok) {
    let errorMsg = `Error ${response.status}: ${response.statusText}`;
    try {
      const errJson = await response.json();
      if (errJson.message) {
        errorMsg = errJson.message;
      }
    } catch {
      // ignore
    }
    throw new Error(errorMsg);
  }

  if (response.status === 204) {
    return {} as T;
  }

  return response.json();
}

export const api = {
  // Auth
  login: (data: { email: string; password: string }) =>
    request<AuthResponse>('/auth/login', {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  register: (data: {
    firstName: string;
    lastName: string;
    email: string;
    password: string;
  }) =>
    request<any>('/auth/register', {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  // Equipment
  listEquipment: (params?: { page?: number; size?: number; q?: string }) => {
    const query = new URLSearchParams();
    if (params?.page !== undefined) query.set('page', params.page.toString());
    if (params?.size !== undefined) query.set('size', params.size.toString());
    if (params?.q) query.set('q', params.q);
    const qs = query.toString();
    return request<{ content: Equipment[]; totalElements: number; totalPages: number }>(
      `/equipment${qs ? `?${qs}` : ''}`
    );
  },

  checkAvailability: (id: number, from: string, to: string) =>
    request<AvailabilityResponse>(`/equipment/${id}/availability?from=${from}&to=${to}`),

  // Reservations
  createReservation: (data: { itemId: number; startDate: string; endDate: string }) =>
    request<Reservation>('/reservations', {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  listMyReservations: (page = 0, size = 20) =>
    request<{ content: Reservation[]; totalElements: number }>(
      `/reservations/my?page=${page}&size=${size}`
    ),

  listAllReservations: (page = 0, size = 20) =>
    request<{ content: Reservation[]; totalElements: number }>(
      `/reservations?page=${page}&size=${size}`
    ),

  cancelReservation: (id: number) =>
    request<Reservation>(`/reservations/${id}/cancel`, {
      method: 'PATCH',
    }),

  approveReservation: (id: number) =>
    request<Reservation>(`/reservations/${id}/approve`, {
      method: 'PATCH',
    }),

  rejectReservation: (id: number) =>
    request<Reservation>(`/reservations/${id}/reject`, {
      method: 'PATCH',
    }),

  issueEquipment: (id: number, issueNote?: string) =>
    request<Reservation>(`/reservations/${id}/issue`, {
      method: 'PATCH',
      body: JSON.stringify({ issueNote }),
    }),

  returnEquipment: (id: number, returnNote?: string) =>
    request<Reservation>(`/reservations/${id}/return`, {
      method: 'PATCH',
      body: JSON.stringify({ returnNote }),
    }),

  // Users Management (Admin)
  listUsers: (page = 0, size = 50) =>
    request<{ content: User[]; totalElements: number }>(`/users?page=${page}&size=${size}`),

  changeUserClearance: (id: number, clearanceLevel: string) =>
    request<User>(`/users/${id}/clearance`, {
      method: 'PATCH',
      body: JSON.stringify({ clearanceLevel }),
    }),

  changeUserRole: (id: number, role: string) =>
    request<User>(`/users/${id}/role`, {
      method: 'PATCH',
      body: JSON.stringify({ role }),
    }),
};
