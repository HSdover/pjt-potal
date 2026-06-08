import { ElMessageBox } from "element-plus";

type ConfirmDialogKind = "save" | "update" | "delete";

type ConfirmDialogOptions = {
  kind: ConfirmDialogKind;
  message: string;
  title?: string;
  confirmButtonText?: string;
};

const defaults: Record<ConfirmDialogKind, Required<Pick<ConfirmDialogOptions, "title" | "confirmButtonText">>> = {
  save: {
    title: "저장 확인",
    confirmButtonText: "저장",
  },
  update: {
    title: "수정 확인",
    confirmButtonText: "수정",
  },
  delete: {
    title: "삭제 확인",
    confirmButtonText: "삭제",
  },
};

export async function confirmDialog(options: ConfirmDialogOptions) {
  const preset = defaults[options.kind];

  try {
    await ElMessageBox.confirm(options.message, options.title ?? preset.title, {
      confirmButtonText: options.confirmButtonText ?? preset.confirmButtonText,
      cancelButtonText: "취소",
      closeOnClickModal: false,
      distinguishCancelAndClose: true,
      type: options.kind === "delete" ? "warning" : "info",
    });
    return true;
  } catch (error) {
    if (error === "cancel" || error === "close") {
      return false;
    }
    throw error;
  }
}

export function confirmSave(message = "저장하시겠습니까?") {
  return confirmDialog({ kind: "save", message });
}

export function confirmUpdate(message = "수정하시겠습니까?") {
  return confirmDialog({ kind: "update", message });
}

export function confirmDelete(message = "삭제하시겠습니까?") {
  return confirmDialog({ kind: "delete", message });
}
