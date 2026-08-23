import { CategoryDto } from './category.model';

export interface EventDto {
    id: string;
    title: string;
    description: string;
    location: string;
    eventDate: string;
    totalSeats: number;
    availableSeats: number;
    categories: CategoryDto[];
}

export interface CreateEventDto {
    title: string;
    description: string;
    location: string;
    eventDate: string;
    totalSeats: number;
    categoryIds: string[];
}

export interface UpdateEventDto {
    title: string;
    description: string;
    location: string;
    eventDate: string;
    categoryIds: string[];
}