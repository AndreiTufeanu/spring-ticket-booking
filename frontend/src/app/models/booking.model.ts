export interface BookingDto {
  id: string;
  eventId: string;
  seatNumber: number;
  eventTitle: string;
  eventDate: string;
}

export interface CreateBookingDto {
  eventId: string;
  seatNumber: number;
}