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

async function parseResponse<T>(response: Response): Promise<T> {
  const contentType = response.headers.get("content-type") ?? "";
  const isJson = contentType.includes("application/json");
  const body = isJson ? await response.json() : await response.text();

  if (!response.ok) {
    if (response.status === 401) {
      redirectToLogin();
    }

    const message =
      typeof body === "object" && body && "message" in body
        ? String(body.message)
        : "API request failed.";

    throw new ApiError(message, {
      status: response.status,
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
