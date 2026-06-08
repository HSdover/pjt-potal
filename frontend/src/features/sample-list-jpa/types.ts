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

export type SampleJpaExcelLargeExport = {
  jobId: string;
  status: "REQUESTED" | "RUNNING" | "COMPLETED" | "FAILED";
  totalRows: number;
  message: string;
  downloadUrl?: string;
};

export type SampleJpaExcelImportError = {
  rowIndex: number;
  column: string;
  message: string;
};

export type SampleJpaExcelImportResult = {
  totalRows: number;
  successRows: number;
  errorRows: number;
  errors: SampleJpaExcelImportError[];
};
