export type SampleListJpaItem = {
  id: number;
  name: string;
  description: string;
};

export type SampleListJpaSearchFilter = {
  keyword?: string;
};

export type SampleListJpaSaveRequest = {
  name: string;
  description?: string;
};
