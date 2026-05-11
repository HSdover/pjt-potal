export type ApiErrorOptions = {
  status?: number;
  code?: string;
  details?: unknown;
};

// [8. 공통 API 클라이언트] 화면별 fetch 에러 처리를 표준화하기 위한 공통 오류 타입이다.
export class ApiError extends Error {
  readonly status?: number;
  readonly code?: string;
  readonly details?: unknown;

  constructor(message: string, options: ApiErrorOptions = {}) {
    super(message);
    this.name = "ApiError";
    this.status = options.status;
    this.code = options.code;
    this.details = options.details;
  }
}
