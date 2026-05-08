// TableDetailSampleView 전용 타입 영역
// 기준: 이 화면에서만 사용하는 데이터 구조는 화면명과 같은 .ts 파일에 둔다.
export type Entity = {
  assetId: string;
  physicalName: string;
  logicalName: string;
  ownerDept: string;
  rowCount: number;
  lastHarvestDate: string;
  securityLevel: string;
  hasPii: boolean;
  isAiAnalyzed: boolean;
  qualityScore: number;
};

export type ColumnInfo = {
  physicalName: string;
  logicalName: string;
  dataType: string;
  isPk: boolean;
  isNullable: boolean;
  mappedTerm: string | null;
};

// TableDetailSampleView 전용 상수 영역
// 기준: 템플릿과 직접 관련은 있지만 화면 로직을 읽기 위한 값은 .ts에서 이름을 부여해 관리한다.
export const commonStyleElementId = "table-detail-sample-common-style";
export const commonStyleHref = "/styles/common-style.css";

// TableDetailSampleView 전용 초기 데이터 영역
// 기준: 화면 전용 하드코딩 샘플 데이터는 API/store로 빼지 않고 대응 .ts에 둔다.
export const initialEntity: Entity = {
  assetId: "AST-2024-001",
  physicalName: "TB_CUST_MST",
  logicalName: "고객_마스터_기본",
  ownerDept: "디지털금융플랫폼부",
  rowCount: 1250430,
  lastHarvestDate: "2026-04-27 15:30",
  securityLevel: "1등급",
  hasPii: true,
  isAiAnalyzed: false,
  qualityScore: 98.5,
};

// TableDetailSampleView 전용 샘플 조회 영역
// 기준: 현재는 DB/API 연결 없이 JSP의 하드코딩 데이터를 보존하는 샘플 함수로 유지한다.
export function getSampleColumns(): ColumnInfo[] {
  return [
    {
      physicalName: "CUST_ID",
      logicalName: "고객식별자",
      dataType: "VARCHAR2(20)",
      isPk: true,
      isNullable: false,
      mappedTerm: "고객번호",
    },
    {
      physicalName: "CUST_NM",
      logicalName: "고객명",
      dataType: "VARCHAR2(50)",
      isPk: false,
      isNullable: false,
      mappedTerm: "성명",
    },
    {
      physicalName: "BIRTH_DT",
      logicalName: "생년월일",
      dataType: "VARCHAR2(8)",
      isPk: false,
      isNullable: true,
      mappedTerm: "생년월일",
    },
    {
      physicalName: "CUST_GRADE_CD",
      logicalName: "고객등급코드",
      dataType: "VARCHAR2(2)",
      isPk: false,
      isNullable: true,
      mappedTerm: "회원등급",
    },
    {
      physicalName: "REG_DTM",
      logicalName: "최초등록일시",
      dataType: "TIMESTAMP",
      isPk: false,
      isNullable: false,
      mappedTerm: null,
    },
  ];
}

// TableDetailSampleView 전용 브라우저 리소스 영역
// 기준: 이 샘플 화면에서만 필요한 외부 CSS는 화면 진입/이탈 시점에 .vue에서 호출해 붙이고 제거한다.
export function mountCommonStyle() {
  if (document.getElementById(commonStyleElementId)) {
    return;
  }

  const link = document.createElement("link");
  link.id = commonStyleElementId;
  link.rel = "stylesheet";
  link.href = commonStyleHref;
  document.head.appendChild(link);
}

export function unmountCommonStyle() {
  document.getElementById(commonStyleElementId)?.remove();
}
