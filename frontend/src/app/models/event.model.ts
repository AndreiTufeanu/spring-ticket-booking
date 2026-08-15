export interface EventDto {
    id: string;
    title: string;
    description: string;
    location: string;
    eventDate: string;
    totalSeats: number;
    availableSeats: number;
}

export interface CreateEventDto {
    title: string;
    description: string;
    location: string;
    eventDate: string;
    totalSeats: number;
}

export interface UpdateEventDto {
    title: string;
    description: string;
    location: string;
    eventDate: string;
}
