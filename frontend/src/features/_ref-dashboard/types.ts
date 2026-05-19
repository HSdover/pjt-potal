export type RefDashboardKpi = {
  label: string;
  value: number;
  unit: string;
  trend: string;
};

export type RefDashboardTimeSeries = {
  labels: string[];
  values: number[];
};

export type RefDashboardCategoryBars = {
  labels: string[];
  values: number[];
};

export type RefDashboardStatusSlice = {
  name: string;
  value: number;
};

export type RefDashboardData = {
  kpis: RefDashboardKpi[];
  timeSeries: RefDashboardTimeSeries;
  categoryBars: RefDashboardCategoryBars;
  statusDonut: RefDashboardStatusSlice[];
};
