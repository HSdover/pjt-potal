export type ApiErrorOptions = {
  status?: number;
  code?: string;
  requestId?: string;
  fieldErrors?: ApiFieldError[];
  details?: unknown;
};

export type ApiFieldError = {
  field: string;
  message: string;
};

// [8. 공통 API 클라이언트] 화면별 fetch 에러 처리를 표준화하기 위한 공통 오류 타입이다.
export class ApiError extends Error {
  readonly status?: number;
  readonly code?: string;
  readonly requestId?: string;
  readonly fieldErrors: ApiFieldError[];
  readonly details?: unknown;

  constructor(message: string, options: ApiErrorOptions = {}) {
    super(message);
    this.name = "ApiError";
    this.status = options.status;
    this.code = options.code;
    this.requestId = options.requestId;
    this.fieldErrors = options.fieldErrors ?? [];
    this.details = options.details;
  }
}
