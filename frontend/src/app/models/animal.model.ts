export type Species = 'DOG' | 'CAT' | 'BIRD' | 'RABBIT' | 'HAMSTER' | 'REPTILE' | 'FISH' | 'OTHER';
export type Gender = 'MALE' | 'FEMALE' | 'UNKNOWN';

export interface Animal {
  id?: number;
  name: string;
  species: Species;
  breed?: string;
  dateOfBirth?: string;
  color?: string;
  gender?: Gender;
  ownerId?: number;
  ownerName?: string;
}

export const SPECIES_LIST: Species[] = ['DOG', 'CAT', 'BIRD', 'RABBIT', 'HAMSTER', 'REPTILE', 'FISH', 'OTHER'];
export const GENDER_LIST: Gender[] = ['MALE', 'FEMALE', 'UNKNOWN'];
