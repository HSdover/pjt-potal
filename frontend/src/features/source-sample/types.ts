export type SourceSampleItem = {
  sampleId: number;
  institutionName: string;
  institutionType: string;
  sidoName: string;
  sigunguName: string;
  specialtyName: string;
  openedDate: string;
};

// [7. 목록 조회 표준 계약] 원천 샘플 목록 검색조건 타입이다.
export type SourceSampleSearchFilter = {
  region?: string;
  keyword?: string;
};
