export type WorkOrderStatus =
  | 'RECEIVED'
  | 'IN_DIAGNOSIS'
  | 'PENDING_INTERNAL_APPROVAL'
  | 'PENDING_APPROVAL'
  | 'AWAITING_PARTS_RELEASE'
  | 'IN_EXECUTION'
  | 'FINALIZED'
  | 'DELIVERED'
  | 'CANCELLED'

export interface LoginRequest { username: string; password: string }
export interface LoginResponse { accessToken: string; tokenType: string; expiresInSeconds: number }

export interface CustomerRequest { taxId: string; name: string; email?: string; phone?: string }
export interface CustomerResponse { id: string; taxId: string; name: string; email: string | null; phone: string | null }

export interface VehicleRequest { customerId: string; plate: string; brand: string; model: string; year: number }
export interface VehicleResponse { id: string; customerId: string; plate: string; brand: string; model: string; year: number }

export interface CatalogServiceRequest { name: string; description?: string; priceCents: number; estimatedMinutes: number }
export interface CatalogServiceResponse { id: string; name: string; description: string | null; priceCents: number; estimatedMinutes: number }

export interface PartRequest { code: string; name: string; priceCents: number; stockQuantity: number; replenishmentPoint?: number }
export interface PartResponse { id: string; code: string; name: string; priceCents: number; stockQuantity: number; replenishmentPoint: number | null }
export interface PartAvailabilityResponse {
  partId: string
  code: string
  name: string
  priceCents: number
  stockQuantity: number
  pendingReservedQuantity: number
  availableQuantity: number
  replenishmentPoint: number | null
}
export interface GoodsReceiptRequest { quantity: number; reference?: string }

export interface WorkOrderServiceLineRequest { catalogServiceId: string; quantity: number }
export interface WorkOrderPartLineRequest { partId: string; quantity: number }
export interface UpdateDiagnosisPlanRequest {
  services: WorkOrderServiceLineRequest[]
  parts: WorkOrderPartLineRequest[]
  diagnosisNotes?: string
}
export interface CreateWorkOrderRequest {
  customerTaxId: string; customerName: string; customerEmail?: string; customerPhone?: string
  plate: string; vehicleBrand: string; vehicleModel: string; vehicleYear: number
  services: WorkOrderServiceLineRequest[]
  parts: WorkOrderPartLineRequest[]
}

export interface WorkOrderServiceLineResponse { catalogServiceId: string; serviceName: string | null; quantity: number; unitPriceCents: number }
export interface WorkOrderPartLineResponse { partId: string; partName: string | null; quantity: number; unitPriceCents: number }
export interface WorkOrderResponse {
  id: string; trackingCode: string; customerId: string; vehicleId: string
  diagnosisNotes: string | null
  status: WorkOrderStatus; servicesTotalCents: number; partsTotalCents: number; totalCents: number
  services: WorkOrderServiceLineResponse[]; parts: WorkOrderPartLineResponse[]
}

export interface WorkOrderTrackingResponse {
  trackingCode: string; status: WorkOrderStatus; totalCents: number
  vehiclePlate: string; maskedCustomerTaxId: string
}

export interface AverageServiceTimeResponse { catalogServiceId: string; serviceName: string; averageMinutes: number; sampleCount: number }
export interface PartReservationResponse { id: string; workOrderId: string; partId: string; partName: string; quantity: number; status: string }
export interface LowStockAlertResponse { partId: string; code: string; name: string; stockQuantity: number; replenishmentPoint: number | null; pendingReservedQuantity: number }
