export type RefBoardAttachment = {
  attachmentId: string;
  fileName: string;
  size: number;
  contentType: string;
  downloadUrl: string;
};

export type RefBoardItem = {
  id: number;
  title: string;
  category: string;
  writerName: string;
  content: string;
  attachment?: RefBoardAttachment | null;
  viewCount: number;
  createdAt: string;
  updatedAt: string;
};

export type RefBoardSearchFilter = {
  keyword?: string;
  category?: string;
};

export type RefBoardSaveRequest = {
  title: string;
  category: string;
  writerName: string;
  content: string;
  attachment?: RefBoardAttachment | null;
};
