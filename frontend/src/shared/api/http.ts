import { ApiError } from "./api-error";

// BFF SAML login entry path. It must match the registration id 'knox'.
const LOGIN_ENTRY_PATH = import.meta.env.VITE_LOGIN_ENTRY_PATH ?? "/saml2/authenticate/knox";
let redirectingToLogin = false;

function redirectToLogin() {
  if (redirectingToLogin) {
    return;
  }
  redirectingToLogin = true;
  window.location.href = LOGIN_ENTRY_PATH;
}

type HttpParams = Record<string, unknown>;

type HttpOptions = {
  params?: HttpParams;
  headers?: HeadersInit;
};

type HttpBodyOptions = {
  headers?: HeadersInit;
};

type ErrorBody = {
  message?: unknown;
  detail?: unknown;
  title?: unknown;
  code?: unknown;
  fieldErrors?: unknown;
};

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null;
}

function readCookie(name: string) {
  return document.cookie
    .split("; ")
    .find((item) => item.startsWith(`${name}=`))
    ?.split("=")
    .slice(1)
    .join("=");
}

function withCsrfHeader(headers?: HeadersInit): HeadersInit {
  const csrfToken = readCookie("XSRF-TOKEN");
  if (!csrfToken) {
    return {
      ...headers,
    };
  }

  return {
    "X-XSRF-TOKEN": decodeURIComponent(csrfToken),
    ...headers,
  };
}

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

async function readResponseBody(response: Response) {
  if (response.status === 204) {
    return undefined;
  }

  const contentType = response.headers.get("content-type") ?? "";
  if (contentType.includes("application/json")) {
    return response.json();
  }

  const text = await response.text();
  return text === "" ? undefined : text;
}

function errorMessage(body: ErrorBody | string | undefined) {
  if (typeof body === "string") {
    return body || "API request failed.";
  }

  if (!body) {
    return "API request failed.";
  }

  const candidate = body.message ?? body.detail ?? body.title;
  return typeof candidate === "string" && candidate.trim() ? candidate : "API request failed.";
}

function errorCode(body: ErrorBody | string | undefined) {
  return isRecord(body) && typeof body.code === "string" ? body.code : undefined;
}

function fieldErrors(body: ErrorBody | string | undefined) {
  if (!isRecord(body) || !Array.isArray(body.fieldErrors)) {
    return [];
  }

  return body.fieldErrors
    .filter(isRecord)
    .map((fieldError) => ({
      field: typeof fieldError.field === "string" ? fieldError.field : "",
      message: typeof fieldError.message === "string" ? fieldError.message : "",
    }))
    .filter((fieldError) => fieldError.field || fieldError.message);
}

async function parseResponse<T>(response: Response): Promise<T> {
  const body = await readResponseBody(response);

  if (!response.ok) {
    if (response.status === 401) {
      redirectToLogin();
    }

    const requestId = response.headers.get("X-Request-Id") ?? (isRecord(body) && typeof body.requestId === "string" ? body.requestId : undefined);

    throw new ApiError(errorMessage(body), {
      status: response.status,
      code: errorCode(body),
      requestId,
      fieldErrors: fieldErrors(body),
      details: body,
    });
  }

  return body as T;
}

// Shared API client for JSON calls, common error handling, and CSRF headers.
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

  async post<T>(path: string, body?: unknown, options: HttpBodyOptions = {}) {
    const url = new URL(path, window.location.origin);

    const response = await fetch(url.pathname + url.search, {
      method: "POST",
      headers: {
        Accept: "application/json",
        "Content-Type": "application/json",
        ...withCsrfHeader(options.headers),
      },
      body: body === undefined ? undefined : JSON.stringify(body),
    });

    return parseResponse<T>(response);
  },

  async put<T>(path: string, body?: unknown, options: HttpBodyOptions = {}) {
    const url = new URL(path, window.location.origin);

    const response = await fetch(url.pathname + url.search, {
      method: "PUT",
      headers: {
        Accept: "application/json",
        "Content-Type": "application/json",
        ...withCsrfHeader(options.headers),
      },
      body: body === undefined ? undefined : JSON.stringify(body),
    });

    return parseResponse<T>(response);
  },

  async delete<T>(path: string, options: HttpBodyOptions = {}) {
    const url = new URL(path, window.location.origin);

    const response = await fetch(url.pathname + url.search, {
      method: "DELETE",
      headers: {
        Accept: "application/json",
        ...withCsrfHeader(options.headers),
      },
    });

    return parseResponse<T>(response);
  },
};
