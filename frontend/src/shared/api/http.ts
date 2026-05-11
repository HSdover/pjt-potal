import { ApiError } from "./api-error";

type HttpParams = Record<string, unknown>;

type HttpOptions = {
  params?: HttpParams;
  headers?: HeadersInit;
};

function appendParams(url: URL, params?: HttpParams) {
  if (!params) {
    return;
  }

  Object.entries(params).forEach(([key, value]) => {
    if (value === undefined || value === null || value === "") {
      return;
    }

    if (Array.isArray(value) || typeof value === "object") {
      url.searchParams.set(key, JSON.stringify(value));
      return;
    }

    url.searchParams.set(key, String(value));
  });
}

async function parseResponse<T>(response: Response): Promise<T> {
  const contentType = response.headers.get("content-type") ?? "";
  const isJson = contentType.includes("application/json");
  const body = isJson ? await response.json() : await response.text();

  if (!response.ok) {
    const message =
      typeof body === "object" && body && "message" in body
        ? String(body.message)
        : "API 요청 처리 중 오류가 발생했습니다.";

    throw new ApiError(message, {
      status: response.status,
      details: body,
    });
  }

  return body as T;
}

// [8. 공통 API 클라이언트] API 호출, JSON 파싱, 공통 오류 변환을 한 곳으로 모은다.
export const http = {
  async get<T>(path: string, options: HttpOptions = {}) {
    const url = new URL(path, window.location.origin);
    appendParams(url, options.params);

    const response = await fetch(url.pathname + url.search, {
      method: "GET",
      headers: {
        Accept: "application/json",
        ...options.headers,
      },
    });

    return parseResponse<T>(response);
  },
};
