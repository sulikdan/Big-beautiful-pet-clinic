export interface Visit {
  id?: number;
  animalId?: number;
  animalName?: string;
  visitDate: string;
  reason?: string;
  /** Height in centimetres */
  height?: number;
  /** Weight in kilograms */
  weight?: number;
  /** Age in years at time of visit */
  age?: number;
  vetName?: string;
  diagnosis?: string;
  treatment?: string;
}
