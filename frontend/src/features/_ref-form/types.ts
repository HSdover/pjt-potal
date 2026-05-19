export type RefFormItem = {
  id: number;
  name: string;
  category: string;
  description: string;
  targetDate: string | null;
  priority: string;
};

export type RefFormSaveRequest = {
  name: string;
  category: string;
  description: string;
  targetDate: string | null;
  priority: string;
};
