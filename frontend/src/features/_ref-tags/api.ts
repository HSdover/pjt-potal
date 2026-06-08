import { http } from "@/shared/api/http";

export type ReferenceAttachment = {
  attachmentId: string;
  fileName: string;
  size: number;
  contentType: string;
  downloadUrl: string;
};

export function uploadReferenceAttachment(file: File): Promise<ReferenceAttachment> {
  const formData = new FormData();
  formData.append("file", file);
  return http.upload<ReferenceAttachment>("/api/_ref/attachments/sample", formData);
}
