export type Role = 'USER' | 'ADMIN';
export type ClearanceLevel = 'BASIC' | 'ADVANCED' | 'SPECIALIST';
export type EquipmentStatus = 'AVAILABLE' | 'IN_SERVICE' | 'RETIRED';
export type ReservationStatus = 'PENDING' | 'APPROVED' | 'ISSUED' | 'RETURNED';

export interface User {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  role: Role;
  clearanceLevel: ClearanceLevel;
}

export interface AuthResponse {
  token: string;
  expiresAt: string;
  user: User;
}

export interface Equipment {
  id: number;
  categoryId?: number;
  categoryName?: string;
  departmentId?: number;
  departmentName?: string;
  hazardClassId?: number;
  hazardClassName?: string;
  name: string;
  inventoryNumber: string;
  description?: string;
  status: EquipmentStatus;
  specs?: Record<string, any>;
}

export interface Reservation {
  id: number;
  userId: number;
  userName?: string;
  itemId: number;
  modelName?: string;
  inventoryNumber?: string;
  serialNumber?: string;
  startDate: string;
  endDate: string;
  status: ReservationStatus;
  createdAt: string;
  issuedAt?: string;
  returnedAt?: string;
  issueNote?: string;
  returnNote?: string;
}

export interface AvailabilityResponse {
  itemId: number;
  from: string;
  to: string;
  available: boolean;
  reason?: string;
}
