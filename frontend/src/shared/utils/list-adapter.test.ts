import { describe, expect, it } from "vitest";
import { toListResponse } from "./list-adapter";
import type { ListRequest } from "@/shared/types/list";

type Row = {
  id: number;
  name: string;
  category: string;
};

function request(keyword: string): ListRequest<{ keyword: string }> {
  return {
    pageNo: 1,
    pageSize: 20,
    sort: [],
    filters: { keyword },
  };
}

describe("toListResponse", () => {
  const rows: Row[] = [
    { id: 100, name: "alpha", category: "source" },
    { id: 200, name: "beta", category: "sample" },
  ];

  it("uses all row values for keyword matching by default", () => {
    const result = toListResponse(rows, request("100"));

    expect(result.totalCount).toBe(1);
    expect(result.rows[0].name).toBe("alpha");
  });

  it("limits keyword matching to configured search fields", () => {
    const result = toListResponse(rows, request("100"), {
      searchFields: ["name", "category"],
    });

    expect(result.totalCount).toBe(0);
    expect(result.rows).toEqual([]);
  });

  it("matches keyword against configured search fields", () => {
    const result = toListResponse(rows, request("sample"), {
      searchFields: ["name", "category"],
    });

    expect(result.totalCount).toBe(1);
    expect(result.rows[0].name).toBe("beta");
  });
});
