export type MetaType = "STRUCTURED" | "FILE" | "SEMI_STRUCTURED";

export type MetaSearchType = "BUSINESS" | "TECHNICAL" | "FILE" | "OWNER" | "SECURITY" | "STORAGE";

export type IntegratedMetaSearchFilter = {
  metaType: MetaType;
  searchTypes: MetaSearchType[];
  keyword?: string;
};

export type IntegratedMetaItem = {
  metaId: string;
  metaType: MetaType;
  metaName: string;
  assetKind: string;
  sourceSystem: string;
  ownerDepartment: string;
  securityLevel: string;
  searchTypes: MetaSearchType[];
  updatedAt: string;
};

export type MetaKeyValue = {
  key: string;
  value: string;
};

export type MetaSection = {
  sectionId: string;
  title: string;
  fields: MetaKeyValue[];
};

export type StructuredColumnMeta = {
  ordinal: number;
  columnName: string;
  dataType: string;
  nullable: string;
  keyType: string;
  securityLevel: string;
  description: string;
};

export type IntegratedMetaDetail = {
  metaId: string;
  metaType: MetaType;
  metaName: string;
  sections: MetaSection[];
  columns: StructuredColumnMeta[];
};
