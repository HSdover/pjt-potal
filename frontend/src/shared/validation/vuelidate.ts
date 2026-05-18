import type { BaseValidation, ErrorObject, ValidationRule } from "@vuelidate/core";
import { helpers, maxLength } from "@vuelidate/validators";
import { unref } from "vue";

type ValidationField = Pick<BaseValidation, "$error" | "$errors">;

function messageText(message: ErrorObject["$message"]): string {
  return unref(message);
}

export function fieldError(field: ValidationField): string {
  if (!field.$error) {
    return "";
  }

  return field.$errors[0] ? messageText(field.$errors[0].$message) : "";
}

export function requiredText(label: string): ValidationRule {
  return helpers.withMessage(`${label}을(를) 입력하세요.`, (value: unknown) => {
    if (typeof value === "string") {
      return value.trim().length > 0;
    }

    return value !== null && value !== undefined && value !== "";
  });
}

export function maxLengthText(label: string, length: number): ValidationRule {
  return helpers.withMessage(`${label}은(는) ${length}자 이하로 입력하세요.`, maxLength(length));
}
