export interface BookingDto {
  id: string;
  eventId: string;
  seatNumber: number;
  eventTitle: string;
  eventDate: string;
  location: string;
  description: string;
}

export interface CreateBookingDto {
  eventId: string;
}