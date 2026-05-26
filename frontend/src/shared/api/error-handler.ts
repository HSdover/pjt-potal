import { ElMessage } from "element-plus";
import { ApiError } from "./api-error";

export function isUserCancel(error: unknown) {
  return error === "cancel" || error === "close";
}

export function toErrorMessage(error: unknown, fallbackMessage: string) {
  if (error instanceof ApiError) {
    const fieldError = error.fieldErrors[0];
    const detail = fieldError?.message
      ? `${fieldError.field ? `${fieldError.field}: ` : ""}${fieldError.message}`
      : "";
    const requestId = error.requestId ? ` 요청ID: ${error.requestId}` : "";
    const message = error.message || fallbackMessage;
    return [message, detail, requestId].filter(Boolean).join(" ");
  }

  if (error instanceof Error && error.message) {
    return error.message;
  }

  return fallbackMessage;
}

export function handleApiError(error: unknown, fallbackMessage: string) {
  if (isUserCancel(error)) {
    return;
  }

  if (error instanceof ApiError && error.status === 401) {
    return;
  }

  ElMessage.error(toErrorMessage(error, fallbackMessage));
}

export function handleGlobalError(error: unknown, fallbackMessage = "처리 중 오류가 발생했습니다.") {
  handleApiError(error, fallbackMessage);
}

export function logClientError(error: unknown, context: string) {
  const requestId = error instanceof ApiError && error.requestId ? ` requestId=${error.requestId}` : "";
  console.warn(`${context}${requestId}`, error);
}
